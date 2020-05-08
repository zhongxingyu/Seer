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
 
 public class NPANDAY_480_CloudServiceWithMultipleRoles
     extends AbstractNPandayIntegrationTestCase
 {
     public NPANDAY_480_CloudServiceWithMultipleRoles()
     {
        super( "[1.5.0-incubating,)", "[v4.0,)" );
 
         skipIfMissingProgramFilesDirectory( "Windows Azure SDK", "Azure SDK is not installed" );
     }
 
     public void test()
         throws Exception
     {
         NPandayIntegrationTestContext context = createDefaultTestContext();
         Verifier verifier = context.getPreparedVerifier();
 
         verifier.executeGoal( "install" );
         final String a = "HelloWorld_CloudService";
         final String v = "1.0.0-SNAPSHOT";
         verifier.assertArtifactPresent(context.getGroupId(), a, v, "cspkg" );
 
         verifier.assertFilePresent(
             verifier.getArtifactPath(
                 context.getGroupId(), a, v, "cscfg", "generated"
             )
         );
 
         verifier.assertFilePresent(
             verifier.getArtifactPath(
                 context.getGroupId(), a, v, "cscfg", "package"
             )
         );
 
         verifier.assertArtifactPresent( context.getGroupId(), "HelloWorld_WorkerRole", v, "dll" );
         verifier.assertArtifactPresent( context.getGroupId(), "HelloWorld_WorkerRole", v, "app.zip" );
 
         // TODO: check package contents
 
         verifier.verifyErrorFreeLog();
         verifier.resetStreams();
     }
 
 }
 
