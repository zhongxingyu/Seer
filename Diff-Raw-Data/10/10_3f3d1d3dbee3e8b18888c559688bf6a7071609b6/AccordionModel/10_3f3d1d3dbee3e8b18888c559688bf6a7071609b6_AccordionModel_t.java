 package gui.customElements.accordion;
 
 import gui.customElements.SimConfigPanel;
 import gui.pluginRegistry.DependencyChecker;
 import gui.pluginRegistry.SimPropRegistry;
 
 import java.util.List;
 import java.util.Map.Entry;
 
 import javax.swing.event.TableModelListener;
 import javax.swing.table.TableModel;
 
 import log.LogLevel;
 import log.Logger;
 
 public class AccordionModel implements TableModel {
 
 	private SimPropRegistry _spr;
 	private List<Entry<String, String>> _propertiesInThisCategory;
 
 	public AccordionModel(String category) {
 
 		_spr = SimPropRegistry.getInstance();
 		_propertiesInThisCategory = _spr.getCategoryItems(category);
 	}
 
 	@Override
 	public void addTableModelListener(TableModelListener arg0) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public Class<?> getColumnClass(int arg0) {
 
 		if (arg0 == 1) {
 			return Object.class;
 		} else if (arg0 == 0) {
 			// First row is always a string (property name)
 			return String.class;
 		}
 		return String.class;
 	}
 
 	@Override
 	public int getColumnCount() {
 		return 2;
 	}
 
 	@Override
 	public String getColumnName(int arg0) {
 		return "col " + arg0;
 	}
 
 	@Override
 	public int getRowCount() {
 		return _propertiesInThisCategory.size();
 	}
 
 	@Override
 	public Object getValueAt(int arg0, int arg1) {
 		
 		if (arg1 == 0){
 			return _propertiesInThisCategory.get(arg0).getKey();
 		}
 		
 		return _spr.getValue(_propertiesInThisCategory.get(arg0).getKey()).getValue();
 	}
 
 	@Override
 	public boolean isCellEditable(int arg0, int arg1) {
 		if (arg1 == 1 && _spr.getValue(_propertiesInThisCategory.get(arg0).getKey()).getEnable() ){
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void removeTableModelListener(TableModelListener arg0) {
 		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void setValueAt(Object arg0, int arg1, int arg2) {
 		
 		String id = _propertiesInThisCategory.get(arg1).getKey();
 		Logger.Log(LogLevel.DEBUG, "Changed " + id);
 		_spr.setValue(id, arg0);
 		
 		DependencyChecker.checkAll(_spr);
			SimConfigPanel.setStatusofSaveButton(!DependencyChecker.errorsInConfig);
 	}
 
 	public List<Entry<String, String>> getProperties() {
 		return _propertiesInThisCategory;
 	}
 
 }
