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
 			File source = new File(sourceF+"\\"+s);
 			File dest = new File(destF+"\\"+source.getName());
 			if(source.isDirectory())
 			{
 				moveAllFiles(source.getAbsolutePath(), dest.getAbsolutePath());
 			}
 			new UI().toUser("Moving " + source + " to " + dest + " |(result):" + String.valueOf(source.renameTo(dest)));
 		}
 	}
 	
 	public void removeAllEncryptedFiles(String path)
 	{
 		File f = new File(path);
 		if(f.isFile())
 		{	
 			new UI().toUser("Deleting "+ f.getAbsolutePath() + "|(result):"+f.delete());
 			return;
 		}
 		for(String s : f.list())
 			{
				File del = new File(s);
 				if(del.isFile())
 				{
 					new UI().toUser("Deleting "+ del.getAbsolutePath() + "|(result):"+del.delete());
 				}
 				else if(del.isDirectory())
 				{
 					removeAllEncryptedFiles(path+"\\"+s);
 					new UI().toUser("Deleting "+ del.getAbsolutePath() + "|(result):"+del.delete());
 				}
 				else if(!del.exists())
 				{
 					new UI().toUser("Attempting to delete a non-existant file " + del.getAbsolutePath());
 				}
 			}
 	}
 
 }
