 package org.apache.jsp.js;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import javax.servlet.jsp.*;
 import com.adito.boot.SystemProperties;
 
 public final class cookieDetect_jsp extends org.apache.jasper.runtime.HttpJspBase
     implements org.apache.jasper.runtime.JspSourceDependent {
 
   private static java.util.Vector _jspx_dependants;
 
   public java.util.List getDependants() {
     return _jspx_dependants;
   }
 
   public void _jspService(HttpServletRequest request, HttpServletResponse response)
         throws java.io.IOException, ServletException {
 
     JspFactory _jspxFactory = null;
     PageContext pageContext = null;
     HttpSession session = null;
     ServletContext application = null;
     ServletConfig config = null;
     JspWriter out = null;
     Object page = this;
     JspWriter _jspx_out = null;
     PageContext _jspx_page_context = null;
 
 
     try {
       _jspxFactory = JspFactory.getDefaultFactory();
       response.setContentType("text/javascript;charset=UTF-8");
       pageContext = _jspxFactory.getPageContext(this, request, response,
       			null, true, 8192, true);
       _jspx_page_context = pageContext;
       application = pageContext.getServletContext();
       config = pageContext.getServletConfig();
       session = pageContext.getSession();
       out = pageContext.getOut();
       _jspx_out = out;
 
       out.write("\r\n");
       out.write("\r\n");
       out.write("\r\n");
       out.write("/* The server absolutely requires cookies to be able to function correctly\r\n");
       out.write(" * This script fragment detects if they are available and directs to an \r\n");
       out.write(" * information page if not.\r\n");
       out.write(" */\r\n");
       out.write(" \r\n");
       out.write("if(!(document.cookie && document.cookie.indexOf(\"");
      out.print( SystemProperties.get("cookie", "SSLX_SSESHID") );
       out.write("\") > -1)) {\r\n");
       out.write("\twindow.location = '/noCookies.do';\r\n");
       out.write("}");
     } catch (Throwable t) {
       if (!(t instanceof SkipPageException)){
         out = _jspx_out;
         if (out != null && out.getBufferSize() != 0)
           out.clearBuffer();
         if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
       }
     } finally {
       if (_jspxFactory != null) _jspxFactory.releasePageContext(_jspx_page_context);
     }
   }
 }
