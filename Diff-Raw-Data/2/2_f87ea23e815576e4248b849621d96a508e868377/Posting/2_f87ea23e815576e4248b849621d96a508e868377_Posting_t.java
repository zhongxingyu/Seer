 package group1.inverted;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 
 import org.apache.hadoop.io.Writable;
 
 public class Posting implements Writable, Comparable<Posting>{
 	
 	String docid;
 	int count;
 	
 	public Posting()
 	{
 	}
 	
 	public int compareTo(Posting rhs) {
		return new Integer(count).compareTo(rhs.count);
 	}
 
 	public void readFields(DataInput in) throws IOException {
 		count = in.readInt();
 		this.docid = in.readUTF();
 	}
 
 	public void write(DataOutput out) throws IOException {
 		out.writeInt(count);
 		out.writeUTF(docid);
 		
 	}
 	
 	@Override
 	public String toString()
 	{
 		return "" + count + ":" + docid;
 	}
 }
