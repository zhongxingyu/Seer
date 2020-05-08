 package de.ptb.epics.eve.viewer.views.plotview.ui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 
 import de.ptb.epics.eve.viewer.views.plotview.MathTableElement;
 
 /**
  * 
  * @author ?
  * @author Marcus Michalsky
  */
 public class MathTableContentProvider implements IStructuredContentProvider {
	// TODO very weird use of content provider.
	// use setInput of TableViewer !
 	private TableViewer tableViewer;
 	private List<MathTableElement> elements;
 
 	/**
 	 * Constructs a <code>MathTableContentProvider</code>.
 	 * 
 	 * @param tableViewer the table viewer which should be provided with 
 	 * 		   contents.
 	 */
 	public MathTableContentProvider(TableViewer tableViewer) {
 		elements = new ArrayList<MathTableElement>();
 		this.tableViewer = tableViewer;
 	}
 
 	/** 
 	 * {@inheritDoc} 
 	 */
 	@Override
 	public Object[] getElements(Object inputElement) {
 		return elements.toArray();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void dispose() {
		tableViewer.setInput(null);
 		elements.clear();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 	}
 	
 	/**
 	 * Adds an element to the content provider.
 	 * @param element the {@link de.ptb.epics.eve.viewer.views.plotview.MathTableElement}
 	 * 	  	  that should be added
 	 */
 	public void addElement(MathTableElement element){
 		elements.add(element);
 		tableViewer.add(element);
 	}
 
 	/**
 	 * Clears all elements.
 	 */
 	public void clear() {
 		if (elements.size() > 0) {
 			tableViewer.remove(elements.toArray());
 			elements.clear();
 		}
 	}
 }
