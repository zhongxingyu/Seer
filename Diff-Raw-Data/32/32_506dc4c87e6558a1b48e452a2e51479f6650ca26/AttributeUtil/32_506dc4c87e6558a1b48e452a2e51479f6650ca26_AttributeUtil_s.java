 package org.cytoscape.cpath2.internal.util;
 
 import java.util.List;
 import java.util.Map.Entry;
 
 import org.cytoscape.model.CyColumn;
 import org.cytoscape.model.CyRow;
 import org.cytoscape.model.CyTable;
 import org.cytoscape.model.CyTableEntry;
 import org.cytoscape.model.CyNetwork;
 
 public class AttributeUtil {
 	
 	public static void set(CyNetwork network, CyTableEntry entry, String name, Object value, Class<?> type) {
 		set(network, entry, null, name, value, type);
 	}
 
 	public static void copyAttributes(CyNetwork network, CyTableEntry source, CyTableEntry target) {
 		CyRow sourceRow = network.getRow(source);
 		for (Entry<String, Object> entry : sourceRow.getAllValues().entrySet()) {
 			String key = entry.getKey();
 			Object value = entry.getValue();
 			CyColumn column = sourceRow.getTable().getColumn(key);
 			Class<?> type;
 			if (value instanceof List) {
 				type = column.getListElementType();
 			} else {
 				type = column.getType();
 			}
 			set(network, target, key, value, type);
 		}
 	}
 	
	public static void copyAttributes(CyTableEntry source, CyTableEntry target) {
		// TODO Auto-generated method stub
		
	}
	
 	
 	public static void set(CyNetwork network, CyTableEntry entry, String tableName, String name, Object value, Class<?> type) {
 		CyRow row = (tableName==null) ? network.getRow(entry) : network.getRow(entry,tableName);
 		CyTable table = row.getTable();
 		CyColumn column = table.getColumn(name);
 		if (column == null) {
 			if (value instanceof List) {
 				table.createListColumn(name, type, false);
 			} else { 
 				table.createColumn(name, type, false);
 			}
 		}
 		row.set(name, value);
 	}
 }
