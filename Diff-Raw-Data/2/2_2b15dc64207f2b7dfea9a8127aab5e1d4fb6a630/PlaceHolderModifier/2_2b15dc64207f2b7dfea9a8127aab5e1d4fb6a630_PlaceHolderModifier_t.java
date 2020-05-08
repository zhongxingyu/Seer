 package net.link.util.wicket.behaviour;
 
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.resources.JavascriptResourceReference;
 import org.apache.wicket.model.IModel;
 
 
 /**
  * Created by IntelliJ IDEA. User: sgdesmet Date: 31/10/11 Time: 14:47 To change this template use File | Settings | File Templates.
  */
 public class PlaceHolderModifier extends AttributeModifier {
 
     Component component = null;
 
     private static final ResourceReference PLACEHOLDER_JS = new JavascriptResourceReference(PlaceHolderModifier.class, "placeholder.js");
 
     public PlaceHolderModifier(IModel<String> model){
        super("placeholder",true, model );
     }
 
     /**
 	 * Bind this handler to the given component.
 	 *
 	 * @param hostComponent
 	 *            the component to bind to
 	 */
 	@Override
 	public void bind(final Component hostComponent)
 	{
         super.bind( hostComponent );
 		if (hostComponent == null)
 		{
 			throw new IllegalArgumentException("Argument hostComponent must be not null");
 		}
 
 		if (component != null)
 		{
 			throw new IllegalStateException("this kind of handler cannot be attached to " +
 				"multiple components; it is already attached to component " + component +
 				", but component " + hostComponent + " wants to be attached too");
 		}
 
 		component = hostComponent;
 	}
 
 
     @Override
     public void renderHead(final IHeaderResponse response) {
         super.renderHead( response );
         renderHtml5Script(response);
     }
 
     private void renderHtml5Script(IHeaderResponse response)
 	{
 		response.renderJavascriptReference( PLACEHOLDER_JS );
 		final String id = getComponent().getMarkupId();
 
 
 		String initJS = String.format("$('#%s').placeholderEnhanced()", id);
 		response.renderOnDomReadyJavascript( initJS );
 	}
 
     public Component getComponent() {
         return component;
     }
 }
