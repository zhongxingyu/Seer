 package VGL;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.CookieHandler;
 import java.net.CookieManager;
 import java.net.CookiePolicy;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.JOptionPane;
 
 import GeneticModels.GeneticModel;
 
 /*
  * utility class to handle communication with edX server
  */
 
 public class EdXServerUtils {
 
	public static EmailAndPassword saveToEdXServer(VGLII vglII, GeneticModel geneticModel, EmailAndPassword eMailAndPassword, String xmlString) {
 
 		CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));
 		String csrftoken = null;
 		URL url = null;
 		try {
 			url = new URL(geneticModel.getProblemTypeSpecification().getEdXServerStrings().edXCookieURL);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 
 		if (url != null) {
 			try {
 				// you need to contact once to get the header to get the csrftoken "cookie"
 				HttpURLConnection firstConnection = (HttpURLConnection)url.openConnection();
 				Map<String, List<String>> headerFields = firstConnection.getHeaderFields();
 				List<String> cookies = headerFields.get("Set-Cookie");
 				if (cookies != null) {
 					Iterator <String> sIt = cookies.iterator();
 					while (sIt.hasNext()) {
 						String s = sIt.next();
 						if (s.startsWith("csrftoken")) {
 							String part = s.split(";")[0];
 							csrftoken = part.split("=")[1];
 							//								System.out.println("first csrf token:" + csrftoken);
 						}
 					}
 				}
 				firstConnection.disconnect();
 
 				if (csrftoken == null) {
 					JOptionPane.showMessageDialog(vglII, "Could not access server");
					return eMailAndPassword;
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			// now login 
 			boolean loginSuccess = false;
 			boolean isRetry = false;
 			
 			while (!loginSuccess) {
 				if (csrftoken != null) {
 					try {
 						url = new URL(geneticModel.getProblemTypeSpecification().getEdXServerStrings().edXLoginURL);
 					} catch (MalformedURLException e) {
 						e.printStackTrace();
 					}
 					if (url != null) {
 						HttpURLConnection secondConnection;
 						try {
 							secondConnection = (HttpURLConnection)url.openConnection();
 							secondConnection.setDoInput(true);
 							secondConnection.setDoOutput(true); // make it a POST
 							secondConnection.setUseCaches(false);
 							secondConnection.setRequestProperty("X-CSRFToken", csrftoken);
 							secondConnection.setRequestProperty("Referer", 
 									geneticModel.getProblemTypeSpecification().getEdXServerStrings().edXCookieURL);
 
 							DataOutputStream output = new DataOutputStream(secondConnection.getOutputStream());
 
 							if (eMailAndPassword == null) {
 								eMailAndPassword = CustomDialogs.getEmailAndPassword(vglII,false);
								if (eMailAndPassword == null) return null;
 							} 
 
 							String content = 
 									"email=" + URLEncoder.encode(eMailAndPassword.eMail, "UTF-8") 
 									+ "&password=" + URLEncoder.encode(eMailAndPassword.password, "UTF-8")
 									+ "&remember=" + URLEncoder.encode("false", "UTF-8");
 
 							output.writeBytes(content);
 							output.flush();
 							output.close();
 
 							String response = null;
 							BufferedReader input = new BufferedReader(
 									new InputStreamReader(
 											new DataInputStream(secondConnection.getInputStream())));
 							while (null != ((response = input.readLine()))) {
 								if (response.contains("{\"success\": true}")) {
 									loginSuccess = true;
 									break;
 								}
 							}
 							input.close();
 						} catch (IOException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 				if (!loginSuccess) {
 					isRetry = true;
 					eMailAndPassword = CustomDialogs.getEmailAndPassword(vglII, isRetry);
					if (eMailAndPassword == null) return null;					
 				}
 			}
 
 			// now, submit it
 			try {
 				url = new URL(geneticModel.getProblemTypeSpecification().getEdXServerStrings().edXSubmissionURL);
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			}
 
 			StringBuffer b = new StringBuffer();
 			if (url != null) {
 				HttpURLConnection secondConnection;
 				try {
 					secondConnection = (HttpURLConnection)url.openConnection();
 					secondConnection.setDoInput(true);
 					secondConnection.setDoOutput(true); // make it a POST
 					secondConnection.setUseCaches(false);
 					secondConnection.setRequestProperty("X-CSRFToken", csrftoken);
 					secondConnection.setRequestProperty("Referer",  
 							geneticModel.getProblemTypeSpecification().getEdXServerStrings().edXCookieURL);
 
 					DataOutputStream output = new DataOutputStream(secondConnection.getOutputStream());
 
 					String content = clean(geneticModel.getProblemTypeSpecification().getEdXServerStrings().edXLocation) + "=" + xmlString;
 
 					output.writeBytes(content);
 					output.flush();
 					output.close();
 
 					String response = null;
 					BufferedReader input = new BufferedReader(
 							new InputStreamReader(
 									new DataInputStream(secondConnection.getInputStream())));
 					while (null != ((response = input.readLine()))) {
 						b.append(response + "\n");
 					}
 					input.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			String response = b.toString();
 
 			System.out.println("server response:\n" + response + "\n*******");
 
 			if (response.contains("progress_changed")) {
 				JOptionPane.showMessageDialog(vglII, "Submission Received by EdX Server\nPlease re-load the edX page to see your score on this part.");
 			} else {
 				JOptionPane.showMessageDialog(vglII, "Sorry, but there was an error in submission. \nPlease try again.\n");
 			}
 		}
		return eMailAndPassword;
 	}
 
 	private static String clean(String s) {
 		return s.replace("/","-").replace(":","").replace("--","-").replace(".", "_");
 	}
 
 
 }
