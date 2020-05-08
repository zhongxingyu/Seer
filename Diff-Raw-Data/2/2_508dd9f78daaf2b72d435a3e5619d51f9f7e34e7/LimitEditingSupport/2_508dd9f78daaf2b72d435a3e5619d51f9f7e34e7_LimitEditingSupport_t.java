 package de.ptb.epics.eve.editor.views.eventcomposite;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TextCellEditor;
 
 import de.ptb.epics.eve.data.EventTypes;
 import de.ptb.epics.eve.data.scandescription.ControlEvent;
 import de.ptb.epics.eve.util.jface.MyComboBoxCellEditor;
 
 /**
  * {@link org.eclipse.jface.viewers.EditingSupport} for the limit column.
  * 
  * @author Marcus Michalsky
  * @since 1.1
  */
 public class LimitEditingSupport extends EditingSupport {
 
 	private static Logger logger = 
 			Logger.getLogger(LimitEditingSupport.class.getName());
 	
 	private TableViewer viewer;
 	
 	private List<String> discreteValues;
 	
 	/**
 	 * Constructor.
 	 * 
 	 * @param viewer
 	 */
 	public LimitEditingSupport(TableViewer viewer) {
 		super(viewer);
 		this.viewer = viewer;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected CellEditor getCellEditor(Object element) {
 		ControlEvent ce = (ControlEvent)element;
 		if(ce.getEvent().getMonitor().getDataType().isDiscrete()) {
 			this.discreteValues = new ArrayList<String>();
 			this.discreteValues.addAll(ce.getEvent().getMonitor().getDataType().
 							getDiscreteValues());
 			return new MyComboBoxCellEditor(this.viewer.getTable(), 
 					this.discreteValues.toArray(new String[0]));
 		}
 		TextCellEditor editor = new TextCellEditor(this.viewer.getTable()) {
 			@Override protected void focusLost() {
 				if (isActivated()) {
 					fireCancelEditor();
 				}
 				deactivate();
 			}
 		};
 		editor.setValidator(new LimitValidator(ce));
 		return editor;
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected boolean canEdit(Object element) {
 		EventTypes type = ((ControlEvent)element).getEvent().getType();
 		if (type == EventTypes.SCHEDULE || type == EventTypes.DETECTOR) {
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected Object getValue(Object element) {
 		ControlEvent ce = (ControlEvent)element;
 		if (ce.getEvent().getMonitor().getDataType().isDiscrete()) {
			return discreteValues.indexOf(ce.getLimit().getValue());
 		} else {
 			return ce.getLimit().getValue();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void setValue(Object element, Object value) {
 		ControlEvent ce = (ControlEvent)element;
 		if (ce.getEvent().getMonitor().getDataType().isDiscrete()) {
 			ce.getLimit().setValue(discreteValues.get((Integer)value));
 			if (logger.isDebugEnabled()) {
 				logger.debug("set limit of " + ce.getEvent().getName() + 
 						" to " + discreteValues.get((Integer)value));
 			}
 		} else {
 			ce.getLimit().setValue((String)value);
 			if (logger.isDebugEnabled()) {
 				logger.debug("set limit of " + ce.getEvent().getName() + 
 						" to " + (String)value);
 			}
 		}
 	}
 }
