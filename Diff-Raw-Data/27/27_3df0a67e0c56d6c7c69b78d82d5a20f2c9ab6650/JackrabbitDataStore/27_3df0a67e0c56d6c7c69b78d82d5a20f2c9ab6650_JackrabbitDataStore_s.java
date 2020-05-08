 package org.otherobjects.cms.datastore;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.otherobjects.cms.dao.GenericDao;
 import org.otherobjects.cms.jcr.UniversalJcrDao;
 import org.otherobjects.cms.model.BaseNode;
 import org.otherobjects.cms.types.TypeDef;
 
 @SuppressWarnings("unchecked")
 public class JackrabbitDataStore implements DataStore
 {
     @Resource
     private UniversalJcrDao universalJcrDao;
 
     public Object create(TypeDef typeDef, String containerId)
     {
         BaseNode item = universalJcrDao.create(typeDef.getName());
        BaseNode container = universalJcrDao.get(containerId);
        item.setPath(container.getJcrPath());
         return item;
     }
 
     public Object get(String id)
     {
         return universalJcrDao.get(id);
     }
 
     public Object delete(Object object)
     {
         return null;
     }
 
     public List list(String containerId)
     {
         return null;
     }
 
     public Object save(Object object)
     {
         return null;
     }
 
     public GenericDao getDao(TypeDef typeDef)
     {
         return universalJcrDao;
     }
 }
