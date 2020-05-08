 package uk.ac.cam.db538.dexter.dex;
 
 import java.util.ArrayList;
 import java.util.EnumSet;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import lombok.AllArgsConstructor;
 import lombok.Getter;
 import lombok.val;
 
 import org.jf.dexlib.Util.AccessFlags;
 
 import uk.ac.cam.db538.dexter.dex.code.DexRegister;
 import uk.ac.cam.db538.dexter.dex.code.elem.DexCodeElement;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_Const;
 import uk.ac.cam.db538.dexter.dex.code.insn.DexInstruction_StaticPut;
 import uk.ac.cam.db538.dexter.dex.type.DexClassType;
 import uk.ac.cam.db538.dexter.dex.type.DexPrimitiveType;
 import uk.ac.cam.db538.dexter.dex.type.DexRegisterType;
 import uk.ac.cam.db538.dexter.utils.Cache;
 import uk.ac.cam.db538.dexter.utils.Triple;
 
 public class DexInstrumentationCache {
 
   private final Dex parentFile;
 
   private final Map<DexField, DexField> fieldInstrumentation;
  private final Cache<Triple<DexClassType, DexPrimitiveType, String>, DexField> staticExternalFieldInstrumentation =
  new Cache<Triple<DexClassType, DexPrimitiveType, String>, DexField>() {
     private boolean fieldExists(String name) {
      for (val field : parentFile.getExternalStaticFieldTaint_Class().getFields())
         if (field.getName().equals(name))
           return true;
       return false;
     }
 
     private String generateFieldName() {
       long suffix = 0L;
       String fieldName;
       do {
         fieldName = "$" + suffix++;
       } while (fieldExists(fieldName));
       return fieldName;
     }
 
     @Override
     protected DexField createNewEntry(Triple<DexClassType, DexPrimitiveType, String> key) {
      val taintClass = parentFile.getExternalStaticFieldTaint_Class();
      val taintClinit = parentFile.getExternalStaticFieldTaint_Clinit();
 
      val newField = new DexField(
         taintClass,
         generateFieldName(),
         DexRegisterType.parse("I", parentFile.getParsingCache()),
         EnumSet.of(AccessFlags.PUBLIC, AccessFlags.STATIC),
         null);
       taintClass.addField(newField);
 
       // add initialisation to <clinit>
      val taintClinit_Code = taintClinit.getCode();
      val regZero = new DexRegister();
       taintClinit_Code.insertAfter(
         new DexCodeElement[] {
           new DexInstruction_Const(taintClinit_Code, regZero, 0L),
           new DexInstruction_StaticPut(taintClinit_Code, regZero, newField)
         },
         taintClinit_Code.getStartingLabel());
 
       return newField;

     }
   };
   @Getter private final List<InstrumentationWarning> warnings;
 
   public DexInstrumentationCache(Dex parentFile) {
     this.parentFile = parentFile;
     fieldInstrumentation = new HashMap<DexField, DexField>();
     warnings = new ArrayList<InstrumentationWarning>();
   }
 
   public DexField getTaintField(DexField f) {
     if (!fieldInstrumentation.containsKey(f))
       fieldInstrumentation.put(f, f.instrument());
     return fieldInstrumentation.get(f);
   }
 
   public DexField getTaintField_ExternalStatic(DexClassType clazz, DexPrimitiveType fieldType, String fieldName) {
     return staticExternalFieldInstrumentation.getCachedEntry(
              new Triple<DexClassType, DexPrimitiveType, String>(clazz, fieldType, fieldName));
   }
 
   @AllArgsConstructor
   @Getter
   public static class InstrumentationWarning {
     private final String message;
   }
 }
