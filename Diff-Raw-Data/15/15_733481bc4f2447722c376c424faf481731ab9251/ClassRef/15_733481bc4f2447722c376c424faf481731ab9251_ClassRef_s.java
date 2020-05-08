 package org.jackie.jclassfile.constantpool.impl;
 
 import org.jackie.jclassfile.constantpool.CPEntryType;
 import org.jackie.jclassfile.constantpool.Constant;
 import org.jackie.jclassfile.constantpool.ConstantPool;
 import org.jackie.jclassfile.constantpool.Task;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 
 /**
  * @author Patrik Beno
  */
 public class ClassRef extends Constant {
 
 	/*
 CONSTANT_Class_info {
     	u1 tag;
     	u2 name_index;
     }
 	 */
 
 	Utf8 value;
 
 	ClassRef(ConstantPool pool) {
 		super(pool);
 	}
 
 	ClassRef(ConstantPool pool, String value) {
 		super(pool);
		this.value = factory().getUtf8(value.replace('.', '/'));
 	}
 
 	public CPEntryType type() {
 		return CPEntryType.CLASS;
 	}
 
 	public String value() {
		return value.value().replace('/', '.');
 	}
 
 	protected Task readConstantDataOrGetResolver(DataInput in) throws IOException {
 		final int nameidx = in.readUnsignedShort();
 		return new Task() {
 			public void execute() throws IOException {
 				value = pool.getConstant(nameidx, Utf8.class);
 			}
 		};
 	}
 
 	protected void writeConstantData(DataOutput out) throws IOException {
 		value.writeReference(out);
 	}
 
 	public boolean equals(Object o) {
 		if (this == o) return true;
 		if (o == null || getClass() != o.getClass()) return false;
 
 		ClassRef classRef = (ClassRef) o;
 
 		if (value != null ? !value.equals(classRef.value) : classRef.value != null) {
 			return false;
 		}
 
 		return true;
 	}
 
 	public int hashCode() {
 		return (value != null ? value.hashCode() : 0);
 	}
 
 	protected String valueToString() {
 		return String.format("value={%s}", value);
 	}
 }
