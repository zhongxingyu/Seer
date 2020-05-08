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
 package calliope.handler.put;
 import calliope.annotation.*;
 import calliope.AeseFormatter;
 import calliope.constants.Formats;
 import calliope.constants.Services;
 import calliope.json.corcode.STILDocument;
 import calliope.Utils;
 import calliope.constants.Database;
 import calliope.handler.AeseHandler;
 import calliope.exception.AeseException;
 import calliope.handler.post.AeseImportHandler;
 import calliope.path.Path;
 import calliope.json.JSONResponse;
 import calliope.json.JSONDocument;
 import calliope.AeseStripper;
 import calliope.Connector;
 import calliope.constants.Config;
 import calliope.constants.JSONKeys;
 import calliope.constants.Params;
 import calliope.exception.AnnotationException;
 import calliope.exception.NativeException;
 import calliope.tests.html.HTMLComment;
 import edu.luc.nmerge.mvd.diff.Diff;
 import edu.luc.nmerge.mvd.diff.Matrix;
 import java.util.Locale;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.List;
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.FileItemFactory;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
 /**
  * Handle a put (update) request
  * @author desmond
  */
 public class AesePutHandler extends AeseHandler
 {
     String HTML_RECIPE = "{ \"type\":\"stripper\","
     +"\"removals\": [ \"head\" ],\"rules\": [ ] }";
     String stripperName = "html";
     String language = Locale.getDefault().getLanguage();
     String fileContent = null;
     String style = "html";
     String title = "no title";
     String version1 = "base";
     String author = "Anonymous";
     String description = "uploaded";
     private void parseRequest( HttpServletRequest request ) 
         throws AeseException
     {
         try
         {
             FileItemFactory factory = new DiskFileItemFactory();
             // Create a new file upload handler
             ServletFileUpload upload = new ServletFileUpload(factory);
             // Parse the request
             List items = upload.parseRequest( request );
             for ( int i=0;i<items.size();i++ )
             {
                 FileItem item = (FileItem) items.get( i );
                 if ( item.isFormField() )
                 {
                     String fieldName = item.getFieldName();
                     if ( fieldName != null )
                     {
                         String contents = item.getString();
                         if ( fieldName.equals(Params.STRIPPER) )
                             stripperName = contents;
                         else if ( fieldName.equals(Params.ENCODING) )
                             encoding = contents;
                         else if ( fieldName.equals(Params.STYLE) )
                             style = contents;
                         else if ( fieldName.equals(Params.TITLE) )
                             title = contents;
                         else if ( fieldName.equals(Params.VERSION1) )
                             version1 = contents;
                         else if ( fieldName.equals(Params.AUTHOR) )
                             author = contents;
                         else if ( fieldName.equals(Params.DESCRIPTION) )
                             description = contents;
                     }
                 }
                 else if ( item.getName().length()>0 )
                 {
                     byte[] rawData = item.get();
                     guessEncoding( rawData );
                     if ( encoding == null )
                         encoding = guessEncoding(rawData);
                     fileContent = new String( rawData, encoding );
                 }
             }  
         }
         catch ( Exception e )
         {
             throw new AeseException( e );
         }
     }
     /**
      * Put half of a HTML document either to CorCode or CorTex
      * @param collName the collection name
      * @param body the text of the document
      * @param docID its docID
      * @param format its format
      * @throws Exception 
      */
     private void putToDb( String collName, String body, String docID, 
         String format ) throws Exception
     {
         JSONDocument doc = new JSONDocument();
         doc.add( JSONKeys.TITLE, title, false );
         doc.add( JSONKeys.VERSION1, version1, false );
         doc.add( JSONKeys.DESCRIPTION, description, false );
         doc.add( JSONKeys.AUTHOR, author, false );
         doc.add( JSONKeys.STYLE, style, false );
         doc.add( JSONKeys.FORMAT, format, false );
         doc.add( JSONKeys.BODY, body, false );
         Connector.getConnection().putToDb(collName, docID, doc.toString() );
     }
     /**
      * Return a list of annotations sorted on start offset
      * @param docID the docID for list annotations for
      * @return an array of sorted Annotation objects
      */
     Annotation[] getAnnotations( String docID )
     {
         return null;
     }
     /**
      * Write the transformed html back to the caller
      * @param html the HTML to write back
      * @param styles an array of complete css styles
      * @param response the http response object
      * @throws AeseException 
      */
     void writeHTMLResponse( JSONResponse html, String[] styles, 
         HttpServletResponse response ) throws AeseException
     {
         response.setContentType("text/html;charset="+encoding);
         try
         {
             HTMLComment comment = new HTMLComment();
             comment.addText( "styles: ");
             for ( int i=0;i<styles.length;i++ )
                 comment.addText( styles[i] );
             response.getWriter().println( comment.toString() );
             response.getWriter().println(html.getBody());   
         }
         catch ( Exception e )
         {
             throw new AeseException( e );
         }
     }
     /**
      * Convert a raw annotation to its corcode equvalent
      * @param anns an array of annotations
      * @return the STIL representation of all the annotations
      */
     private String annToCorCode( Annotation[] anns )
     {
         STILDocument stil = new STILDocument();
         // convert anns to STIL here
         return stil.toString();
     }
     /**
      * Given an array of annotations adjust their offsets
      * @param anns and array of annotations
      * @param diffs an array of diffs between the old and new base texts
      * @param diffLen the difference in length new-old (may be negative)
      */
     private void updateAnnotations( Annotation[] anns, Diff[] diffs, 
         int diffLen )
     {
         for ( int i=0;i<diffs.length;i++ )
         {
             for ( int j=0;j<anns.length;j++ )
             {
                 int start = anns[j].start();
                 if ( start > diffs[i].oldEnd() )
                     break;
                 else if ( anns[j].end() < diffs[i].oldOff() )
                 {
                     int delta = diffs[i].newOff()-diffs[i].oldOff();
                     anns[j].updateOff(start+delta);
                 }
                 else if ( anns[j].end() > diffs[i].oldOff() 
                     && start < diffs[i].oldEnd() )
                 {
                     // compute the proportion of the overlap that gets 
                     // carried over to the updated annotation
                     int overlap = Math.min(anns[j].end(),diffs[i].oldEnd())
                         -Math.max(start,diffs[i].oldOff());
                     int prop;
                     if ( diffs[i].oldLen()==0 )
                         prop = diffs[i].newLen();
                     else
                         prop = (overlap*diffs[i].newLen())/diffs[i].oldLen();
                     // update the position of the annotation start
                     int newOff;
                     // 1) annotation starts before diff
                     if ( start < diffs[i].oldOff() )
                     {
                         int dist = diffs[i].oldOff()-start;
                         newOff = diffs[i].newOff()-dist;
                     }
                     // 2) diff starts before annotation
                     else if ( diffs[i].oldOff() < start )
                     {
                         float ratio = (float)diffs[i].newLen()
                             /(float)diffs[i].oldLen();
                         int dist = start-diffs[i].oldOff();
                         newOff = diffs[i].newOff()+(int)(ratio*dist);
                     }
                     // 3) equal
                     else
                         newOff = diffs[i].newOff();
                     // compute new length
                     int newLen = prop;
                     if ( anns[j].end() > diffs[i].oldEnd() )
                         newLen += anns[j].end()-diffs[i].oldEnd();
                     if ( start < diffs[i].oldOff() )
                         newLen += diffs[i].oldOff()-start;
                     anns[j].updateOff( newOff );
                     anns[j].updateLen( newLen );
                 }
             }
         }
         // in case the last annotation wasn't caught by a diff
         int lastStart = anns[anns.length-1].start();
         anns[anns.length-1].updateOff(lastStart+diffLen);
         // make the updates really stick
         try
         {
             for ( int i=0;i<anns.length;i++ )
             {
                 anns[i].update();
             }
         }
         catch ( AnnotationException ae )
         {
             ae.printStackTrace( System.out );
         }
     }
     void updateOneAnnotation( Annotation ann )
     {
         // compose and send an update annotation message to lorestore
     }
     void deleteOneAnnotation( Annotation ann )
     {
         // compose and send a delete annotaiton message to lorestore
     }
     @Override
     public void handle( HttpServletRequest request, 
         HttpServletResponse response, String urn ) throws AeseException
     {
         String prefix = Path.first( urn );
         if ( prefix != null )
         {
             if ( prefix.equals(Services.HTML) )
             {
                 Path path = new Path( urn );
                 String docID = path.getResourcePath(false);
                 try
                 {
                     parseRequest( request );
                     JSONResponse text = new JSONResponse();
                     JSONResponse markup = new JSONResponse();
                     AeseStripper stripper = new AeseStripper();
                     String config = AeseImportHandler.getConfig( 
                         Config.stripper, stripperName );
                     if ( config == null )
                         config = HTML_RECIPE;
                     int res = stripper.strip( fileContent, config, 
                         Formats.STIL, "html", language, null, 
                         Utils.isHtml(fileContent), text, markup );
                     Annotation[] anns = getAnnotations( docID );
                     String annCorCode = null;
                     if ( res == 1 )
                     {
                         // now diff the new version with that at the docID
                         String old = Connector.getConnection().getFromDb( 
                             Database.CORTEX, docID );
                         if ( old != null )
                         {
                             byte[] base = old.getBytes(encoding);
                             byte[] data = text.getBody().getBytes(encoding);
                             Diff[] diffs = Matrix.computeBasicDiffs( data, base );
                             if ( anns.length > 0 && diffs.length > 0 )
                             {
                                 updateAnnotations( 
                                     anns, diffs, data.length-base.length );
                                 // convert annotations to corcode
                                 annCorCode = annToCorCode( anns );
                                 for ( int i=0;i<anns.length;i++ )
                                 {
                                     if ( anns[i].isValid() )
                                         updateOneAnnotation( anns[i] );
                                     else
                                         deleteOneAnnotation( anns[i] );
                                 }
                             }
                         }
                         // in all cases we post the new version at the docid
                         putToDb( Database.CORTEX, text.getBody(), docID, 
                             Formats.TEXT );
                         putToDb( Database.CORCODE, markup.getBody(), 
                             docID+"/default", Formats.STIL );
                         // format using default+annotation corcode
                         // return formatted HTML
                         JSONResponse html = new JSONResponse();
                         int nCCs = (anns !=null &&anns.length>0)?2:1;
                         // 1. create corCodes array
                         String[] corCodes = new String[nCCs];
                         corCodes[0] = markup.getBody();
                         if ( nCCs > 1 )
                             corCodes[1] = annCorCode;
                         // 2. create styles array
                         String[] styles = new String[1];
                         styles[0] = Utils.fetchStyle(style);
                         String[] formats = new String[nCCs];
                         formats[0] = Formats.STIL;
                         if ( nCCs > 1 )
                             formats[1] = Formats.STIL;
                         res = new AeseFormatter().format( 
                             text.getBody().getBytes(encoding), 
                             corCodes, styles, formats, html );
                         if ( res == 0 )
                             throw new NativeException("formatting failed");
                         else
                             writeHTMLResponse( html, styles, response );
                     }
                     else
                         throw new Exception("Couldn't strip "+docID);
                 }
                 catch ( Exception e )
                 {
                     throw new AeseException(e);
                 }
             }
         }
         else
             throw new AeseException("Unknown PUT request "+urn);
     }
 }
