 package pacman.experimentclient;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Scanner;
 
 import javax.script.ScriptException;
 
 import com.google.gson.Gson;
 
 public class ExperimentClient
 {
 	private static final String SERVER_URL = "http://www.stewartml.co.uk/pac-man/";
 	private static final String EXPERIMENT_URL = SERVER_URL + "active_experiment";
 	private static final String SAVE_URL = SERVER_URL + "experiments";
 	
 	public static void main(String[] args)
 	{
 		ExperimentClient client = new ExperimentClient();
 		
 		if (args.length == 2)
 		{
 			client.uploadScript(args[0], Integer.parseInt(args[1]));
 		}
 		else
 		{
 			
 			client.run();
 		}
 	}
 	
 	
 	public void run()
 	{
 		ExperimentRunner runner = new ExperimentRunner();
 		
 		while (true)
 		{
 			Experiment experiment = getExperiment();
 			
 			if (experiment == null)
 				break;
 			
 			try
 			{
 				int score = runner.run(experiment.script);
 				saveResult(experiment._id, score);
 				System.out.printf("Finished running.  Score: %d.\n", score);
 			}
 			catch (ScriptException ex)
 			{
 				ex.printStackTrace();
 			}
			
 		}
 	}
 	
 	
 	public void uploadScript(String path, int count)
 	{
 		try
 		{
 			Scanner scanner = new Scanner(new FileReader(path));
 			scanner.useDelimiter("\\A");
 			String script = scanner.hasNext() ? scanner.next() : "";
 			
 			scanner.close();
 			
 			NewExperiment experiment = new NewExperiment();
 			experiment.scores = new int[0];
 			experiment.script = script;
 			experiment.count = count;
 			
 			HttpClient client = new HttpClient();
 			Gson gson = new Gson();
 			String json = gson.toJson(experiment);
 			
 			client.post(SAVE_URL, json);
 		}
 		catch (IOException ex)
 		{
 			ex.printStackTrace();
 		}
 	}
 	
 	
 	private Experiment getExperiment()
 	{
 		try
 		{
 			HttpClient client = new HttpClient();
 			String json = client.get(EXPERIMENT_URL);
 			
 			Gson gson = new Gson();
 			Experiment experiment = (Experiment)gson.fromJson(json, Experiment.class);
 			return experiment;
 		}
 		catch (IOException ex)
 		{
 			ex.printStackTrace();
 			return null;
 		}
 	}
 	
 	
 	private void saveResult(String experiment, int score)
 	{
 		try
 		{
 			HttpClient client = new HttpClient();
 			String response = client.post(String.format(SAVE_URL + "/%s", experiment), String.format("%d", score));
 			
 			if (!response.equals("OK"))
 				System.out.println("Error saving score: " + response);
 		}
 		catch (IOException ex)
 		{
 			ex.printStackTrace();
 		}
 	}
 	
 	
 	public class Experiment
 	{
 		public String _id;
 		public String script;
 	}
 	
 	public class NewExperiment
 	{
 		public String script;
 		public int[] scores;
 		public int count;
 	}
 }
