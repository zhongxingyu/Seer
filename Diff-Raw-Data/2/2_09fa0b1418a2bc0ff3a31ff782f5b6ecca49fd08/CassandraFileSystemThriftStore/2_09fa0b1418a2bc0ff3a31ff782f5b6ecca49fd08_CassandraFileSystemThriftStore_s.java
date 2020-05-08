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
 package org.apache.cassandra.hadoop.fs;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.nio.ByteBuffer;
 import java.util.*;
 
 import com.datastax.brisk.BriskInternalServer;
 
 import org.apache.cassandra.hadoop.CassandraProxyClient;
 import org.apache.cassandra.hadoop.trackers.CassandraJobConf;
 import org.apache.cassandra.thrift.*;
 import org.apache.cassandra.utils.ByteBufferUtil;
 import org.apache.cassandra.utils.FBUtilities;
 import org.apache.cassandra.utils.UUIDGen;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.BlockLocation;
 import org.apache.hadoop.fs.Path;
 import org.apache.log4j.Logger;
 import org.apache.thrift.TException;
 
 public class CassandraFileSystemThriftStore implements CassandraFileSystemStore
 {
     private final static Logger logger = Logger.getLogger(CassandraFileSystemThriftStore.class);     
     
     private static final String         keySpace         = "cfs";
     private static final String         inodeCf          = "inode";
     private static final String         blockCf          = "blocks";
 
     private static final ByteBuffer     dataCol          = ByteBufferUtil.bytes("data");
     private static final ByteBuffer     pathCol          = ByteBufferUtil.bytes("path");
    private static final ByteBuffer     sentCol          = ByteBufferUtil.bytes("sentinal");
 
     private static final ColumnPath     blockPath        = new ColumnPath(blockCf);
     private static final ColumnParent   blockParent      = new ColumnParent(blockCf);
 
     private static final ColumnPath     inodePath        = new ColumnPath(inodeCf);
     private static final ColumnParent   inodeParent      = new ColumnParent(inodeCf);
 
     private static final ColumnPath     inodeDataPath    = new ColumnPath(inodeCf).setColumn(dataCol);
     private static final ColumnPath     blockDataPath    = new ColumnPath(blockCf).setColumn(dataCol);
 
     private static final SlicePredicate pathPredicate    = new SlicePredicate().setColumn_names(Arrays.asList(pathCol));
 
     private static final ByteBuffer     sentinelValue    = ByteBufferUtil.bytes("x");
 
     private ConsistencyLevel            consistencyLevel = ConsistencyLevel.QUORUM;   // default
 
     private Cassandra.Iface       client;
 
     public CassandraFileSystemThriftStore()
     {
         
     }
 
     public void initialize(URI uri, Configuration conf) throws IOException
     {
 
         String host = uri.getHost();
         int    port = uri.getPort();
         
         if(host == null || host.isEmpty())
             throw new IOException("Invalid URI: "+uri);
         
         if(port == -1)
             port = 9160; //default
         
         //We could be running inside of cassandra...
         if(conf instanceof CassandraJobConf)
             client = new BriskInternalServer();
         else
             client = CassandraProxyClient.newProxyConnection(host, port, true, true);
         
         KsDef ks = checkKeyspace();
 
         if (ks == null)
             ks = createKeySpace();
 
         // Change consistency if this using NTS
         if (ks.getStrategy_class().contains("NetworkTopologyStrategy"))
             consistencyLevel = ConsistencyLevel.LOCAL_QUORUM;
 
         try
         {
             client.set_keyspace(keySpace);
         }
         catch (Exception e)
         {
             throw new IOException(e);
         }
 
     }
 
     private KsDef checkKeyspace() throws IOException
     {
         try
         {
             return client.describe_keyspace(keySpace);
         }
         catch (NotFoundException e)
         {
             return null;
         }
         catch (InvalidRequestException e)
         {
             throw new IOException(e);
         }
         catch (TException e)
         {
             throw new IOException(e);
         }
     }
 
     public KsDef createKeySpace() throws IOException
     {
         try
         {
             // Stagger create time so nodes don't
             // get confused
             Thread.sleep(new Random().nextInt(5000));
 
             KsDef cfsKs = checkKeyspace();
 
             if (cfsKs != null)
                 return cfsKs;
 
             List<CfDef> cfs = new ArrayList<CfDef>();
 
             CfDef cf = new CfDef();
             cf.setName(inodeCf);
             cf.setComparator_type("BytesType");
             cf.setKey_cache_size(0);
             cf.setRow_cache_size(0);
             cf.setComment("Stores file meta data");
             cf.setKeyspace(keySpace);
 
             cf.setColumn_metadata(Arrays.asList(new ColumnDef(pathCol, "BytesType").setIndex_type(IndexType.KEYS)
                     .setIndex_name("path"), new ColumnDef(sentCol, "BytesType").setIndex_type(IndexType.KEYS)
                     .setIndex_name("sentinel")));
 
             cfs.add(cf);
 
             cf = new CfDef();
             cf.setName(blockCf);
             cf.setComparator_type("BytesType");
             cf.setKey_cache_size(0);
             cf.setRow_cache_size(0);
             cf.setComment("Stores blocks of information associated with a inode");
             cf.setKeyspace(keySpace);
 
             cfs.add(cf);
 
             cfsKs = new KsDef().setName(keySpace).setReplication_factor(1).setStrategy_class(
                     "org.apache.cassandra.locator.SimpleStrategy").setCf_defs(cfs);
 
             client.system_add_keyspace(cfsKs);
 
             return cfsKs;
         }
         catch (Exception e)
         {
             throw new IOException(e);
         }
 
     }
 
     public InputStream retrieveBlock(Block block, long byteRangeStart) throws IOException
     {
         ByteBuffer blockId = getBlockKey(block.id);
 
         ColumnOrSuperColumn blockData = null;
 
         try
         {
             blockData = client.get(blockId, blockDataPath, consistencyLevel);
         }
         catch (Exception e)
         {
             throw new IOException(e);
         }
 
         if (blockData == null || blockData.column == null)
             throw new IOException("Missing block: " + block.id);
 
         InputStream is = ByteBufferUtil.inputStream(blockData.column.value);
 
         is.skip(byteRangeStart);
 
         return is;
     }
 
     public INode retrieveINode(Path path) throws IOException
     {
         ByteBuffer pathKey = getPathKey(path);
         ColumnOrSuperColumn pathInfo;
 
         try
         {
             pathInfo = client.get(pathKey, inodeDataPath, consistencyLevel);
         }
         catch (NotFoundException e)
         {
             return null;
         }
         catch (Exception e)
         {
             throw new IOException(e);
         }
 
         return INode.deserialize(ByteBufferUtil.inputStream(pathInfo.column.value));
     }
 
     public void storeBlock(Block block, ByteArrayOutputStream os) throws IOException
     {
         ByteBuffer blockId = getBlockKey(block.id);
 
         ByteBuffer data = ByteBuffer.wrap(os.toByteArray());
 
         try
         {
             client.insert(blockId, blockParent, new Column(dataCol, data, System.currentTimeMillis()),
                             consistencyLevel);
         }
         catch (Exception e)
         {
             throw new IOException(e);
         }
     }
 
     public void storeINode(Path path, INode inode) throws IOException
     {
         logger.info("Writing inode to: "+path);
                 
         ByteBuffer pathKey = getPathKey(path);
 
         ByteBuffer data = inode.serialize();
 
         Map<ByteBuffer, Map<String, List<Mutation>>> mutationMap = new HashMap<ByteBuffer, Map<String, List<Mutation>>>();
         Map<String, List<Mutation>> pathMutations = new HashMap<String, List<Mutation>>();
         List<Mutation> mutations = new ArrayList<Mutation>();
 
         // setup mutation map
         pathMutations.put(inodeCf, mutations);
         mutationMap.put(pathKey, pathMutations);
 
         long ts = System.currentTimeMillis();
 
         // file name
         mutations.add(new Mutation().setColumn_or_supercolumn(new ColumnOrSuperColumn().setColumn(new Column(pathCol,
                 ByteBufferUtil.bytes(path.toUri().getPath()), ts))));
 
         // sentinal
         mutations.add(new Mutation().setColumn_or_supercolumn(new ColumnOrSuperColumn().setColumn(new Column(sentCol,
                 sentinelValue, ts))));
 
         // serialized inode
         mutations.add(new Mutation().setColumn_or_supercolumn(new ColumnOrSuperColumn().setColumn(new Column(dataCol,
                 data, ts))));
 
         try
         {
             client.batch_mutate(mutationMap, consistencyLevel);
         }
         catch (Exception e)
         {
             throw new IOException(e);
         }
     }
 
     ByteBuffer getPathKey(Path path)
     {
         return ByteBufferUtil.bytes(FBUtilities.hashToBigInteger(ByteBufferUtil.bytes(path.toUri().getPath()))
                 .toString(16));
     }
 
     ByteBuffer getBlockKey(UUID id)
     {
         return ByteBufferUtil.bytes(FBUtilities.bytesToHex(UUIDGen.decompose(id)));
     }
 
     public void deleteBlock(Block block) throws IOException
     {
 
         try
         {
             client.remove(ByteBuffer.wrap(UUIDGen.decompose(block.id)), blockPath, System.currentTimeMillis(),
                     consistencyLevel);
         }
         catch (Exception e)
         {
             throw new IOException(e);
         }
 
     }
 
     public void deleteINode(Path path) throws IOException
     {
         try
         {
             client.remove(getPathKey(path), inodePath, System.currentTimeMillis(), consistencyLevel);
         }
         catch (Exception e)
         {
             throw new IOException(e);
         }
     }
 
     public Set<Path> listDeepSubPaths(Path path) throws IOException
     {
         String startPath = path.toUri().getPath();
 
         List<IndexExpression> indexExpressions = new ArrayList<IndexExpression>();
 
         indexExpressions.add(new IndexExpression(sentCol, IndexOperator.EQ, sentinelValue));
         indexExpressions.add(new IndexExpression(pathCol, IndexOperator.GTE, ByteBufferUtil.bytes(startPath)));
 
         // Limit listings to this root by incrementing the last char
         if (startPath.length() > 1)
         {
             String endPath = startPath.substring(0, startPath.length() - 1)
                     + new Character((char) (startPath.charAt(startPath.length() - 1) + 1));
 
             indexExpressions.add(new IndexExpression(pathCol, IndexOperator.LT, ByteBufferUtil.bytes(endPath)));
         }
 
         try
         {
             List<KeySlice> keys = client.get_indexed_slices(inodeParent, new IndexClause(indexExpressions,
                     ByteBufferUtil.EMPTY_BYTE_BUFFER, 100000), pathPredicate, consistencyLevel);
 
             Set<Path> matches = new HashSet<Path>(keys.size());
 
             for (KeySlice key : keys)
             {
                 for (ColumnOrSuperColumn cosc : key.getColumns())
                 {
                     matches.add(new Path(ByteBufferUtil.string(cosc.column.value)));
                 }
             }
 
             return matches;
         }
         catch (Exception e)
         {
             throw new IOException(e);
         }
     }
 
     public Set<Path> listSubPaths(Path path) throws IOException
     {
         Set<Path> allPaths = listDeepSubPaths(path);
         Set<Path> prunedPath = new HashSet<Path>();
 
         for (Path p : allPaths)
         {
             if (p.depth() == (path.depth() + 1) )
             {
                 prunedPath.add(p);
             }
         }
 
         return prunedPath;
     }
 
     public String getVersion() throws IOException
     {
         return "Cassandra FileSystem Thrift Store";
     }
 
     public BlockLocation[] getBlockLocation(List<Block> blocks) throws IOException
     {
         if (blocks.isEmpty())
             return new BlockLocation[0];
 
         List<ByteBuffer> blockKeys = new ArrayList<ByteBuffer>(blocks.size());
 
         for (Block b : blocks)
             blockKeys.add(getBlockKey(b.id));
 
         BlockLocation[] locations = new BlockLocation[blocks.size()];
 
         try
         {
             List<List<String>> blockEndpoints = ((Brisk.Iface)client).describe_keys(keySpace, blockKeys);
 
             for (int i = 0; i < blockEndpoints.size(); i++)
             {
                 List<String> endpoints = blockEndpoints.get(i);
                 Block b = blocks.get(i);
 
                 // TODO: Add topology info if at all possible?
                 locations[i] = new BlockLocation(null, endpoints.toArray(new String[0]), b.offset, b.length);
             }
 
             return locations;
         }
         catch (TException e)
         {
             throw new IOException(e);
         }
 
     }
 }
