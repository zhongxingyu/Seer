 package de.uniulm.bagception.rfidapi;
 
 import java.util.HashSet;
 
 import android.content.Context;
 import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
 import android.os.Handler;
 import android.util.Log;
 import de.uniulm.bagception.broadcastconstants.BagceptionBroadcastContants;
 import de.uniulm.bagception.rfidapi.CMD_PwrMgt.PowerState;
 import de.uniulm.bagception.rfidapi.connection.USBConnectionServiceCallback;
 import de.uniulm.bagception.rfidapi.connection.USBConnectionServiceHelper;
 
 public class RFIDMiniMe  {
 	
 
 
 	private static MtiCmd mMtiCmd;
 	private volatile static boolean isScanning = false; 
 	static HashSet<String> hashTagList = new HashSet<String>(); // for unique tagIds
 
 	private static double startTime=0;
 	
 
 	private static final Handler broadCastHandler = new Handler();
 	
 	public static void triggerInventory(final Context c){
 		if (UsbCommunication.getInstance() == null){
 			UsbCommunication.newInstance();
 		}
 		USBConnectionServiceHelper connHelper = new USBConnectionServiceHelper(c,new USBConnectionServiceCallback() {
 			
 			@Override
 			public void onUSBConnectionError(Exception e) {
 				e.printStackTrace();
 				
 			}
 			
 			@Override
 			public void onUSBConnected(boolean connected) {
 				if (connected){
 					initInventory(c);
 				}else{
 					Intent intent = new Intent();
 					intent.setAction(BagceptionBroadcastContants.BROADCAST_RFID_NOTCONNECTED);
 					c.sendBroadcast(intent);	
 				}
 			}
 		});
 		connHelper.checkUSBConnection();
 	}
 	
 	private static synchronized void initInventory(final Context c) {
 		log("init inventory");
 		isScanning = true;
 		startTime = System.currentTimeMillis();
 		
 		new Thread() {
			String tagId;	
 
			ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
			
 			public void run() {
 				
 				hashTagList.clear();	// optional?
 				{
 					Intent intent = new Intent();
 					intent.setAction(BagceptionBroadcastContants.BROADCAST_RFID_START_SCANNING);
 					c.sendBroadcast(intent);
 				}
 				while (isScanning){
 					
 					//stop after 3 min
 					
 					
 					if (System.currentTimeMillis()-startTime>1000*3*60){
 						isScanning=false;
 						break;
 					}
 				
 					mMtiCmd = new CMD_Iso18k6cTagAccess.RFID_18K6CTagInventory(UsbCommunication.getInstance());
 					CMD_Iso18k6cTagAccess.RFID_18K6CTagInventory finalCmd = (CMD_Iso18k6cTagAccess.RFID_18K6CTagInventory) mMtiCmd;
 					if (finalCmd.setCmd(CMD_Iso18k6cTagAccess.Action.StartInventory)) {
 						for(int tagCount = 0; tagCount < finalCmd.getTagNumber(); tagCount++){
 							boolean newTagFound = hashTagList.add(finalCmd.getTagId());
 							if(newTagFound){
 								// send broadcast and remove last character from string (blank)
								sendBroadcastTagFound(c, finalCmd.getTagId().substring(0, finalCmd.getTag().length()-1));
 								startTime=System.currentTimeMillis();//update timestamp, to prevent stop scanning 
 								log("tag added: " + finalCmd.getTagId());
 							}
 							finalCmd.setCmd(CMD_Iso18k6cTagAccess.Action.NextTag);
 						}
 
 					} else {
 						// #### process error ####
 					}
 				}
 				// mProgDlg.dismiss();
 				Intent intent = new Intent();
 
 				intent.setAction(BagceptionBroadcastContants.BROADCAST_RFID_FINISHED);
 				c.sendBroadcast(intent);
 
 				sleepMode();
 
 			}
 
 		}.start();
 
 	}
 
 
 	public static void stopInventory(){
 		isScanning=false;
 	}
 
 	private static void sendBroadcastTagFound(final Context c,final String tagId){
 		broadCastHandler.post(new Runnable() {
 			@Override
 			public void run() {
 				Intent intent = new Intent();
 				intent.setAction(BagceptionBroadcastContants.BROADCAST_RFID_TAG_FOUND);
 				intent.putExtra(BagceptionBroadcastContants.BROADCAST_RFID_TAG_FOUND, tagId);
 				c.sendBroadcast(intent);				
 			}
 		});
 
 	}
 	
 	
 	
 	public static void setPowerLevelTo18() {
 		MtiCmd mMtiCmd = new CMD_AntPortOp.RFID_AntennaPortSetPowerLevel(UsbCommunication.getInstance());
 		CMD_AntPortOp.RFID_AntennaPortSetPowerLevel finalCmd = (CMD_AntPortOp.RFID_AntennaPortSetPowerLevel) mMtiCmd;
 		
 		finalCmd.setCmd((byte)18);
 	}
 	
 	public static void sleepMode() {
 		MtiCmd mMtiCmd = new CMD_PwrMgt.RFID_PowerEnterPowerState(
 				UsbCommunication.getInstance());
 		CMD_PwrMgt.RFID_PowerEnterPowerState finalCmd = (CMD_PwrMgt.RFID_PowerEnterPowerState) mMtiCmd;
 		finalCmd.setCmd(PowerState.Sleep);
 	}
 	
 	private static void log(String string) {
 		Log.d("RFIDMiniMe", string);
 	}
 	
 }
