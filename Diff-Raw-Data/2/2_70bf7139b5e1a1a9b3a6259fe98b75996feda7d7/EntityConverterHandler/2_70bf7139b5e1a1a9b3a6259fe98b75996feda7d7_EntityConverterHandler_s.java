 package com.iq4j.faces.taghandler;
 
 import java.io.IOException;
 
 import javax.faces.component.UIComponent;
 import javax.faces.view.facelets.FaceletContext;
 import javax.faces.view.facelets.TagAttribute;
 import javax.faces.view.facelets.TagConfig;
 import javax.faces.view.facelets.TagHandler;
 
 import com.iq4j.faces.util.ExpressionsUtil;
 
 public class EntityConverterHandler extends TagHandler {
 
 	private final TagAttribute entity;
 	private final TagAttribute noSelectionValue;
 	
 	public EntityConverterHandler(TagConfig config) {
 		super(config);
 		this.entity = getAttribute("entity");
 		this.noSelectionValue = getAttribute("noSelectionValue");
 	}
 
 	@Override
 	public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
 		
		if(entity == null) { // if entry attribute not setted. entityConverter would not be registered.
 			return;
 		}
 		
 		parent.getAttributes().put("entity", entity.getValue(ctx));
 		parent.getAttributes().put("noSelectionValue", noSelectionValue == null ? "" : noSelectionValue.getValue(ctx));
 		parent.setValueExpression("converter", new ExpressionsUtil().createValueExpression("#{entityConverter}")); 
 		
 	}
 
 	
 }
