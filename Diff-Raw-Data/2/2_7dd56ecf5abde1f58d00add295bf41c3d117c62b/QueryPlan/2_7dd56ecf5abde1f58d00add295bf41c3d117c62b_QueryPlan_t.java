 package niagaraGUI;
 
 import java.util.*;
 import org.jdom.*;
 import java.io.FileWriter;
 import java.io.IOException;
 import org.jdom.Attribute;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 public class QueryPlan {
     private String filename;
     static private Hashtable<String, OperatorTemplate> opTemplates;//Table of operator templates indexed by operator name
     private List<Operator> opList;//List of operator Instances in the current query plan
     private Operator top;//reference to the top operator
     private String queryName;//name of the query
     private DTDInterpreter dtdInterp;
             
     public QueryPlan(String name, String filename) {
         opTemplates = new Hashtable<String, OperatorTemplate>();
         dtdInterp = new DTDInterpreter(filename);
         opTemplates = dtdInterp.getTemplates();
         opList = new ArrayList<Operator>();
     }
     
     static public Hashtable<String, OperatorTemplate> getOpTemplates() {
         return opTemplates;
     }
     
     static public OperatorTemplate addTemplate(OperatorTemplate opTemplate) {
         return opTemplates.put(opTemplate.getName(), opTemplate);      
     }
     
     public void generateXML(String filename) {
         try{
             Element plan = new Element("plan");
             plan.setAttribute(new Attribute("top", "cons"));
             Document doc1 = new Document(plan);
             DocType type = new DocType("plan", "/stash/datalab/datastreams-student/bin/queryplan.dtd");
             doc1.setDocType(type);
             doc1.setRootElement(plan);
             Iterator iterator;
             iterator = opList.iterator();
             Operator op;
             String name;
             HashMap<String, String> att;
             
             while (iterator.hasNext()){
                 op = (Operator)iterator.next();
                 name = op.getName();
                 Element ele = new Element(name);
                 att = op.getAttributes();
                 Set set = att.entrySet();
                 Iterator i = set.iterator(); 
                 String str1;
                 String str2;
                 
                 while(i.hasNext()) { 
                     Map.Entry me = (Map.Entry)i.next(); 
                     str1 = (String)me.getKey();
                     str2 = (String)me.getValue();
                     if(str2 != null)
                     ele.setAttribute(new Attribute(str1,str2));
                 }
                 doc1.getRootElement().addContent(ele);
             }
         
             XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getRawFormat());
             xmlOutput.output(doc1, new FileWriter(filename));
             System.out.println("File Saved!");
         
         }catch (IOException io) {
         System.out.println(io.getMessage());
       }
     }
     public String[] getOperatorNames(){
     	if (opTemplates != null){
     		Set<String> opNameSet = opTemplates.keySet();
     		String[] opNameAry = new String[opNameSet.size()];
     		opNameAry = opNameSet.toArray(opNameAry);
     		
     		return opNameAry;
     	}
     	else return null;
     }
     
     public void setName(String name) {
         queryName = name;
     }
     
     public String getName() {
     	//returns the name of this query plan
         return queryName;
     }
     public boolean addOperatorInstance(Operator newOp){
     	//Adds a new instansiated operator to this queryplan
     	if (opList.contains(newOp)){
     		return false;
     	}
     	else{
     		opList.add(newOp);
     		return true;
     	}
     	
     }
     public boolean removeOperatorInstance(Operator toRemove){
     	//removes an instansiated from Operator from this query plan
     	if (opList.contains(toRemove)){
     		opList.remove(toRemove);
     		return true;
     	}
     	else{
     		return false;
     	}
     }
     
     // This design pattern is in place to ease future import if
     // additional types are added
     public Boolean parse(String filename) {
         return parseDTD(filename);   
     }
     public Boolean parse(String filename, String docType) {
         if(docType == null) {
         docType = "DTD";            
         }
         if(docType == "DTD") {
             return parseDTD(filename);
         } else return false;
     }
 
     private Boolean parseDTD(String filename) {
         return false;        
     }
     
     public String toString() {
         return null;        
     }
     
     public void setTop(Operator newTop){
     	if (top != null)
     		top.setTop(false);
     	top = newTop;
     	top.setTop(true);
     	
     }
     public Operator getTop(){
     	return top;
     }
 }
