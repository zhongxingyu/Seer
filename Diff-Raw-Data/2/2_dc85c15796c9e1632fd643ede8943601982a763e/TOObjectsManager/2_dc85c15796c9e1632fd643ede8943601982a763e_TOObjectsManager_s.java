 package ru.terraobjects.entity.manager;
 
 /**
  *
  * @author terranz
  */
 import java.util.List;
 import java.util.Date;
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import ru.terraobjects.entity.PersistanceManager;
 import ru.terraobjects.entity.TOObject;
 import ru.terraobjects.entity.TOObjectTemplate;
 import ru.terraobjects.entity.TOPropertyType;
 import ru.terraobjects.entity.dao.DAOConsts;
 
 public class TOObjectsManager extends PersistanceManager<TOObject>
 {
     public List<TOObject> getAllObjects()
     {
 	return session.createCriteria(TOObject.class).list();
     }
 
     public List<TOObject> getAllObjsByTemplate(TOObjectTemplate template)
     {
 	return session.createCriteria(TOObject.class).add(Restrictions.eq("objectTemplate", template)).list();
     }
 
     public List<TOObject> getAllObjsByTemplate(TOObjectTemplate template, Integer page, Integer perPage)
     {
 	return session.createCriteria(TOObject.class).add(Restrictions.eq("objectTemplate", template)).setFirstResult(page).setMaxResults(perPage).list();
     }
 
     public List<TOObject> getAllObjectsByParentId(Integer parentId)
     {
 	return session.createCriteria(TOObject.class).add(Restrictions.eq("parentObjectId", parentId)).list();
     }
 
     public List<TOObject> getAllObjectsByParentId(Integer parentId, Integer page, Integer perPage)
     {
 	return session.createCriteria(TOObject.class).add(Restrictions.eq("parentObjectId", parentId)).setFirstResult(page).setMaxResults(perPage).list();
     }
 
     public TOObject getObject(Integer id)
     {
 	return findById(id);
     }
 
     public TOObject createNewObject(Integer templateId)
     {
 	TOObject newobj = new TOObject();
 	newobj.setObjectTemplate(new TOObjectTemplateManager().findById(templateId));
 	newobj.setObjectCreatedAt(new Date());
 	newobj.setObjectUpdatedAt(new Date());
 	newobj.setParentObjectId(0);
 	this.insert(newobj);
 	return newobj;
     }
 
     public Long getObjectsCountByTemplateId(TOObjectTemplate template)
     {
 	Criteria criteria = session.createCriteria(TOObject.class);
 	criteria.setProjection(Projections.rowCount());
 	criteria.add(Restrictions.eq("objectTemplate", template));
 	return ((Integer) criteria.list().get(0)).longValue();
     }
 
     public void removeObjectWithProps(Integer objId)
     {
 	new TOObjectPropertyManager().removeObjPropertiesFromObject(objId);
 
 	delete(findById(objId));
     }
 
     public void removeObjectsByTemplate(TOObjectTemplate template)
     {
 	for (TOObject o : getAllObjsByTemplate(template))
 	{
 	    delete(o);
 	}
     }
 
     public List<TOObject> getObjectsByTemplateAndProp(Integer templateId, Integer propId)
     {
 	Query q = session.createSQLQuery(DAOConsts.SELECT_OBJECT_BY_TEMPLATE_ID_AND_PROP_ID).addEntity(TOObject.class).setParameter(1, templateId).setParameter(2, propId);
 	return q.list();
     }
 
     public List<TOObject> getObjectsByTemplateAndPropValue(Integer templateId, Integer propId, Integer type, Object val)
     {
 	String sql = DAOConsts.SELECT_OBJECT_BY_TEMPLATE_ID_AND_PROP_ID_AND_PROP_VAL.replace("$TYPE$", TOPropertyType.getTypeValById(type));
 
 	Query q = session.createSQLQuery(sql).addEntity(TOObject.class);
 	q.setParameter("tid", templateId);
 	q.setParameter("pid", propId);
 	q.setParameter("val", val);
 	return q.list();
     }
 
     @Override
     public TOObject findById(Integer id)
     {
 	Criteria c = session.createCriteria(TOObject.class);
 	c.add(Restrictions.eq("objectId", id));
 	return (TOObject) c.uniqueResult();
     }
 
     public List<Integer> getObjectsByPropAndPropVal(Integer propId, Integer type, Object val)
     {
 	String sql = DAOConsts.SELECT_OBJECTID_BY_PROP_ID_AND_PROP_VAL.replace("$TYPE$", TOPropertyType.getTypeValById(type));
	Query q = session.createSQLQuery(sql).addEntity(TOObject.class);
 	q.setParameter("pid", propId);
 	q.setParameter("val", val);
 	return q.list();
     }
 }
