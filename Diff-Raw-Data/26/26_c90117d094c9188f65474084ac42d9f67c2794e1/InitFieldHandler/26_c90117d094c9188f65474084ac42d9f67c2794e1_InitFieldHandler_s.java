 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.base.field.event;
 
 /**
  * @(#)InitFieldHandler.java    0.00 12-Feb-97 Don Corley
  *
  * Copyright © 2012 tourapp.com. All Rights Reserved.
  *      don@tourgeek.com
  */
 
 import org.jbundle.base.db.event.FileRemoveBOnCloseHandler;
 import org.jbundle.base.field.BaseField;
 import org.jbundle.base.field.ListenerOwner;
 import org.jbundle.base.field.ReferenceField;
 import org.jbundle.base.model.DBConstants;
 
 /**
  * When this field is initialized, move the source value to this field.
  *
  * @version 1.0.0
  * @author    Don Corley
  */
 public class InitFieldHandler extends FieldListener
 {
     /**
      * The source string to initialize the field owner to.
      */
     protected Object m_objSource = null;
     /**
      * The source field to initialize the field owner to.
      */
     protected BaseField m_fldSource = null;
     /**
      * The source field to initialize the field owner to.
      */
     protected boolean m_bInitIfSourceNull = true;
     /**
      * Init only if the dest is null.
      */
     protected boolean m_bOnlyInitIfDestNull = false;
 
     /**
      * Constructor.
      */
     public InitFieldHandler()
     {
         super();
     }
     /**
      * Constructor.
      * @param objSource The source string to initialize the field owner to.
      */
     public InitFieldHandler(Object objSource)
     {
         this();
         this.init(null, null, objSource, true, false);
     }
     /**
      * Constructor.
      * @param sourceField The source field to initialize the field owner to.
      */
    public InitFieldHandler(BaseField sourceField)
     {
         this();
         this.init(null, sourceField, null, true, false);
     }
     /**
      * Constructor.
      * @param sourceField The source field to initialize the field owner to.
      */
     public InitFieldHandler(BaseField sourceField, boolean bInitIfSourceNull)
     {
         this();
         this.init(null, sourceField, null, bInitIfSourceNull, false);
     }
     /**
      * Constructor.
      * @param sourceField The source field to initialize the field owner to.
      */
     public InitFieldHandler(BaseField sourceField, boolean bInitIfSourceNull, boolean bOnlyInitIfDestNull)
     {
         this();
         this.init(null, sourceField, null, bInitIfSourceNull, bOnlyInitIfDestNull);
     }
     /**
      * Constructor.
      * Only responds to init moves.
      * @param field The basefield owner of this listener (usually null and set on setOwner()).
      * @param sourceField The source field to initialize the field owner to.
      * @param sourceString The source string to initialize the field owner to.
      */
    public void init(BaseField field, BaseField sourceField, Object sourceString, boolean bInitIfSourceNull, boolean bOnlyInitIfDestNull)
     {
         super.init(field);
         m_objSource = sourceString;
        m_fldSource = sourceField;
         m_bInitIfSourceNull = bInitIfSourceNull;
         m_bOnlyInitIfDestNull = bOnlyInitIfDestNull;
         
         m_bScreenMove = false;
         m_bInitMove = true;     // Only respond to init
         m_bReadMove = false;
     }
     /**
      * Set this cloned listener to the same state at this listener.
      * @param field The field this new listener will be added to.
      * @param The new listener to sync to this.
      * @param Has the init method been called?
      * @return True if I called init.
      */
     public boolean syncClonedListener(BaseField field, FieldListener listener, boolean bInitCalled)
     {
         if (!bInitCalled)
         {
             BaseField fldSource = this.getSyncedListenersField(m_fldSource, listener);
             ((InitFieldHandler)listener).init(null, fldSource, m_objSource, m_bInitIfSourceNull, m_bOnlyInitIfDestNull);
         }
         return super.syncClonedListener(field, listener, true);
     }
     /**
      * Set the field that owns this listener.
      * @owner The field that this listener is being added to (if null, this listener is being removed).
      */
     public void setOwner(ListenerOwner owner)
     {
         super.setOwner(owner);
         if (this.getOwner() != null)
         {
             if (m_fldSource != null) if (m_fldSource.getRecord() != this.getOwner().getRecord())
                 m_fldSource.getRecord().addListener(new FileRemoveBOnCloseHandler(this));     // Not same file, if target file closes, remove this listener!
             InitOnceFieldHandler behavior = (InitOnceFieldHandler)this.getOwner().getListener(InitOnceFieldHandler.class.getName());
             if (this.getOwner().getRecord().getEditMode() == DBConstants.EDIT_CURRENT)
                 behavior = null;    // Special case - if there is a current record, make sure initonce works.
             if (behavior != null)
                 behavior.setFirstTime(true);  // If this listener exists it doesn't init initially everything up, so disable for a sec.
             this.syncBehaviorToRecord(null);    // Init now
             if (this.getOwner() instanceof ReferenceField)
                 if (this.getOwner().getListener(ReadSecondaryHandler.class.getName()) != null)
                 {
                     if (behavior != null)
                         behavior.setEnabledListener(false);  // If this listener exists it doesn't init initially everything up, so disable for a sec.
                     ((FieldListener)this.getOwner().getListener(ReadSecondaryHandler.class.getName())).syncBehaviorToRecord(null);
                 }
         }
     }
     /**
      * The Field has Changed.
      * Move the source field or string to this listener's owner.
      * @param bDisplayOption If true, display the change.
      * @param iMoveMode The type of move being done (init/read/screen).
      * @return The error code (or NORMAL_RETURN if okay).
      * Field Changed, init the field.
      */
     public int fieldChanged(boolean bDisplayOption, int iMoveMode)
     {
         if (m_fldSource != null)
         {
             if (((m_bInitIfSourceNull == true) || (!m_fldSource.isNull()))
                     && ((m_bOnlyInitIfDestNull == false)
                         || (m_bOnlyInitIfDestNull == true) && (this.getOwner().isNull())))
             {
                 boolean bModified = this.getOwner().isModified();
                 int iErrorCode = this.getOwner().moveFieldToThis(m_fldSource, bDisplayOption, iMoveMode);
                 this.getOwner().setModified(bModified);
                 return iErrorCode;
             }
             else
                 return super.fieldChanged(bDisplayOption, iMoveMode);
         }
         else
         {
             if (m_objSource instanceof String)
                 return this.getOwner().setString(m_objSource.toString(), bDisplayOption, iMoveMode);
             else
                 return this.getOwner().setData(m_objSource, bDisplayOption, iMoveMode);
         }
     }
 }
