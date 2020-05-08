 package net.sf.yal10n.dashboard;
 
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
 import java.io.StringWriter;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.UUID;
 import java.util.regex.Pattern;
 
 import net.sf.yal10n.DashboardMojo;
 import net.sf.yal10n.analyzer.ResourceFile;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Unit test for {@link DashboardRenderer}.
  */
 public class DashboardRendererTest
 {
 
     /**
      * Checks the rendering content.
      */
     @Test
     public void testDashboardRenderer()
     {
         StringWriter out = new StringWriter();
         DashboardRenderer renderer = new DashboardRenderer( "." );
 
         DashboardModel model = createModel();
 
         renderer.render( model, out );
         String dashboard = out.toString();

         Assert.assertTrue( dashboard.contains( "<td>Bundle 1</td>" ) );
         Assert.assertTrue( dashboard.contains( "<a href=\"bundle1/base\">default</a>" ) );
         Assert.assertTrue( dashboard.contains( "<a href=\"bundle1/de\">de</a>" ) );
         Assert.assertTrue( dashboard.contains( "<a href=\"bundle1/fr\">fr</a>" ) );
         Assert.assertTrue( Pattern.compile( "<td>\\s*\n\\s*missing", Pattern.MULTILINE ).matcher( dashboard ).find() );
         Assert.assertTrue( Pattern.compile( "<td class=\"severity-major\">\\s*\n\\s*missing", Pattern.MULTILINE )
                 .matcher( dashboard ).find() );
         Assert.assertTrue( dashboard.contains( "<a href=\"reports/bundle1\">report</a>" ) );
         Assert.assertTrue( dashboard.contains( "<td>Bundle 2</td>" ) );
         Assert.assertTrue( dashboard.contains( "<a href=\"bundle2/base\">default</a>" ) );
         Assert.assertTrue( dashboard.contains( DashboardMojo.getVersion() ) );
     }
 
     /**
      * Checks that a file is actually created by the rendering.
      * @throws Exception any error
      */
     @Test
     public void testDashboardCreation() throws Exception
     {
         File expectedOutputDirectory = new File( "./target/test-output/dashboardrenderertest/test-run-"
                 + UUID.randomUUID().toString() );
         File expectedFile = new File( expectedOutputDirectory, "dashboard.html" );
         if ( !expectedOutputDirectory.exists() )
         {
             Assert.assertTrue( "Couldn't create output directory: " + expectedOutputDirectory,
                 expectedOutputDirectory.mkdirs() );
         }
         Assert.assertTrue( "Output directory is not a directory: " + expectedOutputDirectory,
                 expectedOutputDirectory.isDirectory() );
 
         DashboardModel model = new DashboardModel();
         DashboardRenderer renderer = new DashboardRenderer( expectedOutputDirectory.getCanonicalPath() );
         renderer.render( model );
 
         Assert.assertTrue( expectedFile.exists() );
     }
 
     private DashboardModel createModel()
     {
        BundleModel bundle1 = createBundleModel( "Bundle 1", "bundle1", true );
        BundleModel bundle2 = createBundleModel( "Bundle 2", "bundle2", false );
 
         DashboardModel model = new DashboardModel();
         model.setAllLanguages( Arrays.asList( "de", "fr", "es", "de_DE" ) );
         model.setAllBundles( Arrays.asList( bundle1, bundle2 ) );
         model.setGenerationDate( new Date().toString() );
         model.setVersion( DashboardMojo.getVersion() );
 
         return model;
     }
 
    private BundleModel createBundleModel( String projectName, String reportUrl, boolean defaultHasMessages )
     {
        int countOfMessages = defaultHasMessages ? 1 : 0;
         BundleModel bundle1 = new BundleModel();
         bundle1.setProjectName( projectName );
         bundle1.setRelativeReportUrl( reportUrl );
        bundle1.setBase( createLanguageModel( "default", reportUrl + "/base", true, countOfMessages ) );
        bundle1.addLanguage( createLanguageModel( "de", reportUrl + "/de", true, countOfMessages ) );
        bundle1.addLanguage( createLanguageModel( "fr", reportUrl + "/fr", true, countOfMessages ) );
        bundle1.addLanguage( createLanguageModel( "es", null, false, countOfMessages ) );
        bundle1.addLanguage( createLanguageModel( "de_DE", null, false, countOfMessages ) );
         return bundle1;
     }
 
    private LanguageModel createLanguageModel( String name, String svnUrl, boolean existing, int count )
     {
         LanguageModel base = new LanguageModel();
         base.setExisting( existing );
         base.setVariant( ResourceFile.isVariant( name ) );
         base.setName( name );
         base.setSvnUrl( svnUrl );
        base.setCountOfMessages( count );
         return base;
     }
 }
