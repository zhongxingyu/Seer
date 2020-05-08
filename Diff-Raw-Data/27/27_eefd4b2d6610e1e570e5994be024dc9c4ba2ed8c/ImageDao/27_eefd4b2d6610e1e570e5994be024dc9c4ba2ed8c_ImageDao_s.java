 package com.cqlybest.common.dao;
 
 import java.util.Date;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.criterion.ProjectionList;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.transform.AliasToBeanResultTransformer;
 import org.springframework.stereotype.Repository;
 
 import com.cqlybest.common.bean.Image;
 
 @Repository
 @SuppressWarnings("unchecked")
 public class ImageDao extends AbstractDao<Image, String> {
 
   protected ImageDao() {
     super(Image.class);
   }
 
   public List<Image> queryImagesWithoutData(String extra, String extraKey) {
     Criteria criteria = getCurrentSession().createCriteria(entityClass);
     ProjectionList projection = Projections.projectionList();
     projection.add(Projections.property("id"), "id");
     projection.add(Projections.property("imageType"), "imageType");
     projection.add(Projections.property("title"), "title");
     projection.add(Projections.property("description"), "description");
     projection.add(Projections.property("extra"), "extra");
     projection.add(Projections.property("extraKey"), "extraKey");
     projection.add(Projections.property("createdTime"), "createdTime");
     projection.add(Projections.property("lastUpdated"), "lastUpdated");
     criteria.setProjection(projection);
     criteria.add(Restrictions.eq("extra", extra));
     criteria.add(Restrictions.eq("extraKey", extraKey));
     criteria.setResultTransformer(new AliasToBeanResultTransformer(entityClass));
     return criteria.list();
   }
 
   public int update(String id, String name, Object value) {
     String hql = "UPDATE Image SET " + name + " = ?, lastUpdated = ? WHERE id = ?";
     Query query = getCurrentSession().createQuery(hql);
     query.setParameter(0, value);
     query.setParameter(1, new Date());
     query.setParameter(2, id);
     return query.executeUpdate();
   }
 
 }
