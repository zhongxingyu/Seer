 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.lorent.lvmc.controller;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 
 import org.apache.log4j.Logger;
 
 import com.lorent.common.util.ParaUtil;
 import com.lorent.lvmc.Launcher;
 import com.lorent.lvmc.dto.LoginInfo;
 import com.lorent.lvmc.event.PhoneEvent;
 import com.lorent.lvmc.ui.VideoViewsPanel;
 import com.lorent.lvmc.util.ConfigUtil;
 import com.lorent.lvmc.util.Constants;
 import com.lorent.lvmc.util.DataUtil;
 import com.lorent.lvmc.util.LvmcUtil;
 import com.lorent.lvmc.util.MessageUtil;
 import com.lorent.lvmc.util.StringUtil;
 import com.lorent.lvmc.util.VideoAudioSetupUtil;
 import com.lorent.util.LCCUtil;
 
 /**
  *
  * @author Administrator
  */
 public class PhoneController extends BaseController {
 
     private boolean isExitApplication = false;
     private Map<String, String[]> memberinfomap = new ConcurrentHashMap<String, String[]>();
     private Integer  LccState = PhoneEvent.PHONE_STATE_READY;
     public Integer getLccState() {
     	synchronized (LccState) {
     		return LccState;
     	}
 	}
     
     public Map<String, String[]> getMemberinfomap(){
     	return memberinfomap;
     }
     
     public String[] getMemberInfoByUserName(String username){
     	return memberinfomap.get(username);
     }
 
 	private int LccRegisterState = PhoneEvent.PHONE_STATE_REGISTER_FAIL;
     
     
     public PhoneController(){
 //        MessageUtil.getInstance().addEventListener(new PhoneEventListener(), PhoneEvent.class);
     }
     
     public void lccRegisterCallBackOK() throws Exception{
     	log.info("LCC 注册成功");
         LccRegisterState = PhoneEvent.PHONE_STATE_REGISTER_SUCCESS;
         synchronized (LccState) {
         	if (LccState != PhoneEvent.PHONE_STATE_CALLING) {
             	VideoViewsPanel viewsPanel = ViewManager.getComponent(VideoViewsPanel.class);
                 viewsPanel.getIconLabel().setIcon(new ImageIcon(getClass().getResource("/com/lorent/lvmc/resource/images/network-idle.png")));
                 viewsPanel.getStateLabel().setText("注册至视频服务器...成功");
 //                LccState = PhoneEvent.TYPE_REGISTER_SUCCESS;
 //                ControllerFacade.execute("phoneController", "tryConnnectToCS", true);
                 
                 LoginInfo loginInfo=DataUtil.getValue(DataUtil.Key.LoginInfo);
             	LCCUtil.getInstance().doAEC(ConfigUtil.getBoolProperty("enableAEC"));
         		LCCUtil.getInstance().setVideo(true, null);
                 int width = 0;
                 int height = 0;
                 String pixType = ConfigUtil.getProperty(
         				com.lorent.lvmc.util.Constants.VideoParam.PixelType.toString(),
         				StringUtil.getUIString("video.pixel.standardquality"));
 //				if (pixType.equals(StringUtil
 //						.getUIString("video.pixel.highquality"))) {
 //					width = ConfigUtil.getIntProperty("videowidth", 640);
 //					height = ConfigUtil.getIntProperty("videoheight", 360);
 //				} else {
 //					width = ConfigUtil.getIntProperty("svideowidth", 352);
 //					height = ConfigUtil.getIntProperty("svideoheight", 288);
 //				}
 				VideoAudioSetupUtil.GetVideoPixConfigAction action = (VideoAudioSetupUtil.GetVideoPixConfigAction)VideoAudioSetupUtil.getAction(pixType, VideoAudioSetupUtil.GetVideoPixConfigAction.class.getName());
 				int[] pix = action.execute();
 				width = pix[0];
 				height = pix[1];
                 LCCUtil.getInstance().doSetVideoSize(width, height);
                 LCCUtil.getInstance().doSetVideoBitrate(ConfigUtil.getIntProperty("VideoBitrate"));
                 LCCUtil.getInstance().doSetLccType(LCCUtil.LCC_TYPE_MUX);
                 Thread.sleep(500);
                 Boolean isAnswer = DataUtil.getValue(DataUtil.Key.IsAnswer);
                 if(isAnswer == null || !isAnswer){
                 	LCCUtil.getInstance().doCall(loginInfo.getConfno());
                 }else{
                 	LCCUtil.getInstance().doAnswer(loginInfo.getConfno());
                 }
     		}
 		}
     }
     
     public void lccRegisterCallBackFail() throws Exception{
     	log.info("LCC注册失败");
         LccRegisterState = PhoneEvent.PHONE_STATE_REGISTER_FAIL;
         synchronized (LccState) {
         	if (LccState != PhoneEvent.PHONE_STATE_CALLING) {
             	VideoViewsPanel viewsPanel = ViewManager.getComponent(VideoViewsPanel.class);
                 viewsPanel.getIconLabel().setIcon(new ImageIcon(getClass().getResource("/com/lorent/lvmc/resource/images/network-offline.png")));
                 viewsPanel.getStateLabel().setText("注册至视频服务器...失败");
 //                ControllerFacade.execute("phoneController", "tryConnnectToCS", true);
             }
             else if (LccState == PhoneEvent.PHONE_STATE_CALLING) {
             	log.info("挂电话 "+DataUtil.getLoginInfo().getConfno());
             	LCCUtil.getInstance().doHangup(DataUtil.getLoginInfo().getConfno());
                 VideoViewsPanel viewsPanel = ViewManager.getComponent(VideoViewsPanel.class);
                 viewsPanel.getIconLabel().setIcon(new ImageIcon(getClass().getResource("/com/lorent/lvmc/resource/images/network-error.png")));
                 viewsPanel.getStateLabel().setText("与视频服务器的连接已断开...");
                 ControllerFacade.execute("videoViewsController", "disconnectAllVideo");
                 log.info("LccState = "+PhoneEvent.PHONE_STATE_READY);
                 LccState = PhoneEvent.PHONE_STATE_READY;
                 clearMemberInfoMapSSRC();
     		}
         	ControllerFacade.execute("phoneController", "tryConnnectToCS", true);
 		}
     }
     
     public void lccConnected() throws Exception{
     	synchronized (LccState) {
     		log.info("保持连接(电话接通)");
             VideoViewsPanel viewsPanel = ViewManager.getComponent(VideoViewsPanel.class);
             viewsPanel.getIconLabel().setIcon(new ImageIcon(getClass().getResource("/com/lorent/lvmc/resource/images/network-idle-ready.png")));
             viewsPanel.getStateLabel().setText("成功连接至视频服务器");
             LccState = PhoneEvent.PHONE_STATE_CALLING;
             log.info("LccState = "+PhoneEvent.PHONE_STATE_CALLING);
             viewsPanel.getVideosPanel().revalidate();
             viewsPanel.getVideosPanel().repaint();
             reconnectCount = 1;
 		}
     }
     
     public void lccCallerrorCallBack() throws Exception{
     	synchronized (LccState) {
     		log.info("LCC 呼叫错误(对方拒绝)");
         	LccState = PhoneEvent.PHONE_STATE_READY;
         	log.info("LccState = "+PhoneEvent.PHONE_STATE_READY);
             clearMemberInfoMapSSRC();
 //            tryConnnectToCS(false);
             ControllerFacade.execute("phoneController", "tryConnnectToCS", true);
 		}
     }
     
    public void lccHandupCallBack() throws Exception{
     	synchronized (LccState) {
     		log.info("LCC 电话被挂起");
             if (isExitApplication == false) {
                 VideoViewsPanel viewsPanel = ViewManager.getComponent(VideoViewsPanel.class);
                 viewsPanel.getIconLabel().setIcon(new ImageIcon(getClass().getResource("/com/lorent/lvmc/resource/images/network-error.png")));
                 viewsPanel.getStateLabel().setText("与视频服务器的连接已断开...");
                 ControllerFacade.execute("videoViewsController", "disconnectAllVideo");
                 log.info("LccState = "+PhoneEvent.PHONE_STATE_READY);
                 LccState = PhoneEvent.PHONE_STATE_READY;
                 clearMemberInfoMapSSRC();
 //                    tryConnnectToCS(false);
             }
             else{
                 isExitApplication = false;
             }
     		if(LvmcUtil.isUCSAPP() && Launcher.isStartedFromOutSide){
     			DataUtil.setValue(DataUtil.Key.AleardyHangup, true);
     			Launcher.stopLvmcFromOutSide();
     		}
 		}
     }
     
     private void clearMemberInfoMapSSRC(){
     	log.info("clearMemberInfoMapSSRC");
     	for (Iterator<Entry<String,String[]>> iterator = memberinfomap.entrySet().iterator(); iterator.hasNext();) {
 			Entry<String, String[]> next = iterator.next();
 			String[] value = next.getValue();
 			value[4] = "-1";
 //			next.setValue(value);
 		}
     	for (Iterator<Entry<String,String[]>> iterator = memberinfomap.entrySet().iterator(); iterator.hasNext();) {
 			Entry<String, String[]> next = iterator.next();
 			String[] value = next.getValue();
 //			value[4] = "-1";
 //			next.setValue(value);
 			log.info("clearMemberInfoMapSSRC  "+value[0]+" "+value[1]+" "+value[2]+" "+value[3]+" "+value[4]);
 		}
     	
     }
     
     private ArrayList<String> getStateChageMember(Object[] memberInfos) throws Exception{
     	
     	ArrayList<String> ressult = new ArrayList<String>();
     	for(Object memberInfo : memberInfos){
             String[] temp = (String[])memberInfo;   
             String[] strings = memberinfomap.get(temp[0]);
             if (strings!=null && !strings[4].equals(temp[4])) {//ssrc
             	log.info("getStateChageMember:" + "member_id=" + temp[0] + "&sip=" + temp[1] + "&pos=" + temp[2] + "&state=" + temp[3] + "&ssrc=" + temp[4]);
             	ressult.add(temp[0]);
 			}
         }
     	return ressult;
     }
     
     public void memberinfoCallBack(Object[] memberInfos) throws Exception{
     	log.info("mcu会议人员变动callback");
     	if (memberInfos != null) {
     		//判断mcu人员是否有变动
     		ArrayList<String> stateChageMember = getStateChageMember(memberInfos);
     		Object[] array = stateChageMember.toArray();
     		String[] members = new String[array.length];
     		for (int i = 0; i < array.length; i++) {
 				members[i] = (String) array[i];
 			}
 
     		ControllerFacade.execute("videoViewsController", "reconnectVideos", (Object)members);
     		for(Object memberInfo : memberInfos){
                 String[] temp = (String[])memberInfo;   
                 log.info("memberinfoCallBack:" + "username=" + temp[0] + "&sip=" + temp[1] + "&pos=" + temp[2] + "&state=" + temp[3] + "&ssrc=" + temp[4] + "&lcctype=" + temp[5]);
                 if(!memberinfomap.containsKey(temp[0])){
                 	ParaUtil paras = ParaUtil.newInstance().setString("member", temp[0]).setInt("status", Constants.MEMBER_STATUS_JOIN).setBoolean("isOpenfireUser", false);
                     MessageUtil.getInstance().sendMessage("roomMemberChange", new Object[]{paras});
                 }
                 memberinfomap.put(temp[0], temp);
             }
     		//查找退出MCU的成员                 开始
     		Set<String> keys = memberinfomap.keySet();
     		ArrayList<String> leaveMembers = new ArrayList<String>();
     		boolean isLeave = true;
     		for(String key:keys){
     			isLeave = true;
     			for(Object memberInfo : memberInfos){
     				String[] temp = (String[])memberInfo; 
     				if(key.equals(temp[0])){
     					isLeave = false;
     					break;
     				}
     			}
     			if(isLeave){
     				leaveMembers.add(key);
     			}
     		}
     		//查找退出MCU的成员                  结束
     		if(leaveMembers!=null && leaveMembers.size()>0){
     			for(String key:leaveMembers){
     				memberinfomap.remove(key);
     				ParaUtil paras = ParaUtil.newInstance().setString("member", key).setInt("status", Constants.MEMBER_STATUS_LEAVE).setBoolean("isOpenfireUser", false);
                     MessageUtil.getInstance().sendMessage("roomMemberChange", new Object[]{paras});
     			}
     		}
 		}
     }
     
     private int reconnectCount = 1;
     private LoopThread loopThread = new LoopThread();
     class LoopThread extends Thread{
     	
     	public LoopThread(){
     		this.setName("PhoneController.LoopThread");
     	}
     	
     	private boolean runFlag = true;
     	
 		public boolean isRunFlag() {
 			return runFlag;
 		}
 
 		public void setRunFlag(boolean runFlag) {
 			this.runFlag = runFlag;
 		}
 
 		private boolean isFirstTime = false;
 
 		/**
 		 * @return the isFirstTime
 		 */
 		public boolean isFirstTime() {
 			return isFirstTime;
 		}
 
 		/**
 		 * @param isFirstTime the isFirstTime to set
 		 */
 		public void setFirstTime(boolean isFirstTime) {
 			this.isFirstTime = isFirstTime;
 		}
     	
 		
 		@Override
 		public void run() {
 			try {
 				while (runFlag) {
 					synchronized (LccState) {
 						if (LccState != PhoneEvent.PHONE_STATE_CALLING || LccRegisterState != PhoneEvent.PHONE_STATE_REGISTER_SUCCESS) {
 							
 							if (LccRegisterState != PhoneEvent.PHONE_STATE_REGISTER_SUCCESS) {
 								if (DataUtil.getLoginInfo() != null && !LCCUtil.getInstance().isRegister()) {
 									log.info("尝试注册... LccRegisterState:"+LccRegisterState+",LccState:"+LccState);
 									String mcuLocalPort = ConfigUtil.getProperty("csPort");
 									String mcuIP = DataUtil.getLoginInfo().getServerIP();
 									LCCUtil.getInstance().reRegister();
 //										instance.register("sip:" + username + "@" + mcuIP + ":" + mcuLocalPort, password, "sip:" + mcuIP + ":" + mcuLocalPort, 0);
 								}
 							}
 							else if(LccState != PhoneEvent.PHONE_STATE_CALLING && LccRegisterState == PhoneEvent.PHONE_STATE_REGISTER_SUCCESS){
 								if (DataUtil.getLoginInfo() != null) {
 									log.info("呼叫: "+DataUtil.getLoginInfo().getConfno()+" LccState:"+LccState);
 									LCCUtil.getInstance().doCall(DataUtil.getLoginInfo().getConfno());
 //										LccState = PhoneEvent.PHONE_STATE_CALLOUT;
 								}
 							}
 							if (runFlag) {
 								VideoViewsPanel viewsPanel = ViewManager.getComponent(VideoViewsPanel.class);
 								if (isFirstTime) {
 									viewsPanel.getStateLabel().setText("尝试连接至视频服务器 "+reconnectCount+" 次");
 									viewsPanel.getReconnectButton().setVisible(false);
 								}
 								else{
 									viewsPanel.getStateLabel().setText("与视频服务器的连接已断开,重新连接 "+reconnectCount+" 次");
 									viewsPanel.getReconnectButton().setVisible(false);
 								}
 							}
 							
 							reconnectCount++;
 							log.info(this.getId()+" LoopThread reconnectCount:"+reconnectCount+",LccState:"+LccState);
 						}
 					}
 					log.info("While LoopThread "+this.getId());
 					Thread.sleep(15000);
 				}
 			} catch (Exception e) {
 				log.error("LoopThread.run", e);
 			}
 		}
     }
     
     /*
      * isFirstTime:是否首次执行
      * */
     public void tryConnnectToCS(final boolean isFirstTime) throws Exception{
     	if (!loopThread.isAlive()) {
     		loopThread.isFirstTime = isFirstTime;
         	log.info("tryConnnectToCS loopThread.start()");
     		reconnectCount = 1;
     		loopThread.start();
 		}else{
 			log.info("tryConnnectToCS loopThread is started");
 		}
 
     }
     
     public void stopLoopThread() throws Exception{
     	loopThread.setRunFlag(false);
     }
     
     public void exitApplication(String confno) throws Exception{
         isExitApplication = true;
         LCCUtil.getInstance().doHangup(confno);
         Thread.sleep(500);
         LCCUtil.getInstance().doUninit();
     }
 //    public void lccGetMemberList() throws Exception{
 //        LCCUtil.getInstance().doGetMemberList("10000");
 //    }
     
     /*
     private class PhoneEventListener implements BaseListener{
 
         @Override
         public void action(final int type, final Object[] paras) {
             SwingUtilities.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     log.debug("PhoneEventListener,type = "+type+" , paras:"+paras);
                     if (type == PhoneEvent.TYPE_REGISTER_FAIL) {
                         log.info("电话(lcc)注册失败");
                         
                     }else if(type == PhoneEvent.TYPE_REGISTER_SUCCESS){
                         log.info("电话(lcc)注册成功");
                         
                     }else if(type == PhoneEvent.TYPE_HANGUP){
                         log.info("电话被挂起  " + phoneCallNumber+" phoneState:"+phoneState);
                         if (phoneState == PhoneEvent.PHONE_STATE_CALLING || phoneState == PhoneEvent.PHONE_STATE_CALLOUT || phoneState == PhoneEvent.PHONE_STATE_CALLIN) {
                           
                             phoneState = PhoneEvent.PHONE_STATE_READY;
                         }
                     }else if(type == PhoneEvent.TYPE_CALLINCOME){
                         String comeInNumber =  (String) paras[0];
                         log.info("电话呼入 "+comeInNumber);
                         phoneCallNumber = comeInNumber;
                       
                         phoneState = PhoneEvent.PHONE_STATE_CALLIN;
                     }else if(type == PhoneEvent.TYPE_CALLERROR){
                         log.info("错误(对方挂起)");
                         
                         phoneState = PhoneEvent.PHONE_STATE_READY;
                     }else if(type == PhoneEvent.TYPE_CONNECTED){
                         log.info("保持连接(电话接通)");
                         if (phoneState == PhoneEvent.PHONE_STATE_CALLOUT || phoneState == PhoneEvent.PHONE_STATE_CALLIN) {
                             
                             phoneState = PhoneEvent.PHONE_STATE_CALLING;
                         }
                     }
                 }
             });
         }
 
     } 
     */
     private int phoneState;
 //    private String call_out_Number = null;
 //    private String call_in_Number = null;
     private String phoneCallNumber= null;
     private int callInPhoneRecordID = -1;
     private Logger log = Logger.getLogger(PhoneController.class);
     
 
     public void call(Map<String, Object> paramters)  throws Exception{
         log.info("呼出... "+paramters.get("callNumber"));
         String callNumber = (String) paramters.get("callNumber");
         if (callNumber != null && !callNumber.equals("")) {
             //呼出
             LCCUtil.getInstance().doCall(callNumber);
             phoneState = PhoneEvent.PHONE_STATE_CALLOUT;
             
             phoneCallNumber = callNumber;
             
         } else {
             JOptionPane.showMessageDialog(null, StringUtil.getUIString("PhoneController.NeedPhoneNumber"));
         }
     }
     
     public void handup(Map<String, Object> paramters) throws Exception {
         log.info("挂起..." + phoneCallNumber +",  phoneState:"+phoneState);
 //        if (call_in_Number != null) {
 //            LCCUtil.getInstance().doHangup(call_in_Number);
 //            call_in_Number = null;
 //        }
 //        if (call_out_Number != null) {
 //            LCCUtil.getInstance().doHangup(call_out_Number);
 //            call_out_Number = null;
 //        }
         if(phoneCallNumber != null){
             LCCUtil.getInstance().doHangup(phoneCallNumber);
             
         }
     }
     
     public void answer(Map<String, Object> paramters)  throws Exception{
         log.info("接听..."+phoneCallNumber);
         if (phoneCallNumber != null) {
             LCCUtil.getInstance().doAnswer(phoneCallNumber);
 //            call_out_Number = null;
         }
     }
     
     public void openCamera(Map<String, Object> paramters)  throws Exception{
         log.info("打开视频...");
 //        PhoneFrame frame = (PhoneFrame) ViewManager.getComponent(PhoneFrame.class);
 //        LCCUtil.getInstance().setVideo(true, frame.getScreenPanel());
 //        LCCUtil.getInstance().setPreview(true);
     }
     
     public void closeCamera(Map<String, Object> paramters)  throws Exception{
         log.info("关闭视频...");
 //        PhoneFrame frame = (PhoneFrame) ViewManager.getComponent(PhoneFrame.class);
 //        LCCUtil.getInstance().setVideo(false, frame.getScreenPanel());
 //        LCCUtil.getInstance().setPreview(false);
 //        frame.getScreenPanel().DrawImage();
         Thread.sleep(300);
 //        frame.getScreenPanel().repaint();
     }
     
     
     public void unInit(Map<String, Object> paramters) throws Exception{
         log.info("反初始化电话");
         if (phoneState == PhoneEvent.PHONE_STATE_CALLIN || phoneState == PhoneEvent.PHONE_STATE_CALLING || phoneState == PhoneEvent.PHONE_STATE_CALLOUT) {
             handup(null);
             Thread.sleep(500);
         }
         LCCUtil.getInstance().doUninit();
         Thread.sleep(2000);
     }
 }
