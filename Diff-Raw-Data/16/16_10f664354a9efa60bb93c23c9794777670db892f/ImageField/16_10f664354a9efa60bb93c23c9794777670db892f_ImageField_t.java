 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.base.field;
 
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jbundle.base.db.Record;
 import org.jbundle.base.model.DBConstants;
 import org.jbundle.base.model.ScreenConstants;
 import org.jbundle.base.model.ScreenModel;
 import org.jbundle.model.Task;
 import org.jbundle.model.db.Convert;
 import org.jbundle.model.screen.ComponentParent;
 import org.jbundle.model.screen.ScreenComponent;
 import org.jbundle.model.screen.ScreenLoc;
 import org.jbundle.thin.base.db.Constants;
 import org.jbundle.thin.base.screen.BaseApplet;
 import org.jbundle.thin.base.screen.util.SerializableImage;
 
 
 /**
  * ImageField - A GIF Picture.
  *
  * @version 1.0.0
  * @author    Don Corley
  */
 public class ImageField extends ObjectField
 {
 	private static final long serialVersionUID = 1L;
 
     /**
      * Constructor.
      */
     public ImageField()
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
     public ImageField(Record record, String strName, int iDataLength, String strDesc, Object strDefault)
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
     }
     /**
      * Creates a new object of the same class as this object.
      * @return     a clone of this instance.
      * @exception  CloneNotSupportedException  if the object's class does not support the <code>Cloneable</code> interface.
      * @see        java.lang.Cloneable
      */
     public Object clone() throws CloneNotSupportedException
     {
         BaseField field = new ImageField(null, m_strFieldName, m_iMaxLength, m_strFieldDesc, null);
         field.setRecord(m_record);     // Set table without adding to table field list
         return field;
     }
     /**
      * Set up the default screen control for this field.
      * @param itsLocation Location of this component on screen (ie., GridBagConstraint).
      * @param targetScreen Where to place this component (ie., Parent screen or GridBagLayout).
      * @param converter The converter to set the screenfield to.
      * @param iDisplayFieldDesc Display the label? (optional).
      * @return Return the component or ScreenField that is created for this field.
      */
     public ScreenComponent setupDefaultView(ScreenLoc itsLocation, ComponentParent targetScreen, Convert converter, int iDisplayFieldDesc, Map<String, Object> properties)
     {
         ScreenComponent screenField = createScreenComponent(ScreenModel.IMAGE_VIEW, itsLocation, targetScreen, converter, iDisplayFieldDesc, properties);
         properties = new HashMap<String,Object>();
         properties.put(ScreenModel.FIELD, this);
         properties.put(ScreenModel.COMMAND, ScreenModel.OPEN);
         properties.put(ScreenModel.IMAGE, ScreenModel.OPEN);
         ScreenComponent pSScreenField = createScreenComponent(ScreenModel.CANNED_BOX, targetScreen.getNextLocation(ScreenConstants.RIGHT_OF_LAST, ScreenConstants.DONT_SET_ANCHOR), targetScreen, converter, iDisplayFieldDesc, properties);
         pSScreenField.setRequestFocusEnabled(false);
         return screenField;
     }
     /**
      * Get this image as an icon.
      */
     public SerializableImage getImage()
     {
         if (this.isNull())
             return null;
         /*if (this.getData() instanceof ImageIcon)
             return (ImageIcon)this.getData();   // Never (legacy)
         else*/ if (this.getData() instanceof SerializableImage)
             return (SerializableImage)this.getData();
         else
             return null;
     }
     /**
      * Move the physical binary data to this field.
      * (Must be the same physical type... setText makes sure of that)
      * This is a little tricky. First, I call the behaviors (doSetData)
      * which actually moves the data. Then, I call the HandleFieldChange
      * listener for each field, except on a read move, where the HandleFieldChange
      * listener is called in the doValidRecord method because each field comes
      * in one at a time, and if a listener modifies or accesses
      * another field, the field may not have been moved from the db yet.
      * @param data The raw data to move to this field.
      * @param iDisplayOption If true, display the new field.
      * @param iMoveMove The move mode.
      * @return An error code (NORMAL_RETURN for success).
      */
     public int setData(Object data, boolean bDisplayOption, int iMoveMode)
     {
         if (data == Constants.BLANK)
             data = null;
         //if (data instanceof ImageIcon)
         //    data = new SerializableImage(((ImageIcon)data).getImage());
        if ((data != null) && (!(data instanceof SerializableImage)) && (!(data instanceof String)))
         {
             String strError = "Error - Incorrect image format, must be a SerializableImage";
             System.out.println(strError);   // TODO - Get rid of this
             Task task = null;
             if (this.getRecord() != null)
                 if (this.getRecord().getRecordOwner() != null)
                     task = this.getRecord().getRecordOwner().getTask();
             if (task == null)
                 task = BaseApplet.getSharedInstance();
             if (task == null)
                 return DBConstants.ERROR_RETURN;
             return task.setLastError(strError);
         }
         return super.setData(data, bDisplayOption, iMoveMode);
     }
    /**
     * Move this physical binary data to this field.
     * @param data The physical data to move to this field (must be the correct raw data class).
     * @param bDisplayOption If true, display after setting the data.
     * @param iMoveMode The type of move.
     * @return an error code (0 if success).
     */
    public int doSetData(Object data, boolean bDisplayOption, int iMoveMode)
    {
        if (data instanceof String)
            return DBConstants.NORMAL_RETURN;   // Special case, button pressed
        return super.doSetData(data, bDisplayOption, iMoveMode);
    }
 } 
