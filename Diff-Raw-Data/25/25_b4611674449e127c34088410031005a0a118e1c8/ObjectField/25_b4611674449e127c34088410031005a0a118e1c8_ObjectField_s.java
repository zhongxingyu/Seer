 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.base.field;
 
 /**
  * @(#)ImageField.java  0.00 12-Feb-97 Don Corley
  *
  * Copyright © 2012 tourapp.com. All Rights Reserved.
  *      don@tourgeek.com
  */
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.Map;
 
 import org.jbundle.base.db.Record;
 import org.jbundle.base.model.DBConstants;
 import org.jbundle.model.db.Convert;
 import org.jbundle.model.screen.ComponentParent;
 import org.jbundle.model.screen.ScreenComponent;
 import org.jbundle.model.screen.ScreenLoc;
 import org.jbundle.thin.base.db.Constants;
import org.jbundle.util.osgi.ClassService;
 import org.jbundle.util.osgi.finder.ClassServiceUtility;
 
 
 /**
  * ObjectField - A serialized Java Object.
  *
  * @version 1.0.0
  * @author    Don Corley
  */
 public class ObjectField extends BaseField
 {
 	private static final long serialVersionUID = 1L;
 
     /**
      * Constructor.
      */
     public ObjectField()
     {
         super();
     }
     /**
      * Constructor.
      * @param record The parent record.
      * @param strName The field name.
      * @param iDataLength The maximum string length (pass -1 for default).
      * @param strDesc The string description (usually pass null, to use the resource file desc).
      * @param strDefault The default value (if object, this value is the default value, if string, the string is the default).
      */
     public ObjectField(Record record, String strName, int iDataLength, String strDesc, Object strDefault)
     {
         this();
         this.init(record, strName, iDataLength, strDesc, strDefault);
     }
     /**
      * Initialize the object.
      * @param record The parent record.
      * @param strName The field name.
      * @param iDataLength The maximum string length (pass -1 for default).
      * @param strDesc The string description (usually pass null, to use the resource file desc).
      * @param strDefault The default value (if object, this value is the default value, if string, the string is the default).
      */
     public void init(Record record, String strName, int iDataLength, String strDesc, Object strDefault)
     {
         super.init(record, strName, iDataLength, strDesc, strDefault);
         if (iDataLength == DBConstants.DEFAULT_FIELD_LENGTH)
             m_iMaxLength = BIG_DEFAULT_LENGTH;
         m_classData = Object.class;
     }
     /**
      * Creates a new object of the same class as this object.
      * @return     a clone of this instance.
      * @exception  CloneNotSupportedException  if the object's class does not support the <code>Cloneable</code> interface.
      * @see        java.lang.Cloneable
      */
     public Object clone() throws CloneNotSupportedException
     {
         BaseField field = new ObjectField(null, m_strFieldName, m_iMaxLength, m_strFieldDesc, null);
         field.setRecord(m_record);     // Set table without adding to table field list
         return field;
     }
     /**
      * Set up the default screen control for this field.
      * You should override this method depending of the concrete display type.
      * @param itsLocation Location of this component on screen (ie., GridBagConstraint).
      * @param targetScreen Where to place this component (ie., Parent screen or GridBagLayout).
      * @param converter The converter to set the screenfield to.
      * @param iDisplayFieldDesc Display the label? (optional).
      * @return Return the component or ScreenField that is created for this field.
      */
     public ScreenComponent setupDefaultView(ScreenLoc itsLocation, ComponentParent targetScreen, Convert converter, int iDisplayFieldDesc, Map<String, Object> properties)
     {
         return null;
     }
     /**
      * Current string length.
      * @return The current string length (-1 if unknown).
      */
     public int getLength()
     {
         if (m_data == null)
             return 0;
         if (m_data instanceof String)
             return ((String)m_data).length(); // Actual length
         return -1;  // Unknown (non zero)
     }
     /**
      * Get the SQL type of this field.
      * Typically OBJECT or LONGBINARY.
      * @param bIncludeLength Include the field length in this description.
      * @param properties Database properties to determine the SQL type.
      */
     public String getSQLType(boolean bIncludeLength, Map<String, Object> properties) 
     {
         String strType = (String)properties.get("OBJECT");
         if (strType == null)
             strType = "LONGBINARY";     // The default SQL Type (Byte)
         return  strType;        // The default SQL Type
     }
     /**
      * Move the physical binary data to this SQL parameter row.
      * This method uses the getBinaryStream resultset method.
      * @param resultset The resultset to get the SQL data from.
      * @param iColumn the column in the resultset that has my data.
      * @exception SQLException From SQL calls.
      */
     public void moveSQLToField(ResultSet resultset, int iColumn) throws SQLException
     {
         InputStream inStream = resultset.getBinaryStream(iColumn);
         if (resultset.wasNull())
             this.setData(null, false, DBConstants.READ_MOVE); // Null value
         else
         {
             try   {
                 // This is kind of weird, but for some reason, the inStream will only accept read(byte[]), so I have to do this:
                 byte rgBytes[] = new byte[2048];
                 ByteArrayOutputStream baOut = new ByteArrayOutputStream();
                 while (true)
                 {
                     int iRead = 0;
                     try   {
                         iRead = inStream.read(rgBytes);
                     } catch (EOFException ex) {
                         iRead = 0;
                     }
                     if (iRead > 0)
                         baOut.write(rgBytes, 0, iRead);
                     if (iRead < rgBytes.length)
                         break;      // End of stream
                 }
                 rgBytes = baOut.toByteArray();
                 Object objData = null;
                 if (rgBytes.length > 0)
                 {
                    String string = new String(rgBytes, ClassService.OBJECT_ENCODING);
                     objData = ClassServiceUtility.getClassService().convertStringToObject(string, null);
 //x                    ByteArrayInputStream ibyStream = new ByteArrayInputStream(rgBytes);
 //x                    ObjectInputStream iStream = new ObjectInputStream(ibyStream);
 //x                    objData = iStream.readObject();
                 }
                 this.setData(objData, false, DBConstants.READ_MOVE);
             } catch (IOException ex)    {
                 ex.printStackTrace();   // Never
             } catch (ClassNotFoundException ex)   {
                 ex.printStackTrace();   // Never
             }
         }
     }
     /**
      * Move the physical binary data to this SQL parameter row.
      * This method uses the setBinaryStrem statement method.
      * @param statement The SQL prepare statement.
      * @param iType the type of SQL statement.
      * @param iParamColumn The column in the prepared statement to set the data.
      * @exception SQLException From SQL calls.
      */
     public void getSQLFromField(PreparedStatement statement, int iType, int iParamColumn) throws SQLException
     {
         if (this.isNull())
             statement.setNull(iParamColumn, Types.BLOB);
         else
         {
             Object data = this.getData();
             ByteArrayOutputStream ostream = new ByteArrayOutputStream();
             ObjectOutputStream p = null;
             try   {
                 p = new ObjectOutputStream(ostream);
                 p.writeObject(data);
                 p.flush();
                 ostream.close();
             } catch (IOException ex)    {
                 ex.printStackTrace();   // Never
             }
 
             byte[] rgBytes = ostream.toByteArray();
             InputStream iStream = new ByteArrayInputStream(rgBytes);
             try {
                 statement.setBinaryStream(iParamColumn, iStream, rgBytes.length);
             } catch (java.lang.ArrayIndexOutOfBoundsException ex) {
                 ex.printStackTrace();
             }
         }
     }
     /**
      * Read the physical data from a stream file and set this field.
      * @param daIn Input stream to read this field's data from.
      * @param bFixedLength If false (default) be sure to save the length in the stream.
      * @return boolean Success?
      */
     public boolean read(DataInputStream daIn, boolean bFixedLength) // Fixed length = false
     {
         ObjectInputStream iStream = null;
         try   {
             iStream = new ObjectInputStream(daIn);
             Object objData = iStream.readObject();
             if (objData instanceof String) if (((String)objData).length() == 0)
                 objData = null;
             int errorCode = this.setData(objData, false, DBConstants.READ_MOVE);
             iStream.close();    // Unlink ObjectInputStream
             return (errorCode == DBConstants.NORMAL_RETURN);    // Success
         } catch (IOException ex)    {
             ex.printStackTrace();   // Never
         } catch (ClassNotFoundException ex)   {
             ex.printStackTrace();   // Never
         }
         return false; // Error
     }
     /**
      * Write the physical data in this field to a stream file.
      * @param daOut Output stream to add this field to.
      * @param bFixedLength If false (default) be sure to get the length from the stream.
      * @return boolean Success?
      */
     public boolean write(DataOutputStream daOut, boolean bFixedLength)
     {
         Object data = this.getData();
         if (data == null)
             data = Constants.BLANK;
         ObjectOutputStream p = null;
         try   {
             p = new ObjectOutputStream(daOut);
             p.writeObject(data);
             p.flush();
             p.close();  // Unlink ObjectOutputStream
         } catch (IOException ex)    {
             ex.printStackTrace();   // Never
             return false;
         }
         return true;    // Success
     }
 } 
