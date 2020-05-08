 /**
  * Copyright (c) MuleSoft, Inc. All rights reserved. http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.md file.
  */
 
 package org.mule.module.hbase.api.impl;
 
 import org.mule.module.hbase.api.BloomFilterType;
 import org.mule.module.hbase.api.ByteArrayConverter;
 import org.mule.module.hbase.api.CompressionType;
 import org.mule.module.hbase.api.HBaseService;
 import org.mule.module.hbase.api.HBaseServiceException;
 
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.UnhandledException;
 import org.apache.commons.lang.Validate;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.HColumnDescriptor;
 import org.apache.hadoop.hbase.HConstants;
 import org.apache.hadoop.hbase.HTableDescriptor;
 import org.apache.hadoop.hbase.MasterNotRunningException;
 import org.apache.hadoop.hbase.TableNotFoundException;
 import org.apache.hadoop.hbase.ZooKeeperConnectionException;
 import org.apache.hadoop.hbase.client.Delete;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HBaseAdmin;
 import org.apache.hadoop.hbase.client.HConnectionManager;
 import org.apache.hadoop.hbase.client.HTableFactory;
 import org.apache.hadoop.hbase.client.HTableInterface;
 import org.apache.hadoop.hbase.client.HTableInterfaceFactory;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.RowLock;
 import org.apache.hadoop.hbase.client.Scan;
 
 /**
  * {@link HBaseService} that uses the official RPC client to connect with the
  * database. <br>
  * <br>
  * <strong>Important</strong> It requires HBase >= 0.90.3-SNAPSHOT because of this
  * two issues:
  * <ul>
  * <li><a href="https://issues.apache.org/jira/browse/HBASE-3712">
  * https://issues.apache.org/jira/browse/HBASE-3712</a></li>
  * <li><a href="https://issues.apache.org/jira/browse/HBASE-3734">
  * https://issues.apache.org/jira/browse/HBASE-3734</a></li>
  * </ul>
  * 
  * @author Pablo Martin Grigolatto
  * @since Apr 11, 2011
  */
 public class RPCHBaseService implements HBaseService
 {
 
     private static final Charset UTF8 = Charset.forName("utf-8");
     private static final ByteArrayConverter BYTE_ARRAY_CONVERTER = new ByteArrayConverter(UTF8);
     private HTableInterfaceFactory hTableInterfaceFactory;
     private Configuration configuration;
 
     public RPCHBaseService()
     {
         hTableInterfaceFactory = new HTableFactory();
         configuration = HBaseConfiguration.create();
     }
 
     // ------------ Admin Operations
     /** @see HBaseService#alive() */
     public boolean alive()
     {
         try
         {
             return doWithHBaseAdmin(new AdminCallback<Boolean>()
             {
                 public Boolean doWithHBaseAdmin(HBaseAdmin hBaseAdmin)
                 {
                     try
                     {
                         return hBaseAdmin.isMasterRunning();
                     }
                     catch (MasterNotRunningException e)
                     {
                         return false;
                     }
                     catch (ZooKeeperConnectionException e)
                     {
                         return false;
                     }
                     catch (HBaseServiceException e)
                     {
                         return false;
                     }
                 }
             });
         }
         catch (Exception e)
         {
             return false;
         }
     }
 
     /** @see HBaseService#createTable(String) */
     public void createTable(final String name)
     {
         doWithHBaseAdmin(new AdminCallback<Void>()
         {
             public Void doWithHBaseAdmin(HBaseAdmin hBaseAdmin)
             {
                 try
                 {
                     hBaseAdmin.createTable(new HTableDescriptor(name));
                     doFlush(hBaseAdmin, name);
                     return null;
                 }
                 catch (IOException e)
                 {
                     throw new HBaseServiceException(e);
                 }
             }
         });
     }
 
     /** @see HBaseService#existsTable(String) */
     public boolean existsTable(final String name)
     {
         return doWithHBaseAdmin(new AdminCallback<Boolean>()
         {
             public Boolean doWithHBaseAdmin(HBaseAdmin hBaseAdmin)
             {
                 try
                 {
                     return hBaseAdmin.getTableDescriptor(name.getBytes(UTF8)) != null;
                 }
                 catch (TableNotFoundException e)
                 {
                     return false;
                 }
                 catch (IOException e)
                 {
                     throw new HBaseServiceException(e);
                 }
             }
         });
     }
 
     /** @see HBaseService#deleteTable(String) */
     public void deleteTable(final String name)
     {
         doWithHBaseAdmin(new AdminCallback<Void>()
         {
             public Void doWithHBaseAdmin(HBaseAdmin hBaseAdmin)
             {
                 try
                 {
                     hBaseAdmin.disableTable(name);
                     hBaseAdmin.deleteTable(name);
                     doFlush(hBaseAdmin, name);
                     return null;
                 }
                 catch (IOException e)
                 {
                     throw new HBaseServiceException(e);
                 }
             }
         });
     }
 
     /** @see HBaseService#isDisabledTable(String) */
     public boolean isDisabledTable(final String name)
     {
         return doWithHBaseAdmin(new AdminCallback<Boolean>()
         {
             public Boolean doWithHBaseAdmin(HBaseAdmin hBaseAdmin)
             {
                 try
                 {
                     return hBaseAdmin.isTableDisabled(name);
                 }
                 catch (IOException e)
                 {
                     throw new HBaseServiceException(e);
                 }
             }
         });
     }
 
     /** @see HBaseService#enableTable(String) */
     public void enableTable(final String name)
     {
         doWithHBaseAdmin(new AdminCallback<Void>()
         {
             public Void doWithHBaseAdmin(HBaseAdmin hBaseAdmin)
             {
                 try
                 {
                     hBaseAdmin.enableTable(name);
                     return null;
                 }
                 catch (IOException e)
                 {
                     throw new HBaseServiceException(e);
                 }
             }
         });
     }
 
     /** @see HBaseService#disabeTable(String) */
     public void disabeTable(final String name)
     {
         doWithHBaseAdmin(new AdminCallback<Void>()
         {
             public Void doWithHBaseAdmin(HBaseAdmin hBaseAdmin)
             {
                 try
                 {
                     hBaseAdmin.disableTable(name);
                     return null;
                 }
                 catch (IOException e)
                 {
                     throw new HBaseServiceException(e);
                 }
             }
         });
     }
 
     /** @see HBaseService#addColumn(String, String, Integer, Boolean, Integer) */
     public void addColumn(final String name,
                           final String someColumnFamilyName,
                           final Integer maxVersions,
                           final Boolean inMemory,
                           final Integer scope)
     {
         doWithHBaseAdmin(new AdminCallback<Void>()
         {
             public Void doWithHBaseAdmin(HBaseAdmin hBaseAdmin)
             {
                 final HColumnDescriptor descriptor = new HColumnDescriptor(someColumnFamilyName);
                 if (maxVersions != null)
                 {
                     descriptor.setMaxVersions(maxVersions);
                 }
                 if (inMemory != null)
                 {
                     descriptor.setInMemory(inMemory);
                 }
                 if (scope != null)
                 {
                     descriptor.setScope(scope);
                 }
                 try
                 {
                     hBaseAdmin.disableTable(name);
                     hBaseAdmin.addColumn(name, descriptor);
                     hBaseAdmin.enableTable(name);
                     doFlush(hBaseAdmin, name);
                     return null;
                 }
                 catch (IOException e)
                 {
                     throw new HBaseServiceException(e);
                 }
             }
         });
     }
 
     /** @see HBaseService#existsColumn(String, String) */
     public boolean existsColumn(String tableName, final String columnFamilyName)
     {
         return doWithHTable(tableName, new TableCallback<Boolean>()
         {
             public Boolean doWithHBaseAdmin(HTableInterface hTable)
             {
                 try
                 {
                     return hTable.getTableDescriptor().getFamily(columnFamilyName.getBytes(UTF8)) != null;
                 }
                 catch (IOException e)
                 {
                     throw new HBaseServiceException(e);
                 }
             }
         });
     }
 
     /**
      * @see HBaseService#modifyColumn(String, String, Integer, Integer, String,
      *      String, Boolean, Integer, Boolean, String, Integer, Map)
      */
     public void modifyColumn(final String tableName,
                              final String columnFamilyName,
                              final Integer maxVersions,
                              final Integer blocksize,
                              final CompressionType compressionType,
                              final CompressionType compactionCompressionType,
                              final Boolean inMemory,
                              final Integer timeToLive,
                              final Boolean blockCacheEnabled,
                              final BloomFilterType bloomFilterType,
                              final Integer replicationScope,
                              final Map<String, String> values)
     {
 
         doWithHBaseAdmin(new AdminCallback<Void>()
         {
             public Void doWithHBaseAdmin(HBaseAdmin hBaseAdmin)
             {
                 try
                 {
                     HTableDescriptor otd = hBaseAdmin.getTableDescriptor(tableName.getBytes(UTF8));
                     HColumnDescriptor ocd = otd.getFamily(columnFamilyName.getBytes(UTF8));
                     HColumnDescriptor descriptor = new HColumnDescriptor(ocd);
                     loadPropertiesInDescriptor(descriptor, maxVersions, blocksize, compressionType,
                         compactionCompressionType, inMemory, timeToLive, blockCacheEnabled, bloomFilterType,
                         replicationScope, values);
                     hBaseAdmin.disableTable(tableName);
                     hBaseAdmin.modifyColumn(tableName, descriptor);
                     hBaseAdmin.enableTable(tableName);
                     doFlush(hBaseAdmin, tableName);
                     return null;
                 }
                 catch (IOException e)
                 {
                     throw new HBaseServiceException(e);
                 }
             }
 
             private void loadPropertiesInDescriptor(HColumnDescriptor descriptor,
                                                     Integer maxVersions,
                                                     Integer blocksize,
                                                     CompressionType compressionType,
                                                     CompressionType compactionCompressionType,
                                                     Boolean inMemory,
                                                     Integer timeToLive,
                                                     Boolean blockCacheEnabled,
                                                     BloomFilterType bloomFilterType,
                                                     Integer replicationScope,
                                                     Map<String, String> values)
             {
                 if (maxVersions != null)
                 {
                     descriptor.setMaxVersions(maxVersions);
                 }
                 if (blocksize != null)
                 {
                     descriptor.setBlocksize(blocksize);
                 }
                 if (compressionType != null)
                 {
                     descriptor.setCompressionType(compressionType.getAlgorithm());
                 }
                 if (compactionCompressionType != null)
                 {
                     descriptor.setCompactionCompressionType(compactionCompressionType.getAlgorithm());
                 }
                 if (inMemory != null)
                 {
                     descriptor.setInMemory(inMemory);
                 }
                 if (timeToLive != null)
                 {
                     descriptor.setTimeToLive(timeToLive);
                 }
                 if (blockCacheEnabled != null)
                 {
                     descriptor.setBlockCacheEnabled(blockCacheEnabled);
                 }
                 if (bloomFilterType != null)
                 {
                     descriptor.setBloomFilterType(bloomFilterType.getBloomType());
                 }
                 if (replicationScope != null)
                 {
                     descriptor.setScope(replicationScope);
                 }
                 if (values != null)
                 {
                     for (Entry<String, String> entry : values.entrySet())
                     {
                         descriptor.setValue(entry.getKey(), entry.getValue());
                     }
                 }
             }
         });
     }
 
     /** @see HBaseService#deleteColumn(String, String) */
     public void deleteColumn(final String tableName, final String columnFamilyName)
 
     {
         doWithHBaseAdmin(new AdminCallback<Void>()
         {
             public Void doWithHBaseAdmin(HBaseAdmin hBaseAdmin)
             {
                 try
                 {
                     hBaseAdmin.disableTable(tableName);
                     hBaseAdmin.deleteColumn(tableName, columnFamilyName);
                     hBaseAdmin.enableTable(tableName);
                     doFlush(hBaseAdmin, tableName);
                     return null;
                 }
                 catch (IOException e)
                 {
                     throw new HBaseServiceException(e);
                 }
             }
         });
     }
 
     // ------------ Row Operations
     /** @see HBaseService#get(String, String, Integer, Long) */
     public Result get(String tableName, final String rowKey, final Integer maxVersions, final Long timestamp)
 
     {
         return doWithHTable(tableName, new TableCallback<Result>()
         {
             public Result doWithHBaseAdmin(HTableInterface hTable) throws Exception
             {
                 return hTable.get(createGet(rowKey, maxVersions, timestamp));
             }
         });
     }
 
     /**
      * @see HBaseService#put(String, String, String, String, Long, String, Boolean,
      *      RowLock)
      */
     public void put(String tableName,
                     final String row,
                     final String columnFamilyName,
                     final String columnQualifier,
                     final Long timestamp,
                     final Object value,
                     final boolean writeToWAL,
                     final RowLock lock)
     {
         doWithHTable(tableName, new TableCallback<Void>()
         {
             public Void doWithHBaseAdmin(HTableInterface hTable) throws Exception
             {
                 final Put put = createPut(row, columnFamilyName, columnQualifier, timestamp, value,
                     writeToWAL, lock);
                 hTable.put(put);
                 return null;
             }
         });
     }
 
     /** @see HBaseService#exists(String, String, Integer, Long) */
     public boolean exists(String tableName, final String row, final Integer maxVersions, final Long timestamp)
 
     {
         final Result result = get(tableName, row, maxVersions, timestamp);
         return result != null && !result.isEmpty();
     }
 
     /**
      * @see HBaseService#delete(String, String, String, String, Long, Boolean,
      *      RowLock)
      */
     public void delete(final String tableName,
                        final String row,
                        final String columnFamilyName,
                        final String columnQualifier,
                        final Long timestamp,
                        final boolean deleteAllVersions,
                        final RowLock lock)
     {
         doWithHTable(tableName, new TableCallback<Void>()
         {
             public Void doWithHBaseAdmin(HTableInterface hTable) throws Exception
             {
                 final Delete delete = createDelete(row, columnFamilyName, columnQualifier, timestamp,
                     deleteAllVersions, lock);
                 hTable.delete(delete);
                 return null;
             }
         });
     }
 
     /**
      * @see HBaseService#scan(String, String, String, Long, Long, Integer, Integer,
      *      Boolean, Integer, String, String)
      */
     public Iterable<Result> scan(final String tableName,
                                  final String columnFamilyName,
                                  final String columnQualifier,
                                  final Long timestamp,
                                  final Long maxTimestamp,
                                  final Integer caching,
                                  final boolean cacheBlocks,
                                  final int maxVersions,
                                  final String startRow,
                                  final String stopRow,
                                  final int fetchSize)
     {
         return doWithHTable(tableName, new TableCallback<ResultIterable>()
         {
             public ResultIterable doWithHBaseAdmin(HTableInterface hTable) throws Exception
             {
                 Scan scan = new Scan();
                 if (columnFamilyName != null)
                 {
                     if (columnQualifier != null)
                     {
                         scan.addColumn(columnFamilyName.getBytes(UTF8), columnQualifier.getBytes(UTF8));
                     }
                     else
                     {
                         scan.addFamily(columnFamilyName.getBytes(UTF8));
                     }
                 }
                 if (timestamp != null)
                 {
                     if (maxTimestamp != null)
                     {
                         scan.setTimeRange(timestamp, maxTimestamp);
                     }
                     else
                     {
                         scan.setTimeStamp(timestamp);
                     }
                 }
                 if (caching != null)
                 {
                     scan.setCaching(caching);
                 }
                 scan.setCacheBlocks(cacheBlocks);
                 scan.setMaxVersions(maxVersions);
                 if (startRow != null)
                 {
                     scan.setStartRow(startRow.getBytes(UTF8));
                 }
                 if (stopRow != null)
                 {
                     scan.setStopRow(stopRow.getBytes(UTF8));
                 }
 
                 return new ResultIterable(scan, fetchSize, hTable);
             }
        }, false);
     }
 
     private static class ScannerAndResults
     {
         private ResultScanner scanner;
         private Result[] results;
 
         public ScannerAndResults(ResultScanner scanner, int fetchSize) throws IOException
         {
             this(scanner, scanner.next(fetchSize));
         }
 
         public ScannerAndResults(ResultScanner scanner, Result[] results)
         {
             this.scanner = scanner;
             this.results = results;
         }
 
         public Result[] getResults()
         {
             return results;
         }
 
     }
 
     private static final class ResultIterable extends PaginatedIterable<Result, ScannerAndResults>
     {
         private final HTableInterface hTable;
         private final int fetchSize;
         private final Scan scan;
 
         public ResultIterable(Scan scan, int fetchSize, HTableInterface hTable)
         {
             this.scan = scan;
             this.fetchSize = fetchSize;
             this.hTable = hTable;
         }
 
         @Override
         protected ScannerAndResults firstPage()
         {
             try
             {
                 return getMoreResults(hTable.getScanner(scan));
             }
             catch (IOException e)
             {
                 throw new UnhandledException(e);
             }
         }
 
         private ScannerAndResults getMoreResults(ResultScanner scanner)
         {
             try
             {
                 return new ScannerAndResults(scanner, fetchSize);
             }
             catch (IOException e)
             {
                 throw new UnhandledException(e);
             }
         }
 
         @Override
         protected boolean hasNextPage(ScannerAndResults page)
         {
            boolean hasNextPage = page.getResults().length == fetchSize;
            if(!hasNextPage){
            	closeHTable();
            }
        	return hasNextPage;
         }
 
        private void closeHTable() {
        	try
        	{
        		hTable.close();
			} 
        	catch (IOException e) 
        	{
				throw new UnhandledException(e);
			}
			
		}

         @Override
         protected ScannerAndResults nextPage(ScannerAndResults currentPage)
         {
             return getMoreResults(currentPage.scanner);
         }
 
         @Override
         protected Iterator<Result> pageIterator(ScannerAndResults page)
         {
             return Arrays.asList(page.results).iterator();
         }
 
     }
 
     /** @see HBaseService#increment(String, String, String, String, long, boolean) */
     public long increment(final String tableName,
                           final String row,
                           final String columnFamilyName,
                           final String columnQualifier,
                           final long amount,
                           final boolean writeToWAL)
     {
         Validate.isTrue(StringUtils.isNotBlank(tableName));
         Validate.isTrue(StringUtils.isNotBlank(row));
         Validate.isTrue(StringUtils.isNotBlank(columnFamilyName));
         Validate.isTrue(StringUtils.isNotBlank(columnQualifier));
         return doWithHTable(tableName, new TableCallback<Long>()
         {
             public Long doWithHBaseAdmin(HTableInterface hTable) throws Exception
             {
                 return hTable.incrementColumnValue(row.getBytes(UTF8), columnFamilyName.getBytes(UTF8),
                     columnQualifier.getBytes(UTF8), amount, writeToWAL);
             }
         });
     }
 
     /**
      * @see HBaseService#checkAndPut(String, String, String, String, String, String,
      *      String, Long, String, Boolean, RowLock)
      */
     public boolean checkAndPut(final String tableName,
                                final String row,
                                final String checkColumnFamilyName,
                                final String checkColumnQualifier,
                                final Object checkValue,
                                final String putColumnFamilyName,
                                final String putColumnQualifier,
                                final Long putTimestamp,
                                final Object putValue,
                                final boolean putWriteToWAL,
                                final RowLock putLock)
     {
         return doWithHTable(tableName, new TableCallback<Boolean>()
         {
             public Boolean doWithHBaseAdmin(HTableInterface hTable) throws Exception
             {
                 final Put put = createPut(row, putColumnFamilyName, putColumnQualifier, putTimestamp,
                     putValue, putWriteToWAL, putLock);
                 return hTable.checkAndPut(row.getBytes(UTF8), checkColumnFamilyName.getBytes(UTF8),
                     checkColumnQualifier.getBytes(UTF8), toByteArray(checkValue), put);
             }
         });
     }
 
     /**
      * @see HBaseService#checkAndDelete(String, String, String, String, String,
      *      String, String, Long, Boolean, RowLock)
      */
     public boolean checkAndDelete(final String tableName,
                                   final String row,
                                   final String checkColumnFamilyName,
                                   final String checkColumnQualifier,
                                   final Object checkValue,
                                   final String deleteColumnFamilyName,
                                   final String deleteColumnQualifier,
                                   final Long deleteTimestamp,
                                   final Boolean deleteAllVersions,
                                   final RowLock deleteLock)
     {
         return doWithHTable(tableName, new TableCallback<Boolean>()
         {
             public Boolean doWithHBaseAdmin(HTableInterface hTable) throws Exception
             {
                 final Delete delete = createDelete(row, deleteColumnFamilyName, deleteColumnQualifier,
                     deleteTimestamp, deleteAllVersions, deleteLock);
                 return hTable.checkAndDelete(row.getBytes(UTF8), checkColumnFamilyName.getBytes(UTF8),
                     checkColumnQualifier.getBytes(UTF8), toByteArray(checkValue), delete);
             }
         });
     }
 
     /** @see HBaseService#lock(String, String) */
     public RowLock lock(final String tableName, final String row)
     {
         return doWithHTable(tableName, new TableCallback<RowLock>()
         {
             public RowLock doWithHBaseAdmin(HTableInterface hTable) throws Exception
             {
                 return hTable.lockRow(row.getBytes(UTF8));
             }
         });
     }
 
     /** @see HBaseService#unlock(String, RowLock) */
     public void unlock(final String tableName, final RowLock lock)
     {
         doWithHTable(tableName, new TableCallback<Void>()
         {
             public Void doWithHBaseAdmin(HTableInterface hTable) throws Exception
             {
                 hTable.unlockRow(lock);
                 return null;
             }
         });
     }
 
     // ------------ Configuration
     /** @see HBaseService#addProperties(Map) */
     public void addProperties(Map<String, String> properties)
     {
         for (Entry<String, String> entry : properties.entrySet())
         {
             configuration.set(entry.getKey(), entry.getValue());
         }
     }
 
     // ------------ Private
 
     private void doFlush(HBaseAdmin hBaseAdmin, String name)
     {
         try
         {
             hBaseAdmin.flush(name);
         }
         catch (IOException e)
         {
             throw new HBaseServiceException(e);
         }
         catch (InterruptedException e)
         {
             throw new HBaseServiceException(e);
         }
     }
 
     private Get createGet(String rowKey, Integer maxVersions, Long timestamp)
     {
         Get get = new Get(rowKey.getBytes(UTF8));
         if (maxVersions != null)
         {
             try
             {
                 get.setMaxVersions(maxVersions);
             }
             catch (IOException e)
             {
                 new HBaseServiceException(e);
             }
         }
         if (timestamp != null)
         {
             get.setTimeStamp(timestamp);
         }
         return get;
     }
 
     private Put createPut(final String row,
                           final String columnFamilyName,
                           final String columnQualifier,
                           final Long timestamp,
                           final Object value,
                           final boolean writeToWAL,
                           final RowLock lock)
     {
         final Put put;
         if (lock == null)
         {
             put = new Put(row.getBytes(UTF8));
         }
         else
         {
             put = new Put(row.getBytes(UTF8), lock);
         }
         if (timestamp == null)
         {
             put.add(columnFamilyName.getBytes(UTF8), columnQualifier.getBytes(UTF8), toByteArray(value));
         }
         else
         {
             put.add(columnFamilyName.getBytes(UTF8), columnQualifier.getBytes(UTF8), timestamp,
                 toByteArray(value));
         }
         put.setWriteToWAL(writeToWAL);
         return put;
     }
 
     private Delete createDelete(final String row,
                                 final String columnFamilyName,
                                 final String columnQualifier,
                                 final Long timestamp,
                                 final boolean deleteAllVersions,
                                 final RowLock lock)
     {
         final Delete delete = new Delete(row.getBytes(UTF8), HConstants.LATEST_TIMESTAMP, lock);
         if (columnFamilyName != null)
         {
             if (columnQualifier != null)
             {
                 if (deleteAllVersions)
                 {
                     delete.deleteColumns(columnFamilyName.getBytes(UTF8), columnQualifier.getBytes(UTF8),
                         coalesceTimestamp(timestamp));
                 }
                 else
                 {
                     delete.deleteColumn(columnFamilyName.getBytes(UTF8), columnQualifier.getBytes(UTF8),
                         coalesceTimestamp(timestamp));
                 }
             }
             else
             {
                 delete.deleteFamily(columnFamilyName.getBytes(UTF8), coalesceTimestamp(timestamp));
             }
         }
         return delete;
     }
 
     private static long coalesceTimestamp(Long timestamp)
     {
         return timestamp != null ? timestamp : HConstants.LATEST_TIMESTAMP;
     }
 
     private HTableInterface createHTable(String tableName)
     {
         return hTableInterfaceFactory.createHTableInterface(configuration, tableName.getBytes(UTF8));
     }
 
     /**
      * Returns a new instance of {@link HBaseAdmin}. Clients should call
      * {@link RPCHBaseService#destroyHBaseAdmin(HBaseAdmin)}.
      */
     private HBaseAdmin createHBaseAdmin()
     {
         try
         {
             return new HBaseAdmin(configuration);
         }
         catch (MasterNotRunningException e)
         {
             throw new HBaseServiceException(e);
         }
         catch (ZooKeeperConnectionException e)
         {
             throw new HBaseServiceException(e);
         }
     }
 
     /** Release any resources allocated by {@link HBaseAdmin} */
     private void destroyHBaseAdmin(final HBaseAdmin hBaseAdmin)
     {
         if (hBaseAdmin != null)
         {
             HConnectionManager.deleteConnection(hBaseAdmin.getConfiguration(), true);
         }
     }
 
     private byte[] toByteArray(Object o)
     {
         return BYTE_ARRAY_CONVERTER.toByteArray(o);
     }
 
     /** Retain and release the {@link HBaseAdmin} */
     private <T> T doWithHBaseAdmin(AdminCallback<T> callback)
     {
         HBaseAdmin hBaseAdmin = null;
         try
         {
             hBaseAdmin = createHBaseAdmin();
             return callback.doWithHBaseAdmin(hBaseAdmin);
         }
         finally
         {
             destroyHBaseAdmin(hBaseAdmin);
         }
     }
 
    private <T> T doWithHTable(final String tableName, final TableCallback<T> callback){
    	return doWithHTable(tableName, callback, true);
    }
     /** Retain and release the {@link HTable} */
    private <T> T doWithHTable(final String tableName, final TableCallback<T> callback, boolean closeHtable)
     {
         Validate.isTrue(StringUtils.isNotBlank(tableName));
         Validate.notNull(callback);
         HTableInterface hTable = null;
         try
         {
             hTable = createHTable(tableName);
             return callback.doWithHBaseAdmin(hTable);
         }
         catch (Exception e)
         {
             throw new HBaseServiceException(e);
         }
         finally
         {
             if (hTable != null)
             {
                 try
                 {
                     hTable.close();
                 }
                 catch (IOException e)
                 {
                     throw new HBaseServiceException(e);
                 }
             }
         }
     }
 
     /** Callback for using the {@link HBaseAdmin} without worry about releasing it */
     interface AdminCallback<T>
     {
         T doWithHBaseAdmin(final HBaseAdmin hBaseAdmin);
     }
 
     /**
      * Callback for using the {@link HTableInterface} without worry about releasing
      * it
      */
     interface TableCallback<T>
     {
         T doWithHBaseAdmin(final HTableInterface hTable) throws Exception;
     }
 
 }
