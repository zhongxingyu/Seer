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
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.Set;
 import java.util.Iterator;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.apache.fop.apps.FOPException;
 import org.apache.commons.lang.SystemUtils;
 import novelang.configuration.parse.DaemonParameters;
 import novelang.configuration.parse.ArgumentsNotParsedException;
 import novelang.configuration.parse.GenericParameters;
 import novelang.configuration.parse.BatchParameters;
 import static novelang.configuration.parse.DaemonParameters.OPTIONNAME_HTTPDAEMON_PORT;
 import static novelang.configuration.parse.GenericParameters.OPTIONPREFIX;
 import novelang.ScratchDirectoryFixture;
 import novelang.TestResources;
 import novelang.TestResourceTools;
 import novelang.produce.DocumentRequest;
 import static novelang.TestResourceTools.copyResourceToDirectory;
 import com.google.common.collect.Sets;
 import com.google.common.collect.Iterables;
 
 /**
  * Tests for {@link ConfigurationTools}.
  *
  * TODO add tests for:
  *   {@link GenericParameters#getLogDirectory()}
  *   {@link GenericParameters#getStyleDirectory()}
  *   {@link GenericParameters#getHyphenationDirectory()} 
  *
  * @author Laurent Caillette
  */
 public class ConfigurationToolsTest {
 
 // ===================
 // DaemonConfiguration
 // ===================
 
   @Test
   public void createDaemonConfigurationWithCustomPort()
       throws ArgumentsNotParsedException, FOPException
   {
     final DaemonConfiguration configuration = ConfigurationTools
         .createDaemonConfiguration( createDaemonParameters(
             OPTIONPREFIX + OPTIONNAME_HTTPDAEMON_PORT,
             "8888"
         )
     ) ;
     Assert.assertEquals( 8888, configuration.getPort() ) ;
   }
 
   @Test
   public void createDaemonConfigurationFromDefaults()
       throws ArgumentsNotParsedException, FOPException
   {
     final DaemonConfiguration configuration =
         ConfigurationTools.createDaemonConfiguration( createDaemonParameters() ) ;
     Assert.assertEquals( ConfigurationTools.DEFAULT_HTTP_DAEMON_PORT, configuration.getPort() ) ;
   }
 
 
 // ==================
 // BatchConfiguration
 // ==================
 
   @Test( expected = ArgumentsNotParsedException.class )
   public void createBatchConfigurationWithNoDocumentRequest()
       throws ArgumentsNotParsedException, FOPException
   {
     ConfigurationTools.createBatchConfiguration( createBatchParameters() ) ;
 
   }
 
   public void createBatchConfiguration() throws ArgumentsNotParsedException, FOPException {
     final BatchConfiguration configuration = ConfigurationTools.createBatchConfiguration(
         createBatchParameters( "1.html", "2.html" ) ) ;
 
     Assert.assertEquals( new File( SystemUtils.USER_DIR ), configuration.getOutputDirectory() ) ;
 
     final Iterable< DocumentRequest > documentRequests = configuration.getDocumentRequests() ;
     final Iterator<DocumentRequest> iterator = documentRequests.iterator() ;
     Assert.assertTrue( iterator.hasNext() ) ;
     Assert.assertEquals( "1.html", iterator.next().getDocumentSourceName() ) ;
     Assert.assertEquals( "2.html", iterator.next().getDocumentSourceName() ) ;
     Assert.assertFalse( iterator.hasNext() ) ;
 
     Assert.assertNotNull( configuration.getProducerConfiguration() ) ;
 
   }
 
 // ======================
 // RenderingConfiguration
 // ======================
 
   @Test
   public void createRenderingConfigurationFromDefaultsWithNoDefaultFontsDirectory()
       throws ArgumentsNotParsedException, FOPException, MalformedURLException {
     // 'fonts' directory has no 'fonts' subdirectory!
     final RenderingConfiguration renderingConfiguration = ConfigurationTools
         .createRenderingConfiguration( createDaemonParameters( defaultFontsDirectory ) ) ;
 
     Assert.assertNotNull( renderingConfiguration.getResourceLoader() ) ;
     Assert.assertNotNull( renderingConfiguration.getFopFactory() ) ;
     checkAllFontsAreGood( renderingConfiguration.getCurrentFopFontStatus() ) ;
   }
 
   @Test
   public void createRenderingConfigurationFromDefaultsWithDefaultFontsDirectory()
       throws ArgumentsNotParsedException, FOPException, MalformedURLException
   {
     // Sure that parent of 'fonts' subdirectory has a 'fonts' subdirectory!
     final RenderingConfiguration renderingConfiguration = ConfigurationTools
         .createRenderingConfiguration(
             createDaemonParameters( defaultFontsDirectory.getParentFile() ) ) ;
 
     Assert.assertNotNull( renderingConfiguration.getResourceLoader() ) ;
     Assert.assertNotNull( renderingConfiguration.getFopFactory() ) ;
     checkAllFontsAreGood(
         renderingConfiguration.getCurrentFopFontStatus(),
         FONT_FILE_DEFAULT_1,
         FONT_FILE_DEFAULT_2
     ) ;
 
   }
 
   @Test
   public void createRenderingConfigurationFromCustomFontsDirectory()
       throws ArgumentsNotParsedException, FOPException, MalformedURLException
   {
     final DaemonParameters parameters = createDaemonParameters(
         fontStructureDirectory,
         GenericParameters.OPTIONPREFIX + GenericParameters.OPTIONNAME_FONT_DIRECTORIES,
         ALTERNATE_FONTS_DIR_NAME
     ) ;
     final RenderingConfiguration renderingConfiguration = ConfigurationTools
         .createRenderingConfiguration( parameters ) ;
 
     Assert.assertNotNull( renderingConfiguration.getResourceLoader() ) ;
     Assert.assertNotNull( renderingConfiguration.getFopFactory() ) ;
     checkAllFontsAreGood(
         renderingConfiguration.getCurrentFopFontStatus(),
         FONT_FILE_ALTERNATE
     ) ;
 
   }
 
 
 // ====================
 // ContentConfiguration
 // ====================
 
   @Test
   public void createContentConfiguration() throws ArgumentsNotParsedException {
     final ContentConfiguration contentConfiguration =
         ConfigurationTools.createContentConfiguration( createDaemonParameters() ) ;
    Assert.assertEquals( new File( SystemUtils.USER_DIR ), contentConfiguration.getContentRoot() ) ; 
   }
 
 
 // =====================
 // ProducerConfiguration
 // =====================
 
   @Test
   public void createProducerConfiguration() throws ArgumentsNotParsedException, FOPException {
     final ProducerConfiguration producerConfiguration =
         ConfigurationTools.createProducerConfiguration( createDaemonParameters() ) ;
     Assert.assertNotNull( producerConfiguration ) ;
     Assert.assertNotNull( producerConfiguration.getContentConfiguration() ) ;
     Assert.assertNotNull( producerConfiguration.getRenderingConfiguration() ) ;
   }
 
 
 // =======
 // Fixture
 // =======
 
   private void checkAllFontsAreGood( FopFontStatus fontStatus, String... relativeFontNames )
       throws MalformedURLException
   {
     final Iterable< String > embedFilesIterable = Iterables.transform(
         fontStatus.getFontInfos(),
         FopTools.EXTRACT_EMBEDFONTINFO_FUNCTION
     ) ;
     final Set< String > embedFilesSet = Sets.newHashSet( embedFilesIterable ) ;
     Assert.assertEquals( relativeFontNames.length, embedFilesSet.size() ) ;
     for( String relativeFontName : relativeFontNames ) {
       Assert.assertTrue( embedFilesSet.contains( createFontFileUrl( relativeFontName ) ) ) ;
     }
 
   }
 
   private String createFontFileUrl( String fontFileName ) throws MalformedURLException {
     return scratchDirectory.getAbsoluteFile().toURI().toURL().toExternalForm()
         + fontFileName.substring( 1 );
   }
 
   private static final String FONT_STRUCTURE_DIR = TestResources.FONT_STRUCTURE_DIR ;
   private static final String DEFAULT_FONTS_DIR = TestResources.DEFAULT_FONTS_DIR ;
   private static final String FONT_FILE_DEFAULT_1 = TestResources.FONT_FILE_DEFAULT_1 ;
   private static final String FONT_FILE_DEFAULT_2 = TestResources.FONT_FILE_DEFAULT_2 ;
   private static final String ALTERNATE_FONTS_DIR_NAME = TestResources.ALTERNATE_FONTS_DIR_NAME ;
   private static final String FONT_FILE_ALTERNATE = TestResources.FONT_FILE_ALTERNATE ;
   private static final String FONT_FILE_PARENT_CHILD = TestResources.FONT_FILE_PARENT_CHILD ;
   private static final String FONT_FILE_PARENT_CHILD_BAD =
       TestResources.FONT_FILE_PARENT_CHILD_BAD ;
 
   private final File scratchDirectory ;
   private final File fontStructureDirectory ;
   private final File defaultFontsDirectory ;
 
   public ConfigurationToolsTest() throws IOException {
     scratchDirectory = new ScratchDirectoryFixture( ConfigurationToolsTest.class )
         .getTestScratchDirectory() ;
 
     copyResourceToDirectory( getClass(), FONT_FILE_DEFAULT_1, scratchDirectory ) ;
     copyResourceToDirectory( getClass(), FONT_FILE_DEFAULT_2, scratchDirectory ) ;
     copyResourceToDirectory( getClass(), FONT_FILE_ALTERNATE, scratchDirectory ) ;
     copyResourceToDirectory( getClass(), FONT_FILE_PARENT_CHILD, scratchDirectory ) ;
     copyResourceToDirectory( getClass(), FONT_FILE_PARENT_CHILD_BAD, scratchDirectory ) ;
 
     defaultFontsDirectory = TestResourceTools.getDirectoryForSure(
         scratchDirectory, DEFAULT_FONTS_DIR ) ;
 
     fontStructureDirectory = TestResourceTools.getDirectoryForSure(
         scratchDirectory, FONT_STRUCTURE_DIR ) ;
 
   }
 
   private final DaemonParameters createDaemonParameters( String... arguments )
       throws ArgumentsNotParsedException
   {
     return createDaemonParameters( scratchDirectory, arguments ) ;
   }
 
   private final DaemonParameters createDaemonParameters( File baseDirectory, String... arguments )
       throws ArgumentsNotParsedException
   {
     return new DaemonParameters( baseDirectory, arguments ) ;
   }
 
   private final BatchParameters createBatchParameters( String... arguments )
       throws ArgumentsNotParsedException
   {
     return createBatchParameters( scratchDirectory, arguments ) ;
   }
 
   private final BatchParameters createBatchParameters( File baseDirectory, String... arguments )
       throws ArgumentsNotParsedException
   {
     return new BatchParameters( baseDirectory, arguments ) ;
   }
 
 
 
 }
