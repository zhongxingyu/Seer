 package org.vamdc.portal.session.queryBuilder.fields;
 
 import org.vamdc.dictionary.Restrictable;
 
 public class RangeField extends AbstractField{
 
 	public RangeField(Restrictable keyword, String title) {
 		super(keyword, title);
 	}
 
 	public RangeField(String prefix, Restrictable keyword, String title) {
 		super(keyword,title);
 		if (fieldIsSet(prefix))
 			this.prefix=prefix+".";
 	}
 
 	@Override
 	public String getView() { return "/xhtml/query/fields/rangeField.xhtml"; }
 
 	private String prefix="";
 	protected String loValue;
 	protected String hiValue;
 
 	public String getHiValue(){	return hiValue; }
 	public String getLoValue(){	return loValue; }
 
 	public void setLoValue(String loValue){
 		this.loValue = loValue;
 	}
 
 	public void setHiValue(String hiValue){
 		this.hiValue = hiValue;
 	}
 
 	public void setPrefix(String prefix){ this.prefix = prefix; }
 
 	@Override
 	public String getQuery(){
 		fixCompareOrder();
 		String result = "";
		if (hiValue!=null && hiValue.equals(loValue)){
 			result=getQueryPart(this.keyword.name(),"=",loValue);
 		}else{
 			result=getQueryPart(this.keyword.name(),">=",loValue);
 			if (result.length()>0)
 				result+=" AND ";
 			result+=getQueryPart(this.keyword.name(),"<=",hiValue);
 		}
 		return result;
 
 	}
 
 	private String getQueryPart(String keyword,String compare, String value){
 		if (fieldIsSet(value))
 			return prefix+keyword+" "+compare+" "+value;
 		return "";
 	}
 
 	protected void fixCompareOrder(){
 		if (fieldIsSet(hiValue) && fieldIsSet(loValue)){
 			Double lo=Double.NaN;
 			Double hi=Double.NaN;
 			try{
 				lo = Double.parseDouble(loValue);
 				hi = Double.parseDouble(hiValue);
 			}catch(NumberFormatException e){
 
 			}
 
 			if (lo>hi){
 				String tmp = loValue;
 				loValue=hiValue;
 				hiValue=tmp;
 			}
 		}
 	}
 
 	@Override
 	public boolean hasValue(){
 		return (fieldIsSet(hiValue)|| fieldIsSet(loValue));
 	}
 
 	@Override
 	public void clear(){
 		hiValue="";
 		loValue="";
 	}
 
 }
