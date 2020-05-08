 /*
  * Created on Oct 20, 2004
  */
 package uk.org.ponder.rsf.view;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReceiver;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
 import uk.org.ponder.rsf.viewstate.ViewParamsReceiver;
 import uk.org.ponder.rsf.viewstate.ViewParamsReporter;
 import uk.org.ponder.util.UniversalRuntimeException;
 
 /**
  * A concrete implementation of ViewResolver which will resolve
  * ComponentProducer requests into a fixed collection of configured beans, set
  * up by the setViews() method. Can also fall back to a set of generic
  * ViewResolvers should initial lookup fail. This is the default RSF
  * ViewResolver - it is an application scope bean, although it collaborates
  * with the AutoComponentProducerManager to accept request-scope producers.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class ConcreteViewResolver implements ViewResolver {
   // This key is used to identify a producer that *might* produce components
   // in all views. Upgrade this architecture once we think a little more about
   // how we might actually want to locate view templates and component
   // producers relative to views (view IDs or in general, ViewParams)
   public static final String ALL_VIEW_PRODUCER = "  all views  ";
 
   private Map views = new HashMap();
 
   private List resolvers = new ArrayList();
  private boolean unknowniserror = false;
   private ViewParamsReceiver vpreceiver;
   private NavigationCaseReceiver ncreceiver;
   private AutoComponentProducerManager automanager;
 
   public void setUnknownViewIsError(boolean unknowniserror) {
     this.unknowniserror = unknowniserror;
   }
 
   public void setViewParametersReceiver(ViewParamsReceiver vpreceiver) {
     this.vpreceiver = vpreceiver;
   }
   
   public void setNavigationCaseReceiver(NavigationCaseReceiver ncreceiver) {
     this.ncreceiver = ncreceiver;
   }
   
   private List pendingviews = new ArrayList();
 // Apologies for this lack of abstraction. There is currently only one of these,
 // and we do use it for two purposes...
   public void setAutoComponentProducerManager(AutoComponentProducerManager requestmanager) {
     this.automanager = requestmanager;
     pendingviews.addAll(requestmanager.getProducers());
   }
   
   /**
    * Sets a static list of ViewComponentProducers which will be used as a first
    * pass to resolve requests for incoming views. Any plain ComponentProducers
    * will be added as default producers to execute for all views.
    */
   public void setViews(List viewlist) {
     pendingviews.addAll(viewlist);
   }
   
   public void init() {
     for (int i = 0; i < pendingviews.size(); ++ i) {
       ComponentProducer view = (ComponentProducer) pendingviews.get(i);
       // view.setMessageLocator(messagelocator);
       String key = ALL_VIEW_PRODUCER;
       if (view instanceof ViewComponentProducer) {
         key = ((ViewComponentProducer) view).getViewID();
         if (view instanceof ViewParamsReporter) {
           ViewParamsReporter vpreporter = (ViewParamsReporter) view;
           vpreceiver.setViewParamsExemplar(key, vpreporter.getViewParameters());
         }
         if (view instanceof DefaultView) {
           vpreceiver.setDefaultView(key);
         }
         if (view instanceof NavigationCaseReporter) {
           ncreceiver.receiveNavigationCases(key, ((NavigationCaseReporter)view).reportNavigationCases());
         }
       }
       addView(key, view);
     }
     pendingviews.clear();
   }
   /**
    * Sets a list of slave ViewResolvers which will be polled in sequence should
    * no static producer be registered, until the first which returns a non-null
    * result for this view.
    */
   public void setViewResolvers(List resolvers) {
     this.resolvers = resolvers;
   }
 
   private void addView(String key, ComponentProducer view) {
     List got = get(key);
     if (got == null) {
       got = new ArrayList();
       views.put(key, got);
     }
     got.add(view);
   }
 
   private List get(String key) {
     return (List) views.get(key);
   }
 
   public List getProducers(String viewid) {
     List specific = get(viewid);
 
     if (specific == null && resolvers != null) {
       for (int i = 0; i < resolvers.size(); ++i) {
         ViewResolver resolver = (ViewResolver) resolvers.get(i);
         specific = resolver.getProducers(viewid);
         if (specific != null)
           break;
       }
     }
     if (specific == null && unknowniserror) {
       throw UniversalRuntimeException.accumulate(new ViewNotFoundException(),
           "Unable to resolve request for component tree for view " + viewid);
     }
     ArrayList togo = new ArrayList();
     List allproducers = get(ALL_VIEW_PRODUCER);
     if (allproducers != null) {
       togo.addAll(allproducers);
     }
     if (specific != null) {
       togo.addAll(specific);
     }
     mapProducers(togo);
     return togo;
   }
 
   private void mapProducers(List producers) {
     for (int i = 0; i < producers.size(); ++ i) {
       ComponentProducer producer = (ComponentProducer) producers.get(i);
       producers.set(i, automanager.wrapProducer(producer));
     }
   }
 
 }
