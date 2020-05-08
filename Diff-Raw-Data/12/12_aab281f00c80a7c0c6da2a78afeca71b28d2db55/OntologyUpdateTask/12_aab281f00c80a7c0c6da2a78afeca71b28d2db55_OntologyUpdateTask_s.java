 package org.biosharing.cli.tasks;
 
 import org.biosharing.model.*;
 import org.biosharing.utils.OntologyLocator;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * Created by the ISA team
  *
  * @author Eamonn Maguire (eamonnmag@gmail.com)
  *         <p/>
  *         Date: 14/06/2012
  *         Time: 14:42
  */
 public class OntologyUpdateTask extends Task {
 
     private int startingNodeId;
     private int aliasId;
     private int startingComputedId;
 
     public OntologyUpdateTask() {
         super("Ontology update task");
 
     }
 
     @Override
     public void doTask() {
         try {
             // this will let us know what hasn't already been added
             Map<String, Standard> standards = dao.getStandardNodeInformation();
 
             OntologyLocator ontologyLocator = new OntologyLocator();
             List<Standard> ontologies = ontologyLocator.getAllOntologies();
 
             System.out.println("Getting next node id.");
             startingNodeId = getNextNodeId();
             System.out.println(startingNodeId);
 
             System.out.println("Getting next pid.");
             getNextAliasAndStartingComputedId();
             System.out.println(aliasId);
 
             System.out.println("Getting next computed node id.");
             System.out.println(startingComputedId);
 
             int nodeId = startingNodeId;
 
             System.out.println("Going to add " + ontologies.size() + " ontologies");
             System.out.println();
             for (Standard ontology : ontologies) {
                 System.out.println("Going to add " + ontology.getStandardTitle());
                 if (!standards.containsKey(ontology.getStandardTitle())) {
 
                     ontology.getFieldToValue().put(StandardFields.SERIAL_ID, startingComputedId);
                     ontology.getFieldToValue().put(StandardFields.NID, nodeId);
                     ontology.getFieldToValue().put(StandardFields.VID, nodeId);
                     ontology.addFieldAndValue(StandardFields.COMPUTED_ID, createComputedId(startingComputedId, '0', 6));
 
                     dao.insertInformation(ontology);
 
                     Node nodeForStandard = new Node();
                     nodeForStandard.initialiseNodeForStandard(nodeId, ontology);
                     dao.insertInformation(nodeForStandard);
 
                     System.out.println("Inserted Node");
 
                     Alias aliasForStandard = new Alias();
                     aliasForStandard.initialiseAliasForStandard(aliasId, nodeId, ontology);
                     dao.insertInformation(aliasForStandard);
                     System.out.println("Inserted Alias");
 
                     NodeRevision nodeRevision = new NodeRevision();
                     nodeRevision.initialiseNodeRevisionForStandard(nodeId, ontology);
                     dao.insertInformation(nodeRevision);
                     System.out.println("Inserted NodeRevision");
 
                     startingComputedId++;
                     aliasId++;
                     nodeId++;
                 }
             }
 
             if (nodeId == 0) {
                 System.out.println("Database is up to date.");
             } else {
                 System.out.println((nodeId - startingNodeId) +
                         " ontologies were not in the database before. They have now been added.");
             }
         } catch (Exception e) {
             System.err.println("Error occurred: " + e.getMessage());
        } finally {
            dao.closeConnection();    
         }

        
     }
 
 
     private String createComputedId(int id, char paddingCharacter, int desiredLength) {
         System.out.println("Creating computed id for " + id);
         String value = String.valueOf(id);
 
         int padding = desiredLength - value.length();
         for (int paddingCount = 0; paddingCount < padding; paddingCount++) {
             value = paddingCharacter + value;
         }
 
         String bsg = "bsg-" + value;
         System.out.println(bsg);
         return bsg;
     }
 
     public void getNextAliasAndStartingComputedId() {
         List<Alias> aliases = dao.getAliasInformation();
         aliasId = getAliasIdStart(aliases);
         startingComputedId = aliasId;
     }
 
     public int getNextNodeId() {
 
         // this will let us know what hasn't already been added
         List<Node> nodes = dao.getNodeInformation();
         return getNodeIdStart(nodes);
     }
 
     public int getAliasIdStart(List<Alias> aliases) {
         int maxValue = 0;
 
         for (Alias alias : aliases) {
             try {
                 int nodeId = Integer.valueOf(alias.getPID());
                 if (nodeId > maxValue) {
                     maxValue = nodeId;
                 }
             } catch (NumberFormatException nfe) {
                 // skip this node
             }
         }
 
         return maxValue + 1;
     }
 
     public int getNodeIdStart(List<Node> nodes) {
         int maxValue = 0;
 
         for (Node node : nodes) {
             try {
                 int nodeId = Integer.valueOf(node.getNodeId());
                 if (nodeId > maxValue) {
                     maxValue = nodeId;
                 }
             } catch (NumberFormatException nfe) {
                 // skip this node
             }
         }
 
         return maxValue + 1;
     }
 }
