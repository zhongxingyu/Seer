 public class UrlHandler {
 
 	public static String resolve(String url) {
 		if(url == null){
			System.out.println("im null");
 			throw new IllegalArgumentException();
 		}
 
 		switch(url){
		case "":
			System.out.println("im the empty string");
			return "index.html";
 		case "/":
			System.out.println("im the slash");
 			return "index.html";
		case "/favicon.ico":
			System.out.println("im the favicon");
			return "";
 		default:
			return "404.html";
 		}
 	}
 }
