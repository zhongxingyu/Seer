 package game_phase;
 
 import utilities.GameData;
 import utilities.GameParser;
 import utilities.helper_classes.CSVDataFormatter;
 import utilities.helper_classes.GameInfo;
 import utilities.helper_classes.Utilities;
 
 public class GamePhaseHeuristicDiscriminator {
 
 	private static final int NUM_GAMES = 10;
 	private static final String PATH_PREFIX = "../Plotting/game_phase/"; //optional
	private static final String FILE_NAME = PATH_PREFIX + "kmeans_heuristic_probabilities.csv";
 	private static final double[] WEIGHTS = {1.0, -0.5};
 	
 	
 	public enum GamePhase {
         BEGINNING(0), MIDDLE(1), END(2);
         private int value;
 
         private GamePhase(int value) {
                 this.value = value;
         }
         public int getValue() {
             return value;
         }
 	}; 
 	
 	public static void main(String args[]){
 		Utilities.printInfo(String.format("<< Running heuristic game phase discrimination on %d games >>", NUM_GAMES));
 		GameData myGameData = new GameData(NUM_GAMES, 0.0);
 		Utilities.printInfo("Finished fetching game data");
 		myGameData.setMode(GameData.Mode.TEST);		
 		
 		Utilities.printInfo("\nTesting and printing to CSV...");
 		testAndOutputCSV(myGameData);
 	}
 	
 	private static double dotProduct(double[] vector1, double[] vector2){
 		double sum = 0.0;
 		assert(vector1.length == vector2.length);
 		
 		for (int i = 0; i < vector1.length; i++){
 			sum += (vector1[i] * vector2[i]);
 		}
 		
 		return sum;
 	}
 	
 	
	private static double[] assignCluster(double[] features){
 		// TODO: deal with end game in an entirely different way (i.e. with at dedicated set of features)
 		
 		double score = dotProduct(features, WEIGHTS);
 		
 		if (score > 14)
			return new double[]{1.0, 0.0, 0.0};
 		else if (score > 11)
			return new double[]{0.0, 1.0, 0.0};
 		else
			return new double[]{0.0, 0.0, 1.0};
 	}
 
 	/** Uses the test games to assign each GameState to a cluster. Results output to CSV. */
 	private static void testAndOutputCSV(GameData myGameData) {
 		CSVDataFormatter csv = new CSVDataFormatter();
 		int count = 0;
 		
 		while (myGameData.hasNextGame()){
 			final long startTime = System.currentTimeMillis();
 			
 			Utilities.printInfoInline("Testing on game # " + ++count + "..."); //time will be appended in-line
 			GameInfo trainGameInfo = myGameData.getNextGame();
 			GameParser myParser = new GameParser(trainGameInfo);
 			
 			csv.appendValue(Integer.toString(trainGameInfo.getGameID()));
 			while (myParser.hasNextGameState()){
				double[] currProbabilities = assignCluster(FeatureExtractor.extractReducedFeatures( myParser.getNextGameState().getCurr()));
				
				for (int i = 0; i < currProbabilities.length; i++){
					csv.appendValue(Double.toString(currProbabilities[i]));
				}

 			}
 			
 			csv.nextLine();
 			
 			final long endTime = System.currentTimeMillis();
 			Utilities.printInfo("testing took " + Utilities.msToString(endTime - startTime));
 		}
 		
 		csv.finalizeFile(FILE_NAME);
 	}
 	
 }
