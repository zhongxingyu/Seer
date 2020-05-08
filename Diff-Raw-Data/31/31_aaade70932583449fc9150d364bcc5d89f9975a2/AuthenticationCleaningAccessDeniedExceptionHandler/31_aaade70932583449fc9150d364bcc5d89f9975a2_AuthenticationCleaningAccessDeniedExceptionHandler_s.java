 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.web.osod;
 
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.web.access.AccessDeniedHandlerImpl;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 
 /**
  * This class prevents application from going into dead loop. Becaus basic flow is:
  * 1). if user authenticated, then check authorization
  * 2). If authorization denied, then handle it with AccessDeniedHandlerImpl by default
  * 3). if user authenticated, then check authorization
  * 4). If authorization denied, then handle it with AccessDeniedHandlerImpl by default
  * ...
 * So this class removes authentication state, so this request will be considered as anonymous and no more authorization attempts will be made before redirect
  *
  * @author dionis
  *         6/28/12 10:01 PM
  */
 public class AuthenticationCleaningAccessDeniedExceptionHandler extends AccessDeniedHandlerImpl {
     @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(null);
         super.handle(request, response, accessDeniedException);
     }
 }
