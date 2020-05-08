 package net.imagini.cassandra.DumpSSTables;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.nio.ByteBuffer;
 import java.nio.charset.CharacterCodingException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 import net.sf.ehcache.config.CacheConfiguration;
 import net.sf.ehcache.config.PersistenceConfiguration;
 import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
 import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
 
 import org.apache.cassandra.config.CFMetaData;
 import org.apache.cassandra.config.DatabaseDescriptor;
 import org.apache.cassandra.config.Schema;
 import org.apache.cassandra.db.AbstractColumnContainer;
 import org.apache.cassandra.db.ColumnFamily;
 import org.apache.cassandra.db.CounterColumn;
 import org.apache.cassandra.db.DecoratedKey;
 import org.apache.cassandra.db.DeletedColumn;
 import org.apache.cassandra.db.DeletionInfo;
 import org.apache.cassandra.db.ExpiringColumn;
 import org.apache.cassandra.db.IColumn;
 import org.apache.cassandra.db.OnDiskAtom;
 import org.apache.cassandra.db.RangeTombstone;
 import org.apache.cassandra.db.marshal.AbstractType;
 import org.apache.cassandra.exceptions.ConfigurationException;
 import org.apache.cassandra.io.sstable.Descriptor;
 import org.apache.cassandra.io.sstable.SSTableIdentityIterator;
 import org.apache.cassandra.io.sstable.SSTableReader;
 import org.apache.cassandra.io.sstable.SSTableScanner;
 import org.apache.cassandra.tools.SSTableExport;
 import org.apache.cassandra.utils.ByteBufferUtil;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.commons.io.filefilter.DirectoryFileFilter;
 import org.apache.commons.io.filefilter.RegexFileFilter;
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.map.ObjectMapper;
 
 /**
  * Export SSTables to file
  */
 public class SSTableExportMapper {
     private static final ObjectMapper jsonMapper = new ObjectMapper();
 
     private static final String CACHE_PREFIX = "ssTable";
     private static final int MAX_ELEMENTS_IN_CACHE = 10000;
 
     private static final Options options = new Options();
     private static final CacheManager manager;
 
     static {
 	// disabling auto close of the stream
 	jsonMapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
 
 	// Create a singleton CacheManager using defaults
 	manager = CacheManager.create();
 
 	// Create a Cache specifying its configuration.
 	Cache ssCache = new Cache(new CacheConfiguration().name(CACHE_PREFIX).maxEntriesLocalHeap(MAX_ELEMENTS_IN_CACHE).memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LRU).eternal(true).diskExpiryThreadIntervalSeconds(0).persistence(new PersistenceConfiguration().strategy(Strategy.LOCALTEMPSWAP)));
 	manager.addCache(ssCache);
     }
 
     /**
      * JSON Key serializer
      * 
      * @param out
      *            The output stream to write data
      * @param value
      *            value to set as a key
      */
     public static String getJSONKey(String value) {
 	return getJSON(value) + ": ";
     }
 
     /**
      * Serialize columns using given column iterator
      * 
      * @param colFam
      *            The column Family to read from
      * @param columns
      *            column iterator
      * @param comparator
      *            columns comparator
      * @param cfMetaData
      *            Column Family metadata (to get validator)
      * @param key
      *            Rowkey for object decoration
      */
     private static void updateRow(ColumnFamily colFam, Iterator<OnDiskAtom> columns, AbstractType<?> comparator, CFMetaData cfMetaData, String key) {
 	Cache ssCache = manager.getCache(CACHE_PREFIX);
 	Element element;
 	SSTableCassandraRow row;
 	if ((element = ssCache.get(key)) != null) {
 	    row = (SSTableCassandraRow) element.getObjectValue();
 	} else {
 	    row = new SSTableCassandraRow(key);
 	}
 
 	updateMeta(colFam, row);
 
 	while (columns.hasNext()) {
 	    updateSSTableCassandraColumn(columns.next(), comparator, cfMetaData, row);
 	}
 
 	ssCache.put(new Element(key, row));
     }
 
     /**
      * Add deletion record into Cassandra row representation for compaction
      * phase
      * 
      * @param colFam
      *            The column family we are working with
      * @param row
      *            The row to write the updates into
      */
     private static void updateMeta(AbstractColumnContainer colFam, SSTableCassandraRow row) {
 	if (colFam instanceof ColumnFamily) {
 	    ColumnFamily columnFamily = (ColumnFamily) colFam;
 	    if (!columnFamily.deletionInfo().equals(DeletionInfo.LIVE)) {
 		row.getTombstones().add(new SSTableCassandraTombstone(columnFamily.deletionInfo().getTopLevelDeletion().localDeletionTime, columnFamily.deletionInfo().getTopLevelDeletion().markedForDeleteAt));
 	    }
 	}
     }
 
     /**
      * Process column data, discarding if it is old and overwriting or creating
      * as needed where the column should be part of the current record of truth
      * 
      * @param column
      *            The column we are working with
      * @param comparator
      *            Column comparator
      * @param cfMetaData
      *            Metadata container for acquiring a value validator
      * @param outRow
      *            The row representation in which to write the result
      * @return The newly updated cassandra row representation
      */
     private static SSTableCassandraRow updateSSTableCassandraColumn(OnDiskAtom column, AbstractType<?> comparator, CFMetaData cfMetaData, SSTableCassandraRow outRow) {
 	if (column instanceof IColumn) {
 	    IColumn iCol = ((IColumn) column);
 	    ByteBuffer name = ByteBufferUtil.clone(iCol.name());
 	    ByteBuffer value = ByteBufferUtil.clone(iCol.value());
 
 	    // Determine if this should be part of the record of truth then
 	    // discard or use this row to update the current ROT column value.
 	    String strName = (comparator.getString(name));
 	    if (outRow.getColumns().containsKey(strName)) {
 		String timestamp = outRow.getColumns().get(strName).get("timestamp");
 		if (timestamp != null && (Long.parseLong(timestamp) >= iCol.timestamp())) {
 		    return outRow; // Overridden column so we don't care
 		}
 	    } else {
 		// New column
 		outRow.getColumns().put(strName, new HashMap<String, String>());
 	    }
 
 	    // Write the updated timestamp
 	    outRow.getColumns().get(strName).put("timestamp", String.valueOf(iCol.timestamp()));
 
 	    // Update the ROT with the value in this row
 	    if (iCol instanceof DeletedColumn) {
 		// TODO: Confirm deleted columns behave as expected.
 		outRow.getColumns().get(strName).put("value", ByteBufferUtil.bytesToHex(value));
 	    } else {
 		AbstractType<?> validator = cfMetaData.getValueValidator(cfMetaData.getColumnDefinitionFromColumnName(name));
 		outRow.getColumns().get(strName).put("value", String.valueOf(validator.getString(value)));
 	    }
 
 	    // Add metadata markers if needed
 	    if (column instanceof DeletedColumn) {
 		outRow.getColumnTombstones().put(strName, new SSTableCassandraDeletedColumnTombstone(iCol.timestamp()));
 	    } else if (column instanceof ExpiringColumn) {
 		outRow.getColumnTombstones().put(strName, new SSTableCassandraExpiringColumnTombstone(iCol.timestamp(), ((ExpiringColumn) column).getTimeToLive(), ((ExpiringColumn) column).getLocalDeletionTime()));
 	    } else if (column instanceof CounterColumn) {
 		outRow.getColumnTombstones().put(strName, new SSTableCassandraCounterColumnTombstone(iCol.timestamp(), ((CounterColumn) column).timestampOfLastDelete()));
 	    }
 	} else {
 	    // Range tombstones are not fully supported
 	    assert column instanceof RangeTombstone;
 	    RangeTombstone rt = (RangeTombstone) column;
 	    SSTableCassandraRangeTombstone crt = new SSTableCassandraRangeTombstone(comparator.getString(rt.min), comparator.getString(rt.max), rt.data.localDeletionTime, rt.data.markedForDeleteAt);
 	    outRow.getRangeTombstones().add(crt);
 	}
 	return outRow;
     }
 
     /**
      * Get portion of the columns and serialize in loop while not more columns
      * left in the row
      * 
      * @param row
      *            SSTableIdentityIterator row representation with Column Family
      * @param key
      *            Decorated Key for the required row
      * @param out
      *            output stream
      * @throws CharacterCodingException
      */
     private static void serializeRow(SSTableIdentityIterator row, DecoratedKey key) throws CharacterCodingException {
 	ColumnFamily columnFamily = row.getColumnFamily();
 	boolean isSuperCF = columnFamily.isSuper();
 	CFMetaData cfMetaData = columnFamily.metadata();
 	AbstractType<?> comparator = columnFamily.getComparator();
 
 	if (isSuperCF) {
 	    System.err.println("WARNING: SUPERCOLUMN");
 	    throw new UnsupportedOperationException("This program does not support supercolumns");
 	}
 
 	updateRow(columnFamily, row, comparator, cfMetaData, ByteBufferUtil.string(key.key));
     }
 
     // This is necessary to accommodate the test suite since you cannot open a
     // Reader more
     // than once from within the same process.
     static void export(SSTableReader reader) throws IOException {
 	SSTableIdentityIterator row;
 	SSTableScanner scanner = reader.getDirectScanner();
 
 	// collecting keys to export
 	while (scanner.hasNext()) {
 	    row = (SSTableIdentityIterator) scanner.next();
 
 	    serializeRow(row, row.getKey());
 	}
 
 	scanner.close();
     }
 
     /**
      * Export an SSTable and write the resulting JSON to a PrintStream.
      * 
      * @param desc
      *            the descriptor of the sstable table to read from
      * @param outs
      *            PrintStream to write the output to
      * @param excludes
      *            keys to exclude from export
      * 
      * @throws IOException
      *             on failure to read/write input/output
      */
     public static void export(Descriptor desc) throws IOException {
 	export(SSTableReader.open(desc));
     }
 
     /**
      * Given arguments specifying an SSTable, and optionally an output file,
      * export the contents of the SSTable to JSON.
      * 
      * @param args
      *            command lines arguments
      * 
      * @throws IOException
      *             on failure to open/read/write files or output streams
      * @throws ConfigurationException
      *             on configuration failure (wrong params given)
      */
     public static void main(String[] args) throws IOException, ConfigurationException {
 	CommandLine cmd = null;
 	String usage = String.format("Usage: %s <sstable> [-k key [-k key [...]] -x key [-x key [...]]]%n", SSTableExport.class.getName());
 
 	CommandLineParser parser = new PosixParser();
 	try {
 	    cmd = parser.parse(options, args);
 	} catch (ParseException e1) {
 	    System.err.println(e1.getMessage());
 	    System.err.println(usage);
 	    System.exit(1);
 	}
 
 	if (cmd.getArgs().length != 2) {
	    System.err.println("You must supply exactly one (or a comma seperated list) sstable or folder to crawl and one output file");
 	    System.err.println(usage);
 	    System.exit(1);
 	}
 
 	DatabaseDescriptor.loadSchemas();
 	String[] inPaths = cmd.getArgs()[0].split(",");
 	for (String path : inPaths) {
 	    if (path.isEmpty()) {
 		continue;
 	    }
 	    File ssTableFileName = new File(path);
 	    // TODO: Multithreading
 	    readSSTables(ssTableFileName, cmd);
 	}
 
 	// Output
 	Cache ssCache = manager.getCache(CACHE_PREFIX);
 	try {
 	    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(new File(cmd.getArgs()[1]), false)));
 	    final Map<Object, Element> mapElements = ssCache.getAll(ssCache.getKeys());
 	    Iterator<Entry<Object, Element>> iter = mapElements.entrySet().iterator();
 	    String rowStr;
 	    while (iter.hasNext()) {
 		SSTableCassandraRow row = (SSTableCassandraRow) iter.next().getValue().getObjectValue();
 		rowStr = row.toString();
 		if (rowStr != null) {
 		    out.println(rowStr);
 		}
 	    }
 	    out.close();
 	} catch (IOException e) {
 	    System.err.println("IOException: " + e.getMessage());
 	}
 	manager.shutdown();
 	System.exit(0);
     }
 
     private static void readSSTables(File ssTableFileName, CommandLine cmd) throws IOException {
 	if (ssTableFileName.exists()) {
 	    if (ssTableFileName.isDirectory()) {
 		Collection<File> files = org.apache.commons.io.FileUtils.listFiles(ssTableFileName, new RegexFileFilter("^.*Data\\.db"), DirectoryFileFilter.DIRECTORY);
 		for (File file : files) {
 		    readSSTables(file, cmd);
 		}
 	    } else if (ssTableFileName.isFile()) {
 		Descriptor descriptor = Descriptor.fromFilename(ssTableFileName.getAbsolutePath());
 		if (Schema.instance.getCFMetaData(descriptor) == null) {
 		    System.err.println(String.format("The provided column family is not part of this cassandra database: keysapce = %s, column family = %s", descriptor.ksname, descriptor.cfname));
 		    System.exit(1);
 		}
 
 		export(descriptor);
 	    }
 	}
 
     }
 
     public static String getJSON(Object value) {
 	try {
 	    // if (value instanceof String && ((String) value).startsWith("{")
 	    // && ((String) value).endsWith("}")) {
 	    return (String) value;
 	    // }
 	    // return jsonMapper.writeValueAsString(value);
 	} catch (Exception e) {
 	    throw new RuntimeException(e.getMessage(), e);
 	}
     }
 }
