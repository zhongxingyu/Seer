 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.lorent.util;
 
 
 import java.awt.Component;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.event.EventListenerList;
 
 import org.apache.log4j.Logger;
 
 import com.lorent.common.event.JNIEvent;
 import com.lorent.common.event.JNIEventListener;
 import com.lorent.common.util.StringUtil;
 import com.sun.jna.Native;
 /**
  *
  * @author jack
  */
 public class LCCUtil {
 
     private static Logger log = Logger.getLogger(LCCUtil.class);
     private static LCCUtil instance;
     private ArrayBlockingQueue<JNIEvent> eventlist = new ArrayBlockingQueue<JNIEvent>(1000);
     private boolean flag = true;
     private EventListenerList listenerList = new EventListenerList();
     public static final int CALL_TYPE_NONE = 0;
     public static final int CALL_TYPE_SOUND = 1;
     public static final int CALL_TYPE_VIDEO = 2;
     public static final int CALL_TYPE_SOUND_AND_VIDEO = 3;
     public static final int LCC_TYPE_MIX = 0;
     public static final int LCC_TYPE_MUX = 1;
     public static int TIME_OUT = 60000;
     public static int OK = 0;
     public static int FAIL = 1;
     private String regLccNo;
     private String regServerIP;
     private String regPassword;
     private String regServerPort;
     
     public String getRegLccNo() {
 		return regLccNo;
 	}
 
 	public String getRegServerIP() {
 		return regServerIP;
 	}
 
 	public String getRegPassword() {
 		return regPassword;
 	}
 
 	public String getRegServerPort() {
 		return regServerPort;
 	}
 
 	private LCCUtil() {
         new DispatchEventThread().start();
         log.info("lccinit");
         lccinit();
         log.info("javacallbackinit : " + this.toString());
         javacallbackinit(this);
         log.info("setnortcptimeout : " + TIME_OUT);
         setnortcptimeout(TIME_OUT);
     }
 
     public static LCCUtil getInstance() {
         if (instance == null) {
             instance = new LCCUtil();
         }
         return instance;
     }
 
 
     public void addEventListener(JNIEventListener listener) {
         listenerList.add(JNIEventListener.class, listener);
     }
 
     public void removeEventListener(JNIEventListener listener) {
         listenerList.remove(JNIEventListener.class, listener);
     }
     
     public void removeAllListener(){
     	listenerList = new EventListenerList(); 
     }
 
     private synchronized void sendJNIMessage(JNIEvent event) {
         eventlist.offer(event);
     }
 
     public void stop() {
         flag = false;
     }
 
     private class DispatchEventThread extends Thread {
 
         @Override
         public void run() {
             while (flag) {
                 try {
                     JNIEvent event = eventlist.poll(1000, TimeUnit.MILLISECONDS);
                     if (event != null) {
                         for (Object o : listenerList.getListenerList()) {
                             if (o instanceof JNIEventListener) {
                                 JNIEventListener temp = (JNIEventListener) o;
                                 if (JNIEvent.REGISTER_OK == event.getType()) {
                                     temp.registerOkCallBack(event);
                                 }else if(JNIEvent.REGISTER_FAIL == event.getType()){
                                     temp.registerFailCallBack(event);
                                 }else if(JNIEvent.CONNECTED == event.getType()){
                                     temp.connectedCallBack(event);
                                 }else if(JNIEvent.CALLERROR_CB == event.getType()){
                                     temp.callerrorCallBack(event);
                                 }else if(JNIEvent.INCOMING_CB == event.getType()){
                                     temp.incomingCallBack(event);
                                 }else if(JNIEvent.HANDUP_CB == event.getType()){
                                     temp.hangupCallBack(event);
                                 }else if(JNIEvent.MEMBERINFO_CB == event.getType()){
                                     temp.memberinfoCallBack(event);
                                 }else if(JNIEvent.UNREGISTER_OK == event.getType()){
 	                            	temp.unRegOKCallBack(event);
 	                            }else if(JNIEvent.UNINIT_OK == event.getType()){
 		                        	temp.unInitOKCallBack(event);
 		                        }else if(JNIEvent.HANDUP_SUCCESS_CB == event.getType()){
 		                        	temp.hangupSuccessCallBack(event);
 		                        }else if(JNIEvent.DATA_MSG_CB == event.getType()){
 		                        	temp.dataMessageCallBack(event);
 		                        }else if(JNIEvent.OUTGOING_CB == event.getType()){
 		                        	temp.outgoingCallBack(event);
 		                        }
                             }
                         }
                     }
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     private Map<String, StoreData> calls = new HashMap<String, StoreData>();
     private static boolean isRegister = false;
     private static boolean reRegister = false;
     private RegisterData tempRegData;
     private static boolean isCalling = false;
     private boolean oneCall = true;
 
     public static boolean canCall(){
     	return !isCalling && isRegister;
     }
     
     public static boolean isRegister() {
 		return isRegister;
 	}
     
     public void setOneCall(boolean flag){
     	this.oneCall = flag;
     }
     
     //===========================================================================
 
 	protected void javaregisterokcb(String result) {
         log.info("register callback : " + result);
         
         if ("ok".equals(result)) {
             if (!isRegister) {
                 JNIEvent event = new JNIEvent(getInstance(),JNIEvent.REGISTER_OK,new Object[]{result});
                 sendJNIMessage(event);
             }
             isRegister = true;
         } else if ("fail".equals(result)) {
             JNIEvent event = new JNIEvent(getInstance(),JNIEvent.REGISTER_FAIL,new Object[]{result});
             sendJNIMessage(event);
             isRegister = false;
         } else if ("unok".equals(result)) {
             isRegister = false;
             JNIEvent event = new JNIEvent(getInstance(),JNIEvent.UNREGISTER_OK,new Object[]{result});
             sendJNIMessage(event);
             if (reRegister) {
                 reRegister = false;
                 this.register(tempRegData.username, tempRegData.password, tempRegData.serverIP, tempRegData.port);
             }
         }
     }
 
 	//type 0=none 1=sound 2=video 3=sound&video
     protected void javaincomingcb(String lccno, int callIndex, int type) {
         log.info("incoming callback : lccno = " + lccno + " & callIndex = " + callIndex + " & type = " + type);
         if(isCalling && oneCall){
         	log.info("is calling, auto hangup the call");
         	this.hangup(callIndex);
         }else{
         	StoreData temp = calls.get(lccno);
         	if(temp==null){
 		        isCalling = true;
 		        StoreData data = new StoreData();
 		        data.callIndex = callIndex;
 		        data.type = type;
 		        calls.put(lccno, data);
 		        JNIEvent event = new JNIEvent(getInstance(),JNIEvent.INCOMING_CB,new Object[]{lccno, type});
 		        sendJNIMessage(event);
         	}else{//正在通话，自动挂断
             	log.info("is calling, auto hangup the call");
             	this.hangup(callIndex);
         	}
         }
     }
 
     protected void javaconnectedcb(int callIndex) {
     	String lccno = getLccnoFromCallIndex(callIndex);
         log.info("connected callback : lccno = " + lccno + " & callIndex = " + callIndex);
         calls.get(lccno).state = CallState.Connected;
         JNIEvent event = new JNIEvent(getInstance(),JNIEvent.CONNECTED,new Object[]{lccno, calls.get(lccno).type});
         sendJNIMessage(event);
     }
 
     protected void javahangupcb(int callIndex) {
     	String lccno = getLccnoFromCallIndex(callIndex);
         if(lccno != null){
         	log.info("hangup callback : lccno = " + lccno + " & callIndex = " + callIndex);
         	StoreData storeData = calls.get(lccno);
         	if(storeData.state == CallState.MeHangup){//me hangup callback
         		JNIEvent event = new JNIEvent(getInstance(),JNIEvent.HANDUP_SUCCESS_CB,new Object[]{lccno});
     	        sendJNIMessage(event);
         	}else{
         		JNIEvent event = new JNIEvent(getInstance(),JNIEvent.HANDUP_CB,new Object[]{lccno, storeData.type, storeData.state == CallState.Connected});
     	        sendJNIMessage(event);
         		isCalling = false;
         	}
 	    	calls.remove(lccno);
         }else{
             log.info("hangup callback : lccno not exist");
         }
     }
     
     private String getLccnoFromCallIndex(int callIndex){
     	Set<Entry<String,StoreData>> entrySet = calls.entrySet();
     	for(Entry<String,StoreData> entry : entrySet){
     		if(entry.getValue().callIndex == callIndex){
     			return entry.getKey();
     		}
     	}
     	return null;
     }
 
     protected void javacallerrorcb(int callIndex) {
     	synchronized (this) {
         	String lccno = getLccnoFromCallIndex(callIndex);
             log.info("call error callback : lccno = " + lccno + " & callIndex = " + callIndex);
             JNIEvent event = new JNIEvent(getInstance(),JNIEvent.CALLERROR_CB,new Object[]{lccno, calls.get(lccno).type});
             sendJNIMessage(event);
         	if(lccno != null){
         		calls.remove(lccno);
         	}
         	isCalling = false;
 		}
     }
 
     protected void javaunintcompletecb(String nMsg) {
         log.info("unint complete callback : " + nMsg);
         JNIEvent event = new JNIEvent(getInstance(),JNIEvent.UNINIT_OK,new Object[]{});
         sendJNIMessage(event);
     }
     
     //MCU用户挂断或打入会议，会收到MCU服务端发送回来的所有人员信息
     //eg: str[0]=33012,str[1]="<sip:33012@10.168.250.12:5060>",str[2]=position,str[3]=status,str[4]=ssrc(视频信息)
     //str[5]=lcc_type(1为转发型,-1,0为非转发型),str[6]=video(1-开启0-关闭),str[7]=audio(1-开启0-关闭)
     protected void recieveMemberInfos(Object[] memberInfos) {
         log.info("recieveMemberInfos");
         JNIEvent event = new JNIEvent(getInstance(),JNIEvent.MEMBERINFO_CB,new Object[]{memberInfos});
         sendJNIMessage(event);
     }
     
     protected void javaupdatemessage(String msg, int callindex, int type, int status) {
     	String username = getLccnoFromCallIndex(callindex);
         log.info("javaupdatemessage msg = " + msg + " & callindex = " + callindex + " & username = " + username + " & type = " + type + " & status = " + status);
         JNIEvent event = new JNIEvent(getInstance(),JNIEvent.DATA_MSG_CB,new Object[]{msg, username, type, status});
         sendJNIMessage(event);
     }
     
     protected void javaoutgoing(String username){
     	log.info("javaoutgoing username = " + username);
         JNIEvent event = new JNIEvent(getInstance(),JNIEvent.OUTGOING_CB,new Object[]{username});
         sendJNIMessage(event);
     }
     
     //===========================================================================
 
     private native int lccinit();
 
     private native int uninit();
     
     private native int postprocess();
 
     private native int setsipport(int port);
 
     private native int reg(String username, String password, String proxy);
 
     private native int unreg();
 
     private native int call(String sip_url);
 
     private native int answer(int call_index);
 
     private native int hangup(int call_index);
 
     private native int setvideosize(int width, int height);
 
     private native int setvideobitrate(int bitrate);
 
     private native int enablevideopreview(int enable);
 
     private native int enablevideo(int enable, long hwnd);
 
     private native int incomingtest();
 
     private native int javacallbackinit(Object obj);
 
     private native int getanswercallindex();
     
     private native int getsoundcardnum();
    
     private native int setlcctype(int type);//普通会议0 转发型会议1
     
     private native int addmuxvideostream(int call_index,String confid,String srcid,long hwnd);
     
     private native int delmuxvideostream(int call_index,String confid,String srcid);
     
     private native Object[] getmemberlist(String confid);
     
     private native int changemuxvideostream(int call_index,String confid,String srcid,long hwnd);
     
     
     //自适应
     private native int setaec(int enable);
     
     /*
     函数名:Java_com_lorent_util_LCCUtil_setvideohwnd
     功能：切换视频窗口，适应于lcc互打以及拨入混合型会议，呼叫前或接听前设置，通话中设置用于切换窗口
     参数：isremote，为1时设置对端视频窗口，为0时设置本地窗口
   */
     private native int setvideohwnd(int callindex, int isremote, long hwnd);
 
   /*
     函数名:Java_com_lorent_util_LCCUtil_mutewebcam
     功能：通话中禁止本地视频上传
     参数：yesno，为1时禁止视频，为0时开启视频
   */
     private native int mutewebcam(int call_index, int yesno);
 
   /*
     函数名：Java_com_lorent_util_LCCUtil_setplaybackvolume
     功能：调节输出声音音量，使用软转换，默认为原始数据音量
     参数：call_index，电话序号
   	level，范围0到100,0为没有声音，100为最大声音，默认是100
   */
     private native int setplaybackvolume(int call_index, int level);
 
 
   /*
     函数名：Java_com_lorent_util_LCCUtil_setcapturevolume
     功能：调节麦克风音量，使用系统API，默认为系统麦克风声音音量
     参数：level，范围0到100,0为没有声音，100为最大声音
   */
     private native int setcapturevolume(int level);
 
 
   /*
     函数名：Java_com_lorent_util_LCCUtil_getplaybackvolume
     功能：获得对应会话的输出音量
     参数：call_index,电话序号
     返回：音量(0到100）
   */
     private native int getplaybackvolume(int call_index);
 
 
   /*
     函数名：Java_com_lorent_util_LCCUtil_getcaptrurevolume
     功能：获得系统麦克风音量
     返回值：音量(0到100)
   */
     private native int getcaptrurevolume();
 
 
   /*
     函数名：Java_com_lorent_util_LCCUtil_getbandwidth
     功能：获取带宽
     返回：一个float数组，分别包括音频上载、音频下载，视频上载，视频下载，单位为kbps
   */
     private native float[] getbandwidth();
 
   /*
     函数名：Java_com_lorent_util_LCCUtil_getplaybacklist
     功能：获取声音播放设备列表
     返回：列表数组，{"设备号1","设备1名称","设备号2","设备2名称"},如{"1","RealTek output device","2","ac97 output device"}
   */
     private native String[] getplaybacklist();
 
   /* 
     函数名：Java_com_lorent_util_LCCUtil_getcapturelist
     功能：获取麦克风设备列表
   */
     private native String[] getcapturelist();
 
   /*
     函数名：Java_com_lorent_util_LCCUtil_getwebcamlist
     功能：返回摄像头列表
   */
     private native String[] getwebcamlist();
 
   /*
   下面函数分别为选择音频、麦克风、摄像头设备，dev_index为设备号,注意必须在通话前设定
   */
     private native int setplayback(int dev_index);
     private native int setcapture(int dev_index);
     private native int setwebcam(int dev_index);
     
     private native int setnortcptimeout(int timeout);
     
     private native int startrecordaudio(int callindex, String filepath);
     
     private native int stoprecordaudio(int callindex);
     
     private native int startplayaudio(String filePath);
     
     private native int stopplayaudio();
     
     private native int senddtmf(int callindex, int type, String dtmf);
     
     private native int holdcall(int callindex, int enable);
     
     private native int transfercall(int call_index, String sipUrl);
     
     private native int setplaybackmute(int callindex, int enable);
     
     private native int setcaptruremute(int enable);
     
     private native int setvideoproperty(int fps, int bitrate, int width, int height);
     
     //为配合PBX不能转发SIP MESSAGE的关系，目前由lcccore直接把sipmessage发到mcu的sip proxy上，所以需要让lcccore知道mcu的代理ip以及端口
     private native int ucssetmcuproxy(String mcuip, int mcuport);
     
     //设置音频编码器，codecs为jstring数组，表示lcccore支持的音频编码器用于SIP的协商，越靠前的优先级越高，数组里面可以为PCMU, G792, G711, SILK
     private native int setaudiocodes(Object[] codecs);
     
     //由于修改音频编码器后不会马上生效，一般做法是使用sip协议的reinvite功能来重新协商编码器，下面结果是调用reinvite
     private native int callreinvite(int call_index);
     
     //===========================================================================
     
     static {
         log.info("java.library.path : " + System.getProperty("java.library.path"));
         System.loadLibrary("lcccore");
     }
 
     @Deprecated
     private void register(String username, String password, String serverIP, int port) {
     	tempRegData = new RegisterData(username, password, serverIP, port);
         if (isRegister) {
         	log.info("unregister");            
         	reRegister = true;
             this.unreg();
         } else {
         	log.info("setsipport:" + port);
             this.setsipport(port);
             log.info("reg username:" + username + " password:" + password + " serverIP:" + serverIP);
             this.reg(username, password, serverIP);
         }
 
     }
     
     public void registerForP2P(String lccNO,String serverIP,String serverPort,String password,int localPort){
     	this.regLccNo = lccNO;
     	this.regServerIP = serverIP;
     	this.regServerPort = serverPort;
     	this.regPassword = password;
     	tempRegData = new RegisterData(lccNO, password, serverIP, localPort);
         if (isRegister) {
         	log.info("unregister");            
         	reRegister = true;
             this.unreg();
         } else {
         	log.info("setsipport:" + localPort);
             this.setsipport(localPort);
             log.info("reg sip_username:" + lccNO + " password:" + password + " sip_serverIP:" + serverIP);
             this.reg(lccNO, password, serverIP);
         }
     }
     
     public void register(String lccNO,String serverIP,String serverPort,String password,int localPort){
     	this.regLccNo = lccNO;
     	this.regServerIP = serverIP;
     	this.regServerPort = serverPort;
     	this.regPassword = password;
     	String sip_username = "sip:" + lccNO + "@" + serverIP + ":" + serverPort;
 		String sip_serverIP = "sip:" + serverIP + ":" + serverPort;
 		tempRegData = new RegisterData(sip_username, password, sip_serverIP, localPort);
         if (isRegister) {
         	log.info("unregister");            
         	reRegister = true;
             this.unreg();
         } else {
         	log.info("setsipport:" + localPort);
             this.setsipport(localPort);
             log.info("reg sip_username:" + sip_username + " password:" + password + " sip_serverIP:" + sip_serverIP);
             this.reg(sip_username, password, sip_serverIP);
         }
     }
     
     public void reRegister(){
     	register(tempRegData.username, tempRegData.password, tempRegData.serverIP, 0);
     }
     
     public int doUnReg(){
     	log.info("unreg");
     	return this.unreg();
     }
 
     public void doUninit(){
         log.info("doUninit");
         this.uninit();
     }
     
     public int doPostProcess(){
     	log.info("doPostProcess");
     	return this.postprocess();
     }
 
     public void doCall(String username) {
     	doCall(username, CALL_TYPE_SOUND_AND_VIDEO);
     }
     
     public void doCall(String username, int type){
     	synchronized (this) {
             int callIndex = this.call(username);
             log.info("doCall username = " + username + " & type = " + type + " & callindex = " + callIndex);
             StoreData data = new StoreData();
             data.callIndex = callIndex;
             data.type = type;
             calls.put(username, data);
             isCalling = true;
 		}
     }
 
     public int doHangup(String username) {
     	log.info("doHangup username = " + username);
         if(calls.get(username)!=null){
         	StoreData storeData = calls.get(username);
         	if(storeData.state == CallState.OtherHangup || storeData.state == CallState.MeHangup){
         		log.info("is already hangup");
         		return FAIL;
         	}
             int callIndex = calls.get(username).callIndex;
             int flag = this.hangup(callIndex);
             storeData.state = CallState.MeHangup;
             isCalling = false;
             log.info("doHangup username = " + username + " finish");
             return flag;
         }else{
         	log.info("doHangup callindex is null");
         	return FAIL;
         }
     }
 
     public int doAnswer(String username) {
         log.info("doAnswer:" + username );
         int callIndex = calls.get(username).callIndex;
         return this.answer(callIndex);
     }
 
     public void setVideo(boolean open, Component win) {
         log.info("setVideo:" + open + "&" + win);
         int enable = 0;
         if (open) {
             enable = 1;
         }
         if (win == null) {
             this.enablevideo(enable, 0);
         }
         else{
             this.enablevideo(enable, Native.getComponentID(win));
         }
     }
 
     public void setPreview(boolean open) {
         log.info("setPreview:" + open);
         int enable = 0;
         if (open) {
             enable = 1;
         }
         this.enablevideopreview(enable);
     }
 
     //type 0-mix 1-mix
     public int doSetLccType(int type){
         log.info("doSetLccType:"+type);
         return  this.setlcctype(type);
     }
 
     public int doAddMuxVideoStream(String confno,String target,Component win){
         int callIndex = calls.get(confno).callIndex;
         long componentID = Native.getComponentID(win);
         log.info("doAddMuxVideoStream  confno:"+confno+",target:"+target+",hwnd: "+componentID+",component"+win);
         return  this.addmuxvideostream(callIndex, confno, target, componentID);
     }
     
     public int doDelMuxVideoStream(String confno,String target){
     	log.info("doDelMuxVideoStream confno:"+confno+",target:"+target);        	
         if(calls.get(confno) != null){
             int callIndex = calls.get(confno).callIndex;
             return  this.delmuxvideostream(callIndex, confno, target);
         }else{
         	log.info("doDelMuxVideoStream callindex is null");  
         }
         return -1;
     }
 
     
     public int doChangeMuxVideoStream(String confno,String target,Component win){
         int callIndex = calls.get(confno).callIndex;
         long componentID = Native.getComponentID(win);
         log.info("doChangeMuxVideoStream  confno:"+confno+",target:"+target+",hwnd: "+componentID+",component"+win);
         return  this.changemuxvideostream(callIndex, confno, target, componentID);
     }
     
     public int doSetVideoSize(int width, int height){
     	log.info("setvideosize: width = " + width + " & height = " + height);
         return this.setvideosize(width, height);
     }
     
     public int doSetVideoBitrate(int bitrate){
     	log.info("doSetVideoBitrate:" + bitrate);
     	return this.setvideobitrate(bitrate);
     }
     
     public int doCheckSoundcard(){
     	int num = getsoundcardnum();
     	log.info("getsoundcardnum num = " + num);
         return num;
     }
     
     public void doAEC(boolean open){
     	log.info("setaec:" + open);
         int enable = 0;
         if (open) {
             enable = 1;
         }
         this.setaec(enable);
     }
     
     public static boolean isCalling() {
 		return isCalling;
 	}
     
     public boolean existCall(String lccno){
     	if(calls.containsKey(lccno)){
     		return true;
     	}else{
     		return false;
     	}
     }
     
     public List<Device> getLocalCameraList(){
     	log.info("getLocalCameraList");
     	List<Device> devs = new ArrayList<Device>();
     	String[] objs = this.getwebcamlist();
     	if(objs == null || objs.length == 0){
     		return  devs;
     	}
     	for(int i = 0; i < objs.length; i = i + 2){
     		Device dev = new Device();
     		dev.index = Integer.parseInt(objs[i]);
     		dev.name = objs[i + 1];
     		devs.add(dev);
     	}
     	return devs;
     }
     
     public List<Device> getLocalMicList(){
     	log.info("getLocalMicList");
     	List<Device> devs = new ArrayList<Device>();
     	String[] objs = this.getcapturelist();
     	if(objs == null || objs.length == 0){
     		return  devs;
     	}
     	for(int i = 0; i < objs.length; i = i + 2){
     		Device dev = new Device();
     		dev.index = Integer.parseInt(objs[i]);
     		dev.name = objs[i + 1];
     		devs.add(dev);
     	}
     	return devs;
     }
     
     public List<Device> getLocalPlayBackList(){
     	log.info("getLocalPlayBackList");
     	List<Device> devs = new ArrayList<Device>();
     	String[] objs = this.getplaybacklist();
     	if(objs == null || objs.length == 0){
     		return  devs;
     	}
     	for(int i = 0; i < objs.length; i = i + 2){
     		Device dev = new Device();
     		dev.index = Integer.parseInt(objs[i]);
     		dev.name = objs[i + 1];
     		devs.add(dev);
     	}
     	return devs;
     }
     
     public void setCamera(int index){
     	log.info("setCamera index = " + index);
     	this.setwebcam(index);
     }
     
     public void setPlayBack(int index){
     	log.info("setPlayBack index = " + index);
     	this.setplayback(index);
     }
     
     public void setMic(int index){
     	log.info("setMic index = " + index);
     	this.setcapture(index);
     }
     
     public int setMicVolume(int level){
     	log.info("setMicVolume level = " + level);
     	return this.setcapturevolume(level);
     }
     
     public int setMuteMic(boolean enable){
     	log.info("setMuteMic enable = " + enable);
         int falg = 0;
         if (enable) {
         	falg = 1;
         }
     	return this.setcaptruremute(falg);
     }
     
     public int setPlaybackVolume(String username,int level){
     	StoreData storeData = calls.get(username);
     	if(storeData==null){
     		log.info("setPlaybackVolume not found username = " + username);
     		return FAIL;
     	}
     	log.info("setPlaybackVolume username = " + username + " & level = " + level);
 		int callIndex = storeData.callIndex;
     	return setplaybackvolume(callIndex, level);
     }
     
     public int setMutePlayback(String username, boolean enable){
     	int callindex = getCallIndex(username);
     	log.info("setMutePlayback username = " + username + " & callindex = " + callindex + " & enable = " + enable);
     	if(callindex != -1){    		
             int falg = 0;
             if (enable) {
             	falg = 1;
             }
             return this.setplaybackmute(callindex, falg);
     	}
     	return FAIL;
     }
     
     public int getMicVolume(){
     	int level = this.getcaptrurevolume();
     	log.info("getMicVolume level = " + level);
     	return level;
     }
     
     public int getPlaybackVolume(String username){
     	StoreData storeData = calls.get(username);
     	if(storeData==null){    		
     		log.info("getPlaybackVolume not found username = " + username);
     		return -1;
     	}
 		int callIndex = storeData.callIndex;
 		int level = this.getplaybackvolume(callIndex);
 		log.info("getPlaybackVolume level = " + level);
 		return level;
     }
     
     private int getCallIndex(String username){
     	StoreData storeData = calls.get(username);
     	if(storeData==null){   		
     		return -1;
     	}
     	return storeData.callIndex;	
     }
     
     public int startRecordAudio(String username, String filePath){
     	int callindex = getCallIndex(username);
     	log.info("startRecordAudio username = " + username + " & callindex = " + callindex + " & filePath = " + filePath);
     	if(callindex != -1){    		
     		return this.startrecordaudio(callindex, filePath);
     	}
     	return FAIL;
     }
     
     public int stopRecordAudio(String username){
     	int callindex = getCallIndex(username);
     	log.info("stopRecordAudio username = " + username + " & callindex = " + callindex);
     	if(callindex != -1){    		
     		return this.stoprecordaudio(callindex);
     	}
     	return FAIL;
     }
     
     public int startPlayAudioRecord(String filePath){
     	log.info("playAudioRecord filePath = " + filePath);
     	return this.startplayaudio(filePath);
     }
     
     public int stopPlayAudioRecord(){
     	log.info("stopPlayAudioRecord");
     	return this.stopplayaudio();
     }
     
     public int sendDTMF(String username, int type, String dtmf){
     	int callindex = getCallIndex(username);
     	log.info("sendDTMF username = " + username + " & callindex = " + callindex + " & type = " + type + " & dtmf = " + dtmf);
     	if(callindex != -1){
     		return this.senddtmf(callindex, type, dtmf);
     	}
     	return FAIL;
     }
     
     public int startVideo(String username){
     	int callindex = getCallIndex(username);
     	log.info("startVideo username = " + username + " & callindex = " + callindex);
     	if(callindex != -1){
     		return this.mutewebcam(callindex, 0);
     	}
     	return FAIL;
     }
     
     public int stopVideo(String username){
     	int callindex = getCallIndex(username);
     	log.info("stopVideo username = " + username + " & callindex = " + callindex);
     	if(callindex != -1){
     		return this.mutewebcam(callindex, 1);
     	}
     	return FAIL;
     }
     
     public int startLocalVideo(String username, long hwnd){
     	int callindex = getCallIndex(username);
     	log.info("startLocalVideo username = " + username + " & callindex = " + callindex + " & hwnd = " + hwnd);
     	if(callindex != -1){
     		return this.setvideohwnd(callindex, 0, hwnd);
     	}
     	return FAIL;
     }
     
     public int stopLocalVideo(String username){
     	int callindex = getCallIndex(username);
     	log.info("stopLocalVideo username = " + username + " & callindex = " + callindex);
     	if(callindex != -1){
     		return this.setvideohwnd(callindex, 0, 0);
     	}
     	return FAIL;
     }
     
     public int startRemoteVideo(String username, long hwnd){
     	int callindex = getCallIndex(username);
     	log.info("startRemoteVideo username = " + username + " & callindex = " + callindex + " & hwnd = " + hwnd);
     	if(callindex != -1){
     		return this.setvideohwnd(callindex, 1, hwnd);
     	}
     	return FAIL;
     }
     
     public int stopRemoteVideo(String username){
     	int callindex = getCallIndex(username);
     	log.info("stopRemoteVideo username = " + username + " & callindex = " + callindex);
     	if(callindex != -1){
     		return this.setvideohwnd(callindex, 1, 0);
     	}
     	return FAIL;
     }
     
     public int holdCall(String username, boolean enable){
     	int callindex = getCallIndex(username);
     	log.info("holdCall username = " + username + " & callindex = " + callindex + " & enable = " + enable);
     	if(callindex != -1){
             int falg = 0;
             if (enable) {
             	falg = 1;
             }
     		return this.holdcall(callindex, falg);
     	}
     	return FAIL;
     }
     
     public int transferCall(String username, String transferTo){
     	int callindex = getCallIndex(username);
     	log.info("transferCall username = " + username + " & callindex = " + callindex + " & transferTo = " + transferTo);
     	if(callindex != -1){
     		return this.transfercall(callindex, transferTo);
     	}
     	return FAIL;
     }
     
     /**
      * 动态设置视频参数
      * @param fps 帧率
      * @param bitrate 码流
      * @param width 视频宽度
      * @param height 视频高度
      * @return
      */
     public int setVideoProperty(int fps, int bitrate, int width, int height){
     	log.info("setVideoProperty fps = " + fps + " & bitrate = " + bitrate + " & width = " + width + " & height = " + height);
     	return this.setvideoproperty(fps, bitrate, width, height);
     }
     
     //对应mcu.conf的ip和port
     public int setMcuProxy(String ip, int port){
     	log.info("setMcuProxy ip = " + ip + " & port = " + port);
     	return this.ucssetmcuproxy(ip, port);
     }
     
     public int setAudioCodes(Object[] codes){
     	log.info("setAudioCodes codes = " + StringUtil.arrayToDelimitedString(codes, ";"));
     	return this.setaudiocodes(codes);
     }
     
     public int callReInvite(String username){
     	int callindex = getCallIndex(username);
     	log.info("callReInvite username = " + username + " & callindex = " + callindex);
     	if(callindex != -1){
     		return this.callreinvite(callindex);
     	}
     	return FAIL;
     }
     
     //===========================================================================
 
 	public static class RegisterData {
 
         public String username;
         public String password;
         public String serverIP;
         public int port;
 
         public RegisterData(){}
         
         public RegisterData(String username, String password, String serverIP,
                 int port) {
             this.username = username;
             this.password = password;
             this.serverIP = serverIP;
             this.port = port;
         }
     }
 	
 	public static class Device{
 		private int index;
 		private String name;
 		public String toString() {
 			String simpleName = name;
 			int idx = simpleName.indexOf(":");
 			if(idx!=-1){
 				simpleName = simpleName.substring(idx+1);
 				idx = simpleName.indexOf("(");
 				if(idx!=-1){
 					simpleName = simpleName.substring(0, idx);
 				}
 			}
 	        return simpleName;
 	    }
 		public int getIndex() {
 			return index;
 		}
 		public void setIndex(int index) {
 			this.index = index;
 		}
 		public String getName() {
 			return name;
 		}
 		public void setName(String name) {
 			this.name = name;
 		}
 		
 	}
     
     private class StoreData{
     	public int callIndex = -1;
     	public int type = -1;
 //    	public boolean isConnect = false;
     	public CallState state;
     }
     
     private enum CallState{
     	Connected,
     	MeHangup,
     	OtherHangup
     }
 }
