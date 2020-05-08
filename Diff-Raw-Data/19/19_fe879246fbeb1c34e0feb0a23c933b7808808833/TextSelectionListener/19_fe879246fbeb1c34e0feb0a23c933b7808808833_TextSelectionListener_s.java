 package sk.stuba.fiit.perconik.core.listeners;
 
 import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Listener;
 import org.eclipse.ui.IWorkbenchPart;
 
 public interface TextSelectionListener extends Listener
 {
 	public void selectionChanged(IWorkbenchPart part, ITextSelection selection);
 }
