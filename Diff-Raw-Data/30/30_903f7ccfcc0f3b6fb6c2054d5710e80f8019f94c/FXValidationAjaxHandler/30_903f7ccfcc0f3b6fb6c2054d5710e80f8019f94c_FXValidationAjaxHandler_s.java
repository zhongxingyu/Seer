 package wicket.contrib.markup.html.form.validation;
 
 import wicket.AttributeModifier;
 import wicket.Component;
 import wicket.WicketRuntimeException;
 import wicket.contrib.dojo.DojoAjaxHandler;
 import wicket.markup.ComponentTag;
 import wicket.markup.html.HtmlHeaderContainer;
 import wicket.markup.html.form.FormComponent;
 import wicket.model.Model;
 import wicket.util.resource.IResourceStream;
 import wicket.util.resource.StringBufferResourceStream;
 import wicket.util.value.ValueMap;
 
 /**
  * Ajaxhandler to be bound to FormComponents.<br/> 
  * This handler executes wicket validation on set event
  * (e.g. onblur, onchange) with an Ajax server call.<br/>
  * The Formcomponent is highlighted based on the validation result.
  * Example: TextField's background will turn red if validation fails.
  * For usage tutorials see: TODO link to tutorials.
  * 
  * 
  * @author Marco van de Haar
  * @author Ruud Booltink
  */
 public class FXValidationAjaxHandler extends DojoAjaxHandler
 {
 	/** name event, like onblur. */
 	private final String eventName;
 
 	/** component this handler is attached to. */
 	private FormComponent formComponent;
 	
 	private final RGB invalidRGB;
 	/**	not mandatory, if not set validRGB will be components background color*/
 	private RGB validRGB; 
 	
 	private String componentId;
 	
 	/**
 	 * Default constructor which uses node's current 
 	 * background color when component is valid.
 	 * @param eventName @see this.eventName
 	 */
 	public FXValidationAjaxHandler(String eventName)
 	{
 		if (eventName == null)
 		{
 			throw new NullPointerException("argument eventName must be not null");
 		}
 		this.eventName = eventName;
 		this.invalidRGB = RGB.DEFAULT_INVALID;
 	}
 	
 	/**
 	 * Constructor which sets default valid highlight color 
 	 * @param eventName @see this.eventName
 	 * @param colorValid True if default highlight color should be used 
 	 * in stead of node's current background color when 
 	 * component is valid. 
 	 */
 	public FXValidationAjaxHandler(String eventName, boolean colorValid)
 	{
 		if (eventName == null)
 		{
 			throw new NullPointerException("argument eventName must be not null");
 		}
 		this.eventName = eventName;
 		this.invalidRGB = RGB.DEFAULT_INVALID;
 		this.validRGB = RGB.DEFAULT_VALID;
 	}
 	
 	/**
 	 * Constructor with custom invalid RGB values.
 	 * @param eventName @see this.eventName
 	 * @param r int representing Red value for this.invalidRGB
 	 * @param g int representing Green value for this.invalidRGB
 	 * @param b int representing Blue value for this.invalidRGB
 	 */
 	public FXValidationAjaxHandler(String eventName, int r, int g, int b)
 	{
 		if (eventName == null)
 		{
 			throw new NullPointerException("argument eventName must be not null");
 		}
 		this.eventName = eventName;
 		this.invalidRGB = new RGB(r,g,b);
 	}
 	
 	/**
 	 * Constructor with custom invalid RGB values and valid RGB values.
 	 * @param eventName @see this.eventName
 	 * @param ir int representing Red value for this.invalidRGB
 	 * @param ig int representing Green value for this.invalidRGB
 	 * @param ib int representing Blue value for this.invalidRGB
 	 * @param vr int representing Red value for this.validRGB
 	 * @param vg int representing Green value for this.validRGB
 	 * @param vb int representing Blue value for this.validRGB
 	 */
 	public FXValidationAjaxHandler(String eventName, int ir, int ig, int ib, int vr, int vg, int vb)
 	{
 		if (eventName == null)
 		{
 			throw new NullPointerException("argument eventName must be not null");
 		}
 		this.eventName = eventName;
 		this.validRGB = new RGB(ir,ig,ib);
 		this.invalidRGB = new RGB(vr, vg, vb);
 	}
 	
 	/**
 	 * Write the validate/highlight javascript function to the page's head. 
 	 * @see wicket.AjaxHandler#renderHeadContribution(wicket.markup.html.HtmlHeaderContainer)
 	 */
 	public final void renderHeadContribution(HtmlHeaderContainer container)
 	{
 		String highlightValidFunction;
 		String highlightInvalidFunction;
  
 		/*check if valdiRGB is set, if not use node's current background color
 		 *for valid highlighting. 
 		 */
 		if(validRGB != null)
 		{
 			highlightValidFunction = "dojo.fx.html.colorFadeOut(field, " + this.validRGB.toString() + ", duration);";
 			highlightInvalidFunction = "dojo.fx.html.colorFadeOut(field, " + this.invalidRGB.toString() + ", duration ,0);";
 		} 
 		else 
 		{
 			//validRGB is null!
 			highlightValidFunction = "dojo.fx.html.colorFadeOut(field, startbc, duration);";
 			highlightInvalidFunction = "dojo.fx.html.colorFadeOut(field, " + this.invalidRGB.toString() + ", duration ,0);";
 		}
 		
 		String s =
 
 			"\t<script language=\"JavaScript\" type=\"text/javascript\">\n" +
 			"\t"+ componentId  + "_first = false; \n"+ 
 			"\tfunction "+ componentId  + "_validate(componentUrl, componentPath, field) { \n" +
 			"\tduration = " + getDuration() + "; \n"+
 			"\t\t\tif(!"+ componentId  + "_first){\n"+
 			"\t\t\t"+ componentId  + "_first = true; \n"+
 			"\t\t\t\tstartbc = dojo.html.getBackgroundColor(field);\n" +
 			"\t\t\t}\n"+ 
 			"\t\tdojo.io.bind({\n" +
 			"\t\t\turl: componentUrl + '&' + componentPath + '=' + field.value,\n" +
 			"\t\t\tmimetype: \"text/plain\",\n" +
 			"\t\t\tload: function(type, data, evt) {\n" +
 			"\t\t\t\tif(data == 'valid')\n" +
 			"\t\t\t\t{\n" +
 			"\t\t\t\t\t" + highlightValidFunction + "\n" +
 			"\t\t\t\t\treturn true;\n" +
 			"\t\t\t\t}\n" +
 			"\t\t\t\telse if(data == 'invalid')\n"+
 			"\t\t\t\t{\n" +
 			"\t\t\t\t" + highlightInvalidFunction + "\n" +
 			"\t\t\t\t\treturn false;\n" +
 			"\t\t\t\t}\n" +
 			"\t\t\t\telse\n" +
 			"\t\t\t\t{\n" +
 			"\t\t\t\t\treturn false;\n" +
 			"\t\t\t\t}\n" +
 			"\t\t\t}\n" +
 			"\t\t});\n" +
 			
 			"\t}\n" +
 			"\t</script>\n";
 
 		container.getResponse().write(s);
 	}
 	
	/**
 	 * Attaches the event handler for the given component to the given tag.
 	 * @param tag
 	 *            The tag to attach
	 */
 	public final void onComponentTag(final ComponentTag tag)
 	{
 		final ValueMap attributes = tag.getAttributes();
 		final String attributeValue =
 			"javascript:"+ componentId  + "_validate('" + getCallbackUrl() + "', '" + formComponent.getInputName() + "', this);";
 		attributes.put(eventName, attributeValue);
	}
 
 	/**
 	 * Bind this handler to the FormComponent and set the corresponding
 	 * HTML id attribute.
 	 * @see wicket.AjaxHandler#onBind()
 	 */
 	protected void onBind()
 	{
 		Component c = getComponent();
 		if (!(c instanceof FormComponent))
 		{
			throw new WicketRuntimeException("this handler must be bound to FormComponents");
 		}
 		this.formComponent = (FormComponent)c;
		this.formComponent.add(new AttributeModifier("id", true, new Model(this.formComponent.getId())));
 		this.componentId = this.formComponent.getId();
 	
 	}
 
 
 	/**
 	 * Gets the resource to render to the requester.
 	 * In this implementation, just validate formcomponent
 	 * and return the result. In the future, when partial page
 	 * rendering is well implemented in wicket, this method should
 	 * also visit and rerender IFeedback components.
 	 * @return the resource to render to the requester
 	 */
 	protected final IResourceStream getResponse()
 	{
 		StringBufferResourceStream s = new StringBufferResourceStream();
 
 		formComponent.validate();
 
 		// When validation failed...
 		if (!formComponent.isValid())
 		{
 			//TODO finish
 			// The plan here is the visit all feedback components, re-render them, and
 			// return the render results to the browser with the components (top level)
 			// ids attached. We could then use this information to replace the dom
 			// elements in the browser
 
 			// We need a couple of things for this to work first:
 			// 1) The ability to let a component render on its' own
 			// 2) Trap that render result somewhere. Either by setting the response to
 			//			render to on that component, or passing a response as a parameter
 			//			of the render call
 			// Furthermore, we need to have the javascript side covered. That could
 			// be tricky too, but the cool thing about that is that if we would fix
 			// that in a generic fashion, our ajax support would be pretty usable at once
 
 			/*formComponent.getPage().visitChildren(IFeedback.class, new Component.IVisitor()
 			{
 				public Object component(Component component)
 				{
 					// this doesn't work yet.
 					//component.render();
 					return Component.IVisitor.CONTINUE_TRAVERSAL;
 				}
 			});*/
 			
 			s.append("invalid");
 		}
 		else
 		{
 			s.append("valid");
 		}
 		return s;
 	}
 	
 	/**
 	 * Let subclasses define their very own duration.
 	 * @return duration
 	 */
 	protected int getDuration()
 	{
 		return 300;
 	}
 	
 	/**
 	 * Inner class for structured storage 
 	 * of colors using simple RGB values.
 	 * @author Marco van de Haar
 	 * @author Ruud Booltink
 	 */
 	private static class RGB
 	{
 		private final int R;
 		private final int G;
 		private final int B;
 		
 		/** Create new RGB representing the default color for valid components */
 		public static final RGB DEFAULT_VALID = new RGB(152,194,125);
 		/** Create new RGB representing the default color for invalid components */
 		public static final RGB DEFAULT_INVALID = new RGB(252,134,130);
 		
 		/**
 		 * Construct an RGB with given R, G and B values.
 		 * @param R
 		 * @param G
 		 * @param B
 		 */
 		public RGB(int R, int G, int B)
 		{
 			this.R = R;
 			this.G = G;
 			this.B = B;
 						
 		}
 		
 		/**
 		 * @return Red value
 		 */
 		public int getR()
 		{
 			return R;
 		}
 		
 		/**
 		 * @return Green value
 		 */
 		public int getG()
 		{
 			return G;
 		}
 		
 		/**
 		 * @return Blue value
 		 */
 		public int getB()
 		{
 			return B;
 		}
 		
 		/**
 		 * return RGB value in a string which Dojo can use (javascript array).
 		 * @see java.lang.Object#toString()
 		 */
 		public String toString()
 		{
 			return "["+ R +", "+ G +", "+ B +"]";
 		}
 	}
 	
 
 }
