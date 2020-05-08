 package com.nexus.client.pushnotification;
 
 import java.util.HashMap;
 
 import com.google.gson.Gson;
 import com.nexus.NexusServer;
 
 public class PushNotificationButton {
 
 	public int ID = -1;
 	public String Text = "";
 	public EnumPushNotificationButtonEventType EventType = EnumPushNotificationButtonEventType.clientSided;
 	public String EventName = "";
 	public EnumPushNotificationButtonColor Color = EnumPushNotificationButtonColor.white;
 	
 	private IButtonEvent ButtonEvent;
 	private PushNotificationButtonManager ButtonManager = NexusServer.Instance.PushNotificationButtonManager;
 	
 	public void SetServerSideEvent(IButtonEvent event){
 		this.EventType = EnumPushNotificationButtonEventType.serverSided;
 		this.ButtonEvent = event;
 		this.ID = ButtonManager.GetButtonID();
 		ButtonManager.RegisterServerSidedButton(this);
 	}
 	
 	public void OnPress(){
		new Thread(ButtonEvent).start();
 	}
 	
 	public String toJson(){
 		HashMap<String, String> map = new HashMap<String, String>();
 		map.put("ID", Integer.toString(ID));
 		map.put("Text", Text);
 		map.put("EventType", EventType.toString());
 		map.put("EventName", EventName);
 		map.put("Color", Color.toString());
 		return new Gson().toJson(map);
 	}
 }
