 package net.bodz.bas.repr.req;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.bodz.bas.c.object.ObjectInfo;
 
 public class ObjectDumpView
// TODO
//        extends AbstractHttpRenderer 
        {
 
//    @Override
     public Class<?> getType() {
         return Object.class;
     }
 
//    @Override
     public boolean render(Object obj, HttpServletRequest req, HttpServletResponse resp)
             throws IOException {
         IRequestDispatch disp = RequestUtils.getRequestDispatch(req);
         IRequestMethod qmethod = RequestUtils.getRequestMethod(req);
 
         PrintWriter out = resp.getWriter();
 
         out.println("<html><head><title>Object Dump</title></head>");
         out.println("<body><h1>Object Dump: " + ObjectInfo.getSimpleId(obj) + "</h1>");
         out.println("<hr/>");
 
         String string = String.valueOf(obj);
         String html = string; // TODO HtmlUtils.htmlEscape(string);
         out.println("<pre>");
         out.println(html);
         out.println("</pre>");
 
         out.println("<hr/>");
         out.println("<h2>Request Info</h2>");
         out.println("<pre>");
 
         out.println("Context-Path: " + req.getContextPath());
         out.println("Dispatch-Path: " + disp.getDispatchPath());
         out.println("Arrival: " + disp.getArrival());
         out.println("Remaining-Path: " + disp.getRemainingPath());
         out.println("Method: " + qmethod.getMethodName());
         out.println(disp);
 
         out.println("</pre>");
         return true;
     }
 
 }
