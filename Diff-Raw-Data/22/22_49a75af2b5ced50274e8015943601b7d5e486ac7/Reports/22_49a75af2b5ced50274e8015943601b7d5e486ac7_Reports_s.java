 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author <First> <Last> (tmachet)
  * @date <Day> <Month> 2010
  *
  * Changelog:
  * - 13/12/2010 - tmachet - Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.schedserver.reports.intf;
 
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.DataAccessActivator;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RigTypeDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.UserClassDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.UserDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.AcademicPermission;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RequestCapabilities;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Rig;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.RigType;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Session;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.User;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.UserAssociation;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.UserClass;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.reports.impl.UserComparator;
 import au.edu.uts.eng.remotelabs.schedserver.reports.impl.UserRecord;
 import au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.AccessReportType;
 import au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.PaginationType;
 import au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.QueryFilterType;
 import au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.QueryInfo;
 import au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.QueryInfoResponse;
 import au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.QueryInfoResponseType;
 import au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.QueryInfoType;
 import au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.QuerySessionAccess;
 import au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.QuerySessionAccessResponse;
 import au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.QuerySessionAccessResponseType;
 import au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.QuerySessionAccessType;
 import au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.QuerySessionReport;
 import au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.QuerySessionReportResponse;
 import au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.QuerySessionReportResponseType;
 import au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.QuerySessionReportType;
 import au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.RequestorType;
 import au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.SessionReportType;
 import au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.TypeForQuery;
 import au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.UserNSNameSequence;
 
 
 /**
  * @author tmachet
  *
  */
 public class Reports implements ReportsSkeletonInterface
 {
     public static final String QNAME_DELIM = ":";
 
     /** Logger. */
     private final Logger logger;
     
     
     public Reports()
     {
         this.logger = LoggerActivator.getLogger();
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public QueryInfoResponse queryInfo(final QueryInfo queryInfo)
     {
         /** Request parameters. **/
         final QueryInfoType qIReq = queryInfo.getQueryInfo();
         String debug = "Received " + this.getClass().getSimpleName() + "#queryInfo with params:";
             debug += "Requestor: " + qIReq.getRequestor() + ", QuerySelect: " + qIReq.getQuerySelect().toString(); 
             if(qIReq.getQueryFilter() != null)
             {
                 debug += ", QueryFilter: " + qIReq.getQueryFilter().toString();
             }
             debug += ", limit: " + qIReq.getLimit(); 
         this.logger.debug(debug);
         final RequestorType uid = qIReq.getRequestor();
         
         /** Response parameters. */
         final QueryInfoResponse resp = new QueryInfoResponse();
         final QueryInfoResponseType respType = new QueryInfoResponseType();
         respType.setTypeForQuery(TypeForQuery.RIG);
         resp.setQueryInfoResponse(respType);
         
         final org.hibernate.Session ses = DataAccessActivator.getNewSession();
 
         /* First Query Filter - this contains the main selection type to be selected */        
         final QueryFilterType query0 = qIReq.getQuerySelect();
 
         try
         {
             Criteria cri;
 
             /* ----------------------------------------------------------------
              * -- Load the requestor.                                             --
              * ---------------------------------------------------------------- */
             final User user = this.getUserFromUserID(uid, ses);
             if (user == null)
             {
                 this.logger.info("Unable to generate report because the user has not been found. Supplied " +
                         "credentials ID=" + uid.getUserID() + ", namespace=" + uid.getUserNamespace() + ", " +
                         "name=" + uid.getUserName() + '.');
                 return resp;
             }
             final String persona = user.getPersona();
  
             //Get table to be queried
             if(query0.getTypeForQuery() == TypeForQuery.RIG)
             {
                 /* ----------------------------------------------------------------
                  * -- Rig Information only available to ADMIN, to be mediated    --
                  * -- by interface. No criteria on Requestor                     --
                  * ---------------------------------------------------------------- */
                 //Rig Information only available to ADMIN, to be mediated by interface
                 cri = ses.createCriteria(Rig.class);
                 if(query0.getQueryLike() != null)
                 {
                    cri.add(Restrictions.like("name", query0.getQueryLike()));
                 }
                 if(qIReq.getLimit() > 0 ) cri.setMaxResults(qIReq.getLimit());
 
                 cri.addOrder(Order.asc("name"));
                 final List<Rig> list = cri.list();
                 for (final Rig o : list)
                 {
                     respType.addSelectionResult(o.getName());
                 }
             }
             else if(query0.getTypeForQuery() == TypeForQuery.RIG_TYPE)
             {
                 /* ----------------------------------------------------------------
                  * -- Rig Type Information only available to ADMIN, to be        -- 
                  * -- mediated by interface. No criteria on Requestor            --
                  * ---------------------------------------------------------------- */
                 cri = ses.createCriteria(RigType.class);
 
                 if(query0.getQueryLike() != null)
                 {
                    cri.add(Restrictions.like("name", query0.getQueryLike()));
                 }
                 if(qIReq.getLimit() > 0 ) cri.setMaxResults(qIReq.getLimit());
                 cri.addOrder(Order.asc("name"));
                 final List<RigType> list = cri.list();
                 for (final RigType o : list)
                 {
                     respType.addSelectionResult(o.getName());
                 }
             }
             else if(query0.getTypeForQuery() == TypeForQuery.USER_CLASS)
             {
                 cri = ses.createCriteria(UserClass.class);
 
                 if(query0.getQueryLike() != null)
                 {
                    cri.add(Restrictions.like("name", query0.getQueryLike()));
                 }
                 if(qIReq.getLimit() > 0 ) cri.setMaxResults(qIReq.getLimit());
                 cri.addOrder(Order.asc("name"));
                 final List<UserClass> list = cri.list();
                 for (final UserClass o : list)
                 {
                     /* ----------------------------------------------------------------
                      * Check that the requestor has permissions to request the report.
                      * If persona = USER, no reports (USERs will not get here)
                      * If persona = ADMIN, any report 
                      * If persona = ACADEMIC, only for classes they own if they can genrate reports
                      * ---------------------------------------------------------------- */
 
                     if (User.ACADEMIC.equals(persona))
                     {
                         /* An academic may generate reports for their own classes only. */
                         boolean hasPerm = false;
                             
                         final Iterator<AcademicPermission> apIt = user.getAcademicPermissions().iterator();
                         while (apIt.hasNext())
                         {
                             final AcademicPermission ap = apIt.next();
                             if (ap.getUserClass().getId().equals(o.getId()) && ap.isCanGenerateReports())
                             {
                                 hasPerm = true;
                                 break;
                              }    
                         }
                             
                         if (!hasPerm)
                         {
                             this.logger.info("Unable to generate report for user class " + o.getName() + 
                                     " because the user " + user.getNamespace() + ':' + user.getName() +
                                     " does not own or have academic permission to it.");
                             continue;
                         }
                             
                         this.logger.debug("Academic " + user.getNamespace() + ':' + user.getName() + " has permission to " +
                                    "generate report from user class" + o.getName() + '.');
                     }
                     
                     respType.addSelectionResult(o.getName());
                 }
             }
             else if(query0.getTypeForQuery() == TypeForQuery.USER)
             {
                 cri = ses.createCriteria(User.class);
 
                 /* ----------------------------------------------------------------
                  * TODO Check that the requestor has permissions to request the report.
                  * If persona = USER, no reports (USERs will not get here)
                  * If persona = ADMIN, any report 
                  * If persona = ACADEMIC, only for users in classes they own if they can genrate reports
                  * 
                  *  NOTE generate academics class list
                  *  Get users in each class
                  *  Compare to users matching search criteria
                  * ---------------------------------------------------------------- */
 
                 
                 if(query0.getQueryLike() != null)
                 {
                    cri.add(Restrictions.like("name", query0.getQueryLike()));
                 }
                 if(qIReq.getLimit() > 0 ) cri.setMaxResults(qIReq.getLimit());
                 cri.addOrder(Order.asc("name"));
                 final List<User> userList = cri.list();
                 for (final User o : userList)
                 {
                     respType.addSelectionResult(o.getNamespace() + ':' + o.getName());
                 }
             }
             else if(query0.getTypeForQuery() == TypeForQuery.REQUEST_CAPABILITIES)
             {
                 cri = ses.createCriteria(RequestCapabilities.class);
                 if(query0.getQueryLike() != null)
                 {
                    cri.add(Restrictions.like("capabilities", query0.getQueryLike()));
                 }
                 if(qIReq.getLimit() > 0 ) cri.setMaxResults(qIReq.getLimit());
                 cri.addOrder(Order.asc("capabilities"));
                 final List<RequestCapabilities> list = cri.list();
                 for (final RequestCapabilities o : list)
                 {
                     respType.addSelectionResult(o.getCapabilities());
                 }
             }
             else 
             {
                 this.logger.error("QueryInfo request failed because TypeForQuery does not have a valid value.  Value is " +
                         query0.getTypeForQuery().toString());
                 return resp;
             }
             
             final QueryFilterType filter[] = qIReq.getQueryFilter(); 
             if(filter != null)
             {
                 //DODGY Designed, not implemented
             }
         }
         finally
         {
             ses.close();
         }
         
         respType.setTypeForQuery(query0.getTypeForQuery());
         resp.setQueryInfoResponse(respType);
         
         return resp;
     }
 
     /* (non-Javadoc)
      * @see au.edu.uts.eng.remotelabs.schedserver.reports.intf.ReportsSkeletonInterface#querySessionAccess(au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.QuerySessionAccess)
      */
     @SuppressWarnings({ "unchecked" })
     @Override
     public QuerySessionAccessResponse querySessionAccess(final QuerySessionAccess querySessionAccess)
     {
         /** Request parameters. **/
         final QuerySessionAccessType qSAReq = querySessionAccess.getQuerySessionAccess();
         String debug = "Received " + this.getClass().getSimpleName() + "#querySessionAccess with params:";
             debug += "Requestor: " + qSAReq.getRequestor() + ", QuerySelect: " + qSAReq.getQuerySelect().toString(); 
             if(qSAReq.getQueryConstraints() != null)
             {
                 debug += ", QueryConstraints: " + qSAReq.getQueryConstraints().toString();  //DODGY only first displayed
             }
             debug += ", start time: " + qSAReq.getStartTime() + ", end time: " + qSAReq.getEndTime() + ", pagination: " + qSAReq.getPagination();
         this.logger.debug(debug);
         final RequestorType uid = qSAReq.getRequestor();
 
         /** Response parameters. */
         final QuerySessionAccessResponse resp = new QuerySessionAccessResponse();
         final QuerySessionAccessResponseType respType = new QuerySessionAccessResponseType();
         final PaginationType page = new PaginationType();
         page.setNumberOfPages(1);
         page.setPageLength(30);
         page.setPageNumber(1);
         respType.setPagination(page);
         resp.setQuerySessionAccessResponse(respType);
         
         final org.hibernate.Session ses = DataAccessActivator.getNewSession();
 
         /* Set up query from request parameters*/
         try
         {
             /* ----------------------------------------------------------------
              * -- Load the requestor.                                             --
              * ---------------------------------------------------------------- */
             final User user = this.getUserFromUserID(uid, ses);
             if (user == null)
             {
                 this.logger.info("Unable to generate report because the user has not been found. Supplied " +
                         "credentials ID=" + uid.getUserID() + ", namespace=" + uid.getUserNamespace() + ", " +
                         "name=" + uid.getUserName() + '.');
                 return resp;
             }
             final String persona = user.getPersona();
 
             Criteria cri = ses.createCriteria(Session.class);
             // Order by request time
             cri.addOrder(Order.asc("requestTime"));
 
             /* First Query Filter - this contains grouping for the query */        
             final QueryFilterType query0 = qSAReq.getQuerySelect();
 
             /* ----------------------------------------------------------------
              * -- Get the Query type and process accordingly                 --
              * -- 1. Rig                                                     --
              * -- 2. Rig Type                                                --
              * -- 3. User Class                                              --
              * -- 4. User                                                    --
              * -- 5. Capabilities (not yet implemented)                      --
              * ---------------------------------------------------------------- */
             if(query0.getTypeForQuery() == TypeForQuery.RIG)
             {
                 /* ----------------------------------------------------------------
                  * -- 1. Rig Information only available to ADMIN, to be mediated --
                  * --    by interface. No criteria on Requestor                  --
                  * ---------------------------------------------------------------- */
                 
                 // Select Rig name to match query
                 cri.add(Restrictions.eq("assignedRigName", query0.getQueryLike()));
                 // Select time period
                 if (qSAReq.getStartTime() != null)
                 {
                     cri.add(Restrictions.ge("requestTime", qSAReq.getStartTime().getTime()));
                 }
                 if (qSAReq.getEndTime() != null)
                 {
                     cri.add(Restrictions.le("requestTime", qSAReq.getEndTime().getTime()));
                 }
                 
                 //Query Filter to be used for multiple selections in later versions of reporting. 
                 //QueryFilterType queryFilters[] = qSAReq.getQueryConstraints();
                 
                 // Add pagination requirements
                 if (qSAReq.getPagination() != null)
                 {
                     final PaginationType pages = qSAReq.getPagination();
                     final int noPages = pages.getNumberOfPages();
                     final int pageNumber = pages.getPageNumber();
                     final int pageLength = pages.getPageLength();
 
                     if (noPages > 1)
                     {
                         cri.setMaxResults(pageLength);
                     }
                     if (pageNumber > 1)
                     {
                         cri.setFirstResult((pageNumber-1)*pageLength);
                     }
                 }
                                
                 final List<Session> list = cri.list();
                 for (final Session o : list)
                 {
                     final AccessReportType reportType = new AccessReportType();
 
                     //Get user from session object
                     final RequestorType user0 = new RequestorType();
                     final UserNSNameSequence nsSequence = new UserNSNameSequence();
                     nsSequence.setUserName(o.getUser().getName());
                     nsSequence.setUserNamespace(o.getUser().getNamespace());
                     user0.setRequestorTypeSequence_type0(nsSequence);
                     reportType.setUser(user0);
                                         
                     reportType.setRigName(query0.getQueryLike());
                     
                     Calendar cal = Calendar.getInstance();
                     cal.setTime(o.getRequestTime());
                     reportType.setQueueStartTime(cal);
 
                     if(o.getAssignmentTime() != null)
                     {
                         final int queueD = (int) ((o.getAssignmentTime().getTime() - o.getRequestTime().getTime())/1000);
                         reportType.setQueueDuration(queueD);
                         cal = Calendar.getInstance();
                         cal.setTime(o.getAssignmentTime());
                         reportType.setSessionStartTime(cal);
 
                         final int sessionD = (int) ((o.getRemovalTime().getTime() - o.getAssignmentTime().getTime())/1000);
                         reportType.setSessionDuration(sessionD);
                     }
                     else
                     {
                         final int queueD = (int) ((o.getRemovalTime().getTime() - o.getRequestTime().getTime())/1000);
                         reportType.setQueueDuration(queueD);
                         reportType.setSessionDuration(0);
                     }
                     
                     cal = Calendar.getInstance();
                     cal.setTime(o.getRemovalTime());
                     reportType.setSessionEndTime(cal);
                     
                     reportType.setReasonForTermination(o.getRemovalReason());
 
                     respType.addAccessReportData(reportType);
                 }
             }
             else if(query0.getTypeForQuery() == TypeForQuery.RIG_TYPE)
             {
                 /* ----------------------------------------------------------------
                  * -- 2. Rig Type Information only available to ADMIN, to be     -- 
                  * --    mediated by interface. No criteria on Requestor         --
                  * ---------------------------------------------------------------- */
                 final RigTypeDao rTypeDAO = new RigTypeDao(ses);
                 final RigType rType = rTypeDAO.findByName(query0.getQueryLike());
                 cri = ses.createCriteria(Session.class);
                 if (rType == null)
                 {
                     this.logger.warn("No valid rig type found - " + query0.getTypeForQuery().toString());
                     return resp;
                 }
                 // Select Sessions where rig value is of the correct Rig Type
                 cri.createCriteria("rig").add(Restrictions.eq("rigType",rType));
 
                 // Select time period
                 if (qSAReq.getStartTime() != null)
                 {
                     cri.add(Restrictions.ge("requestTime", qSAReq.getStartTime().getTime()));
                 }
                 if (qSAReq.getEndTime() != null)
                 {
                     cri.add(Restrictions.le("requestTime", qSAReq.getEndTime().getTime()));
                 }
                 
                 //Query Filter to be used for multiple selections in later versions of reporting. 
                 //QueryFilterType queryFilters[] = qSAReq.getQueryConstraints();
                 
                 // Add pagination requirements
                 if (qSAReq.getPagination() != null)
                 {
                     final PaginationType pages = qSAReq.getPagination();
                     final int noPages = pages.getNumberOfPages();
                     final int pageNumber = pages.getPageNumber();
                     final int pageLength = pages.getPageLength();
 
                     if (noPages > 1)
                     {
                         cri.setMaxResults(pageLength);
                     }
                     if (pageNumber > 1)
                     {
                         cri.setFirstResult((pageNumber-1)*pageLength);
                     }
                 }
 
                 final List<Session> list = cri.list();
                 for (final Session o : list)
                 {
                     final AccessReportType reportType = new AccessReportType();
 
                     //Get user from session object
                     final RequestorType user0 = new RequestorType();
                     final UserNSNameSequence nsSequence = new UserNSNameSequence();
                     nsSequence.setUserName(o.getUser().getName());
                     nsSequence.setUserNamespace(o.getUser().getNamespace());
                     user0.setRequestorTypeSequence_type0(nsSequence);
                     reportType.setUser(user0);
 
                     reportType.setRigType(query0.getQueryLike());
                     
                     Calendar cal = Calendar.getInstance();
                     cal.setTime(o.getRequestTime());
                     reportType.setQueueStartTime(cal);
                     
                     if(o.getAssignmentTime() != null)
                     {
                         final int queueD = (int) ((o.getAssignmentTime().getTime() - o.getRequestTime().getTime())/1000);
                         reportType.setQueueDuration(queueD);
                         cal = Calendar.getInstance();
                         cal.setTime(o.getAssignmentTime());
                         reportType.setSessionStartTime(cal);
                         final int sessionD = (int) ((o.getRemovalTime().getTime() - o.getAssignmentTime().getTime())/1000);
                         reportType.setSessionDuration(sessionD);
                     }
                     else
                     {
                         final int queueD = (int) ((o.getRemovalTime().getTime() - o.getRequestTime().getTime())/1000);
                         reportType.setQueueDuration(queueD);
                         reportType.setSessionDuration(0);
                     }
                     
                     cal = Calendar.getInstance();
                     cal.setTime(o.getRemovalTime());
                     reportType.setSessionEndTime(cal);
                     
                     reportType.setReasonForTermination(o.getRemovalReason());
 
                     respType.addAccessReportData(reportType);
                 }
                 
             }
             else if(query0.getTypeForQuery() == TypeForQuery.USER_CLASS)
             {
                 /* ----------------------------------------------------------------
                  * -- 3. User Class Information only available to ADMIN and      -- 
                  * --    ACADEMIC users with permissions to generate reports     --
                  * --    for this class.                                         --
                  * ---------------------------------------------------------------- */
                 final UserClassDao uClassDAO = new UserClassDao(ses);
                 final UserClass uClass = uClassDAO.findByName(query0.getQueryLike());
                 if (uClass == null)
                 {
                     this.logger.warn("No valid user class found - " + query0.getTypeForQuery().toString());
                     return resp;
                 }
                 
                 /* ----------------------------------------------------------------
                  * Check that the requestor has permissions to request the report.
                  * If persona = USER, no reports (USERs will not get here)
                  * If persona = ADMIN, any report 
                  * If persona = ACADEMIC, only for classes they own if they can genrate reports
                  * ---------------------------------------------------------------- */
                 if (User.ACADEMIC.equals(persona))
                 {
                     /* An academic may generate reports for their own classes only. */
                     boolean hasPerm = false;
                         
                     final Iterator<AcademicPermission> apIt = user.getAcademicPermissions().iterator();
                     while (apIt.hasNext())
                     {
                         final AcademicPermission ap = apIt.next();
                         if (ap.getUserClass().getId().equals(uClass.getId()) && ap.isCanGenerateReports())
                         {
                             hasPerm = true;
                             break;
                          }    
                     }
                         
                     if (!hasPerm)
                     {
                         this.logger.info("Unable to generate report for user class " + uClass.getName() + 
                                 " because the user " + user.getNamespace() + ':' + user.getName() +
                                 " does not own or have academic permission to it.");
                         return resp;
                     }
                         
                     this.logger.debug("Academic " + user.getNamespace() + ':' + user.getName() + " has permission to " +
                                "generate report from user class" + uClass.getName() + '.');
                 }
 
                 //Select sessions where the resource permission associated is for this user class    
                 cri.createCriteria("resourcePermission").add(Restrictions.eq("userClass",uClass));
 
                 // Select time period
                 if (qSAReq.getStartTime() != null)
                 {
                     cri.add(Restrictions.ge("requestTime", qSAReq.getStartTime().getTime()));
                 }
                 if (qSAReq.getEndTime() != null)
                 {
                     cri.add(Restrictions.le("requestTime", qSAReq.getEndTime().getTime()));
                 }
                 
                 //Query Filter to be used for multiple selections in later versions of reporting. 
                 //QueryFilterType queryFilters[] = qSAReq.getQueryConstraints();
                 
                 // Add pagination requirements
                 if (qSAReq.getPagination() != null)
                 {
                     final PaginationType pages = qSAReq.getPagination();
                     final int noPages = pages.getNumberOfPages();
                     final int pageNumber = pages.getPageNumber();
                     final int pageLength = pages.getPageLength();
 
                     if (noPages > 1)
                     {
                         cri.setMaxResults(pageLength);
                     }
                     if (pageNumber > 1)
                     {
                         cri.setFirstResult((pageNumber-1)*pageLength);
                     }
                 }
 
                 final List<Session> list = cri.list();
                 for (final Session o : list)
                 {
                     final AccessReportType reportType = new AccessReportType();
 
                     //Get user from session object
                     final RequestorType user0 = new RequestorType();
                     final UserNSNameSequence nsSequence = new UserNSNameSequence();
                     nsSequence.setUserName(o.getUser().getName());
                     nsSequence.setUserNamespace(o.getUser().getNamespace());
                     user0.setRequestorTypeSequence_type0(nsSequence);
                     reportType.setUser(user0);
 
                     reportType.setUserClass(query0.getQueryLike());
                     reportType.setRigName(o.getAssignedRigName());
                     
                     Calendar cal = Calendar.getInstance();
                     cal.setTime(o.getRequestTime());
                     reportType.setQueueStartTime(cal);
                     
                     if(o.getAssignmentTime() != null)
                     {
                         final int queueD = (int) ((o.getAssignmentTime().getTime() - o.getRequestTime().getTime())/1000);
                         reportType.setQueueDuration(queueD);
                         cal = Calendar.getInstance();
                         cal.setTime(o.getAssignmentTime());
                         reportType.setSessionStartTime(cal);
                         final int sessionD = (int) ((o.getRemovalTime().getTime() - o.getAssignmentTime().getTime())/1000);
                         reportType.setSessionDuration(sessionD);
                     }
                     else
                     {
                         final int queueD = (int) ((o.getRemovalTime().getTime() - o.getRequestTime().getTime())/1000);
                         reportType.setQueueDuration(queueD);
                         reportType.setSessionDuration(0);
                     }
                     
                     cal = Calendar.getInstance();
                     cal.setTime(o.getRemovalTime());
                     reportType.setSessionEndTime(cal);
                     
                     reportType.setReasonForTermination(o.getRemovalReason());
 
                     respType.addAccessReportData(reportType);
                 }
                 
             }
             else if(query0.getTypeForQuery() == TypeForQuery.USER)
             {
                 /* ----------------------------------------------------------------
                  * -- 4. User Information only available to ADMIN and            -- 
                  * --    ACADEMIC users with permissions to generate reports     --
                  * --    for this classes to which this user belongs.            --
                  * -- NOTE: User nam expected as: namespace:username             --
                  * ---------------------------------------------------------------- */
                 
                 final UserDao userDAO = new UserDao(ses);
                 final String idParts[] = query0.getQueryLike().split(Reports.QNAME_DELIM, 2);
                 final String ns = idParts[0];
                final String name = idParts[1];
                 final User u = userDAO.findByName(ns, name);
                 
                 if (u == null)
                 {
                     this.logger.warn("No valid user found - " + query0.getTypeForQuery().toString());
                     return resp;
                 }
                 
                 /* ----------------------------------------------------------------
                  * Check that the requestor has permissions to request the report.
                  * If persona = USER, no reports (USERs will not get here)
                  * If persona = ADMIN, any report 
                  * If persona = ACADEMIC, only for users belonging to 
                  *    classes they can generate reports for
                  * ---------------------------------------------------------------- */
                 if (User.ACADEMIC.equals(persona))
                 {
                     /* An academic may generate reports for their own classes only. */
                     boolean hasPerm = false;
                         
                     final Iterator<AcademicPermission> apIt = user.getAcademicPermissions().iterator();
                     while (apIt.hasNext())
                     {
                         final AcademicPermission ap = apIt.next();
                         final Iterator<UserAssociation> uaIt = u.getUserAssociations().iterator();
                         while (uaIt.hasNext())
                         {
                             final UserAssociation ua = uaIt.next();
                             if (ap.getUserClass().getId().equals(ua.getUserClass().getId()) && ap.isCanGenerateReports())
                             {
                                 hasPerm = true;
                                 break;
                             }
                         }
                         if(hasPerm)
                         {
                             break;
                         }
                     }
                         
                     if (!hasPerm)
                     {
                         this.logger.info("Unable to generate report for user class " + u.getName() + 
                                 " because the user " + user.getNamespace() + ':' + user.getName() +
                                 " does not own or have academic permission to it.");
                         return resp;
                     }
                         
                     this.logger.debug("Academic " + user.getNamespace() + ':' + user.getName() + " has permission to " +
                                "generate report from user class" + u.getName() + '.');
                 }
                 
                 //Select sessions where the resource permission associated is for this user class    
                 cri.add(Restrictions.eq("user",u));
 
                 // Select time period
                 if (qSAReq.getStartTime() != null)
                 {
                     cri.add(Restrictions.ge("requestTime", qSAReq.getStartTime().getTime()));
                 }
                 if (qSAReq.getEndTime() != null)
                 {
                     cri.add(Restrictions.le("requestTime", qSAReq.getEndTime().getTime()));
                 }
                 
                 //Query Filter to be used for multiple selections in later versions of reporting. 
                 //QueryFilterType queryFilters[] = qSAReq.getQueryConstraints();
                 
                 // Add pagination requirements
                 if (qSAReq.getPagination() != null)
                 {
                     final PaginationType pages = qSAReq.getPagination();
                     final int noPages = pages.getNumberOfPages();
                     final int pageNumber = pages.getPageNumber();
                     final int pageLength = pages.getPageLength();
 
                     if (noPages > 1)
                     {
                         cri.setMaxResults(pageLength);
                     }
                     if (pageNumber > 1)
                     {
                         cri.setFirstResult((pageNumber-1)*pageLength);
                     }
                 }
 
                 final List<Session> list = cri.list();
                 for (final Session o : list)
                 {
                     final AccessReportType reportType = new AccessReportType();
 
                     //Get user from session object
                     final RequestorType user0 = new RequestorType();
                     final UserNSNameSequence nsSequence = new UserNSNameSequence();
                     nsSequence.setUserName(o.getUser().getName());
                     nsSequence.setUserNamespace(o.getUser().getNamespace());
                     user0.setRequestorTypeSequence_type0(nsSequence);
                     reportType.setUser(user0);
 
                     reportType.setRigName(o.getAssignedRigName());
                     
                     Calendar cal = Calendar.getInstance();
                     cal.setTime(o.getRequestTime());
                     reportType.setQueueStartTime(cal);
                     
                     if(o.getAssignmentTime() != null)
                     {
                         final int queueD = (int) ((o.getAssignmentTime().getTime() - o.getRequestTime().getTime())/1000);
                         reportType.setQueueDuration(queueD);
                         cal = Calendar.getInstance();
                         cal.setTime(o.getAssignmentTime());
                         reportType.setSessionStartTime(cal);
                         final int sessionD = (int) ((o.getRemovalTime().getTime() - o.getAssignmentTime().getTime())/1000);
                         reportType.setSessionDuration(sessionD);
                     }
                     else
                     {
                         final int queueD = (int) ((o.getRemovalTime().getTime() - o.getRequestTime().getTime())/1000);
                         reportType.setQueueDuration(queueD);
                         reportType.setSessionDuration(0);
                     }
                     
                     cal = Calendar.getInstance();
                     cal.setTime(o.getRemovalTime());
                     reportType.setSessionEndTime(cal);
 
                     reportType.setReasonForTermination(o.getRemovalReason());
                     
                     respType.addAccessReportData(reportType);
                 }
 
             }
             else if(query0.getTypeForQuery() == TypeForQuery.REQUEST_CAPABILITIES)
             {
                 this.logger.error("Request capabilities not implemented yet - " +
                         query0.getTypeForQuery().toString());
                 return resp;
             }
             else 
             {
                 this.logger.error("queryAccessReport request failed because TypeForQuery does not have a valid value.  Value is " +
                         query0.getTypeForQuery().toString());
                 return resp;
             }
             
 
             
         }
         finally
         {
             ses.close();
         }
         
         return resp;    
     }
     
 
     /* (non-Javadoc)
      * @see au.edu.uts.eng.remotelabs.schedserver.reports.intf.ReportsSkeletonInterface#querySessionReport(au.edu.uts.eng.remotelabs.schedserver.reports.intf.types.QuerySessionReport)
      */
     @SuppressWarnings("unchecked")
     @Override
     public QuerySessionReportResponse querySessionReport(final QuerySessionReport querySessionReport)
     {
         /** Request parameters. **/
         final QuerySessionReportType qSRReq = querySessionReport.getQuerySessionReport();
         String debug = "Received " + this.getClass().getSimpleName() + "#querySessionReport with params:";
             debug += "Requestor: " + qSRReq.getRequestor() + ", QuerySelect: " + qSRReq.getQuerySelect().toString(); 
             if(qSRReq.getQueryConstraints() != null)
             {
                 debug += ", QueryConstraints: " + qSRReq.getQueryConstraints().toString();  //DODGY only first displayed
             }
             debug += ", start time: " + qSRReq.getStartTime() + ", end time: " + qSRReq.getEndTime() + ", pagination: " + qSRReq.getPagination();
         this.logger.debug(debug);
         final RequestorType uid = qSRReq.getRequestor();
 
         /** Response parameters. */
         final QuerySessionReportResponse resp = new QuerySessionReportResponse();
         final QuerySessionReportResponseType respType = new QuerySessionReportResponseType();
         final PaginationType page = new PaginationType();
         page.setNumberOfPages(1);
         page.setPageLength(30);
         page.setPageNumber(1);
         respType.setPagination(page);
         respType.setSessionCount(0);
         respType.setTotalQueueDuration(0);
         respType.setTotalSessionDuration(0);
         resp.setQuerySessionReportResponse(respType);
         
         final org.hibernate.Session ses = DataAccessActivator.getNewSession();
 
         /* Set up query from request parameters*/
         try
         {
             /* ----------------------------------------------------------------
              * -- Load the requestor.                                             --
              * ---------------------------------------------------------------- */
             final User user = this.getUserFromUserID(uid, ses);
             if (user == null)
             {
                 this.logger.info("Unable to generate report because the user has not been found. Supplied " +
                         "credentials ID=" + uid.getUserID() + ", namespace=" + uid.getUserNamespace() + ", " +
                         "name=" + uid.getUserName() + '.');
                 return resp;
             }
             final String persona = user.getPersona();
 
             final Criteria cri = ses.createCriteria(Session.class);
             // Order by request time
             cri.addOrder(Order.asc("requestTime"));
 
             /* First Query Filter - this contains grouping for the query */        
             final QueryFilterType query0 = qSRReq.getQuerySelect();
 
             /* ----------------------------------------------------------------
              * -- Get the Query type and process accordingly                 --
              * -- 1. Rig                                                     --
              * -- 2. Rig Type                                                --
              * -- 3. User Class                                              --
              * -- 4. User                                                    --
              * -- 5. Capabilities (not yet implemented)                      --
              * ---------------------------------------------------------------- */
             if(query0.getTypeForQuery() == TypeForQuery.RIG)
             {
                 /* ----------------------------------------------------------------
                  * -- 1. Rig Information only available to ADMIN, to be mediated --
                  * --    by interface. No criteria on Requestor                  --
                  * ---------------------------------------------------------------- */
                 
                 /* ----------------------------------------------------------------
                  * -- 1a. Get Sessions that match the rig                        --
                  * ---------------------------------------------------------------- */
 
                 cri.add(Restrictions.eq("assignedRigName", query0.getQueryLike()));
                 // Select time period
                 if (qSRReq.getStartTime() != null)
                 {
                     cri.add(Restrictions.ge("requestTime", qSRReq.getStartTime().getTime()));
                 }
                 if (qSRReq.getEndTime() != null)
                 {
                     cri.add(Restrictions.le("requestTime", qSRReq.getEndTime().getTime()));
                 }
                 
                 //Query Filter to be used for multiple selections in later versions of reporting. 
                 //QueryFilterType queryFilters[] = qSAReq.getQueryConstraints();
 
                 final SortedMap<User,UserRecord> recordMap = new TreeMap<User,UserRecord>(new UserComparator());
                 
                 final List<Session> list = cri.list();
                 for (final Session o : list)
                 {
                     /* ----------------------------------------------------------------
                      * -- 1b. Iterate through sessions and create user records       --
                      * ---------------------------------------------------------------- */
                     
                     final User u = o.getUser();
                     
                     if(recordMap.containsKey(u))
                     {
                         recordMap.get(u).addRecord(o);
                     }
                     else
                     {
                         final UserRecord userRec = new UserRecord();
                         userRec.addRecord(o);
                         recordMap.put(u, userRec);
                     }
                 }
 
                 /* ----------------------------------------------------------------
                  * -- 1c. Get pagination requirements so correct records are     -- 
                  * --     returned.                                              --
                  * ---------------------------------------------------------------- */
 
                 int noPages = 1;
                 int pageNumber = 1;
                 int pageLength = recordMap.size();
                 int recordCount = 0;
                 int totalSessionDuration = 0;
                 int totalQueueDuration = 0;
 
                 if (qSRReq.getPagination() != null)
                 {
                     final PaginationType pages = qSRReq.getPagination();
                     noPages = pages.getNumberOfPages();
                     pageNumber = pages.getPageNumber();
                     pageLength = pages.getPageLength();
                     
                 }
 
                 /* ----------------------------------------------------------------
                  * -- 1d. Iterate through user records and calculate report data --
                  * ---------------------------------------------------------------- */
 
                 for (final Entry<User,UserRecord> e : recordMap.entrySet())
                 {
                     recordCount++;
                     totalQueueDuration += e.getValue().getTotalQueueDuration();
                     totalSessionDuration += e.getValue().getTotalSessionDuration();
                     if ((recordCount > (pageNumber-1)*pageLength) && (recordCount <= pageNumber*pageLength))
                     {
                         final SessionReportType reportType = new SessionReportType();
 
                         //Get user from session object
                         final RequestorType user0 = new RequestorType();
                         final UserNSNameSequence nsSequence = new UserNSNameSequence();
                         nsSequence.setUserName(e.getKey().getName());
                         nsSequence.setUserNamespace(e.getKey().getNamespace());
                         user0.setRequestorTypeSequence_type0(nsSequence);
                         reportType.setUser(user0);
 
                         reportType.setRigName(query0.getQueryLike());
                         
                         reportType.setAveQueueDuration(e.getValue().getAverageQueueDuration());
                         reportType.setMedQueueDuration(e.getValue().getMedianQueueDuration());
                         reportType.setMinQueueDuration(e.getValue().getMinimumQueueDuration());
                         reportType.setMaxQueueDuration(e.getValue().getMaximumQueueDuration());
                         reportType.setTotalQueueDuration(e.getValue().getTotalQueueDuration());
 
                         reportType.setAveSessionDuration(e.getValue().getAverageSessionDuration());
                         reportType.setMedSessionDuration(e.getValue().getMedianSessionDuration());
                         reportType.setMinSessionDuration(e.getValue().getMinimumSessionDuration());
                         reportType.setMaxSessionDuration(e.getValue().getMaximumSessionDuration());
                         reportType.setTotalSessionDuration(e.getValue().getTotalSessionDuration());
                         
                         respType.addSessionReport(reportType);
                     }
                 }
                 
                 respType.setSessionCount(recordCount);
                 respType.setTotalQueueDuration(totalQueueDuration);
                 respType.setTotalSessionDuration(totalSessionDuration);
                 
                 resp.setQuerySessionReportResponse(respType);
             }
             
             else if(query0.getTypeForQuery() == TypeForQuery.RIG_TYPE)
             {
                 /* ----------------------------------------------------------------
                  * -- 2. Rig Type Information only available to ADMIN, to be     -- 
                  * --    mediated by interface. No criteria on Requestor         --
                  * ---------------------------------------------------------------- */
                 
                 final RigTypeDao rTypeDAO = new RigTypeDao(ses);
                 final RigType rType = rTypeDAO.findByName(query0.getQueryLike());
                 
                 if (rType == null)
                 {
                     this.logger.warn("No valid rig type found - " + query0.getTypeForQuery().toString());
                     return resp;
                 }
                 
                 /* ----------------------------------------------------------------
                  * -- 2a. Get Sessions that match the rig type                   --
                  * ---------------------------------------------------------------- */
 
                 // Select Sessions where rig value is of the correct Rig Type
                 cri.createCriteria("rig").add(Restrictions.eq("rigType",rType));
 
                 // Select time period
                 if (qSRReq.getStartTime() != null)
                 {
                     cri.add(Restrictions.ge("requestTime", qSRReq.getStartTime().getTime()));
                 }
                 if (qSRReq.getEndTime() != null)
                 {
                     cri.add(Restrictions.le("requestTime", qSRReq.getEndTime().getTime()));
                 }
                 
                 //Query Filter to be used for multiple selections in later versions of reporting. 
                 //QueryFilterType queryFilters[] = qSAReq.getQueryConstraints();
                 
                 final SortedMap<User,UserRecord> recordMap = new TreeMap<User,UserRecord>(new UserComparator());
 
                 final List<Session> list = cri.list();
                 for (final Session o : list)
                 {
                     /* ----------------------------------------------------------------
                      * -- 2b. Iterate through sessions and create user records       --
                      * ---------------------------------------------------------------- */
                     
                     final User u = o.getUser();
                     
                     if(recordMap.containsKey(u))
                     {
                         recordMap.get(u).addRecord(o);
                     }
                     else
                     {
                         final UserRecord userRec = new UserRecord();
                         userRec.addRecord(o);
                         recordMap.put(u, userRec);
                     }
                 }
 
                 /* ----------------------------------------------------------------
                  * -- 2c. Get pagination requirements so correct records are     -- 
                  * --     returned.                                              --
                  * ---------------------------------------------------------------- */
 
                 int noPages = 1;
                 int pageNumber = 1;
                 int pageLength = recordMap.size();
                 int recordCount = 0;
                 int totalSessionDuration = 0;
                 int totalQueueDuration = 0;
 
                 if (qSRReq.getPagination() != null)
                 {
                     final PaginationType pages = qSRReq.getPagination();
                     noPages = pages.getNumberOfPages();
                     pageNumber = pages.getPageNumber();
                     pageLength = pages.getPageLength();
                     
                 }
 
                 /* ----------------------------------------------------------------
                  * -- 2d. Iterate through user records and calculate report data --
                  * ---------------------------------------------------------------- */
 
                 for (final Entry<User,UserRecord> e : recordMap.entrySet())
                 {
                     recordCount++;
                     totalQueueDuration += e.getValue().getTotalQueueDuration();
                     totalSessionDuration += e.getValue().getTotalSessionDuration();
                     if ((recordCount > (pageNumber-1)*pageLength) && (recordCount <= pageNumber*pageLength))
                     {
                         final SessionReportType reportType = new SessionReportType();
 
                         //Get user from session object
                         final RequestorType user0 = new RequestorType();
                         final UserNSNameSequence nsSequence = new UserNSNameSequence();
                         nsSequence.setUserName(e.getKey().getName());
                         nsSequence.setUserNamespace(e.getKey().getNamespace());
                         user0.setRequestorTypeSequence_type0(nsSequence);
                         reportType.setUser(user0);
 
                         reportType.setRigType(rType.getName());
                         
                         reportType.setAveQueueDuration(e.getValue().getAverageQueueDuration());
                         reportType.setMedQueueDuration(e.getValue().getMedianQueueDuration());
                         reportType.setMinQueueDuration(e.getValue().getMinimumQueueDuration());
                         reportType.setMaxQueueDuration(e.getValue().getMaximumQueueDuration());
                         reportType.setTotalQueueDuration(e.getValue().getTotalQueueDuration());
 
                         reportType.setAveSessionDuration(e.getValue().getAverageSessionDuration());
                         reportType.setMedSessionDuration(e.getValue().getMedianSessionDuration());
                         reportType.setMinSessionDuration(e.getValue().getMinimumSessionDuration());
                         reportType.setMaxSessionDuration(e.getValue().getMaximumSessionDuration());
                         reportType.setTotalSessionDuration(e.getValue().getTotalSessionDuration());
                         
                         respType.addSessionReport(reportType);
                     }
                 }
                 
                 respType.setSessionCount(recordCount);
                 respType.setTotalQueueDuration(totalQueueDuration);
                 respType.setTotalSessionDuration(totalSessionDuration);
                 
                 resp.setQuerySessionReportResponse(respType);
 
             }
 
             else if(query0.getTypeForQuery() == TypeForQuery.USER_CLASS)
             {
                 /* ----------------------------------------------------------------
                  * -- 3. User Class Information only available to ADMIN and      -- 
                  * --    ACADEMIC users with permissions to generate reports     --
                  * --    for this class.                                         --
                  * ---------------------------------------------------------------- */
 
                 final UserClassDao uClassDAO = new UserClassDao(ses);
                 final UserClass uClass = uClassDAO.findByName(query0.getQueryLike());
                 if (uClass == null)
                 {
                     this.logger.warn("No valid user class found - " + query0.getTypeForQuery().toString());
                     return resp;
                 }
                 
                 /* ----------------------------------------------------------------
                  * Check that the requestor has permissions to request the report.
                  * If persona = USER, no reports (USERs will not get here)
                  * If persona = ADMIN, any report 
                  * If persona = ACADEMIC, only for classes they own if they can genrate reports
                  * ---------------------------------------------------------------- */
                 if (User.ACADEMIC.equals(persona))
                 {
                     /* An academic may generate reports for their own classes only. */
                     boolean hasPerm = false;
                         
                     final Iterator<AcademicPermission> apIt = user.getAcademicPermissions().iterator();
                     while (apIt.hasNext())
                     {
                         final AcademicPermission ap = apIt.next();
                         if (ap.getUserClass().getId().equals(uClass.getId()) && ap.isCanGenerateReports())
                         {
                             hasPerm = true;
                             break;
                          }    
                     }
                         
                     if (!hasPerm)
                     {
                         this.logger.info("Unable to generate report for user class " + uClass.getName() + 
                                 " because the user " + user.getNamespace() + ':' + user.getName() +
                                 " does not own or have academic permission to it.");
                         return resp;
                     }
                         
                     this.logger.debug("Academic " + user.getNamespace() + ':' + user.getName() + " has permission to " +
                                "generate report from user class" + uClass.getName() + '.');
                 }
 
                 /* ----------------------------------------------------------------
                  * -- 3a. Get Sessions that match the user class                --
                  * ---------------------------------------------------------------- */
 
                 //Select sessions where the resource permission associated is for this user class    
                 cri.createCriteria("resourcePermission").add(Restrictions.eq("userClass",uClass));
                 
                 // Select time period
                 if (qSRReq.getStartTime() != null)
                 {
                     cri.add(Restrictions.ge("requestTime", qSRReq.getStartTime().getTime()));
                 }
                 if (qSRReq.getEndTime() != null)
                 {
                     cri.add(Restrictions.le("requestTime", qSRReq.getEndTime().getTime()));
                 }
                 
                 //Query Filter to be used for multiple selections in later versions of reporting. 
                 //QueryFilterType queryFilters[] = qSAReq.getQueryConstraints();
                 
                 final SortedMap<User,UserRecord> recordMap = new TreeMap<User,UserRecord>(new UserComparator());
 
                 final List<Session> list = cri.list();
                 for (final Session o : list)
                 {
                     /* ----------------------------------------------------------------
                      * -- 3b. Iterate through sessions and create user records       --
                      * ---------------------------------------------------------------- */
                     
                     final User u = o.getUser();
                     
                     if(recordMap.containsKey(u))
                     {
                         recordMap.get(u).addRecord(o);
                     }
                     else
                     {
                         final UserRecord userRec = new UserRecord();
                         userRec.addRecord(o);
                         recordMap.put(u, userRec);
                     }
                 }
 
                 /* ----------------------------------------------------------------
                  * -- 3c. Get pagination requirements so correct records are     -- 
                  * --     returned.                                              --
                  * ---------------------------------------------------------------- */
 
                 int noPages = 1;
                 int pageNumber = 1;
                 int pageLength = recordMap.size();
                 int recordCount = 0;
                 int totalSessionDuration = 0;
                 int totalQueueDuration = 0;
 
                 if (qSRReq.getPagination() != null)
                 {
                     final PaginationType pages = qSRReq.getPagination();
                     noPages = pages.getNumberOfPages();
                     pageNumber = pages.getPageNumber();
                     pageLength = pages.getPageLength();
                     
                 }
 
                 /* ----------------------------------------------------------------
                  * -- 3d. Iterate through user records and calculate report data --
                  * ---------------------------------------------------------------- */
 
                 for (final Entry<User,UserRecord> e : recordMap.entrySet())
                 {
                     recordCount++;
                     totalQueueDuration += e.getValue().getTotalQueueDuration();
                     totalSessionDuration += e.getValue().getTotalSessionDuration();
                     if ((recordCount > (pageNumber-1)*pageLength) && (recordCount <= pageNumber*pageLength))
                     {
                         final SessionReportType reportType = new SessionReportType();
 
                         //Get user from session object
                         final RequestorType user0 = new RequestorType();
                         final UserNSNameSequence nsSequence = new UserNSNameSequence();
                         nsSequence.setUserName(e.getKey().getName());
                         nsSequence.setUserNamespace(e.getKey().getNamespace());
                         user0.setRequestorTypeSequence_type0(nsSequence);
                         reportType.setUser(user0);
 
                         reportType.setUserClass(uClass.getName());
                         
                         reportType.setAveQueueDuration(e.getValue().getAverageQueueDuration());
                         reportType.setMedQueueDuration(e.getValue().getMedianQueueDuration());
                         reportType.setMinQueueDuration(e.getValue().getMinimumQueueDuration());
                         reportType.setMaxQueueDuration(e.getValue().getMaximumQueueDuration());
                         reportType.setTotalQueueDuration(e.getValue().getTotalQueueDuration());
 
                         reportType.setAveSessionDuration(e.getValue().getAverageSessionDuration());
                         reportType.setMedSessionDuration(e.getValue().getMedianSessionDuration());
                         reportType.setMinSessionDuration(e.getValue().getMinimumSessionDuration());
                         reportType.setMaxSessionDuration(e.getValue().getMaximumSessionDuration());
                         reportType.setTotalSessionDuration(e.getValue().getTotalSessionDuration());
                         
                         respType.addSessionReport(reportType);
                     }
                 }
                 
                 respType.setSessionCount(recordCount);
                 respType.setTotalQueueDuration(totalQueueDuration);
                 respType.setTotalSessionDuration(totalSessionDuration);
                 
                 resp.setQuerySessionReportResponse(respType);
 
             }
 
             else if(query0.getTypeForQuery() == TypeForQuery.USER)
             {
                 /* ----------------------------------------------------------------
                  * -- 4. User Information only available to ADMIN and            -- 
                  * --    ACADEMIC users with permissions to generate reports     --
                  * --    for this classes to which this user belongs.            --
                  * -- NOTE: User nam expected as: namespace:username             --
                  * ---------------------------------------------------------------- */
                 
                 final UserDao userDAO = new UserDao(ses);
                 final String idParts[] = query0.getQueryLike().split(Reports.QNAME_DELIM, 2);
                 final String ns = idParts[0];
                final String name = idParts[1];
                 final User user0 = userDAO.findByName(ns, name);
                 
                 if (user0 == null)
                 {
                     this.logger.warn("No valid user found - " + query0.getTypeForQuery().toString());
                     return resp;
                 }
                 
                 /* ----------------------------------------------------------------
                  * Check that the requestor has permissions to request the report.
                  * If persona = USER, no reports (USERs will not get here)
                  * If persona = ADMIN, any report 
                  * If persona = ACADEMIC, only for users belonging to 
                  *    classes they can generate reports for
                  * ---------------------------------------------------------------- */
                 if (User.ACADEMIC.equals(persona))
                 {
                     /* An academic may generate reports for their own classes only. */
                     boolean hasPerm = false;
                         
                     final Iterator<AcademicPermission> apIt = user.getAcademicPermissions().iterator();
                     while (apIt.hasNext())
                     {
                         final AcademicPermission ap = apIt.next();
                         final Iterator<UserAssociation> uaIt = user0.getUserAssociations().iterator();
                         while (uaIt.hasNext())
                         {
                             final UserAssociation ua = uaIt.next();
                             if (ap.getUserClass().getId().equals(ua.getUserClass().getId()) && ap.isCanGenerateReports())
                             {
                                 hasPerm = true;
                                 break;
                             }
                         }
                         if(hasPerm)
                         {
                             break;
                         }
                     }
                         
                     if (!hasPerm)
                     {
                         this.logger.info("Unable to generate report for user class " + user0.getName() + 
                                 " because the user " + user.getNamespace() + ':' + user.getName() +
                                 " does not own or have academic permission to it.");
                         return resp;
                     }
                         
                     this.logger.debug("Academic " + user.getNamespace() + ':' + user.getName() + " has permission to " +
                                "generate report from user class" + user0.getName() + '.');
                 }
                 
 
                 /* ----------------------------------------------------------------
                  * -- 4a. Get Sessions that match the user                       --
                  * ---------------------------------------------------------------- */
 
                 //Select sessions where the resource permission associated is for this user class    
                 cri.add(Restrictions.eq("user",user0));
                 
                 // Select time period
                 if (qSRReq.getStartTime() != null)
                 {
                     cri.add(Restrictions.ge("requestTime", qSRReq.getStartTime().getTime()));
                 }
                 if (qSRReq.getEndTime() != null)
                 {
                     cri.add(Restrictions.le("requestTime", qSRReq.getEndTime().getTime()));
                 }
                 
                 //Query Filter to be used for multiple selections in later versions of reporting. 
                 //QueryFilterType queryFilters[] = qSAReq.getQueryConstraints();
                 
                 // ?? Do we need this - single user only
                 final SortedMap<User,UserRecord> recordMap = new TreeMap<User,UserRecord>(new UserComparator());
 
                 final List<Session> list = cri.list();
                 for (final Session o : list)
                 {
                     /* ----------------------------------------------------------------
                      * -- 4b. Iterate through sessions and create user records       --
                      * ---------------------------------------------------------------- */
                     
                     final User u = o.getUser();
                     
                     if(recordMap.containsKey(u))
                     {
                         recordMap.get(u).addRecord(o);
                     }
                     else
                     {
                         final UserRecord userRec = new UserRecord();
                         userRec.addRecord(o);
                         recordMap.put(u, userRec);
                     }
                 }
 
                 /* ----------------------------------------------------------------
                  * -- 4c. Get pagination requirements so correct records are     -- 
                  * --     returned.                                              --
                  * ---------------------------------------------------------------- */
 
                 int noPages = 1;
                 int pageNumber = 1;
                 int pageLength = recordMap.size();
                 int recordCount = 0;
                 int totalSessionDuration = 0;
                 int totalQueueDuration = 0;
 
                 if (qSRReq.getPagination() != null)
                 {
                     final PaginationType pages = qSRReq.getPagination();
                     noPages = pages.getNumberOfPages();
                     pageNumber = pages.getPageNumber();
                     pageLength = pages.getPageLength();
                     
                 }
 
                 /* ----------------------------------------------------------------
                  * -- 4d. Iterate through user records and calculate report data --
                  * ---------------------------------------------------------------- */
 
                 for (final Entry<User,UserRecord> e : recordMap.entrySet())
                 {
                     recordCount++;
                     totalQueueDuration += e.getValue().getTotalQueueDuration();
                     totalSessionDuration += e.getValue().getTotalSessionDuration();
                     if ((recordCount > (pageNumber-1)*pageLength) && (recordCount <= pageNumber*pageLength))
                     {
                         final SessionReportType reportType = new SessionReportType();
 
                         //Get user from session object
                         final RequestorType user1 = new RequestorType();
                         final UserNSNameSequence nsSequence = new UserNSNameSequence();
                         nsSequence.setUserName(e.getKey().getName());
                         nsSequence.setUserNamespace(e.getKey().getNamespace());
                         user1.setRequestorTypeSequence_type0(nsSequence);
                         reportType.setUser(user1);
 
                         reportType.setUser(user1);
                         
                         reportType.setAveQueueDuration(e.getValue().getAverageQueueDuration());
                         reportType.setMedQueueDuration(e.getValue().getMedianQueueDuration());
                         reportType.setMinQueueDuration(e.getValue().getMinimumQueueDuration());
                         reportType.setMaxQueueDuration(e.getValue().getMaximumQueueDuration());
                         reportType.setTotalQueueDuration(e.getValue().getTotalQueueDuration());
 
                         reportType.setAveSessionDuration(e.getValue().getAverageSessionDuration());
                         reportType.setMedSessionDuration(e.getValue().getMedianSessionDuration());
                         reportType.setMinSessionDuration(e.getValue().getMinimumSessionDuration());
                         reportType.setMaxSessionDuration(e.getValue().getMaximumSessionDuration());
                         reportType.setTotalSessionDuration(e.getValue().getTotalSessionDuration());
                         
                         respType.addSessionReport(reportType);
                     }
                 }
                 
                 respType.setSessionCount(recordCount);
                 respType.setTotalQueueDuration(totalQueueDuration);
                 respType.setTotalSessionDuration(totalSessionDuration);
                 
                 resp.setQuerySessionReportResponse(respType);
 
             }
 
         }
         
         finally
         {
             ses.close();
         }
         
         return resp;
     }
 
     
     /**
      * Gets the user identified by the user id type. 
      * 
      * @param uid user identity 
      * @param ses database session
      * @return user or null if not found
      */
     private User getUserFromUserID(final RequestorType uid, final org.hibernate.Session ses)
     {
         final UserDao dao = new UserDao(ses);
         User user;
         
         final long recordId = this.getIdentifier(uid.getUserID());
         final String ns = uid.getUserNamespace(), nm = uid.getUserName();
         
         if (recordId > 0 && (user = dao.get(recordId)) != null)
         {
             return user;
         }
         else if (ns != null && nm != null && (user = dao.findByName(ns, nm)) != null)
         {
             return user;
         }
         
         return null;
     }
     
     /**
      * Converts string identifiers to a long.
      * 
      * @param idStr string containing a long  
      * @return long or 0 if identifier not valid
      */
     private long getIdentifier(final String idStr)
     {
         if (idStr == null)
         {
             return 0;
         }
         
         try
         {
             return Long.parseLong(idStr);
         }
         catch (final NumberFormatException nfe)
         {
             return 0;
         }
     }
 }
 
