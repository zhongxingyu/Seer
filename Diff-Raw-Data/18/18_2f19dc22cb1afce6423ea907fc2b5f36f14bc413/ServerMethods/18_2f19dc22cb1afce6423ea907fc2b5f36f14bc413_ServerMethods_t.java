 package com.pacwar;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class ServerMethods {
 	
 	static URL url;
 
 	static {
 		try {
 			ServerMethods.url = new URL("http://myserver.herokuapp.com");
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	static String password = "androidelteety ";
 	private static byte[] sharedBuffer = new byte[1000];
 
 	static boolean register(String userName) throws Exception {
 		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
 		httpConn.setDoOutput(true);
 		httpConn.setRequestMethod("POST");
 		httpConn.connect();
 		OutputStream os = httpConn.getOutputStream();
 		os.write((password + "register " + userName).getBytes());
 		os.close();
 		InputStream in = httpConn.getInputStream();
 		int read;
 		String tempstr = "";
 		while ((read = in.read(sharedBuffer)) != -1)
 			tempstr += new String(sharedBuffer, 0, read);
 		in.close();
 		return tempstr.contains("succsessfully");
 	}
 
 	static boolean unregister(String userName) throws Exception {
 		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
 		httpConn.setDoOutput(true);
 		httpConn.setRequestMethod("POST");
 		httpConn.connect();
 		OutputStream os = httpConn.getOutputStream();
 		os.write((password + "unregister " + userName).getBytes());
 		os.close();
 		InputStream in = httpConn.getInputStream();
 		int read;
 		String tempstr = "";
 		while ((read = in.read(sharedBuffer)) != -1)
 			tempstr += new String(sharedBuffer, 0, read);
 		in.close();
 		return tempstr.contains("succsessfully");
 	}
 
 	// run in a different thread
 	/*
 	 * Thread t = new Thread(new Runnable() {
 	 * 
 	 * @Override public void run() { System.out.println("start"); try {
 	 * ServerMethods.host(username); } catch (Exception e) {
 	 * e.printStackTrace(); } } }); t.start();
 	 */
 	static void host(String userName) throws Exception {
 		connected = true;
 		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
 		httpConn.setDoOutput(true);
 		httpConn.setRequestMethod("POST");
 		httpConn.connect();
 		OutputStream os = httpConn.getOutputStream();
 		os.write((password + "host " + userName).getBytes());
 		os.close();
 
 		InputStream in = httpConn.getInputStream();
 		byte[] buffer = new byte[1000];
 		int read;
 		String tempstr = "";
 		while (connected) {
 			while ((read = in.read(buffer)) != -1) {
 				tempstr = new String(buffer, 0, read);
 				// TODO message received from remote
 				// TODO do something
 			}
 			in.close();
 			in = reconnect(userName);
 		}
 		in.close();
 	}
 
 	static boolean connected = false;
 
 	static boolean sendMessage(String from, String message) throws Exception {
 		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
 		httpConn.setDoOutput(true);
 		httpConn.setRequestMethod("POST");
 		httpConn.connect();
 		OutputStream os = httpConn.getOutputStream();
 		os.write((password + "message " + from + " " + message).getBytes());
 		os.close();
 		InputStream in = httpConn.getInputStream();
 		byte[] buffer = new byte[1000];
 		int read;
 		String tempstr = "";
 		while ((read = in.read(buffer)) != -1)
 			tempstr += new String(buffer, 0, read);
 		System.out.println(tempstr);
 		in.close();
 		return tempstr.contains("succsessfully");
 	}
 
 	// connect to a host
 	// run in different thread
 	/*
 	 * Thread t = new Thread(new Runnable() {
 	 * 
 	 * @Override public void run() { System.out.println("start"); try {
 	 * ServerMethods.host(username); } catch (Exception e) {
 	 * e.printStackTrace(); } } }); t.start();
 	 */
 	static boolean connect(String userName, String hostName) throws Exception {
 		connected = true;
 		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
 		httpConn.setDoOutput(true);
 		httpConn.setRequestMethod("POST");
 		httpConn.connect();
 		OutputStream os = httpConn.getOutputStream();
 		os.write((password + "connect " + userName + " " + hostName).getBytes());
 		os.close();
 		InputStream in = httpConn.getInputStream();
 		byte[] buffer = new byte[1000];
 		int read;
 		String tempstr = "";
 		while (connected) {
 			while ((read = in.read(buffer)) != -1) {
 				tempstr = new String(buffer, 0, read);
 				// TODO message received from remote
 				// TODO do something
 			}
 			in.close();
 			in = reconnect(userName);
 		}
 		in.close();
 		return tempstr.contains("succsessfully");
 	}
 
 	static InputStream reconnect(String userName) throws Exception {
 		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
 		httpConn.setDoOutput(true);
 		httpConn.setRequestMethod("POST");
 		httpConn.connect();
 		OutputStream os = httpConn.getOutputStream();
 		os.write((password + "reconnect " + userName).getBytes());
 		os.close();
 		System.out.println("jhabsndjasdd");
 		return httpConn.getInputStream();
 	}
 
 	static boolean diconnect(String userName) throws Exception {
 		connected = false;
 		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
 		httpConn.setDoOutput(true);
 		httpConn.setRequestMethod("POST");
 		httpConn.connect();
 		OutputStream os = httpConn.getOutputStream();
 		os.write((password + "disconnect " + userName).getBytes());
 		os.close();
 		InputStream in = httpConn.getInputStream();
 		int read;
 		String tempstr = "";
 		while ((read = in.read(sharedBuffer)) != -1)
 			tempstr += new String(sharedBuffer, 0, read);
 		in.close();
 		return tempstr.contains("succsessfully");
 	}
 
	public ArrayList<String> getHostsList() throws Exception {
 		URL url = new URL("http://myserver.herokuapp.com/?show=hosts");
 		HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
 		httpConn.setDoOutput(true);
 		httpConn.setRequestMethod("GET");
 		httpConn.connect();
 		InputStream in = httpConn.getInputStream();
 		int read;
 		String tempstr = "";
 		while ((read = in.read(sharedBuffer)) != -1)
 			tempstr += new String(sharedBuffer, 0, read);
 		in.close();
 		Scanner myScanner = new Scanner(tempstr);
 		myScanner.nextLine();
 		ArrayList<String> res = new ArrayList<String>();
 		while (myScanner.hasNext())
 			res.add(myScanner.next());
 		myScanner.close();
 		return res;
 	}
 
 }
