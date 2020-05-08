 package cognex;
 
 import java.io.*;
 import java.net.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * @author Robinson Levin [robinson.levin@cognex.com]
  */
 public class SummitTester {
 
     public static String readerName = "10.0.3.40"; // change this to the host name of your reader (or IP)
     public static int readerTCPPort = 23;
 
     public static void main(String[] args) {
 
	String filename_withDateTime = "DataLog_" + new SimpleDateFormat("YYYYMMDD_HHmmss").format(new Date()) + ".txt";
 	//Need to create a printwriter here, so we don't log the time it takes to open the file.
 	PrintWriter out = null;
 	try {
 	    out = new PrintWriter(new BufferedWriter(new FileWriter(filename_withDateTime, true)));
 	    //Print the header for sanity
 	    out.println("StartDate EndDate TimeMS Response");
 	    out.flush();
 	} catch (IOException ex) {
 	    System.out.println("Error opening the file: " + filename_withDateTime + " " + ex.getMessage());
 	    System.exit(1);
 	}
 
 	ExceptionHandler exHandler = new ExceptionHandler();
 
 	Socket datamanSocket;
 	PrintWriter datamanWriter;
 	BufferedReader datamanReader;
 	DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
 
 	boolean waitingForResult = true;
 	int cycles = 0;
 
 	System.out.println("Type \"exit\" to stop the test");
 
 	BackgroundReader consoleBackgroundReader = new BackgroundReader(new BufferedReader(new InputStreamReader(System.in)));
 	consoleBackgroundReader.start();
 
 	while (true) {
 	    // progress tracking, you could set the printout to 1000 or 10000 as well
 	    //Need to do all logging prior to setting the start date... logging is expensive
 	    if (cycles % 100 == 0) {
 		System.out.println(df.format(new Date()) + "   Cycles so far: " + cycles);
 	    }
 	    // reset the socket connection
 	    datamanSocket = null;
 	    datamanWriter = null;
 	    datamanReader = null;
 
 	    //Get the start date here.
 	    Date startDate = new Date();
 
 	    // OPEN SOCKET
 	    try {
 		datamanSocket = new Socket(readerName, readerTCPPort);
 		datamanWriter = new PrintWriter(datamanSocket.getOutputStream(), true);
 		datamanReader = new BufferedReader(new InputStreamReader(datamanSocket.getInputStream()));
 
 		datamanSocket.setSoTimeout(60000);
 
 	    } catch (Exception e) {
 		exHandler.handleException(e);
 	    }
 
 	    // TRIGGER READER ~ Also, need to force the <CR><LF> (no println)
 	    datamanWriter.print("||>TRIGGER ON\r\n");
 	    //Have to flush the buffer on non-windows machines.
 	    datamanWriter.flush();
 
 	    // WAIT FOR RESULT
 	    waitingForResult = true;
 	    while (waitingForResult) {
 		try {
 		    String response = datamanReader.readLine();
 		    waitingForResult = false;
 		    //get the end date
 		    Date endDate = new Date();
 		    // log to a file
 		    try {
 			out.println("[" + df.format(startDate) + "] [" + df.format(endDate) + "] " + (endDate.getTime() - startDate.getTime()) + " " + response);
 			//Have to flush the buffer on non-windows machines.
 			out.flush();
 		    } catch (Exception e) {
 			exHandler.handleException(e);
 		    }
 
 		} catch (Exception e) {
 		    exHandler.handleException(e);
 		}
 	    }
 
 
 	    // CLOSE THE SOCKET
 	    try {
 		datamanWriter.close();
 		datamanReader.close();
 		datamanSocket.close();
 	    } catch (Exception e) {
 		exHandler.handleException(e);
 	    }
 
 
 	    if (consoleBackgroundReader.exitSignal) {
 		break;
 	    }
 
 	    cycles++;
 	}
 
 
 	System.out.print("Quitting... ");
 	//Close the file
 	out.flush();
 	out.close();
 	consoleBackgroundReader.stop();
 
 	System.out.println("Done.");
 
 	System.exit(0);
     }
 }
