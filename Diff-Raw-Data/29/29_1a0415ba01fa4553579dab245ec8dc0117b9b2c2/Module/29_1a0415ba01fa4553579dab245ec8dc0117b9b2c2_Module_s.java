 package main;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.LinkedList;
 
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
 			URL url = new URL(course_urls.get(i));
 			URLConnection urlConnection = url.openConnection();				
			InputStream inputStream = (InputStream) urlConnection.getContent();
			if(urlConnection.getContentLength() > 0)
			{
				byte[] contentRaw = new byte[urlConnection.getContentLength()];
				inputStream.read(contentRaw);
				String content = new String(contentRaw);
				System.out.println(content);
				System.out.println("---------------------------------------------------");
			}
			else
			{
				System.out.println("ERROR AT THE SITE : "+course_urls.get(i));
			}
 		}
 	}
 }
