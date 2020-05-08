 public class UrlHandler {
 
 	public static String resolve(String url) {
 		if(url == null){
 			throw new IllegalArgumentException();
 		}
 
 		switch(url){
 		case "/":
 			return "index.html";
 		default:
			throw new IllegalArgumentException();
 		}
 	}
 }
