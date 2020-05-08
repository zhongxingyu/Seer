 package sk.stuba.fiit.perconik.core.listeners;
 
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.ui.IWorkbenchPart;
import sk.stuba.fiit.perconik.core.Listener;
 
 public interface TextSelectionListener extends Listener
 {
 	public void selectionChanged(IWorkbenchPart part, ITextSelection selection);
 }
