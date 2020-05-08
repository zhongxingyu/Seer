 package org.jackie.jclassfile.attribute.anno;
 
 import org.jackie.jclassfile.constantpool.Constant;
 import org.jackie.jclassfile.constantpool.ConstantPool;
 import static org.jackie.jclassfile.constantpool.ConstantPool.constantPool;
 import org.jackie.jclassfile.constantpool.impl.DoubleRef;
 import org.jackie.jclassfile.constantpool.impl.FloatRef;
 import org.jackie.jclassfile.constantpool.impl.IntegerRef;
 import org.jackie.jclassfile.constantpool.impl.LongRef;
 import org.jackie.jclassfile.constantpool.impl.Utf8;
 import org.jackie.jclassfile.constantpool.impl.ValueProvider;
 import org.jackie.jclassfile.util.TypeDescriptor;
 import org.jackie.utils.Assert;
 import static org.jackie.utils.Assert.typecast;
 
 import java.io.DataInput;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Patrik Beno
 */
 public abstract class ElementValue {
 
 	/*
 element_value {
 	u1 tag;
 	union {
 		u2   const_value_index;
 		{
 			u2   type_name_index;
 			u2   const_name_index;
 		} enum_const_value;
 		u2   class_info_index;
 		annotation annotation_value;
 		{
 			u2    num_values;
 			element_value values[num_values];
 		} array_value;
 	} value;
 }
 	 */
 
 	static ElementValue forTag(Tag tag) {
 		switch (tag) {
 			case BYTE:
 			case CHAR:
 			case DOUBLE:
 			case FLOAT:
 			case INT:
 			case LONG:
 			case SHORT:
 			case BOOLEAN:
 			case STRING:
 				return new ConstElementValue();
 			case ENUM:
 				return new EnumElementValue();
 			case CLASS:
 				return new ClassElementValue();
 			case ANNOTATION:
 				return new AnnoElementValue();
 			case ARRAY:
 				return new ArrayElementValue();
 			default:
 				throw Assert.invariantFailed(tag);
 		}
 	}
 
 	Object owner;
 	Utf8 name;
 	Tag tag;
 
 	void init(Object owner, Utf8 name, Tag tag) {
 		this.owner = owner;
 		this.name = name;
 		this.tag = tag;
 	}
 
 	public Object owner() {
 		return owner;
 	}
 
 	public String name() {
 		return name.value();
 	}
 
 	public Tag tag() {
 		return tag;
 	}
 
 	abstract void load(DataInput in) throws IOException;
 
 	public String toString() {
 		return String.format("%s=%s", name != null ? name.value() : null, valueToString());
 	}
 
 	protected String valueToString() {
 		return null;
 	}
 
 	static public class ConstElementValue extends ElementValue {
 		Constant value;
 
 		void load(DataInput in) throws IOException {
 			switch (tag) {
 				case BYTE:
 				case CHAR:
 				case SHORT:
 				case BOOLEAN:
 				case INT:
 					value(in, IntegerRef.class);
 					break;
 				case DOUBLE:
 					value(in, DoubleRef.class);
 					break;
 				case FLOAT:
 					value(in, FloatRef.class);
 					break;
 				case LONG:
 					value(in, LongRef.class);
 					break;
 				case STRING:
 					value(in, Utf8.class);
 					break;
 				default:
 					throw Assert.invariantFailed(tag);
 			}
 
 		}
 
 		public Object value() {
			Object extracted = ((ValueProvider) value).value();
			Object converted;
			switch (tag) {
				case BYTE:
					converted = ((Number) extracted).byteValue();
					break;
				case CHAR:
					converted = (char) ((Number) extracted).intValue();
					break;
				case SHORT:
					converted = ((Number) extracted).shortValue();
					break;
				case BOOLEAN:
					assert extracted instanceof Number;
					converted = ((Number) extracted).intValue() == 1 ? Boolean.TRUE : Boolean.FALSE;
					break;
				default:
					converted = extracted;
			}
			return converted;
 		}
 
 		protected String valueToString() {
 			return value.toString();
 		}
 
 		void value(DataInput in, Class<? extends Constant> type) throws IOException {
 			value = constantPool().getConstant(in.readUnsignedShort(), type);
 		}
 	}
 
 	static public class ClassElementValue extends ElementValue {
 
 		Utf8 classinfo;
 
 		public TypeDescriptor type() {
 			return new TypeDescriptor(classinfo.value());
 		}
 
 		void load(DataInput in) throws IOException {
 			classinfo = constantPool().getConstant(in.readUnsignedShort(), Utf8.class);
 		}
 
 		protected String valueToString() {
 			return classinfo.value();
 		}
 	}
 
 	static public class EnumElementValue extends ElementValue {
 		Utf8 type;
 		Utf8 value;
 
 		public TypeDescriptor type() {
 			return new TypeDescriptor(type.value());
 		}
 
 		public String value() {
 			return value.value();
 		}
 
 		void load(DataInput in) throws IOException {
 			ConstantPool pool = constantPool();
 			type = pool.getConstant(in.readUnsignedShort(), Utf8.class);
 			value = pool.getConstant(in.readUnsignedShort(), Utf8.class);
 		}
 
 		protected String valueToString() {
 			return String.format("%s(%s)", value.value(), type.value());
 		}
 	}
 
 	static public class AnnoElementValue extends ElementValue {
 		Annotation annotation;
 
 		public Annotation annotation() {
 			return annotation;
 		}
 
 		void load(DataInput in) throws IOException {
 			annotation = new Annotation(owner);
 			annotation.load(in);
 		}
 
 		protected String valueToString() {
 			return annotation.toString();
 		}
 	}
 
 	static public class ArrayElementValue extends ElementValue {
 		List<ElementValue> values;
 
 		public List<ElementValue> values() {
 			return values;
 		}
 
 		void load(DataInput in) throws IOException {
 			int count = in.readUnsignedShort();
 			values = new ArrayList<ElementValue>(count);
 			while (count-- > 0) {
 				Tag tag = Tag.forId((char) in.readUnsignedByte());
 
 				ElementValue evalue = ElementValue.forTag(tag);
 				evalue.init(this, null, tag);
 				evalue.load(in);
 
 				values.add(evalue);
 			}
 		}
 
 		protected String valueToString() {
 			return String.format("[%s elements]", values.size());
 		}
 	}
 
 
 }
