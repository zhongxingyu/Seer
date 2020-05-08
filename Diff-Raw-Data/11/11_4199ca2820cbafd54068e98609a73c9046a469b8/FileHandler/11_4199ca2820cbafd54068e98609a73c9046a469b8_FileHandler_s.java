 package filing;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import jcifs.smb.NtlmPasswordAuthentication;
 import jcifs.smb.SmbException;
 import jcifs.smb.SmbFile;
 import jcifs.smb.SmbFileInputStream;
 import jcifs.smb.SmbFileOutputStream;
 import jcifs.smb.SmbFilenameFilter;
 
 import common.helper.FileHelper;
 
 import filing.interfaces.IFileHandler;
 
 
 public class FileHandler implements IFileHandler
 {
 	public FileHandler(NtlmPasswordAuthentication auth)
 	{
 		this.auth = auth;
 	}
 
 	private NtlmPasswordAuthentication auth;
 
 	@Override
 	public List<String> getFileNames(String folder)
 	{
 		List<String> files = new ArrayList<String>();
 		
 		if (FileHelper.isValidAndExists(folder, auth))
 			files.addAll(
 				tryGetFiles(folder));
 		
 		return files ;
 	}
 
 	private List<String> tryGetFiles(String folderPath)
 	{
 		List<String> files = new ArrayList<String>();
 		try
 		{
 			files = getFiles(folderPath);
 		} 
 		catch (MalformedURLException e) {} 
 		catch (SmbException e) {}
 						
 		return files;
 	}
 	
     static class FilenameFilter implements SmbFilenameFilter {
        public boolean accept( SmbFile dir, String name ) throws SmbException {
             return !(dir.isDirectory() && name.startsWith("_"));
         }
     }
     
 	private  List<String> getFiles(String folderPath)
 			throws MalformedURLException, SmbException
 	{
 		SmbFile folder = new SmbFile(folderPath, auth);
 		
 		FilenameFilter fileFilter = new FilenameFilter();
 
 		SmbFile[] contents = folder.listFiles(fileFilter);
 		List<String> files = new ArrayList<String>();
 		
 		for (SmbFile file : contents)
 			checkAndAddToList(file, files);
 		
 		return files;
 	}
 
 	private void checkAndAddToList(SmbFile file, List<String> files)
 	{
 		try
 		{
 			if (file.isFile() && fileExtensionIsInList(FileHelper.getFileExtension(file.getName())))
 				files.add(file.getPath());
 			else if (file.isDirectory())
 				files.addAll(
 					getFiles(file.getPath()));
 		} 
 		catch (SmbException e) {}
 		catch (MalformedURLException e) {}
 	}
 
 	private boolean fileExtensionIsInList(String fileExtension)
 	{
 		List<String> acceptedFileExtensions = new ArrayList<String>();
 		acceptedFileExtensions.add("avi");
 		acceptedFileExtensions.add("mp4");
 		
 		return acceptedFileExtensions.contains(fileExtension);
 	}
 
 	@Override
 	public boolean moveFile(String sourcePath, String destinationPath)
 	{
 		Boolean successful = false;
 		
 		try
 		{
 			SmbFile sourceFile = new SmbFile(sourcePath, auth);
 			SmbFile destinationFile = new SmbFile(destinationPath, auth);
 			
 			if(destinationFile.exists())
 			{
 				sourceFile.delete();
 				successful = true;
 			}
 			else
 			{
 				prepareDestination(destinationFile);
 				successful = tryMoveFile(sourceFile, destinationFile);
 			}
 		} 
 		catch (MalformedURLException | SmbException e) { e.printStackTrace(); }
 	    
 	    return successful;
 	}
 
 	private void prepareDestination(SmbFile destinationFile) throws SmbException, MalformedURLException
 	{
 	    if (!destinationFile.exists())
 		{
 	    	if (destinationFile.getPath().endsWith("/"))
 	    		destinationFile.mkdirs();
 	    	else
 	    		prepareDestination(new SmbFile(destinationFile.getParent(), this.auth));
 		}
 	}
 
 	private boolean tryMoveFile(SmbFile sourceFile, SmbFile destinationFile)
 	{
 		boolean successful = false;
 		try 
 	    {
 			SmbFileOutputStream outputStream = new SmbFileOutputStream(destinationFile);
 			SmbFileInputStream inputStream = new SmbFileInputStream(sourceFile);
 
 			byte[] buf = new byte[16 * 1024 * 1024];
 			int len;
 			
 			while ((len = inputStream.read(buf)) > 0) 
 			{
 				outputStream.write(buf, 0, len);
 			}
 
 			inputStream.close();
 			outputStream.close();
 			
 	    	successful = true;
			sourceFile.delete();
 	    } 
 	    catch (IOException e)
 	    {
 		    tryCleanUpAfterError(destinationFile);    	
 	    }
 	    		
 	    return successful;
 	}
 
 	private void tryCleanUpAfterError(SmbFile destinationFile)
 	{
 		try
 		{
 			if(destinationFile.exists()) 
 				destinationFile.delete();
 		} 
 		catch (SmbException e) {}
 	}
 }
