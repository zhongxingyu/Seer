 /* This file is part of calliope.
  *
  *  calliope is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  calliope is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with calliope.  If not, see <http://www.gnu.org/licenses/>.
  */
 package calliope;
 import calliope.handler.get.AeseGetHandler;
 import calliope.handler.delete.AeseDeleteHandler;
 import calliope.handler.put.AesePutHandler;
 import calliope.handler.post.AesePostHandler;
 import calliope.exception.AeseException;
 import calliope.db.*;
 import java.net.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletException;
 import java.io.IOException;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.handler.AbstractHandler;
  
 public class JettyServer extends AbstractHandler
 {
     /** needed by AeseServerThread */
     static String host;
     static int wsPort;
     JettyServer()
     {
         super();
     }
     /**
      * Main entry point
      * @param target the URN part of the URI
      * @param baseRequest 
      * @param request
      * @param response
      * @throws IOException
      * @throws ServletException 
      */
     @Override
     public void handle(String target,
                        Request baseRequest,
                        HttpServletRequest request,
                        HttpServletResponse response) 
         throws IOException, ServletException
     {
         try
         {
             response.setStatus(HttpServletResponse.SC_OK);
             String method = request.getMethod();
             baseRequest.setHandled( true );
             // remove webapp prefix
             if ( target.startsWith("/calliope") )
             {
                 target = target.substring( 9 );
                 Service.PREFIX = "/calliope";
             }
             if ( method.equals("GET") )
                 new AeseGetHandler().handle( request, response, target );
             else if ( method.equals("PUT") )
                 new AesePutHandler().handle( request, response, target );
             else if ( method.equals("DELETE") )
                 new AeseDeleteHandler().handle( request, response, target );
             else if ( method.equals("POST") )
                 new AesePostHandler().handle( request, response, target );
             else
                 throw new AeseException("Unknown http method "+method);
             response.setStatus(HttpServletResponse.SC_OK);
         }
         catch ( Exception e )
         {
             response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            System.out.println(e.getMessage());
         }
         //response.getWriter().close();
     }
     /**
      * Read the commandline arguments
      * @param args the arguments passed into java
      */
     static boolean readArgs( String[] args )
     {
         boolean sane = true;
         try
         {
             int dbPort = 5984;
             wsPort = 8080;
             host = "localhost";
             String webRoot = "/var/www";
             String password = "";
             String user = null;
             Repository repository = Repository.COUCH;
             for ( int i=0;i<args.length;i++ )
             {
                 if ( args[i].charAt(0)=='-' && args[i].length()==2 )
                 {
                     if ( args.length>i+1 )
                     {
                         if ( args[i].charAt(1) == 'u' )
                             user = args[i+1];
                         else if ( args[i].charAt(1) == 'p' )
                             password = args[i+1];
                         else if ( args[i].charAt(1) == 'h' )
                             password = args[i+1];
                         else if ( args[i].charAt(1) == 'd' )
                             dbPort = Integer.parseInt(args[i+1]);
                         else if ( args[i].charAt(1) == 'w' )
                             wsPort = Integer.parseInt(args[i+1]);
                         else if ( args[i].charAt(1) == 'r' )
                             repository = Repository.valueOf(args[i+1]);
                         else if ( args[i].charAt(1) == 'i' )
                             webRoot = args[i+1];
                         else
                             sane = false;
                     } 
                     else
                         sane = false;
                 }
                 if ( !sane )
                     break;
             }
             if ( sane )
             {
                 Connector.init( repository, user, password, host, dbPort, 
                     wsPort, webRoot);
             }
         }
         catch ( Exception e )
         {
             e.printStackTrace( System.out );
             sane = false;
         }
         return sane;
     }
     /**
      * Tell user how to invoke it on commandline
      */
     private static void usage()
     {
         System.out.println( "java -jar calliope.jar [-u user] [-p password]"
             +"[-h host] [-d db-port] [-w ws-port]" );
     }
     /**
      * Launch the AeseServer
      * @throws Exception 
      */
     public static void launchServer() throws Exception
     {
         JettyServerThread p = new JettyServerThread();
         p.start();
     }
     /**
      * Launch the Jetty service
      * @param args unused
      * @throws Exception 
      */
     public static void main(String[] args) throws Exception
     {
         if ( readArgs(args) )
             launchServer();
         else
             usage();
     }
 }
