 package main;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.InputStream;
import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
 public class Module 
 {
 	private static LinkedList<String> course_urls = new LinkedList<String>();
 	public static void main(String args[])throws Exception
 	{
 		//READING ALL INPUT URLS FROM A TEXT FILE 
 		BufferedReader br = new BufferedReader(new FileReader(new File("./input.txt")));
 		String s="";
 		while((s=br.readLine()) != null)
 			course_urls.add(s);
 		br.close();
 		System.out.println("---------------------------------------------------");
 		
 		//GETTING CONTENT OF EACH OF THE URLS 
 		for(int i=0;i<course_urls.size();i++)
 		{
			System.err.println(course_urls.get(i));
			Thread.sleep(5000);
 			URL url = new URL(course_urls.get(i));
 			URLConnection urlConnection = url.openConnection();				
			InputStream is = url.openConnection().getInputStream();
			BufferedReader reader = new BufferedReader( new InputStreamReader( is )  );
	        String line = null;
	        while( ( line = reader.readLine() ) != null )  
	        {
	        	System.out.println(line);
	        }
		    reader.close();
 		}
 	}
 }
