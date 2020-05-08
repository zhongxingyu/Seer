 package server.database;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 public class DataServer 
 {
 	private URL url = null;
 	private BufferedReader in = null;
 	
 	public String sendDataWithReturn(String data)
 	{
 		try 
 		{ 
 			String returnINFO = "";
 			url = new URL(data); 
 			in = new BufferedReader(new InputStreamReader(url.openStream())); 
 			String inputLine; 
 			while ((inputLine = in.readLine()) != null)  
 			{
 				returnINFO += inputLine;
 			}  
			return returnINFO;
 		} 
 		catch (MalformedURLException e) 
 		{ System.out.println("Cannot connect to server"); }
 		catch (IOException e) 
 		{ System.out.println("Cannot read from server"); }
 		
 		return "";
 	}
 	
 	public boolean sendData(String data)
 	{
 		try 
 		{ 
 			url = new URL(data); 
 			in = new BufferedReader(new InputStreamReader(url.openStream())); 
 			return true;
 		} 
 		catch (MalformedURLException e) 
 		{ System.out.println("Cannot connect to server"); }
 		catch (IOException e) 
 		{ System.out.println("Cannot read from server"); }
 		
 		return false;
 	}
 }
