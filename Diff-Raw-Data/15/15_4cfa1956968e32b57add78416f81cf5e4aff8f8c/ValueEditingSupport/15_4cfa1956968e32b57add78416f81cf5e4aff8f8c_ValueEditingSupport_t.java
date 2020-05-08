 package de.ptb.epics.eve.editor.views.scanmoduleview.prescancomposite;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TextCellEditor;
 
 import de.ptb.epics.eve.data.DataTypes;
 import de.ptb.epics.eve.data.scandescription.Prescan;
 import de.ptb.epics.eve.util.jface.MyComboBoxCellEditor;
 
 /**
  * {@link org.eclipse.jface.viewers.EditingSupport} of the value column.
  * 
  * @author Marcus Michalsky
  * @since 1.1
  */
 public class ValueEditingSupport extends EditingSupport {
 
 	private TableViewer viewer;
 	
 	/**
 	 * Constructor.
 	 * 
 	 * @param viewer
 	 */
 	public ValueEditingSupport(TableViewer viewer) {
 		super(viewer);
 		this.viewer = viewer;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected CellEditor getCellEditor(Object element) {
 		final Prescan prescan = (Prescan)element;
 		if(prescan.getAbstractPrePostscanDevice().isDiscrete()) {
 			List<String> discreteValues = new ArrayList<String>();
 			if (prescan.getAbstractPrePostscanDevice().getValue().
 					getType().equals(DataTypes.ONOFF)) {
 				discreteValues.add("On");
 				discreteValues.add("Off");
 			} else if (prescan.getAbstractPrePostscanDevice().getValue().
 					getType().equals(DataTypes.OPENCLOSE)) {
 				discreteValues.add("Open");
 				discreteValues.add("Close");
 			} else {
 				discreteValues.addAll(prescan.getAbstractPrePostscanDevice().
 						getValue().getDiscreteValues());
 			}
 			return new MyComboBoxCellEditor(this.viewer.getTable(), 
 					discreteValues.toArray(new String[0]));
 		} else {
 			return new TextCellEditor(this.viewer.getTable()) {
 				@Override protected void focusLost() {
 					if(isActivated()) {
 						fireCancelEditor();
 					}
 					deactivate();
 				}
 			};
 		}
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
 		final Prescan prescan = (Prescan)element;
 		if(prescan.getAbstractPrePostscanDevice().isDiscrete()) {
 			if (prescan.getAbstractPrePostscanDevice().getValue().
 					getType().equals(DataTypes.ONOFF)) {
 				if (prescan.getValue().equals("On")) {
 					return 0;
 				} else if (prescan.getValue().equals("Off")) {
 					return 1;
 				} else {
					return 0;
 				}
 			} else if (prescan.getAbstractPrePostscanDevice().getValue().
 					getType().equals(DataTypes.OPENCLOSE)) {
 				if (prescan.getValue().equals("Open")) {
 					return 0;
 				} else if (prescan.getValue().equals("Close")) {
 					return 1;
 				} else {
					return 0;
 				}
 			} else {
 				return prescan.getAbstractPrePostscanDevice().getValue().
 					getDiscreteValues().indexOf(element);
 			}
 		} else {
 			return prescan.getValue();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void setValue(Object element, Object value) {
 		final Prescan prescan = (Prescan)element;
 		if(prescan.getAbstractPrePostscanDevice().isDiscrete()) {
 			if (prescan.getAbstractPrePostscanDevice().getValue().
 					getType().equals(DataTypes.ONOFF)) {
 				if (((Integer)value) == 0) {
 					prescan.setValue("On");
 				} else {
 					prescan.setValue("Off");
 				}
 			} else if (prescan.getAbstractPrePostscanDevice().getValue().
 					getType().equals(DataTypes.OPENCLOSE)) {
 				if (((Integer)value) == 0) {
 					prescan.setValue("Open");
 				} else {
 					prescan.setValue("Close");
 				}
 			} else {
 				prescan.setValue(prescan.getAbstractPrePostscanDevice().
 					getValue().getDiscreteValues().get((Integer)value));
 			}
 		} else {
 			prescan.setValue((String)value);
 		}
 	}
 }
