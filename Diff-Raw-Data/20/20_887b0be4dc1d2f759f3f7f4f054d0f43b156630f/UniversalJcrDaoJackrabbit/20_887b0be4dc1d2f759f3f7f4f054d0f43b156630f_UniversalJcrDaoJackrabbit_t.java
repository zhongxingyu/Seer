 package org.otherobjects.cms.dao;
 
 import java.util.List;
 
 import org.apache.jackrabbit.ocm.query.Filter;
 import org.apache.jackrabbit.ocm.query.Query;
 import org.apache.jackrabbit.ocm.query.QueryManager;
 import org.otherobjects.cms.OtherObjectsException;
 import org.otherobjects.cms.jcr.GenericJcrDaoJackrabbit;
 import org.otherobjects.cms.jcr.UniversalJcrDao;
 import org.otherobjects.cms.model.BaseNode;
 import org.otherobjects.cms.types.TypeDef;
 import org.otherobjects.cms.types.TypeService;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.util.Assert;
 
 public class UniversalJcrDaoJackrabbit extends GenericJcrDaoJackrabbit<BaseNode> implements UniversalJcrDao, ApplicationContextAware
 {
     private TypeService typeService;
     
     public UniversalJcrDaoJackrabbit()
     {
         super(BaseNode.class);
     }
 
     public BaseNode create(String typeName)
     {
         Assert.notNull(typeName, "typeName must not be null.");
         TypeDef type = this.typeService.getType(typeName);
         Assert.notNull(type, "No type found: " + typeName);
         Assert.notNull(type.getClassName(), "Type does not have backing class specified: " + typeName);
         try
         {
             BaseNode n = (BaseNode) Class.forName(type.getClassName()).newInstance();
             n.setTypeDef(type);
             return n;
         }
         catch (Exception e)
         {
             //TODO Better exception?
             throw new OtherObjectsException("Could not create new instance of type: " + typeName, e);
         }
     }
 
 
     @SuppressWarnings("unchecked")
     public List<BaseNode> findByPathAndType(String path, String typeName)
     {
         try
         {
             TypeDef type = this.typeService.getType(typeName);
             QueryManager queryManager = getJcrMappingTemplate().createQueryManager();
             Filter filter = queryManager.createFilter(Class.forName(type.getClassName()));
             Query query = queryManager.createQuery(filter);
             filter.setScope(path + "/");
             filter.addEqualTo("ooType", typeName);
             return (List<BaseNode>) getJcrMappingTemplate().getObjects(query);
         }
         catch (ClassNotFoundException e)
         {
             throw new OtherObjectsException("No class found for type: " + typeName);
         }
     }
 
     @SuppressWarnings("unchecked")
     public List<BaseNode> getAllByType(String typeName)
     {
        // TODO Is this the most performant query method?
        return getAllByJcrExpression("/jcr:root//element(*) [@ooType='" + typeName + "']");
     }
 
     public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
     {
         super.setApplicationContext(applicationContext);
     }
     
     public void setTypeService(TypeService typeService)
     {
         this.typeService = typeService;
     }
 
     public List<BaseNode> getAllByType(Class<?> type)
     {
        return getAllByType(type.getName());
     }
 
     public List<BaseNode> getByPathAndType(String path, String type)
     {
         // TODO Auto-generated method stub
         return null;
     }
 }
