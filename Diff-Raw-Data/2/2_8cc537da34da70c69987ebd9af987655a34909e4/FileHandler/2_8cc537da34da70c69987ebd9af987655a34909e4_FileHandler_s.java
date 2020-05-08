 package adubbz.boppatcher;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import net.lingala.zip4j.core.ZipFile;
 import net.lingala.zip4j.exception.ZipException;
 import net.lingala.zip4j.model.ZipParameters;
 
 import org.apache.commons.io.FileUtils;
 
 public class FileHandler 
 {
 	public static void unzipBTW(File jarloc)
 	{
 		File btwLoc = new File(jarloc.getAbsolutePath() + File.separator + "btw");
 
 		if (btwLoc.exists())
 		{
 			Iterator iterator = FileUtils.iterateFiles(btwLoc, new String[] {"zip"}, false);
 			ArrayList<File> btwZips = new ArrayList();
 
 			while (iterator.hasNext())
 			{
 				File iteratedFile = (File)iterator.next();
 				
 				if (iteratedFile.getName().startsWith("BTWMod"))
 				{
 					btwZips.add(iteratedFile);
 				}
 			}
 			
 			if (btwZips.isEmpty())
 			{
 				System.out.println("No BTW zip found!");
 			}
 			else
 			{
 			    try 
 			    {
 					ZipFile zipFile = new ZipFile(btwZips.get(0));
 					
 					zipFile.extractAll(btwLoc.getAbsolutePath() + File.separator + "btwtemp");
 				} 
 			    catch (ZipException e) 
 			    {
 					e.printStackTrace();
 				}
 			}
 		}
 		else
 		{
 			System.out.println(jarloc.getAbsolutePath() + File.separator + "btw" + " does not exist!");
 		}
 	}
 	
 	public static void copyNonBTWEdits(File jarloc)
 	{
 		File mcJar = new File(jarloc.getAbsoluteFile() + File.separator + "nonbtwedits" + File.separator + "MINECRAFT-JAR");
 		File mcServerJar = new File(jarloc.getAbsoluteFile() + File.separator + "nonbtwedits" + File.separator + "MINECRAFT_SERVER-JAR");
 		
 		if (mcJar.exists())
 		{
 			try 
 			{
 				FileUtils.copyDirectory(mcJar, new File(jarloc.getAbsoluteFile() + File.separator + "btw" + File.separator + "btwtemp" + File.separator + "MINECRAFT-JAR"));
 			} 
 			catch (IOException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 		
 		if (mcServerJar.exists())
 		{
 			try 
 			{
				FileUtils.copyDirectory(mcJar, new File(jarloc.getAbsoluteFile() + File.separator + "btw" + File.separator + "btwtemp" + File.separator + "MINECRAFT_SERVER-JAR"));
 			} 
 			catch (IOException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public static void rezipBTW(File jarloc)
 	{
 		File patched = new File(jarloc.getAbsolutePath() + File.separator + "patchedzip");
 
 		if (patched.exists())
 		{
 			try 
 			{
 				FileUtils.deleteDirectory(patched);
 				patched.mkdir();
 			} 
 			catch (IOException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 		else
 		{
 			patched.mkdir();
 		}
 		
 		try 
 		{
 			ZipFile zipFile = new ZipFile(new File(patched.getAbsolutePath() + File.separator + "BTWMod-BOP-Patched.zip"));
 			ZipParameters zipparam = new ZipParameters();
 			
 			zipparam.setIncludeRootFolder(false);
 			
 			zipFile.createZipFileFromFolder(new File(jarloc.getAbsolutePath() + File.separator + "btw" + File.separator + "btwtemp"), zipparam, false, 0);
 		} 
 		catch (ZipException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public static void removeTempDir(File jarloc)
 	{
 		File temp = new File(jarloc.getAbsoluteFile() + File.separator + "btw" + File.separator + "btwtemp");
 
 		if (temp.exists())
 		{
 			try 
 			{
 				FileUtils.deleteDirectory(temp);
 			} 
 			catch (IOException e) 
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 }
