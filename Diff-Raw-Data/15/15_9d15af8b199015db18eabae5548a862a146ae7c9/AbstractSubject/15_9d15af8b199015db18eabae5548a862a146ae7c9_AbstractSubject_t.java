 package code;
 
import java.util.HashSet;
 import java.util.Set;
 
 public class AbstractSubject {
 
 	private Set<AbstractObserver> observers;
 	
 	protected AbstractSubject(){
		observers = new HashSet<AbstractObserver>();
 	}
 	
 	public void attach(AbstractObserver abstractObserver){
 		observers.add(abstractObserver);
 	}
	
 	public void detach(AbstractObserver abstractObserver){
 		observers.remove(abstractObserver);		
 	}
 	
 	protected void notifyObservers(){
 		java.util.Iterator<AbstractObserver> auditorsIterator = observers.iterator();
 		while(auditorsIterator.hasNext()) {
 			auditorsIterator.next().update();
 		} 
 	}
 
 }
