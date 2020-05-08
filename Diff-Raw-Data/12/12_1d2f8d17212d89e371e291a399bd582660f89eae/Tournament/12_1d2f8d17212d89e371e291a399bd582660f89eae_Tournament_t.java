 package backbone;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 public interface Tournament extends Serializable {
 	
 	public Round getCurrentRound();
 
	public List<Grouping> getCategories();
 	
 	public Round createNextRound();
 	
 	public Collection<Round> getRounds();
 	
 	
 }
