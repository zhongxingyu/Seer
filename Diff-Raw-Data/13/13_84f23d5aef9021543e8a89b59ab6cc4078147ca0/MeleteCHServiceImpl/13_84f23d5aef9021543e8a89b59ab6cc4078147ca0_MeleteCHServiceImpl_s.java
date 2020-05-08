 /**********************************************************************************
 *
 * $Header:
 *
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007 Foothill College, ETUDES Project 
 *   
 * Licensed under the Apache License, Version 2.0 (the "License"); you 
 * may not use this file except in compliance with the License. You may 
 * obtain a copy of the License at 
 *   
 * http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License. 
 *
 **********************************************************************************/
 
 package org.sakaiproject.component.app.melete;
 
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.net.URLDecoder;
 
 import org.sakaiproject.api.app.melete.MeleteCHService;
 import org.sakaiproject.api.app.melete.MeleteSecurityService;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.api.app.melete.exception.MeleteException;
 import org.sakaiproject.content.api.ContentCollectionEdit;
 import org.sakaiproject.content.api.ContentCollection;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.content.api.ContentResourceEdit;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.api.ContentEntity;
 import org.sakaiproject.content.cover.ContentTypeImageService;
 import org.sakaiproject.entity.api.Entity;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.entity.api.ResourcePropertiesEdit;
 import org.sakaiproject.exception.IdInvalidException;
 import org.sakaiproject.exception.IdLengthException;
 import org.sakaiproject.exception.IdUniquenessException;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.InUseException;
 import org.sakaiproject.exception.IdUsedException;
 import org.sakaiproject.exception.TypeException;
 import org.sakaiproject.exception.InconsistentException;
 import org.sakaiproject.exception.OverQuotaException;
 import org.sakaiproject.exception.PermissionException;
 import org.sakaiproject.exception.ServerOverloadException;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.entity.cover.EntityManager;
 import org.sakaiproject.entity.api.Reference;
 
 /*
  * Created on Sep 18, 2006 by rashmi
  *
  *  This is a basic class to do all content hosting service work through melete
  *  Rashmi - 12/8/06 - add checks for upload img being greater than 1Mb
  * Rashmi - 12/16/06 - fck editor upload image goes to melete collection and not resources.
  * Rashmi - 1/16/07 - remove word inserted comments
  * Mallika - 1/29/06 - Needed to add some new code to add url for embedded images
  * Rashmi - changed logic to get list of images and url
  * Rashmi - 2/12/07 - add alternate reference property at collection level too
  * Rashmi - 3/6/07 - revised display name of modules collection with no slashes 
  * Rashmi - 5/9/07 - revised iteration to get listoflinks
  * Mallika - 5/31/07 - added Validator
  * Mallika - 6/4/07 - Added two new methods (copyIntoFolder,getCollection)
  * Mallika - 6/4/07 - Added logic to check for resource in findlocal
  */
 public class MeleteCHServiceImpl implements MeleteCHService {
 	/** Dependency:  The logging service. */
 	 public Log logger = LogFactory.getLog(MeleteCHServiceImpl.class);
 	 private ContentHostingService contentservice;
 	 private static final int MAXIMUM_ATTEMPTS_FOR_UNIQUENESS = 100;
 	 private SectionDB sectiondb;
 	 /** Dependency: The Melete Security service. */
 	 private MeleteSecurityService meleteSecurityService;
 
 	 /** This string starts the references to resources in this service. */
 		static final String REFERENCE_ROOT = Entity.SEPARATOR+"meleteDocs";
 
 	 /**
 		 * Check if the current user has permission as author.
 		 * @return true if the current user has permission to perform this action, false if not.
 		 */
 	public boolean isUserAuthor()throws Exception{
 
 			try {
 				return meleteSecurityService.allowAuthor();
 			} catch (Exception e) {
 				throw e;
 			}
 	}
 	public boolean isUserStudent()throws Exception{
 
 		try {
 			return meleteSecurityService.allowStudent();
 		} catch (Exception e) {
 			throw e;
 		}
    }	
 
 	private String addMeleteRootCollection(String rootCollectionRef, String collectionName, String description)
 	{
 		try
 		{
 			 //	Check to see if meleteDocs exists
 			 ContentCollection collection = getContentservice().getCollection(rootCollectionRef);
 			return collection.getId();
 		}
 		catch (IdUnusedException e)
 		{
 			//if not, create it
 			if (logger.isDebugEnabled()) logger.debug("creating melete root collection "+rootCollectionRef);
 
 			try
 			{
 				ContentCollectionEdit edit = getContentservice().addCollection(rootCollectionRef);
 				ResourcePropertiesEdit props = edit.getPropertiesEdit();
 				props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, collectionName);
                 props.addProperty(ResourceProperties.PROP_DESCRIPTION, description);
                 props.addProperty(getContentservice().PROP_ALTERNATE_REFERENCE, REFERENCE_ROOT);
 				getContentservice().commitCollection(edit);
 				return edit.getId();
 			}
 			catch (Exception e2)
 			{
 				logger.warn("creating melete root collection: " + e2.toString());
 			}
    		}
 		catch (Exception e)
 		{
 			logger.warn("checking melete root collection: " + e.toString());
 		}
 		return null;
 	}
 	/*
      *
      */
 	 public String addCollectionToMeleteCollection(String meleteItemColl,String CollName)
      {
             try
     	    {
             	if (!isUserAuthor())
             		{
             		logger.info("User is not authorized to access meleteDocs collection");
             		}
        			//          setup a security advisor
             		meleteSecurityService.pushAdvisor();
             		try
 					{
 	    			// check if the root collection is available
 	    				String rootCollectionRef = Entity.SEPARATOR+"private"+ REFERENCE_ROOT+ Entity.SEPARATOR;
 	    				//Check to see if meleteDocs exists
 	    				String rootMeleteCollId = addMeleteRootCollection(rootCollectionRef, "meleteDocs", "root collection");
 	    				// now sub collection for course
 	    				String meleteItemDirCollId = rootMeleteCollId + meleteItemColl+ Entity.SEPARATOR;
 	    				String subMeleteCollId = addMeleteRootCollection(meleteItemDirCollId, CollName, "module collection");
 	    				return subMeleteCollId;
 					}
                  catch(Exception ex)
              	 {
                      logger.error("error while creating modules collection" + ex.toString());
                  }
     	    }
 		  catch (Throwable t)
 		  {
 			logger.warn("init(): ", t);
 		  }
 		  finally
 		  {
 			// clear the security advisor
 			meleteSecurityService.popAdvisor();
 		  }
 
            return null;
         }
 
     /*
      *
      */
 	 public String getCollectionId( String contentType,Integer modId )
     {
         String addToCollection ="";
         String collName ="";
         if(contentType.equals("typeEditor")){
 
         addToCollection=ToolManager.getCurrentPlacement().getContext()+Entity.SEPARATOR+"module_"+ modId;
          
 	    collName = "module_"+ modId;
         }
         else {
             if (ToolManager.getCurrentPlacement()!=null)
 	        addToCollection=ToolManager.getCurrentPlacement().getContext()+Entity.SEPARATOR+"uploads";
             else 
                 addToCollection=getMeleteSecurityService().getMeleteImportService().getDestinationContext()+Entity.SEPARATOR+"uploads";
         	
         	collName = "uploads";
         }
         //      check if collection exists otherwise create it
         String addCollId = addCollectionToMeleteCollection(addToCollection,collName);
         return addCollId;
     }
 	 
     /*
      *
      */
 	 public String getUploadCollectionId()
 	{
 		try{
             String uploadCollectionId;
             uploadCollectionId=ToolManager.getCurrentPlacement().getContext()+Entity.SEPARATOR+"uploads";
           
 	    String collName = "uploads";
 	    // check if collection exists
 	    //read meletDocs dir name from web.xml
         String uploadCollId = addCollectionToMeleteCollection(uploadCollectionId, collName);
         return uploadCollId;
 		}catch(Exception e)
 		{
 			logger.error("error accessing uploads directory");
 			return null;
 		}
     }
 	 
 	//Methods used by Migrate program  - beginning
 	 public String getCollectionId(String courseId, String contentType,Integer modId )
 	    {
 	        String addToCollection ="";
 	        String collName ="";
 	        if(contentType.equals("typeEditor")){
 				addToCollection=courseId+Entity.SEPARATOR+"module_"+ modId;
 				collName = "module_"+ modId;
 	        }
 	        else {
 	        	addToCollection=courseId+Entity.SEPARATOR+"uploads";
 	        	collName = "uploads";
 	        }
 	        //      check if collection exists otherwise create it
 	        String addCollId = addCollectionToMeleteCollection(addToCollection,collName);
 	        return addCollId;
 	    }	 
 	 public String getUploadCollectionId(String courseId)
 		{
 			try{
 		    String uploadCollectionId=courseId+Entity.SEPARATOR+"uploads";
 		    String collName = "uploads";
 		    // check if collection exists
 		    //read meletDocs dir name from web.xml
 	        String uploadCollId = addCollectionToMeleteCollection(uploadCollectionId, collName);
 	        return uploadCollId;
 			}catch(Exception e)
 			{
 				logger.error("error accessing uploads directory");
 				return null;
 			}
 	    }
 //	Methods used by Migrate program  - End
 	 
 	/*
 	 * for remote browser listing for sferyx editor just get the image files
 	 */
 	 public List getListofImagesFromCollection(String collId)
 	 {
 	   try
 	    {
       			//          setup a security advisor
         		meleteSecurityService.pushAdvisor();
     
         		long starttime = System.currentTimeMillis();
         		logger.debug("time to get all collectionMap" + starttime);
 			 	ContentCollection c= getContentservice().getCollection(collId);
 			 	List	mem = c.getMemberResources();
 			 	if (mem == null) return null;
 			 	
 			 	ListIterator memIt = mem.listIterator();
 			 	while(memIt !=null && memIt.hasNext())
 			 	{
 			 		ContentEntity ce = (ContentEntity)memIt.next();
 			 		if (ce.isResource())
 			 		{
 			 		String contentextension = ((ContentResource)ce).getContentType();
 			 		if(contentextension.equals(MIME_TYPE_LINK) || contentextension.equals(MIME_TYPE_EDITOR))
 			 		{
 			 			 memIt.remove();
 			 		}
 			 		} else  memIt.remove();
 			 	}
 			 	long endtime = System.currentTimeMillis();
         		logger.debug("end time to get all collectionMap" + (endtime - starttime));        		
 			return mem;
 	    }
 		catch(Exception e)
 		{
 			logger.error(e.toString());
 		}
 		finally
 		  {
 			// clear the security advisor
 			meleteSecurityService.popAdvisor();
 		  }
 		  
 		return null;
 	 }
 		/*
 		 * for remote link browser listing for sferyx editor
 		 * just get the urls and links and not image files
 		 */
 	 public List getListofLinksFromCollection(String collId)
 		 {		 	
 		 	try
 		    {
 	        	if (!isUserAuthor())
 	        		{
 	        		logger.info("User is not authorized to access meleteDocs collection");
 	        		}
 	   			//          setup a security advisor
 	        		meleteSecurityService.pushAdvisor();
 	        	 	ContentCollection c= getContentservice().getCollection(collId);
 				 	List	mem = c.getMemberResources();
 				 	if (mem == null) return null;
 				 	
 				 	ListIterator memIt = mem.listIterator();
 				 	while(memIt !=null && memIt.hasNext())
 				 	{
 				 		ContentEntity ce = (ContentEntity)memIt.next();
 				 		if (ce.isResource())
 				 		{
 				 		String contentextension = ((ContentResource)ce).getContentType();
 				 		if(!contentextension.equals(MIME_TYPE_LINK))
 				 			 memIt.remove();
 				 		}else  memIt.remove();
 				 	} 	
 	        		        		
 				return mem;
 		    }
 			catch(Exception e)
 			{
 				logger.error(e.toString());
 			}
 			finally
 			  {
 				// clear the security advisor
 				meleteSecurityService.popAdvisor();
 			  }
 			return null;
 		 }
 	/*
 	 *
 	 */
 	 public ResourcePropertiesEdit fillInSectionResourceProperties(boolean encodingFlag,String secResourceName, String secResourceDescription)
 	{
 	    ResourcePropertiesEdit resProperties = getContentservice().newResourceProperties();
 
 	//  resProperties.addProperty (ResourceProperties.PROP_COPYRIGHT,);
 	//  resourceProperties.addProperty(ResourceProperties.PROP_COPYRIGHT_CHOICE,);
 	    //Glenn said to not set the two properties below
 	  //  resProperties.addProperty(ResourceProperties.PROP_COPYRIGHT_ALERT,Boolean.TRUE.toString());
 		//resProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION,Boolean.FALSE.toString());
 		resProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME,	secResourceName);
 		resProperties.addProperty(ResourceProperties.PROP_DESCRIPTION, secResourceDescription);
 		resProperties.addProperty(getContentservice().PROP_ALTERNATE_REFERENCE, REFERENCE_ROOT);
 
 		//Glenn said the property below may not need to be set either, will leave it in
 		//unless it creates problems
 		if (encodingFlag)
 			resProperties.addProperty(ResourceProperties.PROP_CONTENT_ENCODING,	"UTF-8");
 		return resProperties;
 
 	}
 	/*
 	 *  resource properties for images embedded in the editor content
 	 */
 	 public ResourcePropertiesEdit fillEmbeddedImagesResourceProperties(String name)
 	{
 	    ResourcePropertiesEdit resProperties = getContentservice().newResourceProperties();
 
 		//Glenn said to not set the two properties below
 		//resProperties.addProperty(ResourceProperties.PROP_COPYRIGHT_ALERT,Boolean.TRUE.toString());
 		//resProperties.addProperty(ResourceProperties.PROP_IS_COLLECTION,Boolean.FALSE.toString());
 		resProperties.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
 		resProperties.addProperty(ResourceProperties.PROP_DESCRIPTION,"embedded image");
 		resProperties.addProperty(getContentservice().PROP_ALTERNATE_REFERENCE, REFERENCE_ROOT);
 		return resProperties;
 
 	}
 	/*
 	 *
 	 */
 	 public void editResourceProperties(String selResourceIdFromList, String secResourceName, String secResourceDescription)
 	{
 	 	try
 	    {
         	if (!isUserAuthor())
         		{
         		logger.info("User is not authorized to access meleteDocs collection");
         		}
    			//          setup a security advisor
         		meleteSecurityService.pushAdvisor();
         		selResourceIdFromList = URLDecoder.decode(selResourceIdFromList,"UTF-8");
         		ContentResourceEdit edit = getContentservice().editResource(selResourceIdFromList);
 				ResourcePropertiesEdit rp = edit.getPropertiesEdit();
 				rp.clear();
 				rp.addProperty(ResourceProperties.PROP_DISPLAY_NAME,secResourceName);
 				rp.addProperty(ResourceProperties.PROP_DESCRIPTION,secResourceDescription);
 				rp.addProperty(getContentservice().PROP_ALTERNATE_REFERENCE, REFERENCE_ROOT);
 				getContentservice().commitResource(edit);
 	    }
 		catch(Exception e)
 		{
 			logger.error(e.toString());
 		}
 		finally
 		  {
 			// clear the security advisor
 			meleteSecurityService.popAdvisor();
 		  }
 		return;
 	}
 
 	 public String addResourceItem(String name, String res_mime_type,String addCollId, byte[] secContentData, ResourcePropertiesEdit res ) throws Exception
 	{
 		 ContentResource resource = null;
 		 name = URLDecoder.decode(name,"UTF-8");
 		 if (logger.isDebugEnabled()) logger.debug("IN addResourceItem "+name+" addCollId "+addCollId);
 		// need to add notify logic here and set the arg6 accordingly.
 		try
  	    {
 		    if (!isUserAuthor())
 		    {
 		       logger.info("User is not authorized to add resource");
 		    }
 //       setup a security advisor
          meleteSecurityService.pushAdvisor();
 		 	try{
 		 		 String finalName = addCollId + name;
                  if (finalName.length() > getContentservice().MAXIMUM_RESOURCE_ID_LENGTH)        	 
                  {
                  	//leaving room for CHS inserted duplicate filenames -1 -2 etc
                          int extraChars = finalName.length() - getContentservice().MAXIMUM_RESOURCE_ID_LENGTH +3;
                          name = name.substring(0,name.length() - extraChars);
                  }
 		 		resource = getContentservice().addResource (
 													name,
 										            addCollId,
 										            MAXIMUM_ATTEMPTS_FOR_UNIQUENESS,
 										            res_mime_type,
 										            secContentData,
 										            res, 0);
 		 	//check if its duplicate file and edit the resource name if it is
 		 		String checkDup = resource.getUrl().substring(resource.getUrl().lastIndexOf("/")+1);
 		
 		 		if(!checkDup.equals(name))
 				{					
 		 			ContentResourceEdit edit = getContentservice().editResource(resource.getId());
 					ResourcePropertiesEdit rp = edit.getPropertiesEdit();
 					String desc = rp.getProperty(ResourceProperties.PROP_DESCRIPTION);
 					rp.clear();
 					rp.addProperty(ResourceProperties.PROP_DISPLAY_NAME,checkDup);
 					rp.addProperty(ResourceProperties.PROP_DESCRIPTION,desc);
 					rp.addProperty(getContentservice().PROP_ALTERNATE_REFERENCE, REFERENCE_ROOT);
 					getContentservice().commitResource(edit);
 				}
 			}
 			catch(PermissionException e)
 			{
 			logger.error("permission is denied");
 			}
 			catch(IdInvalidException e)
 			{
 			logger.error("title" + " " + e.getMessage ());
 			throw new MeleteException("failed");
 			}
 			catch(IdLengthException e)
 			{
 			logger.error("The name is too long" + " " +e.getMessage());
 			throw new MeleteException("failed");
 			}
 			catch(IdUniquenessException e)
 			{
 			logger.error("Could not add this resource item to melete collection");
 			throw new MeleteException("failed");
 			}
 			catch(InconsistentException e)
 			{
 			logger.error("Invalid characters in collection title");
 
 			throw new MeleteException("failed");
 			}
 			catch(OverQuotaException e)
 			{
 			logger.error("Adding this resource would place this account over quota.To add this resource, some resources may need to be deleted.");
 			throw new MeleteException("failed");
 			}
 			catch(ServerOverloadException e)
 			{
 			logger.error("failed - internal error");
 			throw new MeleteException("failed");
 			}
 			catch(RuntimeException e)
 			{
 			logger.error("SectionPage.addResourcetoMeleteCollection ***** Unknown Exception ***** " + e.getMessage());            
 			e.printStackTrace();
 			throw new MeleteException("failed");
 			}
 	     } catch (Exception e) {
 		  logger.error(e.toString());
 	     }
 	     finally{
 			meleteSecurityService.popAdvisor();
 		}
 	     return resource.getId();
 	}
 
 	 /*
 	  *
 	  */
 	 public List getAllResources(String uploadCollId)
 	 {
 	 	try
 	    {
         	if (!isUserAuthor())
         		{
         		logger.info("User is not authorized to access meleteDocs collection");
         		}
    			//          setup a security advisor
         		meleteSecurityService.pushAdvisor();
         		return (getContentservice().getAllResources(uploadCollId));
 	    }
 		catch(Exception e)
 		{
 			logger.error(e.toString());
 		}
 		finally
 		  {
 			// clear the security advisor
 			meleteSecurityService.popAdvisor();
 		  }
 		return null;
 	 }
 	 /*
 	  *
 	  */
 	 public ContentResource getResource(String resourceId) throws Exception
 	 {
 	 	try
 	    {
         	if (!isUserAuthor() && !isUserStudent())
         		{
         		logger.info("User is not authorized to access meleteDocs collection");
         		}
    			//          setup a security advisor
         		meleteSecurityService.pushAdvisor();
         		resourceId = URLDecoder.decode(resourceId,"UTF-8");
         		return (getContentservice().getResource(resourceId));
 	    }
 		catch(Exception e)
 		{
 			logger.error(e.toString());
 			throw e;
 		}
 		finally
 		  {
 			// clear the security advisor
 			meleteSecurityService.popAdvisor();
 		  }	
 	 }
 
 	 public void checkResource(String resourceId) throws Exception
 	 {
 	 	try
 	    {
         	if (!isUserAuthor())
         		{
         		logger.info("User is not authorized to access meleteDocs collection");
         		}
    			//          setup a security advisor
         		meleteSecurityService.pushAdvisor();
         		resourceId = URLDecoder.decode(resourceId,"UTF-8");
         		getContentservice().checkResource(resourceId);
         		return;
 	    }
 	 	catch (IdUnusedException ex)
 		{
 	 		logger.debug("resource is not available so create one" + resourceId);
 	 		throw ex;
 		}
 		catch(Exception e)
 		{
 			logger.error(e.toString());
 		}
 		finally
 		  {
 			// clear the security advisor
 			meleteSecurityService.popAdvisor();
 		  }
 		return ;
 	 }
 
 	/*
 	 *
 	 */
 	 public void editResource(String resourceId, String contentEditor) throws Exception
 	 {
 	 	try
 	    {
         	if (!isUserAuthor())
         		{
         		logger.info("User is not authorized to access meleteDocs collection");
         		}
    			//          setup a security advisor
         		meleteSecurityService.pushAdvisor();
        		resourceId = URLDecoder.decode(resourceId,"UTF-8");
        		ContentResourceEdit edit = getContentservice().editResource(resourceId);
        		edit.setContent(contentEditor.getBytes());
        		edit.setContentLength(contentEditor.length());
        		getContentservice().commitResource(edit);
         		return;
 	    }
 	 	catch(Exception e)
 		{
 			logger.error("error saving editor content "+e.toString());
 			throw e;
 		}
 		finally
 		  {
 			// clear the security advisor
 			meleteSecurityService.popAdvisor();
 		  }
 	 }
 
 	 /*
 	     *  before saving editor content, look for embedded images from local system
 	     *  and add them to collection
 	     * if filename has # sign then throw error
 	     *  Do see diego's fix to paste from word works
 	     */
 	    public String findLocalImagesEmbeddedInEditor(String uploadHomeDir, String contentEditor) throws MeleteException
 	    {
 	    	 String checkforimgs = contentEditor;
 	         // get collection id where the embedded files will go
 	         String UploadCollId = getUploadCollectionId();
 	         String fileName;
 			 int startSrc =0;
 			int endSrc = 0;
 			
              // look for local embedded images
 	         try
 			 {
 	         	  if (!isUserAuthor())
 	             	{
 	         		  logger.info("User is not authorized to access meleteDocs");
 	         		  return null;
 	             	}
 	         	//  remove MSword comments
 	      	  	int wordcommentIdx = -1;
 	      	  	while (checkforimgs != null && (wordcommentIdx = checkforimgs.indexOf("<!--[if gte vml 1]>")) != -1)
 	    		{
 	    			String pre = checkforimgs.substring(0,wordcommentIdx);
 	    			checkforimgs = checkforimgs.substring(wordcommentIdx+19);
 	    			int endcommentIdx = checkforimgs.indexOf("<![endif]-->");
 	    			checkforimgs = checkforimgs.substring(endcommentIdx+12);
 	    			checkforimgs= pre + checkforimgs;
 	    			wordcommentIdx = -1;
 	    		}
 	      	  //for FCK editor inserted comments	
 	      	wordcommentIdx = -1;  
 	      	while (checkforimgs != null && (wordcommentIdx = checkforimgs.indexOf("<!--[if !vml]-->")) != -1)
     		{
     			String pre = checkforimgs.substring(0,wordcommentIdx);
     			checkforimgs = checkforimgs.substring(wordcommentIdx);
     			int endcommentIdx = checkforimgs.indexOf("<!--[endif]-->");
     			checkforimgs = checkforimgs.substring(endcommentIdx+14);
     			checkforimgs= pre + checkforimgs;
     			wordcommentIdx = -1;
     		}
 	      
 	    		contentEditor = checkforimgs;
 	    		// remove word comments code end
 	    		
 		         while(checkforimgs !=null)
 		         {
 		           // look for a href and img tag       
 		        	ArrayList embedData = findEmbedItemPattern(checkforimgs);	    			
 	    			checkforimgs = (String)embedData.get(0);
 	    			if (embedData.size() > 1)
 	    			{
 	    				startSrc = ((Integer)embedData.get(1)).intValue();
 	    				endSrc = ((Integer)embedData.get(2)).intValue();
 	    			}
 	    			if (endSrc <= 0) break;
 	    			// find filename
 	    			fileName = checkforimgs.substring(startSrc, endSrc);
 	    			 String patternStr = fileName;
 	    			logger.debug("processing embed src" + fileName); 
 	    			//process for local uploaded files
 					if(fileName != null && fileName.trim().length() > 0&& (!(fileName.equals(File.separator))) 
 						&& fileName.startsWith("file:/") )
 					{
 		              // word paste fix
 		             patternStr= replace(patternStr,"\\","/");
 		             contentEditor = replace(contentEditor,fileName,patternStr);
 		             checkforimgs =  replace(contentEditor,fileName,patternStr);
 
 		             fileName = patternStr;		           	 
 		  	  	    fileName = fileName.substring(fileName.lastIndexOf("/")+1);
 
 //			  	  	 if filename contains pound char then throw error
 					if(fileName.indexOf("#") != -1)
 			  	  	{
 			  	  	logger.error("embedded FILE contains hash or other characters " + fileName);
 	  	  		    throw new MeleteException("embed_img_bad_filename");
 			  	  	}
 		             // add the file to collection and move from uploads directory
 		             // read data
 		             try{
 		             File re = new File(uploadHomeDir+File.separator+fileName);
 
 		             byte[] data = new byte[(int)re.length()];
 		             FileInputStream fis = new FileInputStream(re);
 		             fis.read(data);
 		             fis.close();
 		             re.delete();
 
 		             // add as a resource to uploads collection
 		             String file_mime_type = fileName.substring(fileName.lastIndexOf(".")+1);
 		             file_mime_type = ContentTypeImageService.getContentType(file_mime_type);
 
 		            
 		            String newEmbedResourceId = null;
 		            //If the resource already exists, use it
 		            try
 	                {
 	                   	  String checkResourceId = UploadCollId + "/" + fileName;
 	                   	  checkResource(checkResourceId);
 				 	      newEmbedResourceId = checkResourceId;
 	                }
 	                catch (IdUnusedException ex2)
 			        {
 	                	ResourcePropertiesEdit res =fillEmbeddedImagesResourceProperties(fileName);
 	                	newEmbedResourceId = addResourceItem(fileName,file_mime_type,UploadCollId,data,res );
 			        }
                    
 		            //add in melete resource database table also
 		             MeleteResource meleteResource = new MeleteResource();
 	            	 meleteResource.setResourceId(newEmbedResourceId);
 	            	 //set default license info to "I have not determined copyright yet" option
 	            	 meleteResource.setLicenseCode(0);
 	            	 sectiondb.insertResource(meleteResource);
 
 		         	// in content editor replace the file found with resource reference url
 		         	 String replaceStr = getResourceUrl(newEmbedResourceId);
 		         	 replaceStr = replace(replaceStr,ServerConfigurationService.getServerUrl(),"");
 		         	 logger.debug("repl;acestr in embedimage processing is " + replaceStr);
 		         	
 		         	 // Replace all occurrences of pattern in input
 		            Pattern pattern = Pattern.compile(patternStr);
 
 		            //Rashmi's change to fix infinite loop on uploading images
 		            contentEditor = replace(contentEditor,patternStr,replaceStr);
 		            checkforimgs = replace(contentEditor,patternStr,replaceStr);
 		             }
 		             catch(FileNotFoundException ff)
 					 {
 		             	logger.error(ff.toString());
 		             	throw new MeleteException("embed_image_size_exceed");
 					 }
 				} 
 				// for internal links to make it relative	
 				else if (fileName.startsWith(ServerConfigurationService.getServerUrl()))
 				{
 					if (fileName.indexOf("/meleteDocs") != -1)
 					{
 						String findEntity = fileName.substring(fileName.indexOf("/access")+7);
 						logger.info("find entity string " + findEntity);
 						Reference ref = EntityManager.newReference(findEntity);
 						logger.debug("ref properties" + ref.getType() +"," +ref.getId());
 						String newEmbedResourceId =ref.getId() ;
 						newEmbedResourceId =newEmbedResourceId.replaceFirst("/content","");
 						
 						if (sectiondb.getMeleteResource(newEmbedResourceId) == null)
 						{	
 						 //add in melete resource database table also
 			             MeleteResource meleteResource = new MeleteResource();
 		            	 meleteResource.setResourceId(newEmbedResourceId);
 		            	 //set default license info to "I have not determined copyright yet" option
 		            	 meleteResource.setLicenseCode(0);
 		            	 sectiondb.insertResource(meleteResource);
 						} 
 						
 					}
 					String replaceStr = replace(fileName,ServerConfigurationService.getServerUrl(),"");
 					contentEditor = replace(contentEditor,patternStr,replaceStr);
 			        checkforimgs = replace(checkforimgs,patternStr,replaceStr);
 				}
 		            // iterate next
 		            checkforimgs =checkforimgs.substring(endSrc);
 		            startSrc=0; endSrc = 0;
 		         }
 			 }
 	         catch(MeleteException me) {throw me;}
 	         catch(Exception e){logger.error(e.toString());e.printStackTrace();}
 
 	    	return contentEditor;
 	    }
 
 	    /*
 	     * 
 	     */
 		protected ArrayList findEmbedItemPattern(String checkforimgs)
 		{
 			ArrayList returnData = new ArrayList();
 			Pattern p1 = Pattern.compile("<[iI][mM][gG]\\s|<[aA]\\s");
 			Pattern pi = Pattern.compile(">|\\s[sS][rR][cC]\\s*=");
 			Pattern pa = Pattern.compile(">|\\s[hH][rR][eE][fF]\\s*=");
 			Pattern ps = Pattern.compile("\\S");
 			Pattern pe = Pattern.compile("\\s|>");
 
 			int startSrc = 0;
 			int endSrc = 0;
 			
 			while(checkforimgs !=null) {
 
 			        // look for <img or <a
 			        Matcher m = p1.matcher(checkforimgs);
 				if (!m.find()) // found anything?
 				    break;
 				checkforimgs = checkforimgs.substring(m.start());
 				// look for src= or href=
 				if (checkforimgs.startsWith("<i") ||
 				    checkforimgs.startsWith("<I"))
 				    m = pi.matcher(checkforimgs);
 				else
 				    m = pa.matcher(checkforimgs);
 				// end = start+1 means that we found a >
 				// i.e. the attribute we're looking for isn't there
 				if (!m.find() || (m.end() == m.start() + 1)) {
 				    // prevent infinite loop by consuming the <
 				    checkforimgs = checkforimgs.substring(1);
 				    continue;
 				}
 
 				checkforimgs = checkforimgs.substring(m.end());
 
 				// look for start of arg, a non-whitespace
 			        m = ps.matcher(checkforimgs);
 				if (!m.find()) // found anything?
 				    break;
 				
 				checkforimgs = checkforimgs.substring(m.start());
 				
 				startSrc = 0;
 				endSrc = 0;
 
 				// handle either quoted or nonquoted arg
 				if (checkforimgs.startsWith("\"") ||
 				    checkforimgs.startsWith("\'")) {
 				    String quotestr = checkforimgs.substring(0,1);
 				    startSrc = 1;
 				    endSrc = checkforimgs.indexOf(quotestr, startSrc);
 				    break;
 				} else {
 				    startSrc = 0;
 				    // ends with whitespace or >
 				    m = pe.matcher(checkforimgs);
 				    if (!m.find()) // found anything?
 					continue;
 				    endSrc = m.start();
 				}
 			} //while end
 			returnData.add(checkforimgs);
 			if (endSrc != 0) {returnData.add(new Integer(startSrc)); returnData.add(new Integer(endSrc)); }
 			return returnData;
 		}
 		
 	    
 	    
 	    /*
 	     * get the URL for replaceStr of embedded images
 	     */
 	    public String getResourceUrl(String newResourceId)
 	    {
 	    	try
      	    {
      	      	meleteSecurityService.pushAdvisor();
      	      	newResourceId = URLDecoder.decode(newResourceId,"UTF-8");
      	      	return getContentservice().getUrl(newResourceId);
              }
        catch (Exception e)
            {
      	      e.printStackTrace();
      	     return "";
            }
        finally
            {
          	  meleteSecurityService.popAdvisor();
            }
 
 	    }
 
 
 		private String replace(String s, String one, String another) {
 				// In a string replace one substring with another
 				if (s.equals(""))
 					return "";
 				String res = "";
 				int i = s.indexOf(one, 0);
 				int lastpos = 0;
 				while (i != -1) {
 					res += s.substring(lastpos, i) + another;
 					lastpos = i + one.length();
 					i = s.indexOf(one, lastpos);
 				}
 				res += s.substring(lastpos); // the rest
 				return res;
 	  }
 		
 	  public void copyIntoFolder(String fromColl,String toColl)
 	  {
 			try
 		    {
 	        if (!isUserAuthor())
 	        {
 	        		logger.info("User is not authorized to perform the copyIntoFolder function");
 	        }
 	   			//          setup a security advisor
 	        meleteSecurityService.pushAdvisor();			
 		    try
 			{
 	         getContentservice().copyIntoFolder(fromColl, toColl);
 	       }
 	       catch(InconsistentException e)
           {
             logger.error("Inconsistent exception thrown");
           }
 	       catch(IdLengthException e)
           {
             logger.error("IdLength exception thrown");
           }
 	       catch(IdUniquenessException e)
           {
             logger.error("IdUniqueness exception thrown");
           }
           catch(PermissionException e)
           {
             logger.error("Permission to copy uploads collection is denied");
           }
           catch(IdUnusedException e)
           {
             logger.error("Failed to create uploads collection in second site");
           }
           catch(TypeException e)
           {
             logger.error("TypeException thrown: "+e.getMessage());
           }
           catch(InUseException e)
           {
             logger.error("InUseException thrown: "+e.getMessage());
           }
           catch(IdUsedException e)
           {
             logger.error("IdUsedException thrown");
           }
           catch(OverQuotaException e)
           {
             logger.error("Copying this collection would place this account over quota.");
            }
            catch(ServerOverloadException e)
            {
              logger.error("Server overload exception");
            }
          }
 		 catch (Exception e)
 	     {
 	        e.printStackTrace();
 	      }
 	     finally
 	     {
 	       meleteSecurityService.popAdvisor();
 	     }
 			
 		}
 	
 	  
 	  public ContentCollection getCollection(String toColl)
 	  {
 		ContentCollection toCollection = null;  
 		try
 	    {
         if (!isUserAuthor())
         {
         		logger.info("User is not authorized to perform the copyIntoFolder function");
         }
    			//          setup a security advisor
         meleteSecurityService.pushAdvisor();		
 	    try
    	    {
    		  toCollection = getContentservice().getCollection(toColl);
    		}
    	    catch(IdUnusedException e1)
 	    {
    		  logger.error("IdUnusedException thrown: "+e1.getMessage());
 	    }
    	    catch(TypeException e1)
         {
           logger.error("TypeException thrown: "+e1.getMessage());
         }
    	    catch(PermissionException e1)
         {
           logger.error("Permission to get uploads collection is denied");
         }
 	    }
 		catch (Exception e)
 	    {
 	        e.printStackTrace();
 	    }
 	    finally
 	    {
 	       meleteSecurityService.popAdvisor();
 	    }   	    
    	    return toCollection;
 	  }
 	  
 	    /**
 	     * @return Returns the logger.
 	     */
 	    public void setLogger(Log logger) {
 	            this.logger = logger;
 	    }
 
 	    /**
 	     * @return Returns the contentservice.
 	     */
 	    public ContentHostingService getContentservice() {
 	            return contentservice;
 	    }
 	    /**
 	     * @param contentservice The contentservice to set.
 	     */
 	    public void setContentservice(ContentHostingService contentservice) {
 	            this.contentservice = contentservice;
 	    }
 
 	    /**
 		 * @return meleteSecurityService
 		 */
 		public MeleteSecurityService getMeleteSecurityService() {
 			return meleteSecurityService;
 		}
 
 
 	    /**
 		 * @param meleteSecurityService The meleteSecurityService to set.
 		 */
 		public void setMeleteSecurityService(MeleteSecurityService meleteSecurityService) {
 			this.meleteSecurityService = meleteSecurityService;
 		}
 
 	    /**
 		 * @return Returns the sectiondb.
 		 */
 		public SectionDB getSectiondb() {
 			return sectiondb;
 		}
 		/**
 		 * @param sectiondb The sectiondb to set.
 		 */
 		public void setSectiondb(SectionDB sectiondb) {
 			this.sectiondb = sectiondb;
 		}
 }
