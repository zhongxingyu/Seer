 package org.dawb.common.ui.views;
 
 import java.util.ArrayList;
 import java.util.List;
import java.util.Vector;
 
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 
 /**
  * Joins several selection providers so that one view
  * can report selections from many places.
  * 
  * @author fcp94556
  *
  */
 public class CompositeSelectionProvider implements ISelectionProvider, ISelectionChangedListener {
 	
 	
 	private List<ISelectionProvider>        providers;
 	private List<ISelectionChangedListener> listeners;
 	private int selectionIndex = -1;
 	private ISelection lastSelection;
 	
 	/**
 	 * A compound provider allows several providers to be joined.
 	 * @param type either notify when any or when a particular provider changes.
 	 */
 	public CompositeSelectionProvider() {
		providers = new Vector<ISelectionProvider>();
		listeners = new Vector<ISelectionChangedListener>();
 	}
 
 	@Override
 	public void addSelectionChangedListener(ISelectionChangedListener listener) {
 		listeners.add(listener);
 	}
 
 	@Override
 	public ISelection getSelection() {
 		return lastSelection;
 	}
 
 	@Override
 	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
 		listeners.remove(listener);
 	}
 
 	@Override
 	public void setSelection(ISelection selection) {
 		for (ISelectionProvider sp : providers) {
 			try {
 			    sp.setSelection(selection);
 			} catch (Throwable ignored) {
 				continue;
 			}
 		}
 	}
 
     public void addProvider(ISelectionProvider sp) {
     	providers.add(sp);
         sp.addSelectionChangedListener(this);
     }
 	
 	public int getSelectionIndex() {
 		return selectionIndex;
 	}
 
 	public void setSelectionIndex(int selectionIndex) {
 		this.selectionIndex = selectionIndex;
 	}
 
 	@Override
 	public void selectionChanged(SelectionChangedEvent evt) {
 		lastSelection = evt.getSelection();
 		for (ISelectionChangedListener scl : listeners) scl.selectionChanged(evt);
 	}
 
 }
