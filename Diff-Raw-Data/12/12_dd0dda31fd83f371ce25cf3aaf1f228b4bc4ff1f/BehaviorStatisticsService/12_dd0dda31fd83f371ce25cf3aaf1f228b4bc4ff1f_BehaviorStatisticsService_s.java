 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mne.advertmanager.service;
 
 import com.mne.advertmanager.dao.GenericDao;
 import com.mne.advertmanager.model.AffProgram;
 import com.mne.advertmanager.model.FilterableBehaviorStatistics;
 import com.mne.advertmanager.util.Page;
 import com.mne.advertmanager.util.PageCtrl;
 import java.util.*;
 import org.slf4j.LoggerFactory;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author Misha
  */
 public class BehaviorStatisticsService {
     
     private static org.slf4j.Logger logger = LoggerFactory.getLogger(BehaviorStatisticsService.class);
     private GenericDao<FilterableBehaviorStatistics, Integer> totalBehaviorStatsDao;
     private GenericDao<FilterableBehaviorStatistics, Integer> prevMonthBehaviorStatsDao;
     private GenericDao<FilterableBehaviorStatistics, Integer> curMonthBehaviorStatsDao;
     private GenericDao<FilterableBehaviorStatistics, Integer> dailyBehaviorStatsDao;
     private GenericDao<FilterableBehaviorStatistics, Integer> statsDao;
     private AccessLogService accessService;
     private AffProgramService affProgramService;
     private BillingProjectService blngService;
     
     public void setAccessService(AccessLogService accessService) {
 	
 	this.accessService = accessService;
     }
     
     public void setAffProgramService(AffProgramService affProgramService) {
 	this.affProgramService = affProgramService;
     }
     
     public void setBlngService(BillingProjectService blngService) {
 	this.blngService = blngService;
     }
     
     public void setTotalBehaviorStatsDao(GenericDao<FilterableBehaviorStatistics, Integer> totalBehaviorStatsDao) {
 	this.totalBehaviorStatsDao = totalBehaviorStatsDao;
     }
     
     public void setPrevMonthBehaviorStatsDao(GenericDao<FilterableBehaviorStatistics, Integer> prevMonthBehaviorStatsDao) {
 	this.prevMonthBehaviorStatsDao = prevMonthBehaviorStatsDao;
     }
     
     public void setCurMonthBehaviorStatsDao(GenericDao<FilterableBehaviorStatistics, Integer> curMonthBehaviorStatsDao) {
 	this.curMonthBehaviorStatsDao = curMonthBehaviorStatsDao;
     }
     
     public void setDailyBehaviorStatsDao(GenericDao<FilterableBehaviorStatistics, Integer> dailyBehaviorStatsDao) {
 	this.dailyBehaviorStatsDao = dailyBehaviorStatsDao;
     }
     
     @Transactional(readOnly = true)
     public FilterableBehaviorStatistics findTotalAffProgramStatistics(int affProgramId) {
 	
 	FilterableBehaviorStatistics result = findAffProgramStatistics(totalBehaviorStatsDao, "TotalBehaviorStats.findAffProgCountryStats", affProgramId, "Filter.All");
 	
 	logger.info("ProgramId={},Retrieved total statistics for all filters", affProgramId);
 	
 	return result;
     }
    
     
     @Transactional(readOnly = true)
     public Set<FilterableBehaviorStatistics> findAffProgramStatistics(int affProgramId) {
 	
 	FilterableBehaviorStatistics result = findAffProgramStatistics(totalBehaviorStatsDao, "TotalBehaviorStats.findAffProgCountryStats", affProgramId, "Filter.All");
 	
 	logger.info("ProgramId={},Retrieved total statistics for all filters", affProgramId);
 	
	return result;
     }   
     
     
     @Transactional(readOnly = true)
     public Set<FilterableBehaviorStatistics> findTotalAffiliateStatistics(int affiliateId) {
 	
 	Set<FilterableBehaviorStatistics> result = findAffiliateStatistics(totalBehaviorStatsDao, "TotalBehaviorStats.findAffiliateCountryStats", affiliateId, "Filter.All");
 	
 	logger.info("AffiliateId={},Retrieved total statistics for all programs", affiliateId);
 	
 	return result;
     }
 
 //=================================== findTotalAccesAmounByDomain ==============
     @Transactional(readOnly = true)
    public Set<FilterableBehaviorStatistics> findTotalAccesAmounByDomain(int affProgId) {
 	
	Set<FilterableBehaviorStatistics> result = findAccessByDomain(totalBehaviorStatsDao, "TotalBehaviorStats.findTotalAccesAmounByDomain", affProgId);
 	//    
 	logger.info("affProgId={},Retrieved total assecces by domain", affProgId);
 	
 	return result;
     }
     
     @Transactional(readOnly = true)
     public FilterableBehaviorStatistics findCurMonthAffProgramStatistics(int affProgramId) {
 	
 	FilterableBehaviorStatistics result = findAffProgramStatistics(curMonthBehaviorStatsDao, "CurMonthBehaviorStats.findAffProgCountryStats", affProgramId, "Filter.All");
 	
 	logger.info("ProgramId={}.Retrieved current month stats ", affProgramId);
 	
 	
 	return result;
     }
     
   //================================= findTotalAccessByCountry ===============
     //in progress
     @Transactional(readOnly =true)
     public Collection<FilterableBehaviorStatistics> findTotalAccessByCountry(int affProgId){
         
        ArrayList params = new ArrayList();
        params.add(affProgId);
         Collection<FilterableBehaviorStatistics> result = totalBehaviorStatsDao.findByQuery("TotalBehaviorStats.findTotalAccessesByCountry", affProgId);
         
         return result;
     }
   //==================================== findPrevMonthAffProgramStatistics =====
     @Transactional(readOnly = true)
     public FilterableBehaviorStatistics findPrevMonthAffProgramStatistics(int affProgramId) {
 	
 	FilterableBehaviorStatistics result = findAffProgramStatistics(prevMonthBehaviorStatsDao, "PrevMonthBehaviorStats.findAffProgCountryStats", affProgramId, "Filter.All");
 	
 	logger.info("Retrieved prev month aggregation data ,Program {}", affProgramId);
 	
 	return result;
     }
     
     @Transactional(readOnly = true)
     public FilterableBehaviorStatistics findDailyAffProgramGeneralStats(int affProgramId) {
 	
 	FilterableBehaviorStatistics result = findAffProgramStatistics(dailyBehaviorStatsDao, "DailyBehaviorStats.findAffProgCountryStats", affProgramId, "Filter.All");
 	
 	logger.info("ProgramId={}:::Retrieved daily statistics", affProgramId);
 	
 	return result;
     }
 //================================ calculate ===================================
     //@Scheduled(cron="0 0 4 * * ?")
     public void calculate() {
 	
 	logger.info("Started behavior statistics calculation");
 	
 	HashSet<AffProgram> affProgs = new HashSet<AffProgram>(affProgramService.findAllAffPrograms());
 	if (affProgs != null) {
 	    logger.info("Retrieved {} affiliate programs", affProgs.size());
 	    Iterator<AffProgram> affProgIter = affProgs.iterator();
 	    AffProgram curAffProgram = null;
 	    while (affProgIter.hasNext()) {
 		curAffProgram = affProgIter.next();
 		if (curAffProgram != null) {
 		    blngService.importBillingData(curAffProgram);
 		    calculateAffProgramStatistics(curAffProgram.getId());
 		}
 	    }
 	}
 	
 	logger.info("Finished behavior statistics calculation");
     }
 
     /*
      *
      * <query name="TotalBehaviorStats.findAffProgrCountryStats"><![CDATA[ from TotalBehaviorStats where countryName=? and affProgram.id=? ]]> </query> *
      */
 //=============================== calcDailyAffProgramStatsSet ==================
       private List<FilterableBehaviorStatistics> calcDailyAffProgramStatsSet(int affProgramId, Date refTime) {
 	
 	ArrayList<FilterableBehaviorStatistics> result = new ArrayList<FilterableBehaviorStatistics>();
         
         logger.info("Program={}:::BehaviorStatisticsCalculation:Start Financial Statistics Calculation", affProgramId);
         //get total purchase num and total commision amount
 	FilterableBehaviorStatistics total = dailyBehaviorStatsDao.findSingleItemByQuery("BehaviorStats.calcAffProgPeriodTotalFinancialStats", affProgramId, refTime);
         logger.info("Program={}:::BehaviorStatisticsCalculation:Finish Financial Statistics Calculation", affProgramId);
 	
 	if (total != null) {
             logger.info("Program={}:::BehaviorStatisticsCalculation:Start Period Access Calculation", affProgramId);
 	    int accessCount = accessService.findDailyAffProgramAccessAmount(affProgramId, refTime);
 	    logger.info("Program={}:::BehaviorStatisticsCalculation:Finish Access Calculation", affProgramId);
 	    total.setCountryName("Filter.All");
 	    total.setAccessAmount(accessCount);
 	    result.add(total);
 	    logger.debug("Program={}:::BehaviorStatisticsCalculation : Period=After {} ::Access={}, PO={} , Commision={}",
 		    new Object[]{affProgramId, refTime, accessCount, total.getPurchaseAmount(), total.getTotalCommision()});
 	    
 	}
 	
         //domain statistics
         logger.info("Program={}:::BehaviorStatisticsCalculation:Start Period Access by Domain Stats Calculation", affProgramId); 
 	result.addAll(dailyBehaviorStatsDao.findByQuery("BehaviorStats.calcAffProgPeriodDomainStats", affProgramId, refTime));
         logger.info("Program={}:::BehaviorStatisticsCalculation:Finish Period Access by  Domain Stats Calculation", affProgramId);
         
         //country accesses statistics
         logger.info("Program={}:::BehaviorStatisticsCalculation:Start Period Access by Country Stats Calculation", affProgramId); 
 	Collection<FilterableBehaviorStatistics> fsl = dailyBehaviorStatsDao.findByQuery("BehaviorStats.calcAffProgPeriodCountryStats", affProgramId, refTime);
         logger.info("Program={}:::BehaviorStatisticsCalculation:Finish Period Access by Country Stats Calculation", affProgramId); 
     
         //country purchase statistics
         logger.info("Program={}:::BehaviorStatisticsCalculation:Start Period Financial by Country Stats Calculation", affProgramId); 
 	Collection<FilterableBehaviorStatistics> countryFsl = dailyBehaviorStatsDao.findByQuery("BehaviorStats.calcAffProgPeriodFinancialStatsByCountry", affProgramId, refTime);
         logger.info("Program={}:::BehaviorStatisticsCalculation:Finish Period Financial by Country Stats Calculation", affProgramId); 
 	TreeMap<String, FilterableBehaviorStatistics> finStat = builFBSTree(fsl);
 	
 	FilterableBehaviorStatistics countryFBS = null;
 	
 	for (FilterableBehaviorStatistics fbs : countryFsl) {
 	    countryFBS = finStat.get(fbs.getKey());
 	    if (countryFBS != null) {
 		countryFBS.setPurchaseAmount(fbs.getPurchaseAmount());
 		countryFBS.setTotalCommision(fbs.getTotalCommision());
 	    }
 	}
 	result.addAll(finStat.values());
 	
 	return result;
     }
 //============================================== builFBSTree ===================
     private TreeMap<String, FilterableBehaviorStatistics> builFBSTree(Collection<FilterableBehaviorStatistics> data) {
 	
 	TreeMap<String, FilterableBehaviorStatistics> result = new TreeMap<String, FilterableBehaviorStatistics>();
 	if (data != null) {
 	    for (FilterableBehaviorStatistics fbs : data) {
 		result.put(fbs.getKey(), fbs);
 	    }
 	}
 	
 	return result;
     }
     
     private TreeMap<String, FilterableBehaviorStatistics> findAffProgramStats(GenericDao<FilterableBehaviorStatistics, Integer> statsDao, String queryName, int affProgramId) {
 	
 	Collection<FilterableBehaviorStatistics> data = statsDao.findByQuery(queryName, affProgramId);
 	
 	return builFBSTree(data);
     }
 //============================== calculateAffProgramStatistics =================
     
     @Transactional
     public void calculateAffProgramStatistics(int affProgramId) {
 	
 	logger.info("Program={}:::BehaviorStatisticsCalculation:Start", affProgramId);
 	Calendar cal = Calendar.getInstance();
 	//cal.add(Calendar.DATE, -1);
 	cal.clear();
 	//just for testing should be removed
 	cal.set(2012, Calendar.FEBRUARY, 1, 1, 0);
 	Date refTime = cal.getTime();
 	
         //calculate today statistics
         logger.info("Program={}:::BehaviorStatisticsCalculation:Start Daily Calculation", affProgramId);
 	List<FilterableBehaviorStatistics> todayStats = calcDailyAffProgramStatsSet(affProgramId, refTime);
         logger.info("Program={}:::BehaviorStatisticsCalculation:Finish Daily Calculation", affProgramId);
 
         //retrive current total stats and update them 
 	TreeMap<String, FilterableBehaviorStatistics> totalStats = findAffProgramStats(totalBehaviorStatsDao, "TotalBehaviorStats.findAffProgStats", affProgramId);
 	updateStats(totalBehaviorStatsDao, totalStats, todayStats);
         logger.info("Program={}:::BehaviorStatisticsCalculation:Total statistics updated", affProgramId);
 
         
         //retrive current month statistics 
 	TreeMap<String, FilterableBehaviorStatistics> cmStats = findAffProgramStats(curMonthBehaviorStatsDao, "CurMonthBehaviorStats.findAffProgStats", affProgramId);
 	
         //check if current month has ended, if so make it privious month and
         //begin new current month. add today statistics to current month.
 	if (cal.get(Calendar.DAY_OF_MONTH) == 1) {
 	    shiftStats(prevMonthBehaviorStatsDao, "PrevMonthBehaviorStats.deleteAffProgStats", affProgramId, cmStats.values());
 	    shiftStats(curMonthBehaviorStatsDao, "CurMonthBehaviorStats.deleteAffProgStats", affProgramId, todayStats);
             logger.info("Program={}:::BehaviorStatisticsCalculation:PrevMonth statistics updated",affProgramId);
             logger.info("Program={}:::BehaviorStatisticsCalculation:CurvMonth statistics updated",affProgramId);
 	} else {
 	    updateStats(curMonthBehaviorStatsDao, cmStats, todayStats);
 	    logger.info("Program={}:::BehaviorStatisticsCalculation:CurvMonth statistics updated",affProgramId);
 	}
 
 	//delete outdate statistics from the daily table
 	cleanAffProgStats(dailyBehaviorStatsDao, "DailyBehaviorStats.deleteAffProgStats", affProgramId);
 	//save today statistics
 	processFBSList(dailyBehaviorStatsDao, todayStats);
 	logger.info("Program={}:::BehaviorStatisticsCalculation:Daily statistics updated",affProgramId);
 	
 	logger.info("Program={}:::BehaviorStatisticsCalculation:Finish", affProgramId);
     }
 //============================== shiftStats ====================================
     private void shiftStats(GenericDao<FilterableBehaviorStatistics, Integer> statsDao, String queryName, int affProgramId, Collection<FilterableBehaviorStatistics> stats) {
 	
 	cleanAffProgStats(statsDao, queryName, affProgramId);
 	ArrayList<FilterableBehaviorStatistics> shifted = new ArrayList<FilterableBehaviorStatistics>();
 	FilterableBehaviorStatistics curFbs;
 	for (FilterableBehaviorStatistics fbs : stats) {
 	    curFbs = new FilterableBehaviorStatistics(fbs);
 	    shifted.add(curFbs);
 	}
 	processFBSList(statsDao, shifted);
 	
     }
 //============================== updateStats ===================================
     private void updateStats(GenericDao<FilterableBehaviorStatistics, Integer> statsDao,
 			     TreeMap<String, FilterableBehaviorStatistics> curStats,
 			     List<FilterableBehaviorStatistics> todayStats) {
 	
 	ArrayList<FilterableBehaviorStatistics> updatedStats = new ArrayList<FilterableBehaviorStatistics>();
 	for (FilterableBehaviorStatistics todayFbs : todayStats) {
 	    FilterableBehaviorStatistics curFbs = curStats.get(todayFbs.getKey());
 	    if (curFbs != null) {
 		curFbs.add(todayFbs);
 		updatedStats.add(curFbs);
 	    } else {
 		updatedStats.add(new FilterableBehaviorStatistics(todayFbs));
 	    }
 	}
 	
 	processFBSList(statsDao, updatedStats);
     }
 //============================== findAffProgramStatistics ======================
     @SuppressWarnings("unchecked")
     private FilterableBehaviorStatistics findAffProgramStatistics(GenericDao<FilterableBehaviorStatistics, Integer> statsDao, String query, int affProgramId, Object... params) {
 	
         //convert given params to array
 	ArrayList allParams = new ArrayList(Arrays.asList(params));
 	allParams.add(0, affProgramId);
        
 	FilterableBehaviorStatistics result = statsDao.findSingleItemByQuery(query, allParams.toArray());
 
 	return result;
     }
 //=============================== findAffiliateStatistics ======================
     
     @SuppressWarnings("unchecked")
     private Set<FilterableBehaviorStatistics> findAffiliateStatistics(GenericDao<FilterableBehaviorStatistics, Integer> statsDao, String query, int affProgramId, Object... params) {
 	
 	
 	ArrayList allParams = new ArrayList(Arrays.asList(params));
 	allParams.add(0, affProgramId);
 	
 	Collection<FilterableBehaviorStatistics> data = null;
 	data = statsDao.findByQuery(query, allParams.toArray());
 	HashSet<FilterableBehaviorStatistics> result = new HashSet<FilterableBehaviorStatistics>(data);
 	
 	return result;
     }
 //=============================== findAccessByDomain ===========================
 //this function return statistics of first 10 items recieved from db with given query.
 //this function is a common ground to query fbs for accesses by domain in defrent
 //time periods ( today, curWeek ...).
 //params:statistics Dao, query, program id, params needed for given query.
 //return: hashSet of 10 first items recieved from db with given query.
 //each item represented by FilterabaleBehaviorStatistics entity ( fbs )
     @SuppressWarnings("unchecked")
     private Set<FilterableBehaviorStatistics> findAccessByDomain(GenericDao<FilterableBehaviorStatistics, Integer> statsDao, String query, int affProgramId, Object... params) {
 	
         //convert param list to array
 	ArrayList allParams = new ArrayList(Arrays.asList(params));
 	allParams.add(0, affProgramId);
 	
 	Collection<FilterableBehaviorStatistics> data = null;
         //prepare page of data ( we want only 10 item, That is one page with 10 items )
 	Page<FilterableBehaviorStatistics> page = null;
 	page = statsDao.findPageByQuery(query, new PageCtrl(1, 0, 10), allParams.toArray());
 	data = page.getItems();
         
         //convert collection to hashSet
 	LinkedHashSet<FilterableBehaviorStatistics> result = new LinkedHashSet<FilterableBehaviorStatistics>(data);
 	
 	return result;
     }
 //=============================== cleanAffProgStats ============================
     @Transactional(propagation = Propagation.REQUIRES_NEW)
     private void cleanAffProgStats(GenericDao<FilterableBehaviorStatistics, Integer> statsDao, String queryName, int affProgramId) {
 	statsDao.executeUpdateByQuery(queryName, affProgramId);
     }
 //=============================== processFBSList ===============================
     private void processFBSList(GenericDao<FilterableBehaviorStatistics, Integer> statsDao, List<FilterableBehaviorStatistics> fbsList) {
 	
 	if (fbsList == null) {
 	    return;
 	}
 	
 	int setSize = fbsList.size();
 	int fromIdx = 0;
 	int toIdx = 0;
 	
 	List<FilterableBehaviorStatistics> subList = null;
 	
 	while (toIdx < setSize) {
 	    fromIdx = toIdx;
 	    toIdx = Math.min(fromIdx + 10, setSize);
 	    subList = fbsList.subList(fromIdx, toIdx);
             try {
                 persistItemList(statsDao, subList);
             }catch(Exception e) {
                 logger.error("{} while processing FBS list.Message={}",e.getClass().getSimpleName(),e.getMessage());
             }
 	}
     }
 //=============================== persistItemList ==============================
     @Transactional(propagation = Propagation.REQUIRES_NEW)
     private void persistItemList(GenericDao<FilterableBehaviorStatistics, Integer> statsDao, List<FilterableBehaviorStatistics> itemList) {
 	statsDao.saveDataSet(itemList);
     }
 }
