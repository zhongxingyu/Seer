 package com.kokakiwi.fun.pulsar.web;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 
 public class StaticPages
 {
     public static boolean handle(HttpServletRequest req,
             HttpServletResponse resp) throws ServletException, IOException
     {
         boolean handled = false;
         
         String request = req.getPathInfo().substring(1);
         if (request.isEmpty())
         {
             show("special/home.html", resp);
             handled = true;
         }
         else
         {
             if (request.endsWith(".html") && test("static/" + request))
             {
                 show("static/" + request, resp);
                 handled = true;
             }
             else
             {
                 if (test("static/" + request + ".html"))
                 {
                     show("static/" + request + ".html", resp);
                     handled = true;
                 }
                 else
                 {
                     send("static/" + request, resp);
                 }
             }
         }
         
         return handled;
     }
     
     public static void show(String page, HttpServletResponse resp)
             throws IOException
     {
         send("special/header.html", resp);
         send(page, resp);
         send("special/footer.html", resp);
     }
     
     public static void send(String file, HttpServletResponse resp)
             throws IOException
     {
         InputStream in = StaticPages.class.getResourceAsStream("/" + file);
         IOUtils.copy(in, resp.getOutputStream());
     }
     
     public static boolean test(String file)
     {
         return StaticPages.class.getResource("/" + file) != null;
     }
 }
