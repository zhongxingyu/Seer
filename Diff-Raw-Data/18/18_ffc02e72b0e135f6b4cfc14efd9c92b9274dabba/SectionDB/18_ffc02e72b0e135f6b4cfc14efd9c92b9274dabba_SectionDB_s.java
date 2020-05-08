 /**********************************************************************************
 *
 * $Header: /usr/src/sakai/melete-2.4/melete-impl/src/java/org/sakaiproject/component/app/melete/SectionDB.java,v 1.17 2007/07/13 18:05:18 rashmim Exp $
 *
 ***********************************************************************************
 *
 * Copyright (c) 2004, 2005, 2006, 2007 Foothill College.
  *
  * Licensed under the Educational Community License, Version 1.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.opensource.org/licenses/ecl1.php
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 *
 **********************************************************************************/
 package org.sakaiproject.component.app.melete;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dom4j.Element;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.StaleObjectStateException;
 import org.hibernate.Transaction;
 import org.sakaiproject.api.app.melete.MeleteCHService;
 import org.sakaiproject.api.app.melete.exception.MeleteException;
 
 /**
  * @author Rashmi
  *
  * implements actual operations to the section table
  * and related tables
  * Rashmi - 8/22/06 - revised insertsection() and add insertsectionresource()
  * Rashmi - 1/4/07 - add deassociate section Resource method
  * Mallika -3/1/07- added editsection method for notype
  * Mallika - 3/9/07 - commented code for ME-327
  */
 
 public class SectionDB implements Serializable {
 	private HibernateUtil hibernateUtil;
 	private MeleteBookmarksDB bookmarksDB;
 	
 	 /** Dependency: a logger component. */
 	 private Log logger = LogFactory.getLog(SectionDB.class);
 
 	public SectionDB(){
 		hibernateUtil = getHibernateUtil();
 	}
 	
 	/**
 	 * Add section sets the not-null values not been populated yet and then
 	 * inserts the section into section table.
 	 * update module witht his association to new added section
 	 * If error in committing transaction, it rollbacks the transaction.
 	 */
 	//Mallika - 5/3/06 - This addSection is the new one with section id
 	//in section filename. The previous version of this function is in comments
 	//below
 	//Rashmi - 8/22/06 - according to new section design integrated with CH
 	public Integer addSection(Module module, Section section, boolean fromImport) throws MeleteException
 	{
 		try{
 		     Session session = hibernateUtil.currentSession();
 	         Transaction tx = null;
 			try
 			{
 			  // set default values for not-null fields
 			  section.setCreationDate(new java.util.Date());
 			  section.setModificationDate(new java.util.Date());
 			  section.setModuleId(module.getModuleId().intValue());
 			  section.setDeleteFlag(false);
 			  	// save object
 			  tx = session.beginTransaction();
 			  session.save(section);
 				
 			  if(!fromImport)
 			  {
 	//				 set xml structure for sequencing and placement of sections
 			  String sectionsSeqXML = module.getSeqXml();
 			  SubSectionUtilImpl SectionUtil = new SubSectionUtilImpl();
 			  logger.debug("adding section id to the xmllist" + section.getSectionId().toString());
 			  SectionUtil.addSectiontoList(sectionsSeqXML, section.getSectionId().toString());
 			  sectionsSeqXML = SectionUtil.storeSubSections();
 			  module.setSeqXml(sectionsSeqXML);
 					
 			  session.saveOrUpdate(module);
 			  }
 			  tx.commit();
 			  if (logger.isDebugEnabled()) logger.debug("commiting transaction and new added section id:" + section.getSectionId() + ","+section.getTitle());
 			  return section.getSectionId();
 
 	        }
 			catch(StaleObjectStateException sose)
 		     {
 				logger.error("stale object exception" + sose.toString());
 		     }
 			catch(HibernateException he)
 				     {
 						if(tx !=null) tx.rollback();
 						logger.error(he.toString());
 						throw he;
 				     }
 	        	finally{
 				hibernateUtil.closeSession();
 				 }
 		}catch(Exception ex){
 				// Throw application specific error
 			throw new MeleteException("add_section_fail");
 			}
 		return null;
 	}
 		
 	public Integer editSection( Section section) throws MeleteException
 	{
 		try{
 		     Session session = hibernateUtil.currentSession();
 	         Transaction tx = null;
 			try
 			{
 			  // set default values for not-null fields
 			  section.setCreationDate(new java.util.Date());
 			  section.setModificationDate(new java.util.Date());
 	 	  	  // save object
 			  tx = session.beginTransaction();
 			  session.saveOrUpdate(section);
  		  	  tx.commit();
 			  if (logger.isDebugEnabled()) logger.debug("commiting transaction and new added section id:" + section.getSectionId() + ","+section.getTitle());
 			  return section.getSectionId();
 
 	        }
 			catch(StaleObjectStateException sose)
 		     {
 				logger.error("stale object exception" + sose.toString());
 		     }
 			catch(HibernateException he)
 				     {
 						if(tx !=null) tx.rollback();
 						logger.error(he.toString());
 						throw he;
 				     }
 	        	finally{
 				hibernateUtil.closeSession();
 				 }
 		}catch(Exception ex){
 				// Throw application specific error
 			throw new MeleteException("add_section_fail");
 			}
 		return null;
 	}
 	/*
 	 * edit section....
 	 */
 
 	public void editSection(Section section, MeleteResource melResource) throws MeleteException
 	{
 		try{
 		     Session session = hibernateUtil.currentSession();
 	         Transaction tx = null;
 			try
 			{
 			  // set default values for not-null fields
 				SectionResource secResource = (SectionResource)section.getSectionResource();
 				if(secResource == null)
 					secResource = new SectionResource();
 								
 				secResource.setSection(section);
 				secResource.setResource(melResource);
 								
 			  section.setModificationDate(new java.util.Date());
 			  section.setSectionResource(secResource);
 			
 		 // save object
 			  tx = session.beginTransaction();
 			  	  session.saveOrUpdate(melResource);
 			  	  session.saveOrUpdate(secResource);
 			  	  session.saveOrUpdate(section);
 				  tx.commit();
 			  if (logger.isDebugEnabled()) logger.debug("commit transaction and edit section :" + section.getModuleId() + ","+section.getTitle());
 	//		  updateExisitingResource(secResource);
 			  return ;
 
 	        }
 			catch(StaleObjectStateException sose)
 		     {
 				if(tx !=null) tx.rollback();
 				logger.error("stale object exception" + sose.toString());
 				sose.printStackTrace();
 				throw new MeleteException("edit_section_multiple_users");
 		     }
 			catch (HibernateException he)
 				     {
 						if(tx !=null) tx.rollback();
 						logger.error(he.toString());
 						he.printStackTrace();
 						throw he;
 				     }
 	       	finally{
 	       			hibernateUtil.closeSession();
 				 	}
 		}
 		catch(MeleteException ex){
 			// Throw application specific error
 			throw ex;
 		}
 		catch(Exception ex){
 				// Throw application specific error
 			throw new MeleteException("add_section_fail");
 			}
 	}
 
 	 public void deleteSection(Section sec, String userId) throws MeleteException
 	 {
 		 try{
 		       Transaction tx = null;
 		       Session session = hibernateUtil.currentSession();
 	 	
 		       try
 		       	{
 		    	   tx = session.beginTransaction();
 		    	   
 		    	   //Delete section
 		    	   sec.setDeleteFlag(true);
 		    	   session.saveOrUpdate(sec);
 		    	   
 		    	   Module module = (Module)sec.getModule();
 		    	   logger.debug("checking module element");
 		    	   if(module != null)
 		    	   {
 		    	   		String sectionsSeqXML = module.getSeqXml();
 		    	   		logger.debug("module is not null so changing seq"+ sectionsSeqXML);
 		    	   		SubSectionUtilImpl SectionUtil = new SubSectionUtilImpl();
 		    	   		logger.debug("deleting section id from xmllist" + sec.getSectionId().toString());
 		    	   		sectionsSeqXML =SectionUtil.deleteSection(sectionsSeqXML, sec.getSectionId().toString());
 		    	   		module.setSeqXml(sectionsSeqXML);
 		    	   		session.saveOrUpdate(module);
 		    	   }
 		    	   tx.commit();
 
 		       	}
 		    catch (HibernateException he)
 		    {
 		      if (tx!=null) tx.rollback();
 			  logger.error(he.toString());
 			  throw he;
 		    }
 		    catch (Exception e) {
 		      if (tx!=null) tx.rollback();
 		      logger.error(e.toString());		     
 		      throw e;
 		    }
 		    finally
 			{
 		      	hibernateUtil.closeSession();
     		  }
 		    if (userId != null)
 		    {	
 		    MeleteBookmarks mb = new MeleteBookmarks();
 			mb.setUserId(userId);
 			mb.setCourseId(sec.getModule().getCoursemodule().getCourseId());
 			mb.setModuleId(sec.getModuleId());
 			mb.setSectionId(sec.getSectionId());
 			bookmarksDB.deleteBookmark(mb);
 		    }
 			
 		}
 	      catch (Exception ex)
 		  {
 			  logger.error(ex.toString());
 			  ex.printStackTrace();
 			  throw new MeleteException("delete_module_fail");
 		  }
 	 }
 	 public Section getSection(int sectionId) throws HibernateException {
 		 	Section sec = null;
 		 	try
 			{
 
 		 		Session session = hibernateUtil.currentSession();
 
 		 		String queryString = "select section from Section as section where section.sectionId = :sectionId";
 		 		Query query=  session.createQuery(queryString);
 		 		query.setParameter("sectionId", new Integer(sectionId));
 		 		sec = (Section)query.uniqueResult();
 		    }
 		    catch (HibernateException he)
 		    {
 			  logger.error(he.toString());
 		    }
 		    finally
 			{
 		    	try
 				  {
 			      	hibernateUtil.closeSession();
 				  }
 			      catch (HibernateException he)
 				  {
 					  logger.error(he.toString());
 				  }
 			}
 		    return sec;
 		 }
 
 
 
 	/*
 	 *  add resource
 	 */
 	public void insertResource(MeleteResource melResource) throws Exception
 	{
 		try{
 		     Session session = hibernateUtil.currentSession();
 	         Transaction tx = null;
 			try
 			{
 				tx = session.beginTransaction();
 			//save resource
 				session.save(melResource);
 			  // complete transaction
 				tx.commit();
 
 		 	  if (logger.isDebugEnabled()) logger.debug(" resource is added" );
 
 			}
 			catch(StaleObjectStateException sose)
 		     {
 				logger.error("stale object exception" + sose.toString());
 		     }
 			catch(HibernateException he)
 				     {
 						if(tx !=null) tx.rollback();
 						logger.error(he.toString());
 						he.printStackTrace();
 						throw he;
 				     }
 	        	finally{
 				hibernateUtil.closeSession();
 				 }
 		}catch(Exception ex){
 				// Throw application specific error
 			ex.printStackTrace();
 			throw new MeleteException("add_section_fail");
 			}
 	}
 
 	/*
 	 *  update resource
 	 */
 	public void updateResource(MeleteResource melResource) throws Exception
 	{
 		try{
 		     Session session = hibernateUtil.currentSession();
 	         Transaction tx = null;
 			try
 			{
 				tx = session.beginTransaction();
 			//save resource
 				session.saveOrUpdate(melResource);
 			  // complete transaction
 				tx.commit();
 
 		 	  if (logger.isDebugEnabled()) logger.debug(" resource is updated" );
 
 			}
 			catch(StaleObjectStateException sose)
 		     {
 				logger.error("stale object exception" + sose.toString());
 		     }
 			catch(HibernateException he)
 				     {
 						if(tx !=null) tx.rollback();
 						logger.error(he.toString());
 						he.printStackTrace();
 						throw he;
 				     }
 	        	finally{
 				hibernateUtil.closeSession();
 				 }
 		}catch(Exception ex){
 				// Throw application specific error
 			ex.printStackTrace();
 			throw new MeleteException("add_section_fail");
 			}
 	}
 
 	/*
 	 *  add resource associated with section
 	 */
 	public void insertMeleteResource(Section section, MeleteResource melResource) throws Exception
 	{
 		try{
 		     Session session = hibernateUtil.currentSession();
 	         Transaction tx = null;
 			try
 			{
 				SectionResource secResource = (SectionResource)section.getSectionResource();
 				if (secResource == null) secResource = new  SectionResource();
 				// set secResource fields
 				secResource.setSection(section);
 				secResource.setSectionId(section.getSectionId());
 				secResource.setResource(melResource);
 
 				// update Section
 				tx = session.beginTransaction();
 			//save resource
 				session.save(melResource);
 //				 save sectionResource
 		 		 session.save(secResource);
 				 section.setSectionResource(secResource);
 				 session.saveOrUpdate(section);
 
 			  // complete transaction
 				tx.commit();
 
 		 	  if (logger.isDebugEnabled()) logger.debug("section resource association and resource is added" );
 			}
 			catch(StaleObjectStateException sose)
 		     {
 				logger.error("stale object exception" + sose.toString());
 		     }
 			catch(HibernateException he)
 				     {
 						if(tx !=null) tx.rollback();
 						logger.error(he.toString());
 						he.printStackTrace();
 						throw he;
 				     }
 	        	finally{
 				hibernateUtil.closeSession();
 				 }
 		}catch(Exception ex){
 				// Throw application specific error
 			ex.printStackTrace();
 			throw new MeleteException("add_section_fail");
 			}
 	}
 
 	/*
 	 *  add resource associated with section
 	 */
 	public void insertSectionResource(Section section, MeleteResource melResource) throws Exception
 	{
 		try{
 
 		     Session session = hibernateUtil.currentSession();
 	         Transaction tx = null;
 			try
 			{
 				SectionResource secResource = (SectionResource)section.getSectionResource();
 				if (secResource == null) secResource = new  SectionResource();
 				// set secResource fields
 				secResource.setSection(section);
 				secResource.setSectionId(section.getSectionId());
 				secResource.setResource(melResource);
 
 				// update Section
 				tx = session.beginTransaction();
 			//save resource
 				// comment below line just for checking if this removes sose exception for IMS import
 //				session.saveOrUpdate(melResource);
 //				 save sectionResource
 		 		 session.save(secResource);
 				 section.setSectionResource(secResource);
 				 session.saveOrUpdate(section);
 
 			  // complete transaction
 				tx.commit();
 
 		 	  if (logger.isDebugEnabled()) logger.debug("section resource is added" );
 			}
 			catch(StaleObjectStateException sose)
 		     {
 				logger.error("stale object exception" + sose.toString());
 		     }
 			catch(HibernateException he)
 				     {
 						if(tx !=null) tx.rollback();
 						logger.error(he.toString());
 						he.printStackTrace();
 						throw he;
 				     }
 	        	finally{
 				hibernateUtil.closeSession();
 				 }
 		}catch(Exception ex){
 				// Throw application specific error
 			ex.printStackTrace();
 			throw new MeleteException("add_section_fail");
 			}
 	}
 
 	/*
 	 *  get the melete resource based on resource id.
 	 */
 	public MeleteResource getMeleteResource(String selResourceId)
 	{
 		try{
 		     Session session = hibernateUtil.currentSession();
 		     String queryString = "from MeleteResource meleteresource where meleteresource.resourceId=:resourceId";
 		     Query query = session.createQuery(queryString);
 		     query.setParameter("resourceId",selResourceId);
 		     List result_list = query.list();
 		     if(result_list != null && result_list.size()!= 0)
 		     	return (MeleteResource)result_list.get(0);
 		    else {
 		    	//insert missing ones
 		    	MeleteResource mr = new MeleteResource();
 		    	mr.setResourceId(selResourceId);
 		    	insertResource(mr);
 		    	return mr;
 		    }
 		}
 		catch(Exception ex){
 			logger.error(ex.toString());
 			return null;
 			}
 	}
 
 	/*
 	 *  get the section resource based on resource id.
 	 */
 	public SectionResource getSectionResource(String secResourceId)
 	{
 		try{
 		     Session session = hibernateUtil.currentSession();
 		     String queryString = "from SectionResource sectionresource where sectionresource.resourceId=:resourceId";
 		     Query query = session.createQuery(queryString);
 		     query.setParameter("resourceId",secResourceId);
 		     List result_list = query.list();
 		     if(result_list != null)
 		     	return (SectionResource)result_list.get(0);
 		    else return null;
 		}
 		catch(Exception ex){
 			logger.error(ex.toString());
 			return null;
 			}
 	}
 
 	public void deassociateSectionResource(Section section, SectionResource secResource) throws Exception
 	{
 		try{
 		     Session session = hibernateUtil.currentSession();
 	         Transaction tx = null;
 			try
 			{
 				// delete SectionResource
 				tx = session.beginTransaction();
 				section.setSectionResource(null);
 				session.saveOrUpdate(section);
 			//	session.delete(secResource);
 				session.saveOrUpdate(secResource);
 			  // complete transaction
 				tx.commit();
 
 		 	  if (logger.isDebugEnabled()) logger.debug("section resource is deassociated" );
 			}
 			catch(StaleObjectStateException sose)
 		     {
 				logger.error("stale object exception" + sose.toString());
 		     }
 			catch(HibernateException he)
 				     {
 						if(tx !=null) tx.rollback();
 						logger.error(he.toString());
 						he.printStackTrace();
 						throw he;
 				     }
 	        	finally{
 				hibernateUtil.closeSession();
 				 }
 		}catch(Exception ex){
 				// Throw application specific error
 			ex.printStackTrace();
 			throw new MeleteException("add_section_fail");
 			}
 	}
 
 	/*
 	 *  add resource associated with section
 	 */
 	public void updateSectionResource(Section section, SectionResource secResource) throws Exception
 	{
 		try{
 		     Session session = hibernateUtil.currentSession();
 	         Transaction tx = null;
 			try
 			{
 				// set secResource fields
 				secResource.setSection(section);
 				secResource.setSectionId(section.getSectionId());
 				// update Section
 				tx = session.beginTransaction();
 //				 save sectionResource
 		 		 session.saveOrUpdate(secResource);
 				 section.setSectionResource(secResource);
 				 session.saveOrUpdate(section);
 
 			  // complete transaction
 				tx.commit();
 
 		 	  if (logger.isDebugEnabled()) logger.debug("section resource is updated" );
 //		 	 find existing resources with same resource_id and change their properties
 	//	 	 updateExisitingResource(secResource);
 			}
 			catch(StaleObjectStateException sose)
 		     {
 				logger.error("stale object exception" + sose.toString());
 		     }
 			catch(HibernateException he)
 				     {
 						if(tx !=null) tx.rollback();
 						logger.error(he.toString());
 						he.printStackTrace();
 						throw he;
 				     }
 	        	finally{
 				hibernateUtil.closeSession();
 				 }
 		}catch(Exception ex){
 				// Throw application specific error
 			ex.printStackTrace();
 			throw new MeleteException("add_section_fail");
 			}
 	}	
 	
 	public List<String> checkInSectionResources(String selResourceId, String courseId)
 	{
 		
 		try{
 		     Session session = hibernateUtil.currentSession();
 		     
 		     String queryString = "Select a from Section a, SectionResource b, CourseModule c where a.sectionId = b.sectionId AND " + 
 		     					   "a.moduleId = c.moduleId AND c.courseId=:courseId AND c.deleteFlag=0" + 
 		     					   " AND b.resource.resourceId=:resourceId AND a.deleteFlag=0" ;
 		     
 		     Query query = session.createQuery(queryString);
 		     query.setParameter("courseId",courseId);
 		     query.setParameter("resourceId",selResourceId);
 		     List result_list = query.list();
 		     if (result_list == null) return null;
 		     logger.debug("result_list for sec resources " + result_list.size() + result_list.toString());
 		     List foundResources = new ArrayList<String> (0);
 		     for(Iterator<Section> itr=result_list.listIterator(); itr.hasNext();)
 		     {
 		    	 Section sec = itr.next();
 		    	 String foundAt = sec.getModule().getTitle() +" >> " + sec.getTitle();
 		    	 foundResources.add(foundAt);
 		     }
 		     return foundResources;		    
 		}
 		catch(Exception ex){
 			logger.error(ex.toString());
 			return null;
 			}
 	
 	}
 	
 	public void deleteResourceInUse(String delResourceId,String courseId) throws MeleteException
 	{
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			Transaction tx = null;
 			try
 			{
 				String queryString = "update SectionResource secResource set secResource.resource.resourceId = null where secResource.sectionId IN "
 						+ "(select a.sectionId from Section a, CourseModule b where a.moduleId = b.moduleId AND b.courseId=:courseId) "
 						+ "AND secResource.resource.resourceId=:resourceId";
 
 				tx = session.beginTransaction();
 				int delResources = session.createQuery(queryString).setString("courseId", courseId).setString("resourceId", delResourceId)
 						.executeUpdate();
 
 				logger.debug(delResources + "section resources are set to null");
 
 				MeleteResource melRes = (MeleteResource) session.get(org.sakaiproject.component.app.melete.MeleteResource.class, delResourceId);
 				session.delete(melRes);
 
 				// complete transaction
 				tx.commit();
 			}
 			catch (StaleObjectStateException sose)
 			{
 				logger.error("stale object exception" + sose.toString());
 			}
 			catch (HibernateException he)
 			{
 				if (tx != null) tx.rollback();
 				logger.error(he.toString());
 				he.printStackTrace();
 				throw he;
 			}
 			finally
 			{
 				hibernateUtil.closeSession();
 			}
 		}
 		catch (Exception ex)
 		{
 			logger.error(ex.toString());
 			throw new MeleteException("delete_resource_fail");
 		}
 	
 	}
 	
 	/**
 	 * @return Returns the hibernateUtil.
 	 */
 	public HibernateUtil getHibernateUtil() {
 		return hibernateUtil;
 	}
 	/**
 	 * @param hibernateUtil
 	 *        The hibernateUtil to set.
 	 */
 	public void setHibernateUtil(HibernateUtil hibernateUtil) {
 		this.hibernateUtil = hibernateUtil;
 	}
 	
 	public void setBookmarksDB(MeleteBookmarksDB bookmarksDB)
 	{
 		this.bookmarksDB = bookmarksDB;
 	}	
 
 	/**
 	 * @param logger The logger to set.
 	 */
 	public void setLogger(Log logger) {
 		this.logger = logger;
 	}	
 }
