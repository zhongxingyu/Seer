 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 
 /**
  * This class is a naive implementation of a flat-file database (DB) for the
  * RT2witter application. 
  * 
  * This class offers the following functionalities: 
  * <ul>
  * 	<li>Choose where the DB is stored on disk</li>
  *  <li>Save a user into the DB</li>
  *  <li>Load all the user from the database</li>
  *  <li>Load all the tweets of a user from the database</li>
  * </ul>
  * 
  * This class can be improved by using standard containers class instead of 
  * primitive arrays
  * 
  * @author Didier Hoareau
  * 
  */
 public class UserDB {
 
 	private static String db_prefix ="/dev/null/";
 	private static String file_suffix =".rt2";
 
 
 	public static void setPrefix(String s) {
 		File f = new File(s) ;
 		if (f.isDirectory()) {
 			UserDB.db_prefix = s ;
 		} else {
 			System.out.println("** Error: "+s+" is not a directory.") ;
 		}
 
 	}
 
 
 	/**
 	 * Save all the user data to a file. The file created has a name of the 
 	 * form 'login.txt' where 'login' is the login of the user.
 	 * @param u The user whose data  has to be serialized
 	 * @return true if the the operation succeed, false otherwise.
 	 */
 	public static boolean saveUser(UserItf u) {
 
 
 		try {
 			PrintWriter pw = new PrintWriter(new FileWriter(db_prefix + "/"+ u.getLogin()+file_suffix)) ;
 			pw.println(u.getPassword()) ;
 			String[] followings = u.getFollowings() ;
 			String[] followers = u.getFollowers() ;
 			for (int i=0 ; i<followings.length ; i++) {
 				pw.print(followings[i]) ;
 				if (i!=followings.length -1) {
 					pw.print(":") ;
 				}
 			}
 			pw.println() ;
 			
 			for (int i=0 ; i<followers.length ; i++) {
 				pw.print(followers[i]) ;
 				if (i!=followers.length -1) {
 					pw.print(":") ;
 				}
 			}
 			pw.println() ;
 			
 			String[] tweets = u.getAllTweetAsString() ;
 			pw.println(tweets.length) ;
 			
 			for (int i=0 ; i<tweets.length ; i++) {
 				pw.println(tweets[i]) ;
 			}
 			pw.close() ;
 			return true ; 
 		} catch (IOException e) {
 			System.out.println("** ERROR: Impossible to save user data") ;
 			System.out.println("** You should check the db_prefix.") ;
 			return false ;
 		}
 	}
 
 
 	/**
 	 * Load in a 2-dimensional array all the users that have been saved. 
 	 * 
 	 * @return The array of users. Each entry is an array of length 2. 
 	 * The first index (result[i][0]) contains the login of the user and the second one
 	 * (result[i][1]) contains the password. 
 	 */
 	public static String[][] loadAllUser() {
 
 		File f = new File(db_prefix) ;
 		if (!f.exists()) {
 			System.out.println("** Error: the db_prefix is not correctly set") ;
 			return null ;
 		}
 
 		String[] usersFile  = f.list() ;
 		String[][] users = new String[usersFile.length][2] ;
 
 		BufferedReader reader = null ;
 		for(int i=0 ; i<usersFile.length ; i++) {
 			String login = usersFile[i].substring(0, usersFile[i].length() - UserDB.file_suffix.length()) ;
 
 			try {
 				reader = new BufferedReader(new FileReader(UserDB.db_prefix + "/" + usersFile[i])) ;
 				String password = reader.readLine() ;
 				users[i][0] = login ;
 				users[i][1] = password ;
 
 				reader.close() ;
 
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 		return users ;
 
 	}
 
 	/**
 	 * 
	 * @param userLogin The corresponding login for which we want to retrieve the tweets. 
	 * @return 2D-Array: 
	 * <ul>
	 * </ul> 
 	 */
 	public static String[][] loadTweets(String userLogin) {
 		String[][] result = null ;
 		
 		try {
 			BufferedReader reader = 
 				new BufferedReader(new FileReader(UserDB.db_prefix + "/" + userLogin + UserDB.file_suffix )) ;
 		
 			int nbLineToSkip = 3 ;
 			UserDB.skipLines(nbLineToSkip, reader) ;
 			
 			int nbTweets  = Integer.parseInt(reader.readLine()) ;
 			result = new String[nbTweets][2] ;
 			int j=0 ;
 			String[] split ;
 			while (j<nbTweets) {
 				split = reader.readLine().split(":") ;
 				result[j][0] = split[0] ;
 				result[j][1] = split[1] ;
 				j++ ;
 			}
 			reader.close() ;
 			
 			return result ;
 
 		} catch(IOException e) { 
 			e.printStackTrace() ; 
 			return null ;
 		}
 
 	}
 	
 	public static String[] loadFollowers(String userLogin) {
 		String[] result = null ;
 		
 		try {
 			BufferedReader reader = 
 				new BufferedReader(new FileReader(UserDB.db_prefix + "/" + userLogin + UserDB.file_suffix )) ;
			int nbLineToSkip = 2 ;
 			UserDB.skipLines(nbLineToSkip, reader) ;
 			result = reader.readLine().split(":") ;
 			reader.close() ;
 			
 		} catch(IOException e) {
 			e.printStackTrace() ;
 		}
 		return result ;
 	}
 	
 	public static String[] loadFollowings(String userLogin) {
 		String[] result = null ;
 		
 		try {
 			BufferedReader reader = 
 				new BufferedReader(new FileReader(UserDB.db_prefix + "/" + userLogin + UserDB.file_suffix )) ;
			int nbLineToSkip = 1 ;
 			UserDB.skipLines(nbLineToSkip, reader) ;
 			result = reader.readLine().split(":") ;
 			reader.close() ;
 			
 		} catch(IOException e) {
 			e.printStackTrace() ;
 		}
 		return result ;
 	}
 	
 	
 	
 	private static void skipLines(int nbLines, BufferedReader r) throws IOException {
 		for (int i=0 ; i<nbLines; i++) {
 			r.readLine() ;
 		}
 	}
 	
 
 }
