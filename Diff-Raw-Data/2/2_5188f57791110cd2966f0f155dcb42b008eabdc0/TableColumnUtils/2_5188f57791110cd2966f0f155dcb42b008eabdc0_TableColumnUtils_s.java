 package jetbrick.schema.app.model.methods;
 
 import java.util.HashMap;
 import java.util.Map;
 import jetbrick.schema.app.model.EnumItem;
 import jetbrick.schema.app.model.TableColumn;
 import org.apache.commons.lang3.StringEscapeUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.text.WordUtils;
 
 public class TableColumnUtils {
 
     public static String getter(TableColumn c) {
         return "get" + WordUtils.capitalize(c.getFieldName());
     }
 
     public static String setter(TableColumn c) {
         return "set" + WordUtils.capitalize(c.getFieldName());
     }
 
     public static String rsGetter(TableColumn c) {
        Map<Class<?>, String> map = new HashMap<Class<?>, String>(20);
         map.put(String.class, "String");
         map.put(Boolean.class, "Boolean");
         map.put(Byte.class, "Byte");
         map.put(Short.class, "Short");
         map.put(Integer.class, "Int");
         map.put(Long.class, "Long");
         map.put(Float.class, "Float");
         map.put(Double.class, "Double");
         map.put(java.sql.Date.class, "Date");
         map.put(java.sql.Time.class, "Time");
         map.put(java.sql.Timestamp.class, "Timestamp");
         map.put(java.util.Date.class, "Timestamp");
         map.put(java.math.BigDecimal.class, "BigDecimal");
         map.put(java.sql.Blob.class, "Blob");
         map.put(java.sql.Clob.class, "Clob");
         map.put(byte[].class, "Bytes");
         map.put(java.net.URL.class, "URL");
         map.put(java.sql.Array.class, "Array");
 
         String type = map.get(c.getFieldClass());
         if (type == null) type = "Object";
         return "get" + type;
     }
 
     public static String sqlTypeName(TableColumn c) {
         return c.getTable().getSchema().getDialect().asSqlType(c.getTypeName(), c.getTypeLength(), c.getTypeScale());
     }
 
     public static String simpleClassName(TableColumn c) {
         if (c.getFieldClass().getName().startsWith("java.lang.")) {
             return c.getFieldClass().getSimpleName();
         } else {
             return c.getFieldClass().getName();
         }
     }
 
     public static String fullClassName(TableColumn c) {
         return c.getFieldClass().getName();
     }
 
     public static String enumGroupDescription(TableColumn c) {
         StringBuilder sb = new StringBuilder();
         sb.append(StringUtils.trimToEmpty(c.getDescription()));
         if (c.getEnumGroup() != null) {
             for (EnumItem en : c.getEnumGroup().getItems()) {
                 if (sb.length() > 0) {
                     sb.append("<br/>");
                 }
                 sb.append(en.getId() + "-" + en.getName());
             }
         }
         return sb.toString();
     }
 
     public static String fieldDefaultValue(TableColumn c) {
         StringBuffer sb = new StringBuffer();
         if (String.class.equals(c.getFieldClass())) {
             sb.append("\"");
             sb.append(StringEscapeUtils.escapeJava((String) c.getDefaultValue()));
             sb.append("\"");
         } else if (Double.class.equals(c.getFieldClass())) {
             sb.append(c.getDefaultValue() + "D");
         } else if (Float.class.equals(c.getFieldClass())) {
             sb.append(c.getDefaultValue() + "F");
         } else if (Long.class.equals(c.getFieldClass())) {
             sb.append(c.getDefaultValue() + "L");
         } else {
             sb.append(c.getDefaultValue());
         }
         return sb.toString();
     }
 
     // hibernate hbm column 定义
     public static String hbmColumnDefination(TableColumn c) {
         StringBuffer sb = new StringBuffer();
         sb.append("<column name='");
         sb.append(StringEscapeUtils.escapeHtml4(c.getTable().getSchema().getDialect().getIdentifier(c.getColumnName())));
         sb.append("'");
         if (c.getTypeLength() != null) {
             sb.append(" length='");
             sb.append(c.getTypeLength());
             sb.append("'");
         }
         if (c.getTypeScale() != null) {
             sb.append(" scale='");
             sb.append(c.getTypeScale());
             sb.append("'");
         }
         sb.append(" />");
         return StringUtils.replaceChars(sb.toString(), "'", "\"");
     }
 }
