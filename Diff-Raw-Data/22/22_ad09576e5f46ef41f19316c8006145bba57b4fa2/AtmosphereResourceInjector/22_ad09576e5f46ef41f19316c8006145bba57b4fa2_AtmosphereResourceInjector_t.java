 /*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
 package org.atmosphere.jersey;
 
 import com.sun.jersey.core.spi.component.ComponentContext;
 import com.sun.jersey.core.spi.component.ComponentScope;
 import com.sun.jersey.spi.inject.Injectable;
 import com.sun.jersey.spi.inject.InjectableProvider;
 import org.atmosphere.cpr.AtmosphereResource;
 import org.atmosphere.cpr.AtmosphereResourceEvent;
 import org.atmosphere.cpr.AtmosphereServlet;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.ext.Provider;
import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 
 /**
  * Allow {@link AtmosphereResourceEvent} injection via the {@link Context} annotation supported
  * by Jersey.
  *
  * @author Jeanfrancois Arcand
  */
 @Provider
 public class AtmosphereResourceInjector implements InjectableProvider<Context, Type> {
 
     // The current {@link HttpServletRequest}
     @Context
     HttpServletRequest req;
 
     public ComponentScope getScope() {
         return ComponentScope.Singleton;
     }
 
     public Injectable<AtmosphereResource<HttpServletRequest, HttpServletResponse>> getInjectable(ComponentContext ic, Context a, Type c) {
        if (isValidType(c)) {
 
             return new Injectable<AtmosphereResource<HttpServletRequest, HttpServletResponse>>() {
                 /**
                  * Return the current {@link AtmosphereResourceEvent}
                  */
                 public AtmosphereResource<HttpServletRequest, HttpServletResponse> getValue() {
                     return (AtmosphereResource<HttpServletRequest, HttpServletResponse>)
                             req.getAttribute(AtmosphereServlet.ATMOSPHERE_RESOURCE);
                 }
             };
         }
         return null;
     }

    public boolean isValidType(Type c) {
        if (c == AtmosphereResource.class) return true;

        if (c  instanceof ParameterizedType){
            ParameterizedType pt = (ParameterizedType) c;
            if (pt.getRawType() != AtmosphereResource.class) return false;

            if (pt.getActualTypeArguments().length != 2) return false;

            if (pt.getActualTypeArguments()[0] != HttpServletRequest.class) return false;
            if (pt.getActualTypeArguments()[1] != HttpServletResponse.class) return false;

            return true;
        }
        return false;
    }
 }
