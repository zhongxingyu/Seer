 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target;
 
 
 import java.util.HashSet;
 import java.util.Set;
 
 
 /**
  * ONXɒ`ĂtB[h̏ۑ邽߂̃NXD
  * 
  * @author higo
  */
 @SuppressWarnings("serial")
 public final class ExternalFieldInfo extends FieldInfo {
 
     /**
      * OC^C`ĂNX^ďD 
      * 
      * @param name tB[h
      * @param type ^
      * @param definitionClass tB[h`ĂNX
      */
     public ExternalFieldInfo(final Set<ModifierInfo> modifiers, final String name,
             final TypeInfo type, final ExternalClassInfo definitionClass,
             final boolean privateVisible, final boolean namespaceVisible,
             final boolean inheritanceVisible, final boolean publicVisible, final boolean instance) {
         super(modifiers, name, type, definitionClass, privateVisible, namespaceVisible,
                inheritanceVisible, publicVisible, instance, 0, 0, 0, 0);
     }
 
     /**
      * Oƒ`ĂNX^ďD ^͕sD
      * 
      * @param name tB[h
      * @param definitionClass tB[h`ĂNX
      */
     public ExternalFieldInfo(final String name, final ExternalClassInfo definitionClass) {
         super(new HashSet<ModifierInfo>(), name, UnknownTypeInfo.getInstance(), definitionClass,
                true, true, true, true, true, 0, 0, 0, 0);
     }
 
     /**
      * O^ďD`ĂNXsȏꍇɗpD
      * 
      * @param name tB[h
      */
     public ExternalFieldInfo(final String name) {
         super(new HashSet<ModifierInfo>(), name, UnknownTypeInfo.getInstance(),
                ExternalClassInfo.UNKNOWN, true, true, true, true, true, 0, 0, 0, 0);
     }
 
     /**
      * ExternalFieldInfo ł͗płȂ
      */
     @Override
     public Set<ModifierInfo> getModifiers() {
         throw new CannotUseException();
     }
 }
