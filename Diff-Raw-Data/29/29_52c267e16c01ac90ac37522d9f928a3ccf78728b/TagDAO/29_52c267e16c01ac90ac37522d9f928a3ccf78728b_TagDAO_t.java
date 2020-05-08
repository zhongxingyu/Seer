 package nz.co.searchwellington.repositories;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import nz.co.searchwellington.model.Tag;
 
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;
 
 public class TagDAO {
 
 	SessionFactory sessionFactory;
 
 	public TagDAO() {
 	}
 	
 	public TagDAO(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 
 	
 	public Tag createNewTag() {
 	        Tag newTag = new Tag(0, "", "", null, new HashSet<Tag>(), 0, false);	     
 			return newTag;
 	 }
 	
 	
 	public Tag loadTagById(int tagID) {
 		return (Tag) sessionFactory.getCurrentSession().load(Tag.class, tagID);
 	}
 	
 	
 	public Tag loadTagByName(String tagName) {
         return (Tag) sessionFactory.getCurrentSession().
         createCriteria(Tag.class).
         add(Restrictions.eq("name", tagName)).
         uniqueResult();    
     }
 
 	@SuppressWarnings("unchecked")
 	public List<Tag> getAllTags() {
 		return sessionFactory.getCurrentSession().createCriteria(Tag.class).
 			addOrder(Order.asc("displayName")).
 			setCacheable(true).
 			list();
 	}
 
 	// TODO can hiberate do a batch load?
 	public List<Tag> loadTagsById(List<Integer> tagIds) {
 		List<Tag> tags = new ArrayList<Tag>();
 		for (Integer tagId : tagIds) {
 			Tag tag = this.loadTagById(tagId);
 			if (tag != null) {
 				tags.add(tag);
 			}
 		}
 		return tags;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<Tag> getTopLevelTags() {
 		return sessionFactory.getCurrentSession().createCriteria(Tag.class)
 				.add(Restrictions.isNull("parent")).addOrder(Order.asc("name"))
 				.setCacheable(true).list();
 	}
 	
	
	@Transactional
	public void saveTag(Tag editTag) {
 		 sessionFactory.getCurrentSession().saveOrUpdate(editTag);
 		 sessionFactory.evictCollection("nz.co.searchwellington.model.Tag.children");
 		 // TODO solr index needs updating if a tag moves to a new parent.
 	 }
 	 
	
	@Transactional
	public void deleteTag(Tag tag) {
 		 sessionFactory.getCurrentSession().delete(tag);
	}
 	
 }
