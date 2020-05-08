 package Analysis;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 import java.util.Properties;
 import java.util.Vector;
 
 import Model.Block;
 import Model.Blocks;
 import Model.Index;
 import Model.Constants;
 import Model.Labels;
 import Model.Query;
 import Model.Relations;
 import Model.Result;
 import Model.Scores;
 import Model.WinGenoConfigResolver;
 import Utils.Common;
 
 public class AnalysisRunner {
 
 	public static void main(String[] args) throws java.io.IOException {
 
 		//
 		//	Initialize logging
 		//initLog();
 
 		//
 		//	Start analysis
 		System.err.println("Starting analysis.");
 
 		////////////////////////////////////////////////////////////////////////////////
 		//
 		//	Load experiment configuration.
 		//
 		////////////////////////////////////////////////////////////////////////////////		
 		if ( args.length < 1 || args[0].equals("-h") ) {
 			System.err.println("args: <config_file>");
 			System.exit(-1);
 		}
 
 		String confFileName = args[0];
 		BufferedWriter calledBlockIO = null;
 
 		System.err.println("Loading experiment properties:");
 		Properties experimentConf = new Properties();
 		try {
 			experimentConf.load(new FileReader( confFileName ));
 		} catch ( IOException exp ) {
 			System.err.println("Could not load input files.");
 			exp.printStackTrace();
 			System.exit(-1);
 		}
 
 
 		String callFN = experimentConf.getProperty("outCallFile");
 		calledBlockIO = new BufferedWriter(new FileWriter(callFN));
 
 		//	Compute window size
 		windowSize = Integer.parseInt(experimentConf.getProperty("windowSize"));
 		numConfigurations = (int) Math.pow(Constants.NUM_GENO_STATES, windowSize);
 
 
 		////////////////////////////////////////////////////////////////////////////////
 		//
 		//	Load data files.
 		//
 		////////////////////////////////////////////////////////////////////////////////
 		try {
 			System.err.println("Loading input files.");
 			loadInputFiles(experimentConf);						
 		} catch ( IOException exp ) {
 			System.err.println("Could not load input files.");
 			exp.printStackTrace();
 			System.exit(-1);
 		}
 
 		////////////////////////////////////////////////////////////////////////////////
 		//
 		//	Perform the analysis.
 		//
 		////////////////////////////////////////////////////////////////////////////////		
 		System.err.println("Applying analysis.");
 		LinearAnalysis la = new LinearAnalysis();
 
         calledBlockIO.write("#queryName\thitName\tisCorrectPair\tblockIdx\twinStart\twinEnd\tsnpStart\tsnpEnd\tcallScore\tnumRHs\n"); // header
 
 		// for ( String queryID : query ) {
 		for (int q = 0; q < query.getNumIndividuals(); ++q) {
 
 			String queryID = query.getName(q);
 			System.err.println("Analyzing "+ queryID);
 			int queryConfs[] = query.getIndConf( queryID );
 
 			Vector<Vector<Result> > results = new Vector<Vector<Result> >();
 
 			int relatedIndividuals[] = la.getRelated(labels,
                                                      queryConfs,
                                                      index,
                                                      blocks,
                                                      scores,
                                                      relations,
                                                      queryID,
                                                      results);
 



 			int relIndIdx = 0;
 			for ( int indId : relatedIndividuals ) {
 				System.out.println("FOUND RELATED:\t"+ queryID+"\t"+labels.getString(indId) );
 
                 for (Result result : results.get(relIndIdx))
                 {
                     String correct = "F";
                     String indName = labels.getString(indId);
 
                     if (relations.size() == 0)
                         correct = "?";
                     else if (relations.isRelated(queryID, indName))
                         correct = "T";
 
                     Block block = result.block;
 
                     int wstart = block.getFirstWindow();
                     int wend   = block.getLastWindow() + 1;
                     int mstart = wstart * windowSize;
                     int mend   = wend * windowSize;
 
                     calledBlockIO.write(queryID +
                                         "\t" + indName +
                                         "\t" + correct +
                                         "\t" + block.getID() +
                                        "\t" + "-1" +
                                        "\t" + "-1" +
                                         "\t" + result.score +
                                         "\t" + Common.countReverseHomozygotes(winGenoConfigResolver, block, query, index, q, indId) +
                                         "\n");
 
                 }
 
                 ++relIndIdx;
             }
 		}
 
         calledBlockIO.close();
 
 		//
 		//	Analysis done
 		System.err.println("Analysis done.");
 	}
 
 	private static void loadInputFiles(Properties experimentConf)
 			throws IOException {
 
 		//
 		//	Load index labels
 		String labelFN = experimentConf.getProperty("labelFile");
 		System.err.println("Loading index-individuals labels:"+ labelFN );
 		labels = Labels.load( labelFN );
 
 		//
 		//	Load index file
 		String indexFN = experimentConf.getProperty("indexFile");
 		System.err.println("Loading indexed individuals"+ indexFN );
 		index = Index.load( indexFN, windowSize );
 
 		//
 		//	Load query individuals
 		String queryFN = experimentConf.getProperty("queryFile");
 		System.err.println("Loading query individuals "+ queryFN );
 		query = Query.load( queryFN );
 
         //
         //  Load genotype enumeration to configuration mapping.
         String winGenoFN = experimentConf.getProperty("winGenoFile");
         System.err.println("Loading window genotype configuration mapping: " + winGenoFN);
         winGenoConfigResolver = WinGenoConfigResolver.load(winGenoFN);
 
 		//
 		//	Load relationship file
 		String relationshipFN = experimentConf.getProperty("relationshipFile");
 		System.err.println("Loading relationship file.");
 		relations = Relations.load( relationshipFN );
 
 		//
 		//	Load block file
 		String blockFN = experimentConf.getProperty("blockFile");
 		System.err.println("Loading block-information file"+blockFN);
 		blocks = Blocks.load( blockFN );
 
 		//
 		//	Load score files
 		String scoreFN = experimentConf.getProperty("scoreFile");
 		System.err.println("Loading window scores"+ scoreFN );
 		scores = Scores.load( scoreFN, windowSize );
 	}
 
 	/*
 	private static void initLog() {
 		BasicConfigurator.configure();
 		System.err.println("HGI.Analysis.Runner");
 	}
 	 */
 
 	// private static Logger logger;
 
 	private static Labels labels = null;
 	private static Index index = null;
 	private static Query query = null;
 	private static Relations relations = null;
 	private static Blocks blocks = null;
 	private static Scores scores = null;
     private static WinGenoConfigResolver winGenoConfigResolver = null;
 
 	private static int windowSize;
 	private static int numConfigurations;
 
 }
 
 
 // Local Variables:
 // c-basic-offset: 4
 // c-file-style: "bsd"
 // End:
