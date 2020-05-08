 package ua.sitronics.Mail;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 /**
  * Created by IntelliJ IDEA. User: Admin Date: 13.11.12 Time: 11:50
  */
 public class Archiver
 {
 	private              ArrayList<File> files     = new ArrayList<File>(1);
 	private static final String          separator = "/";
 	private int suppressLevel = 0;
 
 	public int getSuppressLevel()
 	{
 		return suppressLevel;
 	}
 
 	public void setSuppressLevel(int suppressLevel)
 	{
 		this.suppressLevel = suppressLevel;
 	}
 
 	public Archiver(ArrayList<File> files)
 	{
 		this.files = files;
 	}
 
 	public Archiver(File file)
 	{
 		files.add(file);
 
 	}
 
 	public ArrayList<File> getFiles()
 	{
 		return files;
 	}
 
 	public void setFiles(ArrayList<File> files)
 	{
 		this.files = files;
 	}
 
 	public void create(File pathTo, int level) throws IOException
 	{
 		ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(pathTo));
 		zip.setLevel(level);
		create(zip);
 	}
 	
 	public void create(File pathTo) throws IOException
 	{
 		create(pathTo, 0);
 	}
 
 	private void create(ZipOutputStream zip) throws IOException
 	{
 		try
 		{
 			for (File file : files)
 			{
 				//пропускаем неподходящие нам файлы
 				if (file == null || !file.exists() || !file.canRead())
 				{
 					continue;
 				}
 				addZipEntry(file, zip, "");
 			}
 		}
 		catch (IOException e)
 		{
 			throw new IOException("An error while creating archive: " + e.getLocalizedMessage());
 		}
 		finally
 		{
 			zip.flush();
 			zip.close();
 		}
 
 	}
 
 	private void addZipEntry(File file, ZipOutputStream zipStream, String prefix) throws IOException
 	{
 		if (file.isDirectory())
 		{
 			String[] content = file.list();
 			for (String subFile : content)
 			{
 				addZipEntry(new File(file.getPath() + separator + subFile), zipStream,
 						prefix + file.getName() + separator);
 			}
 			if (content.length == 0)
 			{
 				zipStream.putNextEntry(new ZipEntry(prefix + file.getName() + separator));
 				zipStream.closeEntry();
 			}
 		}
 		else
 		{
 			zipStream.putNextEntry(new ZipEntry(prefix + file.getName()));
 
 			FileInputStream fis = new FileInputStream(file.getPath());
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len = fis.read(buf)) > 0)
 			{
 				zipStream.write(buf, 0, len);
 			}
 			zipStream.closeEntry();
 			fis.close();
 		}
 	}
 }
