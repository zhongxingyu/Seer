 package net.madz.db.core.impl.validation.mysql;
 
 import java.util.HashMap;
 
 import net.madz.db.utils.MessageConsts;
 
 public class ErrorCodeTypes {
 
     public static final int FIELD_NOT_MATCHED = 001;
     // For Schema level, Error Code is 1XX
     public static final int SCHEMA_TABLES_NUMBER_DIFFERENT = 104;
     // For Table level, Error Code is 2XX
     public static final int TABLE_NOT_FOUND = 201;
     public static final int TABLE_COLUMNS_NUMBER_DIFFERENT = 202;
     public static final int TABLE_INDEXES_NUMBER_NOT_MATCHED = 203;
     public static final int TABLE_FOREIGN_KEYS_SIZE_NOT_MATCHED = 204;
     public static final int TABLE_PRIMARY_KEY_NOT_MATCHED = 205;
     // For Column level, Error Code is 3XX
     // For Index level, Error Code is 4XX
     public static final int INDEX_NOT_FOUND = 401;
     public static final int INDEX_ENTRIES_NUMBER_NOT_MATCHED = 405;
     // For Foreign Key level, Error Code is 5XX
     public static final int FOREIGN_KEYS_NUMBER_DIFFERENT = 501;
     public static final int FOREIGN_KEY_ENTRIES_NUMBER_DIFFERENT = 502;
     final static HashMap<Integer, String> ERROR_CODES_MAP = new HashMap<Integer, String>();
     static {
         ERROR_CODES_MAP.put(ErrorCodeTypes.FIELD_NOT_MATCHED, "Field is not matched.");
         ERROR_CODES_MAP.put(ErrorCodeTypes.SCHEMA_TABLES_NUMBER_DIFFERENT, "Schema tables' number is different.");
         ERROR_CODES_MAP.put(ErrorCodeTypes.TABLE_NOT_FOUND, "Table is not found in target database.");
         ERROR_CODES_MAP.put(ErrorCodeTypes.TABLE_COLUMNS_NUMBER_DIFFERENT, "Table column number is different.");
         ERROR_CODES_MAP.put(ErrorCodeTypes.TABLE_INDEXES_NUMBER_NOT_MATCHED, "Table indexes' number is different.");
         ERROR_CODES_MAP.put(ErrorCodeTypes.INDEX_NOT_FOUND, "Index is not found in target table.");
         ERROR_CODES_MAP.put(ErrorCodeTypes.INDEX_ENTRIES_NUMBER_NOT_MATCHED, "Index entries' number is different.");
         ERROR_CODES_MAP.put(ErrorCodeTypes.FOREIGN_KEYS_NUMBER_DIFFERENT, "Foreign keys' number is different.");
         ERROR_CODES_MAP.put(ErrorCodeTypes.FOREIGN_KEY_ENTRIES_NUMBER_DIFFERENT, "Foreign key entries' number is different.");
     }
 
     public static String getErrorCodeMessage(int errorCode) {
         if ( !ERROR_CODES_MAP.containsKey(errorCode) ) {
             throw new IllegalArgumentException(MessageConsts.THE_ERROR_CODE_INVALID);
         }
         return ERROR_CODES_MAP.get(errorCode);
     }
 }
