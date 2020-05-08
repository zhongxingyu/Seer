 package uk.ac.cam.db538.dexter.hierarchy.builder;
 
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
 import java.util.Set;
 
import org.jf.dexlib.Util.AccessFlags;

 import lombok.val;
 
 import uk.ac.cam.db538.dexter.dex.DexUtils;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
 
 @SuppressWarnings("rawtypes")
 public class VmClassScanner implements IClassScanner {
 
 	private final DexTypeCache typeCache;
 	private final Class classDef;
 	
 	public VmClassScanner(Class classDef, DexTypeCache typeCache) {
 		this.typeCache = typeCache;
 		this.classDef = classDef;
 	}
 
 	@Override
 	public String getClassDescriptor() {
 		return DexClassType.jvm2dalvik(classDef.getName());
 	}
 
 	@Override
 	public String getSuperclassDescriptor() {
 		val scls = classDef.getSuperclass();
 		if (scls != null)
 			return DexClassType.jvm2dalvik(scls.getName());
 		else
 			return null;
 	}
 
 	@Override
 	public boolean isInterface() {
 		return classDef.isInterface();
 	}
 
 	@Override
 	public int getAccessFlags() {
 		val flags = convertModifier(classDef.getModifiers());
 		
 		if (classDef.isAnnotation())
 			flags.add(AccessFlags.ANNOTATION);
 		if (classDef.isEnum())
 			flags.add(AccessFlags.ENUM);
 		if (classDef.isInterface())
 			flags.add(AccessFlags.INTERFACE);
 		if (classDef.isSynthetic())
 			flags.add(AccessFlags.SYNTHETIC);
 
 		return DexUtils.assembleAccessFlags(flags);
 	}
 
 	@Override
 	public Collection<DexClassType> getInterfaces() {
 		val interfaceClasses = classDef.getInterfaces();
 		
 		if (interfaceClasses.length == 0)
 			return Collections.emptyList();
 		
 		val allInterfaces = new ArrayList<DexClassType>(interfaceClasses.length);
 		for (val interfaceClass : interfaceClasses)
 			allInterfaces.add(
 				DexClassType.parse(DexClassType.jvm2dalvik(interfaceClass.getName()), typeCache));
 		
 		return allInterfaces;
 	}
 
 	@Override
 	public Collection<IMethodScanner> getMethodScanners() {
 		val allMethods = new ArrayList<IMethodScanner>();
 
 		val methods = classDef.getDeclaredMethods();
 		for (val method : methods)
 			allMethods.add(new VmMethodScanner(method, typeCache));
 		
 		val constructors = classDef.getDeclaredConstructors();
 		for (val constructor : constructors)
 			allMethods.add(new VmConstructorScanner(constructor, typeCache));
 		
 		return allMethods;
 	}
 
 	@Override
 	public Collection<IFieldScanner> getStaticFieldScanners() {
 		val fields = classDef.getDeclaredFields();
 		
 		val allStaticFields = new ArrayList<IFieldScanner>(fields.length);
 		for (val field : fields)
 			if (Modifier.isStatic(field.getModifiers()))
 				allStaticFields.add(new VmFieldScanner(field, typeCache));
 		
 		return allStaticFields;
 	}
 
 	@Override
 	public Collection<IFieldScanner> getInstanceFieldScanners() {
 		val fields = classDef.getDeclaredFields();
 		
 		val allInstanceFields = new ArrayList<IFieldScanner>(fields.length);
 		for (val field : fields)
 			if (!Modifier.isStatic(field.getModifiers()))
 				allInstanceFields.add(new VmFieldScanner(field, typeCache));
 		
 		return allInstanceFields;
 	}
 	
 	static Set<AccessFlags> convertModifier(int jvmMod) {
 		val flags = new HashSet<AccessFlags>();
 		
 		if (Modifier.isAbstract(jvmMod))
 			flags.add(AccessFlags.ABSTRACT);
 		if (Modifier.isFinal(jvmMod))
 			flags.add(AccessFlags.FINAL);
 		if (Modifier.isInterface(jvmMod))
 			flags.add(AccessFlags.INTERFACE);
 		if (Modifier.isNative(jvmMod))
 			flags.add(AccessFlags.NATIVE);
 		if (Modifier.isPrivate(jvmMod))
 			flags.add(AccessFlags.PRIVATE);
 		if (Modifier.isProtected(jvmMod))
 			flags.add(AccessFlags.PROTECTED);
 		if (Modifier.isPublic(jvmMod))
 			flags.add(AccessFlags.PUBLIC);
 		if (Modifier.isStatic(jvmMod))
 			flags.add(AccessFlags.STATIC);
 		if (Modifier.isStrict(jvmMod))
 			flags.add(AccessFlags.STRICTFP);
 		if (Modifier.isSynchronized(jvmMod))
 			flags.add(AccessFlags.SYNCHRONIZED);
 		if (Modifier.isTransient(jvmMod))
 			flags.add(AccessFlags.TRANSIENT);
 		if (Modifier.isVolatile(jvmMod))
 			flags.add(AccessFlags.VOLATILE);
 		
 		return flags;
 	}
 }
