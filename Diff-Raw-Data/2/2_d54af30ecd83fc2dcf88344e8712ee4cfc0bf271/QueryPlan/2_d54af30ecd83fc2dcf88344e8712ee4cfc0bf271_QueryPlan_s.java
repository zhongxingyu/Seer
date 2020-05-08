 package niagaraGUI;
 
 import java.util.*;
 import org.jdom.*;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.StringWriter;
 
 import javax.xml.transform.stream.StreamResult;
 
 import org.jdom.*;
 import org.jdom.input.*;
 import org.jdom.output.*;
 
 public class QueryPlan {
     private String internalDTDfilename;//local DTD to populate the opTemplates
     private String externalDTDfilename;//external DTD path which will be written in xml file
     static private Hashtable<String, OperatorTemplate> opTemplates;//Table of operator templates indexed by operator name
     private List<Operator> opList;//List of operator Instances in the current query plan
     private Operator top;//reference to the top operator, top is also in opList
     private String queryName;//name of this query
     private DTDInterpreter dtdInterp;//a dtd interpreter reference
             
     public QueryPlan(String name, String internalDTDfilename) {
         opTemplates = new Hashtable<String, OperatorTemplate>();
         this.internalDTDfilename = internalDTDfilename;
         this.externalDTDfilename = "/stash/datalab/datastreams-student/bin/queryplan.dtd";
         dtdInterp = new DTDInterpreter(this.internalDTDfilename);
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
             String name;
             String elements;
             String comments;
             //String DTDString = "/stash/datalab/datastreams-student/bin/queryplan.dtd";
             String DTDString = "D:\\wes my docs\\NiagraGUI\\src\\niagaraGUI\\queryplan.dtd";
             DocType type = new DocType("plan", DTDString);
             Iterator iterator;
             iterator = opList.iterator();
             Operator op;
             
             HashMap<String, String> att;
             
             //Set the root element first
             Element plan = new Element("plan");
             //plan.setAttribute(new Attribute("top", "cons"));
             if (top != null) {
                 plan.setAttribute("top", top.getName());
             } else {
                 plan.setAttribute("top", "");
             }
             Document doc1 = new Document(plan);
             doc1.setDocType(type);
             doc1.setRootElement(plan);
             
             //Now iterate through our operators
             while (iterator.hasNext()){
                 op = (Operator)iterator.next();
                 name = op.getName();
                 comments = op.getComments();
                 elements = op.getElements();
                 Element ele = new Element(name);
                 att = op.getAttributes();
                 Set set = att.entrySet();
                 Iterator i = set.iterator(); 
                 String str1;
                 String str2;
                 //Comments go just before the operator!
                 if(comments != null){
                     Comment com = new Comment(comments);
                     //ele.setContent(com);
                     doc1.getRootElement().addContent(com);
                 }
                 while(i.hasNext()) { 
                     Map.Entry me = (Map.Entry)i.next(); 
                     str1 = (String)me.getKey();
                     str2 = (String)me.getValue();
                     if(str2 != null)
                     ele.setAttribute(new Attribute(str1,str2));
                 }
                 
                 /* Welcome to kludge town!  Since sub-elements are just strings,
                  * our friendly jdom is going to escape characters like < and >.
                  * The library doesn't let us override this!
                  * 
                  * So the workaround for now is to load the string into jdom
                  * and make a psudo-document out of it, and then force those
                  * elements into our real document.
                 */ 
                 if(elements != null) {
                     //ele.addContent("\n" + elements + "\n");
                     List<Content> contentList = strToJdomContent(elements);
                     // Had to use an old for loop and not iterators because modifying lists
                     // while iterating causes exceptions.  Since we're already so kludge-tastic
                     // at this point I don't mind doing this.
                     for (int j = 0; j < contentList.size();j++) {
                         Content c = contentList.get(j);
                         c.detach();
                         ele.addContent(c);
                     }
                 }
 
                 //return jdomDocument.getRootElement();
                 doc1.getRootElement().addContent(ele);
             }
         
             XMLOutputter xmlOutput = new XMLOutputter();
             xmlOutput.setFormat(Format.getPrettyFormat());
             xmlOutput.output(doc1, new FileWriter(filename));
             validateOutput(filename);
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
     public String getInternalDTDFileName(){
     	return this.internalDTDfilename;
     }
     public void setInternalDTDFileName(String newInternalDTDFileName){
     	opTemplates = new Hashtable<String, OperatorTemplate>();
         this.internalDTDfilename = newInternalDTDFileName;
         dtdInterp = new DTDInterpreter(this.internalDTDfilename);
         opTemplates = dtdInterp.getTemplates();
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
     
     // Code credit: Elliotte Rusty Harold
     // Code source: http://www.cafeconleche.org/books/xmljava/chapters/ch14s07.html
     // Seemed like some validation would be nice.
     private boolean validateOutput(String filename) {        
         SAXBuilder builder = new SAXBuilder();
         
         // command line should offer URIs or file names
         try {
           builder.build(filename);
           // If there are no well-formedness errors, 
           // then no exception is thrown
           System.out.println(filename + " is well-formed.");
         }
         // indicates a well-formedness error
         catch (JDOMException e) { 
           System.out.println(filename + " is not well-formed.");
           System.out.println(e.getMessage());
         }  
         catch (IOException e) { 
           System.out.println("Could not check " + filename);
           System.out.println(" because " + e.getMessage());
         }        
         return true;
     }
     
     private List<Content> strToJdomContent(String strContent) {
         StringWriter sw = new StringWriter();
         StreamResult result = new StreamResult(sw);
         SAXBuilder saxBuilder = new SAXBuilder();
         Reader stringReader = new StringReader(strContent);
         try {
             Document jdomDocument = saxBuilder.build(stringReader);
             System.out.println(jdomDocument.getContent());
             return jdomDocument.getContent();
         }
             catch (Exception e) { 
                System.out.println("Could not check " + filename);
                 System.out.println(" because " + e.getMessage());
        }
        return new ArrayList<Content>();
     }
 }
