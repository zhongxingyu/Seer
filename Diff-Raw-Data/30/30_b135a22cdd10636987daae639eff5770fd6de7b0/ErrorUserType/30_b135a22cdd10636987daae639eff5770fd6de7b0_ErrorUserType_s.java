 package org.geoserver.monitor.hib;
 
import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.sql.Blob;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 
 import org.hibernate.HibernateException;
import org.hibernate.cfg.annotations.SetBinder;
 import org.hibernate.lob.BlobImpl;
 import org.hibernate.usertype.UserType;
 
 public class ErrorUserType implements UserType {
 
     public Object assemble(Serializable cached, Object owner) throws HibernateException {
         return cached;
     }
 
     public Object deepCopy(Object value) throws HibernateException {
         return value;
     }
 
     public Serializable disassemble(Object value) throws HibernateException {
         return (Throwable) value;
     }
 
     public boolean equals(Object x, Object y) throws HibernateException {
         return x == y;
     }
 
     public int hashCode(Object x) throws HibernateException {
         return x.hashCode();
     }
 
     public boolean isMutable() {
         return false;
     }
 
     public Object nullSafeGet(ResultSet rs, String[] names, Object owner)
             throws HibernateException, SQLException {
         Blob blob = rs.getBlob(names[0]);
         if (blob == null) {
             return null;
         }
 //        byte[] bytes = rs.getBytes(names[0]);
 //        if (bytes == null) {
 //            return null;
 //        }
         
         
         ObjectInputStream in = null;
         try {
             //in = new ObjectInputStream(new ByteArrayInputStream(bytes));
             in = new ObjectInputStream(blob.getBinaryStream());
             return in.readObject();
         } catch (IOException e) {
             throw new HibernateException(e);
         } catch (ClassNotFoundException e) {
             throw new HibernateException(e);
         }
         finally {
             if (in != null) {
                 try {
                     in.close();
                 } catch (IOException e) {}
             }
         }
     }
 
     public void nullSafeSet(PreparedStatement st, Object value, int index)
             throws HibernateException, SQLException {
         if (value != null) {
             try {
                 ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                 ObjectOutputStream out = new ObjectOutputStream(bytes);
                 out.writeObject(value);
                 out.flush();
                 
                //st.setBytes(index, bytes.toByteArray());
                st.setBlob(index, new BlobImpl(bytes.toByteArray()));
                 
                 out.close();
             } 
             catch (IOException e) {
                 throw new HibernateException(e);
             }
         }
         else {
             st.setNull(index, Types.BLOB);
             //st.setBytes(index, null);
         }
     }
 
     public Object replace(Object original, Object target, Object owner) throws HibernateException {
         return target;
     }
 
     public Class returnedClass() {
         return Throwable.class;
     }
 
     public int[] sqlTypes() {
         return new int[]{Types.BLOB};
     }
 
 }
