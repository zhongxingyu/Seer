 package org.romaframework.aspect.persistence;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 public class QueryByFilterItemGroup implements QueryByFilterItem {
 
 	public static final String			PREDICATE_OR	= QueryByFilter.PREDICATE_OR;
 	public static final String			PREDICATE_AND	= QueryByFilter.PREDICATE_AND;
 
 	private String									predicate;
 	private List<QueryByFilterItem>	items					= new ArrayList<QueryByFilterItem>();
 
 	public QueryByFilterItemGroup(String predicate) {
 		this.predicate = predicate;
 	}
 
 	public void addItem(QueryByFilterItem item) {
 		items.add(item);
 	}
 
 	public String getPredicate() {
 		return predicate;
 	}
 
 	public List<QueryByFilterItem> getItems() {
 		return items;
 	}
 
 	public void addItem(String iName, QueryOperator iOperator, Object iValue) {
 		addItem(new QueryByFilterItemPredicate(iName, iOperator, iValue));
 	}
 
 	public void addItem(String iCondition) {
 		addItem(new QueryByFilterItemText(iCondition));
 	}
 
 	public QueryByFilterItemGroup addGroup(String predicate) {
 		QueryByFilterItemGroup item = new QueryByFilterItemGroup(predicate);
 		addItem(item);
 		return item;
 	}
 
 	public void addReverseItem(QueryByFilter byFilter, String field) {
 		addReverseItem(byFilter, field, null, QueryOperator.EQUALS);
 	}
 
 	public void addReverseItem(QueryByFilter byFilter, String field, String fieldReverse) {
		addReverseItem(byFilter, field, fieldReverse, QueryOperator.EQUALS);
 	}
 
 	public void addReverseItem(QueryByFilter byFilter, String field, String fieldReverse, QueryOperator operator) {
 		addItem(new QueryByFilterItemReverse(byFilter, field, fieldReverse, operator));
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder buffer = new StringBuilder();
 		buffer.append(" ( ");
 		Iterator<QueryByFilterItem> iter = items.iterator();
 		if (iter.hasNext()) {
 			buffer.append(iter.next());
 			while (iter.hasNext()) {
 				buffer.append(" ").append(predicate).append(" ");
 				buffer.append(iter.next());
 			}
 		}
 		buffer.append(" ) ");
 		return buffer.toString();
 	}
 }
