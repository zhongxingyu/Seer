 package at.bestsolution.persistence.emap.generator.internal;
 
 import at.bestsolution.persistence.emap.eMap.EAttribute;
 import at.bestsolution.persistence.emap.generator.DatabaseSupport;
 import com.google.common.base.Objects;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EEnum;
 
 @SuppressWarnings("all")
 public class MSSQLServerDatabaseSupport extends DatabaseSupport {
   public String getDatabaseId() {
     return "mssql";
   }
   
   public String getSequenceStatement(final EAttribute primaryKey) {
     return "";
   }
   
   public String processInsert(final EAttribute primaryKey, final String insert) {
     return insert;
   }
   
   public boolean supportsGeneratedKeys() {
     return true;
   }
   
   public String getDatabaseType(final EDataType dataType) {
     if ((dataType instanceof EEnum)) {
       return "nvarchar(255)";
     } else {
       boolean _or = false;
       String _name = dataType.getName();
       boolean _equals = Objects.equal("EInt", _name);
       if (_equals) {
         _or = true;
       } else {
         String _name_1 = dataType.getName();
         boolean _equals_1 = Objects.equal("EIntegerObject", _name_1);
         _or = (_equals || _equals_1);
       }
       if (_or) {
         return "int";
       } else {
         boolean _or_1 = false;
         String _name_2 = dataType.getName();
         boolean _equals_2 = Objects.equal("ELong", _name_2);
         if (_equals_2) {
           _or_1 = true;
         } else {
           String _name_3 = dataType.getName();
           boolean _equals_3 = Objects.equal("ELongObject", _name_3);
           _or_1 = (_equals_2 || _equals_3);
         }
         if (_or_1) {
           return "bigint";
         } else {
           boolean _or_2 = false;
           boolean _or_3 = false;
           String _name_4 = dataType.getName();
           boolean _equals_4 = Objects.equal("EDouble", _name_4);
           if (_equals_4) {
             _or_3 = true;
           } else {
             String _name_5 = dataType.getName();
             boolean _equals_5 = Objects.equal("EDoubleObject", _name_5);
             _or_3 = (_equals_4 || _equals_5);
           }
           if (_or_3) {
             _or_2 = true;
           } else {
             String _name_6 = dataType.getName();
             boolean _equals_6 = Objects.equal("EBigDecimal", _name_6);
             _or_2 = (_or_3 || _equals_6);
           }
           if (_or_2) {
             return "real";
           } else {
             String _name_7 = dataType.getName();
             boolean _equals_7 = Objects.equal("EString", _name_7);
             if (_equals_7) {
               return "nvarchar(255)";
             } else {
               String _instanceClassName = dataType.getInstanceClassName();
               boolean _equals_8 = Objects.equal("java.sql.Clob", _instanceClassName);
               if (_equals_8) {
                 return "ntext";
               } else {
                 String _instanceClassName_1 = dataType.getInstanceClassName();
                 boolean _equals_9 = Objects.equal("java.sql.Blob", _instanceClassName_1);
                 if (_equals_9) {
                   return "varbinary";
                 } else {
                   boolean _or_4 = false;
                   String _name_8 = dataType.getName();
                   boolean _equals_10 = Objects.equal("EBoolean", _name_8);
                   if (_equals_10) {
                     _or_4 = true;
                   } else {
                     String _name_9 = dataType.getName();
                     boolean _equals_11 = Objects.equal("EBooleanObject", _name_9);
                     _or_4 = (_equals_10 || _equals_11);
                   }
                   if (_or_4) {
                     return "bit";
                   } else {
                     String _name_10 = dataType.getName();
                     boolean _equals_12 = Objects.equal("EDate", _name_10);
                     if (_equals_12) {
                       return "datetime";
                     }
                   }
                 }
               }
             }
           }
         }
       }
     }
     String _name_11 = dataType.getName();
     String _plus = ("***UNKOWN " + _name_11);
     return (_plus + "***");
   }
   
   public String getAutokeyDefinition(final EAttribute primaryKey) {
     return "IDENTITY(1,1)";
   }
   
   public boolean isPrimaryKeyPartOfColDef(final EAttribute primaryKey) {
     return true;
   }
  
  public boolean supportsGeneratedKeyAsResultSet() {
    return false;
  }
 }
