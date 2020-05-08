 package org.geoserver.web.publish;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.CheckBox;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.ComponentPropertyModel;
 import org.apache.wicket.model.PropertyModel;
 
 public class BasicLayerConfig extends LayerConfigurationPanel {
 	
 	public BasicLayerConfig(String id, IModel model) {
 		super(id, model);
 		init();
 		add(new TextField("name"));
 		add(new CheckBox("enabled"));
 	}
 	
 	private void init(){
 	}
 	
 	private static final long serialVersionUID = 677955476932894110L;
 }
