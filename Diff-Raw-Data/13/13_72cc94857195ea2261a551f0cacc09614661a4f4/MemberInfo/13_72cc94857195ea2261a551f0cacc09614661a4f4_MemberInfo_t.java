 package org.jackie.jclassfile.model;
 
 import org.jackie.jclassfile.attribute.AttributeHelper;
 import org.jackie.jclassfile.attribute.AttributeSupport;
 import org.jackie.jclassfile.constantpool.ConstantPool;
 import org.jackie.jclassfile.constantpool.impl.Utf8;
 import org.jackie.jclassfile.flags.Flags;
import static org.jackie.utils.CollectionsHelper.sizeof;import static org.jackie.utils.CollectionsHelper.iterable;
 
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author Patrik Beno
  */
 public abstract class MemberInfo extends Base implements ClassFileProvider, AttributeSupport {
    /*
 field|member info {
     	u2 access_flags;
     	u2 name_index;
     	u2 descriptor_index;
     	u2 attributes_count;
     	attribute_info attributes[attributes_count];
     }
     */
 
 	ClassFile classfile;
 
 	Flags flags;
 	Utf8 name;
 	Utf8 descriptor;
 
 	List<AttributeInfo> attributes;
 
 	protected MemberInfo(ClassFile classfile) {
 		this.classfile = classfile;
 	}
 
 	public Flags flags() {
		if (flags == null) {
			flags = new Flags();
		}
 		return flags;
 	}
 
 	public String name() {
 		return name.value();
 	}
 
 	public void name(String name) {
 		this.name = classFile().pool().factory().getUtf8(name);
 	}
 
 	public String descriptor() {
 		return descriptor.value();
 	}
 
 	public void descriptor(String descriptor) {
 		this.descriptor = classFile().pool().factory().getUtf8(descriptor);
 	}
 
 	public List<AttributeInfo> attributes() {
 		return attributes != null ? attributes : Collections.<AttributeInfo>emptyList();
 	}
 
 	public void addAttribute(AttributeInfo attribute) {
 		if (attributes == null) {
 			attributes = new ArrayList<AttributeInfo>();
 		}
 		attributes.add(attribute);
 	}
 
 	public ClassFile classFile() {
 		return classfile;
 	}
 
 	public void load(DataInput in) throws IOException {
 		ConstantPool pool = classfile.pool();
 
 		flags = new Flags(in);
 		name = pool.getConstant(in.readUnsignedShort(), Utf8.class);
 		descriptor = classfile.pool().getConstant(in.readUnsignedShort(), Utf8.class);
 		attributes = AttributeHelper.loadAttributes(this, in);
 	}
 
 	public void save(DataOutput out) throws IOException {
		flags().save(out);
 		name.writeReference(out);
 		descriptor.writeReference(out);
 
		out.writeShort(sizeof(attributes));
		for (AttributeInfo a : iterable(attributes)) {
 			a.save(out);
 		}
 	}
 
 	public String toString() {
 		return String.format("%s: %s", getClass().getSimpleName(), name);
 	}
 }
