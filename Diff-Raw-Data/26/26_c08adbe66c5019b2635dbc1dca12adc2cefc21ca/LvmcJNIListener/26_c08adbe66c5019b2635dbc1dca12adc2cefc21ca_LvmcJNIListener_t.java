 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.lorent.lvmc.event;
 
 import com.lorent.common.event.JNIEvent;
 import com.lorent.common.event.JNIEventAdapter;
 import com.lorent.lvmc.util.MessageUtil;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author Administrator
  */
 public class LvmcJNIListener extends JNIEventAdapter{
     
     private static  Logger log = Logger.getLogger(LvmcJNIListener.class);
     
     @Override
     public void registerOkCallBack(com.lorent.common.event.JNIEvent event) {
         log.debug("registerOkCallBack");
         MyEvent myEvent = new MyEvent();
         myEvent.setId("lccRegisterCallBackOK");
         MessageUtil.getInstance().sendMessage(myEvent);
     }
 
     @Override
     public void registerFailCallBack(com.lorent.common.event.JNIEvent event) {
         log.debug("registerFailCallBack");
         MyEvent myEvent = new MyEvent();
         myEvent.setId("lccRegisterCallBackFail");
         MessageUtil.getInstance().sendMessage(myEvent);
     }
 
     @Override
     public void connectedCallBack(com.lorent.common.event.JNIEvent event) {
         log.debug("connectedCallBack");
         MyEvent myEvent = new MyEvent();
         myEvent.setId("lccConnected");
         MessageUtil.getInstance().sendMessage(myEvent);
     }
 
     @Override
     public void callerrorCallBack(JNIEvent event) {
         log.debug("callerrorCallBack");
         MyEvent myEvent = new MyEvent();
         myEvent.setId("lccCallerrorCallBack");
         MessageUtil.getInstance().sendMessage(myEvent);
     }
 
     @Override
     public void hangupCallBack(JNIEvent event) {
         log.debug("hangupCallBack");
         MyEvent myEvent = new MyEvent();
         myEvent.setId("lccHandupCallBack");
        myEvent.setParas(new Object[]{event.getParas()[0]});
         MessageUtil.getInstance().sendMessage(myEvent);
     }
 
 	@Override
 	public void memberinfoCallBack(JNIEvent event) {
 		log.debug("memberinfoCallBack");
 		MyEvent myEvent = new MyEvent();
         myEvent.setId("memberinfoCallBack");
         myEvent.setParas(event.getParas());
         MessageUtil.getInstance().sendMessage(myEvent);
 	}
 
 }
