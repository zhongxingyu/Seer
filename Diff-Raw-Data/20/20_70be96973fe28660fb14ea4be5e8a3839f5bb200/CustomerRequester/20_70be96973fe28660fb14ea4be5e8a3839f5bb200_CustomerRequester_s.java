 package utils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.os.AsyncTask;
 import model.User;
 
 public class CustomerRequester extends AsyncTask<String, Void, User>{
 	User user;
 	
 	@Override
 	protected User doInBackground(String... params) {
 		find(params[0]);
 		return user;
 	}
 	
 	private String findId(String email){
 		String id = null;
 		System.out.println("Customer Requester#findId: email: "+email);
 		Response res = new Response(Functions.urlConcat(Vars.wsServer, 
 				Vars.wsCustomersPath + "/?email="+email+"&ws_key="+Vars.wsKey));
 		InputStream i = res.getResponse();
 		try {
 						
 			XMLParser2 parser = new XMLParser2();
 			List customerId = parser.parse(i, XMLParser2.GET_CUSTOMER_BY_EMAIL);
 			id = customerId.get(0).toString();
 		} catch (XmlPullParserException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return id;
 	}
 	
 	private void findUser(String id){
 		Response res = new Response(Functions.urlConcat(Vars.wsServer, 
 				Vars.wsCustomersPath + "/"+id+"?ws_key="+ Vars.wsKey));
 		//String response = res.getResponse().toString();
 		InputStream i = res.getResponse();
 		//user = parseAttributes(response);
 		XMLParser2 parser = new XMLParser2();
 		try {
 			List entries = parser.parse(i, XMLParser2.GET_CUSTOMER_BY_ID);
 			user = (User) entries.get(0);
 		} catch (XmlPullParserException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 	private void find(String email){
 		String id = findId(email);
 		System.out.println("Customer Requester: find id "+ id);
 		findUser(id);
 	}
 	
 	private User parseAttributes(String response){
 		User user = new User();
 		
 		XMLParser xml = new XMLParser(response, "email");
 		xml.parse();
 		user.setLogin(xml.getResp());
 		
 		xml = new XMLParser(response, "password");
 		xml.parse();
 		user.setPass(xml.getResp());
 
 		return user;
 	}
 	
 	
 	
 }
