 /**
  * 
  */
 
 package de.uni_potsdam.hpi.fgnaumann.lsdd;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 import de.uni_potsdam.hpi.fgnaumann.lsdd.stubs.BalancedBlockFilterStep;
 import de.uni_potsdam.hpi.fgnaumann.lsdd.stubs.CoGroupCDsWithTracks;
 import de.uni_potsdam.hpi.fgnaumann.lsdd.stubs.CountOutputStep;
 import de.uni_potsdam.hpi.fgnaumann.lsdd.stubs.CountStep;
 import de.uni_potsdam.hpi.fgnaumann.lsdd.stubs.FirstBlockingStep;
 import de.uni_potsdam.hpi.fgnaumann.lsdd.stubs.MatchStep;
 import de.uni_potsdam.hpi.fgnaumann.lsdd.stubs.SortedNeighbourhood;
 import de.uni_potsdam.hpi.fgnaumann.lsdd.stubs.UnbalancedBlockFilterStep;
 import de.uni_potsdam.hpi.fgnaumann.lsdd.stubs.UnionStep;
 import de.uni_potsdam.hpi.fgnaumann.lsdd.stubs.ValidatorStep;
 import eu.stratosphere.pact.common.contract.CoGroupContract;
 import eu.stratosphere.pact.common.contract.FileDataSink;
 import eu.stratosphere.pact.common.contract.FileDataSource;
 import eu.stratosphere.pact.common.contract.GenericDataSink;
 import eu.stratosphere.pact.common.contract.MapContract;
 import eu.stratosphere.pact.common.contract.MatchContract;
 import eu.stratosphere.pact.common.contract.Order;
 import eu.stratosphere.pact.common.contract.Ordering;
 import eu.stratosphere.pact.common.contract.ReduceContract;
 import eu.stratosphere.pact.common.io.RecordInputFormat;
 import eu.stratosphere.pact.common.io.RecordOutputFormat;
 import eu.stratosphere.pact.common.plan.Plan;
 import eu.stratosphere.pact.common.plan.PlanAssembler;
 import eu.stratosphere.pact.common.plan.PlanAssemblerDescription;
 import eu.stratosphere.pact.common.type.base.PactInteger;
 import eu.stratosphere.pact.common.type.base.PactString;
 import eu.stratosphere.pact.common.type.base.parser.DecimalTextIntParser;
 import eu.stratosphere.pact.common.type.base.parser.VarLengthStringParser;
 
 /**
  * A Stratosphere-Pact-Implementation of "A fast approach for parallel
  * deduplication on multicore processors" - Guilherme Dal Bianco et al.
  * with variations in big block handling.
  * 
  * @author fabian.tschirschnitz@student.hpi.uni-potsdam.de
  * 
  */
 public class MultiBlocking implements PlanAssembler, PlanAssemblerDescription {
 	// record field indizes
 	public static final int DISC_ID_FIELD = 0;
 	public static final int DISC_FREEDB_ID_FIELD = 1;
 	public static final int ARTIST_NAME_FIELD = 2;
 	public static final int DISC_TITLE_FIELD = 3;
 	public static final int GENRE_TITLE_FIELD = 4;
 	public static final int DISC_RELEASED_FIELD = 5;
 	public static final int DISC_TRACKS_FIELD = 6;
 	public static final int DISC_SECONDS_FIELD = 7;
 	public static final int DISC_LANGUAGE_FIELD = 8;
 	public static final int TRACKS_FIELD = 9;
 	public static final int BLOCKING_KEY_FIELD = 10;
 	public static final int BLOCKING_ID_FIELD = 11;
 	public static final int BLOCKING_KEY_EXTENDED_FIELD = 12;
 	public static final int COUNT_FIELD = 13;
 	public static final int DUPLICATE_ID_1_FIELD = 0;
 	public static final int DUPLICATE_ID_2_FIELD = 1;
 
 	// stats
	private static int MAX_BLOCK_SIZE = 200000;
 	// parameters
 	public static int MAX_WINDOW_FOR_LARGE_BLOCKS = 5;
 	public static int MAX_WINDOW_SIZE = 25;
 	public static float SIMILARITY_THRESHOLD = 0.9f;
 	public static boolean takeTracksIntoAccount = false;
 	public static int MAXIMUM_COMPARISON = MAX_WINDOW_FOR_LARGE_BLOCKS
 			* MAX_BLOCK_SIZE;
 	public static int THRESHOLD = (int) Math.round(Math.sqrt(MAXIMUM_COMPARISON));
 	
 
 	@Override
 	public Plan getPlan(final String... args) {
 		final int noSubtasks;
 		final String inputFileDiscs;
 		final String inputFileTracks;
 		final String output;
 		final String inputFileGold;
 		if (args.length >= 4) {
 			noSubtasks = Integer.parseInt(args[0]);
 			inputFileDiscs = args[1];
 			inputFileTracks = args[2];
 			inputFileGold = args[3];
 			output = args[4];
 		} else {
 			throw new IllegalArgumentException(
 					"The number of given parameters is wrong!");
 		}
 
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
 
 		FileDataSource tracks = null;
 		if (takeTracksIntoAccount) {
 			// create DataSourceContract for tracks input
 			// disc_id;track_number;"track_title";"artist_name";track_seconds
 			// 2;1;"Intro+Chor Der Kriminalbeamten";"Kottans Kapelle";115
 			tracks = new FileDataSource(RecordInputFormat.class,
 					inputFileTracks, "Tracks");
 			RecordInputFormat.configureRecordFormat(tracks)
 					.recordDelimiter('\n').fieldDelimiter(';')
 					.field(DecimalTextIntParser.class, 0) // disc_id
 					.field(DecimalTextIntParser.class, 1) // freedbdiscid
 					.field(VarLengthStringParser.class, 2) // "track_title"
 					.field(VarLengthStringParser.class, 3) // "artist_name"
 					.field(DecimalTextIntParser.class, 4); // track_seconds
 		}
 
 		// create DataSourceContract for gold standard input
 		// disc_id1;disc_id2
 		// 13;3163
 		FileDataSource gold = new FileDataSource(RecordInputFormat.class,
 				inputFileGold, "GoldStandard");
 		RecordInputFormat.configureRecordFormat(gold).recordDelimiter('\n')
 				.fieldDelimiter(';')
 				.field(DecimalTextIntParser.class, DUPLICATE_ID_1_FIELD) // disc_id1
 				.field(DecimalTextIntParser.class, DUPLICATE_ID_2_FIELD); // disc_id1
 
 		CoGroupContract coGrouper = null;
 		if (takeTracksIntoAccount) {
 			coGrouper = CoGroupContract
 					.builder(CoGroupCDsWithTracks.class, PactInteger.class, 0,
 							0).name("Group CDs with Tracks").build();
 
 			coGrouper.setFirstInput(discs);
 			coGrouper.setSecondInput(tracks);
 		}
 
 		// contracts
 		MapContract firstBlockingStep = null;
 		if (takeTracksIntoAccount) {
 			firstBlockingStep = MapContract.builder(FirstBlockingStep.class)
 					.input(coGrouper)
 					.name("first blocking step with track list").build();
 		} else {
 			firstBlockingStep = MapContract.builder(FirstBlockingStep.class)
 					.input(discs)
 					.name("first blocking step without track list").build();
 		}
 
 		ReduceContract countStep = new ReduceContract.Builder(CountStep.class,
 				PactString.class, BLOCKING_KEY_FIELD)
 				.keyField(PactString.class, BLOCKING_ID_FIELD)
 				.input(firstBlockingStep).name("count records step").build();
 
 		MapContract unbalancedBlockFilter = MapContract
 				.builder(UnbalancedBlockFilterStep.class).input(countStep)
 				.name("filter unbalanced blocks step").build();
 
 		MapContract balancedBlockFilter = MapContract
 				.builder(BalancedBlockFilterStep.class).input(countStep)
 				.name("filter balanced blocks step").build();
 
 		ReduceContract matchStepReducerBalanced = new ReduceContract.Builder(
 				MatchStep.class, PactString.class, BLOCKING_KEY_FIELD)
 				.keyField(PactString.class, BLOCKING_ID_FIELD)
 				.input(unbalancedBlockFilter).name("match step balanced")
 				.build();
 		
 		ReduceContract unionStep1 = new ReduceContract.Builder(UnionStep.class,
 				PactString.class, DUPLICATE_ID_1_FIELD)
 				.keyField(PactInteger.class, DUPLICATE_ID_2_FIELD)
 				.input(matchStepReducerBalanced)
 				.name("union step 1").build();
 		
 
 		ReduceContract sortedNeighbourhoodStep = new ReduceContract.Builder(
 				SortedNeighbourhood.class, PactString.class, BLOCKING_KEY_FIELD)
 				.keyField(PactString.class, BLOCKING_ID_FIELD)
 				.secondaryOrder(
 						new Ordering(BLOCKING_KEY_EXTENDED_FIELD,
 								PactString.class, Order.ASCENDING))
 				.input(balancedBlockFilter).name("second blocking step")
 				.build();
 
 		ReduceContract unionStep2 = new ReduceContract.Builder(UnionStep.class,
 				PactString.class, DUPLICATE_ID_1_FIELD)
 				.keyField(PactInteger.class, DUPLICATE_ID_2_FIELD)
 				.input(sortedNeighbourhoodStep).name("union step 2").build();
 		unionStep2.addInput(unionStep1);
 		unionStep2.setDegreeOfParallelism(1);
 
 		MatchContract validatorStep = MatchContract
 				.builder(ValidatorStep.class, PactInteger.class,
 						DUPLICATE_ID_1_FIELD, DUPLICATE_ID_1_FIELD)
 				.input1(gold)
 				.input2(unionStep2)
 				.name("validator step")
 				.keyField(PactInteger.class, DUPLICATE_ID_2_FIELD,
 						DUPLICATE_ID_2_FIELD).build();
 
 		// debugging contracts
 		ReduceContract countOutputStep = new ReduceContract.Builder(
 				CountOutputStep.class, PactInteger.class, COUNT_FIELD)
 				.keyField(PactString.class, BLOCKING_ID_FIELD)
 				.keyField(PactString.class, BLOCKING_KEY_FIELD)
 				.input(countStep).name("count output step").build();
 		
 		
 		unionStep2.addInput(matchStepReducerBalanced);
 		// file output result
 		FileDataSink outResult = new FileDataSink(RecordOutputFormat.class,
 				output + "/result.csv", unionStep2, "Output Result");
 		RecordOutputFormat.configureRecordFormat(outResult)
 				.recordDelimiter('\n').fieldDelimiter(' ').lenient(true)
 				.field(PactInteger.class, 0).field(PactInteger.class, 1);
 		outResult.setDegreeOfParallelism(1);
 
 		// file output tp
 		FileDataSink outTruePositives = new FileDataSink(
 				RecordOutputFormat.class, output + "/tp.csv", validatorStep,
 				"Output True Positives");
 		RecordOutputFormat.configureRecordFormat(outTruePositives)
 				.recordDelimiter('\n').fieldDelimiter(' ').lenient(true)
 				.field(PactInteger.class, 0).field(PactInteger.class, 1);
 		outTruePositives.setDegreeOfParallelism(1);
 
 		// debug outputs
 		// count output steps
 		FileDataSink countOutput = new FileDataSink(RecordOutputFormat.class,
 				output + "/block_size.csv", countOutputStep,
 				"Output Block Sizes");
 		RecordOutputFormat.configureRecordFormat(countOutput)
 				.recordDelimiter('\n').fieldDelimiter(';').lenient(true)
 				.field(PactInteger.class, 0).field(PactString.class, 1)
 				.field(PactString.class, 2);
 		countOutput.setDegreeOfParallelism(1);
 //		FileDataSink blocksOutput = new FileDataSink(RecordOutputFormat.class,
 //				output + "/blocks.csv", countStep,
 //				"Output Block");
 //		RecordOutputFormat.configureRecordFormat(blocksOutput)
 //				.recordDelimiter('\n').fieldDelimiter(';').lenient(true)
 //				.field(PactInteger.class, DISC_ID_FIELD).field(PactString.class, BLOCKING_KEY_FIELD)
 //				.field(PactString.class, BLOCKING_ID_FIELD);
 //		blocksOutput.setDegreeOfParallelism(1);
 
 		// assemble the PACT plan
 		Collection<GenericDataSink> sinks = new HashSet<GenericDataSink>();
 		sinks.add(outResult);
 		sinks.add(outTruePositives);
 		sinks.add(countOutput);
 //		sinks.add(blocksOutput);
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
 }
