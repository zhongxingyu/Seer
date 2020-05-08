 package quality;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class ExperimentRunner {
 
 	private ExperimentTunerIF tuner;
 //	private ArrayList<ExperimentUnit> experimentUnits;
 	private String title;
 	private String description;
 	private String[] usernames;
 	private Double[][] resultMatrix;
 
 	private static final String EXPERIMENTS_LOG_PATH = "../Experiments_Log";
 	private static String graphColors[] = {"b", "r", "g", "m", "c", "y", "k"};
 	/*
 	 * b : blue
 	 * r : red
 	 * g : green
 	 * m : magenta
 	 * c : cyan
 	 * y : yellow
 	 * k : black
 	 */
 	
 	public ExperimentRunner(ExperimentTunerIF tuner, String[] usernames, String title){
 		this.tuner = tuner;
 		this.usernames = usernames;
 		this.title = title;
 		this.description = "";
 		this.resultMatrix = new Double[usernames.length][];
 	}
 	
 	public ExperimentRunner(ExperimentTunerIF tuner, String[] usernames, String title, String description){
 		this.tuner = tuner;
 		this.usernames = usernames;
 		this.title = title;
 		this.description = description;
 		this.resultMatrix = new Double[usernames.length][];
 	}
 
 	private void printResultMatrix(){
 		for(int i=0; i<usernames.length; i++){
 			System.out.print(usernames[i] + ": ");
 			for(int j=0; j<resultMatrix[i].length; j++)
 				System.out.printf(" %2.2f", resultMatrix[i][j]);
 			System.out.println();
 		}
 	}
 	
 	private void ExportJSONObject(ArrayList<ExperimentUnit> units) throws JSONException, IOException{
 		if(usernames.length > graphColors.length){
 			System.err.println("Warning: ExperimentRunner.java: No sufficient colors to assign a unique color to each user in this Experiment");
 		}
 
 		JSONObject jsonRoot = new JSONObject();
 		
 		JSONArray usersArr = new JSONArray();
 		for(int i=0; i<usernames.length; i++){
 			JSONObject jsonUser = new JSONObject();
 			
 			jsonUser.put("color", graphColors[i%usernames.length]);
 			
 			JSONArray jsonUserValues = new JSONArray();
 			for(int j=0; j<units.size(); j++) 
 				jsonUserValues.put(resultMatrix[i][j]);
 			
 			jsonUser.put("values", jsonUserValues);
 			jsonUser.put("name", usernames[i]);
 			usersArr.put(jsonUser);						
 		}
 		
 		JSONArray jsonCombinationsArr = new JSONArray();
 		for(ExperimentUnit unit : units)
 			jsonCombinationsArr.put(unit.getTitle());
 		
 		jsonRoot.put("criterias", usersArr);
 		jsonRoot.put("title", this.title);
 		jsonRoot.put("combinations_names", jsonCombinationsArr);
 		jsonRoot.put("ylabel", "Accuracy %");
 
 		JSONArray jsonArrayWrapper = new JSONArray();
 		jsonArrayWrapper.put(jsonRoot);
 		
 		String filename = EXPERIMENTS_LOG_PATH + "/" + this.title + "_" + System.currentTimeMillis();
 		PrintWriter pw = new PrintWriter(new FileWriter(filename));
 		pw.println(jsonArrayWrapper.toString(4));
 		pw.close();
 	}
 	
 	public void runExperiment() throws Exception {
 		ArrayList<ExperimentUnit> units = tuner.getExperimentUnits();
 		for(int i=0; i<resultMatrix.length; i++)
 			resultMatrix [i] = new Double[units.size()];
 		
 		for(int i=0; i<usernames.length; i++){
 			for (int j=0; j<units.size(); j++){
 				ExperimentUnit unit = units.get(j);
 				
 				QualityReporterRunner qualityReporterRunner = new QualityReporterRunner(unit.getFilterCreators(), unit.getPreprocessors(), usernames[i], unit.getClassifierType(), unit.getTrainingSetPercentage());
 				QualityReporter reporter = qualityReporterRunner.EvaluateClassifer();
 				resultMatrix[i][j] = reporter.getAccuracy();
 				//XXX add timeStamp + unit name + unit description + result summary to Log file
 				
 				System.out.println(reporter.toSummaryString());
 			}
			// re-fetch the ExperimentUnits, as the current one is used and can't be reused (e.g: WF filter is initialized in the above loop)
			units = tuner.getExperimentUnits();
 		}
 
 		printResultMatrix();
 		
 		ExportJSONObject(units);
 	}
 }
