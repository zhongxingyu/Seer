 package nu.cotentin.impl.renderer;
 
 import javax.faces.component.UIComponent;
 import javax.faces.component.UISelectItems;
 
 import nu.cotentin.api.CotentinContext;
 import nu.cotentin.api.CotentinException;
 import nu.cotentin.api.CotentinScriptRenderer;
 import nu.cotentin.api.renderer.ComponentScriptRenderer;
 
 import nu.cotentin.cotentinel.AccessorNode;
 import nu.cotentin.cotentinel.ContextNode;
 import nu.cotentin.cotentinel.ExprNode;
 import nu.cotentin.cotentinel.IdentifierNode;
 import nu.cotentin.cotentinel.LiteralNode;
 import nu.cotentin.cotentinel.OpNode;
 import nu.cotentin.impl.utils.ScriptBuffer;
 
 
 /**
  * Renderer for JavaScript Strings
  * @author Christian Heike
  *
  */
 public abstract class ScriptRenderer {
 	
 	/**
 	 * The function for getting a component from the DOM by its id
 	 */
 	public static final String GETTER_COMPONENT = "$c";
 	/**
 	 * The function for getting a property on an object
 	 */
 	public static final String GETTER_PROPERTY = "$g";
 	/**
 	 * The function for setting a property on an object
 	 */
 	public static final String SETTER_PROPERTY = "$s";
 	/**
 	 * The function for setting options
 	 */
 	public static final String SETTER_OPTIONS = "$o$s";
 	/**
 	 * The function for getting options
 	 */
 	public static final String GETTER_OPTIONS = "$o$g";
 	/**
 	 * The context object
 	 */
 	public static final String CONTEXT_OBJECT = "_c";
 	/**
 	 * The request property on the context
 	 */
 	public static final String REQUEST_OBJECT = "request";
 	/**
 	 * The response property on the context
 	 */
 	public static final String RESPONSE_OBJECT = "response";
 	/**
 	 * The exception object
 	 */
 	public static final String EXCEPTION_OBJECT = "exception";
 	/**
 	 * The cotentin object (namespace)
 	 */
 	public static final String COTENTIN_OBJECT = "cotentin";
 	/**
 	 * The target object (for the ajax request)
 	 */
 	public static final String TARGET_OBJECT = "target";
 	/**
 	 * The event object (for the ajax request)
 	 */
 	public static final String EVENT_OBJECT = "event";
 	/**
 	 * The function on the context evaluating code in a cotentin context 
 	 */
 	public static final String FUNCTION_EVALCTX = "evaluateInContext";
 	/**
 	 * The property describing the view root on the context object
 	 */
 	public static final String PROPERTY_VIEWROOT = "viewRoot";
 	/**
 	 * The property describing the root component on the context object
 	 */
 	public static final String PROPERTY_ROOTCOMPONENT = "rootComponent";
 	/**
 	 * The component identifier for the accessor node of components  
 	 */
 	public static final String COMPONENTS_IDENTIFIER = "components";
 	
 	/**
 	 * Renders a provided script to be evaluated in the provided context
 	 * @param ctx the context the script is to be evaluated in
 	 * @param script the script to be evaluated
 	 * @return the JavaScript string evaluating the provided script in the context
 	 */
 	public static String renderInContext(CotentinContext ctx, String script) {
 		ScriptBuffer sb = new ScriptBuffer();
 		sb.append(COTENTIN_OBJECT);
 		sb.append(".");
 		sb.append(FUNCTION_EVALCTX);
 		sb.append("(");
 		renderContext(sb, ctx);
 		sb.append(",this,event = typeof event == \"undefined\" ? null : event,");
 		renderScript(sb, script);
 		sb.append(");");
 		return sb.toString();
 	}
 
 	/**
 	 * Renders the provided context to the provided string buffer
 	 * @param sb the string buffer to render to
 	 * @param ctx the context to render
 	 */
 	private static void renderContext(ScriptBuffer sb, CotentinContext ctx) {
 		if (ctx == null) {
 			throw new CotentinException("Context may not be null!");
 		}
 		sb.append("{");
 		sb.appendProperties(false, 
 				PROPERTY_VIEWROOT, renderGetElementById(ctx.getFacesContext().getViewRoot(), ctx), 
 				PROPERTY_ROOTCOMPONENT, renderGetElementById(ctx.getRootComponent(), ctx));
 		sb.append(",");
 		sb.appendProperties(false,
 				REQUEST_OBJECT, ctx.getRequestJSON(),
 				RESPONSE_OBJECT, ctx.getResponseJSON());
 		sb.append("}");
 	}
 
 	/**
 	 * Renders the JavaScript getting the DOM element representing the
 	 * component provided in the provided context 
 	 * @param comp the component the DOM element is to be resolved
 	 * @param ctx the context to resolve it in
 	 * @return the JavaScript getting the DOM element
 	 */
 	private static String renderGetElementById(UIComponent comp, CotentinContext ctx) {
 		return renderGetElementById(comp.getClientId(ctx.getFacesContext()));
 	}
 
 	/**
 	 * @see ScriptRenderer#renderGetElementById(UIComponent, CotentinContext)
 	 * @param id the id of the DOM element
 	 * @return the JavaScript to determine the DOM element going by the provided id
 	 */
 	private static String renderGetElementById(String id) {
 		return "document.getElementById('".concat(id).concat("')");
 	}
 
 	/**
 	 * Renders a script to be called by the code to be called by
 	 * @see ScriptRenderer#renderInContext(CotentinContext, String)
 	 * by wrapping the script into the appropriate function 
 	 * @param sb the string buffer to render the script to
 	 * @param script the function running the script in the context
 	 */
 	private static void renderScript(ScriptBuffer sb, String script) {
 		sb.append("function(");
 		renderParams(sb, CONTEXT_OBJECT, GETTER_COMPONENT, GETTER_PROPERTY, 
 				SETTER_PROPERTY, GETTER_OPTIONS, SETTER_OPTIONS, TARGET_OBJECT,
 				EVENT_OBJECT);
 		sb.append("){");
 		sb.append(script);
 		sb.append("}");
 	}
 
 	/**
 	 * Renders a JavaScript getting the object denoted by the expression tree
 	 * provided by the root node
 	 * @param ctx the context to create the JavaScript for
 	 * @param root the root node of the expression tree to get the object for
 	 * @return the JavaScript code for getting the object
 	 */
 	public static String renderGet(CotentinContext ctx, ExprNode root) {
 		WrapRenderer renderer = new WrapRenderer(ctx);
 		renderer.chainGet(root);
 		return renderer.toString();
 	}
 
 	/**
 	 * Renders a JavaScript getting the object denoted by the expression tree
 	 * provided by the root node to the provided value
 	 * @param ctx the context to create the JavaScript for
 	 * @param root the root node of the expression tree to set the object
 	 * @param value the value to set
 	 * @return the JavaScript code for setting the object to the value
 	 */
 	public static String renderSet(CotentinContext ctx, ExprNode root, String value) {
 		WrapRenderer renderer = new WrapRenderer(ctx);
 		renderer.chainSet(root, value);
 		return renderer.toString();
 	}
 
 	private static void renderParams(ScriptBuffer sb, String... params) {
 		if (params.length > 0) {
 			sb.append(params[0]);
 			for (int i = 1; i < params.length; i++) {
 				sb.append(",");
 				sb.append(params[i]);
 			}
 		}
 	}
 
 	/**
 	 * Internal implementation for rendering by wrapping
 	 * @author Christian Heike
 	 *
 	 */
 	private static class WrapRenderer {
 		private ScriptBuffer sb = new ScriptBuffer();
 		private CotentinContext ctx;
 
 		public WrapRenderer(CotentinContext ctx) {
 			this.ctx = ctx;
 		}
 
 		void chainGet(ExprNode root) {
 			chainGetSet(root, null);
 		}
 
 		void chainSet(ExprNode root, String value) {
 			chainGetSet(root, value);
 			// Chain setter
 			sb.append(",");
 			sb.append(value);
 			sb.wrapFunctionCall(SETTER_PROPERTY);
 		}
 
 		private void chainProperty(String object, String property, String wrapper, 
 								   boolean createNonExisting) {
 			if (object != null) {
 				sb.append(object);
 			}
 			sb.append(",'");
 			sb.append(property);
 			sb.append("'");
 			if (createNonExisting) {
 				sb.append(",true");
 			}
 			if (wrapper != null) {
 				sb.wrapFunctionCall(wrapper);
 			}
 		}
 
 		private void chainGetSet(ExprNode root, String value) {
 			String wrapper = (value == null || !root.isLeaf()) ? GETTER_PROPERTY : null;
 			if (root instanceof ContextNode) {
 				if (!(!root.isLeaf() 
 					  && root.getChildren().get(0) instanceof IdentifierNode
 					  && ((IdentifierNode)root.getChildren().get(0))
 					  		.getIdentifier().equals(COMPONENTS_IDENTIFIER))) {
 					sb.append(CONTEXT_OBJECT);
 				}
 				if (!root.isLeaf()) {
 					chainGetSet(root.getChildren().get(0), value);
 				}				
 			} else if (root instanceof IdentifierNode) {
 				IdentifierNode cNode = (IdentifierNode)root;
 				if (root.getParent() instanceof ContextNode
 						   && cNode.getIdentifier().equals(COMPONENTS_IDENTIFIER)
 						   && !root.isLeaf()
 						   && root.getChildren().get(0) instanceof AccessorNode) {
 					renderComponent((AccessorNode) root.getChildren().get(0) , value);
 				} else {
 					if (root.getParent() == null || root.getParent() instanceof OpNode) {
 						sb.append(cNode.getIdentifier());
 					} else {
						chainProperty(null, cNode.getIdentifier(), wrapper, wrapper != null);
 					}
 					if (!root.isLeaf()) {
 						chainGetSet(root.getChildren().get(0), value);
 					}
 				}
 			} else if (root instanceof AccessorNode) {
 				String valueStr;
 				Object valueObj = ((AccessorNode)root).getValue();
 				if (valueObj instanceof String) {
 					valueStr = "'".concat((String)valueObj).concat("'");
 				} else {
 					valueStr = valueObj.toString();
 				}
				chainProperty(null, valueStr, wrapper, wrapper != null);
 				if (!root.isLeaf()) {
 					chainGetSet(root.getChildren().get(0), value);
 				}
 			} else if (root instanceof OpNode) {
 				if (value != null) {
 					throw new CotentinException("Cannot create setter for an expression tree containing an operation!");
 				}
 				if (!root.isLeaf()) {
 					WrapRenderer left = new WrapRenderer(ctx);
 					left.chainGetSet(root.getChildren().get(0), value);
 					WrapRenderer right = new WrapRenderer(ctx);
 					right.chainGetSet(root.getChildren().get(1), value);
 					sb.append(left.toString());
 					sb.append(((OpNode)root).getOperator());
 					sb.append(right.toString());
 				}
 			} else if (root instanceof LiteralNode) {
 				if (value != null) {
 					throw new CotentinException("Cannot create setter for an expression tree containing a literal node!");
 				}
 				sb.appendQuoted(((LiteralNode)root).getValue()+"");
 			}			
 		}
 
 		@Override
 		public String toString() {
 			return sb.toString();
 		}
 		
 		
 		private void renderComponent(AccessorNode idNode, String value) {
 			UIComponent comp = ctx.getRootComponent().findComponent(idNode.getValue().toString());
 			if (comp == null) {
 				throw new CotentinException("Cannot find component!");
 			}
 			CotentinScriptRenderer ann = 
 					comp.getClass().getAnnotation(CotentinScriptRenderer.class);
 			ComponentScriptRenderer renderer = null;
 			if (comp instanceof UISelectItems) {
 				renderer = new OptionsScriptRenderer(); 
 			} else if (ann != null) {
 				Class<? extends ComponentScriptRenderer> rendererClass = ann.value();
 				try {
 					renderer = rendererClass.newInstance();
 				} catch (InstantiationException ex) {
 					throw new CotentinException(ex);
 				} catch (IllegalAccessException ex) {
 					throw new CotentinException(ex);
 				}
 			} 
 			if (renderer != null) {
 				if (value == null) {
 					sb.append(renderer.renderGet(ctx, comp, idNode.getChildren()));
 				} else {
 					sb.append(renderer.renderSet(ctx, comp, idNode.getChildren(), value));
 				}
 			} else {
 				sb.append(GETTER_COMPONENT);
 				sb.append("('");
 				sb.append(idNode.getValue().toString());
 				sb.append("')");
 				if (!idNode.isLeaf()) {
 					chainGetSet(idNode.getChildren().get(0), value);
 				}
 			}
 
 		}
 	}
 }
