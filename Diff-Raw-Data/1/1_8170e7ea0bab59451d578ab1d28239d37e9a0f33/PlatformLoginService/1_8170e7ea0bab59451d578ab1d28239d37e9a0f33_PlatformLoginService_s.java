 package com.xingcloud.users.services;
 
 import org.json.JSONObject;
 
 import com.xingcloud.core.Config;
 import com.xingcloud.core.XingCloud;
 import com.xingcloud.event.IEventListener;
 import com.xingcloud.event.XingCloudEvent;
 import com.xingcloud.items.spec.AsObject;
 import com.xingcloud.tasks.base.TaskEvent;
 import com.xingcloud.tasks.net.Remoting;
 import com.xingcloud.tasks.net.Remoting.RemotingMethod;
 import com.xingcloud.tasks.services.Service;
 import com.xingcloud.tasks.services.ServiceManager;
 import com.xingcloud.users.AbstractUserProfile;
 
 public class PlatformLoginService extends Service {
 	
 	protected AbstractUserProfile prof;
 	
 	protected static int loginAttempCount = 1;
 	private AsObject extra = null;
 
 	/**
 	 * 平台用户登陆服务。 首先会尝试登陆，如果失败则会自动进行注册
 	 * @param prof 用户UserProfile
 	 * @param onSuccess 成功回掉
 	 * @param onFail 失败回掉
 	 */
 	public PlatformLoginService(AbstractUserProfile prof,IEventListener onSuccess,IEventListener onFail) {
 		super(PLATFORM_LOGIN, onSuccess, onFail, RemotingMethod.POST);
 		
 		loginAttempCount = 1;
 		XingCloud.instance().getSessionId(true);
 		this.command = Config.PLATFORM_LOGIN_SERVICE;
 		this.prof = prof;
 	}
 	
 	/**
 	 * 平台用户登陆服务。 首先会尝试登陆，如果失败则会自动进行注册
 	 * @param prof 用户UserProfile
 	 * @param extraParams 额外参数
 	 * @param onSuccess 成功回掉
 	 * @param onFail 失败回掉
 	 */
 	public PlatformLoginService(AbstractUserProfile prof,AsObject extraParams,IEventListener onSuccess,IEventListener onFail) {
 		super(PLATFORM_LOGIN, onSuccess, onFail, RemotingMethod.POST);
 		
 		loginAttempCount = 1;
 		XingCloud.instance().getSessionId(true);
 		this.command = Config.PLATFORM_LOGIN_SERVICE;
 		this.prof = prof;
 		extra = extraParams;
 		if(extra!=null)
 			this.params.properties.putAll(extraParams.properties);
 	}
 	
 	private PlatformLoginService(AbstractUserProfile prof,AsObject extraParams,IEventListener onSuccess,IEventListener onFail,boolean isRegister) {
 		super(PLATFORM_LOGIN, onSuccess, onFail, RemotingMethod.POST);
 		
 		if(isRegister)
 			this.command = Config.PLARFOMR_REGISTER_SERVICE;
 		else
 			this.command = Config.PLATFORM_LOGIN_SERVICE;
 		XingCloud.instance().getSessionId(true);
 		this.prof = prof;
 		if(extra!=null)
 			this.params.properties.putAll(extraParams.properties);
 	}
 	
 	protected void handleSuccess(XingCloudEvent evt)
 	{
 		loginAttempCount++;
 		
 		Remoting rem = (Remoting)((TaskEvent)evt).getTarget();
 		if(rem.response.getHttpResponseStatusCode()==200)
 		{
 			Object data =rem.response.getData();
 			if(data==JSONObject.NULL)
 			{
 				if(loginAttempCount<=3)
 				{
 					ServiceManager.instance().send(new PlatformLoginService(prof,extra,this.onSuccess,this.onFail,false));
 				}
 				else
 				{
 					ServiceManager.instance().send(new PlatformLoginService(prof,extra,this.onSuccess,this.onFail,true));
 				}
 				
 				return;
 			}
 			else
 			{
 				XingCloud.getOwnerUser().updateUserData(((AsObject)data));
 			}
 		}
 		
 		super.handleSuccess(evt);
 	}
 	
 	protected void handleFail(XingCloudEvent evt)
 	{
 		prof.dispatchEvent(new XingCloudEvent(XingCloudEvent.LOGIN_ERROR,null));
 		
 		super.handleFail(evt);
 	}
 	
 }
