 package niagaraGUI;
 
 import java.util.*;
 //import niagaraGUI.OperatorTemplate.DefaultValue;
 import org.xmlmiddleware.schemas.dtds.*;
 
 /**
  * 
  * @author Weston Wedding
  *
  */
 
 public class Operator {
     protected HashMap<String, String> reqAttribs; //String name, String value
     protected HashMap<String,String> optAttribs;  //String name, String value
     protected Set<String> inputs;
     protected String elements; //houses xml code for any internal elements like predicates for Select
     protected String name;//This is name of operator NOT id
     protected String comments;//Comments to be inserted with this operators XML code
     protected boolean isTop = false;
     
     public Operator(OperatorTemplate opTemplate) {
         
         HashMap<String, Attribute> templateAttributes = opTemplate.getAttributes();
         reqAttribs = new HashMap<String,String>();
         optAttribs = new HashMap<String,String>();
        inputs = new HashSet<String>();
         name = opTemplate.getName();
         //Sort out the required and optional attributes
         Iterator<Map.Entry<String, Attribute>> it = templateAttributes.entrySet().iterator();
 
         while (it.hasNext()) {
           Map.Entry<String, Attribute> entry = it.next();
 
           //Sort out the attributes into required and optionals
           if (entry.getValue().required == Attribute.REQUIRED_REQUIRED) {
               reqAttribs.put(entry.getKey(), null);
           } else if(entry.getValue().required == Attribute.REQUIRED_OPTIONAL) {
               optAttribs.put(entry.getKey(), null);
           }
         }
     }
     public String getName(){
     	return this.name;
     }
     
     public Boolean setAttribute(String name, String value) {
         return false;
     }
     
     // Attribute "getter" methods.  There are three distinct methods
     // even though I don't see a cause for using the last two, yet. - Wes
     
     public HashMap<String,String> getAttributes() {
         HashMap<String,String> allAttribs = new HashMap<String,String>();
         allAttribs.putAll(reqAttribs);
         allAttribs.putAll(optAttribs);
         return allAttribs;
     }
     
     public HashMap<String,String> getRequiredAttributes() {
         return reqAttribs;
     }
     public HashMap<String,String> getOptionalAttributes() {
         return optAttribs;
     }
     public Set<String> getInputArray(){
     	return inputs;
     }
     public void addInput(Operator input){
    	//System.out.println(input.getClass());
    	if (input != null && input instanceof Operator){
    		String name = input.getName();
    		if (name != null){
	    		//System.out.println(name==null);
	    		//System.out.println(name);
	    		inputs.add(name);
    		}
    	}
     }
 }
