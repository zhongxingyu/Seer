 package djproject.gui.client;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 
 import djproject.comments.Comment;
 import djproject.song_history.History;
 import djproject.songs.Songs;
 import djproject.wishes.Wish;
 
 public class RESTHandler {
 	
 	
 	static ClientConfig config = new DefaultClientConfig();
 	static Client client = Client.create(config);
 	
 	final static String host = "http://localhost:4444";
 	
 	public static Songs getSongs(String type, String text) {
 		WebResource service = client.resource(host + "/songs/?type=" + type + "&text=" + text);
 		ClientResponse response = service.type("application/xml")
 	            .accept("application/xml")
 	            .get(ClientResponse.class);
 		return response.getEntity(Songs.class);
 	}
 	
 	public static void addWish(Wish w) {
 		WebResource service = client.resource(host + "/wishes/");
 		service.type("application/xml")
 	            .accept("application/xml")
 	            .entity(w)
 	            .post();
 	}
 
 	public static void addComment(Comment c) {
		WebResource service = client.resource(host + "/comments/");
 		service.type("application/xml")
 	            .accept("application/xml")
 	            .entity(c)
 	            .post();
 	}
 
 	public static History getHistory() {
 		WebResource service = client.resource(host + "/history/");
 		ClientResponse response = service.type("application/xml")
 	            .accept("application/xml")
 	            .get(ClientResponse.class);
 		return response.getEntity(History.class);
 	}
 	
 }
