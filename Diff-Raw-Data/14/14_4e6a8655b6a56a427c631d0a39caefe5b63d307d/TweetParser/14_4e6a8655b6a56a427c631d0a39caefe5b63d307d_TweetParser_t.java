 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.simple.JSONValue;
 
 import com.twitter.Extractor;
 import org.apache.commons.io.FileUtils;
 
 import com.larvalabs.megamap.MegaMapException;
 import com.larvalabs.megamap.MegaMapManager;
 import com.larvalabs.megamap.MegaMap;
 
 public class TweetParser {
 
 	// Constant (better put as an input parameter).
	private static String path = "C:\\Users\\Jack\\Dropbox\\KTH-stuff\\DD2476 Search Engines and Information Retrieval Systems\\DD2476 Search Engines and Information Retrieval Project\\EmilJack\\tweets";
	private static String path2 = "C:\\Users\\Jack\\Dropbox\\KTH-stuff\\DD2476 Search Engines and Information Retrieval Systems\\DD2476 Search Engines and Information Retrieval Project\\BerFer\\data\\BowieState\\TweetsList";
 	
 	/** The graph as a hashtables. */
     public static HashMap<String, ArrayList<LinksEntry>> index2 = new HashMap<String, ArrayList<LinksEntry>>();
 	
     private static MegaMap index;
     private static MegaMapManager manager;
     
 	public static void main(String[] args) throws MegaMapException {
 		// Variables.
     	Extractor extractor = new Extractor();   // To extract @user, #hashtag ...		
     	File dokDir = new File(path2);
     	String[] fs = dokDir.list();
 		ArrayList<String> userTweets = new ArrayList<String>();	
 		
 		try {
 		    manager = MegaMapManager.getMegaMapManager();
 		    index = manager.createMegaMap( "graph", path.replace("\\tweets", "\\graph"), true, false );
 		}
 		catch ( Exception e ) {
 		    e.printStackTrace();
 		}
     	
     	//Read all the files from /Users/ directory
     	if (dokDir.canRead()){
     		if (dokDir.isDirectory()){
     			if ( fs != null ) {
     			    for ( int i=0; i<fs.length; i++ ) {
     			    	// Read the files in the directory	
     			    	userTweets.add(loadUserTweets(fs[i]).toString());
     			    	System.out.println(userTweets.get(i));
     			    }
     			}
     		}
     	}
 			
 		// Jason array with JSon Object (each object would be one tweet).
     	for (int i=0; i<fs.length; i++){
     		try {
 	         	JSONArray jsonArray;
 				jsonArray = new JSONArray(userTweets.get(i));
 	            
 				System.out.print("Reading " + fs[i].toString() + " tweets.");
 	    	    System.out.println(" (Total : " + jsonArray.length() + ")");
 	    	 
 		    	// Look the @user references in each of the tweets.
 		    	List<String> namesReferenced;
 		    	
 		    	/**Loop through every tweet for user fs[i]*/
 		    	for (int j = 0; j < jsonArray.length(); j++) {
 		    	
 		    		System.out.print("Tweet " + j + " : ");
 		    		
 		    		// Get JSon Object (tweet) in position j.
 		    		JSONObject jsonObject = jsonArray.getJSONObject(j);
 		    		
 		    		// Get the field "Text" in that tweet. (also are others "id", "created_at", ...").
 		    		namesReferenced = extractor.extractMentionedScreennames(jsonObject.getString("text"));
 		    		
 		    		LinksList links = new LinksList();
		    		LinksList listOfLinksEntry = null;
 		    		
		    		if (index.hasKey(fs[i].replace(".json", ""))){		    			
 		    			listOfLinksEntry = (LinksList) index.get(fs[i].replace(".json", ""));		    			
 		    		}
 		    		
 		    	    for (String name : namesReferenced) {
 		    	        System.out.print(name);
 		    	        if (name!=""){
 		    	        	if (!index.hasKey(fs[i].replace(".json", ""))){
 				    			links.add(name);
 				    			index.put(fs[i].replace(".json", ""),links);		    			
 				    		}
 				    		else{
 				    			boolean tru = false;
 				    			for (int k = 0; k<listOfLinksEntry.size(); k++){
 				    				System.out.println(listOfLinksEntry.get(k).getScreenName());
				    				if (name.toString().equals(listOfLinksEntry.get(k).getScreenName())){				    					
 				    					listOfLinksEntry.get(k).addOne();
 				    					k=listOfLinksEntry.size()+1;
 				    					tru = true;
 				    				}
 				    			}
 				    			if (!tru) listOfLinksEntry.add(name);
 				    		}
 		    	        }
 		    	        
 		    	    }
 		    	    //if (!links.isEmpty()) index.put(fs[i].replace(".txt", ""), links);
 		    	    System.out.println();		    	    
 		    	}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	}
     	/**String serialized = JSONValue.toJSONString(index);
     	try {
 			FileUtils.writeStringToFile(new File("C:\\Users\\Jack\\workspace\\ir-project\\graph.txt"), serialized);
 			System.out.println("SKRIVER UT");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}**/
     	System.out.println();
     	Object[] keylist = getDictionary();
     	
     	//Create File
     	try {
 			FileWriter fstream = new FileWriter(path.replace("\\tweets", "\\graph\\") + "graph.txt");
 			BufferedWriter out = new BufferedWriter(fstream);
 			
 			for (int k=0; k<keylist.length; k++){
 				String outPutRow = keylist[k].toString() + ";";
 	    		try {
 	    			LinksList temp = (LinksList) index.get(keylist[k].toString());
 	    			/**Loop through all LinksEntries that exist in the current LinksList for the current key (screenname)*/
 	    			for (int y = 0; y<temp.size(); y++){
 	    				LinksEntry tempEntry = temp.get(y);
 	    				outPutRow = outPutRow + tempEntry.getScreenName() + "(" + tempEntry.getNumberOfPaths() + "),";	    				
 	    			}
 	    			out.write(outPutRow + "\n");
 					System.out.println(keylist[k].toString() + ": " + index.get(keylist[k].toString()));
 				} catch (MegaMapException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}    		
 	    	}
 			out.close();
 			
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} 	
     	
     	manager.shutdown();
 	}	
 	
 	/**
      *  Returns the dictionary (the set of terms in the index)
      *  as a Object.
      */
     @SuppressWarnings("rawtypes")
 	public static Object[] getDictionary() {
     	Set keys = index.getKeys();
 	   	Object[] keylist= keys.toArray();    	
     	return keylist;
     }
 	
 	@SuppressWarnings("finally")
 	public static String loadUserTweets(String name){
 		String content = "";
 		try {
 			File file = new File(path2 + "/" + name);
 			
 			if (file.exists()){
 				BufferedReader br = new BufferedReader(new FileReader(file));
 				content = br.readLine();
 				br.close();
 			} else {
 				System.out.println("File " + file.getPath() + " doesnt exists.");
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			return content;
 		}
 	}
 }
