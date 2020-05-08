 package net.java.dev.sommer.foafssl.j2ee.filter;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.security.cert.X509Certificate;
 import java.util.Collection;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import net.java.dev.sommer.foafssl.verifier.*;
 import net.java.dev.sommer.foafssl.principals.FoafSslPrincipal;
 
 /**
  * Hello world!
  * 
  */
 public class FoafSSLFilter implements Filter {
 
     public static final String PRINCIPALS_ATTR_NAME = "net.java.dev.sommer.foafssl.j2ee.principals";
 
     public void init(FilterConfig arg0) throws ServletException {
         // do nothing. Perhaps this sets path regexps arguments?
     }
 
     public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
             throws IOException, ServletException {
         DereferencingFoafSslVerifier verif = new DereferencingFoafSslVerifier();
         X509Certificate[] certs = (X509Certificate[]) req
                 .getAttribute("javax.servlet.request.X509Certificate");
         Collection<? extends FoafSslPrincipal> pls;
         try {
             pls = verif.verifyFoafSslCertificate(certs[0]);
             if (pls == null || pls.size() == 0) {
                 resp.getOutputStream().write("No foaf+ssl certificates".getBytes());
                 return;
             }
        } catch (Exception ex) {
             Logger.getLogger(FoafSSLFilter.class.getName()).log(Level.SEVERE, null, ex);
             resp.getOutputStream().write("cought error doing verification:".getBytes());
             ex.printStackTrace(new PrintStream(resp.getOutputStream()));
             return;
         }
         req.setAttribute(PRINCIPALS_ATTR_NAME, pls);
         chain.doFilter(req, resp);
     }
 
     public void destroy() {
         // do nothing yet
     }
 }
