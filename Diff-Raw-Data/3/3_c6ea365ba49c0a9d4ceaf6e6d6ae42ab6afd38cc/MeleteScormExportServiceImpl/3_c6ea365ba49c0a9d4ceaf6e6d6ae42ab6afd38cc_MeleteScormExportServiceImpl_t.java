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
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.dom4j.DocumentHelper;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.Namespace;
 import org.dom4j.QName;
 import org.etudes.api.app.melete.MeleteExportService;
 import org.etudes.api.app.melete.MeleteSecurityService;
 import org.etudes.api.app.melete.exception.MeleteException;
 import org.etudes.api.app.melete.util.XMLHelper;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.util.Validator;
 import org.xml.sax.SAXException;
 import org.sakaiproject.entity.api.Entity;
 
 /**
  * SCORM 2004 3rd Edition Asset Export Implementation
  * @author CRIM
  * Mallika M Thoppay, 4/2/08 - The SCORM export process requires that all root level <item> elements either have an identifierref attribute
  * or the element's child elements have the identifierref attribute. It is ok to have an identifierref
  * attribute both for a child <item> element and its nested child <item> elements (supports subsections), however
  * you cannot have a main <item> element *and* its child <item> element have an identifierref attribute(empty module
  * with next steps scenario). To ensure all of these requirements are met, generateOrganizationResourceItems, createSectionElement
  * and createResourceElement methods have been changed.
  */
 public class MeleteScormExportServiceImpl extends MeleteAbstractExportServiceImpl implements MeleteExportService{
 
 	public void initValues()
 	{
 	   setMetaDataNameSpace("http://ltsc.ieee.org/xsd/LOM");
       setSchema("ADL SCORM");
       setSchemaVersion("2004 3rd Edition");
       setLangString("string");
 	}
 
     @Override
 	public Element createMetadataCopyright(int licenseCode)
     {
         //imsmd:copyright
         Element mdCopyright = createLOMElement("imsmd:copyrightAndOtherRestrictions", "copyrightAndOtherRestrictions");
 
         Element mdSource = createLOMElement("imsmd:source", "source");
         mdSource.setText("LOMv1.0");
         mdCopyright.add(mdSource);
         // if public domain then no restrictions are applied
         // and for all other licenses restrictions are applied
         Element mdValue = createLOMElement("imsmd:value", "value");
         if(licenseCode != RESOURCE_LICENSE_PD_CODE)
             mdValue.setText("yes");
         else mdValue.setText("no");
         mdCopyright.add(mdValue);
 
         return mdCopyright;
     }
 
     /*
      *  process section type and create resource element object
      */
     @Override
     public void createResourceElement(Section section, Element resource, byte[] content_data1, File resoucesDir, String imagespath, String resource_id, String sectionFileName,int i) throws Exception
     {
         if (section.getContentType().equals("typeLink")){
             String linkData = new String(content_data1);
 
             if(linkData.startsWith(ServerConfigurationService.getServerUrl()) && linkData.indexOf("/access/content/group")!= -1)
             {
                 String link_resource_id = meleteUtil.replace(linkData,ServerConfigurationService.getServerUrl()+"/access/content","");
 
                 // read resource and create a file
                 ArrayList link_content = new ArrayList();
                 byte[] linkdata =setContentResourceData(link_resource_id, link_content);
                 if(linkdata == null) {resource =null;return;}
                 if(!((String)link_content.get(2)).equals(getMeleteCHService().MIME_TYPE_LINK))
                 {
                     logger.debug("link resource points to site res item as file. Include file in zip");
                     // Site resource item is file and not URL
                 String resfileName = Validator.escapeResourceName((String)link_content.get(0));
                 File resfile = new File(resoucesDir+ "/"+ resfileName);
                 createFileFromContent(linkdata, resfile.getAbsolutePath());
 
                 Element file = resource.addElement("file");
                 file.addAttribute("href", "resources/"+ resfileName);
                 }
             }
 //           resource will always point to link location otherwise it changes type to upload on import
             resource.addAttribute("href", linkData);
         }else if (section.getContentType().equals("typeEditor")){
             Element file = resource.addElement("file");
             String fileName = sectionFileName;
 
             if (fileName.startsWith("module_"))
             {
                 int und_index = fileName.indexOf("_",7);
                 fileName = fileName.substring(und_index+1, fileName.length());
             }
 
             file.addAttribute("href", "resources/"+ fileName);
             resource.addAttribute("href", "resources/"+ fileName);
 
             //read the content to modify the path for images
 
             //replace image path and create image files
             ArrayList rData = replaceImagePath(new String(content_data1), imagespath, resource,false,new HashSet<String>(),null);
             String modSecContent = (String)rData.get(0);
             //create the file
             File resfile = new File(resoucesDir+ "/"+fileName);
             createFileFromContent( modSecContent.getBytes(), resfile.getAbsolutePath());
         }else if(section.getContentType().equals("typeUpload")){
             Element file = resource.addElement("file");
             String fileName = Validator.escapeResourceName(sectionFileName);
 
             if (fileName.startsWith("module_"))
             {
                 int und_index = fileName.indexOf("_",7);
                 fileName = fileName.substring(und_index+1, fileName.length());
             }
 
             file.addAttribute("href", "resources/"+ fileName);
             resource.addAttribute("href", "resources/"+ fileName);
 
             //create the file
             File resfile = new File(resoucesDir+ "/"+ fileName);
             createFileFromContent(content_data1, resfile.getAbsolutePath());
         }
         else if (section.getContentType().equals("notype"))
         {
         	 Element file = resource.addElement("file");
              String fileName = sectionFileName;
 
              file.addAttribute("href", "resources/"+ fileName);
              resource.addAttribute("href", "resources/"+ fileName);
 
              //read the content to modify the path for images
 
                   //create the file
              File resfile = new File(resoucesDir+ "/"+fileName);
              createFileFromContent(new String("  ").getBytes(), resfile.getAbsolutePath());
         }
     }
 
     @Override
     public int createSectionElement(Element ParentSection, Section section, int i, int k, Element resources, File resoucesDir, String imagespath) throws Exception
     {
             Element secElement = ParentSection.addElement("item");
             secElement.addAttribute("identifier", "ITEM"+ k);
             Element secTitleEle = secElement.addElement("title");
             secTitleEle.setText(section.getTitle());
             int item_ref_num = k;
             logger.debug("now processing createSectionElement" + section.getTitle());
             // dtd specifies nested item tag to be before imsmd tags.
             if(currItem.hasChildNodes())
             {
                 int size = currItem.getChildNodes().getLength();
                 logger.debug("processing childNodes of " + section.getTitle() + "and no of child nodes are:" +  size);
                 int childNo = 0;
                 while(childNo < size)
                 {
                         currItem = sectionUtil.getNextSection(currItem);
                         k = createSectionElement(secElement, sectionDB.getSection(Integer.parseInt(currItem.getAttribute("id"))), i,++k, resources,resoucesDir,imagespath);
                         childNo++;
                 }
             }
 
             Element imsmdlom = createLOMElement("imsmd:lom", "lom");
             //add section instructions
             if (section.getInstr() != null && section.getInstr().trim().length() > 0)
                {
                 Element imsmdgeneral = imsmdlom.addElement("imsmd:general");
                 imsmdgeneral.add(createMetadataDescription(section.getInstr()));
                }
             // add section instructions end
 
             // if content exists then create resource object otherwise just create item object
             if(section.getSectionResource() != null)
             {
                 MeleteResource meleteResource = (MeleteResource)section.getSectionResource().getResource();
                 if(meleteResource == null) return k;
                 String content_resource_id = meleteResource.getResourceId();
                 ArrayList content_data = new ArrayList();
                 byte[] content_data1 =setContentResourceData(content_resource_id,content_data);
 
                 if(content_data1 == null || content_data == null) return k;
                 //Rashmi - if no resources are written then see if createResourceElement needs a return type
                 Element resource = resources.addElement("resource");
                 resource.addAttribute("identifier","RESOURCE"+ item_ref_num);
                 resource.addAttribute("type ","webcontent");
                 resource.addAttribute("adlcp:scormType","asset");
                 createResourceElement(section, resource, content_data1, resoucesDir, imagespath,null,(String)content_data.get(0),i);
                 secElement.addAttribute("identifierref", resource.attributeValue("identifier"));
                 // add copyright information - rashmi
                 Element imsmdright = imsmdlom.addElement("imsmd:rights");
 
 
                 imsmdright.add(createMetadataCopyright(meleteResource.getLicenseCode()));
 
                 // add license description
                 Element mdLicenseDesc = createLOMElement("imsmd:description", "description");
                 Element mdLangString2 = createLOMElement("imsmd:"+ getLangString(), getLangString());
                 String lurl = createLicenseUrl(meleteResource.getLicenseCode(),meleteResource.getCcLicenseUrl(),meleteResource.getCopyrightOwner(),meleteResource.getCopyrightYear());
                 mdLangString2.setText(lurl);
                 mdLicenseDesc.add(mdLangString2);
                 imsmdright.add(mdLicenseDesc);
                 // copyright info add end
 
                 secElement.add(imsmdlom);
             }   // end if contents
             else
             {
             	 Element resource = resources.addElement("resource");
                  resource.addAttribute("identifier","RESOURCE"+ item_ref_num);
                  resource.addAttribute("type ","webcontent");
                  resource.addAttribute("adlcp:scormType","asset");
                  createResourceElement(section, resource, null, resoucesDir, imagespath,null,"nocontent.html",i);
                  secElement.addAttribute("identifierref", resource.attributeValue("identifier"));
 
 
             }
     return k;
     }
 
     /**
      * adds organization and resource items tomanifest
      * @param modDateBeans - module date beans
      * @param packagedir - package directory
      * @return - returns the list of manifest elements
      * @throws Exception
      */
     @Override
     public List generateOrganizationResourceItems(List modList,boolean allFlag, File packagedir,String maintitle, String courseId)throws Exception{
     	     String probEncounteredSections ="";
         try{
             String packagedirpath = packagedir.getAbsolutePath();
             String resourcespath  = packagedirpath + File.separator + "resources";
             File resoucesDir = new File(resourcespath);
             if (!resoucesDir.exists())resoucesDir.mkdir();
             String imagespath  = resoucesDir.getAbsolutePath() + File.separator + "images";
 
             Element organizations = createOrganizations();
             Element resources = createResources();
             Element organization = addOrganization(organizations);
             organizations.addAttribute("default", organization.attributeValue("identifier"));
 
             Iterator modIter = modList.iterator();
             int i = 0,k=0;
             //create item for each module and items under the module item for
             // scetions
             Element mainTitle = organization.addElement("title");
             mainTitle.setText(maintitle);
             while (modIter.hasNext()){
                 Module module = (Module) modIter.next();
 
                 Element modMainItem = organization.addElement("item");
                 modMainItem.addAttribute("identifier", "MF01_ORG1_MELETE_MOD"+ ++i);
 
                 Element title = modMainItem.addElement("title");
                 if (module.getTitle() != null && module.getTitle().trim().length() > 0)
                     title.setText(module.getTitle());
 
                 String sectionsSeqList = module.getSeqXml();
                 sectionUtil = new SubSectionUtilImpl();
 
                 if (sectionsSeqList != null){
                     sectionUtil.getSubSectionW3CDOM(sectionsSeqList);
                     currItem = null;
                     //create items and resources for sections
                     while ((currItem = sectionUtil.getNextSection(currItem)) != null){
                         logger.debug("exporting item from generateOrgan" + currItem.getAttribute("id"));
                         Section section = sectionDB.getSection(Integer.parseInt(currItem.getAttribute("id")));
                         try{
                             // create secElement only if data exists
                             logger.debug("exporting section from generateOrgan" + section.getTitle());
                             k = createSectionElement(modMainItem, section, i,++k, resources,resoucesDir,imagespath);
                             } // if end add secElement only if content exists
                         catch(Exception e){
                             Section probSection = sectionDB.getSection(Integer.parseInt(currItem.getAttribute("id")));
                             probEncounteredSections += module.getTitle() +" section: "+ probSection.getTitle();
                             logger.debug("problems found in impl" + probEncounteredSections);
                             throw new MeleteException(probEncounteredSections);
                         //  continue;
                             }
                     }
                 }
                 else
                 {
                 	if (module.getWhatsNext() == null || module.getWhatsNext().trim().length() == 0)
                     {
                 	 Element resource = resources.addElement("resource");
                      resource.addAttribute("identifier","RESOURCE"+ ++k);
                      resource.addAttribute("type ","webcontent");
                      resource.addAttribute("adlcp:scormType","asset");
 
                      File resfile = new File(resoucesDir+ "/nocontent.html");
                      createFileFromContent(new String("  ").getBytes(), resfile.getAbsolutePath());
                      Element file = resource.addElement("file");
                      file.addAttribute("href", "resources/nocontent.html");
                      resource.addAttribute("href", "resources/nocontent.html");
                      modMainItem.addAttribute("identifierref", resource.attributeValue("identifier"));
 
                     }
                 }
                 //       add next steps as the last section of the module by rashmi
                 if (module.getWhatsNext() != null && module.getWhatsNext().trim().length() > 0)
                 {
                     Element whatsNextElement = modMainItem.addElement("item");
                     whatsNextElement.addAttribute("identifier", "NEXTSTEPS"+ ++k);
 
                     Element nextTitleEle = whatsNextElement.addElement("title");
                     nextTitleEle.setText("NEXTSTEPS");
 
                     Element resource = resources.addElement("resource");
                     resource.addAttribute("identifier","RESOURCE"+ k);
                     resource.addAttribute("type ","webcontent");
                     resource.addAttribute("adlcp:scormType","asset");
 
 //                  create the file
                     File resfile = new File(resoucesDir+ "/module_"+ i +"_nextsteps.html");
                     createFileFromContent( module.getWhatsNext().getBytes(), resfile.getAbsolutePath());
                     whatsNextElement.addAttribute("identifierref", resource.attributeValue("identifier"));
                     Element file = resource.addElement("file");
                     file.addAttribute("href", "resources/module_"+ i +"_nextsteps.html");
                     resource.addAttribute("href", "resources/module_"+ i +"_nextsteps.html");
                 }
             // add next steps end
 
                 //add module description thru metadata
                 Element imsmdlom = createLOMElement("imsmd:lom", "lom");
                 Element imsmdgeneral = imsmdlom.addElement("imsmd:general");
 
                 if (module.getDescription() != null && module.getDescription().trim().length() > 0)
                     imsmdgeneral.add(createMetadataDescription(module.getDescription()));
 
                 // add keyword if available - rashmi
                 if (module.getKeywords() != null && module.getKeywords().trim().length() > 0)
                     imsmdgeneral.add(createMetadataKeyword(module.getKeywords()));
 
                 modMainItem.add(imsmdlom);
 
             }
             if (allFlag == true) resources = transferManageItems(resources, courseId, resoucesDir, k+1);
             ArrayList manElements = new ArrayList();
             manElements.add(organizations);
             manElements.add(resources);
             manElements.add(probEncounteredSections);
             return manElements;
 
         }catch(Exception e){
             logger.debug("i am catching it");
             throw e;
         }
     }
 
     public Element transferManageItems(Element resources, String courseId, File resoucesDir, int item_ref_num) throws Exception
 	{
 		String fromUploadsColl = Entity.SEPARATOR+"private"+ REFERENCE_ROOT+ Entity.SEPARATOR+courseId+Entity.SEPARATOR+"uploads"+Entity.SEPARATOR;
 		List fromContextList = meleteCHService.getMemberNamesCollection(fromUploadsColl);
 		if ((fromContextList != null)&&(fromContextList.size() > 0))
 		{
 		  List meleteResourceList = sectionDB.getAllMeleteResourcesOfCourse(courseId);
 		  if ((meleteResourceList != null)&&(meleteResourceList.size() > 0))
 		  {
 			  fromContextList.removeAll(meleteResourceList);
		  }	  
 		  if ((fromContextList != null)&&(fromContextList.size() > 0))
 		  {
 			  ListIterator repIt = fromContextList.listIterator();
 				while (repIt != null && repIt.hasNext())
 				{
 					String content_resource_id = (String) repIt.next();
 					ArrayList content_data = new ArrayList();
 					logger.debug("calling secContent from create section");
 					Element resource = resources.addElement("resource");
 					resource.addAttribute("identifier","MANAGERESOURCE"+ item_ref_num);
 	                resource.addAttribute("type ","webcontent");
 	                resource.addAttribute("adlcp:scormType","asset");
 	                byte[] content_data1 =setContentResourceData(content_resource_id,content_data);
 					String sectionFileName = (String)content_data.get(0);
 					if(((String)content_data.get(2)).equals(getMeleteCHService().MIME_TYPE_LINK))
 			 		{
 							String linkData = new String(content_data1);
 
 							if(linkData.startsWith(ServerConfigurationService.getServerUrl()) && linkData.indexOf("/access/content/group")!= -1)
 				            {
 				                String link_resource_id = meleteUtil.replace(linkData,ServerConfigurationService.getServerUrl()+"/access/content","");
 
 				                // read resource and create a file
 				                ArrayList link_content = new ArrayList();
 				                byte[] linkdata =setContentResourceData(link_resource_id, link_content);
 				                if(linkdata == null) {continue;}
 				                if(!((String)link_content.get(2)).equals(getMeleteCHService().MIME_TYPE_LINK))
 				                {
 				                    logger.debug("link resource points to site res item as file. Include file in zip");
 				                    // Site resource item is file and not URL
 				                String resfileName = Validator.escapeResourceName((String)link_content.get(0));
 				                File resfile = new File(resoucesDir+ "/"+ resfileName);
 				                createFileFromContent(linkdata, resfile.getAbsolutePath());
 
 				                Element file = resource.addElement("file");
 				                file.addAttribute("href", "resources/"+ resfileName);
 				                }
 				            }
 //				           resource will always point to link location otherwise it changes type to upload on import
 				            resource.addAttribute("href", linkData);
 						}
 						else
 						{
 							Element file = resource.addElement("file");
 				            String fileName = Validator.escapeResourceName(sectionFileName);
 
 				            if (fileName.startsWith("module_"))
 				            {
 				                int und_index = fileName.indexOf("_",7);
 				                fileName = fileName.substring(und_index+1, fileName.length());
 				            }
 
 				            file.addAttribute("href", "resources/"+ fileName);
 				            resource.addAttribute("href", "resources/"+ fileName);
 
 				            //create the file
 				            File resfile = new File(resoucesDir+ "/"+ fileName);
 				            createFileFromContent(content_data1, resfile.getAbsolutePath());
 					}
 					item_ref_num++;
 
 				}//End while repIt
 		  }
 		}
 		return resources;
 	}
 }
