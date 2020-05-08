 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.uhsarp.billrive.services.impl;
 
 //import com.uhsarp.billrive.dao.GenericDao;
 //import com.uhsarp.billrive.dao.MySqlDao;
 import com.uhsarp.billrive.dao.framework.BillDAO;
 import com.uhsarp.billrive.dao.framework.GroupDAO;
 import com.uhsarp.billrive.dao.framework.UserDAO;
 import com.uhsarp.billrive.domain.Bill;
 import com.uhsarp.billrive.domain.User;
 import com.uhsarp.billrive.services.BillService;
 import java.util.List;
 import javax.annotation.Resource;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author Prashanth Batchu
  */
 @Service("billService")
 public class BillServiceImpl implements BillService {
     
 //    @Resource(name="genericDao")
 //    GenericDao genericDao;
     
 //    @Resource(name="billDao")
 //    BillDao billDao;
     
     @Resource(name="billDAO")
     BillDAO billDAO;
     
     @Resource(name="groupDAO")
     GroupDAO groupDAO;
     
     @Resource(name="userDAO")
     UserDAO userDAO;
 //    @Autowired
 //    MySqlDao mySqlDao;
     public List<Bill> getBills(Long userId) {
          User user = (User) userDAO.getUserByUserId(userId);
         
 //       Group group = (Group) groupDAO.findGroupByGroupId(userId);
 //        mySqlDao.saveBill();
         //List<Bill> rtnval = mySqlDao.getBills();
 //        rtnval=genericDAO.getBills(userId);
         //test
         
 //        groupDAO.findGroupId(userId);
 //       List<Bill> bills = billDAO.getBillsByGroupId(userId);
 //        List<Bill> bills = new ArrayList<Bill>();
 //        bills.add(billDao.getBill(2l));
 //        bills.add(billDao.getBill(4l));
         
         return null;
     }
 
     public Bill getBillById(Long id) {
         return null;//(Bill) genericDao.get(id);
     }
 @Transactional(propagation=Propagation.REQUIRED, rollbackFor=Exception.class)
     public Bill addBill(Bill bill_p) {
         
          billDAO.save(bill_p);
         return null;
     }
 
     public Bill updateBill(Bill bill_p) {
         throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     public Boolean deleteBill(Long billId_p) {
       Boolean deleted= billDAO.removeById(billId_p);
       if(deleted)
           return true;
       else
            return false;
     }
 
 
     
 }
