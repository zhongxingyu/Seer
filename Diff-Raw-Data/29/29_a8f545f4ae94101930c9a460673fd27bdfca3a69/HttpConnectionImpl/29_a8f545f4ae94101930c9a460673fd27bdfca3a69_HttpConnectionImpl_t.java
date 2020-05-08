 package alpv.mwp.crawler;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 
 public class HttpConnectionImpl implements HttpConnection {
 
 	private final int _responseCode;
 	private final ArrayList<String> _headerKeys;
 	private final ArrayList<String> _headerValues;
 	private final InputStream _content;
 
 	public HttpConnectionImpl(HttpURL httpURL) throws UnknownHostException,
 			IOException {
 		Socket socket = new Socket(httpURL.getHost(), httpURL.getPort());
 
 //		System.out.println("Connect to: " + httpURL.getHost() + ":"
 //				+ httpURL.getPort());
 		
		// Receive response
		InputStream inputStream = socket.getInputStream();
		InputStreamReader reader = new InputStreamReader(
						inputStream);
 
 		// Send request
 		String protocol = "HTTP/1.1";
 		String request = "GET " + httpURL.getPath() + " " + protocol + "\r\n"
 				+ "Host: " + httpURL.getHost() + "\r\n" + "\r\n";
 
 		OutputStream outputStream = socket.getOutputStream();
 		outputStream.write(request.getBytes());
 		socket.shutdownOutput();
 		
 		// Parse response
 		// Parse status code
		 String line = readLine(reader);
 //		System.out.println(line);
 		if (line != null) {
 			String[] parts = line.split(" ");
 			if (line.length() >= 2) {
 				_responseCode = Integer.parseInt(parts[1]);
 			} else {
 				throw new IOException("Invalid response");
 			}
 		} else {
 			throw new IOException("Invalid response");
 		}
 
 		// Parse header
 		boolean finished = false;
 		boolean dontParse = false;
 		_headerKeys = new ArrayList<String>();
 		_headerValues = new ArrayList<String>();
 		while (!finished && (line = readLine(reader)) != null) {
 			if (line.isEmpty()) {
 				finished = true;
 			} else {
 				String[] parts = line.split(": ", 2);
 				if (parts.length == 2) {
 					if(parts[0].equals("Content-Type") && !parts[1].startsWith("text/html")) {
 						dontParse = true;
 					}
 					_headerKeys.add(parts[0]);
 					_headerValues.add(parts[1]);
 				} else {
 					throw new IOException("Invalid response");
 				}
 			}
 		}
 		
 		if (dontParse) {
			socket.close();
			_content = null;
		}
		else {
			_content = inputStream;
 		}
 	}
 	
 	/**
 	 * Read a line by reading single chars
 	 * (Don't buffer the content)
 	 */
 	private String readLine(InputStreamReader reader) throws IOException {
 		char[] c = new char[1];
 		String line = "";
 		while(reader.read(c) == 1 && c[0] != '\n') {
 			if(c[0] != '\r') {
 				line += c[0];
 			}
 		}
 		return line;
 	}
 
 	@Override
 	public int getResponseCode() {
 		return _responseCode;
 	}
 
 	@Override
 	public String getHeaderFieldKey(int field) {
 		return _headerKeys.get(field);
 	}
 
 	@Override
 	public String getHeaderField(int field) {
 		return _headerValues.get(field);
 	}
 
 	@Override
 	public InputStream getContent() throws IOException {
 		return _content;
 	}
 
 }
