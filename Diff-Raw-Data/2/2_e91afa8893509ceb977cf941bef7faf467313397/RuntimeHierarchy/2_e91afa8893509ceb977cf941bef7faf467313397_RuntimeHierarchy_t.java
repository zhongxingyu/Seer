 package uk.ac.cam.db538.dexter.hierarchy;
 
 import java.util.Map;
 
 import lombok.Getter;
 import lombok.val;
 import uk.ac.cam.db538.dexter.dex.type.DexArrayType;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
 import uk.ac.cam.db538.dexter.dex.type.DexReferenceType;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 import uk.ac.cam.db538.dexter.dex.type.DexTypeCache;
 
 public class RuntimeHierarchy {
 
     @Getter private final DexTypeCache typeCache;
     private final Map<DexClassType, BaseClassDefinition> definedClasses;
     @Getter private final ClassDefinition root;
 
     public RuntimeHierarchy(Map<DexClassType, BaseClassDefinition> definedClasses, ClassDefinition root, DexTypeCache typeCache) {
         this.definedClasses = definedClasses;
         this.root = root;
         this.typeCache = typeCache;
     }
 
     public BaseClassDefinition getBaseClassDefinition(DexReferenceType refType) {
         if (refType instanceof DexClassType) {
             val result = definedClasses.get((DexClassType) refType);
             if (result == null)
                 throw new NoClassDefFoundError("Cannot find " + refType.getPrettyName());
             else
                 return result;
         } else if (refType instanceof DexArrayType)
             return root;
         else
             throw new Error();
     }
 
     public ClassDefinition getClassDefinition(DexReferenceType refType) {
         val baseClass = getBaseClassDefinition(refType);
         if (baseClass instanceof ClassDefinition)
             return (ClassDefinition) baseClass;
         else
             throw new HierarchyException("Type " + refType.getPrettyName() + " is not a proper class");
     }
 
     public InterfaceDefinition getInterfaceDefinition(DexReferenceType refType) {
         val baseClass = getBaseClassDefinition(refType);
         if (baseClass instanceof InterfaceDefinition)
             return (InterfaceDefinition) baseClass;
         else
             throw new HierarchyException("Type " + refType.getPrettyName() + " is not an interface class");
     }
 
     public static enum TypeClassification {
         PRIMITIVE,
         REF_INTERNAL,
         REF_EXTERNAL,
         REF_UNDECIDABLE,
         ARRAY_PRIMITIVE,
         ARRAY_REFERENCE
     }
 
     public TypeClassification classifyType(DexRegisterType type) {
         if (type instanceof DexPrimitiveType)
             return TypeClassification.PRIMITIVE;
 
         else if (type instanceof DexArrayType) {
             if (((DexArrayType) type).getElementType() instanceof DexPrimitiveType)
                 return TypeClassification.ARRAY_PRIMITIVE;
             else
                 return TypeClassification.ARRAY_REFERENCE;
         }
 
         else {
             val classDef = getBaseClassDefinition((DexReferenceType) type);
 
            if (classDef.isInternal() && !classDef.isAbstract())
                 return TypeClassification.REF_INTERNAL;
             else if (classDef.hasInternalNonAbstractChildren())
                 return TypeClassification.REF_UNDECIDABLE;
             else
                 return TypeClassification.REF_EXTERNAL;
         }
     }
 }
