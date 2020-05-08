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
import java.util.Arrays;
import java.util.List;
 
 public class NPandayIT0020Test
     extends AbstractNPandayIntegrationTestCase
 {
     public NPandayIT0020Test()
     {
         super( "[1.2,)" );
     }
 
     public void testEmbeddedResources()
         throws Exception
     {
         
         File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/NPandayIT0020" );
         Verifier verifier = getVerifier( testDir );
         verifier.executeGoal( "install" );
         verifier.assertFilePresent(
            new File( testDir, "target/assembly-resources/resource/resgen/fix.gif" ).getAbsolutePath());
         verifier.assertFilePresent(
            new File( testDir, "target/assembly-resources/resource/resgen/my-prop.x-properties").getAbsolutePath());
         String assembly = new File( testDir, getAssemblyFile( "NPandayIT0020", "1.0.0.0", "dll" ) ).getAbsolutePath();
         verifier.assertFilePresent( assembly );  
        assertResourcePresent( assembly, "resgen/fix.gif" );
        assertResourcePresent( assembly, "resgen/my-prop.x-properties" );
         assertClassNotPresent( assembly, "NPandayIT0020.resgen.fix.gif" );
         assertClassNotPresent( assembly, "NPandayIT0020.fix.gif" );
         verifier.verifyErrorFreeLog();
         verifier.resetStreams();
     }
 }
