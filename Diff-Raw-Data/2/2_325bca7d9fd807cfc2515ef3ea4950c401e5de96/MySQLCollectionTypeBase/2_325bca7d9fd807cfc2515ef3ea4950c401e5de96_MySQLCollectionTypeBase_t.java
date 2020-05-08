 package net.madz.db.core.meta.immutable.mysql.datatype;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import net.madz.db.core.meta.mutable.mysql.MySQLColumnMetaDataBuilder;
 import net.madz.db.utils.MessageConsts;
 
 public abstract class MySQLCollectionTypeBase implements DataType {
 
     private List<String> values;
     private String charsetName;
     private String collationName;
 
     public List<String> getValues() {
         return values;
     }
 
     public MySQLCollectionTypeBase addValue(String value) {
         if ( null == value ) {
             return this;
         }
         if ( null == values ) {
             values = new LinkedList<String>();
         }
         values.add(value);
         return this;
     }
 
     public String getCharsetName() {
         return charsetName;
     }
 
     public void setCharsetName(String charsetName) {
         this.charsetName = charsetName;
     }
 
     public String getCollationName() {
         return collationName;
     }
 
     public void setCollationName(String collationName) {
         this.collationName = collationName;
     }
 
     @Override
     public void build(MySQLColumnMetaDataBuilder builder) {
         builder.setSqlTypeName(getName());
         final StringBuilder result = new StringBuilder();
         result.append(getName());
         if ( 0 >= values.size() ) {
             throw new IllegalArgumentException(MessageConsts.COLLECTION_DATA_TYPE_SHOULD_NOT_BE_NULL);
         }
         result.append("(");
         for ( String value : values ) {
             builder.addTypeValue(value);
            result.append("'").append(value).append("'");
             result.append(",");
         }
         result.deleteCharAt(result.length() - 1);
         result.append(")");
         builder.setColumnType(result.toString());
     }
 }
