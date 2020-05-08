 /*
  * Copyright 2007 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License")
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.sf.gilead.comet;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import net.sf.gilead.annotations.Comet;
 import net.sf.gilead.exception.CometException;
 
 import org.apache.catalina.CometEvent;
 import org.apache.catalina.CometProcessor;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.google.gwt.user.client.rpc.SerializationException;
 import com.google.gwt.user.server.rpc.RPC;
 import com.google.gwt.user.server.rpc.RPCRequest;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 /**
  * Abstract class for GWT remote service using tomcat CometProcessor. Note that,
  * as I use Session to identify pending request, all clients have to enable
  * cookies.
  * 
  * @author vincent legendre
  */
 public abstract class CometRemoteService extends RemoteServiceServlet 
 										 implements CometProcessor
 {
 	//----
 	// Constant
 	//----
 	/**
 	 * Name of the pending request HTTP session attribute
 	 */
 	private static final String PENDING_REQUEST_ATTRIBUTE = "c4gwt_pending_request";
 	
 	// ----
 	// Attributes
 	// ----
 	/**
 	 * Serialization ID
 	 */
 	private static final long serialVersionUID = -2812019173043540023L;
 	
 	/**
 	 * Log channel
 	 */
 	private static Log _log = LogFactory.getLog(CometRemoteService.class);
 
 	/**
 	 * list of Comet annotated methods.
 	 */
 	protected Map<String, Comet> m_comets = new HashMap<String, Comet>();
 
 	/**
 	 * List of all pending requests
 	 */
 	private static Set<PendingRequest> s_pendingRequests = new HashSet<PendingRequest>();
 
 	// -------------------------------------------------------------------------
 	//
 	// Constructor / init
 	//
 	// -------------------------------------------------------------------------
 	/**
 	 * Static constructor
 	 */
 	static 
 	{
 		new CleanPendingSessionIdThread().start();
 	}
 	
 	/**
 	 * Empty constructor
 	 */
 	public CometRemoteService()
 	{
 		super();
 	}
 
 	/**
 	 * Servlet initialisation
 	 */
 	public void init() throws ServletException
 	{
 		super.init();
 
 		// read @Comet annotation
 		Class<?> classe = this.getClass();
 		for (Method method : classe.getMethods())
 		{
 			Comet comet = method.getAnnotation(Comet.class);
 			if (comet != null)
 			{
 				m_comets.put(method.getName(), comet);
 			}
 		}
 	}
 
 	// -------------------------------------------------------------------------
 	// to be overloaded
 	// -------------------------------------------------------------------------
 	/**
 	 * @return true if answer associated to this request have data to be send.
 	 *         if false is returned, the request will be queued for a later
 	 *         call.
 	 */
 	protected boolean haveData(RPCRequest p_request)
 	{
 		return true;
 	}
 
 	/**
 	 * This method is called when a request is aborted by client. ie when client
 	 * leave current page. You can't write into response, but you can broadcast
 	 * event on all pending request as p_abortedRequest is already closed note
 	 * that p_abortedRequest.event == null
 	 */
 	protected void onCurrentRequestAbort(PendingRequest p_abortedRequest) 
 	{
 		// No default behavior
 	}
 
 	// -------------------------------------------------------------------------
 	// API used by sub class
 	// -------------------------------------------------------------------------
 	/**
 	 * 
 	 */
 	protected void setCurrentPendingRequest(PendingRequest p_request)
 	{
 		
 		PendingRequest current = getCurrentPendingRequest();
 		if (current != null)
 		{
 			// it shouldn't occur, because it's means that same session (ie
 			// client)
 			// have two pending request...
 			// to avoid problem, we close one by calling his service.
 			try 
 			{
 				invokeService(current);
 			} 
 			catch (IOException e)
 			{
 				throw new CometException("Error during service invocation : " + e.getMessage(), e);
 			}
 			current.event = null;
 			synchronized (getAllPendingRequest())
 			{
 				getAllPendingRequest().remove(current);
 			}
 		}
 		
 	//	Add the current request to pending ones
 	//
 		if (p_request != null)
 		{
 			synchronized (getAllPendingRequest())
 			{
 				getAllPendingRequest().add(p_request);
 			}
 		}
 
 	//	Store pending request in HTTP session
 	//
 		getCurrentSession().setAttribute(PENDING_REQUEST_ATTRIBUTE,
 										 p_request);
 	}
 
 	/**
 	 * @return the pending request (null if none)
 	 */
 	protected PendingRequest getCurrentPendingRequest() 
 	{
 		HttpSession session = getCurrentSession();
 		if (session == null)
 		{
 		//	No session : no request...
 		//
 			return null;
 		}
 		
 		return (PendingRequest) session.getAttribute(PENDING_REQUEST_ATTRIBUTE);
 	}
 
 	/**
 	 * Service invocation
 	 * @param p_request
 	 * @throws IOException
 	 */
 	protected void invokeService(PendingRequest p_request) throws IOException
 	{
 	//	Precondition checking
 	//
 		if (p_request == null || p_request.event == null)
 		{
 			return;
 		}
 		
 		invokeService(p_request.rpcRequest, p_request.event);
 		
 		// we should remove Pending request class, but if we do so
 		// we can't use this method in loop on getAllPending request
 		// (concurrent exception)
 		p_request.event = null;
 	}
 
 	// -------------------------------------------------------------------------
 	// Remote service servlet override
 	// -------------------------------------------------------------------------
 	/**
 	 * @see
 	 * org.apache.catalina.CometProcessor#event(org.apache.catalina.CometEvent)
 	 */
 	public void event(CometEvent p_event) throws IOException, ServletException
 	{
 	// Store the request & response objects in thread-local storage.
 	//
 		getPerThreadRequest().set(p_event.getHttpServletRequest());
 		getPerThreadResponse().set(p_event.getHttpServletResponse());
 
 		if (p_event.getEventType() == CometEvent.EventType.BEGIN)
 		{
 			if (_log.isDebugEnabled())
 			{
 				_log.debug(p_event.getEventType());
 			}
 		} 
 		else if (p_event.getEventType() == CometEvent.EventType.READ)
 		{
 			try 
 			{
 				// Get payload
 				String payload = readRequest(p_event);
 				RPCRequest rpcRequest = null;
 
 				// Decode request
 				//
 				rpcRequest = RPC.decodeRequest(payload, this.getClass(), this);
 
 				// Is it a Comet or not ?
 				Comet comet = getComet(rpcRequest.getMethod());
 				if (comet != null && !haveData(rpcRequest))
 				{
 					// if its the event request then wait for events
 					if (comet.timeout() != 0)
 					{
 						p_event.setTimeout(comet.timeout());
 					}
 					
 					// save this request for processing later.
 					setCurrentPendingRequest(new PendingRequest(rpcRequest,
 															    p_event));
 				}
 				else 
 				{
 					// otherwise process the RPC call as usual
 					// sendResponse( event, rpcRequest );
 					// Invoke method
 					//
 					invokeService(rpcRequest, p_event);
 				}
 			} 
 			catch (Throwable e)
 			{
 				_log.warn("Comet Event error !", e);
 				// Give a subclass a chance to either handle the exception or
 				// rethrow it
 				//
 				writeResponse(p_event.getHttpServletResponse(),
 							  "Server Error : " + e.getMessage());
 			}
 		}
 		else if (p_event.getEventType() == CometEvent.EventType.END)
 		{
 			PendingRequest abortedRequest = getCurrentPendingRequest();
 			boolean isAborted = false;
 			if (abortedRequest != null)
 			{
 				if (abortedRequest.event != null)
 				{
 					isAborted = true;
 				}
 				setCurrentPendingRequest(null);
 			}
 			p_event.close();
 			if (isAborted)
 			{
 				onCurrentRequestAbort(abortedRequest);
 			}
 		}
 		else if (p_event.getEventType() == CometEvent.EventType.ERROR)
 		{
 		//	Is it a "normal" error case ?
 		//
 			if (p_event.getEventSubType() == CometEvent.EventSubType.TIMEOUT)
 			{
 				invokeService(getCurrentPendingRequest());
 			} 
 			else if (p_event.getEventSubType() == CometEvent.EventSubType.CLIENT_DISCONNECT)
 			{
 				if (getCurrentPendingRequest() != null)
 				{
 					onCurrentRequestAbort(getCurrentPendingRequest());
 				}
 			} 
 			else 
 			{
 				_log.error("server event error: " + p_event.getEventSubType());
 				setCurrentPendingRequest(null);
 				writeResponse(p_event.getHttpServletResponse(),
 						"server event error: " + p_event.getEventSubType());
 				p_event.close();
 			}
 		}
 		// null the thread-locals to avoid holding request/response
 		//
 		getPerThreadRequest().set(null);
 		getPerThreadResponse().set(null);
 	}
 	
 	//-------------------------------------------------------------------------
 	//
 	// Internal methods
 	//
 	//-------------------------------------------------------------------------
 	/**
 	 * Get the method associated Comet annotation.
 	 */
 	protected Comet getComet(Method method)
 	{
 		return m_comets.get(method.getName());
 	}
 
 	/**
 	 * Get the underlying thread request.
 	 * access to private property (no choice)
 	 */
 	@SuppressWarnings("unchecked")
 	protected ThreadLocal<HttpServletRequest> getPerThreadRequest()
 	{
 		try
 		{
 			Field field = RemoteServiceServlet.class
 					.getDeclaredField("perThreadRequest");
 			field.setAccessible(true);
 			return (ThreadLocal<HttpServletRequest>) field.get(this);
 		} 
 		catch (Exception e)
 		{
 			throw new RuntimeException("Cannot get 'perThreadRequest' member (bad GWT version ?) !", e);
 		} 
 	}
 
 	/**
 	 * Get the underlying thread response.
 	 * access to private property (no choice)
 	 */
 	@SuppressWarnings("unchecked")
 	protected ThreadLocal<HttpServletResponse> getPerThreadResponse()
 	{
 		try
 		{
 			Field field = RemoteServiceServlet.class
 					.getDeclaredField("perThreadResponse");
 			field.setAccessible(true);
 			return (ThreadLocal<HttpServletResponse>) field.get(this);
 		} 
 		catch (Exception e)
 		{
 			throw new RuntimeException("Cannot get 'perThreadResponse' member (bad GWT version ?) !", e);
 		}
 	}
	
	/**
	 * Get current HTTP session
	 * @return
	 */
 	protected HttpSession getCurrentSession()
 	{
		return getThreadLocalRequest().getSession();
 	}
 	
 	/**
 	 * @return the collection of all requests (static set)
 	 */
 	protected static Set<PendingRequest> getAllPendingRequest()
 	{
 		return s_pendingRequests;
 	}
 
 	/**
 	 * 
 	 * @param p_event
 	 * @return
 	 * @throws IOException
 	 * @throws ServletException
 	 */
 	protected String readRequest(CometEvent p_event) throws IOException,
 															ServletException
     {
 		int contentLength = p_event.getHttpServletRequest().getContentLength();
 		if (contentLength == -1)
 		{
 			// Content length must be known.
 			throw new ServletException("Content-Length must be specified");
 		}
 		
 	//	Read servlet input stream
 	//
 		InputStream in = p_event.getHttpServletRequest().getInputStream();
 		byte[] payload = new byte[contentLength];
 		int offset = 0;
 		int len = contentLength;
 		int byteCount;
 		while (offset < contentLength)
 		{
 			byteCount = in.read(payload, offset, len);
 			if (byteCount == -1) {
 				throw new ServletException("Client did not send "
 						+ contentLength + " bytes as expected");
 			}
 			offset += byteCount;
 			len -= byteCount;
 		}
 		return new String(payload, "UTF-8");
 	}
 
 	/**
 	 * Write HTTP response
 	 * @param p_response
 	 * @param p_body
 	 */
 	protected void writeResponse(HttpServletResponse p_response, String p_body) 
 	{
 		try
 		{
 			PrintWriter writer = p_response.getWriter();
 			writer.print(p_body);
 			writer.flush();
 		} 
 		catch (IOException e)
 		{
 			_log.error("IOExeption sending response", e);
 		}
 	}
 
 	/**
 	 * if a PendingRequest class was created, you should call
 	 * 'invokeService(PendingRequest)'
 	 * 
 	 * @param p_rpcRequest
 	 * @param p_event
 	 * @throws IOException
 	 */
 	protected void invokeService(RPCRequest p_rpcRequest, CometEvent p_event)
 															throws IOException 
 	{
 	//	Precondition checking
 	//
 		if (p_rpcRequest == null || p_event == null)
 		{
 			return;
 		}
 		
 		if (_log.isDebugEnabled())
 		{
 			_log.debug("invokeService: "+ p_rpcRequest.getMethod().getName());
 		}
 		
 		// store perThreadRequest/Response to make this methods re-entrant
 		//
 		HttpServletRequest oldRequest = getPerThreadRequest().get();
 		HttpServletResponse oldResponse = getPerThreadResponse().get();
 		getPerThreadRequest().set(p_event.getHttpServletRequest());
 		getPerThreadResponse().set(p_event.getHttpServletResponse());
 
 		try 
 		{
 			String responsePayload = null;
 			try 
 			{
 				try 
 				{
 					// invoke service
 					// 
 					Object returnValue = p_rpcRequest.getMethod().invoke(this,
 							p_rpcRequest.getParameters());
 
 					// Encode response
 					//  
 					responsePayload = RPC.encodeResponseForSuccess(p_rpcRequest
 							.getMethod(), returnValue, p_rpcRequest
 							.getSerializationPolicy());
 
 				} 
 				catch (IllegalAccessException e) 
 				{
 					// TODO message
 					// SecurityException securityException = new
 					// SecurityException( RPC
 					// .formatIllegalAccessErrorMessage( this,
 					// p_rpcRequest.getMethod() )
 					// );
 					SecurityException securityException = new SecurityException(
 							"IllegalAccessError : " + this + "."
 									+ p_rpcRequest.getMethod());
 					securityException.initCause(e);
 					throw securityException;
 				} catch (InvocationTargetException e) {
 					_log.error("Invocation exception : " + e.getMessage(), e);
 					
 					responsePayload = RPC.encodeResponseForFailure(p_rpcRequest
 							.getMethod(), e.getCause());
 				}
 			} catch (SerializationException e)
 			{
 				_log.error("Serialization exception : " + e.getMessage(), e);
 				responsePayload = "server error: " + e.getMessage();
 			}
 			writeResponse(p_event.getHttpServletResponse(), responsePayload);
 			p_event.close();
 		} 
 		finally
 		{
 			getPerThreadRequest().set(oldRequest);
 			getPerThreadResponse().set(oldResponse);
 		}
 	}
 
 	/**
 	 * Watchdog for cleaning obsolete pending sessions
 	 * @author Vincent Legendre
 	 *
 	 */
 	private static class CleanPendingSessionIdThread extends Thread
 	{
 		//----
 		// Constants
 		//----
 		private static int SLEEP_DELAY = 60000; // ms => 1 minute
 		/**
 		 * Thread lige cycle
 		 */
 		public void run()
 		{
 			// lets sleep for 1 min
 			try {
 				Thread.sleep(SLEEP_DELAY);
 			} catch (InterruptedException e)
 			{
 				_log.warn("Interrupted thread sleep", e);
 			}
 			
 		//	Check all pending requests
 		//
 			Set<PendingRequest> toBeRemoved = new HashSet<PendingRequest>();
 			synchronized (getAllPendingRequest()) 
 			{
 				for (PendingRequest request : getAllPendingRequest())
 				{
 					if (request.event == null)
 					{
 						toBeRemoved.add(request);
 					}
 				}
 				
 			//	Remove obsolete requests
 			//
 				getAllPendingRequest().removeAll(toBeRemoved);
 			}
 		}
 	}	
 }
