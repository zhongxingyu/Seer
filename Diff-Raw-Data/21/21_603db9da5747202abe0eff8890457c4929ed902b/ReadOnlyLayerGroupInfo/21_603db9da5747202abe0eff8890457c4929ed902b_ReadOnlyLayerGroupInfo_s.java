 package org.geoserver.security.decorators;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.geoserver.catalog.LayerGroupInfo;
 import org.geoserver.catalog.LayerInfo;
 
public class ReadOnlyLayerGroupInfo extends DecoratingLayerGroupInfo {
 
     private List<LayerInfo> layers;
 
     /**
      * Overrides the layer group layer list with the one provided (which is
      * supposed to have been wrapped so that each layer can be accessed only
      * accordingly to the current user privileges)
      * 
      * @param delegate
      * @param layers
      */
     public ReadOnlyLayerGroupInfo(LayerGroupInfo delegate, List<LayerInfo> layers) {
         super(delegate);
         this.layers = layers;
     }
 
     public ReadOnlyLayerGroupInfo(LayerGroupInfo delegate) {
         super(delegate);
 
         // wrap all layers in read only mode
         List<LayerInfo> original = delegate.getLayers();
         layers = new ArrayList<LayerInfo>(original.size());
         for (LayerInfo layer : original) {
             if(layer == null)
                 layers.add(null);
             else
                 layers.add(new ReadOnlyLayerInfo(layer));
         }
     }
 
     @Override
     public List<LayerInfo> getLayers() {
         return layers;
     }
 
 }
