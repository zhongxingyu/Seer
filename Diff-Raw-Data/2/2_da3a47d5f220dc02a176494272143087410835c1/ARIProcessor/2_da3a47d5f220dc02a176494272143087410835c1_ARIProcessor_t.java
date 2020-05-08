 /*
  * Created on 5 Feb 2007
  */
 package uk.org.ponder.rsf.flow;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import uk.org.ponder.rsf.view.MappingViewResolver;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 
 /** Coordinates discovery of other ActionResultInterceptors from round the
  * context. A particular function is to locate "local ARIs" that are registered
  * as ViewComponentProducers just for the current view.
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  *
  */
 
 public class ARIProcessor implements ActionResultInterceptor {
   private List interceptors;
   private MappingViewResolver mappingViewResolver;
   
   public void setInterceptors(List interceptors) {
     this.interceptors = interceptors;
   }  
 
   public void setMappingViewResolver(MappingViewResolver mappingViewResolver) {
     this.mappingViewResolver = mappingViewResolver;
   }
   
   public void interceptActionResult(ARIResult ariresult, ViewParameters incoming, 
       Object result) {
     if (interceptors == null) {
       interceptors = new ArrayList();
     }
     ArrayList requestinterceptors = new ArrayList(interceptors);
     
     accreteViewARIs(requestinterceptors, incoming);
     for (int i = 0; i < requestinterceptors.size(); ++ i) {
      ActionResultInterceptor ari = (ActionResultInterceptor) requestinterceptors.get(i);
       ari.interceptActionResult(ariresult, incoming, result);
     }
   }
 
   private void accreteViewARIs(List interceptors, ViewParameters incoming) {
     List producers = mappingViewResolver.getProducers(incoming.viewID);
     for (int i = 0; i < producers.size(); ++ i) {
       Object producer = producers.get(i);
       if (producer instanceof ActionResultInterceptor) {
         interceptors.add(mappingViewResolver.mapProducer(producer));
       }
     }
   }
 
 
 }
