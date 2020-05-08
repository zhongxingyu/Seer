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
 package novelang.produce;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.nio.charset.Charset;
 
 import novelang.system.LogFactory;
 import novelang.system.Log;
 import novelang.book.Book;
 import novelang.common.FileTools;
 import novelang.common.LanguageTools;
 import novelang.common.Problem;
 import novelang.common.Renderable;
 import novelang.common.StructureKind;
 import novelang.configuration.ProducerConfiguration;
 import novelang.configuration.RenderingConfiguration;
 import novelang.loader.ResourceName;
 import novelang.part.Part;
 import novelang.rendering.GenericRenderer;
 import novelang.rendering.HtmlWriter;
 import novelang.rendering.NlpWriter;
 import novelang.rendering.PdfWriter;
 import novelang.rendering.PlainTextWriter;
 import novelang.rendering.RenditionMimeType;
 import novelang.rendering.XmlWriter;
 
 /**
  * Produces a document into passed-in {@link DocumentRequest}s.
  *
  * @author Laurent Caillette
  */
 public class DocumentProducer {
 
   private static final Log LOG = LogFactory.getLog( DocumentProducer.class ) ;
 
   private final File basedir ;
   private final RenderingConfiguration renderingConfiguration ;
   private final Charset defaultSourceCharset ;
 
 
   public DocumentProducer( ProducerConfiguration configuration ) {
     this.basedir = configuration.getContentConfiguration().getContentRoot() ;
     this.renderingConfiguration = configuration.getRenderingConfiguration() ;
     this.defaultSourceCharset = configuration.getContentConfiguration().getSourceCharset() ;
   }
 
   public Iterable< Problem > produce(
       final AbstractRequest request,
       final OutputStream outputStream
   ) throws Exception {
     return produce( request, createRenderable( request ), outputStream ) ;
   }
 
   public Iterable< Problem > produce(
       final AbstractRequest request,
       final Renderable rendered,
       final OutputStream outputStream
   ) throws Exception {
 
     final RenditionMimeType mimeType = request.getRenditionMimeType() ;
 
     // Java-flavored curryfication, wow!
     class Serve {
       public void with( GenericRenderer renderer ) throws Exception {
         serve( outputStream, renderer, rendered ) ;
       }
     }
     final Serve serve = new Serve() ;
 
     LOG.debug( "Attempting to produce '%s' %s", request.getOriginalTarget(), request.toString() ) ;
 
     final ResourceName stylesheet = LanguageTools.firstNotNull(
         request.getAlternateStylesheet(),
         rendered.getCustomStylesheetMap().get( mimeType )
     ) ;
 
     final Charset charset = rendered.getRenderingCharset() ;
 
     switch( mimeType ) {
 
       case PDF :
         serve.with( new GenericRenderer( new PdfWriter( renderingConfiguration, stylesheet ) ) ) ;
         break ;
 
       case TXT :
         serve.with( new GenericRenderer( new PlainTextWriter( charset ) ) ) ;
         break ;
 
       case XML :
         serve.with( new GenericRenderer( new XmlWriter() ) ) ;
         break ;
 
       case HTML :
         serve.with( new GenericRenderer(
             new HtmlWriter( renderingConfiguration, stylesheet, charset ) ) ) ;
         break ;
 
       case NLP :
         serve.with( new GenericRenderer(
             new NlpWriter( renderingConfiguration, stylesheet, charset ) ) ) ;
         break ;
 
       default :
         throw new IllegalArgumentException( "Unsupported: " + mimeType ) ;
     }
 
     LOG.debug( "Done with '%s'", request.getOriginalTarget() ) ;
 
 
     return rendered.getProblems() ;
     
   }
 
   private void serve(
       OutputStream outputStream,
       GenericRenderer renderer,
       Renderable rendered
   ) throws Exception {
     renderer.render( rendered, outputStream ) ;
   }
 
   public Renderable createRenderable( AbstractRequest documentRequest )
       throws IOException
   {
     final Charset suggestedRenderingCharset = renderingConfiguration.getDefaultCharset() ;
 
     try {
       final File bookFile = FileTools.load(
           basedir,
           documentRequest.getDocumentSourceName(),
           StructureKind.BOOK.getFileExtensions()
       ) ;
       return new Book(
           basedir,
           bookFile,
           defaultSourceCharset,
           suggestedRenderingCharset,
           documentRequest.getTags()
       ) ;
     } catch( FileNotFoundException e ) {
       final File partFile = FileTools.load(
           basedir,
           documentRequest.getDocumentSourceName(),
           StructureKind.PART.getFileExtensions()
       ) ;
       return new Part( 
           partFile, 
           defaultSourceCharset, 
           suggestedRenderingCharset
       ).relocateResourcePaths( basedir ).makeStandalone( documentRequest.getTags() ) ;
     }
 
   }
 
 
 }
