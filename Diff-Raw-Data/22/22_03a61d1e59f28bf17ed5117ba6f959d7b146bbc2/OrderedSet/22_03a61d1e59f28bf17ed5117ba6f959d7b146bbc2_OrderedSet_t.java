 import java.lang.Iterable;
 import java.util.ListIterator;
 
public class OrderedSet<T extends Shorter<? super T>> extends Set<T> {
 	public OrderedSet(){
 		super();
 	}
 
 	public void insert(T e){
 		if(list.contains(e)) return;
 
 		ListIterator<T> iter = this.list.iterator();
 		while(iter.hasNext()) {
 			if(iter.next().shorter(e))
 				continue;
 
 			if(iter.hasPrevious())
 				iter.previous();
 
 			iter.add(e);
 			return;
 		}
 
 		iter.add(e);
 		
 	}
 }
