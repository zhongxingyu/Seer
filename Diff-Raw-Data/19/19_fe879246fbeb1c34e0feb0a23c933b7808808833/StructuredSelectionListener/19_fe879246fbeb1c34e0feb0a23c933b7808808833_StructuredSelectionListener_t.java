 package sk.stuba.fiit.perconik.core.listeners;
 
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IWorkbenchPart;
import sk.stuba.fiit.perconik.core.Listener;
 
 public interface StructuredSelectionListener extends Listener
 {
 	public void selectionChanged(IWorkbenchPart part, IStructuredSelection selection);
 }
