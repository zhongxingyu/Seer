 package org.paxle.gui.impl.servlets;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.velocity.Template;
 import org.apache.velocity.context.Context;
 import org.paxle.core.IMWComponent;
 import org.paxle.core.queue.ICommand;
 import org.paxle.gui.AServlet;
 import org.paxle.gui.impl.ServiceManager;
 
 public class QueueView extends AServlet {
 
     private static final long serialVersionUID = 1L;
     
     protected Log logger = LogFactory.getLog(QueueView.class);
     
     public QueueView(ServiceManager manager) {
         super(manager);
     }
     
     public Template handleRequest( HttpServletRequest request,
                                    HttpServletResponse response,
                                    Context context ) {
         
         Template template = null;
 
         try {
             if (request.getParameter("queue") != null) {
             	String queueName = request.getParameter("queue");
            	Object[] services = this.manager.getServices("org.paxle.core.IMWComponent","(component.ID="+queueName+")");
                 if (services.length == 1 && services[0] instanceof IMWComponent) {
                 	List<ICommand> activeJobs = ((IMWComponent<ICommand>)services[0]).getActiveJobs();
                 	context.put("activeJobs", activeJobs);
                 }
             }
             template = this.getTemplate("/resources/templates/QueueView.vm");
             
         } catch (Exception e) {
             // TODO Auto-generated catch block
             this.logger.error("Error",e);
         }
         return template;
     }
 }
