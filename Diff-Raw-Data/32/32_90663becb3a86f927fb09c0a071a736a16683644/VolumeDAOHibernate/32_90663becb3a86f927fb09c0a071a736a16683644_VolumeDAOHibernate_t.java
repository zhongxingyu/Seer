 package com.book.identification.dao;
 
 import java.util.List;
 
 import com.book.identification.model.Volume;
 
 public class VolumeDAOHibernate extends GenericHibernateDAO<Volume, Long>
 		implements VolumeDAO {
 
 	@SuppressWarnings("unchecked")
 	public List<Volume> volumesWithCategory(Long categoryId) {
		String q = "Select distinct v From Volume v JOIN v.volumeInfo vi JOIN vi.categoriess c LEFT OUTER JOIN c.parent cp WHERE c.id = :categoryId OR cp.id = :categoryId";
 		return (List<Volume>) getSession().createQuery(q).setParameter("categoryId",categoryId).list();
 	}
 
 
 
 }
