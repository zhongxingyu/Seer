 package il.technion.ewolf.server;
 
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.configuration.ConfigurationException;
 import org.apache.commons.configuration.PropertiesConfiguration;
 
 import il.technion.ewolf.EwolfAccountCreator;
 import il.technion.ewolf.EwolfAccountCreatorModule;
 import il.technion.ewolf.EwolfModule;
 import il.technion.ewolf.chunkeeper.ChunKeeper;
 import il.technion.ewolf.chunkeeper.ChunKeeperModule;
 import il.technion.ewolf.dht.SimpleDHTModule;
 import il.technion.ewolf.http.HttpConnector;
 import il.technion.ewolf.http.HttpConnectorModule;
 import il.technion.ewolf.kbr.KeybasedRouting;
 import il.technion.ewolf.kbr.openkad.KadNetModule;
 import il.technion.ewolf.socialfs.SocialFSCreatorModule;
 import il.technion.ewolf.socialfs.SocialFSModule;
 import il.technion.ewolf.stash.StashModule;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 public class EwolfServer {
 	
 	private static final int EWOLF_PORT = 10000;
 	private static final String EWOLF_CONFIG = "ewolf.config.properties";
 
 
 	public static void main(String[] args) {
 		// starting server
 		ServerModule serverModule = new ServerModule();
 		Injector serverInjector = Guice.createInjector(
 				new HttpConnectorModule()
 					.setProperty("httpconnector.net.port", serverModule.getPort()),
 					serverModule);
 		
 		HttpConnector connector = serverInjector.getInstance(HttpConnector.class);
 		connector.bind();
 		connector.start();
 
 		
 		//server resources handlers register
 		connector.register("/json*", new JsonHandler() );
        connector.register("/file*", new HttpFileHandler("/",new ServerResourceFactory()));
 		
 /*
 		try {
 			PropertiesConfiguration config = new PropertiesConfiguration(EWOLF_CONFIG);
 			config.setProperty("username", "user");
 			config.setProperty("password", "1234");
 			config.save(EWOLF_CONFIG);
 		} catch (ConfigurationException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 		}
 */
 		// starting Ewolf
 		String username = null;
 		String password = null;
 		String name = null;
 		List<URI> kbrURIs = new ArrayList<URI>();
 		try {
 			PropertiesConfiguration config = new PropertiesConfiguration(EWOLF_CONFIG);
 			username = config.getString("username");
 			password = config.getString("password");
 			name = config.getString("name");
 			for (Object o: config.getList("kbr.urls")) {
 				kbrURIs.add(new URI((String)o));
 			}
 			if (username == null) {
 				//TODO get username/password from user, store to EWOLF_CONFIG and continue
 			}
 		} catch (ConfigurationException e2) {
 			// TODO Auto-generated catch block
 			e2.printStackTrace();
 			System.out.println("Cant' read configuration file");
 		} catch (URISyntaxException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			System.out.println("String from configuration file could not be parsed as a URI");
 		}
 		
 		Injector injector = Guice.createInjector(
 				new KadNetModule()
 					.setProperty("openkad.keyfactory.keysize", "20")
 					.setProperty("openkad.bucket.kbuckets.maxsize", "20")
 					.setProperty("openkad.net.udp.port", ""+(EWOLF_PORT)),
 					
 				new HttpConnectorModule()
 					.setProperty("httpconnector.net.port", ""+(EWOLF_PORT)),
 				
 				new SimpleDHTModule(),
 					
 				new ChunKeeperModule(),
 				
 				new StashModule(),
 				
 				new SocialFSCreatorModule()
 					.setProperty("socialfs.user.username", username)
 					.setProperty("socialfs.user.password", password)
 					.setProperty("socialfs.user.name", name),
 				
 				new SocialFSModule(),
 				
 				new EwolfAccountCreatorModule(),
 				
 				new EwolfModule()
 		);
 		
 		KeybasedRouting kbr = injector.getInstance(KeybasedRouting.class);
 		try {
 			kbr.create();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		// bind the http connector
 		HttpConnector ewolfConnector = injector.getInstance(HttpConnector.class);
 		ewolfConnector.bind();
 		ewolfConnector.start();
 		
 		// bind the chunkeeper
 		ChunKeeper chnukeeper = injector.getInstance(ChunKeeper.class);
 		chnukeeper.bind();
 		
 		//FIXME port for testing
 		kbr.join(kbrURIs);
 
 		EwolfAccountCreator accountCreator = injector.getInstance(EwolfAccountCreator.class);
 		
 		try {
 			accountCreator.create();
 		} catch (Exception e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		
 		//ewolf resources handlers register
		connector.register("/viewSelfProfile", injector.getInstance(ViewSelfProfileHandler.class));
 		connector.register("/addSocialGroup", injector.getInstance(AddNewSocialGroupHandler.class));
 		connector.register("/viewProfile/*", injector.getInstance(ViewProfileHandler.class));
 		connector.register("/viewSocialGroupMembers/*", injector.getInstance(ViewSocialGroupMembersHandler.class));
 		connector.register("/addTextPost/*", injector.getInstance(AddMessageBoardPostHandler.class));
 		connector.register("/viewMessageBoard/*", injector.getInstance(ViewMessageBoardHandler.class));
 		connector.register("/addSocialGroupMember/*", injector.getInstance(AddSocialGroupMemberHandler.class));

		System.out.println("server started");
 	}
 }
