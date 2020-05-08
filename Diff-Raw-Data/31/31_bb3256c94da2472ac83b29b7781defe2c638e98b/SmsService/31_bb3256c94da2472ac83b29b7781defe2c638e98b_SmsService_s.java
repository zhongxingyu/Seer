 package com.cqlybest.common.service;
 
 import java.util.List;
 
 import org.hibernate.criterion.Order;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.cqlybest.common.bean.Sms;
 import com.cqlybest.common.dao.SmsDao;
 
 @Service
 public class SmsService {
 
   private com.belerweb.sms._9nuo.Sms SMS = com.belerweb.sms._9nuo.Sms.init();
 
   @Autowired
   private SmsDao smsDao;
 
   @Transactional
  public void send(String phone, String content) {
     Sms sms = new Sms();
     sms.setPhone(phone);
     sms.setContent(content);
     sms.setSuccess(SMS.send(phone, content).isSuccess());
     smsDao.saveOrUpdate(sms);
   }
 
 
   public Long querySmsTotal() {
     return smsDao.total();
   }
 
   public List<Sms> querySmss(int page, int pageSize) {
     return smsDao.find(Order.desc("sentDate"), page, pageSize);
   }
 
 }
