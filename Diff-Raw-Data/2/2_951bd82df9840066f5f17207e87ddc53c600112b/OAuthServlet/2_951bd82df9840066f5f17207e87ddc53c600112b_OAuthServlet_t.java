 /*
  * Cip&Ciop
  * Copyright (C) 2012 Stefano Fornari
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY Stefano Fornari, Stefano Fornari
  * DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  */
 package ste.cipeciop.web;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.commons.lang.StringUtils;
 import org.openid4java.OpenIDException;
 import org.openid4java.association.AssociationSessionType;
 import org.openid4java.consumer.ConsumerManager;
 import org.openid4java.consumer.InMemoryConsumerAssociationStore;
 import org.openid4java.consumer.InMemoryNonceVerifier;
 import org.openid4java.consumer.VerificationResult;
 import org.openid4java.discovery.DiscoveryInformation;
 import org.openid4java.discovery.Identifier;
 import org.openid4java.message.AuthRequest;
 import org.openid4java.message.AuthSuccess;
 import org.openid4java.message.MessageException;
 import org.openid4java.message.ParameterList;
 import org.openid4java.message.ax.AxMessage;
 import org.openid4java.message.ax.FetchRequest;
 import org.openid4java.message.ax.FetchResponse;
 import ste.cipeciop.Constants;
 
 /**
  *
  * @author ste
  */
 public class OAuthServlet extends HttpServlet implements Constants {
     
     public static final String PARAM_IS_RETURN = "is_return";
 
     private static final Logger log = Logger.getLogger("ste.cipeciop.web");
     private ConsumerManager consumerManager;
 
     public OAuthServlet() {
         consumerManager = null;
     }
 
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
 
         if (log.isLoggable(Level.INFO)) {
             log.info("OAuthServlet initialization...");
         }
 
         //
         // OAuth 2.0 manager
         //
         createConsumerManager();
     }
 
     /** 
      * Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /** 
      * Handles the HTTP <code>POST</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 
     /** 
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Cip&Ciop authentication servlet";
     }
 
     // ------------------------------------------------------- Protected methods
     /** 
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     protected void processRequest(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         response.setContentType("text/html;charset=UTF-8");
         if ("true".equals(request.getParameter(PARAM_IS_RETURN))) {
             processReturn(request, response);
         } else {
             //String identifier = AUTHENTICATION_SERVER_URL + '/' + request.getParameter("openid");
             String identifier = request.getParameter("openid");
             if (identifier != null) {
                 authRequest(identifier, request, response);
             } else {
                 throw new ServletException("At this point openid cannot be null!");
             }
 
         }
     }
 
     // --------------------------------------------------------- Private methods
     private void processReturn(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         Identifier identifier = verifyResponse(request);
         if (identifier == null) {
             request.getSession().setAttribute(ATTRIBUTE_IDENTIFIER, null);
             getServletContext().getRequestDispatcher("/index.bsh").forward(request, response);
         } else {
             getServletContext().getRequestDispatcher("/cip.bsh").forward(request, response);
         }
     }
 
     private String authRequest(String userSuppliedString,
             HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
         try {
             // configure the return_to URL where your application will receive
             // the authentication responses from the OpenID provider
             // String returnToUrl = "http://example.com/openid";
             String returnToUrl = request.getRequestURL().toString()
                     + "?is_return=true";
 
             // perform discovery on the user-supplied identifier
             List discoveries = consumerManager.discover(userSuppliedString);
 
             // attempt to associate with the OpenID provider
             // and retrieve one service endpoint for authentication
             DiscoveryInformation discovered = consumerManager.associate(discoveries);
 
             // store the discovery information in the user's session
             request.getSession().setAttribute("openid-disc", discovered);
 
             // obtain a AuthRequest message to be sent to the OpenID provider
             AuthRequest authReq = consumerManager.authenticate(discovered, returnToUrl);
 
             // Simple registration example
             addSimpleRegistrationToAuthRequest(request, authReq);
 
             if (!discovered.isVersion2()) {
                 // Option 1: GET HTTP-redirect to the OpenID Provider endpoint
                 // The only method supported in OpenID 1.x
                 // redirect-URL usually limited ~2048 bytes
                 response.sendRedirect(authReq.getDestinationUrl(true));
             } else {
                 // Option 2: HTML FORM Redirection (Allows payloads >2048 bytes)
                 RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/formredirection.jsp");
                 request.setAttribute("prameterMap", request.getParameterMap());
                 request.setAttribute("message", authReq);
                 dispatcher.forward(request, response);
             }
         } catch (OpenIDException e) {
             // present error to the user
             throw new ServletException(e);
         }
 
         return null;
     }
 
     /**
      * Simple Registration Extension example.
      * 
      * @param httpReq
      * @param authReq
      * @throws MessageException
      * @see <a href="http://code.google.com/p/openid4java/wiki/SRegHowTo">Simple Registration HowTo</a>
      * @see <a href="http://openid.net/specs/openid-simple-registration-extension-1_0.html">OpenID Simple Registration Extension 1.0</a>
      */
     private void addSimpleRegistrationToAuthRequest(HttpServletRequest httpReq,
             AuthRequest authReq) throws MessageException {
         FetchRequest fetch = FetchRequest.createFetchRequest();
         fetch.addAttribute("email", "http://axschema.org/contact/email", true);
         fetch.addAttribute("fullname", "http://axschema.org/namePerson", true);
 
         // attach the extension to the authentication request
         authReq.addExtension(fetch);
     }
 
     // --- processing the authentication response ---
     private Identifier verifyResponse(HttpServletRequest request)
             throws ServletException {
         try {
             // extract the parameters from the authentication response
             // (which comes in as a HTTP request from the OpenID provider)
             ParameterList response = new ParameterList(request.getParameterMap());
 
             // retrieve the previously stored discovery information
             DiscoveryInformation discovered = (DiscoveryInformation) request.getSession().getAttribute("openid-disc");
 
             // extract the receiving URL from the HTTP request
             StringBuffer receivingURL = request.getRequestURL();
             String queryString = request.getQueryString();
             if (queryString != null && queryString.length() > 0) {
                 receivingURL.append("?").append(request.getQueryString());
             }
 
             // verify the response; ConsumerManager needs to be the same
             // (static) instance used to place the authentication request
             VerificationResult verification = consumerManager.verify(receivingURL.toString(), response, discovered);
 
             // examine the verification result and extract the verified
             // identifier
             Identifier verified = verification.getVerifiedId();
             if (verified != null) {
                 AuthSuccess authSuccess = (AuthSuccess) verification.getAuthResponse();
 
                 receiveAttributeExchange(request, authSuccess);
 
                 return verified; // success
             }
         } catch (OpenIDException e) {
             // present error to the user
             throw new ServletException(e);
         }
 
         return null;
     }
 
     /**
      * @param httpReq
      * @param authSuccess 
      * @throws MessageException 
      */
     private void receiveAttributeExchange(HttpServletRequest httpReq,
             AuthSuccess authSuccess) throws MessageException {
 
         if (authSuccess.hasExtension(AxMessage.OPENID_NS_AX)) {
             FetchResponse fetchResp = (FetchResponse) authSuccess.getExtension(AxMessage.OPENID_NS_AX);
 
             List aliases = fetchResp.getAttributeAliases();
             Map attributes = new LinkedHashMap();
             for (Iterator iter = aliases.iterator(); iter.hasNext();) {
                 String alias = (String) iter.next();
                 List values = fetchResp.getAttributeValues(alias);
                 if (values.size() > 0) {
                     String[] arr = new String[values.size()];
                     values.toArray(arr);
                     attributes.put(alias, StringUtils.join(arr));
                     if (ALIAS_EMAIL.equals(alias)) {
                        attributes.put(ALIAS_USER_ID, arr[0]);
                     }
                 }
             }
 
             httpReq.getSession().setAttribute(ATTRIBUTE_IDENTIFIER, attributes);
         }
     }
     
     private void createConsumerManager() {
         consumerManager = new ConsumerManager();
         consumerManager.setAssociations(new InMemoryConsumerAssociationStore());
         consumerManager.setNonceVerifier(new InMemoryNonceVerifier(5000));
         consumerManager.setMinAssocSessEnc(AssociationSessionType.DH_SHA256);
     }  
 }
