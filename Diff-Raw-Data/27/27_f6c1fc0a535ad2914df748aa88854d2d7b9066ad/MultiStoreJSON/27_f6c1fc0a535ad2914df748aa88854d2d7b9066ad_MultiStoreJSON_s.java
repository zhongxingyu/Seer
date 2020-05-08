 package org.nts.pigutils.udfs;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.text.NumberFormat;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.WritableComparable;
 import org.apache.hadoop.io.compress.BZip2Codec;
 import org.apache.hadoop.io.compress.CompressionCodec;
 import org.apache.hadoop.io.compress.GzipCodec;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.OutputFormat;
 import org.apache.hadoop.mapreduce.RecordWriter;
 import org.apache.hadoop.mapreduce.TaskAttemptContext;
 import org.apache.hadoop.mapreduce.TaskID;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
 import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
 import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
 import org.apache.hadoop.util.ReflectionUtils;
 import org.apache.pig.ResourceSchema;
 import org.apache.pig.ResourceSchema.ResourceFieldSchema;
 import org.apache.pig.ResourceStatistics;
 import org.apache.pig.StoreFunc;
 import org.apache.pig.StoreMetadata;
 import org.apache.pig.backend.executionengine.ExecException;
 import org.apache.pig.data.DataBag;
 import org.apache.pig.data.DataType;
 import org.apache.pig.data.Tuple;
 import org.apache.pig.impl.util.StorageUtil;
 import org.apache.pig.impl.util.UDFContext;
 import org.apache.pig.impl.util.Utils;
 import org.codehaus.jackson.JsonEncoding;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonGenerator;
 
 /**
  * 
  * Shamelessly copied from the Piggybank MultiStore, and JSONStore.
  * What makes this class different is that it stores the data as json objects using the multistore output splitting.
  * 
  */
 public class MultiStoreJSON extends StoreFunc implements StoreMetadata {
 
 	private int splitFieldIndex = -1; // Index of the key field
 	private String fieldDel; // delimiter of the output record.
 	private Compression comp; // Compression type of output data.
 
 	private String udfSignature;
     private ResourceSchema schema;
 	// Compression types supported by this store
 	enum Compression {
 		none, bz2, bz, gz;
 	};
 
 	public MultiStoreJSON(String parentPathStr, String splitFieldIndex) {
 		this(parentPathStr, splitFieldIndex, "none");
 	}
 
 	public MultiStoreJSON(String parentPathStr, String splitFieldIndex,
 			String compression) {
 		this(parentPathStr, splitFieldIndex, compression, "\\t");
 	}
 
 	/**
 	 * Constructor
 	 * 
 	 * @param parentPathStr
 	 *            Parent output dir path
 	 * @param splitFieldIndex
 	 *            key field index
 	 * @param compression
 	 *            'bz2', 'bz', 'gz' or 'none'
 	 * @param fieldDel
 	 *            Output record field delimiter.
 	 */
 	public MultiStoreJSON(String parentPathStr, String splitFieldIndex,
 			String compression, String fieldDel) {
 		this.splitFieldIndex = Integer.parseInt(splitFieldIndex);
 		this.fieldDel = fieldDel;
 		try {
 			this.comp = (compression == null) ? Compression.none : Compression
 					.valueOf(compression.toLowerCase());
 		} catch (IllegalArgumentException e) {
 			System.err.println("Exception when converting compression string: "
 					+ compression + " to enum. No compression will be used");
 			this.comp = Compression.none;
 		}
 
 	}
 
 	// --------------------------------------------------------------------------
 	// Implementation of StoreFunc
 
 	private RecordWriter<String, Tuple> writer;
 
 	@Override
 	public void putNext(Tuple tuple) throws IOException {
 		if (tuple.size() <= splitFieldIndex) {
 			throw new IOException("split field index:" + this.splitFieldIndex
 					+ " >= tuple size:" + tuple.size());
 		}
 		Object field = null;
 		try {
 			field = tuple.get(splitFieldIndex);
 		} catch (ExecException exec) {
 			throw new IOException(exec);
 		}
 		try {
 			writer.write(String.valueOf(field), tuple);
 		} catch (InterruptedException e) {
 			throw new IOException(e);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public OutputFormat getOutputFormat() throws IOException {
 		MultiStorageOutputFormat format = new MultiStorageOutputFormat(schema);
 		format.setKeyValueSeparator(fieldDel);
 		return format;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void prepareToWrite(RecordWriter writer) throws IOException {
 		UDFContext udfc = UDFContext.getUDFContext();
         Properties p =
             udfc.getUDFProperties(this.getClass(), new String[]{udfSignature});
        String strSchema = p.getProperty("json.schema");
         if (strSchema == null) {
             throw new IOException("Could not find schema in UDF context");
         }
 
         // Parse the schema from the string stored in the properties object.
         schema = new ResourceSchema(Utils.getSchemaFromString(strSchema));
 
 		this.writer = writer;
 	}
 
 	@Override
 	public void setStoreLocation(String location, Job job) throws IOException {
 		job.getConfiguration().set("mapred.textoutputformat.separator", "");
 		FileOutputFormat.setOutputPath(job, new Path(location));
 		if (comp == Compression.bz2 || comp == Compression.bz) {
 			FileOutputFormat.setCompressOutput(job, true);
 			FileOutputFormat.setOutputCompressorClass(job, BZip2Codec.class);
 		} else if (comp == Compression.gz) {
 			FileOutputFormat.setCompressOutput(job, true);
 			FileOutputFormat.setOutputCompressorClass(job, GzipCodec.class);
 		}
 	}
 
 	// --------------------------------------------------------------------------
 	// Implementation of OutputFormat
 
 	public static class MultiStorageOutputFormat extends
 			TextOutputFormat<String, Tuple> {
 
 		private String keyValueSeparator = "\\t";
 		private byte fieldDel = '\t';
 
 		
 		private final ResourceSchema schema;
 		
 		public MultiStorageOutputFormat(ResourceSchema schema) {
 			super();
 			this.schema = schema;
 		}
 
 		@Override
 		public RecordWriter<String, Tuple> getRecordWriter(
 				TaskAttemptContext context) throws IOException,
 				InterruptedException {
 
 			final TaskAttemptContext ctx = context;
 			
 			return new RecordWriter<String, Tuple>() {
 
 				private Map<String, MyLineRecordWriter> storeMap = new HashMap<String, MyLineRecordWriter>();
 
 				private static final int BUFFER_SIZE = 1024;
 
 				private ByteArrayOutputStream mOut = new ByteArrayOutputStream(
 						BUFFER_SIZE);
 				final JsonFactory jsonFactory = new JsonFactory();
 
 				@Override
 				public void write(String key, Tuple val) throws IOException {
 
 					JsonGenerator json = jsonFactory.createJsonGenerator(mOut,
 							JsonEncoding.UTF8);
 
 					// Write the beginning of the top level tuple object
 					json.writeStartObject();
 					
 					ResourceFieldSchema[] fields = schema.getFields();
 					for (int i = 0; i < val.size(); i++) {
 						writeField(json, fields[i], val.get(i));
 					}
 					json.writeEndObject();
 					json.close();
 
 					getStore(key).write(null, new Text(mOut.toByteArray()));
 
 					mOut.reset();
 				}
 
 				@Override
 				public void close(TaskAttemptContext context)
 						throws IOException {
 					for (MyLineRecordWriter out : storeMap.values()) {
 						out.close(context);
 					}
 				}
 
 				private MyLineRecordWriter getStore(String fieldValue)
 						throws IOException {
 					MyLineRecordWriter store = storeMap.get(fieldValue);
 					if (store == null) {
 						DataOutputStream os = createOutputStream(fieldValue);
 						store = new MyLineRecordWriter(os, keyValueSeparator);
 						storeMap.put(fieldValue, store);
 					}
 					return store;
 				}
 
 				private DataOutputStream createOutputStream(String fieldValue)
 						throws IOException {
 					Configuration conf = ctx.getConfiguration();
 					TaskID taskId = ctx.getTaskAttemptID().getTaskID();
 
 					// Check whether compression is enabled, if so get the
 					// extension and add them to the path
 					boolean isCompressed = getCompressOutput(ctx);
 					CompressionCodec codec = null;
 					String extension = "";
 					if (isCompressed) {
 						Class<? extends CompressionCodec> codecClass = getOutputCompressorClass(
 								ctx, GzipCodec.class);
 						codec = (CompressionCodec) ReflectionUtils.newInstance(
 								codecClass, ctx.getConfiguration());
 						extension = codec.getDefaultExtension();
 					}
 
 					Path path = new Path(fieldValue + extension, fieldValue
 							+ '-'
 							+ NumberFormat.getInstance().format(taskId.getId())
 							+ extension);
 					Path workOutputPath = ((FileOutputCommitter) getOutputCommitter(ctx))
 							.getWorkPath();
 					Path file = new Path(workOutputPath, path);
 					FileSystem fs = file.getFileSystem(conf);
 					FSDataOutputStream fileOut = fs.create(file, false);
 
 					if (isCompressed)
 						return new DataOutputStream(
 								codec.createOutputStream(fileOut));
 					else
 						return fileOut;
 				}
 
 			};
 		}
 
 		public void setKeyValueSeparator(String sep) {
 			keyValueSeparator = sep;
 			fieldDel = StorageUtil.parseFieldDel(keyValueSeparator);
 		}
 
 		 @SuppressWarnings("unchecked")
 		    private void writeField(JsonGenerator json,
 		                            ResourceFieldSchema field, 
 		                            Object d) throws IOException {
 
 		        // If the field is missing or the value is null, write a null
 		        if (d == null) {
 		            json.writeNullField(field.getName());
 		            return;
 		        }
 
 		        // Based on the field's type, write it out
 		        switch (field.getType()) {
 		        case DataType.INTEGER:
 		            json.writeNumberField(field.getName(), (Integer)d);
 		            return;
 
 		        case DataType.LONG:
 		            json.writeNumberField(field.getName(), (Long)d);
 		            return;
 
 		        case DataType.FLOAT:
 		            json.writeNumberField(field.getName(), (Float)d);
 		            return;
 
 		        case DataType.DOUBLE:
 		            json.writeNumberField(field.getName(), (Double)d);
 		            return;
 
 		        case DataType.BYTEARRAY:
 		            json.writeStringField(field.getName(), d.toString());
 		            return;
 
 		        case DataType.CHARARRAY:
 		            json.writeStringField(field.getName(), (String)d);
 		            return;
 
 		        case DataType.MAP:
 		            json.writeFieldName(field.getName());
 		            json.writeStartObject();
 		            for (Map.Entry<String, Object> e : ((Map<String, Object>)d).entrySet()) {
 		                json.writeStringField(e.getKey(), e.getValue().toString());
 		            }
 		            json.writeEndObject();
 		            return;
 
 		        case DataType.TUPLE:
 		            json.writeFieldName(field.getName());
 		            json.writeStartObject();
 
 		            ResourceSchema s = field.getSchema();
 		            if (s == null) {
 		                throw new IOException("Schemas must be fully specified to use "
 		                    + "this storage function.  No schema found for field " +
 		                    field.getName());
 		            }
 		            ResourceFieldSchema[] fs = s.getFields();
 
 		            for (int j = 0; j < fs.length; j++) {
 		                writeField(json, fs[j], ((Tuple)d).get(j));
 		            }
 		            json.writeEndObject();
 		            return;
 
 		        case DataType.BAG:
 		            json.writeFieldName(field.getName());
 		            json.writeStartArray();
 		            s = field.getSchema();
 		            if (s == null) {
 		                throw new IOException("Schemas must be fully specified to use "
 		                    + "this storage function.  No schema found for field " +
 		                    field.getName());
 		            }
 		            fs = s.getFields();
 		            if (fs.length != 1 || fs[0].getType() != DataType.TUPLE) {
 		                throw new IOException("Found a bag without a tuple "
 		                    + "inside!");
 		            }
 		            // Drill down the next level to the tuple's schema.
 		            s = fs[0].getSchema();
 		            if (s == null) {
 		                throw new IOException("Schemas must be fully specified to use "
 		                    + "this storage function.  No schema found for field " +
 		                    field.getName());
 		            }
 		            fs = s.getFields();
 		            for (Tuple t : (DataBag)d) {
 		                json.writeStartObject();
 		                for (int j = 0; j < fs.length; j++) {
 		                    writeField(json, fs[j], t.get(j));
 		                }
 		                json.writeEndObject();
 		            }
 		            json.writeEndArray();
 		            return;
 		        }
 		    }
 		// ------------------------------------------------------------------------
 		//
 
 		protected static class MyLineRecordWriter extends
 				TextOutputFormat.LineRecordWriter<WritableComparable, Text> {
 
 			public MyLineRecordWriter(DataOutputStream out,
 					String keyValueSeparator) {
 				super(out, keyValueSeparator);
 			}
 		}
 	}
 
 	@Override
 	public void checkSchema(ResourceSchema s) throws IOException {
 		super.checkSchema(s);
 		UDFContext udfc = UDFContext.getUDFContext();
 		Properties p = udfc.getUDFProperties(this.getClass(),
 				new String[] { udfSignature });
		p.setProperty("udf.schema", s.toString());
 	}
 
 	@Override
 	public void setStoreFuncUDFContextSignature(String signature) {
 		super.setStoreFuncUDFContextSignature(signature);
		this.udfSignature = udfSignature;
 	}
 
	public void storeSchema(ResourceSchema schema, String arg1, Job arg2)
 			throws IOException {

 	}
 
 	public void storeStatistics(ResourceStatistics arg0, String arg1, Job arg2)
 			throws IOException {
		// TODO Auto-generated method stub

 	}
 
 }
