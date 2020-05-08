 package org.dawnsci.macro;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.dawnsci.macro.generator.MacroFactory;
 import org.eclipse.dawnsci.macro.api.AbstractMacroGenerator;
 import org.eclipse.dawnsci.macro.api.IMacroEventListener;
 import org.eclipse.dawnsci.macro.api.IMacroService;
 import org.eclipse.dawnsci.macro.api.MacroEventObject;
 
 import py4j.reflection.MethodInvoker;
 
 /**
  * 
  * When defining services, if OSGi gets its knickers in a knot, you can delete 
  * the .metadata \.plugins\org.eclipse.pde.core\ uk.ac.diamond.dawn.product\org.eclipse.osgi
  * 
  * folder in the workspace of eclipse you are using to reset.
  * 
  * @author fcp94556
  *
  */
 public class MacroServiceImpl implements IMacroService {
 	
 	private static Set<IMacroEventListener> listeners;
 	
 	public MacroServiceImpl() {
 		if (listeners==null) {
 			listeners = Collections.synchronizedSet(new HashSet<IMacroEventListener>(11));
 			System.out.println("Started macro service");
 		}
 	}
 
 	@Override
 	public synchronized void publish(MacroEventObject evt) {
 				
 		if (listeners.isEmpty()) return;

		// We can automatically deal with some events, to reduce dependency
		// inside the API generating the objects. For instance regions can be generically
		// translated into the python which creates them
		evt = MacroFactory.generate(evt);
		if (evt==null) return;
		
		// If we have no command, not much point doing anythin
		if (!evt.isCommandAvailable()) return;
 		
 		// Clean up
 		for (Iterator<IMacroEventListener> iterator = listeners.iterator(); iterator.hasNext();) {
 			IMacroEventListener l = iterator.next();
 			if (l.isDisposed()) {
 				iterator.remove();
 				continue;
 			}
 		}
		
 		if (listeners.isEmpty()) return;
 		
 		// If Py4j is in the stack, we do not want to publish.
 		// This operation parses the stack so is dangerously slow, do
 		// not do it unless there are definitely macro listeners active
 		StackTraceElement[] stack = Thread.currentThread().getStackTrace();
 		for (StackTraceElement stackTraceElement : stack) {
 			if (stackTraceElement.getClassName()!=null && stackTraceElement.getClassName().equals(MethodInvoker.class.getName())) {
 				return; // We do not publish commands coming from p4j because these commands
 				        // come from the console. We only publish things that the user does in the UI.
 			}
 		}
 	
 		for (Iterator<IMacroEventListener> iterator = listeners.iterator(); iterator.hasNext();) {
 			IMacroEventListener l = iterator.next();
 			try {
 			    l.macroChangePerformed(evt);
 			} catch (Exception ne) {
 				ne.printStackTrace();
 			}
 		}
 	}
 
 	@Override
 	public void addMacroListener(IMacroEventListener l) {
 		listeners.add(l);
 	}
 
 	@Override
 	public void removeMacroListener(IMacroEventListener l) {
 		listeners.remove(l);
 	}
 
 	@Override
 	public AbstractMacroGenerator getGenerator(Class<? extends Object> clazz) {
 		return MacroFactory.getGenerator(clazz);
 	}
 
 }
