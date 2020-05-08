 package org.glite.security.voms.admin.servlets.rest;
 
 import it.infn.cnaf.voms.aa.VOMSAttributes;
 import it.infn.cnaf.voms.x509.ACServlet;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.glite.security.voms.admin.error.VOMSAuthorizationException;
 import org.glite.security.voms.admin.error.VOMSException;
 import org.glite.security.voms.admin.error.VOMSSyntaxException;
 import org.glite.security.voms.admin.operations.CurrentAdmin;
 import org.glite.security.voms.admin.operations.aa.GetAllUserAttributesOperation;
 import org.glite.security.voms.admin.persistence.error.NoSuchCertificateException;
 import org.glite.security.voms.admin.persistence.error.NoSuchUserException;
 import org.glite.security.voms.admin.persistence.error.SuspendedCertificateException;
 import org.glite.security.voms.admin.persistence.error.SuspendedUserException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class AAServlet extends ACServlet {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
     
     private static final Logger log = LoggerFactory.getLogger(AAServlet.class);
 
     protected void writeErrorResponse(HttpServletResponse response,
 	    int httpErrorCode, String vomsErrorCode, String errorMessage) {
 	
 	try {
 	    
 	    response.setStatus(httpErrorCode);
 	    response.setContentType("text/plain");
 	    response.setCharacterEncoding( "UTF-8" );
 	    response.getWriter().write(String.format("%s: %s", vomsErrorCode, errorMessage));
 	
 	} catch (IOException e) {
 	    
 	}
 	
 
     }
 
     @Override
     protected void doGet(HttpServletRequest request,
 	    HttpServletResponse response) throws ServletException, IOException {
 
 	if (CurrentAdmin.instance().isUnauthenticated()) {
 	    writeErrorResponse(
 		    response,
 		    400,
 		    VOMS_ERROR_BAD_REQUEST,
 		    "Please authenticated with an X509 certificate to obtain a VOMS attribute certificate");
 	    return;
 	}
 
 	
 	String voMember = request.getParameter("dn");
 	
	if (voMember == null || "".equals(voMember))
 	    voMember = CurrentAdmin.instance().getRealSubject();
 	
 		
 	GetAllUserAttributesOperation op = new GetAllUserAttributesOperation(voMember);
 	
 	
 	try{
 	    
 	    VOMSAttributes attrs = (VOMSAttributes) op.execute();
 	    writeResponse(response, attrs);
 	    
 	
 	}catch (VOMSAuthorizationException e) {
 	    
 	    writeErrorResponse(response, 400, VOMS_ERROR_BAD_REQUEST, "The client is not authorized to request VOMS attributes in this VO");
 	    return;
 	    
 	}catch (VOMSException e){
 	    
 	    int httpErrorCode;
 	    String vomsErrorCode;
 
 	    log.error("Error getting VOMS attributes for user '" + voMember
 		    + "':" + e.getMessage());
 
 	    if (e instanceof NoSuchUserException
 		    || e instanceof NoSuchCertificateException) {
 		httpErrorCode = 400;
 		vomsErrorCode = VOMS_ERROR_NO_SUCH_USER;
 	    } else if (e instanceof SuspendedUserException) {
 		httpErrorCode = 400;
 		vomsErrorCode = VOMS_ERROR_SUSPENDED_USER;
 	    } else if (e instanceof SuspendedCertificateException) {
 		httpErrorCode = 400;
 		vomsErrorCode = VOMS_ERROR_SUSPENDED_CERTIFICATE;
 
 	    } else if (e instanceof VOMSSyntaxException) {
 		httpErrorCode = 400;
 		vomsErrorCode = VOMS_ERROR_BAD_REQUEST;
 	    } else {
 		httpErrorCode = 500;
 		vomsErrorCode = VOMS_ERROR_INTERNAL_ERROR;
 	    }
 	    writeErrorResponse(response, httpErrorCode, vomsErrorCode, e
 		    .getMessage());
 	    return;
 	    
 	}catch (Throwable e){
 	    
 	    writeErrorResponse(response, 500, VOMS_ERROR_INTERNAL_ERROR, "Internal server error: "+e.getClass().getSimpleName()+" - "+e.getMessage());
 	    return;
 	}
 
     }
 
     private void writeResponse(HttpServletResponse response,
 	    VOMSAttributes attrs) {
 
 	response.setContentType("text/plain");
 	response.setCharacterEncoding("UTF-8");
 
 	try {
 
 	    response.getWriter()
 		    .write(StringUtils.join(attrs.getFqans(), "\n"));
 	    response.getWriter().write("\n");
 	    response.getWriter().write(
 		    StringUtils.join(attrs.getGenericAttributes(), "\n"));
 
 	} catch (IOException e) {
 
 	    e.printStackTrace();
 	}
 
     }
 }
