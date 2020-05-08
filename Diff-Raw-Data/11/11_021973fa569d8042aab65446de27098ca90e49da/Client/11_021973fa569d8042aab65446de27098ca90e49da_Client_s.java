 
 
 package edu.ucsb.cs56.projects.FTP_client;
 
 import java.util.*;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import org.apache.commons.net.ftp.FTP;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPReply;
 import org.apache.commons.net.ftp.FTPFile;
 import org.apache.commons.net.ftp.FTPConnectionClosedException;
 
 /**
  * An interface that different transfer protocols can implement
  * @version CS56, W14
  * @author David Coffill
  */
 
 
 public abstract class Client {
 
 	protected String hostname;
 	protected String username;
 	protected String password;
 	protected int port;
 
 	/**
 	*	Change directory
 	* 	@param target directory
 	*/
 	public abstract void ChangeDirectory(String dir);
 
 
 	/**
 	 * List all files in current directory
 	 * @return String array of file names
 	 */
 	public abstract String[] listFile();
 
 
 	/**
 	 * Determine if the file is a regular file
 	 * @param filename file's name
 	 * @return true if filename is a file, false if it's a directory
 	 */
 	public abstract boolean isFile(String filename);
 
 
 	/** 
 	*	Download file user input on current directory.
 	* 	@param file name to download
 	*/
 	public abstract void download(String input);
 
 	/**
 	 * Establish connection to remote host
 	 * @param url url to parse into username, host, and port if applicable
 	 * @param password password to authenticate with
 	 * @return true if the connection was successful, false if unsuccessful
 	 */
 
 	public abstract boolean connect(String url, String password);
 
 	/**
 	 * Establish connection to remote host
 	 * @param host hostname of remote host (such as csil.cs.ucsb.edu)
 	 * @param username username to authenticate with
 	 * @param password password to authenticate with
 	 * @return true if the connection was successful, false if unsuccessful
 	 */
 
 	public abstract boolean connect(String host, String username, String password);
 
 
 
 	/**
 	 * Explicitly close the connection the remote host
 	 */
 	public abstract void logout();
 
 	/**
 	 * Takes a valid RFC 1738 or RFC 3986 URL/URI in the form of
	 * user:password@host:port/path and parses it for connection information
	 * @param url Any valid RFC 1738/3986 URL/URI
 	 */
 	public void parseURL(String url) {
 		if (url.contains("@")) {
 			String[] parseString = url.split("[@]+");
 			username = parseString[0];
 
 			if (parseString[1].contains(":")) {
 				parseString = parseString[1].split("[:]+");
 				hostname = parseString[0];
 				port = Integer.parseInt(parseString[1]);
 			}
 			else {
 				hostname = parseString[1];
 			}
 		}
 		else if (url.contains(":")) {
 			String[] parseString = url.split("[:]+");
 			hostname = parseString[0];
 			port = Integer.parseInt(parseString[1]);
 		}
 		else hostname = url;
 	}
 
 
 }
