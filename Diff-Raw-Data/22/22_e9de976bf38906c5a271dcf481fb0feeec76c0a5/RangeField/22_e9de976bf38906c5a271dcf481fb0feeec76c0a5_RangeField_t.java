 package org.vamdc.portal.session.queryBuilder.fields;
 
 import org.vamdc.dictionary.Restrictable;
 
 public class RangeField extends AbstractField{
 
 	public RangeField(Restrictable keyword, String title) {
 		super(keyword, title);
 	}
 	
 	@Override
 	public String getView() { return "/xhtml/query/fields/rangeField.xhtml"; }
 
 	private String loValue;
 	private String hiValue;
 	
 	public String getHiValue(){	return hiValue; }
 	public String getLoValue(){	return loValue; }
 	
 	public void setLoValue(String loValue){
 		this.loValue = loValue;
 	}
 
 	public void setHiValue(String hiValue){
 		this.hiValue = hiValue;
 	}
 	
 	@Override
 	public String getQuery(){
 		fixCompareOrder();
 		
 		String result = getQueryPart(this.keyword.name(),"<",hiValue);
 		if (result.length()>0)
 			result+=" AND ";
 		result+=getQueryPart(this.keyword.name(),">",loValue);
 		return result;
 		
 	}
 	
 	private String getQueryPart(String keyword,String compare, String value){
 		if (fieldIsSet(value))
 			return keyword+" "+compare+" "+value;
 		return "";
 	}
 	
 	private void fixCompareOrder(){
 		if (fieldIsSet(hiValue) && fieldIsSet(loValue)){
 			Double lo = Double.parseDouble(loValue);
 			Double hi = Double.parseDouble(hiValue);
 			
 			if (lo>hi){
 				String tmp = loValue;
 				loValue=hiValue;
 				hiValue=tmp;
 			}
 		}
 	}
 	
	public boolean hasValue(){
		return (fieldIsSet(hiValue)|| fieldIsSet(loValue));
	}
	
 }
