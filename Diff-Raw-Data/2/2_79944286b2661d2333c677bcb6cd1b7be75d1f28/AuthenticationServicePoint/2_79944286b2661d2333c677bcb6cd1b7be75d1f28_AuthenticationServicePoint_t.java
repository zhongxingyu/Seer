 /*
  * jGuard is a security framework based on top of jaas (java authentication and authorization security).
  * it is written for web applications, to resolve simply, access control problems.
  * version $Name$
  * http://sourceforge.net/projects/jguard/
  *
  * Copyright (C) 2004-2010  Charles Lescot
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *
  * jGuard project home page:
  * http://sourceforge.net/projects/jguard/
  */
 
 package net.sf.jguard.core.authentication;
 
 import net.sf.jguard.core.authentication.callbackhandler.JGuardCallbackHandler;
 import net.sf.jguard.core.lifecycle.Request;
 import net.sf.jguard.core.lifecycle.Response;
 import net.sf.jguard.core.technology.ImpersonationScopes;
 
 import javax.security.auth.Subject;
 
 /**
  * central point of authentication.
  *
  * @author <a href="mailto:diabolo512@users.sourceforge.net">Charles Lescot</a>
  */
 public interface AuthenticationServicePoint<Req, Res> {
 
 
     /**
      * return <b>true</b> if the user <b>tries</b> to answer to an authentication challenge
      * validated by an AuthenticationSchemeHandler.
      *
      * @param request
      * @param response
      * @return
      */
     boolean answerToChallenge(Request<Req> request, Response<Res> response);
 
     /**
      * authenticate the user.
      * if the {@link net.sf.jguard.core.technology.Scopes} implementation implements the
      * StatefulScopes interface, this method removes a possible old {@link LoginContextWrapperImpl} object,
      * invalidate the session, create a new one, and bound the new Authenticationutils to it.
      *
      * @param request
      * @param response
      * @param callbackHandler
      * @return
      */
     LoginContextWrapper authenticate(Request<Req> request, Response<Res> response, JGuardCallbackHandler<Req, Res> callbackHandler);
 
 
     /**
      * impersonate the current user as a Guest user with the related credentials.
      * it set the NameCallback to <b>guest<b/>,the PasswordCallback to <b>guest</b>,
      * the InetAddressCallback host address and host name to 127.0.0.1 and localhost.
      * a wrapping mechanism for authenticationSchemeHandler and Scopes impersonate
      * the user as a guest, but the underlying authenticationBindings contains the real user.
      *
      * @param request
      * @param response
      * @param impersonationScopes
      * @return wrapper around the Guest Subject
      */
     LoginContextWrapper impersonateAsGuest(Request<Req> request,
                                            Response<Res> response,
                                            ImpersonationScopes impersonationScopes);
 
     /**
     * utility method wrapping impersonateAsGuest method to not handle {@link net.sf.jguard.core.authentication.LoginContextWrapper}
      * and its {@Link net.sf.jguard.core.authentication.AuthenticationStatus}.
      *
      * @param request
      * @param response
      * @param impersonationScopes
      * @param callbackHandler
      * @return
      */
     Subject getGuestSubject(Request<Req> request, Response<Res> response, ImpersonationScopes impersonationScopes, JGuardCallbackHandler callbackHandler);
 
 
     /**
      * return the <i>current</i> {@link Subject}:
      * this method is looking for from the local scope to the global scope.
      * - firstly, looking for the AccessCOntrolContext bound to the Thread.
      * - if not present, and if the authenticationBindings implements StatefulScopes,
      * looking for the Subject present in the session.
      * - if not present or not stateful, looking for the Guest Subject present in the application scope.
      *
      * @return current Subject
      */
     Subject getCurrentSubject();
 
     /**
      * return <b>true</> if the request has triggered a successful authentication, <false/>
      * otherwise.
      *
      * @param request
      * @param response
      * @return
      */
     boolean authenticationSucceededDuringThisRequest(Request<Req> request, Response<Res> response);
 }
