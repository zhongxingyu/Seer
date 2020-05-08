 package pls;
 
 import java.io.ByteArrayInputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.Writable;
 
 import pls.vrp.LnsExtraData;
 
 public class LnsSolutionData extends SolutionData {
 	private double cost;
 	private BytesWritable solBytes;
 	private LnsExtraData extraData;
 	private PlsMetadata metadata;
 	
 	public LnsSolutionData(){
 	}
 	
 	@Override
 	public double getBestCost() {
 		return cost;
 	}
 
 	@Override
 	public void init(BytesWritable bytes) throws IOException {
 		ByteArrayInputStream bais = new ByteArrayInputStream(bytes.getBytes(), 0, bytes.getLength());
 		DataInputStream dis = new DataInputStream(bais);
 		
 		extraData = new LnsExtraData();
 		extraData.readFields(dis);
 		
 		metadata = new PlsMetadata();
 		metadata.readFields(dis);
 		
 		//read the rest as the solution
 		byte[] bytesArr = new byte[dis.available()];
 		dis.readFully(bytesArr);
 		cost = ByteBuffer.wrap(bytesArr).getDouble();
 		this.solBytes = new BytesWritable(bytesArr);
 	}
 	
 	@Override
 	public BytesWritable getBestSolutionBytes() {
 		return solBytes;
 	}
 	
 	@Override
 	public BytesWritable getEndSolutionBytes() {
 		return solBytes;
 	}
 
 	@Override
	public LnsExtraData getExtraData() {
 		return extraData;
 	}
 
 	@Override
 	public PlsMetadata getMetadata() {
 		return metadata;
 	}
 }
