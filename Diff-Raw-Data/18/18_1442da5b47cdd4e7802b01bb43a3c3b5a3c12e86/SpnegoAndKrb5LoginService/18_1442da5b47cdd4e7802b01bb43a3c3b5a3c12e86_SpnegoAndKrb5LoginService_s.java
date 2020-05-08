 /*
  * Copyright (c) 2013, IT Services, Stockholm University
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer.
  *
  * Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  *
  * Neither the name of Stockholm University nor the names of its contributors
  * may be used to endorse or promote products derived from this software
  * without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 
 package se.su.it.svc.security;
 
 import org.eclipse.jetty.security.IdentityService;
 import org.eclipse.jetty.security.LoginService;
 import org.eclipse.jetty.security.SpnegoUserPrincipal;
 import org.eclipse.jetty.server.UserIdentity;
 import org.eclipse.jetty.util.B64Code;
 import org.eclipse.jetty.util.component.AbstractLifeCycle;
 import org.eclipse.jetty.util.log.Log;
 import org.eclipse.jetty.util.log.Logger;
 import org.ietf.jgss.*;
 
 import javax.security.auth.Subject;
 
 /**
  * Handle Negotiate requests for mechs SPNEGO & Krb5, based on org.eclipse.jetty.security.SpnegoLoginService
  */
 public final class SpnegoAndKrb5LoginService extends AbstractLifeCycle implements LoginService {
 
   private static final Logger LOG = Log.getLogger(SpnegoAndKrb5LoginService.class);
 
   public static final String OID_MECH_KRB5   = "1.2.840.113554.1.2.2";
   public static final String OID_MECH_SPNEGO = "1.3.6.1.5.5.2";
 
   private String name;
   private IdentityService service;
   private GSSContext gssContext;
 
   public SpnegoAndKrb5LoginService( String name, String targetName ) throws IllegalStateException, GSSException {
     this.name = name;
 
     gssContext = setupContext(targetName);
 
     if (gssContext == null) {
      throw new IllegalStateException("Failed to establish GSSContext");
     }
   }
 
   public final String getName() {
     return name;
   }
 
   /**
    * Login a user using GSSAPI through SPNEGO or KRB5 mechs.
    *
    * @param username will not be used, credentials contains all that's necessary.
    * @param credentials a auth token String. Expect ClassCastException for anything else.
    * @return a UserIdentity if we succeed or null if we don't.
    */
   public final UserIdentity login(String username, Object credentials) {
     byte[] authToken = B64Code.decode((String)credentials);
 
     try {
       while (!gssContext.isEstablished()) {
         authToken = gssContext.acceptSecContext(authToken,0,authToken.length);
       }
 
       String clientName = gssContext.getSrcName().toString();
       String role = clientName.substring(clientName.indexOf('@') + 1);
 
      LOG.debug("GSSContext: established a security context");
      LOG.debug("Client Principal is: " + gssContext.getSrcName());
      LOG.debug("Server Principal is: " + gssContext.getTargName());
      LOG.debug("Client Default Role: " + role);
 
       SpnegoUserPrincipal user = new SpnegoUserPrincipal(clientName, authToken);
       Subject subject = new Subject();
       subject.getPrincipals().add(user);
 
      return service.newUserIdentity(subject,user, new String[]{role});
     } catch (GSSException gsse) {
       // Can't throw exception forward due to interface implementation
      LOG.warn("Failed while validating credentials: " + gsse.getMessage());
     }
 
     return null;
   }
 
   /**
    * Validate a user identity.
    *
    * @param user the UserIdentity to validate.
    * @return always false
    * @see LoginService#validate(org.eclipse.jetty.server.UserIdentity)
    */
   public final boolean validate(UserIdentity user) {
     return false; // A previously created user identity is never valid.
   }
 
   /**
    * @see org.eclipse.jetty.security.LoginService#getIdentityService() ()
    */
   public final IdentityService getIdentityService() {
     return service;
   }
 
   /**
    * @see LoginService#setIdentityService(org.eclipse.jetty.security.IdentityService)
    */
   public final void setIdentityService(IdentityService service) {
     this.service = service;
   }
 
   /**
    * Not implemented, not needed
    */
   public final void logout(UserIdentity user) {
     // No need to implement.
   }
 
   /**
    * Setup & return a GSSContext.
    *
    * @param targetName the target name (server principal) to use
    * @return a GSSContext.
    * @throws GSSException if something goes wrong with the setup of the context.
    */
   private static GSSContext setupContext(String targetName) throws GSSException {
     Oid[] mechs = {
             new Oid(OID_MECH_KRB5), // Krb5
             new Oid(OID_MECH_SPNEGO) // Spnego
     };
 
     GSSManager manager = GSSManager.getInstance();
     GSSName gssName = manager.createName(targetName, null);
 
     GSSCredential serverCreds = manager.createCredential(gssName, GSSCredential.INDEFINITE_LIFETIME, mechs, GSSCredential.ACCEPT_ONLY);
     return manager.createContext(serverCreds);
   }
 }
