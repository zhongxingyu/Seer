 package ie.wombat.landedestates;
 
 import java.util.HashSet;
 import java.util.Set;
 
 
 public class EmploymentRecord implements Indexable {
 	private Long id;
 	private String description;
 	
	private House house;
 	
 	
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 	
 	
 	public String getDescription() {
 		return description;
 	}
 	public void setDescription(String description) {
 		this.description = description;
 	}
 	
 	@Override
 	public String getLuceneId() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	@Override
 	public String getName() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	
 }
