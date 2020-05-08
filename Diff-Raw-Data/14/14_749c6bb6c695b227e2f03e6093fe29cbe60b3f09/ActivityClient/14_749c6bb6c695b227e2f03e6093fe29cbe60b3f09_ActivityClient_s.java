 package com.pluralsite.client;
 
 import java.util.List;
 
 import javax.ws.rs.client.Client;
 import javax.ws.rs.client.ClientBuilder;
 import javax.ws.rs.client.WebTarget;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.GenericType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import javax.ws.rs.client.Entity;
 
 import com.pluralsite.model.*; 
 
 public class ActivityClient {
 	private Client client;
 
 	public ActivityClient() { 
 		client = ClientBuilder.newClient();
 		}
 	
 
 	public Activity create(Activity activity) {
 		WebTarget target = client.target("http://localhost:8080/REST-Jersey-Demo01/pluralsite/");
 
		Response response =  target.path("activities/activity").request(MediaType.APPLICATION_JSON)
				.post(Entity.entity(activity,MediaType.APPLICATION_JSON));
 		
 		if (response.getStatus() != 200) {
 			throw new RuntimeException(response.getStatus() + ": error on server.");			
 		}
 
 		return response.readEntity(Activity.class);
 	}
 	
 	
 	public Activity get(String id) {
 		WebTarget target = client.target("http://localhost:8080/REST-Jersey-Demo01/pluralsite/");
 		Response response =  target.path("activities/" + id).request().get(Response.class);
 		
 		if (response.getStatus() != 200) {
 			throw new RuntimeException(response.getStatus() + ": error on server.");
 			
 		}
 		
 		String json =  target.path("activities/" + id).request(MediaType.APPLICATION_JSON).get(String.class);
 		
 		String xml1 =  target.path("activities/" + id).request().get(String.class);
 		String xml2 =  target.path("activities/" + id).request(MediaType.APPLICATION_XML).get(String.class);
 		
 		
 		// System.out.println("Text output: " + response.getId() + " " + response.getDuration() + " " + response.getDescription());
 		System.out.println("Json output: " + json);
 		System.out.println("XML default: " + xml1);
 		System.out.println("XML explicit:" + xml2);
 		System.out.println("Class output:" + response);
 		
 		return response.readEntity(Activity.class);		
 	}
 
 	public List<Activity> get() {
 		WebTarget target = client.target("http://localhost:8080/REST-Jersey-Demo01/pluralsite/");
 		
 		// This is the nasty method of declaring a LIST to pass. 
 		List<Activity> response = target.path("activities").request().get(new GenericType<List<Activity>>() {});
 		
 		return response;
 	}
 
 public static void main(String args[]) { 
 	ActivityClient AC = new ActivityClient();
 	System.out.println("Testing object:");
 	AC.get("zzzz");
 	System.out.println("Testing List:");
 	List<Activity> AL = AC.get();
 	for (Activity al : AL) 
 		System.out.println(al);
 	
 	System.out.println("Testing bad object:");
 	AC.get("zz");
 }
 	
 }
