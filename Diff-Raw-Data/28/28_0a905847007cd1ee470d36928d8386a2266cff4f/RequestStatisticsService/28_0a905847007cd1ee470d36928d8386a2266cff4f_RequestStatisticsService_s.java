 package fr.cg95.cvq.service.request.impl;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.apache.log4j.Logger;
 
 import fr.cg95.cvq.business.authority.Agent;
 import fr.cg95.cvq.business.authority.Category;
 import fr.cg95.cvq.business.authority.CategoryProfile;
 import fr.cg95.cvq.business.authority.CategoryRoles;
 import fr.cg95.cvq.dao.request.IRequestDAO;
 import fr.cg95.cvq.exception.CvqException;
 import fr.cg95.cvq.permission.CvqPermissionException;
 import fr.cg95.cvq.permission.PrivilegeDescriptor;
 import fr.cg95.cvq.security.SecurityContext;
 import fr.cg95.cvq.service.authority.ICategoryService;
 import fr.cg95.cvq.service.authority.LocalAuthorityConfigurationBean;
 import fr.cg95.cvq.service.request.IRequestStatisticsService;
 import fr.cg95.cvq.util.Critere;
 
 /**
  * This class provides statistics about requests.
  *
  * @author Benoit Orihuela (bor@zenexity.fr)
  */
 public class RequestStatisticsService implements IRequestStatisticsService {
 
     private static Logger logger = Logger.getLogger(RequestStatisticsService.class);
     
     private IRequestDAO requestDAO;
     private ICategoryService categoryService;
     private IRequestTypeDAO requestTypeDAO;
     
     public RequestStatisticsService() {
         super();
     }
 
     /**
      * FIXME : This should be better handled by an aspect. 
      */
     private void checkAgentRights() throws CvqException {
         Agent agent = SecurityContext.getCurrentAgent();
         Set<CategoryRoles> agentCategoriesRoles = agent.getCategoriesRoles();
         for (CategoryRoles categoryRole : agentCategoriesRoles) {
             if (categoryRole.getProfile().equals(CategoryProfile.MANAGER))
                 return;
         }
         
         throw new CvqPermissionException("Statistics", PrivilegeDescriptor.MANAGE);
     }
     
     private List<String> getCategoriesWithManagerProfile(Agent agent) 
         throws CvqException {
         
         List<Category> agentCategories = 
             categoryService.getAgentManagedCategories(agent);
         List<String> categoriesNames = new ArrayList<String>();
         for (Category category : agentCategories) {
             categoriesNames.add(category.getName());
         }
         
         return categoriesNames;
     }
     
     public Long getCount(final Set criteriaSet)
         throws CvqException {
 
         checkAgentRights();
         
         Critere crit = new Critere();
         List<Category> agentCategories = 
             categoryService.getAgentManagedCategories(SecurityContext.getCurrentAgent());
         StringBuffer sb = new StringBuffer();
         for (Category category : agentCategories) {
             if (sb.length() > 0)
                 sb.append(",");
             sb.append("'").append(category.getId()).append("'");
         }
         crit.setAttribut("belongsToCategory");
         crit.setComparatif(Critere.EQUALS);
         crit.setValue(sb.toString());
         criteriaSet.add(crit);
 
         return requestDAO.count(criteriaSet);
     }
 
     public Map<Date, Long> getDetailedStats(final Timescale timescale, final Lifecycle lifecycle, 
             final Long requestTypeId, final Long categoryId) {
 
         Map<Date, Long> results = new TreeMap<Date, Long>();
 
         String[] resultingState = getStatesFromLifecycle(lifecycle);
      
         List<Date> searchDates = getNextSearchEndDate(timescale, null);
         while (searchDates != null) {
             logger.debug("getDetailedStats() searching between " + searchDates.get(0)
                     + " and " + searchDates.get(1));
             Long count = requestDAO.countByResultingState(resultingState, searchDates.get(0), 
                     searchDates.get(1), requestTypeId, categoryId);
             logger.debug("getDetailedStats() adding " + count
                     + " to date " + searchDates.get(0));            
             results.put(searchDates.get(0), count);
             searchDates = getNextSearchEndDate(timescale, searchDates.get(1));
         }
         
         return results;
     }
 
     public Map<RequestType, Long> getSummarizedStats(final Timescale timescale, 
             final Lifecycle lifecycle, final Long requestTypeId, final Long categoryId) {
 
         Map<RequestType, Long> results = new HashMap<RequestType, Long>();
 
         String[] resultingState = getStatesFromLifecycle(lifecycle);        
         List<Date> searchDates = getNextSearchEndDate(timescale, null);
         
         Date now = new Date();
 
         if (requestTypeId == null && categoryId == null) {
             List<RequestType> requestTypes = requestTypeDAO.listAll();
             for (RequestType requestType : requestTypes) {
                 Long count = requestDAO.countByResultingState(resultingState, searchDates.get(0), 
                         now, requestType.getId(), null);
                 results.put(requestType, count);                
             }
         }
         
         return results;
     }
 
     public Map<String, Long> getQualityStats(final Timescale timescale, final Long requestTypeId, 
             final Long categoryId) {
 
         Map<String, Long> results = new HashMap<String, Long>();
         
         LocalAuthorityConfigurationBean lacb = SecurityContext.getCurrentConfigurationBean();
         if (!lacb.getInstructionAlertsEnabled())
             return null;
         
         List<Date> searchDates = getNextSearchEndDate(timescale, null);
             
         Date now = new Date();
         
         Long count = requestDAO.countByQuality(searchDates.get(0), now, 
                 lacb.getInstructionDoneStates(), QUALITY_TYPE_OK, requestTypeId, categoryId);
         results.put(QUALITY_TYPE_OK, count);
         // (startDate, endDate, resultingStates, qualityType, requestTypeLabel, categoriesNames)
         count = requestDAO.countByQuality(searchDates.get(0), now, 
                 lacb.getInstructionDoneStates(), QUALITY_TYPE_ORANGE, requestTypeId, categoryId);
         results.put(QUALITY_TYPE_ORANGE, count);
         
         count = requestDAO.countByQuality(searchDates.get(0), now, 
                 lacb.getInstructionDoneStates(), QUALITY_TYPE_RED, requestTypeId, categoryId);
         results.put(QUALITY_TYPE_RED, count);
         
         return results;
     }
 
     /**
      * Compute the next search period according to a timescale and start date.
      * 
      * @return a list containing a start and end date or null if no more dates
      */
     private List<Date> getNextSearchEndDate(final Timescale timescale, Date startDate) {
         
         logger.debug("getNextSearchEndDate() computing date for timescale "
                 + timescale + " and date " + startDate);
         
         Calendar calendar = new GregorianCalendar();
         if (startDate == null) {
             startDate = new Date();
             calendar.setTime(startDate);
             calendar.set(Calendar.HOUR_OF_DAY, 0);
             calendar.set(Calendar.MINUTE, 0);
             calendar.set(Calendar.SECOND, 0);
             switch(timescale) {
                 case MONTH : calendar.add(Calendar.MONTH, -1);break;
                 case WEEK : calendar.add(Calendar.DAY_OF_YEAR, -7);break;
                 case YEAR : calendar.add(Calendar.MONTH, -12);break;
             }
         } else {
             calendar.setTime(startDate);
         }
         
         List<Date> results = new ArrayList<Date>();
         results.add(calendar.getTime());
         Date now = new Date();
         if (timescale.equals(Timescale.MONTH) || timescale.equals(Timescale.WEEK)) {
             calendar.add(Calendar.DAY_OF_YEAR, 1);
             if (calendar.getTime().after(now))
                 return null;
             results.add(calendar.getTime());
             return results;
         } else if (timescale.equals(Timescale.YEAR)) {
             calendar.add(Calendar.MONTH, 1);
             if (calendar.getTime().after(now))
                 return null;
             results.add(calendar.getTime());
             return results;
         } else {
             return null;
         }
     }
     
     private String[] getStatesFromLifecycle(final Lifecycle lifecycle) {
         String[] resultingState = null;
         if (lifecycle.equals(Lifecycle.CREATED)) {
             resultingState = new String[] { RequestState.PENDING.toString() };
         } else {
             resultingState = new String[] { 
                     RequestState.CANCELLED.toString(),
                     RequestState.REJECTED.toString(),
                     RequestState.VALIDATED.toString()
             };
         }
         
         return resultingState;
     }
     
     public void setRequestDAO(IRequestDAO requestDAO) {
         this.requestDAO = requestDAO;
     }
 
     public void setRequestTypeDAO(IRequestTypeDAO requestTypeDAO) {
         this.requestTypeDAO = requestTypeDAO;
     }
 
     public void setCategoryService(ICategoryService categoryService) {
         this.categoryService = categoryService;
     }
 }
