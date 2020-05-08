 package ch.cern.atlas.apvs.server;
 
 import java.util.Iterator;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.domain.InterventionMap;
 import ch.cern.atlas.apvs.client.event.AudioUsersSettingsChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.InterventionMapChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.settings.AudioSettings;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.eventbus.shared.RequestRemoteEvent;
 
 import com.cedarsoftware.util.io.JsonReader;
 import com.cedarsoftware.util.io.JsonWriter;
 
 public class AudioUsersSettingsStorage {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 	
 	private static final String APVS_AUDIO_USERS_SETTINGS = "APVS.audioUsers.settings";
 	private static AudioUsersSettingsStorage instance;
 	private AudioSettings audioSettings;
 
 	public AudioUsersSettingsStorage(final RemoteEventBus eventBus) {
 
 		load();
 
 		AudioUsersSettingsChangedRemoteEvent.register(eventBus, new AudioUsersSettingsChangedRemoteEvent.Handler() {
 					
 					@Override
 					public void onAudioUsersSettingsChanged(AudioUsersSettingsChangedRemoteEvent event) {
 						audioSettings = event.getAudioSettings();
 						store();	
 					}
 		});
 		
 		InterventionMapChangedRemoteEvent.subscribe(eventBus, new InterventionMapChangedRemoteEvent.Handler() {
 
 			@Override
 			public void onInterventionMapChanged(
 					InterventionMapChangedRemoteEvent event) {
 				InterventionMap interventions = event.getInterventionMap();
 				boolean changed = false;
 				for (Iterator<String> i = interventions.getPtuIds().iterator(); i.hasNext(); ) {
 					String ptuId = i.next();
 					if(audioSettings.contains(ptuId)) {
 						boolean set = audioSettings.setUsername(ptuId, interventions.get(ptuId).getName());
 						changed |= set;
 					} else {
 						boolean added = audioSettings.add(ptuId);
 						changed |= added;
 					}
 				}
 
 				if (changed) {
 					eventBus.fireEvent(new AudioUsersSettingsChangedRemoteEvent(audioSettings));
 				}
 			}
 		});
 		
 		RequestRemoteEvent.register(eventBus, new RequestRemoteEvent.Handler() {
 
 			@Override
 			public void onRequestEvent(RequestRemoteEvent event) {
 				if (event.getRequestedClassName().equals(AudioUsersSettingsChangedRemoteEvent.class.getName())) {
 					eventBus.fireEvent(new AudioUsersSettingsChangedRemoteEvent(audioSettings));
 				}
 			}
 		});
 
 		eventBus.fireEvent(new AudioUsersSettingsChangedRemoteEvent(audioSettings));
 	}
 
 	public static AudioUsersSettingsStorage getInstance(RemoteEventBus eventBus) {
 		if (instance == null) {
 			instance = new AudioUsersSettingsStorage(eventBus);
 		}
 		return instance;
 	}
 
 	private void load() {
 		ServerStorage store = ServerStorage.getLocalStorageIfSupported();
 		if (store == null) {
			log.warn("Audio Users Settings will not be stored");
 			return;
 		}
 
 		String json = store.getItem(APVS_AUDIO_USERS_SETTINGS);
 		
 		
 		//************** Audio Settings **************
 		if (json != null) {
 			audioSettings = (AudioSettings) JsonReader.toJava(json);
 		}
 
 		if (audioSettings == null) {
			log.warn("Could not read Audio Settings, using defaults");
 			audioSettings = new AudioSettings();
 		}else{
 			log.info("Audio User Settings Read");		
 		}
 		
 	}
 
 	private void store() {
 		ServerStorage store = ServerStorage.getLocalStorageIfSupported();
 		if (store == null) {
 			return;
 		}
 
 		String json = JsonWriter.toJson(audioSettings);
 //		log.info("Storing json " + json);
 
 		if (json != null) {
 			store.setItem(APVS_AUDIO_USERS_SETTINGS, json);
 		}
 	}
 }
