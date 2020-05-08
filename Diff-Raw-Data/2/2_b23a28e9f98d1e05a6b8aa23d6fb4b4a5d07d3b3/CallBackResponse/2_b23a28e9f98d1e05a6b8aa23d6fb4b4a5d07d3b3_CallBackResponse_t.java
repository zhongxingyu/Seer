 package foneapijavawrapper.callbacks;
 
 import java.util.ArrayList;
 
 import com.google.gson.Gson;
 import com.google.gson.annotations.SerializedName;
 
 public class CallBackResponse {
 
 	@SerializedName("actions")
 	private ArrayList<Action> actions;
 	@SerializedName("settings")
 	private ArrayList<Setting> settings;
 
 	public CallBackResponse() {
 		actions = new ArrayList<Action>();
 	}
 
 	public CallBackResponse(int limitCallDurationSeconds) {
 		this();
 		settings = new ArrayList<Setting>();
 		setLimitCallDuration(limitCallDurationSeconds);
 	}
 
 	@Override
 	public String toString() {
 		// TODO Auto-generated method stub
 		Gson gson = new Gson();
 		return gson.toJson(this);
 	}
 	
 
 	public void setLimitCallDuration(int limitCallDurationSeconds) {
 		for (Setting item : settings)
         {
             if (item instanceof SetLimitCallDurationSetting)
             {
                 settings.remove(item);
                 break;
             }
         }
         SetLimitCallDurationSetting limitCallSetting = new SetLimitCallDurationSetting(limitCallDurationSeconds);
         settings.add(limitCallSetting);
 	}
 
 	public void Answer() {
 		AnswerAction answerAction = new AnswerAction();
 		actions.add(answerAction);
 	}
 
 	public void BridgeTo(String otherCallId, String url) {
 		BridgeToAction bridgeToAction = new BridgeToAction(otherCallId, url);
 		actions.add(bridgeToAction);
 	}
 }
