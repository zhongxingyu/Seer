 /**********************************************************************************
 *
 * $URL$
 *
 ***********************************************************************************
 * Copyright (c) 2008 Etudes, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
 *******************************************************************************/
 
 package org.sakaiproject.component.app.melete;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.Namespace;
 import org.dom4j.QName;
 import org.sakaiproject.api.app.melete.MeleteCHService;
 import org.sakaiproject.api.app.melete.MeleteExportService;
 import org.sakaiproject.api.app.melete.MeleteSecurityService;
 import org.sakaiproject.api.app.melete.exception.MeleteException;
 import org.sakaiproject.api.app.melete.util.XMLHelper;
 import org.sakaiproject.component.app.melete.MeleteUtil;
 import org.xml.sax.SAXException;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.util.Validator;
 import org.sakaiproject.entity.cover.EntityManager;
 import org.sakaiproject.entity.api.Reference;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.id.cover.IdManager;

 /**
  * @author Faculty
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public abstract class MeleteAbstractExportServiceImpl implements MeleteExportService{
 
 	/** Dependency:  The logging service. */
 	protected Log logger = LogFactory.getLog(MeleteAbstractExportServiceImpl.class);
 
 	/**default namespace and metadata namespace*/
 	protected String DEFAULT_NAMESPACE_URI = "http://www.imsglobal.org/xsd/imscp_v1p1";
 
 	protected int RESOURCE_LICENSE_CODE = 0; //not determined yet
 	protected String RESOURCE_LICENSE_URL = "I have not determined copyright yet"; //No license
 	protected int RESOURCE_LICENSE_COPYRIGHT_CODE = 1; //Copyright of author
 	protected int RESOURCE_LICENSE_PD_CODE = 2; //		Public Domain
 	protected int RESOURCE_LICENSE_CC_CODE = 3; //Creative Commons
 	protected int RESOURCE_LICENSE_FAIRUSE_CODE = 4; //FairUse Exception
 
     protected MeleteCHService meleteCHService;
     protected MeleteLicenseDB meleteLicenseDB;
 	protected SectionDB sectionDB;
     protected SubSectionUtilImpl sectionUtil;
     protected org.w3c.dom.Element currItem = null;
     protected MeleteUtil meleteUtil = new MeleteUtil();
     protected String metaDataNameSpace;
     protected String schema;
     protected String schemaVersion;
     protected String langString;
 
     abstract public void initValues();
     abstract public Element createMetadataCopyright(int licenseCode);
     abstract void createResourceElement(Section section, Element resource, byte[] content_data1, File resoucesDir, String imagespath, String sectionFileName,int i) throws Exception;
     abstract public int createSectionElement(Element ParentSection, Section section, int i, int k, Element resources, File resoucesDir, String imagespath) throws Exception;
     abstract public List generateOrganizationResourceItems(List modList, File packagedir,String maintitle)throws Exception;
 
 	/**
 	 * Establish my logger component dependency.
 	 * @param logger the logger component.
 	 */
 	public void setLogger(Log logger){
 		this.logger = logger;
 	}
 	/**
 	 * Final initialization, once all dependencies are set.
 	 */
 	public void init(){
 		logger.debug(this +".init()");
 	}
 
 	/**
 	 * Final cleanup.
 	 */
 	public void destroy(){
 		logger.debug(this +".destroy()");
 	}
 
 	String getMetaDataNameSpace(){
         return metaDataNameSpace;
     }
 
 	void setMetaDataNameSpace(String metaDataNameSpace){
         this.metaDataNameSpace = metaDataNameSpace;
     }
 
 	String getSchema(){
         return this.schema;
     }
 
 	void setSchema(String schema){
         this.schema = schema;
     }
 
 	String getSchemaVersion(){
         return schemaVersion;
     }
 
 	void setSchemaVersion(String schemaVersion) {
 		this.schemaVersion = schemaVersion;
 	}
 
 	String getLangString(){
         return langString;
     }
 
 	void setLangString(String langString) {
 		this.langString = langString;
 	}
 
 	/**
 	 * creates document root element "manifest" and adds the namespaces
 	 *
 	 * @return returns the manifest element
 	 * @throws  Exception
 	 */
 	public Element createManifest() throws Exception {
 		Element root = DocumentHelper.createElement("manifest");
 		//Set up the necessary namespaces
 		root.setQName(new QName("manifest", new Namespace(null,	DEFAULT_NAMESPACE_URI)));
 		root.add(new Namespace("imsmd",getMetaDataNameSpace()));
 		root.add(new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"));
 
 		/*root.addAttribute("xsi:schemaLocation",
 				"http://www.imsglobal.org/xsd/imscp_v1p1 "
 						+ "http://www.imsglobal.org/xsd/imscp_v1p1.xsd "
 						+ "http://www.imsglobal.org/xsd/imsmd_v1p2 "
 						+ "http://www.imsglobal.org/xsd/imsmd_v1p2.xsd ");
 		*/
 
		root.addAttribute("identifier", "Manifest-" + getUUID());
 		root.addAttribute("version", "IMS CP 1.1.4");
 		return root;
 	}
 
 	/**
 	 * creates document root element "manifest" from the default manifest file
 	 * and adds the namespaces
 	 * @param xmlFile - Default manifest file
 	 * @return returns the manifest element
 	 * @throws  Exception
 	 */
 	public Element getManifest(File xmlFile) throws Exception {
 		try {
 			Document document = XMLHelper.getSaxReader().read(xmlFile);
 			Element root = document.getRootElement();
 			Element rootnew = root.createCopy();
 			List childEleList  = rootnew.elements();
 			childEleList.clear();
 
 			this.DEFAULT_NAMESPACE_URI = rootnew.getNamespaceURI();
 
 			List nslist = rootnew.declaredNamespaces();
 
 			for (int i=0; i<nslist.size(); i++){
 				if (((Namespace)nslist.get(i)).getPrefix().equals("imsmd")){
 					setMetaDataNameSpace(((Namespace)nslist.get(i)).getURI());
 					break;
 				}
 			}
			rootnew.addAttribute("identifier", "Manifest-" + getUUID());
 			return rootnew;
 		} catch (DocumentException de) {
 			throw de;
 		} catch (SAXException se) {
 			throw se;
 		}catch (Exception e) {
 			throw e;
 		}
 	}
 
 	/**
 	 * create manifest metadata element with schema and schemaversion elements
 	 *
 	 * @return - returns metadata element
 	 */
 	public Element createManifestMetadata() {
         Element metadata = createDefaultNSElement("metadata", "metadata");
 
         //schema element
         Element schema = createDefaultNSElement("schema", "schema");
 
         schema.setText(getSchema());
         metadata.add(schema);
 
         //schema version element
         Element schemaVersion = createDefaultNSElement("schemaversion", "schemaversion");
         schemaVersion.setText(getSchemaVersion());
         metadata.add(schemaVersion);
 
         return metadata;
     }
 
 
 	/**
 	 * creates the default namespace element
 	 * @param elename - element name
 	 * @param qname - qualified name
 	 * @return - returns the default namespace element
 	 */
 	public Element createDefaultNSElement(String elename, String qname) {
 		Element metadata = DocumentHelper.createElement(elename);
         metadata.setQName(new QName(qname,new Namespace(null, DEFAULT_NAMESPACE_URI)));
 		return metadata;
 	}
 
 
 	/**
 	 * creates the LOM metadata element
 	 * @param elename - element name
 	 * @param qname - qualified name
 	 * @return - returns the metadata element
 	 */
 	public Element createLOMElement(String elename, String qname) {
 
 		Element imsmdlom = DocumentHelper.createElement(elename);
 		imsmdlom.setQName(new QName(qname,new Namespace("imsmd", getMetaDataNameSpace())));
 
 		return imsmdlom;
 	}
 
 	/**
 	 * creates metadata title element
 	 * @param title - title
 	 * @return - returns the title element
 	 */
 	public Element createMetadataTitle(String title) {
 		//imsmd:title
         Element imsmdtitle = createLOMElement("imsmd:title", "title");
 
         //imsmd:langstring
         Element imsmdlangstring = createLOMElement("imsmd:"+getLangString(), getLangString());
         //imsmdlangstring.addAttribute("xml:lang", "en-US");
         imsmdlangstring.setText(title);
 
         imsmdtitle.add(imsmdlangstring);
 
         return imsmdtitle;
 	}
 
 	/**
 	 * creates metadata description element
 	 * @param description - description
 	 * @return - returns the metadata description element
 	 */
 	public Element createMetadataDescription(String description) {
 		//imsmd:description
 		Element mdDesc = createLOMElement("imsmd:description", "description");
 
 		//imsmd:langstring
 		Element mdLangString = createLOMElement("imsmd:"+getLangString(), getLangString());
 		//mdLangString.addAttribute("xml:lang", "en-US");
 		mdLangString.setText(description);
 
 		mdDesc.add(mdLangString);
 
 		return mdDesc;
 	}
 
 	/*
 	 * create keyword element
 	 * add by rashmi
 	 */
 	public Element createMetadataKeyword(String keyword) {
 		//imsmd:keyword
 		Element mdKeyword = createLOMElement("imsmd:keyword", "keyword");
 
 		//imsmd:langstring
 		Element mdLangString = createLOMElement("imsmd:"+getLangString(), getLangString());
 		//mdLangString.addAttribute("xml:lang", "en-US");
 		mdLangString.setText(keyword);
 
 		mdKeyword.add(mdLangString);
 
 		return mdKeyword;
 	}
 
 
 	/*
 	 * create license url for manifest file
 	 * add by rashmi
 	 */
 	String createLicenseUrl (int lcode, String lurl, String owner, String year)
 	{
 		if(lcode == RESOURCE_LICENSE_CODE) return RESOURCE_LICENSE_URL;
 		if (lcode == RESOURCE_LICENSE_COPYRIGHT_CODE) return lurl;
 
 		if(lcode == RESOURCE_LICENSE_PD_CODE || lcode == RESOURCE_LICENSE_CC_CODE)
 		{
 			lurl = meleteLicenseDB.fetchCcLicenseName(lurl);
 			if(lcode == RESOURCE_LICENSE_CC_CODE)
 						lurl = "Creative Commons " + lurl;
 		}
 		if(owner != null && (owner=owner.trim()).length() !=0) {lurl = lurl + "," + owner;}
 		if(year != null && (year = year.trim()).length() !=0) {lurl = lurl + "," + year;}
 
 		return lurl;
 	}
 
 	/*
 	 * get resource information from content resource object
 	 */
 	byte[] setContentResourceData(String resourceId, ArrayList data)throws Exception
 	{
 		try{
 			if(resourceId != null)
 	    	{
 			resourceId = URLDecoder.decode(resourceId,"UTF-8");
 	       	ContentResource cr = getMeleteCHService().getResource(resourceId);
 	       	if(cr == null)return null;
 
 	       	data.add(cr.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
 	       	data.add(cr.getProperties().getProperty(ResourceProperties.PROP_DESCRIPTION));
 	       	data.add(cr.getContentType());
 	       	return cr.getContent();
 	    	}
 		}
 		catch(IdUnusedException unuse){
 			// if file not found exception or content is missing continue working
 			logger.error("error in reading resource content in export section");
 			return null;
 			}
 		catch(Exception e){
 			logger.error("error in reading resource in export section");
 			throw e;
 		}
 		return null;
 	}
 
 	/**
 	 * replace image path in the section content for uploaded images thru
 	 * content editor and create the image files under resources/images
 	 * @param secContent
 	 * @param imagespath
 	 * @param resource
 	 * @return the content with modifed image path
 	 */
 	String replaceImagePath(String secContent, String imagespath, Element resource)throws Exception{
 		StringBuffer strBuf = new StringBuffer();
 		String checkforimgs = secContent;
 		int imgindex = -1;
 
 		String imgSrcPath, imgName, imgLoc;
 		String modifiedSecContent = new String(secContent);
 	//	meletedocsdirpath = meleteDocsDirPath;
 
 		try {
 			File imagesDir = new File(imagespath);
 
 			if (!imagesDir.exists())
 				imagesDir.mkdir();
 
 			int startSrc =0;
 			int endSrc = 0;
 
 			while(checkforimgs !=null) {
 
 				ArrayList embedData = meleteUtil.findEmbedItemPattern(checkforimgs);
 				checkforimgs = (String)embedData.get(0);
 				if (embedData.size() > 1)
 				{
 					startSrc = ((Integer)embedData.get(1)).intValue();
 					endSrc = ((Integer)embedData.get(2)).intValue();
 				}
 				if (endSrc <= 0) break;
 				imgSrcPath = checkforimgs.substring(startSrc, endSrc);
 				if(imgSrcPath.indexOf("/access") !=-1)
 				{
 				String findEntity = imgSrcPath.substring(imgSrcPath.indexOf("/access")+7);
 				Reference ref = EntityManager.newReference(findEntity);
 				logger.debug("ref properties" + ref.getType() +"," +ref.getId());
 
 				if(ref.getType().equals(ContentHostingService.APPLICATION_ID) || ref.getType().equals(MeleteSecurityService.APPLICATION_ID))
 				{
 					String img_resource_id =ref.getId() ;
 					if(ref.getType().equals(MeleteSecurityService.APPLICATION_ID))
 						img_resource_id =img_resource_id.replaceFirst("/content","");
 
 					if(ref.getType().equals(ContentHostingService.APPLICATION_ID) && !ref.getId().startsWith("/group"))
 					{
 						// not a site resource item so make it a full URL
 						String patternStr = imgSrcPath;
 						String replacementStr =ServerConfigurationService.getServerUrl() + imgSrcPath;
 						modifiedSecContent = meleteUtil.replace(modifiedSecContent,patternStr, replacementStr);
 						return modifiedSecContent;
 					}
 
 					ArrayList img_content = new ArrayList();
 					byte[] img_data =setContentResourceData(img_resource_id, img_content);
 					if(img_data == null) {
 						checkforimgs =checkforimgs.substring(endSrc);
 			            startSrc=0; endSrc = 0;
 						continue;
 					}
 					imgName= (String)img_content.get(0);
 					imgName = Validator.escapeResourceName(imgName);
 					createFileFromContent(img_data,imagesDir.getAbsolutePath()+File.separator+ imgName);
 
 					String patternStr = imgSrcPath;
 					String replacementStr = "images/"+ imgName;
 					Pattern pattern = Pattern.compile(Pattern.quote(patternStr));
 
 					// Replace all occurrences of pattern in input
 					modifiedSecContent = meleteUtil.replace(modifiedSecContent,patternStr, replacementStr);
 
 					//add image to resources element
 					Element file = resource.addElement("file");
 					file.addAttribute("href", "resources/images/"+ imgName);
 					}
 				}
 				else if(imgSrcPath.startsWith("/")){
 					//internal link resides somewhere within sakai
 					String patternStr = imgSrcPath;
 					String replacementStr =ServerConfigurationService.getServerUrl() + imgSrcPath;
 					modifiedSecContent = meleteUtil.replace(modifiedSecContent,patternStr, replacementStr);
 					return modifiedSecContent;
 				}
 				checkforimgs =checkforimgs.substring(endSrc);
 	            startSrc=0; endSrc = 0;
 			}
 		}catch (Exception e) {
 			throw e;
 		}
 
 		return modifiedSecContent;
 	}
 
 	/**
 	 * creates file from input path to output path
 	 * @param inputpath - input path for file
 	 * @param outputpath - output path for file
 	 * @throws Exception
 	 */
 	public void createFile(String inputurl, String outputurl)throws Exception{
 		FileInputStream in = null;
 		FileOutputStream out = null;
 		try {
 			File inputFile = new File(inputurl);
 			File outputFile = new File(outputurl);
 			in = new FileInputStream(inputFile);
 			out = new FileOutputStream(outputFile);
 			int c;
 			int len;
 			byte buf[] = new byte[102400];
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 		} catch (FileNotFoundException e) {
 			logger.error(e.toString());
 		} catch (IOException e) {
 			throw e;
 		} finally{
 			try {
 				if (in != null)
 					in.close();
 			} catch (IOException e1) {
 			}
 			try {
 				if (out != null)
 					out.close();
 			} catch (IOException e2) {
 			}
 		}
 	}
 
 	/**
 	 * creates file from input path to output path
 	 * @param inputpath - input path for file
 	 * @param outputpath - output path for file
 	 * @throws Exception
 	 */
 	public void createFileFromContent(byte[] content, String outputurl)throws Exception{
 		FileOutputStream fout = new FileOutputStream(new File(outputurl));
 		try {
 			fout.write(content);
 			fout.flush();
 		} catch (IOException e) {
 			throw e;
 		} catch (Exception e) {
 			throw e;
 		} finally {
 			if (fout != null)
 				fout.close();
 		}
 	}
 
 	/**
 	 * deletes the file and its children
 	 * @param delfile - file to be deleted
 	 */
 	public void deleteFiles(File delfile){
 
 		if (delfile.isDirectory()){
 			File files[] = delfile.listFiles();
 			int i = files.length;
 			while (i > 0)
 				deleteFiles(files[--i]);
 
 			delfile.delete();
 		}else
 			delfile.delete();
 
 	}
 
 	/**
 	 * gets UUID
 	 * @return - returns the UUID
 	 */
	String getUUID() {
		return IdManager.createUuid();
 	}
 
 
 	/**
 	 * creates organizations element
 	 * @return returns organizations element
 	 */
 	Element createOrganizations(){
 		return createDefaultNSElement("organizations", "organizations");
 	}
 
 	/**
 	 * creates resources element
 	 * @return returns resources element
 	 */
 	Element createResources(){
 
 		return createDefaultNSElement("resources", "resources");
 	}
 
 	/**
 	 * add organization for melete modules
 	 * @param organizations - organizations element
 	 */
 	Element addOrganization(Element organizations) {
 		Element organization = organizations.addElement("organization");
 		organization.addAttribute("identifier", "MF01_ORG1_MELETE");
 		organization.addAttribute("structure", "hierarchical");
 
 		return organization;
 	}
 
 	/**
 	 * @return Returns the meleteCHService.
 	 */
 	public MeleteCHService getMeleteCHService() {
 		return meleteCHService;
 	}
 	/**
 	 * @return Returns the meleteLicenseDB.
 	 */
 	public MeleteLicenseDB getMeleteLicenseDB() {
 		return meleteLicenseDB;
 	}
 	/**
 	 * @return Returns the sectionDB.
 	 */
 	public SectionDB getSectionDB() {
 		return sectionDB;
 	}
 	/**
 	 * @param sectionDB The sectionDB to set.
 	 */
 	public void setSectionDB(SectionDB sectionDB) {
 		this.sectionDB = sectionDB;
 	}
 	/**
 	 * @param meleteCHService The meleteCHService to set.
 	 */
 	public void setMeleteCHService(MeleteCHService meleteCHService) {
 		this.meleteCHService = meleteCHService;
 	}
 	/**
 	 * @param meleteLicenseDB The meleteLicenseDB to set.
 	 */
 	public void setMeleteLicenseDB(MeleteLicenseDB meleteLicenseDB) {
 		this.meleteLicenseDB = meleteLicenseDB;
 	}
 }
