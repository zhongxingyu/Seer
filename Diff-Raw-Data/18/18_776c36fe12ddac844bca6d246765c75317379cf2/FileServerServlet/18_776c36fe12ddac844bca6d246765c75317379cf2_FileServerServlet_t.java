 package org.sonatype.jettytestsuite.proxy;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.codehaus.plexus.util.IOUtil;
 
 public class FileServerServlet
     extends AbstractMonitorServlet
 {
     private static final long serialVersionUID = -6702619558275132007L;
 
     private File content;
 
     public FileServerServlet( File content )
     {
         this.content = content;
     }
 
     @Override
     public void service( HttpServletRequest req, HttpServletResponse res )
         throws ServletException, IOException
     {
         String path = req.getPathInfo();
 
         File file = new File( content, path );
         if ( !file.exists() )
         {
             res.sendError( HttpServletResponse.SC_NOT_FOUND, "File not found " + file.getAbsolutePath() );
             return;
         }
         if ( !file.isFile() )
         {
             res.sendError( HttpServletResponse.SC_FORBIDDEN, "Directory not accessible " + file.getAbsolutePath() );
             return;
         }
 
        addUri( req );

         InputStream input = new FileInputStream( file );
         OutputStream output = res.getOutputStream();
         IOUtil.copy( input, output );
         IOUtil.close( input );
         IOUtil.close( output );
     }
 
 }
