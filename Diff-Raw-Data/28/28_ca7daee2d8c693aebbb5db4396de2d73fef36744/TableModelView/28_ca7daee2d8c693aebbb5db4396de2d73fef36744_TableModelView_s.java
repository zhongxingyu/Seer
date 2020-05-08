 package org.omo.core;
 
 import java.util.Collection;
 import java.util.concurrent.ConcurrentSkipListMap;
 
 import javax.swing.table.AbstractTableModel;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class TableModelView<K, V> extends AbstractTableModel implements View<K, V> {
 
 	private final Log log = LogFactory.getLog(getClass());
	
 	private Metadata<K, V> metadata;
 	private final ConcurrentSkipListMap<K, V> map = new ConcurrentSkipListMap<K, V>();
 	
 	public void setMetadata(Metadata<K, V> metadata) {
 		this.metadata = metadata;
 	}
 
 	// TableModel
 	
 	// Listener
 	
 	@Override
 	public void update(Collection<V> updates) {
 		//log.debug("UPDATE("+updates+")");
 		if (updates != null)
 			for (V insertion : updates) {
 				K key = metadata.getKey(insertion);
 				map.put(key, insertion);
 				int index = map.headMap(key).size();
 				fireTableRowsInserted(index, index);
 			}
 	}
 
 	protected String columnNames[] = new String[]{"id", "version"};
 
 	@Override
 	public String getColumnName(int columnIndex) {
		return metadata.getAttributeNames().get(columnIndex);
 	}
 	
 	@Override
 	public int getColumnCount() {
		return metadata.getAttributeNames().size();
 	}
 
 	@Override
 	public int getRowCount() {
 		return map.size();
 	}
 
 	@Override
 	public Object getValueAt(int rowIndex, int columnIndex) {
 		V value = (V)map.values().toArray()[rowIndex]; // yikes !!
		return metadata.getAttributeValue(value, columnIndex);
 	}
 
 }
