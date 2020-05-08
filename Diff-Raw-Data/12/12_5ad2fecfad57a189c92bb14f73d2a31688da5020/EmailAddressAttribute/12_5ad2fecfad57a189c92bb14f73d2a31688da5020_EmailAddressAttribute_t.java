 package net.link.safeonline.attribute.provider.profile.attributes;
 
import java.util.ArrayList;
import java.util.List;
 import net.link.safeonline.attribute.provider.*;
 import net.link.safeonline.attribute.provider.exception.AttributePermissionDeniedException;
 import net.link.safeonline.attribute.provider.input.AttributeInputPanel;
 import net.link.safeonline.attribute.provider.profile.attributes.panel.EmailAttributeInputPanel;
 import net.link.safeonline.attribute.provider.service.LinkIDService;
 
 
 public class EmailAddressAttribute extends AbstractProfileAttribute {
 
     public static String NAME = "profile.email.address";
 
     private static final String RESOURCE_EMAIL_TEMPLATE = "PasswordConfirmationEmailTemplate.vm";
 
     public EmailAddressAttribute(String providerJndi) {
 
         super( providerJndi, null, DataType.STRING );
 
     }
 
     public String getName() {
 
         return NAME;
     }
 
     @Override
     public AttributeType getAttributeType() {
         return new AttributeType( getName(), getDataType(), getProviderJndi(), true, true, true, true, false );
     }
 
      @Override
     public AttributeInputPanel findAttributeInputPanel(final LinkIDService linkIDService, final String id, final String userId,
                                                        final AttributeCore attribute) {
         return new EmailAttributeInputPanel( id, linkIDService, attribute, userId );
     }
 
     /**
      * Overridden in case someone modifies the emailaddress attribute directly
      * @param linkIDService
      * @param userId
      * @param attribute
      * @return
      * @throws AttributePermissionDeniedException
      */
     @Override
     public AttributeCore setAttribute(final LinkIDService linkIDService, final String userId, final AttributeCore attribute)
             throws AttributePermissionDeniedException {
 
         AttributeCore compound;
         AttributeCore previousValue;
         if (attribute.getId() == null){
             // doesn't exist yet in database, create everything
             compound = new AttributeCore(  new EmailAttribute(null).getAttributeType() );
            List<AttributeCore> members = new ArrayList<AttributeCore>( 3 );
            members.add( attribute );
            Compound value = new Compound( members );
            compound.setValue( value );
         }
         else{
             //get existing values from database. The attribute we have is an updated one, so we can't use its value to find the compound
             previousValue = linkIDService.getPersistenceService().findAttribute( userId, attribute.getName(), attribute.getId() );
             compound = linkIDService.getPersistenceService().findCompoundAttributeWhere( userId, EmailAttribute.NAME, previousValue.getName() , previousValue.getValue() );
            previousValue.setValue( attribute.getValue() );
         }
 
        compound = (new EmailAttribute( null )).setAttribute( linkIDService, userId, compound );
 
         return ((AttributeCore) ((Compound)compound.getValue()).findMember( NAME ));
     }
 
 }
