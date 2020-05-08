 /**
  * 
  */
 
 package de.uni_potsdam.hpi.fgnaumann.lsdd;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 
 import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
 import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
 import eu.stratosphere.pact.common.contract.CoGroupContract;
 import eu.stratosphere.pact.common.contract.FileDataSink;
 import eu.stratosphere.pact.common.contract.FileDataSource;
 import eu.stratosphere.pact.common.contract.GenericDataSink;
 import eu.stratosphere.pact.common.contract.MapContract;
 import eu.stratosphere.pact.common.contract.MatchContract;
 import eu.stratosphere.pact.common.contract.ReduceContract;
 import eu.stratosphere.pact.common.io.RecordInputFormat;
 import eu.stratosphere.pact.common.io.RecordOutputFormat;
 import eu.stratosphere.pact.common.plan.Plan;
 import eu.stratosphere.pact.common.plan.PlanAssembler;
 import eu.stratosphere.pact.common.plan.PlanAssemblerDescription;
 import eu.stratosphere.pact.common.stubs.CoGroupStub;
 import eu.stratosphere.pact.common.stubs.Collector;
 import eu.stratosphere.pact.common.stubs.MapStub;
 import eu.stratosphere.pact.common.stubs.MatchStub;
 import eu.stratosphere.pact.common.stubs.ReduceStub;
 import eu.stratosphere.pact.common.type.PactRecord;
 import eu.stratosphere.pact.common.type.base.PactInteger;
 import eu.stratosphere.pact.common.type.base.PactList;
 import eu.stratosphere.pact.common.type.base.PactString;
 import eu.stratosphere.pact.common.type.base.parser.DecimalTextIntParser;
 import eu.stratosphere.pact.common.type.base.parser.VarLengthStringParser;
 
 /**
  * A Stratosphere-Pact-Implementation of "A fast approach for parallel
  * deduplication on multicore processors" - Guilherme Dal Bianco et al.
  * 
  * @author fabian.tschirschnitz@student.hpi.uni-potsdam.de
  * 
  */
 public class MultiBlocking implements PlanAssembler, PlanAssemblerDescription {
 	private static final int TRACKS_FIELD = 9;
 	public static final int BLOCKING_KEY_FIELD = 10;
 	public static final int BLOCKING_ID_FIELD = 11;
 	private static final int COUNT_FIELD = 12;
 	public static final int THRESHOLD = 2;
 	private static final int DUPLICATE_ID_1_FIELD = 0;
 	private static final int DUPLICATE_ID_2_FIELD = 1;
 
 	@Override
 	public Plan getPlan(final String... args) {
 		// parse program parameters
 		/*
 		 * 4 file:///home/fabian/lsdd/data/mini.csv
 		 * file:///home/fabian/lsdd/data/freedb_tracks.csv
 		 * file:///home/fabian/lsdd/out
 		 */
 		final int noSubtasks = (args.length > 0 ? Integer.parseInt(args[0]) : 1);
 		final String inputFileDiscs = (args.length > 1 ? args[1] : "");
 		final String inputFileTracks = (args.length > 2 ? args[2] : "");
 		final String output = (args.length > 3 ? args[3] : "");
 		final String inputFileGold = (args.length > 4 ? args[4] : "");
 
 		// create DataSourceContract for discs input
 		// disc_id;freedbdiscid;"artist_name";"disc_title";"genre_title";"disc_released";disc_tracks;disc_seconds;"disc_language"
 		// 7;727827;"Tenacious D";"Tenacious D";"Humour";"2001";19;2843;"eng"
 		FileDataSource discs = new FileDataSource(DiscsInputFormat.class,
 				inputFileDiscs, "Discs");
 		DiscsInputFormat.configureRecordFormat(discs).recordDelimiter('\n')
 				.fieldDelimiter(';').field(DecimalTextIntParser.class, 0) // disc_id
 				.field(DecimalTextIntParser.class, 1) // freedbdiscid
 				.field(VarLengthStringParser.class, 2) // "artist_name"
 				.field(VarLengthStringParser.class, 3) // "disc_title"
 				.field(VarLengthStringParser.class, 4) // "genre_title"
 				.field(VarLengthStringParser.class, 5) // "disc_released"
 				.field(DecimalTextIntParser.class, 6) // disc_tracks
 				.field(DecimalTextIntParser.class, 7) // disc_seconds
 				.field(VarLengthStringParser.class, 8); // "disc_language"
 
 		// create DataSourceContract for tracks input
 		// disc_id;track_number;"track_title";"artist_name";track_seconds
 		// 2;1;"Intro+Chor Der Kriminalbeamten";"Kottans Kapelle";115
 		FileDataSource tracks = new FileDataSource(RecordInputFormat.class,
 				inputFileTracks, "Tracks");
 		RecordInputFormat.configureRecordFormat(tracks).recordDelimiter('\n')
 				.fieldDelimiter(';').field(DecimalTextIntParser.class, 0) // disc_id
 				.field(DecimalTextIntParser.class, 1) // freedbdiscid
 				.field(VarLengthStringParser.class, 2) // "track_title"
 				.field(VarLengthStringParser.class, 3) // "artist_name"
 				.field(DecimalTextIntParser.class, 4); // track_seconds
 
 		// create DataSourceContract for gold standard input
 		// disc_id1;disc_id2
 		// 13;3163
 		FileDataSource gold = new FileDataSource(RecordInputFormat.class,
 				inputFileGold, "GoldStandard");
 		RecordInputFormat.configureRecordFormat(gold).recordDelimiter('\n')
 				.fieldDelimiter(';')
 				.field(DecimalTextIntParser.class, DUPLICATE_ID_1_FIELD) // disc_id1
 				.field(DecimalTextIntParser.class, DUPLICATE_ID_2_FIELD); // disc_id1
 
 		CoGroupContract coGrouper = CoGroupContract
 				.builder(CoGroupCDsWithTracks.class, PactInteger.class, 0, 0)
 				.name("Group CDs with Tracks").build();
 
 		coGrouper.setFirstInput(discs);
 		coGrouper.setSecondInput(tracks);
 
 		// contracts
 		MapContract firstBlockingStepMapper = MapContract
 				.builder(FirstBlockingStep.class).input(coGrouper)
 				.name("first blocking step").build();
 
 		ReduceContract countStepReducer = new ReduceContract.Builder(
				CountStep.class, PactString.class, BLOCKING_KEY_FIELD)
 				.input(firstBlockingStepMapper).name("count records step")
 				.build();
 
 		MapContract unbalancedBlockFilterMapper = MapContract
 				.builder(UnbalancedBlockFilterStep.class)
 				.input(countStepReducer).name("filter unbalanced blocks step")
 				.build();
 
 		MapContract balancedBlockFilterMapper = MapContract
 				.builder(BalancedBlockFilterStep.class).input(countStepReducer)
 				.name("filter balanced blocks step").build();
 
 		ReduceContract matchStepReducerBalanced = new ReduceContract.Builder(
				MatchStep.class, PactString.class, BLOCKING_KEY_FIELD)
 				.input(unbalancedBlockFilterMapper).name("match step balanced")
 				.build();
 
 		ReduceContract secondBlockingStep = new ReduceContract.Builder(
				SecondBlockingStep.class, PactString.class, BLOCKING_KEY_FIELD)
 				.input(balancedBlockFilterMapper).name("second blocking step")
 				.build();
 
 		ReduceContract matchStepReducerUnbalanced = new ReduceContract.Builder(
				MatchStep.class, PactString.class, BLOCKING_KEY_FIELD)
 				.input(secondBlockingStep).name("match step unbalanced")
 				.build();
 
 		ReduceContract unionStep = new ReduceContract.Builder(UnionStep.class,
 				PactString.class, DUPLICATE_ID_1_FIELD)
 				.keyField(PactInteger.class, DUPLICATE_ID_2_FIELD)
 				.input(matchStepReducerUnbalanced).name("match step").build();
 		unionStep.addInput(matchStepReducerBalanced);
 
 		MatchContract validatorStep = MatchContract
 				.builder(ValidatorStep.class, PactInteger.class,
 						DUPLICATE_ID_1_FIELD, DUPLICATE_ID_1_FIELD)
 				.input1(gold)
 				.input2(unionStep)
 				.name("validator step")
 				.keyField(PactInteger.class, DUPLICATE_ID_2_FIELD,
 						DUPLICATE_ID_2_FIELD).build();
 
 		// file output result
 		FileDataSink outResult = new FileDataSink(RecordOutputFormat.class,
 				output + "/result", unionStep, "Output Result");
 		RecordOutputFormat.configureRecordFormat(outResult)
 				.recordDelimiter('\n').fieldDelimiter(' ').lenient(true)
 				.field(PactInteger.class, 0).field(PactInteger.class, 1);
 
 		// file output tp
 		FileDataSink outTruePositives = new FileDataSink(
 				RecordOutputFormat.class, output + "/tp", validatorStep,
 				"Output True Positives");
 		RecordOutputFormat.configureRecordFormat(outTruePositives)
 				.recordDelimiter('\n').fieldDelimiter(' ').lenient(true)
 				.field(PactInteger.class, 0).field(PactInteger.class, 1);
 
 		// assemble the PACT plan
 		Collection<GenericDataSink> sinks = new HashSet<GenericDataSink>();
 		sinks.add(outResult);
 		sinks.add(outTruePositives);
 		Plan plan = new Plan(sinks, "MultiBlocking");
 		plan.setDefaultParallelism(noSubtasks);
 		return plan;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String getDescription() {
 		return "Parameters: [noSubStasks], [discs], [tracks], [output]";
 	}
 
 	/**
 	 * Mapper that applies the blocking functions to each record
 	 * 
 	 * @author fabian.tschirschnitz@student.hpi.uni-potsdam.de
 	 * 
 	 */
 	public static class FirstBlockingStep extends MapStub {
 
 		@Override
 		public void map(PactRecord record, Collector<PactRecord> collector) {
 			for (BlockingFunction bf : BlockingFunction.blockingFuntions) {
 				collector.collect(bf.copyWithBlockingKey(record));
 			}
 		}
 
 	}
 
 	/**
 	 * Reducer that expands the blocking keys of the records in unbalanced
 	 * blocks
 	 * 
 	 * @author fabian.tschirschnitz@student.hpi.uni-potsdam.de
 	 * 
 	 */
 	public static class SecondBlockingStep extends ReduceStub {
 		@Override
 		public void reduce(Iterator<PactRecord> records,
 				Collector<PactRecord> out) throws Exception {
 			while (records.hasNext()) {
 				PactRecord recordToExpand = records.next();
 				PactString appliedBlockingFunctionId = recordToExpand.getField(
 						BLOCKING_ID_FIELD, PactString.class);
 				for (BlockingFunction bf : BlockingFunction.blockingFuntions) {
 					if (appliedBlockingFunctionId.equals(bf.getID())) {
 						out.collect(bf
 								.copyWithExplodedBlockingKey(recordToExpand));
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Reducer that does the matching step for each record in the block
 	 * 
 	 * @author fabian.tschirschnitz@student.hpi.uni-potsdam.de
 	 * @author richard.meissner@student.hpi.uni-potsdam.de
 	 * 
 	 */
 	public static class MatchStep extends ReduceStub {
 		@Override
 		public void reduce(Iterator<PactRecord> records,
 				Collector<PactRecord> out) throws Exception {
 			PactRecord record = new PactRecord();
 			List<PactRecord> r_temp = new ArrayList<PactRecord>();
 			while (records.hasNext()) {
 				record = records.next();
 				r_temp.add(record.createCopy());
 			}
 			for (int i = 0; i < r_temp.size(); i++) {
 				for (int j = i + 1; j < r_temp.size(); j++) {
 					PactRecord r1 = r_temp.get(i);
 					PactRecord r2 = r_temp.get(j);
 					AbstractStringMetric dist = new Levenshtein();
 					if (dist.getSimilarity(r1.getField(3, PactString.class)
 							.getValue(), r2.getField(3, PactString.class)
 							.getValue()) > 0.9) {
 						PactRecord outputRecord = new PactRecord();
 						if (r1.getField(0, PactInteger.class).getValue() < r2
 								.getField(0, PactInteger.class).getValue()) {
 							outputRecord.setField(DUPLICATE_ID_1_FIELD,
 									r1.getField(0, PactInteger.class));
 							outputRecord.setField(DUPLICATE_ID_2_FIELD,
 									r2.getField(0, PactInteger.class));
 						} else {
 							outputRecord.setField(DUPLICATE_ID_2_FIELD,
 									r1.getField(0, PactInteger.class));
 							outputRecord.setField(DUPLICATE_ID_1_FIELD,
 									r2.getField(0, PactInteger.class));
 						}
 
 						out.collect(outputRecord);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Reducer that counts the entries of each block to identify unbalanced
 	 * blocks
 	 * 
 	 * @author richard.meissner@student.hpi.uni-potsdam.de
 	 * @author fabian.tschirschnitz@student.hpi.uni-potsdam.de
 	 * 
 	 */
 	public static class CountStep extends ReduceStub {
 		@Override
 		public void reduce(Iterator<PactRecord> records,
 				Collector<PactRecord> out) throws Exception {
 			PactRecord outputRecord;
 			List<PactRecord> r_temp = new ArrayList<PactRecord>();
 			int sum = 0;
 			while (records.hasNext()) {
 				outputRecord = records.next();
 				r_temp.add(outputRecord.createCopy());
 				sum++;
 			}
 			PactInteger cnt = new PactInteger();
 			cnt.setValue(sum);
 			for (PactRecord r : r_temp) {
 				r.setField(COUNT_FIELD, cnt);
 				out.collect(r);
 			}
 		}
 	}
 
 	/**
 	 * Mapper that emits only balanced blocks
 	 * 
 	 * @author richard.meissner@student.hpi.uni-potsdam.de
 	 * 
 	 */
 	public static class UnbalancedBlockFilterStep extends MapStub {
 
 		@Override
 		public void map(PactRecord record, Collector<PactRecord> collector) {
 			if (record.getField(COUNT_FIELD, PactInteger.class).getValue() <= THRESHOLD)
 				collector.collect(record);
 		}
 	}
 
 	/**
 	 * Mapper that emits only unbalanced blocks
 	 * 
 	 * @author richard.meissner@student.hpi.uni-potsdam.de
 	 * 
 	 */
 	public static class BalancedBlockFilterStep extends MapStub {
 
 		@Override
 		public void map(PactRecord record, Collector<PactRecord> collector) {
 			if (record.getField(COUNT_FIELD, PactInteger.class).getValue() > THRESHOLD)
 				collector.collect(record);
 		}
 	}
 
 	/**
 	 * CoGrouper that combines CD's with tracks
 	 * 
 	 * @author fabian.tschirschnitz@student.hpi.uni-potsdam.de
 	 * 
 	 */
 	public static class CoGroupCDsWithTracks extends CoGroupStub {
 
 		@Override
 		public void coGroup(Iterator<PactRecord> inputRecords,
 				Iterator<PactRecord> concatRecords, Collector<PactRecord> out) {
 			while (inputRecords.hasNext()) {
 				PactRecord outputRecord = inputRecords.next().createCopy();
 				PactList<PactRecord> trackList = new TrackList();
 				while (concatRecords.hasNext()) {
 					trackList.add(concatRecords.next().createCopy());
 				}
 				outputRecord.setField(TRACKS_FIELD, trackList);
 				out.collect(outputRecord);
 			}
 
 		}
 	}
 
 	/**
 	 * Reducer that unions the set of duplicate pairs by emiting only one pair
 	 * for each reducer
 	 * 
 	 * @author fabian.tschirschnitz@student.hpi.uni-potsdam.de
 	 * 
 	 */
 	public static class UnionStep extends ReduceStub {
 		@Override
 		public void reduce(Iterator<PactRecord> records,
 				Collector<PactRecord> out) throws Exception {
 			if (records.hasNext()) {
 				PactRecord record = records.next().createCopy();
 				out.collect(record);
 			}
 		}
 	}
 
 	/**
 	 * Matcher that compares the result with the gold standard and only emits
 	 * tp's
 	 * 
 	 * @author fabian.tschirschnitz@student.hpi.uni-potsdam.de
 	 * 
 	 */
 	public static class ValidatorStep extends MatchStub {
 
 		@Override
 		public void match(PactRecord value1, PactRecord value2,
 				Collector<PactRecord> out) throws Exception {
 			out.collect(value1.createCopy());
 		}
 
 	}
 }
