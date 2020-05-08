 package org.apache.jsp;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import javax.servlet.jsp.*;
 
 public final class index_jsp extends org.apache.jasper.runtime.HttpJspBase
     implements org.apache.jasper.runtime.JspSourceDependent {
 
   private static final JspFactory _jspxFactory = JspFactory.getDefaultFactory();
 
   private static java.util.List _jspx_dependants;
 
   private javax.el.ExpressionFactory _el_expressionfactory;
   private org.apache.AnnotationProcessor _jsp_annotationprocessor;
 
   public Object getDependants() {
     return _jspx_dependants;
   }
 
   public void _jspInit() {
     _el_expressionfactory = _jspxFactory.getJspApplicationContext(getServletConfig().getServletContext()).getExpressionFactory();
     _jsp_annotationprocessor = (org.apache.AnnotationProcessor) getServletConfig().getServletContext().getAttribute(org.apache.AnnotationProcessor.class.getName());
   }
 
   public void _jspDestroy() {
   }
 
   public void _jspService(HttpServletRequest request, HttpServletResponse response)
         throws java.io.IOException, ServletException {
 
     PageContext pageContext = null;
     HttpSession session = null;
     ServletContext application = null;
     ServletConfig config = null;
     JspWriter out = null;
     Object page = this;
     JspWriter _jspx_out = null;
     PageContext _jspx_page_context = null;
 
 
     try {
       response.setContentType("text/html");
       pageContext = _jspxFactory.getPageContext(this, request, response,
       			null, true, 8192, true);
       _jspx_page_context = pageContext;
       application = pageContext.getServletContext();
       config = pageContext.getServletConfig();
       session = pageContext.getSession();
       out = pageContext.getOut();
       _jspx_out = out;
 
       out.write("<!DOCTYPE html>\n");
       out.write("<!--[if lt IE 7]>      <html class=\"no-js lt-ie9 lt-ie8 lt-ie7\"> <![endif]-->\n");
       out.write("<!--[if IE 7]>         <html class=\"no-js lt-ie9 lt-ie8\"> <![endif]-->\n");
       out.write("<!--[if IE 8]>         <html class=\"no-js lt-ie9\"> <![endif]-->\n");
       out.write("<!--[if gt IE 8]><!--> <html class=\"no-js\"> <!--<![endif]-->\n");
       out.write("    <head>\n");
       out.write("        <meta charset=\"utf-8\">\n");
       out.write("        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\">\n");
       out.write("        <title>SECK Home</title>\n");
       out.write("        <meta name=\"description\" content=\"\">\n");
       out.write("        <meta name=\"viewport\" content=\"width=device-width\">\n");
       out.write("\n");
       out.write("        <link rel=\"stylesheet\" href=\"css/bootstrap.min.css\">\n");
       out.write("        <link rel=\"stylesheet\" href=\"css/bootstrap-responsive.min.css\">\n");
       out.write("        \n");
       out.write("        <!-- User Defined CSS -->\n");
       out.write("        <link rel=\"stylesheet\" href=\"css/main.css\">\n");
       out.write("\n");
       out.write("        <script src=\"js/vendor/modernizr-2.6.1-respond-1.1.0.min.js\"></script>\n");
       out.write("    </head>\n");
       out.write("    <body>\n");
       out.write("        <!--[if lt IE 7]>\n");
       out.write("            <p class=\"chromeframe\">You are using an outdated browser. <a href=\"http://browsehappy.com/\">Upgrade your browser today</a> or <a href=\"http://www.google.com/chromeframe/?redirect=true\">install Google Chrome Frame</a> to better experience this site.</p>\n");
       out.write("        <![endif]-->\n");
       out.write("        \n");
       out.write("        <div class=\"container-fluid\">\n");
       out.write("        \n");
       out.write("        \t<div class=\"row-fluid pagination-centered\">\n");
       out.write("        \t\t<div class=\"span12\"><img style=\"margin:100px auto 20px 0px;\" src=\"img/logo-large.png\" /></div>\n");
       out.write("        \t</div>\n");
       out.write("        \t\n");
       out.write("        \t<div class=\"row-fluid pagination-centered\">\n");
       out.write("        \t\t<div class=\"span12\">\n");
      out.write("\t\t\t\t\t<form class=\"form-search\" action=\"/servlet/hello\" method=\"get\">\n");
       out.write("\t\t\t\t\t<div class=\"input-append\">\n");
       out.write("\t\t\t\t\t  <input type=\"text\" name=\"query\" class=\"input-xxlarge search-query\" x-webkit-speech x-webkit-grammar=\"builtin:search\" placeholder=\"Enter your search term...\" >\n");
       out.write("\t\t\t\t\t  <button type=\"submit\" class=\"btn\"><i class=\"icon-search\"></i> Search</button>\n");
       out.write("\t\t\t\t\t  </div>\n");
       out.write("\t\t\t\t\t</form>\n");
       out.write("\t\t\t\t</div>\n");
       out.write("        \t</div>\n");
       out.write("        \n");
       out.write("        </div>\n");
       out.write("\n");
       out.write("        \n");
       out.write("\n");
       out.write("        <script src=\"//ajax.googleapis.com/ajax/libs/jquery/1.8.2/jquery.min.js\"></script>\n");
       out.write("        <script>window.jQuery || document.write('<script src=\"js/vendor/jquery-1.8.2.min.js\"><\\/script>')</script>\n");
       out.write("\n");
       out.write("        <script src=\"js/vendor/bootstrap.min.js\"></script>\n");
       out.write("\n");
       out.write("\t\t<!-- User defined JS -->\n");
       out.write("        <script src=\"js/main.js\"></script>\n");
       out.write("    </body>\n");
       out.write("</html>");
     } catch (Throwable t) {
       if (!(t instanceof SkipPageException)){
         out = _jspx_out;
         if (out != null && out.getBufferSize() != 0)
           try { out.clearBuffer(); } catch (java.io.IOException e) {}
         if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
       }
     } finally {
       _jspxFactory.releasePageContext(_jspx_page_context);
     }
   }
 }
