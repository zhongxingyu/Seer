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
 
 package org.neo4j.neoclipse.property;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.PropertyContainer;
 import org.neo4j.graphdb.Transaction;
 
 
 /**
  * <p>
  * Contains all existed node types 
  * </p>
  * @author Saelenchits_N
  * @since 1.0.0
  */
 public enum NodeTypes {
     // TODO: Enum has a cop: org.neo4j.neoclipse.property.NodeTypes
     // TODO: Copy needed for fully implementation of Feature #962
     // TODO: All changes must be reflected in copy
     GIS_PROPERTY("gis_property_type"),
     SITE("site"),
     CITY("city"),
     TRANSMISSION("transmission"),
     MP("mp"),
     SECTOR("sector"),
     NEIGHBOUR("neighbours"),
     MM("mm"),
     CALL("call"),
    GIS_PROPERTIES("properties"),
     NETWORK("network"),
     HILBERT_INDEX("hilbert_index",false),
     CALLS("calls"),
     ROOT_SECTOR_DRIVE("root_sector_site"),
     PROBE("probe"),
     SPREADSHEET("spreadsheet"),
     SPLASH_FORMAT("splash_format"),
     SCRIPT("ruby_script"),
     RUBY_PROJECT("ruby_project"),
     REPORT("report"),
     PIE_CHART("spreadsheet_chart"),
     PIE_CHART_ITEM("spreadsheet_pie_chart_item"),
     CHART("spreadsheet_chart"),
     CHART_ITEM("spreadsheet_chart_item"),
     CELL("spreadsheet_cell"),
     AWE_PROJECT("awe_project"),
     FILE("file"),
     DIRECTORY("directory"),
     DATASET("dataset"),
     HEADER_M("m"),
     HEADER_MS("ms"),
     AGGREGATION("aggregation"),
     COUNT("count"),
     CALL_ANALYSIS("call analysis"),
     S_ROW("s_row"),
     GIS("gis","name"),
     CALL_ANALYSIS_ROOT("call analysis root"),
     S_CELL("s_cell"),
     BSC("bsc"),
     DELTA_NETWORK("delta_network"),
     DELTA_SITE("delta_site"), 
     DELTA_SECTOR("delta_sector"), 
     MISSING_SITES("missing_sites"),
     MISSING_SECTORS("missing_sectors"), 
     MISSING_SITE("missing_site"),
     MISSING_SECTOR("missing_sector");
     
     private final String id;
     private boolean nodeReadOnly;
     private Set<String> notEditableProperties;
     
     /**
      * Constructor with list of properties that can't be modified for current node type 
      * @param id node type ID
      * @param notEditableProperties list of not editable properties
      */
     private NodeTypes(String id, String... notEditableProperties) {
         this.id = id;
         Set<String> pr = new HashSet<String>();
         applyGeneralNotEditablePropertis();
         if(notEditableProperties != null){
             pr.addAll(Arrays.asList(notEditableProperties));
         }
         updatePropertySet(pr);
     }
     
     /**
      * 
      * @param id node type ID
      * @param isNodeReadOnly true if all properties of current node can not be edited.
      */
     private NodeTypes(String id, boolean isNodeReadOnly) {
         this.id = id;
 
         if(isNodeReadOnly){
             this.nodeReadOnly = isNodeReadOnly;
         }else {
             applyGeneralNotEditablePropertis();
         }
     }
     
     /**
      * Apply list of not editable properties that actual for any nodeTypes
      */
     private void applyGeneralNotEditablePropertis() {
         Set<String> properties = new HashSet<String>();
         
         //General not editable properties for all node types
         properties.add("type");
         //end
         
         updatePropertySet(properties);
     }
 /**
  * Update set of not editable properties with new values.
  *
  * @param update set with properties must be added
  */
     private void updatePropertySet(Set<String> update) {
         if(notEditableProperties != null){
             update.addAll(notEditableProperties);
         }
         notEditableProperties=Collections.unmodifiableSet(update);
     }
     /**
      * @return Returns the id.
      */
     public String getId() {
         return id;
     }
     /**
      * Returns NodeTypes by its ID
      *
      * @param enumId id of Node Type
      * @return NodeTypes or null
      */
     public static NodeTypes getEnumById(String enumId) {
         if (enumId == null) {
             return null;
         }
         for (NodeTypes call : NodeTypes.values()) {
             if (call.getId().equals(enumId)) {
                 return call;
             }
         }
         return null;
     }
 /**
  * returns type of node
  *
  * @param container PropertyContainer
  * @param service NeoService
  * @return type of node 
  */
     public static NodeTypes getNodeType(PropertyContainer container,GraphDatabaseService service) {
         Transaction tx = service==null?null:service.beginTx();
         try{
             return getEnumById((String)container.getProperty("type", null));
         }finally{
             if (service!=null){
                 tx.finish();
             }
         }
     }
     /**
      * Check if property editable for current node
      *
      * @param property property to check
      * @return true if property editable
      */
     public boolean isPropertyEditable(String property) {
         if(nodeReadOnly)
             return false;
         return !notEditableProperties.contains(property);  
     }
 
 }
