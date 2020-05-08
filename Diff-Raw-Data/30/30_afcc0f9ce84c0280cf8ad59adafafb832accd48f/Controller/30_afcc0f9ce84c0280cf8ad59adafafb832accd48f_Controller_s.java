 package app.controller;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.InetAddress;
 import java.net.Socket;
 
 // Controller class to provide functionality like File Export and Network connectivity
 // Follows Singleton Pattern
 //
 public class Controller {
 	
 	// static class instance
 	private static Controller instance = new Controller();
 	
 	private ApplicationState state;
 	private String csvData;
 	
 	// constructor
 	private Controller()
 	{
 		state = ApplicationState.INITIAL;
 	}
 	
 	// Get Singleton instance
 	public static Controller getInstance()
 	{
 		return instance;
 	}
 	
 	// Connect to the Collecting Node for read out
	public boolean connect(byte ip[], int port)
 	{
 		String csv = null;
 		
 		try
 		{
			InetAddress addr = InetAddress.getByAddress(ip);
 			if(!addr.isReachable(1500))
 			{
 				// selected Machine is unavailable
 				return false;
 			}
 				
 			// Socket on which the Client is operating
 			Socket clientSocket = new Socket(addr, port);
 			BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
 			
 			csv = "";
 			while(inFromServer.ready()) // while Lines are available
 			{
 				// read line and store in csv
 				csv = csv + inFromServer.readLine() + "\n";
 			}
 			
 			// close Connection
 			clientSocket.close();
 		}
 		catch(IOException e){
 			System.out.println(e);
 		}
 		if (csv != null)
 		{
 			// read in successfull, store CSV Data and set Applications State to initialized.
 			csvData = csv;
 			state = ApplicationState.INITIALIZED_WITH_DATA;
 			return true;
 		}
 		//read in unsuccessfull
 		return false;
 	}
 	
 	// Export CSV Data to File
 	public void exportCSV(File file)
 	{
 		BufferedWriter out;
 		try
 		{
 			out = new BufferedWriter(new OutputStreamWriter(
 					  			 new FileOutputStream(file)));
 
 			out.write(csvData);
 
 			out.close();
 		}
 		catch(IOException e)
 		{
 			System.out.println(e);
 		}
 	}
 	
 	// Get applications State
 	public ApplicationState getState()
 	{
 		return state;
 	}
 
 	// Get CSV Data String
 	public String getCSVData()
 	{
 		return csvData;
 	}
 }
