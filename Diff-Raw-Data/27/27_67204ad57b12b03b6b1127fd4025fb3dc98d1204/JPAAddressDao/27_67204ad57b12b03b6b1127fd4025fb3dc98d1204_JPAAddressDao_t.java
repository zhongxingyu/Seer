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
 
 import com.mpower.dao.AddressDao;
 import com.mpower.domain.Address;
 import com.mpower.util.CalendarUtils;
 
 @Repository("addressDao")
 public class JPAAddressDao implements AddressDao {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
 
     @PersistenceContext
     private EntityManager em;
 
     @Override
     public Address maintainAddress(Address address) {
         if (address.getId() == null) {
             em.persist(address);
             return address;
         } else {
             return em.merge(address);
         }
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Address> readAddresses(Long personId) {
         Query query = em.createNamedQuery("READ_ACTIVE_ADDRESSES_BY_PERSON_ID");
        query.setFlushMode(FlushModeType.COMMIT);
         query.setParameter("personId", personId);
         return query.getResultList();
     }
 
     @Override
     public void deleteAddress(Address address) {
         em.remove(address);
     }
 
     @Override
     public Address readAddress(Long addressId) {
         return em.find(Address.class, addressId);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public List<Address> readCurrentAddresses(Long personId, Calendar cal, boolean mailOnly) {
         // List<Address> retAddresses = new ArrayList<Address>();
         LinkedHashSet<String> typeSet = new LinkedHashSet<String>(); // store types of addresses
         Query query = em.createNamedQuery("READ_ACTIVE_ADDRESSES_BY_PERSON_ID_ORDER_BY_TYPE");
         query.setParameter("personId", personId);
         List<Address> addresses = query.getResultList();
         // create an ordered map of "status" -> (ordered map of "type" -> address)
         LinkedHashMap<String, LinkedHashMap<String, List<Address>>> addressMap = new LinkedHashMap<String, LinkedHashMap<String, List<Address>>>();
         for (Address address : addresses) {
             if ("temporary".equals(address.getActivationStatus())) {
                 if (!cal.getTime().before(address.getTemporaryStartDate()) && !cal.getTime().after(address.getTemporaryEndDate())) {
                     addToMap(addressMap, typeSet, "temporary", address);
                 }
             } else if ("seasonal".equals(address.getActivationStatus())) {
                 Calendar today = CalendarUtils.getToday(false);
                 Date startDate = address.getSeasonalStartDate();
                 Calendar startCal = new GregorianCalendar();
                 startCal.setTime(startDate);
                 Date endDate = address.getSeasonalEndDate();
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
                     addToMap(addressMap, typeSet, "seasonal", address);
                 }
             } else if ("permanent".equals(address.getActivationStatus())) {
                 if (address.getEffectiveDate() == null || !address.getEffectiveDate().after(cal.getTime())) {
                     addToMap(addressMap, typeSet, "permanent", address);
                 }
             }
         }
         List<Address> addressList = new ArrayList<Address>();
         for (String type : typeSet) {
             if (addressMap.get("temporary") != null && addressMap.get("temporary").get(type) != null) {
                 addressList.addAll(addressMap.get("temporary").get(type));
             } else if (addressMap.get("seasonal") != null && addressMap.get("seasonal").get(type) != null) {
                 addressList.addAll(addressMap.get("seasonal").get(type));
             } else if (addressMap.get("permanent") != null && addressMap.get("permanent").get(type) != null) {
                 addressList.addAll(addressMap.get("permanent").get(type));
             }
         }
 
         // if want only those accepting mail, then filter out no mail addresses
         for (Iterator iterator = addressList.iterator(); iterator.hasNext();) {
             Address address = (Address) iterator.next();
             if (mailOnly && !address.isReceiveMail()) {
                 iterator.remove();
             }
         }
         if (logger.isDebugEnabled()) {
             logger.debug("readCurrentAddresses(), mailOnly = " + mailOnly);
             for (Address a : addressList) {
                 logger.debug("status = " + a.getActivationStatus() + ", type = " + a.getAddressType() + ", address = " + a.getAddressLine1());
             }
         }
         return addressList;
     }
 
     private void addToMap(LinkedHashMap<String, LinkedHashMap<String, List<Address>>> statusMap, Set<String> typeSet, String key, Address address) {
         LinkedHashMap<String, List<Address>> typeMap = statusMap.get(key);
         if (typeMap == null) {
             typeMap = new LinkedHashMap<String, List<Address>>();
         }
         List<Address> aList = typeMap.get(address.getAddressType());
         if (aList == null) {
             aList = new ArrayList<Address>();
         }
         aList.add(address);
         typeSet.add(address.getAddressType());
         typeMap.put(address.getAddressType(), aList);
         statusMap.put(key, typeMap);
     }
 
     @Override
     public void inactivateAddresses() {
         Query query = em.createNamedQuery("SET_EXPIRED_TEMPORARY_ADDRESSES_INACTIVE");
         int modifedRecordCount = query.executeUpdate();
         if (logger.isDebugEnabled() && modifedRecordCount > 0) {
             logger.debug("  inactivateAddressJob: number of addresses marked inactive = " + modifedRecordCount);
         }
     }
 }
