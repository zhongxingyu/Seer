 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package edu.uga.radiant.printTree;
 
 import edu.uga.cs.wstool.parser.sawsdl.ComplexTypeOBJ;
 import edu.uga.cs.wstool.parser.sawsdl.MessageOBJ;
 import edu.uga.cs.wstool.parser.sawsdl.OperationOBJ;
 import edu.uga.cs.wstool.parser.sawsdl.SAWSDLParser;
 import edu.uga.cs.wstool.parser.sawsdl.SimpleTypeOBJ;
 
 import java.util.Map;
 import java.util.Iterator;
 
 import java.io.IOException;
 
 import javax.wsdl.PortType;
 
 import org.semanticweb.owlapi.model.IRI;
 
 /**
  *
  * @author Chaitu, Yung-Long Li
  */
 public class LoadWSDLTree {
 
 	@SuppressWarnings("rawtypes")
 	public static void loadWSDL(SAWSDLParser wsParser , StringBuffer buf, String fileName) throws Exception{
         
 		org.w3c.dom. Element defDoc = wsParser.getDefinition().getDocumentationElement();
         String doc = "";
         if(defDoc != null){
             doc = defDoc.getTextContent();
             if(doc == null)
                 doc = "";
         }
     	buf.append("<div class='ui-widget-content ui-corner-all ui-state-default' style='width:95%; margin:6px;padding:6px'>"
         		+ "<span style=\"width:40%;float:left;padding:2px\">" + fileName + "</span>"
         		+ "<span id='save' style=\"color:white;background:#616D7E;padding:3px;margin-right:3px\" "
         		+ "class=\"ui-button ui-corner-all\" onclick=\"save();\" >Save WSDL</span>"
         		+ "<span id=\"recommend\" class=\"ui-button ui-corner-all\" "
         		+ "style=\"color:white;background:#616D7E;padding:3px;margin-right:3px;\" onclick='recommendAll()'>Recommend All Terms</span>"
         		+ "<span id=\"switchWSView\" style=\"color:white;background:#616D7E;padding:3px;\" class=\"ui-button ui-corner-all\">WSDLXML view</span>"
         		+ "<span id=\"removeUnapprovedRecommendations\" style=\"color:white;background:#616D7E;padding:3px;\" class=\"ui-button ui-corner-all\" onclick=\"removeUnapproved()\">Remove Unapproved Recommendations</span></div>"
     			);
         
     	buf.append("<div id=\"wsxmlview\" style=\"widh:100%; overflow:auto; \"></div>");
         
     	buf.append("<div id=\"wstree\" class='ui-widget-content ui-corner-all' "
         		+ "style='overflow:auto;margin:6px; height:475px; padding:10px'>"
         		+ "<div class='ui-widget-content ui-corner-all' style=\"padding:4px\">" + doc + "</div>");
             
     	Map portTypes = wsParser.getDefinition().getPortTypes();
         Iterator it = portTypes.keySet().iterator();
         while(it.hasNext()){
         	PortType portType = (PortType) portTypes.get(it.next());
             String portTypeName = portType.getQName().getLocalPart();
             buf.append("<ul><li id=\"portType" + portTypeName + "\"><div class=\"drop\" value=\"portType:" + portTypeName + "\" id=\"" + portTypeName + "\">Identified Operations in the WSDL (portType : " + portTypeName + ")</div>"
         			+ "<div id=\"" + portTypeName + "annotation\" class=\"annotation\" value=\"portType:" + portTypeName + "\"></div>");
         	printWSDL(wsParser, buf);
         	buf.append("</li></ul>");
         }
     	buf.append("</div>");
     }
 
     public static void printWSDL(SAWSDLParser wsParser, StringBuffer buf) throws Exception{
         
         buf.append("<ul>");
         for(OperationOBJ oper : wsParser.getAllOperations().values()){
         	String opDoc = "";
         	if (oper.getDoc() != null) opDoc = oper.getDoc().replace('\'', '"');
             String oPname = oper.getName();
             String attrval = "operation:" + oPname;
             buf.append("<li id=\"li" + oPname + "op\">"
             		+ "<div id=\"operation-" + oPname + "-" + oper.getId() + "-droppable\" name=\"ws-droppable\" class=\"operation drop ui-widget-content ui-corner-all\" value=\"operation:" + oPname + "\" "
             		+ " title='" + opDoc + "'>"
             		+ "<span style='width:35%; float:left'>" + oPname + "</span>"
             		+ "<span class=\"ui-button ui-corner-all\""
             		+ "onclick=\"recommend(" + oper.getId() + ", '" + oPname + "','operation','" + opDoc + "')\" "
             		+ "style=\"background:#616D7E; color:white; text-align: center; width: 120px; margin: 2px; padding:1px\">Recommend Terms</span></div>");
                 
             IRI functionality = oper.getFunctionality();
             if(functionality != null){
             	String [] values = functionality.toString().split(" ");
             	int count = 0;
             	for(String value : values){
             		String owlid = LoadOWLTree.charReplace(value);
                 	int start = value.lastIndexOf("/");
                 	String functionalityName = value.substring(start + 1, value.length()); 
                 	buf.append("<div id=\"preExist-" + oper.getId() + "-modelReference" + count + "\" value=\"" + attrval + "\">"
             				+ "<div style=\"margin:10px\"><span id=\"" + oper.getId() + "\" class=\"preExisting ui-widget-content ui-corner-all\" "
                             + "value=\"" + value + "\" title=\"" + value + "\" onclick=\"openOWLNode('" + owlid + "')\">" + functionalityName + "</span>"
                             + "<span id=\"remove" + oper.getId() + "\" onclick=\"removeElement('wsdl', " + oper.getId() + ", 'operation', 'modelReference', '" + value.replace('\'', '"') + "', 'preExist-" + oper.getId() + "-modelReference" + count + "')\" class=\"ui-icon ui-icon-close hove\" style=\"float:left\"></span></div></div>");
                 	count++;
             	}
             }
             buf.append("<div name=\"operation-" + oper.getId() + "-annotation\" class=\"annotation\" value=\"" + attrval + "\"></div>");
                 
             
             // for input message
             String inMsgName = oper.getInput().getName();
             attrval = "message:" + inMsgName;
             buf.append("<ul><li id=\"li" + inMsgName + "msg\">"
             		+ "<div id=\"" + inMsgName + "msg\" class=\"message drop ui-widget-content ui-corner-all\" "
             		+ "value=\"" + attrval + "\" >"
             		+ "<span style='width:50%;'>inputs (" + inMsgName + ")</span>"
             		+ "</div>");
             printMessageAnnotation(buf, oper.getInput(), attrval);
             buf.append("<div name=\"message-" + oper.getInput().getId() + "-annotation\" class=\"annotation\"value=\"" + attrval +"\"></div>");
             buf.append("<ul>");
             printMessage(oper.getInput(), buf);
             buf.append("</ul>");
             buf.append("</li>");
                
             // for output message
             String outMsgName = oper.getOutput().getName();
             attrval = "message:" + outMsgName;
             buf.append("<li id=\"li" + outMsgName + "msg\">"
             		+ "<div id=\"" + outMsgName + "msg\" class=\"drop ui-widget-content ui-corner-all message\" "
             		+ "value=\"" + attrval + "\">"
             		+ "<span style='width:50%;'>Outputs (" + outMsgName + ")</span>"
             		+ "</div>");
             printMessageAnnotation(buf, oper.getOutput(), attrval);
             buf.append("<div name=\"message-" + oper.getOutput().getId() + "-annotation\" class=\"annotation\" value=\"" + attrval + "\"></div>");
             buf.append("<ul>");
             printMessage(oper.getOutput(), buf);
             buf.append("</ul>");
             buf.append("</li></ul>");
             buf.append("</li>");
             
         }
         buf.append("</ul>");
     }
     
     public static void printMessageAnnotation(StringBuffer buf, MessageOBJ message, String attr) throws IOException
     {
     	if(message.getModelReference() != null) {
             String[] values = message.getModelReference().toString().split(" ");
             int count = 0;
             for(String value : values){
             	String owlid = LoadOWLTree.charReplace(value);
             	int start = value.lastIndexOf("/");
             	String name = value.substring(start + 1, value.length()); 
             	buf.append("<div id=\"preExist- " + message.getId() + " -modelReference" + count + "\""
                         + "value=\"" + attr + "\">"
                         + "<div style='margin:10px'><span id=\"" + message.getId() + "\" class=\"preExisting hove ui-widget-content ui-corner-all\" "
                         + "value=\"" + value + "\" title=\"" + value + "\" onclick=\"openOWLNode('" + owlid + "')\" >" + name + "</span>"
                         + "<span id=\"remove" + message.getId() + "\" class=\"ui-icon ui-icon-close hove\" onclick=\"removeElement('wsdl', " + message.getId() + ", 'message', 'modelReference', '" + value.replace('\'', '"') + "', 'preExist- " + message.getId() + " -modelReference" + count + "')\" style=\"float:left\"></span></div></div>");
             	count++;
             }
         }
         if (message.getLiftingSchemaMapping() != null){
             String[] values = message.getLiftingSchemaMapping().toString().split(" ");
             for(String value : values){
             	buf.append("<div id=\"preExist-" + message.getId() + "-liftingSchemaMapping\""
             			+ "class='liftingSchemaMapping' value=\"" + attr + "\">"
             			+ "<div style='margin:10px'><span id=\"" + message.getId() + "\" class=\"preExisting schemaMapper ui-widget-content ui-corner-all\" value=\"" + value + "\">" + value + "</span>"
             			+ "<span id=\"remove" + message.getId() + "\" class=\"ui-icon ui-icon-close hove\" onclick=\"removeElement('wsdl', " + message.getId() + ", 'message', 'liftingSchemaMapping', '" + value + "', 'preExist-" + message.getId() + "-liftingSchemaMapping')\" style=\"float:left\"></span></div></div>");
             }
         }
         if(message.getLoweringSchemaMapping() != null){
             String []values = message.getLoweringSchemaMapping().toString().split(" ");
             for(String value : values){
             	buf.append("<div id=\"preExist-" + message.getId() + "-loweringShemaMapping\""
             			+ "class='loweringShemaMapping' value=\"" + attr + "\">"
             			+ "<div style='margin:10px'><span id=\"" + message.getId() + "\" class=\"preExisting schemaMapper hove ui-widget-content ui-corner-all \" value=\"" + value + "\">" + value + "</span>"
             			+ "<span id=\"remove" + message.getId() + "\" class=\"ui-icon ui-icon-close hove\" onclick=\"removeElement('wsdl', " + message.getId() + ", 'message', 'loweringSchemaMapping', '" + value + "', 'preExist-" + message.getId() + "-loweringShemaMapping')\" style=\"float:left\"></span></div></div>");
             }
         } 
     }
 
     public static void printMessage(MessageOBJ message, StringBuffer buf) throws Exception{
     	
     	// print simple type
     	buf.append("<ul>");
     	for (SimpleTypeOBJ simple : message.getSimpletype()){
     		String required = "";
     		if (simple.isRequired()) required = "(*)";
     		String eleDoc = "";
     		if (simple.getDescription() != null){
                 eleDoc = simple.getDescription().trim().replace('\'', '"');
             }
     		String attrval = "element:" + simple.getName() + " " + message.getName();
             buf.append("<li id=\"li"+ message.getName() + simple.getName() + "\">"
              		+ "<div id=\"simple-" + simple.getName() + "-" + simple.getId() + "-droppable\" name=\"ws-droppable\" class=\"element drop ui-widget-content ui-corner-all\" "
                     + "value=\"element:" + simple.getName() + " " + message.getName() + "\" title='" + eleDoc + "'>"
                     + "<span style='width:35%;float:left'>" + simple.getName() + required + "</span>"
                     + "<span onClick=\"recommend(" + simple.getId() + ", '" + simple.getName() + "', 'simple' ,'" + eleDoc + "')\" class='ui-button ui-state-default ui-corner-all' style='background:#616D7E;color: white; text-align:center;;width:120px;margin:2px'>Recommend Terms</span>"
                    //+ "<span onClick=\"addSchemaMapper(" + simple.getId() + ")\" class='ui-button ui-state-default ui-corner-all' style='text-align:center;background:#616D7E;color:white;width:120px;margin:2px'>Add SchemaMapper</span>"
                    + "</div>");
            printSimpleAnnotation(buf, simple, attrval);
             buf.append("<div name=\"simple-" + simple.getId() + "-annotation\" class=\"annotation\" value=\""+attrval+"\"></div></li>");
     	}
     	buf.append("</ul>");
     	
     	// print complex type
     	buf.append("<ul>");
     	for (ComplexTypeOBJ complex : message.getComplextype()){
     		printComplex(complex, buf);
     	}
     	buf.append("</ul>");
     	
     }
 
     public static void printComplex(ComplexTypeOBJ complex, StringBuffer buf) throws Exception{
     	
     	printCompelxContent(complex, buf);
     	// print simple type
     	buf.append("<ul>");
     	for (SimpleTypeOBJ simple : complex.getSimples()){
     		String required = "";
     		if (simple.isRequired()) required = "(*)";
     		String eleDoc = "";
     		if (simple.getDescription() != null){
                 eleDoc = simple.getDescription().trim().replace('\'', '"');
             }
     		String attrval = "element:" + simple.getName() + " " + complex.getName();
             buf.append("<li id=\"li"+ complex.getName() + simple.getName() + "\">"
              		+ "<div id=\"simple-" + simple.getName() + "-" + simple.getId() + "-droppable\" name=\"ws-droppable\" class=\"element drop ui-widget-content ui-corner-all\" "
                     + "value=\"element:" + simple.getName() + " " + complex.getName() + "\" title='" + eleDoc + "'>"
                     + "<span style='width:35%;float:left'>" + simple.getName() + required + "</span>"
                     + "<span onClick=\"recommend('" + simple.getId() + "', '" + simple.getName() + "' ,'simple' ,'" + eleDoc + "')\" class='ui-button ui-state-default ui-corner-all' style='background:#616D7E;color: white; text-align:center;;width:120px;margin:2px'>Recommend Terms</span>"
                    //+ "<span onClick=\"addSchemaMapper(" + simple.getId() + ")\" class='ui-button ui-state-default ui-corner-all' style='text-align:center;background:#616D7E;color:white;width:120px;margin:2px'>Add SchemaMapper</span>"
                    + "</div>");
            printSimpleAnnotation(buf, simple, attrval);
             buf.append("<div name=\"simple-" + simple.getId() + "-annotation\" class=\"annotation\" value=\""+attrval+"\"></div></li>");
     	}
     	buf.append("</ul>");
     	
     	// print nested complex
     	buf.append("<ul>");
     	for (ComplexTypeOBJ cplex : complex.getComplextypes()){
     		printComplex(cplex, buf);
     	}
     	buf.append("</ul>");
     	
     	// close complex type
     	buf.append("</li>");
     }
     
     public static void printCompelxContent(ComplexTypeOBJ cplex, StringBuffer buf) throws Exception{
     	
     	String eleDoc = "";
 		if (cplex.getDescription() != null){
             eleDoc = cplex.getDescription().trim().replace('\'', '"');
         }
 		String attrval = "complex:" + cplex.getName();
         buf.append("<li id=\"li" + cplex.getName() + "\">"
          		+ "<div id=\"complex-" + cplex.getName() + "-" + cplex.getId() + "-droppable\" name=\"ws-droppable\" class=\"complex drop ui-widget-content ui-corner-all\" "
                 + "value=\"complex:" + cplex.getName() + "\" title='" + eleDoc + "'>"
                 + "<span style='width:35%;float:left'>" + cplex.getName() + "</span>"
                 + "<span onClick=\"recommend(" + cplex.getId() + ", '" + cplex.getName() + "', 'complex', '" + eleDoc + "')\" class='ui-button ui-state-default ui-corner-all' style='background:#616D7E;color: white; text-align:center;;width:120px;margin:2px'>Recommend Terms</span>"
                 + "</div>");
         printComplexAnnotation(buf, cplex, attrval);
         buf.append("<div name=\"complex-" + cplex.getId() + "-annotation\" class=\"annotation\" value=\"" + attrval + "\"></div>");
 
     }
     
     private static void printComplexAnnotation(StringBuffer buf, ComplexTypeOBJ complex, String attrval) 
     {
     	if(complex.getModelReference() != null) {
             String[] values = complex.getModelReference().toString().split(" ");
             for(String value : values){
             	int count = 0;
             	String owlid = LoadOWLTree.charReplace(value);
             	int start = value.lastIndexOf("/");
             	String name = value.substring(start + 1, value.length()); 
             	buf.append("<div id=\"preExist-" + complex.getId() + "-modelReference" + count + "\""
                         + "value=\"" + attrval + "\">"
                         + "<div style='margin:10px'><span id=\"" + complex.getId() + "\" class=\"preExisting hove ui-widget-content ui-corner-all\" "
                         + "value=\"" + value + "\" title=\"" + value + "\" onclick=\"openOWLNode('" + owlid + "')\" >" + name + "</span>"
                         + "<span id=\"remove" + complex.getId() + "\" class=\"ui-icon ui-icon-close hove\" onclick=\"removeElement('wsdl', " + complex.getId() + ", 'complex', 'modelReference', '" + value.replace('\'', '"') + "', 'preExist-" + complex.getId() + "-modelReference" + count + "')\" style=\"float:left\"></span></div></div>");
             	count++;
             }
         }
 	}
     
    private static void printSimpleAnnotation(StringBuffer buf, SimpleTypeOBJ simple, String attrval) 
     {
     	if(simple.getModelReference() != null) {
             String[] values = simple.getModelReference().toString().split(" ");
             for(String value : values){
             	int count = 0;
             	String owlid = LoadOWLTree.charReplace(value);
             	int start = value.lastIndexOf("/");
             	String name = value.substring(start + 1, value.length()); 
             	buf.append("<div id=\"preExist-" + simple.getId() + "-modelReference" + count + "\""
                         + "value=\"" + attrval + "\">"
                         + "<div style='margin:10px'><span id=\"" + simple.getId() + "\" class=\"preExisting hove ui-widget-content ui-corner-all\" "
                         + "value=\"" + value + "\" title=\"" + value + "\" onclick=\"openOWLNode('" + owlid + "')\" >" + name + "</span>"
                         + "<span id=\"remove" + simple.getId() + "\" class=\"ui-icon ui-icon-close hove\" onclick=\"removeElement('wsdl', " + simple.getId() + ", 'simple', 'modelReference', '" + value.replace('\'', '"') + "', 'preExist-" + simple.getId() + "-modelReference" + count + "')\" style=\"float:left\"></span></div></div>");
             	count++;
             }
         }
         if (simple.getLiftingSchemaMapping() != null){
             String[] values = simple.getLiftingSchemaMapping().toString().split(" ");
             for(String value : values){
             	buf.append("<div id=\"preExist-" + simple.getId() + "-liftingSchemaMapping\""
             			+ "class='liftingSchemaMapping' value=\"" + attrval + "\">"
             			+ "<div style='margin:10px'><span id=\"" + simple.getId() + "\" class=\"preExisting schemaMapper ui-widget-content ui-corner-all\" value=\"" + value + "\">" + value + "</span>"
             			+ "<span id=\"remove" + simple.getId() + "\" class=\"ui-icon ui-icon-close hove\" onclick=\"removeElement('wsdl', " + simple.getId() + ", 'simple', 'liftingSchemaMapping', '" + value + "', 'preExist-" + simple.getId() + "-liftingSchemaMapping')\" style=\"float:left\"></span></div></div>");
             }
         }
         if(simple.getLoweringSchemaMapping() != null){
             String []values = simple.getLoweringSchemaMapping().toString().split(" ");
             for(String value : values){
             	buf.append("<div id=\"preExist-" + simple.getId() + "-loweringSchemaMapping\""
             			+ "class='loweringShemaMapping' value=\"" + attrval + "\">"
             			+ "<div style='margin:10px'><span id=\"" + simple.getId() + "\" class=\"preExisting schemaMapper hove ui-widget-content ui-corner-all \" value=\"" + value + "\">" + value + "</span>"
             			+ "<span id=\"remove" + simple.getId() + "\" class=\"ui-icon ui-icon-close hove\" onclick=\"removeElement('wsdl', " + simple.getId() + ", 'simple', 'loweringSchemaMapping', '" + value + "', 'preExist-" + simple.getId() + "-loweringSchemaMapping')\" style=\"float:left\"></span></div></div>");
             }
         }
 	}
 
 }
