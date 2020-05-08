 package npanday.its;
 
 /*
  * Copyright 2009
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
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
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.zip.ZipFile;
 
 public class NPandayIT0012VBWebAppTest
     extends AbstractNPandayIntegrationTestCase
 {
     public NPandayIT0012VBWebAppTest()
     {
         super( "[1.2,)" );
     }
 
     public void testWebAppInstall()
         throws Exception
     {
         File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/NPandayIT0012VBWebAppTest" );
         Verifier verifier = getVerifier( testDir );
         verifier.executeGoal( "install" );
         File zipFile = new File( testDir, getAssemblyFile( "VBWebAppTest", "1.0.0", "zip" ) );
         verifier.assertFilePresent( zipFile.getAbsolutePath() );
         verifier.verifyErrorFreeLog();
         verifier.resetStreams();
 
         List<String> expectedEntries = Arrays.asList( "bin/VBWebAppTest.dll", "Default.aspx",
                                                       "My Project/Application.myapp", "My Project/Resources.resx",
                                                       "My Project/Settings.settings", "Web.config" );
 
         assertZipEntries( zipFile, expectedEntries );
 
        String assembly = new File( testDir, "target/bin/VBWebAppTest.dll" ).getAbsolutePath();
         assertResourcePresent( assembly, "VBWebAppTest.Resources.resources" );
         assertClassPresent( assembly, "VBWebAppTest._Default" );
     }
 }
