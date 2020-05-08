 package client;
 
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import ggTCalculator.LogPOA;
 
 public class LogImpl extends LogPOA
 {
 	String file = "";
 	String row = "";
 
 
 	@Override
 	public void log(String user, String msg) {
 		// TODO Auto-generated method stub
 		/* Print Log Entry and add to File List */
 		System.out.println(user + " : " + msg);
 		file += user + " : " + msg + "\n";
 		
 		/* Create Log File */
 		try
 		{
			File file1 = new File(new SimpleDateFormat("HHmmss").format(new Date()) +".log");
			FileWriter fw = new FileWriter(file1);
 			fw.write(this.file);
 			fw.flush();
 			fw.close();
 		}
 		catch( IOException e )
 		{
 			e.printStackTrace();
 		}
 		
 	}
 }
