 package org.otherobjects.cms.dao;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.persistence.Entity;
 
 import org.apache.commons.lang.StringUtils;
 import org.hibernate.SessionFactory;
 import org.otherobjects.cms.OtherObjectsException;
 import org.otherobjects.cms.hibernate.GenericDaoHibernate;
 import org.otherobjects.cms.model.BaseNode;
 import org.otherobjects.cms.types.TypeService;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 
 @SuppressWarnings("unchecked")
 public class DaoServiceImpl implements DaoService, BeanFactoryAware
 {
     private static final String UNIVERSAL_JCR_DAO_KEY = "universalJcrDao";
     private Map<String, GenericDao> daoMap = new HashMap<String, GenericDao>();
     private BeanFactory beanFactory;
     private TypeService typeService;
     private SessionFactory sessionFactory;
 
     public GenericDao getDao(Class clazz)
     {
         return getDao(clazz.getName());
     }
 
     public GenericDao get(String type)
     {
         return getDao(type);
     }
 
     /**
      * Returns a Dao for provided type. First looks in daoMap and then in the application context. If nothing found return UniversalJcrDao (for Jcr objects) 
      * or GenericDaoHibernate for database objects.
      */
     public GenericDao getDao(String type)
     {
         GenericDao dao = null;
 
         // Look in configured map first
         if (daoMap != null && daoMap.containsKey(type))
             return daoMap.get(type);
 
         if (dao == null)
         {
             String daoBeanName = determineDaoBeanName(type);
 
             //then try find named bean in context
             if (beanFactory.containsBean(daoBeanName))
                 dao = (GenericDao) beanFactory.getBean(daoBeanName);
             else
             {
                 // then return universal jcr dao for types extending baseNode
                 //FIXME Should we support this? Need a better way of getting DAOs for objects.
                 if (type.equalsIgnoreCase("baseNode"))
                     return (GenericDao) beanFactory.getBean(UNIVERSAL_JCR_DAO_KEY);
 
                 // then return universal jcr dao for types extending baseNode
                 if (type.equalsIgnoreCase("jcr"))
                     return (GenericDao) beanFactory.getBean(UNIVERSAL_JCR_DAO_KEY);
 
                 try
                 {
                     Class cls = Class.forName(type);
                     if (BaseNode.class.isAssignableFrom(cls))
                     {
                         dao = (GenericDao) beanFactory.getBean(UNIVERSAL_JCR_DAO_KEY);
 
                     }
                     else if (cls.getAnnotation(Entity.class) != null) // then return GenericDaoHibernate for hibernate entities
                     {
                         GenericDaoHibernate hibernateDao = new GenericDaoHibernate(cls);
                        
                         // If sesion factory is null (ie during tests) then this will fail
                        if(sessionFactory!=null)
                             hibernateDao.setSessionFactory(sessionFactory);
                         dao = hibernateDao;
                         daoMap.put(daoBeanName, dao);
                     }
                     else
                     {
                         throw new OtherObjectsException("No Dao in context for type: " + type);
                     }
                 }
                 catch (Exception e)
                 {
                     // Check to see if this a DynaNode -- no class but should be registered in typeService.
                     if (typeService.getType(type) != null)
                         return (GenericDao) beanFactory.getBean(UNIVERSAL_JCR_DAO_KEY);
                     throw new OtherObjectsException("Could not fetch DAO for non-instantiatable type: " + type, e);
                 }
             }
         }
         return dao;
     }
 
     /**
      * Determines the conventional bean name for this type's DAO based on the type name.
      * The bean name should be the non-qualified type name, starting lowercase and appended
      * with "Dao". 
      * <p>For example <code>org.example.ProductCategory</code> should have a DAO bean name
      * of <code>productCategoryDao</code>.  
      * @param type
      * @return
      */
     protected String determineDaoBeanName(String type)
     {
         String beanName = type;
 
         // Make non-qualified
         if (type.contains("."))
             beanName = StringUtils.substringAfterLast(beanName, ".");
 
         // Lower case first letter
         beanName = beanName.substring(0, 1).toLowerCase() + beanName.substring(1);
 
         // Append Dao
         beanName = beanName + "Dao";
 
         return beanName;
     }
 
     public void addDao(GenericDao dao)
     {
         String name = dao.getPersistentClassName();
         getDaoMap().put(name, dao);
     }
 
     public void addDao(String name, GenericDao dao)
     {
         getDaoMap().put(name, dao);
     }
    
     public Map<String, GenericDao> getDaoMap()
     {
         return daoMap;
     }
 
     public void setDaoMap(Map<String, GenericDao> daoMap)
     {
         this.daoMap = daoMap;
     }
 
     public void setBeanFactory(BeanFactory beanFactory) throws BeansException
     {
         this.beanFactory = beanFactory;
     }
 
     public boolean hasDao(String type)
     {
         return (getDao(type) != null);
     }
 
     public void setTypeService(TypeService typeService)
     {
         this.typeService = typeService;
     }
 
     public void setSessionFactory(SessionFactory sessionFactory)
     {
         this.sessionFactory = sessionFactory;
     }
 }
