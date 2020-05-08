 package core;
 
 import java.util.HashSet;
 import java.util.Iterator;
 
 public class Closure {
 	//HashSet<FunctionalDependency> dependencies;
 	HashSet<String> closureAttributes;
 	
 
 	HashSet<String>	startAttributes;
 
 	public Closure(){
 		//dependencies = new HashSet<FunctionalDependency>();
 		this.startAttributes = new HashSet<String>();
 		this.closureAttributes = new HashSet<String>();
 	}
 	
	@SuppressWarnings("unchecked")
 	public Closure(HashSet<String> startAttributes, HashSet<FunctionalDependency> knownDependencies){
 		//dependencies = new HashSet<FunctionalDependency>();
 		this.startAttributes = (HashSet<String>) startAttributes.clone();
 		this.closureAttributes = (HashSet<String>) startAttributes.clone();
 		
 		computeClosure(knownDependencies);
 	}
 	
 	public void addStartAttribute(String s){
 		startAttributes.add(s);
 		closureAttributes.add(s);
 	}
 	
	@SuppressWarnings("unchecked")
 	public void computeClosure(HashSet<FunctionalDependency> knownDependencies){
 		HashSet<FunctionalDependency> known = (HashSet<FunctionalDependency>) knownDependencies.clone();
 		
 		Iterator<FunctionalDependency> it = known.iterator();
 		while(it.hasNext()){
 			FunctionalDependency dep = it.next();
 			if(attributesInLeftSide(closureAttributes, dep)){
 				if(closureAttributes.containsAll(dep.getRightList())){
 					continue;
 				}
 				
 				//dependencies.add(dep);
 				closureAttributes.addAll(dep.getRightList());
 				computeClosure(known);
 			}
 		}
 	}
 	
 	
 	
 	private boolean attributesInLeftSide(HashSet<String> attributes, FunctionalDependency dependency){
 		return attributes.containsAll(dependency.getLeftList());
 	}
 	
 	public HashSet<String> getClosureAttributes() {
 		return closureAttributes;
 	}
 	
 	
 }
