 package de.fhb.maus.android.todolist.database;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.net.URLConnection;
 import de.fhb.maus.android.todolist.helpers.PATHs;
 import de.fhb.maus.android.todolist.helpers.URLs;
 
 public class Timestamps {
 
 	public static void exportTimestampToServer() {
 
 		HttpURLConnection httpUrlConnection = null;
 		OutputStream outputStream;
 		BufferedInputStream fileInputStream;
 		BufferedReader serverReader;
 		int totalBytes;
 		int bytesTrasferred;
 		String response = "";
 		String serverResponse = "";
 		String localFileName = PATHs.getInternalTimestampPath();
 
 		// Establish a connection
 		try {
 			httpUrlConnection = (HttpURLConnection) new URL(
 					URLs.getExternalUploadTimestampPHP()).openConnection();
 			httpUrlConnection.setDoOutput(true);
 			httpUrlConnection.setRequestMethod("POST");
 			outputStream = httpUrlConnection.getOutputStream();
 
 			// Buffered input stream
 			fileInputStream = new BufferedInputStream(new FileInputStream(
 					localFileName));
 
 			// Get the size of the image
 			totalBytes = fileInputStream.available();
 
 			// Loop through the files data
 			for (int i = 0; i < totalBytes; i++) {
 				// Write the data to the output stream
 				outputStream.write(fileInputStream.read());
 				bytesTrasferred = i + 1;
 			}
 
 			// Close the output stream
 			outputStream.close();
 
 			// New reader to get server response
 			serverReader = new BufferedReader(new InputStreamReader(
 					httpUrlConnection.getInputStream()));
 
 			// Read the servers response
 			serverResponse = "";
 			while ((response = serverReader.readLine()) != null) {
 				serverResponse = serverResponse + response;
 			}
 
 			// Close the buffered reader
 			serverReader.close();
 
 			// Close the file input stream
 			fileInputStream.close();
 			System.out.println("Timestamp successfully saved on HTTP Server");
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void importTimestampFromServer() {
 
 		String url_str = URLs.getExternalTimestampPath();
 		FileOutputStream os;
 		try {
 			os = new FileOutputStream(PATHs.getInternalTimestampPath());
 			URL url = new URL(url_str);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			conn.setRequestMethod("GET");
 			conn.connect();
 			int responseCode = conn.getResponseCode();
 			if (responseCode == HttpURLConnection.HTTP_OK) {
 				byte tmp_buffer[] = new byte[4096];
 				InputStream is = conn.getInputStream();
 				int n;
 				while ((n = is.read(tmp_buffer)) > 0) {
 					os.write(tmp_buffer, 0, n);
 					os.flush();
 				}
 			}
 			os.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (ProtocolException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void createTimestampOnDevice() {
 
 		try {
 			FileWriter out = new FileWriter(PATHs.getInternalTimestampPath());
 			out.write("" + System.currentTimeMillis());
 			out.flush();
 			out.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void deleteTimestampOnDevice() {
 
 		boolean success = (new File(PATHs.getInternalTimestampPath())).delete();
 		if (!success) {
 			System.out.println("Delete of timestamp failed");
 		}
 	}
 
 	public static String getTimestampFromDevice() {
 
 		String str = "";
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(
 					PATHs.getInternalTimestampPath()));
 			while ((str = in.readLine()) != null) {
 				 System.out.println(str);
 			}
 			in.close();
 		} catch (IOException e) {
 		}
 		return str;
 	}
 
 	public static String getTimestampFromServer() {
 
 		URLConnection conn = null;
 		DataInputStream data = null;
 		String line;
 		StringBuffer sb = new StringBuffer();
 		try {
 			conn = new URL(URLs.getExternalTimestampPath()).openConnection();
 			conn.connect();
 			data = new DataInputStream(new BufferedInputStream(
 					conn.getInputStream()));
 			while ((line = data.readLine()) != null) {
 				sb.append(line);
 			}
 			data.close();
 		} catch (IOException e) {
 			System.out.println("IO Error:" + e.getMessage());
 		}
 		return sb.toString();
 	}
 
 	public static boolean differentTimestamps(int device, int server) {
 		if (device != server)
 			return true;
 		else
 			return false;
 	}
 }
