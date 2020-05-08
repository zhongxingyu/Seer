 package com.matrobot.gha;
 
 import java.io.IOException;
 
 import com.matrobot.gha.archive.cmd.FindEventsCmd;
 import com.matrobot.gha.archive.cmd.RepoActivityCmd;
 
 public class MainApp {
 
 	private Configuration params;
 	
 	
 	public MainApp(Configuration params){
 		this.params = params;
 	}
 	
 	
 	public void run() throws IOException {
 		
 		ICommand command = findCommand(params.getCommand());
 		
 		if(command == null){
 			System.out.println("Command " + params.getCommand() + " not found");
 			return;
 		}
 		
 		command.run(params);
 	}
 
 
 	private ICommand findCommand(String command) {
 		
 		if(command.equals("find_events")){
 			return new FindEventsCmd();
 		}
 		else if(command.equals("repo_activity")){
 			return new RepoActivityCmd();
 		}
 		
 		return null;
 	}
 
 
 	public static void main(String[] args) throws IOException {
 
 		Configuration params = new Configuration(args[0]);
 		
 		if(params.getCommand() != null){
 			MainApp app = new MainApp(params);
 			app.run();
 		}
 		else{
 			showHelp();
 		}
 	}
 
 
 	private static void showHelp() {
 
 		System.out.println("Commands:");
 		System.out.println("- find_events: Find all events and save them to csv file");
 		System.out.println("- repos: Create report with repository activity");
 		System.out.println();
 	}
 }
