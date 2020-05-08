 package org.vamdc.portal.session.queryBuilder.fields;
 
 import java.io.Serializable;
 import java.util.UUID;
 
 import org.vamdc.dictionary.Restrictable;
 import org.vamdc.tapservice.vss2.LogicNode;
 import org.vamdc.tapservice.vss2.LogicNode.Operator;
 import org.vamdc.tapservice.vss2.RestrictExpression;
 
 /**
  * Base class for the form field
  * @author doronin
  */
 public abstract class AbstractField implements Serializable{
 
 	private static final long serialVersionUID = 5518997643252666012L;
 	private String value;
 	protected Restrictable keyword;
 	private String title;
 	private String id;
 	protected String prefix;
 	protected boolean ignoreField=false;
 	
 	public AbstractField(Restrictable keyword, String title){
 		this.keyword = keyword;
 		this.title = title;
 		this.id = UUID.randomUUID().toString();
 		this.prefix="";
 	}
 
 	public String getValue(){
 		return value;
 	}
 	public void setValue(String value){
 		this.value = value;
 	}
 
 	public void setIgnoreField(boolean flag){
 		this.ignoreField=flag;
 	}
 	
 	public Restrictable getKeyword() {
 		return keyword;
 	}
 
 	public void setKeyword(Restrictable keyword) {
 		this.keyword = keyword;
 	}
 	
 	
 	public String getQuery(){
		if (!hasValue())
 			return "";
		
 		String value = this.value.trim();
 
 		StringBuilder result = new StringBuilder();
 		result.append(addPrefix());
 		result.append(keyword.name());
 
 		String tryLike = tryLike(value);
 		if (tryLike.length()>0){
 			result.append(tryLike);
 			return result.toString();
 		}
 
 		String tryIn = tryIn(value);
 		if (tryIn.length()>0){
 			result.append(tryIn);
 			return result.toString();
 		}
 		result.append(" = ");
 		result.append("'");
 		result.append(value.trim());
 		result.append("'");
 
 		return result.toString();
 	}
 
 	String addPrefix(){
 		if (prefix!=null && prefix.length()>0)
 			return prefix+".";
 		return "";
 	}
 	
 	private static String tryLike(String value) {
 
 		if (value.contains("%")|| value.contains("*")){
 			value = value.replace("*", "%");
 			return " LIKE '"+value+"'"; 
 		}
 
 		return "";
 	}
 
 	private static String tryIn(String value){
 		String result ="";
 		result+=" IN (";
 		String in = "";
 		int numRes=0;
 		for (String part:value.split("[,.:_]")){
 			String val = part.trim();
 			if (val.length()>0){
 				numRes++;
 				if (in.length()>0)
 					in+=",";
 				in+="'"+val+"'";
 			}
 		}
 		result+=in+")";
 		if (numRes>1)
 			return result;
 		return "";
 	}
 
 	public boolean hasValue(){
		return !ignoreField && this.keyword !=null && this.value!=null && this.value.trim().length()>0;
 	}
 
 	/**
 	 * Get a face suitable for the form field display
 	 */
 	public abstract String getView();
 
 
 	public String getTitle(){ return this.title; }
 	public String getUnits(){ 
 		if (this.keyword!=null)
 			return this.keyword.getUnits(); 
 		return "";
 	}
 	public String getDescription(){ 
 		if (this.keyword!=null)
 			return this.keyword.getDescription(); 
 		return "";
 	}
 	public String getId(){ return this.id; }
 
 	public void clear(){ this.value=""; }
 
 	protected boolean fieldIsSet(String value){
 		return (value!=null && value.length()>0);
 	}
 
 	public void setPrefix(String prefix) {
 		this.prefix = prefix;
 	}
 	
 	public String getPrefix(){
 		return prefix;
 	}
 
 	public void loadFromQuery(LogicNode part){
 		if (part==null || part.getOperator()== null)
 			return;
 		Operator clause = part.getOperator();
 		switch(clause){
 		case EQUAL_TO:
 		case IN:
 		case LIKE:
 			RestrictExpression key = (RestrictExpression) part;
 			this.setValue((String)key.getValue());
 			break;	
 		}
 	}
 }
