 package uk.ac.cam.db538.dexter.dex;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 import lombok.val;
 
 import org.jf.dexlib.DexFile;
 import org.jf.dexlib.MethodIdItem;
 import org.jf.dexlib.ProtoIdItem;
 import org.jf.dexlib.StringIdItem;
 import org.jf.dexlib.TypeIdItem;
 import org.jf.dexlib.EncodedValue.AnnotationEncodedValue;
 import org.jf.dexlib.EncodedValue.ArrayEncodedSubValue;
 import org.jf.dexlib.EncodedValue.ArrayEncodedValue;
 import org.jf.dexlib.EncodedValue.EncodedValue;
 import org.jf.dexlib.EncodedValue.EnumEncodedValue;
 import org.jf.dexlib.EncodedValue.FieldEncodedValue;
 import org.jf.dexlib.EncodedValue.MethodEncodedValue;
 import org.jf.dexlib.EncodedValue.StringEncodedValue;
 import org.jf.dexlib.EncodedValue.TypeEncodedValue;
 import org.jf.dexlib.Util.AccessFlags;
 
 import uk.ac.cam.db538.dexter.dex.field.DexField;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.dex.type.DexFieldId;
 import uk.ac.cam.db538.dexter.dex.type.DexMethodId;
 import uk.ac.cam.db538.dexter.dex.type.DexPrototype;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 import uk.ac.cam.db538.dexter.dex.type.DexType;
 import uk.ac.cam.db538.dexter.hierarchy.FieldDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.MethodDefinition;
 import uk.ac.cam.db538.dexter.hierarchy.RuntimeHierarchy;
 
 public class DexUtils {
 
     public static int assembleAccessFlags(Collection<AccessFlags> accessFlags) {
         int result = 0;
         for (val flag : accessFlags)
             result |= flag.getValue();
         return result;
     }
 
     public static int assembleAccessFlags(AccessFlags ... accessFlags) {
         return assembleAccessFlags(Arrays.asList(accessFlags));
     }
 
     public static DexField getInstanceField(Dex dex, DexClassType fieldClass, String fieldName, DexRegisterType fieldType) {
         for (val clazz : dex.getClasses())
             if (clazz.getClassDef().getType().equals(fieldClass)) {
                 for (val field : clazz.getInstanceFields())
                     if (field.getFieldDef().getFieldId().getName().equals(fieldName) && field.getFieldDef().getFieldId().getType().equals(fieldType))
                         return field;
                 return null;
             }
         return null;
     }
 
     public static DexField getStaticField(Dex dex, DexClassType fieldClass, String fieldName, DexRegisterType fieldType) {
         for (val clazz : dex.getClasses())
             if (clazz.getClassDef().getType().equals(fieldClass)) {
                 for (val field : clazz.getStaticFields())
                     if (field.getFieldDef().getFieldId().getName().equals(fieldName) && field.getFieldDef().getFieldId().getType().equals(fieldType))
                         return field;
                 return null;
             }
         return null;
     }
 
     public static String parseString(StringIdItem stringItem) {
         if (stringItem == null)
             return null;
         else
             return stringItem.getStringValue();
     }
 
     private static FieldDefinition findStaticField(DexClassType clsType, DexRegisterType fieldType, String name, RuntimeHierarchy hierarchy) {
         val fieldId = DexFieldId.parseFieldId(name, fieldType, hierarchy.getTypeCache());
         val classDef = hierarchy.getBaseClassDefinition(clsType);
        return classDef.getAccessedStaticField(fieldId);
     }
 
     private static MethodDefinition findStaticMethod(DexClassType clsType, DexPrototype prototype, String name, RuntimeHierarchy hierarchy) {
         val methodId = DexMethodId.parseMethodId(name, prototype, hierarchy.getTypeCache());
         val classDef = hierarchy.getBaseClassDefinition(clsType);
         return classDef.getMethod(methodId);
     }
 
     public static EncodedValue cloneEncodedValue(DexFile outFile, EncodedValue value, DexAssemblingCache asmCache) {
         val hierarchy = asmCache.getHierarchy();
         val typeCache = hierarchy.getTypeCache();
 
         switch (value.getValueType()) {
         case VALUE_ARRAY:
             val arrayValue = (ArrayEncodedSubValue) value;
             val isSubValue = !(value instanceof ArrayEncodedValue);
 
             int innerValuesCount = arrayValue.values.length;
             val innerValues = new EncodedValue[innerValuesCount];
             for (int i = 0; i < innerValuesCount; ++i)
                 innerValues[i] = cloneEncodedValue(outFile, arrayValue.values[i], asmCache);
 
             if (isSubValue)
                 return new ArrayEncodedSubValue(innerValues);
             else
                 return new ArrayEncodedValue(innerValues);
 
         case VALUE_BOOLEAN:
         case VALUE_BYTE:
         case VALUE_CHAR:
         case VALUE_DOUBLE:
         case VALUE_FLOAT:
         case VALUE_INT:
         case VALUE_LONG:
         case VALUE_NULL:
         case VALUE_SHORT:
             return value;
 
         case VALUE_ENUM:
             val enumValue = (EnumEncodedValue) value;
             return new EnumEncodedValue(
                        asmCache.getField(findStaticField(
                                              DexClassType.parse(enumValue.value.getContainingClass().getTypeDescriptor(), typeCache),
                                              DexRegisterType.parse(enumValue.value.getFieldType().getTypeDescriptor(), typeCache),
                                              enumValue.value.getFieldName().getStringValue(),
                                              hierarchy)));
 
         case VALUE_FIELD:
             val fieldValue = (FieldEncodedValue) value;
             return new FieldEncodedValue(
                        asmCache.getField(findStaticField(
                                              DexClassType.parse(fieldValue.value.getContainingClass().getTypeDescriptor(), typeCache),
                                              DexRegisterType.parse(fieldValue.value.getFieldType().getTypeDescriptor(), typeCache),
                                              fieldValue.value.getFieldName().getStringValue(),
                                              hierarchy)));
 
         case VALUE_METHOD:
             val methodValue = (MethodEncodedValue) value;
             val methodDef = findStaticMethod(
                     DexClassType.parse(methodValue.value.getContainingClass().getTypeDescriptor(), typeCache),
                     DexPrototype.parse(methodValue.value.getPrototype(), typeCache),
                     methodValue.value.getMethodName().getStringValue(),
                     hierarchy);
             if (methodDef != null) {
                 return new MethodEncodedValue(asmCache.getMethod(methodDef));
             } else {
                 return new MethodEncodedValue(MethodIdItem.internMethodIdItem(outFile,
                         asmCache.getType(DexClassType.parse(methodValue.value.getContainingClass().getTypeDescriptor(), typeCache)),
                         asmCache.getPrototype(DexPrototype.parse(methodValue.value.getPrototype(), typeCache)),
                         StringIdItem.internStringIdItem(outFile, methodValue.value.getMethodName().getStringValue())));
             }
 
         case VALUE_STRING:
             val stringValue = (StringEncodedValue) value;
             return new StringEncodedValue(asmCache.getStringConstant(stringValue.value.getStringValue()));
 
         case VALUE_TYPE:
             val typeValue = (TypeEncodedValue) value;
             return new TypeEncodedValue(asmCache.getType(DexType.parse(typeValue.value.getTypeDescriptor(), typeCache)));
 
         case VALUE_ANNOTATION:
             val annotationValue = (AnnotationEncodedValue) value;
 
             val newNames = new StringIdItem[annotationValue.names.length];
             for (int i = 0; i < annotationValue.names.length; ++i)
                 newNames[i] = asmCache.getStringConstant(annotationValue.names[i].getStringValue());
 
             val newEncodedValues = new EncodedValue[annotationValue.values.length];
             for (int i = 0; i < annotationValue.values.length; ++i)
                 newEncodedValues[i] = cloneEncodedValue(outFile, annotationValue.values[i], asmCache);
 
             return new AnnotationEncodedValue(
                        asmCache.getType(DexType.parse(annotationValue.annotationType.getTypeDescriptor(), typeCache)),
                        newNames,
                        newEncodedValues);
 
         default:
             throw new RuntimeException("Unexpected EncodedValue type: " + value.getValueType().name());
         }
     }
 }
