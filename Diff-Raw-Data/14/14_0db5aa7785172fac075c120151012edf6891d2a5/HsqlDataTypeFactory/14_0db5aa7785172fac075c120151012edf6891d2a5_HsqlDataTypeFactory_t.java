 package com.aciertoteam.dbunit;
 
 import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
 import org.dbunit.dataset.datatype.DataType;
 import org.dbunit.dataset.datatype.DataTypeException;
 import org.dbunit.dataset.datatype.DefaultDataTypeFactory;
 
 /**
  * Creates a factory
 * 
  * @author Bogdan Nechyporenko
  */
 public class HsqlDataTypeFactory extends DefaultDataTypeFactory {
 
    private static final Collection DATABASE_PRODUCTS = Arrays.asList("derby", "H2");

     public DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException {
         if (sqlType == Types.BOOLEAN) {
             return DataType.BOOLEAN;
         }
         return super.createDataType(sqlType, sqlTypeName);
     }

    @Override
    public Collection getValidDbProducts() {
        return DATABASE_PRODUCTS;
    }
 }
