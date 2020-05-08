 package org.geoserver.web.publish;
 
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.CheckBox;
 import org.apache.wicket.markup.html.form.TextField;
 import org.geoserver.web.util.MapModel;
 
 public class HTTPLayerConfig extends LayerConfigurationPanel {
     public HTTPLayerConfig(String id, IModel model){
         super(id, model);
        add(new Label("section", "HTTP Settings"));
         add(new CheckBox("cachingEnabled", new MapModel(getLayerInfo().getResource().getMetadata(), "cachingEnabled")));
         add(new TextField("cacheAgeMax", new MapModel(getLayerInfo().getResource().getMetadata(), "cacheAgeMax")));
     }
 }
 
