 package npanday.its;
 
 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 import org.apache.maven.it.Verifier;
 import org.apache.maven.it.util.ResourceExtractor;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 
 public class NPandayIT0013WebAppInstallTest
     extends AbstractNPandayIntegrationTestCase
 {
     public NPandayIT0013WebAppInstallTest()
     {
         super( "[1.0.2,)" );
     }
 
     public void testWebAppInstall()
         throws Exception
     {
         File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/NPandayIT0013WebAppInstallTest" );
         Verifier verifier = getVerifier( testDir );
 
         File localRepoZip = new File( verifier.getArtifactPath( "NPanday.ITs", "WebAppExample", "1.0-SNAPSHOT", "zip" ) );
         File localRepoDll = new File( verifier.getArtifactPath( "NPanday.ITs", "WebAppExample", "1.0-SNAPSHOT", "dll" ) );
         localRepoZip.delete();
         localRepoDll.delete();
 
         verifier.executeGoal( "install" );
         File zipFile = new File( testDir, getAssemblyFile( "WebAppExample", "1.0.0", "zip" ) );
         verifier.assertFilePresent( zipFile.getAbsolutePath() );
         verifier.verifyErrorFreeLog();
         verifier.resetStreams();
 
         List<String> expectedEntries = Arrays.asList( "bin/WebAppExample.dll", "Default.aspx", "Web.config" );
 
         assertZipEntries( zipFile, expectedEntries );
 
         String assembly = new File( testDir, "target/WebAppExample/bin/WebAppExample.dll" ).getCanonicalPath();
         assertClassPresent( assembly, "_Default" );
 
         // aspx:package sets the main artifact as the ZIP, not the DLL
        if ( checkNPandayVersion( "[1.4.1-incubating,)" ) )
         {
             assertTrue( localRepoZip.exists() );
             assertFalse( localRepoDll.exists() );
         }
     }
 }
