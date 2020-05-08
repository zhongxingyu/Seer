 /*
  * Copyright (C) 2008 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package novelang.rendering;
 
 import java.io.OutputStream;
 import java.nio.charset.Charset;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import org.apache.fop.apps.FOPException;
 import org.apache.fop.apps.FOUserAgent;
 import org.apache.fop.apps.Fop;
 import org.apache.fop.apps.FopFactory;
 import org.apache.fop.apps.MimeConstants;
 import org.xml.sax.ContentHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import novelang.configuration.RenderingConfiguration;
 import novelang.common.metadata.TreeMetadata;
 import novelang.common.Nodepath;
 import novelang.loader.ResourceName;
 import novelang.parser.Symbols;
 
 /**
  * @author Laurent Caillette
  */
 public class PdfWriter extends XslWriter {
 
   private static final Logger LOGGER = LoggerFactory.getLogger( PdfWriter.class ) ;
 
   protected static final ResourceName DEFAULT_FO_STYLESHEET = new ResourceName( "pdf.xsl" ) ; 
 
   public PdfWriter( RenderingConfiguration configuration, ResourceName stylesheet ) {
     super( configuration, null == stylesheet ? DEFAULT_FO_STYLESHEET : stylesheet ) ;
   }
 
 
 // ==========
 // Generation
 // ==========
 
   protected final ContentHandler createSinkContentHandler(
       OutputStream outputStream,
       TreeMetadata treeMetadata,
       Charset encoding
   )
       throws FOPException
   {
     final FopFactory fopFactory = FopFactory.newInstance() ;
     final FOUserAgent foUserAgent = fopFactory.newFOUserAgent() ;
 
     final Fop fop ;
     try {
       fop = fopFactory.newFop( MimeConstants.MIME_PDF, foUserAgent, outputStream ) ;
     } catch( FOPException e ) {
       throw new RuntimeException( e );
     }
 
     return fop.getDefaultHandler() ;
 
   }
 
 // ========
 // Litteral
 // ========
 
  private static final Pattern FIND_SPACE = Pattern.compile( "(\\ )" ) ;
  private static final String REPLACE_SPACE = "&nbsp;" ;

  private static final Pattern FIND_AMPERSAND = Pattern.compile( "(\\&)" ) ;
  private static final String REPLACE_AMPERSAND = "&amp;" ;

   public void writeLitteral( Nodepath kinship, String word ) throws Exception {
    LOGGER.debug( "{} word: '{}'", kinship, word ) ;
//    final String ampersandsReplaced = FIND_AMPERSAND.matcher(
//            FIND_SPACE.matcher( word ).replaceAll( REPLACE_SPACE )
//        ).replaceAll( REPLACE_AMPERSAND )
//    ;
    super.write( kinship, Symbols.unescapeText( word ) ) ;
   }
 
 
 }
