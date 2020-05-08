 package com.appliedanalog.gamesync;
 
 import java.io.File;
 import java.util.Date;
 import java.util.concurrent.Semaphore;
 
 import android.app.Activity;
 
 import com.appliedanalog.gamesync.net.SyncClient;
 import com.appliedanalog.gamesync.types.AppInfo;
 import com.appliedanalog.gamesync.utils.Packager;
 
 /**
  * This is the master class that handles the application logic for this app.
  * @author jbetker
  *
  */
 public class GameSyncEngine {
 	public static final int ERROR = -1;
 	public static final int DO_NOTHING = 0;
 	public static final int PUSH_UP = 1;
 	public static final int PULL_DOWN = 2;
 	public static final int DESYNCED = 3;
 	
 	SyncClient syncclient;
 	SuperuserInterface suiface;
 	Packager packager;
 	Activity context;
 	
 	public GameSyncEngine(Activity cont, SyncClient cli, SuperuserInterface sui){
 		context = cont;
 		syncclient = cli;
 		suiface = sui;
 		packager = new Packager();
 	}
 	
 	/**
 	 * Returns one of the three constants defined by this class, denoting whether the app data is up to date,
 	 * needs to be pulled down, needs to be pushed up, or whether the state is desynced and needs user interaction.
 	 * @param id
 	 * @return
 	 */
 	public synchronized int determineSyncOp(String id){
 		if(!syncclient.connected()) return ERROR;
 		
 		//if(id != null) return DESYNCED;
 		
 		if(!suiface.hasData(id)){
 			AppInfo sinfo = syncclient.getInfo(id);
 			if(sinfo == null){
 				return DO_NOTHING;
 			}else{
 				return PULL_DOWN;
 			}
 		}
 		
 		String err = putAppInZipFile(id);
 		if(err != null){
 			System.out.println("Error in GameSyncEngine.determineSyncOp: " + err);
 			return ERROR;
 		}
 		File zip = new File(suiface.BASE_DIR + id + ".zip");
 		long cksum = -1;
 		try{
 			cksum = packager.getChecksum(zip);
 		}catch(Exception e){
 			e.printStackTrace();
 			return ERROR;
 		}
 		GameSyncData gsdata = GameSyncData.getData(context);
 		AppInfo info = gsdata.getInfo(id);
 		gsdata.close();
 		AppInfo sinfo = syncclient.getInfo(id);
 		if(sinfo == null){ //if its not on the server, push it up!
 			return PUSH_UP;
 		}
 		if(info == null){
 			System.out.println("desynced because info is null");
 			return DESYNCED;
 		}
 		if(cksum == info.crc){
 			if(info.crc == sinfo.crc){
 				return DO_NOTHING;
 			}else{
 				if(sinfo.getLastModified().after(info.getLastModified())){
 					return PULL_DOWN;
 				}else{
 					System.out.println("desynced because crc is equal but lastmodified locally is: " + info.getLastModified().toString() + " and remote is: " + sinfo.getLastModified().toString());
 					return DESYNCED;
 				}
 			}
 		}else{
 			if(info.crc == sinfo.crc){
 				return PUSH_UP;
 			}else{
 				System.out.println("Desynced because all crcs are different: localcrc=" + cksum + " localstashedcrc=" + info.crc + " servercrc=" +  sinfo.crc);
 				return DESYNCED;
 			}
 		}
 	}
 	
 	/**
 	 * Pushes an app save state up to the server, replacing any currently residing there. THIS FUNCTION IS INTENDED TO BE CALLED
 	 * IMMEDIATELY OR SOON AFTER CALLING {determineSyncOp}, NOT DOING THIS CAN BE VERY BUGGY.
 	 * @param id
 	 * @return Whether the push is successful or not.
 	 */
 	public synchronized boolean pushUp(String id){
 		if(!syncclient.connected()) return false;
 		System.out.println("PUSHING " + id);
 		//if(id != null) return true;
 		
 		File zip = new File(suiface.BASE_DIR + id + ".zip");
 		if(!zip.exists()){
 			System.out.println("Fatal error in GameSyncEngine.pushUp: " + zip.getName() + " does not exist.");
 			return false;
 		}
 		
 		long cksum = -1;
 		try{
 			//compute checksum
 			cksum = packager.getChecksum(zip);
 		}catch(Exception e){
 			e.printStackTrace();
 			return false;
 		}
 		
 		String err = syncclient.sendFile(id, zip, cksum);
 		if(err != null){
 			System.out.println("Fatal error in GameSyncEngine.pushUp: " + err);
 			return false;
 		}
 		
 		AppInfo tinfo = syncclient.getInfo(id);
 		if(tinfo == null){
 			System.out.println("Fatal error in GameSyncEngine.pushUp: failed to pull newly pushed app from server!");
 			return false;
 		}
 		Date lmod = tinfo.last_modified;
 		
 		GameSyncData gsdata = GameSyncData.getData(context);
 		AppInfo info = gsdata.getInfo(id);
 		if(info == null){
 			info = new AppInfo(id);
 			gsdata.addApp(info);
 			info = gsdata.getInfo(id);
 		}
 		info.crc = cksum;
 		info.enabled = true;
 		info.hidden = false;
 		info.last_modified = lmod;
 		gsdata.updateApp(info);
 		gsdata.close();
 		return true;
 	}
 	
 	/**
 	 * Pulls an app save state from the server, replacing all local app data. THIS FUNCTION IS INTENDED TO BE CALLED
 	 * IMMEDIATELY OR SOON AFTER CALLING {determineSyncOp}, NOT DOING THIS CAN CAUSE LOSS OF DATA.
 	 * @param id
 	 * @return Whether the pull is successful or not.
 	 */
 	public synchronized boolean pullDown(String id){
 		if(!syncclient.connected()) return false;
 		System.out.println("PULLING " + id);
 		//if(id != null) return true;
 		
 		System.out.println("Trying to pull " + id);
 		
 		File zip = new File(suiface.BASE_DIR + id + ".zip");
 		
 		String err = syncclient.getFile(id, zip);
 		if(err != null){
 			System.out.println("Fatal error in GameSyncEngine.pullDown: " + err);
 			return false;
 		}
 		System.out.println("Pulled down zip file");
 		
 		File extract_dir = new File(suiface.PULL_DIRECTORY);
 		if(!Utils.clearDirectory(extract_dir)){
 			System.out.println("Fatal error in GameSyncEngine.pullDown: unable to clear out backup restoration directory.");
 			return false;
 		}
 		System.out.println("Cleared out extraction destination");
 		if(!packager.unpackAndUnzip(zip, extract_dir)){
 			System.out.println("Fatal error in GameSyncEngine.pullDown: Unable to unzip backup file.");
 			return false;
 		}
 		System.out.println("Unzipped file");
 		err = suiface.restoreDataDir(); //the restore directory is actually saved inside of the zip file; this reinforces backup integrity
 		if(err != null){
 			System.out.println("Fatal error in GameSyncEngine.pullDown: " + err);
 			return false;
 		}		
 		
 		AppInfo sinfo = syncclient.getInfo(id);
 		if(sinfo == null){
 			System.out.println("Fatal error in GameSyncEngine.pullDown: Error retrieving app info for " + id + ".");
 			return false;
 		}
 		
 		GameSyncData syncdb = GameSyncData.getData(context);
 		AppInfo info = syncdb.getInfo(id);
 		if(info == null){
 			info = new AppInfo(id); 
 			syncdb.addApp(info);
 			info = syncdb.getInfo(id);
 		}
 		info.crc = sinfo.crc;
 		info.last_modified = sinfo.last_modified;
 		syncdb.updateApp(info);
 		syncdb.close();
 		return true;
 	}
 	
 	/**
 	 * Re-pulls the data directory of the specified app, zips it up, and pushes it to the server, without doing any of the other checks.
 	 * You should only use this method if you know that before you had a newer version of the app data than was on the server.
 	 * @param id
 	 * @return
 	 */
 	public synchronized boolean rezipAndPushUp(String id){
 		if(!syncclient.connected()) return false;
 		
 		String err = putAppInZipFile(id);
 		if(err != null){
 			System.out.println("Error in GameSyncEngine.rezipAndPushUp: " + err);
 		}
 		return pushUp(id);
 	}
     
     String putAppInZipFile(String app){
     	String err = suiface.copyDataDir(app);
     	if(err != null){
     		return err;
     	}            	
     	//apply a file size filter (no need to backup files > 1MB)
     	Utils.fileCiel(new File(suiface.PUSH_DIRECTORY), 1000000);
     	
     	//now zip it all up
     	if(!packager.packAndZip(new File(suiface.PUSH_DIRECTORY), new File(suiface.BASE_DIR + app + ".zip"))){
     		return "Error zipping up data.";
     	}
     	return null;
     }
 }
