 package org.omo.core;
 
 import java.util.Collection;
 import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
 
 import javax.swing.table.AbstractTableModel;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class TableModelView<InputKey, InputValue> extends AbstractTableModel implements View<InputKey, InputValue> {
 
 	private final Log log = LogFactory.getLog(getClass());
 	
 	public interface Mapper<InputKey, InputValue> {
 		InputKey getKey(InputValue value);
 		Object getField(InputValue value, int index);
 		List<String> getFieldNames();
 	}
 
 	private final Mapper<InputKey, InputValue> mapper;
	private final ConcurrentSkipListMap<InputKey, InputValue> map = new ConcurrentSkipListMap<InputKey, InputValue>();
 	
 	public TableModelView(Mapper<InputKey, InputValue> mapper) {
 		this.mapper = mapper;
 	}
 	
 	// Listener
 	
 	@Override
 	public void upsert(Collection<InputValue> upsertions) {
 		log.trace("UPDATE("+upsertions+")");
 		for (InputValue upsertion : upsertions) {
 			InputKey key = mapper.getKey(upsertion);
 			map.put(key, upsertion);
 			int index = map.headMap(key).size();
 			fireTableRowsUpdated(index, index);
 		}
 	}
 
 	@Override
 	public void upsert(InputValue upsertion) {
 		log.trace("UPDATE("+upsertion+")");
 		InputKey key = mapper.getKey(upsertion);
 		map.put(key, upsertion);
 		int index = map.headMap(key).size();
 		fireTableRowsUpdated(index, index);
 	}
 	
 	@Override
 	public void delete(Collection<InputKey> deletions) {
 		throw new UnsupportedOperationException("NYI");
 	}
 
 	@Override
 	public void delete(InputKey deletion) {
 		throw new UnsupportedOperationException("NYI");
 	}
 
 	// TableModel
 
 	
 	protected String columnNames[] = new String[]{"id", "version"};
 
 	@Override
 	public String getColumnName(int columnIndex) {
 		return mapper.getFieldNames().get(columnIndex);
 	}
 	
 	@Override
 	public int getColumnCount() {
 		return mapper.getFieldNames().size();
 	}
 
 	@Override
 	public int getRowCount() {
 		return map.size();
 	}
 
 	@Override
 	public Object getValueAt(int rowIndex, int columnIndex) {
 		InputValue value = (InputValue)map.values().toArray()[rowIndex]; // yikes !!
 		return mapper.getField(value, columnIndex);
 	}
 
 }
