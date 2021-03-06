 package cz.zcu.kiv.eegdatabase.data.dao;
 
 import java.util.List;
 import cz.zcu.kiv.eegdatabase.data.pojo.Person;
 import cz.zcu.kiv.eegdatabase.logic.controller.search.SearchRequest;
 import cz.zcu.kiv.eegdatabase.logic.controller.util.ControllerUtils;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Map;
 import org.springframework.dao.support.DataAccessUtils;
 
 /**
  * This class extends powers class SimpleGenericDao.
  * Class is determined only for Person.
  * @author Pavel Bořík, A06208
  */
 public class SimplePersonDao
         extends SimpleGenericDao<Person, Integer> implements PersonDao {
 
   public SimplePersonDao() {
     super(Person.class);
   }
 
   /**
    * This method finds record (Person) that have userName
    * identical with searched userName. Search only one record,
    * because userName is unique.
    * @param userName - userName of searched Person
    * @return Person with searched userName
    */
   public Person getPerson(String userName) {
     String HQLselect = "from Person person " + "where person.username = :userName";
 
     Person foundUser = (Person) DataAccessUtils.uniqueResult(getHibernateTemplate().findByNamedParam(HQLselect, "userName", userName));
     return foundUser;
 
     /*List<Person> foundUser = getHibernateTemplate()
     .findByNamedParam(HQLselect, "userName", userName);
     return foundUser.iterator().next();*/
   }
 
   /**
    * This method finds record (Person) that have hashCode
    * identical with searched hashCode. Search only one record,
    * because hashCode is unique.
    * @param hashCode - hashCode of searched Person
    * @return Person with searched hashCode
    */
   public Person getPersonByHash(String hashCode) {
     String HQLselect = "from Person person " + "where person.authenticationHash = :hashCode";
 
     Person foundUser = (Person) DataAccessUtils.uniqueResult(getHibernateTemplate().findByNamedParam(HQLselect, "hashCode", hashCode));
     return foundUser;
   }
 
   /**
    * Method doesn't used now.
    * @return
    */
   public List<Person> getPersonsWherePendingRequirement() {
     String HQLselect = "from Person person " + "where person.requiresWriting = 'T' " + "and person.authority = 'ROLE_READER'";
     return getHibernateTemplate().find(HQLselect);
   }
 
   /**
    * This method compares designated userName with
    * content of database. If the list is empty, then
    * userName didn't found in database.
    * @param userName - searched username
    * @return true if userName exists (was founded},
    * else return false
    */
   public boolean usernameExists(String userName) {
     String HQLselect = "from Person person " + "where person.username = :userName";
     List<Person> list = getHibernateTemplate().
             findByNamedParam(HQLselect, "userName", userName);
     return (!list.isEmpty());
   }
 
   /**
    * This method finds records (Persons) that have set
    * authority on supervisor.
    * @return list of Person with supervisor's authority.
    */
   public List<Person> getSupervisors() {
     String HQLselect = "from Person person " + "where person.authority = 'ROLE_SUPERVISOR'";
     return getHibernateTemplate().find(HQLselect);
   }
 
   /**
    * This method gets currently person, which is currently logged into system
    * and returns its Person object.
    * @return Object of the logged person
    */
   public Person getLoggedPerson() {
     String userName = ControllerUtils.getLoggedUserName();
     return getPerson(userName);
   }
 
   public Map getInfoForAccountOverview(Person loggedPerson) {
     String hqlSelect = "select new map(" + "p.username as username, " + "p.givenname as givenname, " + "p.surname as surname, " + "p.authority as authority" + ") from Person p where p.personId = :personId";
     Map info;
     List list = getHibernateTemplate().
             findByNamedParam(hqlSelect, "personId", loggedPerson.getPersonId());
     if (list.size() == 1) {
       info = (Map) list.get(0);
     } else {
       info = new HashMap<String, String>();
     }
     return info;
   }
 
   public boolean userNameInGroup(String userName, int groupId) {
     String hqlQuery = "select p.personId " + "from Person p " + "left join p.researchGroupMemberships rgm " + "where p.username = :userName " + "and rgm.researchGroup.researchGroupId = :groupId";
 
     String[] paramNames = {"userName", "groupId"};
     Object[] values = {userName, groupId};
     List list = getHibernateTemplate().findByNamedParam(hqlQuery, paramNames, values);
     return (list.size() > 0);
   }
 
   public List<Person> getPersonSearchResults(List<SearchRequest> requests) throws NumberFormatException {
     boolean ignoreChoice = false;
     String hqlQuery = "from Person where ";
     for (SearchRequest request : requests) {
 
       if ((request.getCondition().equals("")) && (!request.getSource().equals("defect"))) {
         if (request.getChoice().equals("")) {
           ignoreChoice = true;
         }
         continue;
       }
       if (!ignoreChoice) {
         hqlQuery += request.getChoice();
 
       }
       if (request.getSource().startsWith("age")) {
         hqlQuery += "dateOfBirth" + getCondition(request.getSource()) +
                "'" + getPersonDateOfBirth(request.getCondition()) + "'";
       } else if (request.getSource().equals("defect")) {
         hqlQuery += "(visualImpairments.size = 0 and hearingImpairments.size = 0)";
      } else if (request.getSource().equals("gender")) {
        hqlQuery += "gender = '" + request.getCondition().toUpperCase().charAt(0) + "'";
       } else {
        hqlQuery += "lower(" + request.getSource() + ")" +
                 getCondition(request.getSource()) + "lower('%" + request.getCondition() + "%')";
       }
     }
     List<Person> results;
    System.out.println(hqlQuery);
     try {
       results = getHibernateTemplate().find(hqlQuery);
     } catch (Exception e) {
       return new ArrayList<Person>();
     }
     return results;
   }
 
   private String getCondition(String choice) {
     if (choice.equals("ageMax")) {
       return ">=";
     }
     if (choice.equals("ageMin")) {
       return "<=";
     }
     return " like ";
   }
 
  private String getPersonDateOfBirth(String age) throws NumberFormatException {
     // Create a calendar object with the date of birth
     Calendar today = Calendar.getInstance(); // Get age based on year
     int yearOfBirth = today.get(Calendar.YEAR) - Integer.parseInt(age);
 
    return today.get(Calendar.DAY_OF_MONTH) + "-" + (today.get(Calendar.MONTH) + 1) + "-" + yearOfBirth;
   }
 }
