 package com.atlassian.aui.test;
 
 
 import com.atlassian.plugin.Plugin;
 import com.atlassian.plugin.PluginAccessor;
 import com.atlassian.plugin.webresource.WebResourceManager;
 import org.apache.commons.io.IOUtils;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Writer;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 /**
  *
  */
 public class FuncTestServlet extends HttpServlet
 {
     private final WebResourceManager webResourceManager;
     private final Plugin plugin;
 
     public FuncTestServlet(WebResourceManager webResourceManager, PluginAccessor pluginAccessor)
     {
         this.webResourceManager = webResourceManager;
         this.plugin = pluginAccessor.getPlugin("auiplugin-tests");
     }
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
     {
         webResourceManager.requireResource("com.atlassian.auiplugin:ajs");
         webResourceManager.requireResource("auiplugin-tests:qunit");
         if (req.getPathInfo().endsWith("/"))
         {
             try
             {
                 displayIndex(req, resp);
             }
             catch (URISyntaxException e)
             {
                 throw new IOException(e);
             }
         }
         else
         {
             String path = req.getPathInfo();
             if (path.endsWith(".html"))
             {
                 resp.setContentType("text/html");
             }
             else if (path.endsWith(".js"))
             {
                 resp.setContentType("text/javascript");
             }
             else if (path.endsWith(".css"))
             {
                 resp.setContentType("text/css");
             }
             InputStream in = plugin.getResourceAsStream(path);
             OutputStream out = resp.getOutputStream();
             IOUtils.copy(in, out);
             out.close();
         }
     }
 
     private void displayIndex(HttpServletRequest req, HttpServletResponse resp) throws IOException, URISyntaxException
     {
         resp.setContentType("text/html");
         Writer writer = resp.getWriter();
         URL fileURL = plugin.getResource(req.getPathInfo());
         if (fileURL == null)
         {
             resp.sendError(404);
             return;
         }
         else if ("file".equals(fileURL.getProtocol().toLowerCase()))
         {
             File file = new File(fileURL.toURI());
             writer.append("<ul>");
             writer.append("<li><a href=\"../\">..</li>\n");
             for (File kid : file.listFiles())
             {
                 String name = kid.getName();
                 if (kid.isDirectory())
                 {
                     name += "/";
                 }
                 writer.append("<li><a href=\"" + name + "\">" + name + "</a></li>\n");
             }
             writer.append("</ul>");
             writer.close();
         }
     }
 }
