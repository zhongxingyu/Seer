 package com.atlassian.refapp.decorator;
 
 import com.atlassian.templaterenderer.TemplateRenderer;
 import com.atlassian.templaterenderer.RenderingException;
 import com.opensymphony.module.sitemesh.Page;
 import com.opensymphony.module.sitemesh.RequestConstants;
 import com.opensymphony.module.sitemesh.HTMLPage;
 import com.opensymphony.module.sitemesh.util.OutputConverter;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.util.Map;
 import java.util.HashMap;
 
 public class DecoratorServlet extends HttpServlet
 {
     private static final String ADMIN_PATH = "/admin.vmd";
 
     private final TemplateRenderer templateRenderer;
 
     public DecoratorServlet(TemplateRenderer templateRenderer)
     {
         this.templateRenderer = templateRenderer;
     }
 
     protected void service(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
     {
         Page page = (Page) request.getAttribute(RequestConstants.PAGE);
         if (page != null)
         {
             applyDecoratorUsingVelocity(request, page, response);
         }
         else
         {
             String servletPath = (String) request.getAttribute("javax.servlet.include.servlet_path");
             if (servletPath == null)
             {
                 servletPath = request.getServletPath();
             }
             throw new ServletException("No sitemesh page to decorate. This servlet should not be invoked directly. " +
                 "The path invoked was " + servletPath);
         }
     }
 
     private void applyDecoratorUsingVelocity(HttpServletRequest request, Page page, HttpServletResponse response) throws
         IOException
     {
         String template;
         String pathInfo = (String) request.getAttribute("javax.servlet.include.path_info");
         if (pathInfo == null)
         {
             pathInfo = request.getPathInfo();
         }
         if (pathInfo != null && pathInfo.endsWith(ADMIN_PATH))
         {
             template = "/templates/admin.vmd";
         }
         else
         {
             template = "/templates/general.vmd";
         }
 
         Map<String, Object> velocityParams = getVelocityParams(request, page, response);
 
         final PrintWriter writer = response.getWriter();
         try
         {
             response.setContentType("text/html");
             templateRenderer.render(template,  velocityParams, writer);
         }
         catch (RenderingException e)
         {
             writer.write("Exception rendering velocity file " + template);
             writer.write("<br><pre>");
             e.printStackTrace(writer);
             writer.write("</pre>");
         }
     }
 
     private Map<String, Object> getVelocityParams(HttpServletRequest request, Page page, HttpServletResponse response)
         throws IOException
     {
         Map<String, Object> velocityParams = new HashMap<String, Object>();
 
         velocityParams.put("page", page);
        velocityParams.put("title", page.getTitle());
 
         StringWriter bodyBuffer = new StringWriter();
         page.writeBody(OutputConverter.getWriter(bodyBuffer));
        velocityParams.put("body", bodyBuffer);
 
         if (page instanceof HTMLPage)
         {
             HTMLPage htmlPage = (HTMLPage) page;
             StringWriter buffer = new StringWriter();
             htmlPage.writeHead(OutputConverter.getWriter(buffer));
            velocityParams.put("head", buffer.toString());
         }
 
         velocityParams.put("request", request);
         return velocityParams;
     }
 }
