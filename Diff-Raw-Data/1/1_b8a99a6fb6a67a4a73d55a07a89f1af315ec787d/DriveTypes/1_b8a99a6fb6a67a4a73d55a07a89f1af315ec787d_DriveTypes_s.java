 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.core.enums;
 
 import java.util.ArrayList;
 
 import org.amanzi.neo.core.INeoConstants;
 import org.amanzi.neo.core.utils.NeoUtils;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.PropertyContainer;
 import org.neo4j.graphdb.Transaction;
 
 /**
  * <p>
  * Drive types
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public enum DriveTypes {
     TEMS("tems", "fmt", "TEMS Drive Test Export (*.FMT)"), 
     ROMES("romes", "asc", "Romes drive test export (*.ASC)"),
     GPS("gps", "gps", "GPS truecoverage data export (*.GPS)"),
     NEMO2("nemo2", "nmf", "Nemo drive test export (*.nmf)"), 
     NEMO1("nemo1", "dt1", "Nemo drive test export (*.dt1)"), 
     AMS("ams", "log", ""),
     AMS_CALLS("ams calls", "", "") {
         @Override
         public boolean isVirtual() {
             return true;
         }
 
         @Override
         public String getFullDatasetName(String datasetName) {
             return datasetName + " Calls";
         }
     },
     MS("ms", "", "") {
         @Override
         public boolean isVirtual() {
             return true;
         }
 
         @Override
         public String getFullDatasetName(String datasetName) {
             return datasetName + " (measurement)";
         }
     },
     OSS("oss", "", ""),
     IDEN("iden", "", "");
     
     
     
     //TODO: Lagutko: comments
     private final String id;
     private final String extension;
     private final String description;
 
     /**
      * Constructor
      * 
      * @param id type id
      * @param extension file extension
      * @param description description
      */
     DriveTypes(String id, String extension, String description) {
         this.id = id;
         this.extension = extension;
         this.description = description;
     }
 
     /**
      * @return Returns the id.
      */
     public String getId() {
         return id;
     }
 
     /**
      * @return Returns the extension.
      */
     public String getExtension() {
         return extension;
     }
 
     /**
      * @return Returns the file extension as array.
      */
     public static String[] getFileExtensions(DriveTypes... drive) {
         ArrayList<String> result = new ArrayList<String>();
         if (drive == null) {
             return result.toArray(new String[0]);
         }
         for (DriveTypes driveSingle : drive) {
             StringBuilder ext = new StringBuilder("*.").append(driveSingle.getExtension().toLowerCase())
                 .append(";*.").append(driveSingle.getExtension().toUpperCase());
             result.add(ext.toString());
         }
         return result.toArray(new String[0]);
     }
 
     /**
      * @return Returns the file descriptions as array.
      */
     public static String[] getFileDescriptions(DriveTypes... drive) {
         ArrayList<String> result = new ArrayList<String>();
         if (drive == null) {
             return result.toArray(new String[0]);
         }
         for (DriveTypes driveSingle : drive) {
             result.add(driveSingle.getDescription());
         }
         return result.toArray(new String[0]);
     }
 
     /**
      * @return Returns the description.
      */
     public String getDescription() {
         return description;
     }
 
     /**
      * Finds type by id
      * 
      * @param typeId type id
      * @return DriveTypes or null
      */
     public static DriveTypes findById(String typeId) {
         if (typeId == null) {
             return null;
         }
         for (DriveTypes drive : DriveTypes.values()) {
             if (drive.getId().equals(typeId)) {
                 return drive;
             }
         }
         return null;
     }
 
     /**
      * check dataset is virtual
      * 
      * @return
      */
     public boolean isVirtual() {
         return false;
     }
 
     /**
      * gets full dataset name
      * 
      * @param datasetName- name of Dataset
      * @return full dataset name
      */
     public String getFullDatasetName(String datasetName) {
         return datasetName;
     }
     /**
      * returns type of node
      * 
      * @param container PropertyContainer
      * @param service NeoService
      * @return type of node
      */
     public static DriveTypes getNodeType(PropertyContainer node, GraphDatabaseService service) {
         Transaction tx = service == null ? null : service.beginTx();
         try {
             return findById((String)node.getProperty(INeoConstants.DRIVE_TYPE, null));
         } finally {
             if (service != null) {
                 tx.finish();
             }
         }
     }
     /**
      * Set drive type to current node
      * 
      * @param node - node
      * @param service NeoService - neo service, if null then transaction do not created
      */
     public void setTypeToNode(Node node, GraphDatabaseService service) {
         Transaction tx = NeoUtils.beginTx(service);
         try {
             node.setProperty(INeoConstants.DRIVE_TYPE, getId());
             NeoUtils.successTx(tx);
         } finally {
             NeoUtils.finishTx(tx);
         }
     }
 }
