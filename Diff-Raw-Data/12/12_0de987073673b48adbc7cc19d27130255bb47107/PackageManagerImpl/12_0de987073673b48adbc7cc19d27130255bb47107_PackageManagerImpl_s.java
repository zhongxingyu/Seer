 /**
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.contrib.metadatarepository.service.impl;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 
 import org.openmrs.contrib.metadatarepository.dao.PackageDao;
 import org.openmrs.contrib.metadatarepository.model.MetadataPackage;
 import org.openmrs.contrib.metadatarepository.service.APIException;
 import org.openmrs.contrib.metadatarepository.service.PackageManager;
 import org.openmrs.contrib.metadatarepository.service.impl.GenericManagerImpl;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Service;
 
 
 /**
  * Implementation of PackageManager interface.
  */
 @Service("packageManager")
 public class PackageManagerImpl extends GenericManagerImpl<MetadataPackage, Long> implements PackageManager {
 
     PackageDao packageDao;
     
 	@Autowired
 	public void setPackageDao(PackageDao packageDao) {
 		this.dao=packageDao;
 		this.packageDao=packageDao;
 	}
 	 
 	 @Value("${packages.storage.dir}")
 	 String packagesStorageDir;
 	 
 	public MetadataPackage savePackage(MetadataPackage metadataPackage) {
 		
 		boolean saveFile = (metadataPackage.getId() == null);
 		MetadataPackage metadatapackage = super.save(metadataPackage);
 		String filename =metadatapackage.getId().toString()+".zip";
 		if(saveFile){
 		try{
 		    saveFile(filename,metadataPackage.getFile());
 		}catch(IOException e){
 		    remove(metadataPackage.getId());
	    	throw new APIException("Failed to save the package on the disk");
 		 }
 		}
 		return metadatapackage;	
 	}
 	
 	protected void saveFile(final String filename,byte[] file) throws IOException{
 	
         // Create the directory if it doesn't exist
         File dirPath = new File(packagesStorageDir);
 
         if (!dirPath.exists()) {
             dirPath.mkdir();
         }
        
        
          if(log.isDebugEnabled())
         	 log.debug("Saving file to "+dirPath.toString());
                
         //write the file to the file specified
         File packagedata = new File(dirPath,filename);
         FileOutputStream bos =null;
        try{
         bos = new FileOutputStream(packagedata);
         bos.write(file);
         bos.close();
        }catch(IOException e){
    	   throw new APIException("error writing a file");
        }
        finally {
     	   try{
       	 if (bos != null) {
       	   bos.close();
       	  }
       	 }catch (IOException e) {
       	   //close quietly
       		 log.error(e);
       	  }
       }
        
 	}
     	 
         
     
 	
 	
 		
 }
