 package org.openxdm.xcap.server.slee;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.slee.ActivityContextInterface;
 import javax.slee.ChildRelation;
 import javax.slee.RolledBackContext;
 import javax.slee.SbbContext;
 import javax.slee.facilities.Tracer;
 
 import net.java.slee.resource.http.events.HttpServletRequestEvent;
 import org.mobicents.slee.xdm.server.ServerConfiguration;
 import org.openxdm.xcap.common.error.BadRequestException;
 import org.openxdm.xcap.common.error.ConflictException;
 import org.openxdm.xcap.common.error.InternalServerErrorException;
 import org.openxdm.xcap.common.error.MethodNotAllowedException;
 import org.openxdm.xcap.common.error.NoParentConflictException;
 import org.openxdm.xcap.common.error.NotFoundException;
 import org.openxdm.xcap.common.error.PreconditionFailedException;
 import org.openxdm.xcap.common.error.UnsupportedMediaTypeException;
 import org.openxdm.xcap.common.http.HttpConstant;
 import org.openxdm.xcap.common.resource.Resource;
 import org.openxdm.xcap.common.uri.ParseException;
 import org.openxdm.xcap.common.uri.Parser;
 import org.openxdm.xcap.common.uri.ResourceSelector;
 import org.openxdm.xcap.server.etag.ETagValidator;
 import org.openxdm.xcap.server.etag.IfMatchETagValidator;
 import org.openxdm.xcap.server.etag.IfNoneMatchETagValidator;
 import org.openxdm.xcap.server.result.ReadResult;
 import org.openxdm.xcap.server.result.WriteResult;
 
 /**
  * 
  * @author martins
  * @author aayush.bhatnagar
  * 
  */
 public abstract class AggregationProxySbb implements javax.slee.Sbb {
 
 	private SbbContext sbbContext = null;
 
 	private static Tracer logger;
 
 	public void setSbbContext(SbbContext context) {
 		this.sbbContext = context;
 		if (logger == null) {
 			logger = sbbContext.getTracer(this.getClass().getSimpleName());
 		}
 	}
 
 	public void unsetSbbContext() {
 		this.sbbContext = null;
 	}
 
 	public void sbbCreate() throws javax.slee.CreateException {
 
 	}
 
 	public void sbbPostCreate() throws javax.slee.CreateException {
 
 	}
 
 	public void sbbActivate() {
 
 	}
 
 	public void sbbPassivate() {
 
 	}
 
 	public void sbbRemove() {
 
 	}
 
 	public void sbbLoad() {
 
 	}
 
 	public void sbbStore() {
 
 	}
 
 	public void sbbExceptionThrown(Exception exception, Object event,
 			ActivityContextInterface activity) {
 		if (logger.isFineEnabled())
 			logger.fine("sbbExceptionThrown(exception=" + exception.toString()
 					+ ",event=" + event.toString() + ",activity="
 					+ activity.toString() + ")");
 	}
 
 	public void sbbRolledBack(RolledBackContext sbbRolledBack) {
 		if (logger.isFineEnabled())
 			logger.fine("sbbRolledBack(sbbRolledBack="
 					+ sbbRolledBack.toString() + ")");
 	}
 
 	protected SbbContext getSbbContext() {
 		return sbbContext;
 	}
 
 	// CHILD RELATIONS & RA ABSTRACTIONS
 	// ################################################################
 
 	public abstract ChildRelation getRequestProcessorChildRelation();
 
 	protected RequestProcessorSbbLocalObject getRequestProcessor() {
 		try {
 			return (RequestProcessorSbbLocalObject) getRequestProcessorChildRelation()
 			.create();
 		} catch (Exception e) {
 			logger.severe("Failed to create child sbb", e);
 			return null;
 		}
 	}
 
 	// added by aayush here: Child relation and child sbb creation
 	// for Authentication Proxy.
 	public abstract ChildRelation getAuthenticationProxyChildRelation();
 
 	protected AuthenticationProxySbbLocalObject getAuthenticationProxy() {
 			try {
 				return (AuthenticationProxySbbLocalObject) getAuthenticationProxyChildRelation()
 						.create();
 			} catch (Exception e) {
 				logger.severe("Failed to create child sbb", e);
 				return null;
 			}
 	}
 
 	public void onDelete(HttpServletRequestEvent event,
 			ActivityContextInterface aci) {
 
 		// detach from the activity
 		aci.detach(sbbContext.getSbbLocalObject());
 
 		HttpServletRequest request = event.getRequest();
 		HttpServletResponse response = event.getResponse();
 
 		try {
 
 			PrintWriter responseWriter = response.getWriter();
 
 			try {
 
 				// get xcap root from config
 				String xcapRoot = ServerConfiguration.getInstance().getXcapRoot();
 
 				// create resource selector from request's uri & query
 				// string
 				ResourceSelector resourceSelector = Parser
 						.parseResourceSelector(xcapRoot, request
 								.getRequestURI(), request.getQueryString());
 
 				// user authentication
 				String user = getAuthenticationProxy().authenticate(request, response);
 				if (response.isCommitted()) {
 					// authentication proxy replied, stop processing request
 					return;
 				}
 				
 				// check conditional request headers
 				// get ifMatch eTag
 				ETagValidator eTagValidator = null;
 				String eTag = request.getHeader(HttpConstant.HEADER_IF_MATCH);
 				if (eTag != null) {
 					eTagValidator = new IfMatchETagValidator(eTag);
 				} else {
 					eTag = request.getHeader(HttpConstant.HEADER_IF_NONE_MATCH);
 					if (eTag != null) {
 						eTagValidator = new IfNoneMatchETagValidator(eTag);
 					}
 				}
 
 				// delete in data source
 				if (logger.isInfoEnabled()) {
 					logger.info("delete(resourceSelector=" + resourceSelector
 							+ ",eTagValidator=" + eTagValidator + ",xcapRoot="
 							+ xcapRoot + ")");
 				}
 				WriteResult result = getRequestProcessor().delete(
 						resourceSelector, eTagValidator, xcapRoot,user);
 				// set response status
 				response.setStatus(result.getResponseStatus());
 				// set response entity tag if provided
 				if (result.getResponseEntityTag() != null) {
 					response.setHeader(HttpConstant.HEADER_ETAG, result
 							.getResponseEntityTag());
 				}
 
 			} catch (ParseException e) {
 				NotFoundException ne = new NotFoundException();
 				if (logger.isFineEnabled())
 					logger.fine("invalid xcap uri, replying , replying "
 							+ ne.getResponseStatus(),e);
 				response.setStatus(ne.getResponseStatus());
 
 			} catch (NotFoundException e) {
 				if (logger.isFineEnabled())
 					logger.fine("doc/elem/attrib not found, replying "
 							+ e.getResponseStatus(),e);
 				response.setStatus(e.getResponseStatus());
 
 			} catch (ConflictException e) {
 				if (logger.isFineEnabled())
 					logger.fine("conflict exception, replying "
 							+ e.getResponseStatus(),e);
 				response.setStatus(e.getResponseStatus());
 				responseWriter.print(e.getResponseContent());
 
 			} catch (MethodNotAllowedException e) {
 				if (logger.isFineEnabled())
 					logger.fine("method not allowed, replying "
 							+ e.getResponseStatus(),e);
 				response.setStatus(e.getResponseStatus());
 				// add all exception headers
 				Map<String, String> exceptionHeaders = e.getResponseHeaders();
 				for (Iterator<String> i = exceptionHeaders.keySet().iterator(); i
 						.hasNext();) {
 					String headerName = i.next();
 					String headerValue = exceptionHeaders.get(headerName);
 					response.setHeader(headerName, headerValue);
 				}
 
 			} catch (PreconditionFailedException e) {
 				if (logger.isFineEnabled())
 					logger.fine("precondition failed on etags, replying "
 							+ e.getResponseStatus(),e);
 				response.setStatus(e.getResponseStatus());
 
 			} catch (InternalServerErrorException e) {
 				logger.warning("internal server error: " + e.getMessage()
 						+ ", replying " + e.getResponseStatus(),e);
 				response.setStatus(e.getResponseStatus());
 
 			} catch (BadRequestException e) {
 				if (logger.isFineEnabled())
 					logger.fine("bad request, replying "
 							+ e.getResponseStatus(),e);
 				response.setStatus(e.getResponseStatus());
 			}
 			// send to client
 			responseWriter.close();
 
 		} catch (Throwable e) {
 			logger.severe("Error processing onDelete()", e);
 		}
 
 	}
 
 	public void onGet(HttpServletRequestEvent event,
 			ActivityContextInterface aci) {
 
 		// detach from the activity
 		aci.detach(sbbContext.getSbbLocalObject());
 
 		HttpServletRequest request = event.getRequest();
 		HttpServletResponse response = event.getResponse();
 
 		try {
 
 			PrintWriter responseWriter = response.getWriter();
 
 			try {
 
 				// create jxcap resource selector from request's uri & query
 				// string
 				ResourceSelector resourceSelector = Parser
 						.parseResourceSelector(ServerConfiguration.getInstance().getXcapRoot(),
 								request.getRequestURI(), request
 										.getQueryString());
 				// read result from data source
 				if (logger.isInfoEnabled()) {
 					logger.info("get(resourceSelector=" + resourceSelector
 							+ ")");
 				}
 				
 				// user authentication
 				String user = getAuthenticationProxy().authenticate(request, response);
 				if (response.isCommitted()) {
 					// authentication proxy replied, stop processing request
 					return;
 				}
 				
 				ReadResult result = getRequestProcessor().get(resourceSelector,user);
 				// get data object from result
 				Resource dataObject = result.getResponseDataObject();
 				// set response content type
 				response.setContentType(dataObject.getMimetype());
 				// set response entity tag
 				response.setHeader(HttpConstant.HEADER_ETAG, result
 						.getResponseEntityTag());
 				// add response content
 				responseWriter.println(dataObject.toXML());
 
 			} catch (ParseException e) {
 				NotFoundException ne = new NotFoundException();
 				logger.warning("invalid xcap uri, replying "
 						+ ne.getResponseStatus(),e);
 				response.setStatus(ne.getResponseStatus());
 
 			} catch (NotFoundException e) {
 
 				if (logger.isFineEnabled())
 					logger.fine("doc/elem/attrib not found, replying "
 							+ e.getResponseStatus(),e);
 				response.setStatus(e.getResponseStatus());
 
 			} catch (InternalServerErrorException e) {
 				logger.warning("internal server error: " + e.getMessage()
 						+ ", replying " + e.getResponseStatus(),e);
 				response.setStatus(e.getResponseStatus());
 
 			} catch (BadRequestException e) {
 				if (logger.isFineEnabled())
 					logger.fine("bad request, replying "
 							+ e.getResponseStatus(),e);
 				response.setStatus(e.getResponseStatus());
 			}
 			// send to client
 			responseWriter.close();
 
 		} catch (Throwable e) {
 			logger.severe("Error processing onGet()", e);
 		}
 
 	}
 
 	public void onPut(HttpServletRequestEvent event,
 			ActivityContextInterface aci) {
 
 		// detach from the activity
 		aci.detach(sbbContext.getSbbLocalObject());
 
 		HttpServletRequest request = event.getRequest();
 		HttpServletResponse response = event.getResponse();
 
 		try {
 
 			PrintWriter responseWriter = response.getWriter();
 
 			try {
 
 				// create resource selector from request's uri & query
 				// string
 				ResourceSelector resourceSelector = Parser
 						.parseResourceSelector(ServerConfiguration.getInstance().getXcapRoot(),
 								request.getRequestURI(), request
 										.getQueryString());
 
 				// user authentication
 				String user = getAuthenticationProxy().authenticate(request, response);
 				if (response.isCommitted()) {
 					// authentication proxy replied, stop processing request
 					return;
 				}
 				
 				// check conditional request headers
 				// get ifMatch eTag
 				ETagValidator eTagValidator = null;
 				String eTag = request.getHeader(HttpConstant.HEADER_IF_MATCH);
 				if (eTag != null) {
 					eTagValidator = new IfMatchETagValidator(eTag);
 				} else {
 					eTag = request.getHeader(HttpConstant.HEADER_IF_NONE_MATCH);
 					if (eTag != null) {
 						eTagValidator = new IfNoneMatchETagValidator(eTag);
 					}
 				}
 				// get content mimetype
 				String mimetype = request.getContentType();
 				if (logger.isInfoEnabled()) {
 					logger.info("put(resourceSelector=" + resourceSelector
 							+ ",mimetype=" + mimetype + ",eTagValidator="
 							+ eTagValidator + ",xcapRoot="
 							+ ServerConfiguration.getInstance().getXcapRoot() + ")");
 				}
 				// put object in data source
 				WriteResult result = getRequestProcessor().put(
 						resourceSelector, mimetype, request.getInputStream(),
 						eTagValidator, ServerConfiguration.getInstance().getXcapRoot(),user);
 				// set response status
 				response.setStatus(result.getResponseStatus());
 				// set response entity tag with new one on result
 				response.setHeader(HttpConstant.HEADER_ETAG, result
 						.getResponseEntityTag());
 
 			} catch (ParseException e) {
 				// invalid resource selector
 				BadRequestException bre = new BadRequestException();
 				if (logger.isFineEnabled())
 					logger.fine("invalid xcap uri, replying "
 							+ bre.getResponseStatus(),e);
 				response.setStatus(bre.getResponseStatus());
 
 			} catch (IOException e) {
 				InternalServerErrorException ie = new InternalServerErrorException(
 						e.getMessage(),e);
 				logger.warning("internal server error: " + e.getMessage()
 						+ ", replying " + ie.getResponseStatus(),e);
 				response.setStatus(ie.getResponseStatus());
 
 			} catch (NoParentConflictException e) {
 				// add base uri
 				e
 						.setSchemeAndAuthorityURI(ServerConfiguration.getInstance().getSchemeAndAuthority());
 				// add query string if exists
 				if (request.getQueryString() != null) {
 					e.setQueryComponent(request.getQueryString());
 				}
 				if (logger.isFineEnabled())
 					logger.fine("no parent conflict exception, replying "
 							+ e.getResponseStatus(),e);
 				response.setStatus(e.getResponseStatus());
 				responseWriter.print(e.getResponseContent());
 
 			} catch (ConflictException e) {
 				if (logger.isFineEnabled())
 					logger.fine("conflict exception, replying "
 							+ e.getResponseStatus(),e);
 				response.setStatus(e.getResponseStatus());
 				responseWriter.print(e.getResponseContent());
 
 			} catch (MethodNotAllowedException e) {
 				if (logger.isFineEnabled())
 					logger.fine("method not allowed, replying "
 							+ e.getResponseStatus(),e);
 				response.setStatus(e.getResponseStatus());
 				// add all exception headers
 				Map<String, String> exceptionHeaders = e.getResponseHeaders();
 				for (Iterator<String> i = exceptionHeaders.keySet().iterator(); i
 						.hasNext();) {
 					String headerName = i.next();
 					String headerValue = exceptionHeaders.get(headerName);
 					response.setHeader(headerName, headerValue);
 				}
 
 			} catch (UnsupportedMediaTypeException e) {
 				if (logger.isFineEnabled())
 					logger.fine("unsupported media exception, replying "
 							+ e.getResponseStatus(),e);
 				response.setStatus(e.getResponseStatus());
 
 			} catch (InternalServerErrorException e) {
 				logger.warning("internal server error: " + e.getMessage()
 						+ ", replying " + e.getResponseStatus(),e);
 				response.setStatus(e.getResponseStatus());
 
 			} catch (PreconditionFailedException e) {
 				if (logger.isFineEnabled())
 					logger.fine("precondition failed on etags, replying "
 							+ e.getResponseStatus(),e);
 				response.setStatus(e.getResponseStatus());
 
 			} catch (BadRequestException e) {
 				if (logger.isFineEnabled())
 					logger.fine("invalid xcap uri, replying "
 							+ e.getResponseStatus(),e);
 				response.setStatus(e.getResponseStatus());
			}
 			// send to client
 			responseWriter.close();
 
 		} catch (Throwable e) {
 			logger.severe("Error processing onPut()", e);
 		}
 
 	}
 
 	// ######################################## NOT SUPPORTED METHODS
 
 	public void onPost(HttpServletRequestEvent event,
 			ActivityContextInterface aci) {
 
 		sendUnsupportedRequestErrorResponse(event, aci);
 	}
 
 	public void onHead(HttpServletRequestEvent event,
 			ActivityContextInterface aci) {
 
 		sendUnsupportedRequestErrorResponse(event, aci);
 	}
 
 	public void onOptions(HttpServletRequestEvent event,
 			ActivityContextInterface aci) {
 
 		sendUnsupportedRequestErrorResponse(event, aci);
 	}
 
 	public void onTrace(HttpServletRequestEvent event,
 			ActivityContextInterface aci) {
 		sendUnsupportedRequestErrorResponse(event, aci);
 	}
 
 	private void sendUnsupportedRequestErrorResponse(
 			HttpServletRequestEvent event, ActivityContextInterface aci) {
 
 		// detach from the activity
 		aci.detach(sbbContext.getSbbLocalObject());
 
 		// method not allowed, set right sc and allow header then send response
 		try {
 			HttpServletResponse response = event.getResponse();
 			response.setStatus(MethodNotAllowedException.RESPONSE_STATUS);
 			response.setHeader(HttpConstant.HEADER_ALLOW, "GET, PUT, DELETE");
 			response.flushBuffer();
 		} catch (Exception e) {
 			logger.severe("unable to send response", e);
 		}
 	}
 
 }
