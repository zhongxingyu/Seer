 package com.tehasdf.mapreduce.data;
 
 import org.apache.hadoop.io.BytesWritable;
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.WritableComparable;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 
 public class WritableColumn implements WritableComparable<WritableColumn> {
   public enum State {
     NORMAL, DELETED, EXPIRING
   }
 
   public State state;
   public BytesWritable name;
   public BytesWritable data;
   public LongWritable timestamp;
   public LongWritable ttl; // seconds
   public LongWritable expiration; // ms
 
   public WritableColumn() {
 
   }
 
   public WritableColumn(State state, BytesWritable name, BytesWritable data, LongWritable timestamp) {
     this.state = state;
     this.name = name;
     this.data = data;
     this.timestamp = timestamp;
   }
 
   public WritableColumn(State state, BytesWritable name, BytesWritable data, LongWritable ttl, LongWritable expiration, LongWritable timestamp) {
     this.state = state;
     this.name = name;
     this.data = data;
     this.ttl = ttl;
     this.expiration = expiration;
     this.timestamp = timestamp;
   }
 
   public void readFields(DataInput in) throws IOException {
     int stateVal = in.readInt();
     state = State.values()[stateVal];
 
     BytesWritable n = new BytesWritable();
     n.readFields(in);
     name = n;
 
     if (state == State.NORMAL || state == State.EXPIRING) {
       BytesWritable d = new BytesWritable();
       d.readFields(in);
       data = d;
     } else if (state == State.DELETED) {
     } else {
 
     }
 
     LongWritable ts = new LongWritable();
     ts.readFields(in);
     timestamp = ts;
 
     if (state == State.EXPIRING) {
       LongWritable t = new LongWritable();
       t.readFields(in);
       ttl = t;
 
       LongWritable exp = new LongWritable();
       exp.readFields(in);
       expiration = exp;
     }
   }
 
   public void write(DataOutput out) throws IOException {
     out.writeInt(state.ordinal());
     name.write(out);
    if (state == State.NORMAL || state == State.EXPIRING) {
       data.write(out);
     } else if (state == State.DELETED) {
     } else {
     }
     timestamp.write(out);
 
     if (state == State.EXPIRING) {
       ttl.write(out);
       expiration.write(out);
     }
   }
 
   public int compareTo(WritableColumn other) {
     return timestamp.compareTo(other.timestamp);
   }
 
   public String toString() {
     if (state == State.NORMAL) {
       return "Column("+name+","+data+","+timestamp+")";
     } else if (state == State.DELETED) {
       return "Deleted("+name+")";
     } else {
       return "Unknown";
     }
   }
 }
