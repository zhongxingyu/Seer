 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.ajax;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * Requires all parameters passed to be lower case, and directly what are in the template
  * @author Sam
  */
 @WebServlet(name = "fillTemplate", urlPatterns = {"/fillTemplate"})
 public class fillTemplate extends HttpServlet {
 
     // Just to keep track of statistics for fun and giggles, as well how to optimize, but mostly fun and giggles.
     private long StatNumNullCalls = 0;
     private long StatNumNullParams = 0;
     private long StatNumCompleteCalls = 0;
     private long StatNumCompleteParams = 0;
     private long StatNumFillCalls = 0;
     private long StatNumFillParams = 0;
     private long StatNumFillHTMLCalls = 0;
     private long StatNumFillHTMLParams = 0;
     private long StatNumFillSimpleCalls = 0;
     private long StatNumFillSimpleParams = 0;
     private long StatTotalTimeWorking = 0;
     private long StatTotalCalls = 0;
     private ServletContext context;
     ConfigTemplate template = new ConfigTemplate(); // template helps determine valid parameters, and of course, the output.
 
     private String buildParameterXML(String[] names, String[] values) {
         // Expects names & values to never be null, only template valid parameters are passed
         StringBuilder out = new StringBuilder();
         out.append("<parameters>");
 
         for (int i = 0; i < names.length; i++) {
             out.append(String.format("<parameter name=\"%s\" value=\"%s\"/>", names[i], values[i]));
         }
         out.append("</parameters>");
         return out.toString();
     }
 
     private String buildPopulatedTemplateXML(String[] names, String[] values) {
         // Expects names & values to never be null
         StringBuilder out = new StringBuilder();
         out.append("<template>");
         out.append(buildPopulatedTemplateString(names, values));
         out.append("</template>");
         return out.toString();
     }
 
     private String buildPopulatedTemplateString(String[] names, String[] values) {
         // if names or values is null or not same size, return unpopulated template
         if (names == null || values == null || names.length != values.length) {
             return template.get();
         } else {
             template.passReplacements(names, values);
         }
 
         return template.get();
     }
 
     /** 
      * Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         StatTotalCalls++;
         long startTime = System.currentTimeMillis();
         String action = request.getParameter("action");
         int NumberOfParams = request.getParameterMap().size();
 
 
         if (action == null) {
             StatNumNullCalls++;
             StatNumNullParams += NumberOfParams;
             // Assumes they just want an unpopulated template
             response.setContentType("text/html;charset=UTF-8");
             PrintWriter out = response.getWriter();
             try {
                 out.print(buildPopulatedTemplateString(null, null));
             } finally {
                 out.close();
             }
             //context.getRequestDispatcher("/error.jsp").forward(request, response);
         } else if (action.equals("complete")) {
             StatNumCompleteCalls++;
             StatNumCompleteParams += NumberOfParams;
             // We're over a choose-envOption field now, so we get a popup table on the side
             // that returns all possible options (that reduces based on what's already typed)
             // There'll be another check for type of complete:
             // -choosing the option type
             // -choosing the option value
             // -choosing main option?
             // lets have a type, and a currentValue (which is what's been typed)
             String currentValue = request.getParameter("currentValue");
             if (currentValue == null || currentValue.isEmpty())  {
                 currentValue = "";
             }
             String typeOfEnvOption = request.getParameter("type");
             if (typeOfEnvOption == null || typeOfEnvOption.isEmpty()) {
                typeOfEnvOption = null; // pass the buck on
             }
             String tableXML = template.getReducedXMLList(currentValue,typeOfEnvOption);
             // Respond with parameters + a reduced popup table in xml
             response.setContentType("text/xml");
            response.setHeader("Cache-Control", "no-cache"); // Keeps browsers from cacheing web-page (not updating to new values bad)
             PrintWriter out = response.getWriter();
             try {
                 out.print("<wrapper>");
                 out.print("<type>"+"config"+"</type>");
                 out.print("<tableXML>"+tableXML+"</tableXML>");
                 out.print("</wrapper>");
 
             } finally {
                 out.close();
             }
 
         } else if (action.equals("fill") || action.equals("fillSimple") || action.equals("fillHTML")) {
 
             // Get values of valid parameters
            ArrayList<String> names = new <String>ArrayList();
            ArrayList<String> values = new <String>ArrayList();
             for (int i = 0; i < ConfigTemplate.getValidPlaceholders().length; i++) {
                 String value = request.getParameter(ConfigTemplate.getValidPlaceholders()[i].toLowerCase()); // null if doesn't exist or not lowercase...
                 if (value == null) {
                     value = request.getParameter(ConfigTemplate.getValidPlaceholders()[i].toUpperCase()); // try Upper Case also, mixed cases won't work
                 }
                 if (value != null) {
                     names.add(ConfigTemplate.getValidPlaceholders()[i]);
                     values.add(value);
                 }
             } // Could actually do this the other way around ,but that invites people to send in too many parameters?
             String[] placeholders = new String[names.size()];
             String[] pValues = new String[names.size()];
             for (int i = 0; i < names.size(); i++) {
                placeholders[i] = names.get(i);
                pValues[i] = values.get(i);
             }
 
 
 
 
             if (action.equals("fillSimple") || action.equals("fillHTML")) {
                 // Respond a populated template as a string
                 response.setContentType("text/html;charset=UTF-8");
                response.setHeader("Cache-Control", "no-cache"); // Keeps browsers from cacheing web-page (not updating to new values bad)
                 PrintWriter out = response.getWriter();
                 try {
                     if (action.equals("fillHTML")) {
                         // replace \n with <br> so it shows up in browser
                         StatNumFillHTMLCalls++;
                         StatNumFillHTMLParams += NumberOfParams;
                         out.print(buildPopulatedTemplateString(placeholders, pValues).replaceAll("\n", "<br>"));
                     } else {
                         // Simple
                         StatNumFillSimpleCalls++;
                         StatNumFillSimpleParams += NumberOfParams;
                         out.print(buildPopulatedTemplateString(placeholders, pValues));
                     }
                 } finally {
                     out.close();
                 }
             } else {
                 StatNumFillCalls++;
                 StatNumFillParams += NumberOfParams;
                 // Respond with parameters + filled template wrapped in xml
                 response.setContentType("text/xml");
                response.setHeader("Cache-Control", "no-cache"); // Keeps browsers from cacheing web-page (not updating to new values bad)
                 PrintWriter out = response.getWriter();
                 try {
                     out.print("<wrapper>");
                     out.print(buildParameterXML(placeholders, pValues));
                     out.print(buildPopulatedTemplateXML(placeholders, pValues));
                     out.print("</wrapper>");
 
                 } finally {
                     out.close();
                 }
             }
 
 
         } else if (action.equals("stats")) {
             response.setContentType("text/html;charset=UTF-8");
             PrintWriter out = response.getWriter();
             try {
                 out.print(getStats().replaceAll("\n", "<br>"));
             } finally {
                 out.close();
             }
         } else {
             context.getRequestDispatcher("/error.jsp").forward(request, response);
         }
 
         StatTotalTimeWorking += (System.currentTimeMillis() - startTime);
     }
 
     /** 
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "Returns a populated CESM configuration template from passed parameters";
     }
 
     @Override
     public void init(ServletConfig config) throws ServletException {
         this.context = config.getServletContext();
     }
 
     /**
      * Returns metrics information of usage so far, fun with numbers!
      * @return 
      */
     private String getStats() {
         String stats = "";
 
         double totalCalls = StatNumNullCalls + StatNumFillCalls
                 + StatNumFillHTMLCalls + StatNumFillSimpleCalls; // Total # of calls to /fillTemplate
         double totalParams = StatNumNullParams + StatNumFillParams
                 + StatNumFillHTMLParams + StatNumFillSimpleParams; // Total # Params passed
 
 
         double avgNull = 0;
         double avgFill = 0;
         double avgHTML = 0;
         double avgSimple = 0;
         double avgTotal = 0;
 
         if (StatNumNullCalls > 0) {
             avgNull = (double) StatNumNullParams / StatNumNullCalls;
         }
         if (StatNumFillCalls > 0) {
             avgFill = (double) StatNumFillParams / StatNumFillCalls;
         }
         if (StatNumFillHTMLCalls > 0) {
             avgHTML = (double) StatNumFillHTMLParams / StatNumFillHTMLCalls;
         }
         if (StatNumFillSimpleCalls > 0) {
             avgSimple = (double) StatNumFillSimpleParams / StatNumFillSimpleCalls;
         }
         if (totalCalls > 0) {
             avgTotal = (double) totalParams / totalCalls;
         }
 
 
 
         stats += "Name(action) ,        # of Calls        , Average # of Parameters passed (including the action) <br>";
         stats += "none         , " + StatNumNullCalls + " , " + avgNull + "<br>";
         stats += "fill         , " + StatNumFillCalls + " , " + avgFill + "<br>";
         stats += "fillHTML     , " + StatNumFillHTMLCalls + " , " + avgHTML + "<br>";
         stats += "fillSimple   , " + StatNumFillSimpleCalls + " , " + avgSimple + "<br>";
         stats += "Total        , " + totalCalls + " , " + avgTotal + "<br>";
         stats += "<br>";
         stats += "Total Calls of any kind: " + StatTotalCalls + "<br>";
         stats += "Total time working: " + StatTotalTimeWorking / 1000 + " seconds<br>";
         stats += "Average Response time: " + (double) StatTotalTimeWorking / StatTotalCalls + " milliseconds per call<br>";
 
         return stats;
     }
 }
