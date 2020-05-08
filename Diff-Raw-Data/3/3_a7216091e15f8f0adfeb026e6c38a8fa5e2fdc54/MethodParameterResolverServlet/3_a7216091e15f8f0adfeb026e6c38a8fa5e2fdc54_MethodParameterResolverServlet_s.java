 package fedora.server.test;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Enumeration;
 
 /**
  * <p>Title: MethodParameterResolverServlet.java</p>
  * <p>Description: <p>This servlet accepts the result of a posted web form
  * containing information about which method parameter values were selected
  * for a dissemination request. The information is read from the form and
  * translated into an appropriate dissemination request and then executes
  * the dissemination request.</p>
  *
  * <p>Copyright: Copyright (c) 2002</p>
  * <p>Company: </p>
  * @author Ross Wayland
  * @version 1.0
  */
 public class MethodParameterResolverServlet extends HttpServlet
 {
 
   /** A string constant for the html MIME type */
   static final private String CONTENT_TYPE = "text/html";
 
   public void init() throws ServletException
   {}
 
   //Process the HTTP Post request
   public void doPost(HttpServletRequest request, HttpServletResponse response)
       throws ServletException, IOException
   {
     String PID = null;
     String bDefPID = null;
     String methodName = null;
     String serverURI = null;
     response.setContentType(CONTENT_TYPE);
     PrintWriter out = response.getWriter();
 
     Enumeration parms = request.getParameterNames();
     StringBuffer sb = new StringBuffer();
 
     while (parms.hasMoreElements())
     {
       String name = new String((String)parms.nextElement());
       String value = new String(request.getParameter(name));
       if (name.equals("PID"))
       {
         PID = (String)request.getParameter(name);
         System.out.println("PID: "+PID);
       } else if (name.equals("bDefPID"))
       {
         bDefPID = (String)request.getParameter(name);
         System.out.println("bDefPID: "+bDefPID);
       } else if (name.equals("methodName"))
       {
         methodName = (String)request.getParameter(name);
         System.out.println("methodName: "+methodName);
       } else if (name.equals("serverURI"))
       {
         serverURI = (String)request.getParameter(name);
         System.out.println("serverURI: "+serverURI);
       } else if (name.equals("Submit")) {
         // Submit parameter is ignored
       } else
       {
         // Any remaining parameters are method parameters.
         sb.append(name+"="+(String)request.getParameter(name)+"&");
       }
     }
    sb.replace(sb.lastIndexOf("&"),sb.lastIndexOf("&")+1,"");
 
     // Check for any missing parameters in form
     if ((PID == null || PID.equalsIgnoreCase("")) ||
         (bDefPID == null || bDefPID.equalsIgnoreCase("")) ||
         (methodName == null || methodName.equalsIgnoreCase("")) ||
         (serverURI == null || serverURI.equalsIgnoreCase("")) )
     {
       out.println("<br><b>Unable to resolve dissemination request."
                   + "<br><table border=\"0\"><tr><td>PID</td><td>"
                   + PID + "</td></tr><tr><td>bDefPID</td><td>"
                   + bDefPID + "</td></tr><tr><td>methodName</td><td>"
                   + methodName + "</td></tr><tr><td>serverURI</td><td>"
                   + serverURI + "</td></tr></table><br>");
     } else
     {
       response.sendRedirect("http://localhost:8080/fedora/access/soapservlet?"
           + "action_=GetDissemination&"
           + "PID_=" + PID + "&"
           + "bDefPID_=" + bDefPID + "&"
           + "methodName_=" + methodName + "&"
           +sb.toString());
 
       System.out.println("WebFormURL: \n"
           +serverURI + "action_=GetDissemination&PID_=" + PID
           + "&bDefPID_=" + bDefPID
           + "&methodName_=" + methodName + "&"
           + sb.toString());
     }
   }
 
   //Clean up resources
   public void destroy()
   {
   }
 }
