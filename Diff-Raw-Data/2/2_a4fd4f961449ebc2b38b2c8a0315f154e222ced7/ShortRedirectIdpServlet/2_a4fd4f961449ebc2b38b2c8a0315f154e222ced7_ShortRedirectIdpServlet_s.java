 /**-----------------------------------------------------------------------
 
  Copyright (c) 2009-2010, The University of Manchester, United Kingdom.
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:
 
  * Redistributions of source code must retain the above copyright notice, 
  this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright 
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.
  * Neither the name of the The University of Manchester nor the names of 
  its contributors may be used to endorse or promote products derived
  from this software without specific prior written permission.
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  POSSIBILITY OF SUCH DAMAGE.
 
  -----------------------------------------------------------------------*/
 package net.java.dev.sommer.foafssl.login;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URLEncoder;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.security.Signature;
 import java.security.SignatureException;
 import java.security.cert.X509Certificate;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NameNotFoundException;
 import javax.naming.NamingException;
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.java.dev.sommer.foafssl.principals.FoafSslPrincipal;
 
 import org.bouncycastle.util.encoders.Base64;
 
 /**
  * @author Bruno Harbulot (Bruno.Harbulot@manchester.ac.uk)
  * @author Henry Story
  */
 public class ShortRedirectIdpServlet extends AbstractIdpServlet {
     private static final long serialVersionUID = 2L;
 
     public static final transient Logger LOG = Logger.getLogger(ShortRedirectIdpServlet.class
             .getName());
 
     public static final String SIGNATURE_PARAMNAME = "sig";
     public static final String SIGALG_PARAMNAME = "sigalg";
     public static final String TIMESTAMP_PARAMNAME = "ts";
     public static final String WEBID_PARAMNAME = "webid";
     public static final String ERROR_PARAMNAME = "error";
     public static final String AUTHREQISSUER_PARAMNAME = "authreqissuer";
 
     public static final String SHORT_REDIRECT_USAGE_INCLUDE_INITPARAM = "shortRedirectUsageInclude";
     public static final String SHORT_REDIRECT_USAGE_INCLUDE_JNDI_NAME = "foafssl/shortRedirectUsageInclude";
 
     protected volatile String shortRedirectInclude;
 
     @Override
     public void init() throws ServletException {
         super.init();
 
         shortRedirectInclude = getInitParameter(SHORT_REDIRECT_USAGE_INCLUDE_INITPARAM);
 
         try {
             Context initCtx = new InitialContext();
             Context ctx = (Context) initCtx.lookup("java:comp/env");
             try {
                 try {
                     String jndiShortRedirectJsp = (String) ctx
                             .lookup(SHORT_REDIRECT_USAGE_INCLUDE_JNDI_NAME);
                     if (jndiShortRedirectJsp != null) {
                         shortRedirectInclude = jndiShortRedirectJsp;
                     }
                 } catch (NameNotFoundException e) {
                 }
             } finally {
                 if (ctx != null) {
                     ctx.close();
                 }
                 if (initCtx != null) {
                     initCtx.close();
                 }
             }
         } catch (NameNotFoundException e) {
             LOG.log(Level.INFO, "Unable to load JNDI context.", e);
         } catch (NamingException e) {
             LOG.log(Level.INFO, "Unable to load JNDI context.", e);
         }
     }
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 
         request.setAttribute(AbstractIdpServlet.SIGNING_CERT_REQATTR, certificate);
         request.setAttribute(AbstractIdpServlet.SIGNING_PUBKEY_REQATTR, publicKey);
 
         Collection<? extends FoafSslPrincipal> verifiedWebIDs = null;
 
         String replyTo = request.getParameter(AUTHREQISSUER_PARAMNAME);
 
         /*
          * Verifies the certificate passed in the request.
          */
         X509Certificate[] certificates = (X509Certificate[]) request
                 .getAttribute("javax.servlet.request.X509Certificate");
 
         if (certificates == null || certificates.length == 0) {
             if (replyTo != null) {
                 redirect(response, replyTo + "?" + ERROR_PARAMNAME + "=nocert");
                 return;
             }
         } else {
             X509Certificate foafSslCertificate = certificates[0]; //TODO: what about the other certs? should one iterate?
             try {
                 verifiedWebIDs = FOAF_SSL_VERIFIER.verifyFoafSslCertificate(foafSslCertificate);
             } catch (Exception e) {
                 if (replyTo != null) {
                     redirect(response, replyTo + "?" + ERROR_PARAMNAME + "="
                             + URLEncoder.encode(e.getMessage(), "UTF-8"));
                     return;
                 } else {
                     // TODO error message
                 }
             }
             if (replyTo != null) {
                 if ((verifiedWebIDs != null) && (verifiedWebIDs.size() > 0)) {
                     try {
                         String authnResp = createSignedResponse(verifiedWebIDs, replyTo);
                         redirect(response, authnResp);
 
                     } catch (InvalidKeyException e) {
                         LOG.log(Level.SEVERE, "Error when signing the response.", e);
                         redirect(response, replyTo + "?" + ERROR_PARAMNAME + "=IdPError");
                     } catch (NoSuchAlgorithmException e) {
                         LOG.log(Level.SEVERE, "Error when signing the response.", e);
                         redirect(response, replyTo + "?" + ERROR_PARAMNAME + "=IdPError");
                     } catch (SignatureException e) {
                         LOG.log(Level.SEVERE, "Error when signing the response.", e);
                         redirect(response, replyTo + "?" + ERROR_PARAMNAME + "=IdPError");
                     }
                 } else {
                    redirect(response, replyTo + "?" + ERROR_PARAMNAME + "=noWebID");
                 }
                 return;
             }
         }
         //anything that falls through to here, we show the help page
 
         request.setAttribute(AbstractIdpServlet.VERIFIED_WEBID_PRINCIPALS_REQATTR,
                 verifiedWebIDs);
         response.setContentType("text/html");
         RequestDispatcher requestDispatcher = request
                 .getRequestDispatcher(shortRedirectInclude);
         requestDispatcher.include(request, response);
 
     }
 
     /**
      * @param verifiedWebIDs     a list of webIds identifying the user (only the fist will be
      *                           used)
      * @param simpleRequestParam the service that the response is sent to
      * @return the URL of the response with the webid, timestamp appended and
      *         signed
      * @throws NoSuchAlgorithmException
      * @throws UnsupportedEncodingException
      * @throws InvalidKeyException
      * @throws SignatureException
      */
 
     private String createSignedResponse(Collection<? extends FoafSslPrincipal> verifiedWebIDs,
                                         String simpleRequestParam) throws NoSuchAlgorithmException,
             UnsupportedEncodingException, InvalidKeyException, SignatureException {
         /*
          * Reads the FoafSsl simple authn request.
          */
         String authnResp = simpleRequestParam;
 
         String sigAlg = null;
         if ("RSA".equals(privateKey.getAlgorithm())) {
             sigAlg = "SHA1withRSA";
         } else if ("DSA".equals(privateKey.getAlgorithm())) {
             sigAlg = "SHA1withDSA";
         } else {
             throw new NoSuchAlgorithmException("Unsupported key algorithm type.");
         }
 
         URI webId = verifiedWebIDs.iterator().next().getUri();
         authnResp += "?" + WEBID_PARAMNAME + "="
                 + URLEncoder.encode(webId.toASCIIString(), "UTF-8");
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
         authnResp += "&" + TIMESTAMP_PARAMNAME + "="
                 + URLEncoder.encode(dateFormat.format(Calendar.getInstance().getTime()), "UTF-8");
 
         String signedMessage = authnResp;
         Signature signature = Signature.getInstance(sigAlg);
         signature.initSign(privateKey);
         signature.update(signedMessage.getBytes("UTF-8"));
         byte[] signatureBytes = signature.sign();
         authnResp += "&" + SIGNATURE_PARAMNAME + "="
                 + URLEncoder.encode(new String(Base64.encode(signatureBytes)), "UTF-8");
         return authnResp;
     }
 
     /**
      * Redirect request to the given url
      *
      * @param response
      * @param respUrl  1the response Url to redirect to
      */
     private void redirect(HttpServletResponse response, String respUrl) {
         response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
         response.setHeader("Location", respUrl);
     }
 }
