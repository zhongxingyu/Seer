 package bc.utils.filesystem;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import bc.utils.runtime.NativeLibLoader;
 
 public class FileUtils 
 {
 	private static final String LIB_NAME_X64 = "fileutilsx64";
 	private static final String LIB_NAME_X86 = "fileutils";
 
 	private static native void deleteToRecycleBin(String path);
 	
 	static
 	{
 		String arcDataModel = System.getProperty("sun.arch.data.model");
 		String libName = "64".equals(arcDataModel) ? LIB_NAME_X64 : LIB_NAME_X86;
 		NativeLibLoader.loadLibrary(libName);
 	}
 	
 	public static String makeRelativePath(File parent, File child)
 	{
 		String parentPath = parent.getAbsolutePath();
 		String childPath = child.getAbsolutePath();
 
 		int len = Math.min(parentPath.length(), childPath.length());
 		
 		int differenceCharIndex;
 		for (differenceCharIndex = 0; differenceCharIndex < len; differenceCharIndex++) 
 		{
 			if (parentPath.charAt(differenceCharIndex) != childPath.charAt(differenceCharIndex))
 				break;
 		}
 		
 		if (differenceCharIndex == 0)
 			return childPath;
 		
 		String childSubPath = childPath.substring(differenceCharIndex);
 		String parentSubPath = parentPath.substring(differenceCharIndex);
 		
 		int parentSubFoldersCount = 0;
 		for (int chrIndex = 0; chrIndex < parentSubPath.length(); chrIndex++) 
 		{
 			if (parentSubPath.charAt(chrIndex) == File.separatorChar)
 				parentSubFoldersCount++;
 		}
 		if (parentSubPath.length() > 0)
 			parentSubFoldersCount++;
 		
 		StringBuilder result = new StringBuilder();
 		for (int i = 0; i < parentSubFoldersCount; i++) 
 		{
 			result.append(".." + File.separatorChar);
 		}
 		if (childSubPath.startsWith(File.separator))
 			childSubPath = childSubPath.substring(1);
 		
 		result.append(childSubPath);
 		return result.toString();
 	}
 	
 	public static File[] listFiles(File file, final String... extensions)
 	{
 		return listFilesHelper(file, false, extensions);
 	}
 	
 	public static File[] listFilesAndDirectories(File file, final String... extensions)
 	{
 		return listFilesHelper(file, true, extensions);
 	}
 	
 	private static File[] listFilesHelper(File file, final boolean listDirectories, final String... extensions) 
 	{
 		return file.listFiles(new FileFilter() 
 		{
 			@Override
 			public boolean accept(File pathname) 
 			{
 				if (pathname.isDirectory())
 				{
 					return listDirectories;
 				}
 				
 				String fileExt = getFileExt(pathname);
 				for (String ext : extensions) 
 				{
 					if (fileExt.equalsIgnoreCase(ext))
 						return true;
 				}
 				return false;
 			}
 		});
 	}
 	
 	public static boolean copy(File src, File dst)
 	{
 		if (src.isDirectory())
 			throw new IllegalArgumentException("Can't copy directory: " + src);
 		
 		if (dst.isDirectory())
 			dst = new File(dst, src.getName());
 		
 		try 
 		{
 			FileInputStream fis = null;
 			FileOutputStream fos = null;
 			try 
 			{
 				fis = new FileInputStream(src);
 				fos = new FileOutputStream(dst);
 				byte[] buffer = new byte[4096];
 				int read;
 				while ((read = fis.read(buffer)) != -1)
 				{
 					fos.write(buffer, 0, read);
 				}
 			} 
 			finally 
 			{
 				if (fis != null) fis.close();
 				if (fos != null) fos.close();
 			}
 			return true;
 		} 
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public static void moveToTrash(String path)
 	{
 		moveToTrash(new File(path));
 	}
 	
 	public static void moveToTrash(File file)
 	{
 		if (file.exists())
 		{
 			deleteToRecycleBin(file.getAbsolutePath());
 		}
 	}
 	
 	public static void deleteFiles(File dir, final String[] extensions)
 	{
 		File[] files = listFiles(dir, extensions);
 		for (File file : files) 
 		{
 			file.delete();
 		}
 	}
 	
 	public static boolean delete(File file)
 	{
 		if (file.exists())
 		{
 			if (file.isDirectory())
 			{
 				File[] files = file.listFiles();
 				for (File childFile : files) 
 				{
 					boolean succeed = delete(childFile);
 					if (!succeed)
 					{
 						return false;
 					}
 				}
 			}
 			return file.delete();
 		}
 		return true;
 	}
 
 	public static List<String> readFile(File file) throws IOException
 	{
 		return readFile(file, null);
 	}
 	
 	public static List<String> readFile(File file, StringFilter filter) throws IOException
 	{
 		BufferedReader reader = null;
 		try
 		{
 			reader = new BufferedReader(new FileReader(file));
 			List<String> lines = new ArrayList<String>();
 
 			String line;
 			while ((line = reader.readLine()) != null)
 			{	
 				String filteredLine = filter != null ? filter.filter(line) : line;
 				if (filteredLine != null)
 				{
					lines.add(filteredLine);
 				}
 			}
 			return lines;
 		}
 		finally
 		{
 			if (reader != null)
 			{
 				reader.close();
 			}
 		}
 	}
 
 	
 	public static String getFileExt(File pathname) 
 	{
 		String filename = pathname.getName();
 		return getFileExt(filename);
 	}
 
 	public static String getFileExt(String filename) 
 	{
 		int dotIndex = filename.lastIndexOf('.');
 		
 		if (dotIndex == -1)
 			return "";
 		
 		return filename.substring(dotIndex);
 	}
 	
 	public static String getFilenameNoExt(File pathname)
 	{
 		return getFilenameNoExt(pathname.getName());
 	}
 
 	public static String getFilenameNoExt(String filename) 
 	{
 		int dotIndex = filename.lastIndexOf('.');
 		
 		if (dotIndex == -1)
 			return filename;
 		
 		return filename.substring(0, dotIndex);
 	}
 
 	public static File changeExt(File file, String ext)
 	{
 		String simpleName = getFilenameNoExt(file);
 		if (!ext.startsWith("."))
 			ext = "." + ext;		
 		return new File(file.getParentFile(), simpleName + ext);
 	}
 }
