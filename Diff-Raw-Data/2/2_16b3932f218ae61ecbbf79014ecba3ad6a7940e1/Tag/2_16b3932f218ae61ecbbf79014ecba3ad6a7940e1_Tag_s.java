 package uk.ac.cam.sup.models;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import org.hibernate.Criteria;
 import org.hibernate.criterion.Restrictions;
 
 import uk.ac.cam.sup.util.HibernateUtil;
 
 @Entity
 @Table(name="Tags")
 public class Tag extends Model implements Comparable<Tag>{
 	@Id private String name;
 	
 	public String getName() {
 		return this.name;
 	}
 	
 	private Tag(){}
 	private Tag(String name) {
 		this.name = name;
 	}
 	
 	public static Tag get(String name) {
 		Criteria criteria = HibernateUtil.getTransactionSession()
 			.createCriteria(Tag.class)
			.add(Restrictions.eq("name", name));
 		Tag t = (Tag) criteria.uniqueResult();
 		
 		if (t == null) {
 			t = new Tag(name);
 			t.saveOrUpdate();
 		}
 		
 		return t;
 	}
 	
 	public int compareTo(Tag tag){
 		return this.getName().compareTo(tag.getName());
 	}
 	
 	public static Set<Tag> parseTagString(String taglist) {
 		Set<Tag> r = new HashSet<Tag>();
 		String[] tagarray = taglist.split(",");
 		
 		for (String s: tagarray) {
 			r.add(new Tag(s.trim()));
 		}
 		
 		return r;
 	}
 	
 	public boolean equals (Object t) {
 		if (!(t instanceof Tag)) {
 			return false;
 		} else { 
 			return ((Tag)t).name.toLowerCase().equals(this.name.toLowerCase());
 		}
 	}
 	
 	public int hashCode () {
 		return name.toLowerCase().hashCode();
 	}
 }
