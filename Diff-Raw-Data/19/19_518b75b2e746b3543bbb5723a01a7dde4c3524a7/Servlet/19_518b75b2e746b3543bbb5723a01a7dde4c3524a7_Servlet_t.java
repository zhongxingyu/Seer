 package com.redshape.servlet;
 
 import com.redshape.applications.SpringApplication;
 import com.redshape.servlet.core.controllers.FrontController;
 import org.apache.log4j.Logger;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nikelin
  * Date: 10/10/10
  * Time: 11:09 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Servlet extends HttpServlet {
 	private static final long serialVersionUID = -885951356937232550L;
 
 	public static String CONTEXT_CONFIG_PARAMETER = "contextConfigPath";
 	
     private static final Logger log = Logger.getLogger( Servlet.class );
     private WebApplication application;
 
     private FrontController front;
     private ServletConfig servletConfig;
 
     @Override
     public ServletConfig getServletConfig() {
         return this.servletConfig;
     }
 
     public void setFront( FrontController front ) {
     	this.front = front;
     }
     
     protected FrontController getFront() {
     	return this.front;
     }
 
     public void init(javax.servlet.ServletConfig config) throws javax.servlet.ServletException {
         try {
             this.servletConfig = config;
 
         	String configPath = config.getInitParameter( CONTEXT_CONFIG_PARAMETER );
             if ( configPath == null || configPath.isEmpty() ) {
         		throw new ServletException("Spring context location must be sepecified as <init-param>!");
         	}
 
 			System.setProperty(SpringApplication.SPRING_CONTEXT_PARAM,
 					"/" + config.getServletContext().getRealPath("/") + configPath );
 
             if ( WebApplication.getContext() == null ) {
                 this.application = new WebApplication();
                 log.info( config.getInitParameterNames() );
                 log.info( "Path to config: " + config.getInitParameter("resources-path") );
                 this.application.setEnvArg("dataPath", config.getInitParameter("resources-path") );
                 this.application.start();
             
                 this.setFront( this.application.getContext().getBean( FrontController.class ) );
             } else {
                 log.info("Context already started...");
             }
         } catch ( Throwable e ) {
             log.error( e.getMessage(), e );
             throw new ServletException( e.getMessage(), e );
         }
     }
 
     @Override
     public void service( HttpServletRequest request, HttpServletResponse response ) throws ServletException {
         try {
            if ( this.getFront() == null ) {
                this.init( getServletConfig() );
            }

         	this.getFront().dispatch( this, request, response );
        } catch ( Throwable e ) {
         	log.error( e.getMessage(), e );
             throw new ServletException( e.getMessage(), e );
         }
     }
 
 }
