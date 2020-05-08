 /**
  * Contains some common constants and helper functions that are used
  * in our application
  * 
  * @author Dan Sanders, 4/29/13
  *
  */
 
 package com.huskysoft.interviewannihilator.util;
 
 import static com.huskysoft.interviewannihilator.
 util.NetworkConstants.AMPERSAND;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 
 import com.huskysoft.interviewannihilator.model.Likeable;
 
 public class Utility {
 	
 	/**
 	 * Dummy email address to use in debug mode
 	 */
 	public static final String DEBUG_USER_EMAIL = "debug@huskysoft.com";
 	
 	/**
 	 * The name of the file in which user info is stored
 	 */
 	public static final String USER_INFO_FILENAME = "userInfo.txt";
 	
 	/**
 	 * String to fetch Google accounts from the device
 	 */
 	public static final String ACCOUNT_TYPE_GOOGLE = "com.google";
 			
 	/**
 	 * Default encoding for File I/O
 	 */
 	public static final String ASCII_ENCODING = "ASCII";
 	
 	/** The minimum number of likes needed to have to have a valid positive 
 	 * rating */
 	private static final int MIN_LIKES = 5;
 	
 	/**
 	 * Calculates the rank based on the number of likes and
 	 * dislikes of the question
 	 * 
 	 * @return a float representing the rating. This is
 	 * calculated based on the ratio of likes to dislikes, as long as the
 	 * question has at least a minimum number of likes. If it doesn't, 
 	 * then it is given a negative rating, which starts at the negative of
 	 * the minimum number and increments for every like it
 	 * gets (until it reaches the min. # of likes).
 	 */
 	public double getRank(Likeable obj) {
 		int likes = obj.getLikes();
 		int dislikes = obj.getDislikes();
 
 		if (likes < MIN_LIKES) {
 			return (-1 * MIN_LIKES) + likes;
 		}
 		if (dislikes == 0) {
 			return likes;
 		}
 		return ((double) likes / dislikes);
 	}
 	
 	private static String convertStreamToString(InputStream is) 
 			throws IOException {
 		InputStreamReader isReader = new InputStreamReader(is, ASCII_ENCODING);
 		BufferedReader reader = new BufferedReader(isReader);
 		StringBuilder sb = new StringBuilder();
 		String line = null;
 		while ((line = reader.readLine()) != null) {
 			sb.append(line).append('\n');
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * Read a text file as a String. Code from:
 	 * http://stackoverflow.com/questions/12910503/android-read-file-as-string
 	 * 
 	 * @param filePath
 	 * @return String contents of the file
 	 * @throws IOException 
 	 * @throws Exception
 	 */
 	public static String readStringFromFile(File file) throws IOException {
 		FileInputStream fin = new FileInputStream(file);
 		String ret = convertStreamToString(fin);
 		fin.close();        
 		return ret;
 	}
 
 	/**
 	 * Write a String to a text file. Will overwrite existing file contents.
 	 * Returns true if a new file was created, false if one was already present.
 	 * 
 	 * @param file
 	 * @param string
 	 * @throws IOException
 	 */
 	public static boolean writeStringToFile(File file, String string) 
 			throws IOException {
 		boolean newFile = file.createNewFile();
 		FileOutputStream fout = new FileOutputStream(file);
 		OutputStreamWriter out 
 			= new OutputStreamWriter(fout, ASCII_ENCODING);
 		try {
 			out.write(string);
 		} finally {
 			out.close();
 		}
 		return newFile;
 	}
 	
 	/**
 	 * Append a given parameter to a url string
 	 * 
 	 * @param paramName
 	 *            the name of the parameter appended to the url
 	 * @param paramVal
 	 *            the value of the parameter appended to the url
 	 * @param addAmpersand
 	 *            should be set to false if this is the last param that is to be
 	 *            appended
 	 * @return a new String with the parameter appended. Returns the empty
 	 *         String if either of the Strings passed in were null
 	 */
 	public static String appendParameter(String paramName, String paramVal) {
 		if (paramName == null || paramVal == null) {
 			return "";
 		}
 		return (paramName + "=" + paramVal + AMPERSAND);
 	}
 	
 	/** Ensure that a given object is not null.
 	 * 
 	 * @param o
 	 * @param objectName
 	 */
 	public static void ensureNotNull(Object o, String objectName) {
 		if (o == null) {
 			throw new IllegalStateException(objectName + " cannot be null!");
 		}
 	}
 }
