 package polyglot.ext.esj.primitives;
 
 import polyglot.ext.esj.tologic.*;
 
 import java.util.HashSet;
 import java.util.Iterator;
 
 public class ESJSet<E> extends HashSet<E> {
 
     public Integer dontcare;
     int relationizerStep = 0;    
     public LogVar var_log;
     public ESJSet<E> old;
     int clonerStep = 0;
     
     public ESJSet(LogVar dontcare, boolean isQuantifyVar) {
 	super();
     }
 
     public ESJSet() {
 	super();
     }
 
     boolean isRelationized() { return this.relationizerStep == LogMap.relationizerStep(); }
     boolean isCloned() { return this.clonerStep == LogMap.clonerStep(); }
     public ESJSet<E> old() { return old; }
 
     public ESJSet<E> clone() {
 	ESJSet<E> res = (ESJSet<E>) super.clone();
 	for (Object e : (ESJSet<Object>) this)
 	    if (e instanceof ESJObject)
 		((ESJObject)e).clone();
 	this.old = res;
 	return res;
     }
 
     public void relationize() { 
 	if (!isRelationized()) { 
 	    this.relationizerStep++;
 	    // FIXME
 	    for (Object e : (ESJSet<Object>) this)
 		if (e instanceof ESJObject)
 		    ((ESJObject) e).relationize();
 	    relationizeOld(); 
 	} 
     }
     
     public void relationizeOld() { 
     }
 
 
     public ESJSet<E> plus(E e) {
 	add(e);
 	return this;
     }
 
     public ESJSet<E> minus(E e) {
 	remove(e);
 	return this;
     }
 
    public boolean containsPtrEqCmp(Object o) {
	for (Object e : (ESJSet<Object>) this)
	    if (e == o) 
		return true;	
	return false;
    }
 
 }
