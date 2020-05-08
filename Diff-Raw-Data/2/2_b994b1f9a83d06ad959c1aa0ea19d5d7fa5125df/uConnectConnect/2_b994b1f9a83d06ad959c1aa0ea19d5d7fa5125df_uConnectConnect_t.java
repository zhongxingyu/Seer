 package com.useful.useful.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 
 import com.dropbox.client2.DropboxAPI;
 import com.dropbox.client2.session.AccessTokenPair;
 import com.dropbox.client2.session.AppKeyPair;
 import com.dropbox.client2.session.Session.AccessType;
 import com.dropbox.client2.session.WebAuthSession;
 import com.useful.useful.useful;
 
 public class uConnectConnect {
 	private boolean loaded = false;
 	private SortedMap<String, String> oauth = new TreeMap<String, String>();
 	private String pluginAuth = "";
 	private uConnectConnect instance = null;
 	private static String APPKEY = "";
     private static String APPSECRET = "";
 	public uConnectConnect(String pluginAuth, String appkey, String appsecret, String token, String secret){
 		this.loaded = true;
 		this.oauth.put("oauth_consumer_key", appkey);
 		this.oauth.put("oauth_signature_method", "HMAZ-SHA1");
 		this.oauth.put("oauth_version", "1.0");
 		this.oauth.put("oauth_token", token);
 		this.oauth.put("oauth_token_secret", secret);
 		this.pluginAuth = pluginAuth;
 		this.instance = this;
 		APPKEY = appkey;
 		APPSECRET = appsecret;
 	}
     private static DropboxAPI<WebAuthSession> mDBApi = null;
     private static final AccessType ACCESS_TYPE = AccessType.APP_FOLDER; 
     private final uConnectConnect ucInstance = instance;
     	public Boolean uploadYaml(final YamlConfiguration yaml, final String path, final String uuid, final String pluginAuth){
     		if(!pluginAuth.equals(this.pluginAuth)){
     			return false;
     		}
     		final String token = this.oauth.get("oauth_token");
     		final String secret = this.oauth.get("oauth_token_secret");
     		useful.plugin.getServer().getScheduler().runTaskAsynchronously(useful.plugin, new Runnable(){
 
 				@Override
 				public void run() {
 					try {
 			        	AppKeyPair appKeys = new AppKeyPair(APPKEY, APPSECRET);
 						WebAuthSession session = new WebAuthSession(appKeys, ACCESS_TYPE);
 						AccessTokenPair tokens = new AccessTokenPair(token, secret);
 						session.setAccessTokenPair(tokens);
 						//FileInputStream in = new FileInputStream(yaml.saveToString());
 						
 						File dir = new File(useful.plugin.getDataFolder().getAbsolutePath() + File.separator + "uConnect" + File.separator + "Data cache");
 						dir.mkdirs();
 						
 						final File file = File.createTempFile(uuid, ".uc", dir);
 						yaml.save(file);
 						file.setReadOnly();
 						FileInputStream in = new FileInputStream(file);
 						
 												//TODO open yaml as instream wivout file  (V not working?)
 						/*
 						FileInputStream in = new FileInputStream(yaml.saveToString());
 						
 						*/
 						mDBApi = new DropboxAPI<WebAuthSession>(session);
 						mDBApi.putFileOverwrite(path, in, file.length(), null);
 						in.close();
 						file.delete();
 						/* setup again if i delete from dropbox
 						WebAuthSession session = new WebAuthSession(appKeys, ACCESS_TYPE);
 						WebAuthInfo authInfo = session.getAuthInfo();
 						
 						RequestTokenPair pair = authInfo.requestTokenPair;
 						String url = authInfo.url;
 			 
 						Desktop.getDesktop().browse(new URL(url).toURI());
 						JOptionPane.showMessageDialog(null, "Press ok to continue once you have authenticated.");
 						session.retrieveWebAccessToken(pair);
 						
 						AccessTokenPair tokens = session.getAccessTokenPair();
 						*/
 						//key:  l4yln3msdyua24o secret:  jf23d653v9cryms   -   for dropbox
 						//System.out.println("Use this token pair in future so you don't have to re-authenticate each time:");
 						/*
 						System.out.println("Key token: " + tokens.key);
 						System.out.println("Secret token: " + tokens.secret);
 						*/	
 						//ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContents.getBytes());
 					} catch (Exception e) {
 						useful.plugin.uconnect.tasks.put(uuid, true);
 					}
 					useful.plugin.uconnect.tasks.put(uuid, true);
 					/*
 					YamlConfiguration result = new YamlConfiguration();
 					try {
 						result.load(file);
 					} catch (Exception e) {
 						request.setType("error");
 						result.set("error.msg", "Unable to connect to uConnect!");
 						request.setData(result);
 					}
 					request.setData(result);
 					UConnectDataAvailableEvent event = new UConnectDataAvailableEvent(request, request.getSender());
 					useful.plugin.getServer().getPluginManager().callEvent(event);
 					*/
 					
 					useful.plugin.uconnect.tasks.remove(uuid);
 				}});
     		
         return true;
     	}
     	
     	public void getFile(final String path, final String uuid, final UConnectDataRequest request, final String pluginAuthentication){
     		useful.plugin.uconnect.tasks.put(uuid, false);
     		if(!pluginAuthentication.equals(this.pluginAuth)){
     			if(request.getSender() == null){
     				return;
     			}
     			request.getSender().sendMessage(useful.plugin.colors.getError() + "ILLEGAL uconnect request!");
     			return;
     		}
     		final String token = this.oauth.get("oauth_token");
     		final String secret = this.oauth.get("oauth_token_secret");
     		useful.plugin.getServer().getScheduler().runTaskAsynchronously(useful.plugin, new Runnable(){
 
 				@Override
 				public void run() {
 					try {
     					AppKeyPair appKeys = new AppKeyPair(APPKEY, APPSECRET);
     	    			WebAuthSession session = new WebAuthSession(appKeys, ACCESS_TYPE);
     	    			AccessTokenPair tokens = new AccessTokenPair(token, secret);
     	    			session.setAccessTokenPair(tokens);
     	    			mDBApi = new DropboxAPI<WebAuthSession>(session);
     	    			/* setup again if i delete from dropbox
     	    			WebAuthSession session = new WebAuthSession(appKeys, ACCESS_TYPE);
     	    			WebAuthInfo authInfo = session.getAuthInfo();
     	    			
     	    			RequestTokenPair pair = authInfo.requestTokenPair;
     	    			String url = authInfo.url;
     	     
     	    			Desktop.getDesktop().browse(new URL(url).toURI());
     	    			JOptionPane.showMessageDialog(null, "Press ok to continue once you have authenticated.");
     	    			session.retrieveWebAccessToken(pair);
     	    			
     	    			AccessTokenPair tokens = session.getAccessTokenPair();
     	    			*/
     	    			//key:  l4yln3msdyua24o secret:  jf23d653v9cryms   -   for storm345Dev dropbox
     	    			//System.out.println("Use this token pair in future so you don't have to re-authenticate each time:");
     	    			/*
     	    			System.out.println("Key token: " + tokens.key);
     	    			System.out.println("Secret token: " + tokens.secret);
     	    			*/
     	    			//ByteArrayInputStream inputStream = new ByteArrayInputStream(fileContents.getBytes());
     	    			InputStream stream = null;
     	    			try {
 							stream = mDBApi.getFileStream(path, null);
 						} catch (Exception e1) {
 							if(stream!=null){
 							stream.close();
 							}
 							YamlConfiguration newFile = new YamlConfiguration();
 							newFile.set("uconnect.create", true);
 							String uuid = UniqueString.generate();
 							useful.plugin.uconnect.tasks.put(uuid, false);
 							try {
 								Boolean success = ucInstance.uploadYaml(newFile, "/main.yml", uuid, pluginAuthentication);
 								if(!success){
 									if(request.getSender() == null){
 										return;
 									}
 									request.getSender().sendMessage(useful.plugin.colors.getError()+"ILLEGAL Uconnect access!");
 								}
 							} catch (Exception e) {
 								if(request.getSender() == null){
 									return;
 								}
								request.getSender().sendMessage(useful.plugin.colors.getError()+"Euston we've got a problem! It appears the uconnect service is temporarily unavailble! This could be an error or perhaps the server isn't connected to the web... Or the web disappeared?"); //:(
 							}
 							return;
 						}
     	    			useful.plugin.uconnect.tasks.put(uuid, true);
     					YamlConfiguration result = new YamlConfiguration();
     					try {
     						result.load(stream);
     					} catch (Exception e) {
     						request.setType("error");
     						result.set("error.msg", "Error connecting to uConnect!");
     						request.setData(result);
     					}
     					stream.close();
     					request.setData(result);
     					UConnectDataAvailableEvent event = new UConnectDataAvailableEvent(request, request.getSender());
     					useful.plugin.getServer().getPluginManager().callEvent(event);
     					useful.plugin.uconnect.tasks.remove(uuid);
     		} catch (Exception e) {
     			e.printStackTrace();
     			useful.plugin.uconnect.tasks.put(uuid, true);
     		}
 					
 				}});
             
             return;
         	}
     	public void deleteFile(final String path, final String uuid, final String pluginAuthentication){
     		if(!pluginAuthentication.equals(this.pluginAuth)){
     			return;
     		}
     		final String token = this.oauth.get("oauth_token");
     		final String secret = this.oauth.get("oauth_token_secret");
     		useful.plugin.getServer().getScheduler().runTaskAsynchronously(useful.plugin, new Runnable(){
 
 				@Override
 				public void run() {
 					try {
 		    			AppKeyPair appKeys = new AppKeyPair(APPKEY, APPSECRET);
 		    			WebAuthSession session = new WebAuthSession(appKeys, ACCESS_TYPE);
 		    			AccessTokenPair tokens = new AccessTokenPair(token, secret);
 		    			session.setAccessTokenPair(tokens);
 		    			mDBApi = new DropboxAPI<WebAuthSession>(session);
 		    			mDBApi.delete(path);
 		    		}
 		    		catch (Exception e) {
 		    			e.printStackTrace();
 		    			useful.plugin.uconnect.tasks.put(uuid, true);
 		    			return;
 		    		}
 		    		useful.plugin.uconnect.tasks.put(uuid, true);
 		    		useful.plugin.uconnect.tasks.remove(uuid);
 				}});
     		
     		return;
     	}//UCONNECT WORK
     	public DropboxAPI<WebAuthSession> getApi(final String pluginAuthentication){
     		if(!pluginAuthentication.equals(this.pluginAuth)){
     			return null;
     		}
     		final String token = this.oauth.get("oauth_token");
     		final String secret = this.oauth.get("oauth_token_secret");
     		try {
     			AppKeyPair appKeys = new AppKeyPair(APPKEY, APPSECRET);
     			WebAuthSession session = new WebAuthSession(appKeys, ACCESS_TYPE);
     			AccessTokenPair tokens = new AccessTokenPair(token, secret);
     			session.setAccessTokenPair(tokens);
     			mDBApi = new DropboxAPI<WebAuthSession>(session);
                 return mDBApi;
     		}
     		catch (Exception e) {
     			e.printStackTrace();
     			return null;
     		}
     	}
     	
     
 }
