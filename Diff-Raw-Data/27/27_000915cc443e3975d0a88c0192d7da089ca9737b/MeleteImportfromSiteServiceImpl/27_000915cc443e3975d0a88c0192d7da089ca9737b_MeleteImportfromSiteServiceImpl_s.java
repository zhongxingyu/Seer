 /**********************************************************************************
  *
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2008,2009 Etudes, Inc.
  *
  * Portions completed before September 1, 2008 Copyright (c) 2004, 2005, 2006, 2007, 2008 Foothill College, ETUDES Project
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
 package org.etudes.component.app.melete;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.net.URLDecoder;
 
 import org.etudes.component.app.melete.MeleteUtil;
import org.etudes.util.XrefHelper;
 import org.etudes.api.app.melete.MeleteCHService;
 import org.etudes.api.app.melete.MeleteImportfromSiteService;
 import org.etudes.api.app.melete.exception.MeleteException;
 
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.content.cover.ContentTypeImageService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.entity.api.Reference;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.thread_local.api.ThreadLocalManager;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.sakaiproject.entity.api.Entity;
 import org.sakaiproject.entity.api.ResourcePropertiesEdit;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.util.ResourceLoader;
 import org.sakaiproject.entity.cover.EntityManager;
 
 /**
  * MeleteImportServiceImpl is the implementation of MeleteImportService
  * that provides the methods for import export
  *
  * @author Murthy @ Foothill college
  */
 public class MeleteImportfromSiteServiceImpl extends MeleteImportBaseImpl implements MeleteImportfromSiteService{
 	/*******************************************************************************
 	 * Dependencies
 	 *******************************************************************************/
 	/** Dependency:  The logging service. */
 	protected Log logger = LogFactory.getLog(MeleteImportfromSiteServiceImpl.class);
 
 	/**
 	 * Imports composed section files and for other content types, imports the resource if it not exists in site
 	 */
 	private void createContentResource(Module module,Section section,MeleteResource meleteResource, String hrefVal, String courseId, String oldCourseId)
 	throws MalformedURLException, UnknownHostException, MeleteException, Exception {
 		String newResourceId = "";
 
 		if (logger.isDebugEnabled())
 			logger.debug("Entering createSection...");
 
 		//html file
 		if (section.getContentType().equals("typeEditor")) {
 			String addCollId = getMeleteCHService().getCollectionId(courseId, section.getContentType(), module.getModuleId());
 			String sectionResourceName = "Section_" + section.getSectionId().toString();
 			newResourceId = processEmbedDatafromHTML(hrefVal,sectionResourceName, courseId,addCollId);
 			meleteResource.setResourceId(newResourceId);
 			sectionDB.insertMeleteResource((Section)section, (MeleteResource)meleteResource);
 			return;
 		}
 		//for any other section upload/link/LTI check the resource exists and associate with it
 		ArrayList rdata = checkAndAddResource(hrefVal,courseId,true, oldCourseId);
 		// if exists then associate with section else insert everywhere
 		logger.debug("associate now" + rdata.toString());
 		if(rdata != null && rdata.get(0).equals("exists") && rdata.get(1) != null)
 		{
 			meleteResource.setResourceId((String)rdata.get(1));		 				 		
 			sectionDB.insertSectionResource((Section)section, (MeleteResource)meleteResource);
 		}
 		else if(rdata != null && rdata.get(0).equals("new") && rdata.get(1) != null )
 		{
 			meleteResource.setResourceId((String)rdata.get(1));
 			sectionDB.insertMeleteResource((Section)section, (MeleteResource)meleteResource);
 		}
 		if (logger.isDebugEnabled())
 			logger.debug("Exiting createSection...");
 	}
 
 	/*
 	 * check if resource exists in the meletedocs collection else add it and record it for skipped files
 	 */
 	private ArrayList<String> checkAndAddResource(String hrefVal,String toSiteId, boolean addDB, String oldCourseId) throws Exception
 	{
 		ArrayList<String> rdata = new ArrayList<String>();
 		ContentResource cr = null;
 		String melResourceName = null;
 		String melResourceDescription = null;
 
 		try{
 			cr = getMeleteCHService().getResource(hrefVal);
 			if (cr == null) return null;
 			melResourceDescription = cr.getProperties().getProperty(ResourceProperties.PROP_DESCRIPTION);
 			melResourceName = cr.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
 
 			// check if the item has already been imported to this site (in uploads collection)
 			addToThreadList(melResourceName, "MELETE_importResources");
 			String checkResourceId = "/private/meleteDocs/"+toSiteId+"/uploads/"+melResourceName;
 			getMeleteCHService().checkResource(checkResourceId);
 			rdata.add("exists");
 			rdata.add(checkResourceId);
 			return rdata;
 		}catch (IdUnusedException ex)
 		{
 			// if not found in meleteDocs collection include it
 			String uploadCollId = getMeleteCHService().getUploadCollectionId(toSiteId);
 			String newResourceId = null;		
 
 			if(cr.getContentType().equals(getMeleteCHService().MIME_TYPE_LINK))
 			{
 				String linkData = new String(cr.getContent());
 
 				//LINKS POINTING TO RESOURCES AND MELETEDOCS COLLECTION
 				if(linkData.indexOf("/access/content/group") != -1 || linkData.indexOf("/access/meleteDocs/content") != -1)
 				{					
 					String firstReferId = copyResource(linkData, oldCourseId, toSiteId, uploadCollId);
 					// link item should point to new address
 					byte[] melContentData = (getMeleteCHService().getResourceUrl(firstReferId)).getBytes();
 					ResourcePropertiesEdit res = getMeleteCHService().fillInSectionResourceProperties(true,melResourceName,melResourceDescription);
 					newResourceId = getMeleteCHService().addResourceItem(melResourceName, getMeleteCHService().MIME_TYPE_LINK,uploadCollId,melContentData,res );			 		
 				}
 				//external urls or my workspace etc
 				else newResourceId = getMeleteCHService().copyIntoFolder(cr.getId(), uploadCollId);
 			}
 			//LTI resource
 			else if(cr.getContentType().equals(getMeleteCHService().MIME_TYPE_LTI))
 			{		
 				ResourcePropertiesEdit res = getMeleteCHService().fillInSectionResourceProperties(true,melResourceName,melResourceDescription);
 				newResourceId = getMeleteCHService().addResourceItem(melResourceName, getMeleteCHService().MIME_TYPE_LTI,uploadCollId,cr.getContent(),res );			
 			}	
 			else
 			{
 				// upload resource
 				newResourceId = copyResource(getMeleteCHService().getResourceUrl(cr.getId()), oldCourseId, toSiteId, uploadCollId);
 			}
 
 			addToThreadList(melResourceName, "MELETE_addedNowResource");	
 			rdata.add("new");
 			rdata.add(newResourceId);
 			return rdata;
 		} // catch end
 	}
 	
 	/*
 	 *  Finds the source of resource item - meletedocs or group collection.
 	 *  If resource is html then call processEmbed method otherwise create a new resource
 	 */
 	private String copyResource(String Data, String oldCourseId, String toSiteId, String uploadCollId) throws Exception
 	{
 		String firstReferId = null;
 		ArrayList<String> r = meleteUtil.findResourceSource(Data, oldCourseId, toSiteId, true);
 		if(r == null || r.size() == 0) return null;
 		String ref_id = r.get(0);
 		String checkReferenceId = r.get(1);
 		try
 		{						
 			getMeleteCHService().checkResource(checkReferenceId);
 			firstReferId = checkReferenceId;
 		}
 		catch (IdUnusedException iue)
 		{
 			String extn = ref_id.substring(ref_id.lastIndexOf(".")+1);
 			if (extn.equals("htm") || extn.equals("html"))
 			{
 				firstReferId = processEmbedDatafromHTML(ref_id,null,toSiteId,uploadCollId);
 			}
 			else
 			{
 				ContentResource cr1 = getMeleteCHService().getResource(ref_id);
 				String fileResourceName = cr1.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
 				ResourcePropertiesEdit res = getMeleteCHService().fillInSectionResourceProperties(false,fileResourceName,cr1.getProperties().getProperty(ResourceProperties.PROP_DESCRIPTION));
 				firstReferId = getMeleteCHService().addResourceItem(fileResourceName, cr1.getContentType(),uploadCollId,cr1.getContent(),res );
 				addToThreadList(fileResourceName, "MELETE_addedNowResource");
 			}				
 		}
 
 		return firstReferId;
 	}
 
 	/*
 	 *  process HTML resources.
 	 */
 	private String processEmbedDatafromHTML(String ref_id,String fileResourceName, String toSiteId,String uploadCollId) throws Exception
 	{
 		ContentResource cr1 = getMeleteCHService().getResource(ref_id);
 		if(fileResourceName == null) fileResourceName = cr1.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
 		String contentEditor = new String(cr1.getContent());
 		String parentStr = meleteUtil.findParentReference(cr1.getId());
 		ArrayList content = createHTMLFile(contentEditor,toSiteId,new HashSet<String>() ,parentStr);
 		contentEditor = (String)content.get(0);
 		addToThreadList(fileResourceName, "MELETE_addedNowResource");
 		ResourcePropertiesEdit res = getMeleteCHService().fillInSectionResourceProperties(true,fileResourceName,cr1.getProperties().getProperty(ResourceProperties.PROP_DESCRIPTION));
 		return getMeleteCHService().addResourceItem(fileResourceName, cr1.getContentType(),uploadCollId,contentEditor.getBytes(),res );
 	}
 
 	/*
 	 * Abstract method implementation to read from a content resource
 	 */
 	protected byte[] readData(String unZippedDirPath, String hrefVal) throws Exception
 	{			
 		ContentResource cr = getMeleteCHService().getResource(hrefVal);
 		return cr.getContent();
 	}
 
 	/*
 	 * Parse HTML files and create resources for embedded data.
 	 * If embedded data is an html file then calls itself again to process.
 	 * Set is used to record the html files processed in one recursion cycle to avoid circular references.
 	 */
 	protected ArrayList createHTMLFile(String contentEditor, String courseId, Set<String> checkEmbedHTMLResources, String parentRef)throws Exception{
 		//save uploaded img inside content editor to destination directory
 		String checkforimgs = contentEditor;
 		int imgindex = -1;
 		String imgSrcPath, imgName, imgLoc;
 
 		int startSrc =0;
 		int endSrc = 0;
 		logger.debug("parentStr inside createHTML" + parentRef);
 		while (checkforimgs != null) {
 			ArrayList embedData = meleteUtil.findEmbedItemPattern(checkforimgs);
 			checkforimgs = (String)embedData.get(0);
 			if (embedData.size() > 1)
 			{
 				startSrc = ((Integer)embedData.get(1)).intValue();
 				endSrc = ((Integer)embedData.get(2)).intValue();
 			}
 			if (endSrc <= 0) break;
 
 			imgSrcPath = checkforimgs.substring(startSrc, endSrc);
 			
            	//This part executed by import from site
 			String imgActualPath = "";
 
 			//make it full url
 			if(imgSrcPath.indexOf("://") == -1 && imgSrcPath.indexOf("/") == -1)
 			{
 				logger.debug("parentRef on checking rel path " + parentRef);
 				if(parentRef != null && parentRef.length() > 0)
 				{
 					contentEditor = meleteUtil.replace(contentEditor,imgSrcPath, parentRef + imgSrcPath);
 					imgSrcPath = parentRef + imgSrcPath;
 				}
 			}
 			// if img src is in library or any other inside sakai path then don't process
 			if(imgSrcPath.indexOf("/access") !=-1)
 			{
 				checkforimgs = checkforimgs.substring(endSrc);
 				ArrayList<String> r = meleteUtil.findResourceSource(imgSrcPath, null, courseId, false);
 				if(r == null || r.size() == 0)
 				{					
 					imgindex = -1;
 					startSrc=0; endSrc = 0;
 					continue;
 				}
 				imgActualPath = r.get(0);
 
 				String importResName = imgActualPath.substring(imgActualPath.lastIndexOf('/')+1);
 				addToThreadList(URLDecoder.decode(importResName,"UTF-8"), "MELETE_importResources");
 
 				if(imgActualPath.endsWith(".htm") || imgActualPath.endsWith(".html"))
 				{
 					// if not processed yet then add to the set
 					if(checkEmbedHTMLResources.contains(imgActualPath))
 					{
 						imgindex = -1;
 						startSrc=0; endSrc = 0;
 						String replacementStr = "/access/meleteDocs/content/private/meleteDocs/" + courseId + "/uploads/" + imgSrcPath.substring( imgSrcPath.lastIndexOf('/') + 1);
 						String patternStr = imgSrcPath;
 						contentEditor = meleteUtil.replace(contentEditor,patternStr, replacementStr);
 						continue;
 					}
 					checkEmbedHTMLResources.add(imgActualPath);
 					// look for its embedded data
 					ContentResource embedResource = getMeleteCHService().getResource(imgActualPath);
 					if(embedResource.getContentLength() > 0)
 					{
 						String moreContentData = new String(embedResource.getContent());
 						String parentStr = meleteUtil.findParentReference(imgActualPath);
 						String filename = imgActualPath.substring( imgActualPath.lastIndexOf('/') + 1);
 						ArrayList contentData = createHTMLFile(moreContentData,courseId,checkEmbedHTMLResources,parentStr);
 						moreContentData = (String)contentData.get(0);
 						checkEmbedHTMLResources = (Set<String>)contentData.get(1);
 						try
 						{
 							String checkResourceId = Entity.SEPARATOR + "private" + Entity.SEPARATOR + "meleteDocs" +Entity.SEPARATOR+courseId+Entity.SEPARATOR+"uploads"+Entity.SEPARATOR+filename;
 							getMeleteCHService().checkResource(checkResourceId);
 						}catch (IdUnusedException ex)
 						{
 							addResource(filename, moreContentData.getBytes(), courseId);
 						}
 						catch(Exception e){
 							//logger.debug("error adding a resource on import from site");
 						}
 					}
 				}
 				contentEditor = ReplaceEmbedMediaWithResourceURL(contentEditor, imgSrcPath, imgActualPath, courseId, null);
 
 			} //if access check end
 			// for other inside sakai paths
 			else checkforimgs = checkforimgs.substring(endSrc);
 
 			imgindex = -1;
 			startSrc=0; endSrc = 0;
 		}//End while    
 		ArrayList returnData = new ArrayList();
 		returnData.add(contentEditor);
 		returnData.add(checkEmbedHTMLResources);
 		return returnData;
 	}
 
 	/*
 	 * Call for import from site process
 	 */
 	public void copyModules(String fromContext, String toContext)
 	{
 		//Copy the uploads collection
 		buildModules(fromContext, toContext);
 		transferManageItems(fromContext, toContext);
 		//   	setMeleteSitePreference(fromContext, toContext);
 	}
 
 	/*
 	 * 
 	 */
 	private void setMeleteSitePreference(String fromContext, String toContext)
 	{
 		MeleteSitePreference fromMsp = meleteUserPrefDB.getSitePreferences(fromContext);
 		meleteUserPrefDB.setSitePreferences(toContext,fromMsp.isPrintable(),fromMsp.isAutonumber());
 	}
 
 	/*
 	 * Imports all acive and archived modules
 	 */
 	private void buildModules(String fromContext, String toContext)
 	{
 		//		Get modules in site A
 		Map sectionList = null;
 		MeleteResource toMres = null;
 		int fromSecId, toSecId;
 
 		List fromModuleList = moduleDB.getActivenArchiveModules(fromContext);
 
 		//Iterate through all modules in site A
 		if (fromModuleList == null || fromModuleList.size() <= 0) return;
 
 		for (ListIterator i = fromModuleList.listIterator(); i.hasNext(); )
 		{
 			Module fromMod = (Module) i.next();
 			String fromModSeqXml = fromMod.getSeqXml();
 
 			//Copy module properties and insert, seqXml is null for now
 			Module toMod = new Module(fromMod.getTitle(), fromMod.getLearnObj(), fromMod.getDescription(), fromMod.getKeywords(), fromMod.getCreatedByFname(), fromMod.getCreatedByLname(), fromMod.getUserId(), fromMod.getModifiedByFname(), fromMod.getModifiedByLname(), fromMod.getInstitute(), fromMod.getWhatsNext(), fromMod.getCreationDate(), fromMod.getModificationDate(), null);
 			ModuleShdates toModshdate = new ModuleShdates(((ModuleShdates)fromMod.getModuleshdate()).getStartDate(), ((ModuleShdates)fromMod.getModuleshdate()).getEndDate());
 			if (fromMod.getCoursemodule().isArchvFlag() == false)
 			{
 				try{
 					moduleDB.addModule(toMod, toModshdate, fromMod.getUserId(), toContext);
 				}catch(Exception ex3){
 					// logger.debug("error importing module");
 				}
 			}
 			else
 			{
 				CourseModule toCmod = new CourseModule(toContext, -1, true, fromMod.getCoursemodule().getDateArchived(), false, toMod);
 				try{
 					moduleDB.addArchivedModule(toMod, toModshdate, fromMod.getUserId(), toContext, toCmod);
 				}
 				catch(Exception ex3){
 					// logger.debug("error importing archived module");
 				}
 
 			}
 
 			sectionList = fromMod.getSections();
 			//Iterate throug sections of a module
 			if (sectionList != null)
 			{
 				int mapSize = sectionList.size();
 				if (mapSize > 0)
 				{
 					Iterator keyValuePairs = sectionList.entrySet().iterator();
 					while (keyValuePairs.hasNext())
 					{
 						Map.Entry entry = (Map.Entry) keyValuePairs.next();
 						Section fromSec = (Section) entry.getValue();
 						fromSecId = fromSec.getSectionId().intValue();
 						Section toSec = new Section(fromSec.getTitle(), fromSec.getCreatedByFname(), fromSec.getCreatedByLname(), fromSec.getModifiedByFname(), fromSec.getModifiedByLname(), fromSec.getInstr(), fromSec.getContentType(), fromSec.isAudioContent(), fromSec.isVideoContent(), fromSec.isTextualContent(), fromSec.isOpenWindow(), fromSec.isDeleteFlag(), fromSec.getCreationDate(), fromSec.getModificationDate());
 						// logger.debug("copied section open window value" + toSec.getTitle()+"," + toSec.isOpenWindow() );
 						try
 						{
 							//Insert into the SECTION table
 							sectionDB.addSection(toMod, toSec, false);
 							toSecId = toSec.getSectionId().intValue();
 							//Replace old references of sections to new references in SEQ xml
 							//TODO : Move the update seqxml lower down so sequence does not update
 							//if exception is thrown
 							if(!fromSec.getContentType().equals("notype") && fromSec.getSectionResource() != null)
 							{
 								toMres = new MeleteResource((MeleteResource)fromSec.getSectionResource().getResource());
 								toMres.setResourceId(null);
 								createContentResource(toMod,toSec,toMres,((MeleteResource)fromSec.getSectionResource().getResource()).getResourceId(),toContext,fromContext);
 							}
 							if (fromModSeqXml!=null)
 								fromModSeqXml = fromModSeqXml.replace(Integer.toString(fromSecId), Integer.toString(toSecId));
 
 						}
 						catch(Exception ex)
 						{
 							if (logger.isDebugEnabled()) {
 								// logger.debug("error in inserting section "+ ex.toString());
 								ex.printStackTrace();
 							}
 							//rollback and delete section
 							try
 							{
 								sectionDB.deleteSection(toSec,toContext, null);
 							}
 							catch (Exception ex2)
 							{
 								logger.debug("Error in deleting section "+ex2.toString());
 							}
 						}
 
 					}
 
 					//Finally, update the seqXml for the module
 
 					try
 					{
 						Module secModule = moduleDB.getModule(toMod.getModuleId().intValue());
 						secModule.setSeqXml(fromModSeqXml);
 						moduleDB.updateModule(secModule);
 					}
 					catch (Exception ex)
 					{
 						logger.debug("error in updating module");
 					}
 
 				}
 			}
 
 		}
		reportSkippedFiles(toContext);
	}

	/*
	 * Report skipped files at the end of import from site process.
	 * MELETE_importResources records all files referred and MELETE_addedNowResource records the one
	 * added in the current process.
	 */
	private void reportSkippedFiles(String toContext)
	{
		// remove the ones from importResources
		Set<String> importResources = (Set<String>) threadLocalManager.get("MELETE_importResources");
		Set<String> addReferred = (Set<String>) threadLocalManager.get("MELETE_addedNowResource");
		if ((importResources != null) && (addReferred != null))
		{
			importResources.removeAll(addReferred);
		}
		if (importResources != null)
		{
			for (String name : importResources)
			{
				String chsId = "/private/meleteDocs/" + toContext + "/uploads/" + name;
				XrefHelper.recordFileSkipped(chsId);
			}
		}
 	}
 
 	/*
 	 * Import of unreferred meletedocs items
 	 */
 	private void transferManageItems(String fromContext, String toContext)
 	{
 		long totalstart = System.currentTimeMillis();
 
 		String fromUploadsColl = Entity.SEPARATOR+"private"+ REFERENCE_ROOT+ Entity.SEPARATOR+fromContext+Entity.SEPARATOR+"uploads"+Entity.SEPARATOR;
 		String toUploadsColl = Entity.SEPARATOR+"private"+ REFERENCE_ROOT+ Entity.SEPARATOR+toContext+Entity.SEPARATOR+"uploads"+Entity.SEPARATOR;
 
 		List fromContextList = meleteCHService.getMemberNamesCollection(fromUploadsColl);
 
 		List toContextList = meleteCHService.getMemberNamesCollection(toUploadsColl);
 
 		if ((fromContextList != null)&&(toContextList != null))
 		{
 			ListIterator memIt = fromContextList.listIterator();
 			List replaceContextList = new ArrayList();
 			while(memIt !=null && memIt.hasNext())
 			{
 				String resId = (String) memIt.next();
 				resId = resId.replace(fromContext, toContext);
 				replaceContextList.add(resId);
 			}
 			if (replaceContextList != null)
 			{
 				if (replaceContextList.size() > 0)
 				{
 					replaceContextList.removeAll(toContextList);
 					if (replaceContextList.size() > 0)
 					{
 						ListIterator repIt = replaceContextList.listIterator();
 						while (repIt != null && repIt.hasNext())
 						{
 							String resId = (String) repIt.next();
 							resId = resId.replace(toContext,fromContext);
 							byte[] melContentData;
 							String res_mime_type,melResourceName;
 							try
 							{
 								ContentResource cr = getMeleteCHService().getResource(resId);
 								melContentData = cr.getContent();
 								res_mime_type = cr.getContentType();
 								melResourceName =  cr.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME);
 								try
 								{
 									// check if the item has already been imported to this site (in uploads collection)
 									String checkResourceId = toUploadsColl+melResourceName;
 									getMeleteCHService().checkResource(checkResourceId);
 								}
 								catch (IdUnusedException ex)
 								{
 									ResourcePropertiesEdit res = getMeleteCHService().fillInSectionResourceProperties(false,melResourceName,"");
 									try
 									{
 										String newResourceId = getMeleteCHService().addResourceItem(melResourceName, res_mime_type,toUploadsColl,melContentData,res );
 									}
 									catch(Exception e)
 									{
 										logger.debug("Error thrown in exporting of manage resources");
 										logger.debug(e.toString());
 									}
 								}
 							}
 							catch(IdUnusedException unuse)
 							{
 								// if file not found exception or content is missing continue working
 								logger.debug("error in reading resource content in exporting manage resources");
 							}
 							catch(Exception e)
 							{
 								logger.error("error in reading resource in export manage resource");
 							}
 
 						}//End while repIt
 					}//End if
 				}
 			}
 		}
 
 		long totalend = System.currentTimeMillis();
 
 		logger.debug("TRANSFER took "+(totalend-totalstart)+" millisecs");
 
 	}
 
 }
