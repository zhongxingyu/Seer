 package com.where.hadoop;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * 
  * @author fliuzzi
  * 
  * 
  * LSHinfostrip
  * 		usage: takes in four args:
  * 					input dir of zip bucketed files
  * 					output dir 
  * 					num threads
  *					source name(cs,yelp,yp)   -- used for zip file extensions
  */
 public class LSHinfostrip {
 	
 	BufferedReader reader;
 	ExecutorService thePool;
 	String path;
 	File[] files;
 	String extension;
 	
 	
 	public LSHinfostrip(File[] files, String dirPath, int numThreads,String ex)
 	{
 		this.files = files;
 		this.path = dirPath;
 		extension = ex;
 		thePool = Executors.newFixedThreadPool(numThreads);
 	}
 	
 
 	public void start() throws IOException
 	{
 		
 		for(int i = 0; i < files.length; i++)
 		{
 			final int lcv = i;
 			
 			
 			@SuppressWarnings("unused")
 			Future<?> fut = thePool.submit(
                     new Runnable(){ public void run(){
 			
 				try{
 					
 					readAndWriteFile(new FileReader(files[lcv]),files[lcv].getName());
 					
 				}
 				catch(Exception e)
 				{
 					e.printStackTrace();
 				}
             }});
 		}
 	}
 	
 	
 	public void readAndWriteFile(FileReader stream,String name) throws IOException, JSONException
 	{
 		BufferedReader reader = new BufferedReader(stream);
 		BufferedWriter writer = new BufferedWriter(new FileWriter(path+"/"+name+"."+extension));
 		
 		String line = null;
 		
 		
 		while((line = reader.readLine()) != null) {
 			
 			stripAndWrite(new JSONObject(line), writer);
 			
 		}
 		reader.close();
 		writer.close();
 	}
 	
 	public String cleanPhone(String phone) {
 	    if(phone == null || phone.trim().length() == 0){return phone;}
 		StringBuffer buffer = new StringBuffer();
 		char[] chrs = phone.toCharArray();
 		for(int i = 0, n = chrs.length; i < n; i++) {
 			if(Character.isDigit(chrs[i])) {
 				buffer.append(chrs[i]);
 			}
 		}
 		
 		String tel = buffer.toString();
 		if(tel.length() > 10) tel = tel.substring(0, 9);
 		
 		return tel;
 	}
 	
 	public void stripAndWrite(JSONObject line, BufferedWriter writer) throws JSONException, IOException
 	{
 		JSONObject json = line.optJSONObject("location");
 		if(json != null)
 		{
 			if(line.optString("ypurl").contains("yellowpages"))
 			{
 				//yellowpages
 				json.put("source", "yp");
 				
 				String phone = line.optString("hours");
				if (phone.length() > 0 && phone.contains("Please contact")){
 					phone = phone.substring(phone.indexOf("at ")+3,phone.length()-1);
 					
 					json.put("phone",cleanPhone(phone));
 				}
 				
 				
 				json.put("name",line.optString("name"));
 				json.put("id", line.optString("pid"));
 			}
 			else if(line.optString("ypurl").contains("yelp"))
 			{
 				//yelp
 				json.put("source","yelp");
 				json.put("phone",line.optString("phone"));
 				json.put("name",line.optString("name"));
 				json.put("id", line.optString("pid"));
 			}
 		}
 		else
 		{
 			//lis
 			
 			json = line.optJSONObject("details");
 			JSONArray ids = json.optJSONArray("ids");
 			
 			json = json.optJSONObject("location");
 			
 			json.put("source", "cs");
 			json.put("phone",line.optString("phone"));
 			json.put("name",line.optString("name"));
 			
 			
 			
 			JSONObject csid = (JSONObject) ids.get(0);
 			
 			
 			json.put("id",csid.optString("cs"));
 		}
 		
 		write(json, writer);
 	}
 	
 	public void write(JSONObject json,BufferedWriter writer) throws IOException
 	{
 		writer.write(json.toString());
 		writer.newLine();
 	}
 	
 	
 	public static void main(String[] args) throws FileNotFoundException, IOException {
 		if(args.length != 4) return;
 		
 		File inDir = new File(args[0]);
 		File[] files = inDir.listFiles();
 		
 		
 		
 		
 		LSHinfostrip lshis = new LSHinfostrip(files,args[1],Integer.parseInt(args[2]),args[3]);
 		lshis.start();
 		System.out.println("DONE.");
 
 	}
 
 }
