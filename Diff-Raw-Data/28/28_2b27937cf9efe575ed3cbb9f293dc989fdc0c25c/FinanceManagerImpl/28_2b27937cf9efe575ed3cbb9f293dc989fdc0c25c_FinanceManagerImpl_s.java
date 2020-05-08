 package jxt.service.impl;
 
 import jxt.dao.FinanceDAO;
 import jxt.model.Finance;
 import jxt.service.FinanceManager;
 import jxt.util.Formate;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.Resource;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: karl
  * Date: 5/5/13
  * Time: 7:08 PM
  * To change this template use File | Settings | File Templates.
  */
 @Component("service")
 public class FinanceManagerImpl implements FinanceManager{
 
     private FinanceDAO financeDAO;
     @Override
     public void saveIncident(Finance finance) {
         //To change body of implemented methods use File | Settings | File Templates.
         financeDAO.addIncident(finance);
     }
 
     @Override
     public List<Finance> queryFinance(String startDate, String endDate) {
         Formate formate = new Formate();
         startDate = formate.forMate(startDate);
         endDate = formate.forMate(endDate);
         List<Finance> queryList = financeDAO.queryList(startDate, endDate);
         return queryList;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     public FinanceDAO getFinanceDAO() {
         return financeDAO;
     }
     @Resource(name = "dao")
     public void setFinanceDAO(FinanceDAO financeDAO) {
         this.financeDAO = financeDAO;
     }
 }
