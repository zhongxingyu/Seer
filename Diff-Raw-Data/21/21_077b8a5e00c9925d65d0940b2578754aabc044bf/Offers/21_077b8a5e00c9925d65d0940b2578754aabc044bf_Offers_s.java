 package controllers;
 
 
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Date;
 import java.util.List;
 
 import models.Acceptance;
 import models.Offer;
 import models.User;
 import play.libs.WS;
 import play.libs.WS.HttpResponse;
 import play.mvc.Controller;
 import siena.Model;
 import siena.Query;
 
 import com.google.gson.Gson;
 
 import dwolla.DwollaTransfer;
 
 public class Offers extends Controller {
 
 	protected static Offer parseJSON(InputStream in) {
 		return new Gson().fromJson(new InputStreamReader(in), Offer.class);
 	}
 
 	public static void create() {
 		Offer offer = parseJSON(request.body);
 		String token = request.headers.get("authorization").values.get(0);
 		User owner = all().filter("token", token.replaceAll("\"", "")).get();
 		offer.owner = owner;
 		offer.insert();
 		renderJSON(offer);
 	}
 	
 	public static void getAll() {
 		renderJSON(Model.all(Offer.class).fetch());
 	}
 	
 	public static void accept(Long id) throws Exception {
 		String token = request.headers.get("authorization").values.get(0);
 		User user = all().filter("token", token.replaceAll("\"", "")).get();
 		Offer offer = Model.all(Offer.class).filter("id", id).get();
 		String url = "https://api.singly.com/types/statuses?access_token="+ user.singlyAccessToken + "&to="+offer.type.toLowerCase()+"&body="+ URLEncoder.encode(offer.content,"UTF-8");
 		HttpResponse post = WS.url(url).post();
 		Acceptance acceptance = new Acceptance();
 		acceptance.acceptor = user;
 		acceptance.offer = offer;
 		acceptance.executed = true;
 		acceptance.executionTime = new Date();
 		acceptance.save();
 		renderJSON(acceptance);
 	}
 	
 	public static void acceptances() {
 		renderJSON(Model.all(Acceptance.class).fetch());
 	}
 	
 
 
 	static Query<User> all() {
         return Model.all(User.class);
     }
 }
