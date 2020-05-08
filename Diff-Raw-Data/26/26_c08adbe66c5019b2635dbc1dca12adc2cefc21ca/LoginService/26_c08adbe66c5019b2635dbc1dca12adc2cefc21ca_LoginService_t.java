 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.lorent.lvmc.service;
 
 import org.apache.log4j.Logger;
 import org.jhotdraw.samples.svg.SVGPanels;
 
 import com.lorent.common.dto.LCMRoleDto;
 import com.lorent.common.util.OpenfireUtil;
 import com.lorent.common.util.ParaUtil;
 import com.lorent.lvmc.controller.ControllerFacade;
 import com.lorent.lvmc.dto.LoginInfo;
 import com.lorent.lvmc.dto.MyMultiUserChat;
 import com.lorent.lvmc.event.LvmcJNIListener;
 import com.lorent.lvmc.exception.MyException;
 import com.lorent.lvmc.util.ConfigUtil;
 import com.lorent.lvmc.util.DataUtil;
 import com.lorent.lvmc.util.LCMUtil;
 import com.lorent.lvmc.util.LvmcOpenfireUtil;
 import com.lorent.lvmc.util.LvmcUtil;
 import com.lorent.lvmc.util.MessageUtil;
 import com.lorent.lvmc.util.ShareFileServerUtil;
 import com.lorent.lvmc.util.StringUtil;
 import com.lorent.lvmc.util.DataUtil.Key;
 import com.lorent.util.LCCUtil;
 import com.lorent.whiteboard.client.Client;
 
 /**
  *
  * @author jack
  */
 public class LoginService extends BaseService {
 
     private Logger log = Logger.getLogger(LoginService.class);
 
     public void doLogoutFromOutSide() throws Exception{
     	
     	ShareFileServerUtil.getInstance().closeSessionAndkeepAliveSession();
 //        LvmcOpenfireUtil.close();
     	
     	SVGPanels.getInstance().close();
         LCCUtil instance = LCCUtil.getInstance();
         if(DataUtil.getLvmcJNIListener() !=null ){
             instance.removeEventListener(DataUtil.getLvmcJNIListener());
             DataUtil.removeElement(Key.LvmcJNIListener);
         }
 //        instance.addEventListener(DataUtil.getLvmcJNIListener());
         instance.removeEventListener(DataUtil.getLvmcJNIListener());
         DataUtil.setValue(Key.LvmcJNIListener, null);
         Boolean isHanguped = DataUtil.getValue(DataUtil.Key.AleardyHangup);
         if(isHanguped ==null || !isHanguped){
             LCCUtil.getInstance().doHangup(DataUtil.getLoginInfo().getConfno());
            ControllerFacade.execute("phoneController", "lccHandupCallBack", DataUtil.getLoginInfo().getConfno());
         }
         DataUtil.setValue(DataUtil.Key.AleardyHangup, null);
         LCCUtil.getInstance().doSetLccType(LCCUtil.LCC_TYPE_MIX);
 //        LCCUtil.getInstance().doUninit();
         
         MyMultiUserChat chat = DataUtil.getValue(DataUtil.Key.ReadyToLeaveRoom);
         chat.removeListeners();
         
         LvmcOpenfireUtil.doRemoveListener();
         OpenfireUtil.getInstance().disconnect();
         DataUtil.setValue(DataUtil.Key.Room, null);
         
         
         
         DataUtil.setValue(Key.LoginInfo, null);
         DataUtil.setValue(DataUtil.Key.RepeaterHost, null);
     }
     
     public boolean doLoginFromOutSide(
     		String username,String password,String confno,String serverIP,Integer serverPort) throws Exception{
         LoginInfo info = new LoginInfo(username,password, confno, serverIP, String.valueOf(serverPort));
         DataUtil.setValue(Key.LoginInfo, info);
         DataUtil.setValue(DataUtil.Key.RepeaterHost, serverIP);
         LCMRoleDto roleDto = null;
         ConfigUtil.setProperty("serverIP", serverIP);
         try {
         	roleDto = LCMUtil.getMyRoleAndPermission(confno, username);
         	if(LvmcUtil.isUCSAPP()){
         		info.setNickName(username);
         	}else{
         		info.setNickName(roleDto.getNickname());
         	}
             info.setConferenceTitle(roleDto.getConferenceTitle());
             DataUtil.setValue(Key.myPermission, roleDto);
 		} catch (Exception e) {
 			log.error("doLoginFromOutSide.Fail", e);
 			String exceptionMessage = e.getMessage();
         	if(exceptionMessage!=null){
         		if(exceptionMessage.indexOf("Connection timed out: connect")>-1 
         				|| exceptionMessage.indexOf("Read timed out") > -1
         				|| exceptionMessage.indexOf("Connection refused: connect") > -1){
         			throw new Exception(StringUtil.getErrorString("error.server.ip"));
         		}
         		if(exceptionMessage.indexOf("用户不存在")>-1){
         			throw new Exception(StringUtil.getErrorString("error.username_password.msg"));
         		}
         		int idx = exceptionMessage.indexOf(":");
         		if(idx!=-1){
         			throw new Exception(e.getMessage().substring(idx + ":".length()));
         		}
         	}
 		}
 		if(roleDto == null){
             throw new MyException("permit.nopermission");
         }
 		if(OpenfireUtil.getInstance().getConn() == null){
 			try{
 				OpenfireUtil.getInstance().init(serverIP, serverPort, ConfigUtil.getIntProperty("timeout"));
 				OpenfireUtil.getInstance().login(username, password, "Spark 2.6.3");
 			}catch(Exception e){
 				OpenfireUtil.getInstance().disconnect();
 				throw e;
 			}
 		}
 		LvmcOpenfireUtil.doAddListener();
         LvmcOpenfireUtil.enterRoom(confno, username);
         
         
         LCCUtil instance = LCCUtil.getInstance();
         String mcuIP = serverIP;
         String mcuLocalPort = ConfigUtil.getProperty("csPort");
         try {
 //            Thread.sleep(1000);
             if(DataUtil.getLvmcJNIListener() !=null ){
                 instance.removeEventListener(DataUtil.getLvmcJNIListener());
                 DataUtil.removeElement(Key.LvmcJNIListener);
             }
             DataUtil.setValue(Key.LvmcJNIListener, new LvmcJNIListener());
             instance.addEventListener(DataUtil.getLvmcJNIListener());
 //            if(DataUtil.getLccRegisterFlag()==null || DataUtil.getLccRegisterFlag().booleanValue() == false){
             if(!LCCUtil.isRegister()){
                 log.info("lvmc  LCCUtil  register=================================================");
 //                instance.register("sip:" + username + "@" + mcuIP + ":" + mcuLocalPort, password, "sip:" + mcuIP + ":" + mcuLocalPort, 0);
                 instance.register(username, mcuIP, mcuLocalPort, password, 0);
             }else{
             	MessageUtil.getInstance().sendMessage("lccRegisterCallBackOK", null);
             }
 //                DataUtil.setValue(Key.LccRegisterFlag, Boolean.TRUE);
 //            }
             
         } catch (Exception e) {
             //log.error("", ex);
             throw e;
         }
         ControllerFacade.execute("shareDesktopController", "startScreenShareProcess");
         
     	return true;
     }
     
     public boolean doLogin(ParaUtil paras) throws Exception {
         String username = paras.getValue("username");
         String password = paras.getValue("password");
         String confno = paras.getValue("confno");
         String serverIP = paras.getValue("serverIP");
         Integer serverPort = ConfigUtil.getIntProperty("serverPort");
         log.info("登录信息：username=" + username + "&password=" + password + "&confno=" + confno + "&serverIP=" + serverIP + "&serverPort=" + serverPort);
         //save login info
         log.info("保存信息");
         ConfigUtil.setProperty("username", username);
         ConfigUtil.setProperty("confno", confno);
         ConfigUtil.setProperty("serverIP", serverIP);
         ConfigUtil.setProperty("serverPort", serverPort + "");
         LoginInfo info = new LoginInfo(username,password, confno, serverIP, String.valueOf(serverPort));
         DataUtil.setValue(Key.LoginInfo, info);
         DataUtil.setValue(DataUtil.Key.RepeaterHost, serverIP);
 //        DataUtil.setValue(DataUtil.Key.RepeaterHost, "10.168.150.71");
         LCMRoleDto roleDto = null;
         try {
             
             try{
             	roleDto = LCMUtil.getMyRoleAndPermission(confno, username);
             	if(LvmcUtil.isUCSAPP()){
             		info.setNickName(username);
             	}else{
             		info.setNickName(roleDto.getNickname());
             	}
                 info.setConferenceTitle(roleDto.getConferenceTitle());
                 DataUtil.setValue(Key.myPermission, roleDto);
             }catch(Exception e){
 //            	System.out.println("111111111111" + e.getMessage());
             	log.error("doLogin.fail", e);
             	//注销已经登录的用户
             	LvmcOpenfireUtil.logout();
             	String exceptionMessage = e.getMessage();
             	if(exceptionMessage!=null){
             		if(exceptionMessage.indexOf("Connection timed out: connect")>-1){
             			throw new Exception(StringUtil.getErrorString("error.server.ip"));
             		}
             		if(exceptionMessage.indexOf("用户不存在")>-1){
             			throw new Exception(StringUtil.getErrorString("error.username_password.msg"));
             		}
             		int idx = exceptionMessage.indexOf(":");
             		if(idx!=-1){
             			throw new Exception(e.getMessage().substring(idx + ":".length()));
             		}
             	}
             	
             }
             LvmcOpenfireUtil.login(username, password, serverIP, serverPort);
             if(roleDto == null){
                 throw new MyException("permit.nopermission");
             }
             LvmcOpenfireUtil.enterRoom(confno, username);
         } catch (Exception e) {
             log.error("doLogin", e);
             LvmcOpenfireUtil.logout();
             DataUtil.removeElement(Key.Connection);
             DataUtil.removeElement(Key.Room);
             DataUtil.removeElement(Key.myPermission);
             throw e;
 //            return false;
         }
         
         
         
         
 
         
 
         LCCUtil instance = LCCUtil.getInstance();
         String mcuIP = serverIP;
         String mcuLocalPort = ConfigUtil.getProperty("csPort");
         try {
 //            Thread.sleep(1000);
             if(DataUtil.getLvmcJNIListener() !=null ){
                 instance.removeEventListener(DataUtil.getLvmcJNIListener());
                 DataUtil.removeElement(Key.LvmcJNIListener);
             }
             DataUtil.setValue(Key.LvmcJNIListener, new LvmcJNIListener());
             instance.addEventListener(DataUtil.getLvmcJNIListener());
 //            if(DataUtil.getLccRegisterFlag()==null || DataUtil.getLccRegisterFlag().booleanValue() == false){
                 log.info("lvmc  LCCUtil  register=================================================");
 //                instance.register("sip:" + username + "@" + mcuIP + ":" + mcuLocalPort, password, "sip:" + mcuIP + ":" + mcuLocalPort, 0);
                 instance.register(username, mcuIP, mcuLocalPort, password, 0);
 //                DataUtil.setValue(Key.LccRegisterFlag, Boolean.TRUE);
 //            }
             
         } catch (Exception e) {
             //log.error("", ex);
             throw e;
         }
         ControllerFacade.execute("shareDesktopController", "startScreenShareProcess");
         log.info("登录成功");
         return true;
     }
     
     public void logout() throws Exception{
         ShareFileServerUtil.getInstance().closeSessionAndkeepAliveSession();
         LvmcOpenfireUtil.close();
         Client.getInstance().close(true);
         LCCUtil.getInstance().doHangup(DataUtil.getLoginInfo().getConfno());
     }
     
     public int doCheckSoundCard(){
         return LCCUtil.getInstance().doCheckSoundcard();
     }
     
     public void reAddMultUserChatListeners(){
     	LvmcOpenfireUtil.reAddListeners();
     }
 }
