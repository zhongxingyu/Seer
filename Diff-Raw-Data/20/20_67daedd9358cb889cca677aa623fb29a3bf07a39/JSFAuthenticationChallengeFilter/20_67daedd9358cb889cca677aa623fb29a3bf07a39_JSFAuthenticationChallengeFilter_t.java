 /*
  * jGuard is a security framework based on top of jaas (java authentication and authorization security).
  * it is written for web applications, to resolve simply, access control problems.
  * version $Name$
  * http://sourceforge.net/projects/jguard/
  *
  * Copyright (C) 2004-2011  Charles Lescot
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
 
 package net.sf.jguard.jsf.authentication.filters;
 
 import net.sf.jguard.core.authentication.AuthenticationServicePoint;
import net.sf.jguard.core.authentication.callbackhandler.JGuardCallbackHandler;
 import net.sf.jguard.core.authentication.filters.AuthenticationChallengeFilter;
import net.sf.jguard.core.authentication.schemes.AuthenticationSchemeHandler;
 import net.sf.jguard.jsf.FacesContextAdapter;
import net.sf.jguard.jsf.JSFCallbackHandler;
 
 import javax.inject.Inject;
import java.util.Collection;
 
 /**
  * @author <a href="mailto:diabolo512@users.sourceforge.net">Charles Lescot</a>
  */
 public class JSFAuthenticationChallengeFilter extends AuthenticationChallengeFilter<FacesContextAdapter, FacesContextAdapter> {
 
     @Inject
     public JSFAuthenticationChallengeFilter(AuthenticationServicePoint<FacesContextAdapter, FacesContextAdapter> authenticationServicePoint,
                                            Collection<AuthenticationSchemeHandler<FacesContextAdapter, FacesContextAdapter>> registeredAuthenticationSchemeHandlers) {
        super(authenticationServicePoint, registeredAuthenticationSchemeHandlers);
    }

    @Override
    public JGuardCallbackHandler<FacesContextAdapter, FacesContextAdapter> getCallbackHandler(FacesContextAdapter facesContextAdapter, FacesContextAdapter facesContextAdapter1) {
        return new JSFCallbackHandler(facesContextAdapter, facesContextAdapter, registeredAuthenticationSchemeHandlers);
     }
 }
