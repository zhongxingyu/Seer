 import java.net.*;
 import java.io.*;
 import java.util.*;
 import java.text.DateFormat;
 
 public class Utils
 {
 	public Utils()
 	{
 	}
 	
 	public String time(String message)
 	{
 		DateFormat df = DateFormat.getInstance();
 		String time = df.format(new java.util.Date());
 		return time;
 	}
 
 	public String about(String owner)
 	{
 			return "I am a bot written in java, operated by "+ owner;
 	}
 	public String getTitle(String message)
 	{
 		String urlGrab = message.substring(message.indexOf("http"), message.length());
 		urlGrab = urlGrab.split(" ")[0];
 		return "URL TITLE: " + scrapeTitle(urlGrab);
 	}
 
 	public String scrapeTitle(String grabbedURL)
 	{
 		String result = "";
 		try{ 
 			URL url = new URL(grabbedURL);
 			URLConnection conn = url.openConnection();
 			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			String line;
 			String prevLine = "";	
 			while ((line = rd.readLine()) != null) 
 			{
 				if(prevLine.indexOf("</title>") < 0)
 				{
 					result += line + "\r\n";
 					prevLine = line;
 				}
 				else{rd.close();}
 			}
 			rd.close();
 		}
 		catch (Exception e) 
 		{
 			System.out.println(e.getMessage());
 		}
 
 		result = result.split("<title>")[1].split("</title>")[0];
		result = result.replace("&amp;", "&");
 		return result;
 	}
 	
 	public String test()
 	{
 		return "We thank you for testing with Aperture Science!";
 	}
 
 }
