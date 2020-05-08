 package npanday.its;
 
 /*
  * Copyright 2010
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import org.apache.maven.it.Verifier;
 import org.apache.maven.it.util.ResourceExtractor;
 
 import java.io.File;
 
 public class NPandayIT12940MixedVersionsTest
     extends AbstractNPandayIntegrationTestCase
 {
     public NPandayIT12940MixedVersionsTest()
     {
        super( "[1.2,)" );
     }
 
     public void testMixedVersionResolution()
         throws Exception
     {
         File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/NPandayIT12940" );
         Verifier verifier = getVerifier( testDir );
 
         verifier.deleteArtifact( "test", "test-snapshot", "1.0-SNAPSHOT", "dll" );
 
         verifier.executeGoal( "install" );
         verifier.assertFilePresent(
             new File( testDir, getAssemblyFile( "test-mixed-versions", "1.0-SNAPSHOT", "dll" ) ).getAbsolutePath() );
         verifier.assertFilePresent( new File( testDir, getAssemblyFile( "test-mixed-versions-test", "1.0-SNAPSHOT",
                                                                         "dll" ) ).getAbsolutePath() );
         verifier.assertFilePresent(
             new File( testDir, "target/test-assemblies/test-mixed-versions.dll" ).getAbsolutePath() );
         verifier.assertFilePresent(
             new File( testDir, "target/test-assemblies/test-mixed-versions-test.dll" ).getAbsolutePath() );
         verifier.assertFilePresent( new File( testDir, "target/test-assemblies/test-snapshot.dll" ).getAbsolutePath() );
         verifier.verifyErrorFreeLog();
         verifier.resetStreams();
     }
 }
