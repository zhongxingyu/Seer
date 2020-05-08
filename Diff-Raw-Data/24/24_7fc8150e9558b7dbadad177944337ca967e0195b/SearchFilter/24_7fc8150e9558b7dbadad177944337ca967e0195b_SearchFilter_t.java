 package com.yy.app;
 
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.googlecode.objectify.ObjectifyService;
 import com.googlecode.objectify.cmd.Query;
 import com.yy.app.site.Profile;
 
 public class SearchFilter{
 	public String orderField;
 	public long bookmark;
 	public int size=(Integer)Profile.I.pageSize;
 	private List<String> filterFields=new ArrayList<String>();
 	private List<Object> filterValues=new ArrayList<Object>();
 	private Class<?> type;
 	public String title;
 	
 	public SearchFilter(Class<?> type){
 		this.type=type;
 	}
 	
 	public SearchFilter addFilter(String name, Object value){
 		filterFields.add(name);
 		filterValues.add(value);
 		return this;
 	}
 	
 	public Object typedValue(String field, String value){
 		try {
 			return resolveTypedValue(value, type.getField(field).getGenericType());
 		} catch (Exception e) {
 			throw new RuntimeException(e.getMessage(),e);
 		}
 	}
 	
 	private Object resolveTypedValue(String value, Type fieldType){
 		if(fieldType==String.class)
 			return value;
 		else if(fieldType==Integer.class)
 			return Integer.parseInt(value);
 		else if(fieldType==Long.class)
 			return Long.parseLong(value);
 		else if(fieldType==Float.class)
 			return Float.parseFloat(value);
 		else if(fieldType==Double.class)
 			return Double.parseDouble(value);
 		else if(fieldType==Boolean.class)
 			return Boolean.parseBoolean(value);
 		else if(fieldType instanceof ParameterizedType){
 			ParameterizedType ptype=(ParameterizedType)fieldType;
 			fieldType=ptype.getActualTypeArguments()[0];
 			return resolveTypedValue(value,fieldType);
 		}
 		return value;
 	}
 	
 	@SuppressWarnings("rawtypes")
 	private Query getFilteredQuery(int limit){
 		Query query=ObjectifyService.ofy().load().type(type);
 		if(limit>0)
			query=query.limit(limit);
 		if(bookmark>0)
			query=query.filter("ID", bookmark);
 		
 		if(orderField!=null && orderField.length()>0)
			query=query.order(orderField);
 
 
 		for(int i=0; i<filterFields.size(); i++)
			query=query.filter(filterFields.get(i), filterValues.get(i));
 
 		return query;
 	}
 	
 	public int getCount(){
 		return getFilteredQuery(0).count();
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public List list(){
 		return getFilteredQuery(size).list();
 	}
 	
 	public Object of(String name){
 		int index=filterFields.indexOf(name);
 		if(index!=-1)
 			return filterValues.get(index);
 		return "";
 	}
 }
