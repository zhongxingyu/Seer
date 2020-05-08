 package com.lorent.lvmc.ucs;
 
 import java.util.Date;
 
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import org.apache.log4j.Logger;
 import org.apache.xmlrpc.server.PropertyHandlerMapping;
 import org.apache.xmlrpc.server.XmlRpcServer;
 import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
 import org.apache.xmlrpc.webserver.WebServer;
 
 import com.jtattoo.plaf.mcwin.McWinLookAndFeel;
 import com.lorent.common.util.LCMUtil;
 import com.lorent.common.util.PlatformUtil;
 import com.lorent.common.util.StringUtil;
 import com.lorent.lvmc.Launcher;
 import com.lorent.lvmc.util.ConfigUtil;
 import com.lorent.lvmc.util.Constants;
 import com.lorent.util.LCCUtil;
 
 public class UCSServer {
 	private static Logger log = Logger.getLogger(UCSServer.class);
 	
 	public static void init(int port)throws Exception{
 		log.info("init : " + port);
         WebServer webServer = new WebServer(port);
         
         XmlRpcServer xmlRpcServer = webServer.getXmlRpcServer();
         
         PropertyHandlerMapping phm = new PropertyHandlerMapping();
         /* Load handler definitions from a property file.
          * The property file might look like:
          *   Calculator=org.apache.xmlrpc.demo.Calculator
          *   org.apache.xmlrpc.demo.proxy.Adder=org.apache.xmlrpc.demo.proxy.AdderImpl
          */
         phm.addHandler("ucs", UCSServer.class);
         /* You may also provide the handler classes directly,
          * like this:
          * phm.addHandler("Calculator",
          *     org.apache.xmlrpc.demo.Calculator.class);
          * phm.addHandler(org.apache.xmlrpc.demo.proxy.Adder.class.getName(),
          *     org.apache.xmlrpc.demo.proxy.AdderImpl.class);
          */
         xmlRpcServer.setHandlerMapping(phm);
       
         XmlRpcServerConfigImpl serverConfig =
             (XmlRpcServerConfigImpl) xmlRpcServer.getConfig();
         serverConfig.setEnabledForExtensions(true);
         serverConfig.setEnabledForExceptions(true);
         serverConfig.setContentLengthOptional(false);
         webServer.start();
 	}
 	
 	public boolean init() throws Exception{
 		log.info("init()");
 		LCCUtil.getInstance().addEventListener(new MyJNIListener());
 		LCCUtil.getInstance().setVideo(true, null);
 		LCCUtil.getInstance().setOneCall(false);
 		LCCUtil.getInstance().setMcuProxy(ConfigUtil.getProperty("serverIP"), ConfigUtil.getIntProperty("csPort"));
 		return true;
 	}
 	
 	private static class ConfigData{
 		public String serverIP;
 		public int serverPort;
 		public String username;
 		public String passwd;
 	}
 	
 	private static ConfigData data = new ConfigData();
 
 	
 	public boolean setsipserver(String serverIP, int serverPort){
 		log.info("setsipserver : serverIP = " + serverIP + " & serverPort = " + serverPort);
 		data.serverIP = serverIP;
 		data.serverPort = serverPort;
 		return true;
 	}
 	
 	public boolean setusername(String username){
 		log.info("setusername : " + username );
 		data.username = username;
 		return true;
 	}
 	
 	public boolean setpassword(String passwd){
 		log.info("setpassword : " + passwd );
 		data.passwd = passwd;
 		return true;
 	}
 	
 	public boolean register(){
 		String username = "sip:" + data.username + "@" + data.serverIP + ":" + data.serverPort;
 		String serverIP = "sip:" + data.serverIP + ":" + data.serverPort;
 		String passwd = data.passwd;
 		log.info("register : username = " + username + " & passwd = " + passwd + " & serverIP = " +serverIP);
 //		LCCUtil.getInstance().register(username, passwd, serverIP, 0);
 		LCCUtil.getInstance().register(data.username, data.serverIP, data.serverPort+"", data.passwd, 0);
 		return true;
 	}
 	
 	public boolean isregister(){
 		boolean flag = LCCUtil.isRegister();
 		log.info("isregister = " + flag);
 		return flag;
 	}
 	
 	public int uninit(){
 		log.info("uninit");
 		return LCCUtil.getInstance().doPostProcess();
 	}
 	
 	public int unregister(){
 		log.info("unregister");
 		return LCCUtil.getInstance().doUnReg();
 	}
 	
 	public boolean call(String username){
 		log.info("call : username = " + username);
 		Start.calling = username;
 		LCCUtil.getInstance().doCall(username);
 		return true;
 	}
 	
 	public int hangup(String username){
 		log.info("hangup : username = " + username);
 		return LCCUtil.getInstance().doHangup(username);
 	}
 	
 	public int answercall(String username){
 		log.info("answercall : username = " + username);
 		return LCCUtil.getInstance().doAnswer(username);
 	}
 	
 	public boolean setconfserverip(String ip){
 		try {
 			ConfigUtil.setProperty("serverIP", ip);
 		} catch (Exception e) {
 			log.error("setconfserverip", e);
 		}
 		return true;
 	}
 	
 	public boolean callmeeting(final String confno){
 		log.info("callmeeting : confno = " + confno);
 		showConf(confno, false);
 		return true;
 	}
 	
 	private void showConf(final String confno, final boolean isAnswer){
 		SwingUtilities.invokeLater(new Runnable() {
 			
 			@Override
 			public void run() {
 				try{
 					UIManager.setLookAndFeel(new McWinLookAndFeel());
 					Launcher.startLvmcFromOutSide(new String[]{confno, confno, data.username, data.passwd, ConfigUtil.getProperty("serverIP")}, Constants.AppName.UCS, isAnswer);
 				}catch(Exception e){
 					log.error("showConf", e);
 				}
 			}
 		});
 	}
 	
 	public boolean createconf(String username, String confno){
 		log.info("createconf : username = " + username + " & confno = " + confno);
 		try{
 			getLCMUtil().createUCSConf(username, confno);
 		}catch(Exception e){
 			log.error("createconf", e);
 			return false;
 		}
 		return true;
 	}
 	
 	private LCMUtil getLCMUtil()throws Exception{
 		String url = "http://" + ConfigUtil.getProperty("serverIP") + ConfigUtil.getProperty("lcm.xmlrpc");
 		return LCMUtil.newInstance(url);
 	}
 	
 	public boolean removeconf(String username, String confno){
 		log.info("removeconf : username = " + username + " & confno = " + confno);
 		try{
 			getLCMUtil().removeUCSConf(username, confno);
 		}catch(Exception e){
 			log.error("removeconf", e);
 			return false;
 		}
 		return true;
 	}
 	
 	public boolean getDevList(){
 //		List<Device> cs = LCCUtil.getInstance().getLocalCameraList();
 //		System.out.println("===============CameraList===============");
 //		for(Device c : cs){
 //			System.out.println("index = " + c.index + " & name = " + c.name);
 //		}
 //		cs = LCCUtil.getInstance().getLocalMicList();
 //		System.out.println("===============MicList===============");
 //		for(Device c : cs){
 //			System.out.println("index = " + c.index + " & name = " + c.name);
 //		}
 //		cs = LCCUtil.getInstance().getLocalPlayBackList();
 //		System.out.println("===============PlayBackList===============");
 //		for(Device c : cs){
 //			System.out.println("index = " + c.index + " & name = " + c.name);
 //		}
 		return true;
 	}
 	
 	public int startrecordaudio(String number, String filePath){
 		log.info("startrecordaudio : username = " + number + " & filePath = " + filePath);
 		return LCCUtil.getInstance().startRecordAudio(number, filePath);
 	}
 	
 	public int stoprecordaudio(String number){
 		log.info("stoprecordaudio : username = " + number);
 		return LCCUtil.getInstance().stopRecordAudio(number);
 	}
 	
 	public int startplayaudio(String filePath){
 		log.info("startplayaudio : filePath = " + filePath);
 		return LCCUtil.getInstance().startPlayAudioRecord(filePath);
 	}
 	
 	public int stopplayaudio(){
 		log.info("startplayaudio");
 		return LCCUtil.getInstance().stopPlayAudioRecord();
 	}
 	
 	public int senddtmf(String username, int type, String dtmf){
 		log.info("sendDTMF username = " + username + " & type = " + type + " & dtmf = " + dtmf);
 		return LCCUtil.getInstance().sendDTMF(username, type, dtmf);
 	}
 	
 	public int startvideo(String username){
 		log.info("startvideo username = " + username);
 		return LCCUtil.getInstance().startVideo(username);
 	}
 	
 	public int stopvideo(String username){
 		log.info("stopvideo username = " + username);
 		return LCCUtil.getInstance().stopVideo(username);
 	}
 	
 	public int startlocalvideo(String username, int hwnd){
 		log.info("startlocalvideo username = " + username + " & hwnd = " + hwnd);
 		return LCCUtil.getInstance().startLocalVideo(username, hwnd);
 	}
 	
 	public int stoplocalvideo(String username){
 		log.info("stoplocalvideo username = " + username);
 		return LCCUtil.getInstance().stopLocalVideo(username);
 	}
 	
 	public int startremotevideo(String username, int hwnd){
 		log.info("startremotevideo username = " + username + " & hwnd = " + hwnd);
 		return LCCUtil.getInstance().startRemoteVideo(username, hwnd);
 	}
 	
 	public int stopremotevideo(String username){
 		log.info("stopremotevideo username = " + username);
 		return LCCUtil.getInstance().stopRemoteVideo(username);
 	}
 	
 	public int holdcall(String username, boolean enable){
 		log.info("holdcall username = " + username + " & enable = " + enable);
 		return LCCUtil.getInstance().holdCall(username, enable);
 	}
 	
 	public int transfercall(String username, String transferTo){
 		log.info("transfercall username = " + username + " & transferTo = " + transferTo);
 		return LCCUtil.getInstance().transferCall(username, transferTo);
 	}
 	
 	public int setspeakervolume(String username, int level){
 		log.info("setspeakervolume username = " + username + " & level = " + level);
 		return LCCUtil.getInstance().setPlaybackVolume(username, level);
 	}
 	
 	public int setmicvolume(int level){
 		log.info("setmicvolume level = " + level);
 		return LCCUtil.getInstance().setMicVolume(level);
 	}
 	
 	public int getspeakervolume(String username){
 		int level = LCCUtil.getInstance().getPlaybackVolume(username);
 		log.info("getspeakervolume username = " + username + " & level = " + level);
 		return level;
 	}
 	
 	public int getmicvolume(){
 		int level = LCCUtil.getInstance().getMicVolume();
 		log.info("getmicvolume level = " + level);
 		return level;
 	}
 	
 	public int setspeakermute(String username, boolean enable){
 		log.info("setspeakermute username = " + username + " & enable = " + enable);
 		return LCCUtil.getInstance().setMutePlayback(username, enable);
 	}
 	
 	public int setmicmute(boolean enable){
 		log.info("setmicmute enable = " + enable);
 		return LCCUtil.getInstance().setMuteMic(enable);
 	}
 	
 	public boolean answermeeting(String confno){
 		log.info("answermeeting confno = " + confno);
 		showConf(confno, true);
 		return true;
 	}
 	
 	public Object[] getconflist()throws Exception{
 		log.info("getconflist");
 		return getLCMUtil().getUCSConf();
 	}
 	
 	/**
 	 * 
 	 * @param users 每一个Object是String[],其中Str[0]=username,Str[1]=realname,Str[2]=lccno,Str[3]=passwd
 	 * @return
 	 */
 	public boolean addorupdateuser(Object[] users)throws Exception{
 		for(Object user : users){
 			Object[] temp = (Object[])user;
 			log.info("addOrUpdateUser username = " + temp[0] + " & realname = " + temp[1] + " & lccno = " + temp[2] + " & passwd = " + temp[3]);
 		}
 		getLCMUtil().addOrUpdateUCSUser(users);
 		return true;
 	}
 	
 }
