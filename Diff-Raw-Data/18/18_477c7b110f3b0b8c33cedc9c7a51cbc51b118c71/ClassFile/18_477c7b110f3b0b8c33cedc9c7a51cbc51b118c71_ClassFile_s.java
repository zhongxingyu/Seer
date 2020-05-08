 package org.jackie.jclassfile.model;
 
 import org.jackie.jclassfile.attribute.AttributeHelper;
 import org.jackie.jclassfile.attribute.AttributeProviderRegistry;
 import org.jackie.jclassfile.attribute.AttributeSupport;
 import org.jackie.jclassfile.constantpool.ConstantPool;
 import org.jackie.jclassfile.constantpool.impl.ClassRef;
 import org.jackie.jclassfile.flags.Flags;
 import static org.jackie.jclassfile.util.Helper.writeConstantReference;
 import org.jackie.utils.Assert;
 import static org.jackie.utils.CollectionsHelper.*;
 import org.jackie.utils.Log;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataInput;
 import java.io.DataOutput;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author Patrik Beno
  */
 public class ClassFile extends Base implements ClassFileProvider, AttributeSupport {
 
 	static {
 		AttributeProviderRegistry.instance();
 	}
 
 	/*
 ClassFile {
     	u4 magic;
     	u2 minor_version;
     	u2 major_version;
     	u2 constant_pool_count;
     	cp_info constant_pool[constant_pool_count-1];
     	u2 access_flags;
     	u2 this_class;
     	u2 super_class;
     	u2 interfaces_count;
     	u2 interfaces[interfaces_count];
     	u2 fields_count;
     	field_info fields[fields_count];
     	u2 methods_count;
     	method_info methods[methods_count];
     	u2 attributes_count;
     	attribute_info attributes[attributes_count];
     }
     */
 
    int magic = 0xCAFEBABE;
 	int minor;
 	int major = 50;
 
 	ConstantPool pool;
 
 	Flags flags;
 	ClassRef classname;
 	ClassRef superclass;
 	List<ClassRef> interfaces;
 
 	List<FieldInfo> fields;
 	List<MethodInfo> methods;
 	List<AttributeInfo> attributes;
 
 	{
 		pool = new ConstantPool(this);
 		flags = new Flags();
 	}
 
 	public ClassFile classname(String clsname) {
 		classname = pool().factory().getClassRef(clsname);
 		return this;
 	}
 
 	public String classname() {
 		return classname.value();
 	}
 
 	public String superclass() {
 		return superclass != null ? superclass.value() : null;
 	}
 
 	public ClassFile superclass(String clsname) {
 		superclass = pool().factory().getClassRef(clsname);
 		return this;
 	}
 
 	public Flags flags() {
 		return flags;
 	}
 
 	public List<ClassRef> interfaces() {
 		return interfaces;
 	}
 
 	public void addInterface(String className) {
 		if (interfaces == null) {
 			interfaces = new ArrayList<ClassRef>();
 		}
 		interfaces.add(pool().factory().getClassRef(className));
 	}
 
 	public List<FieldInfo> fields() {
 		return fields;
 	}
 
 	public void addField(FieldInfo field) {
 		if (fields == null) {
 			fields = new ArrayList<FieldInfo>();
 		}
 		fields.add(field);
 	}
 
 	public List<MethodInfo> methods() {
 		return methods != null ? methods : Collections.<MethodInfo>emptyList();
 	}
 
 	public void addMethod(MethodInfo method) {
 		if (methods == null) {
 			methods = new ArrayList<MethodInfo>();
 		}
 		methods.add(method);
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
 
 	///
 
 	public ClassFile classFile() {
 		return this;
 	}
 
 	public ConstantPool pool() {
 		return pool;
 	}
 
 	public void load(final DataInput in) throws IOException {
 		Log.enter();
 
 		magic = in.readInt();
 		minor = in.readUnsignedShort();
 		major = in.readUnsignedShort();
 
		pool = new ConstantPool(this, in);
 
 		flags = new Flags(in);
 		classname = pool.getConstant(in.readUnsignedShort(), ClassRef.class);
 		Log.debug("Loading class %s", classname.value());
 
 		superclass = pool.getConstant(in.readUnsignedShort(), ClassRef.class);
 
 		// interfaces
 		{
 			int count = in.readUnsignedShort();
 			interfaces = new ArrayList<ClassRef>(count);
 			while (count-- > 0) {
 				ClassRef iface = pool.getConstant(in.readUnsignedShort(), ClassRef.class);
 				interfaces.add(iface);
 			}
 		}
 
 		// fields
 		{
 			int count = in.readUnsignedShort();
 			fields = new ArrayList<FieldInfo>(count);
 			while (count-- > 0) {
 				FieldInfo f = new FieldInfo(this);
 				f.load(in);
 				fields.add(f);
 			}
 		}
 
 		// methods
 		{
 			int count = in.readUnsignedShort();
 			methods = new ArrayList<MethodInfo>(count);
 			while (count-- > 0) {
 				MethodInfo m = new MethodInfo(this);
 				m.load(in);
 				methods.add(m);
 			}
 		}
 
 		attributes = AttributeHelper.loadAttributes(this, in);
 
 		Log.leave();
 	}
 
 	public void save(DataOutput out) throws IOException {
 		Log.enter();
 
 		out.writeInt(magic);
 		out.writeShort(minor);
 		out.writeShort(major);
 
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		DataOutputStream tmpout = new DataOutputStream(baos);
 
 		flags.save(tmpout);
 		writeConstantReference(classname, tmpout);
 		writeConstantReference(superclass, tmpout);
 
 		// interfaces
 		tmpout.writeShort(sizeof(interfaces));
 		for (ClassRef i : iterable(interfaces)) {
 			i.writeReference(tmpout);
 		}
 
 		save(tmpout, fields);
 		save(tmpout, methods);
 		save(tmpout, attributes);
 
 		pool.save(out);
 
 		tmpout.close();
 		out.write(baos.toByteArray());
 
 		Log.leave();
 	}
 
 	private void save(DataOutput out, List<? extends Base> items) throws IOException {
 		out.writeShort(sizeof(items));
 		for (Base item : iterable(items)) {
 			item.save(out);
 		}
 	}
 
 	public byte[] toByteArray() {
 		try {
 			ByteArrayOutputStream baos = new ByteArrayOutputStream(1024*4);
 			DataOutput out = new DataOutputStream(baos);
 			save(out);
 			return baos.toByteArray();
 		} catch (IOException e) {
 			throw Assert.unexpected(e);
 		}
 	}
 }
