 /**
  *  Copyright (C) 2008 Progress Software, Inc. All rights reserved.
  *  http://fusesource.com
  *
  *  The software in this package is published under the terms of the AGPL license
  *  a copy of which has been included with this distribution in the license.txt file.
  */
 package org.fusesource.cloudmix.agent.resources;
 
 
 import java.io.InputStream;
 import java.net.URI;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.UriInfo;
 
 import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.annotation.PostConstruct;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.fusesource.cloudmix.agent.EndpointRegistry;
 
 import org.fusesource.cloudmix.agent.RestGridClient;
 import org.fusesource.cloudmix.agent.dir.DirectoryInstallerAgent;
 import org.fusesource.cloudmix.agent.webapp.GridAgentWebapp;
 import org.fusesource.cloudmix.common.dto.AgentDetails;
 
 import com.sun.jersey.spi.resource.Singleton;
 import com.sun.jersey.spi.inject.Inject;
 
 @Path("/agent")
 @Singleton
 public class AgentResource implements LifecycleObserver {
     private static final transient Log LOG = LogFactory.getLog(AgentResource.class);
 
     @Inject
     private GridAgentWebapp webapp;
     @Inject
     private EndpointRegistry endpointRegistry;
     
     @Context
     private UriInfo uriInfo;
     
     @Context
     private ServletConfig config;
 
     /**
      * Add an endpoint to the agent details.
      *
      * @param id the endpoint key
      * @param ref the endpoint reference
      * @return a response with appropriate status
      */
     @PUT
     @Path("endpoint/{id}")
     @Consumes("application/xml")
     public Response addEndpoint(@PathParam("id")String id,
                                 W3CEndpointReference ref) {
         endpointRegistry.addEndpoint(id, ref);
         URI uri = uriInfo.getAbsolutePath();
         return Response.created(uri).build();       
     }
     
     /**
      * Remove an endpoint from the agent details.
      *
      * @param id the endpoint key
      * @return a response with appropriate status
      */
     @DELETE
     @Path("endpoint/{id}")
     public Response removeEndpoint(@PathParam("id")String id) {
         LOG.info("removing endpoint: " + id);
         boolean exists = endpointRegistry.removeEndpoint(id);
         return exists ? Response.ok().build() : Response.status(404).build(); 
     }
 
     /**
      * Retrieve the agent status.
      *
      * @return an appropriate status summary in HTML
      */    
     @GET
     @Path("status")
     @Produces("text/html")
     public String getStatus() {
         return webapp.getStatus();
     }
     
     /**
      * Retrieve an image from the agent webapp.
      *
      * @return a response encapculating an image stream
      */  
     @GET
     @Path("images/{image}")
     @Produces("image/gif")
     public Response getImage(@PathParam("image")String image) {
         String res = GridAgentWebapp.IMAGES_ROOT + image;
         InputStream is = config.getServletContext().getResourceAsStream(res);
         return is != null 
                ? Response.ok().entity(is).build() 
                : Response.status(404).build(); 
     }
     
     /**
      * Retrieve the sytle-sheet for the agent webapp.
      *
      * @return a response encapculating the stylesheet
      */ 
     @GET
     @Path(GridAgentWebapp.STYLESHEET_HREF)
     @Produces("text/xml")
     public Response getStyleSheet() {
         String res = GridAgentWebapp.STYLESHEET_HREF;
         InputStream is = config.getServletContext().getResourceAsStream(res);
         return Response.ok().entity(is).build();
     }
 
     /**
      * Injection setter for grid agent webapp
      *
      * @param webapp grid agent webapp
      */
     public void setGridAgentWebapp(GridAgentWebapp webapp) {
         this.webapp = webapp;
     }
 
     /**
      * Injection setter for the endpoint registry
      *
      * @param endpointRegistry the endpoint registry
      */
     public void setEndpointRegistry(EndpointRegistry endpointRegistry) {
         this.endpointRegistry = endpointRegistry;
     }
 
     /**
      * Setter for the URI info
      *
      * @param uriInfo the URI info
      */
     public void setUriInfo(UriInfo uriInfo) {
         this.uriInfo = uriInfo;
     }
 
     /**
      * Setter for servlet config
      *
      * @param config servlet config
      */
     public void setConfig(ServletConfig config) {
         this.config = config;
     }
 
    @PostConstruct
     public void init() {
         try {
             webapp.init(config);
         } catch (ServletException se) {
             LOG.warn("webapp init failed", se);
         }
     }
 
     /**
      * Work-around for @PreDestroy annotation seemingly not being honoured.
      */
     public void destroy() {
         webapp.destroy();
     }
 
     /**
      * @return agent
      */
     private final DirectoryInstallerAgent getAgent() {
         return webapp.getAgent();
     }
     
     /**
      * @return agent details
      */
     private final AgentDetails getAgentDetails() {
         return webapp.getAgent().getAgentDetails();
     }
     
     /**
      * @return grid client
      */
     private final RestGridClient getClient() {
        return webapp.getClient();
     }
 }
     
