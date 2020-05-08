 package common.FileManager;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 
 import common.Download;
 import common.Main;
 
 
 public class FileManager {
 public static String dir = getDir();
 public static String modsDir = dir+"/mods";
 public static String updaterDir = dir + "/Updater";
 public static String modTemp = updaterDir+"/mods";
 public static String updateURl = "https://dl.dropbox.com/u/70622753/updater/ModList.list.txt";
 
 public static List<File> modsStored = new ArrayList<File>();
 public static List<File> jarMods = new ArrayList<File>();
 public static List<File> installedMods = new ArrayList<File>();
 public static File mc = null;
 public static File currentMc = null;
 public static List<String> errors = new ArrayList<String>();
 public static boolean rootFileCheck()
 {
 	
 	if(!folderCreator(dir, "mods")){errors.add("Missing mods folder");return false;}
 	else
 	{
 		installedMods = getFileList(modsDir,".zip") != null ? getFileList(modsDir,".zip") : new ArrayList<File>();
 	}
 	if(!folderCreator(dir, "Updater")){errors.add("Missing Update folder");return false;}
 	else
 	{
 		//checks for updates/mods folder which is used to temp store mods
 		if(!folderCreator(dir+"/Updater/", "mods")){errors.add("Missing update/mods folder");}
 		else
 		{
 			modsStored = getFileList(modTemp,".zip") != null ? getFileList(modTemp,".zip") : new ArrayList<File>();
 		}
 		//checks for updates/jar folder which stores minecraft.jar backup
 		if(!folderCreator(updaterDir, "jars")){errors.add("Missing Update/jars folder");}
 		else
 		{
			File file = new File(updaterDir+"jars/minecraft.jar");
 			if(!file.exists())
 			{
 				errors.add("Missing minecraft.jar");
 				//TODO add code to copy or download minecraft.jar
 			}
 			else
 			{
 				mc = file;
 			}
 			
 		}
 		//checks for update/jarMods folder which stores mod that are injected into the client
 		if(!folderCreator(dir+"/Updater/", "jarMods")){errors.add("Missing Update/jarMods folder");}
 		else
 		{
 			jarMods = getFileList(updaterDir+"jarMods",".zip");
 		}
 	}
 	if(!folderCreator(dir, "bin")){errors.add("Missing bin folder");return false;}
 	else
 	{
 		File minecraft = new File(dir+"/bin/minecraft.jar");
 		if(!minecraft.exists())
 		{
 			JOptionPane.showMessageDialog(null, "Missing minecraft.jar.\n Run minecraft once");
 		}
 		else
 		{
 			currentMc = minecraft;
 			File met = new File(minecraft.getAbsolutePath()+"/META-INF/");
 			if(met.exists() && mc == null)
 			{
 				try {
 					copyFile(minecraft, new File(updaterDir+"/jars"));
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
	return false;	
 }
 public static boolean updateList()
 {
 	//Download mod list
 	File modList = new File(FileManager.updaterDir+"/ModList.list");
 	File oldList = new File(FileManager.updaterDir+"/ModList.list.backup");
 	if(modList.exists())
 	{
 		System.out.print("Backing up mod List");
 		try {
 			if(oldList.exists())
 			{
 			Boolean delc = FileManager.deleteFile(FileManager.updaterDir, "/ModList.list.backup", false);
 			}
 			Boolean cc = FileManager.copyFile(modList,new File(modList+".backup"));
 			oldList = modList;
 			if(cc)
 			{
 				System.out.print("Downloading mod List");
 				File NmodList = Download.downloadFromUrl(updateURl, FileManager.updaterDir, "ModList.list");
 				if(NmodList != null)
 				{
 					modList = NmodList;
 					System.out.print("Downloaded new list");
 					return true;
 				}
 				else
 				{
 					System.out.print("Failed to get list");
 					System.out.print("restoring old list");
 					oldList.renameTo(new File(FileManager.updaterDir+"/ModList.list"));
 					modList = oldList;
 					
 				}
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	return false;
 }
 /**
  * Used to see if a fileExists
  * @param loc - director location
  * @param file - file too look fire
  * @return true if it is found
  */
 public static File fileExist(String loc,String file)
 {
 	if(loc == null)	{loc = dir;}
 	if(file == null){return null;}
 	if(loc != null && file != null)
 	{
 		String sFile = loc + file;
 		File aFile = new File(sFile);
 		if(aFile.exists())
 		{
 			return aFile;
 		}
 	}
 	return null;
 }
 /**
  * Used to manage file deletion with hook for backing up the file
  * @param loc - location to look for file
  * @param file - file too look for and delete
  * @param backup should backup
  * @return true if the file was deleted
  */
 public static boolean deleteFile(String loc,String file,boolean backup)
 {
 	File del = fileExist(loc,file);
 	if(del != null)
 	{
 		if(!backup)
 		{
 			return del.delete();
 		}
 		else
 		{
 			File bac = fileExist(modTemp,file);
 			if(bac == null)
 			{
 				try {
 					boolean c = copyFile(del,bac);
 					if(c)
 					{
 						return del.delete();	
 					}
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	return false;
 }
 /**
  * Copies a file from one spot to another
  * @param sourceFile
  * @param destFile
  * @throws IOException
  */
 public static boolean copyFile(File sourceFile, File destFile) throws IOException 
 {
 	if (!sourceFile.exists()) {
 	        return false;
 	}
 	if (!destFile.exists()) {
 	        destFile.createNewFile();
 	}
 	FileChannel source = null;
 	FileChannel destination = null;
 	source = new FileInputStream(sourceFile).getChannel();
 	destination = new FileOutputStream(destFile).getChannel();
 	if (destination != null && source != null) 
 	{
 	        destination.transferFrom(source, 0, source.size());
 	        source.close();
 	        destination.close();
 	        return true;
 	}
 	return false;
 
 
 }
 public static List<File> getFileList(String loc,String fileEnd)
 {
 	if(fileEnd == null)
 	{
 		fileEnd = "";
 	}
 	if(loc == null)
 	{
 		loc = dir;
 	}
 	// Directory path here 
 	  String file;
 	  File folder = new File(loc);
 	  File[] listOfFiles = folder.listFiles(); 
 	  List<File> files = new ArrayList<File>();
 	  if(listOfFiles != null)
 	  {
 		  for (int i = 0; i < listOfFiles.length; i++) 
 		  {
 		 
 			   if (listOfFiles[i].isFile()) 
 			   {
 			   file = listOfFiles[i].getName();
 			       if (file.endsWith(fileEnd)|| fileEnd == "")
 			       {
 			          files.add(listOfFiles[i]);
 			       }
 			   }
 		  }
 	  }
 	  return files;
 }
 
 public static String getDir()
 {
 	final File loader = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
 	return loader.getPath();
 }
 /**
  * Creates folders nothing more nothing less
  * @param dest
  * @param name
  * @return true if folder is created
  */
 public static boolean folderCreator(String dest, String name)
 {
 	 File fileMain = new File(dest +"/"+ name);
 	 if(!fileMain.exists())
 	 {
 		 try
 		 {
 			 if(fileMain.mkdir())
 			 {
 				 System.out.println("Folder "+name+" Created");
 				 return true;
 			 }
 		 }
 		 catch(Exception e)
 		 {
 			 e.printStackTrace();
 		 }
 	 }	
 	else
 	{
 		return true;
 	}
 	 System.out.println("Folder creation failed for " + name);
 	return false;
 }
 
 }
