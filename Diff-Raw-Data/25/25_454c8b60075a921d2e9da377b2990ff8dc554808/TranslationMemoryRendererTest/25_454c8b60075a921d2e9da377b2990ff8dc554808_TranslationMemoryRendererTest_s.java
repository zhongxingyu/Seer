 package net.sf.yal10n.tmx;
 
 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.Arrays;
 import java.util.Collection;
 
 import net.sf.yal10n.analyzer.NullLog;
 import net.sf.yal10n.analyzer.ResourceAnalyzer;
 import net.sf.yal10n.analyzer.ResourceBundle;
 import net.sf.yal10n.settings.DashboardConfiguration;
 import net.sf.yal10n.settings.Repository;
 import net.sf.yal10n.svn.SVNUtil;
 import net.sf.yal10n.svn.SVNUtilMock;
 
 import org.codehaus.plexus.util.IOUtil;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Unit test for {@link TranslationMemoryRenderer}.
  */
 public class TranslationMemoryRendererTest
 {
     /**
      * Test to convert a single bundle.
      * @throws Exception any error
      */
     @Test
     public void testSingleBundle() throws Exception
     {
         File outputDirectory = new File( "./target/test-output/translationmemory/" );
         File completeDirectory = new File( outputDirectory, "/reports" );
         if ( !completeDirectory.exists() )
         {
             Assert.assertTrue( "Could create output directory for test: " + completeDirectory,
                     completeDirectory.mkdirs() );
         }
         Assert.assertTrue( "Not a directory: " + completeDirectory, completeDirectory.isDirectory() );
 
         TranslationMemoryRenderer renderer = new TranslationMemoryRenderer( outputDirectory.getCanonicalPath() );
         ResourceBundle bundle = createBundle().iterator().next();
        renderer.render( bundle  );
 
         String relativeTmxUrl = bundle.toBundleModel( bundle.getLanguages() ).getRelativeTmxUrl();
         Assert.assertTrue( new File( completeDirectory, relativeTmxUrl ).exists() );
         String fileData = IOUtil.toString( new FileInputStream( new File( completeDirectory, relativeTmxUrl ) ),
                 "UTF-8" );
        Assert.assertTrue( fileData.contains( "tuid=\"http://svn/repo/messages:not.translated.missing\"" ) );
        Assert.assertTrue( fileData.contains( "<prop type=\"x-key\">not.translated.missing</prop>" ) );
        Assert.assertTrue( fileData.contains( "xml:lang=\"de\"" ) );
        Assert.assertTrue( fileData.contains( "this is a sample for for unit testing in locale German" ) );
     }
 
     /**
     * Test to convert a colletion of bundles.
      * @throws Exception any error
      */
     @Test
     public void testCollection() throws Exception
     {
         File outputDirectory = new File( "./target/test-output/translationmemory/" );
         File completeDirectory = new File( outputDirectory, "/reports" );
         if ( !completeDirectory.exists() )
         {
             Assert.assertTrue( "Could create output directory for test: " + completeDirectory,
                     completeDirectory.mkdirs() );
         }
         Assert.assertTrue( "Not a directory: " + completeDirectory, completeDirectory.isDirectory() );
 
         TranslationMemoryRenderer renderer = new TranslationMemoryRenderer( outputDirectory.getCanonicalPath() );
         Collection<ResourceBundle> bundle = createBundle();
         renderer.render( bundle  );
 
         String relativeTmxUrl = "all-translations.tmx";
         Assert.assertTrue( new File( outputDirectory, relativeTmxUrl ).exists() );
         String fileData = IOUtil.toString( new FileInputStream( new File( outputDirectory, relativeTmxUrl ) ),
                 "UTF-8" );
        Assert.assertTrue( fileData.contains( "http://svn/repo/messages:not.translated.same" ) );
     }
 
     private Collection<ResourceBundle> createBundle() throws Exception
     {
         SVNUtil svnUtil = new SVNUtilMock( null );
         ResourceAnalyzer analyzer = new ResourceAnalyzer( svnUtil, new NullLog() );
         DashboardConfiguration config = new DashboardConfiguration();
         Repository repo = new Repository();
         repo.setUrl( "http://svn/repo" );
         repo.setIncludes( Arrays.asList( "**/messages*.properties" ) );
         String repoId = SVNUtil.toRepoId( "", repo.getUrl() );
         config.getRepositories().add( repo );
 
         analyzer.analyze( repo.getUrl(), new File( "./target/test-classes/unit/subdirectory/" ).getCanonicalPath(),
                 config, repo, repoId );
 
         return analyzer.getBundles().values();
     }
 }
