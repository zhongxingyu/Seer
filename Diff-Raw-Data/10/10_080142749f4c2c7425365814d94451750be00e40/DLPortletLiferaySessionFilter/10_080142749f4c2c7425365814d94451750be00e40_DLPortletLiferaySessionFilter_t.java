 /**
  * Copyright 26.2.11 (c) DataLite, spol. s r.o. All rights reserved.
  * Web: http://www.datalite.cz    Mail: info@datalite.cz
  */
 package cz.datalite.zk.liferay;
 
import com.liferay.portal.kernel.util.ThreadLocalRegistry;
 import com.liferay.portal.security.auth.PrincipalThreadLocal;
 import com.liferay.portal.security.permission.PermissionThreadLocal;
 import com.liferay.portal.theme.ThemeDisplay;
 import com.liferay.portal.util.WebKeys;
 import org.springframework.web.filter.OncePerRequestFilter;
 
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.io.IOException;
 
 /**
  * Filter to add Liferay Threadlocal variables into AJAX request (which bypass Liferay)
  *
  * Currently PermissionThreadLocal and PrincipalThreadLocal is supported
  */
 public class DLPortletLiferaySessionFilter extends OncePerRequestFilter {
 
     @Override
     protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {


         ThemeDisplay themeDisplay = (ThemeDisplay) request.getAttribute(WebKeys.THEME_DISPLAY);
 
         // in AJAX calls the portal attribute is not available, lookup session attribute - set by DLPortlet.
         if (themeDisplay == null)
         {
             // do NOT create new session EVER!
             // If you create new session here, it will colide with portal session (it may happen after timeout)
             // see http://liferay.datalite.cz/blog/-/blogs/create-session-v-zk-update-requestu
             HttpSession httpSession = request.getSession(false);
 
             if (httpSession != null)
                 themeDisplay = (ThemeDisplay) httpSession.getAttribute(WebKeys.THEME_DISPLAY);
         }
 
         if (themeDisplay != null)
         {
             PermissionThreadLocal.setPermissionChecker(themeDisplay.getPermissionChecker());
             PrincipalThreadLocal.setName(themeDisplay.getUserId());
 
             try {
                 filterChain.doFilter(request, response);
             }
             finally {
                 PermissionThreadLocal.setPermissionChecker(null);
                 PrincipalThreadLocal.setName(null);

                // at the end of each request, reset all thread locals
                // normally it is done by com.liferay.portal.servlet.filters.threadlocal.ThreadLocalFilter
                // with AJAX request, this is bypassed (request does not pass through ROOT webapp)
                ThreadLocalRegistry.resetThreadLocals();
             }
 
         }
         else
         {
             filterChain.doFilter(request, response);
         }
     }
 
 }
