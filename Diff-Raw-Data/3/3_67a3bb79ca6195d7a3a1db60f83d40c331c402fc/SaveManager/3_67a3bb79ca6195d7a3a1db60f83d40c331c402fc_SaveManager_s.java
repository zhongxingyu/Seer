 package com.inspedio.system.helper.record;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.util.Vector;
 import javax.microedition.rms.RecordStore;
 import com.inspedio.enums.LogLevel;
 import com.inspedio.system.helper.InsLogger;
 
 public class SaveManager {
 
 	protected String recordName;
 	protected String recordVersion;
 	protected Vector dataList;
 	protected int dataCount;
 	
 	public SaveManager(String RecordName){
 		this(RecordName, "1.0");
 	}
 	
 	public SaveManager(String RecordName, String RecordVersion){
 		this.recordName = RecordName;
 		this.recordVersion = RecordVersion;
 		this.dataList = new Vector();
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
 			
 			SaveDataObject obj;
 			this.dataCount = dataList.size();
 			
 			dataStream.writeUTF(this.recordName);
 			dataStream.writeUTF(this.recordVersion);
 			dataStream.writeInt(this.dataCount);
 			
 			InsLogger.writeLog("Writing SaveData Header Sucess", LogLevel.PROCESS);
 			
 			for(int i = 0; i < dataCount; i++)
 			{
 				obj = (SaveDataObject) dataList.elementAt(i);
 				if((obj != null))
 				{
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
 				InsLogger.writeLog("Record Name : " + dataName, LogLevel.INFO);
 				InsLogger.writeLog("Previous Version : " + dataVersion, LogLevel.INFO);
 				InsLogger.writeLog("Current Version : " + this.recordVersion, LogLevel.INFO);
 				InsLogger.writeLog("Item Count : " + this.dataCount, LogLevel.INFO);
 				
 				for(int i = 0; i < dataCount; i++){
 					SaveDataObject obj = new SaveDataObject(dataStream);
 					this.dataList.addElement(obj);
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
 		this.dataList.removeAllElements();
 	}
 	
 	/**
 	 * Add new Data to List. <br>
 	 * If there is already other data with same name, change its value instead.
 	 */
 	public void addData(SaveDataObject obj){
 		if(this.isDataExist(obj.name)){
 			this.getData(obj.name).setData(obj);
 		} else {
 			this.dataList.addElement(obj);
 		}
 	}
 	
 	public boolean isDataExist(String Name){
 		return (this.searchDataId(Name) != -1);
 	}
 	
 	protected int searchDataId(String Name){
 		int foundIdx = -1;
 		for(int i = 0; i < this.dataList.size(); i++)
 		{
 			SaveDataObject obj = (SaveDataObject) this.dataList.elementAt(i);
 			if(obj.name.equals(Name)){
 				foundIdx = i;
 				break;
 			}
 		}
 		return foundIdx;
 	}
 	
 	public SaveDataObject getData(String Name){
 		int idx = this.searchDataId(Name);
 		if(idx != -1){
 			return (SaveDataObject) this.dataList.elementAt(idx);
 		}
 		return null;
 	}
 		
 }
