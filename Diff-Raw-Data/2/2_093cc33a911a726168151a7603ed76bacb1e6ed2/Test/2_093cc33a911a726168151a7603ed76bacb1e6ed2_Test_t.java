 package cleaner;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.file.Files;
 import java.nio.file.attribute.BasicFileAttributes;
 import java.util.ArrayList;
 
 public class Test
 {
 	public static final String desktop = SaveNLoad.getDesktop();
 	
 	public static void main(String[] args)
 	{
		printAttributes(new File("Lists\\ExampleBlacklist.txt"));
 	}
 	
 	public static void killProcess(String process)
 	{
 		try
 		{
 			Runtime.getRuntime().exec("taskkill /f /im "+process+".exe");
 		} catch (Exception e){}
 	}
 	
 	public static void printAttributes(File file)
 	{
 		if(file.exists())
 		{
 			try
 			{
 				BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
 				
 				System.out.println("creationTime: " + attr.creationTime());
 				System.out.println("lastAccessTime: " + attr.lastAccessTime());
 				System.out.println("lastModifiedTime: " + attr.lastModifiedTime());
 
 				System.out.println("isDirectory: " + attr.isDirectory());
 				System.out.println("isOther: " + attr.isOther());
 				System.out.println("isRegularFile: " + attr.isRegularFile());
 				System.out.println("isSymbolicLink: " + attr.isSymbolicLink());
 				System.out.println("size: " + attr.size());
 			} catch (IOException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 }
