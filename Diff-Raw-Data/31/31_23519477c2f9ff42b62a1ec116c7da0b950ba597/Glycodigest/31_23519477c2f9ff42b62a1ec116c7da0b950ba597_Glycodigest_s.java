 package controllers;
 
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import play.mvc.*;
 import play.data.*;
 import play.*;
 import play.cache.*;
 import views.html.*;
 import models.*;
 import play.db.ebean.*;
 import play.data.format.*;
 import play.data.validation.*;
 import static play.data.Form.*;
 
 import com.avaje.ebean.*; //dont think this should be here due to SqlRow
 
 
 
 public class Glycodigest extends Controller {
 	
 	public static Result glycodigest(Long id) {
 		String[] enzymes = null;
 		Map<String, String> hashMap = new HashMap<String, String>();
		
 		
 		Logger.info("URL STUFF " + id );
 		
 		ct ctt = new ct();
 		ctt = ct.find.byId(id);
 		
 		try{
 			hashMap = ctt.digest(ctt.ct, enzymes);
 		}
 		catch(IOException e){
 			e.printStackTrace();
 			
 		}
 		
 		return ok( 
 				glycodigest.render(hashMap)
 				); 
 	};
 	
 	public static Result glycodigesttest(Long id, String s){
 		Map<String, String> hashMap = new HashMap<String, String>();
 		
 		String[] x = request().queryString().get("digest");
 		
 		Logger.info("---------hdhhdhd#################################  " + request().queryString().get("digest").toString() );
 		
 		//Lorequest().uri();
 		
 		return ok( 
				glycodigest.render(hashMap)
 				); 
 	}
 
 }
