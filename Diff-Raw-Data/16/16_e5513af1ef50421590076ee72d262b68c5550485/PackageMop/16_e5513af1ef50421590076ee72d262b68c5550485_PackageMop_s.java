 package org.exoplatform.management.packaging.task;
 
<<<<<<< HEAD
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
import java.util.ArrayList;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;

import org.gatein.management.api.operation.model.ExportResourceModel;
=======
>>>>>>> manage options
 import org.gatein.management.api.operation.ResultHandler;
 
 /**
  * Created with IntelliJ IDEA. User: gregorysebert Date: 11/07/12 Time: 15:38 To
  * change this template use File | Settings | File Templates.
  */
 public class PackageMop {
 
 	public PackageMop(String zipPath, ResultHandler resultHandler) {
 		try {
 			String tempFolder = System.getProperty("java.io.tmpdir");
 			File temp = new File(tempFolder + "/exploded-extension");
 			temp.mkdir();

<<<<<<< HEAD
 			extractZip(zipPath, temp);
 		} catch (FileNotFoundException ex) {
 			ex.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
 	private void extractZip(String zipPath, File destFolder)
 			throws FileNotFoundException, IOException {
 		ZipInputStream zipInputStream;
 		ZipEntry zipEntry;
 		byte[] buffer = new byte[2048];
 		zipInputStream = new ZipInputStream(new FileInputStream(zipPath));
 		zipEntry = zipInputStream.getNextEntry();
 		while (zipEntry != null) {
 			String fileName = zipEntry.getName();
 			if (zipEntry.isDirectory()) {
 				File newDir = new File(destFolder.getPath() + "/" + fileName);
 				newDir.mkdir();
 			} else {
 				FileOutputStream fileoutputstream = new FileOutputStream(
 						destFolder.getPath() + "/" + fileName);
 				int n;
 
 				while ((n = zipInputStream.read(buffer, 0, 2048)) > -1) {
 					fileoutputstream.write(buffer, 0, n);
 				}
 
 				fileoutputstream.close();
 			}
 			zipEntry = zipInputStream.getNextEntry();
 		}
 
 		zipInputStream.close();
 	}
=======
    }
>>>>>>> manage options
 
 }
