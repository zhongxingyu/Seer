 package net.link.safeonline.attribute.provider.profile.attributes;
 
 import java.util.List;
 import net.link.safeonline.attribute.provider.*;
 import net.link.safeonline.attribute.provider.exception.*;
 import net.link.safeonline.attribute.provider.input.AttributeInputPanel;
 import net.link.safeonline.attribute.provider.profile.ProfileAttribute;
 import net.link.safeonline.attribute.provider.service.LinkIDService;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jetbrains.annotations.Nullable;
 
 
 /**
  * Abstract Profile Attribute.
  * <p/>
  * If a primary attribute is specified, it will first check if that attribute exists and if so return that one.
  */
 public abstract class AbstractProfileAttribute implements ProfileAttribute {
 
     private static final Log LOG = LogFactory.getLog( AbstractProfileAttribute.class );
 
     private final String   providerJndi;
     private final String   fallbackAttributeName;
     private final DataType dataType;
 
     protected AbstractProfileAttribute(final String providerJndi, @Nullable final String fallbackAttributeName, final DataType dataType) {
 
         this.providerJndi = providerJndi;
         this.fallbackAttributeName = fallbackAttributeName;
         this.dataType = dataType;
     }
 
     public AttributeType getAttributeType() {
 
         return new AttributeType( getName(), dataType, providerJndi, true, true, false, false, false );
     }
 
     public List<AttributeCore> listAttributes(final LinkIDService linkIDService, final String userId) {
 
         List<AttributeCore> attributes = linkIDService.getPersistenceService().listAttributes( userId, getName(), true );
         if (attributes.isEmpty() && null != fallbackAttributeName) {
             // no values found, fallback if possible
             try {
                 List<AttributeCore> fallbackAttributes = linkIDService.getAttributeService()
                                                                       .listAttributes( userId, fallbackAttributeName, true );
                 if (!fallbackAttributes.isEmpty()) {
                     // jay, found some, copy them over, beware to change attribute type and ID!
                     for (AttributeCore fallbackAttribute : fallbackAttributes) {
 
                         attributes.add( new AttributeCore( null, getAttributeType(), fallbackAttribute.getValue() ) );
                     }
                 }
             }
             catch (AttributeTypeNotFoundException ignored) {
                 // do nothing, seems fallback attribute does not exist, too bad
             }
         }
         return attributes;
     }
 
     @Nullable
     public AttributeCore findAttribute(final LinkIDService linkIDService, final String userId, @Nullable final String attributeId) {
 
         AttributeCore attribute = linkIDService.getPersistenceService().findAttribute( userId, attributeId, getName() );
         if (null == attribute && null != fallbackAttributeName) {
             // no value found, fallback if possible
             AttributeCore fallbackAttribute = null;
             try {
                 fallbackAttribute = linkIDService.getAttributeService().findAttribute( userId, fallbackAttributeName, attributeId );
             }
             catch (AttributeTypeNotFoundException ignored) {
                 // do nothing, seems fallback attribute does not exist, too bad
             }
             if (null != fallbackAttribute) {
                 attribute = new AttributeCore( null, getAttributeType(), fallbackAttribute.getValue() );
             }
         }
 
         return attribute;
     }
 
     public void removeAttributes(final LinkIDService linkIDService, final String userId) {
 
         linkIDService.getPersistenceService().removeAttributes( userId, getName() );
     }
 
     public void removeAttribute(final LinkIDService linkIDService, final String userId, final String attributeId)
             throws AttributeNotFoundException {
 
        linkIDService.getPersistenceService().removeAttribute( userId, attributeId, getName() );
     }
 
     public void removeAttributes(final LinkIDService linkIDService) {
 
         linkIDService.getPersistenceService().removeAttributes( getName() );
     }
 
     public AttributeCore setAttribute(final LinkIDService linkIDService, final String userId, final AttributeCore attribute)
             throws AttributePermissionDeniedException {
 
         return linkIDService.getPersistenceService().setAttribute( userId, attribute );
     }
 
     @Nullable
     public AttributeInputPanel findAttributeInputPanel(final LinkIDService linkIDService, final String id, final String userId,
                                                        final AttributeCore attribute) {
 
         return null;
     }
 
     public void initialize(final LinkIDService linkIDService) {
 
         LOG.debug( "initialize" );
     }
 
     public String getProviderJndi() {
 
         return providerJndi;
     }
 
     public DataType getDataType() {
 
         return dataType;
     }
 
     public String getFallbackAttributeName() {
 
         return fallbackAttributeName;
     }
 }
