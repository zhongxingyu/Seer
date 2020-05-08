 package se.atrosys.birds.db;
 
 import org.hibernate.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.orm.hibernate3.HibernateTemplate;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 import org.springframework.stereotype.Repository;
 import se.atrosys.birds.model.*;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ola
  * Date: 12/4/11
  * Time: 8:56 PM
  * To change this template use File | Settings | File Templates.
  */
 
 @Repository("birdDao")
 public class BirdDaoImpl extends HibernateDaoSupport implements BirdDao {
 	@Autowired BirdPhotoDao birdPhotoDao;
 	public BirdModel findById(String id) {
 		HibernateTemplate hibernateTemplate = getHibernateTemplate();
 		Session session = hibernateTemplate.getSessionFactory().openSession();
 
 		BirdModel model = (BirdModel) session.get(BirdModel.class, id);
 
 		retrieveLazyBindings(model);
 
 		session.close();
 
 		return model;
 	}
 
 	public List<BirdModel> findAll() {
 		HibernateTemplate hibernateTemplate = getHibernateTemplate();
 		Session session = hibernateTemplate.getSessionFactory().openSession();
 
 		String hql = "from se.atrosys.birds.model.BirdModel";
 		Query query = session.createQuery(hql);
 		List<BirdModel> list = query.list();
 		
 		for (BirdModel model: list) {
 			retrieveLazyBindings(model);
 		}
 
 		session.close();
 
 		return list;
 	}
 
 	public void save(BirdModel model) {
 		HibernateTemplate hibernateTemplate = getHibernateTemplate();
 		Session session = hibernateTemplate.getSessionFactory().openSession();
 		Transaction transaction = session.beginTransaction();
 		
 		session.save(model);
 
 		Set<RegionModel> regionModelSet = new HashSet<RegionModel>();
 
 		for (RegionalScarcityModel regionalScarcityModel: model.getRegionalScarcity()){
 			regionModelSet.add(regionalScarcityModel.getRegion());
 		}
 		
 		for (RegionModel regionModel: regionModelSet) {
			Query query = session.createQuery("from se.atrosys.birds.model.RegionModel where name = ?");
			query.setText(0, regionModel.getName());
			if (query.list().isEmpty()) {
				session.save(regionModel);
			}
 		}
 
 		for (RegionalScarcityModel regionalScarcityModel: model.getRegionalScarcity()){
 			session.save(regionalScarcityModel);
 		}
 		
 		for (BirdNameModel birdNameModel: model.getNames()) {
 			session.save(birdNameModel);
 		}
 
 		transaction.commit();
 		session.close();
 	}
 
 	public void update(BirdModel model) {
 		getHibernateTemplate().update(model);
 	}
 
 	public void delete(BirdModel model) {
 		getHibernateTemplate().delete(model);
 	}
 
 	public void shutdown() {
 		getHibernateTemplate().getSessionFactory().openSession().createSQLQuery("SHUTDOWN").executeUpdate();
 	}
 
 	// another prime example as to why SQL is peerless.
 	public BirdModel getRandomBird() {
 		String sql = "select id from birds limit 1 offset (select floor(count(*) * random()) from birds)";
 
 		Session session = getSession();
 		SQLQuery sqlQuery = session.createSQLQuery(sql);
 		String id = (String) sqlQuery.list().get(0);
 		session.close();
 
 		return findById(id);
 	}
 
 	public BirdModel findByScientificName(String name) {
 		HibernateTemplate hibernateTemplate = getHibernateTemplate();
 		Session session = hibernateTemplate.getSessionFactory().openSession();
 		Query query = session.createQuery("from se.atrosys.birds.model.BirdModel where scientificName = ?");
 		query.setText(0, name);
 		List<BirdModel> list = (List<BirdModel>)query.list();
 		if (list.isEmpty()) {
 			return null;
 		}
 		
 		BirdModel model = (BirdModel) list.get(0);
 
 		retrieveLazyBindings(model);
 
 		session.close();
 
 		return model;
 	}
 
 	private void retrieveLazyBindings(BirdModel model) {
 		if (model != null) {
 			for (BirdPhotoModel photoModel: model.getPhotos()) {
 				photoModel.getFarm();
 			}
 
 			for (BirdModel birdModel: model.getFamily().getBirds()) {
 				birdModel.getId();
 			}
 		}
 	}
 
 	@Autowired
     public void init( SessionFactory sessionFactory ) {
         setSessionFactory( sessionFactory );
     }
 }
