 package fr.cg95.cvq.dao.users.hibernate;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Criteria;
 import org.hibernate.Hibernate;
 import org.hibernate.Query;
 import org.hibernate.type.Type;
 
 import fr.cg95.cvq.business.QoS;
 import fr.cg95.cvq.business.users.Individual;
 import fr.cg95.cvq.business.users.RoleType;
 import fr.cg95.cvq.business.users.UserState;
 import fr.cg95.cvq.dao.hibernate.GenericDAO;
 import fr.cg95.cvq.dao.hibernate.HibernateUtil;
 import fr.cg95.cvq.dao.users.IIndividualDAO;
 import fr.cg95.cvq.util.Critere;
 
 /**
  * The "Individual" service Hibernate implementation. This class is responsible
  * for data access logic functions
  * 
  * @author bor@zenexity.fr
  */
 public class IndividualDAO extends GenericDAO implements IIndividualDAO {
 
     private static Logger logger = Logger.getLogger(IndividualDAO.class);
 
     public Individual findByFederationKey(final String federationKey) {
         Criteria crit = HibernateUtil.getSession().createCriteria(Individual.class);
         crit.add(Critere.compose("federationKey", federationKey, Critere.EQUALS));
         return (Individual) crit.uniqueResult();
     }
 
     @Override
     public boolean hasSimilarIndividuals(String firstName, String lastName, String email,
         String phone, String streetNumber, String streetName, String postalCode, String city) {
         Query query = HibernateUtil.getSession().createSQLQuery(
             "select count(*) from adult a right outer join individual i using(id) left outer join child c using(id)" +
             "where first_name = :firstName and last_name = :lastName and (" +
             "  email = :email or home_phone = :phone or (" +
             (streetNumber != null ?
                 "((select street_number from address where id = i.address_id) is null or (select street_number from address where id = i.address_id) = :streetNumber) and "
                     : "") +
             "    (select street_name from address where id = i.address_id) = :streetName" +
             "    and (select postal_code from address where id = i.address_id) = :postalCode" +
             "    and (select city from address where id = i.address_id) = :city" +
             "));");
         query.setString("firstName", firstName);
         query.setString("lastName", lastName);
         query.setString("email", email);
         query.setString("phone", phone);
         if (streetNumber != null) query.setString("streetNumber", streetNumber);
         query.setString("streetName", streetName);
         query.setString("postalCode", postalCode);
         query.setString("city", city);
         return !BigInteger.ZERO.equals(query.uniqueResult());
     }
 
     public List<Individual> search(final Set<Critere> criteria, final String orderedBy, 
             final UserState[] excludedStates) {
 
         StringBuffer sb = new StringBuffer();
         sb.append("from Individual as individual").append(" where 1 = 1 ");
 
         List<Type> typeList = new ArrayList<Type>();
         List<Object> objectList = new ArrayList<Object>();
 
         // go through all the criteria and create the query
         for (Critere searchCrit : criteria) {
             if (searchCrit.getAttribut().equals(Individual.SEARCH_BY_LASTNAME)) {
                 sb.append(" and lower(individual.lastName) " + searchCrit.getSqlComparatif() + " lower(?)");
                 objectList.add(searchCrit.getSqlStringValue());
                 typeList.add(Hibernate.STRING);
             } else if (searchCrit.getAttribut().equals(Individual.SEARCH_BY_FIRSTNAME)) {
                 sb.append(" and lower(individual.firstName) " + searchCrit.getSqlComparatif() + " lower(?)");
                 objectList.add(searchCrit.getSqlStringValue());
                 typeList.add(Hibernate.STRING);
             } else if (searchCrit.getAttribut().equals(Individual.SEARCH_BY_BIRTHDATE)) {
                 sb.append(" and individual.birthDate " + searchCrit.getSqlComparatif() + " ?");
                 objectList.add(searchCrit.getDateValue());
                 typeList.add(Hibernate.DATE);
             } else if (searchCrit.getAttribut().equals(Individual.SEARCH_BY_HOME_FOLDER_ID)) {
                 sb.append(" and individual.homeFolder.id " + searchCrit.getSqlComparatif() + " ?");
                 objectList.add(searchCrit.getLongValue());
                 typeList.add(Hibernate.LONG);
             } else {
                 logger.warn("Unknown search criteria for Individual object");
             }
         }
 
         if (excludedStates != null && excludedStates.length > 0) {
             for (int i = 0; i < excludedStates.length; i++) {
                 sb.append(" and individual.homeFolder.state != ?");
                 objectList.add(excludedStates[i].toString());
                 typeList.add(Hibernate.STRING);
             }
         }
 
 //        if (orderedBy != null) {
 //            if (orderedBy.equals(Individual.ORDER_BY_LASTNAME))
 //                sb.append(" order by individual.lastName");
 //            else
 //                sb.append(" order by individual.id");
 //        } else {
 //            // default sort order
 //            sb.append(" order by individual.id");
 //        }
 
         Type[] typeTab = typeList.toArray(new Type[0]);
         Object[] objectTab = objectList.toArray(new Object[0]);
         return HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setParameters(objectTab, typeTab)
             .list();
     }
 
     public List<Individual> listByHomeFolder(Long homeFolderId) {
         StringBuffer sb = new StringBuffer();
         sb.append("from Individual as individual")
             .append(" where individual.homeFolder = ?");
         return HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setLong(0, homeFolderId.longValue())
             .list();
     }
 
     public List<String> getSimilarLogins(final String baseLogin) {
 
         StringBuffer sb = new StringBuffer().append("select individual.login ")
             .append(" from Individual as individual").append(" where individual.login like ?");
 
         Query query = HibernateUtil.getSession().createQuery(sb.toString());
         query.setString(0, baseLogin + "%");
         return query.list();
     }
 
     @Override
     public List<Individual> listByHomeFolderRole(Long homeFolderId, RoleType role) {
 
         StringBuffer sb = new StringBuffer();
         sb.append("select individual from Individual as individual")
             .append(" join individual.individualRoles individualRole ")
             .append(" where individualRole.homeFolderId = ?");
         if (role != null)
             sb.append(" and individualRole.role = ?");
         
         Query query = HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setLong(0, homeFolderId);
         if (role != null)
             query.setString(1, role.toString());
         
         return query.list();
     }
     
     @Override
     public List<Individual> listByHomeFolderRoles(Long homeFolderId, RoleType[] roles, 
             boolean onlyExternals) {
 
         StringBuffer sb = new StringBuffer();
         sb.append("select individual from Individual as individual")
             .append(" join individual.individualRoles individualRole ")
             .append(" where individualRole.homeFolderId = ?");
         
         sb.append(" and (");
         for (int i = 0; i < roles.length; i++) {
             sb.append(" individualRole.role = ? ");
             if (i < roles.length - 1)
                 sb.append(" or ");
         }
         sb.append(")");
         
         if (onlyExternals)
             sb.append(" and individual.homeFolder = null");
         
         Query query = HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setLong(0, homeFolderId);
 
         for (int i = 0; i < roles.length; i++) {
             query.setString(i + 1, roles[i].toString());
         }
         
         return query.list();
     }
 
     @Override
     public List<Individual> listBySubjectRole(Long subjectId, RoleType role) {
         StringBuffer sb = new StringBuffer();
         sb.append("select individual from Individual as individual")
             .append(" join individual.individualRoles individualRole ")
             .append(" where individualRole.individualId = ?");
         if (role != null)
             sb.append(" and individualRole.role = ?");
         
         Query query = HibernateUtil.getSession()
             .createQuery(sb.toString())
             .setLong(0, subjectId);
         if (role != null)
             query.setString(1, role.toString());
         
         return query.list();
     }
 
     @Override
     public List<Individual> listBySubjectRoles(Long subjectId, RoleType[] roles, boolean onlyExternals) {
         StringBuffer sb = new StringBuffer(
             "select * from individual where id in (select owner_id from individual_role where individual_id = ? and (");
         for (int i = 0; i < roles.length; i++) {
             sb.append(" role = ? ");
             if (i < roles.length - 1)
                 sb.append(" or ");
         }
         sb.append("))");
 
         if (onlyExternals)
             sb.append(" and home_folder_id is null");
 
         Query query = HibernateUtil.getSession().createSQLQuery(sb.toString())
             .addEntity(Individual.class).setLong(0, subjectId);
 
         for (int i = 0; i < roles.length; i++) {
             query.setString(i + 1, roles[i].toString());
         }
 
         return query.list();
     }
     
     public List<Individual> search(Set<Critere> criterias, Map<String,String>sortParams,
                                     Integer max, Integer offset) {
         List<Type> types = new ArrayList<Type>();
         List<Object> values = new ArrayList<Object>();
         
         StringBuffer sb = this.buildStatement(criterias,types,values,"select distinct individual ");
         this.buildSort(sortParams,sb);
         return this.execute(sb.toString(),types,values,max,offset);
     }
     
     public Integer searchCount(Set<Critere> criterias) {
         List<Type> types = new ArrayList<Type>();
         List<Object> values = new ArrayList<Object>();
         
         StringBuffer sb = this.buildStatement(criterias,types,values,"select count(distinct individual.id) ");
         return  (this.<Long>execute(sb.toString(),types,values)).intValue();
     }
     
     protected StringBuffer buildStatement(Set<Critere> criterias,List<Type> typeList,
                                   List<Object> objectList,String prefix) {
         StringBuffer sb = new StringBuffer();
         
         sb.append(prefix != null ? prefix : "") 
             .append("from Individual as individual left join individual.individualRoles roles")
             .append(" where 1 = 1 ");
         
         // we filter the individual who don't get a home folder
         sb.append(" and individual.homeFolder is not null ");

        // TODO (rdj) : I know this is apoor solution
        boolean searchByUserState = false;
         // go through all the criteria and create the query
         for (Critere criteria : criterias) {
             if (criteria.getAttribut().equals(Individual.SEARCH_BY_LASTNAME)) {
                 sb.append(" and lower(individual.lastName) ")
                     .append(criteria.getSqlComparatif())
                     .append(" lower(?)");
                 objectList.add(criteria.getSqlStringValue());
                 typeList.add(Hibernate.STRING);
             } else if(criteria.getAttribut().equals(Individual.SEARCH_BY_FIRSTNAME)) {
                 sb.append(" and lower(individual.firstName) ")
                     .append(criteria.getSqlComparatif())
                     .append(" lower(?)");
                 objectList.add(criteria.getSqlStringValue());
                 typeList.add(Hibernate.STRING);
             } else if (criteria.getAttribut().equals(Individual.SEARCH_BY_HOME_FOLDER_ID)) {
                 sb.append(" and individual.homeFolder.id ")
                     .append(criteria.getSqlComparatif())
                     .append(" ?");
                 objectList.add(criteria.getLongValue());
                 typeList.add(Hibernate.LONG);
             } else if(criteria.getAttribut().equals(Individual.SEARCH_BY_INDIVIDUAL_ID)) {
                 sb.append(String.format(" and individual.id %1$s ? ",criteria.getSqlComparatif()));
                 objectList.add(criteria.getLongValue());
                 typeList.add(Hibernate.LONG);
             } else if(criteria.getAttribut().equals(Individual.SEARCH_BY_HOME_FOLDER_STATE)) {
                 sb.append(String.format(" and lower(individual.homeFolder.state) %1$s lower(?) ",criteria.getSqlComparatif()));
                 objectList.add(criteria.getValue());
                 typeList.add(Hibernate.STRING);
             } else if(criteria.getAttribut().equals(Individual.SEARCH_IS_HOME_FOLDER_RESPONSIBLE)) {
                 sb.append(" and roles.role = 'HomeFolderResponsible' ");
             } else if (Individual.SEARCH_BY_USER_STATE.equals(criteria.getAttribut())) {
                 sb.append(String.format(" and lower(individual.state) %1$s lower(?) ", criteria.getSqlComparatif()));
                 objectList.add(criteria.getValue());
                 typeList.add(Hibernate.STRING);
                searchByUserState = true;
             } else {
                 logger.warn("Unknown search criteria for Individual object");
             }
         }

        // exclude archived individual if no UserState filter is used
        if (!searchByUserState) {
            sb.append(" and individual.state != ?");
            objectList.add(UserState.ARCHIVED.toString());
            typeList.add(Hibernate.STRING);
        }

         return sb;
     }
 
     @Override
     public List<Individual> listTasks(QoS qoS, int max) {
         Query query = HibernateUtil.getSession()
             .createQuery("from Individual i where i.qoS = :qoS order by i.lastModificationDate")
             .setString("qoS", qoS.toString());
         if (max > 0)
             query.setMaxResults(max);
         return query.list();
     }
 
     @Override
     public Long countTasks(QoS qoS) {
         return (Long)HibernateUtil.getSession()
             .createQuery("select count(*) from Individual i where i.qoS = :qoS")
             .setString("qoS", qoS.toString())
             .iterate().next();
     }
 
     @Override
     public List<Individual> searchTasks(Date date) {
         return HibernateUtil.getSession()
             .createQuery("from Individual i where i.state in (:new, :modified, :invalid) and (i.lastModificationDate is null or i.lastModificationDate <= :limitDate) order by i.lastModificationDate")
             .setString("new", UserState.NEW.toString())
             .setString("modified", UserState.MODIFIED.toString())
             .setString("invalid", UserState.INVALID.toString())
             .setTimestamp("limitDate", date)
             .list();
     }
 }
