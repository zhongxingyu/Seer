 /* ===============================================================================
  *
  * Part of the InfoGlue Content Management Platform (www.infoglue.org)
  *
  * ===============================================================================
  *
  *  Copyright (C)
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License version 2, as published by the
  * Free Software Foundation. See the file LICENSE.html for more information.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY, including the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc. / 59 Temple
  * Place, Suite 330 / Boston, MA 02111-1307 / USA.
  *
  * ===============================================================================
  */
 
 package org.infoglue.cms.applications.managementtool.actions;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.exolab.castor.jdo.Database;
 import org.exolab.castor.mapping.Mapping;
 import org.exolab.castor.xml.Marshaller;
 import org.infoglue.cms.applications.common.VisualFormatter;
 import org.infoglue.cms.applications.common.actions.InfoGlueAbstractAction;
 import org.infoglue.cms.controllers.kernel.impl.simple.CastorDatabaseService;
 import org.infoglue.cms.controllers.kernel.impl.simple.CategoryController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentController;
 import org.infoglue.cms.controllers.kernel.impl.simple.ContentTypeDefinitionController;
 import org.infoglue.cms.controllers.kernel.impl.simple.RepositoryController;
 import org.infoglue.cms.controllers.kernel.impl.simple.SiteNodeController;
 import org.infoglue.cms.entities.content.Content;
 import org.infoglue.cms.entities.content.impl.simple.ContentImpl;
 import org.infoglue.cms.entities.management.Repository;
 import org.infoglue.cms.entities.management.impl.simple.InfoGlueExportImpl;
 import org.infoglue.cms.entities.structure.SiteNode;
 import org.infoglue.cms.entities.structure.impl.simple.SiteNodeImpl;
 import org.infoglue.cms.util.CmsPropertyHandler;
 
 
 /**
  * This class handles Exporting of a repository to an XML-file.
  * 
  * @author mattias
  */
 
 public class ExportRepositoryAction extends InfoGlueAbstractAction
 {
     private final static Logger logger = Logger.getLogger(ExportRepositoryAction.class.getName());
 
 	private Integer repositoryId = null;
 	private List repositories = new ArrayList();
 	
 	private String fileUrl 	= "";
 	private String fileName = "";
 
 	/**
 	 * This shows the dialog before export.
 	 * @return
 	 * @throws Exception
 	 */	
 
 	public String doInput() throws Exception
 	{
 		repositories = RepositoryController.getController().getRepositoryVOList();
 		
 		return "input";
 	}
 	
 	/**
 	 * This handles the actual exporting.
 	 */
 	
 	protected String doExecute() throws Exception 
 	{
 		Database db = CastorDatabaseService.getDatabase();
 		
 		try 
 		{
 			Mapping map = new Mapping();
 			String exportFormat = CmsPropertyHandler.getExportFormat();
 
 			if(exportFormat.equalsIgnoreCase("2"))
 			{
 				logger.info("MappingFile:" + CastorDatabaseService.class.getResource("/xml_mapping_site_2.5.xml").toString());
 				map.loadMapping(CastorDatabaseService.class.getResource("/xml_mapping_site_2.5.xml").toString());
 			}
 			else
 			{
 				logger.info("MappingFile:" + CastorDatabaseService.class.getResource("/xml_mapping_site.xml").toString());
 				map.loadMapping(CastorDatabaseService.class.getResource("/xml_mapping_site.xml").toString());
 			}
 			
 			// All ODMG database access requires a transaction
 			db.begin();
 
 			List siteNodes = new ArrayList();
 			List contents = new ArrayList();
 			
 			String names = "";
 			String[] repositories = getRequest().getParameterValues("repositoryId");
 			for(int i=0; i<repositories.length; i++)
 			{
 				Integer repositoryId = new Integer(repositories[i]);
 				Repository repository 	= RepositoryController.getController().getRepositoryWithId(repositoryId, db);
 				SiteNode siteNode 		= SiteNodeController.getController().getRootSiteNode(repositoryId, db);
 				Content content 		= ContentController.getContentController().getRootContent(repositoryId, db);
 				//System.out.println("content type def:" + content.getContentTypeDefinitionId());
 				siteNodes.add(siteNode);
 				contents.add(content);
 				names = names + "_" + repository.getName();
 			}
 			
 			List contentTypeDefinitions = ContentTypeDefinitionController.getController().getContentTypeDefinitionList(db);
 			List categories = CategoryController.getController().findAllActiveCategories();
 			
 			InfoGlueExportImpl infoGlueExportImpl = new InfoGlueExportImpl();
 			
 			VisualFormatter visualFormatter = new VisualFormatter();
			names = new VisualFormatter().replaceNonAscii(names, '_');
 			String fileName = "Export_" + names + "_" + visualFormatter.formatDate(new Date(), "yyyy-MM-dd") + ".xml";
 			String filePath = CmsPropertyHandler.getDigitalAssetPath();
 			String fileSystemName =  filePath + File.separator + fileName;
 						
 			fileUrl = CmsPropertyHandler.getWebServerAddress() + "/" + CmsPropertyHandler.getDigitalAssetBaseUrl() + "/" + fileName;
 			this.fileName = fileName;
 						
 			String encoding = "UTF-8";
 			File file = new File(fileSystemName);
             FileOutputStream fos = new FileOutputStream(file);
             OutputStreamWriter osw = new OutputStreamWriter(fos, encoding);
             Marshaller marshaller = new Marshaller(osw);
             marshaller.setMapping(map);
 			marshaller.setEncoding(encoding);
             
 			infoGlueExportImpl.getRootContent().addAll(contents);
 			infoGlueExportImpl.getRootSiteNode().addAll(siteNodes);
 			
 			//infoGlueExportImpl.getRootContents().add((ContentImpl)content);
 			//infoGlueExportImpl.getRootSiteNodes().add((SiteNodeImpl)siteNode);
 			/*
 			infoGlueExportImpl.setRootContent((ContentImpl)content);
 			infoGlueExportImpl.setRootSiteNode((SiteNodeImpl)siteNode);
 			*/
 			infoGlueExportImpl.setContentTypeDefinitions(contentTypeDefinitions);
 			infoGlueExportImpl.setCategories(categories);
 			
 			marshaller.marshal(infoGlueExportImpl);
 			
 			osw.flush();
 			osw.close();
 			
 			//fos.flush();
 			//fos.close();
 			
 			db.commit();
 			db.close();
 
 		} 
 		catch (Exception e) 
 		{
 			logger.error("An error was found exporting a repository: " + e.getMessage(), e);
 			db.rollback();
 		}
 		
 		return "success";
 	}
 
 
 	public void setRepositoryId(Integer repositoryId)
 	{
 		this.repositoryId = repositoryId;
 	}
 
 	public String getFileName()
 	{
 		return fileName;
 	}
 
 	public String getFileUrl()
 	{
 		return fileUrl;
 	}
 
 	public Integer getRepositoryId()
 	{
 		return repositoryId;
 	}
 
 	public List getRepositories() 
 	{
 		return repositories;
 	}
 
 }
