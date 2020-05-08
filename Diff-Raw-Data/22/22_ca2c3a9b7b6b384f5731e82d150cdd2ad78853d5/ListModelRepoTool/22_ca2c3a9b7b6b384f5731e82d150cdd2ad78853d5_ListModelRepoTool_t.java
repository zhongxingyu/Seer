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
 
 import java.util.List;
 import java.util.Set;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Sets;
 
 import org.kiji.common.flags.Flag;
 import org.kiji.modelrepo.KijiModelRepository;
 import org.kiji.modelrepo.ModelContainer;
 import org.kiji.schema.KConstants;
 import org.kiji.schema.Kiji;
 import org.kiji.schema.KijiURI;
 import org.kiji.schema.tools.BaseTool;
 
 /**
  * Implementation of the model repository list tool.
  * This will scan model repository table and list all requested fields.
  *
  * List all models' locations, production-readiness flags, messages, and model containers:
  * <pre>
  *   kiji model-repo list --kiji=kiji://.env/default --location --production-ready --message
  *       --model-container --max-versions=all
  * </pre>
  *
  * List only the latest production ready models:
  * <pre>
  *   kiji model-repo list --kiji=kiji://.env/default --production-ready-only
  * </pre>
  *
  * List only latest models matching specified name or version:
  * <pre>
  *   kiji model-repo list --kiji=kiji://.env/default --artifact=org.kiji.test.model
  *   kiji model-repo list --kiji=kiji://.env/default --artifact=org.kiji.test.model --version=1.0.1
  * </pre>
  */
 public final class ListModelRepoTool extends BaseTool implements KijiModelRepoTool {
 
   @Flag(name = "kiji", usage = "Name of the KIJI instance housing the model repository.")
   private String mInstanceName = null;
 
   private KijiURI mInstanceURI = null;
 
   @Flag(name = "artifact", usage = "Artifact to filter for.")
   private String mArtifactPattern = null;
 
   @Flag(name = "version", usage = "Version to filter for.")
   private String mVersionPattern = null;
 
   @Flag(name = "location", usage = "Print the locations of models.")
   private boolean mLocation = false;
 
   @Flag(name = "production-ready", usage = "Print the production readiness of models.")
   private boolean mProductionReady = false;
 
   @Flag(name = "production-ready-only",
       usage = "Print only those models which are production ready.")
   private boolean mProductionReadyOnly = false;
 
   @Flag(name = "message", usage = "Print the messages of models.")
   private boolean mMessage = false;
 
   @Flag(name = "model-container", usage = "Print the model container.")
   private boolean mModelContainer = false;
 
   @Flag(name = "max-versions",
       usage = "Max number of versions per cell to display ('all' for all versions). Default is 1.")
   private String mMaxVersions = "1";
 
   private int mMaxVersionsInteger;
 
   @Override
   protected void validateFlags() throws Exception {
     super.validateFlags();
     // Set up instance.
     if (mInstanceName == null) {
       mInstanceName = KConstants.DEFAULT_INSTANCE_NAME;
     }
     mInstanceURI = KijiURI.newBuilder(mInstanceName).build();
 
     // Set up max-versions.
     if (mMaxVersions.equals("all")) {
       mMaxVersionsInteger = Integer.MAX_VALUE;
     } else {
       mMaxVersionsInteger = Integer.parseInt(mMaxVersions);
       if (mMaxVersionsInteger < 1) {
         getPrintStream().printf("--max-versions must be positive, got '%s'%n", mMaxVersions);
       }
     }
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
     return "list";
   }
 
   @Override
   public String getDescription() {
     return "Lists models in model repository.";
   }
 
   @Override
   public String getUsageString() {
     return
         "Usage:\n"
         + "    kiji model-repo list --kiji=<kiji-uri> "
         + "[name filter flags: --artifact=<artifact name> --version=<artifact version>] "
         + "[feature flags: --location --message --production-ready --model-container] "
         + "[other filter flags: --production-ready --max-versions=...]\n"
         + "\n"
         + "Example:\n"
         + "  List all models' locations, production-readiness flags, messages, and containers:\n"
         + "    kiji model-repo list --kiji=kiji://.env/default --location "
         + "--production-ready --message  --model-container --max-versions=all\n"
         + "\n"
         + "  List only the latest production ready models:\n"
         + "    kiji model-repo list --kiji=kiji://.env/default --production-ready-only\n"
         + "\n"
         + "  List only latest models matching specified name or version:\n"
         + "    kiji model-repo list --kiji=kiji://.env/default --artifact=org.kiji.test.model\n"
         + "    kiji model-repo list --kiji=kiji://.env/default --artifact=org.kiji.test.model "
         + "--version=1.0.1\n";
   }
 
   /**
    * Read cmdline options and determine which fields that the user would like to print.
    *
    * @return set of fields to print.
    */
   private Set<String> getFieldsToPrint() {
     final Set<String> fieldsToPrint = Sets.newHashSet();
     if (mLocation) {
       fieldsToPrint.add(ModelContainer.LOCATION_KEY);
     }
     if (mModelContainer) {
       fieldsToPrint.add(ModelContainer.MODEL_CONTAINER_KEY);
     }
     if (mProductionReady) {
       fieldsToPrint.add(ModelContainer.PRODUCTION_READY_KEY);
     }
     if (mMessage) {
       fieldsToPrint.add(ModelContainer.MESSAGES_KEY);
     }
    if (fieldsToPrint.isEmpty()) {
      fieldsToPrint.add(ModelContainer.LOCATION_KEY);
      fieldsToPrint.add(ModelContainer.MODEL_CONTAINER_KEY);
      fieldsToPrint.add(ModelContainer.PRODUCTION_READY_KEY);
      fieldsToPrint.add(ModelContainer.MESSAGES_KEY);
    }
     return fieldsToPrint;
   }
 
   @Override
   protected int run(List<String> nonFlagArgs) throws Exception {
     // Check that arguments are valid.
     Preconditions.checkArgument(nonFlagArgs.size() == 0,
         "This tool does not accept unnamed arguments: ", nonFlagArgs.toString());
     Preconditions.checkNotNull(mInstanceURI);
     Preconditions.checkArgument(mMaxVersionsInteger > 0, "Max versions must be positive.");
 
     // Open model repository table.
     final Kiji kijiInstance = Kiji.Factory.open(mInstanceURI);
     final KijiModelRepository modelRepository = KijiModelRepository.open(kijiInstance);
 
     // Create list of fields that the user wants to print.
     final Set<String> fieldsToPrint = this.getFieldsToPrint();
 
     // Query the model repository for a list of entries.
     final Set<ModelContainer> setOfModels = modelRepository.getModelContainers(
         fieldsToPrint,
         mMaxVersionsInteger,
         mProductionReadyOnly);
 
     // Iterate through list of model row data and appropriately pretty print fields.
     for (final ModelContainer model : setOfModels) {
       if (isMatch(model)) {
         getPrintStream().println(model.toString());
       }
     }
 
     modelRepository.close();
     kijiInstance.release();
     return SUCCESS;
   }
 
   /**
    * Determines if a given model is valid with respect to any conditions placed by the
    * user when lauching the tool.
    *
    * @param model the model to determine if it's a match.
    * @return true if the model's name and version matches what the user requested. If the user
    *         did not request any name/version specifics, then it will return true.
    */
   private boolean isMatch(ModelContainer model) {
     boolean isMatch = true;
     if (mVersionPattern != null) {
       isMatch &= model.getArtifactName().getVersion().toString()
           .equalsIgnoreCase(mVersionPattern);
     }
     if (mArtifactPattern != null) {
       isMatch &= mArtifactPattern.equalsIgnoreCase(model.getArtifactName().getName());
     }
 
     return isMatch;
   }
 }
