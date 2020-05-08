 package com.enonic.cms.plugin.confluence;
 
 import com.enonic.cms.api.plugin.ext.http.HttpController;
 import com.enonic.cms.plugin.confluence.model.FileModel;
 import com.enonic.cms.plugin.confluence.service.WikiDocService;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public final class ConfluenceFileController
     extends HttpController
 {
     private WikiDocService service;
 
     public void setService( WikiDocService service )
     {
         this.service = service;
     }
 
     public void handleRequest( HttpServletRequest req, HttpServletResponse res )
         throws Exception
     {
        try {
         String page = req.getParameter( "page" );
         String file = req.getParameter( "file" );
 
        if ((file != null) && file.contains("?")) {
            file = file.substring( 0, file.indexOf( '?' ) );
        }


         if ( ( page == null ) || ( file == null ) )
         {
             sendNotFound( res );
             return;
         }
 
         FileModel model = this.service.getFile( page, file );
         if ( model == null )
         {
             sendNotFound( res );
             return;
         }
 
         res.setContentType( model.getContentType() );
         res.setHeader( "Content-Disposition", getDispositionHeader( req, file ) );
         res.getOutputStream().write( model.getData() );
        } catch ( Exception e ) {
            e.printStackTrace();
            throw e;
        }
     }
 
     private void sendNotFound( HttpServletResponse res )
         throws Exception
     {
         res.sendError( HttpServletResponse.SC_NOT_FOUND );
     }
 
     private String getDispositionHeader( HttpServletRequest req, String fileName )
     {
         StringBuffer str = new StringBuffer();
         if ( req.getParameter( "download" ) != null )
         {
             str.append( "attachment" );
         }
         else
         {
             str.append( "inline" );
         }
 
         str.append( "; filename=" ).append( fileName );
         return str.toString();
     }
 }
