 package npanday.its;
 
 import java.io.File;
 
 import org.apache.maven.it.Verifier;
 import org.apache.maven.it.util.ResourceExtractor;
 
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
 
 public class NPandayITSnapshotResolutionTest
     extends AbstractNPandayIntegrationTestCase
 {
     public NPandayITSnapshotResolutionTest()
     {
         super( "[1.0,)" );
     }
 
     public void testUniqueSnapshotResolution()
         throws Exception
     {
         File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/test-unique-snapshot" );
         Verifier verifier = getVerifier( testDir );
 
         verifier.deleteArtifact( "test", "test-snapshot", "1.0-SNAPSHOT", "dll" );
 
         verifier.executeGoal( "install" );
         verifier.assertFilePresent(
             new File( testDir, getAssemblyFile( "test-unique-snapshot", "1.0-SNAPSHOT", "dll" ) ).getAbsolutePath() );
         verifier.assertFilePresent( new File( testDir, getAssemblyFile( "test-unique-snapshot-test", "1.0-SNAPSHOT",
                                                                         "dll" ) ).getAbsolutePath() );
        verifier.assertFilePresent( new File( testDir, "test-assemblies/" +
            getAssemblyFile( "test-unique-snapshot", "1.0-SNAPSHOT", "dll" ) ).getAbsolutePath() );
        verifier.assertFilePresent( new File( testDir, "test-assemblies/" +
            getAssemblyFile( "test-unique-snapshot-test", "1.0-SNAPSHOT", "dll" ) ).getAbsolutePath() );
        verifier.assertFilePresent( new File( testDir, "test-assemblies/" +
            getAssemblyFile( "test-snapshot", "1.0-SNAPSHOT", "dll" ) ).getAbsolutePath() );
         verifier.verifyErrorFreeLog();
         verifier.resetStreams();
     }
 
     public void testNonUniqueSnapshotResolution()
         throws Exception
     {
         File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/test-non-unique-snapshot" );
         Verifier verifier = getVerifier( testDir );
 
         verifier.deleteArtifact( "test", "test-snapshot", "1.0-SNAPSHOT", "dll" );
 
         verifier.executeGoal( "install" );
         verifier.assertFilePresent( new File( testDir, getAssemblyFile( "test-non-unique-snapshot", "1.0-SNAPSHOT",
                                                                         "dll" ) ).getAbsolutePath() );
         verifier.assertFilePresent( new File( testDir, getAssemblyFile( "test-non-unique-snapshot-test", "1.0-SNAPSHOT",
                                                                         "dll" ) ).getAbsolutePath() );
        verifier.assertFilePresent( new File( testDir, "test-assemblies/" +
            getAssemblyFile( "test-non-unique-snapshot", "1.0-SNAPSHOT", "dll" ) ).getAbsolutePath() );
        verifier.assertFilePresent( new File( testDir, "test-assemblies/" +
            getAssemblyFile( "test-non-unique-snapshot-test", "1.0-SNAPSHOT", "dll" ) ).getAbsolutePath() );
        verifier.assertFilePresent( new File( testDir, "test-assemblies/" +
            getAssemblyFile( "test-snapshot", "1.0-SNAPSHOT", "dll" ) ).getAbsolutePath() );
         verifier.verifyErrorFreeLog();
         verifier.resetStreams();
     }
 }
