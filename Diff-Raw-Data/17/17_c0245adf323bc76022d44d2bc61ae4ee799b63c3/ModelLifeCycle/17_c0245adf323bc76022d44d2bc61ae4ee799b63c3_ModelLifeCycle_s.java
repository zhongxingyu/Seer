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
 
 package org.kiji.modelrepo;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NavigableMap;
 import java.util.Set;
 import java.util.TreeMap;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import org.codehaus.plexus.util.FileUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import org.kiji.modeling.avro.AvroColumnFamilyRequestOutput;
 import org.kiji.modeling.avro.AvroKijiSingleColumnOutputSpec;
 import org.kiji.modeling.avro.AvroModelDefinition;
 import org.kiji.modeling.avro.AvroModelEnvironment;
import org.kiji.modeling.avro.AvroQualifiedColumnRequestOutput;
 import org.kiji.modelrepo.artifactvalidator.ArtifactValidator;
 import org.kiji.modelrepo.artifactvalidator.WarArtifactValidator;
 import org.kiji.schema.Kiji;
 import org.kiji.schema.KijiColumnName;
 import org.kiji.schema.KijiRowData;
 import org.kiji.schema.KijiURI;
 import org.kiji.schema.util.ProtocolVersion;
 import org.kiji.scoring.KijiFreshnessManager;
 import org.kiji.scoring.lib.server.ScoringServerScoreFunction;
 
 /**
  * Class to work with model artifact metadata sourced from the model repository table.
  */
 public class ModelLifeCycle {
 
   public static final String DEFINITION_KEY = "definition";
   public static final String ENVIRONMENT_KEY = "environment";
   public static final String LOCATION_KEY = "location";
   public static final String MESSAGES_KEY = "message";
   public static final String PRODUCTION_READY_KEY = "production_ready";
   public static final String UPLOADED_KEY = "uploaded";
   public static final String MODEL_REPO_FAMILY = "model";
 
   private static final Logger LOG = LoggerFactory.getLogger(ModelLifeCycle.class);
 
   private final boolean mUploaded;
   private final ArtifactName mArtifact;
   private final String mLocation;
   private final AvroModelDefinition mDefinition;
   private final AvroModelEnvironment mEnvironment;
   private final Map<Long, String> mMessages = Maps.newTreeMap();
   private final TreeMap<Long, Boolean> mProductionReady = Maps.newTreeMap();
 
   private final URI mBaseStorageURI;
 
   /**
    * Construct model lifecycle object from row data and required fields
    * from the model repository table. Proper construction of ModelArtifact
    * requires UPLOADED_KEY field set to true.
    *
    * @param model row data from the model repository table.
    * @param fieldsToRead read only these fields from the row data.
    * @param baseURI is the base URI of the underlying storage layer.
    * @throws IOException if the row data can not be properly accessed to retrieve values.
    */
   public ModelLifeCycle(final KijiRowData model,
       final Set<String> fieldsToRead, URI baseURI) throws IOException {
 
     // Load name and version.
     mArtifact = new ArtifactName(
         model.getEntityId().getComponentByIndex(0).toString(),
         ProtocolVersion.parse(model.getEntityId().getComponentByIndex(1).toString()));
 
     mBaseStorageURI = baseURI;
     // Load name, definition, environment, and location.
 
     // Every row must have the uploaded field specified and it must be true.
     try {
       mUploaded = model.<Boolean>getMostRecentValue(MODEL_REPO_FAMILY, UPLOADED_KEY);
     } catch (final Exception e) {
       throw new IOException("Requested model could not be extracted.");
     }
     if (!mUploaded) {
       throw new IOException("Requested model was not uploaded, therefore not deployed.");
     }
     if (fieldsToRead.contains(DEFINITION_KEY)) {
       try {
         mDefinition =
             (AvroModelDefinition) model.getMostRecentValue(MODEL_REPO_FAMILY, DEFINITION_KEY);
       } catch (final Exception e) {
         throw new IOException("Following field was not extracted: " + DEFINITION_KEY);
       }
     } else {
       mDefinition = null;
     }
     if (fieldsToRead.contains(ENVIRONMENT_KEY)) {
       try {
         mEnvironment =
             (AvroModelEnvironment) model.getMostRecentValue(MODEL_REPO_FAMILY, ENVIRONMENT_KEY);
       } catch (final Exception e) {
         throw new IOException("Following field was not extracted: " + ENVIRONMENT_KEY);
       }
     } else {
       mEnvironment = null;
     }
     if (fieldsToRead.contains(LOCATION_KEY)) {
       try {
         mLocation = model.getMostRecentValue(MODEL_REPO_FAMILY, LOCATION_KEY).toString();
       } catch (final Exception e) {
         throw new IOException("Following field was not extracted: " + LOCATION_KEY);
       }
     } else {
       mLocation = null;
     }
     // Load production_ready flag.
     if (fieldsToRead.contains(PRODUCTION_READY_KEY)) {
       try {
         final NavigableMap<Long, Boolean> productionReadyValues =
             model.getValues(MODEL_REPO_FAMILY, PRODUCTION_READY_KEY);
         for (final Entry<Long, Boolean> cell : productionReadyValues.entrySet()) {
           mProductionReady.put(cell.getKey(), cell.getValue());
         }
       } catch (final Exception e) {
         throw new IOException("Following field was not extracted: " + PRODUCTION_READY_KEY);
       }
     }
     // Load messages.
     if (fieldsToRead.contains(MESSAGES_KEY)) {
       try {
         final NavigableMap<Long, CharSequence> messagesValues =
             model.getValues(MODEL_REPO_FAMILY, MESSAGES_KEY);
         for (final Entry<Long, CharSequence> cell : messagesValues.entrySet()) {
           mMessages.put(cell.getKey(), cell.getValue().toString());
         }
       } catch (final Exception e) {
         throw new IOException("Following field was not extracted: " + MESSAGES_KEY);
       }
     }
   }
 
   /**
    * Gets lifecycle artifact's name.
    *
    * @return artifact name.
    */
   public ArtifactName getArtifactName() {
     return mArtifact;
   }
 
   /**
    * Gets the location where the artifact package exists.
    *
    * @return artifact location.
    */
   public String getLocation() {
     return mLocation;
   }
 
   /**
    * Gets lifecycle's model definition.
    *
    * @return model definition.
    */
   public AvroModelDefinition getDefinition() {
     return mDefinition;
   }
 
   /**
    * Gets lifecycle's model environment.
    *
    * @return model environment.
    */
   public AvroModelEnvironment getEnvironment() {
     return mEnvironment;
   }
 
   /**
    * Gets a map of the messages associated with this lifecycle, keyed by timestamp.
    *
    * @return map of messages keyed by timestamp.
    */
   public Map<Long, String> getMessages() {
     return mMessages;
   }
 
   /**
    * Gets a map of the production_ready flags associated with this lifecycle, keyed by timestamp.
    *
    * @return map of production_ready flags keyed by timestamp.
    */
   public Map<Long, Boolean> getProductionReady() {
     return mProductionReady;
   }
 
   @Override
   public String toString() {
     final StringBuilder modelStringBuilder = new StringBuilder();
     modelStringBuilder.append(String.format("name: %s%n", mArtifact.getName()));
     modelStringBuilder.append(String.format("version: %s%n", mArtifact.getVersion()));
 
     // Print the fields which were requested by the user.
     if (null != getLocation()) {
       modelStringBuilder.append(String.format("%s: %s%n", "location", getLocation()));
     }
     if (null != getDefinition()) {
       modelStringBuilder.append(String.format("%s: %s%n", "definition", getDefinition()));
     }
     if (null != getEnvironment()) {
       modelStringBuilder.append(String.format("%s: %s%n", "environment", getEnvironment()));
     }
 
     // Merge the sorted lists of production_ready and message flags and pretty print.
     final Iterator<Entry<Long, Boolean>> productionReady =
         getProductionReady().entrySet().iterator();
     final Iterator<Entry<Long, String>> messages = getMessages().entrySet().iterator();
     Map.Entry<Long, Boolean> productionReadyEntry = null;
     Map.Entry<Long, String> messagesEntry = null;
     if (productionReady.hasNext()) {
       productionReadyEntry = productionReady.next();
     }
     if (messages.hasNext()) {
       messagesEntry = messages.next();
     }
     while (null != productionReadyEntry || null != messagesEntry) {
       if ((null == productionReadyEntry)
           || ((null != messagesEntry)
           && (productionReadyEntry.getKey().compareTo(messagesEntry.getKey()) >= 0))) {
         modelStringBuilder.append(String.format("%s [%d]: %s%n", "message",
             messagesEntry.getKey(),
             messagesEntry.getValue()));
         if (messages.hasNext()) {
           messagesEntry = messages.next();
         } else {
           messagesEntry = null;
         }
       } else {
         modelStringBuilder.append(String.format("%s [%d]: %s%n", "production_ready",
             productionReadyEntry.getKey(),
             productionReadyEntry.getValue()));
         if (productionReady.hasNext()) {
           productionReadyEntry = productionReady.next();
         } else {
           productionReadyEntry = null;
         }
       }
     }
     return modelStringBuilder.toString();
   }
 
   /**
    * Check that this model lifecycle is associated with a valid model location
    * in the model repository, i.e. that a valid model artifact is found at the model location.
    *
    * @param download set to true allows the method to download and validate the artifact file.
    *
    * @return List of exceptions of inconsistent model locations over time.
    * @throws IOException if a temporary file can not be allocated for downloading model artifacts.
    */
   public List<Exception> checkModelLocation(
       final boolean download) throws IOException {
     final List<Exception> issues = Lists.newArrayList();
     final URL location = mBaseStorageURI.resolve(this.getLocation()).toURL();
     // Currently only supports http and fs.
     // TODO: Support more protocols: ssh, etc.
     if (download) {
       final File artifactFile = File.createTempFile(this.getArtifactName().toString(), ".war");
       artifactFile.deleteOnExit();
       // Download artifact to artifactFile.
       if (downloadArtifact(artifactFile)) {
         // Parse artifactFile for validation.
         final ArtifactValidator artifactValidator = new WarArtifactValidator();
         if (!artifactValidator.isValid(artifactFile)) {
           issues.add(new ModelRepositoryConsistencyException(
               this.getArtifactName(),
               String.format("Artifact from %s is not a valid jar/war file.",
                   location.toString())));
         }
       } else {
         issues.add(new ModelRepositoryConsistencyException(
             this.getArtifactName(),
             String.format("Unable to retrieve artifact from %s.",
                 location.toString())));
       }
       // Delete artifactFile as it's no use any more.
       artifactFile.delete();
     } else {
       InputStream artifactInputStream = null;
       try {
         artifactInputStream = location.openStream();
       } catch (Exception e) {
         issues.add(new ModelRepositoryConsistencyException(
             this.getArtifactName(),
             String.format("Unable to find artifact at %s.",
                 location.toString())));
       } finally {
         if (null != artifactInputStream) {
           artifactInputStream.close();
         }
       }
     }
     return issues;
   }
 
   /**
    * Copies artifact from URL to file on disk.
    *
    * @param file target
    *
    * @return true iff copy happen unexceptionally
    * @throws IOException if there is a problem downloading the file.
    */
   public boolean downloadArtifact(final File file) throws IOException {
     final URI resolvedURI = URI.create(mBaseStorageURI.toString() + "/"
         + mLocation);
     final URL location = resolvedURI.toURL();
 
     LOG.info("Preparing to download model artifact to temporary location {}",
         file.getAbsolutePath());
     try {
       FileUtils.copyURLToFile(location, file);
     } catch (Exception e) {
       return false;
     }
     return true;
   }
 
   /**
    * Returns the canonical name of a model life cycle given the group and artifact name. This
    * is here as the layout supports a single name field but other parts of the system support
    * the separate group/artifact names.
    *
    * @param groupName the group name of the lifecycle.
    * @param artifactName the artifact name of the lifecycle.
    * @return the canonical name of the model lifecycle suitable for storage in the Kiji table
    *         storing deployed lifecycles.
    */
   public static String getModelName(String groupName, String artifactName) {
     Preconditions.checkNotNull(groupName);
     Preconditions.checkNotNull(artifactName);
     Preconditions.checkArgument(!groupName.isEmpty(), "Group name must be nonempty string.");
     Preconditions.checkArgument(!artifactName.isEmpty(),
         "Artifact name must be nonempty string.");
     return (groupName + "." + artifactName).intern();
   }
 
   /**
    * Attach this model lifecycle as a remote freshener fulfilled by the ScoringServer. Requires that
    * the model be production ready. Attached to the table and column specified in the model
    * environment.
    *
    * @param kiji the Kiji instance in which the table the Freshener will be attached within lives.
    * @param policyClass fully qualified class name of the KijiFreshnessPolicy class to use to govern
    *     Freshening.
    * @param parameters string-string configuration parameters which will be available to the policy
    *     and model. The policy may access these via its context objects, the model may use a special
    *     KeyValueStore. TODO (EXP-240) name this KVStore and make it work.
    * @param overwriteExisting whether to overwrite an existing attachment to the column specified in
    *     the model environment.
    * @param instantiateClasses whether to instantiate the policy and score function classes during
    *     registration in order to call their serializeToParameters methods.
    * @param setupClasses whether to setup the policy and score function classes during registration
    *     before calling their serializeToParameters methods. This may open remote connections for
    *     KeyValueStores. Context objects for setup will be created using the other parameters given
    *     here. This option has no effect if instantiate classes is false.
    * @throws IOException in case of an error registering the Freshener.
    */
   public void attachAsRemoteFreshener(
       final Kiji kiji,
       final String policyClass,
       final Map<String, String> parameters,
       final boolean overwriteExisting,
       final boolean instantiateClasses,
       final boolean setupClasses
   ) throws IOException {
     Preconditions.checkState(mProductionReady.lastEntry().getValue(),
         "Model must be production ready to be attached as a Freshener.");
     final AvroKijiSingleColumnOutputSpec outputSpec =
         mEnvironment.getScoreEnvironment().getOutputSpec();
 
     final String tableName = KijiURI.newBuilder(outputSpec.getTableUri()).build().getTable();
 
     final KijiColumnName columnName;
     Object outputColumn = outputSpec.getOutputColumn();
    if (outputColumn instanceof AvroQualifiedColumnRequestOutput) {
      String family = ((AvroQualifiedColumnRequestOutput) outputColumn).getFamily();
      String qualifier = ((AvroQualifiedColumnRequestOutput) outputColumn).getQualifier();
       columnName = new KijiColumnName(family, qualifier);
    } else if (outputColumn instanceof AvroColumnFamilyRequestOutput) {
      String family = ((AvroColumnFamilyRequestOutput) outputColumn).getFamily();
       columnName = new KijiColumnName(family);
     } else {
       throw new IOException("Error getting output column name");
     }
 
     final String scoreFunctionClass = ScoringServerScoreFunction.class.getName();
     final Map<String, String> innerParams = Maps.newHashMap(parameters);
     // TODO eliminate this hard coding.
     final byte[] baseURLBytes = kiji.getMetaTable().getValue(
         KijiModelRepository.MODEL_REPO_TABLE_NAME,
         KijiModelRepository.SCORING_SERVER_URL_METATABLE_KEY);
     innerParams.put(ScoringServerScoreFunction.SCORING_SERVER_BASE_URL_PARAMETER_KEY,
         new String(baseURLBytes));
     innerParams.put(ScoringServerScoreFunction.SCORING_SERVER_MODEL_ID_PARAMETER_KEY,
         mArtifact.getFullyQualifiedName());
 
     final KijiFreshnessManager manager = KijiFreshnessManager.create(kiji);
     try {
       manager.registerFreshener(
           tableName,
           columnName,
           policyClass,
           scoreFunctionClass,
           innerParams,
           overwriteExisting,
           instantiateClasses,
           setupClasses);
     } finally {
       manager.close();
     }
   }
 }
