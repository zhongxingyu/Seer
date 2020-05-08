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
 
 import com.mpower.dao.PhoneDao;
 import com.mpower.domain.Phone;
 import com.mpower.util.CalendarUtils;
 
 @Repository("phoneDao")
 public class JPAPhoneDao implements PhoneDao {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @PersistenceContext
     private EntityManager em;
 
     @Override
     public Phone maintainPhone(Phone phone) {
         if (phone.getId() == null) {
             em.persist(phone);
             return phone;
         } else {
             return em.merge(phone);
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Phone> readPhones(Long personId) {
         Query query = em.createNamedQuery("READ_ACTIVE_PHONES_BY_PERSON_ID");
        query.setFlushMode(FlushModeType.COMMIT);
         query.setParameter("personId", personId);
         return query.getResultList();
     }
 
     @Override
     public void deletePhone(Phone phone) {
         em.remove(phone);
     }
 
     @Override
     public Phone readPhone(Long phoneId) {
         return em.find(Phone.class, phoneId);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Phone> readCurrentPhones(Long personId, Calendar cal, boolean mailOnly) {
         // List<Address> retAddresses = new ArrayList<Address>();
         LinkedHashSet<String> typeSet = new LinkedHashSet<String>(); // store types of phones
         Query query = em.createNamedQuery("READ_ACTIVE_PHONES_BY_PERSON_ID_ORDER_BY_TYPE");
         query.setParameter("personId", personId);
         List<Phone> phones = query.getResultList();
         // create an ordered map of "status" -> (ordered map of "type" -> phone)
         LinkedHashMap<String, LinkedHashMap<String, List<Phone>>> phoneMap = new LinkedHashMap<String, LinkedHashMap<String, List<Phone>>>();
         for (Phone phone : phones) {
             if ("temporary".equals(phone.getActivationStatus())) {
                 if (!cal.getTime().before(phone.getTemporaryStartDate()) && !cal.getTime().after(phone.getTemporaryEndDate())) {
                     addToMap(phoneMap, typeSet, "temporary", phone);
                 }
             } else if ("seasonal".equals(phone.getActivationStatus())) {
                 Calendar today = CalendarUtils.getToday(false);
                 Date startDate = phone.getSeasonalStartDate();
                 Calendar startCal = new GregorianCalendar();
                 startCal.setTime(startDate);
                 Date endDate = phone.getSeasonalEndDate();
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
                     addToMap(phoneMap, typeSet, "seasonal", phone);
                 }
             } else if ("permanent".equals(phone.getActivationStatus())) {
                 if (phone.getEffectiveDate() == null || !phone.getEffectiveDate().after(cal.getTime())) {
                     addToMap(phoneMap, typeSet, "permanent", phone);
                 }
             }
         }
         List<Phone> phoneList = new ArrayList<Phone>();
         for (String type : typeSet) {
             if (phoneMap.get("temporary") != null && phoneMap.get("temporary").get(type) != null) {
                 phoneList.addAll(phoneMap.get("temporary").get(type));
             } else if (phoneMap.get("seasonal") != null && phoneMap.get("seasonal").get(type) != null) {
                 phoneList.addAll(phoneMap.get("seasonal").get(type));
             } else if (phoneMap.get("permanent") != null && phoneMap.get("permanent").get(type) != null) {
                 phoneList.addAll(phoneMap.get("permanent").get(type));
             }
         }
 
         // if want only those accepting mail, then filter out no mail phones
         for (Iterator iterator = phoneList.iterator(); iterator.hasNext();) {
             Phone phone = (Phone) iterator.next();
             if (mailOnly && !phone.isReceiveMail()) {
                 iterator.remove();
             }
         }
         if (logger.isDebugEnabled()) {
             logger.debug("readCurrentPhones(), mailOnly = " + mailOnly);
             for (Phone p : phoneList) {
                 logger.debug("status = " + p.getActivationStatus() + ", type = " + p.getPhoneType() + ", phone = " + p.getNumber());
             }
         }
         return phoneList;
     }
 
     private void addToMap(LinkedHashMap<String, LinkedHashMap<String, List<Phone>>> statusMap, Set<String> typeSet, String key, Phone phone) {
         LinkedHashMap<String, List<Phone>> typeMap = statusMap.get(key);
         if (typeMap == null) {
             typeMap = new LinkedHashMap<String, List<Phone>>();
         }
         List<Phone> aList = typeMap.get(phone.getPhoneType());
         if (aList == null) {
             aList = new ArrayList<Phone>();
         }
         aList.add(phone);
         typeSet.add(phone.getPhoneType());
         typeMap.put(phone.getPhoneType(), aList);
         statusMap.put(key, typeMap);
     }
 
     @Override
     public void inactivatePhones() {
         Query query = em.createNamedQuery("SET_EXPIRED_TEMPORARY_PHONES_INACTIVE");
         int modifedRecordCount = query.executeUpdate();
         if (logger.isDebugEnabled() && modifedRecordCount > 0) {
             logger.debug("  inactivatePhoneJob: number of phones marked inactive = " + modifedRecordCount);
         }
     }
 }
