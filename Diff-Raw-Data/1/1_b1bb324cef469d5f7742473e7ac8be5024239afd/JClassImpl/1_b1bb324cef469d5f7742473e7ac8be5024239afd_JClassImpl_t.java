 package org.jackie.compiler_impl.jmodelimpl;
 
 import org.jackie.compiler.typeregistry.TypeRegistry;
 import org.jackie.compiler_impl.bytecode.ByteCodeBuilder;
 import org.jackie.compiler_impl.jmodelimpl.attribute.AttributesImpl;
 import org.jackie.compiler_impl.jmodelimpl.structure.JFieldImpl;
 import org.jackie.compiler_impl.jmodelimpl.structure.JMethodImpl;
 import org.jackie.compiler_impl.typeregistry.JClassLoader;
 import static org.jackie.compiler_impl.util.Helper.assertEditable;
 import org.jackie.jclassfile.model.ClassFile;
 import org.jackie.jvm.JClass;
 import org.jackie.jvm.JPackage;
 import org.jackie.jvm.attribute.Attributes;
 import org.jackie.jvm.attribute.JAttribute;
 import org.jackie.jvm.extension.Extensions;
 import org.jackie.jvm.props.AccessMode;
 import org.jackie.jvm.props.Flag;
 import org.jackie.jvm.props.Flags;
 import org.jackie.jvm.spi.AbstractJNode;
 import org.jackie.jvm.structure.JField;
 import org.jackie.jvm.structure.JMethod;
 import org.jackie.utils.Assert;
 import static org.jackie.utils.Assert.typecast;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import static java.util.Collections.emptyList;
 import java.util.List;
 
 /**
  * @author Patrik Beno
  */
 public class JClassImpl extends AbstractJNode implements JClass {
 
 	// infrastructure stuff
 
 	protected TypeRegistry typeRegistry;
 	public LoadLevel loadLevel;
 
 	// core model
 
 	protected String name;
 	protected JPackage jpackage;
 	protected JClass superclass;
 	protected List<JClass> interfaces;
 
 	protected Attributes attributes;
 
 	protected List<JField> fields;
 	protected List<JMethod> methods;
 
 	protected AccessMode access;
 	protected FlagsImpl flags;
 
 	protected Extensions extensions;
 
 
 	{
 		loadLevel = LoadLevel.NONE;
 		access = AccessMode.PACKAGE;
 	}
 
 
 	public JClassImpl(String name, JPackage jpackage, TypeRegistry typeRegistry) {
 		super(jpackage);
 		this.name = name;
 		this.jpackage = jpackage;
 		this.typeRegistry = typeRegistry;
 	}
 
 	public TypeRegistry getTypeRegistry() {
 		return typeRegistry;
 	}
 
 	protected void checkLoaded(LoadLevel min) {
 		if (this.loadLevel.atLeast(min)) {
 			return;
 		}
 
 		typecast(typeRegistry, JClassLoader.class).load(this, min);
 	}
 
 	/// JClass ///
 
 	public String getName() {
 		return name;
 	}
 
 	public String getFQName() {
 		if (jpackage != null) {
 			return jpackage.getFQName() + "." + name;
 		} else {
 			return name;
 		}
 	}
 
 	public JPackage getJPackage() {
 		return jpackage;
 	}
 
 	public JClass getSuperClass() {
 		checkLoaded(LoadLevel.CLASS);
 		return superclass;
 	}
 
 	public List<JClass> getInterfaces() {
 		checkLoaded(LoadLevel.CLASS);
 		return interfaces != null ? Collections.unmodifiableList(interfaces) : Collections.<JClass>emptyList();
 	}
 
 	public Flags flags() {
 		checkLoaded(LoadLevel.CLASS);
 		if (flags == null) {
 			flags = new FlagsImpl();
 		}
 		return flags;
 	}
 
 	public Attributes attributes() {
 		checkLoaded(LoadLevel.ATTRIBUTES);
 		if (attributes == null) {
 			attributes = new AttributesImpl(this);
 		}
 		return attributes;
 	}
 
 	public List<JField> getFields() {
 		checkLoaded(LoadLevel.API);
 		if (fields == null) { 
 			return emptyList();
 		}
 		return Collections.unmodifiableList(fields);
 	}
 
 	public List<? extends JMethod> getMethods() {
 		checkLoaded(LoadLevel.API);
 		if (methods == null) {
 			return emptyList();
 		}
 		return Collections.unmodifiableList(methods);
 	}
 
 	public boolean isAssignableFrom(JClass jclass) {
 		throw Assert.notYetImplemented(); // todo implement this 
 	}
 
 	public boolean isAssignableFrom(Class cls) {
 		throw Assert.notYetImplemented(); // todo implement this
 	}
 
 	public boolean isInstance(JClass jclass) {
 		throw Assert.notYetImplemented(); // todo implement this
 	}
 
 	public boolean isInstance(Class cls) {
 		return getFQName().equals(cls.getName()); // fixme isInstance(): partial naive implementation
 	}
 
 	/// Accessible ///
 
 	public AccessMode getAccessMode() {
 		checkLoaded(LoadLevel.CLASS);
 		return access;
 	}
 
 
 	/// Extensions ///
 
 	public Extensions extensions() {
		checkLoaded(LoadLevel.CLASS);
 		if (extensions == null) {
 			extensions = new ExtensionsImpl(this); 
 		}
 		return extensions;
 	}
 	
 
 	/// Editable ///
 
 	public boolean isEditable() {
 		return typeRegistry.isEditable();
 	}
 
 	public Editor edit() {
 		assertEditable(this);
 		checkLoaded(LoadLevel.ATTRIBUTES);
 		return new Editor() {
 			final JClassImpl cthis = JClassImpl.this;
 			public Editor setName(String name) {
 				JClassImpl.this.name = name;
 				return this;
 			}
 
 			public Editor setPackage(JPackage jpackage) {
 				JClassImpl.this.jpackage = jpackage;
 				return this;
 			}
 
 			public Editor setSuperClass(JClass jclass) {
 				JClassImpl.this.superclass = jclass;
 				return this;
 			}
 
 			public Editor setInterfaces(JClass... ifaces) {
 				for (JClass iface : ifaces) {
 					addInterface(iface);
 				}
 				return this;
 			}
 
 			public Editor addInterface(JClass iface) {
 				if (interfaces == null) {
 					interfaces = new ArrayList<JClass>();
 				}
 				interfaces.add(iface);
 				return this;
 			}
 
 			public Editor setAccessMode(AccessMode accessMode) {
 				JClassImpl.this.access = accessMode;
 				return this;
 			}
 
 			public Editor setFlags(Flag ... flags) {
 				if (cthis.flags == null) {
 					cthis.flags = new FlagsImpl();
 				}
 				cthis.flags.reset().setAll(flags);
 				return this;
 			}
 
 			public Editor addField(JField jfield) {
 				if (fields == null) {
 					fields = new ArrayList<JField>();
 				}
 				fields.add(jfield);
 				return this;
 			}
 
 			public Editor addMethod(JMethod jmethod) {
 				if (methods == null) {
 					methods = new ArrayList<JMethod>();
 				}
 				methods.add(jmethod);
 				return this;
 			}
 
 			public JClass editable() {
 				return JClassImpl.this;
 			}
 		};
 	}
 
 	public String toString() {
 		return getFQName();
 	}
 
 	/// binary/bytecode stuff ///
 
 	public void compile(final ClassFile classfile) {
 		ByteCodeBuilder.execute(new ByteCodeBuilder() {
 			protected void run() {
 				classfile.classname(toBinaryClassName(JClassImpl.this));
 				if (getSuperClass() != null) {
 					classfile.superclass(toBinaryClassName(getSuperClass()));
 				}
 				for (JClass iface : getInterfaces()) {
 					classfile.addInterface(toBinaryClassName(iface));
 				}
 
 				for (JField f : getFields()) {
 					((JFieldImpl)f).compile(classfile);
 				}
 				for (JMethod m : getMethods()) {
 					((JMethodImpl)m).compile(classfile);
 				}
 				for (JAttribute a : attributes().getAttributes()) {
 //					((JAttributeImpl)a).compile(classfile); // todo implement attribute compilation
 				}
 			}
 		});
 	}
 
 }
