 package sk.stuba.fiit.perconik.core.listeners;
 
 import org.eclipse.jface.text.IMarkSelection;
 import org.eclipse.ui.IWorkbenchPart;
import sk.stuba.fiit.perconik.core.Listener;
 
 public interface MarkSelectionListener extends Listener
 {
 	public void selectionChanged(IWorkbenchPart part, IMarkSelection selection);
 }
