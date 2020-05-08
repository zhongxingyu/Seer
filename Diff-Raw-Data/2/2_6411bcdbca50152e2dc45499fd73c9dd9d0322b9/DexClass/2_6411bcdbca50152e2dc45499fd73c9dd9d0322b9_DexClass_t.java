 package uk.ac.cam.db538.dexter.dex;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import lombok.Getter;
 import lombok.val;
 
 import org.jf.dexlib.AnnotationDirectoryItem;
 import org.jf.dexlib.AnnotationDirectoryItem.FieldAnnotation;
 import org.jf.dexlib.AnnotationDirectoryItem.MethodAnnotation;
 import org.jf.dexlib.AnnotationDirectoryItem.ParameterAnnotation;
 import org.jf.dexlib.AnnotationItem;
 import org.jf.dexlib.AnnotationSetItem;
 import org.jf.dexlib.ClassDataItem;
 import org.jf.dexlib.ClassDataItem.EncodedField;
 import org.jf.dexlib.ClassDataItem.EncodedMethod;
 import org.jf.dexlib.ClassDefItem;
 import org.jf.dexlib.ClassDefItem.StaticFieldInitializer;
 import org.jf.dexlib.DexFile;
 import org.jf.dexlib.EncodedValue.EncodedValue;
 
 import uk.ac.cam.db538.dexter.dex.field.DexInstanceField;
 import uk.ac.cam.db538.dexter.dex.field.DexStaticField;
 import uk.ac.cam.db538.dexter.dex.method.DexMethod;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.hierarchy.BaseClassDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.ClassDefinition;
 
 public class DexClass {
 
 	@Getter private final Dex parentFile;
 	@Getter private final BaseClassDefinition classDef;
   
 	private final List<DexMethod> _methods;
 	@Getter private final List<DexMethod> methods;
   
 	private final List<DexInstanceField> _instanceFields;
 	@Getter private final List<DexInstanceField> instanceFields;
 
 	private final List<DexStaticField> _staticFields;
 	@Getter private final List<DexStaticField> staticFields;
 	
 	private final List<DexAnnotation> _annotations;
 	@Getter private final List<DexAnnotation> annotations;
   
 	@Getter private final String sourceFile;
   
 	public DexClass(Dex parent, BaseClassDefinition classDef, String sourceFile) {
 		this.parentFile = parent;
 		this.classDef = classDef;
     
 		this._methods = new ArrayList<DexMethod>();
     	this.methods = Collections.unmodifiableList(this._methods);
     
     	this._instanceFields = new ArrayList<DexInstanceField>();
     	this.instanceFields = Collections.unmodifiableList(this._instanceFields);
 
     	this._staticFields = new ArrayList<DexStaticField>();
     	this.staticFields = Collections.unmodifiableList(this._staticFields);
     	
     	this._annotations = new ArrayList<DexAnnotation>();
     	this.annotations = Collections.unmodifiableList(this._annotations);
     
     	this.sourceFile = sourceFile;
 	}
   
 	public DexClass(Dex parent, ClassDefItem clsItem) {
 		this(parent,
 		     init_FindClassDefinition(parent, clsItem),
 		     DexUtils.parseString(clsItem.getSourceFile()));
 
 		val annotationDirectory = clsItem.getAnnotations();
 		this._annotations.addAll(init_ParseAnnotations(parent, annotationDirectory));
 		
 		val clsData = clsItem.getClassData();
 		if (clsData != null) {
 			
 			// static fields
 			int sfieldIndex = 0;
 			for (val sfieldItem : clsData.getStaticFields())
				this._staticFields.add(new DexStaticField(this, clsItem, sfieldItem, sfieldIndex++, annotationDirectory));
 
 			// instance fields
 			for (val ifieldItem : clsData.getInstanceFields())
 				this._instanceFields.add(new DexInstanceField(this, ifieldItem, annotationDirectory));
 			
 			// methods
 			for (val methodItem : clsData.getDirectMethods())
 				this._methods.add(new DexMethod(this, methodItem, annotationDirectory));
 			for (val methodItem : clsData.getVirtualMethods())
 				this._methods.add(new DexMethod(this, methodItem, annotationDirectory));
 		}
 	}
 	
 	private static BaseClassDefinition init_FindClassDefinition(Dex parent, ClassDefItem clsItem) {
 		val hierarchy = parent.getHierarchy();
 		val clsType = DexClassType.parse(clsItem.getClassType().getTypeDescriptor(), 
 		                                 hierarchy.getTypeCache());
 		return hierarchy.getBaseClassDefinition(clsType); 
 	}
 
 	private static List<DexAnnotation> init_ParseAnnotations(Dex parent, AnnotationDirectoryItem annoDir) {
 		if (annoDir == null)
 			return Collections.emptyList();
 		else
 			return DexAnnotation.parseAll(annoDir.getClassAnnotations(), parent.getTypeCache());
 	}
 	
 	public List<DexClassType> getInterfaceTypes() {
 		if (classDef instanceof ClassDefinition) {
 			val ifaceDefs = ((ClassDefinition) classDef).getInterfaces();
 			if (ifaceDefs.isEmpty())
 				return Collections.emptyList();
 
 			val list = new ArrayList<DexClassType>(ifaceDefs.size());
 			for (val ifaceDef : ifaceDefs)
 				list.add(ifaceDef.getType());
 			return list;
 		} else
 			return Collections.emptyList();
 	}
 
 	public void addAnnotation(DexAnnotation anno) {
 		this._annotations.add(anno);
 	}
 
 	public void instrument(DexInstrumentationCache cache) {
 //		System.out.println("Instrumenting class " + this.classDef.getType().getPrettyName());
 //	  
 //		for (val method : this._methods)
 //			method.instrument(cache);
 //
 //		this.addAnnotation(new DexAnnotation(
 //			parentFile.getAuxiliaryDex().getAnno_InternalClass().getType(),
 //			AnnotationVisibility.RUNTIME));
 	}
 
 	public void writeToFile(DexFile outFile, DexAssemblingCache cache) {
 		System.out.println("Assembling class " + this.classDef.getType().getPrettyName());
     
 		val classAnnotations = this.getAnnotations();
 
 		val asmClassType = cache.getType(classDef.getType());
 		val asmSuperType = cache.getType(classDef.getSuperclass().getType());
 		val asmAccessFlags = DexUtils.assembleAccessFlags(classDef.getAccessFlags());
 		val asmInterfaces = cache.getTypeList(getInterfaceTypes());
 		val asmSourceFile = cache.getStringConstant(sourceFile);
 
 		val asmClassAnnotations = new ArrayList<AnnotationItem>(classAnnotations.size());
 		for (val anno : classAnnotations)
 			asmClassAnnotations.add(anno.writeToFile(outFile, cache));
 
 		val asmMethodAnnotations = new ArrayList<MethodAnnotation>(_methods.size());
 		for (val method : _methods) {
 			val methodAnno = method.assembleAnnotations(outFile, cache);
 			if (methodAnno != null)
 				asmMethodAnnotations.add(methodAnno);
 		}
 
 		val asmFieldAnnotations = new ArrayList<FieldAnnotation>(_instanceFields.size() + _staticFields.size());
 		for (val field : _instanceFields) {
 			val fieldAnno = field.assembleAnnotations(outFile, cache);
 			if (fieldAnno != null)
 				asmFieldAnnotations.add(fieldAnno);
 		}
 		for (val field : _staticFields) {
 			val fieldAnno = field.assembleAnnotations(outFile, cache);
 			if (fieldAnno != null)
 				asmFieldAnnotations.add(fieldAnno);
 		}
 
 		val asmParamAnnotations = new ArrayList<ParameterAnnotation>(_methods.size());
 		for (val method : _methods) {
 			val paramAnno = method.assembleParameterAnnotations(outFile, cache);
 			if (paramAnno != null)
 				asmParamAnnotations.add(paramAnno);
 		}
 
 		AnnotationSetItem asmClassAnnotationSet = null;
 		if (asmClassAnnotations.size() > 0)
 			asmClassAnnotationSet = AnnotationSetItem.internAnnotationSetItem(
                                     outFile,
                                     asmClassAnnotations);
     
 		AnnotationDirectoryItem asmAnnotations = null;
 		if (asmClassAnnotationSet!= null || asmFieldAnnotations.size() != 0 || 
 				asmMethodAnnotations.size() != 0 || asmParamAnnotations.size() != 0) {
 			asmAnnotations = AnnotationDirectoryItem.internAnnotationDirectoryItem(
                                                    outFile,
                                                    asmClassAnnotationSet,
                                                    asmFieldAnnotations,
                                                    asmMethodAnnotations,
                                                    asmParamAnnotations);
 		}
 
 		val asmStaticFields = new LinkedList<EncodedField>();
 		val asmInstanceFields = new LinkedList<EncodedField>();
 		val asmDirectMethods = new LinkedList<EncodedMethod>();
 		val asmVirtualMethods = new LinkedList<EncodedMethod>();
 		val staticFieldInitializers = new LinkedList<StaticFieldInitializer>();
 
 		for (val field : _staticFields) {
 			EncodedField outField = field.writeToFile(outFile, cache);  
 			asmStaticFields.add(outField);
         
 			EncodedValue initialValue = field.getInitialValue();
 			if (initialValue != null)
 				initialValue = DexUtils.cloneEncodedValue(initialValue, cache);
 			staticFieldInitializers.add(new StaticFieldInitializer(initialValue, outField));
 		}
     
 		for (val field : _instanceFields)
 			asmInstanceFields.add(field.writeToFile(outFile, cache));
 
 		for (val method : _methods) {
 			if (method.getMethodDef().isVirtual())
 				asmVirtualMethods.add(method.writeToFile(outFile, cache));
 			else
 				asmDirectMethods.add(method.writeToFile(outFile, cache));
 		}
 
 		val classData = ClassDataItem.internClassDataItem(
 			  outFile,
 			  asmStaticFields,
 			  asmInstanceFields,
 			  asmDirectMethods,
 			  asmVirtualMethods);
 
 		ClassDefItem.internClassDefItem(
 				outFile, asmClassType, asmAccessFlags, asmSuperType,
 				asmInterfaces, asmSourceFile, asmAnnotations,
 				classData, staticFieldInitializers);
 	}
 }
