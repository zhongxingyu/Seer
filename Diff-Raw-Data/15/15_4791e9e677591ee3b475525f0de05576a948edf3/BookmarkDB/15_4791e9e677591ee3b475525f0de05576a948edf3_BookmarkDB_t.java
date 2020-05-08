 /**********************************************************************************
  *
  * $URL: https://source.sakaiproject.org/contrib/etudes/melete/trunk/melete-impl/src/java/org/etudes/component/app/melete/BookmarkDB.java $
  * $Id: BookmarkDB.java 60573 2009-05-19 20:17:20Z mallika@etudes.org $
  ***********************************************************************************
  *
  * Copyright (c) 2010, 2011 Etudes, Inc.
  *
  * Portions completed before September 1, 2008 Copyright (c) 2004, 2005, 2006, 2007, 2008 Foothill College, ETUDES Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
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
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.etudes.api.app.melete.BookmarkObjService;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.StaleObjectStateException;
 import org.hibernate.Transaction;
 
 public class BookmarkDB
 {
 	private HibernateUtil hibernateUtil;
 	private Log logger = LogFactory.getLog(BookmarkDB.class);
 
 	/**
 	 * default constructor
 	 */
 	public BookmarkDB()
 	{
 
 	}
 
 	/**
 	 * Deletes a bookmark
 	 * 
 	 * @param bookmarkId
 	 *        Bookmark id
 	 * @throws Exception
 	 */
 	public void deleteBookmark(int bookmarkId) throws Exception
 	{
 		Transaction tx = null;
 	
 		String delBookmarkStr = "delete Bookmark bm where bm.bookmarkId=:bookmarkId";
 		try
 		{
 			Session session = getHibernateUtil().currentSession();
 			tx = session.beginTransaction();
 			session.createQuery(delBookmarkStr).setInteger("bookmarkId", bookmarkId).executeUpdate();
 			tx.commit();
 		}
 		catch (HibernateException he)
 		{
 			if (tx != null) tx.rollback();
 			logger.error(he.toString());
 			throw he;
 		}
 		catch (Exception e)
 		{
 			if (tx != null) tx.rollback();
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
 
 	/**
 	 * Get bookmark for this user, site and section
 	 * 
 	 * @param userId
 	 *        User id
 	 * @param siteId
 	 *        Site id
 	 * @param sectionId
 	 *        Section id
 	 * @return Bookmark object or null if bookmark doesn't exist
 	 */
 	public Bookmark getBookmark(String userId, String siteId, int sectionId)
 	{
 		Bookmark mb = null;
 		try
 		{
 			Session session = getHibernateUtil().currentSession();
 			Query q = session
 					.createQuery("from Bookmark mb where mb.userId =:userId and mb.siteId=:siteId and mb.section.sectionId=:sectionId and mb.section.module.coursemodule.archvFlag = 0");
 			q.setParameter("userId", userId);
 			q.setParameter("siteId", siteId);
 			q.setParameter("sectionId", sectionId);
 			mb = (Bookmark) q.uniqueResult();
 
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
 		return mb;
 	}
 
 	/**
 	 * Get bookmarks for this user and site
 	 * 
 	 * @param userId
 	 *        User id
 	 * @param siteId
 	 *        Site id
 	 * @return List of bookmarks, empty list if none are found
 	 */
 	public List<BookmarkObjService> getBookmarks(String userId, String siteId)
 	{
 		List<? extends SpecialAccess> saList = new ArrayList<SpecialAccess>();
 		List<BookmarkObjService> bmList = new ArrayList<BookmarkObjService>();
 		try
 		{
 			Session session = getHibernateUtil().currentSession();
 			Query q = session
 					.createQuery("select sa from SpecialAccess sa,CourseModule cm where sa.users like :userId and sa.moduleId=cm.moduleId and cm.courseId = :siteId");
 			q.setParameter("userId", "%" + userId + "%");
 			q.setParameter("siteId", siteId);
 			saList = q.list();
 			q = session
 					.createQuery("from Bookmark mb where mb.userId =:userId and mb.siteId=:siteId and mb.section.module.coursemodule.archvFlag = 0 order by mb.lastVisited desc,mb.bookmarkId asc");
 			q.setParameter("userId", userId);
 			q.setParameter("siteId", siteId);
 			bmList = q.list();
 			if (bmList != null)
 			{
 				for (ListIterator<?> i = bmList.listIterator(); i.hasNext();)
 				{
 					Bookmark bmark = (Bookmark) i.next();
 					if (bmark.getNotes() != null)
 					{
 						if (bmark.getNotes().length() > 70)
 						{
 							bmark.setBriefNotes(bmark.getNotes().substring(0, 69) + "...");
 						}
 						else
 						{
 							bmark.setBriefNotes(bmark.getNotes());
 						}
 					}
 					else
 					{
 						bmark.setBriefNotes("");
 					}
 					if ((saList == null) || (saList.size() == 0))
 					{
 						bmark.setSectionVisibleFlag(bmark.getSection().getModule().getModuleshdate().isVisibleFlag());
 					}
 					else
 					{
 						boolean accessFound = false;
 						// Iterate through special access list to see if this module has special access set
 						for (ListIterator<?> j = saList.listIterator(); j.hasNext();)
 						{
 							SpecialAccess sa = (SpecialAccess) j.next();
 							// If module matches, compare dates and set visible flag
 							if (sa.getModuleId() == bmark.getSection().getModule().getModuleId().intValue())
 							{
 								accessFound = true;
 								bmark.setSectionVisibleFlag(isSectionVisible(sa, (ModuleShdates) bmark.getSection().getModule().getModuleshdate()));
 							}
 						}
 						// If access has not been set for this module, use module's dates
 						if (accessFound == false)
 						{
 							bmark.setSectionVisibleFlag(bmark.getSection().getModule().getModuleshdate().isVisibleFlag());
 						}
 
 					}
 				}
 			}
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
 		return bmList;
 
 	}
 
 	/**
 	 * @return Returns the hibernateUtil.
 	 */
 	public HibernateUtil getHibernateUtil()
 	{
 		return hibernateUtil;
 	}
 
 	/**
 	 * Returns the section bookmarked as the last visited. For students, checks to see if special access is defined. If restricted, returns 0.
 	 * 
 	 * @param isAuthor
 	 *        true for author, false otherwise
 	 * @param userId
 	 *        user id
 	 * @param siteId
 	 *        site id
 	 * @return
 	 */
 	public int getLastVisitSectionId(boolean isAuthor, String userId, String siteId)
 	{
 		int sectionId = 0;
 		try
 		{
 			Session session = getHibernateUtil().currentSession();
 			Query q = session
 					.createQuery("select mb.section from Bookmark mb where mb.userId =:userId and mb.siteId=:siteId and mb.lastVisited=1 and mb.section.module.coursemodule.archvFlag = 0");
 			q.setParameter("userId", userId);
 			q.setParameter("siteId", siteId);
 			Section section = (Section) q.uniqueResult();
 			if (isAuthor)
 			{
				Date startDate = section.getModule().getModuleshdate().getStartDate();
				Date endDate = section.getModule().getModuleshdate().getEndDate();
				//Since we no longer show invalid modules to instructors in list view,
				//we also don't want to show the link to a last visited section if it is invalid
				if ((startDate != null) && (endDate != null) && (startDate.compareTo(endDate) >= 0))
				{
					sectionId = 0;
				}
				else
				{
					sectionId = section.getSectionId().intValue();
				}
 			}
 			else
 			{
 				// Check to see if student has special access dates set for last visited module
 				int moduleId = section.getModule().getModuleId().intValue();
 				q = session.createQuery("from SpecialAccess sa where sa.users like :userId and sa.moduleId=:moduleId");
 				q.setParameter("userId", "%" + userId + "%");
 				q.setParameter("moduleId", moduleId);
 				SpecialAccess sa = (SpecialAccess) q.uniqueResult();
 				// If student has special access, use those dates to determine if section is visible to them
 				if (sa != null)
 				{
 					if (isSectionVisible(sa, (ModuleShdates) section.getModule().getModuleshdate()))
 					{
 						sectionId = section.getSectionId().intValue();
 					}
 					else
 					{
 						sectionId = 0;
 					}
 				}
 				else
 				{
 					if (section.getModule().getModuleshdate().isVisibleFlag())
 					{
 						sectionId = section.getSectionId().intValue();
 					}
 					else
 					{
 						sectionId = 0;
 					}
 				}
 			}
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
 		return sectionId;
 	}
 
 	/**
 	 * Insert a bookmark
 	 * 
 	 * @param mb
 	 *        The bookmark object
 	 * @throws Exception
 	 */
 	public void setBookmark(Bookmark mb) throws Exception
 	{
 		Transaction tx = null;
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			tx = session.beginTransaction();
 
 			Query q = session
 					.createQuery("select mb1 from Bookmark as mb1 where mb1.userId =:userId and mb1.siteId = :siteId and mb1.section.sectionId = :sectionId");
 			q.setParameter("userId", mb.getUserId());
 			q.setParameter("siteId", mb.getSiteId());
 			q.setParameter("sectionId", mb.getSectionId());
 			Bookmark find_mb = (Bookmark) q.uniqueResult();
 
 			if (find_mb == null)
 				session.save(mb);
 			else
 			{
 				find_mb.setTitle(mb.getTitle());
 				find_mb.setNotes(mb.getNotes());
 				find_mb.setLastVisited(mb.getLastVisited());
 				// TODO: Code in loop to set all other last visiteds to false
 				session.update(find_mb);
 			}
 
 			tx.commit();
 
 		}
 		catch (StaleObjectStateException sose)
 		{
 			if (tx != null) tx.rollback();
 			logger.error("stale object exception" + sose.toString());
 			throw sose;
 		}
 		catch (HibernateException he)
 		{
 			logger.error(he.toString());
 			he.printStackTrace();
 			throw he;
 		}
 		catch (Exception e)
 		{
 			if (tx != null) tx.rollback();
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
 		// This code sets all other last visited flags to false
 		// for this user's site
 		if (mb.getLastVisited().booleanValue() == true)
 		{
 			List<BookmarkObjService> mbList = getBookmarks(mb.getUserId(), mb.getSiteId());
 			if (mbList != null)
 			{
 				if (mbList.size() > 0)
 				{
 					adjustLastVisited(mbList, mb.getSectionId());
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param hibernateUtil
 	 *        The hibernateUtil to set.
 	 */
 	public void setHibernateUtil(HibernateUtil hibernateUtil)
 	{
 		this.hibernateUtil = hibernateUtil;
 	}
 
 	/**
 	 * Adjusts the last visited status of all other bookmarks when a certain bookmark is marked as last visited
 	 * 
 	 * @param mbList
 	 *        List of bookmarks
 	 * @param sectionId
 	 *        Section Id
 	 * @throws Exception
 	 */
 	private void adjustLastVisited(List<? extends BookmarkObjService> mbList, int sectionId) throws Exception
 	{
 		Transaction tx = null;
 		try
 		{
 			Session session = hibernateUtil.currentSession();
 			tx = session.beginTransaction();
 			for (ListIterator<?> i = mbList.listIterator(); i.hasNext();)
 			{
 				Bookmark bm = (Bookmark) i.next();
 				if ((bm.getSectionId() != sectionId) && (bm.getLastVisited().booleanValue() == true))
 				{
 					bm.setLastVisited(new Boolean(false));
 					session.saveOrUpdate(bm);
 				}
 			}
 			tx.commit();
 
 		}
 		catch (StaleObjectStateException sose)
 		{
 			if (tx != null) tx.rollback();
 			logger.error("stale object exception" + sose.toString());
 			throw sose;
 		}
 		catch (HibernateException he)
 		{
 			logger.error(he.toString());
 			he.printStackTrace();
 			throw he;
 		}
 		catch (Exception e)
 		{
 			if (tx != null) tx.rollback();
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
 
 	/**
 	 * Determines a section corresponding to a bookmark is visible, checks with special access and module start and end dates
 	 * 
 	 * @param sa
 	 *        Special access object
 	 * @param mshdates
 	 *        ModuleShdates object
 	 * @return true if section is visible
 	 */
 	private boolean isSectionVisible(SpecialAccess sa, ModuleShdates mshdates)
 	{
 		java.sql.Timestamp currentTimestamp = new java.sql.Timestamp(Calendar.getInstance().getTimeInMillis());
 		Date startDate;
 		Date endDate;
 
 		if (sa.isOverrideStart())
 			startDate = sa.getStartDate();
 		else
 			startDate = mshdates.getStartDate();
 
 		if (sa.isOverrideEnd())
 			endDate = sa.getEndDate();
 		else
 			endDate = mshdates.getEndDate();
 
 		if (((startDate == null) || (startDate.before(currentTimestamp))) && ((endDate == null) || (endDate.after(currentTimestamp))))
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 
 }
