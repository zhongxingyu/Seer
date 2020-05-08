 package radio;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.TimeZone;
 
 import radio.server.RadioServer;
 
 /**
  * Utilities for the radio server & client
  * 
  * @author Adam Campbell
  */
 public class RadioUtil {
 	/**
 	 * The server's response when the user login is successful
 	 */
 	public static final String VALID_USER_MESSAGE = "GOOD";
 
 	/**
 	 * The server's response when the user login is not successful
 	 */
 	public static final String INVALID_USER_MESSAGE = "BAD";
 
 	/**
 	 * The size of the buffer to be used for reading/writing
 	 */
 	public static final int BUFFER_SIZE = 1024;
 
 	/**
 	 * Running this class lists all of the communiation ports available
 	 */
 	public static void main(String[] args){
 		CommunicationPortUtil.listPorts();
 	}
 
 	/**
 	 * Flush the input stream by reading until it has nothing left to give
 	 * 
 	 * @param in
 	 *            The input stream to be flushed
 	 * @throws IOException
 	 *             If something goes wrong
 	 */
 	public static void flush(InputStream in) throws IOException{
 		while(in.read() > -1)
 			;
 	}
 
 	/**
 	 * Clear the buffer by filling it with 0s
 	 * 
 	 * @param buf
 	 *            The buffer to be cleared
 	 */
 	public static void clear(byte[] buf){
 		for(int i = 0; i < buf.length; i++){
 			buf[i] = 0;
 		}
 	}
 
 	/**
 	 * Add the bytes contained in <i>toAdd</i> to <i>base</i>.
 	 * 
 	 * @param base
 	 *            The array list to which the bytes will be appended
 	 * @param toAdd
 	 *            The buffer containing the bytes to add
 	 */
 	public static void addBytes(ArrayList<Byte> base, byte[] toAdd){
 		for(int i = 0; i < toAdd.length; i++){
 			base.add(toAdd[i]);
 		}
 	}
 
 	/**
 	 * Get the bytes contained in an array list as an array of byte primitives
 	 * 
 	 * @param src
 	 *            The array list containing the bytes
 	 * @return An array of byte primitives containing the values, in order,
 	 *         present in <i>src</i>
 	 */
 	public static byte[] getBytes(ArrayList<Byte> src){
 		byte[] bytes = new byte[src.size()];
 		int i = 0;
 		for(Byte b : src){
 			bytes[i++] = b;
 		}
 
 		return bytes;
 	}
 
 	/**
 	 * Checks if the message is present in the array list of bytes
 	 * 
 	 * @param buffer
 	 *            The array list of bytes to check
 	 * @param msg
 	 *            The string to look for in the bytes
 	 * @return True, if the string is found in the bytes, false otherwise
 	 */
 	public static boolean contains(ArrayList<Byte> buffer, String msg){
 		String bufString = new String(getBytes(buffer));
 		return bufString.contains(msg);
 	}
 
 	/**
 	 * Trim the trailing 0s from an array of bytes
 	 * 
 	 * @param buf
 	 *            The byte array
 	 * @return A new array which is a copy of the original array, truncated such
 	 *         that the trailing 0s in the original array are removed.
 	 */
 	public static byte[] trimTrailing0s(byte[] buf){
 		int i;
 		for(i = 0; i < buf.length && buf[i] != 0; i++)
 			;
 		return Arrays.copyOf(buf, i);
 	}
 
 	/**
 	 * Get the directory where the jar file is stored on the computer
 	 * 
 	 * @return The jar file's directory on the computer
 	 */
 	public static String getJarDirectory(){
 		String path = RadioServer.class.getProtectionDomain().getCodeSource().getLocation().getPath();
 		try{
			return URLDecoder.decode(path, "UTF-8");
 		} catch(UnsupportedEncodingException e){
 			e.printStackTrace();
 			return "";
 		}
 	}
 
 	/**
 	 * Backup an array of byte data to a file. The data will be appended to the
 	 * log file, and if the file does not exist, it shall be created. The file
 	 * name will be <i>logBaseString/yyyy-MM-dd.txt</i>, where yyyy-MM-dd is the
 	 * date at which the file is created.
 	 * 
 	 * @param buffer
 	 *            The byte data to be appended to the file
 	 * @param logBaseString
 	 *            The name of the file base directory.
 	 */
 	public static void backupData(String data, String logBaseString){
 		// //Get a timestamp to attach to the message (in local time)
 		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
 		// String timeStamp = sdf.format(new Date());
 
 		// Get a timestamp from the log entry, so that it can be put in the
 		// appropriate log file
 		SimpleDateFormat logsdf = new SimpleDateFormat("yyyy-MM-dd");
 		logsdf.setTimeZone(TimeZone.getTimeZone("UTC"));
 		String logTimeStamp = logsdf.format(new Date());
 
 		// Get a text log and append the message to it
 		BufferedWriter out;
 		File logDir = new File(logBaseString);
 		if(!logDir.exists() || !logDir.isDirectory()){
 			logDir.mkdirs();
 		}
 		try{
 			out = new BufferedWriter(new FileWriter(new File(logBaseString + logTimeStamp + ".txt"), true));
 			if(data.length() > 0){
 				out.write(data + "\n");
 			}
 			out.close();
 		} catch(Exception e){
 			System.err.println("Error writing to log file!");
 			e.printStackTrace();
 		}
 	}
 }
