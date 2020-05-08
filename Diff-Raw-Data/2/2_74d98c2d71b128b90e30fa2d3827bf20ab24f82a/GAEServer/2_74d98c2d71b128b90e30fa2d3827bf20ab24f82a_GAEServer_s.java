 package michael.ranks.gae;
 
 public class GAEServer {
 	public static void main(String... args) throws Exception {
		String[] warArgs = {"com.google.appengine.tools.development.DevAppServerMain", "--port=8080","c:/dev/workspace/chalanga/Foosball/gae-war/"};
 		com.google.appengine.tools.KickStart.main(warArgs);
 	}
 }
