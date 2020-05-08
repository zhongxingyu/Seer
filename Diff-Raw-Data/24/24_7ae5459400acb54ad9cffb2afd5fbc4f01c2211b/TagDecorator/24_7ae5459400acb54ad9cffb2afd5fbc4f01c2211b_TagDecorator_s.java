 package com.redshape.renderer.forms.renderers.decorators;
 
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.Widget;
 import com.redshape.form.IFormItem;
 import com.redshape.form.decorators.DecoratorAttribute;
 import com.redshape.form.decorators.Placement;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class TagDecorator extends AbstractDecorator {
 
 	public static class Attributes extends DecoratorAttribute {
 
 		protected Attributes(String name, boolean renderable ) {
 			super(name);
 		}
 
 		public static final Attributes Attributes = new Attributes("Decorator.Tag.Attributes", true);
 
 	}
 
 	private String tagName;
 	private Placement placement;
 	
 	public TagDecorator( String tagName, Placement placement ) {
 		this(tagName, placement, new HashMap<DecoratorAttribute, Object>() );
 	}
 	
 	public TagDecorator( String tagName, Placement placement, Map<DecoratorAttribute, Object> attributes ) {
 		super( attributes );
 
 		this.placement = placement;
 		this.tagName = tagName;
 	}
 	
 	protected Panel buildTag(IFormItem item) {
         Panel panel = HTMLPanel.wrap( DOM.createElement(this.tagName) );
         this.buildAttributes(item.getAttributes(), panel);
         return panel;
     }
     
 	@Override
 	public Widget decorate(IFormItem item, Widget data) {
         Panel wrapper = new HorizontalPanel();
 		switch ( this.placement ) {
 		case AFTER:
             wrapper.add( this.buildTag(item) );
             wrapper.add(data);
 		break;
 		case BEFORE:
             wrapper.add( data );
             wrapper.add( this.buildTag(item) );
 		break;
 		case WRAPPED:
             Panel widget = this.buildTag(item);
             widget.add( data );
             wrapper.add( widget );
             
 		}
 
         return wrapper;
 	}
 
 	@Override
 	public boolean isSupported(DecoratorAttribute attribute) {
		return Attributes.class.isAssignableFrom(attribute.getClass());
 	}
 }
