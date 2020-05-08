 package ai.ilikeplaces.servlets.filters;
 
 import ai.ilikeplaces.doc.License;
 import ai.ilikeplaces.logic.Listeners.TemplateSourceGeoBusiness;
 import ai.ilikeplaces.servlets.Controller;
 import ai.ilikeplaces.util.Loggers;
 import ai.ilikeplaces.util.Parameter;
 import ai.ilikeplaces.util.SmartLogger;
 
 import javax.servlet.*;
 import javax.servlet.http.HttpServletRequest;
 import java.io.IOException;
 import java.net.URI;
 
 /**
  * Okay this class if for the profiles. However, still we do not support "very" short urls.
  * Instead, we support www.ilikeplaces/i/username kind of urls here. Looks better huh? :D
  * <p/>
  * <p/>
  * Hey this filter I say again, is to support www.ilikeplaces.com/i/ urls. No other filter should mess with this url too.
  * <p/>
  * <p/>
  * Well, nevertheless, when a request comes in the above format, we redirect it to the
  * ItsNat servlet, (current url format is /page/_i?up=userurl and maybe /page/_i_userurl.
  * We send a redirect there.
  * <p/>
  * <p/>
  * Let's try to make it fast and simple.
  * <p/>
  * <p/>
  * Created by IntelliJ IDEA.
  * User: <a href="http://www.ilikeplaces.com"> http://www.ilikeplaces.com </a>
  * Date: Jun 10, 2010
  * Time: 3:27:18 PM
  */
 
 @License(content = "This code is licensed under GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
 public class SubdomainForward implements Filter {
     private static final String SLASH = "/";
     private static final String SUBDOMAIN_PAGE_FORMAT = "/page/_org?" + "subdomain" + "=";
     private static final String REDIRECTING_USER_TO = "Redirecting User To:";
     private static final String REQUESTED_PATH = "Requested Path:";
     protected static final String DROPPING_SUBDOMAIN_GRAB_FOR = "Dropping Subdomain Grab for:";
     protected static final String WWW = "www";
 
     protected static boolean ON = false;
     protected static final String PROCESSING_SUBDOMAIN_FORWARD = "Processing Subdomain Forward";
     protected static final String EMPTY = "";
     public static final String ON_PARAMETER_IN_WEB_DOT_XML = "ON";
     public static final String ILIKEPLACES_COM = "ilikeplaces.com";
     public static final String LOCALHOST = "localhost";
     public static final String DOMAIN_FORWARD_MAPPING_TO = "Domain Forward mapping to ";
     public static final String SPACE = " ";
 
     @Override
     public void init(final FilterConfig filterConfig) throws ServletException {
         ON = Boolean.valueOf(filterConfig.getInitParameter(ON_PARAMETER_IN_WEB_DOT_XML));
     }
 
     @Override
     public void doFilter(final ServletRequest request, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {
 
         if (!ON) {
             filterChain.doFilter(request, servletResponse);
             return;
         }
 
         final SmartLogger sl = SmartLogger.start(Loggers.LEVEL.DEBUG, null, 100, null, true);
         try {
             final String unformattedurl = ((HttpServletRequest) request).getRequestURL().toString();
 
             sl.appendToLogMSG(REQUESTED_PATH + unformattedurl);
 
             final String domain = new URI(unformattedurl).getHost();
 
             if (domain.endsWith(ILIKEPLACES_COM) || domain.contains(LOCALHOST)) {//Normal request on our website. Just forward.
                 filterChain.doFilter(request, servletResponse);
                 sl.complete(Loggers.LEVEL.DEBUG, "Simple Forward. Done.");
             } else {
                 sl.appendToLogMSG(DOMAIN_FORWARD_MAPPING_TO + domain);
                 request.getRequestDispatcher(new Parameter(Controller.Page.GeoBusiness.getURL()).append(TemplateSourceGeoBusiness.DOMAIN, domain, true).get()).forward(request, servletResponse);
                 sl.complete(Loggers.LEVEL.DEBUG, PROCESSING_SUBDOMAIN_FORWARD + SPACE + Loggers.DONE);
             }
 
 //            if(domain.endsWith(".GeoBusiness.ilikeplaces.com")){//Do this strict checks. We don't need somebody exploiting these stuff as bugs :-/
 //               final String[] temp = domain.split(".GeoBusiness.ilikeplaces.com"); //Escape regesx
 //            }else{
 //                //Not a subdomain forward. drop drop drop. Be very strict
 //            }
 //
 //
 
 //            final String[] domainArr = domain.split(".");
 //            String subdomain = EMPTY;
 //            for (int i = 0; i < domainArr.length - 2/*Disregards co.uk, co.in, us nd goes for com, org type length 3*/; i++) {
 //                subdomain += domainArr[i];
 //            }
 //
 //            if (!subdomain.toLowerCase().equals(WWW) && !subdomain.toLowerCase().equals(EMPTY)) {
 //                sl.appendToLogMSG(REDIRECTING_USER_TO + subdomain);
 //                ((HttpServletResponse) servletResponse).sendRedirect(SUBDOMAIN_PAGE_FORMAT + subdomain);
 //                sl.complete(Loggers.DONE);
 //            } else {
 //                filterChain.doFilter(request, servletResponse);
 //            }
         } catch (final Throwable e) {
            sl.complete(Loggers.LEVEL.ERROR, e);
             filterChain.doFilter(request, servletResponse);
         }
     }
 
     @Override
     public void destroy() {
         //So far nothing to put here
     }
 }
