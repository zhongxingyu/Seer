 package org.dpi.web;
 
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.tiles.locale.impl.DefaultLocaleResolver;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.LocaleResolver;
 import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
 
 
 public class TilesLocaleInterceptor extends HandlerInterceptorAdapter
 {
 	Logger log = LoggerFactory.getLogger(this.getClass());
 	
	@Autowired
	private LocaleResolver localeResolver;
 
 	public TilesLocaleInterceptor()
 	{
 
 	}
 
 	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
 	{
 		if(SecurityContextHolder.getContext()!=null){
 		 if(SecurityContextHolder.getContext().getAuthentication()!=null){
 			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 			
			if(request.getSession()!=null){
				Locale theUserLocale = localeResolver.resolveLocale(request);
				request.getSession().setAttribute(DefaultLocaleResolver.LOCALE_KEY, theUserLocale);
 			}
 			
 		 }
 		
 		}
 		
 		return true;
 	}
 
 }
 
 
