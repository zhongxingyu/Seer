 package org.vamdc.portal.session.queryBuilder.forms;
 
 
 import org.vamdc.portal.session.queryBuilder.QueryData;
 
 public class QueryEditForm extends AbstractForm implements Form{
 
	public String getTitle() { return "Query comments"; }
 	public Integer getOrder() { return Order.Query; }
 	public String getView() { return "/xhtml/query/forms/editorForm.xhtml"; }
 	
 	private String queryString = "";
 	
 	public QueryEditForm(){
 	}
 	
 	@Override
 	public String getValue(){
 		if (queryString.length()>0)
 			return queryString;
 		return queryData.buildQueryString();
 	}
 	
 	public void setValue(String newQueryString){
 		if (!newQueryString.equals(queryData.buildQueryString()))
 				queryString=newQueryString;
 	}
 	
 	@Override
 	public void setQueryData(QueryData queryData){
 		super.setQueryData(queryData);
 		queryData.setQueryEditForm(this);
 	}
 	
 	@Override
 	public void delete(){
 		queryData.setQueryEditForm(null);
 		super.delete();
 	}
 
 	@Override
 	public void clear(){
 		super.clear();
 		queryString="";
 	}
 	
 }
