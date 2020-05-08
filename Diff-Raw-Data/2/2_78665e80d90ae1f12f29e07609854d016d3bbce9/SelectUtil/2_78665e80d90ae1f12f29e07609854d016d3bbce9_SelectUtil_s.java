 package csplugins.jActiveModules.util;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.cytoscape.model.CyColumn;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyNode;
 import org.cytoscape.model.CyRow;
 import org.cytoscape.model.CyTableEntry;
 import org.cytoscape.model.CyTable;
 
 
 public class SelectUtil {
 
 	public static Set<CyNode> getSelectedNodes(CyNetwork cyNetwork) {
 		return getSelected(cyNetwork.getNodeList());
 	}
 	
 	static <T extends CyTableEntry> Set<T> getSelected(Collection<T> items) {
 		Set<T> entries = new HashSet<T>();
 		for (T item : items) {
 			CyRow row = item.getCyRow();
 			if (row.get(CyNetwork.SELECTED, Boolean.class)) {
 				entries.add(item);
 			}
 		}
 		return entries;
 	}
 
	static String[] getColumnNames(CyTable tbl) {
         CyColumn[] cols = (CyColumn[])tbl.getColumns().toArray();
         String[] names = new String[cols.length];
 
         for (int i=0; i< cols.length; i++){
         	names[i] = cols[i].getName();
         }
         return names;
 	}
 }
