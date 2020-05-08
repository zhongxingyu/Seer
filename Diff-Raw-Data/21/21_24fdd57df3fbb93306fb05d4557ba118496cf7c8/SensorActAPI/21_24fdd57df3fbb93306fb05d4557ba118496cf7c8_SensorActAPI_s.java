 /*
  * Name: SensorActAPI.java
  * Project: SensorAct, MUC@IIIT-Delhi
  * Version: 1.0
  * Date: 2012-04-14
  * Author: Pandarasamy Arjunan, Haksoo Choi
  */
 package edu.iiitd.muc.sensoract.api;
 
 import java.util.Iterator;
 import java.util.List;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 import controllers.Application;
 import edu.iiitd.muc.sensoract.api.data.DataQuery;
 import edu.iiitd.muc.sensoract.api.data.DataUploadWaveSegment;
 import edu.iiitd.muc.sensoract.api.device.DeviceAdd;
 import edu.iiitd.muc.sensoract.api.device.DeviceDelete;
 import edu.iiitd.muc.sensoract.api.device.DeviceGet;
 import edu.iiitd.muc.sensoract.api.device.DeviceList;
 import edu.iiitd.muc.sensoract.api.device.DeviceSearch;
 import edu.iiitd.muc.sensoract.api.device.DeviceShare;
 import edu.iiitd.muc.sensoract.api.device.DeviceTemplateAdd;
 import edu.iiitd.muc.sensoract.api.device.DeviceTemplateDelete;
 import edu.iiitd.muc.sensoract.api.device.DeviceTemplateGet;
 import edu.iiitd.muc.sensoract.api.device.DeviceTemplateGlobalList;
 import edu.iiitd.muc.sensoract.api.device.DeviceTemplateList;
 import edu.iiitd.muc.sensoract.api.guardrule.GuardRuleAdd;
 import edu.iiitd.muc.sensoract.api.guardrule.GuardRuleAssociationAdd;
 import edu.iiitd.muc.sensoract.api.guardrule.GuardRuleAssociationDelete;
 import edu.iiitd.muc.sensoract.api.guardrule.GuardRuleAssociationGet;
 import edu.iiitd.muc.sensoract.api.guardrule.GuardRuleAssociationList;
 import edu.iiitd.muc.sensoract.api.guardrule.GuardRuleDelete;
 import edu.iiitd.muc.sensoract.api.guardrule.GuardRuleGet;
 import edu.iiitd.muc.sensoract.api.guardrule.GuardRuleList;
 import edu.iiitd.muc.sensoract.api.key.KeyDelete;
 import edu.iiitd.muc.sensoract.api.key.KeyDisable;
 import edu.iiitd.muc.sensoract.api.key.KeyEnable;
 import edu.iiitd.muc.sensoract.api.key.KeyGenerate;
 import edu.iiitd.muc.sensoract.api.key.KeyList;
 import edu.iiitd.muc.sensoract.api.response.ResponseFormat;
 import edu.iiitd.muc.sensoract.api.tasklet.TaskletAdd;
 import edu.iiitd.muc.sensoract.api.tasklet.TaskletCancel;
 import edu.iiitd.muc.sensoract.api.tasklet.TaskletExecute;
 import edu.iiitd.muc.sensoract.api.tasklet.TaskletGet;
 import edu.iiitd.muc.sensoract.api.tasklet.TaskletList;
 import edu.iiitd.muc.sensoract.api.tasklet.TaskletStatus;
 import edu.iiitd.muc.sensoract.api.tasklet.TaskleteDelete;
 import edu.iiitd.muc.sensoract.api.user.UserList;
 import edu.iiitd.muc.sensoract.api.user.UserLogin;
 import edu.iiitd.muc.sensoract.api.user.UserRegister;
 import edu.iiitd.muc.sensoract.constants.Const;
 import edu.iiitd.muc.sensoract.exceptions.InvalidJsonException;
 import edu.iiitd.muc.sensoract.profile.Actuator;
 import edu.iiitd.muc.sensoract.profile.DeviceProfile;
 import edu.iiitd.muc.sensoract.profile.UserProfile;
 import edu.iiitd.muc.sensoract.profile.WaveSegmentData;
 import edu.iiitd.muc.sensoract.profile.mongo.ActuatorMongo;
 import edu.iiitd.muc.sensoract.profile.mongo.DeviceProfileMongo;
 import edu.iiitd.muc.sensoract.profile.mongo.WaveSegmentDataMongo;
 import edu.iiitd.muc.sensoract.profile.rdbms.DeviceProfileRDBMS;
 import edu.iiitd.muc.sensoract.profile.rdbms.UserProfileRDBMS;
 import edu.iiitd.muc.sensoract.tasklet.DeviceEvent;
 import edu.iiitd.muc.sensoract.tasklet.TaskletManager;
 import edu.iiitd.muc.sensoract.tasklet.TaskletManagerImpl;
 import edu.iiitd.muc.sensoract.tasklet.TaskletManagerRImpl;
 import edu.iiitd.muc.sensoract.util.ParamValidator;
 import edu.iiitd.muc.sensoract.util.SensorActLogger;
 
 /**
  * Super class for all SensorAct Apis.
  * 
  * @author Pandarasamy Arjunan, Haksoo Choi
  * @version 1.0
  */
 public class SensorActAPI extends Application {
 
 	/*
 	 * API references
 	 */
 	public static RepoInfo repoInfo = new RepoInfo();
 	public static UserRegister userRegister = new UserRegister();
 	// public static UserRemove userRemove = new UserRemove();
 	public static UserLogin userLogin = new UserLogin();
 	public static UserList userList = new UserList();
 
 	public static KeyGenerate keyGenerate = new KeyGenerate();
 	public static KeyDelete keyDelete = new KeyDelete();
 	public static KeyList keyList = new KeyList();
 	public static KeyEnable keyEnable = new KeyEnable();
 	public static KeyDisable keyDisable = new KeyDisable();
 
 	public static DeviceAdd deviceAdd = new DeviceAdd();
 	public static DeviceDelete deviceDelete = new DeviceDelete();
 	public static DeviceGet deviceGet = new DeviceGet();
 	public static DeviceList deviceList = new DeviceList();
 	public static DeviceShare deviceShare = new DeviceShare();
 	public static DeviceSearch deviceSearch = new DeviceSearch();
 
 	public static DeviceTemplateAdd deviceTemplateAdd = new DeviceTemplateAdd();
 	public static DeviceTemplateDelete deviceTemplateDelete = new DeviceTemplateDelete();
 	public static DeviceTemplateGet deviceTemplateGet = new DeviceTemplateGet();
 	public static DeviceTemplateList deviceTemplateList = new DeviceTemplateList();
 	public static DeviceTemplateGlobalList deviceTemplateGlobalList = new DeviceTemplateGlobalList();
 
 	public static GuardRuleAdd guardRuleAdd = new GuardRuleAdd();
 	public static GuardRuleDelete guardRuleDelete = new GuardRuleDelete();
 	public static GuardRuleGet guardRuleGet = new GuardRuleGet();
 	public static GuardRuleList guardRuleList = new GuardRuleList();
 	public static GuardRuleAssociationAdd guardRuleAssociationAdd = new GuardRuleAssociationAdd();
 	public static GuardRuleAssociationDelete guardRuleAssociationDelete = new GuardRuleAssociationDelete();
 	public static GuardRuleAssociationGet guardRuleAssociationGet = new GuardRuleAssociationGet();
 	public static GuardRuleAssociationList guardRuleAssociationList = new GuardRuleAssociationList();
 
 	public static TaskletAdd taskletAdd = new TaskletAdd();
 	public static TaskleteDelete taskleteDelete = new TaskleteDelete();
 	public static TaskletGet taskletGet = new TaskletGet();
 	public static TaskletList taskletList = new TaskletList();
 	public static TaskletExecute taskletExecute = new TaskletExecute();
 	public static TaskletStatus taskletStatus = new TaskletStatus();
 	public static TaskletCancel taskletCancel = new TaskletCancel();
 
 	public static DataUploadWaveSegment dataUploadWaveseg = new DataUploadWaveSegment();
 	public static DataQuery dataQuery = new DataQuery();
 
 	public static Test test = new Test();
 
	public static UserProfile userProfile = new UserProfileRDBMS();
 
	public static DeviceProfile deviceProfile = new DeviceProfileRDBMS();
	//public static DeviceProfile deviceProfile = new DeviceProfileMongo();
 	
 	public static WaveSegmentData waveSegmentData = new WaveSegmentDataMongo();
 	public static Actuator actuator = new ActuatorMongo();
 
 	public static DeviceEvent deviceEvent = new DeviceEvent();
 
	public static TaskletManager taskletManager = new TaskletManagerRImpl();
 	
 	
 	/*
 	 * API helper references
 	 */
 	public static ResponseFormat response = new ResponseFormat();
 	public static ParamValidator validator = new ParamValidator();
 	// TODO Remove pretty printing in deployment.
 	public static Gson json = new GsonBuilder()
 			.serializeSpecialFloatingPointValues().setPrettyPrinting().create();
 	public static SensorActLogger log = new SensorActLogger();
 
 	/**
 	 * Converts the API request json string to corresponding request format
 	 * object.
 	 * 
 	 * @param requestJson
 	 *            Request format Json string
 	 * @param classOfT
 	 * @return Converted API request format object.
 	 * @throws InvalidJsonException
 	 *             If the Json string is not valid or not in the required
 	 *             request format
 	 */
 	protected <T> T convertToRequestFormat(final String requestJson,
 			Class<T> classOfT) throws InvalidJsonException {
 
 		T reqObj = null;
 		try {
 			reqObj = json.fromJson(requestJson, classOfT);
 		} catch (Exception e) {
 			throw new InvalidJsonException(e.getMessage());
 		}
 
 		if (null == reqObj) {
 			throw new InvalidJsonException(Const.EMPTY_JSON);
 		}
 		return reqObj;
 	}
 
 	/**
 	 * Morhphia models keep _id attributes internally. Removes those unnecessary
 	 * _id attributes for printing.
 	 * 
 	 * @param obj
 	 *            Object which contains the _id attribute.
 	 * @return Json string with _id attribute removed.
 	 */
 	protected String remove_Id(final Object obj) {
 
 		try {
 			JsonObject jo = json.toJsonTree(obj).getAsJsonObject();
 			jo.remove("_id");
 			return json.toJson(jo);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return json.toJson(obj);
 	}
 
 	/**
 	 * Morhphia models keep _id attributes internally. Removes those unnecessary
 	 * _id attributes for printing.
 	 * 
 	 * @param obj
 	 *            Object which contains the _id attribute.
 	 * @param name
 	 *            Name of the array list.
 	 * @return Json string with _id attribute removed.
 	 */
 	protected String remove_Id(final Object obj, final String name) {
 
 		try {
 			JsonObject jo = json.toJsonTree(obj).getAsJsonObject();
 			JsonArray ja = jo.getAsJsonArray(name);
 			if (null != ja) {
 				Iterator<JsonElement> itr = ja.iterator();
 				while (itr.hasNext()) {
 					itr.next().getAsJsonObject().remove("_id");
 				}
 				return jo.toString();
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return json.toJson(obj);
 	}
 
 	protected <T> JsonArray convertFromListToJsonArrayRemovingSecretKeyAndId(
 			List<T> objList) {
 		JsonArray jsonArray = new JsonArray();
 		for (T obj : objList) {
 			JsonObject jsonObj = json.toJsonTree(obj).getAsJsonObject();
 			jsonObj.remove("secretkey");
 			jsonObj.remove("_id");
 			jsonArray.add(jsonObj);
 		}
 		return jsonArray;
 	}
 }
