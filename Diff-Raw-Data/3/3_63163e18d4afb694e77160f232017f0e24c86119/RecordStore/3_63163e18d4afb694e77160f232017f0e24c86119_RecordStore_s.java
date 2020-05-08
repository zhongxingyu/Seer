 package javax.microedition.rms;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.List;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 
 import com.buglabs.bug.module.gps.Activator;
 
 
 public class RecordStore {
 	public static final String AUTHMODE_ANY = "AUTHMODE_ANY";
 	private static final String PID_PREFIX = "RECORDSTORE_";
 	private static final String KEYSTORE_KEY = "KEYSTORE_KEY";
 	
 	private String name;
 	private Dictionary props;
 	private int recordIndex = 0;
 
 	protected RecordStore(String name, Dictionary props) {
 		this.name = name;
 		this.props = props;
 	}
 	
 	public static RecordStore openRecordStore(String recordStoreName, boolean createIfNecessary) throws RecordStoreException {
 		ConfigurationAdmin ca = getConfigurationAdmin(Activator.getInstance().getBundleContext());
 		
 		if (ca == null) {
 			throw new RecordStoreException("Unable to access internal persistent store: ConfigurationAdmin.");
 		}
 		
 		try {
 			Configuration c= ca.getConfiguration(createPid(recordStoreName));
 			
 			if (c.getProperties().get(KEYSTORE_KEY) == null && !createIfNecessary) {
 				throw new RecordStoreException("Record store " + recordStoreName + " does not exist."); 
 			}
 			Dictionary keystore = (Dictionary) c.getProperties().get(KEYSTORE_KEY);
 			if (keystore == null) {
 				keystore = new Hashtable();
 				c.getProperties().put(KEYSTORE_KEY, keystore);
 			} 
 			
 			return new RecordStore(recordStoreName, keystore);
 		} catch (IOException e) {
 			throw new RecordStoreException("Unable to access internal persistent store: ConfigurationAdmin.", e);
 		}
 	}
 
 	public int addRecord(byte[] data, int offset, int numBytes) throws RecordStoreFullException {
 		props.put(new Integer(recordIndex), new ByteArray(data));
 		recordIndex ++;
 		return recordIndex - 1;
 	}
 
 	public static void deleteRecordStore(String recordStoreName) throws RecordStoreNotFoundException {
 		ConfigurationAdmin ca = getConfigurationAdmin(Activator.getInstance().getBundleContext());
 
 		if (ca == null) {
 			throw new RecordStoreNotFoundException("Unable to access internal persistent store: ConfigurationAdmin.");
 		}
 		
 		try {
 			Configuration c = ca.getConfiguration(createPid(recordStoreName));
 			
 			if (c.getProperties() != null) {
 				c.delete();
 			}
 		} catch (IOException e) {
 			throw new RecordStoreNotFoundException(e.getMessage(), e);
 		}
 	}
 
 	public static String[] listRecordStores() throws IOException {
 		ConfigurationAdmin ca = getConfigurationAdmin(Activator.getInstance().getBundleContext());
 
 		if (ca == null) {
 			throw new IOException("Unable to access internal persistent store: ConfigurationAdmin.");
 		}
 		
 		List stores = new ArrayList();
 		try {
 			Configuration[] configurations = ca.listConfigurations(null);
 			
 			for (int i = 0; i < configurations.length; ++i) {
 				if (configurations[i].getPid().startsWith(PID_PREFIX)) {
 					stores.add(configurations[i].getPid().substring(PID_PREFIX.length()));
 				}
 			}
 			
 		} catch (InvalidSyntaxException e) {
 			throw new IOException(e.getMessage());
 		}
 		
 		return (String []) stores.toArray(new String[stores.size()]);
 	}
 
 	public void setMode(String authmodeAny, boolean b) {
 		//TODO implement
 		//this is unimplemented as it doesn't seem to be used by openlapi.
 	}
 
 	public byte[] getRecord(int iD) throws RecordStoreException {
 		ByteArray ba = (ByteArray) props.get(new Integer(iD));
 		
 		if (ba == null) {
 			throw new RecordStoreException("Record ID " + iD + " does not exist.");
 		}
 		
 		return ba.getBytes();
 	}
 
 	public void deleteRecord(int iD) throws RecordStoreException {
 		if (props.get(new Integer(iD)) == null) {
 			throw new RecordStoreException("Record ID " + iD + " does not exist.");
 		}
 	}
 
 	public RecordEnumeration enumerateRecords(RecordFilter filter, RecordComparator comparator, boolean keepUpdated) {
 
 		if (filter != null || comparator != null) {
 			throw new RuntimeException("RMS filter and sort features unimplemented.");
 		}
 		
 		return new RecordEnumerationImpl(props);
 			
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public int getNumRecords() throws RecordStoreException {
 		return props.size();
 	}
 
 	public void setRecord(int recordId, byte[] data, int i, int length) throws RecordStoreException {
 		//TODO handle length, index.  openlapi always seems to store full byte arrays.
 		props.put(new Integer(recordId), new ByteArray(data));
 	}
 	
 	private static ConfigurationAdmin getConfigurationAdmin(BundleContext c) {
 		ServiceReference sr = c.getServiceReference(ConfigurationAdmin.class.getName());
 		if (sr != null) {
 			return (ConfigurationAdmin) c.getService(sr);
 		}
 		
 		return null;
 	}
 	
 
 	private static String createPid(String recordStoreName) {
 		return PID_PREFIX + recordStoreName;
 	}
 	
 	/**
 	 * Stores byte arrays for RMS records.
 	 * @author kgilmer
 	 *
 	 */
 	private class ByteArray {
 		private final byte[] bytes;
 
 		public ByteArray(byte[] data) {
 			this.bytes = data;
 		}
 		
 		public byte[] getBytes() {
 			return bytes;
 		}
 	}
 	
 	private class RecordEnumerationImpl implements RecordEnumeration {
 		private final Enumeration keys;
 
 		public RecordEnumerationImpl(Dictionary properties) {	
 			keys = properties.keys();
 		}
 
 		public boolean hasNextElement() {
 			return keys.hasMoreElements();
 		}
 
 		public int nextRecordId() {
 			return ((Integer)keys.nextElement()).intValue();
 		}
 	}
 }
