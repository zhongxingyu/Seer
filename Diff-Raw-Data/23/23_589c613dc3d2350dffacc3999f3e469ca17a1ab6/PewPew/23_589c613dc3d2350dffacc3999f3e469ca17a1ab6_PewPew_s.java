 /*
  * Custom system to hit my EC2 instances with a large volume of randomly generated crashes.
  *
  * Hack not really worth signing yet.
  */
 
 package com.smartass.pewpew;
 
 import com.smartass.pewpew.generators.*;
 
 import java.io.*;
 import java.nio.*;
 import java.net.*;
 import javax.net.ssl.*;
 
 public class PewPew {
 
 	private static final String USER_AGENT = "mobileDevice";
 
     // Actual tests
 
     public static void main(String[] args) {
 
         // Little welcome message
 
         System.out.println("\nWarming up the PewPew cannon...");
 
         // Start our server socket
 
         ServerSocket serverSocket = null;
         try {
             serverSocket = new ServerSocket(2109);
         }
         catch (IOException e) {
             System.err.println("Failed to listen on port 2109. Bailing");
             System.exit(1);
         }
 
         // Start up connection to the server
 
         try {
 
             Socket clientSocket = serverSocket.accept();
 
             while(true) {
 
                 System.out.println("Awaiting firing orders.");
                 
                 ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
                 TestConfig config = (TestConfig)ois.readObject();
 
                 System.out.println("Received firing orders. Loading shells.");
 
                 TestResult result = runPostTest(config);
                 result.print();
 
                 System.out.println("Reporting results to commander.");
 
                 ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                 oos.writeObject(result);
 
             }
 
         }
         catch (ClassNotFoundException e) {
             System.err.println("Transmission came through, but didn't follow the right format.");
         }
         catch (IOException e) {
             System.err.println("Transmission garbled. Standing down until new orders.");
             e.printStackTrace();
         }
     }
 
     // Run a post test
 
     private static TestResult runPostTest(TestConfig config) {
 
         System.out.println("Firing at "+config.url+" with "+config.numHits+" hits at a "+config.msDelay+" ms delay.\n");
 
         TestResult results = new TestResult();
 
         for (int i = 0; i < config.numHits; i++) {
             System.out.print("pew!");
             AssertResult ar = assertPost(config.url, config.urlParamGenerator.generateParameters(), config.expectedResponse, config.msTimeout);
             if (ar.success) {
                 results.passedTests++;
                 results.msAverageResponse += ar.msTime;
             }
             else {
                 results.failedTests++;
             }
 
             // delay between tests
 
             long currentTimeMS = System.currentTimeMillis();
             while (System.currentTimeMillis() - currentTimeMS < config.msDelay) {
                 // spin
             }
         }
        results.msAverageResponse /= results.passedTests;
 
         System.out.println("\n");
 
         return results;
 
     }
 
     // Random crash report
 
     private static String generateRandomCrashReport() {
         return "";
     }
 
     // AssertResult is used to pass back results from network asserts
     
     private static class AssertResult {
         boolean success;
         long msTime;
     }
 
     // Assert Get
 
     private static AssertResult assertGet(String url, String expectedResponse, int msTimeout) {
         AssertResult assertResult = new AssertResult();
         long startTimeMS = System.currentTimeMillis();
         try {
             NetworkResult res = sendGet(url,msTimeout);
             assertResult.success = (res.responseCode == 200) && (res.responseText.equals(expectedResponse));
             assertResult.msTime = System.currentTimeMillis() - startTimeMS;
             return assertResult;
         }
         catch (Exception e) {
             assertResult.success = false;
             assertResult.msTime = System.currentTimeMillis() - startTimeMS;
             return assertResult;
         }
     }
 
     // Assert Post
 
     private static AssertResult assertPost(String url, String urlParameters, String expectedResponse, int msTimeout) {
         AssertResult assertResult = new AssertResult();
         long startTimeMS = System.currentTimeMillis();
         try {
             NetworkResult res = sendPost("http://"+url,urlParameters,msTimeout);
             assertResult.success = (res.responseCode == 200) && (res.responseText.equals(expectedResponse));
             assertResult.msTime = System.currentTimeMillis() - startTimeMS;
             return assertResult;
         }
         catch (Exception e) {
             e.printStackTrace();
             assertResult.success = false;
             assertResult.msTime = System.currentTimeMillis() - startTimeMS;
             return assertResult;
         }
     }
 
     // NetworkResult is used to pass back results from POST and GET
 
     private static class NetworkResult {
         int responseCode;
         String responseText;
     }
 
 	// HTTP GET request
 
 	private static NetworkResult sendGet(String url, int msTimeout) throws Exception {
  
 		URL obj = new URL(url);
 		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
  
 		// optional default is GET
 
 		con.setRequestMethod("GET");
  
 		// add request header
 
 		con.setRequestProperty("User-Agent", USER_AGENT);
  
 		int responseCode = con.getResponseCode();
 		System.out.println("\nSending 'GET' request to URL : " + url);
 		System.out.println("Response Code : " + responseCode);
  
 		BufferedReader in = new BufferedReader(
 		        new InputStreamReader(con.getInputStream()));
 		String inputLine;
 		StringBuffer response = new StringBuffer();
  
 		while ((inputLine = in.readLine()) != null) {
 			response.append(inputLine);
 		}
 		in.close();
  
 		// format and return result
 
         NetworkResult res = new NetworkResult();
         res.responseCode = responseCode;
         res.responseText = response.toString();
 		return res;
  
 	}
  
 	// HTTP POST request
     
 	private static NetworkResult sendPost(String url, String urlParameters, int msTimeout) throws Exception {
 
 		URL obj = new URL(url);
 		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 
         con.setConnectTimeout(msTimeout);
         con.setReadTimeout(msTimeout);
  
 		// add reuqest header
 
 		con.setRequestMethod("POST");
 		con.setRequestProperty("User-Agent", USER_AGENT);
 		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
  
 		// Send post request
 
 		con.setDoOutput(true);
 		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
 		wr.writeBytes(urlParameters);
 		wr.flush();
 		wr.close();
  
 		int responseCode = con.getResponseCode();
 		System.out.println("\nSending 'POST' request to URL : " + url);
  
 		BufferedReader in = new BufferedReader(
 		        new InputStreamReader(con.getInputStream()));
 		String inputLine;
 		StringBuffer response = new StringBuffer();
  
 		while ((inputLine = in.readLine()) != null) {
 			response.append(inputLine);
 		}
 		in.close();
  
 		// format and return result
 
         NetworkResult res = new NetworkResult();
         res.responseCode = responseCode;
         res.responseText = response.toString();
 		return res;
  
 	}
 
 }
