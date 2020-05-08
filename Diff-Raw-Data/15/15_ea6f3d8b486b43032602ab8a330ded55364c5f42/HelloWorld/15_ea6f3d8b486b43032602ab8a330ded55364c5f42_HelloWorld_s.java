 package net.link.safeonline.sdk.example;
 
 import java.io.*;
 import java.util.List;
 import java.util.Map;
 import javax.servlet.ServletException;
 import javax.servlet.http.*;
 import net.link.safeonline.sdk.api.attribute.AttributeSDK;
 import net.link.safeonline.sdk.api.auth.LoginMode;
 import net.link.safeonline.sdk.api.auth.RequestConstants;
 import net.link.safeonline.sdk.auth.filter.LoginManager;
 import net.link.safeonline.sdk.auth.protocol.AuthnProtocolResponseContext;
 import net.link.safeonline.sdk.auth.protocol.ProtocolManager;
 import net.link.util.error.ValidationFailedException;
 import net.link.util.servlet.ErrorMessage;
 import net.link.util.servlet.ServletUtils;
 
 
 /**
  * Created by IntelliJ IDEA.
  * User: sgdesmet
  * Date: 28/02/12
  * Time: 13:09
  * To change this template use File | Settings | File Templates.
  */
 public class HelloWorld extends HttpServlet {
     
     //an attribute
     public static final String SURNAME_ATTRIBUTE = "profile.givenName";
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
 
        HttpSession currentSession = request.getSession(); 
         boolean authenticated = LoginManager.isAuthenticated( currentSession );
         Map<String, List<AttributeSDK<Serializable>>> attributes = LoginManager.findAttributes( currentSession );
         String userName;
         if (attributes != null && attributes.containsKey( SURNAME_ATTRIBUTE )){
             userName = (String)attributes.get(SURNAME_ATTRIBUTE).get(0).getValue();
         } else {
             userName = "unknown user";
         }
         
         response.setContentType( "text/html" );
         PrintWriter out = response.getWriter();
         out.println( "<html>" );
         out.println( "<head>" );
         out.println( "</head>" );
         out.println( "<body>" );
         if (!authenticated)
             out.println( "<a href=\"/startlogin?return_uri=/helloworld\">Login</a>" ); //startlogin path is defined in web.xml!
        else
            out.println( "<a href=\"/startlogout\">Logout</a>" );
         out.println( "<p>Hello  " + userName + " </p>" );
         out.println( "<p>Authenticated:  " + authenticated + " </p>" );
         out.println( "</body>" );
         out.println( "</html>" );
     }
 
 }
