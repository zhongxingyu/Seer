 /**
  * 
  */
 
 package de.uni_potsdam.hpi.fgnaumann.lsdd;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import eu.stratosphere.pact.common.contract.FileDataSink;
 import eu.stratosphere.pact.common.contract.FileDataSource;
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
 
 	@Override
 	public Plan getPlan(final String... args) {
 		// parse program parameters
 		/*
 		 * 4 file:///home/fabian/lsdd/data/freedb_discs.csv
 		 * file:///home/fabian/lsdd/data/freedb_tracks.csv
 		 * file:///home/fabian/lsdd/out
 		 */
 		final int noSubtasks = (args.length > 0 ? Integer.parseInt(args[0]) : 1);
 		final String inputFileDiscs = (args.length > 1 ? args[1] : "");
 		final String inputFileTracks = (args.length > 2 ? args[2] : "");
 		final String output = (args.length > 3 ? args[3] : "");
 
 		// create DataSourceContract for Orders input
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
 		ReduceContract coutStepReducer = new ReduceContract.Builder(
 				CountStep.class, PactString.class, 0)
 				.input(firstBlockingStepMapper).name("count records step")
 				.build();
 		MapContract unbalancedBlockFilterMapper = MapContract
 				.builder(UnbalancedBlockFilterStep.class).input(coutStepReducer)
 				.name("filter unbalanced blocks step").build();
 		MapContract balancedBlockFilterMapper = MapContract
 				.builder(BalancedBlockFilterStep.class).input(coutStepReducer)
 				.name("filter balanced blocks step").build();
 		ReduceContract matchStepReducer = new ReduceContract.Builder(
 				MatchStep.class, PactString.class, 0)
 				.input(unbalancedBlockFilterMapper).name("match step").build();
 		FileDataSink out = new FileDataSink(RecordOutputFormat.class, output,
 				matchStepReducer, "Output");
 		RecordOutputFormat.configureRecordFormat(out).recordDelimiter('\n')
 				.fieldDelimiter(' ').lenient(true).field(PactString.class, 0)
 				.field(PactString.class, 1);
 
 		// assemble the PACT plan
 		Plan plan = new Plan(out, "MultiBlocking");
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
 		// initialize reusable mutable objects
 		private final PactRecord outputRecord = new PactRecord();
 
 		@Override
 		public void map(PactRecord record, Collector<PactRecord> collector) {
 			PactString genre = record.getField(4, PactString.class);
 			PactString year = record.getField(5, PactString.class);
 			PactString blockingKey = new PactString(genre);
 			AsciiUtils.toLowerCase(blockingKey);
 			outputRecord.setField(0, blockingKey);
 			outputRecord.setField(1, record.getField(2, PactString.class));
 			collector.collect(outputRecord);
 		}
 	}
 
 	/**
 	 * Reducer that does the matching step for each record in the block
 	 * 
 	 * @author fabian.tschirschnitz@student.hpi.uni-potsdam.de
 	 * @author richard.meissner@student.hpi.uni-potsdam.de
 	 * 
 	 */
 	@Combinable
 	public static class MatchStep extends ReduceStub {
 		@Override
 		public void reduce(Iterator<PactRecord> records,
 				Collector<PactRecord> out) throws Exception {
 			PactRecord record = new PactRecord();
 			List<PactRecord> r_temp = new ArrayList<PactRecord>();
 			while (records.hasNext()) {
 				record = records.next();
 				r_temp.add(record);
 			}
 			for (int i = 0; i < r_temp.size(); i++) {
 				for (int j = i; j < r_temp.size(); j++) {
 					PactRecord r1 = r_temp.get(i);
 					PactRecord r2 = r_temp.get(j);
 					if (jaccardSimilarity(r1.getField(1, PactString.class)
 							.getValue(), r2.getField(1, PactString.class)
 							.getValue()) > 0.8) {
 						PactRecord outputRecord = new PactRecord();
 						outputRecord.setField(0,
 								r1.getField(0, PactString.class));
 						outputRecord.setField(1,
 								r1.getField(1, PactString.class));
 						outputRecord.setField(1,
 								r2.getField(1, PactString.class));
 						out.collect(outputRecord);
 					}
 				}
 			}
 		}
 
 		/**
 		 * 
 		 * @see http://www.cs.rit.edu/~vvs1100/Code/JaccardSimilarity.java
 		 * @param similar1
 		 * @param similar2
 		 * @return jaccard index
 		 */
 		public static double jaccardSimilarity(String similar1, String similar2) {
 			HashSet<String> h1 = new HashSet<String>();
 			HashSet<String> h2 = new HashSet<String>();
 
 			for (String s : similar1.split("\\s+")) {
 				h1.add(s);
 			}
 			for (String s : similar2.split("\\s+")) {
 				h2.add(s);
 			}
 
 			int sizeh1 = h1.size();
 			// Retains all elements in h3 that are contained in h2 ie
 			// intersection
 			h1.retainAll(h2);
 			// h1 now contains the intersection of h1 and h2
 
 			h2.removeAll(h1);
 			// h2 now contains unique elements
 
 			// Union
 			int union = sizeh1 + h2.size();
 			int intersection = h1.size();
 
 			return (double) intersection / union;
 
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
 	 * Reducer that countes the entries of each block to identify unbalanced
 	 * blocks
 	 * 
 	 * @author richard.meissner@student.hpi.uni-potsdam.de
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
 				r_temp.add(outputRecord);
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
 		// initialize reusable mutable objects
 		public static int THRESHOLD = 250;
 
 		@Override
 		public void map(PactRecord record, Collector<PactRecord> collector) {
			if (record.getField(record.getNumFields(), PactInteger.class)
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
 		// initialize reusable mutable objects
 		public static int THRESHOLD = 250;
 
 		@Override
 		public void map(PactRecord record, Collector<PactRecord> collector) {
 			if (record.getField(record.getNumFields(), PactInteger.class)
 					.getValue() > THRESHOLD)
 				collector.collect(record);
 		}
 	}
 }
