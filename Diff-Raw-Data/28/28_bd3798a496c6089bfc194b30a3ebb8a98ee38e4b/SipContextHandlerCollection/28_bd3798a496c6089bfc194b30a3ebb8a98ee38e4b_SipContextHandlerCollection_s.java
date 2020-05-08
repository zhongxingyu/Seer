 // ========================================================================
 // Copyright 2008-2009 NEXCOM Systems
 // ------------------------------------------------------------------------
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at 
 // http://www.apache.org/licenses/LICENSE-2.0
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 // ========================================================================
 
 package org.cipango.server.handler;
 
 import java.io.IOException;
 import java.lang.reflect.Method;
 
 import javax.servlet.ServletException;
 import javax.servlet.sip.Address;
 import javax.servlet.sip.ServletParseException;
 import javax.servlet.sip.SipServletMessage;
 import javax.servlet.sip.SipServletResponse;
 import javax.servlet.sip.SipURI;
 import javax.servlet.sip.ar.SipApplicationRouterInfo;
 import javax.servlet.sip.ar.SipApplicationRoutingDirective;
 import javax.servlet.sip.ar.SipRouteModifier;
 
 import org.cipango.server.ConnectorManager;
 import org.cipango.server.ID;
 import org.cipango.server.Server;
 import org.cipango.server.SipConnector;
 import org.cipango.server.SipHandler;
 import org.cipango.server.SipMessage;
 import org.cipango.server.SipRequest;
 import org.cipango.server.SipResponse;
 import org.cipango.server.ar.RouterInfoUtil;
 import org.cipango.server.session.CallSessionHandler;
 import org.cipango.server.session.SipSessionHandler;
 import org.cipango.server.transaction.TransactionManager;
 import org.cipango.sip.NameAddr;
 import org.cipango.sip.SipParams;
 import org.cipango.sip.SipURIImpl;
 import org.cipango.sip.URIFactory;
 import org.cipango.sipapp.SipAppContext;
 import org.cipango.util.ExceptionUtil;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.handler.ContextHandlerCollection;
 import org.eclipse.jetty.util.LazyList;
 import org.eclipse.jetty.util.component.LifeCycle;
 import org.eclipse.jetty.util.log.Log;
 
 /**
  * Handler responsible for application selection based on application router result. In addition, it also 
  * performs session key targeting when needed. 
  * 
  * It extends the HTTP {@link ContextHandlerCollection}. However, contrary to HTTP, the selected handler 
  * is not invoked directly for processing since several handlers necessary to SIP Servlets are invoked first. 
  * Therefore, selected application handler is stored in the request for use at a later stage. 
  */
 public class SipContextHandlerCollection extends ContextHandlerCollection implements SipHandler
 {  
     private SipAppContext[] _sipContexts;
     private SipHandler _handler;
     
 	public SipContextHandlerCollection() 
     {
 		super();
 		setContextClass(SipAppContext.class);
		
		CallSessionHandler callSessionHandler = new CallSessionHandler();
		TransactionManager transactionHandler = new TransactionManager();
		SipSessionHandler sipSessionHandler = new SipSessionHandler();
		
		callSessionHandler.setHandler(transactionHandler);
		transactionHandler.setHandler(sipSessionHandler);
		
		_handler = callSessionHandler;
 	}
 	
 	@Override
 	protected void doStart() throws Exception
 	{
 		if (_handler instanceof LifeCycle)
 			((LifeCycle) _handler).start();
 		super.doStart();
 	}
 	
	@Override
	public void setServer(org.eclipse.jetty.server.Server server)
	{
		_handler.setServer(server);
		super.setServer(server);
	}
	
 	public SipAppContext[] getSipContexts()
 	{
 		return _sipContexts;
 	}
 	
 	public SipAppContext getContext(String name)
 	{
 		if (_sipContexts != null)
 		{
 			for (int i = 0; i < _sipContexts.length; i++)
 			{
 				if (_sipContexts[i].getName().equals(name))	
 					return _sipContexts[i];
 			}
 		}
 		return null;
 	}
 	
 	@Override
     public void setHandlers(Handler[] handlers)
     {
     	super.setHandlers(handlers);
         
         Object sipHandlers = null;
         if (handlers != null)
         {
             for (int i = 0; i < handlers.length; i++)
             {
                 if (handlers[i] instanceof SipAppContext)
                     sipHandlers = LazyList.add(sipHandlers, handlers[i]);
             }
         }
         _sipContexts = (SipAppContext[]) LazyList.toArray(sipHandlers, SipAppContext.class);
     }
 
 	public ConnectorManager getConnectorManager()
 	{
 		return ((Server) getServer()).getConnectorManager();
 	}
 	
 	private boolean isInitial(SipRequest request)
 	{
 		return ((request.getTo().getParameter(SipParams.TAG) == null) && !request.isCancel());
 	}
 	
 	public Address popLocalRoute(SipRequest request)
 	{
 		Address route = request.getTopRoute();
 		if (route != null && getConnectorManager().isLocalUri(route.getURI()))
 		{
 			request.removeTopRoute();
 			return route;
 		}
 		return null;
 	}
 	
 	public void handle(SipServletMessage message) throws ServletException, IOException 
     {
 		if (((SipMessage) message).isRequest())
 		{
 			SipRequest request = (SipRequest) message;
 		
 			Address route = popLocalRoute(request); 
 			
 			if (isInitial(request))
 	        {
 				request.setInitial(true);
 				
 				SipApplicationRouterInfo routerInfo = null;
 				SipAppContext appContext = null;
 				
 				try
 				{
 					if (route != null)
 					{
 						SipURI uri = (SipURI) route.getURI();
 						if (RouterInfoUtil.ROUTER_INFO.equals(uri.getUser()))
 						{
 							routerInfo = RouterInfoUtil.decode(uri);
 							route = popLocalRoute(request);
 						}
 						if (route != null)
 							request.setPoppedRoute(route);
 					}
 					
 					if (routerInfo == null)
 					{
 						routerInfo = ((Server) getServer()).getApplicationRouter().getNextApplication(
 							request, null, SipApplicationRoutingDirective.NEW, null, null);
 					}
 				}
 				catch (Throwable t) 
 				{
 					if (!request.isAck())
 					{
 						SipResponse response = new SipResponse(
 								request,
 								SipServletResponse.SC_SERVER_INTERNAL_ERROR,
 								"Application router error: " + t.getMessage());
 						ExceptionUtil.fillStackTrace(response, t);
 						getConnectorManager().sendResponse(response);
 					}
 					return;
 				}
 				
 				if (routerInfo != null && routerInfo.getNextApplicationName() != null)
 				{
 					boolean handle = handlingRoute(request, routerInfo);
 					if (handle)
 						return;
 					
 					request.setStateInfo(routerInfo.getStateInfo());
 					request.setRegion(routerInfo.getRoutingRegion());
 					
 					String s = routerInfo.getSubscriberURI();
 					if (s != null)
 					{
 						try
 						{
 							request.setSubscriberURI(URIFactory.parseURI(s));
 						}
 						catch (ServletParseException e)
 						{
 							Log.debug(e);
 						}
 					}
 					
 					String applicationName = routerInfo.getNextApplicationName();
 					appContext = (SipAppContext) getContext(applicationName);
 										
 					Method method = appContext == null ? null : appContext.getSipApplicationKeyMethod();
 					if (method != null)
 					{
 						try
 						{
 							String sessionKey = (String) method.invoke(null, request);
 							
 							if (Log.isDebugEnabled())
 								Log.debug("routing initial request to key {}", sessionKey);
 							
 							request.addHandlerAttribute(ID.SESSION_KEY_ATTRIBUTE, sessionKey);
 						}
 						catch (Exception e)
 						{
 							Log.debug("failed to get SipApplicationKey", e);
 						}
 					}
 					
 					if (Log.isDebugEnabled())
 						Log.debug("application router returned application {} for initial request {}", applicationName, request.getMethod());
 				}
 				
 				if (appContext == null)
 				{
 					if (!request.isAck())
 					{
 						SipResponse response = new SipResponse(request, SipServletResponse.SC_NOT_FOUND, null);
 						response.to().setParameter(SipParams.TAG, ID.newTag());
 						getConnectorManager().sendResponse(response);
 					}
 					return;
 				}			
 				request.addHandlerAttribute(ID.CONTEXT_ATTRIBUTE, appContext);
 			}
 			else
 			{
 				if (route != null)
 					request.setPoppedRoute(route);
 			}
 		}		
 		_handler.handle(message);
     }
 	
 	private boolean handlingRoute(SipRequest request, SipApplicationRouterInfo routerInfo)
 	{
 		if (routerInfo.getRouteModifier() == null || SipRouteModifier.NO_ROUTE == routerInfo.getRouteModifier())
 			return false;
 		
 		String[] routes = routerInfo.getRoutes();
 		try
 		{
 			if (SipRouteModifier.ROUTE == routerInfo.getRouteModifier() && routes != null)
 			{
 				Address topRoute = new NameAddr(routes[0]);
 				if (getConnectorManager().isLocalUri(topRoute.getURI()))
 					request.setPoppedRoute(topRoute);
 				else
 				{
 					for (int i = routes.length; i >= 0; --i)
 						request.pushRoute(new NameAddr(routes[i]));
 					request.send();
 					return true;
 				}
 			}
 			else if (SipRouteModifier.ROUTE_BACK == routerInfo.getRouteModifier() && routes != null)
 			{
 				SipConnector defaultConnector = getConnectorManager().getDefaultConnector();
     			SipURI ownRoute = new SipURIImpl(null, defaultConnector.getHost(), defaultConnector.getPort());
     			RouterInfoUtil.encode(ownRoute, routerInfo);
 
     			ownRoute.setLrParam(true);
 				request.pushRoute(ownRoute);
 				for (int i = routes.length; i >= 0; --i)
 					request.pushRoute(new NameAddr(routes[i]));
 				request.send();
 				return true;
 			} 
 			else if (routes == null 
 					&& (SipRouteModifier.ROUTE_BACK == routerInfo.getRouteModifier() || SipRouteModifier.ROUTE == routerInfo.getRouteModifier()))
 			{
 				Log.debug("Router info set route modifier to {} but no route provided, assume NO_ROUTE", routerInfo.getRouteModifier());
 			}
 			return false;
 		}
 		catch (Exception e)
 		{
 			if (!request.isAck())
 			{
 				// Could have ServletParseException or IllegalArgumentException on pushRoute
 				SipResponse response = (SipResponse) request.createResponse(
 	        			SipServletResponse.SC_SERVER_INTERNAL_ERROR,
 	        			"Error in handler: " + e.getMessage());
 				ExceptionUtil.fillStackTrace(response, e);
 				try { getConnectorManager().sendResponse(response); } catch (Exception e1) {Log.ignore(e1); }
 			}
         	return true;
 		}
 	}
 }
