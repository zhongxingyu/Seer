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
 
 public class NPandayIT11480MVCProjectTest
     extends AbstractNPandayIntegrationTestCase
 {
     public NPandayIT11480MVCProjectTest()
     {
         super( "[1.2,)", "[v3.5,)" ); 
 
        File f = new File( System.getenv( "SYSTEMROOT" ), "assembly/GAC_MSIL/System.Web.MVC" );
         if ( !f.exists() || !f.isDirectory() )
         {
             skipReason = "MVC.NET is not installed";
             skip = true;
         }
     }
 
     public void testMVCProject()
         throws Exception
     {
         File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/NPanday11480" );
         Verifier verifier = getVerifier( testDir );
         verifier.executeGoal( "install" );
         		
 		String assembly = new File( testDir + "/NPanday11480",
             getAssemblyFile( "NPanday11480", "1.0.0.0", "dll" ) ).getAbsolutePath();
         verifier.assertFilePresent( assembly );
 		
 		assertClassPresent( assembly, "NPanday11480.Controllers.AccountController" );
         assertClassPresent( assembly, "NPanday11480.Controllers.HomeController" );
         assertClassPresent( assembly, "NPanday11480.MvcApplication" );
         verifier.verifyErrorFreeLog();
         verifier.resetStreams();
     }
 }
