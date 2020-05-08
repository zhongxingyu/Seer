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
 
 import java.io.IOException;
 import java.util.List;
 
 import com.google.common.base.Preconditions;
 
 import org.kiji.common.flags.Flag;
 import org.kiji.modelrepo.KijiModelRepository;
 import org.kiji.schema.KConstants;
 import org.kiji.schema.Kiji;
 import org.kiji.schema.KijiURI;
 import org.kiji.schema.tools.BaseTool;
 
 /**
  * Implementation of the model repository upgrade tool. This will upgrade the model repository
  * to the latest version.
  */
 public final class UpgradeModelRepoTool extends BaseTool implements KijiModelRepoTool {
 
   @Flag(name = "kiji", usage = "Name of the Kiji instance housing the model repository.")
   private String mKijiURIFlag = null;
 
   private KijiURI mInstanceURI = null;
 
   /** The default instance to use for housing the model lifecycle table and meta information. **/
   private static final String DEFAULT_INSTANCE_URI = KConstants.DEFAULT_INSTANCE_URI;
 
   @Override
   protected void validateFlags() throws Exception {
     super.validateFlags();
     if (mKijiURIFlag == null) {
       mKijiURIFlag = DEFAULT_INSTANCE_URI;
     }
     mInstanceURI = KijiURI.newBuilder(mKijiURIFlag).build();
   }
 
   @Override
   public String getName() {
     return MODEL_REPO_TOOL_BASE + getModelRepoToolName();
   }
 
   @Override
   public String getCategory() {
     return MODEL_REPO_TOOL_CATEGORY;
   }
 
   @Override
   public String getModelRepoToolName() {
     return "upgrade";
   }
 
   @Override
   public String getDescription() {
     return "Upgrades a new model repository.";
   }
 
   @Override
   protected int run(List<String> nonFlagArgs) throws Exception {
     Preconditions.checkArgument(nonFlagArgs.size() == 0,
         getName() + " doesn't take any non flag arguments.  Found: " + nonFlagArgs.size());
 
     Kiji kijiInstance = null;
     try {
       kijiInstance = Kiji.Factory.open(mInstanceURI);
       KijiModelRepository.upgrade(kijiInstance);
    } catch (IOException ioe) {
       getPrintStream().printf("Unable to upgrade model repository: " + ioe.getMessage());
       return FAILURE;
     } finally {
       if (null != kijiInstance) {
         kijiInstance.release();
       }
     }
     return SUCCESS;
   }
 }
