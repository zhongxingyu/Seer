 package freeencryptrepo.cipher;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.FileWriter;
 import java.security.GeneralSecurityException;
 import java.util.Vector;
 
 public class FileEncrypter {
 	private Vector<String> salts = new Vector<String>();
 	private Vector<String> source_filenames = new Vector<String>();
 	
 	public boolean encryptFilesinFolder(String key, String folderpath)
 	{
 		File directory = new File(folderpath);
 		File encryptedFolder = new File(directory.getParent()+"\\"+directory.getName()+"FER");
 		if(!encryptedFolder.exists())
 		{
 			if(!encryptedFolder.mkdir())
 				// if the destination path is unable to be created, then don't worry about encrypting the files when there is no place to put them
 				return false;
 		}
 		// move all the source files to the FER directory
 		new FileRelocator().moveAllFiles(directory.getAbsolutePath(), encryptedFolder.getAbsolutePath());
 		EncryptDirectory(folderpath, encryptedFolder.getAbsolutePath(), key);
 		return true;
 	}
 	
 	protected String encryptFileName(String _key, String salt, File f)
 	{
 		try {
 			return new AES().Encrypt(_key, salt, f.getName());
 		} catch (GeneralSecurityException e) {
 			e.printStackTrace();
 		}
 		return "";
 	}
 	protected String encryptFileContents(String _key, String salt, File f)
 	{
 		try {
 			return new AES().Encrypt(_key, salt, FReader(f));
 		} catch (GeneralSecurityException e) {
 			e.printStackTrace();
 		}
 		return "";
 	}
 	
 	protected void EncryptDirectory(String directoryPath, String encryptedFolderPath, String key)
 	{
 		File dir = new File(directoryPath);
 		if(dir.isFile())
 			return;
 		for(String filename : dir.list())
 		{
 			
			File current = new File(dir.getAbsolutePath()+"\\"+filename);
 			if(current.isFile())
 			{			
 				String salt = genPRSalt(10);
 				salts.add(salt);
 				source_filenames.add(current.getName());
 				//encrypt the contents and create a file with the encrypted contents
 				FWriter(new File(encryptedFolderPath+"\\"+encryptFileName(key, salt, current)), encryptFileContents(key, genPRSalt(10), current));
 			}
 			else
 			{
 				EncryptDirectory(current.getAbsolutePath(), encryptedFolderPath, key);
 			}
 	
 		}
 	}
 	
 	public Vector<String> getSalts() {	return salts;  }
 	public Vector<String> getSourceFilenames() {	return source_filenames;  }
 	
 	protected String genPRSalt(int length)
 	{
 		StringBuffer sb = new StringBuffer();
 		for(int i = 0; i < length; i++)
 		{
			int rand = (int) (Math.random()*254+1);
 			sb.append((char)rand);
 		}
 		return sb.toString();
 	}
 	
 	/* @param 	File f - the file where the contents will be written
 	 * 			String contents - the contents of the file being written
 	 * 
 	 * @returns true or false based on whether the file was written or not
 	 */
 	public boolean FWriter(File f, String contents)
 	{
 		PrintWriter pw;
 		
 		try{ pw = new PrintWriter(new BufferedWriter(new FileWriter(f, true)));
 			pw.append(contents);
 			pw.close();
 			return true;
 		}
 		catch(IOException ioe)
 		{
 			return false;
 		}
 	}
 	/*
 	 * @param File f - the file to be read and the contents returned
 	 * 
 	 * @returns the contents of the file being read or the reason the read failed (an exception occured)
 	 */
 	public String FReader(File f)
 	{
 		BufferedReader br;
 		try{
 			br = new BufferedReader(new FileReader(f));
 			String line;
 			StringBuilder sb = new StringBuilder();
 			while((line = br.readLine())!=null)
 			{
 				sb.append(line);
 			}
 			return sb.toString();
 			
 		}
 		catch(FileNotFoundException fnfe)
 		{
 			fnfe.printStackTrace();
 			return "FileNotFoundException";
 		}
 		catch (IOException e) {
 			e.printStackTrace();
 			return "IOException";
 		}
 		
 	}
 	
 }
