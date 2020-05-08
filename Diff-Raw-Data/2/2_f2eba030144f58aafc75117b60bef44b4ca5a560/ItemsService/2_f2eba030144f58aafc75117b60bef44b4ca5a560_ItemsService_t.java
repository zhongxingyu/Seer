 package com.xingcloud.tasks.services;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import android.app.Activity;
 import android.content.SharedPreferences;
 import android.content.res.AssetManager;
 
 import com.xingcloud.core.Config;
 import com.xingcloud.core.FileHelper;
 import com.xingcloud.core.XingCloud;
 import com.xingcloud.event.IEventListener;
 import com.xingcloud.event.XingCloudEvent;
 import com.xingcloud.items.ItemsParser;
 import com.xingcloud.utils.DbAssitant;
 
 public class ItemsService extends FileService {
 
 	public ItemsService(IEventListener onSuccess,IEventListener onFail) {
 		super(onSuccess, onFail);
 		this.type = ITEMS;
 		this.command = Config.ITEMSDB_SERVICE;
 	}
 
 	public ItemsService() {
 		super();
 		this.type = ITEMS;
 		this.command = Config.ITEMSDB_SERVICE;
 	}
 
 	protected void handleSuccess(XingCloudEvent evt)
 	{
 		delFiles();
 		super.handleSuccess(evt);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.xingcloud.tasks.services.Service#applyService(java.lang.Object)
 	 */
 	public void applyService(Object content)
 	{
 		DbAssitant.instance().updateDatabase();
 		ItemsParser.parse(content.toString());
 	}
 	
 	protected int checkDB()
 	{
 		SharedPreferences settings = XingCloud.instance().getActivity().getSharedPreferences("XingCloudSDK", Activity.MODE_PRIVATE);
 		return settings.getInt("dbcache", -1);
 	}
 
 	public boolean sendable()
 	{
 		if(XingCloud.enableCache)
 		{
 			String dbFile = Service.ITEMS+md5+XingCloud.instance().appVersionCode+".db";
 			String fileName=type+"?"+md5+XingCloud.instance().appVersionCode;
 			int checkdb = checkDB();
 			
			if((FileHelper.exist(fileName) || FileHelper.exist(dbFile)) && checkdb!=1)
 			{
 				return false;
 			}
 			else if (checkdb==1 || !FileHelper.exist(dbFile)) 
 			{
 				AssetManager assetManager = XingCloud.instance().getContext().getAssets();  
 		        try {
 		        	InputStream is = assetManager.open("xingcloud/language/"+Config.languageType()+"/"+dbFile);
 		        	byte[] content = readFile(is);
 		        	FileHelper.save(dbFile, content);
 					return false;
 		        } catch (IOException e) {
 		        	
 		        	if(checkdb==1)
 		        	{
 		        		FileHelper.delete(fileName);
 		        	}
 		        	
 		        	//如果存在xml缓存，则也不从网络加载
 		        	if(!super.sendable())
 		        	{
 		        		return false;
 		        	}
 		        	
 		        	checkOldCache();
 					return true;
 				}
 			}
 			checkOldCache();
 			return true;
 		}
 		else
 		{
 			checkOldCache();
 			return true;
 		}
 	}
 
 	private byte[] readFile(InputStream inputStream) {  
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();  
         byte buf[] = new byte[1024];  
         int len;  
         try {  
             while ((len = inputStream.read(buf)) != -1) {  
                 outputStream.write(buf, 0, len);  
             }  
             outputStream.close();  
             inputStream.close();  
         } catch (IOException e) {  
         }  
         return outputStream.toByteArray();  
     }
 }
