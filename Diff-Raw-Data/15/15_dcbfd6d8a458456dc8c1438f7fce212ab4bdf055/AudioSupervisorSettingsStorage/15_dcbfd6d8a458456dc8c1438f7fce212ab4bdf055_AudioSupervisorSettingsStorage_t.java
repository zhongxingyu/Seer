 package ch.cern.atlas.apvs.server;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.cern.atlas.apvs.client.event.AudioSupervisorSettingsChangedRemoteEvent;
 import ch.cern.atlas.apvs.client.event.AudioSupervisorStatusRemoteEvent;
 import ch.cern.atlas.apvs.client.settings.VoipAccount;
 import ch.cern.atlas.apvs.eventbus.shared.RemoteEventBus;
 import ch.cern.atlas.apvs.eventbus.shared.RequestRemoteEvent;
 
 import com.cedarsoftware.util.io.JsonReader;
 import com.cedarsoftware.util.io.JsonWriter;
 
 public class AudioSupervisorSettingsStorage {
 
 	private Logger log = LoggerFactory.getLogger(getClass().getName());
 	
 	private static final String APVS_AUDIO_SUPERVISOR_SETTINGS = "APVS.audioSupervisor.settings";
 	private static AudioSupervisorSettingsStorage instance;
 	private VoipAccount supervisorAccount;
 
 	public AudioSupervisorSettingsStorage(final RemoteEventBus eventBus) {
 
 		load();
 
 		AudioSupervisorSettingsChangedRemoteEvent.register(eventBus, new AudioSupervisorSettingsChangedRemoteEvent.Handler() {
 			
 			@Override
 			public void onAudioSupervisorSettingsChanged(AudioSupervisorSettingsChangedRemoteEvent event) {
 				supervisorAccount = event.getSupervisorSettings();
 				store();	
 				
 			}
 		}); 
 		
 		RequestRemoteEvent.register(eventBus, new RequestRemoteEvent.Handler() {
 
 			@Override
 			public void onRequestEvent(RequestRemoteEvent event) {
 				if (event.getRequestedClassName().equals(AudioSupervisorSettingsChangedRemoteEvent.class.getName())) {
 					eventBus.fireEvent(new AudioSupervisorSettingsChangedRemoteEvent(supervisorAccount));
 				}
 			}
 		});
 
 		eventBus.fireEvent(new AudioSupervisorSettingsChangedRemoteEvent(supervisorAccount));
 	}
 
 	public static AudioSupervisorSettingsStorage getInstance(RemoteEventBus eventBus) {
 		if (instance == null) {
 			instance = new AudioSupervisorSettingsStorage(eventBus);
 		}
 		return instance;
 	}
 
 	private void load() {
 		ServerStorage store = ServerStorage.getLocalStorageIfSupported();
 		if (store == null) {
			log.warn("Supervisor Audio Settings will not be stored");
 			return;
 		}
 
 		String json = store.getItem(APVS_AUDIO_SUPERVISOR_SETTINGS);
 		
 		
 		//************** Supervisor Settings **************
 		if (json != null) {
 			supervisorAccount = (VoipAccount) JsonReader.toJava(json);
 		}
 
 		if (supervisorAccount == null) {
			log.warn("Could not read Supervisor Audio Settings, using defaults");
 			supervisorAccount = new VoipAccount(true);
 		}else{
 			log.info("Audio Supervisor Settings Read");
 		}
 		
 	}
 
 	private void store() {
 		ServerStorage store = ServerStorage.getLocalStorageIfSupported();
 		if (store == null) {
 			return;
 		}
 
 		String json = JsonWriter.toJson(supervisorAccount);
 //		log.info("Storing json " + json);
 
 		if (json != null) {
 			store.setItem(APVS_AUDIO_SUPERVISOR_SETTINGS, json);
 		}
 	}
 }
