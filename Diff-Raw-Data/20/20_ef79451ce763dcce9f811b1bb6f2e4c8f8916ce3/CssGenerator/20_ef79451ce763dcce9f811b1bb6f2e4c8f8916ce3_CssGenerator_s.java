 package com.powerlanguage.rultimatecss;
 /*
  * Parses a folder of images and produces css compatible with reddit
 * Images will be displayed randomly based on the usertext form name 
  */
 
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
/* TODO
 * Text will be generated using the file name, so it should be exact
 * format the output of image file names to match reddits conversion
 * 
 * Have a file of photographers and their URLS
 * Setup a key > value dict so that each url is matched to a photographer
 * 
 */

 public class CssGenerator {
 
 public String fileDir = "data/images/";
 public String pgInfo = "data/photographer-info.txt";
 
 public ArrayList<String> filenames = new ArrayList<String>();
 public HashMap<String, String> photographerNameURLMap = new HashMap<String,String>();
 public ArrayList<String> ids = new ArrayList<String>();
 
 	public CssGenerator(){
 		
 	}
 	
 	public void run() throws IOException{
 		getFilenames();
 		getPhotographerInfo();
 		printSidebarText();
 		generateIds();
 		ArrayList<String> loopedFilenames = getLoopedFilenames();
 		generateCSS(loopedFilenames);
 		
 	}
 	
 	public void generateCSS(ArrayList<String> loopedFilenames){
 		System.out.println("/** Begin image and credit CSS **/ \n");
 		
 		for(int i = 0; i < ids.size(); i++){
 			
 			String id  = ids.get(i);
 			
 			String rawFilename = loopedFilenames.get(i);
 			//remove suffix
 			String filename = rawFilename.substring(0, rawFilename.indexOf("."));
 			//separate photographer name and photo number
 			String[] pair = filename.split("_");
 			String photographerName = pair[0];
 			String photoNum = pair[1];
 			
 			//get external url associated with photographer
 			String externalURL = photographerNameURLMap.get(photographerName);
 			
 			//replace spaces with dashes
 			String redditFormattedName = photographerName.replace(' ', '-');
 			//generate reddit formatted URL
 			String redditURL = "%%" + redditFormattedName + "-" + photoNum + "%%";
 			
 			StringBuilder sb = new StringBuilder(); //adding image
 			sb.append(String.format(".listing-page .side .usertext[id$=\"%s\"] a[href*=\"/random-image\"]::before {", id));
 			sb.append("\n");
 			sb.append(String.format("content: url(%s);", redditURL));
 			sb.append("\n");
 			sb.append("}");
 			sb.append("\n \n");//adding credit
			sb.append(String.format(".listing-page .side .usertext[id$=\"%s\"] a[href^=\"%s\"]::after {", id, externalURL));
 			sb.append("\n");
 			sb.append(String.format("content: \"Photo by %s, Ultiphotos\";", photographerName));
 			sb.append("\n");
 			sb.append("}");
 			sb.append("\n \n");
 			
 			System.out.print(sb);
 					
 		}
 		System.out.println("/** End image and credit CSS **/");
 
 	}
 	
 	public void getFilenames(){
 		File folder = new File(fileDir);
 		File[] listOfFiles = folder.listFiles();
 		for (File file : listOfFiles) {
 		    if (file.isFile() && file.getName().charAt(0) != '.') {
 		        filenames.add(file.getName());
 		    }
 		}
 	}
 	
 	public void getPhotographerInfo() throws IOException{
 		BufferedReader in = new BufferedReader(new FileReader(pgInfo));
 
 		while (in.ready()) {
 		  String s = in.readLine();
 		  String[] splits = s.split(",");
 		  String name = splits[0];
 		  String url = splits[1];
 		  photographerNameURLMap.put(name, url);
 		}
 		in.close(); 
 
 	}
 	
 	public void printSidebarText(){
 		System.out.println("/** Begin sidebar text **/");
 		System.out.println("");
 		for(String url : photographerNameURLMap.values()){
 			System.out.println("[](" + url + ")");
 		}
 		
 		System.out.println("");
 		System.out.println("/** End sidebar text **/");
 		System.out.println("");
 	}
 	
 	public void generateIds(){
 		for(int i='a';i<='z';i++){
 			ids.add(Character.toString( (char)i) );
 		}
 		for(int j=0; j<=9; j++){
 			ids.add(Integer.toString (j) );
 		}
 		//System.out.println(ids);
 	}
 	
 	public ArrayList<String> getLoopedFilenames(){
 		ArrayList<String> loopedFilenames = new ArrayList<String>();
 		//looping num and iteration allow for more ids than images
 		int iteration = 0;
 		int loopingNum = 0;
 		for(int i=0; i<ids.size(); i++){
 			if(loopingNum >= filenames.size()-1){
 				iteration++;
 			}
 			loopingNum = (i - (filenames.size() * iteration));
 			//System.out.println("i: " + i + " it: " + iteration + " ln: " + loopingNum);
 			loopedFilenames.add(filenames.get(loopingNum));
 		}
 		return loopedFilenames;
 	}
 }
