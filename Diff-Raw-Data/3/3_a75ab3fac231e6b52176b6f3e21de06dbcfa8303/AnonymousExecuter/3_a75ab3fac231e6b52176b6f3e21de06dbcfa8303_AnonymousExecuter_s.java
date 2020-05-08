 //xumengyi@baixing.com
 package com.baixing.anonymous;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Handler;
 import android.os.Message;
 
 import com.baixing.anonymous.AnonymousNetworkListener.ResponseData;
 import com.baixing.broadcast.CommonIntentAction;
 import com.baixing.data.GlobalDataManager;
 import com.baixing.network.api.ApiError;
 import com.baixing.network.api.ApiParams;
 import com.baixing.network.api.BaseApiCommand;
 import com.baixing.network.api.BaseApiCommand.Callback;
 
 public class AnonymousExecuter implements Callback{
 	private final static String checkStatusApi = "checkAccountStatus";
 	private final static int MSG_VERIFY_TIMEOUT = 0xffff1111;
 	private AnonymousNetworkListener listener;
 	private BroadcastReceiver smsReceiver = null;
 	private boolean waitingForVerifyCode = false;
 	
 	static public String retreiveAccountStatusSync(String mobile){
 		ApiParams param = new ApiParams();
 		param.addParam("mobile", mobile);
 		param.addParam("nickname", mobile);
 		String retStatus = "请求失败";
 		String result = BaseApiCommand.createCommand(checkStatusApi, true, param).executeSync(GlobalDataManager.getInstance().getApplicationContext());
 		if(result == null) return retStatus;
 		JSONObject obj;
 		try {
 			obj = new JSONObject(result);
 			if(obj != null){
 				JSONObject error = obj.getJSONObject("error");
 				if(error != null){
 					String code = error.getString("code");
 					if(code != null){
 						Integer intCode = Integer.valueOf(code);
 						if(intCode == 1){
 							retStatus = BaseAnonymousLogic.Status_UnRegistered;
 						}else if(intCode == 2){
 							retStatus = BaseAnonymousLogic.Status_Registered_Verified;
 						}else if(intCode == 3){
 							retStatus = BaseAnonymousLogic.Status_Registered_UnVerified;
 						}else{
 							retStatus = error.getString("message");
 						}
 					}
 				}
 			}
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}		
 		return retStatus;
 	}
 	
 	public void setCallback(AnonymousNetworkListener listener){
 		this.listener = listener;
 	}
 	
 	public void executeAction(String action, String mobile){
 		if(action.equals(BaseAnonymousLogic.Action_Login)){
 			ApiParams params = new ApiParams();
 			if(listener != null){
 				listener.beforeActionDone(BaseAnonymousLogic.Action_Login, params);
 			}		
 			String pwd = params.hasParam("password") ? params.getParam("password") : "";
 			doLogin(mobile, pwd);
 		}else if(action.equals(BaseAnonymousLogic.Action_Register)){
 			ApiParams params = new ApiParams();
 			if(listener != null){
 				listener.beforeActionDone(BaseAnonymousLogic.Action_Register, params);
 			}			
 			doRegister(mobile, params);
 		}else if(action.equals(BaseAnonymousLogic.Action_SendSMS)){
 			ApiParams params = new ApiParams();
 			if(listener != null){
 				listener.beforeActionDone(BaseAnonymousLogic.Action_SendSMS, params);
 			}				
 			requestVerifyCode(mobile, params);
 		}else if(action.equals(BaseAnonymousLogic.Action_Verify)){
 			ApiParams params = new ApiParams();
 			if(listener != null){
 				listener.beforeActionDone(BaseAnonymousLogic.Action_Verify, params);
 			}	
 			String code = params.getParam("verifyCode");
 			this.doVerify(mobile, code);
 		}
 	}
 	
 	private void doLogin(String mobile, String password){
 		ApiParams params = new ApiParams();
 		params.addParam("mobile", mobile);
 		params.addParam("nickname", mobile);
 		params.addParam("password", password);
 		BaseApiCommand.createCommand("user_login", true, params).execute(GlobalDataManager.getInstance().getApplicationContext(), this);		
 	}
 	
 	Handler handler = new Handler(){
 		@Override
 		public void handleMessage(Message msg) {
 			if(msg.what == MSG_VERIFY_TIMEOUT && waitingForVerifyCode){
 				if(listener != null){
 					AnonymousNetworkListener.ResponseData data = new AnonymousNetworkListener.ResponseData();
 					data.success = false;
 					data.message = "验证超时";
 					listener.onActionDone(BaseAnonymousLogic.Action_Verify, data);
 				}
 				if(smsReceiver != null){
 					GlobalDataManager.getInstance().getApplicationContext().unregisterReceiver(smsReceiver);
 					smsReceiver = null;
 				}
 				waitingForVerifyCode = false;
 			}
 		}
 	};
 	
 	private void doRegister(String mobile, ApiParams param){
 		ApiParams params = new ApiParams();
 		params.addParam("mobile", mobile);		
 		params.addAll(param.getParams());
 		BaseApiCommand.createCommand("user_register", true, params).execute(GlobalDataManager.getInstance().getApplicationContext(), this);		
 	}
 	
 	private void doVerify(String mobile, String code){
 		ApiParams params = new ApiParams();
 		params.addParam("mobile", mobile);
 		params.addParam("verifyCode", code);
 		BaseApiCommand.createCommand("verifyMobile", true, params).execute(GlobalDataManager.getInstance().getApplicationContext(), this);		
 	}
 	
 	private void requestVerifyCode(final String mobile, ApiParams param){
 		ApiParams params = new ApiParams();
 		params.addParam("mobile", mobile);
 		params.addAll(param.getParams());
 		BaseApiCommand.createCommand("sendsmscode", true, params).execute(GlobalDataManager.getInstance().getApplicationContext(), this);
 		if(smsReceiver == null){
 			smsReceiver = new BroadcastReceiver(){
 
 				@Override
 				public void onReceive(Context context, Intent intent) {
 					// TODO Auto-generated method stub
 					if(waitingForVerifyCode && intent.getAction() != null && intent.getAction().equals(CommonIntentAction.ACTION_BROADCAST_SMS)){
 						waitingForVerifyCode = false;
 						String smsMessage = intent.getStringExtra("msg");
 						String verifyCode = "";
 						if(smsMessage != null){
 							int index = smsMessage.indexOf("验证码为");
 							if(index > 0){
 								verifyCode = smsMessage.substring(index + 4, index + 10);
 								if(listener != null){
 									ResponseData response = new ResponseData();
 									response.success = true;
 									response.message = verifyCode;
 									listener.onActionDone(BaseAnonymousLogic.Action_SendSMS, response);
 									return;
 								}
 							}
 						}
 						ResponseData response = new ResponseData();
 						response.success = false;
 						response.message = "验证码获取失败";
 						listener.onActionDone(BaseAnonymousLogic.Action_SendSMS, response);
 //						doVerify(mobile, verifyCode);						
 					}
 				}
 				
 			};
 		}else{
 			GlobalDataManager.getInstance().getApplicationContext().unregisterReceiver(smsReceiver);
 		}
 		GlobalDataManager.getInstance().getApplicationContext().registerReceiver(smsReceiver, new IntentFilter(CommonIntentAction.ACTION_BROADCAST_SMS));
 		handler.sendEmptyMessageDelayed(MSG_VERIFY_TIMEOUT, 15000);
 	}
 	
 	private AnonymousNetworkListener.ResponseData parseCommonResponse(String response){
 		AnonymousNetworkListener.ResponseData ret = new AnonymousNetworkListener.ResponseData();
 		ret.success = false;
 		ret.message = "请求失败";
 		JSONObject obj;
 		try {
 			obj = new JSONObject(response);
 			
 			if(obj != null && obj.has("error")){
 				JSONObject errorjs = obj.getJSONObject("error");
 	
 				if(errorjs != null){
 					ret.success = errorjs.getString("code").equals("0") ? true : false;
 					ret.message = errorjs.getString("message");
 				}
 			}
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}	
 		return ret;
 	}	
 	
 	private static String getPairAction(String apiName){
 		String pair = "";
 		if(apiName.equals("verifyMobile")){			
 			pair = BaseAnonymousLogic.Action_Verify;
 		}else if(apiName.equals("user_login")){
 			pair = BaseAnonymousLogic.Action_Login;
 		}else if(apiName.equals("user_register")){
 			pair = BaseAnonymousLogic.Action_Register;
 		}
 		return pair;
 	}
 
 	@Override
 	public void onNetworkDone(String apiName, String responseData) {
 		// TODO Auto-generated method stub
 		AnonymousNetworkListener.ResponseData response = parseCommonResponse(responseData);
 		if(apiName.equals("sendsmscode")){
 			if(response.success){
 				waitingForVerifyCode = true;
 			}
 		}else{
 			String pair = getPairAction(apiName);
 			if(pair != null && pair.length() > 0){
 				if(listener != null){
 					listener.onActionDone(pair, response);
 				}		
 			}
 		}
 	}
 
 	@Override
 	public void onNetworkFail(String apiName, ApiError error) {
 		// TODO Auto-generated method stub
 		String pair = getPairAction(apiName);
 		ResponseData response = new ResponseData();
 		response.success = false;
 		response.message = error == null ? "未知错误" : error.getMsg();
 		if(listener != null){
 			listener.onActionDone(pair, response);
 		}
 	}
 	
 }
