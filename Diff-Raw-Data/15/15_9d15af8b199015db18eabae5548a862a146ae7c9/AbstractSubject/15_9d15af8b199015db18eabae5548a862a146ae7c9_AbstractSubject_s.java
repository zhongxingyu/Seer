 package code;
 
 import java.util.Set;
import java.util.TreeSet;
 
 public class AbstractSubject {
 
 	private Set<AbstractObserver> observers;
 	
 	protected AbstractSubject(){
		observers = new TreeSet<AbstractObserver>();
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
