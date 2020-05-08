 package net.sf.yal10n.svn;
 
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
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 
 import net.sf.yal10n.analyzer.NullLog;
 import net.sf.yal10n.diff.UnifiedDiff;
 import net.sf.yal10n.settings.ScmType;
 
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.plugin.logging.SystemStreamLog;
 import org.codehaus.plexus.util.FileUtils;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Unit test for {@link SVNUtil}.
  */
 public class SVNUtilTest
 {
 
     /**
      * Check the repo id.
      */
     @Test
     public void testRepoId()
     {
         String id = SVNUtil.toRepoId( "prefix", "url" );
         Assert.assertEquals( "c1a3dcfee6262f557e84cabffdbe32f7", id );
     }
 
     /**
      * Check the complete url generation.
      */
     @Test
     public void testCompleteUrl()
     {
         Assert.assertEquals( "http://svn/repo/trunk", SVNUtil.toCompleteUrl( "http://svn", "repo/trunk" ) );
         Assert.assertEquals( "http://svn/repo/trunk", SVNUtil.toCompleteUrl( "http://svn/", "repo/trunk" ) );
         Assert.assertEquals( "http://svn/repo/trunk", SVNUtil.toCompleteUrl( "http://svn", "/repo/trunk" ) );
         Assert.assertEquals( "http://svn/repo/trunk", SVNUtil.toCompleteUrl( "http://svn/", "/repo/trunk" ) );
         Assert.assertEquals( "http://svn/repo/trunk", SVNUtil.toCompleteUrl( "http://svn", "/repo/trunk/" ) );
     }
 
     /**
      * Checks whether the change is correctly detected as modification
      * instead of adding.
      * @throws Exception any error
      */
     @Test
     public void testIssue24DetectChanges() throws Exception
     {
         SVNUtil svnUtil = new SVNUtil();
         Log log = new NullLog();
 
         String svnUrl = "file://" + new File( "./src/test/resources/svnrepos/issue24-detectchanges" ).getAbsolutePath();
         String destination = new File( "./target/svnrepos/issue24-detectchanges" ).getAbsolutePath();
         if ( new File( destination ).exists() )
         {
             FileUtils.deleteDirectory( destination );
         }
 
         String revision = svnUtil.checkout( log, ScmType.SVN, svnUrl + "/trunk", destination );
         Assert.assertEquals( "3", revision );
         // checkout a second time into the already checked out working directory
         revision = svnUtil.checkout( log, ScmType.SVN, svnUrl + "/trunk", destination );
         Assert.assertEquals( "3", revision );
         SVNLogChange result = svnUtil.log( log, ScmType.SVN, svnUrl, destination, "messages.properties",
                 "2", "3" );
         Assert.assertEquals( SVNLogChange.MODIFICATION, result );
     }
 
     /**
      * Test various diffs with and without property changes.
      * @throws Exception any error
      */
     @Test
     public void testChangedPropertiesOnly() throws Exception
     {
         SVNUtil svnUtil = new SVNUtil();
         Log log = new NullLog();
 
         String svnUrl = "file://" + new File( "./src/test/resources/svnrepos/detectchanges-props-only" )
             .getCanonicalPath() + "/trunk";
         String destination = new File( "./target/svnrepos/detectchanges-props-only" ).getCanonicalPath();
         if ( new File( destination ).exists() )
         {
             FileUtils.deleteDirectory( destination );
         }
 
         String revision = svnUtil.checkout( log, ScmType.SVN, svnUrl, destination );
         Assert.assertEquals( "4", revision );
 
         SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss Z" );
         Date expectedDate = simpleDateFormat.parse( "2013-08-04 18:47:40 +0200" );
         String expectedDateString = simpleDateFormat.format( expectedDate );
         SVNInfo info = svnUtil.checkFile( log, ScmType.SVN, svnUrl, destination, "testfile.txt" );
         Assert.assertEquals( "4", info.getRevision() );
         Assert.assertTrue( "Expected: " + expectedDateString + "\nWas: " + info.getCommittedDate(),
                 info.getCommittedDate().startsWith( expectedDateString ) );
 
         // revision 2: only prop change
         SVNLogChange result = svnUtil.log( log, ScmType.SVN, svnUrl, destination, "testfile.txt", "1", "2" );
         Assert.assertEquals( SVNLogChange.MODIFICATION, result );
         String diff = svnUtil.diff( log, ScmType.SVN, svnUrl, destination, "testfile.txt", "1", "2" );
         Assert.assertTrue( diff.contains( "Property changes on: " ) );
        Assert.assertTrue( diff.contains( "Index: " ) ); // svnexe provides a Index, but empty changes
         UnifiedDiff unifiedDiff = new UnifiedDiff( diff );
         Assert.assertTrue( unifiedDiff.getHunks().isEmpty() );
 
         // revision 3: only file change (real diff)
         result = svnUtil.log( log, ScmType.SVN, svnUrl, destination, "testfile.txt", "2", "3" );
         Assert.assertEquals( SVNLogChange.MODIFICATION, result );
         diff = svnUtil.diff( log, ScmType.SVN, svnUrl, destination, "testfile.txt", "2", "3" );
         Assert.assertFalse( diff.contains( "Property changes on: " ) );
         Assert.assertTrue( diff.contains( "Index: " ) );
 
         // revision 4: combined change of file and property
         result = svnUtil.log( log, ScmType.SVN, svnUrl, destination, "testfile.txt", "3", "4" );
         Assert.assertEquals( SVNLogChange.MODIFICATION, result );
         diff = svnUtil.diff( log, ScmType.SVN, svnUrl, destination, "testfile.txt", "3", "4" );
         Assert.assertTrue( diff.contains( "Property changes on: " ) );
         Assert.assertTrue( diff.contains( "Index: " ) );
     }
 
     /**
      * Verify that git checkout works.
      * @throws Exception any error
      */
     @Test
     public void testGitCheckout() throws Exception
     {
         SVNUtil svnUtil = new SVNUtil();
         Log log = new NullLog();
         log = new SystemStreamLog();
 
         String destination = new File( "./target/gitrepos/repo1-checkout" ).getCanonicalPath();
         if ( new File( destination ).exists() )
         {
             FileUtils.deleteDirectory( destination );
         }
 
         Process unzip = Runtime.getRuntime().exec( "unzip -o repo1.zip", null,
                 new File( "./src/it/git-it/gitrepos/" ) );
         Assert.assertEquals( 0, unzip.waitFor() );
 
         String url = "./src/it/git-it/gitrepos/repo1/.git";
         String checkout = svnUtil.checkout( log, ScmType.GIT, url, destination );
         Assert.assertEquals( "f5d50077a92f9e29d704518ab2fbd9ecf7307214", checkout );
         File dstPath = new File( destination );
         Assert.assertTrue( dstPath.exists() && dstPath.isDirectory() );
         String[] files = dstPath.list();
         Arrays.sort( files );
         Assert.assertEquals( "[.git, project-a]", Arrays.toString( files ) );
     }
 }
