 /*******************************************************************************
  * Date : 13/04/2012                                                           *
 * Description : This class downloads the tweets of a group of followers given *
  *               a file with the relationship <ID,Screen_name>.                *
  *                                                                             *
  * Execution : SaveTweets <screennamesfile>                                    *     
  * Format ScreenNameFile : screennames_UniverseName_timestamp.txt              *
  *                                                                             *
  * Improvements : 1 - Save instead of (null/true/false) the number of tweets.  *
  *                2 - Allow to try to redownload unauthorized past requests.   *
  *                                                                             * 
  *******************************************************************************/
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 /**
  * @author Bernard Hernández Pérez, Fernando José Iglesias García
  */
 
 public class SaveTweets {
 	
 	/**
 	 * Max number of tweets we want to download for one user.
 	 */
 	public static final int nTweetsToDownload = 50;
 		
 	/**
 	 * Auxiliary structure to keep track of the IDs whose tweets have already been 
 	 * downloaded or we cannot download because their are private.
 	 */
 	public static HashMap<String, Boolean> screenNameToNumberTweets = 
 			new HashMap<String, Boolean>();
 
 	private static int nTweetsResolved = 0;
 
 	private static int nTweetsTotal = 0;
 	
 	private static int nTweetsNew = 0;
 	
 	private static int nTweetsUnauthorized = 0;
 	
 	/**
 	 * Important directories.
 	 */
 	public static String universeDirectory = "TwitterPageRank/data/";
 	
 	/**
 	 * Show some debugging information
 	 */
 	public static final boolean DEBUG_OUT = false;
 	
 	/**
 	 * Download the tweets for all the users of a given file with the
 	 * relationship <ID,Screen_Name>.
 	 */
 	public static void main(String[] args) {
 
 		// Check correct number of input parameters.
 		if (args.length < 1) {
 			System.err.println("usage: SaveTweets <ScreenNamesFile>");
 			return;
 		}
 		
 		// Get parameters.
 		universeDirectory += args[0].split("_")[1];
 		
 		// Read the available information from the file and populate the 
 		// HashMap
 		try {
 
 			File file = new File(universeDirectory + "/" + args[0]);
 			
 			if (file.exists()) {
 				
 				BufferedReader br = new BufferedReader(new FileReader(file));
 				String line;
 				while ( (line = br.readLine()) != null ) {
 					String[] tokens = line.split(";");
 					
 					if (tokens.length < 2) {
 						screenNameToNumberTweets.put(tokens[0], null);
 					} else {
 						if (tokens[1].equals("false")) {
 							screenNameToNumberTweets.put(tokens[0], false);
 							nTweetsUnauthorized++;
 						} else {
 							screenNameToNumberTweets.put(tokens[0], true);
 							nTweetsResolved++;
 						}
 					}
 					
 					nTweetsTotal++;
 				}
 				br.close();
 				
 			} else {
 				System.err.println("File " + universeDirectory + "/" +
 			                       args[0] + " not found!!");
 			}
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		System.out.println("Followers ScreenName file loaded, " + nTweetsResolved + 
 				" resolved IDs, " + nTweetsUnauthorized + " unauthorized IDs, out of " +
 				nTweetsTotal + " IDs in " + args[0].split("_")[1] + ".");
 		
 		if (DEBUG_OUT) {
 			System.out.println(">>>> State of the HashMap after reading");
 			
 			// Display the state of the HashMap
 			Iterator<String> it = screenNameToNumberTweets.keySet().iterator();
 			while (it.hasNext()) {
 				String key = it.next();
 				Boolean value = screenNameToNumberTweets.get(key);
 				if (value!=null)
 					System.out.println(key + " -> " + value);
 				else
 					System.out.println(key);
 			}
 		}
 		
 		// Get the Tweets for the IDs that have not been done yet.
 		Iterator<String>  it = screenNameToNumberTweets.keySet().iterator();
 		while (it.hasNext()) {
 			
 			String key = it.next();
 			Boolean value = screenNameToNumberTweets.get(key);
 			
 			if (value==null) {
 				
 				// Get the JSON Array associated to this follower Screen_name.
 				
 				try {
 					// Get user information in .json
 					URL url = new URL("https://twitter.com/statuses/user_timeline/" +
 							key + ".json?count=" + nTweetsToDownload);
 				
 					BufferedReader urlInput = new BufferedReader(
 								new InputStreamReader(url.openStream()));      	
 			    	
 			    	String content = urlInput.readLine();
 				
 			    	// DEBUG : Knowing the number of tweets.
 			    	if (DEBUG_OUT) {
 					    JSONArray jsonArray = new JSONArray(content);
 					    System.out.println(">>>> User:" + key + " nTweets:" + jsonArray.length());
 			    	}
 				    
 			    	StringBuffer urlOutput = new StringBuffer(content); 
 			    	
 					// Save it in a file (user.json).
 					try {
 						File file = new File(universeDirectory +"/TweetsList/" + key + ".json");
 						BufferedWriter bw = new BufferedWriter(new FileWriter(file));
 						bw.write(urlOutput.toString());		
 						bw.close();			
 					} catch (IOException e) {
 						System.out.println("Error saving " + key + ".txt.");
 						e.printStackTrace();
 					}
 			    						
 				    urlInput.close();
 					
 					// Overwrite the previous value associated to this key
 				    screenNameToNumberTweets.put(key, true);
 
 					nTweetsNew++;
 				} catch (FileNotFoundException e) {
 					System.out.println("ID " + key + " not found!!");
 				} catch (MalformedURLException e) {
 					e.printStackTrace();
 				} catch (IOException e) {					
 					// Get and print error info.
 					int code = Integer.parseInt(e.getMessage().split(" ")[5]);
 					boolean exit = HTTPExceptionHandler(code,key);
 					System.err.println(e.getMessage());
 					
 					if (exit) {
 						saveScreenNamesToTweets(universeDirectory + "/" + args[0]);
 						return;
 					}
 
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 
 			}
 
 		}
 		
 		// All the IDs have been associated
 		System.out.println("Assocation complete! Saving HashMap ...");
 		saveScreenNamesToTweets(universeDirectory + "/" + args[0]);
 	}
 
 	/**
 	 * Method to write the HashSet<ScreenName,boolean> into a file.
 	 */
 	private static void saveScreenNamesToTweets(String fname) {
 
 		if (DEBUG_OUT) {
 			System.out.println(">>>> State of the HashMap before saving");
 
 			// Display the state of the HashMap
 			Iterator<String> it = screenNameToNumberTweets.keySet().iterator();
 			while (it.hasNext()) {
 				String key = it.next();
 				Boolean value = screenNameToNumberTweets.get(key);
 				if (value!=null)
 					System.out.println(key + " -> " + value);
 				else
 					System.out.println(key);
 			}
 		}
 		
 		try {
 			File file = new File(fname);
 			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
 			
 			// Write the associations in the file
 			Iterator<String> it = screenNameToNumberTweets.keySet().iterator();
 			while (it.hasNext()) {
 				String key = it.next();
 				Boolean value = screenNameToNumberTweets.get(key);
 				if (value!=null)
 					bw.write(key + ";" + value + ((it.hasNext() ? "\n" : "")));
 				else
 					bw.write(key + ";" + ((it.hasNext() ? "\n" : "")));
 			}
 			bw.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 	
 	/**
 	 * Method to control all posiblle HTTP errors that could happen.
 	 */
 	private static boolean HTTPExceptionHandler(int code, String key){
 		boolean exit = false;
 		switch (code){
 			case 401 : { System.out.println(code + " : Download " + key + 
 				         " tweets is unauthorized.");
 					     screenNameToNumberTweets.put(key,false);
 						 nTweetsUnauthorized++;
 			           } break;
 			case 400 : { System.out.println(code+ " : Limit reached, " + 
 				         nTweetsNew + " new IDs associated, " + nTweetsUnauthorized +
 				         " unauthorized IDs, saving HashMap ...");
 						 exit = true;
 			           } break;
 			default :  { System.err.println("New error : " + code + " has appeared.");
 			             break;
 			           }
 		}
 		return exit;
 	}
 
 }
