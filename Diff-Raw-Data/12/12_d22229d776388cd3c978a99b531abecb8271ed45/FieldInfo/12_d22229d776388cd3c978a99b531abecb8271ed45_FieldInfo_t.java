 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.thin.base.db;
 
 /**
  * @(#)FieldInfo.java 0.00 12-Feb-97 Don Corley
  *
  * Copyright © 2012 tourapp.com. All Rights Reserved.
  *      don@tourgeek.com
  */
 
 import java.io.Serializable;
 import java.util.Date;
 import java.util.Vector;
 
 import org.jbundle.model.db.Convert;
 import org.jbundle.model.db.Field;
 import org.jbundle.model.db.Rec;
 import org.jbundle.model.screen.FieldComponent;
 import org.jbundle.model.screen.ScreenComponent;
 
 /**
  * FieldInfo - This is the base class for all fields.
  *
  * @version 1.0.0
  * @author    Don Corley
  */
 public class FieldInfo extends Converter
     implements Field, Serializable
 {
 	private static final long serialVersionUID = 1L;
 
     /**
      * Has this field been modified?
      */
     protected boolean m_bModified = false;
 	/**
      * Parent.
      */
     protected FieldList m_record = null;
     /**
      * Field name.
      */
     protected String m_strFieldName = null;
     /**
      * Field description for displays (Use ClassnameResource if null).
      */
     protected String m_strFieldDesc = null;
     /**
      * Minimum string length.
      */
     protected int m_iMinLength = 0;
     /**
      * Maximum string length.
      */
     protected int m_iMaxLength = 0;
     /**
      * Maximum string length.
      */
     protected boolean m_bHidden = false;
     /**
      * Pointer to the actual data!!!
      */
     protected Object m_data = null;
     /**
      * Default value
      */
     protected Object m_objDefault = null;
     /**
      * Raw data class.
      */
     protected Class<?> m_classData = null;
     /**
      * For numeric fields, this is the number of decimals, for date, either DATE_ONLY or TIME_ONLY.
      * Note: This is used more in the thick "NumberField" class.
      * Be careful, the default is set to 2 which means float and double are rounded to two and datetime
      * fields are rounded to the second (which matches the thick implementation).
      * For date scale, set to non -1 for date, DATE_ONLY or TIME_ONLY.
      */
     protected int m_ibScale = 2;
     /**
      * List of Components displaying this field.
      */
     protected transient Vector<Object> m_vScreenField = null;
 
     /**
      * Constructor.
      */
     public FieldInfo()
     {
         super();
     }
     /**
      * Constructor.
      */
     public FieldInfo(FieldList record, String strName, int iDataLength, String strDesc, Object strDefault)
     {
         this();
         this.init(record, strName, iDataLength, strDesc, strDefault);
     }
     /**
      * Initialize this field.
      * After setting up the input params, initField() is called.
      * @param record The parent record. This field is added to the record's field list.
      * @param strName The field name.
      * @param iDataLength The maximum length (DEFAULT_FIELD_LENGTH if default).
      * @param strDesc The string description (pass null to use the description in the resource file).
      * @param objDefault The default (initial) field value in string or native format.
      */
     public void init(Rec record, String strName, int iDataLength, String strDesc, Object objDefault)
     {
         m_record = (FieldList)record;
         if (m_record != null)
             m_record.addField(this);
         m_strFieldName = strName;
         m_iMaxLength = iDataLength;
         m_strFieldDesc = strDesc;
         if (objDefault != null)
             m_classData = objDefault.getClass();
         else
             m_classData = String.class;
         m_bHidden = false;
         m_bModified = false;
         m_data = null;  // The actual data!!!
         m_iMinLength = 0;
         m_ibScale = 2;
         m_vScreenField = null;
         m_objDefault = objDefault;
 
         super.init(null);
         this.initField(false);      // Don't display because there can't be any sFields yet.
     }
     /**
      * Free this field's resources.
      * Removes this fieldlist from the parent record.
      */
     public void free()
     {
         super.free();
         if (m_record != null)
             m_record.removeField(this);
         m_record = null;
         m_strFieldName = null;
         m_strFieldDesc = null;
         if (m_vScreenField != null)
         {   // Remove all the screen fields   
             m_vScreenField.removeAllElements();
             m_vScreenField = null;
         }
     }
     /**
      * This screen component is displaying this field.
      * @param Object sField The screen component.. either a awt.Component or a ScreenField.
      */
     public void addComponent(Object sField)
     {
         if (m_vScreenField == null)
             m_vScreenField = new Vector<Object>();
         m_vScreenField.addElement(sField);        // Notify this screen field if I change
     }
     /**
      * Remove this control from this field's control list.
      * @param Object sField The screen component.. either a awt.Component or a ScreenField
      */
     public void removeComponent(Object screenField)
     {   // DO NOT call Object or inherited
         if (m_vScreenField != null) if (screenField != null)
             m_vScreenField.removeElement(screenField);        // Remove this component
     }
     /**
      * Get the component at this position.
      * @return The component at this position or null.
      */
     public Object getComponent(int iPosition)
     {
         if (m_vScreenField == null)
             return null;
         if (iPosition < m_vScreenField.size())
         {
             try   {
                 return m_vScreenField.elementAt(iPosition);
             }
             catch (ArrayIndexOutOfBoundsException e)    {
                 return null;
             }
         }
         return null;
     }
     /**
      * Creates a new object of the same class as this object.
      * Must override.
      * @return     a clone of this instance.
      * @exception  CloneNotSupportedException  if the object's class does not support the <code>Cloneable</code> interface.
      * @see        java.lang.Cloneable
      */
     public Object clone() throws CloneNotSupportedException
     {
         return null;
     }
     /**
      * Compare field to this and return < > or = (-,+,0).
      * @return compare value.
      */
     public int compareTo(Field field)
     { // Override this
         return this.compareTo(field.getData());
     }
     /**
      * Compare field to this and return < > or = (-,+,0).
      * @return compare value.
      */
     @SuppressWarnings({ "unchecked", "rawtypes" })
     public int compareTo(Object dataCompare)
     { // Override this
         Object data = this.getData();
         if (data == null)
         {
             if (dataCompare == null)
                 return 0; // Both null -> equal
             return -1;  // Anything is larger than a null.
         }
         if (dataCompare == null)
             return +1;  // This data is larger than a null
         try   {
             if (data instanceof Comparable)
                 return ((Comparable)data).compareTo(dataCompare);
         } catch (ClassCastException ex)   {
             // Possibly different data types, do a string compare.
         }
         String strThis = data.toString();
         String strField = dataCompare.toString();
         return strThis.compareTo(strField);
     }
     /**
      * Move the physical binary data to this field.
      * (Must be the same physical type... setText makes sure of that)
      * After seting the data, I call the doRecordChange() method of the record if I changed.
      * @param vpData The data to set the field to.
      * @param bDisplayOption Display the data on the screen if true (call displayField()).
      * @param iMoveMode INIT, SCREEN, or READ move mode.
      * @return The error code.
      */
     public int setData(Object vpData, boolean bDisplayOption, int iMoveMode)
     {
         boolean bModified = true;
         if ((m_data == vpData) || ((m_data != null) && (m_data.equals(vpData))))
             bModified = false;
         m_data = vpData;    // Set the data
         if (bDisplayOption)
             this.displayField();    // Display the data
         if (iMoveMode == Constants.SCREEN_MOVE)
         {
             if (bModified)
             {
                 m_bModified = true; // This field has been modified.
                if (m_record != null)   // Never
                    return m_record.doRecordChange(this, iMoveMode, bDisplayOption);        // Tell the record that I changed
             }
         }
         else
             m_bModified = false;    // Init or read clears this flag
         return Constants.NORMAL_RETURN;
     }
     /**
      * Get a pointer to the binary image of this field's data.
      * @return The raw data.
      */
     public Object getData() 
     { // Move raw data from this field
         return this.doGetData();
     }
     /**
      * Get a pointer to the binary image of this field's data.
      * @return The raw data.
      */
     public Object doGetData() 
     { // Move raw data from this field
         return m_data;
     }
     /**
      * Display this field.
      * Go through the sFieldList and setText for JTextComponents and setControlValue for
      * FieldComponents.
      * @see org.jbundle.model.screen.FieldComponent
      */
     public void displayField()                  // init this field override for other value
     {
         if (m_vScreenField == null)
             return;
         for (int i = 0; i < m_vScreenField.size(); i++)
         {
             Object component = m_vScreenField.elementAt(i);
             Convert converter = null;
             if (component instanceof ScreenComponent)
                 converter = ((ScreenComponent)component).getConverter();
             if (converter == null)
                 converter = this;
             if ((this.getFieldName().equals(this.getNameByReflection(component)))
                 || (converter.getField() == this))
             {
                 if (component instanceof FieldComponent)
                     ((FieldComponent)component).setControlValue(converter.getData());
               else if (component.getClass().getName().contains("ext"))  // JTextComponent/JTextArea - TODO FIX This lame code!
                     this.setTextByReflection(component, converter.getString());
 //                else if (component instanceof JTextComponent)
 //                    ((JTextComponent)component).setText(converter.getString());
             }
         }
     }
     /**
      * Lame method, since awt, swing, and android components all have setText methods.
      * @param obj
      * @param text
      */
     public void setTextByReflection(Object obj, String text)
     {
         try {
             java.lang.reflect.Method method = obj.getClass().getMethod("setText", String.class);
             
             if (method != null)
                 method.invoke(obj, text);
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     /**
      * Lame method, since awt, swing, and android components all have setText methods.
      * @param obj
      * @param text
      */
     public String getNameByReflection(Object obj)
     {
         Object name = null;
         try {
             java.lang.reflect.Method method = obj.getClass().getMethod("getName");
             
             if (method != null)
                 name = method.invoke(obj);
         } catch (Exception e) {
             e.printStackTrace();
         }
         if (name == null)
             return null;
         else
             return name.toString();
     }
     /**
      * Return the end field in the converter chain (This is the end!).
      * @return This field.
      */
     public FieldInfo getField()
     {
         return this;    // A converter is not a field!
     }   // Return the source BaseField (if linked to a field!)
     /**
      * Get the short field description.
      * @return The field desc. If null, return the resource from the record's resouce file.
      * If not available, return the field name.
      */
     public String getFieldDesc()
     {
         String strFieldDesc = null;
         if (m_strFieldDesc != null)    // Field desc null? (using default)
             strFieldDesc = m_strFieldDesc;
         else
         { // Get the description from the resource file
             if (this.getRecord() != null)
                 strFieldDesc = this.getRecord().getString(this.getFieldName());
         }
         return strFieldDesc;
     }
     /**
      * Set the field description.
      * @param strFieldDesc The field desc.
      */
     public void setFieldDesc(String strFieldDesc)
     {
         m_strFieldDesc = strFieldDesc;
     }
     /**
      * Get the long field description tip.
      * Look in the resource for the long description, if not found, return the field desc.
      * @return The field tip.
      */
     public String getFieldTip()
     {
         String strTipKey = this.getFieldName() + Constants.TIP;
         String strFieldTip = strTipKey;
         if (this.getRecord() != null)
             strFieldTip = this.getRecord().getString(strTipKey);
         if (strFieldTip == strTipKey)
             return this.getFieldDesc();
         return strFieldTip;
     }
     /**
      * Get this field's Record.
      * @return The record.
      */
     public FieldList getRecord()
     {
         return m_record;
     }
     /**
      * Get this field's name.
      * @param bAddQuotes All quotes to the name.
      * @param bInclude the FileName (ie., filename.fieldname).
      * @return The field name.
      */
     public String getFieldName(boolean bAddQuotes, boolean bIncludeFileName)
     {
         return m_strFieldName;
     }
     /**
      * Current string length.
      * @return The length (0 if null, -1 if unknown);
      */
     public int getLength()
     {
         if (m_data == null)
             return 0;
         return -1;  // Unknown (non zero)
     }
     /**
      * Maximum string length.
      * @return The max field length.
      */
     public int getMaxLength()
     {
         if (m_iMaxLength == Constants.DEFAULT_FIELD_LENGTH)
         {
             m_iMaxLength = 20;
             if (m_classData == Short.class)
                 m_iMaxLength = 4;
             else if (m_classData == Integer.class)
                 m_iMaxLength = 8;
             else if (m_classData == Float.class)
                 m_iMaxLength = 8;
             else if (m_classData == Double.class)
                 m_iMaxLength = 15;
             else if (m_classData == java.util.Date.class)
                 m_iMaxLength = 15;
             else if (m_classData == String.class)
                 m_iMaxLength = 30;
             else if (m_classData == Boolean.class)
                 m_iMaxLength = 10;
         }
         return m_iMaxLength;
     }
     /**
      * Set this field back to the original value.
      * @param bDisplayOption If true, display the data.
      * @return The error code.
      */
     public int initField(boolean bDisplayOption)                    // Init this field override for other value
     {
         if ((this.getDefault() == null) || (this.getDefault() instanceof String))
             return this.setString((String)this.getDefault(), bDisplayOption, Constants.INIT_MOVE);   // zero out the field
         return this.setData(this.getDefault(), bDisplayOption, Constants.INIT_MOVE);
     }
     /**
      * Is this field null?
      * @return true if the data is null or the current length is 0.
      */
     public boolean isNull()
     {
         if (this.getData() == null)
             return true;
         return (this.getLength() == 0);
     }
     /**
      * Set this field's name.
      * @param sString The field name.
      */
     public void setFieldName(String sString)
     {
         m_strFieldName = sString;
     }
     /**
      * Set this field's Record.
      * WARNING - You must add this field to the record manually.
      * @param record The record to set.
      */
     public void setRecord(Rec record)
     {
         m_record = (FieldList)record;
     }
     /**
      * Convert and move string to this field.
      * Override this method to convert the String to the actual Physical Data Type.
      * @param bState the state to set the data to.
      * @param bDisplayOption Display the data on the screen if true.
      * @param iMoveMode INIT, SCREEN, or READ move mode.
      * @return The error code (or NORMAL_RETURN).
      */
     public int setString(String strString, boolean bDisplayOption, int iMoveMode) // init this field override for other value
     {
         try {
             Object objData = Converter.convertObjectToDatatype(strString, this.getDataClass(), null, m_ibScale);
             if (objData == null)
                 if (this.getDataClass() != Boolean.class)
                     if (!(Number.class.isAssignableFrom(this.getDataClass())))
                         if (this.getDataClass() != java.util.Date.class)
                             objData = Constants.BLANK;    // To set a null internally, you must call setData directly
             return this.setData(objData, bDisplayOption, iMoveMode);
         } catch (Exception ex)  {
             String strError = ex.getMessage();
             if (strError == null)
                 strError = ex.getClass().getName();
             if (this.getRecord() != null)
                 if (this.getRecord().getTask() != null)
                     return this.getRecord().getTask().setLastError(strError);
             return Constants.ERROR_RETURN;
         }
     }
     /**
      * Retrieve (in string format) from this field.
      * @return This field converted to a string.
      */
     public String getString()
     {
         try {
             return Converter.formatObjectToString(this.getData(), this.getDataClass(), this.getDefault());
         } catch (Exception ex) {
             return null;
         }
     }
     /**
      * Get the class of the m_data field.
      * @return the java class of the raw data.
      */
     public Class<?> getDataClass()
     {
         return m_classData;
     }
     /**
      * Set the class of the m_data field.
      * @param classData the java class of the raw data.
      */
     public void setDataClass(Class<?> classData)
     {
         m_classData = classData;
         this.initField(false);
     }
     /**
      * Is this field active?
      * Used primarily for sql selects.
      * @return true if selected.
      */
     public boolean isSelected()
     {
         return true;
     }
     /**
      * Is this a virtual field?
      * A virtual field does not actually carry data, it simply reflects another field (usually in a different format).
      * @return true if virtual.
      */
     public boolean isVirtual()
     {
         return false;
     }
     /**
      * For numeric fields, set the current value.
      * Override this method to convert the value to the actual Physical Data Type.
      * @param bState the state to set the data to.
      * @param bDisplayOption Display the data on the screen if true.
      * @param iMoveMode INIT, SCREEN, or READ move mode.
      * @return The error code.
      */
     public int setValue(double dValue, boolean bDisplayOption, int iMoveMode)
     {
         Class<?> classData = this.getDataClass();
         if (classData != Object.class)
         {
             Object objData = null;
             if (classData == Short.class)
                 objData = new Short((short)dValue);
             else if (classData == Integer.class)
                 objData = new Integer((int)dValue);
             else if (classData == Float.class)
                 objData = new Float((float)dValue);
             else if (classData == Double.class)
                 objData = new Double(dValue);
             else if (classData == java.util.Date.class)
                 objData = new Date((long)dValue);
             else if (classData == Boolean.class)
                 objData = new Boolean(dValue == 0 ? false : true);
             else if (classData == String.class)
                 objData = new Double(dValue).toString();
             return this.setData(objData, bDisplayOption, iMoveMode);
         }
         return super.setValue(dValue, bDisplayOption, iMoveMode);
     }
     /**
      * For numeric fields, get the current value.
      * Usually overidden.
      * @return the field's value.
      */
     public double getValue()
     {
         Object objData = this.getData();
         if (objData != null)
         {
             Class<?> classData = this.getDataClass();
             if (classData != Object.class)
                 if (classData == objData.getClass())
             {
                 if (classData == Short.class)
                     return ((Short)objData).shortValue();
                 else if (classData == Integer.class)
                     return ((Integer)objData).intValue();
                 else if (classData == Float.class)
                     return ((Float)objData).floatValue();
                 else if (classData == Double.class)
                     return ((Double)objData).doubleValue();
                 else if (classData == java.util.Date.class)
                     return ((Date)objData).getTime();
                 else if (classData == Boolean.class)
                     return (((Boolean)objData).booleanValue() ? 1 : 0);
                 else if (classData != String.class)
                 {
                     try   {
                         return this.stringToDouble((String)objData).doubleValue();
                     } catch (Exception ex)  { // Ignore the exception
                     }
                 }
                 }
         }
         return super.getValue();    // Return a 0
     }
     /**
      * Set the data in this field to true or false.
      */
     public int setState(boolean bState, boolean bDisplayOption, int iMoveMode)
     {
         Boolean objData = new Boolean(bState);
         if (this.getDataClass() == Boolean.class)
             return this.setData(objData, bDisplayOption, iMoveMode);
         else if (Number.class.isAssignableFrom(this.getDataClass()))
             return this.setValue(objData.booleanValue() ? 1 : 0, bDisplayOption, iMoveMode);
         else if (this.getDataClass() == String.class)
             return this.setString(objData.booleanValue() ? Constants.TRUE : Constants.FALSE, bDisplayOption, iMoveMode);
         return super.setState(bState, bDisplayOption, iMoveMode);
     }
     /**
      * Get the state of this boolean field.
      * @return true if true, false if not boolean or null.
      */
     public boolean getState()               // init this field override for other value
     {
         if (this.getDataClass() == Boolean.class)
         {
             Boolean objData = (Boolean)this.getData();
             if (objData != null)
                 return objData.booleanValue();
         }
         return false;
     }
     /**
      * Set the default value.
      * @param objDefault The value to set (must be in string or native format).
      */
     public void setDefault(Object objDefault)
     {
         m_objDefault = objDefault;
     }
     /**
      * Get the default value.
      * @return The default value.
      */
     public Object getDefault()
     {
         return m_objDefault;
     }
     /**
      * Get the number of fractional digits.
      */
     public void setScale(int iScale)
     {
         m_ibScale = iScale;
     }
     /**
      * Does this field prefer to be hidden on displays?
      */
     public boolean isHidden()
     {
     	return m_bHidden;
     }
     /**
      * Set whether this field prefers to be hidden on displays.
      */
     public void setHidden(Boolean bHidden)
     {
     	m_bHidden = bHidden;
     }
     /**
      * Get the dirty flag.
      * @return True is this field has been changes since the last init.
      */
     public boolean isModified()
     {
         return m_bModified;
     }
     /**
      * Set the dirty flag.
      * @param flag If true, this field is intrepreted as modified.
      */
     public void setModified(boolean flag)
     {
         m_bModified = flag;
     }
     /**
      * Convert this string to a Float.
      * @param strString string to convert.
      * @return Float value.
      * @throws Exception NumberFormatException.
      */
     public Float stringToFloat(String strString) throws Exception
     {
         return FieldInfo.stringToFloat(strString, m_ibScale);
     }
     /**
      * Convert this string to a Double.
      * @param strString string to convert.
      * @return Double value.
      * @throws Exception NumberFormatException.
      */
     public Double stringToDouble(String strString) throws Exception
     {
         return FieldInfo.stringToDouble(strString, m_ibScale);
     }
     /**
      * Convert this string to a Date.
      * Runs sequentually through the following formats: DateTime, DateShortTime,
      * Date, DateShort, Time.
      * @param strString string to convert.
      * @return Date value.
      * @throws Exception NumberFormatException.
      */
     public Date stringToDate(String strString) throws Exception
     {
         return FieldInfo.stringToDate(strString, m_ibScale);
     }
 }
