 package jkind.xtext.util;
 
 import java.util.List;
 
 import jkind.xtext.jkind.util.JkindSwitch;
 
 import org.eclipse.emf.ecore.EObject;
 
 public class JKindIterator extends JkindSwitch<Object> {
 	protected static final Object DONE = new Object();
 
 	protected void doSwitchList(List<? extends EObject> list) {
 		for (EObject e : list) {
 			doSwitch(e);
 		}
 	}
 
 	@Override
 	public Object defaultCase(EObject e) {
		for (EObject sub : e.eContents()) {
			doSwitch(sub);
		}
 		return DONE;
 	}
 }
