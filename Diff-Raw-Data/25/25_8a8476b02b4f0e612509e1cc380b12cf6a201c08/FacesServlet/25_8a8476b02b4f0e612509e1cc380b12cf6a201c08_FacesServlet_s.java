 /*
 * $Id: FacesServlet.java,v 1.6 2001/12/06 22:59:16 visvan Exp $
  *
  * Copyright 2000-2001 by Sun Microsystems, Inc.,
  * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
  * All rights reserved.
  *
  * This software is the confidential and proprietary information
  * of Sun Microsystems, Inc. ("Confidential Information").  You
  * shall not disclose such Confidential Information and shall use
  * it only in accordance with the terms of the license agreement
  * you entered into with Sun.
  */
 
 package com.sun.faces.servlet;
 
 import java.io.IOException;
 import javax.faces.Constants;
 import javax.faces.EventQueue;
 import javax.faces.EventQueueFactory;
 import javax.faces.FacesException;
 import javax.faces.ObjectTable;
 import javax.faces.ObjectTableFactory;
 import javax.faces.RenderContext;
 import javax.faces.RenderContextFactory;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpSession;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.faces.RenderKit;
 import javax.faces.FacesEvent;
 import javax.faces.EventDispatcher;
 import javax.faces.EventDispatcherFactory;
 import java.util.EventObject;
 
 import org.mozilla.util.Assert;
 import org.mozilla.util.Debug;
 import org.mozilla.util.Log;
 import org.mozilla.util.ParameterCheck;
 
 
 public class FacesServlet extends HttpServlet {
 
     /**
      * PRECONDITION: Nothing, this is to be called at the very beginning of
      * the Faces app instance.
      *
      * POSTCONDITION: ObjectTable instance created using Factory.  Instance
      * put in ServletContext.  ObjectTable global scope contains
      * RenderContextFactory, EventQueueFactory.
      *
      * @param config The Servlet configuration object which contains
      *        parameters for this servlet.
      *
      * @exception ServletException If an exception occured during the 
      *         creation of the object table factory or object table.
      */
     public void init(ServletConfig config) throws ServletException {
 
         super.init(config);
 	
 	// Uncomment this to cause the program to hang forever, until
 	// you go in the debugger and un-hang it.
 	// com.sun.faces.util.DebugUtil.waitForDebugger();
 
         ObjectTable objectTable;
         ObjectTableFactory otFactory;
         EventQueueFactory eqFactory;
         RenderContextFactory rcFactory;
         EventDispatcherFactory edFactory;
 
         ServletContext servletContext = getServletContext();
         Assert.assert_it(null != servletContext);
 
 	// Step 1: Create the singleton ObjectTable instance and put it
 	// in the servletContext.
 
         objectTable = (ObjectTable) servletContext.getAttribute(
             Constants.REF_OBJECTTABLE);
 	// The objectTable must not exist at this point.  It is an error
 	// if it does exist.
 	Assert.assert_it(null == objectTable);
 
 	// create the ObjectTable
 	try {
 	    otFactory = ObjectTableFactory.newInstance();
 	    objectTable = otFactory.newObjectTable();
 	} catch (FacesException e) {
 	    throw new ServletException(e.getMessage());
 	}
         Assert.assert_it(null != objectTable);
 
         // Store the Object Table in the servlet context 
         // (application scope).
         //
         servletContext.setAttribute(Constants.REF_OBJECTTABLE, objectTable);
 
 	// Step 2: Create the EventQueueFactory and put it in the
 	// ObjectTable in Global Scope.
 
         eqFactory = (EventQueueFactory)objectTable.get(
             Constants.REF_EVENTQUEUEFACTORY);
 
 	// The EventQueueFactory must not exist at this point.  It is an
 	// error if it does exist.
 	Assert.assert_it(null == eqFactory);
 
 	eqFactory = EventQueueFactory.newInstance();
         Assert.assert_it(null != eqFactory);
 	objectTable.put(ObjectTable.GlobalScope,
 			Constants.REF_EVENTQUEUEFACTORY, eqFactory);
 
 	// Step 3: Create the RenderContextFactory and put it in the
 	// ObjectTable in GlobalScope
 
         rcFactory = (RenderContextFactory)objectTable.get(
             Constants.REF_RENDERCONTEXTFACTORY);
 	// The RenderContextFactory must not exist at this point.  It is an
 	// error if it does exist.
 	Assert.assert_it(null == rcFactory);
 
 	rcFactory = RenderContextFactory.newInstance();
         Assert.assert_it(null != rcFactory);
 	objectTable.put(ObjectTable.GlobalScope,
 			Constants.REF_RENDERCONTEXTFACTORY, rcFactory);
 
         // Step 4: Create the EventDispatcherFactory and put it in the
         // ObjectTable in GlobalScope
 
         edFactory = (EventDispatcherFactory)objectTable.get(
             Constants.REF_EVENTDISPATCHERFACTORY);
         // The EventDispatcherFactory must not exist at this point.  It is an
         // error if it does exist.
         Assert.assert_it(null == edFactory);
 
         edFactory = EventDispatcherFactory.newInstance();
         Assert.assert_it(null != edFactory);
         objectTable.put(ObjectTable.GlobalScope,
                         Constants.REF_EVENTDISPATCHERFACTORY, edFactory);
     }
 
     /**
      * Process an HTTP "POST" request.
      *
      * @param request The servlet request we are processing
      * @param response The servlet response we are creating
      *
      * @exception IOException if an input/output error occurs
      * @exception ServletException if a servlet exception occurs
      */
     public void doPost(HttpServletRequest req,
                       HttpServletResponse res)
         throws IOException, ServletException {
         processRequest(req, res);
     }
 
     /**
      * Process an HTTP "GET" request.
      *
      * @param request The servlet request we are processing
      * @param response The servlet response we are creating
      *
      * @exception IOException if an input/output error occurs
      * @exception ServletException if a servlet exception occurs
      */
     public void doGet(HttpServletRequest req,
                       HttpServletResponse res)
         throws IOException, ServletException {
         processRequest(req, res);
     }
 
     /** 
 
     * We override this so we can exit the scope for this request.
 
     * PRECONDITION: init() has been called
 
     * POSTCONDITION: The Scope for this request has been exited.
 
     */ 
     public void service(HttpServletRequest req,
 			HttpServletResponse res) {
 	// We set this attr here so that when ObjectTable.get() comes
 	// around for this request, we pull it out and use it as the
 	// value for the scopeKey.  This is necessary because various
 	// entities modify the HttpServletRequest instance so it's not
 	// suitable for being used as a key
 	req.setAttribute(Constants.REF_REQUESTINSTANCE, req);
 
 	try {
 	    super.service(req, res);
 	}
 	catch (Exception e) {
 	    System.out.println("Caught exception calling super.service: " + 
 			       e.getMessage());
 	} 
 	
 	// exit the scope for this request
 	ObjectTable objectTable;
         objectTable = (ObjectTable) getServletContext().
 	    getAttribute(Constants.REF_OBJECTTABLE);
 	objectTable.exit(req);
     } 
 
 
     /**
      * Process an HTTP request.
      *
      * PRECONDITION: ObjectTable exists in ServletContext.  
      * ObjectTable global scope contains RenderContextFactory, 
      * EventQueueFactory.
      *
      * POSTCONDITION: ObjectTable contains RenderContext and
      * EventQueue instances in session scope.
      *
      * @param request The servlet request we are processing
      * @param response The servlet response we are creating
      *
      * @exception IOException if an input/output error occurs
      * @exception ServletException if a servlet exception occurs
      */
     protected void processRequest(HttpServletRequest req,
                       HttpServletResponse res)
         throws IOException, ServletException {
 
         RenderContext rc;
         RenderContextFactory rcFactory;
         EventQueue eq;
         EventQueueFactory eqFactory;
         EventDispatcherFactory edFactory;
 
 	HttpSession thisSession = req.getSession();
 
         ObjectTable objectTable = (ObjectTable)getServletContext().
             getAttribute(Constants.REF_OBJECTTABLE);
         Assert.assert_it(null != objectTable); 
 
         // Attempt to get a render context from the object table
         // for the current session.  If one doesn't exist, create one.
         //
         rc = (RenderContext)objectTable.get(thisSession, 
 					    Constants.REF_RENDERCONTEXT);
 
         if (rc == null) {
             rcFactory = (RenderContextFactory)
 		objectTable.get(Constants.REF_RENDERCONTEXTFACTORY);
 	    
             Assert.assert_it(null != rcFactory);
             try {
                 rc = rcFactory.newRenderContext(req);
             } catch (FacesException e) {
                 throw new ServletException(e.getMessage());
             }
 	    Assert.assert_it(null != rc); 
             objectTable.put(thisSession, 
 			    Constants.REF_RENDERCONTEXT, rc);
         }
 
         // Attempt to get an event queue from the object table
         // for the current session.  If one doesn't exist, create one.
          
         eq = (EventQueue)objectTable.get(thisSession, 
 					 Constants.REF_EVENTQUEUE);
         if (eq == null) {
             eqFactory = (EventQueueFactory)
 		objectTable.get(Constants.REF_EVENTQUEUEFACTORY);
             Assert.assert_it(null != eqFactory);
             try {
                 eq = eqFactory.newEventQueue();
             } catch (FacesException e) {
                 throw new ServletException(e.getMessage());
             }
 	    Assert.assert_it(null != eq); 
             objectTable.put(thisSession, Constants.REF_EVENTQUEUE, eq);
         }
 
         // If there are no events in the queue, simply forward to the target.
         
         RenderKit rk = rc.getRenderKit();
         Assert.assert_it(rk != null);
 
         rk.queueEvents(req, eq);
 
         if (!eq.isEmpty()) {
            edFactory = (EventDispatcherFactory)
                        objectTable.get(Constants.REF_EVENTDISPATCHERFACTORY);
            Assert.assert_it(edFactory != null );
 
            while (!eq.isEmpty()) {
                EventObject e = (EventObject) eq.getNext();
                try {
                    
                    EventDispatcher d = edFactory.newEventDispatcher(e);
                    Assert.assert_it ( d != null );
 
                    d.dispatch(req, res,e);
                } catch ( FacesException fe ) {
                    throw new ServletException("Error while dispatching events " +
                        fe.getMessage() );
                }    
                eq.remove(e);
            }    
         }
   
         // Get the last portion of the request and use it as an index 
         // into mappings configuration table to get the target url.
         //  
        System.out.println("FORWARDING REQUEST");
        String selectUrl = null;
        String requestInfo = req.getRequestURI();
        int lastPathSeperator = requestInfo.lastIndexOf("/") + 1;
        if (lastPathSeperator != -1) { 
            selectUrl = requestInfo.substring(lastPathSeperator,
            requestInfo.length()); 
        }
 
        //PENDING (rogerk) use selectUrl to determine where to forward
        // (from action mappings table)
         // Use that url as the target.
        String forwardUrl = "/"+selectUrl;
         System.out.println("FORWARDURL:"+ forwardUrl);
         RequestDispatcher reqD = 
                getServletContext().getRequestDispatcher(res.encodeURL(forwardUrl));
         try {
             reqD.forward(req, res); 
         } catch ( IllegalStateException ie ) {
             // PENDING ( visvan ) skip this exception which is occuring
             // because of two forwards one in CommandDispatcher if there
             // was a Command and one in FacesServlet. Revisit later.
         }        
     }    
 
 }
