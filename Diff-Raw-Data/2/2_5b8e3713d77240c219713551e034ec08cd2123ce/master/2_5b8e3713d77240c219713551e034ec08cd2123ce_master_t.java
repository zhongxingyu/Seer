 import java.io.IOException;
 
 import misc.Log;
 import socket.ServerList;
 import thread.*;
 
 public class master
 {
 	private static threading thr_lib;
	final static String version = "Beta 0.6.3e";
 	
 	public master()	{}
 	
 	public static void main(String args[]) throws IOException, InterruptedException
 	{
 		Log.outString("FSS Com Client version " + version);
 		
 		new ServerList();
 		thr_lib = new threading();
 		thr_lib.start();
 	}
 	
 	protected void finalize()
 	{
 		thr_lib.interrupt();
 	}
 }
