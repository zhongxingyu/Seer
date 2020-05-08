 package net.link.safeonline.sdk.auth.protocol.openid;
 
 import java.io.Serializable;
 import java.util.*;
 import net.link.safeonline.attribute.provider.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.openid4java.message.ax.FetchResponse;
 
 
 public abstract class OpenIdUtil {
 
     private static final Log LOG = LogFactory.getLog( OpenIdUtil.class );
 
     public static final String COMPOUND_TYPE_ID     = "E7AC5C06-077F-420B-A8C3-C8561A152330";
     public static final String MULTIVALUED_TYPE_ID  = "5870B6F9-CD4A-4496-854D-9A37EF9340AF";
     public static final String SINGLEVALUED_TYPE_ID = "272C6ADA-BA72-45FA-A9F9-E795CF70047C";
 
     public static final String TYPE_URI_ATTRIBUTE_ID           = "uri.attributeId.";
     public static final String TYPE_URI_ATTRIBUTE_TYPE         = "uri.type.";
     public static final String TYPE_URI_ATTRIBUTE_NAME         = "uri.name.";
     public static final String TYPE_URI_ATTRIBUTE_VALUE        = "uri.value.";
     public static final String TYPE_URI_MEMBER_ATTRIBTUE_NAME  = "uri.member.name.";
     public static final String TYPE_URI_MEMBER_ATTRIBTUE_VALUE = "uri.member.value.";
 
     /*
     * OpenID attribute encoding:
     *
     * 	field		typeURI				                value
     *
     *   ID		    uri.attributeId.<1>				    attributeId
     *   type		uri.type.<attributeId>		        Type.UUID
     *   name	    uri.name.<attributeId>		        attributeName
     *   value	    uri.value.<attributeId>		        value
     *
     *   compound: no value
     *   m.name	    uri.member.name.<attributeId>.<1>   memberName
     *   m.value	    uri.member.value.<attributeId>.<1>  value
     */
 
     /*
     * Returns FetchResponse for list of attributes. Filtered if fetch_required_attributes or fetch_optional_attributes it not null as in
     * that case we had a FetchRequest attached.
     */
     @SuppressWarnings("unchecked")
     public static FetchResponse getFetchResponse(Map<String, List<AttributeCore>> attributes, Map<String, String> requiredAttributes,
                                                  Map<String, String> optionalAttributes) {
 
         FetchResponse fetchResponse = FetchResponse.createFetchResponse();
         int idx = 0;
         for (Map.Entry<String, List<AttributeCore>> attributeEntry : attributes.entrySet()) {
 
             for (AttributeCore attribute : attributeEntry.getValue()) {
                 fetchResponse = addAttribute( idx++, fetchResponse, attribute, requiredAttributes, optionalAttributes );
             }
         }
 
         return fetchResponse;
     }
 
     private static FetchResponse addAttribute(int idx, FetchResponse fetchResponse, AttributeCore attribute,
                                               Map<String, String> requiredAttributes, Map<String, String> optionalAttributes) {
 
         LOG.debug( "add attribute: " + attribute.getName() );
 
         boolean add = false;
         if (null != requiredAttributes && null != optionalAttributes) {
            if (requiredAttributes.containsValue( attribute.getName() ) || optionalAttributes.containsValue(
                    attribute.getName() )) {
                 add = true;
             }
         } else
             add = true;
 
         if (add) {
 
             String type;
             if (attribute.getAttributeType().isCompound()) {
                 type = COMPOUND_TYPE_ID;
             } else if (attribute.getAttributeType().isMultivalued()) {
                 type = MULTIVALUED_TYPE_ID;
             } else {
                 type = SINGLEVALUED_TYPE_ID;
             }
 
             // add attributeId
             fetchResponse.addAttribute( TYPE_URI_ATTRIBUTE_ID + idx, attribute.getId() );
 
             // add type
             fetchResponse.addAttribute( TYPE_URI_ATTRIBUTE_TYPE + attribute.getId(), type );
 
             // add attribute name
             fetchResponse.addAttribute( TYPE_URI_ATTRIBUTE_NAME + attribute.getId(), attribute.getName() );
 
             // add value
             if (attribute.getAttributeType().isCompound()) {
 
                 int memberIdx = 0;
                 for (AttributeSDK<?> memberSDK : ((Compound) attribute.getValue()).getMembers()) {
                     AttributeCore member = (AttributeCore) memberSDK;
 
                     // add member name
                    fetchResponse.addAttribute( TYPE_URI_MEMBER_ATTRIBTUE_NAME + attribute.getId() + '.' + memberIdx,
                                                member.getName() );
 
                     // add member value
                     String value = getAttributeValue( member.getValue() );
                     fetchResponse.addAttribute( TYPE_URI_MEMBER_ATTRIBTUE_VALUE + attribute.getId() + '.' + memberIdx, value );
 
                     memberIdx++;
                 }
             } else {
                 // add attribute value
                 String value = getAttributeValue( attribute.getValue() );
                 fetchResponse.addAttribute( TYPE_URI_ATTRIBUTE_VALUE + attribute.getId(), value );
             }
         }
 
         return fetchResponse;
     }
 
     private static String getAttributeValue(Serializable attributeValue) {
 
         if (Boolean.class.isAssignableFrom( attributeValue.getClass() ))
             return Boolean.toString( (Boolean) attributeValue );
         else if (Integer.class.isAssignableFrom( attributeValue.getClass() ))
             return Integer.toString( (Integer) attributeValue );
         else if (Long.class.isAssignableFrom( attributeValue.getClass() ))
             return Long.toString( (Long) attributeValue );
         else if (Short.class.isAssignableFrom( attributeValue.getClass() ))
             return Short.toString( (Short) attributeValue );
         else if (Byte.class.isAssignableFrom( attributeValue.getClass() ))
             return Byte.toString( (Byte) attributeValue );
         else if (Float.class.isAssignableFrom( attributeValue.getClass() ))
             return Float.toString( (Float) attributeValue );
         else if (Double.class.isAssignableFrom( attributeValue.getClass() ))
             return Double.toString( (Double) attributeValue );
         else if (Date.class.isAssignableFrom( attributeValue.getClass() ))
             return ((Date) attributeValue).toString();
         else
             return (String) attributeValue;
     }
 
     /**
      * Parse LinkID attributes from OpenID {@link FetchResponse}
      *
      * @param fetchResponse OpenID fetch response
      *
      * @return map of LinkID attributes
      */
     @SuppressWarnings("unchecked")
     public static Map<String, List<AttributeSDK<?>>> getAttributeMap(final FetchResponse fetchResponse) {
 
         Map<String, List<AttributeSDK<?>>> attributeMap = new HashMap<String, List<AttributeSDK<?>>>();
 
         int idx = 0;
         while (true) {
 
             // get attribute ID
             String attributeIdAlias = fetchResponse.getAttributeAlias( TYPE_URI_ATTRIBUTE_ID + idx );
             if (null == attributeIdAlias)
                 break;
             String attributeId = getValue( fetchResponse, TYPE_URI_ATTRIBUTE_ID + idx );
 
             // get type
             String type = getValue( fetchResponse, TYPE_URI_ATTRIBUTE_TYPE + attributeId );
 
             // get attribute name
             String attributeName = getValue( fetchResponse, TYPE_URI_ATTRIBUTE_NAME + attributeId );
 
             DataType dataType = DataType.STRING;
             if (type.equals( COMPOUND_TYPE_ID ))
                 dataType = DataType.COMPOUNDED;
 
             AttributeType attributeType = new AttributeType( attributeName, dataType );
 
             // get attribute value
             if (attributeType.isCompound()) {
 
                 // get members
                 List<AttributeSDK<?>> members = new LinkedList<AttributeSDK<?>>();
                 int memberIdx = 0;
                 while (true) {
                     String memberNameAlias = fetchResponse.getAttributeAlias(
                             TYPE_URI_MEMBER_ATTRIBTUE_NAME + attributeId + '.' + memberIdx );
                     if (null == memberNameAlias)
                         break;
 
                     String memberName = getValue( fetchResponse, TYPE_URI_MEMBER_ATTRIBTUE_NAME + attributeId + '.' + memberIdx );
                     String memberValue = getValue( fetchResponse, TYPE_URI_MEMBER_ATTRIBTUE_VALUE + attributeId + '.' + memberIdx );
 
                     members.add( new AttributeSDK( attributeId, memberName, memberValue ) );
 
                     memberIdx++;
                 }
 
                 addToMap( attributeMap, new AttributeSDK( attributeId, attributeType.getName(), new Compound( members ) ) );
             } else {
                 addToMap( attributeMap, new AttributeSDK( attributeId, attributeType.getName(),
                                                          getValue( fetchResponse, TYPE_URI_ATTRIBUTE_VALUE + attributeId ) ) );
             }
 
             idx++;
         }
 
         return attributeMap;
     }
 
     private static void addToMap(Map<String, List<AttributeSDK<?>>> attributeMap, AttributeSDK<?> attribute) {
 
         List<AttributeSDK<?>> attributes = attributeMap.get( attribute.getName() );
         if (null == attributes) {
             attributes = new LinkedList<AttributeSDK<?>>();
         }
         attributes.add( attribute );
         attributeMap.put( attribute.getName(), attributes );
     }
 
     private static String getValue(FetchResponse fetchResponse, String typeUri) {
 
         String alias = fetchResponse.getAttributeAlias( typeUri );
         if (null == alias)
             throw new RuntimeException( "Alias not found for typeUri: " + typeUri );
 
         String value = fetchResponse.getAttributeValue( alias );
         if (null == value)
             throw new RuntimeException( "Value not found for alias: " + alias + " typeUri: " + typeUri );
         return value;
     }
 }
