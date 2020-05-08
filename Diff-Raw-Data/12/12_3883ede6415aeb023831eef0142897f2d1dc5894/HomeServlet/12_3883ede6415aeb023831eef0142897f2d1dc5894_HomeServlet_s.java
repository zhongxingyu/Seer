 package com.force.demo.servlet;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.codec.binary.Base64;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.JsonParseException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 
 public class HomeServlet extends HttpServlet {
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		doPost(req, resp);
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String signed_request = req.getParameter("signed_request");
 		
 		if(signed_request != null) {
 			System.out.println("signed request: " + signed_request);
 			String[] elements = signed_request.split("\\.");
 			System.out.println("elements " + elements);
 			
 			if(elements.length > 1) {			
 				String payload = elements[1];
 				System.out.println("payload: " + payload);
 				String data = new String(Base64.decodeBase64(payload.getBytes()));
 				
 		        Pattern p = Pattern.compile("[\\x00-\\x1f]");
 		        Matcher m = p.matcher(data);
 		        data = m.replaceAll("");
 		        if(data.charAt(data.length() - 1) != '}') {
 		        	data = data + '}';
 		        }
 				
 				System.out.println("data from elements" + data);
 				req.setAttribute("oauth", getOAuthToken(data));
 				String accessToken = getOAuthToken(data);
 				System.out.println("accessToken: " + accessToken);
 				if(accessToken == null || "".equals(accessToken)) {
 					req.setAttribute("sendRedirect", true);
					System.out.println("sending redirect");
 				} else {
 					req.setAttribute("sendRedirect", false);					
 					req.setAttribute("checkins", getCheckInInfo(req, getOAuthToken(data)));
					System.out.println("not sending redirect");
 				}
 			} else {
 				req.setAttribute("sendRedirect", true);
				System.out.println("sending redirect");
 			}
 		}
 		
	    req.getRequestDispatcher("facebook.jsp").forward(req, resp);
 
 	}
 	
 	private String getOAuthToken(String data) throws ServletException {
 		ObjectMapper mapper = new ObjectMapper();
 		String oauthToken = null;
 		try {
 			JsonNode rootNode = mapper.readValue(data.getBytes(), JsonNode.class);
 			if(rootNode.path("oauth_token") != null) {				
 				oauthToken = rootNode.path("oauth_token").getTextValue();
 			}
 		} catch (JsonParseException e) {
 			throw new ServletException(e);
 		} catch (JsonMappingException e) {
 			throw new ServletException(e);
 		} catch (IOException e) {
 			throw new ServletException(e);
 		}
 		
 		return oauthToken;
 	}
 	
     private URL buildFBUrl(HttpServletRequest req, String target, String accessToken) throws MalformedURLException {
         return new URL("https://graph.facebook.com" + target + "?access_token=" + accessToken);
     }
     
     private String readApiData(URL apiUrl) {
         StringBuilder jsonReturn = new StringBuilder();
         BufferedReader in = null;
         
         try {
             URLConnection urlConn = apiUrl.openConnection();   
             in = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
             
             String inputLine;
             
             while((inputLine = in.readLine()) != null) {
                 jsonReturn.append(inputLine);
             }             
         } catch(IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 if(in != null) {
                     in.close();                
                 }   
             } catch(IOException e) {
                 e.printStackTrace();
             }
         }
         
         return jsonReturn.toString();        
     }
     
     private String getCheckInInfo(HttpServletRequest req, String token) {
         URL restUrl;
         try {
             restUrl = buildFBUrl(req, "/me/checkins", token);
             return readApiData(restUrl);
         } catch (MalformedURLException e) {
             e.printStackTrace();
             return null;
         }
     }
 
 }
