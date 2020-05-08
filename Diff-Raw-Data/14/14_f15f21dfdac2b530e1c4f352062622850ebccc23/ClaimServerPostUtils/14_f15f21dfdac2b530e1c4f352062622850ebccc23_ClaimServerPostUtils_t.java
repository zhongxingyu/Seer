 package com.covermymeds.claimserverpost;
 
 import java.awt.Desktop;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.HttpResponseException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicNameValuePair;
 import com.beust.jcommander.JCommander;
 import com.beust.jcommander.ParameterException;
 
 /**
  * &copy 2012 CoverMyMeds <br/>
  * Utility class used to assist in the setting up, initiating, and processing of 
  * http requests.
  * 	@author Juan Roman
  */
 public class ClaimServerPostUtils {
 
 	/**
 	 * Map containing all basic http errors for a quick reference
 	 */
 	private static Map<Integer, String> errors = ClaimServerPostUtils.createErrors();
 
 	/**
 	 * STX character
 	 */
 	public static final char START_OF_CLAIM = '\2';
 	
 	/**
 	 * ETX character
 	 */
 	public static final char END_OF_CLAIM = '\3';
 
 
 	/**
 	 * Tests that the specified file is not null and exists.
 	 * @param claimFile the file to be tested
 	 * @return true if the file is not <code>null</code> and it exists
 	 */
 	public static boolean claimFileExists(File claimFile) {
 		return (claimFile != null && claimFile.exists());
 	}
 
 	/**
 	 * Returns a populate <code>JCommanderOptions</code> object or <code>null</code> 
 	 * if any parsing errors occur.
 	 * @param args the arguments to be parsed
 	 * @return a populated <code>JCommanderOptions</code> object or <code>null</code>
 	 * if parsing errors occur
 	 */
 	public static JCommanderOptions parseCommandLine(String[] args) {
 		JCommanderOptions parsedOptions = null;
 		try {
 			parsedOptions = new JCommanderOptions();
 			//Creating a new instance of JCommander initiates parsing
 			//and populates passed in object
 			new JCommander(parsedOptions, args);
 		} catch (ParameterException e) {
 			System.err.println("Error: " + e.getMessage());
 			parsedOptions = null;
 		}
 		return parsedOptions;
 	}
 
 	/**
 	 * Returns a URLEncodedFormEntity that holds the parsedOptions's 
 	 * 			parameters: username,password,claim, and api key.
 	 * @param parsedOptions the object holding the values to be encoded
 	 * @return a URLEncodedFormEntity that holds the parsedOptions's
 	 * 			parameters: username, password, claim, and api key
 	 * @throws IOException
 	 */
 	public static UrlEncodedFormEntity encodeParameters(
 			JCommanderOptions parsedOptions) throws IOException {
 		List<BasicNameValuePair> params = Arrays.asList(
 				new BasicNameValuePair("username", parsedOptions.getUsername()),
 				new BasicNameValuePair("password", parsedOptions.getPassword()),
 				new BasicNameValuePair("ncpdp_claim", ClaimServerPostUtils
 						.getClaim(parsedOptions.getClaimFile(),
 								parsedOptions.readFromStdin())),
 				new BasicNameValuePair("api_dkey", parsedOptions.getApiKey()));
 		//TODO change back to api_key
 		return new UrlEncodedFormEntity(params, "UTF-8");
 	}
 
 	/**
 	 * Sends a request to the serviceURL using the encodedParameters. Returns a 
 	 * list of the returned urls as strings or <code>null</code> if an error occurs.
 	 * @param serviceUrl the url to send the request to
 	 * @param encodedParameters the parameters being sent in the request
 	 * @param verbose specifies whether output should be verbose or not.
 	 * @return a list of the returned urls as strings or <code>null</code>
 	 * if an error occurs.
 	 * @throws ClientProtocolException
 	 * @throws IOException
 	 */
 	public static List<String> sendHttpRequest(String serviceUrl,
 			UrlEncodedFormEntity encodedParameters, boolean verbose)
 			throws ClientProtocolException, IOException {
 
 		HttpClient client = new DefaultHttpClient();
 		HttpPost httpPost = new HttpPost(serviceUrl);
 		httpPost.setEntity(encodedParameters);
 
 		List<String> addresses = null;
 		try {
 			String response = client.execute(httpPost,new BasicResponseHandler());
 			addresses = Arrays.asList(response.split("\n"));
 			if(verbose) {
 				for(String address : addresses) {
 					System.out.println("Request created at: " + address);
 				}
 			}
 		}
 		catch (HttpResponseException e) {
 			String errorMessage = String.valueOf(e.getStatusCode()) + " " + e.getMessage();
 			errorMessage = (verbose ? errorMessage + errors.get(e.getStatusCode()) : errorMessage);
 			System.err.println(errorMessage);
 		} finally {
 		     // When HttpClient instance is no longer needed,
 		     // shut down the connection manager to ensure
 		     // immediate deallocation of all system resources
 			client.getConnectionManager().shutdown();
 		}
 		return addresses;
 	}
 
 	/**
 	 * Opens the the user's default browser to each address in the list
 	 * of addresses.
 	 * @param addresses the string urls to open the default browser to.
 	 * @param verbose specifies whether output should be verbose or not.
 	 * @throws IOException
 	 * @throws URISyntaxException
 	 */
 	public static void openBrowser(List<String> addresses, boolean verbose)
 			throws URISyntaxException, IOException {
 		if (Desktop.isDesktopSupported()) {
 			Desktop desktop = Desktop.getDesktop();
 			if (desktop.isSupported(Desktop.Action.BROWSE)) {
 				for (String address : addresses) {
 					if (verbose) {
 						System.out.println("Opening browser to: " + address);
 					}
 					desktop.browse(new URI(address));
 				}
 			} else {
 				System.err.println("Browse action is not supported.");
 			}
 		} else {
 			System.err.println("Desktop is not supported.");
 		}
 	}
 
 	/**
 	 * Returns a claim either from a file or from standard input.
 	 * 
 	 * @param claimFile the file containing the claim.
 	 * @param readFromStdin flag used to specify that a claim will be
 	 * 						entered using standard input.
 	 * @return a string representation of either the specified file or
 	 * 			the claim that was entered from standard input.
 	 * @throws IOException
 	 */
 	private static String getClaim(File claimFile, boolean readFromStdin)
 			throws IOException {
 		/* 
 		 * Reading from stdin take precedence over the file so check
 		 * it first
 		 */
 		if (readFromStdin) {
 			System.out.println("Enter claim:");
 			Scanner stdin = new Scanner(System.in);
 			String input = stdin.nextLine();
 			
 			/*
 			 * Check that the STX and ETX characters surround the claim and
 			 * if not wrap them around the claim
 			 */
 			if (input.length() >= 2 && input.charAt(0) != START_OF_CLAIM) {
 				input = START_OF_CLAIM + input;
 			}
 			if (input.length() >= 2 && input.charAt(input.length() - 1) != END_OF_CLAIM) {
 				input += END_OF_CLAIM;
 			}
 			return input;
 		} else {
 			//Read claimFile into a string
 			BufferedReader reader = null;
 			try {
 				reader = new BufferedReader(new FileReader(claimFile));
 				char[] buf = new char[1024];
 				int numRead = 0;
 				StringBuffer fileData = new StringBuffer();
 				while ((numRead = reader.read(buf)) != -1) {
 					String readData = String.valueOf(buf, 0, numRead);
 					fileData.append(readData);
 				}
 				return fileData.toString();
 			} finally {
 				if (reader != null) {
 					reader.close();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Returns a map containing http error messages, associated with their
 	 * error code.
 	 * @return a map containting basic http errors referenced by their error 
 	 * codes.
 	 */
 	private static Map<Integer, String> createErrors() {
 		Map<Integer, String> result = new HashMap<Integer, String>();
		result.put(400, ". Oops, there was a connection problem. Please"
 				+ " try one more time, then contact CoverMyMeds at 1-866-452-"
 				+ "5017/help@covermymeds.com and they will help you diagnose"
 				+ " this issue.");
		result.put(403, ". Oops, login failed for the username or password that"
 				+ " was submitted. Please check the username and password in your"
 				+ " account settings in your Pharmacy System and at the CMM website to"
 				+ " make sure they match. If you still have trouble, please contact"
 				+ " CoverMyMeds at 1-866-452-5017/help@covermymeds.com and they will"
 				+ " help you fix this issue.");
		result.put(404, ". Oops, there was a problem. Please check the username and"
 				+ " password in your account settings in your Pharmacy System and at the"
 				+ " CMM website to make sure they match. If you still have trouble, please"
 				+ " contact CoverMyMeds at 1-866-452-5017/help@covermymeds.com and they will"
 				+ " help you fix this issue.");
		result.put(408, ". Oops, there was a timeout. Please try the request again in one"
 				+ " minute. If you still have trouble, please contact CoverMyMeds at 1-866-452"
 				+ "-5017/help@covermymeds.com and they will help you fix this issue.");
		result.put(500, ". Oops, there was a problem. Please try the request again in one minute."
 				+ " If you still have trouble, please contact CoverMyMeds at 1-866-452-5017"
 				+ "/help@covermymeds.com and they will help you diagnose this issue.");
 		return Collections.unmodifiableMap(result);
 	}
 	
 	private ClaimServerPostUtils() {
 		//disable external instantiation
 	}
 }
