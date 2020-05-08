 package org.vamdc.portal.session.queryBuilder.fields;
 
 import javax.faces.event.ValueChangeEvent;
 
 import org.vamdc.dictionary.Restrictable;
 import org.vamdc.portal.session.queryBuilder.unitConv.UnitConverter;
 
 public class UnitConvRangeField extends RangeField{
 	
 	private static final long serialVersionUID = -7749430017746806827L;
 
 	public UnitConvRangeField(Restrictable keyword, String title, UnitConverter converter) {
 		super(keyword, title);
 		this.converter = converter;
 	}
 
 	@Override
 	public String getView() { return "/xhtml/query/fields/unitConvRangeField.xhtml"; }
 
 	private UnitConverter converter;
 	protected String userHiValue="";
 	protected String userLoValue="";
 	
 	public UnitConverter getConverter(){
 		return converter;
 	}
 	
 	String doConv(String value) {
 		String result ="";
 		if (value==null || value.length()==0)
 			return result;
 		try {
 			Double userValue = Double.parseDouble(value);
 			result=""+converter.convert(userValue);
 		}catch (NumberFormatException e){}
 		return result;
 	}
 
 	public String getUserHiValue() { return userHiValue; }
 	public void setUserHiValue(String userHiValue) { 
 		this.userHiValue = userHiValue;
 		this.setHiValue(doConv(userHiValue));
 	}
 	public String getUserLoValue() { return userLoValue; }
 	public void setUserLoValue(String userLoValue) { 
 		this.userLoValue = userLoValue;
 		this.setLoValue(doConv(userLoValue));
 	}
 	
 	public void update(ValueChangeEvent event){
 		converter.setSelectedOption((String)event.getNewValue());
 		this.setLoValue(doConv(userLoValue));
 		this.setHiValue(doConv(userHiValue));
 		this.fixCompareOrder();
 	}
 	
 	@Override
 	public void setLoValue(String loValue){
 		super.setLoValue(loValue);
 		if (this.userLoValue.equals(""))
 			this.userLoValue = loValue;
 	}
 	
 	@Override
 	public void setHiValue(String hiValue){
 		super.setHiValue(hiValue);
 		if (this.userHiValue.equals(""))
 			this.userHiValue = hiValue;
 	}
 	
	@Override
	public void clear(){
		super.clear();
		this.userHiValue = "";
		this.userLoValue = "";
	}
	
 }
