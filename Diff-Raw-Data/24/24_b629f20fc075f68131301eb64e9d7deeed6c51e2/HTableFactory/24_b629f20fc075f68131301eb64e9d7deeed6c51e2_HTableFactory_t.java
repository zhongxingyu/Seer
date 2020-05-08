 package ch.unibe.scg.cells.hadoop;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 import javax.inject.Inject;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.client.HTable;
 
 /** Make a new HTable object (not the underlying database) for a given name. */
 class HTableFactory implements Serializable {
 	final private static long serialVersionUID = 1L;
 
 	private transient Configuration hbaseConfig;
 
 	@Inject
 	HTableFactory(Configuration hbaseConfig) {
 		this.hbaseConfig = hbaseConfig;
 	}
 
 	private void writeObject(ObjectOutputStream out) throws IOException {
 		out.defaultWriteObject();
 		hbaseConfig.write(out);
 	}
 
 	private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
 		in.defaultReadObject();
 		hbaseConfig = new Configuration();
 		hbaseConfig.readFields(in);
 	}
 
 	HTable make(String tableName) {
 		try {
 			HTable htable = new HTable(hbaseConfig, tableName);
 			htable.setAutoFlush(false);
 			htable.setWriteBufferSize(1024 * 1024 * 12);
 			htable.getTableDescriptor().setDeferredLogFlush(true);
 			return htable;
 		} catch (IOException e) {
			throw new RuntimeException("Couldn't make table " + tableName, e);
 		}
 	}
 }
