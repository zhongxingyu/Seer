 package org.jackie.jclassfile.attribute.anno;
 
 import org.jackie.jclassfile.constantpool.ConstantPool;
 import org.jackie.jclassfile.constantpool.impl.Utf8;
 import org.jackie.jclassfile.util.TypeDescriptor;
 import org.jackie.jclassfile.util.Helper;
 import org.jackie.jclassfile.code.ConstantPoolSupport;
 import static org.jackie.utils.CollectionsHelper.sizeof;
 import org.jackie.utils.XDataInput;
 import org.jackie.utils.XDataOutput;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Patrik Beno
 */
 public class Annotation implements ConstantPoolSupport {
 
 	/*
 annotation {
 	u2 type_index;
 	u2 num_element_value_pairs;
 	{
 		u2 element_name_index;
 		element_value value;
 	} element_value_pairs[num_element_value_pairs];
 }
 	 */
 
 	Object owner;
 
 	Utf8 type;
 	List<ElementValue> elements;
 
 	public Annotation(Object owner) {
 		this.owner = owner;
 	}
 
 	public TypeDescriptor type() {
 		return new TypeDescriptor(type.value());
 	}
 
 	public List<ElementValue> elements() {
 		return elements;
 	}
 
 	void load(XDataInput in, ConstantPool pool) {
 		type = pool.getConstant(in.readUnsignedShort(), Utf8.class);
 		int count = in.readUnsignedShort();
 		elements = new ArrayList<ElementValue>(count);
 		while (count-- > 0) {
 			Utf8 ename = pool.getConstant(in.readUnsignedShort(), Utf8.class);
 			Tag tag = Tag.forId((char) in.readByte());
 
 			ElementValue evalue = ElementValue.forTag(tag);
 			evalue.init(this,  ename, tag);
 			evalue.load(in, pool);
 
 			elements.add(evalue);
 		}
 	}
 
 	void save(XDataOutput out) {
 		Helper.writeConstantReference(type, out);
 		out.writeShort(elements.size());
 		for (ElementValue e : elements) {
			e.name.writeReference(out);
 			e.save(out);
 		}
 	}
 
 	public void registerConstants(ConstantPool pool) {
 		type = pool.register(type);
 		for (ElementValue e : elements) {
 			e.registerConstants(pool);
 		}
 	}
 
 	public String toString() {
 		return String.format("%s(%s elements)", type.value(), sizeof(elements));
 	}
 }
