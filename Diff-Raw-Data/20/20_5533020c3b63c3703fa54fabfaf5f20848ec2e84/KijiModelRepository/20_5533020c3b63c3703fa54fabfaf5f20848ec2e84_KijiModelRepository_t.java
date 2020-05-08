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
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.net.URI;
 import java.nio.ByteBuffer;
 
 import com.google.common.base.Preconditions;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.kiji.express.avro.AvroModelDefinition;
 import org.kiji.express.avro.AvroModelEnvironment;
 import org.kiji.schema.AtomicKijiPutter;
 import org.kiji.schema.EntityId;
 import org.kiji.schema.Kiji;
 import org.kiji.schema.KijiMetaTable;
 import org.kiji.schema.KijiTable;
 import org.kiji.schema.avro.TableLayoutDesc;
 import org.kiji.schema.layout.KijiTableLayout;
 import org.kiji.schema.util.ProtocolVersion;
 
 /**
  *
  * A class providing an API to install and access the model repository Kiji table.
  *
  * Used by CLI tools to read and write to the model repository along with other downstream
  * clients such as the scoring server to fetch information from the repository.
  *
  */
 public final class KijiModelRepository implements Closeable {
 
   /** The default table name to use for housing model lifecycle metadata information. **/
   public static final String MODEL_REPO_TABLE_NAME = "model_repo";
 
   /** The path to the layout for the table in our resources. */
   private static final String TABLE_LAYOUT_BASE_PKG = "/org/kiji/modelrepo/layouts";
 
   /** The HBaseKijiTable managed by the KijiModelRepository. */
   private final KijiTable mKijiTable;
 
   /** The associated Kiji metatable associated with the KijiModelRepository. */
   private KijiMetaTable mKijiMetaTable;
 
   private int mCurrentModelRepoVersion = 0;
 
   /**
    * The latest layout version. Defined by looking at all the
    * json files org.kiji.modelrepo.layouts
    **/
   private static int mLatestLayoutVersion = 0;
 
   /** The latest layout. **/
   private static KijiTableLayout mLatestLayout = null;
 
   private static final String REPO_VERSION_KEY = "kiji.model_repo.version";
   private static final String REPO_BASE_URL_KEY = "kiji.model_repo.base_repo_url";
 
   private static final String REPO_LAYOUT_VERSION_PREFIX = "MR-";
 
   private static final Logger LOG = LoggerFactory.getLogger(KijiModelRepository.class);
 
   /**
    * The latest layout file. This will have to be updated when we make a change to the
    * model repository table layout. Was hoping to inspect the contents of
    * org.kiji.modelrepo.layouts/*.json but doing this in a jar doesn't seem very straightforward.
    **/
   private static final String LATEST_LAYOUT_FILE=
       TABLE_LAYOUT_BASE_PKG + "/model-repo-layout-MR-1.json";
 
   static {
     /*
      * Go through all the JSON files in org/kiji/modelrepo/layouts and store each
      */
     try {
       mLatestLayout = KijiTableLayout
           .createFromEffectiveJson(KijiModelRepository
               .class.getResourceAsStream(LATEST_LAYOUT_FILE));
       String tableLayoutId = mLatestLayout.getDesc().getLayoutId();
       mLatestLayoutVersion = Integer.parseInt(tableLayoutId
           .substring(REPO_LAYOUT_VERSION_PREFIX.length()));
     } catch (IOException ioe) {
       LOG.error("Error opening file ", ioe);
     }
   }
 
   /**
    * Opens a KijiModelRepository for a given kiji using the default model repository table name.
    * This method should be matched with a call to {@link #close}.
    *
    * @param kiji The kiji instance to use.
    * @return An opened KijiModelRepository.
    * @throws IOException If there is an error opening the table or the table is not a valid
    *         model repository table.
    */
   public static KijiModelRepository open(Kiji kiji) throws IOException {
     return new KijiModelRepository(kiji);
   }
 
   /**
    * Private constructor that opens a new KijiModelRepository, creating it if necessary.
    * This method also updates an existing layout to the latest layout for the job
    * history table.
    *
    * @param kiji The kiji instance to retrieve the job history table from.
    * @throws IOException If there's an error opening the underlying HBaseKijiTable.
    */
   private KijiModelRepository(Kiji kiji) throws IOException {
     if (!isModelRepoTable(kiji)) {
       throw new IOException(MODEL_REPO_TABLE_NAME + " is not a valid model repository table.");
     }
 
     mKijiTable = kiji.openTable(MODEL_REPO_TABLE_NAME);
     mKijiMetaTable = kiji.getMetaTable();
 
     mCurrentModelRepoVersion = getCurrentVersion(mKijiMetaTable);
   }
 
   @Override
   public void close() throws IOException {
     mKijiTable.release();
   }
 
   /**
    * Returns the current version of the model repository.
    *
    * @return the current version of the model repository.
    */
   public int getCurrentVersion() {
     return mCurrentModelRepoVersion;
   }
 
   /**
    * Install the model repository table into a Kiji instance. Will use the latest version
    * of the layouts as the table's layout.
    *
    * @param kiji is the Kiji instance to install this table in.
    * @param baseStorageURI is the base URI of the storage layer.
    * @throws IOException If there is an error.
    */
   public static void install(Kiji kiji, URI baseStorageURI) throws IOException {
 
     // Few possibilities:
     // 1) The tableName doesn't exist at all ==> Create new table and set meta information
     // 2) The tableName exists and is a model-repo table ==> Do nothing or upgrade.
     // 3) The tableName exists and is not a model-repo table ==> Error.
 
     if (mLatestLayout == null) {
       throw new IOException("Unable to upgrade. Latest layout information is null.");
     }
 
     if (!kiji.getTableNames().contains(MODEL_REPO_TABLE_NAME)) {
       TableLayoutDesc tableLayout = mLatestLayout.getDesc();
       tableLayout.setReferenceLayout(null);
       tableLayout.setName(MODEL_REPO_TABLE_NAME);
       kiji.createTable(tableLayout);
 
       // Set the version
       writeLatestVersion(kiji.getMetaTable());
 
       // Set the base URI
       kiji.getMetaTable().putValue(MODEL_REPO_TABLE_NAME, REPO_BASE_URL_KEY,
           baseStorageURI.toString().getBytes());
     } else if (isModelRepoTable(kiji)) {
       // Do an upgrade possibly.
       int currentVersion = getCurrentVersion(kiji.getMetaTable());
       doUpgrade(kiji, currentVersion);
     } else {
       throw new IOException("Can not install model repository in table "
           + MODEL_REPO_TABLE_NAME + ".");
     }
   }
 
   /**
    * Write the latest version of the model repository into the Kiji metadata table.
    *
    * @param metaTable is the Kiji metadata table.
    * @throws IOException if there is an exception writing the latest layout to the metatable.
    */
   private static void writeLatestVersion(KijiMetaTable metaTable)
       throws IOException {
     ByteBuffer buf = ByteBuffer.allocate(Integer.SIZE);
     buf.putInt(mLatestLayoutVersion);
     metaTable.putValue(MODEL_REPO_TABLE_NAME, REPO_VERSION_KEY, buf.array());
   }
 
   /**
    * Retrieves the latest model repository version.
    *
    * @param metaTable is the Kiji metadata table.
    * @return the current version of the model repository.
    * @throws IOException if there is an exception retrieving the current model repo version.
    */
   private static int getCurrentVersion(KijiMetaTable metaTable)
       throws IOException {
     ByteBuffer buf = ByteBuffer.wrap(metaTable.getValue(MODEL_REPO_TABLE_NAME, REPO_VERSION_KEY));
     return buf.getInt();
   }
 
   /**
    * Performs the upgrade of the model repository. Will apply the latest layout to the
    * model repository table.
    *
    * @param kiji is the kiji instance.
    * @param fromVersion is the previous version of the repository.
    * @throws IOException if there is an exception performing the upgrade.
    */
   private static void doUpgrade(Kiji kiji, int fromVersion)
       throws IOException {
 
     if (mLatestLayout == null) {
       throw new IOException("Unable to upgrade. Latest layout information is null.");
     }
     if (fromVersion != mLatestLayoutVersion) {
       // Apply the latest layout and set the reference layout to the previous known version.
       TableLayoutDesc newLayoutDesc = mLatestLayout.getDesc();
       newLayoutDesc.setName(MODEL_REPO_TABLE_NAME);
       newLayoutDesc.setReferenceLayout(REPO_LAYOUT_VERSION_PREFIX + Integer.toString(fromVersion));
       kiji.modifyTableLayout(newLayoutDesc);
       writeLatestVersion(kiji.getMetaTable());
     }
   }
 
   /**
    * Upgrade the model repository by applying the latest layout.
    *
    * @param kiji is the Kiji instance.
    * @throws IOException if there is an exception upgrading the model repository.
    */
   public static void upgrade(Kiji kiji) throws IOException {
    Preconditions.checkNotNull(mLatestLayout,
       "Unable to upgrade. Latest layout information is null.");
 
     if (isModelRepoTable(kiji)) {
       int fromVersion = getCurrentVersion(kiji.getMetaTable());
       doUpgrade(kiji, fromVersion);
     } else {
       throw new IOException(MODEL_REPO_TABLE_NAME + " is not a valid model repository table.");
     }
   }
 
   /**
    * Deletes the model repository.
    *
    * @param kiji is the instance in which the repo resides.
    * @throws IOException if delete cannot be correctly performed.
    */
   public static void delete(Kiji kiji) throws IOException {
 
     // Similarly to the install tool, there are a few possibilities:
     // 1) The tableName doesn't exist at all ==> throw Exception.
     // 2) The tableName exists and is a model-repo table ==> delete table.
     // 3) The tableName exists and is not a model-repo table ==> throw Exception.
 
     if (!kiji.getTableNames().contains(MODEL_REPO_TABLE_NAME)) {
       throw new IOException("Model repository which is to be deleted "
           + MODEL_REPO_TABLE_NAME
           + " does not exist in instance "
           + kiji.getURI() + ".");
     } else if (isModelRepoTable(kiji)) {
       LOG.info("Deleting model repository table...");
       kiji.deleteTable(MODEL_REPO_TABLE_NAME);
       // Remove metadata keys.
       LOG.info("Removing model repository keys from metadata table...");
       kiji.getMetaTable().removeValues(MODEL_REPO_TABLE_NAME, REPO_BASE_URL_KEY);
       kiji.getMetaTable().removeValues(MODEL_REPO_TABLE_NAME, REPO_VERSION_KEY);
     } else {
       throw new IOException("Expected model repository table is not a valid model repository table "
           + MODEL_REPO_TABLE_NAME + ".");
     }
   }
 
   /**
    * Determines whether or not the model repository table is a valid table (For safety).
    *
    * @param kiji is the Kiji instance in which the model repository resides.
    * @return whether or not the model repository table is valid.
    * @throws IOException if there is an exception reading any information from the Kiji
    *         instance or the metadata table.
    */
   private static boolean isModelRepoTable(Kiji kiji) throws IOException {
     // Checks the instance metadata table for the model repo keys and that the kiji instance has
     // a model repository.
     if (!kiji.getTableNames().contains(MODEL_REPO_TABLE_NAME)) {
       return false;
     }
     try {
       kiji.getMetaTable().getValue(MODEL_REPO_TABLE_NAME, REPO_BASE_URL_KEY);
       kiji.getMetaTable().getValue(MODEL_REPO_TABLE_NAME, REPO_VERSION_KEY);
       return true;
     } catch (IOException ioe) {
       // Means that the key doesn't exist (or something else bad happened).
       // TODO: Once SCHEMA-507 is patched to return null on getValue() not existing, then
       // we can change this OR if an exists() method is added on the MetaTable intf.
       if (ioe.getMessage() != null
           && ioe.getMessage().contains("Could not find any values associated with table")) {
         return false;
       } else {
         throw ioe;
       }
     }
   }
 
   /**
    * Deploys a new model lifecycle or update an existing model lifecycle.
    *
    * @param name that identifies this model lifecycle
    * @param version of this particular instance of a model lifecycle
    * @param definition AvroModelDefinition of model lifecycle
    * @param environment AvroModelEnvironment of model lifecycle
    * @param productionReady is true iff model lifecycle is ready for scoring
    * @param message (optional) latest update message of the model lifecycle
    * @throws IOException if model lifecycle cannot be written to model repository table.
    */
   public void deployModelLifecycle(
       final String name,
       final ProtocolVersion version,
       final AvroModelDefinition definition,
       final AvroModelEnvironment environment,
       final boolean productionReady,
       final String message
       ) throws IOException {
     final AtomicKijiPutter putter = mKijiTable.getWriterFactory().openAtomicPutter();
     final EntityId eid = mKijiTable.getEntityId(name, version.toCanonicalString());
     try {
       putter.begin(eid);
       putter.put("model", "definition", definition);
       putter.put("model", "environment", environment);
       putter.put("model", "production_ready", productionReady);
       if (null != message) {
         putter.put("model", "message", message);
       }
       putter.commit();
     } finally {
       putter.close();
     }
   }
 }
