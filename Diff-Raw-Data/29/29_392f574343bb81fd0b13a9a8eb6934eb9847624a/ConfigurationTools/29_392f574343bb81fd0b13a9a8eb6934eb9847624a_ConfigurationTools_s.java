 /*
  * Copyright (C) 2010 Laurent Caillette
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
 package org.novelang.configuration;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.util.Iterator;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ThreadFactory;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import org.apache.fop.apps.FOPException;
 import org.apache.fop.apps.FopFactory;
 import org.novelang.configuration.parse.BatchParameters;
 import org.novelang.configuration.parse.DaemonParameters;
 import org.novelang.configuration.parse.DocumentGeneratorParameters;
 import org.novelang.configuration.parse.GenericParameters;
 import org.novelang.configuration.parse.LevelExploderParameters;
 import org.novelang.logger.Logger;
 import org.novelang.logger.LoggerFactory;
 import org.novelang.outfit.DefaultCharset;
 import org.novelang.outfit.loader.ClasspathResourceLoader;
 import org.novelang.outfit.loader.ResourceLoader;
 import org.novelang.outfit.loader.ResourceLoaderTools;
 import org.novelang.outfit.loader.UrlResourceLoader;
 import org.novelang.produce.DocumentRequest;
 
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
 
   private static final Logger LOGGER = LoggerFactory.getLogger( ConfigurationTools.class ) ;
 
   public static final int DEFAULT_HTTP_DAEMON_PORT = 8080 ;
   public static final boolean DEFAULT_HTTP_DAEMON_SERVE_REMOTES = false ;
   public static final String DEFAULT_FONTS_DIRECTORY_NAME = "fonts" ;
   public static final String DEFAULT_HYPHENATION_DIRECTORY_NAME = "hyphenation" ;
   public static final String BUNDLED_STYLE_DIR = "style" ;
   private static final String DEFAULT_STYLE_DIR = "style" ;
   public static final String DEFAULT_OUTPUT_DIRECTORY_NAME = "output" ;
   public static final Charset DEFAULT_RENDERING_CHARSET = DefaultCharset.RENDERING ;
 
 
   private static final ThreadGroup EXECUTOR_THREAD_GROUP = new ThreadGroup( "Executor" ) ;
 
   private static final ThreadFactory EXECUTOR_THREAD_FACTORY = new ThreadFactory() {
     @Override
     public Thread newThread( final Runnable runnable ) {
       final Thread thread = new Thread( EXECUTOR_THREAD_GROUP, runnable ) ;
       thread.setDaemon( true ) ;
       return thread ;
     }
   } ;
 
   private ConfigurationTools() { }
 
   public static ThreadFactory getExecutorThreadFactory() {
     return EXECUTOR_THREAD_FACTORY ;
   }
 
   private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool( 
       Runtime.getRuntime().availableProcessors(), EXECUTOR_THREAD_FACTORY ) ;
 
   public static ProducerConfiguration createProducerConfiguration( 
       final GenericParameters parameters 
   )
       throws FOPException
   {
     final RenderingConfiguration renderingConfiguration =
         createRenderingConfiguration( parameters ) ;
     final ContentConfiguration contentConfiguration =
         createContentConfiguration( parameters ) ;
 
     return new ProducerConfiguration() {
       @Override
       public RenderingConfiguration getRenderingConfiguration() {
         return renderingConfiguration ;
       }
       @Override
       public ContentConfiguration getContentConfiguration() {
         return contentConfiguration ;
       }
 
       @Override
       public ExecutorService getExecutorService() {
         return EXECUTOR_SERVICE;
       }
     };
   }
 
   public static DaemonConfiguration createDaemonConfiguration( final DaemonParameters parameters )
       throws FOPException
   {
 
     final ProducerConfiguration producerConfiguration = createProducerConfiguration( parameters ) ;
 
     final int port ;
     final Integer customPort = parameters.getHttpDaemonPort() ;
     if( null == customPort ) {
       port = DEFAULT_HTTP_DAEMON_PORT ;
      LOGGER.info(
          "Got port number from default value [",
          DEFAULT_HTTP_DAEMON_PORT,
          "] (option not set: ",
          parameters.getHttpDaemonPortOptionDescription(),
          ")."
      ) ;
     } else {
       port = customPort ;
       LOGGER.info(
           "Got port number from custom value '",
           customPort,
           "' (from option: ",
           parameters.getHttpDaemonPortOptionDescription(),
           ")."
       ) ;
     }
 
     final boolean serveRemotes ;
     final Boolean serveRemotesAsBoolean = parameters.getServeRemotes() ;
     if( null == serveRemotesAsBoolean ) {
       serveRemotes = DEFAULT_HTTP_DAEMON_SERVE_REMOTES;
      LOGGER.info(
          "Got restriction to localhost from default value [",
          DEFAULT_HTTP_DAEMON_SERVE_REMOTES,
          "] (option not set: ",
          parameters.getHttpDaemonServeRemotesOptionDescription(),
          ")."
      ) ;
     } else {
       serveRemotes = serveRemotesAsBoolean ;
       LOGGER.info(
           "Got restriction to localhost from custom value '",
           customPort,
           "' (from option: ",
           parameters.getHttpDaemonServeRemotesOptionDescription(),
           ")."
       ) ;
     }
 
     return new DaemonConfiguration() {
       @Override
       public int getPort() {
         return port ;
       }
 
       @Override
       public boolean getServeRemotes() {
         return serveRemotes ;
       }
 
       @Override
       public ProducerConfiguration getProducerConfiguration() {
         return producerConfiguration ;
       }
     } ;
   }
 
   public static LevelExploderConfiguration createExplodeLevelsConfiguration(
       final LevelExploderParameters parameters
   ) throws FOPException {
     final ProducerConfiguration producerConfiguration = createProducerConfiguration( parameters ) ;
 
     final File outputDirectory ;
     outputDirectory = extractOutputDirectory( parameters ) ;
 
     return new LevelExploderConfiguration() {
       @Override
       public ProducerConfiguration getProducerConfiguration() {
         return producerConfiguration ;
       }
       @Override
       public DocumentRequest getDocumentRequest() {
         return parameters.getDocumentRequest() ;
       }
       @Override
       public File getOutputDirectory() {
         return outputDirectory ;
       }
     } ;
 
   }
 
   public static DocumentGeneratorConfiguration createDocumentGeneratorConfiguration(
       final DocumentGeneratorParameters parameters
   )
       throws FOPException, IllegalArgumentException
   {
     final ProducerConfiguration producerConfiguration = createProducerConfiguration( parameters ) ;
 
     final File outputDirectory ;
     outputDirectory = extractOutputDirectory( parameters ) ;
 
     return new DocumentGeneratorConfiguration() {
       @Override
       public ProducerConfiguration getProducerConfiguration() {
         return producerConfiguration ;
       }
       @Override
       public Iterable<DocumentRequest> getDocumentRequests() {
         return parameters.getDocumentRequests() ;
       }
       @Override
       public File getOutputDirectory() {
         return outputDirectory ;
       }
     } ;
 
   }
 
   private static File extractOutputDirectory( final BatchParameters parameters ) {
     final File outputDirectory;
     if( null == parameters.getOutputDirectory() ) {
       outputDirectory = new File( parameters.getBaseDirectory(), DEFAULT_OUTPUT_DIRECTORY_NAME ) ;
      LOGGER.info(
          "Got output directory from default value '",
          DEFAULT_OUTPUT_DIRECTORY_NAME,
          "' (option not set: ",
          parameters.getOutputDirectoryOptionDescription(),
          ")."
      ) ;
     } else {
       outputDirectory = parameters.getOutputDirectory() ;
      LOGGER.info(
          "Got output directory from custom value '",
          outputDirectory,
          "' (from option: ",
          parameters.getOutputDirectoryOptionDescription(),
          ")."
      ) ;
     }
     return outputDirectory;
   }
 
   public static ContentConfiguration createContentConfiguration(
       final GenericParameters parameters
   ) {
     final Charset defaultSourceCharset ;
     {
       final Charset charset = parameters.getDefaultSourceCharset() ;
       if( null == charset ) {
         defaultSourceCharset = DefaultCharset.SOURCE ;
         LOGGER.info(
            "Default source charset is ",
             defaultSourceCharset.name(),
            " (from option ",
             parameters.getDefaultSourceCharsetOptionDescription(),
             ")."
         ) ;
       } else {
         defaultSourceCharset = charset ;
        LOGGER.info( "Default source charset set as ", defaultSourceCharset.name() ) ;
       }
     }
     
     final File contentRoot ;
     {
       if( null == parameters.getContentRoot() ) {
         contentRoot = parameters.getBaseDirectory() ;
         LOGGER.info( "Content root set as ", contentRoot ) ;
       } else {
         contentRoot = parameters.getContentRoot() ;
         LOGGER.info(
             "Content root is '",
             contentRoot,
             "' (from option '",
             parameters.getContentRootOptionDescription(),
             "')"
         ) ;
       }
     }
 
     return new ContentConfiguration() {
       @Override
       public File getContentRoot() {
         return contentRoot ;
       }
       @Override
       public Charset getSourceCharset() {
         return defaultSourceCharset ;
       }
     } ;
   }
 
   public static RenderingConfiguration createRenderingConfiguration( 
       final GenericParameters parameters 
   )
       throws FOPException
   {
     final Iterable< File > fontDirectories = findMultipleDirectoriesWithDefault(
         parameters.getBaseDirectory(),
         parameters.getFontDirectories(),
         "font directories",
         DEFAULT_FONTS_DIRECTORY_NAME,
         parameters.getFontDirectoriesOptionDescription() 
     ) ;
 
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
         LOGGER.info(
            "Default rendering charset is ",
             defaultRenderingCharset.name(),
            " (from option ",
             parameters.getDefaultRenderingCharsetOptionDescription(),
             ")."
         ) ;
       } else {
         defaultRenderingCharset = charset ;
        LOGGER.info( "Default rendering charset set as ", defaultRenderingCharset.name() ) ;
       }
     }
 
     return new RenderingConfiguration() {
       @Override
       public ResourceLoader getResourceLoader() {
         return resourceLoader ;
       }
       @Override
       public FopFactory getFopFactory() {
         return fopFactory ;
       }
 
       @Override
       public FopFontStatus getCurrentFopFontStatus() {
         try {
           return FopTools.createGlobalFontStatus( fopFactory, fontDirectories ) ;
         } catch( FOPException e ) {
           throw new RuntimeException( e ) ;
         }
       }
 
       @Override
       public Charset getDefaultCharset() {
         return defaultRenderingCharset ;
       }
     } ;
 
   }
 
   private static Iterable< File > findMultipleDirectoriesWithDefault(
       final File baseDirectory, 
       final Iterable< File > userDirectories,
       final String directoriesName, 
       final String defaultSingleDirectoryName, 
       final String directoriesOptionDescription 
   ) {
     final Iterable<File> directories;
     if( userDirectories.iterator().hasNext() ) {
       directories = userDirectories ;
       LOGGER.info(
           "Got " + directoriesName + " from custom value '",
           directories,
           "' (from option: ",
           directoriesOptionDescription,
           ")."
       ) ;
     } else {
       final File maybeDefaultDirectory = findDefaultDirectoryIfNeeded(
           baseDirectory,
           null,
           directoriesName,
           directoriesOptionDescription,
           defaultSingleDirectoryName
       ) ;
 
       if( null == maybeDefaultDirectory ) {
         directories = ImmutableList.of() ;
       } else {
         directories = Lists.newArrayList( maybeDefaultDirectory ) ;
       }
     }
     return directories;
   }
 
   public static ResourceLoader createResourceLoader( final GenericParameters parameters ) {
     final Iterable< File > userDefinedDirectories = findMultipleDirectoriesWithDefault(
         parameters.getBaseDirectory(),
         parameters.getStyleDirectories(),
         "style directories",
         DEFAULT_STYLE_DIR,
         parameters.getStyleDirectoriesDescription() 
     ) ;
     return createResourceLoader( userDefinedDirectories ) ;
   }
 
   public static ResourceLoader createResourceLoader( 
       final Iterable< File > userDefinedDirectories
   ) {
     final ResourceLoader classpathResourceLoader = 
         new ClasspathResourceLoader( BUNDLED_STYLE_DIR ) ;
     final Iterator< File > userDefinedDirectoryIterator = userDefinedDirectories.iterator() ;
     
     if( userDefinedDirectoryIterator.hasNext() ) {
       ResourceLoader resultingResourceLoader = classpathResourceLoader ;
       
       while ( userDefinedDirectoryIterator.hasNext() ) {
         final File userStyleDirectory = userDefinedDirectoryIterator.next() ;
         final URL userStyleUrl ;
         try {
           userStyleUrl = userStyleDirectory.toURI().toURL();
         } catch( MalformedURLException e ) {
           throw new RuntimeException( e );
         }
         resultingResourceLoader = ResourceLoaderTools.compose( 
             new UrlResourceLoader( userStyleUrl ), 
             resultingResourceLoader 
         ) ;
       }
 
       return resultingResourceLoader ;
       
     } else {
       return classpathResourceLoader ;
     }
     
     
   }
 
   protected static File findDefaultDirectoryIfNeeded(
       final File baseDirectory,
       final File userDefinedDirectory,
       final String topic,
       final String directoryDescription,
       final String defaultDirectoryName
   ) {
     if( null == userDefinedDirectory ) {
       final File defaultDirectory = new File( baseDirectory, defaultDirectoryName ) ;
       if( defaultDirectory.exists() ) {
         LOGGER.info(
             "Got ",
             topic,
             " from default value '",
             defaultDirectory.getAbsolutePath(),
             "' (option not set: ",
             directoryDescription,
             ")."
         ) ;
         return defaultDirectory ;
       } else {
         LOGGER.info(
             "Got no ",
             topic,
             " (no default directory '",
             defaultDirectoryName,
             "' nor was set this option: ",
             directoryDescription,
             ")."
         ) ;
 
         return null ;
       }
     } else {
       LOGGER.info(
           "Got ",
           topic,
           " from user value '",
           userDefinedDirectory,
           "' (from option: ",
           directoryDescription,
           ")."
       ) ;
       return userDefinedDirectory ;
     }
   }
 
 
 }
