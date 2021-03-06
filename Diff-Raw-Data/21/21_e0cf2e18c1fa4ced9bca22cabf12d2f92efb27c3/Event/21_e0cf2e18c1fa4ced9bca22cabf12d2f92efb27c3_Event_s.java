 package org.lwes;
 
 import org.lwes.db.EventTemplateDB;
 import org.lwes.serializer.Deserializer;
 import org.lwes.serializer.DeserializerState;
 import org.lwes.serializer.Serializer;
 import org.lwes.util.CharacterEncoding;
 import org.lwes.util.IPAddress;
 import org.lwes.util.Log;
 import org.lwes.util.NumberCodec;
 
 import java.math.BigInteger;
 import java.net.InetAddress;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.concurrent.ConcurrentHashMap;
 
 public class Event {
 
     public static final int MAX_MESSAGE_SIZE = 65507;
 
     /**
      * Reserved metadata keywords
      */
     public static final String ENCODING = "enc";
     public static final String RECEIPT_TIME = "ReceiptTime";
     public static final String SENDER_IP = "SenderIP";
     public static final String SENDER_PORT = "SenderPort";
 
     /**
      * Encoding variables
      */
     public static final short ISO_8859_1 = 0;
     public static final short UTF_8 = 1;
     public static final short DEFAULT_ENCODING = UTF_8;
     public static final CharacterEncoding[] ENCODING_STRINGS = {
             CharacterEncoding.ISO_8859_1, CharacterEncoding.UTF_8};
 
     /**
      * Event data
      */
     private ConcurrentHashMap<String, BaseType> attributes = new ConcurrentHashMap<String, BaseType>();
     private String name = null;
     private EventTemplateDB eventTemplateDB = null;
     private short encoding = DEFAULT_ENCODING;
 
     /**
      * If this is set to true, types and attributes are validated against the EventTemplateDB
      */
     private boolean validating = true;
 
     /**
      * Internal object for deserialization state
      */
     private DeserializerState state = null;
 
     /**
      * the size of the event in bytes
      */
     private int bytesStoreSize = 0;
 
     /**
      * Create an event called <tt>eventName</tt>
      *
      * @param eventName       the name of the event
      * @param eventTemplateDB the EventTemplateDB to use for validation
      * @throws NoSuchEventException         if the Event does not exist in the EventTemplateDB
      * @throws NoSuchAttributeException     if an attribute does not exist in the EventTemplateDB
      * @throws NoSuchAttributeTypeException if an attribute type does not exist in the EventTemplateDB
      */
     public Event(String eventName, EventTemplateDB eventTemplateDB)
             throws EventSystemException {
         this(eventName, true, eventTemplateDB);
     }
 
     /**
      * Create an event called <tt>eventName</tt>
      *
      * @param eventName       the name of the event
      * @param validate        true if the EventTemplateDB should be checked for types before all mutations
      * @param eventTemplateDB the EventTemplateDB to use for validation
      * @throws NoSuchEventException         if the Event does not exist in the EventTemplateDB
      * @throws NoSuchAttributeException     if an attribute does not exist in the EventTemplateDB
      * @throws NoSuchAttributeTypeException if an attribute type does not exist in the EventTemplateDB
      */
     public Event(String eventName, boolean validate, EventTemplateDB eventTemplateDB)
             throws EventSystemException {
         this(eventName, validate, eventTemplateDB, DEFAULT_ENCODING);
     }
 
     /**
      * Create an event called <tt>eventName</tt>
      *
      * @param eventName the name of the event
      * @param validate  true if the EventTemplateDB should be checked for types before all mutations
      * @param encoding  the character encoding used by the event
      * @throws NoSuchEventException         if the Event does not exist in the EventTemplateDB
      * @throws NoSuchAttributeException     if an attribute does not exist in the EventTemplateDB
      * @throws NoSuchAttributeTypeException if an attribute type does not exist in the EventTemplateDB
      */
     public Event(String eventName, boolean validate, EventTemplateDB eventTemplateDB, short encoding)
             throws EventSystemException {
         setEventTemplateDB(eventTemplateDB);
         validating = validate;
         setEventName(eventName);
         setEncoding(encoding);
     }
 
     /**
      * Creates an event by deserializing a raw byte array.
      *
      * @param bytes           the raw bytes to convert
      * @param eventTemplateDB the EventTemplateDB to use to validate the event
      * @throws NoSuchEventException
      * @throws NoSuchAttributeException
      * @throws NoSuchAttributeTypeException
      */
     public Event(byte[] bytes, EventTemplateDB eventTemplateDB)
             throws EventSystemException {
         this(bytes, true, eventTemplateDB);
     }
 
 
     /**
      * Creates an event by deserializing a raw byte array.
      *
      * @param bytes           the raw bytes to convert
      * @param validate        whether or not to validate the event
      * @param eventTemplateDB the EventTemplateDB to use to validate the event
      * @throws NoSuchEventException
      * @throws NoSuchAttributeException
      * @throws NoSuchAttributeTypeException
      */
     public Event(byte[] bytes, boolean validate, EventTemplateDB eventTemplateDB)
             throws EventSystemException {
         setEventTemplateDB(eventTemplateDB);
         validating = validate;
         deserialize(bytes);
     }
 
     /**
      * Returns an enumeration of all the event attribute names
      *
      * @return an enumeration of attribute strings
      */
     public Enumeration<String> getEventAttributeNames() {
         if (attributes == null) {
             return null;
         }
 
         return attributes.keys();
     }
 
     /**
      * Returns the number of attributes in the event
      *
      * @return number of attributes in the event
      */
     public int size() {
         if (attributes == null) {
             return 0;
         }
         return attributes.size();
     }
 
     /**
      * Returns true if the event validates against the EventTemplateDB before making changes
      *
      * @return the validating state
      */
     public boolean isValidating() {
         return this.validating;
     }
 
     /**
      * Set to true if the event should validate against the EventTemplateDB before making changes
      *
      * @param validate the validating value
      */
     public void setValidating(boolean validate) {
         this.validating = validate;
     }
 
     /**
      * Returns the EventTemplateDB for this event, used for validation of types and attributes.
      *
      * @return the EventTemplateDB
      */
     public EventTemplateDB getEventTemplateDB() {
         return this.eventTemplateDB;
     }
 
     /**
      * Sets the EventTemplateDB for this event, used for validation of types and attributes.
      *
      * @param eventTemplateDB the EventTemplateDB to be used for validation
      */
     public void setEventTemplateDB(EventTemplateDB eventTemplateDB) {
         this.eventTemplateDB = eventTemplateDB;
     }
 
     /**
      * Returns the name of the event
      *
      * @return the name of the event
      */
     public synchronized String getEventName() {
         return this.name;
     }
 
     /**
      * Sets the name of the Event
      *
      * @param name the name of the event
      * @throws NoSuchEventException if the event is validating and does not exist in the EventTemplateDB
      */
     public synchronized void setEventName(String name) throws NoSuchEventException {
         if (isValidating() && getEventTemplateDB() != null) {
             if (!getEventTemplateDB().checkForEvent(name)) {
                 throw new NoSuchEventException("Event " + name + " does not exist in event definition");
             }
         }
 
         /* determine if we already have the name and are just resetting it */
         if (this.name != null) {
             bytesStoreSize -= (this.name.length() + 1 + 2);
         }
 
         bytesStoreSize += (name.length() + 1 + 2);
 
         this.name = name;
     }
 
     /**
      * Get the character encoding for this event
      *
      * @return the encoding
      */
     public short getEncoding() {
         return this.encoding;
     }
 
     /**
      * Set the character encoding for event strings
      *
      * @param encoding the character encoding
      * @throws NoSuchAttributeTypeException if the type for the encoding attribute does not exist
      * @throws NoSuchAttributeException     if the encoding attribute does not exist
      */
     public void setEncoding(short encoding) throws EventSystemException {
         this.encoding = encoding;
         setInt16(ENCODING, this.encoding);
     }
 
     /**
      * Generic accessor, checks if an attribute exists and returns its value.  The user must do their
      * own type checking.
      *
      * @param attributeName name of the attribute to lookup
      * @return the object poitned to by attributeName
      * @throws NoSuchAttributeException if the attribute does not exist in this event
      */
     public Object get(String attributeName) throws NoSuchAttributeException {
         if (attributes == null) {
             return null;
         }
 
         if (attributes.containsKey(attributeName)) {
             return attributes.get(attributeName).getTypeObject();
         }
 
         if (isValidating() && getEventTemplateDB() != null) {
             if (getEventTemplateDB().checkForAttribute(name, attributeName)) {
                 return null;
             }
             else {
                 throw new NoSuchAttributeException("Attribute " + attributeName + " does not exist for event " + name);
             }
         }
 
         return null;
     }
 
     /**
      * Method to check if an attribute is set in the event. This method does not throw
      * NoSuchAttributeException because it shouldn't really care. If it's not there, it's
      * not there.
      *
      * @param attributeName The attribute name to check for existance.
      * @return true if there is a value, false if not.
      */
     public boolean isSet(String attributeName) {
         try {
             return (get(attributeName) != null);
         }
         catch (NoSuchAttributeException e) {
             return false;
         }
     }
 
     /**
      * Accessor that returns a boolean value for attribute <tt>attributeName</tt>
      *
      * @param attributeName the name of the attribute to fetch
      * @return the boolean value
      * @throws NoSuchAttributeException if the attribute does not exist in this event
      */
     public Boolean getBoolean(String attributeName) throws NoSuchAttributeException {
         return (Boolean) get(attributeName);
     }
 
     /**
      * Accessor that returns an <tt>unsigned short</tt>, in the guise of an <tt>int</tt>, for attribute <tt>attributeName</tt>
      *
      * @param attributeName the name of the attribute to fetch
      * @return the unsigned short as an int
      * @throws NoSuchAttributeException if the attribute does not exist in this event
      */
     public Integer getUInt16(String attributeName) throws NoSuchAttributeException {
         return (Integer) get(attributeName);
     }
 
     /**
      * Accessor that returns an <tt>short</tt>, for attribute <tt>attributeName</tt>
      *
      * @param attributeName the name of the attribute to fetch
      * @return the short value
      * @throws NoSuchAttributeException if the attribute does not exist in this event
      */
     public Short getInt16(String attributeName) throws NoSuchAttributeException {
         return (Short) get(attributeName);
     }
 
     /**
      * Accessor that returns an <tt>unsigned int</tt>, in the guise of an <tt>long</tt>, for attribute <tt>attributeName</tt>
      *
      * @param attributeName the name of the attribute to fetch
      * @return the unsigned int as a long
      * @throws NoSuchAttributeException if the attribute does not exist in this event
      */
     public Long getUInt32(String attributeName) throws NoSuchAttributeException {
         return (Long) get(attributeName);
     }
 
     /**
      * Accessor that returns an <tt>int</tt>, for attribute <tt>attributeName</tt>
      *
      * @param attributeName the name of the attribute to fetch
      * @return the int value
      * @throws NoSuchAttributeException if the attribute does not exist in this event
      */
     public Integer getInt32(String attributeName) throws NoSuchAttributeException {
         return (Integer) get(attributeName);
     }
 
     /**
      * Accessor that returns an <tt>unsigned long</tt>, in the guise of an <tt>BigInteger</tt>, for attribute <tt>attributeName</tt>
      *
      * @param attributeName the name of the attribute to fetch
      * @return the unsigned long as a BigInteger
      * @throws NoSuchAttributeException if the attribute does not exist in this event
      */
     public BigInteger getUInt64(String attributeName) throws NoSuchAttributeException {
         return (BigInteger) get(attributeName);
     }
 
 
     /**
      * Accessor that returns an <tt>long</tt>, for attribute <tt>attributeName</tt>
      *
      * @param attributeName the name of the attribute to fetch
      * @return the long value
      * @throws NoSuchAttributeException if the attribute does not exist in this event
      */
     public Long getInt64(String attributeName) throws NoSuchAttributeException {
         return (Long) get(attributeName);
     }
 
     /**
      * Accessor that returns an <tt>String</tt>, for attribute <tt>attributeName</tt>
      *
      * @param attributeName the name of the attribute to fetch
      * @return the String value
      * @throws NoSuchAttributeException if the attribute does not exist in this event
      */
     public String getString(String attributeName) throws NoSuchAttributeException {
         return (String) get(attributeName);
     }
 
     /**
      * Accessor that returns an <tt>InetAddress</tt>, for attribute <tt>attributeName</tt>
      *
      * @param attributeName the name of the attribute to fetch
      * @return the InetAddress value
      * @throws NoSuchAttributeException if the attribute does not exist in this event
      */
     public InetAddress getInetAddress(String attributeName) throws NoSuchAttributeException {
        return (InetAddress) get(attributeName);
     }
 
     /**
      * Accessor that returns an IP address in bytes, for attribute <tt>attributeName</tt>
      *
      * @param attributeName the name of the attribute to fetch
      * @return the IP address in bytes
      * @throws NoSuchAttributeException if the attribute does not exist in this event
      */
     public byte[] getIPAddress(String attributeName) throws NoSuchAttributeException {
         return (byte[]) get(attributeName);
     }
 
 
     /**
      * Set the object's attribute <tt>attributeName</tt> with the Object given
      *
      * @param attributeName  the name of the attribute to set
      * @param attributeValue the object to set the attribute with
      * @throws NoSuchAttributeException     if the attribute does not exist in this event
      * @throws NoSuchAttributeTypeException if there is an attribute with an undefined type
      */
     public void set(String attributeName, Object attributeValue)
             throws EventSystemException {
         if (isValidating() && getEventTemplateDB() != null) {
             if (getEventTemplateDB().checkForAttribute(getEventName(), attributeName)) {
                 BaseType bt = getEventTemplateDB().getBaseTypeForObjectAttribute(getEventName(),
                                                                                  attributeName, attributeValue);
                 set(attributeName, bt);
             }
         }
         else {
             throw new NoSuchAttributeException("Must be able to check the EventTemplateDB to use set(String,Object)");
         }
     }
 
     /**
      * Private method to set a BaseType
      *
      * @param attribute the name of the attribute to set
      * @param anObject  the BaseType to set in the event
      * @throws NoSuchAttributeException     if the attribute does not exist in this event
      * @throws NoSuchAttributeTypeException if there is an attribute with an undefined type
      */
     private void set(String attribute, BaseType anObject)
             throws EventSystemException {
 
         if (isValidating() && getEventTemplateDB() != null) {
             if (getEventTemplateDB().checkForAttribute(name, attribute)) {
                 if (!getEventTemplateDB().checkTypeForAttribute(name, attribute, anObject)) {
                     throw new NoSuchAttributeTypeException("Wrong type '" + anObject.getTypeName() +
                                                            "' for " + name + "." + attribute);
                 }
             }
             else {
                 throw new NoSuchAttributeException("Attribute " + attribute + " does not exist for event " + name);
             }
         }
 
         if (anObject.getTypeObject() != null) {
             BaseType oldObject = null;
             int newSize = bytesStoreSize + ((attribute.length() + 1) + anObject.bytesStoreSize(encoding));
             if (newSize > MAX_MESSAGE_SIZE) {
                 throw new EventSystemException("Event size limit is " + MAX_MESSAGE_SIZE + " bytes.");
             }
             if ((oldObject = attributes.remove(attribute)) != null) {
                 bytesStoreSize -= (attribute.length() + 1) + oldObject.bytesStoreSize(encoding);
             }
 
             bytesStoreSize += (attribute.length() + 1) + anObject.bytesStoreSize(encoding);
             attributes.put(attribute, anObject);
         }
     }
 
     /**
      * Sets the given attribute with a <tt>boolean</tt> value given by <tt>aBool</tt>.
      *
      * @param attributeName the attribute to set
      * @param aBool         the boolean value to set
      * @throws NoSuchAttributeException     if the attribute does not exist in the event
      * @throws NoSuchAttributeTypeException if the attribute type does not match the EventTemplateDB
      */
     public void setBoolean(String attributeName, boolean aBool)
             throws EventSystemException {
         setBoolean(attributeName, Boolean.valueOf(aBool));
     }
 
     /**
      * Sets the given attribute with a <tt>Boolean</tt> value given by <tt>aBool</tt>.
      *
      * @param attributeName the attribute to set
      * @param aBool         the boolean value to set
      * @throws NoSuchAttributeException
      * @throws NoSuchAttributeTypeException
      */
     public void setBoolean(String attributeName, Boolean aBool)
             throws EventSystemException {
         set(attributeName, new BaseType(TypeID.BOOLEAN_STRING, TypeID.BOOLEAN_TOKEN, aBool));
     }
 
     /**
      * Set the given attribute with the <tt>unsigned short</tt> value given by <tt>aNumber</tt>.
      * Because Java does not support unsigned types, we must use a signed int to cover the range of unsigned short.
      *
      * @param attributeName the attribute to set
      * @param aNumber       the unsigned short value as an integer
      * @throws NoSuchAttributeException     if the attribute does not exist in the event
      * @throws NoSuchAttributeTypeException if the attribute type does not match the EventTemplateDB
      */
     public void setUInt16(String attributeName, int aNumber)
             throws EventSystemException {
         setUInt16(attributeName, Integer.valueOf(aNumber));
     }
 
     /**
      * Set the given attribute with the <tt>Integer</tt> value given by <tt>aNumber</tt>.
      * This should be an <tt>unsigned short</tt>, but is an Integer because Java does not support unsigned types,
      * and a signed integer is needed to cover the range of an unsigned short.
      *
      * @param attributeName the attribute to set
      * @param aNumber       the value
      */
     public void setUInt16(String attributeName, Integer aNumber)
             throws EventSystemException {
         set(attributeName, new BaseType(TypeID.UINT16_STRING, TypeID.UINT16_TOKEN, aNumber));
     }
 
     /**
      * Set the given attribute with the <tt>short</tt> value given by <tt>aNumber</tt>.
      *
      * @param attributeName the attribute to set
      * @param aNumber       the short value to set
      * @throws NoSuchAttributeException     if the attribute does not exist in the event
      * @throws NoSuchAttributeTypeException if the attribute type does not match the EventTemplateDB
      */
     public void setInt16(String attributeName, short aNumber)
             throws EventSystemException {
         setInt16(attributeName, Short.valueOf(aNumber));
     }
 
     /**
      * Set the given attribute with the <tt>Short</tt> value given by <tt>aNumber</tt>.
      *
      * @param attributeName the attribute to set
      * @param aNumber       the short value to set
      * @throws NoSuchAttributeException     if the attribute does not exist in the event
      * @throws NoSuchAttributeTypeException if the attribute type does not match the EventTemplateDB
      */
     public void setInt16(String attributeName, Short aNumber)
             throws EventSystemException {
         set(attributeName, new BaseType(TypeID.INT16_STRING, TypeID.INT16_TOKEN, aNumber));
     }
 
     /**
      * Set the given attribute with the <tt>unsigned int</tt> value given by <tt>aNumber</tt>.
      * Because Java does not support unsigned types, we must use a signed long to cover the range of an unsigned int.
      *
      * @param attributeName the attribute to set
      * @param aNumber       the unsigned int value as a long
      * @throws NoSuchAttributeException     if the attribute does not exist in the event
      * @throws NoSuchAttributeTypeException if the attribute type does not match the EventTemplateDB
      */
     public void setUInt32(String attributeName, long aNumber)
             throws EventSystemException {
         setUInt32(attributeName, Long.valueOf(aNumber));
     }
 
     /**
      * Set the given attribute with the <tt>Long</tt> value given by <tt>aNumber</tt>.
      * This should be an <tt>unsigned int</tt>, but is an Long because Java does not support unsigned types,
      * and a signed long is needed to cover the range of an unsigned int.
      *
      * @param attributeName the attribute to set
      * @param aNumber       the value
      */
     public void setUInt32(String attributeName, Long aNumber)
             throws EventSystemException {
         set(attributeName, new BaseType(TypeID.UINT32_STRING, TypeID.UINT32_TOKEN, aNumber));
     }
 
     /**
      * Set the given attribute with the <tt>int</tt> value given by <tt>aNumber</tt>.
      *
      * @param attributeName the attribute to set
      * @param aNumber       the integer value to set
      * @throws NoSuchAttributeException     if the attribute does not exist in the event
      * @throws NoSuchAttributeTypeException if the attribute type does not match the EventTemplateDB
      */
     public void setInt32(String attributeName, int aNumber)
             throws EventSystemException {
         setInt32(attributeName, Integer.valueOf(aNumber));
     }
 
     /**
      * Set the given attribute with the <tt>Integer</tt> value given by <tt>aNumber</tt>.
      *
      * @param attributeName the attribute to set
      * @param aNumber       the Integer value to set
      * @throws NoSuchAttributeException     if the attribute does not exist in the event
      * @throws NoSuchAttributeTypeException if the attribute type does not match the EventTemplateDB
      */
     public void setInt32(String attributeName, Integer aNumber)
             throws EventSystemException {
         set(attributeName, new BaseType(TypeID.INT32_STRING, TypeID.INT32_TOKEN, aNumber));
     }
 
     /**
      * Set the given attribute with the <tt>unsigned long</tt> value given by <tt>aNumber</tt>.
      *
      * @param attributeName the attribute to set
      * @param aNumber       the value
      * @throws NoSuchAttributeException     if the attribute does not exist in the event
      * @throws NoSuchAttributeTypeException if the attribute type does not match the EventTemplateDB
      */
     public void setUInt64(String attributeName, long aNumber)
             throws EventSystemException {
         set(attributeName, new BaseType(TypeID.UINT64_STRING, TypeID.UINT64_TOKEN, BigInteger.valueOf(aNumber)));
     }
 
     /**
      * Set the given attribute with the <tt>Long</tt> value given by <tt>aNumber</tt>.
      *
      * @param attributeName the attribute to set
      * @param aNumber       the value
      * @throws NoSuchAttributeException     if the attribute does not exist in the event
      * @throws NoSuchAttributeTypeException if the attribute type does not match the EventTemplateDB
      */
     public void setUInt64(String attributeName, Long aNumber)
             throws EventSystemException {
         set(attributeName,
             new BaseType(TypeID.UINT64_STRING, TypeID.UINT64_TOKEN, BigInteger.valueOf(aNumber.longValue())));
     }
 
     /**
      * Set the given attribute with the <tt>BigInteger</tt> value given by <tt>aNumber</tt>.
      * This should be an <tt>unsigned long</tt>, but is an BigInteger because Java does not support unsigned types,
      * and a BigInteger is needed to cover the range of an unsigned long.
      *
      * @param attributeName the attribute to set
      * @param aNumber       the value
      */
     public void setUInt64(String attributeName, BigInteger aNumber)
             throws EventSystemException {
         set(attributeName, new BaseType(TypeID.UINT64_STRING, TypeID.UINT64_TOKEN, aNumber));
     }
 
     /**
      * Set the given attribute with the <tt>long</tt> value given by <tt>aNumber</tt>.
      *
      * @param attributeName the attribute to set
      * @param aNumber       the long value to set
      * @throws NoSuchAttributeException     if the attribute does not exist in the event
      * @throws NoSuchAttributeTypeException if the attribute type does not match the EventTemplateDB
      */
     public void setInt64(String attributeName, long aNumber)
             throws EventSystemException {
         setInt64(attributeName, Long.valueOf(aNumber));
     }
 
     /**
      * Set the given attribute with the <tt>Long</tt> value given by <tt>aNumber</tt>.
      *
      * @param attributeName the attribute to set
      * @param aNumber       the Long value to set
      * @throws NoSuchAttributeException     if the attribute does not exist in the event
      * @throws NoSuchAttributeTypeException if the attribute type does not match the EventTemplateDB
      */
     public void setInt64(String attributeName, Long aNumber)
             throws EventSystemException {
         set(attributeName, new BaseType(TypeID.INT64_STRING, TypeID.INT64_TOKEN, aNumber));
     }
 
     /**
      * Set the given attribute with a <tt>String</tt>
      *
      * @param attributeName the attribute to set
      * @param aString       the String value to set
      * @throws NoSuchAttributeException     if the attribute does not exist in the event
      * @throws NoSuchAttributeTypeException if the attribute type does not match the EventTemplateDB
      */
     public void setString(String attributeName, String aString)
             throws EventSystemException {
         set(attributeName, new BaseType(TypeID.STRING_STRING, TypeID.STRING_TOKEN, aString));
     }
 
     /**
      * Set the given attribute with the <tt>ip address</tt> value given by <tt>address</tt>
      *
      * @param attributeName the attribute to set
      * @param address       the ip address in bytes
      * @throws NoSuchAttributeException     if the attribute does not exist in the event
      * @throws NoSuchAttributeTypeException if the attribute type does not match the EventTemplateDB
      */
     public void setIPAddress(String attributeName, byte[] address)
             throws EventSystemException {
         setIPAddress(attributeName, new IPAddress(address));
     }
 
     /**
      * Set the given attribute with the <tt>ip address</tt> value given by <tt>address</tt>
      *
      * @param attributeName the attribute to set
      * @param address       the ip address in bytes
      * @throws NoSuchAttributeException     if the attribute does not exist in the event
      * @throws NoSuchAttributeTypeException if the attribute type does not match the EventTemplateDB
      */
     public void setIPAddress(String attributeName, InetAddress address)
             throws EventSystemException {
         setIPAddress(attributeName, new IPAddress(address));
     }
 
     /**
      * Set the given attribute with the <tt>ip address</tt> value given by <tt>address</tt>
      *
      * @param attributeName the attribute to set
      * @param address       the ip address in bytes
      * @throws NoSuchAttributeException     if the attribute does not exist in the event
      * @throws NoSuchAttributeTypeException if the attribute type does not match the EventTemplateDB
      */
     public void setIPAddress(String attributeName, IPAddress address)
             throws EventSystemException {
         set(attributeName, new BaseType(TypeID.IPADDR_STRING, TypeID.IPADDR_TOKEN, address));
     }
 
     /**
      * Serializes the Event into a byte array
      *
      * @return the serialized byte array
      */
     public byte[] serialize() {
         /*
            * Serialization uses the following protocol
            * EVENTWORD,<number of elements>,ATTRIBUTEWORD,TYPETOKEN,
            * (UINT16|INT16|UINT32|INT32|UINT64|INT64|BOOLEAN|STRING)
            * ...ATTRIBUTEWORD,TYPETOKEN(UINT16|INT16|UINT32|INT32|
            * UINT64|INT64|BOOLEAN|STRING)
            *
            * The first attribute will always be the encoding if present.
            */
         byte[] bytes = new byte[this.bytesStoreSize];
         int offset = 0;
         int attributeCount = 0;
         short encoding = DEFAULT_ENCODING;
 
         if (attributes != null) {
             attributeCount = attributes.size();
         }
 
         offset += Serializer.serializeEVENTWORD(name, bytes, offset);
         offset += Serializer.serializeUINT16((short) (attributeCount), bytes, offset);
 
         /*
            * Set the encoding attributes in the event
            */
         if (attributes != null) {
             BaseType encodingBase = attributes.get(ENCODING);
             if (encodingBase != null) {
                 Object encodingObj = encodingBase.getTypeObject();
                 byte encodingType = encodingBase.getTypeToken();
                 if (encodingObj != null) {
                     if (encodingType == TypeID.INT16_TOKEN) {
                         encoding = (Short) encodingObj;
                         Log.trace("Character encoding: " + encoding);
                         offset += Serializer.serializeATTRIBUTEWORD(ENCODING, bytes, offset);
                         offset += Serializer.serializeBYTE(encodingType, bytes, offset);
                         offset += Serializer.serializeUINT16(encoding, bytes, offset);
                     }
                 }
             }
             else {
                 Log.warning("Character encoding null in event " + name);
             }
 
             Enumeration<String> e = attributes.keys();
             while (e.hasMoreElements()) {
                 String key = e.nextElement();
                 if (key.equals(ENCODING)) {
                     continue;
                 }
 
                 BaseType value = attributes.get(key);
                 Object data = value.getTypeObject();
                 byte typeToken = value.getTypeToken();
 
                 /* don't try to serialize nulls */
                 if (data == null) {
                     Log.warning("Attribute " + key + " was null in event " + name);
                     continue;
                 }
 
                 offset += Serializer.serializeATTRIBUTEWORD(key, bytes, offset);
                 offset += Serializer.serializeBYTE(typeToken, bytes, offset);
 
                 switch (typeToken) {
                     case TypeID.BOOLEAN_TOKEN:
                         offset += Serializer.serializeBOOLEAN((Boolean) data, bytes, offset);
                         break;
                     case TypeID.UINT16_TOKEN:
                         offset += Serializer.serializeUINT16((Integer) data, bytes, offset);
                         break;
                     case TypeID.INT16_TOKEN:
                         offset += Serializer.serializeINT16((Short) data, bytes, offset);
                         break;
                     case TypeID.UINT32_TOKEN:
                         offset += Serializer.serializeUINT32((Long) data, bytes, offset);
                         break;
                     case TypeID.INT32_TOKEN:
                         offset += Serializer.serializeINT32((Integer) data, bytes, offset);
                         break;
                     case TypeID.UINT64_TOKEN:
                         offset += Serializer.serializeUINT64((BigInteger) data, bytes, offset);
                         break;
                     case TypeID.INT64_TOKEN:
                         offset += Serializer.serializeINT64((Long) data, bytes, offset);
                         break;
                     case TypeID.STRING_TOKEN:
                         offset += Serializer.serializeSTRING(((String) data), bytes, offset, encoding);
                         break;
                     case TypeID.IPADDR_TOKEN:
                         offset += Serializer.serializeIPADDR(((IPAddress) data), bytes, offset);
                         break;
                     default:
                         Log.warning("Unknown BaseType token: " + typeToken);
                         break;
                 } // switch(typeToken)
 
                 Log.trace("Serialized attribute " + key);
             } // while(e.hasMoreElements())
         } // if(attributes != null)
 
         return bytes;
     }
 
     /**
      * Deserialize the Event from byte array
      *
      * @param bytes the byte array containing a serialized Event
      */
     public void deserialize(byte[] bytes)
             throws EventSystemException {
         if (bytes == null) {
             return;
         }
         if (state == null) {
             state = new DeserializerState();
         }
 
         state.reset();
         setEventName(Deserializer.deserializeEVENTWORD(state, bytes));
         long num = Deserializer.deserializeUINT16(state, bytes);
         if (Log.isLogTrace()) {
             Log.trace("Event name = " + getEventName());
             Log.trace("Number of attribute: " + num);
         }
         for (int i = 0; i < num; ++i) {
             String attribute = Deserializer.deserializeATTRIBUTEWORD(state, bytes);
 
             byte type = Deserializer.deserializeBYTE(state, bytes);
             if (Log.isLogTrace()) {
                 Log.trace("Attribute: " + attribute);
                 Log.trace("Type: " + TypeID.byteIDToString(type));
                 Log.trace("State: " + state);
             }
             if (attribute != null) {
                 if (i == 0 && attribute.equals(ENCODING)) {
                     if (type == TypeID.INT16_TOKEN) {
                         setEncoding(Deserializer.deserializeINT16(state, bytes));
                         continue;
                     }
                     else {
                         Log.warning("Found encoding, but type was not int16 while deserializing");
                     }
                 }
 
                 switch (type) {
                     case TypeID.BOOLEAN_TOKEN:
                         boolean aBool = Deserializer.deserializeBOOLEAN(state, bytes);
                         setBoolean(attribute, aBool);
                         break;
                     case TypeID.UINT16_TOKEN:
                         int uShort = Deserializer.deserializeUINT16(state, bytes);
                         setUInt16(attribute, uShort);
                         break;
                     case TypeID.INT16_TOKEN:
                         short aShort = Deserializer.deserializeINT16(state, bytes);
                         setInt16(attribute, aShort);
                         break;
                     case TypeID.UINT32_TOKEN:
                         long uInt = Deserializer.deserializeUINT32(state, bytes);
                         setUInt32(attribute, uInt);
                         break;
                     case TypeID.INT32_TOKEN:
                         int aInt = Deserializer.deserializeINT32(state, bytes);
                         setInt32(attribute, aInt);
                         break;
                     case TypeID.UINT64_TOKEN:
                         long uLong = Deserializer.deserializeUINT64(state, bytes);
                         setUInt64(attribute, BigInteger.valueOf(uLong));
                         break;
                     case TypeID.INT64_TOKEN:
                         long aLong = Deserializer.deserializeINT64(state, bytes);
                         setInt64(attribute, aLong);
                         break;
                     case TypeID.STRING_TOKEN:
                         String s = Deserializer.deserializeSTRING(state, bytes, encoding);
                         setString(attribute, s);
                         break;
                     case TypeID.IPADDR_TOKEN:
                         byte[] inetAddress = Deserializer.deserializeIPADDR(state, bytes);
                         setIPAddress(attribute, inetAddress);
                         break;
                     default:
                         Log.warning("Unknown type " + type + " in deserialization");
                 }
             }
         } // for (int i =0 ...
 
     }
 
     /**
      * Returns a mutable copy of the event.  This is a SLOW operation.
      *
      * @return Event the Event object
      * @throws NoSuchEventException         if the Event does not exist in the EventTemplateDB
      * @throws NoSuchAttributeException     if the attribute does not exist in this event
      * @throws NoSuchAttributeTypeException if there is an attribute that does not match a type in the EventTemplateDB
      */
     public Event copy() throws EventSystemException {
         /* match the type-checking of the original event */
         Event evt = new Event(name, isValidating(), getEventTemplateDB());
         for (Enumeration<String> e = attributes.keys(); e.hasMoreElements();) {
             String key = e.nextElement();
             BaseType value = attributes.get(key);
             evt.set(key, value);
         }
 
         return evt;
     }
 
     /**
      * Returns a String representation of this event
      *
      * @return a String return of this event.
      */
     public String toString() {
         if (name == null) {
             return "";
         }
 
         StringBuffer sb = new StringBuffer();
         sb.append(name);
         sb.append("\n{\n");
 
         if (attributes != null) {
             int i = 0;
             String[] keys = new String[attributes.size()];
             for (Enumeration<String> e = attributes.keys(); e.hasMoreElements();) {
                 keys[i++] = e.nextElement();
             }
 
             Arrays.sort(keys);
 
             for (i = 0; i < attributes.size(); ++i) {
                 BaseType value = attributes.get(keys[i]);
                 if (isValidating() && getEventTemplateDB() != null) {
                     if (getEventTemplateDB().checkTypeForAttribute(name, keys[i], TypeID.UINT64_STRING)) {
                         try {
                             sb.append("\t")
                                     .append(keys[i])
                                     .append(" = ")
                                     .append(NumberCodec.toHexString(getUInt64(keys[i])))
                                     .append(";\n");
                         }
                         catch (EventSystemException exc) {
                             Log.warning("Event.toString : ", exc);
                         }
                     }
                     else {
                         sb.append("\t").append(keys[i]).append(" = ").append(value).append(";\n");
                     }
                 }
                 else {
                     sb.append("\t").append(keys[i]).append(" = ").append(value).append(";\n");
                 }
             } // for(i = 0; i < attributes.size() ...
         } // if(attributes != null)
 
         sb.append("}");
         return sb.toString();
     }
 
     @Override
     public int hashCode() {
         return toString().hashCode();
     }
 
     public boolean equals(Object o) {
         if (o == null) {
             return false;
         }
         if (getClass().getName().equals(o.getClass().getName())) {
             return toString().equals(o.toString());
         }
         else {
             return false;
         }
     }
 
     /**
      * This method can be used to validate an event after it has been created.
      *
      * @throws EventSystemException
      */
     public void validate() throws EventSystemException {
         EventTemplateDB templ = getEventTemplateDB();
         if (templ == null) {
             throw new EventSystemException("No template defined.");
         }
         if (!templ.checkForEvent(name)) {
             throw new NoSuchEventException("Event " + name + " does not exist in event definition");
         }
         for (String key : attributes.keySet()) {
             if (!templ.checkForAttribute(name, key)) {
                 throw new NoSuchAttributeException("Attribute " + key + " does not exist for event " + name);
             }
             Object value = get(key);
             BaseType expected = templ.getBaseTypeForObjectAttribute(name, key, value);
             BaseType bt = BaseType.baseTypeFromObject(value);
             /**
              * There are no unsigned values in java so they are kind of a special case
              * in that i can't guess which one the person meant. This small hack treats
              * similar types the same way.
              */
             if ((expected.getTypeToken() == TypeID.UINT16_TOKEN &&
                  bt.getTypeToken() == TypeID.INT32_TOKEN) ||
                 (expected.getTypeToken() == TypeID.UINT32_TOKEN &&
                  bt.getTypeToken() == TypeID.INT64_TOKEN) ||
                 (expected.getTypeToken() == TypeID.UINT64_TOKEN &&
                  bt.getTypeToken() == TypeID.INT64_TOKEN)) {
                 bt = expected;
             }
             if (!templ.checkTypeForAttribute(name, key, bt)) {
                 throw new NoSuchAttributeTypeException("Wrong type '" + bt.getTypeName() +
                                                        "' for " + name + "." + key);
             }
         }
     }
 }
