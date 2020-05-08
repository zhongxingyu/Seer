 package nu.cotentin.impl.jsf2.behavior;
 
 import java.io.IOException;

 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.el.ValueExpression;
 import javax.faces.application.ResourceDependencies;
 import javax.faces.application.ResourceDependency;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIComponentBase;
 import javax.faces.component.behavior.AjaxBehavior;
 import javax.faces.component.behavior.ClientBehaviorContext;
 import javax.faces.component.behavior.FacesBehavior;
 import javax.faces.context.FacesContext;
 import javax.faces.context.PartialResponseWriter;
 import javax.ws.rs.core.MediaType;
 
 import nu.cotentin.api.CotentinContext;
 import nu.cotentin.api.CotentinException;
 import nu.cotentin.api.rest.JerseyResponse;
 import nu.cotentin.impl.eval.ServerSideEvaluator;
 import nu.cotentin.impl.jsf2.components.JerseyPathParameter;
 import nu.cotentin.impl.jsf2.renderer.ClientScriptBuilder;
 import nu.cotentin.impl.logging.LoggerFacade;
 import nu.cotentin.impl.rest.JerseyRequestImpl;
 import nu.cotentin.impl.rest.RequestExecuter;
 import nu.cotentin.impl.services.CotentinServiceRegister;
 import nu.cotentin.impl.services.JerseyService;
 
 
 /**
  * Faces behavior class implementing the behavior handler for a jersey request
  * @author Christian Heike
  *
  */
 @FacesBehavior(JerseyRequestBehavior.BEHAVIOR_ID)
 @ResourceDependencies(
 	{@ResourceDependency(library="javax.faces", name="jsf.js", target="HEAD"),
 	 @ResourceDependency(library="js", name="cotentin.js", target="HEAD")})
 public class JerseyRequestBehavior extends AjaxBehavior {
 
 	private static final LoggerFacade logger = LoggerFacade.getLogger(JerseyRequestBehavior.class);
 
 	private static final String SERVICE = "service";
 	private static final String ON_RECEIVED = "onReceived";
 
 	public static final MediaType DEFAULT_TYPE = MediaType.APPLICATION_JSON_TYPE;
 	public static final MediaType DEFAULT_ACCEPT_TYPE = MediaType.APPLICATION_JSON_TYPE;
 	
 	public static final String EMPTY_JSON_OBJ = "{}";
 	public static final String PARAM_NAME_REQOBJ = "JerseyRequest";
 	
 	/**
 	 * <p class="changed_added_2_0">The standard id for this behavior.</p>
 	 */
 	public static final String BEHAVIOR_ID = "nu.cotentin.RequestBehavior";
 
 	/**
 	 * Constructor, logger trace only 
 	 */
 	public JerseyRequestBehavior() {
 		logger.trace("Created new JerseyRequestBehavior-object: "+this);    	
 	}
 
 	/**
 	 * Map containing the variables of the handler for properties saving
 	 */
 	private Map<String, Object> properties;
 
 	private Map<String, ValueExpression> bindings;
 
 	/**
 	 * Renders the script calling the implemented javascript part
 	 * @see javax.faces.component.behavior.ClientBehaviorBase#getScript(javax.faces.component.behavior.ClientBehaviorContext)
 	 */
 	@Override
 	public String getScript(ClientBehaviorContext behaviorContext) {
 		logger.trace("JerseyRequestBehavior#getScript called on context: "+behaviorContext);
 		// Create new cotentin context object
 		CotentinContext ctx = new CotentinContext(behaviorContext.getComponent());
 		ClientScriptBuilder renderer = new ClientScriptBuilder(ctx, this);
 		logger.debug("Created a new cotentin context {0}", ctx);
 		// Replace the behaviorContext by wrapping the original one
 		CotentinClientBehaviorContext newBehaviorContext = new CotentinClientBehaviorContext(behaviorContext);
 		logger.debug("Wrapped original behavior context {0} to {1}", behaviorContext, newBehaviorContext);
 		// Get the behavior script from the ajax behavior
 		String jsfBehaviorScript = super.getScript(newBehaviorContext);
 		logger.debug("Original JSF-Script was evaluated as {0}", jsfBehaviorScript);
 		String cotentinBehaviorScript = renderer.getRequestJavaScript(jsfBehaviorScript, newBehaviorContext);
 		logger.debug("Rerenderered behavior script: {0}", cotentinBehaviorScript);
 		return cotentinBehaviorScript;
 	}	
 
 	/**
 	 * @return javascript code which will be executed, once the request is complete
 	 */
 	public String getOnreceived() {
 		return (String) eval(ON_RECEIVED); 							  
 	}
 
 	/**
 	 * Sets the event handler javascript code, once the request is complete
 	 * @param onReceived the javascript to execute once the response has been received 
 	 */
 	public void setOnreceived(String onReceived) {
 		getState().put(ON_RECEIVED, onReceived);
 		clearInitialState();
 	}
 
 	/**
 	 * @return the method of the request
 	 */
 	public String getService() {
 		return (String) eval(SERVICE);
 	}
 
 	/**
 	 * Sets the service
 	 * @param service the method to use for the request
 	 */
 	public void setService(String service) {		
 		getState().put(SERVICE, service);
 		clearInitialState();
 	}
 
 	/**
 	 * @return the map for the properties, will create a new instance, if the properties is "null" 
 	 */
 	private Map<String, Object> getState() {
 		if (properties == null) {
 			properties = new HashMap<String, Object>();
 		}
 		return properties;
 	}
 
 	private Object eval(String property) {
 		Object value = getState().get(property);
 		if (value != null) {
 			return value;
 		}
 		ValueExpression expression = getValueExpression(property);
 		if (expression != null) {
 			FacesContext ctx = FacesContext.getCurrentInstance();
 			return expression.getValue(ctx.getELContext());
 		}
 		return null;
 	}
 
 	/**
 	 * @see javax.faces.component.behavior.AjaxBehavior#getValueExpression(java.lang.String)
 	 */
 	@Override
 	public ValueExpression getValueExpression(String name) {
 		if (name == null) { throw new NullPointerException("PropertyComputation name cannot be null!"); }
 		return (bindings == null) ? null : bindings.get(name);
 	}
 	
 	/**
 	 * @see javax.faces.component.behavior.AjaxBehavior#setValueExpression(java.lang.String, javax.el.ValueExpression)
 	 */
 	@Override
 	public void setValueExpression(String name, ValueExpression binding) {
 		if (name == null) { throw new NullPointerException("PropertyComputation name cannot be null!"); }
 		if (binding != null) {
 			if (binding.isLiteralText()) { 
 				properties.put(name, binding);
 			} else {
 				if (bindings == null) {
 					bindings = new HashMap<String, ValueExpression>(6,1.0f);
 				}
 				bindings.put(name, binding);
 			}
 		} else {
 			if (bindings != null) {
 				bindings.remove(name);
 				if (bindings.isEmpty()) {
 					bindings = null;
 				}
 			}
 		}
 		clearInitialState();
 	}
 
 	/**
 	 * @see javax.faces.component.behavior.BehaviorBase#saveState(javax.faces.context.FacesContext)
 	 */
 	@Override
 	public Object saveState(FacesContext context) {
 		logger.debug("Saving properties for JerseyRequestBehavior "+this+" in FCSCTX "+context+".");
 		Object[] state = new Object[3];
 		state[0] = super.saveState(context);
 		state[1] = properties;
 		state[2] = bindings;
 		return UIComponentBase.saveAttachedState(context, state);
 	}
 
 	/**
 	 * @see javax.faces.component.behavior.BehaviorBase#restoreState(javax.faces.context.FacesContext, java.lang.Object)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void restoreState(FacesContext context, Object state) {
 		try {
 			logger.debug("Restoring properties for JerseyRequestBehavior "+this+" from FCSCTX "+context+".");
 			Object[] stateArray = (Object[]) UIComponentBase.restoreAttachedState(context, state);
 			bindings = (Map<String, ValueExpression>) stateArray[2];
 			logger.debug("Restored bindings: {0}", bindings);
 			properties = (Map<String, Object>) stateArray[1];
 			logger.debug("Restored properties: {0}", properties);
 			super.restoreState(context, stateArray[0]);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	/**
 	 * @see javax.faces.component.behavior.ClientBehaviorBase#decode(javax.faces.context.FacesContext, javax.faces.component.UIComponent)
 	 */
 	@Override
 	public void decode(FacesContext context, UIComponent component) {
 		try {
 			logger.trace("JerseyRequestBehavior#decode called.");
 			CotentinContext cotentinCtx = new CotentinContext(component);
 			logger.debug("Set up cotentin context {0}", cotentinCtx);
 			ClientScriptBuilder renderer = new ClientScriptBuilder(cotentinCtx, this);
 			Map<String, String> params = context.getExternalContext().getRequestParameterMap();
 			logger.debug("Building request object...");
 			JerseyService service = CotentinServiceRegister.getRegister(context)
 													   .getService(getService());
 			if (service == null) {
 				throw new CotentinException("Cannot find service for id: {0}", getService());
 			}
 			JerseyRequestImpl request = new JerseyRequestImpl();
 			request.setMethod(service.getMethod());
 			request.setRequestURI(getParsedUrl(service.getUrl(), 
 											component.getChildren(), params));
 			request.setConsumeType(DEFAULT_TYPE);
 			request.setUnmarshalTo(service.getReturnType());
 			if (!EMPTY_JSON_OBJ.equals(params.get(PARAM_NAME_REQOBJ))) {
 				request.setConsumeRepresentation(params.get(PARAM_NAME_REQOBJ));
 			}
 			
 			request.setAcceptType(DEFAULT_ACCEPT_TYPE);
 			JerseyResponse response = RequestExecuter.execute(request);
 			String result = response != null ? response.getResponseRepresentation() : null;
 			if (result != null) {
 				cotentinCtx.setResponseJSON(result);
 				cotentinCtx.setResponse(response.getResponseObject());
 			}
 			logger.debug("Evaluation response java script...");
 			String evaluation = renderer.getResponseHandler(result);
 			logger.debug("Evaluated response java script to be: {0}");
 			ServerSideEvaluator.evaluateMappings(cotentinCtx);
 			renderEval(context, evaluation);
 		} catch (Exception ex) {
 			renderEval(context, ClientScriptBuilder.getExceptionScript(this, new CotentinException(ex)));
 		}
 	}
 
 	private String getParsedUrl(String parsableURL, List<UIComponent> children, Map<String, String> params) {
 		logger.debug("Parsing url {0}...", parsableURL);
 		for (UIComponent child : children) {
 			if (child instanceof JerseyPathParameter) {
 				String name = ((JerseyPathParameter) child).getName();
 				parsableURL = parsableURL.replace("{".concat(name).concat("}"), params.get(name)); 
 			}
 		}
 		logger.debug("Parsed url: {0}", parsableURL);
 		return parsableURL;
 	}
 	
 	private void renderEval(FacesContext ctx, String eval) {
 		if (ctx.getPartialViewContext() == null || !ctx.getPartialViewContext().isAjaxRequest()) {
 			return;
 		}
 		try {
 			PartialResponseWriter writer = ctx.getPartialViewContext().getPartialResponseWriter();
 			writer.startDocument();
 			// Render ViewState marker
 			writer.startUpdate(PartialResponseWriter.VIEW_STATE_MARKER);
 			writer.write(ctx.getApplication().getStateManager().getViewState(ctx));
 			writer.endUpdate();
 			// Render eval
 			writer.startEval();
 			writer.write(eval);
 			writer.endEval();
 			writer.endDocument();
 			writer.flush();
 			writer.close();
 		} catch (IOException e) {
 			logger.error("Error while rendering <eval>: {0}", e.getMessage());
 		}
 	}
 
 	/**
 	 * @see javax.faces.component.behavior.AjaxBehavior#getRender()
 	 */
 	@Override
 	public Collection<String> getRender() {
 		return Collections.emptyList();
 	}
 }
