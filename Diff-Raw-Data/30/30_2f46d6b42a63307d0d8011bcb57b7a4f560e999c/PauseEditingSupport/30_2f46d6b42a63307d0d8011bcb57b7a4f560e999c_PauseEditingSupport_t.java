 package de.ptb.epics.eve.editor.views.eventcomposite;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.ColumnViewer;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.TableViewer;
 
 import de.ptb.epics.eve.data.EventActions;
 import de.ptb.epics.eve.data.EventTypes;
 import de.ptb.epics.eve.data.scandescription.PauseEvent;
 import de.ptb.epics.eve.util.jface.MyComboBoxCellEditor;
 
 /**
  * {@link org.eclipse.jface.viewers.EditingSupport} for the pause column.
  * 
  * @author Marcus Michalsky
  * @since 1.2
  */
 public class PauseEditingSupport extends EditingSupport {
 
 	private static Logger logger = 
 			Logger.getLogger(PauseEditingSupport.class.getName());
 
 	private TableViewer viewer;
 
 	private List<String> eventActions;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param viewer the viewer
 	 */
 	public PauseEditingSupport(ColumnViewer viewer) {
 		super(viewer);
 		this.viewer = (TableViewer)viewer;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected CellEditor getCellEditor(Object element) {
 		this.eventActions = new ArrayList<String>();
 		for(EventActions action : EventActions.values()) {
 			eventActions.add(action.toString());
 		}
 		if(!((PauseEvent)element).getEventType().equals(EventTypes.MONITOR)) {
			this.eventActions.remove("ONOFF");
 		}
 		return new MyComboBoxCellEditor(this.viewer.getTable(), 
 				eventActions.toArray(new String[0]));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected boolean canEdit(Object element) {
 		return true;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected Object getValue(Object element) {
 		PauseEvent ce = (PauseEvent)element;
 		return this.eventActions.indexOf(ce.getEventAction().toString());
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void setValue(Object element, Object value) {
 		PauseEvent ce = (PauseEvent)element;
 		ce.setEventAction(EventActions.valueOf(this.eventActions.get((Integer)value)));
 		if (logger.isDebugEnabled()) {
 			logger.debug("set CIF of PauseEvent " + ce.getEvent().getName() +
 						" to " + value);
 		}
 	}
 }
