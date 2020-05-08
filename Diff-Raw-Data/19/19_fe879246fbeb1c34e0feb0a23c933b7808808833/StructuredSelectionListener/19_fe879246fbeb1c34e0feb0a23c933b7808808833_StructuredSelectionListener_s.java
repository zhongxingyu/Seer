 package sk.stuba.fiit.perconik.core.listeners;
 
 import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Listener;
 import org.eclipse.ui.IWorkbenchPart;
 
 public interface StructuredSelectionListener extends Listener
 {
 	public void selectionChanged(IWorkbenchPart part, IStructuredSelection selection);
 }
