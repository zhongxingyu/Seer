 
 public class Program {
 
 	/**
 	 * @param args The program arguments are the filenames of the data on which
 	 * 	the name recommendation should be done.
 	 * 
	 * The first has to be of the user activity data.
 	 * 
 	 * Afterwards an arbitrary number of files with the item similarity can
 	 * be injected.
 	 */
 	public static void main(String[] args) {
 		
 		if( args.length < 1 ) {
			System.out.println("At least the user activity data has to be given.");
 			return;
 		}
 		
 		loadData(args);
 		createRecommender();
 		outputResults();
 	}
 	
 	/**
 	 * Load several files with instance data. The structure of the files could
 	 * be arbitrary for the loader but nut for the following processes.
 	 * 
 	 * @param files A list with filenames. The first one has to be the userData.
 	 * Afterward any number of item similarity files can follow.
 	 */
 	private static void loadData(String[] files) {
 		// Load without time stamp
 		userData = new InstanceBase(files[0], 4, 3);
 		
 		// Load only the two names and there similarity -> 3 attributes
 		if( files.length > 1 ) {
 			itemData = new InstanceBase[files.length-1];
 			for( int i=1; i<files.length; ++i )
 				itemData[i-1] = new InstanceBase(files[i], 6, 3);
 		}
 	}
 
 	/**
 	 * Initialize a trained recommender using the loaded data. The recommender
 	 * is required to generate any output data.
 	 * 
 	 * Calling this method twice would create the same result and is only
 	 * computional overhead.
 	 */
 	private static void createRecommender() {
 		Evaluator trainer = new Evaluator();
 		recommender = trainer.train( userData, itemData );
 	}
 	
 	
 	/**
 	 * This method uses the current recommender to create the recommendation
 	 * lists. createRecommender must be called before!
 	 */
 	private static void outputResults() {
 		System.out.println( "\n\nRESULTS:");
 		System.out.println(recommender.getItemListForUser(0));
 	}
 
 	private static Recommender recommender;
 	private static InstanceBase userData;
 	private static InstanceBase[] itemData;
 }
