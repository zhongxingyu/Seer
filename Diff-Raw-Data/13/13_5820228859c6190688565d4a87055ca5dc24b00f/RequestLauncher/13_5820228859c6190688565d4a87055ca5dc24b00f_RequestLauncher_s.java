 /*
  * Created on 8 Jan 2008
  */
 package uk.org.ponder.rsf.bare;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.springframework.context.ApplicationContext;
 
 import uk.org.ponder.arrayutil.ArrayUtil;
 import uk.org.ponder.beanutil.WriteableBeanLocator;
 import uk.org.ponder.beanutil.support.ConcreteWBL;
 import uk.org.ponder.rsac.RSACBeanLocator;
 import uk.org.ponder.rsf.bare.junit.PlainRSFTests;
 import uk.org.ponder.rsf.componentprocessor.BindingFixer;
 import uk.org.ponder.rsf.componentprocessor.ComponentChildIterator;
 import uk.org.ponder.rsf.componentprocessor.ValueFixer;
 import uk.org.ponder.rsf.components.UIBound;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIComponent;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIParameter;
 import uk.org.ponder.rsf.components.UIParameterHolder;
 import uk.org.ponder.rsf.flow.ARIResult;
 import uk.org.ponder.rsf.processor.support.RSFActionHandler;
 import uk.org.ponder.rsf.renderer.ViewRender;
 import uk.org.ponder.rsf.request.EarlyRequestParser;
 import uk.org.ponder.rsf.util.RSFUtil;
 import uk.org.ponder.rsf.view.ViewRoot;
 import uk.org.ponder.rsf.viewstate.RawURLState;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParamsCodec;
 import uk.org.ponder.saxalizer.SAXalizerMappingContext;
 import uk.org.ponder.stringutil.StringGetter;
 
 /**
  * A point of control for a "standard" RSF request cycle in a test environment,
  * which allows easy control over the apparently submitted bindings and URL
  * information. These are dispensed from the {@link PlainRSFTests} class which
  * is assumed to be the base class for the test class being written. Each
  * RequestLauncher object corresponds to a single RSAC cycle.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  */
 
 public class RequestLauncher implements EarlyRequestParser {
 
   public static final String TEST_VIEW = "test";
 
   private Map multipartMap;
   private String[] pathInfo;
   private Map requestMap;
   private String requestType;
   private RSACBeanLocator rsacbl;
   private ApplicationContext context;
   private SAXalizerMappingContext smc;
 
   private boolean singleshot;
 
   public Map getMultipartMap() {
     return multipartMap;
   }
 
   public void setMultipartMap(Map multipartMap) {
     this.multipartMap = multipartMap;
   }
 
   public String[] getPathInfo() {
     return pathInfo;
   }
 
   public void setPathInfo(String[] pathInfo) {
     this.pathInfo = pathInfo;
   }
 
   public Map getRequestMap() {
     return requestMap;
   }
 
   public void setRequestMap(Map requestMap) {
     this.requestMap = requestMap;
   }
 
   public String getRequestType() {
     return requestType;
   }
 
   public void setRequestType(String requestType) {
     this.requestType = requestType;
   }
 
   public String getEnvironmentType() {
     return EarlyRequestParser.TEST_ENVIRONMENT;
   }
   
   public void setParameter(String key, String[] value) {
     requestMap.put(key, value);
   }
   
   public void addParameter(String key, String value) {
     String[] values = (String[]) requestMap.get(key);
     if (values == null) {
       requestMap.put(key, new String[] { value });
     }
     else {
       requestMap.put(key, ArrayUtil.append(values, value));
     }
   }
   
   private boolean isFixed(UIComponent component) {
     ViewRoot root = RSFUtil.findViewRoot(component);
     return root == null? false : root.isFixed;
   }
   
   private void processAndAccrete(UIParameterHolder form, UICommand command) {
     boolean isFixed = isFixed(form);
     BindingFixer fixer = (BindingFixer) context.getBean("bindingFixer");
     ValueFixer valueFixer = (ValueFixer) getRSACBeanLocator().getBeanLocator().locateBean("valueFixer");
 
     ComponentChildIterator cci = new ComponentChildIterator(form, smc);
     while (cci.hasMoreComponents()) {
       UIComponent component = cci.nextComponent();
       if (component instanceof UIParameterHolder) {
         UIParameterHolder holder = (UIParameterHolder) component;
         fixer.processComponent(holder);
         
         if (!(component instanceof UICommand) || component == command) {
           for (int i = 0; i < holder.parameters.size(); ++i) {
             UIParameter param = holder.parameters.parameterAt(i);
             addParameter(param.name, param.value);
           }          
         }
       }
       if (component instanceof UIBound) {
         UIBound bound = (UIBound) component;
         if (!isFixed) {
           valueFixer.processComponent(bound);
         }
         if (bound.willinput) {
           Object value = bound.acquireValue();
           if (value instanceof String[]) {
             setParameter(bound.submittingname, (String[]) value);
           }
           else {
             addParameter(bound.submittingname, value.toString());
           }
           addParameter(bound.fossilizedbinding.name, bound.fossilizedbinding.value);
           if (bound.fossilizedshaper != null) {
             addParameter(bound.fossilizedshaper.name, bound.fossilizedshaper.value);
           }
         }
       }
     }
   }
 
   /** Perform a rendering of the (default) test view **/
   public RenderResponse renderView() {
     return renderView(new SimpleViewParameters(TEST_VIEW));
   }
   
   private void updateViewParameters(ViewParameters viewparams) {
     ViewParamsCodec vpcodec = (ViewParamsCodec) context.getBean("viewParamsMapper");
     RawURLState rus = vpcodec.renderViewParams(viewparams);
     pathInfo = rus.pathinfo;
     requestMap = rus.params;
     //context.set("parsedViewParameters", torender);
   }
   
   /** Perform a rendering of the specified view 
    * @param torender The ViewParameters specifying the view to be rendered.
    * @return A {@link RenderResponse} record holding a summary of the final
    * context state after action processing.
    * **/
   public RenderResponse renderView(ViewParameters torender) {
     RenderResponse togo = new RenderResponse();
     try {
       WriteableBeanLocator context = rsacbl.getBeanLocator();
       
       updateViewParameters(torender);
       context.locateBean("rootHandlerBean");
       
       togo.requestContext = rsacbl.getDeadBeanLocator();
       ViewRender viewRender = (ViewRender) togo.requestContext.locateBean("viewRender");
       if (viewRender != null && viewRender.getView() != null) {
         togo.viewWrapper = new ViewWrapper(viewRender.getView().viewroot, smc);
       }
       BareRootHandlerBean brhb = (BareRootHandlerBean) context.locateBean("rootHandlerBean");
       
       StringGetter response = (StringGetter) context.locateBean("servletResponseFactory");
       String directMarkup = response.get();
       if (directMarkup.length() == 0) {
         togo.markup = brhb.getMarkup();
       }
       else {
         togo.markup = directMarkup;
       }
       togo.redirect = brhb.getRedirectTarget();      
     }
     finally {
       if (!singleshot) {
         rsacbl.endRequest();
       }
     }
     return togo;
     
   }
   
   /** @see #submitForm(ViewParameters, UIForm, UICommand)  
    * The form is assumed to submit from the default test view */
   public ActionResponse submitForm(UIForm form, UICommand command) {
     return submitForm(null, form, command);
   }
   
   /**
    * Submit the supplied form, as if via the specified command, returning the
    * "final state" of the request context at cycle end (note that any disposable
    * beans will at this point be in the disposed condition).
    * 
    * @param location The view from which the submitting form is to be submitted.
    * @param form A form to be submitted to form this test cycle's input.
    * @param command The form command which it is to appear the form was
    *            submitted by. This may be <code>null</code>
    * @return An {@link ActionResponse} record holding a summary of the final
    * context state after action processing.
    */
   public ActionResponse submitForm(ViewParameters location, UIForm form, UICommand command) {
     WriteableBeanLocator context = rsacbl.getBeanLocator();
     if (location != null) {
       updateViewParameters(location);
     }
     processAndAccrete(form, command);
     
     setRequestType(EarlyRequestParser.ACTION_REQUEST);
 
     ActionResponse togo = new ActionResponse();
 
     try {
 
       context.locateBean("rootHandlerBean");
       togo.requestContext = rsacbl.getDeadBeanLocator();
       togo.ARIResult = (ARIResult) rsacbl.getBeanLocator().locateBean("ARIResultConcrete");
       RSFActionHandler actionHandler = (RSFActionHandler) rsacbl.getBeanLocator().locateBean("actionHandler");
       togo.actionResult = actionHandler.getActionResult();
     }
     finally {
       if (!singleshot) {
         ConcreteWBL saved = new ConcreteWBL();
         for (Iterator names = togo.requestContext.iterator(); names.hasNext();) {
           String name = (String) names.next();
           saved.set(name, togo.requestContext.locateBean(name));
         }
         // this will trash all request beans
         rsacbl.endRequest();
         togo.requestContext = saved;
       }
     }
     return togo;
   }
 
   public RSACBeanLocator getRSACBeanLocator() {
     return rsacbl;
   }
 
   public RequestLauncher(ApplicationContext context, RSACBeanLocator rsacbl,
       boolean singleshot) {
     this.rsacbl = rsacbl;
     this.context = context;
     this.singleshot = singleshot;
     multipartMap = new HashMap();
     pathInfo = new String[] {TEST_VIEW};
     requestMap = new HashMap();
     requestType = EarlyRequestParser.RENDER_REQUEST;
     smc = (SAXalizerMappingContext) context.getBean("ELMappingContext");
   }
 }
