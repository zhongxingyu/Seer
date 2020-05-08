 /*
  *	http://www.jrecruiter.org
  *
  *	Disclaimer of Warranty.
  *
  *	Unless required by applicable law or agreed to in writing, Licensor provides
  *	the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS,
  *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied,
  *	including, without limitation, any warranties or conditions of TITLE,
  *	NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE. You are
  *	solely responsible for determining the appropriateness of using or
  *	redistributing the Work and assume any risks associated with Your exercise of
  *	permissions under this License.
  *
  */
 package org.jrecruiter.persistent.dao;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.jrecruiter.Constants.StatsMode;
 import org.jrecruiter.model.Job;
 import org.jrecruiter.model.User;
 import org.jrecruiter.model.UserRole;
 import org.springframework.orm.hibernate3.HibernateCallback;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 
 /**
  * This DAO provides job-related database methods.
  *
  * @author Jerzy Puchala, Gunnar Hillert
  * @version @version $Id$
  */
 public class JobsDAOHibernate extends HibernateDaoSupport implements JobsDAO {
 
     private static JobsDAOHibernate daoHibernate = null;
 
     /**
      * User Dao.
      */
     private UserDAO userDao;
 
     /**
      * @param userDao The userDao to set.
      */
     public void setUserDao(UserDAO userDao) {
         this.userDao = userDao;
     }
 
     /**
      * Constructor.
      *
      */
     private JobsDAOHibernate() {
         super();
     }
 
     /**
      * Returns a new instance of the JobsDAO.
      * @return JobsDAO
      */
     public static JobsDAO getInstance() {
         if (daoHibernate == null) {
             daoHibernate = new JobsDAOHibernate();
         }
         return daoHibernate;
     }
 
     /**
      * Method for returning list of all jobs.
      *
      * @return List of Jobs
      *
      * @throws DAOException
      *
      */
     public List<Job> getAllJobs() throws DAOException {
 
         List<Job> jobs = (List<Job>) getHibernateTemplate().find(
                 "select job from Job job " +
                 "left outer join fetch job.statistics order by job.updateDate DESC");
 
         return jobs;
     }
 
     /**
      * Update a job posting in the persistence store.
      */
     public void update(Job job) {
 
         getHibernateTemplate().saveOrUpdate(job);
 
     }
 
     /**
      * Get a gob from the persistence store.
      *
      * @param jobId Id of the job posting.
      * @return A single job posting
      *
      * @see org.jrecruiter.persistent.dao.JobReqDAO#get(java.lang.Integer)
      */
     public Job get(Long jobId) throws DAOException {
 
         Job job = (Job) getHibernateTemplate().get(Job.class, jobId);
         return job;
     }
 
     /**
      * @see org.jrecruiter.persistent.dao.JobReqDAO#delete(java.lang.Integer)
      */
     public void delete(final Long jobId) throws DAOException {
 
         Job job = (Job) getHibernateTemplate().load(Job.class, jobId);
         getHibernateTemplate().delete(job);
 
     }
 
     /* (non-Javadoc)
      * @see org.jrecruiter.persistent.dao.
      * JobReqDAO#getAllUserJobs(java.lang.String)
      */
     public List<Job> getAllUserJobs(String username) {
 
         List<Job> jobs;
 
         User user = userDao.getUser(username);
 
         boolean administrator = false;
 
         Iterator it = user.getRoles().iterator();
 
         while (it.hasNext()) {
 
             UserRole userRole = (UserRole) it.next();
 
             if ("admin".equals(userRole.getName())) {
                 administrator = true;
             }
 
         }
 
         if (administrator) {
             jobs = this.getAllJobs();
         } else {
 
             jobs = getHibernateTemplate().find(
                     "from Job j where j.owner.username=?", username);
 
         }
 
         return jobs;
     }
 
     /* (non-Javadoc)
      * @see org.jrecruiter.persistent.dao.
      * JobReqDAO#getAllUserJobs(java.lang.String)
      */
     public List<Job> getAllUserJobsForStatistics(String username) {
 
         List < Job > jobs;
 
         User user = userDao.getUser(username);
 
         boolean administrator = false;
 
         Iterator it = user.getRoles().iterator();
 
         while (it.hasNext()) {
 
             UserRole userRole = (UserRole) it.next();
 
             if ("admin".equals(userRole.getName())) {
                 administrator = true;
             }
 
         }
 
         if (administrator) {
             jobs = this.getAllJobs();
         } else {
 
             jobs = getHibernateTemplate().find(
                     "from Job j left outer join fetch j.statistics where j.owner.username=?", username);
 
         }
 
         return jobs;
     }
 
     /* (non-Javadoc)
      * @see org.jrecruiter.persistent.dao.JobsDAO#getUsersJobsForStatistics(java.lang.String, java.lang.Integer, org.jrecruiter.Constants.StatsMode)
      */
     public List<Job> getUsersJobsForStatistics(String username, Integer maxResult, StatsMode statsMode) {
 
         List < Job > jobs;
 
         User user = userDao.getUser(username);
 
         boolean administrator = false;
 
         Iterator it = user.getRoles().iterator();
 
         while (it.hasNext()) {
 
             UserRole userRole = (UserRole) it.next();
 
             if ("admin".equals(userRole.getName())) {
                 administrator = true;
             }
 
         }
 
         final Session session = getSession(false);
 
         try {
 
             Query query = null;
 
             if (statsMode == StatsMode.PAGE_HITS) {
 
                 if (administrator) {
 
                     query = session.createQuery("select j from Job j left outer join fetch j.statistics as stats "
                            + "where stats is not null order by stats.counter desc");
 
                 } else {
 
                     query = session
                             .createQuery("select j from Job j left outer join fetch j.statistics as stats "
                                    + "where j.owner.username=:username and stats is not null "
                                    + "order by stats.counter desc");
                     query.setString("username", username);
                 }
             } else {
 
                 if (administrator) {
                     query = session
                     .createQuery("select j from Job j left outer join fetch j.statistics as stats "
                               + "where stats is not null order by stats.uniqueVisits desc");
                 } else {
 
                     query = session
                             .createQuery("select j from Job j left outer join fetch j.statistics as stats "
                                    + "where j.owner.username=:username and stats is not null "
                                    + "order by stats.uniqueVisits desc");
                     query.setString("username", username);
                 }
             }
 
             query.setMaxResults(maxResult);
 
 
             jobs = query.list();
 
         } catch (HibernateException ex) {
             throw convertHibernateAccessException(ex);
         }
 
         return jobs;
     }
 
     /**
      * Perform a simple search within the persistence store.
      *
      * @param keyword
      *            The search keyword
      * @return List of job postings representing the search results.
      */
     public List searchByKeyword(final String keyword) throws DAOException {
 
         List list = (List) getHibernateTemplate().execute(
                 new HibernateCallback() {
 
                     public Object doInHibernate(final Session session)
                             throws HibernateException {
                         Query q = session.createQuery("from Job j where "
                                 + "lower(j.jobTitle) like :keyword or "
                                 + "lower(j.description) like :keyword or "
                                 + "lower(j.jobRestrictions) like :keyword or "
                                 + "lower(j.businessLocation) like :keyword");
                         q.setString("keyword", "%" + keyword + "%");
 
                         return q.list();
                     }
 
                 });
 
         return list;
     }
 
 }
