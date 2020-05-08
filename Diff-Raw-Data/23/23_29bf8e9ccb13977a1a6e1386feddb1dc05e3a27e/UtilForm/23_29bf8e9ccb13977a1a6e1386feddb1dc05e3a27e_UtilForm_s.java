 package org.vamdc.portal.session.queryBuilder.forms;
 
 import org.vamdc.dictionary.Restrictable;
 import org.vamdc.portal.session.queryBuilder.fields.SimpleField;
 
 public class UtilForm extends AbstractForm{
 
 	private static final long serialVersionUID = 604009831586236633L;
 
 	@Override
 	public String getTitle() { return "Misc."; }
 
 	@Override
 	public String getView() { return "/xhtml/query/forms/standardForm.xhtml"; }
 
 	@Override
 	public Integer getOrder() {	return Order.Util; }
 
 	public UtilForm(){
 		super();
 		
 		addField(new SimpleField(Restrictable.AsOfDate, "History date"));
		addField(new SimpleField(Restrictable.SourceDOI,"Source DOI"));
 		
 	}
 	
 }
