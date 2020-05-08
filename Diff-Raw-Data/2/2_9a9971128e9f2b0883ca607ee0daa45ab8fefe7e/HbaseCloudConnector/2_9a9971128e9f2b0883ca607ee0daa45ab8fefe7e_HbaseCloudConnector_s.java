 /**
 is * Mule HBase Cloud Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.hbase;
 
 import org.mule.api.annotations.Configurable;
 import org.mule.api.annotations.Module;
 import org.mule.api.annotations.Processor;
 import org.mule.api.annotations.param.Default;
 import org.mule.api.annotations.param.Optional;
 import org.mule.api.lifecycle.Initialisable;
 import org.mule.api.lifecycle.InitialisationException;
 import org.mule.module.hbase.api.BloomFilterType;
 import org.mule.module.hbase.api.CompressionType;
 import org.mule.module.hbase.api.HBaseService;
 import org.mule.module.hbase.api.impl.RPCHBaseService;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.annotation.PostConstruct;
 
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.RowLock;
 
 /**
  * <p>
  * HBase connector
  * </p>
  * <p>
  * It delegates each Processor on a {@link HBaseService} and it accepts custom
  * configuration in a Key-Value fashion
  * </p>
  * 
  * @author Pablo Martin Grigolatto
  * @since Apr 18, 2011
  */
 @Module(name = "hbase", schemaVersion = "2.0")
 public class HbaseCloudConnector {
 	/** The HBaseService You may change it for mocking purposes */
 	@Configurable
 	@Optional
 	private HBaseService facade;
 
 	/**
 	 * HBase internal configuration properties. Be sure to add the following
 	 * properties to ensure a connection against your hBase instance:
 	 * hbase.zookeeper.quorum, hbase.zookeeper.property.clientPort
 	 * 
 	 * For more information please consult HBase documentation.
 	 */
 	@Configurable
 	@Optional
 	private Map<String, String> properties;
 
 	public HbaseCloudConnector() {
 		properties = Collections.emptyMap();
 	}
 
 	// ------------ Admin Processors
 
 	/**
 	 * Answers if the HBase server is reachable
 	 * 
 	 * {@sample.xml ../../../doc/mule-module-hbase.xml.sample
 	 * hbase:is-alive-server}
 	 * 
 	 * @return true if the server can be reached and the master node is alive,
 	 *         false otherwise.
 	 */
 	@Processor
 	public boolean isAliveServer() {
 		return facade.alive();
 	}
 
 	/**
 	 * Creates a new table given its name. The descriptor must be unique and not
 	 * reserved.
 	 * 
 	 * {@sample.xml ../../../doc/mule-module-hbase.xml.sample
 	 * hbase:create-table}
 	 * 
 	 * @param tableName
 	 *            the descriptor for the new table.
 	 */
 	@Processor
 	public void createTable(final String tableName) {
 		facade.createTable(tableName);
 	}
 
 	/**
 	 * Answers if a given table exists, regardless it is enabled or not
 	 * 
 	 * {@sample.xml ../../../doc/mule-module-hbase.xml.sample
 	 * hbase:exists-table}
 	 * 
 	 * @param tableName
 	 *            the table name
 	 * @return true only if the table exists, false otherwise
 	 */
 	@Processor
 	public boolean existsTable(final String tableName) {
 		return facade.existsTable(tableName);
 	}
 
 	/**
 	 * Disables and deletes an existent table.
 	 * 
 	 * {@sample.xml ../../../doc/mule-module-hbase.xml.sample
 	 * hbase:delete-table}
 	 * 
 	 * @param tableName
 	 *            name of table to delete
 	 */
 	@Processor
 	public void deleteTable(final String tableName) {
 		facade.deleteTable(tableName);
 	}
 
 	/**
 	 * Answers if the given existent table is enabled.
 	 * 
 	 * {@sample.xml ../../../doc/mule-module-hbase.xml.sample
 	 * hbase:is-enabled-table}
 	 * 
 	 * @param tableName
 	 *            name of the table to query for its enabling state
 	 * @return true only if the table was disabled. False otherwise
 	 */
 	@Processor
 	public boolean isEnabledTable(final String tableName) {
 		return !facade.isDisabledTable(tableName);
 	}
 
 	/**
 	 * Enables an existent table.
 	 * 
 	 * {@sample.xml ../../../doc/mule-module-hbase.xml.sample
 	 * hbase:enable-table}
 	 * 
 	 * @param tableName
 	 *            name of the table to enable
 	 */
 	@Processor
 	public void enableTable(final String tableName) {
 		facade.enableTable(tableName);
 	}
 
 	/**
 	 * Disables an existent table
 	 * 
 	 * {@sample.xml ../../../doc/mule-module-hbase.xml.sample
 	 * hbase:disable-table}
 	 * 
 	 * @param tableName
 	 *            the table name to disable
 	 */
 	@Processor
 	public void disableTable(final String tableName) {
 		facade.disabeTable(tableName);
 	}
 
 	/**
 	 * Adds a column family to a table given a table and column name. This
 	 * Processor gracefully handles necessary table disabling and enabled.
 	 * 
 	 * {@sample.xml ../../../doc/mule-module-hbase.xml.sample
 	 * hbase:add-column-family}
 	 * 
 	 * @param tableName
 	 *            the name of the target table
 	 * @param columnFamilyName
 	 *            the name of the column
 	 * @param maxVersions
 	 *            the optional maximum number of versions the column family
 	 *            supports
 	 * @param inMemory
 	 *            if all the column values will be stored in the region's cache
 	 * @param scope
 	 *            replication scope: 0 for locally scoped data (data for this
 	 *            column family will not be replicated) and 1 for globally
 	 *            scoped data (data will be replicated to all peers.))
 	 */
 	@Processor
 	public void addColumnFamily(final String tableName, final String columnFamilyName, @Optional final Integer maxVersions,
 			@Optional @Default("false") final Boolean inMemory, @Optional final Integer scope) {
 		facade.addColumn(tableName, columnFamilyName, maxVersions, inMemory, scope);
 	}
 
 	/**
 	 * Answers if column family exists.
 	 * 
 	 * {@sample.xml ../../../doc/mule-module-hbase.xml.sample
 	 * hbase:exists-column-family}
 	 * 
 	 * @param tableName
 	 *            the target table name
 	 * @param columnFamilyName
 	 *            the target column family name
 	 * @return true if the column exists, false otherwise
 	 */
 	@Processor
 	public boolean existsColumnFamily(final String tableName, final String columnFamilyName) {
 		return facade.existsColumn(tableName, columnFamilyName);
 	}
 
 	/**
 	 * Changes one or more properties of a column family in a table. This
 	 * Processor gracefully handles necessary table disabling and enabled.
 	 * 
 	 * {@sample.xml ../../../doc/mule-module-hbase.xml.sample
 	 * hbase:modify-column-family}
 	 * 
 	 * @param tableName
 	 *            required the target table
 	 * @param columnFamilyName
 	 *            required the target column family
 	 * @param maxVersions
 	 *            the new max amount of versions
 	 * @param blocksize
 	 *            the the new block size
 	 * @param compressionType
 	 *            the new compression type
 	 * @param compactionCompressionType
 	 *            the new compaction compression type
 	 * @param inMemory
 	 *            new value for if values are stored in Region's cache
 	 * @param timeToLive
 	 *            new ttl
 	 * @param blockCacheEnabled
 	 *            new value of enabling block cache
 	 * @param bloomFilterType
 	 *            new value of bloom filter type
 	 * @param replicationScope
 	 *            new value for replication scope
 	 * @param values
 	 *            other custom parameters values
 	 */
 	@Processor
 	public void modifyColumnFamily(final String tableName, final String columnFamilyName, @Optional final Integer maxVersions,
 			@Optional final Integer blocksize, @Optional final CompressionType compressionType,
 			@Optional final CompressionType compactionCompressionType, @Optional final Boolean inMemory,
 			@Optional final Integer timeToLive, @Optional final Boolean blockCacheEnabled, @Optional final BloomFilterType bloomFilterType,
 			@Optional final Integer replicationScope, @Optional final Map<String, String> values) {
 		facade.modifyColumn(tableName, columnFamilyName, maxVersions, blocksize, compressionType, compactionCompressionType, inMemory,
 				timeToLive, blockCacheEnabled, bloomFilterType, replicationScope, values);
 	}
 
 	/**
 	 * Delete a column family
 	 * 
 	 * {@sample.xml ../../../doc/mule-module-hbase.xml.sample
 	 * hbase:delete-column-family}
 	 * 
 	 * @param tableName
 	 *            required the target table
 	 * @param columnFamilyName
 	 *            required the target column family
 	 */
 	@Processor
 	public void deleteColumnFamily(final String tableName, final String columnFamilyName) {
 		facade.deleteColumn(tableName, columnFamilyName);
 	}
 
 	// ------------ Row Processors
 
 	/**
 	 * Answers the values at the given row - (table, row) combination
 	 * 
 	 * {@sample.xml ../../../doc/mule-module-hbase.xml.sample hbase:get-values}
 	 * 
 	 * @param tableName
 	 *            required the target table
 	 * @param rowKey
 	 *            the key of the row to update
 	 * @param maxVersions
 	 *            the maximum number of versions to retrieved
 	 * @param timestamp
 	 *            the timestamp
 	 * @return the {@link Result}
 	 */
 	@Processor
 	public Result getValues(final String tableName, final String rowKey, @Optional final Integer maxVersions, @Optional final Long timestamp) {
 		return facade.get(tableName, rowKey, maxVersions, timestamp);
 	}
 
 	/**
 	 * Saves a value at the specified (table, row, familyName, familyQualifier,
 	 * timestamp) combination
 	 * 
 	 * {@sample.xml ../../../doc/mule-module-hbase.xml.sample hbase:put-value}
 	 * 
 	 * @param tableName
 	 *            required the target table
 	 * @param rowKey
 	 *            the key of the row to update
 	 * @param columnFamilyName
 	 *            the column family dimension
 	 * @param columnQualifier
 	 *            the column qualifier dimension
 	 * @param timestamp
 	 *            the version dimension
 	 * @param value
 	 *            the value to put. It must be either a byte array or a
 	 *            serializable object. As a special case, strings are saved
 	 *            always in standard utf-8 format.
 	 * @param writeToWAL
 	 *            set it to false means that in a fail scenario, you will lose
 	 *            any increments that have not been flushed.
 	 * @param lock
 	 *            a optional {@link RowLock}
 	 */
 	@Processor
 	public void putValue(final String tableName, final String rowKey, final String columnFamilyName, final String columnQualifier,
 			@Optional final Long timestamp, final Object value, @Optional @Default("true") final boolean writeToWAL,
 			@Optional final RowLock lock) {
 		facade.put(tableName, rowKey, columnFamilyName, columnQualifier, timestamp, value, writeToWAL, lock);
 	}
 
 	/**
 	 * 
 	 * Deletes the values at a given row {@sample.xml
 	 * ../../../doc/mule-module-hbase.xml.sample hbase:delete-values}
 	 * 
 	 * @param tableName
 	 *            the name of the target table
 	 * @param rowKey
 	 *            the key of the row to delete
 	 * @param columnFamilyName
 	 *            set null to delete all column families in the specified row
 	 * @param columnQualifier
 	 *            the qualifier of the column values to delete. If no qualifier
 	 *            is specified, the Processor will affect all the qulifiers for
 	 *            the given column family name to delete. Thus it has only sense
 	 *            if deleteColumnFamilyName is specified
 	 * @param timestamp
 	 *            the timestamp of the values to delete. If no timestamp is
 	 *            specified, the most recent timestamp for the deleted value is
 	 *            used. Only has sense if deleteColumnFamilyName is specified
 	 * @param deleteAllVersions
 	 *            if all versions should be deleted,or only those more recent
 	 *            than the deleteTimestamp. Only has sense if
 	 *            deleteColumnFamilyName and deleteColumnQualifier are specified
 	 * @param lock
 	 *            an optional {@link RowLock}
 	 */
 	@Processor
 	public void deleteValues(final String tableName, final String rowKey, @Optional final String columnFamilyName,
 			@Optional final String columnQualifier, @Optional final Long timestamp,
 			@Optional @Default("false") final boolean deleteAllVersions, @Optional final RowLock lock) {
 		facade.delete(tableName, rowKey, columnFamilyName, columnQualifier, timestamp, deleteAllVersions, lock);
 	}
 
 	/**
 	 * Scans across all rows in a table, returning a scanner over it
 	 * 
 	 * {@sample.xml ../../../doc/mule-module-hbase.xml.sample hbase:scan-table}
 	 * 
 	 * @param tableName
 	 *            limits the scan to a specific table. This is the only required
 	 *            argument.
 	 * @param columnFamilyName
 	 *            limits the scan to a specific column family or null
 	 * @param columnQualifier
 	 *            limits the scan to a specific column or null. Requires a
 	 *            columnFamilyName to be defined.
 	 * @param timestamp
 	 *            limits the scan to a specific timestamp
 	 * @param maxTimestamp
 	 *            get versions of columns only within the specified timestamp
 	 *            range: [timestamp, maxTimestamp)
 	 * @param caching
 	 *            the number of rows for caching
 	 * @param batch
 	 *            the maximum number of values to return for each call to next()
 	 *            in the ResultScanner
 	 * @param cacheBlocks
 	 *            the number of rows for caching that will be passed to scanners
 	 * @param maxVersions
 	 *            limits the number of versions on each column
 	 * @param allVersions
 	 *            get all available versions on each column
 	 * @param startRowKey
 	 *            limits the beginning of the scan to the specified row
 	 *            inclusive
 	 * @param stopRowKey
 	 *            limits the end of the scan to the specified row exclusive
 	 * @param fetchSize
 	 *            the number of results internally fetched by request to the
 	 *            HBase server. Increase it for improving network efficiency, or
 	 *            decrease it for reducing memory usage
 	 * @return an Iterable of Result's. It may be used with a collection
 	 *         splitter.
 	 */
 	@Processor
 	public Iterable<Result> scanTable(final String tableName, @Optional final String columnFamilyName,
 			@Optional final String columnQualifier, @Optional final Long timestamp, @Optional final Long maxTimestamp,
 			@Optional final Integer caching, @Optional @Default("true") final boolean cacheBlocks,
 			@Optional @Default("1") final int maxVersions, @Optional final String startRowKey, @Optional final String stopRowKey,
 			@Optional @Default("50") int fetchSize) {
 		return facade.scan(tableName, columnFamilyName, columnQualifier, timestamp, maxTimestamp, caching, cacheBlocks, maxVersions,
 				startRowKey, stopRowKey, fetchSize);
 	}
 
 	/**
 	 * Atomically increments the value of at a (table, row, familyName,
 	 * familyQualifier) combination. If the cell value does not yet exist it is
 	 * initialized to amount.
 	 * 
 	 * {@sample.xml ../../../doc/mule-module-hbase.xml.sample
 	 * hbase:increment-value}
 	 * 
 	 * @param tableName
 	 *            the name of the table that contains the cell to increment.
 	 * @param rowKey
 	 *            the row key that contains the cell to increment.
 	 * @param columnFamilyName
 	 *            the column family of the cell to increment.
 	 * @param columnQualifier
 	 *            the column qualifier of the cell to increment.
 	 * @param amount
 	 *            the amount to increment the cell with (or decrement, if the
 	 *            amount is negative).
 	 * @param writeToWAL
 	 *            set it to false means that in a fail scenario, you will lose
 	 *            any increments that have not been flushed.
 	 * @return the new value, post increment
 	 */
 	@Processor
 	public long incrementValue(final String tableName, final String rowKey, final String columnFamilyName, final String columnQualifier,
 			final long amount, @Optional @Default("true") final boolean writeToWAL) {
 		return facade.increment(tableName, rowKey, columnFamilyName, columnQualifier, amount, writeToWAL);
 	}
 
 	/**
 	 * Atomically checks if a value at a (table, row,family,qualifier) matches
 	 * the given one. If it does, it performs the put.
 	 * 
 	 * {@sample.xml ../../../doc/mule-module-hbase.xml.sample
 	 * hbase:check-and-put-value}
 	 * 
 	 * @param tableName
 	 *            the name of the table that contains the cell to check.
 	 * @param rowKey
 	 *            the row key that contains the cell to check.
 	 * @param checkColumnFamilyName
 	 *            the column family of the cell to check.
 	 * @param checkColumnQualifier
 	 *            the column qualifier of the cell to check.
 	 * @param checkValue
 	 *            the value to check. It must be either a byte array or a
 	 *            serializable object. As a special case, strings are saved
 	 *            always in standard utf-8 format.
 	 * @param putColumnFamilyName
 	 *            the column family of the cell to put.
 	 * @param putColumnQualifier
 	 *            the column qualifier of the cell to put.
 	 * @param putTimestamp
 	 *            the version dimension to put.
 	 * @param value
 	 *            the value to put. It must be either a byte array or a
 	 *            serializable object. As a special case, strings are saved
 	 *            always in standard utf-8 format.
 	 * @param writeToWAL
 	 *            set it to false means that in a fail scenario, you will lose
 	 *            any increments that have not been flushed.
 	 * @param lock
 	 *            and optional {@link RowLock}
 	 * @return true if the new put was executed, false otherwise
 	 */
 	@Processor
 	public boolean checkAndPutValue(final String tableName, final String rowKey, final String checkColumnFamilyName,
 			final String checkColumnQualifier, final Object checkValue, final String putColumnFamilyName, final String putColumnQualifier,
 			@Optional final Long putTimestamp, final Object value, @Optional @Default("true") final boolean writeToWAL,
 			@Optional final RowLock lock) {
 		return facade.checkAndPut(tableName, rowKey, checkColumnFamilyName, checkColumnQualifier, checkValue, putColumnFamilyName,
 				putColumnQualifier, putTimestamp, value, writeToWAL, lock);
 	}
 
 	/**
 	 * Atomically checks if a value at a (table, row,family,qualifier) matches
 	 * the given one. If it does, it performs the delete.
 	 * 
 	 * {@sample.xml ../../../doc/mule-module-hbase.xml.sample
 	 * hbase:check-and-delete-value}
 	 * 
 	 * @param tableName
 	 *            the name of the table that contains the cell to check.
 	 * @param rowKey
 	 *            the row key that contains the cell to check.
 	 * @param checkColumnFamilyName
 	 *            the column family of the cell to check.
 	 * @param checkValue
 	 *            the value to check. It must be either a byte array or a
 	 *            serializable object. As a special case, strings are saved
 	 *            always in standard utf-8 format.
 	 * @param deleteColumnFamilyName
 	 *            the name of the column family to delete
 	 * @param checkColumnQualifier
 	 *            the qualifier of the column to check
 	 * @param deleteColumnQualifier
 	 *            the qualifier of the column values to delete. If no qualifier
 	 *            is specified, the Processor will affect all the qulifiers for
 	 *            the given column family name to delete. Thus it has only sense
 	 *            if deleteColumnFamilyName is specified
 	 * @param deleteTimestamp
 	 *            the timestamp of the values to delete. If no timestamp is
 	 *            specified, the most recent timestamp for the deleted value is
 	 *            used. Only has sense if deleteColumnFamilyName is specified
 	 * @param deleteAllVersions
 	 *            if all versions should be deleted,or only those more recent
 	 *            than the deleteTimestamp. Only has sense if
 	 *            deleteColumnFamilyName and deleteColumnQualifier are specified
 	 * @param lock
 	 *            an optional {@link RowLock}
 	 * @return true if the new delete was executed, false otherwise
 	 */
 	@Processor
 	public boolean checkAndDeleteValue(final String tableName, final String rowKey, final String checkColumnFamilyName,
 			final String checkColumnQualifier, final Object checkValue, final String deleteColumnFamilyName,
 			final String deleteColumnQualifier, @Optional final Long deleteTimestamp,
 			@Optional @Default("false") final boolean deleteAllVersions, @Optional final RowLock lock) {
 		return facade.checkAndDelete(tableName, rowKey, checkColumnFamilyName, checkColumnQualifier, checkValue, deleteColumnFamilyName,
 				deleteColumnQualifier, deleteTimestamp, deleteAllVersions, lock);
 	}
 
 	// ------------ Configuration
 
 	public void setFacade(HBaseService facade) {
 		this.facade = HBaseServiceAdaptor.adapt(facade);
 	}
 
 	public HBaseService getFacade() {
 		return facade;
 	}
 
 	public Map<String, String> getProperties() {
 		return Collections.unmodifiableMap(properties);
 	}
 
 	public void setProperties(Map<String, String> properties) {
 		this.properties = new HashMap<String, String>(properties);
 	}
 
 	/** @see org.mule.api.lifecycle.Initialisable#initialise() */
 	@PostConstruct
 	public void initialise() throws InitialisationException {
 		if (facade == null) {
 			setFacade(new RPCHBaseService());
 			facade.addProperties(properties);
 		}
 	}
 
 }
