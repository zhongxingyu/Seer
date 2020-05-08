 package com.versionone.om.tests;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.Authenticator;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.PasswordAuthentication;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Assert;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import sun.net.www.protocol.http.AuthCacheImpl;
 import sun.net.www.protocol.http.AuthCacheValue;
 
 import com.versionone.apiclient.ConnectionException;
 import com.versionone.om.Effort;
 import com.versionone.om.Project;
 import com.versionone.om.Story;
 import com.versionone.om.Task;
 import com.versionone.om.V1Instance;
 
 
 
 public class AssetsStateTester extends BaseSDKTester {
 	private final static int port = 8080;
 	private V1Instance proxyInstance;
 
 	@Test
 	public void effortAssetStateTest() {
 		TestServer testServer = new TestServer(port, null, 100);
 		runServer(testServer);
 		String storyName = "story name 13";
 		String taskName = "task name 123";
 		Double effortTime = 11D;
 		Project rootProject = getProxyInstance().get().projectByID(SCOPE_ZERO);
 		Story story = rootProject.createStory(storyName);
 		Task task = story.createTask(taskName);
 		Effort effort = task.createEffort(effortTime);
 
 		resetInstance();
 
 		effort = getProxyInstance().get().effortByID(effort.getID());
 
 		resetInstance();
 
 		effort = getProxyInstance().get().effortByID(effort.getID());
 		testServer.stopServer();
 
 		List<String> urls = testServer.getUrls();
 		Assert.assertFalse(urls.contains("meta.v1/Actual/AssetState"));
 
 
 	}
 
     protected V1Instance getProxyInstance() {
         if (proxyInstance == null) {
        	proxyInstance = new V1Instance("http://localhost:" + port + "/V1JavaSDKTests/", "1", "1");
         	proxyInstance.validate();
         }
         return proxyInstance;
     }
 
 
 	private void runServer(TestServer server) {
 		new Thread(server).start();
 
 		// waiting till server is started
 		int count = 0;
 		while (server.isNotRun && count < 50) {
 			try {
 				Thread.sleep(200);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			count++;
 		}
 		// JIC
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private class TestServer extends Thread {
 		public volatile boolean isNotRun = true;
 		final String HTTP = "HTTP/1.0 ";
 		private final List<String> cookies = new LinkedList<String>();
 		private final int port;
 		private ServerSocket serverSocket;
 		private final int requestNumbers;
 		private static final String UTF8 = "UTF-8";
 		private final List<String> urls = new ArrayList<String>();
 
 		public TestServer(int port, String urlPart, int requestNumbers) {
 			if (cookies != null) {
 				this.cookies.addAll(cookies);
 			}
 			this.port = port;
 			this.requestNumbers = requestNumbers;
 		}
 
 
 		public void run() {
 			serverSocket = null;
 			Socket clientSocket = null;
 			PrintWriter out = null;
 			BufferedReader in = null;
 			try {
 				serverSocket = new ServerSocket(port);
 			} catch (IOException e) {
 				System.out.println("Could not listen on port: " + port + ". "
 						+ e.getMessage());
 				System.exit(-1);
 			}
 
 			isNotRun = false;
 			try {
 				for (int i = 0; i < requestNumbers; i++) {
 					clientSocket = null;
 					out = null;
 					in = null;
 
 					clientSocket = serverSocket.accept();
 					out = new PrintWriter(clientSocket.getOutputStream(), true);
 
 					in = new BufferedReader(new InputStreamReader(clientSocket
 							.getInputStream()));
 					HttpURLConnection connection = makeRequestToRealV1(in);
 
 					String responce;
 					responce = createResponce(connection);
 					out.write(responce);
 					out.flush();
 					out.close();
 				}
 			} catch (Exception ex) {
 				System.out.println(ex.getMessage());
 			} finally {
 				if (out != null) {
 					out.close();
 				}
 				try {
 					if (in != null) {
 						in.close();
 					}
 					if (clientSocket != null) {
 						clientSocket.close();
 					}
 				} catch (Exception ex) {
 				}
 
 				stopServer();
 			}
 		}
 
 		private String createResponce(HttpURLConnection connection) throws IOException {
 			BufferedReader in= null;
 			String result = "";
 			try {
 				in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
 			} catch (UnsupportedEncodingException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				in = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
 			}
 			String header = connection.getHeaderField( 0 );
 			result += header + "\n";
 			int i = 1;
 			while ( ( header = connection.getHeaderField(i) ) != null ) {
 				String key = connection.getHeaderFieldKey(i);
 				result += key + ": " + header + "\n";
 				i++;
 			}
 			result += "\n";
 			int numByte =  Integer.valueOf(connection.getHeaderField("Content-Length"));
             for (int k = 0; k < numByte; k++) {
             	char data = (char)in.read();
             	result += data;
             }
 			return result;
 		}
 
 
 		private HttpURLConnection makeRequestToRealV1(BufferedReader in)
 				throws ConnectionException {
 			//get URL and request method
 			V1RequestInfo urlAndType = getHeaderFirstLineData(in);
 			String path = getApplicationPath() + urlAndType.url;
 			HttpURLConnection request;
 			AuthCacheValue.setAuthCache(new AuthCacheImpl());
 			Authenticator.setDefault(new Credentials(getUsername(), getPassword()));
 			Map<String, String> customHttpHeaders = null;
 			try {
 				customHttpHeaders = getHeaderParams(in);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			try {
 				URL url = new URL(path);
 				request = (HttpURLConnection) url.openConnection();
 				request.setDoOutput(true);
 				addHeaders(request, customHttpHeaders);
 				if (urlAndType.method.equals("POST")) {
 					addPostData(in, request, customHttpHeaders);
 				}
 
 			} catch (MalformedURLException e) {
 				throw new ConnectionException("Invalid URL", e);
 			} catch (IOException e) {
 				throw new ConnectionException("Error Opening Connection", e);
 			}
 			return request;
 		}
 
 
 		private void addPostData(BufferedReader in, HttpURLConnection request,
 				Map<String, String> customHttpHeaders)
 				throws UnsupportedEncodingException, IOException {
 			OutputStreamWriter stream = new OutputStreamWriter(request.getOutputStream(), UTF8);
 			int numByte = Integer.parseInt(customHttpHeaders.get("Content-Length"));
 			String test = "";
 			for (int i = 0; i < numByte; i++) {
 				char data = (char)in.read();
 			    stream.write(data);
 			    test += data;
 			}
 			stream.flush();
 		}
 
 		private void addHeaders(HttpURLConnection request, Map<String, String> customHttpHeaders) {
 			if (customHttpHeaders == null) {
 				return;
 			}
 
 			for (String key : customHttpHeaders.keySet()) {
 				request.setRequestProperty(key, customHttpHeaders.get(key));
 			}
 		}
 
 		private Map<String, String> getHeaderParams(BufferedReader in)
 				throws IOException {
 			Map<String, String> headers = new HashMap<String, String>();
 			String inputLine;
 			while ((inputLine = in.readLine()) != null) {
 				if (inputLine.equals("")) {
 					break;
 				} else if (!inputLine.contains(": ")) {
 					continue;
 				}
 				String[] data = inputLine.split(": ");
 				if (data.length > 1) {
 					String value = data[1].replace("localhost:" + port, getHostName());
 					headers.put(data[0], value);
 				}
 			}
 			return headers;
 		}
 
 		private String getHostName() {
 			String domen  = getApplicationPath();
 			URL url = null;
 			try {
 				url = new URL(domen);
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return url != null ? url.getHost() : "";
 		}
 
 
 		private V1RequestInfo getHeaderFirstLineData(BufferedReader in) {
 			String url = "";
 			try {
 				url = in.readLine();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			String[] tmp = url.split(" ");
 			url = tmp[1].substring(1, tmp[1].length());
 			urls.add(url);
 
 			return new V1RequestInfo(url, tmp[0]);
 		}
 
 		public void stopServer() {
 			try {
 				if (serverSocket != null) {
 					serverSocket.close();
 				}
 			} catch (Exception ex) {
 			}
 		}
 
 		public List<String> getUrls() {
 			return urls;
 		}
 
 		private class V1RequestInfo {
 			public String url;
 			public String method;
 			public V1RequestInfo(String url, String method) {
 				this.url = url;
 				this.method = method;
 			}
 		}
 	}
 
 	private class Credentials extends Authenticator {
 
 		PasswordAuthentication _value;
 
 		@Override
 		protected PasswordAuthentication getPasswordAuthentication() {
 			return _value;
 		}
 
 		Credentials(String userName, String password) {
 			if (null == password) {
 				_value = new PasswordAuthentication(userName, null);
 			} else {
 				_value = new PasswordAuthentication(userName, password
 						.toCharArray());
 			}
 		}
 
 		@Override
 		public String toString() {
 			return _value.getUserName() + ":"
 					+ String.valueOf(_value.getPassword());
 		}
 	}
 
 }
