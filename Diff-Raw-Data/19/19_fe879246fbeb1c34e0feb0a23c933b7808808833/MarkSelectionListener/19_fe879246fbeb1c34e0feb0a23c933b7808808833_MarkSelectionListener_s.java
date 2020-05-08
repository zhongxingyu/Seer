 package sk.stuba.fiit.perconik.core.listeners;
 
 import org.eclipse.jface.text.IMarkSelection;
import org.eclipse.swt.widgets.Listener;
 import org.eclipse.ui.IWorkbenchPart;
 
 public interface MarkSelectionListener extends Listener
 {
 	public void selectionChanged(IWorkbenchPart part, IMarkSelection selection);
 }
