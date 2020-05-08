 package jp.gr.java_conf.afterthesunrise.thadoop.comparator;
 
 import static jp.gr.java_conf.afterthesunrise.thadoop.sample.ThadoopSample._Fields.V_INT;
 import static jp.gr.java_conf.afterthesunrise.thadoop.sample.ThadoopSample._Fields.V_LONG;
 import static jp.gr.java_conf.afterthesunrise.thadoop.sample.ThadoopSample._Fields.V_SHORT;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.util.Arrays;
 
 import jp.gr.java_conf.afterthesunrise.thadoop.sample.ThadoopSample._Fields;
 
 import org.apache.commons.lang.ArrayUtils;
import org.apache.hadoop.hive.serde2.io.ShortWritable;
 import org.apache.hadoop.io.DataOutputBuffer;
 import org.apache.hadoop.io.IntWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.MapWritable;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author takanori.takase
  */
 public class TMapWritableComparatorTest {
 
 	private TMapWritableComparator<_Fields> target;
 
 	private _Fields[] fields;
 
 	private ShortWritable[] keys;
 
 	@Before
 	public void setUp() throws Exception {
 
 		fields = new _Fields[] { V_SHORT, V_INT, V_LONG };
 
 		keys = new ShortWritable[fields.length];
 
 		for (int i = 0; i < fields.length; i++) {
 			keys[i] = new ShortWritable(fields[i].getThriftFieldId());
 		}
 
 		target = new TMapWritableComparator<_Fields>(fields);
 
 	}
 
 	@Test
 	public void testCompare() throws IOException {
 
 		MapWritable m1 = new MapWritable();
 		MapWritable m2 = new MapWritable();
 
 		m1.put(keys[0], new LongWritable(1L));
 		m2.put(keys[0], new LongWritable(1L));
 
 		m1.put(keys[2], new IntWritable(3));
 		m2.put(keys[2], new IntWritable(2));
 
 		DataOutputBuffer out1 = new DataOutputBuffer();
 		DataOutputBuffer out2 = new DataOutputBuffer();
 
 		m1.write(out1);
 		m2.write(out2);
 
 		byte[] b1 = Arrays.copyOf(out1.getData(), out1.getLength());
 		byte[] b2 = Arrays.copyOf(out2.getData(), out2.getLength());
 		byte[] b = ArrayUtils.addAll(b1, b2);
 
 		assertTrue(target.compare(b, 0, b1.length, b, b1.length, b2.length) > 0);
 
 		assertTrue(target.compare(m1, m2) > 0);
 
 	}
 
 }
