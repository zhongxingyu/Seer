 package ch.cern.atlas.apvs.server;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.event.ServerSettingsChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.settings.ServerPwds;
 import ch.cern.atlas.apvs.client.settings.ServerSettings;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.eventbus.shared.RequestRemoteEvent;
 
 import com.cedarsoftware.util.io.JsonReader;
 import com.cedarsoftware.util.io.JsonWriter;
 
 public class ServerSettingsStorage {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 	
 	private static final String APVS_SERVER_SETTINGS = "APVS.server.settings";
 	private static final String APVS_SERVER_PWDS = "APVS.server.pwds";
 	private static ServerSettingsStorage instance;
 	private ServerSettings settings;
 	private ServerPwds pwds;
 
 	private ServerSettingsStorage(final RemoteEventBus eventBus) {
 
 		load();
 
 		ServerSettingsChangedRemoteEvent.register(eventBus,
 				new ServerSettingsChangedRemoteEvent.Handler() {
 
 					@Override
 					public void onServerSettingsChanged(
 							ServerSettingsChangedRemoteEvent event) {
 						settings = event.getServerSettings();
 
 						store();
 					}
 				});
 
 		RequestRemoteEvent.register(eventBus, new RequestRemoteEvent.Handler() {
 
 			@Override
 			public void onRequestEvent(RequestRemoteEvent event) {
 				if (event.getRequestedClassName().equals(
 						ServerSettingsChangedRemoteEvent.class.getName())) {
 					eventBus.fireEvent(new ServerSettingsChangedRemoteEvent(settings));
 				}
 			}
 		});
 	}
 
 	public static ServerSettingsStorage getInstance(RemoteEventBus eventBus) {
 		if (instance == null) {
 			instance = new ServerSettingsStorage(eventBus);
 		}
 		return instance;
 	}
 	
 	public ServerPwds getPasswords() {
 		return pwds;
 	}
 
 	private void load() {
 		ServerStorage store = ServerStorage.getLocalStorageIfSupported();
 		if (store == null) {
 			log.warn("Server Settings will not be stored");
 			return;
 		}
 
 		String json = store.getItem(APVS_SERVER_SETTINGS);
 		if (json != null) {
 			settings = (ServerSettings) JsonReader.toJava(json);
 		}
 
 		if (settings == null) {
 			log.warn("Could not read Server Settings, using defaults");
 			settings = new ServerSettings(true);
 		} else {
 			log.info("Server Settings Read");			
 		}
 		
 		String jsonPwds = store.getItem(APVS_SERVER_PWDS);
 		if (jsonPwds != null) {
 			pwds = (ServerPwds) JsonReader.toJava(jsonPwds);
 		}
 
 		if (pwds == null) {
 			pwds = new ServerPwds(true);
 		}
 	}
 
 	private void store() {
 		ServerStorage store = ServerStorage.getLocalStorageIfSupported();
 		if (store == null) {
 			return;
 		}
 
 		String json = JsonWriter.toJson(settings);
 //		log.info("Storing json " + json);
 
 		if (json != null) {
 			store.setItem(APVS_SERVER_SETTINGS, json);
 		}
 		
 		String jsonPwds = JsonWriter.toJson(pwds);
 		if (jsonPwds != null) {
 			store.setItem(APVS_SERVER_PWDS, jsonPwds);
 		}
 	}
 
 	public void setPassword(String name, String password) {
 		System.err.println("Storing "+name+" "+password);
 		pwds.put(name, password);
 		store();
 	}
 }
