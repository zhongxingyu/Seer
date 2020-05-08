 package org.greengin.sciencetoolkit.model;
 
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.Vector;
 
 import org.greengin.sciencetoolkit.logic.datalogging.DataLogger;
 import org.greengin.sciencetoolkit.model.notifications.ModelNotificationListener;
 import org.greengin.sciencetoolkit.model.notifications.NotificationListenerAggregator;
 
 import android.content.Context;
 import android.util.Log;
 
 public class ProfileManager extends AbstractModelManager implements ModelNotificationListener {
 
 	private static ProfileManager instance;
 
 	public static void init(Context applicationContext) {
 		instance = new ProfileManager(applicationContext);
 	}
 
 	public static ProfileManager getInstance() {
 		return instance;
 	}
 
 	NotificationListenerAggregator listeners;
 
 	Model settings;
 	Model appSettings;
 
 	private ProfileManager(Context applicationContext) {
 		super(applicationContext, "profiles.xml", 500);
 		settings = SettingsManager.getInstance().get("profiles");
 		appSettings = SettingsManager.getInstance().get("app");
 		listeners = new NotificationListenerAggregator(applicationContext, "profiles:notifications");
 		SettingsManager.getInstance().registerDirectListener("profiles", this);
 		initDefaultProfile();
 		checkDataConsistency();
 	}
 
 	private void initDefaultProfile() {
 		if (appSettings.getBool("create_default", true) && items.size() == 0) {
 			Model defaultProfile = createEmptyProfile();
 			defaultProfile.setString("title", "Default", true);
 			modelModified(defaultProfile);
 			listeners.fireEvent("list");
 
 			appSettings.setBool("create_default", false);
 			settings.setString("current_profile", defaultProfile.getString("id"));
 		}
 	}
 
 	private void checkDataConsistency() {
 		for (Entry<String, Model> entry : items.entrySet()) {
 			String id = entry.getKey();
 			Model model = entry.getValue();
 
 			if (id.length() == 0) {
 				Log.d("stk profiles", "empty id");
 			} else {
 				if (!id.equals(model.getString("id"))) {
 					Log.d("stk profiles", "conflicting ids: " + id + " " + model.getString("id"));
 				}
 			}
 		}
 	}
 
 	private Model createEmptyProfile() {
 		String id = getNewId();
 		Model profile = new Model(this);
 		items.put(id, profile);
 
 		profile.setString("id", id, true);
 		profile.getModel("sensors", true, true);
 		return profile;
 	}
 
 	public void createProfile(String title, boolean makeActive) {
 		Model newProfile = createEmptyProfile();
 		newProfile.setString("title", title, true);
 		modelModified(newProfile);
 		listeners.fireEvent("list");
 
		if (makeActive && !DataLogger.getInstance().isRunning()) {
 			switchActiveProfile(newProfile.getString("id"));
 		}
 	}
 
 	public void deleteProfile(String profileId) {
 		this.remove(profileId);
 		listeners.fireEvent("list");
 		DataLogger.getInstance().deleteData(profileId);
 	}
 
 	public void switchActiveProfile(String profileId) {
 		if (profileId != null && !profileId.equals(settings.getString("current_profile"))) {
 			if (DataLogger.getInstance().isRunning()) {
 				DataLogger.getInstance().stop();
 			}
 			settings.setString("current_profile", profileId);
 		}
 	}
 
 	public int profileCount() {
 		return this.items.size();
 	}
 
 	public void addSensor(Model profile, String sensorId) {
 		addSensor(profile, sensorId, false);
 	}
 
 	public void addSensor(Model profile, String sensorId, boolean suppressSave) {
 		Model profileSensors = profile.getModel("sensors", true, true);
 		int weight = profileSensors.getModels().size();
 
 		Model profileSensor = profileSensors.getModel(sensorId, true, true);
 		profileSensor.setString("id", sensorId, true);
 		profileSensor.setInt("weight", weight, true);
 		Model sensorSettings = SettingsManager.getInstance().get("sensor:" + sensorId);
 		Model profileSensorSettings = profileSensor.getModel("sensor_settings", true, true);
 		profileSensorSettings.copyPrimitives(sensorSettings, true);
 
 		if (!suppressSave) {
 			this.modelModified(profile);
 		}
 	}
 
 	public void addSensors(Model profile, Vector<String> sensorIds) {
 		addSensors(profile, sensorIds, false);
 	}
 
 	public void addSensors(Model profile, Vector<String> sensorIds, boolean suppressSave) {
 		for (String sensorId : sensorIds) {
 			addSensor(profile, sensorId, true);
 		}
 
 		if (sensorIds.size() > 0 && !suppressSave) {
 			this.modelModified(profile);
 		}
 	}
 
 	public void removeSensor(Model profile, String sensorId) {
 		Model profileSensors = profile.getModel("sensors", true, true);
 		profileSensors.clear(sensorId);
 	}
 
 	public Model get(String key) {
 		return get(key, false);
 	}
 
 	public Model getActiveProfile() {
 		return get(getActiveProfileId());
 	}
 
 	public String getActiveProfileId() {
 		return settings.getString("current_profile");
 	}
 
 	public Set<String> getProfileIds() {
 		return items.keySet();
 	}
 
 	private String getNewId() {
 		for (int i = 1;; i++) {
 			String test = Integer.toString(i);
 			if (!items.containsKey(test)) {
 				return test;
 			}
 		}
 	}
 
 	@Override
 	public void modelModified(Model model) {
 		super.modelModified(model);
 
 		if (model != null) {
 			Model profile = model.getRootParent();
 			String profileId = profile.getString("id", null);
 			listeners.fireEvent(profileId);
 		}
 	}
 
 	@Override
 	public void modelNotificationReveiced(String msg) {
 		listeners.fireEvent("switch");
 	}
 
 	public void registerUIListener(ModelNotificationListener listener) {
 		listeners.addUIListener(listener);
 	}
 
 	public void unregisterUIListener(ModelNotificationListener listener) {
 		listeners.removeUIListener(listener);
 	}
 
 	public void registerDirectListener(ModelNotificationListener listener) {
 		listeners.addDirectListener(listener);
 	}
 
 	public void unregisterDirectListener(ModelNotificationListener listener) {
 		listeners.removeDirectListener(listener);
 	}
 }
