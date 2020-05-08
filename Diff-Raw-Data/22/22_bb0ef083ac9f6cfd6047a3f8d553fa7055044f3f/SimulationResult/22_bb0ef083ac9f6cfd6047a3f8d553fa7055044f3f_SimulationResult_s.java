 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.custom.sudplan.geocpmrest.io;
 
 import java.io.Serializable;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 @XmlRootElement
 public final class SimulationResult implements Serializable {
 
     //~ Instance fields --------------------------------------------------------
 
     private String geocpmInfo;
     private String taskId;
 
     private String wmsGetCapabilitiesRequest;
     private String layerName;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new SimulationResult object.
      */
     public SimulationResult() {
     }
 
     /**
      * Creates a new SimulationResult object.
      *
      * @param  geocpmInfo                 DOCUMENT ME!
      * @param  taskId                     DOCUMENT ME!
      * @param  wmsGetCapabilitiesRequest  wmsResults DOCUMENT ME!
      * @param  layerName                  DOCUMENT ME!
      */
     public SimulationResult(final String geocpmInfo,
             final String taskId,
             final String wmsGetCapabilitiesRequest,
             final String layerName) {
         this.geocpmInfo = geocpmInfo;
         this.taskId = taskId;
         this.wmsGetCapabilitiesRequest = wmsGetCapabilitiesRequest;
         this.layerName = layerName;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getGeocpmInfo() {
         return geocpmInfo;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  geocpmInfo  DOCUMENT ME!
      */
     public void setGeocpmInfo(final String geocpmInfo) {
         this.geocpmInfo = geocpmInfo;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getTaskId() {
         return taskId;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  taskId  DOCUMENT ME!
      */
     public void setTaskId(final String taskId) {
         this.taskId = taskId;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getLayerName() {
         return layerName;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  layerName  DOCUMENT ME!
      */
     public void setLayerName(final String layerName) {
         this.layerName = layerName;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public String getWmsGetCapabilitiesRequest() {
         return wmsGetCapabilitiesRequest;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  wmsGetCapabilitiesRequest  DOCUMENT ME!
      */
     public void setWmsGetCapabilitiesRequest(final String wmsGetCapabilitiesRequest) {
         this.wmsGetCapabilitiesRequest = wmsGetCapabilitiesRequest;
     }
 }
