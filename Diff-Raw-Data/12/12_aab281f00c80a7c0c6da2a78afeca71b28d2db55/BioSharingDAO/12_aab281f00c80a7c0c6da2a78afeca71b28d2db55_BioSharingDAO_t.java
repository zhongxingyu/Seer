 package org.biosharing.dao;
 
 import org.biosharing.model.*;
 import org.isatools.isacreator.publicationlocator.CiteExploreResult;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created by the ISA team
  *
  * @author Eamonn Maguire (eamonnmag@gmail.com)
  *         <p/>
  *         Date: 22/05/2012
  *         Time: 11:47
  */
 public class BioSharingDAO extends DAO {
 
     private static final String STANDARD_TABLE = "content_type_standard_cck";
     private static final String NODE_TABLE = "node";
     private static final String ALIAS_TABLE = "url_alias";
 
     public List<Node> getNodeInformation() {
         ResultSet results = executeQuery("select * from " + NODE_TABLE);
         List<Node> nodes = new ArrayList<Node>();
 
         try {
             while (results.next()) {
                 Node node = new Node();
                 // this can be made more compact using the enumeration to populate the object.
 
                 for (Fields field : NodeFields.values()) {
                     String value = results.getString(field.toString());
                     node.addFieldAndValue(field, value == null ? "" : value);
                 }
                 if (node.getNodeId() != null) {
                     nodes.add(node);
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             closeConnection();
         }
 
         return nodes;
     }
 
     public List<Alias> getAliasInformation() {
         ResultSet results = executeQuery("select * from " + ALIAS_TABLE);
         List<Alias> aliases = new ArrayList<Alias>();
 
         try {
             while (results.next()) {
                 Alias node = new Alias();
                 // this can be made more compact using the enumeration to populate the object.
 
                 for (Fields field : AliasFields.values()) {
                     node.addFieldAndValue(field, results.getString(field.toString()));
                 }
                 if (node.getPID() != null) {
                     aliases.add(node);
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             closeConnection();
         }
 
         return aliases;
     }
 
     public Map<String, Standard> getStandardNodeInformation() {
         ResultSet results = executeQuery("select * from " + STANDARD_TABLE);
         Map<String, Standard> standards = new HashMap<String, Standard>();
 
         try {
             while (results.next()) {
                 Standard standard = new Standard();
                 // this can be made more compact using the enumeration to populate the object.
 
                 for (Fields field : StandardFields.values()) {
                     standard.addFieldAndValue(field, results.getString(field.toString()));
                 }
                 if (standard.getStandardTitle() != null) {
                     standards.put(standard.getStandardTitle(), standard);
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         } finally {
             closeConnection();
         }
 
         return standards;
     }
 
 
     public void updateStandardWithPublication(Standard standard, CiteExploreResult publication) {
         try {
             String pubmedURL = "http://www.ncbi.nlm.nih.gov/pubmed?term=";
             int rowsAffected = executeUpdate("UPDATE " + STANDARD_TABLE + " SET " + StandardFields.PUBLICATION_TITLE + "='" + publication.getTitle() + "', "
                     + (publication.getId().equals("") ? "" : StandardFields.PUBLICATION_URL + "='" + pubmedURL + publication.getId()) + "', " +
                     StandardFields.PUBLICATION_ATTRIBUTES + "='" + publication.getId()
                     + "' WHERE `" + StandardFields.STANDARD_TITLE + "`='" + standard.getStandardTitle() + "';");
 
             if (rowsAffected == 0) {
                 System.err.println("** NO UPDATE PERFORMED! **");
             } else {
                 System.out.println(rowsAffected + " rows updated!");
             }
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             closeConnection();
         }
     }
 
     public boolean insertInformation(DefaultTable table) {
 
         try {
             StringBuilder columnNames = new StringBuilder();
             StringBuilder values = new StringBuilder();
             int count = 0;
 
             for (Fields standardField : table.getFieldToValue().keySet()) {
                 columnNames.append("`").append(standardField.toString()).append("`");
                 Object value = table.getFieldToValue().get(standardField);
                 if (value instanceof String) {
                     // replace any quotes with double quotes to escape them
                     String tmpValue = ((String) value).replaceAll("'", "''");
                     values.append("'").append(tmpValue).append("'");
                 } else {
                     values.append(value == null ? "''" : value.toString());
                 }
 
                 if (count < table.getFieldToValue().size() - 1) {
                     columnNames.append(",");
                     values.append(",");
                 }
                 count++;
             }
 
             // this can be made more compact using the enumeration to get back the fields in the order they were served out.
             String queryURL = "INSERT " + (table instanceof Standard ? STANDARD_TABLE : table instanceof Alias ? ALIAS_TABLE : NODE_TABLE)
                     + " (" + columnNames + ") VALUES (" + values + ");";
 
             System.out.println(queryURL);
             return executeInsert(queryURL);
         } catch (Exception e) {
             e.printStackTrace();
        } finally {
            closeConnection();
         }
 
         return false;
     }
 
 }
