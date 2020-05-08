 package com.aciertoteam.dbunit;
 
 import java.sql.Types;
 import org.dbunit.dataset.datatype.DataType;
 import org.dbunit.dataset.datatype.DataTypeException;
 import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
 
 /**
  * @author Bogdan Nechyporenko
  */
 public class HsqlDataTypeFactory extends DefaultDataTypeFactory {
 
     public DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException {
         if (sqlType == Types.BOOLEAN) {
             return DataType.BOOLEAN;
         }
         return super.createDataType(sqlType, sqlTypeName);
     }
 }
