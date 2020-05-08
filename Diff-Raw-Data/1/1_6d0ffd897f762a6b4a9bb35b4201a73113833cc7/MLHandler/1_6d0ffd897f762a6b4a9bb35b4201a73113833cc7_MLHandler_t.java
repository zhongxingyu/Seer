 package org.uncertweb.matlab;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.net.SocketException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.uncertweb.matlab.json.IOExceptionDeserializer;
 import org.uncertweb.matlab.json.MLExceptionDeserializer;
 import org.uncertweb.matlab.json.MLExceptionSerializer;
 import org.uncertweb.matlab.json.MLRequestDeserializer;
 import org.uncertweb.matlab.json.MLResultDeserializer;
 import org.uncertweb.matlab.json.MLValueSerializer;
 import org.uncertweb.matlab.value.MLValue;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonParseException;
 
 /**
  * Handles MATLAB requests/responses. See {@link #sendRequest(String, int, MLRequest)} for how to send 
  * a request to a MATLAB server.
  * 
  * @author Richard Jones
  *
  */
 public class MLHandler {
 
 	private Gson gson;
 
 	/**
 	 * Creates a new <code>MLHandler</code> instance.
 	 * 
 	 */
 	public MLHandler() {
 		GsonBuilder builder = new GsonBuilder();
		builder.serializeSpecialFloatingPointValues();
 		builder.registerTypeAdapter(MLException.class, new MLExceptionDeserializer());
 		builder.registerTypeAdapter(MLException.class, new MLExceptionSerializer());
 		builder.registerTypeAdapter(IOException.class, new IOExceptionDeserializer());
 		builder.registerTypeAdapter(MLRequest.class, new MLRequestDeserializer());
 		builder.registerTypeAdapter(MLResult.class, new MLResultDeserializer());
 		builder.registerTypeAdapter(MLValue.class, new MLValueSerializer());
 		gson = builder.create();
 	}
 
 	/**
 	 * Parses a {@link MLRequest} from an {@link InputStream}.
 	 * 
 	 * @param is the <code>InputStream</code> to parse from
 	 * @return the parsed <code>MLRequest</code>
 	 */
 	public MLRequest parseRequest(InputStream is) {
 		return gson.fromJson(new InputStreamReader(is), MLRequest.class);
 	}
 
 	/**
 	 * Outputs a {@link MLRequest} to an {@link OutputStream}.
 	 * 
 	 * @param request the <code>MLRequest</code> to output
 	 * @param os the <code>OutputStream</code> to output to
 	 */
 	public void outputRequest(MLRequest request, OutputStream os){		
 		String json = gson.toJson(request);
 		PrintWriter pw = new PrintWriter(os, true);
 		pw.println(json);
 	}
 
 	/**
 	 * Outputs a {@link MLResult} to an {@link OutputStream}.
 	 * 
 	 * @param result the <code>MLResult</code> to output
 	 * @param os the <code>OutputStream</code> to output to
 	 */
 	public void outputResult(MLResult result, OutputStream os) {
 		String json = gson.toJson(result);
 		PrintWriter pw = new PrintWriter(os, true);
 		pw.println(json);
 	}
 
 	/**
 	 * Outputs a {@link MLException} to an {@link OutputStream}.
 	 * 
 	 * @param exception the <code>MLException</code> to output
 	 * @param os the <code>OutputStream</code> to output to
 	 */
 	public void outputException(MLException exception, OutputStream os) {
 		String json = gson.toJson(exception);
 		PrintWriter pw = new PrintWriter(os, true);
 		pw.println(json);
 	}
 
 	/**
 	 * Sends a request to a MATLAB server. The server must be using the supplied server.m and waiting for 
 	 * connections on the specified port.
 	 * 
 	 * @param host the address of the MATLAB server
 	 * @param port the port of the MATLAB server
 	 * @param request the <code>MLRequest</code> to send
 	 * @return the <code>MLResult</code> of the function
 	 * @throws MLException if MATLAB encountered an error during function execution
 	 * @throws IOException if the connection to the MATLAB server failed
 	 */
 	// TODO: sometimes a SocketException gets thrown (possible to do with lots of requests in a short amount of time), for now there's just three attempts if this happens
 	public MLResult sendRequest(String host, int port, MLRequest request) throws MLException, IOException {
 		int attempt = 0;
 		SocketException thrown = null;
 		while (attempt < 3) {
 			try {
 				// create socket
 				SocketAddress address = new InetSocketAddress(host, port);
 				Socket socket = new Socket();
 				socket.connect(address, 10 * 1000); // 10s to connect
 				
 				// send request
 				OutputStream out = socket.getOutputStream();
 				InputStream in = socket.getInputStream();
 				outputRequest(request, out);
 
 				// get response
 				char[] buffer = new char[1024];
 				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 				StringWriter writer = new StringWriter();
 				int c;
 				while ((c = reader.read(buffer)) != -1) {
 					writer.write(buffer, 0, c);
 				}
 				String response = writer.toString();
 
 				// parse
 				// try for exception
 				// speedup, not the best
 				if (response.startsWith("{\"exception\"")) {
 					MLException exception = gson.fromJson(response, MLException.class);
 
 					out.close();
 					socket.close();
 
 					throw exception;
 				}
 				else {
 					// not an error, must be result
 					MLResult result = gson.fromJson(response, MLResult.class);			
 
 					out.close();
 					socket.close();
 
 					return result;
 				}
 			}
 			catch (SocketException e) {
 				thrown = e;
 				attempt++;
 			}
 		}
 		
 		throw thrown;
 	}
 
 	public MLResult sendRequest(String mlProxyURL, MLRequest request) throws MLException, IOException {
 		// open connection to proxy
 		URLConnection connection = new URL(mlProxyURL).openConnection();
 
 		// do a http post request
 		connection.setDoOutput(true);
 		OutputStream out = connection.getOutputStream();
 		out.write("request=".getBytes());
 		outputRequest(request, out);
 		out.close();
 
 		// get response
 		InputStream in = connection.getInputStream();
 		char[] buffer = new char[1024];
 		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 		StringWriter writer = new StringWriter();
 		int c;
 		while ((c = reader.read(buffer)) != -1) {
 			writer.write(buffer, 0, c);
 		}
 		String response = writer.toString();
 
 		// parse
 		try {
 			// try for exceptions
 			try {
 				MLException exception = gson.fromJson(response, MLException.class);
 				throw exception;
 			}
 			catch (JsonParseException e) {
 				IOException exception = gson.fromJson(response, IOException.class);
 				throw exception;
 			}
 		}
 		catch (JsonParseException e) {
 			// not an error, must be result
 			MLResult result = gson.fromJson(response, MLResult.class);			
 			return result;
 		}
 		finally {
 			in.close();
 		}
 	}
 
 }
