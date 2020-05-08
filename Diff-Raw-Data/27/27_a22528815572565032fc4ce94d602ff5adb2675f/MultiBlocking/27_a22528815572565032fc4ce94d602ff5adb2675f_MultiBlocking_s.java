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
 import eu.stratosphere.pact.common.contract.FileDataSink;
 import eu.stratosphere.pact.common.contract.FileDataSource;
 import eu.stratosphere.pact.common.contract.GenericDataSink;
 import eu.stratosphere.pact.common.contract.MapContract;
 import eu.stratosphere.pact.common.contract.ReduceContract;
 import eu.stratosphere.pact.common.contract.ReduceContract.Combinable;
 import eu.stratosphere.pact.common.io.RecordInputFormat;
 import eu.stratosphere.pact.common.io.RecordOutputFormat;
 import eu.stratosphere.pact.common.plan.Plan;
 import eu.stratosphere.pact.common.plan.PlanAssembler;
 import eu.stratosphere.pact.common.plan.PlanAssemblerDescription;
 import eu.stratosphere.pact.common.stubs.Collector;
 import eu.stratosphere.pact.common.stubs.MapStub;
 import eu.stratosphere.pact.common.stubs.ReduceStub;
 import eu.stratosphere.pact.common.type.PactRecord;
 import eu.stratosphere.pact.common.type.base.PactDouble;
 import eu.stratosphere.pact.common.type.base.PactInteger;
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
 	public static final int THRESHOLD = 250;
 
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
 
 		// create DataSourceContract for discs input
 		// disc_id;freedbdiscid;"artist_name";"disc_title";"genre_title";"disc_released";disc_tracks;disc_seconds;"disc_language"
 		// 7;727827;"Tenacious D";"Tenacious D";"Humour";"2001";19;2843;"eng"
 		FileDataSource discs = new FileDataSource(RecordInputFormat.class,
 				inputFileDiscs, "Discs");
 		RecordInputFormat.configureRecordFormat(discs).recordDelimiter('\n')
 				.fieldDelimiter(';').field(DecimalTextIntParser.class, 0) // disc_id
 				.field(DecimalTextIntParser.class, 1) // freedbdiscid
 				.field(VarLengthStringParser.class, 2) // "artist_name"
 				.field(VarLengthStringParser.class, 3) // "disc_title"
 				.field(VarLengthStringParser.class, 4) // "genre_title"
 				.field(VarLengthStringParser.class, 5) // "disc_released"
 				.field(DecimalTextIntParser.class, 6) // disc_tracks
 				.field(DecimalTextIntParser.class, 7) // disc_seconds
 				.field(VarLengthStringParser.class, 8); // "disc_language"
 
 		//
 		MapContract firstBlockingStepMapper = MapContract
 				.builder(FirstBlockingStep.class).input(discs)
 				.name("first blocking step").build();
 
 		ReduceContract countStepReducer = new ReduceContract.Builder(
 				CountStep.class, PactString.class, 9)
 				.input(firstBlockingStepMapper).name("count records step")
 				.build();
 		MapContract unbalancedBlockFilterMapper = MapContract
 				.builder(UnbalancedBlockFilterStep.class)
 				.input(countStepReducer).name("filter unbalanced blocks step")
 				.build();
 		MapContract balancedBlockFilterMapper = MapContract
 				.builder(BalancedBlockFilterStep.class).input(countStepReducer)
 				.name("filter balanced blocks step").build();
 		
 		FileDataSink outUnbalanced = new FileDataSink(RecordOutputFormat.class, output+"/unbalanced",
 				balancedBlockFilterMapper, "Output Unbalanced");
 		
 		RecordOutputFormat.configureRecordFormat(outUnbalanced).recordDelimiter('\n')
 		.fieldDelimiter(';').lenient(true).field(PactInteger.class, 0) // disc_id
 		.field(PactInteger.class, 1) // freedbdiscid
 		.field(PactString.class, 2) // "artist_name"
 		.field(PactString.class, 3) // "disc_title"
 		.field(PactString.class, 4) // "genre_title"
 		.field(PactString.class, 5) // "disc_released"
 		.field(PactInteger.class, 6) // disc_tracks
 		.field(PactInteger.class, 7)
 		.field(PactString.class, 8)// disc_seconds
 		.field(PactString.class, 9); // "blockingKey"
 
 		ReduceContract matchStepReducerBalanced = new ReduceContract.Builder(
 				MatchStep.class, PactString.class, 9)
 				.input(unbalancedBlockFilterMapper).name("match step").build();
 		FileDataSink out = new FileDataSink(RecordOutputFormat.class, output,
 				matchStepReducerBalanced, "Output");
 		RecordOutputFormat.configureRecordFormat(out).recordDelimiter('\n')
 				.fieldDelimiter(' ').lenient(true).field(PactInteger.class, 0)
 				.field(PactInteger.class, 1).field(PactDouble.class, 2)
 				.field(PactString.class, 3);
 
 		// assemble the PACT plan
 		Collection<GenericDataSink> sinks = new HashSet<GenericDataSink>();
 		sinks.add(out);
 		sinks.add(outUnbalanced);
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
			BlockingFunction bf1 = new BlockingFunction(record){
 				@Override
				PactString function() {
 					String genre = record.getField(4, PactString.class).getValue()
 							.replace("\"", "");
 					genre = genre.length() > 2 ? genre.substring(0, 2) : "";
 					String year = record.getField(5, PactString.class).getValue()
 							.replace("\"", "");
 					year = year.length() >= 4 ? year.substring(0, 3) : "";
 					PactString blockingKey = new PactString(genre + year);
 					AsciiUtils.toLowerCase(blockingKey);
 					return blockingKey;
 				}
 				
 			};
			BlockingFunction bf2 = new BlockingFunction(record){
 				@Override
				PactString function() {
 					String artist = record.getField(2, PactString.class).getValue()
 							.replace("\"", "");
 					artist = artist.length() > 2 ? artist.substring(0, 2) : "";
 					String year = record.getField(5, PactString.class).getValue()
 							.replace("\"", "");
 					year = year.length() >= 4 ? year.substring(0, 3) : "";
 					PactString blockingKey = new PactString(artist + year);
 					AsciiUtils.toLowerCase(blockingKey);
 					return blockingKey;
 				}
 				
 			};
 			PactRecord rbf1 = bf1.copyWithBlockingKey(record);
 			collector.collect(rbf1);
 			PactRecord rbf2 = bf2.copyWithBlockingKey(record);
 			collector.collect(rbf2);
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
 				for (int j = 0; j < r_temp.size(); j++) {
 					PactRecord r1 = r_temp.get(i);
 					PactRecord r2 = r_temp.get(j);
 					AbstractStringMetric dist = new Levenshtein();
 					if (!(r1.getField(0, PactInteger.class).equals(r2.getField(
 							0, PactInteger.class)))
 							&& dist.getSimilarity(
 									r1.getField(3, PactString.class).getValue(),
 									r2.getField(3, PactString.class).getValue()) > 0.9) {
 						PactRecord outputRecord = new PactRecord();
 						outputRecord.setField(0,
 								r1.getField(0, PactInteger.class));
 						outputRecord.setField(1,
 								r2.getField(0, PactInteger.class));
 						outputRecord.setField(
 								2,
 								new PactDouble(dist.getSimilarity(
 										r1.getField(3, PactString.class)
 												.getValue(),
 										r2.getField(3, PactString.class)
 												.getValue())));
 						outputRecord.setField(3,
 								r1.getField(9, PactString.class));
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
 	@Combinable
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
 				r.addField(cnt);
 				out.collect(r);
 			}
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * eu.stratosphere.pact.common.stubs.ReduceStub#combine(java.util.Iterator
 		 * , eu.stratosphere.pact.common.stubs.Collector)
 		 */
 		@Override
 		public void combine(Iterator<PactRecord> records,
 				Collector<PactRecord> out) throws Exception {
 			// the logic is the same as in the reduce function, so simply call
 			// the reduce method
 			this.reduce(records, out);
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
 			if (record.getField(record.getNumFields() - 1, PactInteger.class)
 					.getValue() <= THRESHOLD)
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
 			if (record.getField(record.getNumFields() - 1, PactInteger.class)
 					.getValue() > THRESHOLD)
 				collector.collect(record);
 		}
 	}
 }
