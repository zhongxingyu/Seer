 package org.apache.jsp.convMovilidad;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import javax.servlet.jsp.*;
 
 public final class listaPropuestas_jsp extends org.apache.jasper.runtime.HttpJspBase
     implements org.apache.jasper.runtime.JspSourceDependent {
 
   private static final JspFactory _jspxFactory = JspFactory.getDefaultFactory();
 
   private static java.util.List _jspx_dependants;
 
   private org.apache.jasper.runtime.TagHandlerPool _005fjspx_005ftagPool_005fc_005fimport_0026_005furl_005fnobody;
   private org.apache.jasper.runtime.TagHandlerPool _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody;
   private org.apache.jasper.runtime.TagHandlerPool _005fjspx_005ftagPool_005fc_005fif_0026_005ftest;
   private org.apache.jasper.runtime.TagHandlerPool _005fjspx_005ftagPool_005fc_005fforEach_0026_005fvarStatus_005fvar_005fitems_005fbegin;
   private org.apache.jasper.runtime.TagHandlerPool _005fjspx_005ftagPool_005fc_005fout_0026_005fvalue_005fnobody;
 
   private javax.el.ExpressionFactory _el_expressionfactory;
   private org.apache.AnnotationProcessor _jsp_annotationprocessor;
 
   public Object getDependants() {
     return _jspx_dependants;
   }
 
   public void _jspInit() {
     _005fjspx_005ftagPool_005fc_005fimport_0026_005furl_005fnobody = org.apache.jasper.runtime.TagHandlerPool.getTagHandlerPool(getServletConfig());
     _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody = org.apache.jasper.runtime.TagHandlerPool.getTagHandlerPool(getServletConfig());
     _005fjspx_005ftagPool_005fc_005fif_0026_005ftest = org.apache.jasper.runtime.TagHandlerPool.getTagHandlerPool(getServletConfig());
     _005fjspx_005ftagPool_005fc_005fforEach_0026_005fvarStatus_005fvar_005fitems_005fbegin = org.apache.jasper.runtime.TagHandlerPool.getTagHandlerPool(getServletConfig());
     _005fjspx_005ftagPool_005fc_005fout_0026_005fvalue_005fnobody = org.apache.jasper.runtime.TagHandlerPool.getTagHandlerPool(getServletConfig());
     _el_expressionfactory = _jspxFactory.getJspApplicationContext(getServletConfig().getServletContext()).getExpressionFactory();
     _jsp_annotationprocessor = (org.apache.AnnotationProcessor) getServletConfig().getServletContext().getAttribute(org.apache.AnnotationProcessor.class.getName());
   }
 
   public void _jspDestroy() {
     _005fjspx_005ftagPool_005fc_005fimport_0026_005furl_005fnobody.release();
     _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.release();
     _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.release();
     _005fjspx_005ftagPool_005fc_005fforEach_0026_005fvarStatus_005fvar_005fitems_005fbegin.release();
     _005fjspx_005ftagPool_005fc_005fout_0026_005fvalue_005fnobody.release();
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
       response.setContentType("text/html; charset=UTF-8");
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
       out.write("<html>\r\n");
       out.write("<head>\r\n");
       if (_jspx_meth_c_005fimport_005f0(_jspx_page_context))
         return;
       out.write('\r');
       out.write('\n');
 session.removeAttribute("movilidad");
 session.removeAttribute("requisitos");
 
       out.write("\r\n");
       out.write("</head>\r\n");
       out.write("<script>\r\n");
       out.write("\tfunction ver(ac,st,id){\r\n");
       out.write("\t\tdocument.lista.accion.value=ac;\r\n");
       out.write("\t\tdocument.lista.id.value=id;\r\n");
       out.write("\t\tdocument.lista.estado.value=st;\r\n");
       out.write("\t\tdocument.lista.action='");
       if (_jspx_meth_c_005furl_005f0(_jspx_page_context))
         return;
       out.write("';\r\n");
       out.write("\t\tdocument.lista.submit();\r\n");
       out.write("\t}\r\n");
       out.write("\r\n");
       out.write("</script>\r\n");
       out.write("<body onLoad=\"mensajeAlert(document.getElementById('msg'));\">\r\n");
       out.write("<br>\r\n");
       if (_jspx_meth_c_005fif_005f0(_jspx_page_context))
         return;
       out.write('\r');
       out.write('\n');
       if (_jspx_meth_c_005fif_005f7(_jspx_page_context))
         return;
       out.write("\r\n");
       out.write("</body>\r\n");
       out.write("</html>\r\n");
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
 
   private boolean _jspx_meth_c_005fimport_005f0(PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:import
     org.apache.taglibs.standard.tag.rt.core.ImportTag _jspx_th_c_005fimport_005f0 = (org.apache.taglibs.standard.tag.rt.core.ImportTag) _005fjspx_005ftagPool_005fc_005fimport_0026_005furl_005fnobody.get(org.apache.taglibs.standard.tag.rt.core.ImportTag.class);
     _jspx_th_c_005fimport_005f0.setPageContext(_jspx_page_context);
     _jspx_th_c_005fimport_005f0.setParent(null);
     // /convMovilidad/listaPropuestas.jsp(6,0) name = url type = null reqTime = true required = true fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fimport_005f0.setUrl("/general.jsp");
     int[] _jspx_push_body_count_c_005fimport_005f0 = new int[] { 0 };
     try {
       int _jspx_eval_c_005fimport_005f0 = _jspx_th_c_005fimport_005f0.doStartTag();
       if (_jspx_th_c_005fimport_005f0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
         return true;
       }
     } catch (Throwable _jspx_exception) {
       while (_jspx_push_body_count_c_005fimport_005f0[0]-- > 0)
         out = _jspx_page_context.popBody();
       _jspx_th_c_005fimport_005f0.doCatch(_jspx_exception);
     } finally {
       _jspx_th_c_005fimport_005f0.doFinally();
       _005fjspx_005ftagPool_005fc_005fimport_0026_005furl_005fnobody.reuse(_jspx_th_c_005fimport_005f0);
     }
     return false;
   }
 
   private boolean _jspx_meth_c_005furl_005f0(PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:url
     org.apache.taglibs.standard.tag.rt.core.UrlTag _jspx_th_c_005furl_005f0 = (org.apache.taglibs.standard.tag.rt.core.UrlTag) _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.get(org.apache.taglibs.standard.tag.rt.core.UrlTag.class);
     _jspx_th_c_005furl_005f0.setPageContext(_jspx_page_context);
     _jspx_th_c_005furl_005f0.setParent(null);
     // /convMovilidad/listaPropuestas.jsp(16,25) name = value type = null reqTime = true required = false fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005furl_005f0.setValue("/movilidad/adminMovilidad.x");
     int _jspx_eval_c_005furl_005f0 = _jspx_th_c_005furl_005f0.doStartTag();
     if (_jspx_th_c_005furl_005f0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f0);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f0);
     return false;
   }
 
   private boolean _jspx_meth_c_005fif_005f0(PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:if
     org.apache.taglibs.standard.tag.rt.core.IfTag _jspx_th_c_005fif_005f0 = (org.apache.taglibs.standard.tag.rt.core.IfTag) _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.get(org.apache.taglibs.standard.tag.rt.core.IfTag.class);
     _jspx_th_c_005fif_005f0.setPageContext(_jspx_page_context);
     _jspx_th_c_005fif_005f0.setParent(null);
     // /convMovilidad/listaPropuestas.jsp(23,0) name = test type = boolean reqTime = true required = true fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fif_005f0.setTest(((java.lang.Boolean) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${!empty requestScope.listaMovilidad}", java.lang.Boolean.class, (PageContext)_jspx_page_context, null, false)).booleanValue());
     int _jspx_eval_c_005fif_005f0 = _jspx_th_c_005fif_005f0.doStartTag();
     if (_jspx_eval_c_005fif_005f0 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
       do {
         out.write(" \r\n");
         out.write("<div align=\"center\">\r\n");
         out.write("\t<fieldset style=\"width:90%;\"><legend>Información General</legend>\r\n");
         out.write("\t<p align=\"center\" class=\"texto1\">Bienvenido al sistema de inscripción para la solicitud de apoyo económico para la apropiación social del conocimiento a partir de presentación de ponencias en modalidad oral en eventos Nacionales y/o Internacionales.</p>\r\n");
         out.write("\t");
         if (_jspx_meth_c_005fif_005f1(_jspx_th_c_005fif_005f0, _jspx_page_context))
           return true;
         out.write('\r');
         out.write('\n');
         out.write('	');
         if (_jspx_meth_c_005fif_005f2(_jspx_th_c_005fif_005f0, _jspx_page_context))
           return true;
         out.write("\r\n");
         out.write("\r\n");
         out.write("\t");
         if (_jspx_meth_c_005fif_005f3(_jspx_th_c_005fif_005f0, _jspx_page_context))
           return true;
         out.write('\r');
         out.write('\n');
         out.write('	');
         if (_jspx_meth_c_005fif_005f4(_jspx_th_c_005fif_005f0, _jspx_page_context))
           return true;
         out.write("\r\n");
         out.write("\r\n");
         out.write("\t</fieldset>\r\n");
         out.write("</div>\r\n");
         out.write("\t<br>\r\n");
         out.write("\r\n");
         out.write("\t<form name=\"lista\">\r\n");
         out.write("\t<input type=\"hidden\" name=\"accion\" value=\"3\">\r\n");
         out.write("\t<input type=\"hidden\" name=\"id\">\r\n");
         out.write("\t<input type=\"hidden\" name=\"estado\">\r\n");
         out.write("\t");
         if (_jspx_meth_c_005fif_005f5(_jspx_th_c_005fif_005f0, _jspx_page_context))
           return true;
         out.write("\r\n");
         out.write("\t</form>\r\n");
         int evalDoAfterBody = _jspx_th_c_005fif_005f0.doAfterBody();
         if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
           break;
       } while (true);
     }
     if (_jspx_th_c_005fif_005f0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f0);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f0);
     return false;
   }
 
   private boolean _jspx_meth_c_005fif_005f1(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f0, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:if
     org.apache.taglibs.standard.tag.rt.core.IfTag _jspx_th_c_005fif_005f1 = (org.apache.taglibs.standard.tag.rt.core.IfTag) _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.get(org.apache.taglibs.standard.tag.rt.core.IfTag.class);
     _jspx_th_c_005fif_005f1.setPageContext(_jspx_page_context);
     _jspx_th_c_005fif_005f1.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f0);
     // /convMovilidad/listaPropuestas.jsp(27,1) name = test type = boolean reqTime = true required = true fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fif_005f1.setTest(((java.lang.Boolean) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${(sessionScope.datosConv.convNumero==1 or sessionScope.datosConv.convNumero==3 or sessionScope.datosConv.convNumero==16) and (sessionScope.persona.papel!=3 and sessionScope.persona.papel!=5)}", java.lang.Boolean.class, (PageContext)_jspx_page_context, null, false)).booleanValue());
     int _jspx_eval_c_005fif_005f1 = _jspx_th_c_005fif_005f1.doStartTag();
     if (_jspx_eval_c_005fif_005f1 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
       do {
         out.write("\r\n");
         out.write("\t<p align=\"center\" class=\"lroja3\">Usted no puede inscribirse en esta convocatoria debido a que su papel en el grupo <b>NO</b> es el de <b>Estudiante</b> de la Universidad Distrital. Si usted es profesor, Favor ingresar a la convocatoria de docentes. Si usted es estudiante, favor modificar el campo \"papel\" en sus datos personales en el menú \"Mis Datos\"</p>\r\n");
         out.write("\t");
         int evalDoAfterBody = _jspx_th_c_005fif_005f1.doAfterBody();
         if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
           break;
       } while (true);
     }
     if (_jspx_th_c_005fif_005f1.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f1);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f1);
     return false;
   }
 
   private boolean _jspx_meth_c_005fif_005f2(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f0, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:if
     org.apache.taglibs.standard.tag.rt.core.IfTag _jspx_th_c_005fif_005f2 = (org.apache.taglibs.standard.tag.rt.core.IfTag) _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.get(org.apache.taglibs.standard.tag.rt.core.IfTag.class);
     _jspx_th_c_005fif_005f2.setPageContext(_jspx_page_context);
     _jspx_th_c_005fif_005f2.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f0);
     // /convMovilidad/listaPropuestas.jsp(30,1) name = test type = boolean reqTime = true required = true fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fif_005f2.setTest(((java.lang.Boolean) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${(sessionScope.datosConv.convNumero==1 or sessionScope.datosConv.convNumero==3 or sessionScope.datosConv.convNumero==16) and (sessionScope.persona.papel==3 or sessionScope.persona.papel==5)}", java.lang.Boolean.class, (PageContext)_jspx_page_context, null, false)).booleanValue());
     int _jspx_eval_c_005fif_005f2 = _jspx_th_c_005fif_005f2.doStartTag();
     if (_jspx_eval_c_005fif_005f2 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
       do {
         out.write("\r\n");
         out.write("\t<p align=\"center\" class=\"texto1\"><br>A continuación usted encontrará el listado de propuestas que ha registrado hasta el momento en el sistema de información.</p>\r\n");
         out.write("\t<p align=\"center\" class=\"texto1\">Si desea una copia de su inscripción, puede dar click en el botón <img border=\"0\" src='");
         if (_jspx_meth_c_005furl_005f1(_jspx_th_c_005fif_005f2, _jspx_page_context))
           return true;
         out.write("'> y esta le será enviada por correo electrónico a la dirección que tiene registrada en el sistema.</p>\r\n");
         out.write("\t<p align=\"center\" class=\"texto1\">Nota: al enviar su inscripción vía correo electrónico, usted encontrará también un código único de su inscripción, tenga en cuenta este código para preguntas, quejas o reclamos.</p>\r\n");
         out.write("\t<div align=\"center\">\r\n");
         out.write("\t\t<a href='");
         if (_jspx_meth_c_005furl_005f2(_jspx_th_c_005fif_005f2, _jspx_page_context))
           return true;
         out.write("'><img border=\"0\" src='");
         if (_jspx_meth_c_005furl_005f3(_jspx_th_c_005fif_005f2, _jspx_page_context))
           return true;
         out.write("'></a>\r\n");
         out.write("\t</div>\r\n");
         out.write("\t");
         int evalDoAfterBody = _jspx_th_c_005fif_005f2.doAfterBody();
         if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
           break;
       } while (true);
     }
     if (_jspx_th_c_005fif_005f2.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f2);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f2);
     return false;
   }
 
   private boolean _jspx_meth_c_005furl_005f1(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f2, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:url
     org.apache.taglibs.standard.tag.rt.core.UrlTag _jspx_th_c_005furl_005f1 = (org.apache.taglibs.standard.tag.rt.core.UrlTag) _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.get(org.apache.taglibs.standard.tag.rt.core.UrlTag.class);
     _jspx_th_c_005furl_005f1.setPageContext(_jspx_page_context);
     _jspx_th_c_005furl_005f1.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f2);
     // /convMovilidad/listaPropuestas.jsp(32,121) name = value type = null reqTime = true required = false fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005furl_005f1.setValue("/comp/img/mail.gif");
     int _jspx_eval_c_005furl_005f1 = _jspx_th_c_005furl_005f1.doStartTag();
     if (_jspx_th_c_005furl_005f1.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f1);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f1);
     return false;
   }
 
   private boolean _jspx_meth_c_005furl_005f2(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f2, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:url
     org.apache.taglibs.standard.tag.rt.core.UrlTag _jspx_th_c_005furl_005f2 = (org.apache.taglibs.standard.tag.rt.core.UrlTag) _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.get(org.apache.taglibs.standard.tag.rt.core.UrlTag.class);
     _jspx_th_c_005furl_005f2.setPageContext(_jspx_page_context);
     _jspx_th_c_005furl_005f2.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f2);
     // /convMovilidad/listaPropuestas.jsp(35,11) name = value type = null reqTime = true required = false fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005furl_005f2.setValue("/convMovilidad/Insercion.jsp");
     int _jspx_eval_c_005furl_005f2 = _jspx_th_c_005furl_005f2.doStartTag();
     if (_jspx_th_c_005furl_005f2.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f2);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f2);
     return false;
   }
 
   private boolean _jspx_meth_c_005furl_005f3(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f2, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:url
     org.apache.taglibs.standard.tag.rt.core.UrlTag _jspx_th_c_005furl_005f3 = (org.apache.taglibs.standard.tag.rt.core.UrlTag) _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.get(org.apache.taglibs.standard.tag.rt.core.UrlTag.class);
     _jspx_th_c_005furl_005f3.setPageContext(_jspx_page_context);
     _jspx_th_c_005furl_005f3.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f2);
     // /convMovilidad/listaPropuestas.jsp(35,79) name = value type = null reqTime = true required = false fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005furl_005f3.setValue("/comp/img/AgregarPropuesta.gif");
     int _jspx_eval_c_005furl_005f3 = _jspx_th_c_005furl_005f3.doStartTag();
     if (_jspx_th_c_005furl_005f3.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f3);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f3);
     return false;
   }
 
   private boolean _jspx_meth_c_005fif_005f3(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f0, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:if
     org.apache.taglibs.standard.tag.rt.core.IfTag _jspx_th_c_005fif_005f3 = (org.apache.taglibs.standard.tag.rt.core.IfTag) _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.get(org.apache.taglibs.standard.tag.rt.core.IfTag.class);
     _jspx_th_c_005fif_005f3.setPageContext(_jspx_page_context);
     _jspx_th_c_005fif_005f3.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f0);
     // /convMovilidad/listaPropuestas.jsp(39,1) name = test type = boolean reqTime = true required = true fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fif_005f3.setTest(((java.lang.Boolean) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${(sessionScope.datosConv.convNumero==2 or sessionScope.datosConv.convNumero==4||sessionScope.datosConv.convNumero==8 or sessionScope.datosConv.convNumero==15) and sessionScope.persona.papel!=1 and sessionScope.persona.papel!=2 and sessionScope.persona.papel!=4  and sessionScope.persona.papel!=6}", java.lang.Boolean.class, (PageContext)_jspx_page_context, null, false)).booleanValue());
     int _jspx_eval_c_005fif_005f3 = _jspx_th_c_005fif_005f3.doStartTag();
     if (_jspx_eval_c_005fif_005f3 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
       do {
         out.write("\r\n");
         out.write("\t<p align=\"center\" class=\"lroja3\">Usted no puede inscribirse en esta convocatoria debido a que su papel en el grupo <b>NO</b> es el de <b>Profesor</b> de la Universidad Distrital. Si usted es estudiante, Favor ingresar a la convocatoria de Estudiantes. Si usted es profesor, favor modificar el campo \"papel\" en sus datos personales en el menú \"Mis Datos\"</p>\r\n");
         out.write("\t");
         int evalDoAfterBody = _jspx_th_c_005fif_005f3.doAfterBody();
         if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
           break;
       } while (true);
     }
     if (_jspx_th_c_005fif_005f3.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f3);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f3);
     return false;
   }
 
   private boolean _jspx_meth_c_005fif_005f4(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f0, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:if
     org.apache.taglibs.standard.tag.rt.core.IfTag _jspx_th_c_005fif_005f4 = (org.apache.taglibs.standard.tag.rt.core.IfTag) _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.get(org.apache.taglibs.standard.tag.rt.core.IfTag.class);
     _jspx_th_c_005fif_005f4.setPageContext(_jspx_page_context);
     _jspx_th_c_005fif_005f4.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f0);
     // /convMovilidad/listaPropuestas.jsp(42,1) name = test type = boolean reqTime = true required = true fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fif_005f4.setTest(((java.lang.Boolean) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${(sessionScope.datosConv.convNumero==2 or sessionScope.datosConv.convNumero==4 ||sessionScope.datosConv.convNumero==8 or sessionScope.datosConv.convNumero==15) and (sessionScope.persona.papel==1 or sessionScope.persona.papel==2 or sessionScope.persona.papel==4  or sessionScope.persona.papel==6)}", java.lang.Boolean.class, (PageContext)_jspx_page_context, null, false)).booleanValue());
     int _jspx_eval_c_005fif_005f4 = _jspx_th_c_005fif_005f4.doStartTag();
     if (_jspx_eval_c_005fif_005f4 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
       do {
         out.write("\r\n");
         out.write("\t<p align=\"center\" class=\"texto1\"><br>A continuación usted encontrará el listado de propuestas que ha registrado hasta el momento en el sistema de información.</p>\r\n");
         out.write("\t<p align=\"center\" class=\"texto1\">Si desea una copia de su inscripción, puede dar click en el botón <img border=\"0\" src='");
         if (_jspx_meth_c_005furl_005f4(_jspx_th_c_005fif_005f4, _jspx_page_context))
           return true;
         out.write("'> y esta le será enviada por correo electrónico a la dirección que tiene registrada en el sistema.</p>\r\n");
         out.write("\t<p align=\"center\" class=\"texto1\">Nota: al enviar su inscripción vía correo electrónico, usted encontrará también un código único de su inscripción, tenga en cuenta este código para preguntas, quejas o reclamos.</p>\r\n");
         out.write("\t<div align=\"center\">\r\n");
         out.write("\t\t<a href='");
         if (_jspx_meth_c_005furl_005f5(_jspx_th_c_005fif_005f4, _jspx_page_context))
           return true;
         out.write("'><img border=\"0\" src='");
         if (_jspx_meth_c_005furl_005f6(_jspx_th_c_005fif_005f4, _jspx_page_context))
           return true;
         out.write("'></a>\r\n");
         out.write("\t</div>\r\n");
         out.write("\t");
         int evalDoAfterBody = _jspx_th_c_005fif_005f4.doAfterBody();
         if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
           break;
       } while (true);
     }
     if (_jspx_th_c_005fif_005f4.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f4);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f4);
     return false;
   }
 
   private boolean _jspx_meth_c_005furl_005f4(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f4, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:url
     org.apache.taglibs.standard.tag.rt.core.UrlTag _jspx_th_c_005furl_005f4 = (org.apache.taglibs.standard.tag.rt.core.UrlTag) _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.get(org.apache.taglibs.standard.tag.rt.core.UrlTag.class);
     _jspx_th_c_005furl_005f4.setPageContext(_jspx_page_context);
     _jspx_th_c_005furl_005f4.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f4);
     // /convMovilidad/listaPropuestas.jsp(44,121) name = value type = null reqTime = true required = false fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005furl_005f4.setValue("/comp/img/mail.gif");
     int _jspx_eval_c_005furl_005f4 = _jspx_th_c_005furl_005f4.doStartTag();
     if (_jspx_th_c_005furl_005f4.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f4);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f4);
     return false;
   }
 
   private boolean _jspx_meth_c_005furl_005f5(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f4, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:url
     org.apache.taglibs.standard.tag.rt.core.UrlTag _jspx_th_c_005furl_005f5 = (org.apache.taglibs.standard.tag.rt.core.UrlTag) _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.get(org.apache.taglibs.standard.tag.rt.core.UrlTag.class);
     _jspx_th_c_005furl_005f5.setPageContext(_jspx_page_context);
     _jspx_th_c_005furl_005f5.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f4);
     // /convMovilidad/listaPropuestas.jsp(47,11) name = value type = null reqTime = true required = false fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005furl_005f5.setValue("/convMovilidad/Insercion.jsp");
     int _jspx_eval_c_005furl_005f5 = _jspx_th_c_005furl_005f5.doStartTag();
     if (_jspx_th_c_005furl_005f5.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f5);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f5);
     return false;
   }
 
   private boolean _jspx_meth_c_005furl_005f6(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f4, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:url
     org.apache.taglibs.standard.tag.rt.core.UrlTag _jspx_th_c_005furl_005f6 = (org.apache.taglibs.standard.tag.rt.core.UrlTag) _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.get(org.apache.taglibs.standard.tag.rt.core.UrlTag.class);
     _jspx_th_c_005furl_005f6.setPageContext(_jspx_page_context);
     _jspx_th_c_005furl_005f6.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f4);
     // /convMovilidad/listaPropuestas.jsp(47,79) name = value type = null reqTime = true required = false fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005furl_005f6.setValue("/comp/img/AgregarPropuesta.gif");
     int _jspx_eval_c_005furl_005f6 = _jspx_th_c_005furl_005f6.doStartTag();
     if (_jspx_th_c_005furl_005f6.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f6);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f6);
     return false;
   }
 
   private boolean _jspx_meth_c_005fif_005f5(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f0, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:if
     org.apache.taglibs.standard.tag.rt.core.IfTag _jspx_th_c_005fif_005f5 = (org.apache.taglibs.standard.tag.rt.core.IfTag) _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.get(org.apache.taglibs.standard.tag.rt.core.IfTag.class);
     _jspx_th_c_005fif_005f5.setPageContext(_jspx_page_context);
     _jspx_th_c_005fif_005f5.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f0);
     // /convMovilidad/listaPropuestas.jsp(59,1) name = test type = boolean reqTime = true required = true fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
    _jspx_th_c_005fif_005f5.setTest(((java.lang.Boolean) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${((sessionScope.datosConv.convNumero==1 or sessionScope.datosConv.convNumero==3) and (sessionScope.persona.papel==3 or sessionScope.persona.papel==5))or(sessionScope.datosConv.convNumero==4 and (sessionScope.persona.papel!=3 and sessionScope.persona.papel!=5))}", java.lang.Boolean.class, (PageContext)_jspx_page_context, null, false)).booleanValue());
     int _jspx_eval_c_005fif_005f5 = _jspx_th_c_005fif_005f5.doStartTag();
     if (_jspx_eval_c_005fif_005f5 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
       do {
         out.write("\r\n");
         out.write("\t\t<table align=\"center\" class=\"tablas\" width=\"90%\">\r\n");
         out.write("\t\t<caption>Lista de Propuestas inscritas</caption>\r\n");
         out.write("\t\t<tr>\r\n");
         out.write("\t\t\t<td class=\"renglones\" align=\"center\"><b>#</b></td>\r\n");
         out.write("\t\t\t<td class=\"renglones\" align=\"center\"><b>Nombre Propuesta</b></td>\r\n");
         out.write("\t\t\t<td width=\"140px\" class=\"renglones\" align=\"center\"><b>Lugar</b></td>\r\n");
         out.write("\t\t\t<td class=\"renglones\" align=\"center\" width=\"100px\"><b>Documentos</b></td>\r\n");
         out.write("\t\t\t<td class=\"renglones\" align=\"center\" width=\"18px\"><b>Mail</b></td>\r\n");
         out.write("\t\t</tr>\r\n");
         out.write("\t\t");
         if (_jspx_meth_c_005fforEach_005f0(_jspx_th_c_005fif_005f5, _jspx_page_context))
           return true;
         out.write("\r\n");
         out.write("\t</table>\r\n");
         out.write("\t");
         int evalDoAfterBody = _jspx_th_c_005fif_005f5.doAfterBody();
         if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
           break;
       } while (true);
     }
     if (_jspx_th_c_005fif_005f5.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f5);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f5);
     return false;
   }
 
   private boolean _jspx_meth_c_005fforEach_005f0(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f5, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:forEach
     org.apache.taglibs.standard.tag.rt.core.ForEachTag _jspx_th_c_005fforEach_005f0 = (org.apache.taglibs.standard.tag.rt.core.ForEachTag) _005fjspx_005ftagPool_005fc_005fforEach_0026_005fvarStatus_005fvar_005fitems_005fbegin.get(org.apache.taglibs.standard.tag.rt.core.ForEachTag.class);
     _jspx_th_c_005fforEach_005f0.setPageContext(_jspx_page_context);
     _jspx_th_c_005fforEach_005f0.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f5);
     // /convMovilidad/listaPropuestas.jsp(69,2) name = items type = java.lang.Object reqTime = true required = false fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fforEach_005f0.setItems((java.lang.Object) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${requestScope.listaMovilidad}", java.lang.Object.class, (PageContext)_jspx_page_context, null, false));
     // /convMovilidad/listaPropuestas.jsp(69,2) name = begin type = int reqTime = true required = false fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fforEach_005f0.setBegin(0);
     // /convMovilidad/listaPropuestas.jsp(69,2) name = var type = java.lang.String reqTime = false required = false fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fforEach_005f0.setVar("lista");
     // /convMovilidad/listaPropuestas.jsp(69,2) name = varStatus type = java.lang.String reqTime = false required = false fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fforEach_005f0.setVarStatus("st");
     int[] _jspx_push_body_count_c_005fforEach_005f0 = new int[] { 0 };
     try {
       int _jspx_eval_c_005fforEach_005f0 = _jspx_th_c_005fforEach_005f0.doStartTag();
       if (_jspx_eval_c_005fforEach_005f0 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
         do {
           out.write("\r\n");
           out.write("\t\t\t<tr ");
           if (_jspx_meth_c_005fif_005f6(_jspx_th_c_005fforEach_005f0, _jspx_page_context, _jspx_push_body_count_c_005fforEach_005f0))
             return true;
           out.write(">\r\n");
           out.write("\t\t\t\t<td>");
           if (_jspx_meth_c_005fout_005f0(_jspx_th_c_005fforEach_005f0, _jspx_page_context, _jspx_push_body_count_c_005fforEach_005f0))
             return true;
           out.write("</td>\r\n");
           out.write("\t\t\t\t<td>");
           if (_jspx_meth_c_005fout_005f1(_jspx_th_c_005fforEach_005f0, _jspx_page_context, _jspx_push_body_count_c_005fforEach_005f0))
             return true;
           out.write("</td>\r\n");
           out.write("\t\t\t\t<td width=\"140px\">");
           if (_jspx_meth_c_005fout_005f2(_jspx_th_c_005fforEach_005f0, _jspx_page_context, _jspx_push_body_count_c_005fforEach_005f0))
             return true;
           out.write(' ');
           out.write('-');
           if (_jspx_meth_c_005fout_005f3(_jspx_th_c_005fforEach_005f0, _jspx_page_context, _jspx_push_body_count_c_005fforEach_005f0))
             return true;
           out.write("</td>\r\n");
           out.write("\t\t\t\t<td width=\"100px\" valign=\"middle\">\r\n");
           out.write("\t\t\t\t\t<img src='");
           if (_jspx_meth_c_005furl_005f7(_jspx_th_c_005fforEach_005f0, _jspx_page_context, _jspx_push_body_count_c_005fforEach_005f0))
             return true;
           out.write("' onclick='ver(9,1,");
           if (_jspx_meth_c_005fout_005f4(_jspx_th_c_005fforEach_005f0, _jspx_page_context, _jspx_push_body_count_c_005fforEach_005f0))
             return true;
           out.write(")'>\r\n");
           out.write("\t\t\t\t</td>\r\n");
           out.write("\t\t\t</tr>\r\n");
           out.write("\t\t");
           int evalDoAfterBody = _jspx_th_c_005fforEach_005f0.doAfterBody();
           if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
             break;
         } while (true);
       }
       if (_jspx_th_c_005fforEach_005f0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
         return true;
       }
     } catch (Throwable _jspx_exception) {
       while (_jspx_push_body_count_c_005fforEach_005f0[0]-- > 0)
         out = _jspx_page_context.popBody();
       _jspx_th_c_005fforEach_005f0.doCatch(_jspx_exception);
     } finally {
       _jspx_th_c_005fforEach_005f0.doFinally();
       _005fjspx_005ftagPool_005fc_005fforEach_0026_005fvarStatus_005fvar_005fitems_005fbegin.reuse(_jspx_th_c_005fforEach_005f0);
     }
     return false;
   }
 
   private boolean _jspx_meth_c_005fif_005f6(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fforEach_005f0, PageContext _jspx_page_context, int[] _jspx_push_body_count_c_005fforEach_005f0)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:if
     org.apache.taglibs.standard.tag.rt.core.IfTag _jspx_th_c_005fif_005f6 = (org.apache.taglibs.standard.tag.rt.core.IfTag) _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.get(org.apache.taglibs.standard.tag.rt.core.IfTag.class);
     _jspx_th_c_005fif_005f6.setPageContext(_jspx_page_context);
     _jspx_th_c_005fif_005f6.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fforEach_005f0);
     // /convMovilidad/listaPropuestas.jsp(70,7) name = test type = boolean reqTime = true required = true fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fif_005f6.setTest(((java.lang.Boolean) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${st.count mod 2==0}", java.lang.Boolean.class, (PageContext)_jspx_page_context, null, false)).booleanValue());
     int _jspx_eval_c_005fif_005f6 = _jspx_th_c_005fif_005f6.doStartTag();
     if (_jspx_eval_c_005fif_005f6 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
       do {
         out.write("class=\"trb\"");
         int evalDoAfterBody = _jspx_th_c_005fif_005f6.doAfterBody();
         if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
           break;
       } while (true);
     }
     if (_jspx_th_c_005fif_005f6.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f6);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f6);
     return false;
   }
 
   private boolean _jspx_meth_c_005fout_005f0(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fforEach_005f0, PageContext _jspx_page_context, int[] _jspx_push_body_count_c_005fforEach_005f0)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:out
     org.apache.taglibs.standard.tag.rt.core.OutTag _jspx_th_c_005fout_005f0 = (org.apache.taglibs.standard.tag.rt.core.OutTag) _005fjspx_005ftagPool_005fc_005fout_0026_005fvalue_005fnobody.get(org.apache.taglibs.standard.tag.rt.core.OutTag.class);
     _jspx_th_c_005fout_005f0.setPageContext(_jspx_page_context);
     _jspx_th_c_005fout_005f0.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fforEach_005f0);
     // /convMovilidad/listaPropuestas.jsp(71,8) name = value type = null reqTime = true required = true fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fout_005f0.setValue((java.lang.Object) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${st.count}", java.lang.Object.class, (PageContext)_jspx_page_context, null, false));
     int _jspx_eval_c_005fout_005f0 = _jspx_th_c_005fout_005f0.doStartTag();
     if (_jspx_th_c_005fout_005f0.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005fout_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005fout_005f0);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005fout_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005fout_005f0);
     return false;
   }
 
   private boolean _jspx_meth_c_005fout_005f1(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fforEach_005f0, PageContext _jspx_page_context, int[] _jspx_push_body_count_c_005fforEach_005f0)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:out
     org.apache.taglibs.standard.tag.rt.core.OutTag _jspx_th_c_005fout_005f1 = (org.apache.taglibs.standard.tag.rt.core.OutTag) _005fjspx_005ftagPool_005fc_005fout_0026_005fvalue_005fnobody.get(org.apache.taglibs.standard.tag.rt.core.OutTag.class);
     _jspx_th_c_005fout_005f1.setPageContext(_jspx_page_context);
     _jspx_th_c_005fout_005f1.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fforEach_005f0);
     // /convMovilidad/listaPropuestas.jsp(72,8) name = value type = null reqTime = true required = true fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fout_005f1.setValue((java.lang.Object) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${lista.nombrePonencia}", java.lang.Object.class, (PageContext)_jspx_page_context, null, false));
     int _jspx_eval_c_005fout_005f1 = _jspx_th_c_005fout_005f1.doStartTag();
     if (_jspx_th_c_005fout_005f1.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005fout_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005fout_005f1);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005fout_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005fout_005f1);
     return false;
   }
 
   private boolean _jspx_meth_c_005fout_005f2(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fforEach_005f0, PageContext _jspx_page_context, int[] _jspx_push_body_count_c_005fforEach_005f0)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:out
     org.apache.taglibs.standard.tag.rt.core.OutTag _jspx_th_c_005fout_005f2 = (org.apache.taglibs.standard.tag.rt.core.OutTag) _005fjspx_005ftagPool_005fc_005fout_0026_005fvalue_005fnobody.get(org.apache.taglibs.standard.tag.rt.core.OutTag.class);
     _jspx_th_c_005fout_005f2.setPageContext(_jspx_page_context);
     _jspx_th_c_005fout_005f2.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fforEach_005f0);
     // /convMovilidad/listaPropuestas.jsp(73,22) name = value type = null reqTime = true required = true fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fout_005f2.setValue((java.lang.Object) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${lista.pais}", java.lang.Object.class, (PageContext)_jspx_page_context, null, false));
     int _jspx_eval_c_005fout_005f2 = _jspx_th_c_005fout_005f2.doStartTag();
     if (_jspx_th_c_005fout_005f2.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005fout_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005fout_005f2);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005fout_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005fout_005f2);
     return false;
   }
 
   private boolean _jspx_meth_c_005fout_005f3(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fforEach_005f0, PageContext _jspx_page_context, int[] _jspx_push_body_count_c_005fforEach_005f0)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:out
     org.apache.taglibs.standard.tag.rt.core.OutTag _jspx_th_c_005fout_005f3 = (org.apache.taglibs.standard.tag.rt.core.OutTag) _005fjspx_005ftagPool_005fc_005fout_0026_005fvalue_005fnobody.get(org.apache.taglibs.standard.tag.rt.core.OutTag.class);
     _jspx_th_c_005fout_005f3.setPageContext(_jspx_page_context);
     _jspx_th_c_005fout_005f3.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fforEach_005f0);
     // /convMovilidad/listaPropuestas.jsp(73,55) name = value type = null reqTime = true required = true fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fout_005f3.setValue((java.lang.Object) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${lista.ciudad}", java.lang.Object.class, (PageContext)_jspx_page_context, null, false));
     int _jspx_eval_c_005fout_005f3 = _jspx_th_c_005fout_005f3.doStartTag();
     if (_jspx_th_c_005fout_005f3.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005fout_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005fout_005f3);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005fout_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005fout_005f3);
     return false;
   }
 
   private boolean _jspx_meth_c_005furl_005f7(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fforEach_005f0, PageContext _jspx_page_context, int[] _jspx_push_body_count_c_005fforEach_005f0)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:url
     org.apache.taglibs.standard.tag.rt.core.UrlTag _jspx_th_c_005furl_005f7 = (org.apache.taglibs.standard.tag.rt.core.UrlTag) _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.get(org.apache.taglibs.standard.tag.rt.core.UrlTag.class);
     _jspx_th_c_005furl_005f7.setPageContext(_jspx_page_context);
     _jspx_th_c_005furl_005f7.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fforEach_005f0);
     // /convMovilidad/listaPropuestas.jsp(75,15) name = value type = null reqTime = true required = false fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005furl_005f7.setValue("/comp/img/Modificar.gif");
     int _jspx_eval_c_005furl_005f7 = _jspx_th_c_005furl_005f7.doStartTag();
     if (_jspx_th_c_005furl_005f7.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f7);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f7);
     return false;
   }
 
   private boolean _jspx_meth_c_005fout_005f4(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fforEach_005f0, PageContext _jspx_page_context, int[] _jspx_push_body_count_c_005fforEach_005f0)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:out
     org.apache.taglibs.standard.tag.rt.core.OutTag _jspx_th_c_005fout_005f4 = (org.apache.taglibs.standard.tag.rt.core.OutTag) _005fjspx_005ftagPool_005fc_005fout_0026_005fvalue_005fnobody.get(org.apache.taglibs.standard.tag.rt.core.OutTag.class);
     _jspx_th_c_005fout_005f4.setPageContext(_jspx_page_context);
     _jspx_th_c_005fout_005f4.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fforEach_005f0);
     // /convMovilidad/listaPropuestas.jsp(75,74) name = value type = null reqTime = true required = true fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fout_005f4.setValue((java.lang.Object) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${lista.idPropuesta}", java.lang.Object.class, (PageContext)_jspx_page_context, null, false));
     int _jspx_eval_c_005fout_005f4 = _jspx_th_c_005fout_005f4.doStartTag();
     if (_jspx_th_c_005fout_005f4.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005fout_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005fout_005f4);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005fout_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005fout_005f4);
     return false;
   }
 
   private boolean _jspx_meth_c_005fif_005f7(PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:if
     org.apache.taglibs.standard.tag.rt.core.IfTag _jspx_th_c_005fif_005f7 = (org.apache.taglibs.standard.tag.rt.core.IfTag) _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.get(org.apache.taglibs.standard.tag.rt.core.IfTag.class);
     _jspx_th_c_005fif_005f7.setPageContext(_jspx_page_context);
     _jspx_th_c_005fif_005f7.setParent(null);
     // /convMovilidad/listaPropuestas.jsp(83,0) name = test type = boolean reqTime = true required = true fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fif_005f7.setTest(((java.lang.Boolean) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${empty requestScope.listaMovilidad}", java.lang.Boolean.class, (PageContext)_jspx_page_context, null, false)).booleanValue());
     int _jspx_eval_c_005fif_005f7 = _jspx_th_c_005fif_005f7.doStartTag();
     if (_jspx_eval_c_005fif_005f7 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
       do {
         out.write("\r\n");
         out.write("<br><br><br>\r\n");
         out.write("<div align=\"center\">\r\n");
         out.write("\t<fieldset style=\"width: 90%\"><legend>Información General </legend>\r\n");
         out.write("\t\t<p align=\"center\" class=\"texto1\">Bienvenido al sistema de inscripción para la solicitud de apoyo económico para la apropiación social del conocimiento a partir de presentación de ponencias en modalidad oral en eventos Nacionales y/o Internacionales.</p>\r\n");
         out.write("\r\n");
         out.write("\t\t");
         if (_jspx_meth_c_005fif_005f8(_jspx_th_c_005fif_005f7, _jspx_page_context))
           return true;
         out.write("\r\n");
         out.write("\t\t");
         if (_jspx_meth_c_005fif_005f9(_jspx_th_c_005fif_005f7, _jspx_page_context))
           return true;
         out.write("\r\n");
         out.write("\r\n");
         out.write("\t\t");
         if (_jspx_meth_c_005fif_005f10(_jspx_th_c_005fif_005f7, _jspx_page_context))
           return true;
         out.write("\r\n");
         out.write("\t\t");
         if (_jspx_meth_c_005fif_005f11(_jspx_th_c_005fif_005f7, _jspx_page_context))
           return true;
         out.write("\r\n");
         out.write("\t</fieldset>\r\n");
         out.write("</div>\r\n");
         int evalDoAfterBody = _jspx_th_c_005fif_005f7.doAfterBody();
         if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
           break;
       } while (true);
     }
     if (_jspx_th_c_005fif_005f7.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f7);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f7);
     return false;
   }
 
   private boolean _jspx_meth_c_005fif_005f8(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f7, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:if
     org.apache.taglibs.standard.tag.rt.core.IfTag _jspx_th_c_005fif_005f8 = (org.apache.taglibs.standard.tag.rt.core.IfTag) _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.get(org.apache.taglibs.standard.tag.rt.core.IfTag.class);
     _jspx_th_c_005fif_005f8.setPageContext(_jspx_page_context);
     _jspx_th_c_005fif_005f8.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f7);
     // /convMovilidad/listaPropuestas.jsp(89,2) name = test type = boolean reqTime = true required = true fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fif_005f8.setTest(((java.lang.Boolean) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${(sessionScope.datosConv.convNumero==1 or sessionScope.datosConv.convNumero==3 or sessionScope.datosConv.convNumero==16) and !(sessionScope.persona.papel==3 or sessionScope.persona.papel==5)}", java.lang.Boolean.class, (PageContext)_jspx_page_context, null, false)).booleanValue());
     int _jspx_eval_c_005fif_005f8 = _jspx_th_c_005fif_005f8.doStartTag();
     if (_jspx_eval_c_005fif_005f8 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
       do {
         out.write("\r\n");
         out.write("\t\t<p align=\"center\" class=\"lroja3\">Usted no puede inscribirse en esta convocatoria debido a que su papel en el grupo <b>NO</b> es el de <b>Estudiante</b> de la Universidad Distrital. Si usted es profesor, Favor ingresar a la convocatoria de docentes. Si usted es estudiante, favor modificar el campo \"papel\" en sus datos personales en el menú \"Mis Datos\"</p>\r\n");
         out.write("\t\t");
         int evalDoAfterBody = _jspx_th_c_005fif_005f8.doAfterBody();
         if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
           break;
       } while (true);
     }
     if (_jspx_th_c_005fif_005f8.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f8);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f8);
     return false;
   }
 
   private boolean _jspx_meth_c_005fif_005f9(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f7, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:if
     org.apache.taglibs.standard.tag.rt.core.IfTag _jspx_th_c_005fif_005f9 = (org.apache.taglibs.standard.tag.rt.core.IfTag) _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.get(org.apache.taglibs.standard.tag.rt.core.IfTag.class);
     _jspx_th_c_005fif_005f9.setPageContext(_jspx_page_context);
     _jspx_th_c_005fif_005f9.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f7);
     // /convMovilidad/listaPropuestas.jsp(92,2) name = test type = boolean reqTime = true required = true fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fif_005f9.setTest(((java.lang.Boolean) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${(sessionScope.datosConv.convNumero==1 or  sessionScope.datosConv.convNumero==3 or sessionScope.datosConv.convNumero==16) and (sessionScope.persona.papel==3 or sessionScope.persona.papel==5)}", java.lang.Boolean.class, (PageContext)_jspx_page_context, null, false)).booleanValue());
     int _jspx_eval_c_005fif_005f9 = _jspx_th_c_005fif_005f9.doStartTag();
     if (_jspx_eval_c_005fif_005f9 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
       do {
         out.write("\r\n");
         out.write("\t\t<p align=\"center\" class=\"texto1\">En estos momentos usted no tiene ninguna propuesta inscrita, Favor dar clik en el botón \"Agregar Propuesta\" para insertar una nueva propuesta</p>\r\n");
         out.write("\t\t<div align=\"center\">\r\n");
         out.write("\t\t\t<a href='");
         if (_jspx_meth_c_005furl_005f8(_jspx_th_c_005fif_005f9, _jspx_page_context))
           return true;
         out.write("'><img border=\"0\" src='");
         if (_jspx_meth_c_005furl_005f9(_jspx_th_c_005fif_005f9, _jspx_page_context))
           return true;
         out.write("'></a>\r\n");
         out.write("\t\t</div>\r\n");
         out.write("\t\t");
         int evalDoAfterBody = _jspx_th_c_005fif_005f9.doAfterBody();
         if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
           break;
       } while (true);
     }
     if (_jspx_th_c_005fif_005f9.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f9);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f9);
     return false;
   }
 
   private boolean _jspx_meth_c_005furl_005f8(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f9, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:url
     org.apache.taglibs.standard.tag.rt.core.UrlTag _jspx_th_c_005furl_005f8 = (org.apache.taglibs.standard.tag.rt.core.UrlTag) _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.get(org.apache.taglibs.standard.tag.rt.core.UrlTag.class);
     _jspx_th_c_005furl_005f8.setPageContext(_jspx_page_context);
     _jspx_th_c_005furl_005f8.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f9);
     // /convMovilidad/listaPropuestas.jsp(95,12) name = value type = null reqTime = true required = false fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005furl_005f8.setValue("/convMovilidad/Insercion.jsp");
     int _jspx_eval_c_005furl_005f8 = _jspx_th_c_005furl_005f8.doStartTag();
     if (_jspx_th_c_005furl_005f8.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f8);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f8);
     return false;
   }
 
   private boolean _jspx_meth_c_005furl_005f9(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f9, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:url
     org.apache.taglibs.standard.tag.rt.core.UrlTag _jspx_th_c_005furl_005f9 = (org.apache.taglibs.standard.tag.rt.core.UrlTag) _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.get(org.apache.taglibs.standard.tag.rt.core.UrlTag.class);
     _jspx_th_c_005furl_005f9.setPageContext(_jspx_page_context);
     _jspx_th_c_005furl_005f9.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f9);
     // /convMovilidad/listaPropuestas.jsp(95,80) name = value type = null reqTime = true required = false fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005furl_005f9.setValue("/comp/img/AgregarPropuesta.gif");
     int _jspx_eval_c_005furl_005f9 = _jspx_th_c_005furl_005f9.doStartTag();
     if (_jspx_th_c_005furl_005f9.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f9);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f9);
     return false;
   }
 
   private boolean _jspx_meth_c_005fif_005f10(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f7, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:if
     org.apache.taglibs.standard.tag.rt.core.IfTag _jspx_th_c_005fif_005f10 = (org.apache.taglibs.standard.tag.rt.core.IfTag) _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.get(org.apache.taglibs.standard.tag.rt.core.IfTag.class);
     _jspx_th_c_005fif_005f10.setPageContext(_jspx_page_context);
     _jspx_th_c_005fif_005f10.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f7);
     // /convMovilidad/listaPropuestas.jsp(99,2) name = test type = boolean reqTime = true required = true fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fif_005f10.setTest(((java.lang.Boolean) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${(sessionScope.datosConv.convNumero==2 || sessionScope.datosConv.convNumero==4 or sessionScope.datosConv.convNumero==15) and sessionScope.persona.papel!=1 and sessionScope.persona.papel!=2 and sessionScope.persona.papel!=4  and sessionScope.persona.papel!=6}", java.lang.Boolean.class, (PageContext)_jspx_page_context, null, false)).booleanValue());
     int _jspx_eval_c_005fif_005f10 = _jspx_th_c_005fif_005f10.doStartTag();
     if (_jspx_eval_c_005fif_005f10 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
       do {
         out.write("\r\n");
         out.write("\t\t<p align=\"center\" class=\"lroja3\">Usted no puede inscribirse en esta convocatoria debido a que su papel en el grupo <b>NO</b> es el de <b>Profesor</b> de la Universidad Distrital. Si usted es estudiante, Favor ingresar a la convocatoria de Estudiantes. Si usted es profesor, favor modificar el campo \"papel\" en sus datos personales en el menú \"Mis Datos\"</p>\r\n");
         out.write("\t\t");
         int evalDoAfterBody = _jspx_th_c_005fif_005f10.doAfterBody();
         if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
           break;
       } while (true);
     }
     if (_jspx_th_c_005fif_005f10.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f10);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f10);
     return false;
   }
 
   private boolean _jspx_meth_c_005fif_005f11(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f7, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:if
     org.apache.taglibs.standard.tag.rt.core.IfTag _jspx_th_c_005fif_005f11 = (org.apache.taglibs.standard.tag.rt.core.IfTag) _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.get(org.apache.taglibs.standard.tag.rt.core.IfTag.class);
     _jspx_th_c_005fif_005f11.setPageContext(_jspx_page_context);
     _jspx_th_c_005fif_005f11.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f7);
     // /convMovilidad/listaPropuestas.jsp(102,2) name = test type = boolean reqTime = true required = true fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005fif_005f11.setTest(((java.lang.Boolean) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${(sessionScope.datosConv.convNumero==2 || sessionScope.datosConv.convNumero==4 or sessionScope.datosConv.convNumero==15) and (sessionScope.persona.papel==1 or sessionScope.persona.papel==2 or sessionScope.persona.papel==4  or sessionScope.persona.papel==6)}", java.lang.Boolean.class, (PageContext)_jspx_page_context, null, false)).booleanValue());
     int _jspx_eval_c_005fif_005f11 = _jspx_th_c_005fif_005f11.doStartTag();
     if (_jspx_eval_c_005fif_005f11 != javax.servlet.jsp.tagext.Tag.SKIP_BODY) {
       do {
         out.write("\r\n");
         out.write("\t\t<p align=\"center\" class=\"texto1\">En estos momentos usted no tiene ninguna propuesta inscrita, Favor dar clik en el botón \"Agregar Propuesta\" para insertar una nueva propuesta</p>\r\n");
         out.write("\t\t<div align=\"center\">\r\n");
         out.write("\t\t\t<a href='");
         if (_jspx_meth_c_005furl_005f10(_jspx_th_c_005fif_005f11, _jspx_page_context))
           return true;
         out.write("'><img border=\"0\" src='");
         if (_jspx_meth_c_005furl_005f11(_jspx_th_c_005fif_005f11, _jspx_page_context))
           return true;
         out.write("'></a>\r\n");
         out.write("\t\t</div>\r\n");
         out.write("\t\t");
         int evalDoAfterBody = _jspx_th_c_005fif_005f11.doAfterBody();
         if (evalDoAfterBody != javax.servlet.jsp.tagext.BodyTag.EVAL_BODY_AGAIN)
           break;
       } while (true);
     }
     if (_jspx_th_c_005fif_005f11.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f11);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005fif_0026_005ftest.reuse(_jspx_th_c_005fif_005f11);
     return false;
   }
 
   private boolean _jspx_meth_c_005furl_005f10(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f11, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:url
     org.apache.taglibs.standard.tag.rt.core.UrlTag _jspx_th_c_005furl_005f10 = (org.apache.taglibs.standard.tag.rt.core.UrlTag) _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.get(org.apache.taglibs.standard.tag.rt.core.UrlTag.class);
     _jspx_th_c_005furl_005f10.setPageContext(_jspx_page_context);
     _jspx_th_c_005furl_005f10.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f11);
     // /convMovilidad/listaPropuestas.jsp(105,12) name = value type = null reqTime = true required = false fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005furl_005f10.setValue("/convMovilidad/Insercion.jsp");
     int _jspx_eval_c_005furl_005f10 = _jspx_th_c_005furl_005f10.doStartTag();
     if (_jspx_th_c_005furl_005f10.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f10);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f10);
     return false;
   }
 
   private boolean _jspx_meth_c_005furl_005f11(javax.servlet.jsp.tagext.JspTag _jspx_th_c_005fif_005f11, PageContext _jspx_page_context)
           throws Throwable {
     PageContext pageContext = _jspx_page_context;
     JspWriter out = _jspx_page_context.getOut();
     //  c:url
     org.apache.taglibs.standard.tag.rt.core.UrlTag _jspx_th_c_005furl_005f11 = (org.apache.taglibs.standard.tag.rt.core.UrlTag) _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.get(org.apache.taglibs.standard.tag.rt.core.UrlTag.class);
     _jspx_th_c_005furl_005f11.setPageContext(_jspx_page_context);
     _jspx_th_c_005furl_005f11.setParent((javax.servlet.jsp.tagext.Tag) _jspx_th_c_005fif_005f11);
     // /convMovilidad/listaPropuestas.jsp(105,80) name = value type = null reqTime = true required = false fragment = false deferredValue = false expectedTypeName = null deferredMethod = false methodSignature = null
     _jspx_th_c_005furl_005f11.setValue("/comp/img/AgregarPropuesta.gif");
     int _jspx_eval_c_005furl_005f11 = _jspx_th_c_005furl_005f11.doStartTag();
     if (_jspx_th_c_005furl_005f11.doEndTag() == javax.servlet.jsp.tagext.Tag.SKIP_PAGE) {
       _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f11);
       return true;
     }
     _005fjspx_005ftagPool_005fc_005furl_0026_005fvalue_005fnobody.reuse(_jspx_th_c_005furl_005f11);
     return false;
   }
 }
