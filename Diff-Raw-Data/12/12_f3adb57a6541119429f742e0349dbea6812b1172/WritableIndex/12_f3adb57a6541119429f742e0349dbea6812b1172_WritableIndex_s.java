 package edu.columbia.mipl.mapreduce;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 
 import org.apache.hadoop.io.Writable;
 import org.apache.hadoop.io.WritableComparable;
 
 import edu.columbia.mipl.datastr.*;
 
 public class WritableIndex implements Writable, WritableComparable {
 	int row;
 	int col;
 
 	public WritableIndex() {
 		
 	}
 	
 	public WritableIndex(long row, long col) {
 		this((int) row, (int) col);
 	}
 
 	public WritableIndex(int row, int col) {
 		this.row = row;
 		this.col = col;
 	}
 
 	public void readFields(DataInput in) throws IOException {
 		row = in.readInt();
 		col = in.readInt();
 	}
 
 	public void write(DataOutput out) throws IOException {
 		out.writeInt(row);
 		out.writeInt(col);
 	}
 
 	public int getRow() {
 		return row;
 	}
 
 	public int getCol() {
 		return col;
 	}
 
 	@Override
 	public int compareTo(Object arg0) {
 		WritableIndex ind = (WritableIndex) arg0;
 		
 		if (ind.row != row) return ind.row - row;
 		return ind.col - col;
 	}
 	
	public String toString()
	{
 		StringBuffer sb = new StringBuffer();
 		
 //		sb.append(row + " " + col);
 		return sb.toString();
 	}
 }
