 package com.lorent.lvmc.ucs;
 
 import org.apache.log4j.Logger;
 
 import com.lorent.common.util.PlatformUtil;
 import com.lorent.common.util.ProcessUtil;
 import com.lorent.common.util.StringUtil;
import com.lorent.lvmc.controller.ControllerFacade;
 import com.lorent.util.LCCUtil;
 
 public class Start {
 	
 	private static Logger log = Logger.getLogger(Start.class);
 	
 	public static void main(String[] args){
 //		new Start().execute(new String[]{"6082", "6081"});
 		new Start().execute(args);
 	}
 	
 	public static String calling;
 	
 	public void execute(String[] args){
 		String slog = "";
 		for (String string : args) {
 			slog = slog +"  "+string;
 		}
 		log.info("ucs phone module start "+slog);
		
 		try{
			clearScreenShare();
 			int dllPort = Integer.parseInt(args[0]);
 			int myPort = Integer.parseInt(args[1]);
 			if (PlatformUtil.getOSVersion() >= 6.0f) {
 				if (PlatformUtil.isUserAnAdmin() && !args[args.length-1].equals("SECURITY_MANDATORY_MEDIUM_RID")) {
 					String createProcessPath = com.lorent.lvmc.util.Constants.AppPath + "/createprocess.exe";
 					createProcessPath = StringUtil.convertFilePath2DOSCommandStr(createProcessPath);
 					String lvmcPath = com.lorent.lvmc.util.Constants.AppPath + "/lvmc.exe";
 					lvmcPath = StringUtil.convertFilePath2DOSCommandStr(lvmcPath+" "+dllPort+" "+myPort+" SECURITY_MANDATORY_MEDIUM_RID");
 					String cmdStr = createProcessPath+" "+lvmcPath;
 					log.info(cmdStr);
 					Process process = Runtime.getRuntime().exec(cmdStr);
 	                process.waitFor();
 	                System.exit(0);
 				}
 			}
 			UCSClient.init(dllPort);
 			UCSServer.init(myPort);
 			UCSClient.sendMyState(UCSClient.START_SUCCESS);
 			new HeartThread().start();
 		}catch(Exception e){
 			log.error("main", e);
 			try {
 				UCSClient.sendMyState(UCSClient.START_FAIL);
 			} catch (Exception e1) {
 				log.error("main", e1);
 			}
 			System.exit(0);
 		}
 	}
 	
 	private void clearScreenShare() throws Exception{
 		if (ProcessUtil.getInstance().processExists("winvnc.exe")) {
     		log.info("clearScreenShare taskkill winvnc");
     		Process process = Runtime.getRuntime().exec("cmd /c taskkill /f /im winvnc.exe /t ");
             process.waitFor();
             log.info("clearScreenShare taskkill winvnc end");
 		}
 		log.info("clearScreenShare  delete service");
 		Process process = Runtime.getRuntime().exec("cmd /c sc delete uvnc_service");
         process.waitFor();
         log.info("clearScreenShare delete service end");
 	}
 	
 	private class HeartThread extends Thread{
 		@Override
 		public void run() {
 			while(true){
 				try{
 					sleep(5000);
 					UCSClient.sendMyState(UCSClient.HEART);
 				}catch(Exception e){
 					log.error("HeartThread" ,e);
 					break;
 				}
 			}
 			log.info("dll is killed i will exit");
 			LCCUtil.getInstance().doPostProcess();
 		}
 	}
 }
