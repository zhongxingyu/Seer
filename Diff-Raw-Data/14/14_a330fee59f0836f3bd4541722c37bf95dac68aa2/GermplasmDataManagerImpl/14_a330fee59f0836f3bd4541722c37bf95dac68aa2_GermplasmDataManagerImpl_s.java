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
 
 import java.math.BigInteger;
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
 import org.generationcp.middleware.dao.gdms.MapDAO;
 import org.generationcp.middleware.dao.gdms.MappingPopDAO;
 import org.generationcp.middleware.exceptions.MiddlewareQueryException;
 import org.generationcp.middleware.hibernate.HibernateSessionProvider;
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
 import org.generationcp.middleware.pojos.gdms.ParentElement;
 import org.generationcp.middleware.pojos.germplasm.GermplasmCross;
 import org.generationcp.middleware.pojos.germplasm.GermplasmCrossElement;
 import org.generationcp.middleware.pojos.germplasm.SingleGermplasmCrossElement;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 
 
 /**
  * Implementation of the GermplasmDataManager interface. To instantiate this
  * class, a Hibernate Session must be passed to its constructor.
  * 
  * @author Kevin Manansala, Lord Hendrix Barboza
  * 
  */
 public class GermplasmDataManagerImpl extends DataManager implements GermplasmDataManager{
 
     public GermplasmDataManagerImpl() {
     }
 
     public GermplasmDataManagerImpl(HibernateSessionProvider sessionProviderForLocal, HibernateSessionProvider sessionProviderForCentral) {
         super(sessionProviderForLocal, sessionProviderForCentral);
     }
 
     public GermplasmDataManagerImpl(Session sessionForLocal, Session sessionForCentral) {
         super(sessionForLocal, sessionForCentral);
     }
 
     @Override
     public List<Location> getAllLocations(int start, int numOfRows) throws MiddlewareQueryException {
         LocationDAO dao = new LocationDAO();
 
         List<Location> locations = new ArrayList<Location>();
 
         long centralCount = 0;
         long localCount = 0;
         long relativeLimit = 0;
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForCentral != null) {
 
             dao.setSession(sessionForCentral);
             centralCount = dao.countAll();
 
             if (centralCount > start) {
 
                 locations.addAll(dao.getAll(start, numOfRows));
 
                 relativeLimit = numOfRows - (centralCount - start);
 
                 if (relativeLimit > 0) {
 
                     if (sessionForLocal != null) {
 
                         dao.setSession(sessionForLocal);
 
                         localCount = dao.countAll();
 
                         if (localCount > 0) {
 
                             locations.addAll(dao.getAll(0, (int) relativeLimit));
 
                         }
                     }
                 }
 
             } else {
 
                 relativeLimit = start - centralCount;
 
                 if (sessionForLocal != null) {
 
                     dao.setSession(sessionForLocal);
 
                     localCount = dao.countAll();
 
                     if (localCount > relativeLimit) {
 
                         locations.addAll(dao.getAll((int) relativeLimit, numOfRows));
 
                     }
                 }
             }
 
         } else if (sessionForLocal != null) {
 
             dao.setSession(sessionForLocal);
 
             localCount = dao.countAll();
 
             if (localCount > start) {
 
                 locations.addAll(dao.getAll(start, numOfRows));
 
             }
         }
 
         return locations;
     }
 
     @Override
     public long countAllLocations() throws MiddlewareQueryException {
 
         long count = 0;
 
         LocationDAO dao = new LocationDAO();
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             count = count + dao.countAll();
         }
 
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             count = count + dao.countAll();
         }
 
         return count;
     }
 
     @Override
     public List<Location> getLocationsByName(String name, Operation op) throws MiddlewareQueryException {
 
         LocationDAO dao = new LocationDAO();
         List<Location> locations = new ArrayList<Location>();
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             locations.addAll(dao.getByName(name, op));
         }
 
         // get the list of Location from the central instance
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             locations.addAll(dao.getByName(name, op));
         }
 
         return locations;
 
     }
 
     @Override
     public List<Location> getLocationsByName(String name, int start, int numOfRows, Operation op) throws MiddlewareQueryException {
         LocationDAO dao = new LocationDAO();
         List<Location> locations = new ArrayList<Location>();
 
         long centralCount = 0;
         long localCount = 0;
         long relativeLimit = 0;
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             centralCount = dao.countByName(name, op);
 
             if (centralCount > start) {
                 locations.addAll(dao.getByName(name, start, numOfRows, op));
                 relativeLimit = numOfRows - (centralCount - start);
                 if (relativeLimit > 0) {
                     if (sessionForLocal != null) {
                         dao.setSession(sessionForLocal);
                         localCount = dao.countByName(name, op);
                         if (localCount > 0) {
                             locations.addAll(dao.getByName(name, 0, (int) relativeLimit, op));
                         }
                     }
                 }
 
             } else {
                 relativeLimit = start - centralCount;
                 if (sessionForLocal != null) {
                     dao.setSession(sessionForLocal);
                     localCount = dao.countByName(name, op);
                     if (localCount > relativeLimit) {
                         locations.addAll(dao.getByName(name, (int) relativeLimit, numOfRows, op));
                     }
                 }
             }
 
         } else if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             localCount = dao.countByName(name, op);
             if (localCount > start) {
                 locations.addAll(dao.getByName(name, start, numOfRows, op));
             }
         }
 
         return locations;
     }
 
     @Override
     public long countLocationsByName(String name, Operation op) throws MiddlewareQueryException {
         long count = 0;
         LocationDAO dao = new LocationDAO();
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             count = count + dao.countByName(name, op);
         }
 
         // get the list of Location from the central instance
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             count = count + dao.countByName(name, op);
         }
 
         return count;
     }
 
     @Override
     public List<Location> getLocationsByCountry(Country country) throws MiddlewareQueryException {
         LocationDAO dao = new LocationDAO();
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
         List<Location> locations = new ArrayList<Location>();
 
         if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             locations.addAll(dao.getByCountry(country));
         }
 
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             locations.addAll(dao.getByCountry(country));
         }
 
         return locations;
     }
 
     @Override
     public List<Location> getLocationsByCountry(Country country, int start, int numOfRows) throws MiddlewareQueryException {
         LocationDAO dao = new LocationDAO();
         List<Location> locations = new ArrayList<Location>();
 
         long centralCount = 0;
         long localCount = 0;
         long relativeLimit = 0;
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             centralCount = dao.countByCountry(country);
 
             if (centralCount > start) {
                 locations.addAll(dao.getByCountry(country, start, numOfRows));
                 relativeLimit = numOfRows - (centralCount - start);
                 if (relativeLimit > 0) {
                     if (sessionForLocal != null) {
                         dao.setSession(sessionForLocal);
                         localCount = dao.countByCountry(country);
                         if (localCount > 0) {
                             locations.addAll(dao.getByCountry(country, 0, (int) relativeLimit));
                         }
                     }
                 }
 
             } else {
                 relativeLimit = start - centralCount;
                 if (sessionForLocal != null) {
                     dao.setSession(sessionForLocal);
                     localCount = dao.countByCountry(country);
                     if (localCount > relativeLimit) {
                         locations.addAll(dao.getByCountry(country, (int) relativeLimit, numOfRows));
                     }
                 }
             }
 
         } else if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             localCount = dao.countByCountry(country);
             if (localCount > start) {
                 locations.addAll(dao.getByCountry(country, start, numOfRows));
             }
         }
 
         return locations;
     }
 
     @Override
     public long countLocationsByCountry(Country country) throws MiddlewareQueryException {
         long count = 0;
         LocationDAO dao = new LocationDAO();
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             count = count + dao.countByCountry(country);
         }
 
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             count = count + dao.countByCountry(country);
         }
 
         return count;
     }
 
     @Override
     public List<Location> getLocationsByType(Integer type) throws MiddlewareQueryException {
         LocationDAO dao = new LocationDAO();
         List<Location> locations = new ArrayList<Location>();
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             locations.addAll(dao.getByType(type));
         }
 
         // get the list of Location from the central instance
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             locations.addAll(dao.getByType(type));
         }
 
         return locations;
     }
 
     @Override
     public List<Location> getLocationsByType(Integer type, int start, int numOfRows) throws MiddlewareQueryException {
         LocationDAO dao = new LocationDAO();
         List<Location> locations = new ArrayList<Location>();
 
         long centralCount = 0;
         long localCount = 0;
         long relativeLimit = 0;
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             centralCount = dao.countByType(type);
 
             if (centralCount > start) {
                 locations.addAll(dao.getByType(type, start, numOfRows));
                 relativeLimit = numOfRows - (centralCount - start);
                 if (relativeLimit > 0) {
                     if (sessionForLocal != null) {
                         dao.setSession(sessionForLocal);
                         localCount = dao.countByType(type);
                         if (localCount > 0) {
                             locations.addAll(dao.getByType(type, 0, (int) relativeLimit));
                         }
                     }
                 }
 
             } else {
                 relativeLimit = start - centralCount;
                 if (sessionForLocal != null) {
                     dao.setSession(sessionForLocal);
                     localCount = dao.countByType(type);
                     if (localCount > relativeLimit) {
                         locations.addAll(dao.getByType(type, (int) relativeLimit, numOfRows));
                     }
                 }
             }
 
         } else if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             localCount = dao.countByType(type);
             if (localCount > start) {
                 locations.addAll(dao.getByType(type, start, numOfRows));
             }
         }
 
         return locations;
     }
 
     @Override
     public long countLocationsByType(Integer type) throws MiddlewareQueryException {
         long count = 0;
         LocationDAO dao = new LocationDAO();
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             count = count + dao.countByType(type);
         }
 
         // get the list of Location from the central instance
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             count = count + dao.countByType(type);
         }
 
         return count;
     }
 
     public List<Germplasm> getAllGermplasm(int start, int numOfRows, Database instance) throws MiddlewareQueryException {
         GermplasmDAO dao = new GermplasmDAO();
         Session session = getSession(instance);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return new ArrayList<Germplasm>();
         }
 
         return (List<Germplasm>) dao.getAll(start, numOfRows);
     }
 
     public long countAllGermplasm(Database instance) throws MiddlewareQueryException {
         long count = 0;
         GermplasmDAO dao = new GermplasmDAO();
         Session session = getSession(instance);
 
         if (session != null) {
             dao.setSession(session);
             count = count + dao.countAll();
         }
 
         return count;
     }
 
     public List<Germplasm> getGermplasmByPrefName(String name, int start, int numOfRows, Database instance) throws MiddlewareQueryException {
         GermplasmDAO dao = new GermplasmDAO();
         Session session = getSession(instance);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return new ArrayList<Germplasm>();
         }
 
         return (List<Germplasm>) dao.getByPrefName(name, start, numOfRows);
     }
 
     public long countGermplasmByPrefName(String name) throws MiddlewareQueryException {
         long count = 0;
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal != null) {
             GermplasmDAO dao = new GermplasmDAO();
             dao.setSession(sessionForLocal);
             count = count + dao.countByPrefName(name).intValue();
         }
 
         if (sessionForCentral != null) {
             GermplasmDAO centralDao = new GermplasmDAO();
             centralDao.setSession(sessionForCentral);
             count = count + centralDao.countByPrefName(name).intValue();
         }
 
         return count;
     }
 
     @Override
     public List<Germplasm> getGermplasmByName(String name, int start, int numOfRows, GetGermplasmByNameModes mode, Operation op,
             Integer status, GermplasmNameType type, Database instance) throws MiddlewareQueryException {
         GermplasmDAO dao = new GermplasmDAO();
         Session session = getSession(instance);
 
         List<Germplasm> germplasms = new ArrayList<Germplasm>();
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return germplasms;
         }
 
         // do string manipulation on name parameter depending on
         // GetGermplasmByNameModes parameter
         String nameToUse = "";
         if (mode == GetGermplasmByNameModes.NORMAL) {
             nameToUse = name;
         } else if (mode == GetGermplasmByNameModes.SPACES_REMOVED) {
             String nameWithSpacesRemoved = GermplasmDataManagerImpl.removeSpaces(name);
             nameToUse = nameWithSpacesRemoved.toString();
         } else if (mode == GetGermplasmByNameModes.STANDARDIZED) {
             String standardizedName = GermplasmDataManagerImpl.standardizeName(name);
             nameToUse = standardizedName;
         }
 
         germplasms = dao.getByName(nameToUse, start, numOfRows, op, status, type);
         return germplasms;
     }
 
     
     @Override
     public List<Germplasm> getGermplasmByName(String name, int start, int numOfRows, Operation op) throws MiddlewareQueryException {
 
         List<String> names = new ArrayList<String>();
         names.add(name);
         names.add(GermplasmDataManagerImpl.standardizeName(name));
         names.add(GermplasmDataManagerImpl.removeSpaces(name));
 
         GermplasmDAO dao = new GermplasmDAO();
         List<Germplasm> germplasms = new ArrayList<Germplasm>();
 
         long centralCount = 0;
         long localCount = 0;
         long relativeLimit = 0;
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             centralCount = dao.countByName(names, op);
 
             if (centralCount > start) {
                 germplasms.addAll(dao.getByName(names, start, numOfRows, op));
                 relativeLimit = numOfRows - (centralCount - start);
                 if (relativeLimit > 0) {
                     if (sessionForLocal != null) {
                         dao.setSession(sessionForLocal);
                         localCount = dao.countByName(names, op);
                         if (localCount > 0) {
                             germplasms.addAll(dao.getByName(names, 0, (int) relativeLimit, op));
                         }
                     }
                 }
 
             } else {
                 relativeLimit = start - centralCount;
                 if (sessionForLocal != null) {
                     dao.setSession(sessionForLocal);
                     localCount = dao.countByName(names, op);
                     if (localCount > relativeLimit) {
                         germplasms.addAll(dao.getByName(names, (int) relativeLimit, numOfRows, op));
                     }
                 }
             }
 
         } else if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             localCount = dao.countByName(names, op);
             if (localCount > start) {
                 germplasms.addAll(dao.getByName(names, start, numOfRows, op));
             }
         }
 
         return germplasms;
 
     }
 
     @Override
     public long countGermplasmByName(String name, GetGermplasmByNameModes mode, Operation op, Integer status, GermplasmNameType type,
             Database instance) throws MiddlewareQueryException {
         // do string manipulation on name parameter depending on
         // GetGermplasmByNameModes parameter
         String nameToUse = "";
         if (mode == GetGermplasmByNameModes.NORMAL) {
             nameToUse = name;
         } else if (mode == GetGermplasmByNameModes.SPACES_REMOVED) {
             String nameWithSpacesRemoved = GermplasmDataManagerImpl.removeSpaces(name);
             nameToUse = nameWithSpacesRemoved.toString();
         } else if (mode == GetGermplasmByNameModes.STANDARDIZED) {
             String standardizedName = GermplasmDataManagerImpl.standardizeName(name);
             nameToUse = standardizedName;
         }
 
         long count = 0;
 
         GermplasmDAO dao = new GermplasmDAO();
         Session session = getSession(instance);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return 0;
         }
 
         count = dao.countByName(nameToUse, op, status, type);
         return count;
     }
 
     @Override
     public long countGermplasmByName(String name, Operation operation) throws MiddlewareQueryException {
 
         List<String> names = new ArrayList<String>();
         names.add(name);
         names.add(GermplasmDataManagerImpl.standardizeName(name));
         names.add(GermplasmDataManagerImpl.removeSpaces(name));
         
         GermplasmDAO dao = new GermplasmDAO();
         long count = 0L;
         
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             count += dao.countByName(names, operation);
         }
 
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             count += dao.countByName(names, operation);
         }
 
         return count;
     }
 
     @Override
     public List<Germplasm> getGermplasmByLocationName(String name, int start, int numOfRows, Operation op, Database instance)
             throws MiddlewareQueryException {
         GermplasmDAO dao = new GermplasmDAO();
         List<Germplasm> germplasms = new ArrayList<Germplasm>();
         Session session = getSession(instance);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return germplasms;
         }
 
         if (op == Operation.EQUAL) {
             germplasms = dao.getByLocationNameUsingEqual(name, start, numOfRows);
         } else if (op == Operation.LIKE) {
             germplasms = dao.getByLocationNameUsingLike(name, start, numOfRows);
         }
 
         return germplasms;
     }
 
     @Override
     public long countGermplasmByLocationName(String name, Operation op, Database instance) throws MiddlewareQueryException {
         GermplasmDAO dao = new GermplasmDAO();
         Session session = getSession(instance);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return 0;
         }
 
         long count = 0;
         if (op == Operation.EQUAL) {
             count = dao.countByLocationNameUsingEqual(name);
         } else if (op == Operation.LIKE) {
             count = dao.countByLocationNameUsingLike(name);
         }
         return count;
     }
 
     @Override
     public List<Germplasm> getGermplasmByMethodName(String name, int start, int numOfRows, Operation op, Database instance)
             throws MiddlewareQueryException {
         GermplasmDAO dao = new GermplasmDAO();
         List<Germplasm> germplasms = null;
         Session session = getSession(instance);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return germplasms;
         }
 
         if (op == Operation.EQUAL) {
             germplasms = dao.getByMethodNameUsingEqual(name, start, numOfRows);
         } else if (op == Operation.LIKE) {
             germplasms = dao.getByMethodNameUsingLike(name, start, numOfRows);
         }
 
         return germplasms;
     }
 
     @Override
     public long countGermplasmByMethodName(String name, Operation op, Database instance) throws MiddlewareQueryException {
         GermplasmDAO dao = new GermplasmDAO();
         Session session = getSession(instance);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return 0;
         }
 
         long count = 0;
         if (op == Operation.EQUAL) {
             count = dao.countByMethodNameUsingEqual(name);
         } else if (op == Operation.LIKE) {
             count = dao.countByMethodNameUsingLike(name);
         }
         return count;
     }
 
     @Override
     public Germplasm getGermplasmByGID(Integer gid) {
         GermplasmDAO dao = new GermplasmDAO();
         Session session = getSession(gid);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return null;
         }
 
         return (Germplasm) dao.getById(gid, false);
     }
 
     @Override
     public Germplasm getGermplasmWithPrefName(Integer gid) throws MiddlewareQueryException {
         GermplasmDAO dao = new GermplasmDAO();
         Session session = getSession(gid);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return null;
         }
 
         return (Germplasm) dao.getByGIDWithPrefName(gid);
     }
 
     @Override
     public Germplasm getGermplasmWithPrefAbbrev(Integer gid) throws MiddlewareQueryException {
         GermplasmDAO dao = new GermplasmDAO();
         Session session = getSession(gid);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return null;
         }
 
         return (Germplasm) dao.getByGIDWithPrefAbbrev(gid);
     }
 
     @Override
     public Name getGermplasmNameByID(Integer id) {
         NameDAO dao = new NameDAO();
         Session session = getSession(id);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return null;
         }
 
         return (Name) dao.getById(id, false);
     }
 
     @Override
     public List<Name> getNamesByGID(Integer gid, Integer status, GermplasmNameType type) throws MiddlewareQueryException {
         NameDAO dao = new NameDAO();
         Session session = getSession(gid);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return new ArrayList<Name>();
         }
 
         return (List<Name>) dao.getByGIDWithFilters(gid, status, type); //names
     }
 
     @Override
     public Name getPreferredNameByGID(Integer gid) throws MiddlewareQueryException {
         NameDAO dao = new NameDAO();
         Session session = getSession(gid);
 
         if (session != null) {
             dao.setSession(session);
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
     public Name getPreferredAbbrevByGID(Integer gid) throws MiddlewareQueryException {
         NameDAO dao = new NameDAO();
         Session session = getSession(gid);
 
         if (session != null) {
             dao.setSession(session);
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
     public Name getNameByGIDAndNval(Integer gid, String nval) throws MiddlewareQueryException {
         NameDAO dao = new NameDAO();
         Session session = getSession(gid);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return null;
         }
 
         return dao.getByGIDAndNval(gid, nval);
     }
 
     @Override
     public Integer updateGermplasmPrefName(Integer gid, String newPrefName) throws MiddlewareQueryException {
         updateGermplasmPrefNameAbbrev(gid, newPrefName, "Name");
         return gid;
     }
 
     @Override
     public Integer updateGermplasmPrefAbbrev(Integer gid, String newPrefAbbrev) throws MiddlewareQueryException {
         updateGermplasmPrefNameAbbrev(gid, newPrefAbbrev, "Abbreviation");
         return gid;
     }
 
     private void updateGermplasmPrefNameAbbrev(Integer gid, String newPrefValue, String nameOrAbbrev) throws MiddlewareQueryException {
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal == null) {
             throw new MiddlewareQueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // initialize session & transaction
         Session session = sessionForLocal;
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
                 throw new MiddlewareQueryException("Error in GermplasmpDataManager.updateGermplasmPrefNameAbbrev(gid=" + gid
                         + ", newPrefValue=" + newPrefValue + ", nameOrAbbrev=" + nameOrAbbrev
                         + "): The specified Germplasm Name does not exist.");
             }
 
             // end transaction, commit to database
             trans.commit();
         } catch (Exception e) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new MiddlewareQueryException("Error in GermplasmpDataManager.updateGermplasmPrefNameAbbrev(gid=" + gid
                     + ", newPrefValue=" + newPrefValue + ", nameOrAbbrev=" + nameOrAbbrev + "):  " + e.getMessage(), e);
         } finally {
             sessionForLocal.flush();
         }
     }
 
     @Override
     public Integer addGermplasmName(Name name) throws MiddlewareQueryException {
         List<Name> names = new ArrayList<Name>();
         names.add(name);
         List<Integer> ids = addOrUpdateGermplasmName(names, Operation.ADD);
         return ids.size() > 0 ? ids.get(0) : null;
     }
 
     @Override
     public List<Integer> addGermplasmName(List<Name> names) throws MiddlewareQueryException {
         return addOrUpdateGermplasmName(names, Operation.ADD);
     }
 
     @Override
     public Integer updateGermplasmName(Name name) throws MiddlewareQueryException {
         List<Name> names = new ArrayList<Name>();
         names.add(name);
         List<Integer> ids = addOrUpdateGermplasmName(names, Operation.UPDATE);
         return ids.size() > 0 ? ids.get(0) : null;
     }
 
     @Override
     public List<Integer> updateGermplasmName(List<Name> names) throws MiddlewareQueryException {
         return addOrUpdateGermplasmName(names, Operation.UPDATE);
     }
 
     private List<Integer> addOrUpdateGermplasmName(List<Name> names, Operation operation) throws MiddlewareQueryException {
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal == null) {
             throw new MiddlewareQueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // initialize session & transaction
         Session session = sessionForLocal;
         Transaction trans = null;
 
         int namesSaved = 0;
         List<Integer> idNamesSaved = new ArrayList<Integer>();
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
                 Name recordAdded = dao.saveOrUpdate(name);
                 idNamesSaved.add(recordAdded.getNid());
                 namesSaved++;
                 if (namesSaved % JDBC_BATCH_SIZE == 0) {
                     // flush a batch of inserts and release memory
                     dao.flush();
                     dao.clear();
                 }
             }
             // end transaction, commit to database
             trans.commit();
         } catch (Exception e) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new MiddlewareQueryException("Error while saving Germplasm Name: GermplasmDataManager.addOrUpdateGermplasmName(names="
                     + names + ", operation=" + operation + "): " + e.getMessage(), e);
         } finally {
             sessionForLocal.flush();
         }
 
         return idNamesSaved;
     }
 
     @Override
     public List<Attribute> getAttributesByGID(Integer gid) throws MiddlewareQueryException {
         AttributeDAO dao = new AttributeDAO();
         Session session = getSession(gid);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return new ArrayList<Attribute>();
         }
         return (List<Attribute>) dao.getByGID(gid); //attributes
     }
 
     @Override
     public Method getMethodByID(Integer id) {
         MethodDAO dao = new MethodDAO();
         Session session = getSession(id);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return null;
         }
 
         return (Method) dao.getById(id, false);
     }
 
     @Override
     public List<Method> getAllMethods() throws MiddlewareQueryException {
         
         List<Method> methods = new ArrayList<Method>();
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal != null) {
             MethodDAO dao = new MethodDAO();
             dao.setSession(sessionForLocal);
             methods.addAll(dao.getAllMethod());
         }
 
         if (sessionForCentral != null) {
             MethodDAO centralDao = new MethodDAO();
             centralDao.setSession(sessionForCentral);
             methods.addAll(centralDao.getAllMethod());
         }
 
         return methods;
 
     }
     
     @Override
     public long countAllMethods() throws MiddlewareQueryException {
 
         long count = 0;
 
         MethodDAO dao = new MethodDAO();
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             count = count + dao.countAll();
         }
 
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             count = count + dao.countAll();
         }
 
         return count;
     }
 
     @Override
     public List<Method> getMethodsByType(String type) throws MiddlewareQueryException {
         MethodDAO dao = new MethodDAO();
         List<Method> methods = new ArrayList<Method>();
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             methods.addAll(dao.getByType(type));
         }
 
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             methods.addAll(dao.getByType(type));
         }
 
         return methods;
     }
 
     @Override
     public List<Method> getMethodsByType(String type, int start, int numOfRows) throws MiddlewareQueryException {
         MethodDAO dao = new MethodDAO();
         List<Method> methods = new ArrayList<Method>();
 
         long centralCount = 0;
         long localCount = 0;
         long relativeLimit = 0;
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             centralCount = dao.countByType(type);
 
             if (centralCount > start) {
                 methods.addAll(dao.getByType(type, start, numOfRows));
                 relativeLimit = numOfRows - (centralCount - start);
                 if (relativeLimit > 0) {
                     if (sessionForLocal != null) {
                         dao.setSession(sessionForLocal);
                         localCount = dao.countByType(type);
                         if (localCount > 0) {
                             methods.addAll(dao.getByType(type, 0, (int) relativeLimit));
                         }
                     }
                 }
 
             } else {
                 relativeLimit = start - centralCount;
                 if (sessionForLocal != null) {
                     dao.setSession(sessionForLocal);
                     localCount = dao.countByType(type);
                     if (localCount > relativeLimit) {
                         methods.addAll(dao.getByType(type, (int) relativeLimit, numOfRows));
                     }
                 }
             }
 
         } else if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             localCount = dao.countByType(type);
             if (localCount > start) {
                 methods.addAll(dao.getByType(type, start, numOfRows));
             }
         }
 
         return methods;
     }
 
     @Override
     public long countMethodsByType(String type) throws MiddlewareQueryException {
         MethodDAO dao = new MethodDAO();
         long numberOfMethods = 0L;
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             numberOfMethods += dao.countByType(type);
         }
 
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             numberOfMethods += dao.countByType(type);
         }
 
         return numberOfMethods;
     }
 
     @Override
     public List<Method> getMethodsByGroup(String group) throws MiddlewareQueryException {
         MethodDAO dao = new MethodDAO();
         List<Method> methods = new ArrayList<Method>();
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             methods.addAll(dao.getByGroup(group));
         }
 
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             methods.addAll(dao.getByGroup(group));
         }
 
         return methods;
     }
 
     @Override
     public List<Method> getMethodsByGroup(String group, int start, int numOfRows) throws MiddlewareQueryException {
         MethodDAO dao = new MethodDAO();
         List<Method> methods = new ArrayList<Method>();
 
         long centralCount = 0;
         long localCount = 0;
         long relativeLimit = 0;
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             centralCount = dao.countByGroup(group);
 
             if (centralCount > start) {
                 methods.addAll(dao.getByGroup(group, start, numOfRows));
                 relativeLimit = numOfRows - (centralCount - start);
                 if (relativeLimit > 0) {
                     if (sessionForLocal != null) {
                         dao.setSession(sessionForLocal);
                         localCount = dao.countByGroup(group);
                         if (localCount > 0) {
                             methods.addAll(dao.getByGroup(group, 0, (int) relativeLimit));
                         }
                     }
                 }
 
             } else {
                 relativeLimit = start - centralCount;
                 if (sessionForLocal != null) {
                     dao.setSession(sessionForLocal);
                     localCount = dao.countByGroup(group);
                     if (localCount > relativeLimit) {
                         methods.addAll(dao.getByGroup(group, (int) relativeLimit, numOfRows));
                     }
                 }
             }
 
         } else if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             localCount = dao.countByGroup(group);
             if (localCount > start) {
                 methods.addAll(dao.getByGroup(group, start, numOfRows));
             }
         }
 
         return methods;
     }
 
     @Override
     public List<Method> getMethodsByGroupAndType(String group, String type) throws MiddlewareQueryException {
         MethodDAO dao = new MethodDAO();
         List<Method> methods = new ArrayList<Method>();
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             methods.addAll(dao.getByGroupAndType(group, type));
         }
 
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             methods.addAll(dao.getByGroupAndType(group, type));
         }
 
         return methods;
     }
 
     @Override
     public long countMethodsByGroup(String group) throws MiddlewareQueryException {
         long numberOfMethods = 0L;
         MethodDAO dao = new MethodDAO();
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             numberOfMethods += dao.countByGroup(group);
         }
 
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             numberOfMethods += dao.countByGroup(group);
         }
 
         return numberOfMethods;
 
     }
 
     @Override
     public Integer addMethod(Method method) throws MiddlewareQueryException {
         requireLocalDatabaseInstance();
 
         // initialize session & transaction
         Session session = getCurrentSessionForLocal();
         Transaction trans = null;
 
         int methodsSaved = 0;
         Integer methodId;
         try {
             // begin save transaction
             trans = session.beginTransaction();
             MethodDAO dao = new MethodDAO();
             dao.setSession(session);
 
             // Auto-assign negative IDs for new local DB records
             Integer negativeId = dao.getNegativeId("mid");
             method.setMid(negativeId);
 
             Method recordSaved = dao.saveOrUpdate(method);
             methodsSaved++;
             methodId = recordSaved.getMid();
 
             trans.commit();
         } catch (Exception e) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new MiddlewareQueryException("Error encountered while saving Method: GermplasmDataManager.addMethod(method=" + method
                     + "): " + e.getMessage(), e);
         } finally {
             session.flush();
         }
 
         return methodId;
     }
 
     @Override
     public List<Integer> addMethod(List<Method> methods) throws MiddlewareQueryException {
         requireLocalDatabaseInstance();
 
         // initialize session & transaction
         Session session = getCurrentSessionForLocal();
         Transaction trans = null;
 
         int methodsSaved = 0;
         List<Integer> idMethodsSaved = new ArrayList<Integer>();
         try {
             // begin save transaction
             trans = session.beginTransaction();
             MethodDAO dao = new MethodDAO();
             dao.setSession(session);
 
             for (Method method : methods) {
                 // Auto-assign negative IDs for new local DB records
                 Integer negativeId = dao.getNegativeId("mid");
                 method.setMid(negativeId);
 
                 Method recordSaved = dao.saveOrUpdate(method);
                 idMethodsSaved.add(recordSaved.getMid());
                 methodsSaved++;
             }
 
             trans.commit();
         } catch (Exception e) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new MiddlewareQueryException("Error encountered while saving a list of Methods: GermplasmDataManager.addMethod(methods="
                     + methods + "): " + e.getMessage(), e);
         } finally {
             session.flush();
         }
 
         return idMethodsSaved;
     }
 
     @Override
     public void deleteMethod(Method method) throws MiddlewareQueryException {
         requireLocalDatabaseInstance();
 
         Session session = getCurrentSessionForLocal();
         Transaction trans = null;
 
         try {
             // begin save transaction
             trans = session.beginTransaction();
 
             MethodDAO dao = new MethodDAO();
             dao.setSession(session);
 
             dao.makeTransient(method);
 
             trans.commit();
         } catch (Exception e) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new MiddlewareQueryException("Error encountered while deleting Method: GermplasmDataMananger.deleteMethod(method="
                     + method + "): " + e.getMessage(), e);
         } finally {
             session.flush();
         }
     }
 
     @Override
     public UserDefinedField getUserDefinedFieldByID(Integer id) {
         UserDefinedFieldDAO dao = new UserDefinedFieldDAO();
         Session session = getSession(id);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return null;
         }
 
         return (UserDefinedField) dao.getById(id, false);
     }
 
     @Override
     public Country getCountryById(Integer id) {
         CountryDAO dao = new CountryDAO();
         Session session = getSession(id);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return null;
         }
 
         Country country = dao.getById(id, false);
         return country;
     }
 
     @Override
     public Location getLocationByID(Integer id) {
         LocationDAO dao = new LocationDAO();
         Session session = getSession(id);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return null;
         }
 
         Location location = dao.getById(id, false);
         return location;
 
     }
 
     @Override
     public Integer addLocation(Location location) throws MiddlewareQueryException {
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal == null) {
             throw new MiddlewareQueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // initialize session & transaction
         Session session = sessionForLocal;
         Transaction trans = null;
 
         int locationsSaved = 0;
         Integer idLocationSaved;
         try {
             // begin save transaction
             trans = session.beginTransaction();
             LocationDAO dao = new LocationDAO();
             dao.setSession(session);
 
             // Auto-assign negative IDs for new local DB records
             Integer negativeId = dao.getNegativeId("locid");
             location.setLocid(negativeId);
 
             Location recordSaved = dao.saveOrUpdate(location);
             idLocationSaved = recordSaved.getLocid();
             locationsSaved++;
 
             trans.commit();
         } catch (Exception e) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new MiddlewareQueryException("Error encountered while saving Location: GermplasmDataManager.addLocation(location="
                     + location + "): " + e.getMessage(), e);
         } finally {
             sessionForLocal.flush();
         }
 
         return idLocationSaved;
     }
 
     @Override
     public List<Integer> addLocation(List<Location> locations) throws MiddlewareQueryException {
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal == null) {
             throw new MiddlewareQueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // initialize session & transaction
         Session session = sessionForLocal;
         Transaction trans = null;
 
         int locationsSaved = 0;
         List<Integer> idLocationsSaved = new ArrayList<Integer>();
         try {
             // begin save transaction
             trans = session.beginTransaction();
 
             LocationDAO dao = new LocationDAO();
             dao.setSession(session);
 
             for (Location location : locations) {
 
                 // Auto-assign negative IDs for new local DB records
                 Integer negativeId = dao.getNegativeId("locid");
                 location.setLocid(negativeId);
 
                 Location recordSaved = dao.saveOrUpdate(location);
                 idLocationsSaved.add(recordSaved.getLocid());
                 locationsSaved++;
             }
 
             trans.commit();
         } catch (Exception e) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new MiddlewareQueryException("Error encountered while saving Locations: GermplasmDataManager.addLocation(locations="
                     + locations + "): " + e.getMessage(), e);
         } finally {
             sessionForLocal.flush();
         }
 
         return idLocationsSaved;
     }
 
     @Override
     public void deleteLocation(Location location) throws MiddlewareQueryException {
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal == null) {
             throw new MiddlewareQueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         Transaction trans = null;
 
         try {
             // begin save transaction
             trans = sessionForLocal.beginTransaction();
 
             LocationDAO dao = new LocationDAO();
             dao.setSession(sessionForLocal);
 
             dao.makeTransient(location);
 
             trans.commit();
         } catch (Exception e) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new MiddlewareQueryException("Error encountered while deleting Location: GermplasmDataManager.deleteLocation(location="
                     + location + "): " + e.getMessage(), e);
         } finally {
             sessionForLocal.flush();
         }
     }
 
     @Override
     public Bibref getBibliographicReferenceByID(Integer id) {
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         BibrefDAO dao = new BibrefDAO();
         if (id < 0 && sessionForLocal != null) {
             dao.setSession(sessionForLocal);
         } else if (id > 0 && sessionForCentral != null) {
             dao.setSession(sessionForCentral);
         } else {
             return null;
         }
 
         Bibref bibref = dao.getById(id, false);
         return bibref;
     }
 
     @Override
     public Integer addBibliographicReference(Bibref bibref) throws MiddlewareQueryException {
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal == null) {
             throw new MiddlewareQueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // initialize session & transaction
         Session session = sessionForLocal;
         Transaction trans = null;
 
         int bibrefSaved = 0;
         Integer idBibrefSaved;
         try {
             // begin save transaction
             trans = session.beginTransaction();
             BibrefDAO dao = new BibrefDAO();
             dao.setSession(session);
 
             // Auto-assign negative IDs for new local DB records
             Integer negativeId = dao.getNegativeId("refid");
             bibref.setRefid(negativeId);
 
             Bibref recordSaved = dao.saveOrUpdate(bibref);
             idBibrefSaved = recordSaved.getRefid();
             bibrefSaved++;
 
             trans.commit();
         } catch (Exception e) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new MiddlewareQueryException(
                     "Error encountered while saving Bibliographic Reference: GermplasmDataManager.addBibliographicReference(bibref="
                             + bibref + "): " + e.getMessage(), e);
         } finally {
             sessionForLocal.flush();
         }
 
         return idBibrefSaved;
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
      * ��� in the name and L is not preceded by a space m) ^/ becomes / and /^
      * becomes /
      * 
      * @param name
      * @return the standardized germplasm name
      */
     public static String standardizeName(String name) {
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
                     // L-N becomes L^N when there is only one ��� in the name
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
 
     
     private static String removeSpaces(String string){
         StringTokenizer tokenizer = new StringTokenizer(string);
         StringBuffer withSpacesRemoved = new StringBuffer();
         while (tokenizer.hasMoreTokens()) {
             withSpacesRemoved.append(tokenizer.nextToken());
         }
         return withSpacesRemoved.toString();
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
     public Germplasm getParentByGIDAndProgenitorNumber(Integer gid, Integer progenitorNumber) throws MiddlewareQueryException {
         GermplasmDAO dao = new GermplasmDAO();
         Session session = getSession(gid);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return null;
         }
 
         Germplasm parent = dao.getProgenitorByGID(gid, progenitorNumber);
 
         return parent;
 
     }
 
     @Override
     public List<Object[]> getDescendants(Integer gid, int start, int numOfRows) throws MiddlewareQueryException {
         GermplasmDAO dao = new GermplasmDAO();
         ProgenitorDAO pDao = new ProgenitorDAO();
         List<Object[]> result = new ArrayList<Object[]>();
         Object[] germplasmList;
 
         Session session = getSession(gid);
 
         if (session != null) {
             dao.setSession(session);
             pDao.setSession(session);
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
     public long countDescendants(Integer gid) throws MiddlewareQueryException {
         GermplasmDAO dao = new GermplasmDAO();
 
         Session session = getSession(gid);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return 0;
         }
 
         return dao.countGermplasmDescendantByGID(gid);
     }
 
     @Override
     public GermplasmPedigreeTree generatePedigreeTree(Integer gid, int level) throws MiddlewareQueryException {
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
      * @throws MiddlewareQueryException
      */
     private GermplasmPedigreeTreeNode addParents(GermplasmPedigreeTreeNode node, int level) throws MiddlewareQueryException {
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
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
 
                     if (germplasmOfNode.getGid() < 0 && sessionForLocal != null) {
                         GermplasmDAO dao = new GermplasmDAO();
                         dao.setSession(sessionForLocal);
                         otherParents = dao.getProgenitorsByGIDWithPrefName(germplasmOfNode.getGid());
                     } else if (germplasmOfNode.getGid() > 0 && sessionForCentral != null) {
                         GermplasmDAO centralDao = new GermplasmDAO();
                         centralDao.setSession(sessionForCentral);
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
     public List<Germplasm> getManagementNeighbors(Integer gid, int start, int numOfRows) throws MiddlewareQueryException {
         GermplasmDAO dao = new GermplasmDAO();
         List<Germplasm> germplasms = new ArrayList<Germplasm>();
 
         long centralCount = 0;
         long localCount = 0;
         long relativeLimit = 0;
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             centralCount = dao.countManagementNeighbors(gid);
 
             if (centralCount > start) {
                 germplasms.addAll(dao.getManagementNeighbors(gid, start, numOfRows));
                 relativeLimit = numOfRows - (centralCount - start);
                 if (relativeLimit > 0) {
                     if (sessionForLocal != null) {
                         dao.setSession(sessionForLocal);
                         localCount = dao.countManagementNeighbors(gid);
                         if (localCount > 0) {
                             germplasms.addAll(dao.getManagementNeighbors(gid, 0, (int) relativeLimit));
                         }
                     }
                 }
 
             } else {
                 relativeLimit = start - centralCount;
                 if (sessionForLocal != null) {
                     dao.setSession(sessionForLocal);
                     localCount = dao.countManagementNeighbors(gid);
                     if (localCount > relativeLimit) {
                         germplasms.addAll(dao.getManagementNeighbors(gid, (int) relativeLimit, numOfRows));
                     }
                 }
             }
 
         } else if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             localCount = dao.countManagementNeighbors(gid);
             if (localCount > start) {
                 germplasms.addAll(dao.getManagementNeighbors(gid, start, numOfRows));
             }
         }
 
         return germplasms;
     }
 
     
     public long countManagementNeighbors(Integer gid) throws MiddlewareQueryException{
         long count = 0;
 
         GermplasmDAO dao = new GermplasmDAO();
 
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             count = count + dao.countManagementNeighbors(gid);
         }
 
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             count = count + dao.countManagementNeighbors(gid);
         }
 
         return count;
     }
 
     @Override
     public long countGroupRelatives(Integer gid) throws MiddlewareQueryException {
         GermplasmDAO dao = new GermplasmDAO();
 
         Session session = getSession(gid);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return 0;
         }
         
         return dao.countGroupRelatives(gid);
     }
     
     @Override
     public List<Germplasm> getGroupRelatives(Integer gid, int start, int numRows) throws MiddlewareQueryException {
         GermplasmDAO dao = new GermplasmDAO();
 
         Session session = getSession(gid);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return new ArrayList<Germplasm>();
         }
 
         List<Germplasm> relatives = dao.getGroupRelatives(gid, start, numRows);
         return relatives;
     }
 
     @Override
     public List<Germplasm> getGenerationHistory(Integer gid) throws MiddlewareQueryException {
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
             throws MiddlewareQueryException {
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
             int treeLevel = numberOfStepsBackward - stepsLeft + numberOfStepsForward;
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
      * @throws MiddlewareQueryException
      */
     private Object[] traceDerivativeRoot(Integer gid, int steps) throws MiddlewareQueryException {
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
      * @throws MiddlewareQueryException
      */
     private GermplasmPedigreeTreeNode getDerivedLines(GermplasmPedigreeTreeNode node, int steps) throws MiddlewareQueryException {
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (steps <= 0) {
             return node;
         } else {
             List<Germplasm> derivedGermplasms = new ArrayList<Germplasm>();
             Integer gid = node.getGermplasm().getGid();
 
             if (gid < 0 && sessionForLocal != null) {
                 GermplasmDAO dao = new GermplasmDAO();
                 dao.setSession(sessionForLocal);
                 derivedGermplasms = dao.getDerivativeChildren(gid);
             } else if (gid > 0 && sessionForCentral != null) {
                 GermplasmDAO dao = new GermplasmDAO();
                 dao.setSession(sessionForCentral);
                 derivedGermplasms = dao.getDerivativeChildren(gid);
 
                 if (sessionForLocal != null) {
                     GermplasmDAO localDao = new GermplasmDAO();
                     localDao.setSession(sessionForLocal);
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
     public Integer addGermplasmAttribute(Attribute attribute) throws MiddlewareQueryException {
         List<Attribute> attributes = new ArrayList<Attribute>();
         attributes.add(attribute);
         List<Integer> ids = addGermplasmAttribute(attributes);
         return ids.size() > 0 ? ids.get(0) : null;
     }
 
     @Override
     public List<Integer> addGermplasmAttribute(List<Attribute> attributes) throws MiddlewareQueryException {
         return addOrUpdateAttributes(attributes, Operation.ADD);
     }
 
     @Override
     public Integer updateGermplasmAttribute(Attribute attribute) throws MiddlewareQueryException {
         List<Attribute> attributes = new ArrayList<Attribute>();
         attributes.add(attribute);
         List<Integer> ids = updateGermplasmAttribute(attributes);
         return ids.size() > 0 ? ids.get(0) : null;
     }
 
     @Override
     public List<Integer> updateGermplasmAttribute(List<Attribute> attributes) throws MiddlewareQueryException {
         return addOrUpdateAttributes(attributes, Operation.UPDATE);
     }
 
     private List<Integer> addOrUpdateAttributes(List<Attribute> attributes, Operation operation) throws MiddlewareQueryException {
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal == null) {
             throw new MiddlewareQueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // initialize session & transaction
         Session session = sessionForLocal;
         Transaction trans = null;
 
         int attributesSaved = 0;
         List<Integer> idAttributesSaved = new ArrayList<Integer>();
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
                 Attribute recordSaved = dao.saveOrUpdate(attribute);
                 idAttributesSaved.add(recordSaved.getAid());
                 attributesSaved++;
             }
             // end transaction, commit to database
             trans.commit();
         } catch (Exception e) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new MiddlewareQueryException(
                     "Error encountered while saving Attribute: GermplasmDataManager.addOrUpdateAttributes(attributes=" + attributes + "): "
                             + e.getMessage(), e);
         } finally {
             sessionForLocal.flush();
         }
 
         return idAttributesSaved;
     }
 
     @Override
     public Attribute getAttributeById(Integer id) {
         AttributeDAO dao = new AttributeDAO();
         Session session = getSession(id);
 
         if (session != null) {
             dao.setSession(session);
         } else {
             return null;
         }
 
         return dao.getById(id, false);
     }
 
     @Override
     public Integer updateProgenitor(Integer gid, Integer progenitorId, Integer progenitorNumber) throws MiddlewareQueryException {
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal == null) {
             throw new MiddlewareQueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // check if the germplasm record identified by gid exists
         Germplasm child = getGermplasmByGID(gid);
         if (child == null) {
             throw new MiddlewareQueryException("Error in GermplasmDataManager.updateProgenitor(gid=" + gid + ", progenitorId="
                     + progenitorId + ", progenitorNumber=" + progenitorNumber + "): There is no germplasm record with gid: " + gid);
         }
 
         // check if the germplasm record identified by progenitorId exists
         Germplasm parent = getGermplasmByGID(progenitorId);
         if (parent == null) {
             throw new MiddlewareQueryException("Error in GermplasmDataManager.updateProgenitor(gid=" + gid + ", progenitorId="
                     + progenitorId + ", progenitorNumber=" + progenitorNumber + "): There is no germplasm record with progenitorId: "
                     + progenitorId);
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
                 addOrUpdateGermplasms(germplasms, Operation.UPDATE);
             } else {
                 throw new MiddlewareQueryException("Error in GermplasmDataManager.updateProgenitor(gid=" + gid + ", progenitorId="
                         + progenitorId + ", progenitorNumber=" + progenitorNumber
                         + "): The gid supplied as parameter does not refer to a local record. Only local records may be updated.");
             }
         } else if (progenitorNumber > 2) {
             ProgenitorDAO dao = new ProgenitorDAO();
             dao.setSession(sessionForLocal);
 
             // check if there is an existing Progenitor record
             ProgenitorPK id = new ProgenitorPK(gid, progenitorNumber);
             Progenitor p = dao.getById(id, false);
 
             if (p != null) {
                 // update the existing record
                 p.setPid(progenitorId);
 
                 List<Progenitor> progenitors = new ArrayList<Progenitor>();
                 progenitors.add(p);
                 int updated = addOrUpdateProgenitors(progenitors);
                 if (updated == 1) {
                     return progenitorId;
                 }
             } else {
                 // create new Progenitor record
                 Progenitor newRecord = new Progenitor(id);
                 newRecord.setPid(progenitorId);
 
                 List<Progenitor> progenitors = new ArrayList<Progenitor>();
                 progenitors.add(newRecord);
                 int added = addOrUpdateProgenitors(progenitors);
                 if (added == 1) {
                     return progenitorId;
                 }
             }
         } else {
             throw new MiddlewareQueryException("Error in GermplasmDataManager.updateProgenitor(gid=" + gid + ", progenitorId="
                     + progenitorId + ", progenitorNumber=" + progenitorNumber + "): Invalid progenitor number: " + progenitorNumber);
         }
 
         return progenitorId;
     }
 
     private List<Integer> addOrUpdateGermplasms(List<Germplasm> germplasms, Operation operation) throws MiddlewareQueryException {
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal == null) {
             throw new MiddlewareQueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // initialize session & transaction
         Session session = sessionForLocal;
         Transaction trans = null;
 
         int germplasmsSaved = 0;
         List<Integer> idGermplasmsSaved = new ArrayList<Integer>();
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
                 Germplasm recordSaved = dao.saveOrUpdate(germplasm);
                 idGermplasmsSaved.add(recordSaved.getGid());
                 germplasmsSaved++;
                 if (germplasmsSaved % JDBC_BATCH_SIZE == 0) {
                     // flush a batch of inserts and release memory
                     dao.flush();
                     dao.clear();
                 }
             }
             // end transaction, commit to database
             trans.commit();
         } catch (Exception e) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new MiddlewareQueryException(
                     "Error encountered while saving Germplasm: GermplasmDataManager.addOrUpdateGermplasms(germplasms=" + germplasms
                             + ", operation=" + operation + "): " + e.getMessage(), e);
         } finally {
             sessionForLocal.flush();
         }
 
         return idGermplasmsSaved;
     }
 
     private int addOrUpdateProgenitors(List<Progenitor> progenitors) throws MiddlewareQueryException {
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal == null) {
             throw new MiddlewareQueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // initialize session & transaction
         Session session = sessionForLocal;
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
         } catch (Exception e) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new MiddlewareQueryException(
                     "Error encountered while saving Progenitor: GermplasmDataManager.addOrUpdateProgenitors(progenitors=" + progenitors
                             + "): " + e.getMessage(), e);
         } finally {
             sessionForLocal.flush();
         }
 
         return progenitorsSaved;
     }
 
     @Override
     public Integer updateGermplasm(Germplasm germplasm) throws MiddlewareQueryException {
         List<Germplasm> germplasms = new ArrayList<Germplasm>();
         germplasms.add(germplasm);
         List<Integer> ids = updateGermplasm(germplasms);
         return ids.size() > 0 ? ids.get(0) : null;
     }
 
     @Override
     public List<Integer> updateGermplasm(List<Germplasm> germplasms) throws MiddlewareQueryException {
         return addOrUpdateGermplasms(germplasms, Operation.UPDATE);
     }
 
     @Override
     public Integer addGermplasm(Germplasm germplasm, Name preferredName) throws MiddlewareQueryException {
         Map<Germplasm, Name> germplasmNameMap = new HashMap<Germplasm, Name>();
         germplasm.setGid(Integer.valueOf(1));
         germplasmNameMap.put(germplasm, preferredName);
         List<Integer> ids = addGermplasm(germplasmNameMap);
         return ids.size() > 0 ? ids.get(0) : null;
     }
 
     @Override
     public List<Integer> addGermplasm(Map<Germplasm, Name> germplasmNameMap) throws MiddlewareQueryException {
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForLocal == null) {
             throw new MiddlewareQueryException(NO_LOCAL_INSTANCE_MSG);
         }
 
         // initialize session & transaction
         Session session = sessionForLocal;
         Transaction trans = null;
 
         int germplasmsSaved = 0;
         List<Integer> idGermplasmsSaved = new ArrayList<Integer>();
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
 
                 Germplasm germplasmSaved = dao.saveOrUpdate(germplasm);
                 idGermplasmsSaved.add(germplasmSaved.getGid());
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
         } catch (Exception e) {
             // rollback transaction in case of errors
             if (trans != null) {
                 trans.rollback();
             }
             throw new MiddlewareQueryException(
                     "Error encountered while saving Germplasm: GermplasmDataManager.addGermplasm(germplasmNameMap=" + germplasmNameMap
                             + "): " + e.getMessage(), e);
         } finally {
             sessionForLocal.flush();
         }
 
         return idGermplasmsSaved;
     }
 
     /**
      * @Override public List<Germplasm> getGermplasmByExample(Germplasm sample,
      *           int start, int numOfRows) { GermplasmDAO dao = new
      *           GermplasmDAO();
      *           dao.setSession(session); return
      *           dao.getByExample(sample, start, numOfRows); }
      * @Override public long countGermplasmByExample(Germplasm sample) {
      *           GermplasmDAO dao = new GermplasmDAO();
      *           dao.setSession(session); return
      *           dao.countByExample(sample).intValue(); }
      **/
 
     @Override
     public List<GidNidElement> getGidAndNidByGermplasmNames(List<String> germplasmNames) throws MiddlewareQueryException {
 
         Session sessionForCentral = getSession(Database.CENTRAL);
         Session sessionForLocal = getSession(Database.LOCAL);
 
         List<GidNidElement> results = new ArrayList<GidNidElement>();
         NameDAO dao = new NameDAO();
 
         if (sessionForCentral != null) {
             dao.setSession(sessionForCentral);
             results.addAll(dao.getGidAndNidByGermplasmNames(germplasmNames));
         }
 
         if (sessionForLocal != null) {
             dao.setSession(sessionForLocal);
             results.addAll(dao.getGidAndNidByGermplasmNames(germplasmNames));
         }
 
         return results;
     }
     
     @Override
     public List<Country> getAllCountry() throws MiddlewareQueryException {
 		
 	        List<Country> country = new ArrayList<Country>();
 
 	        Session sessionForCentral = getCurrentSessionForCentral();
 	        Session sessionForLocal = getCurrentSessionForLocal();
 
 	        if (sessionForLocal != null) {
 	            CountryDAO dao = new CountryDAO();
 	            dao.setSession(sessionForLocal);
 	            country.addAll(dao.getAllCountry());
 	        }
 
 	        if (sessionForCentral != null) {
 	        	CountryDAO centralDao = new CountryDAO();
 	            centralDao.setSession(sessionForCentral);
 	            country.addAll(centralDao.getAllCountry());
 	        }
 
 	        return country;
 
 	    }
 
 	@Override
 	public List<Location> getLocationsByCountryAndType(Country country,
 			Integer type) throws MiddlewareQueryException {
             LocationDAO dao = new LocationDAO();
     
             Session sessionForCentral = getCurrentSessionForCentral();
             Session sessionForLocal = getCurrentSessionForLocal();
             List<Location> locations = new ArrayList<Location>();
     
             if (sessionForLocal != null) {
                 dao.setSession(sessionForLocal);
                 locations.addAll(dao.getByCountryAndType(country,type));
             }
     
             if (sessionForCentral != null) {
                 dao.setSession(sessionForCentral);
                 locations.addAll(dao.getByCountryAndType(country,type));
             }
     
             return locations;
 	}
 
 	@Override
 	public List<UserDefinedField> getUserDefinedFieldByFieldTableNameAndType(String tableName,String fieldType) throws MiddlewareQueryException {
             UserDefinedFieldDAO dao = new UserDefinedFieldDAO();
             Session sessionForCentral = getCurrentSessionForCentral();
             Session sessionForLocal = getCurrentSessionForLocal();
     
             List<UserDefinedField> userDefineField = new ArrayList<UserDefinedField>();
     
             if (sessionForLocal != null) {
                 dao.setSession(sessionForLocal);
                 userDefineField.addAll(dao.getByFieldTableNameAndType(tableName,fieldType));
             }
     
             if (sessionForCentral != null) {
                 dao.setSession(sessionForCentral);
                 userDefineField.addAll(dao.getByFieldTableNameAndType(tableName,fieldType));
             }
     
             return userDefineField;
 	}
 	
 	@Override
 	public List<Method> getMethodsByGroupIncludesGgroup(String group)throws MiddlewareQueryException {
 		 MethodDAO dao = new MethodDAO();
 	        List<Method> methods = new ArrayList<Method>();
 
 	        Session sessionForCentral = getCurrentSessionForCentral();
 	        Session sessionForLocal = getCurrentSessionForLocal();
 
 	        if (sessionForLocal != null) {
 	            dao.setSession(sessionForLocal);
 	            methods.addAll(dao.getByGroupIncludesGgroup(group));
 	        }
 
 	        if (sessionForCentral != null) {
 	            dao.setSession(sessionForCentral);
 	            methods.addAll(dao.getByGroupIncludesGgroup(group));
 	        }
 
 	        return methods;
 	}
 	
     public String getCrossExpansion(Integer gid, int level) throws MiddlewareQueryException {
         Germplasm germplasm = getGermplasmWithPrefName(gid);
         if(germplasm != null){
             SingleGermplasmCrossElement startElement = new SingleGermplasmCrossElement();
             startElement.setGermplasm(germplasm);
             GermplasmCrossElement cross = expandGermplasmCross(startElement, level);
             return cross.toString();
         } else{
             return null;
         }
     }
     
     private GermplasmCrossElement expandGermplasmCross(GermplasmCrossElement element, int level) throws MiddlewareQueryException {
         if(level == 0){
             //if the level is zero then there is no need to expand and the element
             //should be returned as is
             return element;
         } else{
             if(element instanceof SingleGermplasmCrossElement){
                 SingleGermplasmCrossElement singleGermplasm = (SingleGermplasmCrossElement) element;
                 Germplasm germplasmToExpand = singleGermplasm.getGermplasm();
                 
                 if(germplasmToExpand.getGnpgs() < 0){
                     //for germplasms created via a derivative or maintenance method
                     //skip and then expand on the gpid1 parent
                     if(germplasmToExpand.getGpid1() != 0 && germplasmToExpand.getGpid1() != null){
                         SingleGermplasmCrossElement nextElement = new SingleGermplasmCrossElement();
                         nextElement.setGermplasm(getGermplasmWithPrefName(germplasmToExpand.getGpid1()));
                         return expandGermplasmCross(nextElement, level);
                     } else{
                         return element;
                     }
                 } else{
                     GermplasmCross cross = new GermplasmCross();
                     
                     Method method = getMethodByID(germplasmToExpand.getMethodId());
                     if(method != null){
                         String methodName = method.getMname();
                         if(methodName != null){
                             methodName = methodName.toLowerCase();
                         } else{
                             methodName = "";
                         }
                         
                         if(methodName.contains("single cross")){
                             //get the immediate parents
                             Germplasm firstParent = getGermplasmWithPrefName(germplasmToExpand.getGpid1());
                             checkIfGermplasmIsNull(firstParent, germplasmToExpand.getGpid1());
                             Germplasm secondParent = getGermplasmWithPrefName(germplasmToExpand.getGpid2());
                             checkIfGermplasmIsNull(secondParent, germplasmToExpand.getGpid2());
                             SingleGermplasmCrossElement firstParentElem = new SingleGermplasmCrossElement();
                             firstParentElem.setGermplasm(firstParent);
                             SingleGermplasmCrossElement secondParentElem = new SingleGermplasmCrossElement();
                             secondParentElem.setGermplasm(secondParent);
                             
                             //expand the parents as needed, depends on the level
                             GermplasmCrossElement expandedFirstParent = expandGermplasmCross(firstParentElem, level-1);
                             GermplasmCrossElement expandedSecondParent = expandGermplasmCross(secondParentElem, level-1);
                             
                             //get the number of crosses in the first parent
                             int numOfCrosses = 0;
                             if(expandedFirstParent instanceof GermplasmCross){
                                 numOfCrosses = ((GermplasmCross) expandedFirstParent).getNumberOfCrossesBefore() + 1;
                             }
                             
                             cross.setFirstParent(expandedFirstParent);
                             cross.setSecondParent(expandedSecondParent);
                             cross.setNumberOfCrossesBefore(numOfCrosses);
                             
                         } else if(methodName.contains("double cross")){
                             //get the grandparents on both sides
                             Germplasm firstParent = getGermplasmByGID(germplasmToExpand.getGpid1());
                             checkIfGermplasmIsNull(firstParent, germplasmToExpand.getGpid1());
                             Germplasm secondParent = getGermplasmByGID(germplasmToExpand.getGpid2());
                             checkIfGermplasmIsNull(secondParent, germplasmToExpand.getGpid2());
                             
                             Germplasm firstGrandParent = getGermplasmWithPrefName(firstParent.getGpid1());
                             checkIfGermplasmIsNull(firstGrandParent, firstParent.getGpid1());
                             SingleGermplasmCrossElement firstGrandParentElem = new SingleGermplasmCrossElement();
                             firstGrandParentElem.setGermplasm(firstGrandParent);
                             Germplasm secondGrandParent = getGermplasmWithPrefName(firstParent.getGpid2());
                             checkIfGermplasmIsNull(secondGrandParent, secondParent.getGpid2());
                             SingleGermplasmCrossElement secondGrandParentElem = new SingleGermplasmCrossElement();
                             secondGrandParentElem.setGermplasm(secondGrandParent);
                             
                             Germplasm thirdGrandParent = getGermplasmWithPrefName(secondParent.getGpid1());
                             checkIfGermplasmIsNull(thirdGrandParent, secondParent.getGpid1());
                             SingleGermplasmCrossElement thirdGrandParentElem = new SingleGermplasmCrossElement();
                             thirdGrandParentElem.setGermplasm(thirdGrandParent);
                             Germplasm fourthGrandParent = getGermplasmWithPrefName(secondParent.getGpid2());
                             checkIfGermplasmIsNull(fourthGrandParent, secondParent.getGpid2());
                             SingleGermplasmCrossElement fourthGrandParentElem = new SingleGermplasmCrossElement();
                             fourthGrandParentElem.setGermplasm(fourthGrandParent);
                             
                             //expand the grand parents as needed, depends on the level
                             GermplasmCrossElement expandedFirstGrandParent = expandGermplasmCross(firstGrandParentElem, level-1);
                             GermplasmCrossElement expandedSecondGrandParent = expandGermplasmCross(secondGrandParentElem, level-1);
                             GermplasmCrossElement expandedThirdGrandParent = expandGermplasmCross(thirdGrandParentElem, level-1);
                             GermplasmCrossElement expandedFourthGrandParent = expandGermplasmCross(fourthGrandParentElem, level-1);
                             
                             //create the cross object for the first pair of grand parents
                             GermplasmCross firstCross = new GermplasmCross();
                             firstCross.setFirstParent(expandedFirstGrandParent);
                             firstCross.setSecondParent(expandedSecondGrandParent);
                             //compute the number of crosses before this cross
                             int numOfCrossesForFirst = 0;
                             if(expandedFirstGrandParent instanceof GermplasmCross){
                                 numOfCrossesForFirst = ((GermplasmCross) expandedFirstGrandParent).getNumberOfCrossesBefore() + 1;
                             }
                             firstCross.setNumberOfCrossesBefore(numOfCrossesForFirst);
                             
                             //create the cross object for the second pair of grand parents
                             GermplasmCross secondCross = new GermplasmCross();
                             secondCross.setFirstParent(expandedThirdGrandParent);
                             secondCross.setSecondParent(expandedFourthGrandParent);
                             //compute the number of crosses before this cross
                             int numOfCrossesForSecond = 0;
                             if(expandedThirdGrandParent instanceof GermplasmCross){
                                 numOfCrossesForSecond = ((GermplasmCross) expandedThirdGrandParent).getNumberOfCrossesBefore() + 1;
                             }
                             
                             //create the cross of the two sets of grandparents, this will be returned
                             cross.setFirstParent(firstCross);
                             cross.setSecondParent(secondCross);
                             //compute the number of crosses before the cross to be returned
                             int numOfCrosses = numOfCrossesForFirst + 1;
                             if(expandedSecondGrandParent instanceof GermplasmCross){
                                 numOfCrosses = numOfCrosses 
                                     + ((GermplasmCross) expandedSecondGrandParent).getNumberOfCrossesBefore() + 1; 
                             }
                             cross.setNumberOfCrossesBefore(numOfCrosses);
                             
                         } else if(methodName.contains("three-way cross")){
                             //get the two parents first
                             Germplasm firstParent = getGermplasmByGID(germplasmToExpand.getGpid1());
                             checkIfGermplasmIsNull(firstParent, germplasmToExpand.getGpid1());
                             Germplasm secondParent = getGermplasmByGID(germplasmToExpand.getGpid2());
                             checkIfGermplasmIsNull(secondParent, germplasmToExpand.getGpid2());
                             
                             //check for the parent generated by a cross, the other one should be a derived germplasm
                             if(firstParent.getGnpgs() > 0){
                                 // the first parent is the one created by a cross
                                 Germplasm firstGrandParent = getGermplasmWithPrefName(firstParent.getGpid1());
                                 checkIfGermplasmIsNull(firstGrandParent, firstParent.getGpid1());
                                 SingleGermplasmCrossElement firstGrandParentElem = new SingleGermplasmCrossElement();
                                 firstGrandParentElem.setGermplasm(firstGrandParent);
                                 
                                 Germplasm secondGrandParent = getGermplasmWithPrefName(firstParent.getGpid2());
                                 checkIfGermplasmIsNull(secondGrandParent, firstParent.getGpid2());
                                 SingleGermplasmCrossElement secondGrandParentElem = new SingleGermplasmCrossElement();
                                 secondGrandParentElem.setGermplasm(secondGrandParent);
                                 
                                 //expand the grand parents as needed, depends on the level
                                 GermplasmCrossElement expandedFirstGrandParent = expandGermplasmCross(firstGrandParentElem, level-1);
                                 GermplasmCrossElement expandedSecondGrandParent = expandGermplasmCross(secondGrandParentElem, level-1);
                                 
                                 //make the cross object for the grand parents
                                 GermplasmCross crossForGrandParents = new GermplasmCross();
                                 crossForGrandParents.setFirstParent(expandedFirstGrandParent);
                                 crossForGrandParents.setSecondParent(expandedSecondGrandParent);
                                 //compute the number of crosses before this one
                                 int numOfCrossesForGrandParents = 0;
                                 if(expandedFirstGrandParent instanceof GermplasmCross){
                                     numOfCrossesForGrandParents = ((GermplasmCross) expandedFirstGrandParent).getNumberOfCrossesBefore() + 1;
                                 }
                                 crossForGrandParents.setNumberOfCrossesBefore(numOfCrossesForGrandParents);
                                 
                                 //make the element for the second parent
                                 secondParent = getGermplasmWithPrefName(germplasmToExpand.getGpid2());
                                 checkIfGermplasmIsNull(secondParent, germplasmToExpand.getGpid2());
                                 SingleGermplasmCrossElement secondParentElem = new SingleGermplasmCrossElement();
                                 secondParentElem.setGermplasm(secondParent);
                                 
                                 // create the cross to return
                                 cross.setFirstParent(crossForGrandParents);
                                 cross.setSecondParent(secondParentElem);
                                 //compute the number of crosses before this cross
                                 cross.setNumberOfCrossesBefore(numOfCrossesForGrandParents + 1);
                             } else{
                                 // the second parent is the one created by a cross
                                 Germplasm firstGrandParent = getGermplasmWithPrefName(secondParent.getGpid1());
                                 checkIfGermplasmIsNull(firstGrandParent, secondParent.getGpid1());
                                 SingleGermplasmCrossElement firstGrandParentElem = new SingleGermplasmCrossElement();
                                 firstGrandParentElem.setGermplasm(firstGrandParent);
                                 
                                 Germplasm secondGrandParent = getGermplasmWithPrefName(secondParent.getGpid2());
                                 checkIfGermplasmIsNull(secondGrandParent, secondParent.getGpid2());
                                 SingleGermplasmCrossElement secondGrandParentElem = new SingleGermplasmCrossElement();
                                 secondGrandParentElem.setGermplasm(secondGrandParent);
                                 
                                 //expand the grand parents as needed, depends on the level
                                 GermplasmCrossElement expandedFirstGrandParent = expandGermplasmCross(firstGrandParentElem, level-1);
                                 GermplasmCrossElement expandedSecondGrandParent = expandGermplasmCross(secondGrandParentElem, level-1);
                                 
                                 //make the cross object for the grand parents
                                 GermplasmCross crossForGrandParents = new GermplasmCross();
                                 crossForGrandParents.setFirstParent(expandedFirstGrandParent);
                                 crossForGrandParents.setSecondParent(expandedSecondGrandParent);
                                 //compute the number of crosses before this one
                                 int numOfCrossesForGrandParents = 0;
                                 if(expandedFirstGrandParent instanceof GermplasmCross){
                                     numOfCrossesForGrandParents = ((GermplasmCross) expandedFirstGrandParent).getNumberOfCrossesBefore() + 1;
                                 }
                                 crossForGrandParents.setNumberOfCrossesBefore(numOfCrossesForGrandParents);
                                 
                                 //make the element for the first parent
                                 firstParent = getGermplasmWithPrefName(germplasmToExpand.getGpid1());
                                 checkIfGermplasmIsNull(firstParent, germplasmToExpand.getGpid1());
                                 SingleGermplasmCrossElement firstParentElem = new SingleGermplasmCrossElement();
                                 firstParentElem.setGermplasm(firstParent);
                                 
                                 //create the cross to return
                                 cross.setFirstParent(crossForGrandParents);
                                 cross.setSecondParent(firstParentElem);
                                 cross.setNumberOfCrossesBefore(numOfCrossesForGrandParents + 1);
                             }
                             
                         }
                         
                         return cross;
                     } else{
                         throw new MiddlewareQueryException("Error with expanding cross, can not find method with id: " + germplasmToExpand.getMethodId());
                     }
                 }
             } else{
                 throw new MiddlewareQueryException("expandGermplasmCross was incorrectly called");
             }
         }
     }
     
     private void checkIfGermplasmIsNull(Germplasm germplasm, Integer gid) throws MiddlewareQueryException{
         if(germplasm == null){
             throw new MiddlewareQueryException("There is no germplasm with id: " + gid);
         }
     }
     
     @Override
     public List<ParentElement> getAllParentsFromMappingPopulation(
             int start, int numOfRows) throws MiddlewareQueryException {
 
         Long centralCount = Long.valueOf(0);
         Long localCount = Long.valueOf(0);
         int relativeLimit = 0;    	
     	
         MappingPopDAO mappingPopDao = new MappingPopDAO();
        
         List<ParentElement> allParentsFromMappingPopulation = new ArrayList<ParentElement>();
   /**
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForCentral != null) {
             mappingPopDao.setSession(sessionForCentral);
             centralCount = mappingPopDao.countAllParentsFromMappingPopulation();
             
             if (centralCount > start) {
                 allParentsFromMappingPopulation.addAll(mappingPopDao.getAllParentsFromMappingPopulation(start, numOfRows));
                relativeLimit = numOfRows - start;
 
                 if (relativeLimit > 0) {
                 	
                     if (sessionForLocal != null) {
                         mappingPopDao.setSession(sessionForLocal);
                         localCount = mappingPopDao.countAllParentsFromMappingPopulation();
                         
                         if (localCount > 0) {
                            //allParentsFromMappingPopulation.addAll(mappingPopDao.getAllParentsFromMappingPopulation(0, relativeLimit.intValue()));
                         }
                     }
                 }
             } else {
                 relativeLimit = start - centralCount.intValue();
                 if (sessionForLocal != null) {
                     mappingPopDao.setSession(sessionForLocal);
                     localCount = mappingPopDao.countAllParentsFromMappingPopulation();
                     if (localCount > relativeLimit) {
                        //allParentsFromMappingPopulation.addAll(mappingPopDao.getAllParentsFromMappingPopulation(relativeLimit.intValue(), numOfRows));
                     }
                 }
             }
         } else if (sessionForLocal != null) {
             mappingPopDao.setSession(sessionForLocal);
             localCount = mappingPopDao.countAllParentsFromMappingPopulation();
             if (localCount > start) {
                 allParentsFromMappingPopulation.addAll(mappingPopDao.getAllParentsFromMappingPopulation(start, numOfRows));
             }
         }
     **/
         return allParentsFromMappingPopulation;
     }
 
     @Override
     public Long countAllParentsFromMappingPopulation() throws MiddlewareQueryException {
     	
         MappingPopDAO mappingPopDao = new MappingPopDAO();
            
         Database centralInstance = Database.CENTRAL;
         Session centralSession = getSession(centralInstance);
         mappingPopDao.setSession(centralSession);
         Long centralCountParentsFromMappingPopulation = mappingPopDao.countAllParentsFromMappingPopulation();
         
         Database localInstance = Database.LOCAL;
         Session localSession = getSession(localInstance);
         mappingPopDao.setSession(localSession);
         Long localCountParentsFromMappingPopulation = mappingPopDao.countAllParentsFromMappingPopulation();
         
         Long totalCountParentsFromMappingPopulation = localCountParentsFromMappingPopulation + centralCountParentsFromMappingPopulation;
         
         return totalCountParentsFromMappingPopulation;
     }
     
 
     
     @Override
     public List<org.generationcp.middleware.pojos.gdms.Map> getMapDetailsByName(
             String nameLike, int start, int numOfRows) throws MiddlewareQueryException {
 
         Long centralCount = Long.valueOf(0);
         Long localCount = Long.valueOf(0);
         Long relativeLimit = Long.valueOf(0);    	
     	
         MapDAO mapDao = new MapDAO();
        
         List<org.generationcp.middleware.pojos.gdms.Map> maps = new ArrayList<org.generationcp.middleware.pojos.gdms.Map>();
    
         Session sessionForCentral = getCurrentSessionForCentral();
         Session sessionForLocal = getCurrentSessionForLocal();
 
         if (sessionForCentral != null) {
             mapDao.setSession(sessionForCentral);
             centralCount = mapDao.countMapDetailsByName(nameLike);
             
             if (centralCount > start) {
                 maps.addAll(mapDao.getMapDetailsByName(nameLike, start, numOfRows));
                 relativeLimit = numOfRows - (centralCount - start);
 
                 if (relativeLimit > 0) {
                 	
                     if (sessionForLocal != null) {
                         mapDao.setSession(sessionForLocal);
                         localCount = mapDao.countMapDetailsByName(nameLike);
                         
                         if (localCount > 0) {
                             maps.addAll(mapDao.getMapDetailsByName(nameLike, 0, relativeLimit.intValue()));
                         }
                     }
                 }
             } else {
                 relativeLimit = start - centralCount;
                 if (sessionForLocal != null) {
                     mapDao.setSession(sessionForLocal);
                     localCount = mapDao.countMapDetailsByName(nameLike);
                     if (localCount > relativeLimit) {
                         maps.addAll(mapDao.getMapDetailsByName(nameLike, relativeLimit.intValue(), numOfRows));
                     }
                 }
             }
         } else if (sessionForLocal != null) {
             mapDao.setSession(sessionForLocal);
             localCount = mapDao.countMapDetailsByName(nameLike);
             if (localCount > start) {
                 maps.addAll(mapDao.getMapDetailsByName(nameLike, start, numOfRows));
             }
         }
      
         return maps;
     }
 
     @Override
     public Long countMapDetailsByName(String nameLike) throws MiddlewareQueryException {
     	
         MapDAO mapDao = new MapDAO();
            
         Database centralInstance = Database.CENTRAL;
         Session centralSession = getSession(centralInstance);
         mapDao.setSession(centralSession);
         Long centralCount = mapDao.countMapDetailsByName(nameLike);
         
         Database localInstance = Database.LOCAL;
         Session localSession = getSession(localInstance);
         mapDao.setSession(localSession);
         Long localCount = mapDao.countMapDetailsByName(nameLike);
         
         Long totalCount = centralCount + localCount;
         
         return totalCount;
     }
 
 }
