 package novelang.batch;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 
 import org.apache.fop.apps.FopFactory;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 
 import novelang.common.Problem;
 import novelang.configuration.ConfigurationTools;
 import novelang.configuration.ContentConfiguration;
 import novelang.configuration.FopFontStatus;
 import novelang.configuration.ProducerConfiguration;
 import novelang.configuration.RenderingConfiguration;
 import novelang.loader.ResourceLoader;
 import novelang.produce.DocumentProducer;
 import novelang.produce.DocumentRequest;
 import novelang.produce.RequestTools;
 import novelang.system.DefaultCharset;
 import novelang.system.Log;
 import novelang.system.LogFactory;
 
 import com.google.common.collect.ImmutableList;
 
 /**
  * Transforms a given Novelang document into a String.
  * <p>
  * Parameters:
  * <ul>
  *   <li><code>content-root</code> The directory where content files reside. 
  *         Relative paths get resolved from here.
  *   </li>
  * </ul>
  * <pre>
  * <novelang
  *     content-root="" 
  *     log-directory="" 
  *     document-request="" 
  *     source-encoding="" 
  *     stylesheet=""
  *     destination-encoding=""
  *     content-property="html" 
  * />
  * </pre>
  * 
  * @author Laurent Caillette
  */
 public class NovelangTask extends Task {
 
   private static final Log LOG = LogFactory.getLog( NovelangTask.class ) ;
 
 
 // ===============  
 // Task properties
 // ===============  
   
   private File contentRoot ;
   private File styleDirectory ;
   private String documentRequest ;
   private String contentProperty ;
   private String renderingCharsetName ;
   
   public File getContentRoot() {
     return contentRoot ;
   }
 
   public void setContentRoot( File contentRoot ) {
     this.contentRoot = contentRoot ;
   }
 
   public File getStyleDirectory() {
     return styleDirectory;
   }
 
   public void setStyleDirectory( File styleDirectory ) {
     this.styleDirectory = styleDirectory;
   }
 
   public String getDocumentRequest() {
     return documentRequest ;
   }
 
   public void setDocumentRequest( String documentRequest ) {
     this.documentRequest = documentRequest ;
   }
 
   public String getContentProperty() {
     return contentProperty ;
   }
 
   public void setContentProperty( String contentProperty ) {
     this.contentProperty = contentProperty;
   }
 
   public String getRenderingCharsetName() {
     return renderingCharsetName ;
   }
 
   public void setRenderingCharsetName( String renderingCharsetName ) {
     this.renderingCharsetName = renderingCharsetName;
   }
 
 // =========
 // Execution
 // =========
   
   
   @Override
   public void execute() throws BuildException {
     if( contentRoot == null ) {
       contentRoot = getProject().getBaseDir() ;
     }
     if( documentRequest == null ) {
       throw new BuildException( "documentRequest cannot be null" ) ;
     }
 
     final Charset renderingCharset ;
     if( renderingCharsetName == null ) {
       renderingCharset = DefaultCharset.RENDERING ;
     } else {
       renderingCharset = Charset.forName( renderingCharsetName ) ;
     }
 
    // TODO make style directory relative to content directory once this becomes the 
    // standard elsewhere.
     final ResourceLoader resourceLoader = ConfigurationTools.createResourceLoader(
        ImmutableList.of( styleDirectory.getAbsoluteFile() ) ) ;
 
 
     final RenderingConfiguration renderingConfiguration = new RenderingConfiguration() {
       public ResourceLoader getResourceLoader() {
         return resourceLoader ;
       }
 
       public FopFactory getFopFactory() {
         throw new UnsupportedOperationException( "No FOP usage expected" ) ;
       }
 
       public FopFontStatus getCurrentFopFontStatus() {
         throw new UnsupportedOperationException( "No FOP usage expected" ) ;
       }
 
       public Charset getDefaultCharset() {
         return renderingCharset ;
       }
     } ;
 
     final ContentConfiguration contentConfiguration = new ContentConfiguration() {
       public File getContentRoot() {
         return contentRoot ;
       }
 
       public Charset getSourceCharset() {
         return DefaultCharset.SOURCE ;
       }
     };
 
     final ProducerConfiguration producerConfiguration = new ProducerConfiguration() {
       public RenderingConfiguration getRenderingConfiguration() {
         return renderingConfiguration ;
       }
 
       public ContentConfiguration getContentConfiguration() {
         return contentConfiguration ;
       }
     } ;
 
     final DocumentRequest request = RequestTools.createDocumentRequest( documentRequest ) ;
     if( request == null ) {
       throw new BuildException( "Could not parse request '" + documentRequest + "'" ) ;
     }
 
     final DocumentProducer documentProducer = new DocumentProducer( producerConfiguration ) ;
 
     final Iterable< Problem > problems ;
     final ByteArrayOutputStream outputStream = new ByteArrayOutputStream() ;
     
     try {
       problems = produce( request, documentProducer, outputStream );
       outputStream.flush() ;
       outputStream.close() ;
     } catch( Exception e ) {
       throw new BuildException( e );
     }
 
     if( problems.iterator().hasNext() ) {
       throw new BuildException( problems.toString() ) ;
     }
 
     final String documentString;
     try {
       documentString = new String( outputStream.toByteArray(), renderingCharset.name() ) ;
     } catch ( UnsupportedEncodingException e ) {
       throw new BuildException( e ) ;
     }
     getProject().setProperty( contentProperty, documentString ) ;
   }
 
   private static Iterable< Problem > produce(
       final DocumentRequest request,
       final DocumentProducer documentProducer,
       final OutputStream outputStream
   ) throws Exception {
     return documentProducer.produce( request, outputStream ) ;
   }
 
 
 }
