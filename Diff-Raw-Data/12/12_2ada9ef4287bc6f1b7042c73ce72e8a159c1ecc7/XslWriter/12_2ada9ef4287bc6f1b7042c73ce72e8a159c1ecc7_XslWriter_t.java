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
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.nio.charset.Charset;
 import javax.xml.transform.Source;
 import javax.xml.transform.Templates;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.URIResolver;
 import javax.xml.transform.sax.SAXResult;
 import javax.xml.transform.sax.SAXSource;
 import javax.xml.transform.sax.SAXTransformerFactory;
 import javax.xml.transform.sax.TemplatesHandler;
 import javax.xml.transform.sax.TransformerHandler;
 
 import org.apache.xerces.parsers.SAXParser;
 import novelang.system.LogFactory;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 import com.google.common.base.Preconditions;
 import novelang.common.metadata.DocumentMetadata;
 import novelang.configuration.RenderingConfiguration;
 import novelang.loader.ResourceLoader;
 import novelang.loader.ResourceName;
 import novelang.parser.NodeKindTools;
 import novelang.rendering.xslt.validate.SaxConnectorForVerifier;
 import novelang.rendering.xslt.validate.SaxMulticaster;
 import novelang.system.DefaultCharset;
 import novelang.system.Log;
 
 /**
  * @author Laurent Caillette
  */
 public class XslWriter extends XmlWriter {
 
   protected static final Log LOG = LogFactory.getLog( XslWriter.class ) ;
 
   protected final EntityResolver entityResolver;
   protected final URIResolver uriResolver;
   protected static final RenditionMimeType DEFAULT_RENDITION_MIME_TYPE = RenditionMimeType.XML ;
 
   protected final ResourceName xslFileName ;
   protected final ResourceLoader resourceLoader ;
   protected final EntityEscapeSelector entityEscapeSelector ;
   private static final ResourceName IDENTITY_XSL_FILE_NAME = new ResourceName( "identity.xsl" ) ;
 
   public XslWriter( RenderingConfiguration configuration, ResourceName xslFileName ) {
     this( configuration, xslFileName, DefaultCharset.RENDERING, DEFAULT_RENDITION_MIME_TYPE ) ;
   }
 
   public XslWriter(
       String namespaceUri,
       String nameQualifier,
       RenderingConfiguration configuration,
       ResourceName xslFileName
   ) {
     this(
         namespaceUri,
         nameQualifier,
         configuration,
         xslFileName,
         DEFAULT_RENDITION_MIME_TYPE
     ) ;
   }
 
   public XslWriter(
       String namespaceUri,
       String nameQualifier,
       RenderingConfiguration configuration,
       ResourceName xslFileName,
       RenditionMimeType mimeType
   ) {
     this(
         namespaceUri,
         nameQualifier,
         configuration,
         xslFileName,
         DefaultCharset.RENDERING,
         mimeType,
         NO_ENTITY_ESCAPE
     ) ;
   }
 
   public XslWriter(
       RenderingConfiguration configuration,
       ResourceName xslFileName,
       Charset charset,
       RenditionMimeType mimeType
   ) {
     this( configuration, xslFileName, charset, mimeType, NO_ENTITY_ESCAPE ) ;
   }
 
   public XslWriter(
       RenderingConfiguration configuration,
       ResourceName xslFileName,
       Charset charset,
       RenditionMimeType mimeType,
       EntityEscapeSelector entityEscapeSelector
   ) {
     this(
         NAMESPACE_URI,
         NAME_QUALIFIER,
         configuration,
         xslFileName,
         charset,
         mimeType,
         entityEscapeSelector
     ) ;
   }
 
   public XslWriter(
       String namespaceUri,
       String nameQualifier,
       RenderingConfiguration configuration,
       ResourceName xslFileName,
       Charset charset,
       RenditionMimeType mimeType,
       EntityEscapeSelector entityEscapeSelector
   ) {
     super( namespaceUri, nameQualifier, charset, mimeType ) ;
     this.entityEscapeSelector = Preconditions.checkNotNull( entityEscapeSelector ) ;
     this.resourceLoader = Preconditions.checkNotNull( configuration.getResourceLoader() ) ;
 
     if( null == xslFileName ) {
       xslFileName = IDENTITY_XSL_FILE_NAME ;
     }
     this.xslFileName = xslFileName ;
     entityResolver = new LocalEntityResolver() ;
     uriResolver = new LocalUriResolver() ;
     LOG.debug( "Created %s with stylesheet %s", getClass().getName(), xslFileName ) ;
   }
 
 
 
   protected ContentHandler createContentHandler(
       OutputStream outputStream,
       DocumentMetadata documentMetadata,
       Charset charset
   )
       throws Exception
   {
     LOG.debug( "Creating ContentHandler with charset %s", charset.name() );
 
     final SAXTransformerFactory saxTransformerFactory =
         ( SAXTransformerFactory ) TransformerFactory.newInstance() ;
     saxTransformerFactory.setURIResolver( uriResolver ) ;
 
     final TemplatesHandler templatesHandler = saxTransformerFactory.newTemplatesHandler() ;
    
     final XMLReader reader = XMLReaderFactory.createXMLReader() ;
 
     final ContentHandler multicaster = connectXpathVerifier( templatesHandler ) ;
 
     reader.setContentHandler( multicaster ) ;
 
     reader.setEntityResolver( entityResolver ) ;
 
     reader.parse( new InputSource( resourceLoader.getInputStream( xslFileName ) ) ) ;
 
     final Templates templates = templatesHandler.getTemplates() ;
     final TransformerHandler transformerHandler =
         saxTransformerFactory.newTransformerHandler( templates ) ;
     configure( transformerHandler.getTransformer(), documentMetadata ) ;
 
     final ContentHandler sinkContentHandler =
         createSinkContentHandler( outputStream, documentMetadata, charset ) ;
     transformerHandler.setResult( new SAXResult( sinkContentHandler ) ) ;
 
     return transformerHandler ;
 
   }
 
   private void configure( Transformer transformer, DocumentMetadata documentMetadata ) {
     transformer.setParameter(
         "timestamp",
         documentMetadata.getCreationTimestamp()
     ) ;
     transformer.setParameter(
         "charset",
         documentMetadata.getCharset().name()
     ) ;
   }
 
   protected ContentHandler createSinkContentHandler(
       OutputStream outputStream,
       DocumentMetadata documentMetadata,
       Charset charset
   ) throws Exception
   {
     return super.createContentHandler( outputStream, documentMetadata, charset ) ;
   }
 
   public interface EntityEscapeSelector {
     boolean shouldEscape( String publicId, String systemId ) ;
   }
 
   private static final EntityEscapeSelector NO_ENTITY_ESCAPE = new EntityEscapeSelector() {
     public boolean shouldEscape( String publicId, String systemId ) {
       return false ;
     }
   } ;
 
   private static SaxMulticaster connectXpathVerifier( TemplatesHandler templatesHandler ) {
     final SaxConnectorForVerifier xpathVerifier =
         new SaxConnectorForVerifier( NAMESPACE_URI, NodeKindTools.getRenderingNames() ) ;
 
     final SaxMulticaster multicaster = new SaxMulticaster() ;
     multicaster.add( templatesHandler ) ;
     multicaster.add( xpathVerifier ) ;
     return multicaster ;
   }
 
 
   /**
    * Fetches local files in the same directory as the stylesheet.
    * This is because the {@code systemId} as read by the stylesheet loader is prefixed
    * with current directory (bug?).
    */
   protected class LocalEntityResolver implements EntityResolver {
 
     public InputSource resolveEntity(
         String publicId,
         String systemId
     ) throws SAXException, IOException {
       systemId = systemId.substring( systemId.lastIndexOf( "/" ) + 1 ) ;
       final boolean shouldEscapeEntity = entityEscapeSelector.shouldEscape( publicId, systemId ) ;
       LOG.debug(
           "Resolving entity publicId='%s' systemId='%s' escape=%s" ,
           publicId,
           systemId,
           shouldEscapeEntity
       ) ;
       final InputSource dtdSource = new InputSource(
           resourceLoader.getInputStream( new ResourceName( systemId ) ) );
       if( shouldEscapeEntity ) {
         return DtdTools.escapeEntities( dtdSource ) ;
       } else {
         return dtdSource;
       }
     }
   }
 
   protected class LocalUriResolver implements URIResolver {
 
     public Source resolve( String href, String base ) throws TransformerException {
       LOG.debug( "Resolving URI href='%s' base='%s'", href, base ) ;
 
       final SAXTransformerFactory saxTransformerFactory =
           ( SAXTransformerFactory ) TransformerFactory.newInstance() ;
       saxTransformerFactory.setURIResolver( uriResolver ) ;
 
       final SaxConnectorForVerifier xpathVerifier =
           new SaxConnectorForVerifier( NAMESPACE_URI, NodeKindTools.getRenderingNames() ) ;
 
 
       final XMLReader reader ;
 
       // It would be more standard to use XMLReaderFactory.createXMLReader() instead of deriving
       // directly from a Xerces class. Then it would require some kind of wrapper.
       reader = new SAXParser() {
         @Override
         public void setContentHandler( ContentHandler contentHandler ) {
           final SaxMulticaster multicaster = new SaxMulticaster() ;
           multicaster.add( contentHandler ) ;
           multicaster.add( xpathVerifier ) ;
           super.setContentHandler( multicaster ) ;
         }
       } ;
 
       reader.setEntityResolver( entityResolver ) ;
 
       return new SAXSource(
           reader,
           new InputSource( resourceLoader.getInputStream( new ResourceName( href ) ) )
       ) ;
 
     }
 
   }
 }
