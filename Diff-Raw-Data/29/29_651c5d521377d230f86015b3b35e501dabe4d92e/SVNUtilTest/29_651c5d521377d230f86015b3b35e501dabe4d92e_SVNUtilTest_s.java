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
 
 import junit.framework.Assert;
 import net.sf.yal10n.analyzer.NullLog;
 
 import org.apache.maven.plugin.logging.Log;
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
      */
     @Test
     public void testIssue24DetectChanges()
     {
         SVNUtil svnUtil = new SVNUtil();
         Log log = new NullLog();
 
         String svnUrl = "file://" + new File( "./src/test/resources/svnrepos/issue24-detectchanges" ).getAbsolutePath();
         String destination = new File( "./target/svnrepos/issue24-detectchanges" ).getAbsolutePath();
 
        svnUtil.checkout( log, svnUrl, destination );
        SVNLogChange result = svnUtil.log( log, destination, destination + "/messages.properties",
                2, 2 );
         Assert.assertEquals( SVNLogChange.MODIFICATION, result );
     }
 }
