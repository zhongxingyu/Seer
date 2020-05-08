 package topshelf.gwt.common.client;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 public abstract class AbstractGroupedList<VALUE,
 	ITEM,
 	GROUP,
 	GROUPKEY extends Comparable<?>> {
 	
 	protected Map<GROUPKEY,GROUP> groups = new HashMap<GROUPKEY,GROUP>();
 	protected Map<VALUE,ITEM> items = new HashMap<VALUE,ITEM>();
 
 	public void setValues(Collection<VALUE> values) {
 		int cnt = 0;
 		for (VALUE value : values) {
 			addValue(cnt++, value);
 		}
 	}
 	
 	protected void removeValue(VALUE value) {
 		GROUPKEY key = getGroupKey(value);
 		GROUP group = groups.get(key);
 		if (null != group) {
 			ITEM item = items.remove(value);
 			if (null != item) {
 				removeFromGroup(group, item);
 			}
 		}
 	}
 
 	protected ITEM addValue(int index, VALUE value) {
 		GROUPKEY key = getGroupKey(value);
 		GROUP group = groups.get(key);
 		if (null == group) {
 			group = createGroup(key);
 			groups.put(key, group);
 		}
 		ITEM item = createItem(value);
 		addToGroup(group, item, index);			
 		items.put(value, item);
 		return item;
 	}
 	
 	protected Collection<GROUP> getGroups() {
 		return groups.values();
 	}
 	
	protected void reset() {
		items.clear();
		groups.clear();
	}
	
 	abstract public void addToGroup(GROUP group, ITEM item, int index);
 	abstract public void removeFromGroup(GROUP group, ITEM item);
 	abstract public ITEM createItem(VALUE value);
 	abstract public GROUPKEY getGroupKey(VALUE value);
 	abstract public GROUP createGroup(GROUPKEY key);
 }
