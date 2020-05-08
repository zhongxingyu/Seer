 package net.soartex.patcher.helpers;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 public class UnZip
 {
 	List<String> fileList; 
 	/**
 	 * Unzip it
 	 * @param zipFile input zip file
 	 * @param output zip file output folder
 	 */
 	public static void unZipIt(String zipFile, String outputFolder){
 
 		byte[] buffer = new byte[1024];
 
 		try{
 
 			//create output directory is not exists
 			File folder = new File(outputFolder);
 			if(!folder.exists()){
 				folder.mkdir();
 			}
 
 			//get the zip file content
 			ZipInputStream zis = 
 					new ZipInputStream(new FileInputStream(zipFile));
 			//get the zipped file list entry
 			ZipEntry ze = zis.getNextEntry();
 
 			while(ze!=null){
 
 				String fileName = ze.getName();
 				File newFile = new File(outputFolder + File.separator + fileName);
 
 				//create all non exists folders
 				//else you will hit FileNotFoundException for compressed folder
 				if(ze.isDirectory()) 
 				{
 					new File(newFile.getParent()).mkdirs();
 				}
 				else
 				{
					try{
						FileOutputStream fos = null;
						new File(newFile.getParent()).mkdirs();
						
						//this line usally errors
						fos = new FileOutputStream(newFile);             
						int len;
						while ((len = zis.read(buffer)) > 0){
							fos.write(buffer, 0, len);
						}
						fos.close();   
					}
					catch(Exception e){
						e.printStackTrace(); 
 					}
 				}
 				ze = zis.getNextEntry();
 			}
 
 			zis.closeEntry();
 			zis.close();
 			System.out.println(" *Done*");
 
 		}catch(IOException ex){
 			ex.printStackTrace(); 
 		}
 	}    
 }
