 package jp.gr.java_conf.afterthesunrise.thadoop;
 
 import static jp.gr.java_conf.afterthesunrise.thadoop.sample.ThadoopSample._Fields.FIELD_BOOLEAN;
 import static jp.gr.java_conf.afterthesunrise.thadoop.sample.ThadoopSample._Fields.FIELD_BYTE;
 import static jp.gr.java_conf.afterthesunrise.thadoop.sample.ThadoopSample._Fields.FIELD_DOUBLE;
 import static jp.gr.java_conf.afterthesunrise.thadoop.sample.ThadoopSample._Fields.FIELD_INT;
 import static jp.gr.java_conf.afterthesunrise.thadoop.sample.ThadoopSample._Fields.FIELD_LONG;
 import static jp.gr.java_conf.afterthesunrise.thadoop.sample.ThadoopSample._Fields.FIELD_SHORT;
 import static jp.gr.java_conf.afterthesunrise.thadoop.sample.ThadoopSample._Fields.FIELD_STRING;
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 import java.util.SortedMap;
 
 import jp.gr.java_conf.afterthesunrise.thadoop.sample.ThadoopSample;
 import jp.gr.java_conf.afterthesunrise.thadoop.sample.ThadoopSample._Fields;
 
 import org.apache.hadoop.mapreduce.RecordReader;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
 import org.apache.pig.ResourceSchema;
 import org.apache.pig.ResourceSchema.ResourceFieldSchema;
 import org.apache.pig.data.DataType;
 import org.apache.pig.data.Tuple;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author takanori.takase
  */
 public class AbstractTStorageTest {
 
 	/**
 	 * Sample writable implementation for test.
 	 */
 	private class SampleWritable extends AbstractTWritable<ThadoopSample> {
 
 		private final ThadoopSample value = new ThadoopSample();
 
 		@Override
		public ThadoopSample get() {
 			return value;
 		}
 
 	}
 
 	/**
 	 * Sample storage implementation for test.
 	 */
 	private class SampleStorage extends
 			AbstractTStorage<_Fields, SampleWritable> {
 
 		@Override
 		protected SortedMap<_Fields, Byte> getFieldIds() {
 			return transformFields(ThadoopSample.metaDataMap);
 		}
 
 	}
 
 	private AbstractTStorage<_Fields, SampleWritable> target;
 
 	@Before
 	public void setUp() {
 		target = new SampleStorage();
 	}
 
 	@Test
 	public void testGetInputFormat() {
 		assertTrue(target.getInputFormat() instanceof SequenceFileInputFormat);
 	}
 
 	@Test
 	public void testGetNext() throws IOException, InterruptedException {
 
 		// Sample thrift instance
 		SampleWritable writable = new SampleWritable();
 		ThadoopSample sample = writable.get();
 		sample.setFieldBoolean(Boolean.TRUE);
 		sample.setFieldByte(Byte.MAX_VALUE);
 		sample.setFieldShort(Short.MAX_VALUE);
 		sample.setFieldInt(Integer.MAX_VALUE);
 		sample.setFieldLong(Long.MAX_VALUE);
 		sample.setFieldDouble(Double.MAX_VALUE);
 		sample.setFieldString(toString());
 
 		// Mocked reader
 		@SuppressWarnings("unchecked")
 		RecordReader<?, SampleWritable> reader = mock(RecordReader.class);
 		when(reader.nextKeyValue()).thenReturn(true, false);
 		when(reader.getCurrentValue()).thenReturn(writable);
 		target.prepareToRead(reader, null);
 
 		// First record (null)
 		Tuple tuple = target.getNext();
 		assertThat(tuple, notNullValue());
 		assertThat(tuple.size(), equalTo(7));
 		assertEquals(sample.isFieldBoolean(), tuple.get(0));
 		assertEquals(sample.getFieldByte(), tuple.get(1));
 		assertEquals(sample.getFieldShort(), tuple.get(2));
 		assertEquals(sample.getFieldInt(), tuple.get(3));
 		assertEquals(sample.getFieldLong(), tuple.get(4));
 		assertEquals(sample.getFieldDouble(), tuple.get(5));
 		assertEquals(sample.getFieldString(), tuple.get(6));
 
 		// Second record (null)
 		assertNull(target.getNext());
 
 	}
 
 	@Test
 	public void testGetSchema() {
 
 		ResourceSchema schema = target.getSchema(null, null);
 
 		ResourceFieldSchema[] fields = schema.getFields();
 
 		assertThat(fields.length, equalTo(7));
 
 		// Check field names
 		assertThat(fields[0].getName(), equalTo(FIELD_BOOLEAN.getFieldName()));
 		assertThat(fields[1].getName(), equalTo(FIELD_BYTE.getFieldName()));
 		assertThat(fields[2].getName(), equalTo(FIELD_SHORT.getFieldName()));
 		assertThat(fields[3].getName(), equalTo(FIELD_INT.getFieldName()));
 		assertThat(fields[4].getName(), equalTo(FIELD_LONG.getFieldName()));
 		assertThat(fields[5].getName(), equalTo(FIELD_DOUBLE.getFieldName()));
 		assertThat(fields[6].getName(), equalTo(FIELD_STRING.getFieldName()));
 
 		// Check field data types
 		assertThat(fields[0].getType(), equalTo(DataType.BOOLEAN));
 		assertThat(fields[1].getType(), equalTo(DataType.BYTE));
 		assertThat(fields[2].getType(), equalTo(DataType.INTEGER));
 		assertThat(fields[3].getType(), equalTo(DataType.INTEGER));
 		assertThat(fields[4].getType(), equalTo(DataType.LONG));
 		assertThat(fields[5].getType(), equalTo(DataType.DOUBLE));
 		assertThat(fields[6].getType(), equalTo(DataType.CHARARRAY));
 
 	}
 
 }
