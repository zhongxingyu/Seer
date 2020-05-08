 /**
  * 
  */
 package io.spire;
 
 import io.spire.api.Api;
 import io.spire.api.Api.APIDescriptionModel;
 import io.spire.api.Api.APIDescriptionModel.APISchemaModel;
 import io.spire.api.Billing;
 import io.spire.api.Channel;
 import io.spire.api.Resource;
 import io.spire.api.Channel.Channels;
 import io.spire.api.Subscription;
 import io.spire.api.Subscription.Subscriptions;
 import io.spire.api.Session;
 import io.spire.request.ResponseException;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 
 /**
  * @author Jorge Gonzalez
  *
  */
 public class Spire {
	public static final String SPIRE_URL = "https://api.spire.io";
 	public static final Api SPIRE_API = new Api(SPIRE_URL);
 		
 	private String spire_url;
 	private Api api;
 	private Session session;
 	private Billing billing;
 
 	/**
 	 * 
 	 */
 	public Spire(){
 		this.spire_url = SPIRE_URL;
 		this.api = SPIRE_API;
 	}
 	
 	/**
 	 * 
 	 * @param url
 	 */
 	public Spire(String url){
 		this.spire_url = url;
 		this.api = new Api(spire_url);
 	}
 	
 	/**
 	 * 
 	 * @param url
 	 * @param version
 	 */
 	public Spire(String url, String version){
 		this.spire_url = url;
 		this.api = new Api(spire_url, version);
 	}
 	
 	public Api getApi(){
 		return api;
 	}
 	
 	public Session getSession(){
 		return session;
 	}
 	
 	public void discover() throws ResponseException, IOException{
 		api.discover();
 	}
 	
 	public void start(String accountKey) throws ResponseException, IOException{
 		session  = api.createSession(accountKey);
 	}
 	
 	public void login(String email, String password) throws ResponseException, IOException{
 		session  = api.login(email, password);
 	}
 	
 	public void register(String email, String password, String passwordConfirmation) throws ResponseException, IOException{
 		session = api.createAccount(email, password, passwordConfirmation);
 	}
 	
 	public void passwordReset(String email) throws ResponseException, IOException{
 		
 	}
 	
 	public void deleteAccount() throws ResponseException, IOException{
 		session.getAccount().delete();
 	}
 	
 	public Channels getChannels() throws ResponseException, IOException{
 		return session.getChannels();
 	}
 	
 	public Billing billing() throws ResponseException, IOException{
 		billing = api.billing();
 		return billing;
 	}
 	
 	public Channels channels() throws ResponseException, IOException{
 		return session.channels();
 	}
 	
 	public Channel createChannel(String name) throws ResponseException, IOException{
 		return session.createChannel(name);
 	}
 	
 	public Subscriptions getSubscriptions(){
 		return session.getSubscriptions();
 	}
 	
 	public Subscriptions subscriptions() throws ResponseException, IOException{
 		return session.subscriptions();
 	}
 	
 	public Subscription subscribe(String name, String ...channels) throws ResponseException, IOException{
 		return session.subscribe(name, channels);
 	}
 	
 	public static class SpireFactory{
 		private static APIDescriptionModel description;
 		
 		private static void initialize() throws ResponseException, IOException{
 			if(SPIRE_API.getApiDescription() == null){
 				SPIRE_API.discover();
 				description = SPIRE_API.getApiDescription();
 			}
 		}
 		
 		public static Channel createChannel() throws ResponseException, IOException{
 			initialize();
 			return new Channel(description.schema);
 		}
 		
 		public static Channel createChannel(String capability, String url) throws ResponseException, IOException{
 			initialize();
 			return createResource(capability, url, Channel.class);
 		}
 		
 		public static Subscription createSubscription(String capability, String url) throws ResponseException, IOException{
 			initialize();
 			return createResource(capability, url, Subscription.class);
 		}
 		
 		public static <T>T createResource(String capability, String url, Class<T> T) throws ResponseException, IOException{
 			initialize();
 			Constructor<T> constructorT = null;
 			T t = null;
 			try {
 				constructorT = T.getConstructor(APISchemaModel.class);
 				t = constructorT.newInstance(description.schema);
 				Resource r = Resource.class.cast(t);
 				r.setCapability(capability);
 				r.setUrl(url);
 			} catch (NoSuchMethodException e) {
 				e.printStackTrace();
 			} catch (IllegalArgumentException e) {
 				e.printStackTrace();
 			} catch (InstantiationException e) {
 				e.printStackTrace();
 			} catch (IllegalAccessException e) {
 				e.printStackTrace();
 			} catch (InvocationTargetException e) {
 				e.printStackTrace();
 			}
 			return t;
 		}
 	}
 }
 
