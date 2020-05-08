 package org.otherobjects.cms.datastore;
 
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.otherobjects.cms.OtherObjectsException;
 import org.otherobjects.cms.dao.DaoService;
 import org.otherobjects.cms.dao.GenericDao;
 import org.otherobjects.cms.model.CompositeDatabaseId;
import org.otherobjects.cms.model.Editable;
 import org.otherobjects.cms.types.TypeDef;
 import org.otherobjects.cms.util.IdentifierUtils;
 
 @SuppressWarnings("unchecked")
 public class HibernateDataStore implements DataStore
 {
     @Resource
     private DaoService daoService;
    
     public HibernateDataStore()
     {
     }
 
     public HibernateDataStore(DaoService daoService)
     {
         this.daoService = daoService;
     }
 
     public Object create(TypeDef typeDef, String containerId)
     {
         try
         {
            return (Editable) Class.forName(typeDef.getClassName()).newInstance();
         }
         catch (Exception e)
         {
             throw new OtherObjectsException("Could not create object of type: " + typeDef.getName(), e);
         }
     }
 
     public Object get(String compositeId)
     {
         CompositeDatabaseId compositeDatabaseId = IdentifierUtils.getCompositeDatabaseId(compositeId);
        GenericDao dao = daoService.getDao(compositeDatabaseId.getClazz());
         return dao.get(compositeDatabaseId.getId());
     }
 
     public Object delete(Object object)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     public List<Object> list(String query)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     public Object save(Object object)
     {
         // TODO Auto-generated method stub
         return null;
     }
 
     public GenericDao getDao(TypeDef typeDef)
     {
        return daoService.getDao(typeDef.getClassName());
     }
 
 }
