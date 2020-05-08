 package com.trifork.vmap;
 
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.activation.CommandInfo;
 import javax.activation.CommandMap;
 import javax.activation.DataContentHandler;
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.activation.MimeType;
 import javax.activation.MimeTypeParseException;
 
 import com.trifork.activation.IO;
 import com.trifork.vmap.VClock.Time;
 
 public class VectorMap {
 
 	public static class VEntry {
 
 		public final VClock vClock;
 		public final DataSource[] values;
 
 		public VEntry(VClock vClock, DataSource[] ds) {
 			this.vClock = vClock;
 			this.values = ds;
 		}
 
 	}
 
 	public static final String MIME_TYPE_PROTOBUF_STRING;
 	public static final MimeType MIME_TYPE_PROTOBUF;
 
 	static {
 		try {
 			MIME_TYPE_PROTOBUF = new MimeType("application/x-protobuf");
 			MIME_TYPE_PROTOBUF.setParameter("proto", "vectormap.proto");
 			MIME_TYPE_PROTOBUF.setParameter("message", "PBVectorMap");
 			MIME_TYPE_PROTOBUF_STRING = MIME_TYPE_PROTOBUF.toString();
 		} catch (MimeTypeParseException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private String thisPeer;
 	Map<String, VEntry> content;
 	private VClock update_vclock;
 
 	VectorMap(Map<String, VEntry> content) {
 		this.content = content;
 	}
 
 	VectorMap(String thisPeer, Map<String, VEntry> content) {
 
 		this.content = content;
 		setThisPeer(thisPeer);
 	}
 
 	public void setThisPeer(String thisPeer2) {
 		this.thisPeer = thisPeer2;
 		this.update_vclock = computeUpdateVClock();
 	}
 
 	public VectorMap(String thisPeer) throws IOException {
 		this(thisPeer, new HashMap<String, VEntry>());
 	}
 
 	public VectorMap() throws IOException {
 		this(new HashMap<String, VEntry>());
 	}
 
 	@SuppressWarnings("unchecked")
 	private VClock computeUpdateVClock() {
 		Map<String, VClock.Time> max = new HashMap<String, VClock.Time>();
 		for (VEntry ent : content.values()) {
 			ent.vClock.updateMax(max);
 		}
 
 		Time thisTime = max.get(thisPeer);
 		if (thisTime == null) {
 			max.put(thisPeer,
 					thisTime = new Time(1, System.currentTimeMillis()));
 		} else {
 			max.put(thisPeer, thisTime = thisTime.increment());
 		}
 
 		Entry<String, Time>[] ents = max.entrySet().toArray(
 				new Map.Entry[max.size()]);
 		Arrays.sort(ents, new Comparator<Entry<String, Time>>() {
 			@Override
 			public int compare(Entry<String, Time> o1, Entry<String, Time> o2) {
 				if (o1.getValue().time > o2.getValue().time) {
 					return -1;
 				} else if (o1.getValue().time < o2.getValue().time) {
 					return 1;
 				} else {
 					return 0;
 				}
 			}
 		});
 
 		return new VClock(ents);
 	}
 
 	public <T> T get(String key, Class<T> representationClass)
 			throws UnsupportedFlavorException, IOException {
 
 		VEntry enc = content.get(key);
 		DataSource encodedValue = enc.values[0];
 		if (encodedValue == null)
 			return null;
 
 		return decode(encodedValue, representationClass);
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T> T decode(DataSource ds, Class<T> representationClass)
 			throws IOException, UnsupportedFlavorException {
 
 		if (representationClass == DataSource.class)
 			return (T) ds;
 
 		if (representationClass == InputStream.class)
 			return (T) ds.getInputStream();
 
 		if (representationClass == byte[].class) {
 			ByteArrayOutputStream bao = new ByteArrayOutputStream();
 			IO.copystream(ds.getInputStream(), bao);
 			return (T) bao.toByteArray();
 		}
 
 		CommandMap defaultCommandMap = CommandMap.getDefaultCommandMap();
 		CommandInfo cc = defaultCommandMap.getCommand(ds.getContentType(),
 				"content-handler", ds);
 
 		DataContentHandler co;
 		try {
 			co = (DataContentHandler) cc.getCommandObject(null, null);
 		} catch (ClassNotFoundException e) {
 			throw new IOException(e);
 		}
 
 		DataFlavor[] flavors = co.getTransferDataFlavors();
 		for (int i = 0; i < flavors.length; i++) {
 			if (flavors[i].getRepresentationClass() == representationClass) {
 				return (T) co.getTransferData(flavors[i], ds);
 			}
 		}
 		
 		throw new UnsupportedEncodingException();
 	}
 
 	public void remove(String key) {
 		try {
 			put(key, (DataSource)null);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void put(String key, String string) throws IOException {
 		put(key, "text/plain;charset=utf-8", string);
 	}
 
	public void put(String key, byte[] data) throws IOException {
		put(key, "application/binary", data);
	}

 	public void put(String key, String mimeType, Object value)
 			throws IOException {
 		put(key, new DataHandler(value, mimeType).getDataSource());
 	}
 
 	public void put(String key, DataSource ds) throws IOException {
 		update_vclock.timeStamp(thisPeer);
 		if (ds == null) {
 			content.put(key, new VEntry(update_vclock,
 					new DataSource[] { null }));
 		} else {
 			content.put(key, new VEntry(update_vclock, new DataSource[] { ds }));
 		}
 	}
 
 }
