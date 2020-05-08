 package org.eclipse.dltk.validators.internal.ui.externalchecker;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Vector;
 
 import org.eclipse.dltk.validators.internal.core.externalchecker.Rule;
 
 public class RulesList {
 	
 	private Vector rules = new Vector();
 	private Set changeListeners = new HashSet();
 	private String[] types = {"Error", "Warning"};
 	
 	
 	public void addRule(){
		Rule r = new Rule("Put rule here...", "Error");
 		rules.add(r);
 		Iterator iterator = changeListeners.iterator();
 		while(iterator.hasNext()){
 			((IRulesListViewer)iterator.next()).addRule(r);
 		}
 	}
 	public void removeChangeListener(IRulesListViewer viewer) {
 		changeListeners.remove(viewer);
 	}
 
 	public void addChangeListener(IRulesListViewer viewer) {
 		changeListeners.add(viewer);
 	}
 	
 	public Vector getRules(){
 		return rules;
 	}
 	
 	public void ruleChanged(Rule r) {
 		Iterator iterator = changeListeners.iterator();
 		while (iterator.hasNext())
 			((IRulesListViewer) iterator.next()).updateRule(r);
 	}
 	
 	public  void addRule(Rule r){
 		rules.add(r);
 		Iterator iterator = changeListeners.iterator();
 		while(iterator.hasNext()){
 			((IRulesListViewer)iterator.next()).addRule(r);
 		}
 	}
 	
 	public void removeRule(Rule task) {
 		rules.remove(task);
 		Iterator iterator = changeListeners.iterator();
 		while (iterator.hasNext())
 			((IRulesListViewer) iterator.next()).removeRule(task);
 	}
 	
 	public  String[] getTypes(){
 		return types;
 	}
 }
