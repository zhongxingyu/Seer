 package backbone;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 
 public interface Grouping<T extends Unit> {
 	
 	public String getName();
 
 	public List<T> getMembers();
 	
 	public void addMember(T member);
 
	public void deleteMember(T member);
 }
