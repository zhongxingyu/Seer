 /*
 	Copyright 2013, Subhankar Sahu
 
 	Author(s): Subhankar Sahu                                 
 	Public Repo: https://github.com/kensuvy/Sabdakon                                                
 
 	This file is part of Sabdakon.
 
     	Sabdakon is free software: you can redistribute it and/or modify
    	it under the terms of the GNU General Public License as published by
     	the Free Software Foundation, either version 3 of the License, or
     	(at your option) any later version.
 
     	Sabdakon is distributed in the hope that it will be useful,
     	but WITHOUT ANY WARRANTY; without even the implied warranty of
     	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     	GNU General Public License for more details.
 
     	You should have received a copy of the GNU General Public License
     	along with Sabdakon.  If not, see <http://www.gnu.org/licenses/>.
 	
 */
 
 /*	
  	SabdWord.java	
 
   	This class receives data from WordNetWeb.
 */
 
 import java.util.Scanner;		//for getting input from commandline
 import java.net.URL;			//class representing a URL
 import java.net.URLConnection;		//abstract class, represents connection between app and URL
 import java.net.MalformedURLException;
 import java.io.InputStreamReader;	//converts bytes to characters using a specified charset
 import java.io.IOException;
 
 
 public class SabdWord
 {
 	private String word;
 	private String meaning;
	private String formattedOutput="";
 
 	private void getFromURL(String word)
 	{
 		URL url;
 		String urlstring = "http://wordnetweb.princeton.edu/perl/webwn?s="+word+"&sub=Search+WordNet&o2=&o0=1&o8=1&o1=1&o7=&o5=&o9=&o6=&o3=&o4=&h=0"; 
 		try 
 		{
 			url = new URL(urlstring);
 	 
 			URLConnection conn = url.openConnection();
  
                         Scanner urlContent = new Scanner(conn.getInputStream());
 			while(urlContent.hasNext())
 			{
 				meaning += urlContent.nextLine();
 				
 			}
 		}
  
 		
 		catch(MalformedURLException e) 
 		{
 			e.printStackTrace();
 		} 
 		catch(IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	private void formatResult()
 	{
 		int startIndex = 0;
 		int stopIndex = 0;
 
		formattedOutput+="<h2>WordNet</h2>";
 
 		if(meaning.indexOf("Your search did not return any results.")>=0)
 		{
 			formattedOutput+="<b>"+word+": Error: Word Not Found</b>";
 		}
 		else 
 		{
 			if(meaning.indexOf("sentence\" </div>")>=0) 
 				startIndex = meaning.indexOf("sentence\" </div>")+16;
 			if(meaning.indexOf("</body>")>=0)
 				stopIndex = meaning.indexOf("</body>");
 			formattedOutput+=meaning.substring(startIndex, stopIndex)+"<hr/>";
			formattedOutput = formattedOutput.replace("S:", " ");
 		}
		
 	}
 
 	public void setWord(String s)
 	{
 		word = s;
 	}
 
 	public String getFormattedOutput()
 	{
 		getFromURL(word);
 		formatResult();
 		return formattedOutput;
 	}
 
 }
