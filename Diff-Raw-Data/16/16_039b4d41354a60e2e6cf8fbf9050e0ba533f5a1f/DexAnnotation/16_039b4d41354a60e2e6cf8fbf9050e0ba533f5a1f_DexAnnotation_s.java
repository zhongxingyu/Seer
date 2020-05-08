 package uk.ac.cam.db538.dexter.dex;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import lombok.Getter;
 import lombok.val;
 
 import org.apache.bcel.classfile.AnnotationEntry;
 import org.jf.dexlib.AnnotationItem;
 import org.jf.dexlib.AnnotationSetItem;
 import org.jf.dexlib.AnnotationVisibility;
 import org.jf.dexlib.DexFile;
 import org.jf.dexlib.StringIdItem;
 import org.jf.dexlib.EncodedValue.AnnotationEncodedSubValue;
import org.jf.dexlib.EncodedValue.AnnotationEncodedValue;
 import org.jf.dexlib.EncodedValue.ArrayEncodedSubValue;
 import org.jf.dexlib.EncodedValue.ArrayEncodedValue;
 import org.jf.dexlib.EncodedValue.EncodedValue;
 import org.jf.dexlib.EncodedValue.EnumEncodedValue;
 import org.jf.dexlib.EncodedValue.FieldEncodedValue;
 import org.jf.dexlib.EncodedValue.MethodEncodedValue;
 import org.jf.dexlib.EncodedValue.StringEncodedValue;
 import org.jf.dexlib.EncodedValue.TypeEncodedValue;
 
 import uk.ac.cam.db538.dexter.dex.method.DexPrototype;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 import uk.ac.cam.db538.dexter.dex.type.DexType;
 
 public class DexAnnotation {
 
   @Getter private final DexClassType type;
   @Getter private final AnnotationVisibility visibility;
   private final Map<String, EncodedValue> params;
 
   public DexAnnotation(DexClassType type, AnnotationVisibility visibility) {
     this.type = type;
     this.visibility = visibility;
     this.params = new HashMap<String, EncodedValue>();
   }
 
   public DexAnnotation(AnnotationItem anno, DexParsingCache cache) {
     this(DexClassType.parse(anno.getEncodedAnnotation().annotationType.getTypeDescriptor(), cache),
          anno.getVisibility());
 
     val encAnno = anno.getEncodedAnnotation();
     int len = encAnno.names.length;
     for (int i = 0; i < len; ++i)
       addParam(encAnno.names[i].getStringValue(), encAnno.values[i]);
   }
 
   public DexAnnotation(AnnotationEntry anno, DexParsingCache cache) {
     this(DexClassType.parse(anno.getAnnotationType(), cache),
          (anno.isRuntimeVisible()) ? AnnotationVisibility.RUNTIME : AnnotationVisibility.SYSTEM);
 
     // TODO: load parameters of the annotation
     // not necessary though, this is used to load android.jar
   }
 
   public void addParam(String name, EncodedValue value) {
     params.put(name, value);
   }
 
   public Map<String, EncodedValue> getParams() {
     return Collections.unmodifiableMap(params);
   }
 
   public static Set<DexAnnotation> parseAll(AnnotationSetItem annotations, DexParsingCache cache) {
     if (annotations == null)
       return new HashSet<DexAnnotation>();
 
     val items = annotations.getAnnotations();
     val list = new HashSet<DexAnnotation>(items.length);
 
     for (val anno : items)
       list.add(new DexAnnotation(anno, cache));
 
     return list;
   }
 
   private static EncodedValue cloneEncodedValue(EncodedValue value, DexFile outFile, DexAssemblingCache asmCache) {
     val parsingCache = asmCache.getParsingCache();
 
     switch (value.getValueType()) {
     case VALUE_ARRAY:
       val arrayValue = (ArrayEncodedSubValue) value;
       val isSubValue = !(value instanceof ArrayEncodedValue);
 
       int innerValuesCount = arrayValue.values.length;
       val innerValues = new EncodedValue[innerValuesCount];
       for (int i = 0; i < innerValuesCount; ++i)
         innerValues[i] = cloneEncodedValue(arrayValue.values[i], outFile, asmCache);
 
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
                asmCache.getField(
                  DexClassType.parse(enumValue.value.getContainingClass().getTypeDescriptor(), parsingCache),
                  DexRegisterType.parse(enumValue.value.getFieldType().getTypeDescriptor(), parsingCache),
                  enumValue.value.getFieldName().getStringValue()));
 
     case VALUE_FIELD:
       val fieldValue = (FieldEncodedValue) value;
       return new FieldEncodedValue(
                asmCache.getField(
                  DexClassType.parse(fieldValue.value.getContainingClass().getTypeDescriptor(), parsingCache),
                  DexRegisterType.parse(fieldValue.value.getFieldType().getTypeDescriptor(), parsingCache),
                  fieldValue.value.getFieldName().getStringValue()));
 
     case VALUE_METHOD:
       val methodValue = (MethodEncodedValue) value;
       return new MethodEncodedValue(
                asmCache.getMethod(
                  DexClassType.parse(methodValue.value.getContainingClass().getTypeDescriptor(), parsingCache),
                  new DexPrototype(methodValue.value.getPrototype(), parsingCache),
                  methodValue.value.getMethodName().getStringValue()));
 
     case VALUE_STRING:
       val stringValue = (StringEncodedValue) value;
       return new StringEncodedValue(asmCache.getStringConstant(stringValue.value.getStringValue()));
 
     case VALUE_TYPE:
       val typeValue = (TypeEncodedValue) value;
       return new TypeEncodedValue(asmCache.getType(DexType.parse(typeValue.value.getTypeDescriptor(), parsingCache)));
 
     default:
       throw new RuntimeException("Unexpected EncodedValue type: " + value.getValueType().name());
     }
   }
 
   public AnnotationItem writeToFile(DexFile outFile, DexAssemblingCache cache) {
     int paramCount = params.size();
     int paramIndex = 0;
     val paramNames = new StringIdItem[paramCount];
     val paramValues = new EncodedValue[paramCount];
     for (val param : params.entrySet()) {
       paramNames[paramIndex] = cache.getStringConstant(param.getKey());
       paramValues[paramIndex] = cloneEncodedValue(param.getValue(), outFile, cache);
       paramIndex++;
     }
 
     val subValue = new AnnotationEncodedSubValue(cache.getType(type), paramNames, paramValues);
     return AnnotationItem.internAnnotationItem(outFile, visibility, subValue);
   }
 }
