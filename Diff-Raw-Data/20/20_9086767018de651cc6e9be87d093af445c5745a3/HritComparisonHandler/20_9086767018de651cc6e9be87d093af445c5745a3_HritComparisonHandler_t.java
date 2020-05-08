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
 
 package hritserver.handler.get.compare;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import hritserver.HritFormatter;
 import hritserver.exception.*;
 import hritserver.constants.Params;
 import hritserver.constants.Formats;
 import hritserver.path.*;
 import hritserver.handler.HritMVD;
 import hritserver.json.JSONResponse;
 import hritserver.constants.Database;
 import hritserver.handler.get.HritHTMLHandler;
 import hritserver.Utils;
 import hritserver.constants.ChunkState;
 import hritserver.handler.HritVersion;
 import hritserver.tests.html.HTMLComment;
 import java.util.ArrayList;
 
 /**
  * Handle comparison between two versions of a document
  * @author desmond
  */
 public class HritComparisonHandler extends HritHTMLHandler
 {
     /**
      * Concatenate a path with its docid
      * @param urn the docid minus the version group and name
      * @param path the version-group and short name
      * @return 
      */
     String canonisePath( String urn, String path )
     {
         if ( !urn.endsWith("/")&&!path.endsWith("/") )
             return urn+"/"+path;
         else
             return urn+path;
     }
     /**
      * Get an array of CorCodes, and their styles and formats too
      * @param resPath the database+docID for the resource
      * @param version1 the group-path+version name
      * @param userCC an array of specified CorCode names for this docID
      * @param diffCC the CorCode of the diffs
      * @param styleNamess an array of predefined style-names
      * @param styles an empty arraylist of style names to be filled
      * @param formats an empty array of CorCode formats to be filled
      * @return a simple array of CorCode texts in their corresponding formats
      */
     String[] getCorCodes( String resPath, String version1, String[] userCC, 
         CorCode diffCC, String[] styleNames, ArrayList<String> styles, 
         ArrayList<String> formats ) throws HritException
     {
         String[] ccTexts = new String[userCC.length+1];
         // add diffCC entries to corcodes and formats but not styles
         ccTexts[0] = diffCC.toString();
         formats.add( Formats.STIL );
         // load user-defined styles
         if ( styleNames.length>0 )
         {
             String[] styleTexts = fetchStyles( styleNames );
             for ( int i=0;i<styleTexts.length;i++ )
                 styles.add( styleTexts[i] );
         }
         for ( int i=0;i<userCC.length;i++ )
         {
             String ccResource = Utils.canonisePath(resPath,userCC[i]);
             Path ccPath = new Path( ccResource );
             HritVersion hv = doGetMVDVersion( ccPath, version1 );
             try
             {
                 byte[] versionText = hv.getVersion();
                 if ( versionText == null )
                     throw new HritException("version not found");
                 ccTexts[i+1] = new String(versionText,"UTF-8");
                 styles.add( fetchStyle(hv.getStyle()) );
             }
             catch ( Exception e )
             {
                 throw new HritException( e );
             }
             formats.add( hv.getFormat() );
         }
         return ccTexts;
     }
     /**
      * Get the HTML of one version compared to another
      * @param request the request to read from
      * @param path the parsed URN
      * @return a formatted html String
      */
     @Override
     public void handle( HttpServletRequest request, 
         HttpServletResponse response, String urn ) throws HritException
     {
         // the version we will return, with added markup
         String version1 = request.getParameter( Params.VERSION1 );
         // the version to compare WITH
         String version2 = request.getParameter( Params.VERSION2 );
         // the name for the differences with version2 that we generate
         String diffKind = request.getParameter( Params.DIFF_KIND );
         if ( version1 != null && version2 != null )
         {
             if ( diffKind == null )
             {
                 diffKind = ChunkState.DELETED;
                 System.out.println("Missing parameter "
                     +Params.DIFF_KIND+" assuming "+ChunkState.DELETED );
             }
             CorCode cc = new CorCode( diffKind );
             Path path = new Path( urn );
             path.setName( Database.CORTEX );
             HritMVD text = loadMVD( path.getResource() );
             int v1 = text.mvd.getVersionByNameAndGroup(
                 Utils.getShortName(version1),Utils.getGroupName(version1));
             if ( v1 == -1 )
                 throw new HritException(version1+" not found");
             int v2 = text.mvd.getVersionByNameAndGroup(
                 Utils.getShortName(version2),
                 Utils.getGroupName(version2) );
             if ( v2 == -1 )
                 throw new HritException(version2+" not found");
             int[] lengths = text.mvd.getVersionLengths();
             if ( lengths==null || lengths.length<v1 || lengths.length<v2  )
                 throw new HritException( "lengths array is empty" );
             Run[] runs = new Run[1];
             runs[0] = new Run( 0, lengths[v1-1] );
             cc.compareText( text.mvd, v1, v2, diffKind, runs );
             // get corCodes
             String[] corCodes = getEnumeratedParams( Params.CORCODE, 
                 request.getParameterMap(), true );
             String[] styles = getEnumeratedParams( Params.STYLE, 
                 request.getParameterMap(), false );
             path.setName( Database.CORCODE );
             ArrayList<String> styleNames = new ArrayList<String>();
             ArrayList<String> formats = new ArrayList<String>();
             String[] ccTexts = getCorCodes( path.getResourcePath(true), 
                 version1, corCodes, cc, styles, styleNames, formats );
             String[] styleTexts = new String[styleNames.size()];
             styleNames.toArray( styleTexts );
             String[] formatTexts = new String[formats.size()];
             formats.toArray( formatTexts );
             // call the native library
             JSONResponse html = new JSONResponse();
             int res = new HritFormatter().format( text.mvd.getVersion(v1), 
                 ccTexts, styleTexts, formatTexts, html );
             if ( res == 0 )
                 throw new NativeException("formatting failed");
             else
             {
                 response.setContentType("text/html;charset=UTF-8");
                 try
                 {
                     HTMLComment comment = new HTMLComment();
                     for ( int i=0;i<styleTexts.length;i++ )
                         comment.addText( styleTexts[i] );
                     //System.out.println(html.getBody());
                     response.getWriter().println( comment.toString() );
                     response.getWriter().println(html.getBody());   
                 }
                 catch ( Exception e )
                 {
                     throw new HritException( e );
                 }
             }       
         }
         else if ( version1 != null )
         {
             // just get the version
             handleGetVersion( request, response, urn );
         }
         else
             throw new HritException("versions unspecified");
     }
 }
