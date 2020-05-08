 package plugin.s3.model;
 
 import org.hibernate.HibernateException;
 import play.Logger;
 import plugin.s3.model.impl.S3MockBlob;
 import plugin.s3.model.impl.S3RealBlob;
 
 import java.io.InputStream;
 import java.io.Serializable;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * @author Lukasz Piliszczuk <lukasz.piliszczuk AT zenika.com>
  */
 public class S3Blob implements S3BlobInterface {
 
     public static Mode mode = Mode.MOCK;
 
     private S3BlobInterface blob;
 
     public S3Blob() {
         if (mode == Mode.MOCK) {
             blob = new S3MockBlob();
         } else {
             blob = new S3RealBlob();
         }
     }
 
     @Override
     public void delete() {
         blob.delete();
     }
 
     @Override
     public InputStream get() {
         return blob.get();
     }
 
     @Override
     public void set(InputStream is, String type) {
         blob.set(is, type);
     }
 
     @Override
     public long length() {
         return blob.length();
     }
 
     @Override
     public String type() {
         return blob.type();
     }
 
     @Override
     public boolean exists() {
         return blob.exists();
     }
 
     @Override
     public int[] sqlTypes() {
         return blob.sqlTypes();
     }
 
     @Override
     public Class returnedClass() {
         return S3Blob.class;
     }
 
     @Override
     public boolean equals(Object o, Object o1) throws HibernateException {
         return blob.equals(o, o1);
     }
 
     @Override
     public int hashCode(Object o) throws HibernateException {
         return blob.hashCode(o);
     }
 
     @Override
     public Object nullSafeGet(ResultSet rs, String[] names, Object o) throws HibernateException, SQLException {
         blob = (S3BlobInterface) blob.nullSafeGet(rs, names, o);
         return this;
     }
 
     @Override
     public void nullSafeSet(PreparedStatement preparedStatement, Object o, int i) throws HibernateException, SQLException {
         blob.nullSafeSet(preparedStatement, blob, i);
     }
 
     @Override
     public Object deepCopy(Object o) throws HibernateException {
        return blob.deepCopy(((S3Blob) o).getBlob());
     }
 
     @Override
     public boolean isMutable() {
         return blob.isMutable();
     }
 
     @Override
     public Serializable disassemble(Object o) throws HibernateException {
         return blob.disassemble(o);
     }
 
     @Override
     public Object assemble(Serializable serializable, Object o) throws HibernateException {
         return blob.assemble(serializable, o);
     }
 
     @Override
     public Object replace(Object o, Object o1, Object o2) throws HibernateException {
         return blob.replace(o, o1, o2);
     }
 
    public S3BlobInterface getBlob() {
        return blob;
    }

     public enum Mode {
         MOCK, REAL
     }
 }
