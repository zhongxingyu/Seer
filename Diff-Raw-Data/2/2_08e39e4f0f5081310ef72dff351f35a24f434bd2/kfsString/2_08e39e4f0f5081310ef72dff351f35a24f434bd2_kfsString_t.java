 package kfs.kfsDbi;
 
 import java.io.UnsupportedEncodingException;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author pavedrim
  */
 public class kfsString extends kfsColObject implements kfsDbiColumnComparator {
 
     protected static final String defaultCharSet = "UTF-8";
     private static final Logger l = Logger.getLogger(kfsString.class.getName());
     private final int maxLength;
     private final String charset;
 
     public kfsString(final String name, final String label, final int maxLength, final int position) {
         this(name, label, maxLength, position, defaultCharSet);
     }
 
     public kfsString(final String name, final String label, final int maxLength, final int position, final String charset) {
         super(name, label, position, true);
         this.maxLength = maxLength;
         this.charset = charset;
     }
 
     public void setData(String data, kfsRowData row) {
         setString(data, row);
     }
 
     public String getData(kfsRowData row) {
         return getString(row);
     }
 
     public String getString(kfsRowData row) {
         Object o = super.getObject(row);
         return (o == null) ? "" : (String) o;
     }
 
     public void setString(String data, kfsRowData row) {
         if (data == null) {
             super.setObject("", row);
         } else {
             data = data.trim();
             if (data.length() > this.maxLength) {
                 l.log(Level.WARNING, "kfsString.{0}({1}) try to set longer text ({2}): {3}", //
                         new Object[]{getColumnName(), maxLength, data.length(), data});
                super.setObject(data.substring(0, maxLength), row);
             } else {
                 super.setObject(data, row);
             }
         }
     }
 
     public String getStringHead(kfsRowData rd, int shortTxtLen) {
         String s = getString(rd);
         if (s.length() < shortTxtLen) {
             return s.replaceAll("\\p{Cntrl}", ".");
         }
         return s.substring(0, shortTxtLen - 3).replaceAll("\\p{Cntrl}", ".") + "...";
     }
 
     /*
      * public String getString() { return data; }
      *
      * public String getStringStriped() { return data.replaceAll("\\s+", " "); }
      */
     @Override
     public String getColumnCreateTable(kfsDbServerType serverType) {
         switch (serverType) {
             case kfsDbiOracle:
                 return getColumnName() + " VARCHAR(" + maxLength + ") ";
             case kfsDbiMysql:
                 return getColumnName() + " VARCHAR(" + maxLength + ") CHARACTER SET utf8 ";
             case kfsDbiPostgre:
             case kfsDbiSqlite:
                 return getColumnName() + " VARCHAR(" + maxLength + ") ";
         }
         return null;
     }
 
     public int getColumnMaxLength() {
         return maxLength;
     }
 
     @Override
     public void setParam(int inx, PreparedStatement ps, kfsRowData data) throws SQLException {
         ps.setString(inx, getString(data));
     }
 
     @Override
     public void getParam(int inx, ResultSet ps, kfsRowData row) throws SQLException {
         try {
             byte[] strb = ps.getBytes(inx);
             if (strb == null) {
                 setString("", row);
             } else {
                 setString(new String(strb, charset), row);
             }
         } catch (UnsupportedEncodingException ex) {
             l.log(Level.SEVERE, "Error in getParam + " + inx, ex);
         }
     }
 
     @Override
     public Class<?> getColumnJavaClass() {
         return String.class;
     }
 
     @Override
     public Object getObject(kfsRowData row) {
         return ((String) super.getObject(row)).replaceAll("\\p{Cntrl}", ".");
     }
 
     @Override
     public int compare(kfsRowData t, kfsRowData t1) {
         return getSortDirection() * getData(t).compareTo(getData(t1));
     }
 }
