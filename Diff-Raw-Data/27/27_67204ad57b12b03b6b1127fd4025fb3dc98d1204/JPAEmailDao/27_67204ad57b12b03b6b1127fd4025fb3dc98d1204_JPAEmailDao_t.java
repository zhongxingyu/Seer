 package com.mpower.dao.jpa;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
import javax.persistence.FlushModeType;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Repository;
 
 import com.mpower.dao.EmailDao;
 import com.mpower.domain.Email;
 import com.mpower.util.CalendarUtils;
 
 @Repository("emailDao")
 public class JPAEmailDao implements EmailDao {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @PersistenceContext
     private EntityManager em;
 
     @Override
     public Email maintainEmail(Email email) {
         if (email.getId() == null) {
             em.persist(email);
             return email;
         } else {
             return em.merge(email);
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Email> readEmails(Long personId) {
         Query query = em.createNamedQuery("READ_ACTIVE_EMAILS_BY_PERSON_ID");
         query.setParameter("personId", personId);
        query.setFlushMode(FlushModeType.COMMIT);
         return query.getResultList();
     }
 
     @Override
     public void deleteEmail(Email email) {
         em.remove(email);
     }
 
     @Override
     public Email readEmail(Long emailId) {
         return em.find(Email.class, emailId);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Email> readCurrentEmails(Long personId, Calendar cal, boolean mailOnly) {
         // List<Address> retAddresses = new ArrayList<Address>();
         LinkedHashSet<String> typeSet = new LinkedHashSet<String>(); // store types of emails
         Query query = em.createNamedQuery("READ_ACTIVE_EMAILS_BY_PERSON_ID_ORDER_BY_TYPE");
         query.setParameter("personId", personId);
         List<Email> emails = query.getResultList();
         // create an ordered map of "status" -> (ordered map of "type" -> email)
         LinkedHashMap<String, LinkedHashMap<String, List<Email>>> emailMap = new LinkedHashMap<String, LinkedHashMap<String, List<Email>>>();
         for (Email email : emails) {
             if ("temporary".equals(email.getActivationStatus())) {
                 if (!cal.getTime().before(email.getTemporaryStartDate()) && !cal.getTime().after(email.getTemporaryEndDate())) {
                     addToMap(emailMap, typeSet, "temporary", email);
                 }
             } else if ("seasonal".equals(email.getActivationStatus())) {
                 Calendar today = CalendarUtils.getToday(false);
                 Date startDate = email.getSeasonalStartDate();
                 Calendar startCal = new GregorianCalendar();
                 startCal.setTime(startDate);
                 Date endDate = email.getSeasonalEndDate();
                 Calendar endCal = new GregorianCalendar();
                 endCal.setTime(endDate);
                 if (startCal.get(Calendar.MONTH) <= endCal.get(Calendar.MONTH)) {
                     startCal.set(Calendar.YEAR, today.get(Calendar.YEAR));
                     endCal.set(Calendar.YEAR, today.get(Calendar.YEAR));
                 } else {
                     if (startCal.get(Calendar.MONTH) <= cal.get(Calendar.MONTH)) {
                         startCal.set(Calendar.YEAR, today.get(Calendar.YEAR));
                         endCal.set(Calendar.YEAR, today.get(Calendar.YEAR) + 1);
                     } else {
                         startCal.set(Calendar.YEAR, today.get(Calendar.YEAR) - 1);
                         endCal.set(Calendar.YEAR, today.get(Calendar.YEAR));
                     }
                 }
                 if (!cal.before(startCal) && !cal.after(endCal)) {
                     addToMap(emailMap, typeSet, "seasonal", email);
                 }
             } else if ("permanent".equals(email.getActivationStatus())) {
                 if (email.getEffectiveDate() == null || !email.getEffectiveDate().after(cal.getTime())) {
                     addToMap(emailMap, typeSet, "permanent", email);
                 }
             }
         }
         List<Email> emailList = new ArrayList<Email>();
         for (String type : typeSet) {
             if (emailMap.get("temporary") != null && emailMap.get("temporary").get(type) != null) {
                 emailList.addAll(emailMap.get("temporary").get(type));
             } else if (emailMap.get("seasonal") != null && emailMap.get("seasonal").get(type) != null) {
                 emailList.addAll(emailMap.get("seasonal").get(type));
             } else if (emailMap.get("permanent") != null && emailMap.get("permanent").get(type) != null) {
                 emailList.addAll(emailMap.get("permanent").get(type));
             }
         }
 
         // if want only those accepting mail, then filter out no mail emails
         for (Iterator iterator = emailList.iterator(); iterator.hasNext();) {
             Email email = (Email) iterator.next();
             if (mailOnly && !email.isReceiveMail()) {
                 iterator.remove();
             }
         }
         if (logger.isDebugEnabled()) {
             logger.debug("readCurrentEmails(), mailOnly = " + mailOnly);
             for (Email e : emailList) {
                 logger.debug("status = " + e.getActivationStatus() + ", type = " + e.getEmailType() + ", email = " + e.getEmailAddress());
             }
         }
         return emailList;
     }
 
     private void addToMap(LinkedHashMap<String, LinkedHashMap<String, List<Email>>> statusMap, Set<String> typeSet, String key, Email email) {
         LinkedHashMap<String, List<Email>> typeMap = statusMap.get(key);
         if (typeMap == null) {
             typeMap = new LinkedHashMap<String, List<Email>>();
         }
         List<Email> aList = typeMap.get(email.getEmailType());
         if (aList == null) {
             aList = new ArrayList<Email>();
         }
         aList.add(email);
         typeSet.add(email.getEmailType());
         typeMap.put(email.getEmailType(), aList);
         statusMap.put(key, typeMap);
     }
 
     @Override
     public void inactivateEmails() {
         Query query = em.createNamedQuery("SET_EXPIRED_TEMPORARY_EMAILS_INACTIVE");
         int modifedRecordCount = query.executeUpdate();
         if (logger.isDebugEnabled() && modifedRecordCount > 0) {
             logger.debug("  inactivateEmailJob: number of emails marked inactive = " + modifedRecordCount);
         }
     }
 }
