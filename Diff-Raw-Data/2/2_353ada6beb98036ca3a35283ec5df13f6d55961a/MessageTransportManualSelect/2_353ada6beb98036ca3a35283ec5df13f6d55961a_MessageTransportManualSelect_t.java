 /**
  * @(#)MessageTransportManualSelect.
  * Copyright © 2013 jbundle.org. All rights reserved.
  * GPL3 Open Source Software License.
  */
 package org.jbundle.main.msg.db;
 
 import java.util.*;
 
 import org.jbundle.base.db.*;
 import org.jbundle.thin.base.util.*;
 import org.jbundle.thin.base.db.*;
 import org.jbundle.base.db.event.*;
 import org.jbundle.base.db.filter.*;
 import org.jbundle.base.field.*;
 import org.jbundle.base.field.convert.*;
 import org.jbundle.base.field.event.*;
 import org.jbundle.base.model.*;
 import org.jbundle.base.util.*;
 import org.jbundle.model.*;
 import org.jbundle.model.db.*;
 import org.jbundle.model.screen.*;
 
 /**
  *  MessageTransportManualSelect - Only the manual messages.
  */
 public class MessageTransportManualSelect extends MessageTransportSelect
 {
     /**
      * Default constructor.
      */
     public MessageTransportManualSelect()
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
     public MessageTransportManualSelect(Record record, String strName, int iDataLength, String strDesc, Object strDefault)
     {
         this();
         this.init(record, strName, iDataLength, strDesc, strDefault);
     }
     /**
      * Initialize class fields.
      */
     public void init(Record record, String strName, int iDataLength, String strDesc, Object strDefault)
     {
         super.init(record, strName, iDataLength, strDesc, strDefault);
     }
     /**
      * Get (or make) the current record for this reference.
      */
     public Record makeReferenceRecord(RecordOwner recordOwner)
     {
         Record record = super.makeReferenceRecord(recordOwner);
        record.addListener(new CompareFileFilter(record.getField(MessageTransport.MESSAGE_TRANSPORT_TYPE), MessageTransportTypeField.MANUAL_RESPONSE, DBConstants.EQUALS, null, false));
         return record;
     }
 
 }
