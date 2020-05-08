 package freeencryptrepo.cipher;
 
 import java.io.File;
 
 import freeencryptrepo.UI.UI;
 
 //can move the source files up, then once encrypted and uploaded, encrypted files will be deleted and sources can be moved back
 
 public class FileRelocator {
 	
 	public void moveAllFiles(String sourceFolder, String destinationFolder)
 	{
 		File sourceF = new File(sourceFolder);
 		File destF = new File(destinationFolder);
 		
 		if(sourceF.isFile() || destF.isFile() || !sourceF.canRead() || !sourceF.canWrite() || !destF.canRead() || !destF.canWrite())
 			return;
 		for(String s : sourceF.list())
 		{
 			File source = new File(s);
 			File dest = new File(destF+"\\"+source.getName());
 			if(source.isDirectory())
 			{
 				moveAllFiles(source.getAbsolutePath(), dest.getAbsolutePath());
 			}
			new UI().toUser("Moving " + source + " " + String.valueOf(source.renameTo(dest)));
 		}
 	}
 	
 	public void removeAllEncryptedFiles(String path)
 	{
 		File f = new File(path);
 		for(String s : f.list())
 		{
 			if(new File(s).isFile())
 			{
 				new File(s).delete();
 			}
 			else
 			{
 				removeAllEncryptedFiles(path+"\\"+s);
 				new File(s).delete();
 			}
 		}
 	}
 
 }
