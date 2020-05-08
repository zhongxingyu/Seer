 package jp.ac.osaka_u.ist.sel.metricstool.main.data.target.unresolved;
 
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.ClassInfoManager;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.PrimitiveTypeInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.external.ExternalClassInfo;
 
 
 /**
  * Javap̌^ϊ[eBeB
  * 
  * @author y-higo
  */
 final public class JavaTypeConverter extends TypeConverter {
 
     static final JavaTypeConverter SINGLETON = new JavaTypeConverter();
     
     @Override
     public ExternalClassInfo getWrapperClass(final PrimitiveTypeInfo primitiveType) {
 
         if (null == primitiveType) {
             throw new NullPointerException();
         }
 
         switch (primitiveType.getType()) {
         case BOOLEAN:
             final ExternalClassInfo booleanClass = (ExternalClassInfo) ClassInfoManager
                     .getInstance().getClassInfo(new String[] { "java", "lang", "Boolean" });
             return booleanClass;
         case BYTE:
             final ExternalClassInfo byteClass = (ExternalClassInfo) ClassInfoManager.getInstance()
                     .getClassInfo(new String[] { "java", "lang", "Byte" });
             return byteClass;
         case CHAR:
             final ExternalClassInfo charClass = (ExternalClassInfo) ClassInfoManager.getInstance()
                    .getClassInfo(new String[] { "java", "lang", "Charactr" });
             return charClass;
         case DOUBLE:
             final ExternalClassInfo doubleClass = (ExternalClassInfo) ClassInfoManager
                     .getInstance().getClassInfo(new String[] { "java", "lang", "Double" });
             return doubleClass;
         case FLOAT:
             final ExternalClassInfo floatClass = (ExternalClassInfo) ClassInfoManager.getInstance()
                     .getClassInfo(new String[] { "java", "lang", "Float" });
             return floatClass;
         case INT:
             final ExternalClassInfo intClass = (ExternalClassInfo) ClassInfoManager.getInstance()
                     .getClassInfo(new String[] { "java", "lang", "Integer" });
             return intClass;
         case LONG:
             final ExternalClassInfo longClass = (ExternalClassInfo) ClassInfoManager.getInstance()
                     .getClassInfo(new String[] { "java", "lang", "Long" });
             return longClass;
         case SHORT:
             final ExternalClassInfo shortClass = (ExternalClassInfo) ClassInfoManager.getInstance()
                     .getClassInfo(new String[] { "java", "lang", "Short" });
             return shortClass;
         default:
             throw new IllegalArgumentException();
         }
 
     }
 
 }
