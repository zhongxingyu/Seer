 /**********************************************************************************
 *
 * $Header: /usr/src/sakai/melete-2.4/melete-impl/src/java/org/sakaiproject/component/app/melete/ModuleDB.java,v 1.38 2007/11/07 00:54:16 mallikat Exp $
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
 import java.io.Serializable;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.Iterator;
 import java.util.Calendar;
 
 import org.hibernate.Hibernate;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.StaleObjectStateException;
 import org.hibernate.Transaction;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.api.app.melete.MeleteSecurityService;
 import org.sakaiproject.api.app.melete.ModuleObjService;
 import org.sakaiproject.api.app.melete.SectionObjService;
 import org.sakaiproject.api.app.melete.exception.MeleteException;
 import org.hibernate.criterion.Restrictions;
 
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.entity.api.ResourcePropertiesEdit;
 import org.sakaiproject.user.cover.UserDirectoryService;
 import org.sakaiproject.api.app.melete.MeleteCHService;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.util.ResourceLoader;
 import org.dom4j.Element;
 
 /* Mallika - 4/17/07 - added code to support subsections on list pages
  * Mallika -6/6/07 - consolidated methods
  * Mallika - 6/6/07 - Added methods for multiple indent (same as previous)
  * Mallika - 6/18/07 - Correct sections method added
  */
 
 public class ModuleDB implements Serializable {
 
 	private int seqNumber;
 	private HibernateUtil hibernateUtil;
 	private List xmlSecList;
 	private SectionDB sectionDB;
 	private MeleteBookmarksDB bookmarksDB;
 	private MeleteCHService meleteCHService;
 	private MeleteSecurityService meleteSecurityService;
 
 	/** Dependency:  The logging service. */
 	private Log logger = LogFactory.getLog(ModuleDB.class);
 
 	/**
 	 *
 	 */
 	public ModuleDB()
 	{
 		if(hibernateUtil == null)getHibernateUtil();
 	}
 
 	/**
 	 *
 	 * Description:
 	 */
 	public static ModuleDB getModuleDB()
 	{
 		return new ModuleDB();
 	}
 	/**
 	 * @param logger The logger to set.
 	 */
 	public void setLogger(Log logger) {
 		this.logger = logger;
 	}
 
 	 /**
 	   * assign sequence number to the new module.
 	 * if no sequence number is found in course module table for given courseId
 	 * assume that its a first module.
 	 * @param session
 	 * @param course
 	 * @return
 	 */
 	private int assignSequenceNumber(Session session, String courseId)
 	{
 	 int maxSeq= 1;
 	 try
 		{
 		   Query q=session.createQuery("select max(cm.seqNo) from CourseModule cm where cm.courseId =:courseId");
 		   q.setParameter("courseId",courseId);
 		   Integer maxsequence = (Integer)q.uniqueResult();
 
 
 		   // if no sequence is found then its first module.
 		  if(maxsequence == null || maxsequence.intValue() <= 0)
 		  {
 		    return maxSeq ;
  		  }
 		 maxSeq = maxsequence.intValue()+1;
 
 	     }
 	     catch (HibernateException he)
 	     {
 			 logger.error(he.toString());
 			 //he.printStackTrace();
 	     }
 	    return maxSeq ;
 
 	}
 
 	public int getMaxSeqNo(String courseId)
 	{
 	 int maxSeq= 0;
 	 try
 		{
 		 Session session = hibernateUtil.currentSession();
 
 		   Query q=session.createQuery("select max(cm.seqNo) from CourseModule cm where cm.courseId =:courseId and cm.deleteFlag=0");
 		   q.setParameter("courseId",courseId);
 		   Integer maxsequence = (Integer)q.uniqueResult();
 
 
 		   // if no sequence is found then its first module.
 		  if(maxsequence == null || maxsequence.intValue() <= 0)
 		  {
 		    return maxSeq ;
  		  }
 		  maxSeq = maxsequence.intValue();
 
 	     }
 	     catch (HibernateException he)
 	     {
 			 logger.error(he.toString());
 			 //he.printStackTrace();
 	     }
 	     finally
 	     {
 	 		hibernateUtil.closeSession();
 	 	 }
 	    return maxSeq ;
 
 	}
 
 	public int getNextSeqNo(String courseId, int currSeqNo)
 		{
 		 int nextSeqNo = -1;
 		 try
 			{
 			 Session session = hibernateUtil.currentSession();
 
 			   Query q=session.createQuery("select min(cm.seqNo) from CourseModule cm, ModuleShdates ms where cm.courseId =:courseId and cm.deleteFlag=0 and cm.archvFlag=0 and cm.seqNo > :currSeqNo and cm.moduleId=ms.moduleId and ((ms.startDate < :currDate and ms.endDate > :currDate) or (ms.startDate is null and ms.endDate is null) or (ms.startDate is null and ms.endDate > :currDate) or (ms.startDate < :currDate and ms.endDate is null))");
 			   q.setParameter("courseId",courseId);
 			   q.setParameter("currSeqNo", currSeqNo);
 			   q.setParameter("currDate", new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
 			   Integer minsequence = (Integer)q.uniqueResult();
 
 
 			   // if no sequence is found then this is the last module
 			  if(minsequence == null || minsequence.intValue() <= 0)
 			  {
 			    return -1;
 	 		  }
 			  nextSeqNo = minsequence.intValue();
 
 		     }
 		     catch (HibernateException he)
 		     {
 				 logger.error(he.toString());
 				 //he.printStackTrace();
 		     }
 		     finally
 		     {
 		 		hibernateUtil.closeSession();
 		 	 }
 		    return nextSeqNo ;
 
 	}
 
 	public int getPrevSeqNo(String courseId, int currSeqNo)
 	{
 	 int prevSeqNo = -1;
 	 try
 		{
 		 Session session = hibernateUtil.currentSession();
 
 		   Query q=session.createQuery("select max(cm.seqNo) from CourseModule cm, ModuleShdates ms where cm.courseId =:courseId and cm.deleteFlag=0 and cm.archvFlag=0 and cm.seqNo < :currSeqNo and cm.moduleId=ms.moduleId and ((ms.startDate < :currDate and ms.endDate > :currDate) or (ms.startDate is null and ms.endDate is null) or (ms.startDate is null and ms.endDate > :currDate) or (ms.startDate < :currDate and ms.endDate is null))");
 		   q.setParameter("courseId",courseId);
 		   q.setParameter("currSeqNo", currSeqNo);
 		   q.setParameter("currDate", new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis()));
 		   Integer maxsequence = (Integer)q.uniqueResult();
 
 
 		   // if no sequence is found then there is no module before this one
 		  if(maxsequence == null || maxsequence.intValue() <= 0)
 		  {
 			    return -1;
  		  }
 		  prevSeqNo = maxsequence.intValue();
 
 	     }
 	     catch (HibernateException he)
 	     {
 			 logger.error(he.toString());
 			 //he.printStackTrace();
 	     }
 	     finally
 	     {
 	 		hibernateUtil.closeSession();
 	 	 }
 	    return prevSeqNo ;
 }
 
 	   /**
 	 * Actually inserts a row with module information.
 	 * adds a row in module , moduleshdates , course module.
 	 * if a transaction fails , rollback the whole transaction.
 	 *
 	 * @param module
 	 * @param moduleshowdates
 	 * @param course
 	 *
 	 * Revised by rashmi on 1/21/05 -- to associate coursemodule with module
 	 * Murthy 03/08/05 --  set modification date commented
 	 * Rashmi - 07/07/07 - removed season and yr from method signature
 	 */
 	public void addModule(Module module, ModuleShdates moduleshowdates, String userId, String courseId) throws Exception
 	{
 
 	try{
 	     Session session = hibernateUtil.currentSession();
            Transaction tx = null;
 
 		try
 		{
 		  module.setCreationDate(new java.util.Date());
 		  module.setUserId(userId);
 		  //module.setModificationDate(new java.util.Date());
 
     		// assign sequence number
 		  int seq = assignSequenceNumber(session, courseId);
 
 		  moduleshowdates.setModule(module);
 
 		  tx = session.beginTransaction();
              // save module
 
 		 session.save(module);
 
 		// save module show dates
 		 session.save(moduleshowdates);
 
 		//create instance of coursemodules
 		 CourseModule coursemodule = new CourseModule();
 		 coursemodule.setCourseId(courseId);
 		 coursemodule.setModule(module);
   		 coursemodule.setSeqNo(seq);
   		 coursemodule.setDeleteFlag(false);
 
 		// save course module
  		 session.save(coursemodule);
 
 		 CourseModule cms = (CourseModule)module.getCoursemodule();
 		 if (cms == null)
 		 {
 		 	cms = coursemodule;
 		 }
 		 module.setCoursemodule(cms);
 
 		 session.saveOrUpdate(module);
 
 		  tx.commit();
 		  logger.debug("add module success" + module.getModuleId() + module.getCoursemodule().getCourseId());
 		  return ;
 
 	     }
 	     catch (HibernateException he)
 	     {
 			if(tx !=null) tx.rollback();
 			logger.error(he.toString());
 			//he.printStackTrace();
 			throw he;
 	     }
 		finally{
 		hibernateUtil.closeSession();
 		 }
 	}catch(Exception ex){
    // Throw application specific error
 		logger.error("error at module db level");
 		throw new MeleteException("add_module_fail");
 	}
 
   }
 
 
    // end rashmi stuff
 
 
      private List getModuleBookmarks(Module mod, List bookmarkList)
      {
        MeleteBookmarks mb = null;
        List moduleBookmarks = new ArrayList();
 
        Iterator i = bookmarkList.iterator();
        while (i.hasNext())
        {
     	   mb = (MeleteBookmarks) i.next();
     	   if (mb.getModuleId() == mod.getModuleId().intValue())
     	   {
     		   moduleBookmarks.add(mb);
     	   }
        }
        return moduleBookmarks;
      }
 
 	 public List getShownModulesAndDatesForInstructor(String userId, String courseId) throws HibernateException {
 	 	List moduleDateBeansList = new ArrayList();
 	 	List modList = null;
 	 	ModuleDateBean mdBean = null;
 	 	Module mod = null;
 	 	List bookmarkList = null;
 	 	List moduleBookmarks = null;
 
 	 	bookmarkList = bookmarksDB.getBookmarks(userId, courseId);
         try
 		{
 	 	  Session session = hibernateUtil.currentSession();
 	      modList = getModules(courseId);
 	      Iterator i = modList.iterator();
 
 	      while (i.hasNext()) {
 	      	mdBean = new ModuleDateBean();
 	      	mod = (Module) i.next();
 
 	      	if ((bookmarkList == null)||(bookmarkList.size() == 0))
 	      	{
 	      	  populateModuleBean(mod, mdBean, null);
 	      	}
 	      	else
 	      	{
 	      		moduleBookmarks = getModuleBookmarks(mod,bookmarkList);
 	      		if (moduleBookmarks.size() == 0)
 	      		{
 	      			populateModuleBean(mod, mdBean, null);
 	      		}
 	      		else
 	      		{
 	      			populateModuleBean(mod, mdBean, moduleBookmarks);
 	      		}
 	      	}
 
 		    moduleDateBeansList.add(mdBean);
 	      	mod = null;
 	      }
 
 	    }
 	    catch (Exception he)
 	    {
 		  logger.error(he.toString());
 		  he.printStackTrace();
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
 
 	    return moduleDateBeansList;
 
 	  }
 
 
 
 	 public List getModules(String courseId) throws HibernateException {
 	 	List modList = new ArrayList();
 	 	List sectionsList = null;
 	 	Module mod = null;
 	 	Query sectionQuery = null;
 	 	try
 		{
 	      Session session = hibernateUtil.currentSession();
 
 	      String queryString = "from Module module where module.coursemodule.courseId = :courseId  and module.coursemodule.archvFlag = 0 and module.coursemodule.deleteFlag = 0 order by module.coursemodule.seqNo";
 
 	      Query query = session.createQuery(queryString);
 	      query.setParameter("courseId", courseId);
 
 	      modList = query.list();
 
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
 	    return modList;
 	  }
 	 
 	 public List getActivenArchiveModules(String courseId) throws HibernateException {
 		 	List modList = new ArrayList();
 		 	List sectionsList = null;
 		 	Module mod = null;
 		 	Query sectionQuery = null;
 		 	try
 			{
 		      Session session = hibernateUtil.currentSession();
 
 		      String queryString = "from Module module where module.coursemodule.courseId = :courseId  and module.coursemodule.deleteFlag = 0 order by module.coursemodule.seqNo";
 
 		      Query query = session.createQuery(queryString);
 		      query.setParameter("courseId", courseId);
 
 		      modList = query.list();
 
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
 		    return modList;
 		  }
 	 
 //
 	 public ModuleDateBean getModuleDateBean(String userId, String courseId,  int moduleId) throws HibernateException {
 	 	List modList = new ArrayList();
 	 	Module mod = null;
 	 	ModuleDateBean mdBean = null;
 		List bookmarkList = null;
 	 	List moduleBookmarks = null;
 
 	 	try
 		{
 	       Session session = hibernateUtil.currentSession();
 
 	      String queryString = "from Module module where module.moduleId = :moduleId and module.coursemodule.courseId = :courseId  and module.coursemodule.archvFlag = 0 and module.coursemodule.deleteFlag = 0 order by module.coursemodule.seqNo";
 
 	      Query query = session.createQuery(queryString);
 	      query.setParameter("moduleId", new Integer(moduleId));
 	      query.setParameter("courseId", courseId);
 
 	      modList = query.list();
 	      Iterator i = modList.iterator();
 	      while (i.hasNext()) {
 	        mdBean = new ModuleDateBean();
 	        mod = (Module) i.next();
 	        bookmarkList = bookmarksDB.getBookmarks(userId, courseId, mod.getModuleId());
 	        if ((bookmarkList == null)||(bookmarkList.size() == 0))
 	      	{
 		   	  populateModuleBean(mod, mdBean, null);
 	      	}
 	      	else
 	      	{
 	      		moduleBookmarks = getModuleBookmarks(mod,bookmarkList);
 	      		if (moduleBookmarks.size() == 0)
 	      		{
 	      			populateModuleBean(mod, mdBean, null);
 	      		}
 	      		else
 	      		{
 	      			populateModuleBean(mod, mdBean, moduleBookmarks);
 	      		}
 	      	}
 	      }
 		}
 	    catch (Exception he)
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
 	    return mdBean;
 	  }
 	 public ModuleDateBean getModuleDateBeanBySeq(String userId, String courseId,  int seqNo) throws HibernateException {
 		 	List modList = new ArrayList();
 		 	Module mod = null;
 		 	ModuleDateBean mdBean = null;
 			List bookmarkList = null;
 		 	List moduleBookmarks = null;
 
 		 	try
 			{
 		       Session session = hibernateUtil.currentSession();
                String queryString = "from Module module where module.coursemodule.courseId = :courseId and module.coursemodule.seqNo = :seqNo  and module.coursemodule.archvFlag = 0 and module.coursemodule.deleteFlag = 0 order by module.coursemodule.seqNo";
 
               Query query = session.createQuery(queryString);
 		      query.setParameter("seqNo", new Integer(seqNo));
 		      query.setParameter("courseId", courseId);
 
 		      modList = query.list();
 		      Iterator i = modList.iterator();
 		      while (i.hasNext()) {
 		        mdBean = new ModuleDateBean();
 		        mod = (Module) i.next();
 		        bookmarkList = bookmarksDB.getBookmarks(userId, courseId, mod.getModuleId());
 		        if ((bookmarkList == null)||(bookmarkList.size() == 0))
 		      	{
 			   	  populateModuleBean(mod, mdBean, null);
 		      	}
 		      	else
 		      	{
 		      		moduleBookmarks = getModuleBookmarks(mod,bookmarkList);
 		      		if (moduleBookmarks.size() == 0)
 		      		{
 		      			populateModuleBean(mod, mdBean, null);
 		      		}
 		      		else
 		      		{
 		      			populateModuleBean(mod, mdBean, moduleBookmarks);
 		      		}
 		      	}
 		      }
 			}
 		    catch (Exception he)
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
 		    return mdBean;
 		  }
 
 	 private void populateModuleBean(Module mod, ModuleDateBean mdBean, List moduleBookmarks)
 	 {
 	   String modSeq;
 	   SubSectionUtilImpl ssuImpl;
 	   StringBuffer rowClassesBuf;
 	   List sectionBeanList = null;
 	   Map sectionMap = null;
 	   java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis());
 
 	   if (((mod.getModuleshdate().getStartDate() == null)||(mod.getModuleshdate().getStartDate().before(currentTimestamp)))&&((mod.getModuleshdate().getEndDate() == null)||(mod.getModuleshdate().getEndDate().after(currentTimestamp))))
 	   {
 		   mdBean.setVisibleFlag(true);
 	   }
 	   else
 	   {
 		   mdBean.setVisibleFlag(false);
 	   }
 	   mdBean.setModuleId(mod.getModuleId().intValue());
 	   mdBean.setModule((Module)mod);
 	   mdBean.setModuleShdate(mod.getModuleshdate());
 	   mdBean.setCmod(mod.getCoursemodule());
        mdBean.setTruncTitle(createTruncstr(mod.getTitle()));
        if ((moduleBookmarks != null) && (moduleBookmarks.size() > 0))
        {
     	   mdBean.setBookmarkFlag(true);
        }
        else
        {
            mdBean.setBookmarkFlag(false);
        }
 
 
 
 	   sectionMap = mod.getSections();
 
 	  if (sectionMap != null)
 	  {
 	  	if (sectionMap.size() > 0)
 	    {
 	    modSeq = Integer.toString(mod.getCoursemodule().getSeqNo());
 	    ssuImpl = new SubSectionUtilImpl();
 	    ssuImpl.traverseDom(mod.getSeqXml(),modSeq);
 	    xmlSecList = ssuImpl.getXmlSecList();
 	    sectionBeanList = new ArrayList();
 	    rowClassesBuf = new StringBuffer();
 
 	    xmlSecList = correctSections(sectionMap,mod,xmlSecList);
 	    processSections(sectionMap, sectionBeanList,xmlSecList,rowClassesBuf, moduleBookmarks);
 	    mdBean.setSectionBeans(sectionBeanList);
 	    mdBean.setRowClasses(rowClassesBuf.toString());
 	    }
 	  }
 
 	 }
 
 	 private List correctSections(Map sectionMap, Module mod, List xmlSecList)
 	 {
 		 SubSectionUtilImpl ssuImpl = new SubSectionUtilImpl();
 		 String updSeqXml = null;
 		 if (sectionMap == null || sectionMap.size() == 0) return null;
 		 if (sectionMap != null)
 		 {
 
              //Find all entries that are in sectionMap but not in
 			 //xmlSecList
 			 Set secKeySet = sectionMap.keySet();
 			 List newSecList = new ArrayList();
 			 Iterator it = secKeySet.iterator();
 			 while (it.hasNext())
 			 {
 				 newSecList.add((Integer)it.next());
 			 }
 			 List xtraXmlList = new ArrayList();
 
 			 //Find all entries that are in xmlSecList but not in
 			 //secKeySet
 			 it = xmlSecList.iterator();
 			 while (it.hasNext())
 			 {
 				 Integer obj = new Integer(((SecLevelObj)it.next()).getSectionId());
 				 if (newSecList.contains(obj))
 				 {
 					 newSecList.remove(obj);
 					 continue;
 				 }
 				 else
 				 {
 					 xtraXmlList.add(obj);
 				 }
 
 			 }
 
 			  //newSecList contains entries in the section list that aren't in seqXml
 			 //These sections are added to seqXml at the bottom
 			 //xtraXmlList contains entries in seqXml that aren't in section list
 			 //These entries are deleted from seqXml
 
 			 //Both lists are in sync
 			 if ((newSecList.size()==0)&&(xtraXmlList.size()==0))
 			 {
 				 if (secKeySet.size() == xmlSecList.size())
 				 {
 					  return xmlSecList;
 				 }
 			}
 			 else
 			 {
 
 			   updSeqXml = null;
 			   //Add sections to seqXml
 			   if (newSecList != null)
 			   {
 			   if (newSecList.size() > 0)
 			   {
 			     it = newSecList.iterator();
 			     updSeqXml = mod.getSeqXml();
 			     while (it.hasNext())
 			     {
 			      ssuImpl.addSectiontoList(updSeqXml, ((Integer)it.next()).toString());
 			      updSeqXml = ssuImpl.storeSubSections();
 			     }
 			   }
 
 			   }//Add sections to seqXml end
 			   if ((updSeqXml == null)||(updSeqXml.length() == 0))
 			   {
 				 updSeqXml = mod.getSeqXml();
 			   }
 
 			   //Remove sections from seqXml
 			   if (xtraXmlList != null)
 			   {
 				 if (xtraXmlList.size() > 0)
 				 {
 				   it = xtraXmlList.iterator();
 				   while (it.hasNext())
 				   {
 					 try
 					 {
 					  updSeqXml = ssuImpl.deleteSection(updSeqXml, Integer.toString((Integer)it.next()));
 				      }
 				      catch (Exception ex)
         			  {
         			  	logger.error("CorrectSections - Error in deleting section "+ex.toString());
         			  }
 				   }
 				 }
 			   }//Remove sections from seqXml end
 
 		      }//end else if big condition
 
 			   //Update module
 			   if ((updSeqXml != null)&&(updSeqXml.length() > 0))
 			   {
 				 mod.setSeqXml(updSeqXml);
 				try
 				{
 				 updateModule(mod);
 			    }
 	    		catch (Exception ex)
 	 		    {
 	    			logger.error("CorrectSections - error in updating module "+ex.toString());
 	 		    }
 				 ssuImpl.traverseDom(mod.getSeqXml(),((Integer)mod.getCoursemodule().getSeqNo()).toString());
 				 xmlSecList = ssuImpl.getXmlSecList();
 				 return xmlSecList;
 			   }
 		    }//end else sectionMap!= null
 		 return null;
 	 }
 
 	 private void processSections(Map sectionMap,List sectionBeanList,List xmlSecList,StringBuffer rowClassesBuf, List moduleBookmarks)
 	 {
 		Section sec = null;
 		SectionBean secBean = null;
 
 		if ((sectionMap != null) && (xmlSecList != null))
 		{
 		  if (sectionMap.size() == xmlSecList.size())
 		  {
 		  for (ListIterator k = xmlSecList.listIterator(); k.hasNext(); )
 		  {
    		  SecLevelObj slObj = (SecLevelObj)k.next();
    		  if (slObj != null)
    		  {
    			sec =(Section)sectionMap.get(new Integer(slObj.getSectionId()));
    			if (sec != null)
    			{
    			secBean = new SectionBean(sec);
    			secBean.setTruncTitle(createTruncstr(sec.getTitle()));
    			secBean.setDisplaySequence(slObj.getDispSeq());
    			secBean.setBookmarkFlag(false);
    			if (moduleBookmarks != null)
    			{
    				if (moduleBookmarks.size() > 0)
    				{
    					for (ListIterator l = moduleBookmarks.listIterator(); l.hasNext();)
    					{
    						MeleteBookmarks mb = (MeleteBookmarks) l.next();
    						if (mb != null)
    						{
    							if (mb.getSectionId() == slObj.getSectionId())
    							{
    								secBean.setBookmarkFlag(true);
    								break;
    							}
    						}
    					}
    				}
    			}
    			sectionBeanList.add(secBean);
    			rowClassesBuf.append("secrow"+slObj.getLevel()+",");
    			}
    		    }
    	      }
 	 	  rowClassesBuf.delete(rowClassesBuf.toString().length()-1,rowClassesBuf.toString().length());
 		  }
 		}
 	 }
 
 
 	 public ModuleShdates getShownModuleDates(int moduleId) throws HibernateException
 	 {
 	  	ModuleShdates mDate = null;
 	 	try
 		{
 	      Session session = hibernateUtil.currentSession();
 
 	       String queryString =  "select moduleshdate from ModuleShdates as moduleshdate where moduleshdate.module.moduleId = :moduleId";
 	       mDate = (ModuleShdates)
 		  session.createQuery(queryString).setParameter("moduleId", new Integer(moduleId)).uniqueResult();
 
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
 	    return mDate;
 	 }
 	 public CourseModule getCourseModule(int moduleId, String courseId) throws HibernateException
 	 {
 	  	CourseModule cmod = null;
 	 	try
 		{
 	      Session session = hibernateUtil.currentSession();
 	      String queryString =  "select cmod from CourseModule as cmod where cmod.module.moduleId = :moduleId  and cmod.courseId = :courseId";
 	      Query query =
 		  session.createQuery(queryString);
 	      query.setParameter("moduleId", new Integer(moduleId));
 	      query.setParameter("courseId", courseId);
 	      cmod = (CourseModule)query.uniqueResult();
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
 	    return cmod;
 	 }
 
 	 //Only return un-deleted course modules
 	 public List getCourseModules(String courseId) throws HibernateException
 	 {
 		List cmodList = new ArrayList();
 	  	try
 		{
 	     Session session = hibernateUtil.currentSession();
 	      String queryString =  "from CourseModule as cmod where cmod.courseId = :courseId and cmod.deleteFlag = 0";
 	      Query query =
 		  session.createQuery(queryString);
 	      query.setParameter("courseId", courseId);
 	      cmodList = query.list();
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
 	    return cmodList;
 	 }
 
 
 
 	 public void updateModuleDateBeans(List moduleDateBeans)throws Exception
 	 {
 	 	Transaction tx = null;
 
         Session session = hibernateUtil.currentSession();
 	 	for (ListIterator i = moduleDateBeans.listIterator(); i.hasNext(); )
 	 	{
 	 		tx = null;
 	        ModuleDateBean mdbean = (ModuleDateBean) i.next();
 	        if (mdbean.isDateFlag() == false)
 	          {
 	 	      try
 		      {
 	            tx = session.beginTransaction();
 	            //Update module properties
 	            session.saveOrUpdate(mdbean.getModule());
                 //	    Getting the set of show hides dates associated with this module
 	            ModuleShdates mshdates = (ModuleShdates) mdbean.getModule().getModuleshdate();
 
         	    mshdates.setStartDate(mdbean.getModuleShdate().getStartDate());
         	    mshdates.setEndDate(mdbean.getModuleShdate().getEndDate());
         	    session.saveOrUpdate(mshdates);
 
         	    tx.commit();
 	            //session.flush();
 	           }
 	 	       catch(StaleObjectStateException sose)
 	           {
 			     if(tx !=null) tx.rollback();
 			     logger.error("stale object exception" + sose.toString());
 			     throw new MeleteException("edit_module_multiple_users");
 	           }
 	           catch (HibernateException he)
 	           {
 		         logger.error(he.toString());
 		         throw he;
 	           }
 	           catch (Exception e)
 	           {
 	             if (tx!=null) tx.rollback();
 	             logger.error(e.toString());
 	             throw e;
 	           }
 
 	         }
 			}
  	        try
 		     {
 	      	   hibernateUtil.closeSession();
 		      }
 	         catch (HibernateException he)
 		     {
 			   logger.error(he.toString());
 			   throw he;
 		      }
 	 }
 
 	 public void updateModule(Module mod) throws Exception
 	 {
 	 	Transaction tx = null;
 	 	try
 		{
 
 	      Session session = hibernateUtil.currentSession();
 
 	      tx = session.beginTransaction();
 
 	      //Update module properties
 	      session.saveOrUpdate(mod);
 
 	      tx.commit();
 
 	      //session.flush();
 
 	    }
 	 	catch(StaleObjectStateException sose)
 	     {
 			if(tx !=null) tx.rollback();
 			logger.error("stale object exception" + sose.toString());
 			throw new MeleteException("edit_module_multiple_users");
 	     }
 	    catch (HibernateException he)
 	    {
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
 	    	try
 			  {
 		      	hibernateUtil.closeSession();
 			  }
 		      catch (HibernateException he)
 			  {
 				  logger.error(he.toString());
 				  throw he;
 			  }
 		}
 	 }
 
 	 public static boolean deleteDir(File dir) {
         if (dir.isDirectory()) {
             String[] children = dir.list();
             if (children.length == 0)
             {
             	dir.delete();
             	return true;
             }
             else
             {
               for (int i=0; i<children.length; i++) {
                 boolean success = deleteDir(new File(dir, children[i]));
                 if (!success) {
                     return false;
                 }
               }
             }
         }
 
         // The directory is now empty so delete it
         return dir.delete();
     }
     public static boolean renameDir(File dir) {
 		String del_fname = dir.getAbsolutePath().concat("_del");
 		boolean success = dir.renameTo(new File(del_fname));
 		return success;
     }
     
     //Deletemodule bulk method
     public void deleteModules(List delModules, String courseId, String userId) throws Exception {
     	    
    	     logger.debug("deleteModules begin");
   				
    		  long starttime = System.currentTimeMillis(); 
    	      Transaction tx = null;
   
     	  	try
     		{
     	      Session session = hibernateUtil.currentSession();
     	     
     	      //Get resources for modules that need to be deleted
     	      List delResourcesList = getActiveResourcesFromList(delModules);
     	      if (delResourcesList != null) logger.debug("DelResourcesList size is "+delResourcesList.size());
       	      	 
     	      //If there is media or typeUpload/typeLink sections
     	      if ((delResourcesList != null)&&(delResourcesList.size() > 0))
     	      {	  
     	    	//Get all modules of course  
     	    	List<Module> activenArchModules = getActivenArchiveModules(courseId);
     	    	
     	    	//After the command below,activenArchModules will contain all modules in the 
       	        //course except for the ones that need to be deleted
       	        if ((activenArchModules != null)&&(activenArchModules.size() > 0))
       	        {	  
       	    	 boolean res = activenArchModules.removeAll(delModules);
       	         logger.debug("Undeleted modules minus modules to delete in site is "+activenArchModules.size());
       	        }	 
     	    	       
       	        List activeResourcesList = new ArrayList();
       	        //This is to make sure not all modules in the course are being deleted
       	        if ((activenArchModules != null)&&(activenArchModules.size() > 0))
     	        {
     	          //Resources belonging to modules that are not being deleted
     	          activeResourcesList = getActiveResourcesFromList(activenArchModules);
     	          if ((activeResourcesList != null)&&(activeResourcesList.size() > 0))
     	          {	  
     	        	  logger.debug("ActiveResourcesList size is "+activeResourcesList.size());
     	          	  //Remove resources from delResourcesList that can't be deleted since they
     	              //are being used by activeResourcesList
     	              delResourcesList.removeAll(activeResourcesList);
     	              if (delResourcesList != null) logger.debug("Final DelResourcesList size is "+delResourcesList.size());
     	          }
     	        }  
     	      }
     	      
     	      if (!session.isOpen()) session = hibernateUtil.currentSession();
 			  tx = session.beginTransaction();
     	      if (delModules != null) 
     	      {
     	    	  logger.debug("Modules to delete in site is "+delModules.size());
     	    	  for (ListIterator<Module> k = delModules.listIterator(); k.hasNext(); )
     	    	  {
 		  		   		Module delModule = (Module)k.next();
 		  		   		Integer delModuleId = delModule.getModuleId();
 			            String allSecIds = getAllSectionIds(delModule.getSections());
 			            String updSectionResourceStr = "update SectionResource sr set sr.resource = null where sr.section in " + allSecIds;
 			            String delSectionResourceStr = "delete SectionResource sr where sr.section in " + allSecIds;
 			            String delSectionStr = "delete Section s where s.moduleId=:moduleId";
 			            String selCmodseqStr = "select cmod.seqNo from CourseModule cmod where cmod.courseId=:courseId and cmod.moduleId=:moduleId";
 			            String delCourseModuleStr = "delete CourseModule cm where cm.moduleId=:moduleId";
                         String updCmodseqStr = "update CourseModule cmod set cmod.seqNo=cmod.seqNo-1 where cmod.courseId=:courseId and cmod.seqNo>:seqNo";
 			            String delModuleshDatesStr = "delete ModuleShdates msh where msh.moduleId=:moduleId";
 			            String delModuleStr = "delete Module m where m.moduleId=:moduleId";
 			            String delBookmarksStr = "delete MeleteBookmarks mb where mb.moduleId=:moduleId";
 			  	 
 			  		    if (allSecIds != null)
 			            {
 					      int deletedEntities = session.createQuery(updSectionResourceStr).executeUpdate();
 					      deletedEntities = session.createQuery(delSectionResourceStr).executeUpdate();
 					    }
 			            
 			  		    int deletedEntities = session.createQuery(delSectionStr).setInteger("moduleId", delModuleId).executeUpdate();
 			  		    deletedEntities = session.createQuery(delBookmarksStr).setInteger("moduleId", delModuleId).executeUpdate();
                         Query q=session.createQuery(selCmodseqStr);
 	                    q.setParameter("courseId", courseId);
 		                q.setParameter("moduleId",delModuleId);
 		                Integer seqNo = (Integer)q.uniqueResult();
 		                deletedEntities = session.createQuery(delCourseModuleStr).setInteger("moduleId", delModuleId).executeUpdate();
 	                    deletedEntities = session.createQuery(updCmodseqStr).setString("courseId", courseId).setInteger("seqNo", seqNo).executeUpdate();
 	                     
 			            deletedEntities = session.createQuery(delModuleshDatesStr).setInteger("moduleId", delModuleId).executeUpdate();
 			            deletedEntities = session.createQuery(delModuleStr).setInteger("moduleId", delModuleId).executeUpdate();
 			            meleteCHService.removeCollection(courseId, "module_"+delModuleId.toString());  	      
 			  
 			            if ((delResourcesList != null)&&(delResourcesList.size() > 0))
 			    	    {	 
 			              // delete melete resource and from content resource
 			              for (Iterator delIter = delResourcesList.listIterator(); delIter.hasNext();)
 			              {
 					      String delResourceId = (String) delIter.next();
 					      String delMeleteResourceStr = "delete MeleteResource mr where mr.resourceId=:resourceId";
 					      deletedEntities = session.createQuery(delMeleteResourceStr).setString("resourceId", delResourceId).executeUpdate();
 					      meleteCHService.removeResource(delResourceId);
 				          }
 			    	    }  
     	    	      }
     	            }	  
 			      tx.commit();
     	     
     		  }
     	    catch (HibernateException he)
     	    {
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
     	    	try
     			  {
     		      	hibernateUtil.closeSession();
     			  }
     		      catch (HibernateException he)
     			  {
     				  logger.error(he.toString());
     				  throw he;
     			  }
     		}
     		   
     		    long endtime = System.currentTimeMillis();
 
     			logger.debug("delete modules ends " +(endtime - starttime));    	
     }
     
     //The deleteModule method below iterate through each section and deletes
     //them individually
     /*
     public void deleteModule(CourseModule cmod, String userId) throws Exception {
     	
     int affectedEntities;
 	String delModuleShdatesStr = "delete ModuleShdates ms where ms.moduleId=:moduleId";
 	String delModuleStr = "delete Module mod where mod.moduleId=:moduleId";
 	String selCmodseqStr = "select cmod.seqNo from CourseModule cmod where cmod.courseId=:courseId and cmod.moduleId=:moduleId";
 	String delCourseModuleStr = "delete CourseModule cmod where cmod.courseId=:courseId and cmod.moduleId=:moduleId";
 	String updCmodseqStr = "update CourseModule cmod set cmod.seqNo=cmod.seqNo-1 where cmod.courseId=:courseId and cmod.seqNo>:seqNo";
 	
 	
     
      logger.debug("deleteModule begin");
 			
 	  long starttime = System.currentTimeMillis();
       Module mod = (Module) cmod.getModule();
       Map sectionMap = mod.getSections();
       String courseId = cmod.getCourseId();
       Integer modModuleId = cmod.getModuleId();
       Transaction tx = null;
 
       //First delete all the sections
       String queryString = "from Section sec where sec.moduleId = :moduleId";
       List secList = null;
   	try
 	{
 
       Session session = hibernateUtil.currentSession();
       Query query = session.createQuery(queryString);
       query.setParameter("moduleId",modModuleId);
 
       Iterator itr = query.iterate();
 
       Section sec = null;
       secList = new ArrayList();
       while (itr.hasNext()) {
       	sec = (Section) itr.next();
       	secList.add(sec);
        }
 
     if (secList != null)
     {
       for (ListIterator i = secList.listIterator(); i.hasNext(); )
       {
 		sectionDB.deleteSection((Section)i.next(), courseId, userId);
       }
     }
 	  }
     catch (HibernateException he)
     {
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
     	try
 		  {
 	      	hibernateUtil.closeSession();
 		  }
 	      catch (HibernateException he)
 		  {
 			  logger.error(he.toString());
 			  throw he;
 		  }
 	}
     try
 	{
 
       Session session = hibernateUtil.currentSession();
 
 	      tx = session.beginTransaction();
 
 	      affectedEntities = session.createQuery(delModuleShdatesStr).setInteger("moduleId", modModuleId).executeUpdate();
 	      System.out.println(affectedEntities+" row was deleted from MELETE_MODULE_SHDATES");
 
 	      affectedEntities = session.createQuery(delModuleStr).setInteger("moduleId", modModuleId).executeUpdate();
 	      System.out.println(affectedEntities+" row was deleted from MELETE_MODULE");
 
 	      Query q=session.createQuery(selCmodseqStr);
 	      q.setParameter("courseId", courseId);
 		  q.setParameter("moduleId",modModuleId);
 		  Integer seqNo = (Integer)q.uniqueResult();
 		  System.out.println("Seq no of deleted module is "+seqNo.intValue());
 		  
 		  affectedEntities = session.createQuery(delCourseModuleStr).setString("courseId",courseId).setInteger("moduleId", modModuleId).executeUpdate();
 	      System.out.println(affectedEntities+" row was deleted from MELETE_COURSE_MODULE");
 
 	      affectedEntities = session.createQuery(updCmodseqStr).setString("courseId", courseId).setInteger("seqNo", seqNo).executeUpdate();
 	      System.out.println(affectedEntities+" row was updated in MELETE_COURSE_MODULE");
 	
 	      
 	      tx.commit();
 	      logger.debug("Deleted module");
 
 
 	    }
 	    catch (HibernateException he)
 	    {
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
 	    	try
 			  {
 		      	hibernateUtil.closeSession();
 			  }
 		      catch (HibernateException he)
 			  {
 				  logger.error(he.toString());
 				  throw he;
 			  }
 		}
 	    if (userId != null)
 	    {
 	    MeleteBookmarks mb = new MeleteBookmarks();
 		mb.setUserId(userId);
 		mb.setCourseId(courseId);
 		mb.setModuleId(modModuleId);
 		bookmarksDB.deleteBookmark(mb);
 	    }
 	    long endtime = System.currentTimeMillis();
 
 		logger.debug("delete module end " +(endtime - starttime));
 
      }*/
 
 	
 	 public Module getModule(int moduleId) throws HibernateException {
 	 	Module mod = null;
 	 	try
 		{
 	 		Session session = hibernateUtil.currentSession();
 	 		String queryString = "select module from Module as module where module.moduleId = :moduleId";
 	 		mod = (Module) session.createQuery(queryString).setParameter("moduleId", new Integer(moduleId)).uniqueResult();
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
 	    return mod;
 	 }
 
 	 public List getSections(int moduleId) throws HibernateException {
 	 	List sectionsList = new ArrayList();
 	 	Module mod = null;
 	 	Query sectionQuery = null;
 	 	try
 		{
 	      Session session = hibernateUtil.currentSession();
 
 	      String queryString = "select section from Section as section where section.moduleId = :moduleId and section.deleteFlag = 0";
 	      sectionsList = session.createQuery(queryString).setParameter("moduleId", new Integer(moduleId)).list();
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
 	    return sectionsList;
 	  }
 
 
      public void archiveModule(CourseModule cmod) throws Exception {
      	Transaction tx = null;
 	 	try
 		{
 
 	      Session session = hibernateUtil.currentSession();
 
 	      tx = session.beginTransaction();
 	    int modSeqNo = -1;
 
 	      cmod.setArchvFlag(true);
 	      Date currentDate = Calendar.getInstance().getTime();
 	      cmod.setDateArchived(currentDate);
 	      session.saveOrUpdate(cmod);
 	      modSeqNo = cmod.getSeqNo();
 	      cmod.setSeqNo(-1);
 
 	       String queryString = "from CourseModule cmod where cmod.courseId = :courseId  and cmod.seqNo > :seqno";
 	      Query query = session.createQuery(queryString);
 	      query.setParameter("courseId",cmod.getCourseId());
 	      query.setParameter("seqno",new Integer(modSeqNo));
 
 	      Iterator itr = query.iterate();
 
 	      CourseModule cmodObj = null;
 	      while (itr.hasNext()) {
 	      	cmodObj = (CourseModule) itr.next();
 	      	cmodObj.setSeqNo(cmodObj.getSeqNo() - 1);
 	      	session.saveOrUpdate(cmodObj);
 	      }
 
 	      tx.commit();
 
 	      //session.flush();
 
 	    }
 	    catch (HibernateException he)
 	    {
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
 	    	try
 			  {
 		      	hibernateUtil.closeSession();
 			  }
 		      catch (HibernateException he)
 			  {
 				  logger.error(he.toString());
 				  throw he;
 			  }
 		}
 
      }
 /* MANAGE TAB FUNCTIONALITY RELATED TRANSCATIONS*/
 
 	 /**
 	  * author : rashmi
 	  * created on: 11 Jan 2005
 	 * @param courseId
 	 * @return list of archived modules of the course
 	 */
 	public List getArchivedModules(String course_id)
 	 {
 		List archModules = new ArrayList();
 		 try
 			{
 		 	   Session session = hibernateUtil.currentSession();
 			   Query q=session.createQuery("select cm from CourseModule cm where cm.courseId =:course_id and cm.archvFlag=1 order by cm.dateArchived");
 			   q.setParameter("course_id", course_id);
 
 			   archModules = q.list();
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
 		    return archModules ;
 	 }
 
 	/**
 	 * author : rashmi
      * created on: 11 Jan 2005
 	 * @param restoreModules
 	 * @throws MeleteException
 	 *
 	 * to restore a module, update course_module, assign it a seq and
 	 * set module_shdates start date as restored date and end date as 1 yr from there
 	 * revised on 3/24/05 by rashmi to fix bug#460
 	 */
 	public void restoreModules(List restoreModules) throws MeleteException
 	{
 		try{
 		     Session session = hibernateUtil.currentSession();
 		      Transaction tx = null;
 
 		   try{
 	   		//	1.for each element of list
 		   		for(int i=0; i < restoreModules.size(); i++ )
 		   		{
 		   	//	2.set course module object archv_flag to false, archived_date to null,
 		   			CourseModule coursemodule = (CourseModule)restoreModules.get(i);
 		   			Query q=session.createQuery("select cm1 from CourseModule cm1 where cm1.module.moduleId =:moduleId");
 					q.setParameter("moduleId", coursemodule.getModule().getModuleId());
 
 					CourseModule coursemodule1 = (CourseModule)(q.uniqueResult());
 		   			coursemodule1.setArchvFlag(false);
 		   			coursemodule1.setDateArchived(null);
 		   			coursemodule1.setDeleteFlag(false);
 
   			//  seq no as max+1
 		   		   q=session.createQuery("select max(cm.seqNo) from CourseModule cm where cm.courseId =:courseId");
 		 		   q.setParameter("courseId",coursemodule.getCourseId());
 
 		    	  Integer maxsequence = (Integer)q.uniqueResult();
 		    	  if(maxsequence.intValue() < 0)
 		    	  {
 		    	  	coursemodule1.setSeqNo(1);
 		    	  }
 		    	  else coursemodule1.setSeqNo(maxsequence.intValue()+1);
 
 			// 3. fetch module_shdate object
 	   			q=session.createQuery("select msh from ModuleShdates msh where msh.module.moduleId =:moduleId");
 					q.setParameter("moduleId", coursemodule.getModule().getModuleId());
 
 					ModuleShdates moduleShdate = (ModuleShdates)(q.uniqueResult());
					moduleShdate.setStartDate(null);
					moduleShdate.setEndDate(null);
 
 				//	3a.set start date as restored_date and end_date as 1 yr more
 			/*		GregorianCalendar cal = new GregorianCalendar();
 				       cal.set(Calendar.HOUR,8);
 				       cal.set(Calendar.MINUTE,0);
 				       cal.set(Calendar.SECOND,0);
 				       cal.set(Calendar.AM_PM,Calendar.AM);
 					moduleShdate.setStartDate(cal.getTime());
 					   cal.add(Calendar.YEAR, 1);
 				       cal.set(Calendar.HOUR,11);
 				       cal.set(Calendar.MINUTE,59);
 				       cal.set(Calendar.SECOND,0);
 				       cal.set(Calendar.AM_PM,Calendar.PM);
 					moduleShdate.setEndDate(cal.getTime());
 				*/
 
    			//4a. begin transaction
 		   			tx = session.beginTransaction();
 		   	//4b		save all objects
 		   	  	session.saveOrUpdate(coursemodule1);
 		   			session.saveOrUpdate(moduleShdate);
 		   		//4c.commit transaction
 					tx.commit();
 		   		}
 		   		return ;
 		     }
 		     catch (HibernateException he)
 		     {
 				if(tx !=null) tx.rollback();
 				logger.error(he.toString());
 				//he.printStackTrace();
 				throw new MeleteException(he.toString());
 		     }
 
 		} catch (Exception e)
 	     {
 			 logger.error(e.toString());
 			 throw new MeleteException(e.toString());
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
 
 	}
 
 
 
 	/**
 	 * @return Returns the hibernateUtil.
 	 */
 	public HibernateUtil getHibernateUtil() {
 		return hibernateUtil;
 	}
 	/**
 	 * @param hibernateUtil The hibernateUtil to set.
 	 */
 	public void setHibernateUtil(HibernateUtil hibernateUtil) {
 		this.hibernateUtil = hibernateUtil;
 	}
 
 	public String createTruncstr(String modTitle)
 	 {
 	      String truncTitle = null;
 	      if (modTitle.length() <= 30) return modTitle.trim();
 	      if (modTitle.length() > 30)
 	      {
 	      	truncTitle = modTitle.substring(0,27);
 	      	truncTitle = truncTitle.concat("...");
 	      }
 	      return truncTitle;
 	    }
 
 	public void createSubSection(ModuleObjService module, List secBeans) throws MeleteException
 	{
 		try{
 		     Session session = hibernateUtil.currentSession();
 	         Transaction tx = null;
 	         String pattern="\\.";
 	         Integer section_id;
 	         SectionBean secBean = null;
 			try{
 	         String sectionsSeqXML = module.getSeqXml();
 	         if(sectionsSeqXML == null) throw new MeleteException("indent_right_fail");
 			  SubSectionUtilImpl SectionUtil = new SubSectionUtilImpl();
 
 			  for (ListIterator i = secBeans.listIterator(); i.hasNext(); )
 		      {
 				secBean = (SectionBean)i.next();
 				  section_id = secBean.getSection().getSectionId();
 				  logger.debug("indenting section " + section_id);
 			      sectionsSeqXML = SectionUtil.MakeSubSection(sectionsSeqXML,section_id.toString());
 				}
 
 			  module.setSeqXml(sectionsSeqXML);
 
 			  	// save object
 			  tx = session.beginTransaction();
 			  session.saveOrUpdate(module);
 			  tx.commit();
 
 			  if (logger.isDebugEnabled()) logger.debug("commiting transaction and indenting multiple sections");
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
 			catch(MeleteException me){
 				if(tx !=null) tx.rollback();
 				throw me;
 				}
 	        	finally{
 				hibernateUtil.closeSession();
 				 }
 		}catch(Exception ex){
 
 		throw new MeleteException("indent_right_fail");
 		}
 	}
 
 	public void bringOneLevelUp(ModuleObjService module, List secBeans) throws MeleteException
 	{
 		try{
 		     Session session = hibernateUtil.currentSession();
 	         Transaction tx = null;
 	         String pattern="\\.";
 	         Integer section_id;
 	         SectionBean secBean = null;
 			try{
 	         String sectionsSeqXML = module.getSeqXml();
 	         if(sectionsSeqXML == null) throw new MeleteException("indent_left_fail");
 			  SubSectionUtilImpl SectionUtil = new SubSectionUtilImpl();
 			  if (secBeans.size() == 1)
 			  {
 				  secBean = (SectionBean)secBeans.get(0);
 				  section_id = secBean.getSection().getSectionId();
 				  logger.debug("bring up section " + section_id);
 				  sectionsSeqXML = SectionUtil.bringOneLevelUp(sectionsSeqXML,section_id.toString());
 			  }
 			  else
 			  {
 			  for (ListIterator i = secBeans.listIterator(); i.hasNext(); )
 		      {
 				secBean = (SectionBean)i.next();
 				int occurs = secBean.getDisplaySequence().split(pattern).length - 1;
 				//Only left indent non-top level sections
 				if (occurs > 1)
 				{
 				  section_id = secBean.getSection().getSectionId();
 		          sectionsSeqXML = SectionUtil.bringOneLevelUp(sectionsSeqXML,section_id.toString());
 				}
 		      }
 			  }
 
 			  module.setSeqXml(sectionsSeqXML);
 
 			  	// save object
 			  tx = session.beginTransaction();
 			  session.saveOrUpdate(module);
 			  tx.commit();
 
 			  if (logger.isDebugEnabled()) logger.debug("commiting transaction and left indenting multiple sections");
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
 			catch(MeleteException me){if(tx !=null) tx.rollback();throw me;}
 	        	finally{
 				hibernateUtil.closeSession();
 				 }
 		}catch(Exception ex){
 
 		throw new MeleteException("indent_left_fail");
 		}
 	}
 
 
 	public void sortModuleItem(Module module,String course_id, String Direction) throws MeleteException
 	{
 		try{
 		     Session session = hibernateUtil.currentSession();
 	         Transaction tx = null;
 
 			try{
 				List sortModules = new ArrayList();
 				List newModulesList = null;
 		        Query q=session.createQuery("select cm from CourseModule cm where cm.courseId =:course_id and cm.archvFlag=0 and cm.deleteFlag = 0 order by cm.seqNo");
 				q.setParameter("course_id", course_id);
 				sortModules = q.list();
 				// nothing to sort
 				if(sortModules.size() <=1) return;
 
 				int curr_seq = module.getCoursemodule().getSeqNo();
 				logger.debug("curr_seq" + curr_seq );
 				CourseModule curr_cm = (CourseModule)sortModules.get(curr_seq-1);
 				logger.debug("curr_cm" + curr_cm.getSeqNo() + curr_seq + curr_cm.getModuleId() + module.getModuleId());
 				if (!curr_cm.getModuleId().equals(module.getModuleId())) throw new MeleteException("sort_fail");
 				CourseModule change_cm = null;
 
 				if(Direction.equals("allUp"))
             	{
 	            	logger.debug("sort up module " + module.getModuleId());
 	            	curr_cm.setSeqNo(1);
 	            	newModulesList = new ArrayList();
 	            	newModulesList.add(curr_cm);
 	            	int startIdx = curr_seq -1;
 	            	while(startIdx > 0)
 	            	{
 	            		CourseModule cm = (CourseModule)sortModules.get(startIdx -1);
 	            		cm.setSeqNo(startIdx + 1);
 	            		newModulesList.add(cm);
 	            		startIdx--;
 	            	}
 
             	}else if(Direction.equals("up"))
 	            	{
 	            	logger.debug("sort up module " + module.getModuleId());
 	            	int change_seq = curr_seq -2;
 	            	change_cm = (CourseModule)sortModules.get(change_seq);
 	            	change_cm.setSeqNo(curr_seq);
 	            	curr_cm.setSeqNo(change_seq+1);
 			  }
 	         else if(Direction.equals("down"))
 			  {
 	         	logger.debug("sort down module " + module.getModuleId());
 	         	int change_seq = curr_seq;
             	change_cm = (CourseModule)sortModules.get(change_seq);
             	change_cm.setSeqNo(curr_seq);
             	curr_cm.setSeqNo(change_seq+1);
 			  }
 	         else if(Direction.equals("allDown"))
               {
                 logger.debug("sort all down module " + module.getModuleId());
                 int lastIndex = sortModules.size();
                 curr_cm.setSeqNo(lastIndex);
                 newModulesList = new ArrayList();
                 newModulesList.add(curr_cm);
                 int startIdx = curr_seq;
                 logger.debug("start idx :" + startIdx);
                 while(startIdx < lastIndex)
                 {
                     CourseModule cm = (CourseModule)sortModules.get(startIdx);
                     cm.setSeqNo(startIdx);
                     newModulesList.add(cm);
                     startIdx++;
                 }
             }
 
 			  	// save object
 			  tx = session.beginTransaction();
 			  if(newModulesList == null)
 			  {
 			  session.saveOrUpdate(change_cm);
 			  session.saveOrUpdate(curr_cm);
 			  }
 			  else{
 			  	for(int i=0;i < newModulesList.size(); i++)
 			    		session.saveOrUpdate(newModulesList.get(i));
 			  }
 			  tx.commit();
 
 			  if (logger.isDebugEnabled()) logger.debug("commiting transaction and sorting module id " + module.getModuleId());
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
 			catch(MeleteException me){if(tx !=null) tx.rollback();throw me;}
 	        	finally{
 				hibernateUtil.closeSession();
 				 }
 		}catch(Exception ex){
 			// Throw application specific error
 			ex.printStackTrace();
 		throw new MeleteException("sort_fail");
 		}
 	}
 
 	public void sortSectionItem(Module module, String section_id, String Direction) throws MeleteException
 	{
 		try{
 		     Session session = hibernateUtil.currentSession();
 	         Transaction tx = null;
 			try{
 	         String sectionsSeqXML = module.getSeqXml();
 	         SubSectionUtilImpl SectionUtil = new SubSectionUtilImpl();
 	         if(Direction.equals("allUp"))
 			  {
 	         	logger.debug("sort up section " + section_id);
 			  	 sectionsSeqXML = SectionUtil.moveAllUpSection(sectionsSeqXML,section_id);
 			  }
 	         else if(Direction.equals("up"))
 			  {
 	         	logger.debug("sort up section " + section_id);
 			  	 sectionsSeqXML = SectionUtil.moveUpSection(sectionsSeqXML,section_id);
 			  }
 	         else if(Direction.equals("down"))
 			  {
 	         	logger.debug("sort down section " + section_id);
 			  	 sectionsSeqXML = SectionUtil.moveDownSection(sectionsSeqXML,section_id);
 			  }else if(Direction.equals("allDown"))
 			  {
 	         	logger.debug("sort down section " + section_id);
 			  	 sectionsSeqXML = SectionUtil.moveAllDownSection(sectionsSeqXML,section_id);
 			  }
 			  module.setSeqXml(sectionsSeqXML);
 
 			  	// save object
 			  tx = session.beginTransaction();
 			  session.saveOrUpdate(module);
 			  tx.commit();
 
 			  if (logger.isDebugEnabled()) logger.debug("commiting transaction and sorting section id " + section_id);
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
 			catch(MeleteException me){if(tx !=null) tx.rollback();throw me;}
 	        	finally{
 				hibernateUtil.closeSession();
 				 }
 		}catch(Exception ex){
 		throw new MeleteException("sort_fail");
 		}
 	}
 
 	public void copyModule(Module module, String courseId, String userId) throws MeleteException
 	{
 		try
 		{
 			//get module and its sections
 			Module copyMod = new Module(module);
 			String firstName = UserDirectoryService.getCurrentUser().getFirstName();
 			String lastName = UserDirectoryService.getCurrentUser().getLastName();
 
 			DateFormat shortTime = DateFormat.getDateInstance(DateFormat.LONG);
 
 			copyMod.setCreatedByFname(firstName);
 			copyMod.setCreatedByLname(lastName);
 			copyMod.setTitle(copyMod.getTitle() + " (Copied " + shortTime.format(new Date())+" )");
 			ModuleShdates CopyModuleshowdates = new ModuleShdates((ModuleShdates)module.getModuleshdate());
 
 			// insert copy module with blank seq_xml and sections as null
 			addModule(copyMod, CopyModuleshowdates, userId, courseId);
 
 			String copyModSeqXml = module.getSeqXml();
 			//get sections
 			List<Section> toCopySections = getSections(module.getModuleId().intValue());
 			if (toCopySections != null && toCopySections.size() > 0)
 			{
 				for (Section toCopySection : toCopySections)
 				{
 					// with title as copy of xxx and sectionResource
 					Section copySection = new Section(toCopySection);
 					copySection.setCreatedByFname(firstName);
 					copySection.setCreatedByLname(lastName);
 					copySection.setModule(copyMod);
 					copySection.setTitle(copySection.getTitle() + " (Copied " + shortTime.format(new Date())+" )");
 					//insert section
 					Integer copySectionId = sectionDB.addSection(copyMod, copySection, false);
 					copySection.setSectionId(copySectionId);
 					//copySection.setModule(copyMod);
 					if (toCopySection.getContentType() != null && !toCopySection.getContentType().equals("notype"))
 					{
 						// if section content type is composed than create a new copy
 						if (toCopySection.getContentType().equals("typeEditor"))
 						{
 
 							String copyModCollId = meleteCHService.getCollectionId("typeEditor", copyMod.getModuleId());
 							String res_mime_type = meleteCHService.MIME_TYPE_EDITOR;
 							ContentResource cr = meleteCHService.getResource(toCopySection.getSectionResource().getResource().getResourceId());
 							byte[] secContentData = cr.getContent();
 
 							boolean encodingFlag = true;
 							String secResourceName = "Section_" + copySectionId;
 							String secResourceDescription = "compose content";
 
 							ResourcePropertiesEdit res = meleteCHService.fillInSectionResourceProperties(encodingFlag, secResourceName,
 									secResourceDescription);
 							String newResourceId = meleteCHService
 									.addResourceItem(secResourceName, res_mime_type, copyModCollId, secContentData, res);
 
 							MeleteResource copyMelResource = new MeleteResource((MeleteResource) toCopySection.getSectionResource().getResource());
 							copyMelResource.setResourceId(newResourceId);
 							sectionDB.insertMeleteResource(copySection, copyMelResource);
 						}
 						else
 						{
 							// insert section resource with same melete resource
 							sectionDB.insertSectionResource(copySection, (MeleteResource) toCopySection.getSectionResource().getResource());
 						}
 					}
 					// replace with new copied section
 					copyModSeqXml = copyModSeqXml.replace(toCopySection.getSectionId().toString(), copySectionId.toString());
 				}
 				// update module seq xml
 				copyMod.setSeqXml(copyModSeqXml);
 				updateModule(copyMod);
 			}
 
 		}
 		catch (Exception ex)
 		{
 			ex.printStackTrace();
 			throw new MeleteException("copy_fail");
 		}
 	}
 
 	public void moveSection(Section section,Module selectedModule) throws MeleteException
 	{
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			Transaction tx = null;
 			try
 			{
 				// add section to selected Module
 				String selectedModSeqXml = selectedModule.getSeqXml();
 				SubSectionUtilImpl SectionUtil = new SubSectionUtilImpl();
 				SectionUtil.addSectiontoList(selectedModSeqXml, section.getSectionId().toString());
 				selectedModSeqXml = SectionUtil.storeSubSections();
 				selectedModule.setSeqXml(selectedModSeqXml);
 
 				// delete section association from the previous module
 				Module prev_module = (Module) section.getModule();
 				String prevModSeqXml = prev_module.getSeqXml();
 				prevModSeqXml = SectionUtil.deleteSection(prevModSeqXml, section.getSectionId().toString());
 				prev_module.setSeqXml(prevModSeqXml);
 
 				section.setModule(selectedModule);
 				section.setModuleId(selectedModule.getModuleId().intValue());
 
 				// save object
 				tx = session.beginTransaction();
 				session.saveOrUpdate(section);
 				session.saveOrUpdate(prev_module);
 				session.saveOrUpdate(selectedModule);
 				
 				// move section file from content hosting to new location
 				if(section.getContentType() != null && section.getContentType().equals("typeEditor"))
 				{
 				String secContentFile = section.getSectionResource().getResource().getResourceId();
 				String destinationColl = meleteCHService.getCollectionId("typeEditor", selectedModule.getModuleId());
 				String newResId = meleteCHService.moveResource(secContentFile, destinationColl);
 				MeleteResource old = (MeleteResource)section.getSectionResource().getResource();
 				MeleteResource newMR = new MeleteResource(old);
 				newMR.setResourceId(newResId);
 				session.save(newMR);
 				SectionResource newSR = (SectionResource)section.getSectionResource();
 				newSR.setResource(newMR);
 				section.setSectionResource(newSR);
 				session.update(newSR);
 				session.saveOrUpdate(section);
 				session.flush();
 				session.delete(old);				
 				}				
 				
 				//Code that moves the bookmarks
 				Map bookmarks = section.getBookmarks();
 				int bookmarkSize = bookmarks.size();
 		    	if (bookmarkSize > 0)
 		    	{
 		    		Iterator keyValuePairs = bookmarks.entrySet().iterator();
 		    		while (keyValuePairs.hasNext())
 		    		{
 		    		  Map.Entry entry = (Map.Entry) keyValuePairs.next();
 		    		  MeleteBookmarks mb = (MeleteBookmarks) entry.getValue();
 		    		  mb.setModuleId(selectedModule.getModuleId().intValue());
 		    		  session.saveOrUpdate(mb);
 		    		}
 		    	}
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
 				throw he;
 			}
 			catch (MeleteException me)
 			{
 				if (tx != null) tx.rollback();
 				throw me;
 			}
 			finally
 			{
 				hibernateUtil.closeSession();
 			}
 		}
 		catch (Exception e)
 		{
 			throw new MeleteException("move_section_fail");
 		}
 	}
 
 
 	public String prepareModuleSectionsForPrint(Module module) throws MeleteException
 	{
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			Transaction tx = null;
 			StringBuffer printText = null;
 			try
 			{
 				printText = new StringBuffer("<h3>" + module.getCoursemodule().getSeqNo() +"  " +module.getTitle() + "</h3>");
 				SubSectionUtilImpl ssuImpl = new SubSectionUtilImpl();
 				ssuImpl.traverseDom(module.getSeqXml(),new Integer(module.getCoursemodule().getSeqNo()).toString());
 				List<SecLevelObj> xmlSecList = ssuImpl.getXmlSecList();
 				Map printSections = module.getSections();
 
 				if(xmlSecList != null)
 		  		{
 		  			for (ListIterator<SecLevelObj> k = xmlSecList.listIterator(); k.hasNext(); ){
 		  		   		SecLevelObj slObj = k.next();
 		  		   		if (slObj != null)
 		  		   		{
 		  		   			Section sec =(Section)printSections.get(new Integer(slObj.getSectionId()));
 
 		  		   			printText.append("<h4>" +slObj.getDispSeq() +"  " + sec.getTitle()+"</h4>")	;
 		  		   			if(sec.getSectionResource() != null)
 		  		   				printText.append("<p><i>" +getLicenseInformation((MeleteResource)sec.getSectionResource().getResource())+"</i></p>");
 
 		  		   			if (sec.getInstr() != null && sec.getInstr().length() !=0 ) printText.append("<p> <i>Instructions:</i> " + sec.getInstr() + "</p>");
 							if (sec.getContentType() == null || sec.getContentType().equals("notype") || sec.getSectionResource() == null || sec.getSectionResource().getResource() == null)
 							{
 								continue;
 							}
 							String resourceId = sec.getSectionResource().getResource().getResourceId();
 							ContentResource resource = meleteCHService.getResource(resourceId);
 							if (sec.getContentType().equals("typeEditor"))
 							{
 								byte[] data = resource.getContent();
 							//	printText.append("<p>" + new String(data) + "</p>");
 								printText.append(new String(data));
 							}
 							if (sec.getContentType().equals("typeLink"))
 							{
 								String url = resource.getUrl();
 								url = url.replaceAll(" ", "%20");
 								printText.append("<a href=\"" + url + "\" target=\"_blank\">");
 								printText.append(resource.getProperties().getProperty(ResourceProperties.PROP_DISPLAY_NAME));
 								printText.append("</a>");
 							}
 
 							if (sec.getContentType().equals("typeUpload"))
 							{
 								String url = resource.getUrl();
 								url = url.replaceAll(" ", "%20");
 								printText.append("<iframe id=\"iframe1\" src=\"" + url + "\" scrolling=\"auto\" width=\"100%\" border=\"0\" frameborder=\"0\"></iframe>");
 							}
 
 						}
 		  			}
 		  		}
 				return 	printText.toString();
 			}
 			catch (StaleObjectStateException sose)
 			{
 				logger.error("stale object exception" + sose.toString());
 			}
 			catch (HibernateException he)
 			{
 				if (tx != null) tx.rollback();
 				logger.error(he.toString());
 				throw he;
 			}
 			finally
 			{
 				hibernateUtil.closeSession();
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			throw new MeleteException("print_module_fail");
 		}
 		return null;
 	}
 
 
 	private String getLicenseInformation(MeleteResource melResource)
 	{
 		ResourceLoader rl = new ResourceLoader("melete_license");
 		String licenseStr="";
 
 		if(melResource == null)return licenseStr;
 
 		if(melResource.getLicenseCode() == 1)
 		{
 		licenseStr = rl.getString("license_info_copyright");
 		if (melResource.getCopyrightYear() != null)
 			licenseStr += melResource.getCopyrightYear()+", ";
 		licenseStr += melResource.getCopyrightOwner();
 		}
 
 		if(melResource.getLicenseCode() == 2)
 		{
 		licenseStr = rl.getString("license_info_dedicated_to");
 		if (melResource.getCopyrightYear() != null)
 			licenseStr += melResource.getCopyrightYear()+", ";
 		if (melResource.getCopyrightOwner() != null)
 		licenseStr += melResource.getCopyrightOwner();
 		}
 
 		if(melResource.getLicenseCode() == 3)
 		{
 		licenseStr = rl.getString("license_info_licensed_under");
 		if (melResource.getCopyrightYear() != null)
 			licenseStr += melResource.getCopyrightYear()+", ";
 		if (melResource.getCopyrightOwner() != null)
 		licenseStr += melResource.getCopyrightOwner();
 		}
 
 		if(melResource.getLicenseCode() == 4)
 		{
 		licenseStr = rl.getString("license_info_fairuse");
 		if (melResource.getCopyrightYear() != null)
 			licenseStr += melResource.getCopyrightYear()+", ";
 		if (melResource.getCopyrightOwner() != null)
 		licenseStr += melResource.getCopyrightOwner();
 		}
 		return licenseStr;
 	}
 
 // clean up code for admin tool	
 	public int cleanUpDeletedModules() throws Exception
 	{
 		if (!meleteSecurityService.isSuperUser(UserDirectoryService.getCurrentUser().getId())) throw new MeleteException("admin_allow_cleanup");
 
 		int delCount = 0;
 		long totalStart = System.currentTimeMillis();
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			Transaction tx = null;
 			try
 			{
 				// get deleted modules group by course id
 				String queryString = " from CourseModule cmod where cmod.deleteFlag = 1 order by cmod.courseId";
 				Query query = session.createQuery(queryString);
 				List<CourseModule> res = query.list();
 				Map deletedModules = new HashMap<String, ArrayList<Module>>();
 
 				for (Iterator<CourseModule> itr = res.listIterator(); itr.hasNext();)
 				{
 					CourseModule cm = itr.next();
 					if (deletedModules.containsKey(cm.getCourseId()))
 					{
 						ArrayList delmodules = (ArrayList) deletedModules.get(cm.getCourseId());
 						delmodules.add(cm.getModule());
 						deletedModules.put(cm.getCourseId(), delmodules);
 					}
 					else
 					{
 						ArrayList delmodule = new ArrayList();
 						delmodule.add(cm.getModule());
 						deletedModules.put(cm.getCourseId(), delmodule);
 					}
 				}
 				logger.debug("map is created" + deletedModules.size());
 				delCount = deletedModules.size();
 				// for each course id
 				Set alldelCourses = deletedModules.keySet();
 				for (Iterator iter = alldelCourses.iterator(); iter.hasNext();)
 				{
 					// for that course id get all melete resources from melete_resource
 					long starttime = System.currentTimeMillis();
 					String toDelCourseId = (String) iter.next();
 					logger.debug("processing for " + toDelCourseId);
 					List activenArchModules = getActivenArchiveModules(toDelCourseId);
 					List<Module> delModules = (ArrayList) deletedModules.get(toDelCourseId);
 					
 					if(activenArchModules == null || activenArchModules.size() == 0)
 					{
 						if (!session.isOpen()) session = hibernateUtil.currentSession();
 						tx = session.beginTransaction();
 						
 						String allModuleIds = "(";						
 						for(Module mod:delModules)
 							allModuleIds = allModuleIds.concat(mod.getModuleId().toString() + ",");						
 						if(allModuleIds.lastIndexOf(",") != -1)
 							allModuleIds = allModuleIds.substring(0,allModuleIds.lastIndexOf(","))+" )";
 						
 						deleteEverything(toDelCourseId, session, allModuleIds);						
 						// remove entire collection
 						meleteCHService.removeCollection(toDelCourseId,null);
 						tx.commit();
 						continue;
 					}
 					// parse and list all names which are in use
 					List<String> activeResources = getActiveResourcesFromList(activenArchModules);
 					List<String> allCourseResources = getAllMeleteResourcesOfCourse(toDelCourseId);
 					int allresourcesz = allCourseResources.size();
 					// compare the lists and not in use resources are
 					if (!session.isOpen()) session = hibernateUtil.currentSession();
 					tx = session.beginTransaction();
 					if (allCourseResources != null && activeResources != null)
 					{
 						logger.debug("active list and all" + activeResources.size() + " ; " + allCourseResources.size());
 						allCourseResources.removeAll(activeResources);												
 					}
 					// delete modules and module collection and sections marked for delete
 					
 					for (Iterator delModuleIter = delModules.listIterator(); delModuleIter.hasNext();)
 					{
 						Module delModule = (Module) delModuleIter.next();
 						Integer delModuleId = delModule.getModuleId();
 						String allSecIds = null;
 						Map allModuleDelSecs = delModule.getDeletedSections();
 						allSecIds = getAllSectionIds(allModuleDelSecs);
 										
 						String updSectionResourceStr = "update SectionResource sr set sr.resource = null where sr.section in " + allSecIds;
 						String delSectionResourceStr = "delete SectionResource sr where sr.section in " + allSecIds;
 					//	String delSectionStr = "delete Section s where s.moduleId=:moduleId";
 						String delSectionStr = "delete Section s where s.sectionId in " + allSecIds;
 						String delCourseModuleStr = "delete CourseModule cm where cm.moduleId=:moduleId";
 						String delModuleshDatesStr = "delete ModuleShdates msh where msh.moduleId=:moduleId";
 						String delModuleStr = "delete Module m where m.moduleId=:moduleId";
 						
 						if (allSecIds != null)
 						{
 							try{
 							int deletedEntities = session.createQuery(updSectionResourceStr).executeUpdate();
 							deletedEntities = session.createQuery(delSectionResourceStr).executeUpdate();
 							deletedEntities = session.createQuery(delSectionStr).executeUpdate();
 							}
 							catch(Exception e){
 								logger.info("error deleting section and resources " + allSecIds + " for module" + delModuleId);
 								continue;
 							}
 						}
 						logger.debug("deleting stuff for module" + delModuleId);
 					//	int deletedEntities = session.createQuery(delSectionStr).setInteger("moduleId", delModuleId).executeUpdate();
 						int deletedEntities = session.createQuery(delCourseModuleStr).setInteger("moduleId", delModuleId).executeUpdate();
 						deletedEntities = session.createQuery(delModuleshDatesStr).setInteger("moduleId", delModuleId).executeUpdate();
 						deletedEntities = session.createQuery(delModuleStr).setInteger("moduleId", delModuleId).executeUpdate();						
 						meleteCHService.removeCollection(toDelCourseId, "module_"+delModuleId.toString());
 					}
 					logger.debug("looking for extra sections marked for delete");
 					// look for sections just marked for delete
 					queryString = " from Section sec where sec.deleteFlag = 1 and sec.module.coursemodule.courseId=:courseId order by sec.moduleId";
 					query = session.createQuery(queryString);
 					query.setString("courseId", toDelCourseId);
 					List<Section> sectionsres = query.list();
 					if(sectionsres != null)
 					{
 					String allSecIds = "(";
 					
 					for(Section sec:sectionsres)
 						allSecIds = allSecIds.concat(sec.getSectionId().toString() + ",");
 					
 					if(allSecIds.lastIndexOf(",") != -1)
 						allSecIds = allSecIds.substring(0,allSecIds.lastIndexOf(","))+" )";	
 									
 					String updSectionResourceStr = "update SectionResource sr set sr.resource = null where sr.section in " + allSecIds;
 					String delSectionResourceStr = "delete SectionResource sr where sr.section in " + allSecIds;
 					String delSectionStr = "delete Section s where s.sectionId in " + allSecIds;
 				
 					try{
 						int deletedEntities = session.createQuery(updSectionResourceStr).executeUpdate();
 						deletedEntities = session.createQuery(delSectionResourceStr).executeUpdate();
 						deletedEntities = session.createQuery(delSectionStr).executeUpdate();
 						}
 						catch(Exception e){
 							logger.info("error deleting extra section and resources " + allSecIds);	
 							continue;
 						}
 					
 					}
 					logger.debug("suceess remove of deleted modules and their sections.NOW MOVE TO melete resources");
 					// delete melete resource and from content resource
 					for (Iterator delIter = allCourseResources.listIterator(); delIter.hasNext();)
 					{
 						String delResourceId = (String) delIter.next();
 						logger.debug("now deleting mr " + delResourceId);
 						String delMeleteResourceStr = "delete MeleteResource mr where mr.resourceId=:resourceId";
 						int deletedEntities = session.createQuery(delMeleteResourceStr).setString("resourceId", delResourceId).executeUpdate();
 						meleteCHService.removeResource(delResourceId);
 					}				
 					// if course collection is empty than delete course collection
 					meleteCHService.removeCourseCollection(toDelCourseId);
 
 					tx.commit();
 					long endtime = System.currentTimeMillis();
 					logger.debug("to cleanup course with " + allresourcesz + " resources and del modules " + delModules.size() +" and del resources"+ allCourseResources.size()+", it took "
 							+ (endtime - starttime) + "ms");
 				} // for end
 				long totalend = System.currentTimeMillis();
 				logger.debug("to cleanup deleted modules from " + deletedModules.size() + "courses it took " + (totalend - totalStart) + "ms");
 			}
 			catch (HibernateException he)
 			{
 				if (tx != null) tx.rollback();
 				logger.error(he.toString());
 				throw he;
 			}
 			finally
 			{
 				hibernateUtil.closeSession();
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 			throw new MeleteException("cleanup_module_fail");
 		}
 		return delCount;
 	}	
 		
 	private void deleteEverything(String delCourseId, Session session, String allModuleIds)
 	{
 		logger.debug("delete everything for " + delCourseId + allModuleIds);
 		String delMeleteResourceStr = "delete MeleteResource mr where mr.resourceId like '%" + delCourseId + "%'";
 		String delSectionResourceStr = "delete SectionResource sr where sr.resource.resourceId like '%" + delCourseId + "%'";
 		String delSectionStr = "delete Section s where s.moduleId in " + allModuleIds;
 		String delCourseModuleStr = "delete CourseModule cm where cm.courseId= '" + delCourseId + "'";
 		String delModuleshDatesStr = "delete ModuleShdates msh where msh.moduleId in " +  allModuleIds;
 		String delModuleStr = "delete Module m where m.moduleId in " + allModuleIds;
 		
 		int deletedEntities = session.createQuery(delSectionResourceStr).executeUpdate();
 		logger.debug("deleted sr " + deletedEntities);
 		deletedEntities = session.createQuery(delSectionStr).executeUpdate();
 		logger.debug("deleted section " + deletedEntities);
 		deletedEntities = session.createQuery(delModuleshDatesStr).executeUpdate();
 		logger.debug("deleted msh " + deletedEntities);
 		deletedEntities = session.createQuery(delCourseModuleStr).executeUpdate();
 		logger.debug("deleted cm " + deletedEntities);
 		deletedEntities = session.createQuery(delModuleStr).executeUpdate();
 		logger.debug("deleted module " + deletedEntities);		
 		deletedEntities = session.createQuery(delMeleteResourceStr).executeUpdate();
 		logger.debug("deleted mr " + deletedEntities);
 	}
 	
 	
 	private String getAllSectionIds(Map deletedSections)
 	{			
 		StringBuffer allIds = null;
 		String a = null;
 		if(deletedSections != null)
 		{
 			allIds = new StringBuffer("(");
 			for(Iterator i=deletedSections.keySet().iterator();i.hasNext();)
 			{
 				Object obj = i.next();
 				allIds.append(obj + ",");
 			}
 		}
 		if(allIds != null && allIds.lastIndexOf(",") != -1)
 			a = allIds.substring(0,allIds.lastIndexOf(","))+" )";	
 		return a;
 	}
 	
 	protected List getActiveResourcesFromList(List activenArchModules)
 	{
 		List<String> secEmbed = new ArrayList();
 		try{
 		 Iterator<Module> i = activenArchModules.iterator();
 		  while (i.hasNext())
 		  {
 			Module mod = i.next();
 			
 			String modSeqXml = mod.getSeqXml();
 			SubSectionUtilImpl SectionUtil = new SubSectionUtilImpl();
 			if(modSeqXml == null) continue;
 			List<Element> allsec = SectionUtil.getAllSections(modSeqXml);
 			if(allsec == null)continue;
 			for (Iterator<Element> itr = allsec.iterator(); itr.hasNext();)
 			{
 				Section sec = sectionDB.getSection(Integer.parseInt(((Element) itr.next()).attributeValue("id")));
 				if(sec.getContentType().equals("notype") || sec.getSectionResource().getResource() == null) continue;
 				
 				if (sec.getContentType().equals("typeEditor"))
 				{
 					List l = meleteCHService.findAllEmbeddedImages(sec.getSectionResource().getResource().getResourceId());
 					if(l != null)secEmbed.addAll(l);
 				}
 				else secEmbed.add(sec.getSectionResource().getResource().getResourceId());
 			}
 		  }
 		  logger.debug("before sorting and removing dups" + secEmbed.size());
 		  //sort list and remove duplicates
 		  SortedSet s =  new TreeSet();
 		  s.addAll(secEmbed);
 		  secEmbed.clear();
 		  secEmbed.addAll(s);
 		}
 		catch(Exception e){e.printStackTrace();return null;}
 		  return secEmbed;
 	}
 	
 	
 	protected List getAllMeleteResourcesOfCourse(String toDelCourseId)
 	{
 		List allres = meleteCHService.getListofMediaFromCollection("/private/meleteDocs/"+toDelCourseId+"/uploads/");
 		ArrayList<String> allresNames = new ArrayList();
 		if(allres == null) return null;
 		for(Iterator iter = allres.listIterator(); iter.hasNext();)
 		{
 			ContentResource cr = (ContentResource)iter.next();
 			allresNames.add(cr.getId());
 		}	
 		  SortedSet s =  new TreeSet();
 		  s.addAll(allresNames);
 		  allresNames.clear();
 		  allresNames.addAll(s);
 		  return allresNames;		
 	}
 	
 // end clean up deleted stuff code	
 	 /**
 	 * @param meleteSecurityService The meleteSecurityService to set.
 	 */
 	public void setMeleteSecurityService(MeleteSecurityService meleteSecurityService) {
 		this.meleteSecurityService = meleteSecurityService;
 	}
 
 	/**
 	 * @param sectionDB the sectionDB to set
 	 */
 	public void setSectionDB(SectionDB sectionDB)
 	{
 		this.sectionDB = sectionDB;
 	}
 
 	/**
 	 * @param meleteCHService the meleteCHService to set
 	 */
 	public void setMeleteCHService(MeleteCHService meleteCHService)
 	{
 		this.meleteCHService = meleteCHService;
 	}
 
 	public void setBookmarksDB(MeleteBookmarksDB bookmarksDB)
 	{
 		this.bookmarksDB = bookmarksDB;
 	}
 }
 
