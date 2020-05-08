 package adndframework.windows.tools;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import org.w3c.dom.Document;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * Used to represent a table of information from the game's source books.
  * This class is NOT used to display these tables.
  * 
  * @author Darin Beaudreau
  */
 public class InformationTable {
     private String [] columnNames = null;
     private Object [][] rows = null;
     
     public InformationTable(String filename) {
         this.createTable(filename);
     }
     
     private void createTable(String filename) {
         try {
            File f = new File(getClass().getResource(filename).getFile());
             
            if(f != null) {
                 // Create the document builder used for parsing the XML file.
                 DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                 DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(f);
                 doc.getDocumentElement().normalize();
                 
                 {
                     // Create list of column name tags.
                     List<String> columnNamesTemp = new ArrayList<String>();
                     NodeList columnList = doc.getElementsByTagName("columns"); // Get all <columns> nodes.
                     for(int column = 0; column < columnList.getLength(); column++) {
                         Node columnNode = columnList.item(column); // The current columns node.
                         if(columnNode.getNodeType() == Node.ELEMENT_NODE) {
                             // Get all the <name> tags within the column node.
                             NodeList nameList = columnNode.getChildNodes();
                             for(int name = 0; name < nameList.getLength(); name++) {
                                 // Insert each name into the list.
                                 Node nameNode = nameList.item(name);
                                 if(nameNode.getNodeType() == Node.ELEMENT_NODE) {
                                     columnNamesTemp.add(nameNode.getTextContent());
                                 }
                             }
                         }
                     }
                     // Convert the list into an array.
                     this.columnNames = columnNamesTemp.toArray(new String[columnNamesTemp.size()]);
                 }
                 {
                     // Create list of rows.
                     List<List<Object>> rowsTemp = new ArrayList<List<Object>>();
                     NodeList rowList = doc.getElementsByTagName("row"); // Get all <row> nodes.
                     for(int row = 0; row < rowList.getLength(); row++) {
                         Node rowNode = rowList.item(row); // The current row node.
                         if(rowNode.getNodeType() == Node.ELEMENT_NODE) {
                             // Get all the <column> tags within the row node.
                             List<Object> rowContent = new ArrayList<Object>();
                             NodeList rowColumnList = rowNode.getChildNodes();
                             for(int content = 0; content < rowColumnList.getLength(); content++) {
                                 Node columnNode = rowColumnList.item(content);
                                 if(columnNode.getNodeType() == Node.ELEMENT_NODE) {
                                     // Add the text of the <column> node to the current content list.
                                     rowContent.add(columnNode.getTextContent());
                                 }
                             }
                             // Add the current content list as a row.
                             rowsTemp.add(rowContent);
                         }
                     }
                     // Initialize the size of the array and then copy the list into the array.
                     this.rows = new Object[rowsTemp.size()][rowsTemp.get(0).size()];
                     for(int r = 0; r < rowsTemp.size(); r++) {
                         for(int c = 0; c < rowsTemp.get(0).size(); c++) {
                             this.rows[r][c] = rowsTemp.get(r).get(c);
                         }
                     }
                 }
             }
         } catch(javax.xml.parsers.ParserConfigurationException pce) {
             // Handle parser configuration exception.
         } catch(org.xml.sax.SAXException sax) {
             // Handle SAX exception.
         } catch(java.io.IOException io) {
             System.out.println("Error reading file: " + filename);
         }
     }
     
     public javax.swing.JTable getTable() {
         return new javax.swing.JTable(rows, columnNames);
     }
 }
