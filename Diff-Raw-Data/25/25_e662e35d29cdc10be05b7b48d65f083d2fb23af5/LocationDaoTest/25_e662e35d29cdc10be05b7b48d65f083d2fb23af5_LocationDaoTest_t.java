 package com.nedap.healthcare.kadasterbagclient.api.dao;
 
import java.util.Collections;
import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.nedap.healthcare.kadasterbagclient.api.model.Address;
 import com.nedap.healthcare.kadasterbagclient.api.util.DateTimeUtil;
 
 /**
  * Testing {@link LocationHibernateDao}.
  * 
  * @author Dusko Vesin
  */
 public class LocationDaoTest extends AbstractDaoTransactionalTest<Address> {
 
     @Autowired
     private AddressDao locationDao;
 
    private static final Comparator<Address> ADDRESS_COMPARATOR = new Comparator<Address>() {
        // This is where the sorting happens.
        @Override
        public int compare(final Address o1, final Address o2) {
            return o1.getNumber() - o2.getNumber();
        }
    };

     @Override
     protected GenericDao<Address> getDao() {
         return locationDao;
     }
 
     @Override
     public Class<Address> getEntityClass() {
         return Address.class;
     }
 
     @Override
     public void testFindAll() {
         // init
         final Address location1 = createUniqueAddress("1");
         locationDao.save(location1);
 
         final Address location2 = createUniqueAddress("2");
         locationDao.save(location2);
 
         final Address location3 = createUniqueAddress("3");
         locationDao.save(location3);
 
         // method
         takeSnapshot();
         final List<Address> findAll = locationDao.findAll();

        Collections.sort(findAll, ADDRESS_COMPARATOR);

         assertObjects(findAll, location1, location2, location3);
 
     }
 
     /**
      * Testing method {@link AddressDao#findByCountryPostalCodeAndNumber} .
      */
     @Test
     public void testFindByCountryPostalCodeAndNumber() {
 
         // data preparation
         final Address location1 = createUniqueAddress("1");
         locationDao.save(location1);
         final Address location2 = createUniqueAddress("2");
         locationDao.save(location2);
         final Address location3 = createUniqueAddress("3");
         locationDao.save(location3);
         final String countryCode = "conutryCode2";
         final String postalCode = "postalCode2";
         final int number = 2;
 
         takeSnapshot();
 
         // call method
         final Address location = locationDao.findByCountryPostalCodeAndNumber(countryCode, postalCode, number);
 
         // asserting
         assertObjects(location, location2);
 
     }
 
     /**
      * Testing method {@link AddressDao#findByCountryPostalCodeAndNumber}. Method should return null because of wrong
      * value for countryCode parameter.
      */
     @Test
     public void testFindByCountryPostalCodeAndNumberFailForCountryCode() {
 
         // data preparation
         final Address location1 = createUniqueAddress("1");
         locationDao.save(location1);
         final Address location2 = createUniqueAddress("2");
         locationDao.save(location2);
         final Address location3 = createUniqueAddress("3");
         locationDao.save(location3);
         final String countryCode = "conutryCode4";
         final String postalCode = "postalCode2";
         final int number = 2;
 
         takeSnapshot();
 
         // call method
         final Address location = locationDao.findByCountryPostalCodeAndNumber(countryCode, postalCode, number);
 
         // asserting
         assertNull(location);
     }
 
     /**
      * Testing method {@link AddressDao#findByCountryPostalCodeAndNumber}. Method should return null because of wrong
      * value for postalCode parameter.
      */
     @Test
     public void testFindByCountryPostalCodeAndNumberFailForPostalCode() {
 
         // data preparation
         final Address location1 = createUniqueAddress("1");
         locationDao.save(location1);
         final Address location2 = createUniqueAddress("2");
         locationDao.save(location2);
         final Address location3 = createUniqueAddress("3");
         locationDao.save(location3);
         final String countryCode = "conutryCode1";
         final String postalCode = "postalCode2";
         final int number = 1;
 
         takeSnapshot();
 
         // call method
         final Address location = locationDao.findByCountryPostalCodeAndNumber(countryCode, postalCode, number);
 
         // asserting
         assertNull(location);
     }
 
     /**
      * Testing method {@link AddressDao#findByCountryPostalCodeAndNumber}. Method should return null because of wrong
      * value for number parameter.
      */
     @Test
     public void testFindByCountryPostalCodeAndNumberFailForNumber() {
 
         // data preparation
         final Address location1 = createUniqueAddress("1");
         locationDao.save(location1);
         final Address location2 = createUniqueAddress("2");
         locationDao.save(location2);
         final Address location3 = createUniqueAddress("3");
         locationDao.save(location3);
         final String countryCode = "conutryCode1";
         final String postalCode = "postalCode1";
         final int number = 0;
 
         takeSnapshot();
 
         // call method
         final Address location = locationDao.findByCountryPostalCodeAndNumber(countryCode, postalCode, number);
 
         // asserting
         assertNull(location);
     }
 
     /**
      * Testing method {@link AddressDao#findByCountryPostalCodeAndNumber}. Method should return null because null
      * parameters are passed through.
      */
     @Test
     public void testFindByCountryPostalCodeAndNumberFailForNullParameters() {
 
         // data preparation
         final Address location1 = createUniqueAddress("1");
         locationDao.save(location1);
         final Address location2 = createUniqueAddress("2");
         locationDao.save(location2);
         final Address location3 = createUniqueAddress("3");
         locationDao.save(location3);
 
         takeSnapshot();
 
         // call method
         final Address location = locationDao.findByCountryPostalCodeAndNumber(null, null, null);
 
         // asserting
         assertNull(location);
     }
 
     @Override
     public Address createValidObject() {
         return createUniqueAddress("");
     }
 
     /**
      * Testing methods used for access to database outside the regular functions (with custom queries)
      */
     @Test
     public void testHibernateCriteriaMethods() {
 
         // data preparation
         final Address location1 = createUniqueAddress("1");
         locationDao.save(location1);
         final Address location2 = createUniqueAddress("2");
         locationDao.save(location2);
         final Address location3 = createUniqueAddress("3");
         locationDao.save(location3);
 
         final String[] params = {Address.POSTAL_CODE, Address.NUMBER};
         final Map<String, String> criterias = new HashMap<String, String>();
         criterias.put(Address.CITY, "city");
 
         takeSnapshot();
 
         // call method
         final List<Address> list1 = locationDao.findByExample(location1, params);
         final List<Address> list2 = locationDao.findByCriteria(criterias);
         final List<Address> list3 = locationDao.findAll();
 
         // asserting
         assertNotNull(list1);
         assertNotNull(list2);
         assertNotNull(list3);
     }
 
     private Address createUniqueAddress(final String unique) {
         final Address address = new Address();
         address.setCountryCode("conutryCode" + unique);
         address.setLatitude("latitude" + unique);
         address.setLongitude("longitude" + unique);
         if (unique.length() > 0) {
             address.setNumber(Integer.valueOf(unique));
             address.setNumberPostfix("a" + unique);
         } else {
             address.setNumber(0);
             address.setNumberPostfix("a");
         }
         address.setPostalCode("postalCode" + unique);
         address.setStreet("street");
         address.setCity("city");
         final DateTime validFrom = DateTimeUtil.parse("20120512120000");
         final DateTime validTo = DateTimeUtil.parse("20120912120000");
         address.setValidFrom(validFrom);
         address.setValidTo(validTo);
        address.setCreationDate(DateTimeUtil.parse("20121102092100"));
         return address;
     }
 }
