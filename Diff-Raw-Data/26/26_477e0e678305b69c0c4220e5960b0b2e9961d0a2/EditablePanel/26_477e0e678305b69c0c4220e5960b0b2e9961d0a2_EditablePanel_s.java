 package org.geoserver.security.web;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.IModel;
 import org.geoserver.catalog.LayerInfo;
 
 /**
  * EditablePanel
  * 
  * @author Francesco Izzi (geoSDI)
  */
 
 public class EditablePanel extends Panel {
 
 //	private static final List LAYERS = Arrays.asList(new String[] {"layer1", "layer2", "layer3" });
 	
 	public EditablePanel(String id, IModel inputModel,List layers) {
		super(id);
 
 		
 //		TextField field = new TextField("textfield", inputModel);
		DropDownChoice field = new DropDownChoice("textfield", layers);
 		
 		
 		add(field);
 
 		field.add(new AjaxFormComponentUpdatingBehavior("onblur") {
 			protected void onUpdate(AjaxRequestTarget target) {
 			}
 		});
 	}
 }
