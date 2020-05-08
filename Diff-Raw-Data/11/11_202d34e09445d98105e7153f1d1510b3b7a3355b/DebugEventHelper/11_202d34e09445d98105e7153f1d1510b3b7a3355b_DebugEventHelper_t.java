 package org.eclipse.dltk.debug.internal.core.model;
 
 import org.eclipse.debug.core.DebugEvent;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.model.IDebugElement;
 
 public final class DebugEventHelper {
 	private DebugEventHelper() {
 	}
 
 	private static void fireEvent(DebugEvent event) {
 		DebugPlugin.getDefault().fireDebugEventSet(new DebugEvent[] { event });
 	}
 
 	public static void fireCreateEvent(IDebugElement element) {
 		fireEvent(new DebugEvent(element, DebugEvent.CREATE));
 	}
 
 	public static void fireResumeEvent(IDebugElement element, int detail) {
 		fireEvent(new DebugEvent(element, DebugEvent.RESUME, detail));
 	}
 
 	public static void fireSuspendEvent(IDebugElement element, int detail) {
 		fireEvent(new DebugEvent(element, DebugEvent.SUSPEND, detail));
 	}
 
 	public static void fireTerminateEvent(IDebugElement element) {
 		fireEvent(new DebugEvent(element, DebugEvent.TERMINATE));
 	}

	public static void fireChangeEvent(IDebugElement scriptVariable) {
		fireEvent(new DebugEvent(scriptVariable, DebugEvent.CHANGE));
	}
 }
