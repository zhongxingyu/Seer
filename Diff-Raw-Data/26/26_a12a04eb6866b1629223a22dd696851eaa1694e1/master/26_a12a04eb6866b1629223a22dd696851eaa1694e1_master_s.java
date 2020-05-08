 import misc.Log;
 
 import thread.*;
 public class master 
 {
	final static String version = "Alpha 0.4.8c";
 	
 	public static void main(String args[])
 	{
 		Log.outString("FSS Com Server version " + version);
 		Log.outError("Test des logs erreur...");
 		thr_listen lst = new thr_listen();
 		lst.start();
 		thr_database dbt = new thr_database();
 		dbt.start();
 		thr_sesshandler tsh = new thr_sesshandler();
 		tsh.start();
 	}
 }
 
