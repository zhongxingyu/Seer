 package nz.co.searchwellington.controllers.admin;
 
 import java.io.IOException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import nz.co.searchwellington.controllers.BaseMultiActionController;
 import nz.co.searchwellington.controllers.UrlStack;
 import nz.co.searchwellington.model.LinkCheckerQueue;
 import nz.co.searchwellington.model.Resource;
 
 import org.apache.log4j.Logger;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 public class LinkCheckerController extends BaseMultiActionController {
 
     Logger log = Logger.getLogger(LinkCheckerQueue.class);
     
     private LinkCheckerQueue queue;
    private AdminRequestFilter requestFilter;
 
      
    public LinkCheckerController(AdminRequestFilter requestFilter, LinkCheckerQueue queue, UrlStack urlStack) {
         this.requestFilter = requestFilter;
         this.queue = queue;
         this.urlStack = urlStack;        
     }
 
    
     @SuppressWarnings("unchecked")
     public ModelAndView showStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
         ModelAndView mv = new ModelAndView();
                 
         mv.setViewName("linkCheckerShowStatus");          
         mv.getModel().put("queue_length", queue.getSize());     
         return mv;
     }
 
     
     public ModelAndView addToQueue(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ModelAndView mv = new ModelAndView();                
        setRedirect(mv, request);
         requestFilter.loadAttributesOntoRequest(request);         
         if (request.getAttribute("resource") != null) {
             Resource resource = (Resource) request.getAttribute("resource");
             log.info("Adding resource to queue: " + resource.getUrl() + "(" + resource.getId() + ")");
             queue.add(resource.getId()); 
        }
        log.warn("No resource found on request; not adding to queue");
         return mv;
     }
     
     
     
     private void setRedirect(ModelAndView modelAndView, HttpServletRequest request) {
         String url = urlStack.getExitUrlFromStack(request);                
         modelAndView.setView(new RedirectView(url));    
     }
     
 }
