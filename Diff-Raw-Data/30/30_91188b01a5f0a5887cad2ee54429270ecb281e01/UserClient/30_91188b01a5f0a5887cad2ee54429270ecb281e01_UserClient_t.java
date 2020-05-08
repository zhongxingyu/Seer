 package RESTSystemClient;
 
 import systemHibernateEntities.User;
 import javax.ws.rs.core.MediaType;
 
 import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
 
 import staticResources.Configuration;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 
 public class UserClient {
 	private static String RestServiceURLPath;
 	static {
 		RestServiceURLPath = Configuration.getSiteUrl()+"RESTSystem/userService";
 	}
 	public static User get(String parameter){
 		System.out.println("Client Start");
 		System.out.println("New Config");
 		ClientConfig clientConfig = new DefaultClientConfig();
 		System.out.println("Add Jackson");
 		clientConfig.getClasses().add(JacksonJsonProvider.class);
 		System.out.println("Create client");
 		Client Myclient = Client.create(clientConfig);
 		System.out.println("Parse Client Response");
		ClientResponse clientresponse;
		clientresponse = Myclient.resource(RestServiceURLPath)
 				.path("userName").path(parameter)
 				.accept(MediaType.APPLICATION_JSON).type(MediaType.APPLICATION_JSON).
 				get(ClientResponse.class);
 		System.out.println("Client End");
 		return clientresponse.getEntity(User.class);
 	}
 	
 }
