 package jp.gr.java_conf.afterthesunrise.thadoop.comparator;
 
 import java.io.IOError;
 import java.io.IOException;
 
 import org.apache.hadoop.io.DataInputBuffer;
 import org.apache.hadoop.io.DataOutputBuffer;
 import org.apache.hadoop.io.MapWritable;
 import org.apache.hadoop.io.RawComparator;
import org.apache.hadoop.io.ShortWritable;
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.io.WritableComparator;
 import org.apache.thrift.TFieldIdEnum;
 
 /**
  * @author takanori.takase
  */
 public class TMapWritableComparator<F extends TFieldIdEnum> implements
 		RawComparator<MapWritable> {
 
 	private final DataInputBuffer buffer = new DataInputBuffer();
 
 	private final MapWritable writable1 = new MapWritable();
 
 	private final MapWritable writable2 = new MapWritable();
 
 	private final DataOutputBuffer out = new DataOutputBuffer();
 
 	private final F[] fields;
 
 	private final ShortWritable[] keys;
 
 	protected TMapWritableComparator(F... f) {
 
 		fields = f.clone();
 
 		ShortWritable[] keys = new ShortWritable[fields.length];
 
 		for (int i = 0; i < fields.length; i++) {
 
 			short id = fields[i].getThriftFieldId();
 
 			keys[i] = new ShortWritable(id);
 
 		}
 
 		this.keys = keys;
 
 	}
 
 	@Override
 	public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
 
 		try {
 
 			buffer.reset(b1, s1, l1);
 			writable1.clear();
 			writable1.readFields(buffer);
 
 			buffer.reset(b2, s2, l2);
 			writable2.clear();
 			writable2.readFields(buffer);
 
 		} catch (IOException e) {
 			throw new IOError(e);
 		}
 
 		return compare(writable1, writable2);
 
 	}
 
 	@Override
 	public int compare(MapWritable o1, MapWritable o2) {
 
 		int result = 0;
 
 		for (int i = 0; i < keys.length && result == 0; i++) {
 
 			ShortWritable key = keys[i];
 
 			Writable v1 = o1.get(key);
 
 			Writable v2 = o2.get(key);
 
 			result = compare(fields[i], v1, v2);
 
 		}
 
 		return result;
 
 	}
 
 	protected int compare(F field, Writable w1, Writable w2) {
 
 		try {
 
 			out.reset();
 
 			if (w1 != null) {
 				w1.write(out);
 			}
 
 			int l1 = out.getLength();
 
 			if (w2 != null) {
 				w2.write(out);
 			}
 
 			int l2 = out.getLength() - l1;
 
 			byte[] b = out.getData();
 
 			return WritableComparator.compareBytes(b, 0, l1, b, l1, l2);
 
 		} catch (IOException e) {
 			throw new IOError(e);
 		}
 
 	}
 
 }
