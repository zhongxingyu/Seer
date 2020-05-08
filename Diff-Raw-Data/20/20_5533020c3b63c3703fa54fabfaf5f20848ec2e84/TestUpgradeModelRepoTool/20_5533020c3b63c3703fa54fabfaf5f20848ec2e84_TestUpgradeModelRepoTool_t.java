 /**
  * (c) Copyright 2013 WibiData, Inc.
  *
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
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
 
 package org.kiji.modelrepo.tools;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import org.kiji.schema.Kiji;
 import org.kiji.schema.tools.BaseTool;
 import org.kiji.schema.tools.KijiToolTest;
 
 public class TestUpgradeModelRepoTool extends KijiToolTest {
 
   @Test
   public void testUpgradeInstalledModelRepository() throws Exception {
     final Kiji localKiji = getKiji();
     final String baseRepoUrl = "http://someHost:1234/releases";
     final String kijiArg = String.format("--kiji=%s", localKiji.getURI().toString());
     runTool(new InitModelRepoTool(), kijiArg, baseRepoUrl);
 
     int returnCode = runTool(new UpgradeModelRepoTool(), kijiArg);
     Assert.assertEquals(BaseTool.SUCCESS, returnCode);
   }
 
   @Test
   public void testShouldFailIfTableNotInstalled() throws Exception {
     final Kiji localKiji = getKiji();
     final String kijiArg = String.format("--kiji=%s", localKiji.getURI().toString());
     int returnCode = runTool(new UpgradeModelRepoTool(), kijiArg);
     Assert.assertEquals(BaseTool.FAILURE, returnCode);
   }
 
   @Test
   public void testShouldFailWithNonFlagArguments() throws Exception {
     final Kiji localKiji = getKiji();
     final String baseRepoUrl = "http://someHost:1234/releases";
     final String kijiArg = String.format("--kiji=%s", localKiji.getURI().toString());
     try {
       int returnCode = runTool(new UpgradeModelRepoTool(), kijiArg, baseRepoUrl);
       Assert.fail("Should fail with IllegalArgumentException");
     } catch (IllegalArgumentException iae) {
      Assert.assertEquals("model-repo upgrade doesn't take any non flag arguments.  Found: 1",
          iae.getMessage());
     }
   }
 }
