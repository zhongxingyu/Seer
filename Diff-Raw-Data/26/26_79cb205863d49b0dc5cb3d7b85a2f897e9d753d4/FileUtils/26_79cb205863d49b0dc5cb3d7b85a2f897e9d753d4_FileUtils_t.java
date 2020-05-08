 /**
  * @author kobayashi
  * @description 一切對Android儲存裝置的存取都從這個類別呼叫
  */
 package tw.edu.chu.csie.e_learning.util;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 
 import tw.edu.chu.csie.e_learning.config.Config;
 import android.content.Context;
 import android.os.*;
 
 public class FileUtils 
 {
 	private File BasicSDPath;  //SD卡的根目錄（會依照Android版本不同而有所變化）
 	private Context BasicInternalPath;		//內部儲存裝置的根目錄(預設是/data/data/[package.name]/files/)
 	
 	public FileUtils() 
 	{
 		BasicSDPath = Environment.getExternalStorageDirectory();
 	}
 	
 	/**
 	 * @title			isSDCardInsert
 	 * @description	偵測這個裝置有沒有插入記憶卡
 	 * @param			None
 	 * @return			Boolean
 	 */
 	public boolean isSDCardInsert() 
 	{
 		if(!BasicSDPath.equals(Environment.MEDIA_REMOVED))
 		{
 			return true;  //SD卡已經插上去了
 		}
 		else return false;	//沒插入SD卡
 	}
 	
 	/**
 	 * @title			getSDPath
 	 * @description	取得在SD卡上的教材路徑
 	 * @param			None
 	 * @return 		學習教材在SD卡上的路徑
 	 */
 	public String getPath()
 	{
 		if(isSDCardInsert())
 		{
			File path = new File(BasicSDPath+"/"+Config.APP_DIRECTORY+Config.MATERIAL_DIRECTORY);
 			if(!path.exists()) 
 			{
 				path.mkdir();
 				return path.getAbsolutePath();
 			}
			else return BasicSDPath+"/"+Config.APP_DIRECTORY+Config.MATERIAL_DIRECTORY;
 		}
 		else
 		{
			File path = new File(BasicInternalPath+"/"+Config.MATERIAL_DIRECTORY);
 			if(!path.exists())
 			{
 				path.mkdir();
				return BasicInternalPath+"/"+Config.MATERIAL_DIRECTORY;
 			}
			return BasicInternalPath+"/"+Config.MATERIAL_DIRECTORY;
 		}
 	}
 	
 	/**
 	 * @tile	saveFile
 	 * @description	下載檔案時存檔用
 	 * @param 			path
 	 * @return			None
 	 * @throws 		IOException 
 	 */
 	public void saveFile(String path,InputStream is) throws IOException
 	{
 		File savePath = new File(path);
 		if(!savePath.exists()) //如果傳進來的資料夾路徑沒有這個資料夾
 		{
 			savePath.mkdir();   //建立資料夾
 			//將檔案存到該路徑底下
 			output(savePath,is);
 		}
 		else output(savePath,is); //將檔案存到該路徑底下		
 	}
 	
 	/**
 	 * @tile	output
 	 * @description	存檔工具函式
 	 * @param			path,input
 	 * @return			None
 	 * @throws 		IOException 
 	 */
 	private void output(File path,InputStream input) throws IOException
 	{
 		FileWriter write = new FileWriter(path);
 		int str1 = 0;;
 		while((str1=input.read()) != -1)
 		{
 			write.write(str1);
 		}
 		write.close();
 		str1 = 0;
 	}
 }
