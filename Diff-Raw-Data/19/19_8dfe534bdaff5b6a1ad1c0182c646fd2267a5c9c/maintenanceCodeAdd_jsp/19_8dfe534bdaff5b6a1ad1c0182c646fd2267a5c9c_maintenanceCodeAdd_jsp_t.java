 package org.apache.jsp.appCore_Personnel.views;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import javax.servlet.jsp.*;
 
 public final class maintenanceCodeAdd_jsp extends org.apache.jasper.runtime.HttpJspBase
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
 
       out.write("\r\n");
       out.write("       <link href=\"../../css/themes/base/jquery.ui.all.css\" media=\"screen\" rel=\"stylesheet\" type=\"text/css\" />\r\n");
       out.write("       <link href=\"../../css/dialogBoxEdit.css\" media=\"screen\" rel=\"stylesheet\" type=\"text/css\" />\r\n");
       out.write("\r\n");
       out.write("       <link href=\"../../css/codeMaintenance.css\" media=\"screen\" rel=\"stylesheet\" type=\"text/css\" />\r\n");
       out.write("       <script language=\"javascript\" src=\"../../js/viewmodal/codeMaintenanceViewModel.js\"></script>\r\n");
       out.write("       <script language=\"javascript\" src=\"../../js/viewmodal/employeeHelper.js\"></script>\r\n");
       out.write("\r\n");
       out.write("        <script type=\"text/javascript\">\r\n");
       out.write("\r\n");
       out.write("        var form = \"codeForm\";\r\n");
       out.write("\r\n");
       out.write("        var codeType = parseInt(globalCodeMaintenance);\r\n");
       out.write("        var vm;\r\n");
       out.write("        var gridDataObject;\r\n");
       out.write("\r\n");
       out.write("        $(document).ready(function()\r\n");
       out.write("        {\r\n");
       out.write("            $(\"#\" + form).validationEngine();\r\n");
       out.write("\r\n");
       out.write("            if (globalViewModel != undefined && globalViewModel.targetId() != null && globalViewModel.editMode() == coreModeEdit)\r\n");
       out.write("            {\r\n");
       out.write("                vm = new CodeMaintenanceViewModel(coreModeEdit, codeType, globalViewModel.targetId, globalViewModel);\r\n");
       out.write("                gridDataObject = vm.getView();\r\n");
       out.write("            }\r\n");
       out.write("            else\r\n");
       out.write("            {\r\n");
       out.write("                vm = new CodeMaintenanceViewModel(coreModeInsert, codeType, null, globalViewModel);\r\n");
       out.write("                gridDataObject = vm.getView();\r\n");
       out.write("            }\r\n");
      out.write("\r\n");
      out.write("            try\r\n");
      out.write("            {\r\n");
      out.write("                ko.applyBindings(vm, document.getElementById(\"codeForm\"));\r\n");
      out.write("            }\r\n");
      out.write("            catch (ex)\r\n");
      out.write("            {\r\n");
      out.write("                console.log(ex)\r\n");
      out.write("            }\r\n");
       out.write("\r\n");
       out.write("        });\r\n");
       out.write("\r\n");
       out.write("        function getEmptyDate(date)\r\n");
       out.write("        {\r\n");
       out.write("            return \"empty\";\r\n");
       out.write("        }\r\n");
       out.write("\r\n");
       out.write("\r\n");
       out.write("        </script>\r\n");
       out.write("\r\n");
       out.write("\r\n");
       out.write("        <form id=\"codeForm\">\r\n");
       out.write("\r\n");
       out.write("        <div class=\"whiteform\">\r\n");
       out.write("\r\n");
       out.write("        <div class=\"formRowTitle\">\r\n");
       out.write("\r\n");
       out.write("\r\n");
       out.write("       <div class=\"labelSection codeTitle\" data-bind=\"text: title\"></div>\r\n");
       out.write("\r\n");
       out.write("        </div>\r\n");
       out.write("\r\n");
       out.write("        <div class=\"formRowSpacer\">\r\n");
       out.write("        </div>\r\n");
       out.write("\r\n");
       out.write("        <div class=\"sectionalForm\">\r\n");
       out.write("        <div class=\"leftSection\">\r\n");
       out.write("        <div class=\"labelSection\">Name</div><span class='req'>*</span><div class=\"inputSection\"><input\r\n");
       out.write("        type=\"text\" id=\"DivisionCode\" data-bind=\"value: name\" class=\"validate[required, maxSize[10]]\" /><i\r\n");
       out.write("        class=\"icon-tag-1\"></i></div>\r\n");
       out.write("        </div>\r\n");
       out.write("\r\n");
       out.write("        <div class=\"rightSection\">\r\n");
       out.write("        <div class=\"inlineLabelSection\">Disabled</div><div class=\"inlineLabelSection\"><input type=\"checkbox\"\r\n");
       out.write("        id=\"Enabled\" data-bind=\"checked : disabled\" value=\"Disabled\"/></div>\r\n");
       out.write("        </div>\r\n");
       out.write("        </div>\r\n");
       out.write("\r\n");
       out.write("        <div class=\"formRow\">\r\n");
       out.write("        <div class=\"labelSection\">Description</div><span class='req'>*</span><div class=\"inputSection\"><input\r\n");
       out.write("        type=\"text\" id=\"DivisionName\" data-bind=\"value: description\" class=\"validate[required, maxSize[20]]\"/><i\r\n");
       out.write("        class=\"icon-doc2\"></i></div>\r\n");
       out.write("        </div>\r\n");
       out.write("\r\n");
       out.write("        <div class=\"formRow\">\r\n");
       out.write("        <div class=\"labelSection\">Effective Start Date</div><span class='req'>*</span><div class=\"inputSection\"><input\r\n");
       out.write("        type=\"text\" id=\"SartEffectiveDate\" data-bind=\"datepicker: startEffectiveDate, datepickerOptions: { dateFormat :\r\n");
       out.write("        'dd-mm-yy'}, value : startEffectiveDate, enable : !disabled()\" class=\"validate[required, maxSize[80]]\"\r\n");
       out.write("        /></div>\r\n");
       out.write("        </div>\r\n");
       out.write("\r\n");
       out.write("        <div class=\"formRow\">\r\n");
       out.write("        <div class=\"labelSection\">Effective End Date</div><span class='req'>*</span><div class=\"inputSection\"><input\r\n");
       out.write("        type=\"text\" id=\"EndEffectiveDate\" data-bind=\"datepicker: endEffectiveDate, datepickerOptions: { dateFormat :\r\n");
       out.write("        'dd-mm-yy'}, value : endEffectiveDate, enable : !disabled()\" class=\"validate[required, maxSize[80]]\"\r\n");
       out.write("        /></div>\r\n");
       out.write("        </div>\r\n");
       out.write("\r\n");
       out.write("        <div class=\"formRowSpacer\">\r\n");
       out.write("        </div>\r\n");
       out.write("\r\n");
       out.write("\r\n");
       out.write("        <div class=\"formRow darken\">\r\n");
       out.write("        <div class=\"labelSection\"></div><div class=\"inputSection\">\r\n");
       out.write("\r\n");
       out.write("\r\n");
       out.write("        <div> <div class=\"maintenanceCommandSpace\"></div>\r\n");
       out.write("        <div class=\"maintenanceCommand\" data-bind=\"visible : $root.enableAdd || $root.enableUpdate\" id=\"maintenanceCodeCommand\">\r\n");
       out.write("\r\n");
       out.write("           <button id=\"saveBtn\" type=\"button\" class=\"command\" data-bind=\"click : saveDataForm, text: commandText\">Add New Code</button>\r\n");
       out.write("           <button id=\"cancelBtn\" type=\"button\" class=\"command\" data-bind=\"click : cancelChanges\">Cancel</button>\r\n");
       out.write("        </div>\r\n");
       out.write("        </div>\r\n");
       out.write("\r\n");
       out.write("\r\n");
       out.write("        </div>\r\n");
       out.write("        </div>\r\n");
       out.write("\r\n");
       out.write("\r\n");
       out.write("        </div>\r\n");
       out.write("        </form>\r\n");
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
