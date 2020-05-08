 package me.sheimi.pig.storage;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import me.sheimi.hadoop.seq.LocalSetup;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.io.serializer.SerializationFactory;
 import org.apache.hadoop.mapreduce.InputFormat;
 import org.apache.hadoop.mapreduce.Job;
 import org.apache.hadoop.mapreduce.RecordReader;
 import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
 import org.apache.hadoop.mapreduce.lib.input.SequenceFileRecordReader;
 import org.apache.pig.FileInputLoadFunc;
 import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigSplit;
 import org.apache.pig.data.DataByteArray;
 import org.apache.pig.data.DataType;
 import org.apache.pig.data.Tuple;
 import org.apache.pig.data.TupleFactory;
 
import me.sheimi.magic.image.*;
 
 /**
  * A Loader for My Specific SequenceFiles. Key is Text (the filename) Value is
  * BytesWritable
  **/
 
 public class SequenceFileLoader extends FileInputLoadFunc {
 
 	private SequenceFileRecordReader<Text, BytesWritable> reader;
 
 	private Text key;
 	private BytesWritable value;
 
 	private ArrayList<Object> mProtoTuple = null;
 
 	protected static final Log LOG = LogFactory
 			.getLog(SequenceFileLoader.class);
 	protected TupleFactory mTupleFactory = TupleFactory.getInstance();
 	protected SerializationFactory serializationFactory;
 
 	protected byte keyType = DataType.CHARARRAY;
 	protected byte valType = DataType.BYTEARRAY;
 
 	private final byte BYTESWRITABLE = 100;
 
 	public SequenceFileLoader() {
 		mProtoTuple = new ArrayList<Object>(2);
 	}
 
 	protected Object translateBytesWritable(BytesWritable w) {
 		byte[] data = w.getBytes();
    Image image = Image.decode(data, null);
 		return new DataByteArray(image.getImage());
 	}
 
 	@Override
 	public Tuple getNext() throws IOException {
 		boolean next = false;
 		try {
 			next = reader.nextKeyValue();
 		} catch (InterruptedException e) {
 			throw new IOException(e);
 		}
 
 		if (!next)
 			return null;
 
 		key = reader.getCurrentKey();
 		value = reader.getCurrentValue();
 
 		mProtoTuple.add(key.toString());
 		mProtoTuple.add(translateBytesWritable(value));
 		Tuple t = mTupleFactory.newTuple(mProtoTuple);
 		mProtoTuple.clear();
 		return t;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public InputFormat getInputFormat() throws IOException {
 		return new SequenceFileInputFormat<Text, BytesWritable>();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void prepareToRead(RecordReader reader, PigSplit split)
 			throws IOException {
 		this.reader = (SequenceFileRecordReader<Text, BytesWritable>) reader;
 	}
 
 	@Override
 	public void setLocation(String location, Job job) throws IOException {
 		FileInputFormat.setInputPaths(job, location);
 	}
 }
