     /*******************************************************************************
  * Copyright 2010-2013 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
  *
  * This file is part of SITools2.
  *
  * SITools2 is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * SITools2 is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SITools2.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package fr.cnes.sitools.userstorage;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 
 import com.thoughtworks.xstream.XStream;
 import com.thoughtworks.xstream.persistence.FilePersistenceStrategy;
 import com.thoughtworks.xstream.persistence.XmlArrayList;
 
 import fr.cnes.sitools.common.SitoolsSettings;
 import fr.cnes.sitools.common.XStreamFactory;
 import fr.cnes.sitools.common.application.ContextAttributes;
 import fr.cnes.sitools.common.exception.SitoolsException;
 import fr.cnes.sitools.common.model.ResourceCollectionFilter;
 import fr.cnes.sitools.common.model.ResourceComparator;
 import fr.cnes.sitools.userstorage.model.UserStorage;
 
 /**
  * Implementation of UserStorageStore with XStream FilePersistenceStrategy
  * 
  * @author AKKA
  * 
  */
 public final class UserStorageStoreXML implements UserStorageStore {
 
   /** default location for file persistence */
   private static final String COLLECTION_NAME = "userStorages";
 
   /** static logger for this store implementation */
   private static Logger log = Logger.getLogger(UserStorageStoreXML.class.getName());
 
   /** Persistent list of userStorages */
   private List<UserStorage> list = null;
   /** The Context */
   private Context context;
 
   /**
    * Constructor with the XML file location
    * 
    * @param location
    *          directory of FilePersistenceStrategy
    * @param context
    *          the Restlet Context
    */
   public UserStorageStoreXML(File location, Context context) {
     super();
     this.context = context;
     init(location);
   }
 
   /**
    * Default constructor
    * 
    * @param context
    *          the Restlet Context
    */
   public UserStorageStoreXML(Context context) {
     this(new File(COLLECTION_NAME), context);
   }
 
   @Override
   public UserStorage create(UserStorage userStorage) throws SitoolsException {
     UserStorage result = null;
 
     if (userStorage.getUserId() == null || "".equals(userStorage.getUserId())) {
       throw new SitoolsException("USERSTORAGE_USERIDENTIFIER_MANDATORY");
     }
 
     // Recherche sur l'id
     for (Iterator<UserStorage> it = list.iterator(); it.hasNext();) {
       UserStorage current = it.next();
       if (current.getUserId().equals(userStorage.getUserId())) {
         log.info("UserStorage found");
         result = current;
         break;
       }
     }
 
     if (result == null) {
       list.add(userStorage);
       result = userStorage;
     }
     return result;
   }
 
   @Override
   public UserStorage retrieve(String id) {
     UserStorage result = null;
     for (Iterator<UserStorage> it = list.iterator(); it.hasNext();) {
       UserStorage current = it.next();
       if (current.getUserId().equals(id)) {
         log.info("UserStorage found");
         result = current;
         break;
       }
     }
     return result;
   }
 
   @Override
   public UserStorage update(UserStorage userStorage) {
     UserStorage result = null;
     for (Iterator<UserStorage> it = list.iterator(); it.hasNext();) {
       UserStorage current = it.next();
       if (current.getUserId().equals(userStorage.getUserId())) {
         log.info("Updating UserStorage");
 
         result = current;
         current.setStorage(userStorage.getStorage());
         current.setStatus(userStorage.getStatus());
 
         it.remove();
 
         break;
       }
     }
     if (result != null) {
       list.add(result);
     }
     return result;
   }
 
   @Override
   public boolean delete(String id) {
     boolean result = false;
     for (Iterator<UserStorage> it = list.iterator(); it.hasNext();) {
       UserStorage current = it.next();
       if (current.getUserId().equals(id)) {
         log.info("Removing UserStorage");
         it.remove();
         result = true;
         break;
       }
     }
     return result;
   }
 
   @Override
   public UserStorage[] getArray() {
     UserStorage[] result = null;
     if ((list != null) && (list.size() > 0)) {
       result = list.toArray(new UserStorage[list.size()]);
     }
     else {
       result = new UserStorage[0];
     }
     return result;
   }
 
   @Override
   public UserStorage[] getArray(ResourceCollectionFilter filter) {
     List<UserStorage> resultList = getList(filter);
 
     UserStorage[] result = null;
     if ((resultList != null) && (resultList.size() > 0)) {
       result = resultList.toArray(new UserStorage[resultList.size()]);
     }
     else {
       result = new UserStorage[0];
     }
     return result;
   }
 
   @Override
   public UserStorage[] getArrayByXQuery(String xquery) {
     log.severe("getArrayByXQuery NOT IMPLEMENTED");
     return null;
   }
 
   @Override
   public List<UserStorage> getList(ResourceCollectionFilter filter) {
     List<UserStorage> result = new ArrayList<UserStorage>();
     if ((list == null) || (list.size() <= 0) || (filter.getStart() > list.size())) {
       return result;
     }
 
     result.addAll(list);
 
     // Si index premier element > nombre d'elements filtres => resultat vide
     if (filter.getStart() > result.size()) {
       result.clear();
       return result;
     }
 
     // Tri
     sort(result, filter);
 
     return result;
   }
 
   /**
    * Filters a list according to the pagination
    * 
    * @param filter
    *          for pagination
    * @param result
    *          source
    * @return ArrayList<UserStorage>
    */
   public List<UserStorage> getPage(ResourceCollectionFilter filter, List<UserStorage> result) {
     if (result.size() == 0) {
       return result;
     }
 
     // Pagination
    int start = (filter.getStart() <= 0) ? 0 : filter.getStart();
     int limit = ((filter.getLimit() <= 0) || ((filter.getLimit() + start) > result.size())) ? (result.size() - start)
         : filter.getLimit();
     // subList
     // Returns a view of the portion of this list between the specified fromIndex, inclusive,
     // and toIndex, exclusive.
     List<UserStorage> page = result.subList(start, start + limit); // pas -1 puisque exclusive
 
     return new ArrayList<UserStorage>(page);
   }
 
   @Override
   public List<UserStorage> getList() {
     List<UserStorage> result = new ArrayList<UserStorage>();
     if ((list != null) || (list.size() > 0)) {
       result.addAll(list);
     }
 
     // Tri
     sort(result, null);
 
     return result;
   }
 
   /**
    * Sort the list (by default on the name)
    * 
    * @param result
    *          list to be sorted
    * @param filter
    *          ResourceCollectionFilter with sort properties.
    */
   private void sort(List<UserStorage> result, ResourceCollectionFilter filter) {
     if ((filter != null) && (filter.getSort() != null) && !filter.getSort().equals("")) {
       Collections.sort(result, new ResourceComparator<UserStorage>(filter) {
         @Override
         public int compare(UserStorage arg0, UserStorage arg1) {
           if (arg0.getUserId() == null) {
             return 1;
           }
           if (arg1.getUserId() == null) {
             return -1;
           }
           String s1 = (String) arg0.getUserId();
           String s2 = (String) arg1.getUserId();
 
           return super.compare(s1, s2);
         }
       });
     }
   }
 
   @Override
   public List<UserStorage> getListByXQuery(String xquery) {
     log.warning("getListByXQuery DEFAULT IMPLEMENTATION : getList");
     return getList();
   }
 
   /**
    * XStream FilePersistenceStrategy initialization
    * 
    * @param location
    *          Directory
    */
   @SuppressWarnings("unchecked")
   private void init(File location) {
     log.info("Store location " + location.getAbsolutePath());
     SitoolsSettings settings = (SitoolsSettings) context.getAttributes().get(ContextAttributes.SETTINGS);
     boolean strict = !settings.isStartWithMigration();
 
     XStream xstream = XStreamFactory.getInstance().getXStream(MediaType.APPLICATION_XML, context, strict);
 
     xstream.autodetectAnnotations(true);
     xstream.alias("userStorage", UserStorage.class);
 
     FilePersistenceStrategy strategy = new FilePersistenceStrategy(location, xstream);
     list = new XmlArrayList(strategy);
   }
 
   @Override
   public void close() {
     // TODO
   }
 
 }
