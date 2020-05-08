 /*******************************************************************************
  * Copyright (c) 2012, All Rights Reserved.
  * 
  * Generation Challenge Programme (GCP)
  * 
  * 
  * This software is licensed for use under the terms of the GNU General Public
  * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
  * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
  * 
  *******************************************************************************/
 
 package org.generationcp.middleware.manager;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import org.generationcp.middleware.dao.AttributeDAO;
 import org.generationcp.middleware.dao.BibrefDAO;
 import org.generationcp.middleware.dao.CountryDAO;
 import org.generationcp.middleware.dao.GermplasmDAO;
 import org.generationcp.middleware.dao.LocationDAO;
 import org.generationcp.middleware.dao.MethodDAO;
 import org.generationcp.middleware.dao.NameDAO;
 import org.generationcp.middleware.dao.ProgenitorDAO;
 import org.generationcp.middleware.dao.UserDefinedFieldDAO;
 import org.generationcp.middleware.exceptions.QueryException;
 import org.generationcp.middleware.manager.api.GermplasmDataManager;
 import org.generationcp.middleware.pojos.Attribute;
 import org.generationcp.middleware.pojos.Bibref;
 import org.generationcp.middleware.pojos.Country;
 import org.generationcp.middleware.pojos.Germplasm;
 import org.generationcp.middleware.pojos.GermplasmPedigreeTree;
 import org.generationcp.middleware.pojos.GermplasmPedigreeTreeNode;
 import org.generationcp.middleware.pojos.GidNidElement;
 import org.generationcp.middleware.pojos.Location;
 import org.generationcp.middleware.pojos.Method;
 import org.generationcp.middleware.pojos.Name;
 import org.generationcp.middleware.pojos.Progenitor;
 import org.generationcp.middleware.pojos.ProgenitorPK;
 import org.generationcp.middleware.pojos.UserDefinedField;
 import org.generationcp.middleware.util.HibernateUtil;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 /**
  * Implementation of the GermplasmDataManager interface. To instantiate this
  * class, a Hibernate Session must be passed to its constructor.
  * 
  * @author Kevin Manansala, Lord Hendrix Barboza
  * 
  */
 public class GermplasmDataManagerImpl extends DataManager implements GermplasmDataManager {
 	
     public GermplasmDataManagerImpl(HibernateUtil hibernateUtilForLocal, HibernateUtil hibernateUtilForCentral) {
         super(hibernateUtilForLocal, hibernateUtilForCentral);
     }
     
     public List<Location> getAllLocations(int start, int numOfRows) throws QueryException {
         LocationDAO dao = new LocationDAO();
 
         List<Location> locations = new ArrayList<Location>();
         
         int centralCount = 0;
         int localCount = 0;
         int relativeLimit = 0;
         
         if (hibernateUtilForCentral != null) {
         	
             dao.setSession(hibernateUtilForCentral.getCurrentSession());
             centralCount = dao.countAll().intValue();
             
             if (centralCount > start) {
             	
             	locations.addAll(dao.getAll(start, numOfRows));
             	
             	relativeLimit = numOfRows - (centralCount - start);
                 
                 if (relativeLimit > 0) {
 
         	        if (hibernateUtilForLocal != null) {
         	        	
         	            dao.setSession(hibernateUtilForLocal.getCurrentSession());
         	            
         	            localCount = dao.countAll().intValue();
         	            
         	            if (localCount > 0) {
         	            	
         	            	locations.addAll(dao.getAll(0, relativeLimit));
         	            	
         	            }  
         	        }
                 }
                 
             } else {
             	
             	relativeLimit = start - centralCount;
             	
     	        if (hibernateUtilForLocal != null) {
     	        	
     	            dao.setSession(hibernateUtilForLocal.getCurrentSession());
     	            
     	            localCount = dao.countAll().intValue();
     	            
     	            if (localCount > relativeLimit) {
     	            	
     	            	locations.addAll(dao.getAll(relativeLimit, numOfRows));
     	            	
     	            }  
     	        }
             }
             
         } else if (hibernateUtilForLocal != null) {
         	
         	dao.setSession(hibernateUtilForLocal.getCurrentSession());
         	
             localCount = dao.countAll().intValue();
             
             if (localCount > start) {
             	
             	locations.addAll(dao.getAll(start, numOfRows));
             	
             }  
         }
         
         return locations;
     }
 
     public int countAllLocations() throws QueryException {
     	
         int count = 0;
         
         LocationDAO dao = new LocationDAO();
         
         if (hibernateUtilForLocal != null) {
             dao.setSession(hibernateUtilForLocal.getCurrentSession());
             count = count + dao.countAll().intValue();
         }
         
         if (hibernateUtilForCentral != null) {
             dao.setSession(hibernateUtilForCentral.getCurrentSession());
             count = count + dao.countAll().intValue();
         }
         
         return count;
     } 
     
     public List<Location> findLocationByName(String name, int start, int numOfRows, Operation op) throws QueryException {
 
         LocationDAO dao = new LocationDAO();
         
         if (hibernateUtilForLocal != null) {
             dao.setSession(hibernateUtilForLocal.getCurrentSession());
         }
         List<Location> locations = dao.findByName(name, start, numOfRows, op);
         
         // get the list of Location from the central instance
         if (hibernateUtilForCentral != null) {
             dao.setSession(hibernateUtilForCentral.getCurrentSession());
         }
         
         List<Location> centralLocations =  dao.findByName(name, start, numOfRows, op);
         
         locations.addAll(centralLocations);
         
         return locations;
 
     }
 
     public int countLocationByName(String name, Operation op) throws QueryException {
     	
     	int count = 0;
     	
         LocationDAO dao = new LocationDAO();
         
         if (hibernateUtilForLocal != null) {
             dao.setSession(hibernateUtilForLocal.getCurrentSession());
         }
         count = dao.countByName(name, op).intValue();
         
         // get the list of Location from the central instance
         if (hibernateUtilForCentral != null) {
             dao.setSession(hibernateUtilForCentral.getCurrentSession());
         }
         
         count = count + dao.countByName(name, op).intValue();
         
         return count;
 
     }
 
     public List<Germplasm> findAllGermplasm(int start, int numOfRows, Database instance) throws QueryException {
         GermplasmDAO dao = new GermplasmDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(instance);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return new ArrayList<Germplasm>();
         }
 
         return (List<Germplasm>) dao.findAll(start, numOfRows);
     }
 
     public int countAllGermplasm(Database instance) throws QueryException {
         int count = 0;
         GermplasmDAO dao = new GermplasmDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(instance);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
             count = count + dao.countAll().intValue();
         }
 
         return count;
     }
 
     public List<Germplasm> findGermplasmByPrefName(String name, int start, int numOfRows, Database instance) throws QueryException {
         GermplasmDAO dao = new GermplasmDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(instance);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return new ArrayList<Germplasm>();
         }
 
         return (List<Germplasm>) dao.findByPrefName(name, start, numOfRows);
     }
 
     public int countGermplasmByPrefName(String name) throws QueryException {
         int count = 0;
 
         if (this.hibernateUtilForLocal != null) {
             GermplasmDAO dao = new GermplasmDAO();
             dao.setSession(hibernateUtilForLocal.getCurrentSession());
             count = count + dao.countByPrefName(name).intValue();
         }
 
         if (this.hibernateUtilForCentral != null) {
             GermplasmDAO centralDao = new GermplasmDAO();
             centralDao.setSession(hibernateUtilForCentral.getCurrentSession());
             count = count + centralDao.countByPrefName(name).intValue();
         }
 
         return count;
     }
 
     @Override
     public List<Germplasm> findGermplasmByName(String name, int start, int numOfRows, FindGermplasmByNameModes mode, Operation op,
             Integer status, GermplasmNameType type, Database instance) throws QueryException {
         GermplasmDAO dao = new GermplasmDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(instance);
 
         List<Germplasm> germplasms = new ArrayList<Germplasm>();
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return germplasms;
         }
 
         // do string manipulation on name parameter depending on
         // FindGermplasmByNameModes parameter
         String nameToUse = "";
         if (mode == FindGermplasmByNameModes.NORMAL) {
             nameToUse = name;
         } else if (mode == FindGermplasmByNameModes.SPACES_REMOVED) {
             StringTokenizer tokenizer = new StringTokenizer(name);
             StringBuffer nameWithSpacesRemoved = new StringBuffer();
             while (tokenizer.hasMoreTokens()) {
                 nameWithSpacesRemoved.append(tokenizer.nextToken());
             }
             nameToUse = nameWithSpacesRemoved.toString();
         } else if (mode == FindGermplasmByNameModes.STANDARDIZED) {
             String standardizedName = GermplasmDataManagerImpl.standardaizeName(name);
             nameToUse = standardizedName;
         }
 
         germplasms = dao.findByName(nameToUse, start, numOfRows, op, status, type);
         return germplasms;
     }
 
     @Override
     public int countGermplasmByName(String name, FindGermplasmByNameModes mode, Operation op, Integer status, GermplasmNameType type,
             Database instance) throws QueryException {
         // do string manipulation on name parameter depending on
         // FindGermplasmByNameModes parameter
         String nameToUse = "";
         if (mode == FindGermplasmByNameModes.NORMAL) {
             nameToUse = name;
         } else if (mode == FindGermplasmByNameModes.SPACES_REMOVED) {
             StringTokenizer tokenizer = new StringTokenizer(name);
             StringBuffer nameWithSpacesRemoved = new StringBuffer();
             while (tokenizer.hasMoreTokens()) {
                 nameWithSpacesRemoved.append(tokenizer.nextToken());
             }
             nameToUse = nameWithSpacesRemoved.toString();
         } else if (mode == FindGermplasmByNameModes.STANDARDIZED) {
             String standardizedName = GermplasmDataManagerImpl.standardaizeName(name);
             nameToUse = standardizedName;
         }
 
         int count = 0;
 
         GermplasmDAO dao = new GermplasmDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(instance);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return 0;
         }
 
         count = dao.countByName(nameToUse, op, status, type).intValue();
         return count;
     }
 
     @Override
     public List<Germplasm> findGermplasmByLocationName(String name, int start, int numOfRows, Operation op, Database instance)
             throws QueryException {
         GermplasmDAO dao = new GermplasmDAO();
         List<Germplasm> germplasms = new ArrayList<Germplasm>();
         HibernateUtil hibernateUtil = getHibernateUtil(instance);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return germplasms;
         }
 
         if (op == Operation.EQUAL) {
             germplasms = dao.findByLocationNameUsingEqual(name, start, numOfRows);
         } else if (op == Operation.LIKE) {
             germplasms = dao.findByLocationNameUsingLike(name, start, numOfRows);
         }
 
         return germplasms;
     }
 
     @Override
     public int countGermplasmByLocationName(String name, Operation op, Database instance) throws QueryException {
         GermplasmDAO dao = new GermplasmDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(instance);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return 0;
         }
 
         Long count = null;
         if (op == Operation.EQUAL) {
             count = dao.countByLocationNameUsingEqual(name);
         } else if (op == Operation.LIKE) {
             count = dao.countByLocationNameUsingLike(name);
         }
 
         if (count != null) {
             return count.intValue();
         } else {
             throw new QueryException("BigInteger object returned by DAO was null.");
         }
     }
 
     @Override
     public List<Germplasm> findGermplasmByMethodName(String name, int start, int numOfRows, Operation op, Database instance)
             throws QueryException {
         GermplasmDAO dao = new GermplasmDAO();
         List<Germplasm> germplasms = null;
         HibernateUtil hibernateUtil = getHibernateUtil(instance);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return germplasms;
         }
 
         if (op == Operation.EQUAL) {
             germplasms = dao.findByMethodNameUsingEqual(name, start, numOfRows);
         } else if (op == Operation.LIKE) {
             germplasms = dao.findByMethodNameUsingLike(name, start, numOfRows);
         }
 
         return germplasms;
     }
 
     @Override
     public int countGermplasmByMethodName(String name, Operation op, Database instance) throws QueryException {
         GermplasmDAO dao = new GermplasmDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(instance);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return 0;
         }
 
         Long count = null;
 
         if (op == Operation.EQUAL) {
             count = dao.countByMethodNameUsingEqual(name);
         } else if (op == Operation.LIKE) {
             count = dao.countByMethodNameUsingLike(name);
         }
 
         if (count != null) {
             return count.intValue();
         } else {
             throw new QueryException("BigInteger object returned by DAO was null.");
         }
     }
 
     @Override
     public Germplasm getGermplasmByGID(Integer gid) {
         GermplasmDAO dao = new GermplasmDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(gid);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return null;
         }
 
         return (Germplasm) dao.findById(gid, false);
     }
 
     @Override
     public Germplasm getGermplasmWithPrefName(Integer gid) throws QueryException {
         GermplasmDAO dao = new GermplasmDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(gid);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return null;
         }
 
         return (Germplasm) dao.getByGIDWithPrefName(gid);
     }
 
     @Override
     public Germplasm getGermplasmWithPrefAbbrev(Integer gid) throws QueryException {
         GermplasmDAO dao = new GermplasmDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(gid);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return null;
         }
 
         return (Germplasm) dao.getByGIDWithPrefAbbrev(gid);
     }
 
     @Override
     public Name getGermplasmNameByID(Integer id) {
         NameDAO dao = new NameDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(id);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return null;
         }
 
         return (Name) dao.findById(id, false);
     }
 
     @Override
     public List<Name> getNamesByGID(Integer gid, Integer status, GermplasmNameType type) throws QueryException {
         NameDAO dao = new NameDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(gid);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return new ArrayList<Name>();
         }
 
         return (List<Name>) dao.getByGIDWithFilters(gid, status, type); //names
     }
 
     @Override
     public Name getPreferredNameByGID(Integer gid) throws QueryException {
         NameDAO dao = new NameDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(gid);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return null;
         }
 
         List<Name> names = dao.getByGIDWithFilters(gid, 1, null);
         if (!names.isEmpty()) {
             return names.get(0);
         } else {
             return null;
         }
     }
 
     @Override
     public Name getPreferredAbbrevByGID(Integer gid) throws QueryException {
         NameDAO dao = new NameDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(gid);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return null;
         }
 
         List<Name> names = dao.getByGIDWithFilters(gid, 2, null);
         if (!names.isEmpty()) {
             return names.get(0);
         } else {
             return null;
         }
     }
 
     @Override
     public Name getNameByGIDAndNval(Integer gid, String nval) throws QueryException {
         NameDAO dao = new NameDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(gid);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return null;
         }
 
         return dao.getByGIDAndNval(gid, nval);
     }
 
     @Override
     public void updateGermplasmPrefName(Integer gid, String newPrefName) throws QueryException {
         updateGermplasmPrefNameAbbrev(gid, newPrefName, "Name");
     }
 
     @Override
     public void updateGermplasmPrefAbbrev(Integer gid, String newPrefAbbrev) throws QueryException {
         updateGermplasmPrefNameAbbrev(gid, newPrefAbbrev, "Abbreviation");
     }
 
     private void updateGermplasmPrefNameAbbrev(Integer gid, String newPrefValue, String nameOrAbbrev) throws QueryException {
         if (hibernateUtilForLocal == null) {
             throw new QueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // initialize session & transaction
         Session session = hibernateUtilForLocal.getCurrentSession();
         Transaction trans = null;
 
         try {
             // begin update transaction
             trans = session.beginTransaction();
             NameDAO dao = new NameDAO();
             dao.setSession(session);
 
             // check for a name record with germplasm = gid, and nval =
             // newPrefName
             Name newPref = getNameByGIDAndNval(gid, newPrefValue);
             // if a name record with the specified nval exists,
             if (newPref != null) {
                 // get germplasm's existing preferred name/abbreviation, set as
                 // alternative name, change nstat to 0
                 Name oldPref = null;
                 int newNstat = 0; // nstat to be assigned to newPref: 1 for
                 // Name, 2 for Abbreviation
 
                 if ("Name".equals(nameOrAbbrev)) {
                     oldPref = getPreferredNameByGID(gid);
                     newNstat = 1;
                 } else if ("Abbreviation".equals(nameOrAbbrev)) {
                     oldPref = getPreferredAbbrevByGID(gid);
                     newNstat = 2;
                 }
 
                 if (oldPref != null) {
                     oldPref.setNstat(0);
                     dao.validateId(oldPref); // check if old Name is a local DB
                     // record
                     dao.saveOrUpdate(oldPref);
                 }
 
                 newPref.setNstat(newNstat); // update specified name as the new
                 // preferred name/abbreviation
                 dao.validateId(newPref); // check if new Name is a local DB
                 // record
                 dao.saveOrUpdate(newPref); // save the new name's status to the
                 // database
             } else {
                 // throw exception if no Name record with specified value does
                 // not exist
                 throw new QueryException("The specified Germplasm Name does not exist.");
             }
 
             // end transaction, commit to database
             trans.commit();
         } catch (Exception ex) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new QueryException("Error in updating Germplasm Preferred " + nameOrAbbrev + ": " + ex.getMessage(), ex);
         } finally {
             hibernateUtilForLocal.closeCurrentSession();
         }
     }
 
     @Override
     public int addGermplasmName(Name name) throws QueryException {
         List<Name> names = new ArrayList<Name>();
         names.add(name);
         return addOrUpdateGermplasmName(names, Operation.ADD);
     }
 
     @Override
     public int addGermplasmName(List<Name> names) throws QueryException {
         return addOrUpdateGermplasmName(names, Operation.ADD);
     }
 
     @Override
     public int updateGermplasmName(Name name) throws QueryException {
         List<Name> names = new ArrayList<Name>();
         names.add(name);
         return addOrUpdateGermplasmName(names, Operation.UPDATE);
     }
 
     @Override
     public int updateGermplasmName(List<Name> names) throws QueryException {
         return addOrUpdateGermplasmName(names, Operation.UPDATE);
     }
 
     private int addOrUpdateGermplasmName(List<Name> names, Operation operation) throws QueryException {
         if (hibernateUtilForLocal == null) {
             throw new QueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // initialize session & transaction
         Session session = hibernateUtilForLocal.getCurrentSession();
         Transaction trans = null;
 
         int namesSaved = 0;
         try {
             // begin save transaction
             trans = session.beginTransaction();
             NameDAO dao = new NameDAO();
             dao.setSession(session);
 
             for (Name name : names) {
                 if (operation == Operation.ADD) {
                     // Auto-assign negative IDs for new local DB records
                     Integer negativeId = dao.getNegativeId("nid");
                     name.setNid(negativeId);
                 } else if (operation == Operation.UPDATE) {
                     // Check if Name is a local DB record. Throws exception if
                     // Name is a central DB record.
                     dao.validateId(name);
                 }
                 dao.saveOrUpdate(name);
                 namesSaved++;
                 if (namesSaved % JDBC_BATCH_SIZE == 0) {
                     // flush a batch of inserts and release memory
                     dao.flush();
                     dao.clear();
                 }
             }
             // end transaction, commit to database
             trans.commit();
         } catch (Exception ex) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new QueryException("Error encountered while saving Germplasm Name: " + ex.getMessage(), ex);
         } finally {
             hibernateUtilForLocal.closeCurrentSession();
         }
 
         return namesSaved;
     }
 
     @Override
     public List<Attribute> getAttributesByGID(Integer gid) throws QueryException {
         AttributeDAO dao = new AttributeDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(gid);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return new ArrayList<Attribute>();
         }
         return (List<Attribute>) dao.getByGID(gid); //attributes
     }
 
     @Override
     public Method getMethodByID(Integer id) {
         MethodDAO dao = new MethodDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(id);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return null;
         }
 
         return (Method) dao.findById(id, false);
     }
 
     @Override
     public List<Method> getAllMethods() throws QueryException {
         List<Method> methods = new ArrayList<Method>();
 
         if (this.hibernateUtilForLocal != null) {
             MethodDAO dao = new MethodDAO();
             dao.setSession(hibernateUtilForLocal.getCurrentSession());
             methods.addAll(dao.getAllMethod());
         }
 
         if (this.hibernateUtilForCentral != null) {
             MethodDAO centralDao = new MethodDAO();
             centralDao.setSession(hibernateUtilForCentral.getCurrentSession());
             methods.addAll(centralDao.getAllMethod());
         }
 
         return methods;
 
     }
     
     @Override
     public int addMethod(Method method) throws QueryException {
         requireLocalDatabaseInstance();
 
         // initialize session & transaction
         Session session = hibernateUtilForLocal.getCurrentSession();
         Transaction trans = null;
 
         int methodsSaved = 0;
         try {
             // begin save transaction
             trans = session.beginTransaction();
             MethodDAO dao = new MethodDAO();
             dao.setSession(session);
 
             // Auto-assign negative IDs for new local DB records
             Integer negativeId = dao.getNegativeId("mid");
             method.setMid(negativeId);
 
             dao.saveOrUpdate(method);
             methodsSaved++;
 
             trans.commit();
         } catch (Exception ex) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new QueryException("Error encountered while saving Method: " + ex.getMessage(), ex);
         } finally {
             hibernateUtilForLocal.closeCurrentSession();
         }
 
         return methodsSaved;
     }
     
     @Override
     public int addMethod(List<Method> methods) throws QueryException {
         requireLocalDatabaseInstance();
 
         // initialize session & transaction
         Session session = hibernateUtilForLocal.getCurrentSession();
         Transaction trans = null;
 
         int methodsSaved = 0;
         try {
             // begin save transaction
             trans = session.beginTransaction();
             MethodDAO dao = new MethodDAO();
             dao.setSession(session);
 
             for (Method method : methods){
                 // Auto-assign negative IDs for new local DB records
                 Integer negativeId = dao.getNegativeId("mid");
                 method.setMid(negativeId);
     
                 dao.saveOrUpdate(method);
                 methodsSaved++;
             }
 
             trans.commit();
         } catch (Exception ex) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new QueryException("Error encountered while saving a list of Methods: " + ex.getMessage(), ex);
         } finally {
             hibernateUtilForLocal.closeCurrentSession();
         }
 
         return methodsSaved;
     }
     
     @Override
     public void deleteMethod(Method method) throws QueryException {
         requireLocalDatabaseInstance();
         
         Session session = hibernateUtilForLocal.getCurrentSession();
         Transaction trans = null;
         
         try {
             // begin save transaction
             trans = session.beginTransaction();
             
             MethodDAO dao = new MethodDAO();
             dao.setSession(session);
             
             dao.makeTransient(method);
             
             trans.commit();
         } catch (Exception ex) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new QueryException("Error encountered while deleting Method: " + ex.getMessage(), ex);
         } finally {
             hibernateUtilForLocal.closeCurrentSession();
         }
     }
 
     @Override
     public UserDefinedField getUserDefinedFieldByID(Integer id) {
         UserDefinedFieldDAO dao = new UserDefinedFieldDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(id);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return null;
         }
 
         return (UserDefinedField) dao.findById(id, false);
     }
 
     @Override
     public Country getCountryById(Integer id) {
         CountryDAO dao = new CountryDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(id);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return null;
         }
 
         Country country = dao.findById(id, false);
         return country;
     }
 
     @Override
     public Location getLocationByID(Integer id) {
         LocationDAO dao = new LocationDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(id);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return null;
         }
 
         Location location = dao.findById(id, false);
         return location;
 
     }
 
     @Override
     public int addLocation(Location location) throws QueryException {
         if (hibernateUtilForLocal == null) {
             throw new QueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // initialize session & transaction
         Session session = hibernateUtilForLocal.getCurrentSession();
         Transaction trans = null;
 
         int locationsSaved = 0;
         try {
             // begin save transaction
             trans = session.beginTransaction();
             LocationDAO dao = new LocationDAO();
             dao.setSession(session);
 
             // Auto-assign negative IDs for new local DB records
             Integer negativeId = dao.getNegativeId("locid");
             location.setLocid(negativeId);
 
             dao.saveOrUpdate(location);
             locationsSaved++;
 
             trans.commit();
         } catch (Exception ex) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new QueryException("Error encountered while saving Location: " + ex.getMessage(), ex);
         } finally {
             hibernateUtilForLocal.closeCurrentSession();
         }
 
         return locationsSaved;
     }
     
     @Override
     public int addLocation(List<Location> locations) throws QueryException {
         if (hibernateUtilForLocal == null) {
             throw new QueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // initialize session & transaction
         Session session = hibernateUtilForLocal.getCurrentSession();
         Transaction trans = null;
 
         int locationsSaved = 0;
         try {
             // begin save transaction
             trans = session.beginTransaction();
             
             LocationDAO dao = new LocationDAO();
             dao.setSession(session);
             
             for (Location location : locations) {
 
                 // Auto-assign negative IDs for new local DB records
                 Integer negativeId = dao.getNegativeId("locid");
                 location.setLocid(negativeId);
  
                 dao.saveOrUpdate(location);
                 locationsSaved++;
             }
 
             trans.commit();
         } catch (Exception ex) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new QueryException("Error encountered while saving Locations: " + ex.getMessage(), ex);
         } finally {
             hibernateUtilForLocal.closeCurrentSession();
         }
 
         return locationsSaved;
     }
 
     @Override
     public Bibref getBibliographicReferenceByID(Integer id) {
         BibrefDAO dao = new BibrefDAO();
         if (id < 0 && this.hibernateUtilForLocal != null) {
             dao.setSession(hibernateUtilForLocal.getCurrentSession());
         } else if (id > 0 && this.hibernateUtilForCentral != null) {
             dao.setSession(hibernateUtilForCentral.getCurrentSession());
         } else {
             return null;
         }
 
         Bibref bibref = dao.findById(id, false);
         return bibref;
     }
 
     @Override
     public int addBibliographicReference(Bibref bibref) throws QueryException {
         if (hibernateUtilForLocal == null) {
             throw new QueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // initialize session & transaction
         Session session = hibernateUtilForLocal.getCurrentSession();
         Transaction trans = null;
 
         int bibrefSaved = 0;
         try {
             // begin save transaction
             trans = session.beginTransaction();
             BibrefDAO dao = new BibrefDAO();
             dao.setSession(session);
 
             // Auto-assign negative IDs for new local DB records
             Integer negativeId = dao.getNegativeId("refid");
             bibref.setRefid(negativeId);
 
             dao.saveOrUpdate(bibref);
             bibrefSaved++;
 
             trans.commit();
         } catch (Exception ex) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new QueryException("Error encountered while saving Bibliographic Reference: " + ex.getMessage(), ex);
         } finally {
             hibernateUtilForLocal.closeCurrentSession();
         }
 
         return bibrefSaved;
     }
 
     /**
      * Given a germplasm name, apply the standardization procedure to it.
      * 
      * (L= any letter; ^= space; N= any numeral, S= any of {-,',[,],+,.}) a)
      * Capitalize all letters Khao-Dawk-Mali105 becomes KHAO-DAWK-MALI105 b) L(
      * becomes L^( and )L becomes )^L IR64(BPH) becomes IR64 (BPH) c) N( becomes
      * N^( and )N becomes )^N IR64(5A) becomes IR64 (5A) d) L. becomes L^ IR 63
      * SEL. becomes IR 64 SEL e) LN becomes L^N EXCEPT SLN MALI105 becomes MALI
      * 105 but MALI-F4 IS unchanged f) NL becomes N^L EXCEPT SNL B 533A-1
      * becomes B 533 A-1 but B 533 A-4B is unchanged g) LL-LL becomes LL^LL
      * KHAO-DAWK-MALI 105 becomes KHAO DAWK MALI 105 h) ^0N becomes ^N IRTP
      * 00123 becomes IRTP 123 i) ^^ becomes ^ j) REMOVE LEADING OR TRAILING ^ k)
      * ^) becomes ) and (^ becomes ( l) L-N becomes L^N when there is only one
      *  in the name and L is not preceded by a space m) ^/ becomes / and /^
      * becomes /
      * 
      * @param name
      * @return the standardized germplasm name
      */
     public static String standardaizeName(String name) {
         String toreturn = name.trim();
 
         // a) Capitalize all letters
         toreturn = toreturn.toUpperCase();
 
         int stringLength = toreturn.length();
         for (int ctr = 0; ctr < stringLength; ctr++) {
             char currentChar = toreturn.charAt(ctr);
             if (currentChar == '(') {
                 if (ctr - 1 >= 0) {
                     char previousChar = toreturn.charAt(ctr - 1);
                     // L( becomes L^( or N( becomes N^(
                     if (Character.isLetterOrDigit(previousChar)) {
                         String firstHalf = toreturn.substring(0, ctr);
                         String secondHalf = toreturn.substring(ctr);
                         toreturn = firstHalf + " " + secondHalf;
                         stringLength++;
                         continue;
                     }
                 }
 
                 if (ctr + 1 < stringLength) {
                     char nextChar = toreturn.charAt(ctr + 1);
                     // (^ becomes (
                     if (Character.isWhitespace(nextChar)) {
                         String firstHalf = toreturn.substring(0, ctr + 1);
                         String secondHalf = toreturn.substring(ctr + 2);
                         toreturn = firstHalf + secondHalf;
                         stringLength--;
                         continue;
                     }
                 }
             } else if (currentChar == ')') {
                 if (ctr - 1 >= 0) {
                     char previousChar = toreturn.charAt(ctr - 1);
                     // ^) becomes )
                     if (Character.isWhitespace(previousChar)) {
                         String firstHalf = toreturn.substring(0, ctr - 1);
                         String secondHalf = toreturn.substring(ctr);
                         toreturn = firstHalf + secondHalf;
                         stringLength--;
                         ctr--;
                         continue;
                     }
                 }
 
                 if (ctr + 1 < stringLength) {
                     char nextChar = toreturn.charAt(ctr + 1);
                     // )L becomes )^L or )N becomes )^N
                     if (Character.isLetterOrDigit(nextChar)) {
                         String firstHalf = toreturn.substring(0, ctr + 1);
                         String secondHalf = toreturn.substring(ctr + 1);
                         toreturn = firstHalf + " " + secondHalf;
                         stringLength++;
                         continue;
                     }
                 }
             } else if (currentChar == '.') {
                 if (ctr - 1 >= 0) {
                     char previousChar = toreturn.charAt(ctr - 1);
                     // L. becomes L^
                     if (Character.isLetter(previousChar)) {
                         if (ctr + 1 < stringLength) {
                             String firstHalf = toreturn.substring(0, ctr);
                             String secondHalf = toreturn.substring(ctr + 1);
                             toreturn = firstHalf + " " + secondHalf;
                             continue;
                         } else {
                             toreturn = toreturn.substring(0, ctr);
                             break;
                         }
                     }
                 }
             } else if (Character.isLetter(currentChar)) {
                 if (ctr + 1 < stringLength) {
                     char nextChar = toreturn.charAt(ctr + 1);
                     if (Character.isDigit(nextChar)) {
                         // LN becomes L^N EXCEPT SLN
                         // check if there is a special character before the
                         // letter
                         if (ctr - 1 >= 0) {
                             char previousChar = toreturn.charAt(ctr - 1);
                             if (GermplasmDataManagerImpl.isGermplasmNameSpecialChar(previousChar)) {
                                 continue;
                             }
                         }
 
                         String firstHalf = toreturn.substring(0, ctr + 1);
                         String secondHalf = toreturn.substring(ctr + 1);
                         toreturn = firstHalf + " " + secondHalf;
                         stringLength++;
                         continue;
                     }
                 }
             } else if (currentChar == '0') {
                 // ^0N becomes ^N
                 if (ctr - 1 >= 0 && ctr + 1 < stringLength) {
                     char nextChar = toreturn.charAt(ctr + 1);
                     char previousChar = toreturn.charAt(ctr - 1);
 
                     if (Character.isDigit(nextChar) && Character.isWhitespace(previousChar)) {
                         String firstHalf = toreturn.substring(0, ctr);
                         String secondHalf = toreturn.substring(ctr + 1);
                         toreturn = firstHalf + secondHalf;
                         stringLength--;
                         ctr--;
                         continue;
                     }
                 }
             } else if (Character.isDigit(currentChar)) {
                 if (ctr + 1 < stringLength) {
                     char nextChar = toreturn.charAt(ctr + 1);
                     if (Character.isLetter(nextChar)) {
                         // NL becomes N^L EXCEPT SNL
                         // check if there is a special character before the
                         // number
                         if (ctr - 1 >= 0) {
                             char previousChar = toreturn.charAt(ctr - 1);
                             if (GermplasmDataManagerImpl.isGermplasmNameSpecialChar(previousChar)) {
                                 continue;
                             }
                         }
 
                         String firstHalf = toreturn.substring(0, ctr + 1);
                         String secondHalf = toreturn.substring(ctr + 1);
                         toreturn = firstHalf + " " + secondHalf;
                         stringLength++;
                         continue;
                     }
                 }
             } else if (currentChar == '-') {
                 if (ctr - 1 >= 0 && ctr + 1 < stringLength) {
                     // L-N becomes L^N when there is only one  in the name
                     // and L is not preceded by a space
                     char nextChar = toreturn.charAt(ctr + 1);
                     char previousChar = toreturn.charAt(ctr - 1);
 
                     if (Character.isLetter(previousChar) && Character.isDigit(nextChar)
                     // if there is only one '-' in the string then the
                     // last occurrence of that char is the only
                     // occurrence
                             && toreturn.lastIndexOf(currentChar) == ctr) {
                         // check if the letter is preceded by a space or not
                         if (ctr - 2 >= 0) {
                             char prevPrevChar = toreturn.charAt(ctr - 2);
                             if (Character.isWhitespace(prevPrevChar)) {
                                 continue;
                             }
                         }
 
                         String firstHalf = toreturn.substring(0, ctr);
                         String secondHalf = toreturn.substring(ctr + 1);
                         toreturn = firstHalf + " " + secondHalf;
                         continue;
                     }
                 }
 
                 if (ctr - 2 >= 0 && ctr + 2 < stringLength) {
                     // LL-LL becomes LL^LL
                     char nextChar = toreturn.charAt(ctr + 1);
                     char nextNextChar = toreturn.charAt(ctr + 2);
                     char previousChar = toreturn.charAt(ctr - 1);
                     char prevPrevChar = toreturn.charAt(ctr - 2);
 
                     if (Character.isLetter(prevPrevChar) && Character.isLetter(previousChar) && Character.isLetter(nextChar)
                             && Character.isLetter(nextNextChar)) {
                         String firstHalf = toreturn.substring(0, ctr);
                         String secondHalf = toreturn.substring(ctr + 1);
                         toreturn = firstHalf + " " + secondHalf;
                         continue;
                     }
                 }
             } else if (currentChar == ' ') {
                 if (ctr + 1 < stringLength) {
                     char nextChar = toreturn.charAt(ctr + 1);
                     // ^^ becomes ^
                     if (nextChar == ' ') {
                         String firstHalf = toreturn.substring(0, ctr);
                         String secondHalf = toreturn.substring(ctr + 1);
                         toreturn = firstHalf + secondHalf;
                         stringLength--;
                         ctr--;
                         continue;
                     }
                 }
             } else if (currentChar == '/') {
                 // ^/ becomes / and /^ becomes /
                 if (ctr - 1 >= 0) {
                     char previousChar = toreturn.charAt(ctr - 1);
                     if (Character.isWhitespace(previousChar)) {
                         String firstHalf = toreturn.substring(0, ctr - 1);
                         String secondHalf = toreturn.substring(ctr);
                         toreturn = firstHalf + secondHalf;
                         stringLength--;
                         ctr = ctr - 2;
                         continue;
                     }
                 }
 
                 if (ctr + 1 < stringLength) {
                     char nextChar = toreturn.charAt(ctr + 1);
                     if (Character.isWhitespace(nextChar)) {
                         String firstHalf = toreturn.substring(0, ctr + 1);
                         String secondHalf = toreturn.substring(ctr + 2);
                         toreturn = firstHalf + secondHalf;
                         stringLength--;
                         ctr--;
                         continue;
                     }
                 }
             }
 
         }
 
         // REMOVE LEADING OR TRAILING ^
         toreturn = toreturn.trim();
 
         return toreturn;
     }
 
     /**
      * Returns true if the given char is considered a special character based on
      * ICIS Germplasm Name standardization rules. Returns false otherwise.
      * 
      * @param c
      * @return
      */
     private static boolean isGermplasmNameSpecialChar(char c) {
         char specialCharacters[] = { '-', '\'', '[', ']', '+', '.' };
         for (char sp : specialCharacters) {
             if (c == sp) {
                 return true;
             }
         }
 
         return false;
     }
 
     @Override
     public Germplasm getParentByGIDAndProgenitorNumber(Integer gid, Integer progenitorNumber) throws QueryException {
         GermplasmDAO dao = new GermplasmDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(gid);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtilForCentral.getCurrentSession());
         } else {
             return null;
         }
 
         Germplasm parent = dao.getProgenitorByGID(gid, progenitorNumber);
 
         return parent;
 
     }
 
     @Override
     public List<Object[]> findDescendants(Integer gid, int start, int numOfRows) throws QueryException {
         GermplasmDAO dao = new GermplasmDAO();
         ProgenitorDAO pDao = new ProgenitorDAO();
         List<Object[]> result = new ArrayList();
         Object[] germplasmList;
 
         // TODO Local-Central: Verify if the method to identify the database
         // instance is through gid
         // If not, please use getHibernateUtil(instance) and add Database
         // instance as parameter to countDescendants() method
         HibernateUtil hibernateUtil = getHibernateUtil(gid);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtilForCentral.getCurrentSession());
             pDao.setSession(hibernateUtilForCentral.getCurrentSession());
         } else {
             return result;
         }
 
         List<Germplasm> germplasm_descendant = dao.getGermplasmDescendantByGID(gid, start, numOfRows);
         for (Germplasm g : germplasm_descendant) {
             germplasmList = new Object[2];
             if (g.getGpid1().equals(gid)) {
                 germplasmList[0] = 1;
             } else if (g.getGpid2().equals(gid)) {
                 germplasmList[0] = 2;
             } else {
                 germplasmList[0] = pDao.getByGIDAndPID(g.getGid(), gid).getProgntrsPK().getPno().intValue();
             }
 
             germplasmList[1] = g;
 
             result.add(germplasmList);
         }
 
         return result;
 
     }
 
     @Override
     public int countDescendants(Integer gid) throws QueryException {
         GermplasmDAO dao = new GermplasmDAO();
 
         // TODO Local-Central: Verify if the method to identify the database
         // instance is through gid
         // If not, please use getHibernateUtil(instance) and add Database
         // instance as parameter to countDescendants() method
         HibernateUtil hibernateUtil = getHibernateUtil(gid);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return 0;
         }
 
         Integer count = dao.countGermplasmDescendantByGID(gid);
         return count;
 
     }
 
     @Override
     public GermplasmPedigreeTree generatePedigreeTree(Integer gid, int level) throws QueryException {
         GermplasmPedigreeTree tree = new GermplasmPedigreeTree();
         // set root node
         Germplasm root = getGermplasmWithPrefName(gid);
 
         if (root != null) {
             GermplasmPedigreeTreeNode rootNode = new GermplasmPedigreeTreeNode();
             rootNode.setGermplasm(root);
 
             if (level > 1) {
                 rootNode = addParents(rootNode, level);
             }
 
             tree.setRoot(rootNode);
 
             return tree;
         } else {
             return null;
         }
     }
 
     /**
      * Given a GermplasmPedigreeTreeNode and the level of the desired tree, add
      * parents to the node recursively until the specified level of the tree is
      * reached.
      * 
      * @param node
      * @param level
      * @return the given GermplasmPedigreeTreeNode with its parents added to it
      * @throws QueryException
      */
     private GermplasmPedigreeTreeNode addParents(GermplasmPedigreeTreeNode node, int level) throws QueryException {
         if (level == 1) {
             return node;
         } else {
             // get parents of node
             Germplasm germplasmOfNode = node.getGermplasm();
             if (germplasmOfNode.getGnpgs() == -1) {
                 // get and add the source germplasm
                 Germplasm parent = getGermplasmWithPrefName(germplasmOfNode.getGpid2());
                 if (parent != null) {
                     GermplasmPedigreeTreeNode nodeForParent = new GermplasmPedigreeTreeNode();
                     nodeForParent.setGermplasm(parent);
                     node.getLinkedNodes().add(addParents(nodeForParent, level - 1));
                 }
             } else if (germplasmOfNode.getGnpgs() >= 2) {
                 // get and add female parent
                 Germplasm femaleParent = getGermplasmWithPrefName(germplasmOfNode.getGpid1());
                 if (femaleParent != null) {
                     GermplasmPedigreeTreeNode nodeForFemaleParent = new GermplasmPedigreeTreeNode();
                     nodeForFemaleParent.setGermplasm(femaleParent);
                     node.getLinkedNodes().add(addParents(nodeForFemaleParent, level - 1));
                 }
 
                 // get and add male parent
                 Germplasm maleParent = getGermplasmWithPrefName(germplasmOfNode.getGpid2());
                 if (maleParent != null) {
                     GermplasmPedigreeTreeNode nodeForMaleParent = new GermplasmPedigreeTreeNode();
                     nodeForMaleParent.setGermplasm(maleParent);
                     node.getLinkedNodes().add(addParents(nodeForMaleParent, level - 1));
                 }
 
                 if (germplasmOfNode.getGnpgs() > 2) {
                     // if there are more parents, get and add each of them
                     List<Germplasm> otherParents = new ArrayList<Germplasm>();
 
                     if (germplasmOfNode.getGid() < 0 && this.hibernateUtilForLocal != null) {
                         GermplasmDAO dao = new GermplasmDAO();
                         dao.setSession(hibernateUtilForLocal.getCurrentSession());
                         otherParents = dao.getProgenitorsByGIDWithPrefName(germplasmOfNode.getGid());
                     } else if (germplasmOfNode.getGid() > 0 && this.hibernateUtilForCentral != null) {
                         GermplasmDAO centralDao = new GermplasmDAO();
                         centralDao.setSession(this.hibernateUtilForCentral.getCurrentSession());
                         otherParents = centralDao.getProgenitorsByGIDWithPrefName(germplasmOfNode.getGid());
                     }
 
                     for (Germplasm otherParent : otherParents) {
                         GermplasmPedigreeTreeNode nodeForOtherParent = new GermplasmPedigreeTreeNode();
                         nodeForOtherParent.setGermplasm(otherParent);
                         node.getLinkedNodes().add(addParents(nodeForOtherParent, level - 1));
                     }
                 }
             }
 
             return node;
         }
     }
 
     @Override
     public List<Germplasm> getManagementNeighbors(Integer gid) throws QueryException {
         List<Germplasm> neighbors = new ArrayList<Germplasm>();
         GermplasmDAO dao = new GermplasmDAO();
         if (gid < 0 && this.hibernateUtilForLocal != null) {
             dao.setSession(hibernateUtilForLocal.getCurrentSession());
             neighbors = dao.getManagementNeighbors(gid);
             return neighbors;
         } else if (gid > 0 && this.hibernateUtilForCentral != null) {
             dao.setSession(hibernateUtilForCentral.getCurrentSession());
             neighbors = dao.getManagementNeighbors(gid);
 
             if (this.hibernateUtilForLocal != null) {
                 GermplasmDAO localDao = new GermplasmDAO();
                 localDao.setSession(this.hibernateUtilForLocal.getCurrentSession());
                 neighbors.addAll(localDao.getManagementNeighbors(gid));
             }
         }
 
         return neighbors;
     }
 
     @Override
     public List<Germplasm> getGroupRelatives(Integer gid) throws QueryException {
         GermplasmDAO dao = new GermplasmDAO();
 
         // TODO Local-Central: Verify if the method to identify the database
         // instance is through gid
         // If not, please use getHibernateUtil(instance) and add Database
         // instance as parameter to getGroupRelatives() method
         HibernateUtil hibernateUtil = getHibernateUtil(gid);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return new ArrayList<Germplasm>();
         }
 
         List<Germplasm> relatives = dao.getGroupRelatives(gid);
         return relatives;
     }
 
     @Override
     public List<Germplasm> getGenerationHistory(Integer gid) throws QueryException {
         List<Germplasm> toreturn = new ArrayList<Germplasm>();
 
         Germplasm currentGermplasm = getGermplasmWithPrefName(gid);
         if (currentGermplasm != null) {
             toreturn.add(currentGermplasm);
 
             while (currentGermplasm.getGnpgs() == -1) {
                 // trace back the sources
                 Integer sourceId = currentGermplasm.getGpid2();
                 currentGermplasm = getGermplasmWithPrefName(sourceId);
 
                 if (currentGermplasm != null) {
                     toreturn.add(currentGermplasm);
                 } else {
                     break;
                 }
             }
         }
 
         return toreturn;
     }
 
     @Override
     public GermplasmPedigreeTree getDerivativeNeighborhood(Integer gid, int numberOfStepsBackward, int numberOfStepsForward)
             throws QueryException {
         GermplasmPedigreeTree derivativeNeighborhood = new GermplasmPedigreeTree();
 
         // get the root of the neighborhood
         Object[] traceResult = traceDerivativeRoot(gid, numberOfStepsBackward);
 
         if (traceResult != null) {
             Germplasm root = (Germplasm) traceResult[0];
             Integer stepsLeft = (Integer) traceResult[1];
 
             GermplasmPedigreeTreeNode rootNode = new GermplasmPedigreeTreeNode();
             rootNode.setGermplasm(root);
 
             // get the derived lines from the root until the whole neighborhood
             // is created
             int treeLevel = numberOfStepsBackward - stepsLeft + numberOfStepsForward + 1;
             rootNode = getDerivedLines(rootNode, treeLevel);
 
             derivativeNeighborhood.setRoot(rootNode);
 
             return derivativeNeighborhood;
         } else {
             return null;
         }
     }
 
     /**
      * Recursive function which gets the root of a derivative neighborhood by
      * tracing back through the source germplasms. The function stops when the
      * steps are exhausted or a germplasm created by a generative method is
      * encountered, whichever comes first.
      * 
      * @param gid
      * @param steps
      * @return Object[] - first element is the Germplasm POJO, second is an
      *         Integer which is the number of steps left to take
      * @throws QueryException
      */
     private Object[] traceDerivativeRoot(Integer gid, int steps) throws QueryException {
         Germplasm germplasm = getGermplasmWithPrefName(gid);
 
         if (germplasm == null) {
             return null;
         } else if (steps == 0 || germplasm.getGnpgs() != -1) {
             return new Object[] { germplasm, Integer.valueOf(steps) };
         } else {
             Object[] returned = traceDerivativeRoot(germplasm.getGpid2(), steps - 1);
             if (returned != null) {
                 return returned;
             } else {
                 return new Object[] { germplasm, Integer.valueOf(steps) };
             }
         }
     }
 
     /**
      * Recursive function to get the derived lines given a Germplasm. This
      * constructs the derivative neighborhood.
      * 
      * @param node
      * @param steps
      * @return
      * @throws QueryException
      */
     private GermplasmPedigreeTreeNode getDerivedLines(GermplasmPedigreeTreeNode node, int steps) throws QueryException {
         if (steps <= 0) {
             return node;
         } else {
             List<Germplasm> derivedGermplasms = new ArrayList<Germplasm>();
             Integer gid = node.getGermplasm().getGid();
 
             if (gid < 0 && this.hibernateUtilForLocal != null) {
                 GermplasmDAO dao = new GermplasmDAO();
                 dao.setSession(hibernateUtilForLocal.getCurrentSession());
                 derivedGermplasms = dao.getDerivativeChildren(gid);
             } else if (gid > 0 && this.hibernateUtilForCentral != null) {
                 GermplasmDAO dao = new GermplasmDAO();
                 dao.setSession(hibernateUtilForCentral.getCurrentSession());
                 derivedGermplasms = dao.getDerivativeChildren(gid);
 
                 if (this.hibernateUtilForLocal != null) {
                     GermplasmDAO localDao = new GermplasmDAO();
                     localDao.setSession(hibernateUtilForLocal.getCurrentSession());
                    derivedGermplasms.addAll(localDao.getDerivativeChildren(gid));
                 }
             }
 
             for (Germplasm g : derivedGermplasms) {
                 GermplasmPedigreeTreeNode derivedNode = new GermplasmPedigreeTreeNode();
                 derivedNode.setGermplasm(g);
                 node.getLinkedNodes().add(getDerivedLines(derivedNode, steps - 1));
             }
 
             return node;
         }
     }
 
     @Override
     public int addGermplasmAttribute(Attribute attribute) throws QueryException {
         List<Attribute> attributes = new ArrayList<Attribute>();
         attributes.add(attribute);
         return addGermplasmAttribute(attributes);
     }
 
     @Override
     public int addGermplasmAttribute(List<Attribute> attributes) throws QueryException {
         return addOrUpdateAttributes(attributes, Operation.ADD);
     }
 
     @Override
     public int updateGermplasmAttribute(Attribute attribute) throws QueryException {
         List<Attribute> attributes = new ArrayList<Attribute>();
         attributes.add(attribute);
         return updateGermplasmAttribute(attributes);
     }
 
     @Override
     public int updateGermplasmAttribute(List<Attribute> attributes) throws QueryException {
         return addOrUpdateAttributes(attributes, Operation.UPDATE);
     }
 
     private int addOrUpdateAttributes(List<Attribute> attributes, Operation operation) throws QueryException {
         if (hibernateUtilForLocal == null) {
             throw new QueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // initialize session & transaction
         Session session = hibernateUtilForLocal.getCurrentSession();
         Transaction trans = null;
 
         int attributesSaved = 0;
         try {
             // begin save transaction
             trans = session.beginTransaction();
 
             AttributeDAO dao = new AttributeDAO();
             dao.setSession(session);
 
             for (Attribute attribute : attributes) {
                 if (operation == Operation.ADD) {
                     // Auto-assign negative IDs for new local DB records
                     Integer negativeId = dao.getNegativeId("aid");
                     attribute.setAid(negativeId);
                 } else if (operation == Operation.UPDATE) {
                     // Check if Attribute is a local DB record. Throws exception
                     // if Attribute is a central DB record.
                     dao.validateId(attribute);
                 }
                 dao.saveOrUpdate(attribute);
                 attributesSaved++;
             }
             // end transaction, commit to database
             trans.commit();
         } catch (Exception ex) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new QueryException("Error encountered while saving Attribute: " + ex.getMessage(), ex);
         } finally {
             hibernateUtilForLocal.closeCurrentSession();
         }
 
         return attributesSaved;
     }
 
     @Override
     public Attribute getAttributeById(Integer id) {
         AttributeDAO dao = new AttributeDAO();
         HibernateUtil hibernateUtil = getHibernateUtil(id);
 
         if (hibernateUtil != null) {
             dao.setSession(hibernateUtil.getCurrentSession());
         } else {
             return null;
         }
 
         return dao.findById(id, false);
     }
 
     @Override
     public boolean updateProgenitor(Integer gid, Integer progenitorId, Integer progenitorNumber) throws QueryException {
         if (hibernateUtilForLocal == null) {
             throw new QueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // check if the germplasm record identified by gid exists
         Germplasm child = getGermplasmByGID(gid);
         if (child == null) {
             throw new QueryException("There is no germplasm record with gid: " + gid);
         }
 
         // check if the germplasm record identified by progenitorId exists
         Germplasm parent = getGermplasmByGID(progenitorId);
         if (parent == null) {
             throw new QueryException("There is no germplasm record with gid: " + progenitorId);
         }
 
         // check progenitor number
         if (progenitorNumber == 1 || progenitorNumber == 2) {
             // check if given gid refers to a local record
             if (gid < 0) {
                 // proceed with update
                 if (progenitorNumber == 1) {
                     child.setGpid1(progenitorId);
                 } else {
                     child.setGpid2(progenitorId);
                 }
 
                 List<Germplasm> germplasms = new ArrayList<Germplasm>();
                 germplasms.add(child);
                 int updated = addOrUpdateGermplasms(germplasms, Operation.UPDATE);
                 if (updated == 1) {
                     return true;
                 }
             } else {
                 throw new QueryException(
                         "The gid supplied as parameter does not refer to a local record. Only local records may be updated.");
             }
         } else if (progenitorNumber > 2) {
             ProgenitorDAO dao = new ProgenitorDAO();
             dao.setSession(this.hibernateUtilForLocal.getCurrentSession());
 
             // check if there is an existing Progenitor record
             ProgenitorPK id = new ProgenitorPK(gid, progenitorNumber);
             Progenitor p = dao.findById(id, false);
 
             if (p != null) {
                 // update the existing record
                 p.setPid(progenitorId);
 
                 List<Progenitor> progenitors = new ArrayList<Progenitor>();
                 progenitors.add(p);
                 int updated = addOrUpdateProgenitors(progenitors);
                 if (updated == 1) {
                     return true;
                 }
             } else {
                 // create new Progenitor record
                 Progenitor newRecord = new Progenitor(id);
                 newRecord.setPid(progenitorId);
 
                 List<Progenitor> progenitors = new ArrayList<Progenitor>();
                 progenitors.add(newRecord);
                 int added = addOrUpdateProgenitors(progenitors);
                 if (added == 1) {
                     return true;
                 }
             }
         } else {
             throw new QueryException("Invalid progenitor number: " + progenitorNumber);
         }
 
         return false;
     }
 
     private int addOrUpdateGermplasms(List<Germplasm> germplasms, Operation operation) throws QueryException {
         if (hibernateUtilForLocal == null) {
             throw new QueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // initialize session & transaction
         Session session = hibernateUtilForLocal.getCurrentSession();
         Transaction trans = null;
 
         int germplasmsSaved = 0;
         try {
             // begin save transaction
             trans = session.beginTransaction();
 
             GermplasmDAO dao = new GermplasmDAO();
             dao.setSession(session);
 
             for (Germplasm germplasm : germplasms) {
                 if (operation == Operation.ADD) {
                     // Auto-assign negative IDs for new local DB records
                     Integer negativeId = dao.getNegativeId("gid");
                     germplasm.setGid(negativeId);
                     germplasm.setLgid(negativeId);
                 } else if (operation == Operation.UPDATE) {
                     // Check if Germplasm is a local DB record. Throws exception
                     // if Germplasm is a central DB record.
                     dao.validateId(germplasm);
                 }
                 dao.saveOrUpdate(germplasm);
                 germplasmsSaved++;
                 if (germplasmsSaved % JDBC_BATCH_SIZE == 0) {
                     // flush a batch of inserts and release memory
                     dao.flush();
                     dao.clear();
                 }
             }
             // end transaction, commit to database
             trans.commit();
         } catch (Exception ex) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new QueryException("Error encountered while saving Germplasm: " + ex.getMessage(), ex);
         } finally {
             hibernateUtilForLocal.closeCurrentSession();
         }
 
         return germplasmsSaved;
     }
 
     private int addOrUpdateProgenitors(List<Progenitor> progenitors) throws QueryException {
         if (hibernateUtilForLocal == null) {
             throw new QueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // initialize session & transaction
         Session session = hibernateUtilForLocal.getCurrentSession();
         Transaction trans = null;
 
         int progenitorsSaved = 0;
         try {
             // begin save transaction
             trans = session.beginTransaction();
 
             ProgenitorDAO dao = new ProgenitorDAO();
             dao.setSession(session);
 
             for (Progenitor progenitor : progenitors) {
                 dao.saveOrUpdate(progenitor);
                 progenitorsSaved++;
             }
             // end transaction, commit to database
             trans.commit();
         } catch (Exception ex) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new QueryException("Error encountered while saving Progenitor: " + ex.getMessage(), ex);
         } finally {
             hibernateUtilForLocal.closeCurrentSession();
         }
 
         return progenitorsSaved;
     }
 
     @Override
     public int updateGermplasm(Germplasm germplasm) throws QueryException {
         List<Germplasm> germplasms = new ArrayList<Germplasm>();
         germplasms.add(germplasm);
         return updateGermplasm(germplasms);
     }
 
     @Override
     public int updateGermplasm(List<Germplasm> germplasms) throws QueryException {
         return addOrUpdateGermplasms(germplasms, Operation.UPDATE);
     }
 
     @Override
     public int addGermplasm(Germplasm germplasm, Name preferredName) throws QueryException {
         Map<Germplasm, Name> germplasmNameMap = new HashMap<Germplasm, Name>();
         germplasm.setGid(Integer.valueOf(1));
         germplasmNameMap.put(germplasm, preferredName);
         return addGermplasm(germplasmNameMap);
     }
 
     @Override
     public int addGermplasm(Map<Germplasm, Name> germplasmNameMap) throws QueryException {
         if (hibernateUtilForLocal == null) {
             throw new QueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // initialize session & transaction
         Session session = hibernateUtilForLocal.getCurrentSession();
         Transaction trans = null;
 
         int germplasmsSaved = 0;
         try {
             // begin save transaction
             trans = session.beginTransaction();
 
             GermplasmDAO dao = new GermplasmDAO();
             dao.setSession(session);
 
             NameDAO nameDao = new NameDAO();
             nameDao.setSession(session);
 
             for (Germplasm germplasm : germplasmNameMap.keySet()) {
                 Name name = germplasmNameMap.get(germplasm);
 
                 // Auto-assign negative IDs for new local DB records
                 Integer negativeId = dao.getNegativeId("gid");
                 germplasm.setGid(negativeId);
                 germplasm.setLgid(Integer.valueOf(0));
 
                 Integer nameId = nameDao.getNegativeId("nid");
                 name.setNid(nameId);
                 name.setNstat(Integer.valueOf(1));
                 name.setGermplasmId(negativeId);
 
                 dao.saveOrUpdate(germplasm);
                 nameDao.saveOrUpdate(name);
                 germplasmsSaved++;
 
                 if (germplasmsSaved % JDBC_BATCH_SIZE == 0) {
                     // flush a batch of inserts and release memory
                     dao.flush();
                     dao.clear();
                 }
             }
             // end transaction, commit to database
             trans.commit();
         } catch (Exception ex) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new QueryException("Error encountered while saving Germplasm: " + ex.getMessage(), ex);
         } finally {
             hibernateUtilForLocal.closeCurrentSession();
         }
 
         return germplasmsSaved;
     }
 
     /**
      * @Override public List<Germplasm> findGermplasmByExample(Germplasm sample,
      *           int start, int numOfRows) { GermplasmDAO dao = new
      *           GermplasmDAO();
      *           dao.setSession(hibernateUtil.getCurrentSession()); return
      *           dao.findByExample(sample, start, numOfRows); }
      * @Override public int countGermplasmByExample(Germplasm sample) {
      *           GermplasmDAO dao = new GermplasmDAO();
      *           dao.setSession(hibernateUtil.getCurrentSession()); return
      *           dao.countByExample(sample).intValue(); }
      **/
     
     
     @Override
     public List<GidNidElement> getGidAndNidByGermplasmNames(List<String> germplasmNames) throws QueryException{
 
         HibernateUtil hibernateUtilForCentral = getHibernateUtil(Database.CENTRAL);
         HibernateUtil hibernateUtilForLocal = getHibernateUtil(Database.LOCAL);
         
         List<GidNidElement> results = new ArrayList<GidNidElement>();
         NameDAO dao = new NameDAO();
         
         if (hibernateUtilForCentral != null) {
             dao.setSession(hibernateUtilForCentral.getCurrentSession());
             results.addAll(dao.getGidAndNidByGermplasmNames(germplasmNames));
         } 
 
         if (hibernateUtilForLocal != null) {
             dao.setSession(hibernateUtilForLocal.getCurrentSession());
             results.addAll(dao.getGidAndNidByGermplasmNames(germplasmNames));
         } 
         
         return results;
     }
 
 
 }
