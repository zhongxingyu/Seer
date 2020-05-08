 package org.vamdc.portal.session.queryBuilder;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.TreeSet;
 
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.vamdc.dictionary.Restrictable;
 import org.vamdc.portal.session.queryBuilder.forms.Form;
 import org.vamdc.portal.session.queryBuilder.forms.Order;
 
 @Name("queryData")
 @Scope(ScopeType.CONVERSATION)
 public class QueryData {
 
 
 	private Collection<Form> forms=Collections.synchronizedSet(new TreeSet<Form>(new Order()));
 	private List<Form> formsList = Collections.synchronizedList(new ArrayList<Form>());
 	
 	//Species-related forms
 	private Collection<Form> speciesForms=Collections.synchronizedSet(new TreeSet<Form>(new Order()));
 	
 	private Form queryEditForm=null;
 	
 	private String comments="";
 	
 	private String editedQueryId;
 
 	public Collection<Restrictable> getKeywords(){
 		EnumSet<Restrictable> result = EnumSet.noneOf(Restrictable.class);
 		for (Form form:getForms()){
 			result.addAll(form.getKeywords());
 		}
 		return result;
 			
 	}
 
 	public String getQueryString(){
 		if (queryEditForm!=null && queryEditForm.getValue().length()>0)
 			return queryEditForm.getValue();
		return "select * where "+QueryGenerator.getFormsQuery(getForms()); 
 	}
 	
 
 
 
 
 	public boolean isValid(){
 		return getKeywords().size()>0;
 	}
 
 	/**
 	 * This method must be fast and must return a fast collection
 	 * @return
 	 */
 	public List<Form> getForms(){
 		return formsList;
 	}
 	
 	public Collection<Form> getSpeciesForms(){
 		return new ArrayList<Form>(speciesForms);
 	}
 	
 	/**
 	 * Check if we can add a form
 	 * @param newForm form to add
 	 * @return true if form can be added
 	 */
 	private boolean canAdd(Form newForm){
 		if (newForm.getOrder()<Order.SINGLE_LIMIT)
 			return true;
 		for (Form form:forms){
 			if (form.getOrder().equals(newForm.getOrder()))
 				return false;
 		}
 		return true;
 	}
 	
 	public void addForm(Form form){
 		if (canAdd(form)){
 			forms.add(form);
 			form.setQueryData(this);
 			if (form.getOrder()<Order.SPECIES_LIMIT)
 				speciesForms.add(form);
 			formsList = Collections.synchronizedList(new ArrayList<Form>(forms));
 		}
 	}
 	
 	public void setQueryEditForm(Form form){
 		this.queryEditForm = form;
 	}
 	
 	public void deleteForm(Form form){
 		forms.remove(form);
 		if (form.getOrder()<Order.SPECIES_LIMIT)
 			speciesForms.remove(form);
 		formsList = Collections.synchronizedList(new ArrayList<Form>(forms));
 	}
 	
 	public String getComments() { return comments; }
 	public void setComments(String comments) { this.comments = comments; }
 	
 	public String getEditQueryId() { return editedQueryId; }
 	public void setEditQueryId(String editQueryId) { this.editedQueryId = editQueryId; }
 	
 	public void loadQuery(String queryString) {
 		// TODO load forms contents from the query string.
 	}
 
 }
