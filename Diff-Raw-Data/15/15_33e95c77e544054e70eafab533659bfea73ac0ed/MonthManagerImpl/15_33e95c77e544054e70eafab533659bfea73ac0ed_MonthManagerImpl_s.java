 package war.webapp.service.impl;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import war.webapp.dao.MonthDao;
 import war.webapp.model.DutyMonth;
 import war.webapp.service.MonthManager;
 
@Service("monthManager")
 public class MonthManagerImpl extends GenericManagerImpl<DutyMonth, Long> implements MonthManager {
     MonthDao monthDao;
 
     public MonthManagerImpl() {
     }
 
     @Autowired
     public MonthManagerImpl(MonthDao monthDao) {
         super(monthDao);
         this.monthDao = monthDao;
     }
 
     public DutyMonth loadMonth(Integer year, Integer month, String floor) {
         return monthDao.loadMonth(year, month, floor);
     }
 
     public DutyMonth saveMonth(DutyMonth dutyMonth) {
         return monthDao.saveMonth(dutyMonth);
     }
 }
