 package org.paxle.p2p;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
 import org.apache.velocity.tools.view.servlet.VelocityViewServlet;
 
 public class FirewallCheckViewServlet extends VelocityViewServlet {
 	private static final long serialVersionUID = 1L;
 
 	public Template handleRequest( HttpServletRequest request,
 			HttpServletResponse response,
 			Context context ) {
 
 		Template template = null;
    
 		try{
 			template = Velocity.getTemplate("/resources/templates/firewallcheck.vm");
 		} catch( Exception e ) {
 			System.err.println("Exception caught: " + e.getMessage());
 		}
 		//TODO: set not firewalled.
 
 		return template;
 	}
 }
