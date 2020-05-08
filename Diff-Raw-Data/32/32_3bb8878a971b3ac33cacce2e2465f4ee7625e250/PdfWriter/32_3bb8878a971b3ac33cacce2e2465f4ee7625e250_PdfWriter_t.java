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
 
 import org.apache.fop.apps.FOPException;
 import org.apache.fop.apps.FOUserAgent;
 import org.apache.fop.apps.Fop;
 import org.apache.fop.apps.FopFactory;
 import org.apache.fop.apps.MimeConstants;
 import novelang.system.LogFactory;
 import novelang.system.Log;
 import org.xml.sax.ContentHandler;
 import novelang.common.metadata.DocumentMetadata;
 import novelang.configuration.RenderingConfiguration;
 import novelang.loader.ResourceName;
 
 /**
  * @author Laurent Caillette
  */
 public class PdfWriter extends XslWriter {
 
   private static final Log LOG = LogFactory.getLog( PdfWriter.class ) ;
 
  public static final ResourceName DEFAULT_FO_STYLESHEET = new ResourceName( "pdf.xsl" ) ;
   protected final FopFactory fopFactory ;
 
   public PdfWriter(
       RenderingConfiguration configuration,
       ResourceName stylesheet,
       String namespaceUri,
       String nameQualifier
   ) {
     super(
         namespaceUri,
         nameQualifier,
         configuration,
         null == stylesheet ? DEFAULT_FO_STYLESHEET : stylesheet
     ) ;
     fopFactory = configuration.getFopFactory() ;
   }
 
   public PdfWriter( RenderingConfiguration configuration, ResourceName stylesheet ) {
     super( configuration, null == stylesheet ? DEFAULT_FO_STYLESHEET : stylesheet ) ;
     fopFactory = configuration.getFopFactory() ;
   }
 
 // ==========
 // Generation
 // ==========
 
   protected final ContentHandler createSinkContentHandler(
       OutputStream outputStream,
       DocumentMetadata documentMetadata,
       Charset charset
   )
       throws FOPException
   {
     final FOUserAgent foUserAgent = fopFactory.newFOUserAgent() ;
     foUserAgent.setTargetResolution( 300 ) ; // dpi
 
     final Fop fop ;
     try {
       fop = fopFactory.newFop( MimeConstants.MIME_PDF, foUserAgent, outputStream ) ;
     } catch( FOPException e ) {
       throw new RuntimeException( e );
     }
 
     return fop.getDefaultHandler() ;
 
   }
 
 
 }
