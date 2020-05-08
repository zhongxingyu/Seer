 package manager;
 
 public class Reporter {
 
 	Thread theTask;
 	
 	public Reporter(String user, String password, String database, String host, String table) 
 	{
 		theTask = new ReporterTask(user, password, database, host, table);
 		theTask.start();
 	}
 }
