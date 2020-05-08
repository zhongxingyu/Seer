 package org.dawb.hdf5.editor;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
 
 
 public class H5SelectionProvider implements ISelectionProvider {
 
 	private Set<ISelectionChangedListener> listeners;
 	private ISelection currentSelection;
 	
 	public H5SelectionProvider() {
 		listeners = new HashSet<ISelectionChangedListener>(11);
 	}
 	
 	@Override
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		listeners.add(listener);
 	}
 
 	@Override
 	public ISelection getSelection() {
		if (currentSelection==null) currentSelection = new StructuredSelection();
 		return currentSelection;
 	}
 
 	@Override
 	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
 		listeners.remove(listener);
 	}
 
 	@Override
 	public void setSelection(ISelection selection) {
 		this.currentSelection = selection;
 		SelectionChangedEvent e = new SelectionChangedEvent(this, selection);
 		for (ISelectionChangedListener s : listeners) s.selectionChanged(e);
 	}
 
 }
