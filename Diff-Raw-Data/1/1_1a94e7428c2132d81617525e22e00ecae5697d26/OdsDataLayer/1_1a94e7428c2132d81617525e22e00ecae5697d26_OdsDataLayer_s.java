 package org.openstreetmap.josm.plugins.openservices;
 
 import org.openstreetmap.josm.data.osm.DataSet;
 import org.openstreetmap.josm.gui.layer.OsmDataLayer;
 
 /**
  * To distinct the ODS DataLayer from a normal Osm datalayer, 
  * we create a subclass of OsmDataLayer.
  * I'd prefer to subclass Layer instead, but if we did so,
  * we would lose to much functionality that depends directly
  * on the OsmDataLayer class.
  * 
  * @author Gertjan Idema
  *
  */
 public class OdsDataLayer extends OsmDataLayer {
 
   /**
    * Simple constructor providing a new (empty) dataset
    * 
    * @param name
    */
   public OdsDataLayer(String name) {
     super(new DataSet(), name, null);
   }
 
 }
