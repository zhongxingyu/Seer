 package cn.iie.haiep.hbase.store;
 /**
  * @author forhappy
  * Date: 2012-3-31, 10:38 PM.
  */
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.hadoop.hbase.HColumnDescriptor;
 import org.apache.hadoop.hbase.HTableDescriptor;
 import org.apache.hadoop.hbase.io.hfile.Compression.Algorithm;
 import org.apache.hadoop.hbase.regionserver.StoreFile.BloomType;
 import org.apache.hadoop.hbase.util.Bytes;
 
 /**
  * Table definitions for HBase
  */
 public class HBaseTableInfo {
 
 	private Map<String, HTableDescriptor> tableDescriptors = new HashMap<String, HTableDescriptor>();
 
 	// s set of names of the tables.
 	private Set<String> tableNames = new HashSet<String>();
 	
 	private Iterator<String> iterName = null;
 
 	// a map from field name to hbase column
 	private Map<String, HBaseColumn> columnMap = new HashMap<String, HBaseColumn>();
 
 	public HBaseTableInfo() {
 	}
 
 	public void addTableName(String tableName) {
		this.tableNames.add(tableName);
 	}
 
 	public String getTableName(String tableName) {
 		if (tableName == null) {
 			return null;
 		} else { 
 			if (tableNames.contains(tableName))
 				return tableName;
 			else return null;
 		}
 	}
 
 	public void addTable(String tableName) {
 		if (!tableDescriptors.containsKey(tableName)) {
 			if (!tableNames.contains(tableName)) {
 				addTableName(tableName);
				/**
				 * TODO foolish implementation for initializing iterator iterName.
				 */
				iterName = tableNames.iterator();
 			}
 			tableDescriptors.put(tableName, new HTableDescriptor(tableName));
 		}
 	}
 
 	public HTableDescriptor getTable(String tableName) {
 		return tableDescriptors.get(tableName);
 	}
 	
 	public Boolean hasNextTableName() {
 		return iterName.hasNext();
 	}
 	
 	public String nextTableName() {
 		return iterName.next();
 	}
 
 	public void addColumnFamily(String tableName, String familyName,
 			String compression, String blockCache, String blockSize,
 			String bloomFilter, String maxVersions, String timeToLive,
 			String inMemory) {
 
 		HColumnDescriptor columnDescriptor = addColumnFamily(tableName,
 				familyName);
 
 		if (compression != null)
 			columnDescriptor.setCompressionType(Algorithm.valueOf(compression));
 		if (blockCache != null)
 			columnDescriptor.setBlockCacheEnabled(Boolean
 					.parseBoolean(blockCache));
 		if (blockSize != null)
 			columnDescriptor.setBlocksize(Integer.parseInt(blockSize));
 		if (bloomFilter != null)
 			columnDescriptor.setBloomFilterType(BloomType.valueOf(bloomFilter));
 		if (maxVersions != null)
 			columnDescriptor.setMaxVersions(Integer.parseInt(maxVersions));
 		if (timeToLive != null)
 			columnDescriptor.setTimeToLive(Integer.parseInt(timeToLive));
 		if (inMemory != null)
 			columnDescriptor.setInMemory(Boolean.parseBoolean(inMemory));
 
 		getTable(tableName).addFamily(columnDescriptor);
 	}
 
 	public HColumnDescriptor addColumnFamily(String tableName, String familyName) {
 		HTableDescriptor tableDescriptor = getTable(tableName);
 		HColumnDescriptor columnDescriptor = tableDescriptor.getFamily(Bytes
 				.toBytes(familyName));
 		if (columnDescriptor == null) {
 			columnDescriptor = new HColumnDescriptor(familyName);
 			tableDescriptor.addFamily(columnDescriptor);
 		}
 		return columnDescriptor;
 	}
 
 	public void addField(String fieldName, String tableName, String family,
 			String qualifier) {
 		byte[] familyBytes = Bytes.toBytes(family);
 		byte[] qualifierBytes = qualifier == null ? null : Bytes
 				.toBytes(qualifier);
 
 		HBaseColumn column = new HBaseColumn(tableName, familyBytes,
 				qualifierBytes);
 		columnMap.put(fieldName, column);
 	}
 
 	public HBaseColumn getColumn(String fieldName) {
 		return columnMap.get(fieldName);
 	}
 }
