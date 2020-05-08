 package enderdom.eddie.tasks.database;
 
 import java.io.File;
 import java.util.Properties;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 
 import enderdom.eddie.databases.manager.DatabaseManager;
 
 import enderdom.eddie.bio.homology.blast.BlastxHelper;
 import enderdom.eddie.bio.homology.blast.MultiblastParser;
 
 import enderdom.eddie.tasks.TaskState;
 import enderdom.eddie.tasks.TaskXT;
 import enderdom.eddie.tools.Tools_CLI;
 import enderdom.eddie.tools.Tools_String;
 import enderdom.eddie.tools.Tools_System;
 
 public class Task_Blast extends TaskXT{
 
 	private boolean fuzzynames;
 	private String input;
 	private File[] files;
 	private boolean[] ignore;
 	DatabaseManager manager;
 	private String dbname;
 	private String date;
 	private int run_id;
 	private double errorperc;
 	//filecount, fileerror, fileskip, hspcount-up, hspcount-skip, hspcount-error
 	private int[] counts;
 	
 	public Task_Blast(){
 		setHelpHeader("--This is the Help Message for the the blast Task--");
 		run_id =-1;
 		counts = new int[]{0,0,0,0,0,0};
 	}
 	
 	public void printHelpMessage(){
 		Tools_CLI.printHelpMessage(getHelpHeader(), Tools_System.getNewline()+"*Blasts within a 3 week date range that have identical parameters will be grouped together" +
 				"within the database. Alternatively you can force a run id to group blasts together." +
 				" This is needed so you can bulk retrieve statistics for a 'blast run' on say, an assembly."
 				+Tools_System.getNewline()+"-- Share And Enjoy! --", this.options);
 	}
 	
	public void parseArgsSub(CommandLine cmd){
		if(cmd.hasOption("db"))dbname=cmd.getOptionValue("db");
 		if(cmd.hasOption("db"))dbname=cmd.getOptionValue("db");
 		if(cmd.hasOption("i"))input=cmd.getOptionValue("i");
 		if(cmd.hasOption("f"))fuzzynames = true;
 		if(cmd.hasOption("run_id")){
 			Integer g = Tools_String.parseString2Int(cmd.getOptionValue("run_id"));
 			if(g !=  null)run_id =g;
 			else ui.error("Run id should be integer only, crap in, crap out");
 		}
 		if(cmd.hasOption("date"))date=cmd.getOptionValue("date");
 	}
 	
 	public void buildOptions(){
 		super.buildOptions();
 		options.addOption(new Option("r","run_id", true, "Force set run id"));
 		options.addOption(new Option("date", true, "Set date when blast was run*, use format "+Tools_System.SQL_DATE_FORMAT+", [RECOMMENDED]"));
 		options.addOption(new Option("i","input", true, "Input folder or file"));
 		options.addOption(new Option("db","dbname", true, "Default database name, such as nr, swiss-prot etc. If not set, will attempt to get from blast file"));
 		options.addOption(new Option("f","fuzzy", false, "Check for fuzzy names before failing, " +
 				"may be help if blast query-id is different from database id. May lead to incorrect uploads though "));
 	}
 	
 	public void parseOpts(Properties props){
 		Double j = Tools_String.parseString2Double(props.getProperty("MAXERRORPERC"));
 		if( j != null){
 			errorperc=j;
 		}
 		else{
 			props.setProperty("MAXERRORPERC", 0.1+"");
 			errorperc=0.1;
 		}
 	}
 	
 	public Options getOptions(){
 		return this.options;
 	}
 
 	public void run(){
 		setCompleteState(TaskState.STARTED);
 		logger.debug("Started running Assembly Task @ "+Tools_System.getDateNow());
 		File in=null;
 		if(input !=null)in = new File(input);
 		this.checklist = openChecklist(ui);
 		if(in == null || !in.exists()){
 			ui.error("File "+input+" does not exists");
 			return;
 		}
 		else{
			manager = ui.getDatabaseManager(password);
 			try{
 				manager.open();
 				if(in.isDirectory()){
 					files = in.listFiles();
 					ignore = new boolean[files.length];
 					if(checklist.inRecovery()){
 						trimRecovered(checklist.getData());
 					}
 					int i=0;
 					for(;i < files.length; i++){
 						if(!ignore[i]){
 							try{
 								int[] vals = uploadBlastFile(manager, files[i], this.fuzzynames, this.dbname, run_id, date);
 								counts[0]++;
 								if(vals[0] ==-1){
 									counts[5]++;
 								}
 								else{
 									counts[3]+=vals[0];
 									counts[4]+=vals[1];
 								}
 							}
 							catch(Exception e){
 								ui.error("Failed to parse file " + files[i].getName(),e);
 								counts[1]++;
 							}
 							checklist.update(files[i].getName());
 							if(counts[1] > 5){
 								double d = (double)counts[1]/(double)counts[0];
 								if(d >errorperc){
 									logger.error("Error count has reached >5 and 10% of files");
 									this.setCompleteState(TaskState.ERROR);
 									return;
 								}
 								
 							}
 						}
 						else{
 							counts[2]++;
 						}
 						System.out.print("\rFile No.: "+i+" 		");
 					}
 					System.out.println();
 					System.out.println("#####################################################");
 					System.out.println("--Blast Parsing--");
 					System.out.println("Parsed:"+counts[0]+" Skipped:"+counts[2]+" Errored:"+counts[1]);
 					System.out.println();
 					System.out.println("--Blast Matches Upload--");
 					System.out.println("Uploaded:"+counts[3]+" Skipped:"+counts[4]+" Errored:"+counts[5]);
 					System.out.println("#####################################################");
 					System.out.println();
 					System.out.println();
 				}
 				else{
 					try{
 						uploadBlastFile(manager, in, this.fuzzynames, this.dbname, run_id, date);
 					}
 					catch(Exception e){
 						ui.error("Failed to parse file " + in.getName(),e);
 						setCompleteState(TaskState.ERROR);
 						return;
 					}
 				}
 				checklist.complete();
 			}
 			catch(Exception e){
 				logger.error("Failed database or somethinG",e);
 				setCompleteState(TaskState.ERROR);
 				return;
 			}
 		}
 		logger.debug("Finished running Assembly Task @ "+Tools_System.getDateNow());
 	    setCompleteState(TaskState.FINISHED);
 	}
 	
 	public static int[] uploadBlastFile(DatabaseManager manager, File file, boolean fuzzynames, String dbname, int run_id, String date) throws Exception{
 		//Values to return index 0=hsps up, 1 hsps skip, 2 blasts within the file
 		int[] values = new int[]{0,0,0};
 		MultiblastParser parse = new MultiblastParser(MultiblastParser.BASICBLAST, file);
 		while(parse.hasNext()){
 			BlastxHelper helper = new BlastxHelper(parse.next());
 			if(dbname == null){
 				dbname = helper.getBlastDatabase();
 			}
 			helper.setRun_id(run_id);
 			helper.setDate(date);
 			int[] j = helper.upload2BioSQL(manager, fuzzynames, dbname);
 			values[0]+=j[0];
 			values[1]+=j[1];
 			values[2]++;
 		}
 		return values;
 	}
 	
 	private void trimRecovered(String[] data){
 		for(int i =0;i < data.length; i++){
 			for(int j =0; j < files.length; j++){
 				if(data[i].equals(files[i].getName())){
 					this.ignore[i] = true;
 					break;
 				}
 			}
 		}
 	}
 }
