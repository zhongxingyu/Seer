 package pls;
 
 import java.nio.ByteBuffer;
 
 import org.apache.hadoop.io.BytesWritable;
 
 public class PlsUtil {
 	public static final BytesWritable METADATA_KEY = new BytesWritable("metadata".getBytes());
 	public static final BytesWritable SOLS_KEY = new BytesWritable("sols".getBytes());
 	
 	public static BytesWritable getMapSolKey(long endTime) {
 		ByteBuffer buff = ByteBuffer.allocate(12);
 		buff.putChar('s');
 		buff.putChar('o');
		buff.putChar('l');
		buff.putChar('s');
 		buff.putLong(endTime);
 		return new BytesWritable(buff.array());
 	}
 	
 	public static long getEndTimeFromKey(BytesWritable key) {
 		ByteBuffer buff = ByteBuffer.wrap(key.getBytes(), 4, 8);
 		return buff.getLong();
 	}
 }
