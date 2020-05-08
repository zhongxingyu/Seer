 /* This file is part of hritserver.
  *
  *  hritserver is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  hritserver is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with hritserver.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package hritserver.handler.post;
 import hritserver.constants.Formats;
 import hritserver.importer.Archive;
 import hritserver.handler.post.importer.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import hritserver.exception.HritException;
 import hritserver.constants.Config;
 import hritserver.constants.Params;
 import hritserver.constants.JSONKeys;
 import hritserver.json.JSONDocument;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 import java.util.ArrayList;
 /**
  * Handle importation of mixed files from the import dialog.
  * @author desmond
  */
 public class HritMixedImportHandler extends HritImportHandler
 {
     HritMixedImportHandler()
     {
         super();
         style = Formats.TEI+"%2Fdefault";
     }
     /**
      * Handle posted files from the input dialog. Can be mixed TEXT etc files
      * @param request the request object
      * @param response the response object
      * @param urn the urn of the request
      * @throws HritException 
      */
     public void handle( HttpServletRequest request, 
         HttpServletResponse response, String urn ) throws HritException
     {
         try
         {
             if (ServletFileUpload.isMultipartContent(request) )
             {
                 parseImportParams( request );
                 if ( !demo )
                 {
                     Archive cortex = new Archive(docID.getWork(), 
                         docID.getAuthor());
                     cortex.setStyle( style );
                     Archive corcode = new Archive(docID.getWork(), 
                         docID.getAuthor());
                     corcode.setStyle( style );
                     StageOne stage1 = new StageOne( files );
                     log.append( stage1.process(cortex,corcode) );
                     if ( stage1.hasFiles() )
                     {
                         // it's safer to do this in a try-catch handler
                         try
                         {
                             StageTwo stage2 = new StageTwo( stage1, true );
                             log.append( stage2.process(cortex,corcode) );
                             StageThreeXML stage3Xml = new StageThreeXML( stage2, 
                                 style );
                             stage3Xml.setStripConfig( getConfig(Config.stripper,
                                 stripperName) );
                             stage3Xml.setSplitConfig( getConfig(Config.splitter,
                                 splitterName) );
                             if ( stage3Xml.hasTEI() )
                             {
                                 ArrayList<File> notes = stage3Xml.getNotes();
                                 if ( notes.size()> 0 )
                                 {
                                     Archive nCorTex = new Archive(docID.getWork(), 
                                         docID.getAuthor());
                                    nCorTex.setStyle( style );
                                     Archive nCorCode = new Archive(docID.getWork(), 
                                         docID.getAuthor());
                                    StageThreeXML s3notes = new StageThreeXML(style);
                                    s3notes.setStripConfig( 
                                        getConfig(Config.stripper, stripperName) );
                                    s3notes.setSplitConfig( 
                                        getConfig(Config.splitter, splitterName) );
                                     for ( int j=0;j<notes.size();j++ )
                                         s3notes.add(notes.get(j));
                                     log.append( s3notes.process(nCorTex,nCorCode) );
                                     addToDBase(nCorTex, "cortex", "notes" );
                                     addToDBase( nCorCode, "corcode", "notes" );
                                 }
                                 if ( xslt == null )
                                     xslt = Params.XSLT_DEFAULT;
                                 String transform = getConfig(Config.xslt,xslt);
                                 JSONDocument jDoc = JSONDocument.internalise( 
                                     transform );      
                                 stage3Xml.setTransform( (String)
                                     jDoc.get(JSONKeys.BODY) );
                             }
                             log.append( stage3Xml.process(cortex,corcode) );
                             // process the text filers
                             StageThreeText stage3Text = new StageThreeText( 
                                 filterName );
                             stage3Text.setConfig( getConfig(Config.text,
                                 filterName+"%2F"+textName) );
                             ArrayList<File> stage2Files = stage2.getFiles();
                             for ( int i=0;i<stage2Files.size();i++ )
                             {
                                 File f = stage2Files.get(i);
                                 if ( !stage3Xml.containsFile(f)&&!f.isXML() )
                                     stage3Text.add( f );
                             }
                             log.append( stage3Text.process(cortex,corcode) );
                             addToDBase( cortex, "cortex", "" );
                             addToDBase( corcode, "corcode", "" );
                         }
                         catch ( Exception e )
                         {
                             log.append( e.getMessage() );
                         }
                     }
                     else
                         log.append("No cortex/corcode created\n");
                     response.setContentType("text/html;charset=UTF-8");
                     response.getWriter().println( wrapLog() );
                 }
                 else
                 {
                     response.setContentType("text/html;charset=UTF-8");
                     response.getWriter().println( "<p>Not enabled on public server</p>" );
                 }
             }
         }
         catch ( Exception e )
         {
             throw new HritException( e );
         }
     }
 }
