 package utils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import model.User;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.util.Xml;
 
 public class XMLParser2 {
 
 	private static final String ns = null;
 	public static final int GET_CUSTOMER_BY_EMAIL = 1;
 	public static final int GET_CUSTOMER_BY_ID = 2;
 	   
     public List parse(InputStream in, int option) throws XmlPullParserException, IOException {
         try {
             XmlPullParser parser = Xml.newPullParser();
             parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
             parser.setInput(in, null);
             parser.nextTag();
             return readPrestashop(parser, option);
         } finally {
             in.close();
         }
     }
 	
 	private List readPrestashop(XmlPullParser parser, int option) throws XmlPullParserException, IOException {
 		List entries = new ArrayList();
 		
 		parser.require(XmlPullParser.START_TAG, ns, "prestashop");
 		while(parser.next() != XmlPullParser.END_TAG){
 			if(parser.getEventType() != XmlPullParser.START_TAG){
 				continue;
 			}
 			String name = parser.getName();
 			System.out.println("parser getName: "+ parser.getName());
 			if(name == "customers"){//when you resquest a user by email
 				entries.add(readCustomers(parser));
 			}else if(name == "customer"){//when you request a user by id
 				entries.add(readCustomer(parser));
 			}
 		}
 		return entries;
 	}
 	
 	//Read all data from a customer
 	private User readCustomer(XmlPullParser parser) throws XmlPullParserException, IOException {
 		parser.require(XmlPullParser.START_TAG, ns, "customer");
 		String customerID=null;
 		String login = null;
 		String password = null;
 		
 		while(parser.next() != XmlPullParser.END_TAG){
 			if(parser.getEventType() != XmlPullParser.START_TAG){
 				continue;
 			}
 			String name = parser.getName();
			//System.out.println("readCustomer parser getName: "+ parser.getName());
 			if(name.equals("id")){
 				customerID = readID(parser);
 			}else if(name.equals("email")){
 				login = readEmail(parser);
 			}else if(name.equals("passwd")){
				password = readPassword(parser);
 			}else{
 				skip(parser);
 			}
 		}
 		
		return new User( login, password, customerID);
 	}
 	
 	private String readID(XmlPullParser parser) throws XmlPullParserException, IOException{
 		parser.require(XmlPullParser.START_TAG, ns, "id");
 		String id = readText(parser);
 		System.out.println("readID: "+id);
 		parser.require(XmlPullParser.END_TAG, ns, "id");
 		return id;
 	}
 	
 	private String readPassword(XmlPullParser parser) throws XmlPullParserException, IOException{
 		parser.require(XmlPullParser.START_TAG, ns, "passwd");
 		String passwd = readText(parser);
 		System.out.println("readPassword: "+passwd);
 		parser.require(XmlPullParser.END_TAG, ns, "passwd");
 		return passwd;
 	}
 	
 	private String readEmail(XmlPullParser parser) throws XmlPullParserException, IOException{
 		parser.require(XmlPullParser.START_TAG, ns, "email");
 		String login = readText(parser);
 		System.out.println("readEmail: "+login);
 		parser.require(XmlPullParser.END_TAG, ns, "email");
 		return login;
 	}
 	
 	//Read the xml returned from ?email= query and return the id of requested user
 	private String readCustomers(XmlPullParser parser) throws XmlPullParserException, IOException {
 		parser.require(XmlPullParser.START_TAG, ns, "customers");
 		String customerID=null;
 		
 		while(parser.next() != XmlPullParser.END_TAG){
 			if(parser.getEventType() != XmlPullParser.START_TAG){
 				continue;
 			}
 			String name = parser.getName();
 			if(name.equals("customer")){
 				customerID = readCustomerID(parser);
 			}else{
 				skip(parser);
 			}
 		}
 		return(customerID);
 	}
 	
 	//Read the customer Id from ?email= query
 	private String readCustomerID(XmlPullParser parser) throws XmlPullParserException, IOException{
 		parser.require(XmlPullParser.START_TAG, ns, "customer");
 		String tag = parser.getName();
 		System.out.println("readCustomerID parser getName: "+ parser.getName());
 		String id = parser.getAttributeValue(null, "id");
 		System.out.println("readCustomerID parser id: "+ id);
 		
 		return id;
 	}
 	
 	// For the tags login and password, extracts their text values.
 	private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
 	    String result = "";
 	    if (parser.next() == XmlPullParser.TEXT) {
 	        result = parser.getText();
 	        parser.nextTag();
 	    }
 	    return result;
 	}
 	
 	private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
 	    if (parser.getEventType() != XmlPullParser.START_TAG) {
 	        throw new IllegalStateException();
 	    }
 	    int depth = 1;
 	    while (depth != 0) {
 	        switch (parser.next()) {
 	        case XmlPullParser.END_TAG:
 	            depth--;
 	            break;
 	        case XmlPullParser.START_TAG:
 	            depth++;
 	            break;
 	        }
 	    }
 	 }
 	
 	
 }
