 package org.omo.core;
 
 import java.util.Collection;
 import java.util.concurrent.ConcurrentSkipListMap;
 
 import javax.swing.table.AbstractTableModel;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
import edu.emory.mathcs.backport.java.util.concurrent.CountDownLatch;

 public class TableModelView<K, V> extends AbstractTableModel implements View<K, V> {
 
 	private final Log log = LogFactory.getLog(getClass());
	private final CountDownLatch latch = new CountDownLatch(1);
 	private Metadata<K, V> metadata;
 	private final ConcurrentSkipListMap<K, V> map = new ConcurrentSkipListMap<K, V>();
 	
 	public void setMetadata(Metadata<K, V> metadata) {
 		this.metadata = metadata;
		latch.countDown();
 	}
 
	public Metadata<K, V> getMetadata() {
		try {
		latch.await();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return metadata;
	}
	
 	// TableModel
 	
 	// Listener
 	
 	@Override
 	public void update(Collection<V> updates) {
 		//log.debug("UPDATE("+updates+")");
		Metadata<K, V> metadata = getMetadata();
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
		return getMetadata().getAttributeNames().get(columnIndex);
 	}
 	
 	@Override
 	public int getColumnCount() {
		return getMetadata().getAttributeNames().size();
 	}
 
 	@Override
 	public int getRowCount() {
 		return map.size();
 	}
 
 	@Override
 	public Object getValueAt(int rowIndex, int columnIndex) {
 		V value = (V)map.values().toArray()[rowIndex]; // yikes !!
		return getMetadata().getAttributeValue(value, columnIndex);
 	}
 
 }
