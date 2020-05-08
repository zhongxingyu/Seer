 package org.nts.pigutils.proto;
 
 import java.io.DataInput;
 import java.io.DataInputStream;
 import java.io.DataOutput;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.pig.backend.executionengine.ExecException;
 import org.apache.pig.data.Tuple;
 import org.apache.pig.data.TupleFactory;
 
 import com.google.protobuf.Descriptors.Descriptor;
 import com.google.protobuf.Descriptors.FieldDescriptor;
 import com.google.protobuf.Message;
 
 /**
  * This class wraps a protocol buffer message and attempts to delay parsing
  * until individual fields are requested.
  */
 public class ProtobufTuple implements Tuple {
 
 	private static final long serialVersionUID = 8468589454361280269L;
 	private Tuple realTuple;
 
 	private static final ProtobufToPig protoToPig = new ProtobufToPig();
 
 	Message msg;
 	int[] requiredColumns;
 
 	public ProtobufTuple() {
 
 		realTuple = TupleFactory.getInstance().newTuple();
 
 	}
 
 	public ProtobufTuple(Message msg, int[] requiredColumns) throws IOException {
 		this.msg = msg;
 		this.requiredColumns = requiredColumns;
 
 		Descriptor descriptor = msg.getDescriptorForType();
 		List<FieldDescriptor> fieldDescriptors = descriptor.getFields();
 
 		// the tuple length depends if we have a required columns list (i.e.
 		// column pruning is used)
 		// or none.
 
 		if (requiredColumns == null || requiredColumns.length <= 0) {
 			int len = fieldDescriptors.size();
 			realTuple = TupleFactory.getInstance().newTuple(len);
 
 			for (int i = 0; i < len; i++) {
 				copyMessageValueToTuple(i, i, fieldDescriptors, msg, realTuple);
 			}
 
 		} else {
 			int len = requiredColumns.length;
 			realTuple = TupleFactory.getInstance().newTuple(len);
 
 			for (int i = 0; i < len; i++) {
 				copyMessageValueToTuple(requiredColumns[i], i,
 						fieldDescriptors, msg, realTuple);
 			}
 
 		}
 
 	}
 
 	private static final void copyMessageValueToTuple(int index,
 			int tupleIndex, List<FieldDescriptor> fieldDescriptors,
 			Message msg, Tuple tuple) throws IOException {
 		// get message
 		FieldDescriptor fieldDescriptor = fieldDescriptors.get(index);
 
 		Object fieldValue = msg.getField(fieldDescriptor);
 		if (fieldDescriptor.getType() == FieldDescriptor.Type.MESSAGE) {
			Object innertuple = protoToPig.messageToTuple(fieldDescriptor, fieldValue);
			System.out.println("Messagetuple: " + innertuple);
 			tuple.set(tupleIndex,
					innertuple);
 		} else {
 			tuple.set(tupleIndex,
 					protoToPig.singleFieldToTuple(fieldDescriptor, fieldValue));
 		}
 	}
 
 	public ProtobufTuple(Message msg) throws IOException {
 		this(msg, null);
 	}
 
 	public void append(Object obj) {
 		realTuple.append(obj);
 	}
 
 	public List<Object> getAll() {
 		return realTuple.getAll();
 	}
 
 	public long getMemorySize() {
 		// The protobuf estimate is obviously inaccurate.
 		return msg.getSerializedSize() + realTuple.getMemorySize();
 	}
 
 	public byte getType(int idx) throws ExecException {
 
 		return realTuple.getType(idx);
 	}
 
 	public boolean isNull() {
 		return realTuple.isNull();
 	}
 
 	public boolean isNull(int idx) throws ExecException {
 		return realTuple.isNull(idx);
 	}
 
 	public void reference(Tuple arg) {
 		realTuple.reference(arg);
 		// Ignore the Message from now on.
 	}
 
 	public void set(int idx, Object val) throws ExecException {
 		realTuple.set(idx, val);
 	}
 
 	public void setNull(boolean isNull) {
 		realTuple.setNull(isNull);
 	}
 
 	public int size() {
 		return realTuple.size();
 	}
 
 	public String toDelimitedString(String delim) throws ExecException {
 		return realTuple.toDelimitedString(delim);
 	}
 
 	public void readFields(DataInput inp) throws IOException {
 		Message.Builder builder = msg.newBuilderForType();
 		try {
 			builder.mergeDelimitedFrom((DataInputStream) inp);
 		} catch (ClassCastException e) {
 			throw new IOException(
 					"Provided DataInput not instance of DataInputStream.", e);
 		}
 		Message msg = builder.build();
 		realTuple.reference(new ProtobufTuple(msg, requiredColumns));
 	}
 
 	public void write(DataOutput out) throws IOException {
 		if (msg == null)
 			return;
 		realTuple.write(out);
 	}
 
 	public int compareTo(Object obj) {
 		return realTuple.compareTo(obj);
 	}
 
 	public Object get(int index) throws ExecException {
 		return realTuple.get(index);
 	}
 }
