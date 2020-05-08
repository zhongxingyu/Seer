 package fedora.server;
 
 import java.lang.reflect.Method;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 import java.security.Principal;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.axis.MessageContext;
 import org.apache.axis.transport.http.HTTPConstants;
 
 import org.apache.log4j.Logger;
 
 import fedora.common.Constants;
 import fedora.server.utilities.DateUtility;
 
 /**
  * Context that is read-only.
  *
  * @author cwilper@cs.cornell.edu
  * @version $Id$
  */
 public class ReadOnlyContext implements Context {
 
     /** Logger for this class. */
     private static final Logger LOG = Logger.getLogger(
             ReadOnlyContext.class.getName());
 	
     public static ReadOnlyContext EMPTY=new ReadOnlyContext(null, null, "", true);
     static {
     	EMPTY.setActionAttributes(null);
     	EMPTY.setResourceAttributes(null);
     }
     
     private final Date now = new Date();
     
     private MultiValueMap m_environmentAttributes;
     public final MultiValueMap getEnvironmentAttributes() {
     	return m_environmentAttributes;
     }
 
     private MultiValueMap m_subjectAttributes;
     
     private MultiValueMap m_actionAttributes;
     
     private MultiValueMap m_resourceAttributes;
     
     private String password;
     
     private static final String NOOP_PARAMETER_NAME = "noOp";
     public static final boolean NO_OP = true;
     public static final boolean DO_OP = false;
     
     public static final String BACKEND_SERVICE = "backendService";
 
     private boolean noOp = false; 
 
     /**
      * Creates and initializes the <code>Context</code>.
      *
      * @param parameters A pre-loaded Map of name-value pairs
      *        comprising the context.
      */
     private ReadOnlyContext(MultiValueMap environmentAttributes, MultiValueMap subjectAttributes, String password, boolean noOp) {
         //super(parameters);
         m_environmentAttributes=environmentAttributes;
         if (m_environmentAttributes==null) {
             m_environmentAttributes=new MultiValueMap();
         }
         m_environmentAttributes.lock();
         m_subjectAttributes=subjectAttributes;
         if (m_subjectAttributes==null) {
             m_subjectAttributes=new MultiValueMap();
         }
         m_subjectAttributes.lock(); 
         m_actionAttributes=new MultiValueMap();
         m_actionAttributes.lock();
         m_resourceAttributes=new MultiValueMap();
         m_resourceAttributes.lock();
         if (password == null) {
         	password = "";
         }
         this.password = password;
         this.noOp = noOp;
     }
  
     public Iterator environmentAttributes() {
         return m_environmentAttributes.names();
     }
 
     public int nEnvironmentValues(String name) {
         return m_environmentAttributes.length(name);
     }
     
     public String getEnvironmentValue(String name) {
         return m_environmentAttributes.getString(name);
     }
     
     public String[] getEnvironmentValues(String name) {
         return m_environmentAttributes.getStringArray(name);
     }
 
     public Iterator subjectAttributes() {
         return m_subjectAttributes.names();
     }
 
     public int nSubjectValues(String name) {
         return m_subjectAttributes.length(name);
     }
     
     public String getSubjectValue(String name) {
         return m_subjectAttributes.getString(name);
     }
     
     public String[] getSubjectValues(String name) {
         return m_subjectAttributes.getStringArray(name);
     }
     
     public void setActionAttributes(MultiValueMap actionAttributes) {
         m_actionAttributes = actionAttributes;
         if (m_actionAttributes == null) {
             m_actionAttributes = new MultiValueMap();
         }
         m_actionAttributes.lock();    	
     }
 
     public Iterator actionAttributes() {
         return m_actionAttributes.names();
     }
 
     public int nActionValues(String name) {
         return m_actionAttributes.length(name);
     }
     
     public String getActionValue(String name) {
         return m_actionAttributes.getString(name);
     }
     
     public String[] getActionValues(String name) {
         return m_actionAttributes.getStringArray(name);
     }
 
     public Iterator resourceAttributes() {
         return m_resourceAttributes.names();
     }
 
     public void setResourceAttributes(MultiValueMap resourceAttributes) {
         m_resourceAttributes = resourceAttributes;
         if (m_resourceAttributes == null) {
             m_resourceAttributes = new MultiValueMap();
         }
         m_resourceAttributes.lock();    	
     }
     
     public int nResourceValues(String name) {
         return m_resourceAttributes.length(name);
     }
     
     public String getResourceValue(String name) {
         return m_resourceAttributes.getString(name);
     }
     
     public String[] getResourceValues(String name) {
         return m_resourceAttributes.getStringArray(name);
     }
     
     public String toString() {
     	StringBuffer buffer = new StringBuffer();
     	buffer.append("READ-ONLY CONTEXT:\n");
     	buffer.append(m_environmentAttributes);
     	buffer.append(m_subjectAttributes);
     	buffer.append(m_actionAttributes);
     	buffer.append(m_resourceAttributes);
     	buffer.append("(END-READ ONLY CONTEXT)\n");
     	return buffer.toString();
     }
     
     
     public Date now() {
     	return now;
     }
     
     private static final MultiValueMap beginEnvironmentMap(String messageProtocol) throws Exception {
     	MultiValueMap environmentMap = new MultiValueMap();
   	    environmentMap.set(Constants.HTTP_REQUEST.MESSAGE_PROTOCOL.uri, messageProtocol);
 		Date now = new Date();
 		environmentMap.set(Constants.ENVIRONMENT.CURRENT_DATE_TIME.uri, DateUtility.convertDateToString(now));
 		environmentMap.set(Constants.ENVIRONMENT.CURRENT_DATE.uri, DateUtility.convertDateToDateString(now));
 		environmentMap.set(Constants.ENVIRONMENT.CURRENT_TIME.uri, DateUtility.convertDateToTimeString(now));
 		return environmentMap;
     }
 
     //will need fixup for noOp 
     public static Context getSoapContext() {
         HttpServletRequest req=(HttpServletRequest) MessageContext.
                 getCurrentContext().getProperty(
                 HTTPConstants.MC_HTTP_SERVLETREQUEST);
       return ReadOnlyContext.getContext(Constants.HTTP_REQUEST.SOAP.uri, req);
     }
     
     /* i don't see any references.  needed? let's see . . .  
     public static final ReadOnlyContext getContext(Context existingContext, String subjectId, String password, String[] roles) {
   		return getContext(existingContext.getEnvironmentAttributes(), subjectId, password, roles, existingContext.getNoOp());
     }
     */
 
     private static final Class STRING_ARRAY_CLASS;
     static {
     	String[] temp = {""};
     	STRING_ARRAY_CLASS = temp.getClass();
     }
     
     public static final String GETPASSWORD_METHOD_NAME = "getPassword";
 
     public static final String GETROLES_METHOD_NAME = "getRoles";
     
     public static final String getPassword(Principal principal) throws Exception {
     	return getPassword(principal, GETPASSWORD_METHOD_NAME);
     }
     
     public static final String getPassword(Principal principal, String getPasswordMethodName) throws Exception {
         final String here = ReadOnlyContext.class.getName() + ".getPassword()";
 		LOG.debug(here);
     	
 		Class principalClass = principal.getClass();
 		
 		String password = null;			
 		if ((getPasswordMethodName == null) || "".equals(getPasswordMethodName)) {
 			String msg = here + ": getpassword method not configured";
 			LOG.debug(msg);
 			throw new Exception(msg);
 		} else {
 			LOG.debug(here + ": cando getpassword");
 			Method getPasswordMethod = null;
 			try {
 				getPasswordMethod = principalClass.getDeclaredMethod(getPasswordMethodName, (Class[])null);
 			} catch (NoSuchMethodException nsme) {
 				String msg = here + ": Principal-implementing class has no get-password method";
 				LOG.error(msg, nsme);
 				throw new Exception();
 			}					
 			Class passwordClass = getPasswordMethod.getReturnType();
 			if (passwordClass != String.class) {
 				String msg = here + ": get-password method does not return a String";
 				LOG.debug(msg);
 				throw new Exception();		
 			}
 			try {
 				password = (String) getPasswordMethod.invoke(principal, (Object[])null);
 			} catch (Throwable t) {
 				String msg = here + ": get-password method failed";
 				LOG.error(msg, t);
 				throw new Exception();
 			}
 			LOG.debug(here + ": extracted password");
 			if (LOG.isDebugEnabled()) {
 				LOG.debug(here + ": password==" + password); 
 			}
 		}
 		return password;
     }
 
     public static final String[] getRoles(Principal principal) throws Exception {
     	return getRoles(principal, GETROLES_METHOD_NAME);
     }
 
     
     public static final String[] getRoles(Principal principal, String getRolesMethodName) throws Exception {
         final String here = ReadOnlyContext.class.getName() + ".getRoles()";
 		LOG.debug(here);
 
 		Class principalClass = principal.getClass();
 		
 		String[] roles = null;			
 		if ((getRolesMethodName == null) || "".equals(getRolesMethodName)) {
 			String msg = here + ": getroles method not configured";
 			LOG.debug(msg);
 			throw new Exception(msg);
 		} else {
 			LOG.debug(here + ": cando getroles");
 			Method getRolesMethod = null;
 			try {
 				getRolesMethod = principalClass.getDeclaredMethod(getRolesMethodName, (Class[])null);
 			} catch (NoSuchMethodException nsme) {
 				String msg = here + ": Principal-implementing class has no get-roles method";
 				LOG.error(msg, nsme);
 				throw new Exception(msg);
 			} catch (Throwable t) {
 				String msg = here + ": trouble getting get-roles method";
 				LOG.error(msg, t);
 				throw new Exception(msg);				
 			}					
 
 			if (getRolesMethod == null) {
 				String msg = here + ": null get-roles method";
 				LOG.debug(msg);
 				throw new Exception(msg);				
 			}
 			
 			Class rolesClass = null;
 			try {
 				rolesClass = getRolesMethod.getReturnType();
 			} catch (Throwable t) {
 				String msg = here + ": trouble getting get-role method return type";
 				LOG.error(msg, t);
 				throw new Exception(msg);
 			}
 
 			LOG.debug(ReadOnlyContext.class.getName() + ": rolesClass==" + " " + rolesClass);
 			if (rolesClass != STRING_ARRAY_CLASS) {
 				String msg = here + ": get-roles method does not return a String array";
 				LOG.debug(msg);
 				throw new Exception(msg);		
 			}
 			
 			try {
 				LOG.debug(here + ": about to get roles");
 				roles = (String[]) getRolesMethod.invoke(principal, (Object[])null);
 				LOG.debug(here + ": get-roles method succeeded"); //, roles==" + roles);
 			} catch (Throwable t) {
 				String msg = here + ": get-roles method failed";
 				LOG.error(msg, t);
 				throw new Exception(msg);
 			}
 			LOG.debug(here + ": extracted roles");
 		}
 		if (LOG.isDebugEnabled() && (roles != null)) {
 			for (int i = 0; i < roles.length; i++) {
 				LOG.debug(here + ": another role==" + roles[i]);					
 			}
 		}		
 		return roles;
     }
     
     
     private static final ReadOnlyContext getContext(MultiValueMap environmentMap, String subjectId, String password, String[] roles, Map auxSubjectRoles, boolean noOp) {
     	MultiValueMap subjectMap = new MultiValueMap(); 
       	try {		
       		subjectMap.set(Constants.SUBJECT.LOGIN_ID.uri, (subjectId == null) ? "" : subjectId);
       		for (int i = 0; (roles != null) && (i < roles.length); i++) {
       			String[] parts = parseRole(roles[i]);
      			if ((parts != null) && parts.length == 2) {
     				subjectMap.set(parts[0],parts[1]); //todo:  handle multiple values (ldap)
      			}
       		}
      		if (auxSubjectRoles != null) {
	      		Iterator auxSubjectRoleKeys = auxSubjectRoles.keySet().iterator();
	      		while (auxSubjectRoleKeys.hasNext()) {
	      			Object name = (String) auxSubjectRoleKeys.next();
	      			if (name instanceof String) {
	          			Object value = auxSubjectRoles.get(name);
	          			if ((value instanceof String) || (value instanceof String[])) {
	        				subjectMap.set((String) name, value);
	          			}      				
	      			}
	      		}
       		}
       	} catch (Exception e) {	
       		LOG.error("caught exception building subjectMap " + e.getMessage(), e);
       	} finally {
       		subjectMap.lock();
       	}
       	return new ReadOnlyContext(environmentMap, subjectMap, (password == null) ? "" : password, noOp);
     }
 
     // needed for, e.g., rebuild
     public static final ReadOnlyContext getContext(String messageProtocol, String subjectId, String password, String[] roles, boolean noOp) throws Exception {
     	MultiValueMap environmentMap = beginEnvironmentMap(messageProtocol);
   		environmentMap.lock(); // no request to grok more from 
   		return getContext(environmentMap, subjectId, password, roles, null, noOp);
     }
 
     public static final ReadOnlyContext getContext(String messageProtocol, HttpServletRequest request, String[] overrideRoles) {
 		MultiValueMap environmentMap = null;
 	  	try {
 	  		environmentMap = beginEnvironmentMap(messageProtocol);  			
 	  		
 	  		environmentMap.set(Constants.HTTP_REQUEST.SECURITY.uri, 
 	  				(request.isSecure()) ? Constants.HTTP_REQUEST.SECURE.uri : Constants.HTTP_REQUEST.INSECURE.uri);
 	  	    environmentMap.set(Constants.HTTP_REQUEST.SESSION_STATUS.uri, 
 	  	    		request.isRequestedSessionIdValid() ? "valid" : "invalid"   );
 
 	  	    String sessionEncoding = null;
 	  	    if (request.isRequestedSessionIdFromCookie()) {
 	  	    	sessionEncoding =  "cookie";    	
 	  	    } else if (request.isRequestedSessionIdFromURL()) {
 	  	    	sessionEncoding =  "url";    	
 	  	    }
 
 	  	    if (request.getContentLength() > -1) {
 	  	    	environmentMap.set(Constants.HTTP_REQUEST.CONTENT_LENGTH.uri, "" + request.getContentLength());    	
 	  	    }
 	  	    if (request.getLocalPort() > -1) {  			
 	  	    	environmentMap.set(Constants.HTTP_REQUEST.SERVER_PORT.uri, "" + request.getLocalPort());
 	  	    }
 
 	  	    if (request.getProtocol() != null) {  			
 	  	    	environmentMap.set(Constants.HTTP_REQUEST.PROTOCOL.uri, request.getProtocol());
 	  	    }
 	  	    if (request.getScheme() != null) {  			
 	  	    	environmentMap.set(Constants.HTTP_REQUEST.SCHEME.uri, request.getScheme());
 	  	    }
 	  	    if (request.getAuthType() != null) {
 	  	    	environmentMap.set(Constants.HTTP_REQUEST.AUTHTYPE.uri, request.getAuthType());  
 	  	    }
 	  	    if (request.getMethod() != null) {  			
 	  	    	environmentMap.set(Constants.HTTP_REQUEST.METHOD.uri, request.getMethod());	
 	  	    }
 	  	    if (sessionEncoding != null) {
 	  	    	environmentMap.set(Constants.HTTP_REQUEST.SESSION_ENCODING.uri, sessionEncoding);    		  	    	
 	  	    }
 	  	    if (request.getContentType() != null) {
 	  	    	environmentMap.set(Constants.HTTP_REQUEST.CONTENT_TYPE.uri, request.getContentType());
 	  	    }
 	  	    if (request.getLocalAddr() != null) {  			
 	  	    	environmentMap.set(Constants.HTTP_REQUEST.SERVER_IP_ADDRESS.uri, request.getLocalAddr());
 	  	    }
 	  	    if (request.getRemoteAddr() != null) {  			
 	  	    	environmentMap.set(Constants.HTTP_REQUEST.CLIENT_IP_ADDRESS.uri, request.getRemoteAddr());
 	  	    }
 
 	  	    if (request.getRemoteHost() != null) {  			
 	  	    	if (! request.getRemoteHost().matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {	    
 	  	    		environmentMap.set(Constants.HTTP_REQUEST.CLIENT_FQDN.uri, request.getRemoteHost().toLowerCase());        
 	  	    	}
 	  	    }
 	  	    if (request.getLocalName() != null) {  			
 	  	    	if (! request.getLocalName().matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
 	  	    		environmentMap.set(Constants.HTTP_REQUEST.SERVER_FQDN.uri, request.getLocalName().toLowerCase());
 	  	    	}
 	  	    }
 	  	} catch (Exception e) {
 	  	} finally {
 	  		environmentMap.lock();
 	  	}
 	  	
   	  	String subjectId = request.getRemoteUser();
   	  	String password = null;
 	  	Principal principal = request.getUserPrincipal();
   	  	if (principal == null) {
   	  		LOG.debug("in context, no principal to grok password from!!");				
   	  	} else {
   	  		try {
 	  	  		password = getPassword(principal);
 	  	  	} catch (Throwable t) {
 	  	  	}
   	  	}
   	  	if (subjectId == null) {
   	  		subjectId = "";
   	  	}
   	  	if (password == null) {
   	  		password = "";
   	  	}
   	  	if (overrideRoles == null) {
   	  		overrideRoles = new String[0];
   	  	}
   	  	
   	boolean noOp = true; //safest approach 
   	try {
   		noOp = (new Boolean(request.getParameter(NOOP_PARAMETER_NAME))).booleanValue();
   		LOG.debug("NOOP_PARAMETER_NAME="+NOOP_PARAMETER_NAME);
   		LOG.debug("request.getParameter(NOOP_PARAMETER_NAME)="+ request.getParameter(NOOP_PARAMETER_NAME));
   		LOG.debug("noOp="+ noOp);
  
   	} catch (Exception e) {
   		LOG.error(e.getMessage(), e);
   	}
   	Map auxSubjectRoles = null;
   	Object testFedoraAuxSubjectAttributes = request.getAttribute(FEDORA_AUX_SUBJECT_ATTRIBUTES);
   	if ((testFedoraAuxSubjectAttributes != null) && (testFedoraAuxSubjectAttributes instanceof Map)) {
   		auxSubjectRoles = (Map) testFedoraAuxSubjectAttributes;
   	}
   	return getContext(environmentMap, subjectId, password, overrideRoles, auxSubjectRoles, noOp);
     }
 
     /*
      * Gets a Context appropriate for the request, and whether it is ok
      * to use the dissemination cache or not.
      */
     //form context from optional servlet request, overriding request for added parms    
     public static final ReadOnlyContext getContext(String messageProtocol, HttpServletRequest request) {
         final String here = ReadOnlyContext.class.getName() + ".getContext(String, HttpServletRequest)";
 		LOG.debug(here);
   	  	String[] roles = null;
 	  	Principal principal = request.getUserPrincipal();
   	  	if (principal == null) {
   	  		LOG.debug(here + ": no principal to grok roles from!!");				
   	  	} else {
   	  		try {
 	  	  		roles = getRoles(principal);
 	  	  	} catch (Throwable t) {
 	  	  		LOG.error(here + ": exception calling getRoles()", t);
 	  	  	}
   	  	}
   	  	if (roles == null) {
   	  		roles = new String[0];
   	  	}
   	return getContext(messageProtocol, request, roles);
     }
     
   	public static final String[] parseRole (String role) {
   		String[] parts = null;
   		if ((role == null) || (role.length() == 0)) {
   		} else {
   			int i = role.indexOf('=');
   			if (i == 0) {
   			} else {
   				parts = new String[2];	
   				if (i < 0) {
   					parts[0] = role;
   					parts[1] = ""; //Boolean.toString(true);
   				} else {
   					parts[0] = role.substring(0,i);
   					if (i == (role.length()-1)) {
   						parts[1] = ""; //Boolean.toString(true);
   					} else {
   						parts[1] = role.substring(i+1);
   					}
   				}
   			}
   		}
   		return parts; 
   	}
   	
     public String getPassword() {
     	return password;
     }
     
     public boolean getNoOp() {
     	return noOp;
     }
   	
 }
