 package edu.wustl.cab2b.server.datacategory;
 
 import java.rmi.RemoteException;
 import java.sql.Connection;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.cab2b.common.category.DataCategorialAttribute;
 import edu.wustl.cab2b.common.category.DataCategorialClass;
 import edu.wustl.cab2b.common.category.DataCategory;
 import edu.wustl.cab2b.common.errorcodes.ErrorCodeConstants;
 import edu.wustl.cab2b.common.exception.RuntimeException;
 import edu.wustl.cab2b.server.cache.EntityCache;
import edu.wustl.cab2b.server.category.CategoryOperations;
 import edu.wustl.cab2b.server.path.PathFinder;
 import edu.wustl.common.bizlogic.DefaultBizLogic;
 import edu.wustl.common.exception.BizLogicException;
 import edu.wustl.common.querysuite.metadata.category.CategorialAttribute;
 import edu.wustl.common.querysuite.metadata.category.CategorialClass;
 import edu.wustl.common.querysuite.metadata.category.Category;
 import edu.wustl.common.querysuite.metadata.path.IPath;
 import edu.wustl.common.querysuite.metadata.path.Path;
 import edu.wustl.common.security.exceptions.UserNotAuthorizedException;
 import edu.wustl.common.util.dbManager.DAOException;
 import edu.wustl.common.util.global.Constants;
 
 public class DataCategoryOperations extends DefaultBizLogic {
 
     /**
      * Hibernate DAO Type to use.
      */
     private static final int DAO_TYPE = Constants.HIBERNATE_DAO;
 
     /**
      * Saves the input datacategory to database.
      * 
      * @param dataCategory DataCategory to save.
      * @throws RemoteException EBJ specific Exception
      */
     public void saveDataCategory(DataCategory dataCategory) {
         try {
             insert(dataCategory, DAO_TYPE);
         } catch (BizLogicException e) {
             throw new RuntimeException(ErrorCodeConstants.CATEGORY_SAVE_ERROR);
         } catch (UserNotAuthorizedException e) {
             throw new RuntimeException(ErrorCodeConstants.CATEGORY_SAVE_ERROR);
         }
     }
 
     
     
     
     
     /**
      * Returns all the datacategories availble in the system.
      * 
      * @return List of all datacategories.
      */
     public List<DataCategory> getAllDataCategories(Connection connection) {
         List<DataCategory> allCategories = null;
         try {
             allCategories = retrieve(DataCategory.class.getName());
         } catch (DAOException e) {
             throw new RuntimeException("Error in fetching category", e, ErrorCodeConstants.CATEGORY_RETRIEVE_ERROR);
         }
         for (DataCategory dataCategory : allCategories) {
            postRetrievalProcess(dataCategory, EntityCache.getInstance(), connection);
         }
         return allCategories;
     }
     
    
 
     /**
      * @param categoryId Id of the category
      * @return The Category for given id.
      * @throws RemoteException EBJ specific Exception
      */
     public DataCategory getDataCategoryByDataCategoryId(Long categoryId, Connection con) {
         List list = null;
         try {
             list = retrieve(DataCategory.class.getName(), "id", categoryId);
         } catch (DAOException e) {
             throw new RuntimeException(ErrorCodeConstants.CATEGORY_RETRIEVE_ERROR);
         }
         if (list == null || list.isEmpty()) {
             return null;
         }
         if (list.size() > 1) {
             throw new RuntimeException("Problem in code; probably db schema");
         }
         DataCategory datacategory = getCategoryFromList(list);
         postRetrievalProcess(datacategory, EntityCache.getInstance(), con);
         return datacategory;
     }
 
     private DataCategory getCategoryFromList(List dataCategories) {
         if (dataCategories == null || dataCategories.isEmpty()) {
             return null;
         }
         if (dataCategories.size() > 1) {
             throw new RuntimeException("Problem in code; probably db schema");
         }
         return (DataCategory) dataCategories.get(0);
     }
 
     /**
      * @param category Input Category for which all source classes are to be
      *            found.
      * @return Set of all entities present in given categoty.
      */
     public Set<EntityInterface> getAllSourceClasses(Category category) {
         Set<Long> idSet = new HashSet<Long>();
         CategorialClass rootCategorialClass = category.getRootClass();
         idSet.add(rootCategorialClass.getDeEntityId());
         getDeEntityIdsOfChildren(rootCategorialClass, idSet);
         EntityCache cache = EntityCache.getInstance();
         Set<EntityInterface> entities = new HashSet<EntityInterface>(idSet.size());
         for (Long id : idSet) {
             entities.add(cache.getEntityById(id));
         }
         return entities;
     }
 
     /**
      * @param parentCategorialClass Parent Categorial Class
      * @param idSet Set of all Source entity Ids present in the children of
      *            given param.
      */
     private void getDeEntityIdsOfChildren(CategorialClass parentCategorialClass, Set<Long> idSet) {
 
         for (CategorialClass categorialClass : parentCategorialClass.getChildren()) {
             idSet.add(categorialClass.getDeEntityId());
             getDeEntityIdsOfChildren(categorialClass, idSet);
         }
 
     }
 
     private void postRetrievalProcess(DataCategory category, EntityCache cache, Connection con) {
         processCategorialClass(category.getRootClass(), cache, con);
         for (DataCategory subCategory : category.getSubCategories()) {
             postRetrievalProcess(subCategory, cache, con);
         }
     }
 
     private void processCategorialClass(DataCategorialClass categorialClass, EntityCache cache, Connection con) {
         Long pathId = categorialClass.getPathFromParentId();
         if (pathId != null && pathId.intValue() != -1) {
             // this is a NON - root class
             IPath path = PathFinder.getInstance(con).getPathById(pathId);
             categorialClass.setPathFromParent((Path) path);
         }
         long deEntityId = categorialClass.getDeEntityId();
         EntityInterface entity = cache.getEntityById(deEntityId);
         categorialClass.setCategorialClassEntity(entity);
         DataCategory category = categorialClass.getCategory();
         Set<DataCategorialAttribute> dataCategorialAttributeSet =categorialClass.getCategorialAttributeCollection();
         for (DataCategorialAttribute attribute : dataCategorialAttributeSet) {
             long srcClassAttributeId = attribute.getDeSourceClassAttributeId();
             AttributeInterface attributeOfSrcClass = entity.getAttributeByIdentifier(srcClassAttributeId);
             attribute.setSourceClassAttribute(attributeOfSrcClass);
 
 //            long categoryEntityAttributeId = attribute.getDeCategoryAttributeId();
 //            AttributeInterface attributeOfCategoryEntity = categoryEntity.getAttributeByIdentifier(categoryEntityAttributeId);
 //            attribute.setCategoryAttribute(attributeOfCategoryEntity);
         }
 
         for (DataCategorialClass child : categorialClass.getChildren()) {
             child.setCategory(category);
             processCategorialClass(child, cache, con);
         }
     }
 
     /**
      * Returns all the categories which don't have any super category.
      * 
      * @return List of root categories.
      */
     public List<EntityInterface> getAllRootCategories() {
         List<Category> allCategories = null;
         try {
             allCategories = retrieve(Category.class.getName(), null, new String[] { "parentCategory" },
                                      new String[] { "is null" }, new Object[0], null);
         } catch (DAOException e) {
             throw new RuntimeException("Error in fetching category", e, ErrorCodeConstants.CATEGORY_RETRIEVE_ERROR);
         }
         List<EntityInterface> categoryEntities = new ArrayList<EntityInterface>(allCategories.size());
         EntityCache cache = EntityCache.getInstance();
         for (Category category : allCategories) {
             Long categoryEntityId = category.getDeEntityId();
             categoryEntities.add(cache.getEntityById(categoryEntityId));
         }
         return categoryEntities;
     }
 
    
     /**
      * @param category Input Category for which all source attributes are to be
      *            found.
      * @return Set of all source(original) attributes present in given category.
      */
     public Set<AttributeInterface> getAllSourceAttributes(Category category) {
         Set<AttributeInterface> attributeSet = getAllChilrenCategorialClasses(category.getRootClass());
         return attributeSet;
 
     }
 
     /**
      * Recursive method to traverse the whole tree of CategorialClasses and
      * collect attributes of each categorial class.
      * 
      * @param catClass Root of the categorial class tree
      * @return Set of all source(original) attributes present in given
      *         CategorialClass tree.
      */
     private Set<AttributeInterface> getAllChilrenCategorialClasses(CategorialClass catClass) {
         EntityCache cache = EntityCache.getInstance();
         Set<AttributeInterface> attributeSet = new HashSet<AttributeInterface>();
         EntityInterface entity = cache.getEntityById(catClass.getDeEntityId());
         for (CategorialAttribute catAttr : catClass.getCategorialAttributeCollection()) {
             long attrId = catAttr.getDeSourceClassAttributeId();
             attributeSet.add(entity.getAttributeByIdentifier(attrId));
         }
 
         for (CategorialClass child : catClass.getChildren()) {
             attributeSet.addAll(getAllChilrenCategorialClasses(child));
         }
         return attributeSet;
     }
     
     
     
 }
