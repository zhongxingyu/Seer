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
 package novelang.configuration;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.charset.Charset;
 
 import org.apache.fop.apps.FOPException;
 import org.apache.fop.apps.FopFactory;
 import novelang.system.LogFactory;
 import com.google.common.collect.Lists;
 import com.google.common.collect.ImmutableList;
 import novelang.configuration.parse.BatchParameters;
 import novelang.configuration.parse.DaemonParameters;
 import novelang.configuration.parse.GenericParameters;
 import novelang.loader.ClasspathResourceLoader;
 import novelang.loader.ResourceLoader;
 import novelang.loader.ResourceLoaderTools;
 import novelang.loader.UrlResourceLoader;
 import novelang.produce.DocumentRequest;
 import novelang.system.DefaultCharset;
 import novelang.system.Log;
 
 /**
  * Creates various Configuration objects from {@link GenericParameters}.
  * The main contract of this class is that for each created Configuration object, all information
  * needed is held by the Parameters object. No access to system property, no static variable, no
  * hidden state.
  * <p>
  * Each configured value comes from (in order):
  * <ol>
  *   <li>User-defined value (through some parameter).</li>
  *   <li>Fallback value (applies to a directory with known name).</li>
  *   <li>Default value (null or default number).</li>
  * </ol>
  * Each time parameters provide a value, log this origin at INFO level.
  *
  * @author Laurent Caillette
  */
 public class ConfigurationTools {
 
   private static final Log LOG = LogFactory.getLog( ConfigurationTools.class ) ;
 
   public static final int DEFAULT_HTTP_DAEMON_PORT = 8080 ;
   public static final String DEFAULT_FONTS_DIRECTORY_NAME = "fonts" ;
   public static final String DEFAULT_HYPHENATION_DIRECTORY_NAME = "hyphenation" ;
   public static final String BUNDLED_STYLE_DIR = "style" ;
   private static final String DEFAULT_STYLE_DIR = "style" ;
   public static final String DEFAULT_OUTPUT_DIRECTORY_NAME = "output" ;
   public static final Charset DEFAULT_RENDERING_CHARSET = DefaultCharset.RENDERING ;
 
   public static ProducerConfiguration createProducerConfiguration( GenericParameters parameters )
       throws FOPException
   {
     final RenderingConfiguration renderingConfiguration =
         createRenderingConfiguration( parameters ) ;
     final ContentConfiguration contentConfiguration =
         createContentConfiguration( parameters ) ;
 
     return new ProducerConfiguration() {
       public RenderingConfiguration getRenderingConfiguration() {
         return renderingConfiguration ;
       }
       public ContentConfiguration getContentConfiguration() {
         return contentConfiguration ;
       }
     };
   }
 
   public static DaemonConfiguration createDaemonConfiguration( DaemonParameters parameters )
       throws FOPException
   {
 
     final ProducerConfiguration producerConfiguration = createProducerConfiguration( parameters ) ;
 
     final int port ;
     final Integer customPort = parameters.getHttpDaemonPort() ;
     if( null == customPort ) {
       port = DEFAULT_HTTP_DAEMON_PORT ;
      LOG.info(
          "Got port number from default value [%s] (option not set: %s).",
          DEFAULT_HTTP_DAEMON_PORT,
          parameters.getHttpDaemonPortOptionDescription()
      ) ;
     } else {
       port = customPort ;
       LOG.info(
           "Got port number from custom value '%s' (from option: %s"  + ").",
           customPort,
           parameters.getHttpDaemonPortOptionDescription()
       ) ;
     }
 
     return new DaemonConfiguration() {
       public int getPort() {
         return port ;
       }
 
       public boolean getServeLocalhostOnly() {
        return true ;
       }
 
       public ProducerConfiguration getProducerConfiguration() {
         return producerConfiguration ;
       }
     } ;
   }
 
   public static BatchConfiguration createBatchConfiguration( final BatchParameters parameters )
       throws FOPException, IllegalArgumentException
   {
     final ProducerConfiguration producerConfiguration = createProducerConfiguration( parameters ) ;
 
     final File outputDirectory ;
     if( null == parameters.getOutputDirectory() ) {
       outputDirectory = new File( parameters.getBaseDirectory(), DEFAULT_OUTPUT_DIRECTORY_NAME ) ;
      LOG.info(
          "Got output directory from default value '%s' (option not set: %s).",
          DEFAULT_OUTPUT_DIRECTORY_NAME,
          parameters.getOutputDirectoryOptionDescription()
      ) ;
     } else {
       outputDirectory = parameters.getOutputDirectory() ;
      LOG.info(
          "Got output directory from custom value '%s' (from option: %s).",
          outputDirectory,
          parameters.getOutputDirectoryOptionDescription()
      ) ;
     }
 
     return new BatchConfiguration() {
       public ProducerConfiguration getProducerConfiguration() {
         return producerConfiguration ;
       }
       public Iterable< DocumentRequest > getDocumentRequests() {
         return parameters.getDocumentRequests() ;
       }
       public File getOutputDirectory() {
         return outputDirectory ;
       }
     } ;
 
   }
 
   public static ContentConfiguration createContentConfiguration(
       final GenericParameters parameters
   ) {
     final Charset defaultSourceCharset ;
     {
       final Charset charset = parameters.getDefaultSourceCharset() ;
       if( null == charset ) {
         defaultSourceCharset = DefaultCharset.SOURCE ;
         LOG.info(
             "Default source charset is %s (from option %s).",
             defaultSourceCharset.name(),
             parameters.getDefaultSourceCharsetOptionDescription()
         ) ;
       } else {
         defaultSourceCharset = charset ;
         LOG.info( "Default source charset set as %s",
             defaultSourceCharset.name() ) ;
       }
     }
 
     return new ContentConfiguration() {
       public File getContentRoot() {
         return parameters.getBaseDirectory() ;
       }
       public Charset getSourceCharset() {
         return defaultSourceCharset ;
       }
     } ;
   }
 
   public static RenderingConfiguration createRenderingConfiguration( GenericParameters parameters )
       throws FOPException
   {
     final Iterable< File > fontDirectories ;
     final Iterable< File > userFontDirectories = parameters.getFontDirectories() ;
     if( userFontDirectories.iterator().hasNext() ) {
       fontDirectories = userFontDirectories ;
      LOG.info(
          "Got font directories from custom value '%s" + "' (from option: %s).",
          fontDirectories,
          parameters.getFontDirectoriesOptionDescription()
      ) ;
     } else {
       final File maybeDefaultDirectory = findDefaultDirectoryIfNeeded(
           parameters.getBaseDirectory(),
           null,
           "font directories",
           parameters.getFontDirectoriesOptionDescription(),
           DEFAULT_FONTS_DIRECTORY_NAME
       ) ;
 
       if( null == maybeDefaultDirectory ) {
         fontDirectories = ImmutableList.of() ;
       } else {
         fontDirectories = Lists.newArrayList( maybeDefaultDirectory ) ;
       }
     }
 
     final File hyphenationDirectory = findDefaultDirectoryIfNeeded(
         parameters.getBaseDirectory(),
         parameters.getHyphenationDirectory(),
         "hyphenation directory",
         parameters.getHyphenationDirectoryOptionDescription(),
         DEFAULT_HYPHENATION_DIRECTORY_NAME
     ) ;
 
     final FopFactory fopFactory = FopTools
         .createFopFactory( fontDirectories, hyphenationDirectory ) ;
 
     final ResourceLoader resourceLoader = createResourceLoader( parameters ) ;
 
     final Charset defaultRenderingCharset ;
     {
       final Charset charset = parameters.getDefaultRenderingCharset() ;
       if( null == charset ) {
         defaultRenderingCharset = DefaultCharset.RENDERING ;
         LOG.info(
             "Default rendering charset is %s (from option %s).",
             defaultRenderingCharset.name(),
             parameters.getDefaultRenderingCharsetOptionDescription()
         ) ;
       } else {
         defaultRenderingCharset = charset ;
         LOG.info( "Default rendering charset set as %s",
             defaultRenderingCharset.name() ) ;
       }
     }
 
     return new RenderingConfiguration() {
       public ResourceLoader getResourceLoader() {
         return resourceLoader ;
       }
       public FopFactory getFopFactory() {
         return fopFactory ;
       }
 
       public FopFontStatus getCurrentFopFontStatus() {
         try {
           return FopTools.createGlobalFontStatus( fopFactory, fontDirectories ) ;
         } catch( FOPException e ) {
           throw new RuntimeException( e ) ;
         }
       }
 
       public Charset getDefaultCharset() {
         return defaultRenderingCharset ;
       }
     } ;
 
   }
 
   private static ResourceLoader createResourceLoader( GenericParameters parameters ) {
 
     final File userStyleDirectory = findDefaultDirectoryIfNeeded(
         parameters.getBaseDirectory(),
         parameters.getStyleDirectory(),
         "user styles",
         parameters.getStyleDirectoryDescription(),
         DEFAULT_STYLE_DIR
     ) ;
 
     final URL userStyleUrl ;
     if( null == userStyleDirectory ) {
       userStyleUrl = null ;
     } else {
       try {
         userStyleUrl = userStyleDirectory.toURI().toURL();
       } catch( MalformedURLException e ) {
         throw new RuntimeException( e );
       }
     }
 
     final ResourceLoader classpathResourceLoader =
         new ClasspathResourceLoader( BUNDLED_STYLE_DIR ) ;
 
     final ResourceLoader resourceLoader ;
     if( null == userStyleDirectory ) {
       resourceLoader = classpathResourceLoader ;
     } else {
       resourceLoader = ResourceLoaderTools.compose(
           new UrlResourceLoader( userStyleUrl ), classpathResourceLoader ) ;
     }
 
     return resourceLoader ;
 
   }
 
   protected static File findDefaultDirectoryIfNeeded(
       File baseDirectory,
       File userDefinedDirectory,
       String topic,
       String directoryDescription,
       String defaultDirectoryName
   ) {
     if( null == userDefinedDirectory ) {
       final File defaultDirectory = new File( baseDirectory, defaultDirectoryName ) ;
       if( defaultDirectory.exists() ) {
         LOG.info(
             "Got %s from default value '%s' (option not set: %s).",
             topic,
             defaultDirectory.getAbsolutePath(),
             directoryDescription
         ) ;
         return defaultDirectory ;
       } else {
         LOG.info(
             "Got no %s (no default directory '%s' nor was set this option: %s).",
             topic,
             defaultDirectoryName,
             directoryDescription
         ) ;
 
         return null ;
       }
     } else {
       LOG.info(
           "Got %s from user value '%s' (from option: %s).",
           topic,
           userDefinedDirectory,
           directoryDescription
       ) ;
       return userDefinedDirectory ;
     }
   }
 
 
 }
