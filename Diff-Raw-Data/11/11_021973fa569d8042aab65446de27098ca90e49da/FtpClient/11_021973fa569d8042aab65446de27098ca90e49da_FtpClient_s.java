 
 
 package edu.ucsb.cs56.projects.FTP_client;
 
 import java.io.*;
 import java.util.*;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import org.apache.commons.net.ftp.FTP;
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.commons.net.ftp.FTPReply;
 import org.apache.commons.net.ftp.FTPFile;
 import org.apache.commons.net.ftp.FTPConnectionClosedException;
 
 /**
  * An FTP client support anonymous login and download files on current directory.
  * @version CS56, W14
  * @author Wenjie Huang
  * @author David Coffill
  */
 
 public class FtpClient extends Client {
 	private FTPClient client;
 	private FTPFile[] fileList;
 	private String[] stringFileList;
 	/** Constructor
 	 */
 	public FtpClient()	{
 		client = new FTPClient();
 		fileList = null;
 		stringFileList = null;
 		username = "anonymous";
 		password = "anonymous";
		port = 23; // Default FTP port
 	}
 
 	/**
 	 *	Change directory
 	 * 	@param target directory
 	 */
 
 	public void ChangeDirectory(String dir)	{
 		try {
 			client.changeWorkingDirectory(dir);
 		}
 		catch (IOException e){ }
 	}
 
 	/**
 	 *	Show file list on current directory
 	 */
 
 	public String[] listFile()	{
 		System.out.println("*************File List************");
 		fileList=null;
 		stringFileList=null;
 		try {
 			FTPFile[] fileList = client.listFiles();
 			stringFileList = new String[fileList.length];
 			//fileList = client.listFiles();
 			for (int i = 0; i < fileList.length; ++i) {
 				stringFileList[i] = fileList[i].toString();
 				System.out.println(stringFileList[i]);
 
 			}
 		}
 		catch (IOException e)	{}
 		return stringFileList;
 	}
 
 	/**
 	 *	Determine if the file is a regular file
 	 */
 
 	public boolean isFile(String filename)	{
 		for(FTPFile f : fileList)
 			if(filename.equals(f.getName())&&f.isFile())
 				return true;
 		return false;
 
 	}
 
 	/**
 	 *	Download file user input on current directory.
 	 * 	@param file name to download
 	 */
 	public void download(String input)	{
 
 		String[] filenames = input.split("/");
 		String filename = filenames[filenames.length-1];
 		if(isFile(filename))	{
 			try	{
 				File file = new File(filename);
 				OutputStream outputstream = new FileOutputStream(file);
 				if (!file.exists()) {
 					file.createNewFile();
 				}
 				client.retrieveFile(input, outputstream);
 			}
 			catch (FTPConnectionClosedException e){System.out.println("Connection closed"); }
 			catch (IOException e){	}
 
 			System.out.println("Download "+filename);
 		}
 		else System.out.println(filename+" is not a file.");
 
 	}
 
 	 /**
 	 * Establish connection to remote host
 	 * @param hostname hostname of remote host (such as csil.cs.ucsb.edu)
 	 * @param username username to authenticate with
 	 * @param password password to authenticate with
 	 * @return true if the connection was successful, false if unsuccessful
 	 */
 
 	public boolean connect (String hostname, String username, String password)	{
 
 		try {
 			client.connect(hostname, port);
 			System.out.println("Connected to " + hostname + ".");
 			System.out.println(client.getReplyString());
 			client.login("anonymous", "anonymous");
 
 			int reply = client.getReplyCode();
 			if(!FTPReply.isPositiveCompletion(reply))	{
 				client.disconnect();
 				System.err.println("FTP server refused connection.");
 				System.exit(1);
 			}
 
 		}
 		catch(IOException e) {
 			System.out.println("Connection fail.");
 			return false;
 		}
 		return true;
 	}
 
 	public boolean connect (String url, String password) {
 		parseURL(url);
 		return connect(hostname, username, password);
 	}
 
 	/**
 	 *
 	 *
 	 */
 
 	public void logout() {
 		try {
 			client.logout();
 		}
 		catch(IOException ioe) {	}
 	}
 
 	public static void main (String[] args)	{
 
 		System.out.println("Client start!");
 		Client newClient = new FtpClient();
 
 		System.out.println("Enter URL ( [user@]host[:port] ) :");
 		Scanner sc = new Scanner (System.in);
 		String url = sc.nextLine();
 		// Ask for password without echoing input to the console
 		Console c = System.console();
 		char[] password = c.readPassword("Input your password (press Enter for none): ");
 
 		newClient.connect(url, new String(password));
 		String[] f = newClient.listFile();
 		System.out.println("input file to download:");
 		sc = new Scanner (System.in);
 		String input = sc.nextLine();
 		newClient.download(input);
 	}
 }
