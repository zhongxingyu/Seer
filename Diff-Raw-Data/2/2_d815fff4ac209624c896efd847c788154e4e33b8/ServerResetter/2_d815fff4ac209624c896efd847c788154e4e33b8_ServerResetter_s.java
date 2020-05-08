 package com.barroncraft.sce;
 
 import java.io.File;
 import java.io.IOException;
 
 public class ServerResetter 
 {
     public static final String FileName = "reset-required";
 
     public static boolean getResetFlag()
     {
         return new File(FileName).exists();
     }
 
 	public static boolean enableResetFlag()
 	{
         return resetFlag(true);
 	}
 
     public static boolean clearResetFlag()
     {
         return resetFlag(false);
     }
 
    public static boolean setResetFlag(boolean reset)
     {
 		File resetFile = new File(FileName);
         if (resetFile.exists() == reset)
             return true;
 
 		try
 		{
             if (reset)
                 return resetFile.createNewFile();
             else
                 return resetFile.delete();
 
 		} 
 		catch(IOException e)
 		{
 			return false;
 		}
 
     }
 }
