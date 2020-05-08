 package com.se325.controller;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.servlet.http.HttpServletRequest;

 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 @Controller
 //@RequestMapping("/main")
 public class AppController{
 	
 	private static String username64Bit;
 	private static String username;
 
 	@RequestMapping(value = "/home", method = RequestMethod.GET)
 	public String printWelcome(ModelMap model) {
 		model.addAttribute("message", "Hello! This is home page please log in to proceed.");
 		return "home";
 	}
 	
 	
 	@RequestMapping(value = "/UploadReplay", method = RequestMethod.POST)
 	public String upload(HttpServletRequest request) {
		request.setAttribute("username64Bit", username64Bit);
 //		model.addAttribute("message", "Hello! This is Spring MVC Web Controller.");
 		return "uploadFile";
 	}
 	
 	@RequestMapping(value = "/common", method = RequestMethod.GET)
 	public String printLoggedIn(ModelMap model) throws ParserConfigurationException, MalformedURLException, SAXException, IOException{
 		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		username64Bit = this.get64BitId(auth.getName());//gets the 64 bit user name
 		username = this.getSteamUsername(username64Bit);//gets your display name for steam
 		model.addAttribute("message", "Hello! "+username+" You Have Logged In choose file to upload");
 		return "loggedIn_home";
 	}
 	
 	@RequestMapping(value = "/denied", method = RequestMethod.GET)
  	public String getDeniedPage() {
 		return "denied";
 	}
 	
 	
 	@RequestMapping(value = "/logout", method = RequestMethod.GET)
  	public String logout(HttpServletRequest request) {
 		request.getSession(false).invalidate();
 		return "home";
 	}
 	
 	
 	private String get64BitId(String claimedId){		
		
		//getting only the 64bit number from the returned claimed ID from steam
		//claimedId is in the format: http://steamcommunity.com/openid/id/<steamid>
 		return claimedId.substring(claimedId.indexOf("/id/")+4);
 	}
 	
 	private String getSteamUsername(String username) throws ParserConfigurationException, MalformedURLException, SAXException, IOException{
 		
 		//getting xml of your steam profile which contains display name
 		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
 		DocumentBuilder builder = fac.newDocumentBuilder();
 		Document document = builder.parse(new URL("http://steamcommunity.com/profiles/"+username+"/?xml=1").openStream());
 		
 		//extracting the "<steamID>" tag which contains display name
 		NodeList rootElement = document.getElementsByTagName("steamID");
 		
 		return rootElement.item(0).getTextContent();
 		
 	}
 
 }
