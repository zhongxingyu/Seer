 package org.jackie.java5.enumtype.impl;
 
 import org.jackie.compiler.extension.ExtensionProvider;
 import org.jackie.compiler.event.ExtensionEvents;
 import org.jackie.java5.enumtype.EnumType;
 import org.jackie.jvm.JClass;
 import org.jackie.jvm.extension.Extension;
 import org.jackie.jclassfile.flags.Flags;
 import org.jackie.jclassfile.flags.Access;
 import org.jackie.jclassfile.model.ClassFile;
import org.jackie.jclassfile.model.FieldInfo;
 import org.jackie.event.Events;
 
 /**
  * @author Patrik Beno
  */
 public class EnumTypeProvider implements ExtensionProvider<JClass> {
 
 	ExtensionEvents listener = new ExtensionEvents() {
 		public void resolveClassFlags(Flags flags, JClass jclass) {
 			if (flags.isSet(Access.ENUM)) {
 				jclass.extensions().edit().add(new EnumTypeImpl(jclass));
 			}
 		}
 		@Override
 		public void onCompile(JClass jclass, ClassFile classfile) {
 			if (!jclass.extensions().supports(EnumType.class)) { return; }
 			classfile.flags().set(Access.ENUM);
			for (FieldInfo f : classfile.fields()) {
				boolean isSameTypeAsClass = f.typeDescriptor().getTypeName().equals(classfile.classname());
				boolean isFlagsMatch = f.flags().equals(Flags.create(Access.STATIC, Access.FINAL, Access.PUBLIC));
				if (isSameTypeAsClass && isFlagsMatch) {
					f.flags().set(Access.ENUM);
				}
			}
 		}
 
 	};
 
 	public Class<? extends Extension> getType() {
 		return EnumType.class;
 	}
 
 	public Extension getExtension(JClass jclass) {
 		return null; // should already be applied in ExtensionEvents.resolveClassFlags()
 	}
 
 	public void init() {
 		Events.registerEventListener(ExtensionEvents.class, listener);
 	}
 
 	public void done() {
 		Events.unregisterEventListener(listener);
 	}
 }
