 package com.vloxlands.util;
 
 import java.io.File;
 
 import com.vloxlands.settings.CFG;
 
 public class MediaAssistant
 {
 	public static boolean needMediaUpdate(String folder)
 	{
 		try
 		{
			new File(CFG.DIR, folder).mkdirs();
 			boolean need = !Assistant.getFolderChecksum(new File(CFG.DIR, folder)).equals(CFG.class.getField(folder.toUpperCase() + "_CS").get(null));
 			return need;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			return true;
 		}
 	}
 }
