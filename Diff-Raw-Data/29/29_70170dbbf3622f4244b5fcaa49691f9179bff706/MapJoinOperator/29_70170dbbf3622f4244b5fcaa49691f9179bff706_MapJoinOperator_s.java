 /**
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
 
 package org.apache.hadoop.hive.ql.exec;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.List;
 import java.util.Random;
 import java.util.Vector;
 import java.util.ArrayList;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.hive.conf.HiveConf;
 import org.apache.hadoop.hive.ql.metadata.HiveException;
 import org.apache.hadoop.hive.ql.plan.mapJoinDesc;
 import org.apache.hadoop.hive.ql.plan.tableDesc;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hive.serde2.SerDe;
 import org.apache.hadoop.hive.serde2.SerDeException;
 import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
 import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils;
 import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspectorUtils.ObjectInspectorCopyOption;
 import org.apache.hadoop.mapred.Reporter;
 import org.apache.hadoop.util.ReflectionUtils;
 import org.apache.hadoop.hive.ql.util.jdbm.htree.HTree;
 import org.apache.hadoop.hive.ql.util.jdbm.RecordManager;
 import org.apache.hadoop.hive.ql.util.jdbm.RecordManagerFactory;
 import org.apache.hadoop.hive.ql.util.jdbm.RecordManagerOptions;
 
 /**
  * Map side Join operator implementation.
  */
 public class MapJoinOperator extends CommonJoinOperator<mapJoinDesc> implements Serializable {
   private static final long serialVersionUID = 1L;
   static final private Log LOG = LogFactory.getLog(MapJoinOperator.class.getName());
 
   /**
    * The expressions for join inputs's join keys.
    */
   transient protected Map<Byte, List<ExprNodeEvaluator>> joinKeys;
   /**
    * The ObjectInspectors for the join inputs's join keys.
    */
   transient protected Map<Byte, List<ObjectInspector>> joinKeysObjectInspectors;
   /**
    * The standard ObjectInspectors for the join inputs's join keys.
    */
   transient protected Map<Byte, List<ObjectInspector>> joinKeysStandardObjectInspectors;
 
   transient private int posBigTable;       // one of the tables that is not in memory
   transient int mapJoinRowsKey;            // rows for a given key
   
   transient protected Map<Byte, HTree> mapJoinTables;
 
   public static class MapJoinObjectCtx {
     ObjectInspector standardOI;
     SerDe      serde;
     
     /**
      * @param standardOI
      * @param serde
      */
     public MapJoinObjectCtx(ObjectInspector standardOI, SerDe serde) {
       this.standardOI = standardOI;
       this.serde = serde;
     }
     
     /**
      * @return the standardOI
      */
     public ObjectInspector getStandardOI() {
       return standardOI;
     }
 
     /**
      * @return the serde
      */
     public SerDe getSerDe() {
       return serde;
     }
 
   }
 
   transient static Map<Integer, MapJoinObjectCtx> mapMetadata = new HashMap<Integer, MapJoinObjectCtx>();
   transient static int nextVal = 0;
   
   static public Map<Integer, MapJoinObjectCtx> getMapMetadata() {
     return mapMetadata;
   }
   
   transient boolean firstRow;
   
   transient int   metadataKeyTag;
   transient int[] metadataValueTag;
   transient List<File> hTables;
   
   @Override
   public void initializeOp(Configuration hconf, Reporter reporter, ObjectInspector[] inputObjInspector) throws HiveException {
     super.initializeOp(hconf, reporter, inputObjInspector);
     firstRow = true;
     try {
       joinKeys  = new HashMap<Byte, List<ExprNodeEvaluator>>();
       
       populateJoinKeyValue(joinKeys, conf.getKeys());
       joinKeysObjectInspectors = getObjectInspectorsFromEvaluators(joinKeys, inputObjInspector);
       joinKeysStandardObjectInspectors = getStandardObjectInspectors(joinKeysObjectInspectors); 
         
       // all other tables are small, and are cached in the hash table
       posBigTable = conf.getPosBigTable();
 
       metadataValueTag = new int[numAliases];
       for (int pos = 0; pos < numAliases; pos++)
         metadataValueTag[pos] = -1;
       
       mapJoinTables = new HashMap<Byte, HTree>();
       hTables = new ArrayList<File>();
       
       // initialize the hash tables for other tables
       for (int pos = 0; pos < numAliases; pos++) {
         if (pos == posBigTable)
           continue;
         
         Properties props = new Properties();
         props.setProperty(RecordManagerOptions.CACHE_SIZE, 
           String.valueOf(HiveConf.getIntVar(hconf, HiveConf.ConfVars.HIVEMAPJOINCACHEROWS)));
         
         Random rand = new Random();
         File newDir = new File("/tmp/" + rand.nextInt());
         String newDirName = null;
         while (true) {
           if (newDir.mkdir()) {
             newDirName = newDir.getAbsolutePath();
             hTables.add(newDir);
             break;
           }
           newDir = new File("/tmp" + rand.nextInt());
         }
         
         RecordManager recman = RecordManagerFactory.createRecordManager(newDirName + "/" + pos, props );
         HTree hashTable = HTree.createInstance(recman);
         
         mapJoinTables.put(Byte.valueOf((byte)pos), hashTable);
       }
 
       storage.put((byte)posBigTable, new Vector<ArrayList<Object>>());
       
       mapJoinRowsKey = HiveConf.getIntVar(hconf, HiveConf.ConfVars.HIVEMAPJOINROWSIZE);
       
       initializeChildren(hconf, reporter, new ObjectInspector[]{joinOutputObjectInspector});
     } catch (IOException e) {
       e.printStackTrace();
       throw new HiveException(e);
     }
   }
 
   @Override
   public void process(Object row, ObjectInspector rowInspector, int tag) throws HiveException {
     try {
 
       // get alias
       alias = (byte)tag;
 
       if ((lastAlias == null) || (!lastAlias.equals(alias)))
         nextSz = joinEmitInterval;
       
       // compute keys and values as StandardObjects     
       ArrayList<Object> key   = computeValues(row, joinKeys.get(alias), joinKeysObjectInspectors.get(alias));
       ArrayList<Object> value = computeValues(row, joinValues.get(alias), joinValuesObjectInspectors.get(alias));
 
       // does this source need to be stored in the hash map
       if (tag != posBigTable) {
         if (firstRow) {
           metadataKeyTag = nextVal++;
           
           tableDesc keyTableDesc = conf.getKeyTblDesc();
           SerDe keySerializer = (SerDe)ReflectionUtils.newInstance(keyTableDesc.getDeserializerClass(), null);
           keySerializer.initialize(null, keyTableDesc.getProperties());
 
           mapMetadata.put(Integer.valueOf(metadataKeyTag), 
               new MapJoinObjectCtx(
                   ObjectInspectorUtils.getStandardObjectInspector(keySerializer.getObjectInspector(),
                       ObjectInspectorCopyOption.WRITABLE),
                   keySerializer));
           
           firstRow = false;
         }
         
         HTree hashTable = mapJoinTables.get(alias);
         MapJoinObjectKey keyMap = new MapJoinObjectKey(metadataKeyTag, key);
         MapJoinObjectValue o = (MapJoinObjectValue)hashTable.get(keyMap);
         Vector<ArrayList<Object>> res = null;
         
         if (o == null) {
           res = new Vector<ArrayList<Object>>();
         }
         else {
           res = o.getObj();
         }
         
         res.add(value);
   
         if (metadataValueTag[tag] == -1) {
           metadataValueTag[tag] = nextVal++;
                     
           tableDesc valueTableDesc = conf.getValueTblDescs().get(tag);
           SerDe valueSerDe = (SerDe)ReflectionUtils.newInstance(valueTableDesc.getDeserializerClass(), null);
           valueSerDe.initialize(null, valueTableDesc.getProperties());
  
           mapMetadata.put(Integer.valueOf(metadataValueTag[tag]),
               new MapJoinObjectCtx(
                   ObjectInspectorUtils.getStandardObjectInspector(valueSerDe.getObjectInspector(),
                       ObjectInspectorCopyOption.WRITABLE),
               valueSerDe));
         }
         
         // Construct externalizable objects for key and value
         MapJoinObjectKey keyObj = new MapJoinObjectKey(metadataKeyTag, key);
         MapJoinObjectValue valueObj = new MapJoinObjectValue(metadataValueTag[tag], res);
 
         if (res.size() > 1)
           hashTable.remove(keyObj);
 
         // This may potentially increase the size of the hashmap on the mapper
         if (res.size() > mapJoinRowsKey) {
           LOG.warn("Number of values for a given key " + keyObj + " are " + res.size());
         }
         
         hashTable.put(keyObj, valueObj);
         return;
       }
 
       // Add the value to the vector
       storage.get(alias).add(value);
 
       for (Byte pos : order) {
         if (pos.intValue() != tag) {
           MapJoinObjectKey keyMap = new MapJoinObjectKey(metadataKeyTag, key);
           MapJoinObjectValue o = (MapJoinObjectValue)mapJoinTables.get(pos).get(keyMap);
 
           if (o == null) {
             storage.put(pos, new Vector<ArrayList<Object>>());
           }
           else {
             storage.put(pos, o.getObj());
           }
         }
       }
       
       // generate the output records
       checkAndGenObject();
     
       // done with the row
       storage.get(alias).clear();
 
       for (Byte pos : order)
         if (pos.intValue() != tag)
           storage.put(pos, null);
     
     } catch (SerDeException e) {
       e.printStackTrace();
       throw new HiveException(e);
     } catch (IOException e) {
       e.printStackTrace();
       throw new HiveException(e);
     }
   }
   
   /**
    * Implements the getName function for the Node Interface.
    * @return the name of the operator
    */
   public String getName() {
     return "MAPJOIN";
   }
   
   public void close(boolean abort) throws HiveException {
     for (File hTbl : hTables) {
       deleteDir(hTbl);
     }
     super.close(abort);
   }
   
   private void deleteDir(File dir) {
     if (dir.isDirectory()) {
       String[] children = dir.list();
       for (int i = 0; i < children.length; i++) {
         deleteDir(new File(dir, children[i]));
       }
     }
 
     dir.delete();
   }
 }
