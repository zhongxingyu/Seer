 package com.inspedio.system.helper.record;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import javax.microedition.rms.RecordStore;
 import com.inspedio.enums.LogLevel;
 import com.inspedio.system.helper.InsLogger;
 
 public class SaveManager {
 
 	protected String recordName;
 	protected String recordVersion;
 	protected Hashtable dataList;
 	protected int dataCount;
 	
 	public SaveManager(String RecordName){
 		this(RecordName, "1.0");
 	}
 	
 	public SaveManager(String RecordName, String RecordVersion){
 		this.recordName = RecordName;
 		this.recordVersion = RecordVersion;
 		this.dataList = new Hashtable();
 		this.dataCount = 0;
 	}
 	
 	public String getVersion(){
 		return this.recordVersion;
 	}
 	
 	public String getName(){
 		return this.recordName;
 	}
 	
 	/**
 	 * WARNING : DO NOT OVERRIDE THIS.<br>
 	 * This is a Save method to RecordStore. Override InitData method instead.
 	 */
 	public void save(){
 		try
 		{
 			RecordStore.deleteRecordStore(recordName);
 			RecordStore recordStore = RecordStore.openRecordStore(recordName, true, RecordStore.AUTHMODE_PRIVATE, true);
 			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
 			DataOutputStream dataStream = new DataOutputStream(byteStream);
 			
 			this.dataCount = dataList.size();
 			
 			dataStream.writeUTF(this.recordName);
 			dataStream.writeUTF(this.recordVersion);
 			dataStream.writeInt(this.dataCount);
 			
 			InsLogger.writeLog("Writing SaveData Header Sucess", LogLevel.PROCESS);
 			InsLogger.writeLog("Record Name : " + this.recordName + ", Version : " + this.recordVersion, LogLevel.PROCESS);
 			InsLogger.writeLog("SaveData Item Count : " + this.dataCount, LogLevel.PROCESS);
 			
 			for (Enumeration e = this.dataList.elements() ; e.hasMoreElements() ;) {
 				SaveDataObject obj = (SaveDataObject) e.nextElement();
 				if(obj != null){
 					obj.write(dataStream);
 				}
 		     }
 
 			InsLogger.writeLog("Writing SaveData Object Sucess", LogLevel.PROCESS);
 			
 			byte[] data = byteStream.toByteArray();
 			dataStream.close();
 			recordStore.addRecord(data, 0, data.length);
 			recordStore.closeRecordStore();
 			
 			InsLogger.writeLog("Saving GameData Success", LogLevel.SYSTEM);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 	}
 	
 	/**
 	 * WARNING : DO NOT OVERRIDE THIS.<br>
 	 * This is a Load method to RecordStore.
 	 */
 	public boolean load(){
 		boolean success = false;
 		try
 		{
 			RecordStore recordStore = RecordStore.openRecordStore(recordName, true, RecordStore.AUTHMODE_PRIVATE, true);
 			
 			int count = recordStore.getNumRecords();
 			
 			if(count > 0){
 				byte[] data = recordStore.getRecord(count);
 				
 				ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
 				DataInputStream dataStream = new DataInputStream(byteStream);
 				
 				String dataName = dataStream.readUTF();
 				String dataVersion = dataStream.readUTF();
 				this.dataCount = dataStream.readInt();
 				InsLogger.writeLog("Reading SaveData Header Sucess", LogLevel.PROCESS);
 				InsLogger.writeLog("Record Name : " + dataName + ", Version : " + dataVersion, LogLevel.PROCESS);
 				InsLogger.writeLog("SaveData Item Count : " + this.dataCount, LogLevel.PROCESS);
 				
 				for(int i = 0; i < dataCount; i++){
 					SaveDataObject obj = new SaveDataObject(dataStream);
 					this.dataList.put(obj, obj.name);
 				}
 				
 				dataStream.close();
 		
 				InsLogger.writeLog("Loading GameData Success", LogLevel.SYSTEM);
 				success = true;
 			} else {
 				InsLogger.writeLog("GameData is not Found", LogLevel.PROCESS);
 			}
 			recordStore.closeRecordStore();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return success;
 	}
 
 	/**
 	 * Clear all Save Data previously added (Do not delete Data saved on device)
 	 */
 	public void clear(){
 		this.dataList.clear();
 		this.dataCount = 0;
 	}
 	
 	/**
 	 * Add new Data to List. <br>
 	 * If there is already other data with same name, change its value instead.
 	 */
 	public void addData(SaveDataObject obj){
 		if(!this.dataList.containsKey(obj.name)){
			this.dataList.put(obj, obj.name);
 			this.dataCount++;
 		}
 	}
 	
 	public boolean isDataExist(String Name){
 		return this.dataList.containsKey(Name);
 	}
 	
 	public SaveDataObject getData(String Name){
 		if(this.isDataExist(Name)){
 			return (SaveDataObject) this.dataList.get(Name);
 		}
 		return null;
 	}
 		
 }
