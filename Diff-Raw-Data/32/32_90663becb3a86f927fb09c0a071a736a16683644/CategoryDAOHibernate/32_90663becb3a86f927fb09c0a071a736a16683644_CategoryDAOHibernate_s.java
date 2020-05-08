 package com.book.identification.dao;
 
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.hibernate.Query;
 
 import com.book.identification.model.Category;
 
 public class CategoryDAOHibernate extends GenericHibernateDAO<Category, Long>
 		implements CategoryDAO {
 
 	public Category retrieveCategory(String categoryName,
 			String... parentCategoryName) {
 		Query query;
 		if(parentCategoryName == null){
 			query = getSession().createQuery("Select c From Category c Where c.category = :categoryName AND c.parent is null")
 			.setParameter("categoryName", categoryName);
 		}else{
 			StringBuilder stringQuery  = new StringBuilder();
 			stringQuery.append("Select c From Category c Where c.category = :categoryName");
 			for (int i = 0; i < parentCategoryName.length; i++) {
 				stringQuery.append(" AND c."+StringUtils.repeat("parent.", i + 1)  +"category = :parentCategoryName" + i);
 			}
 			stringQuery.append(" AND c.parent"+StringUtils.repeat(".parent", parentCategoryName.length)  +" is null");
 			
 			query = getSession().createQuery(stringQuery.toString()).setParameter("categoryName", categoryName);
			for (int i = 0; i < parentCategoryName.length; i++) {
				query = query.setParameter("parentCategoryName" + i, parentCategoryName[i]);
 			}
 		}
 		return (Category) query.uniqueResult();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Category> childCategories(Long parentId) {
 		Query query;
 		
 			query = getSession().createQuery("Select c From Category c Where c.parent.id = :parentId")
 			.setParameter("parentId", parentId);
 			return (List<Category>) query.list();
 		
 	}
 
 }
