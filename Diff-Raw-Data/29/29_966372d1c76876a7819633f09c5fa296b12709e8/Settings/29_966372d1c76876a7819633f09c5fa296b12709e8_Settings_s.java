 package com.askcs.ADK.lib;
 
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

 import com.askcs.webservices.Ask;
 import com.askcs.webservices.AskPortType;
 
 public class Settings {
 	public static Ask ask=new Ask();
 	/*static {
 		try{
 			ask = new Ask(new URL("http://ask-dev.customers.luna.net/~ludo/ludoDev/webservices/index.php?wsdl"), new QName("urn:webservices.askcs.com"));
 		}catch(MalformedURLException ex){
 			throw new RuntimeException();
 		}
 	};*/
 	public static final AskPortType askport = ask.getAskPort();
 }
