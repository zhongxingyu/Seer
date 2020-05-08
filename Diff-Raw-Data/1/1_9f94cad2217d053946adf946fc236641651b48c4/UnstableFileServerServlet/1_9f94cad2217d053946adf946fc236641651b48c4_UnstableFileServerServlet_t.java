 package org.sonatype.jettytestsuite.proxy;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class UnstableFileServerServlet
     extends FileServerServlet
 {
 
     private static final long serialVersionUID = -6940218205740360901L;
 
     private int numberOfTries;
 
     private final int returnCode;
 
     public UnstableFileServerServlet( int numberOfTries, int returnCode, File content )
     {
         super( content );
         this.numberOfTries = numberOfTries;
         this.returnCode = returnCode;
     }
 
     @Override
     public void service( HttpServletRequest req, HttpServletResponse res )
         throws ServletException, IOException
     {
         if ( numberOfTries > 0 )
         {
            numberOfTries--;
             res.sendError( returnCode );
             return;
         }
         else
         {
             super.service( req, res );
         }
 
     }
 
 }
