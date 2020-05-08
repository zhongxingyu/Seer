 /*
  */
 
 package org.geoserver.catalog.hibernate.beans;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import javax.persistence.PostLoad;
 import javax.persistence.PrePersist;
 import javax.persistence.PreUpdate;
 import org.geoserver.catalog.LayerInfo;
 import org.geoserver.catalog.impl.*;
 import org.geoserver.hibernate.Hibernable;
 import org.geotools.util.logging.Logging;
 
 /**
  * 
  * @author ETj <etj at geo-solutions.it>
  */
 public class LayerGroupInfoImplHb extends LayerGroupInfoImpl implements Hibernable {
     private final static Logger LOGGER = Logging.getLogger(LayerGroupInfoImplHb.class);
 
     /**
      *
      */
     private static final long serialVersionUID = 9062753202399238032L;
 
     private Integer version = null;
 
     private List<GroupedLayerHb> groupedLayers = new ArrayList<GroupedLayerHb>();
 
     public List<GroupedLayerHb> getGroupedLayers() {
         return groupedLayers;
     }
 
     protected void setGroupedLayers(List<GroupedLayerHb> groupedLayers) {
         this.groupedLayers = groupedLayers;
     }
 
 
     @PrePersist
     public void prepersist() {
 
         if(getLayers().size() != getStyles().size())
             throw new IllegalStateException("Layers and Styles sizes don't match.");
 
         LOGGER.info("LG " + getName() +  ": prepersisting " + getGroupedLayers().size() + " -> "+ getStyles().size() + " grouped layers");
 
         getGroupedLayers().clear();
         for (int i = 0; i < layers.size(); i++) {
             getGroupedLayers().add(new GroupedLayerHb((LayerInfoImplHb)layers.get(i),
                                                   (StyleInfoImplHb)styles.get(i)));
         }
     }
 
 //    @PreUpdate
     public void preupdate() {
 
         if(getLayers().size() != getStyles().size())
             throw new IllegalStateException("Layers and Styles sizes don't match.");
 
         LOGGER.info("LG " + getName() +  ": preupdating " + getGroupedLayers().size() + " -> "+ getStyles().size() + " grouped layers");
 
         getGroupedLayers().clear();
         for (int i = 0; i < layers.size(); i++) {
             getGroupedLayers().add(new GroupedLayerHb((LayerInfoImplHb)layers.get(i),
                                                   (StyleInfoImplHb)styles.get(i)));
         }
     }
 
     @PostLoad
     public void postload() {
         LOGGER.info("LG " + getName() +  ": postloaded " + getStyles().size() + " -> "+ getGroupedLayers().size() +   " grouped layers");
 
         getLayers().clear();
         getStyles().clear();
 
         for (GroupedLayerHb groupedLayerHb : getGroupedLayers()) {
             getLayers().add(groupedLayerHb.getLayer());
             getStyles().add(groupedLayerHb.getStyle());
         }
 
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder(getClass().getSimpleName())
                 .append("[id:").append(getId())
                 .append(" name:").append(getName())
                .append(" gl:").append(groupedLayers.size())
                .append(" arr:").append(layers.size())
                 .append(" {");
        for (GroupedLayerHb groupedLayerHb : groupedLayers) {
            sb.append(groupedLayerHb.getId()).append(' ');
         }
        sb.append("}]");
 
         return sb.toString();
     }
 
 }
