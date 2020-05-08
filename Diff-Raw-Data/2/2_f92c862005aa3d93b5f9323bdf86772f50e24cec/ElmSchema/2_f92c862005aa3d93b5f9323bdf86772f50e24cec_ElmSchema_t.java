 
 package eionet.meta.exports.schema;
 
 import java.io.*;
 import java.util.*;
 import java.sql.*;
 
 import eionet.meta.*;
 import eionet.util.Util;
 
 public class ElmSchema extends Schema {
 	
     public ElmSchema(DDSearchEngine searchEngine, PrintWriter writer){
         super(searchEngine, writer);
     }
     
     /**
     * Write a schema for an object given by ID.
     */
     public void write(String elemID) throws Exception{
         
         if (Util.voidStr(elemID))
             throw new Exception("Data element ID not specified!");
         
         // Get the data element object. This will also give us the
         // element's simple attributes + tableID
         DataElement elem = searchEngine.getDataElement(elemID);
         if (elem == null)
             throw new Exception("Data element not found!");
         
         // get and set the element's complex attributes
         elem.setComplexAttributes(searchEngine.getComplexAttributes(elemID, "E",null,elem.getTableID(),elem.getDatasetID()));
         
         if (elem.getType().equalsIgnoreCase("CH1")){
         		
 		    Vector fixedValues =
 		        searchEngine.getFixedValues(elem.getID(), "elem");
 		    elem.setFixedValues(fixedValues);
         }
         
         write(elem);
     }
     
     /**
     * Write a schema for a given object.
     */
     private void write(DataElement elem) throws Exception{
 
 		// set target namespace (being the parent table's namespace)
 		//String parentNsID = elem.getNamespace().getID(); 
 		//if (parentNsID!=null) setTargetNsUrl(parentNsID);
 		
 		String cNamespaceID = getContainerNamespaceID();
 		if (Util.voidStr(cNamespaceID)){
 			Namespace parentNs = elem.getNamespace();
 			if (parentNs==null || Util.voidStr(parentNs.getID()))
 				this.targetNsUrl = this.appContext + "elements/" + elem.getIdentifier();
 			else
 				setTargetNsUrl(parentNs.getID());
 		}
 		else
 			setTargetNsUrl(cNamespaceID);
 
         //writeElemStart(elem.getShortName());
 		writeElemStart(elem.getIdentifier());
         writeAnnotation(elem.getAttributes(), elem.getComplexAttributes());
         writeContent(elem);
         writeElemEnd();
     }
     
     private void writeContent(DataElement elem) throws Exception {
 		writeSimpleContent(elem);
     }
     
     private void writeSimpleContent(DataElement elem) throws Exception {
         
         String dataType = (String)nonAnnotationAttributes.get("Datatype");
         String minSize  = (String)nonAnnotationAttributes.get("MinSize");
         String maxSize  = (String)nonAnnotationAttributes.get("MaxSize");
 		String minValue = (String)nonAnnotationAttributes.get("MinValue");
 		String maxValue = (String)nonAnnotationAttributes.get("MaxValue");
 		String decPrec  = (String)nonAnnotationAttributes.get("DecimalPrecision");
 		
 		if (Util.skipAttributeByDatatype("MinSize", dataType)) minSize = null;
 		if (Util.skipAttributeByDatatype("MaxSize", dataType)) maxSize = null;
 		if (Util.skipAttributeByDatatype("minValue", dataType)) minValue = null;
 		if (Util.skipAttributeByDatatype("maxValue", dataType)) maxValue = null;
 		if (Util.skipAttributeByDatatype("DecimalPrecision", dataType)) decPrec = null;
         
         addString("\t");
         addString("<xs:simpleType>");
         newLine();
         
         if (dataType != null){
             
            if (dataType.equalsIgnoreCase("float") || dataType.equalsIgnoreCase("double"))
                dataType = "decimal";
             addString("\t\t");
             addString("<xs:restriction base=\"xs:");
             addString(dataType);
             addString("\">");
             newLine();
             
             if (!Util.voidStr(minSize)){
                 addString("\t\t\t");
                 addString("<xs:minLength value=\"");
                 addString(minSize);
                 addString("\"/>");
                 newLine();
             }
             
             if (!Util.voidStr(maxSize)){
                 addString("\t\t\t");
                 if (dataType.equalsIgnoreCase("string"))
                 	addString("<xs:maxLength value=\"");
                 else
 					addString("<xs:totalDigits value=\"");
                 addString(maxSize);
                 addString("\"/>");
                 newLine();
             }
 
 			if (!Util.voidStr(minValue)){
 				addString("\t\t\t");
 				addString("<xs:minInclusive value=\"");
 				addString(minValue);
 				addString("\"/>");
 				newLine();
 			}
 
 			if (!Util.voidStr(maxValue)){
 				addString("\t\t\t");
 				addString("<xs:maxInclusive value=\"");
 				addString(maxValue);
 				addString("\"/>");
 				newLine();
 			}
 
 			if (!Util.voidStr(decPrec)){
 				addString("\t\t\t");
 				addString("<xs:fractionDigits value=\"");
 				addString(decPrec);
 				addString("\"/>");
 				newLine();
 			}
             
             Vector fixedValues = elem.getFixedValues();
             for (int k=0; fixedValues!= null && k<fixedValues.size(); k++){
                 
                 FixedValue fxv = (FixedValue)fixedValues.get(k);
             
                 addString("\t\t\t");
                 addString("<xs:enumeration value=\"");
                 addString(escape(fxv.getValue()));
                 addString("\"/>");
                 newLine();
             }
             
             addString("\t\t");
             addString("</xs:restriction>");
             newLine();
         }
         
         addString("\t");
         addString("</xs:simpleType>");
         newLine();
     }
 
     public static void main(String[] args){
         
         Connection conn = null;
         
         try{
             Class.forName("org.gjt.mm.mysql.Driver");
             //conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/DataDict", "dduser", "xxx");
             conn = DriverManager.getConnection("jdbc:mysql://195.250.186.16:3306/DataDict", "dduser", "xxx");
             DDSearchEngine searchEngine = new DDSearchEngine(conn);
             
             FileOutputStream os = new FileOutputStream("x:\\temp\\test.xsd");
             PrintWriter writer = new PrintWriter(os);
             ElmSchema elmSchema = new ElmSchema(searchEngine, writer);
             elmSchema.setIdentitation("\t");
             elmSchema.setAppContext("http://localhost:8080/datadict/public");
             //elmSchema.write("104");
             //elmSchema.write("4593");
             elmSchema.write("111");
             elmSchema.flush();
             
             writer.flush();
             writer.close();
             os.flush();
             os.close();
         }
         catch (Exception e){
             e.printStackTrace();
             //System.out.println(e.toString());
         }
         finally{
             if (conn != null){
                 try{ conn.close(); }
                 catch (Exception e) {}
             }
         }
     }
 }
