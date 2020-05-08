 package de.jpaw.bonaparte.jpa;
 
 import java.sql.Types;
 
 import org.eclipse.persistence.mappings.DatabaseMapping;
 import org.eclipse.persistence.mappings.converters.Converter;
 import org.eclipse.persistence.mappings.foundation.AbstractDirectMapping;
 import org.eclipse.persistence.sessions.Session;
 
 import de.jpaw.util.ByteArray;
 
 public class ByteArrayConverter implements Converter {
 
     private static final long serialVersionUID = 1L;
 
     @Override
     public Object convertDataValueToObjectValue(Object dataValue, Session session) {
        return dataValue == null ? null : new ByteArray((byte[]) dataValue, 0, -1);
     }
 
     @Override
     public Object convertObjectValueToDataValue(Object objectValue, Session session) {
         return objectValue == null ? null : ((ByteArray) objectValue).getBytes();
     }
 
     @Override
     public void initialize(DatabaseMapping mapping, Session session) {
         ((AbstractDirectMapping) mapping).setFieldType(Types.VARBINARY);
     }
 
     @Override
     public boolean isMutable() {
         return false;
     }
 
 }
