 package net.link.safeonline.attribute.provider.profile;
 
 import com.lyndir.lhunath.opal.system.logging.exception.InternalInconsistencyException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.*;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import net.link.safeonline.attribute.provider.*;
 import net.link.safeonline.attribute.provider.exception.AttributeNotFoundException;
 import net.link.safeonline.attribute.provider.exception.LocalizationImportException;
 import net.link.safeonline.attribute.provider.input.AttributeInputPanel;
 import net.link.safeonline.attribute.provider.profile.attributes.*;
 import net.link.safeonline.attribute.provider.service.LinkIDService;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jetbrains.annotations.Nullable;
 
 
 public class ProfileAttributeProvider extends AttributeProvider implements ServletContextListener {
 
     private static final Log LOG = LogFactory.getLog( ProfileAttributeProvider.class );
 
     private final List<ProfileAttribute> attributes = new LinkedList<ProfileAttribute>();
 
     public ProfileAttributeProvider() {
 
         attributes.add( new CityAttribute( getJndiLocation() ) );
         attributes.add( new CountryAttribute( getJndiLocation() ) );
         attributes.add( new DobAttribute( getJndiLocation() ) );
         attributes.add( new EmailAttribute( getJndiLocation() ) );
         attributes.add( new FamilyNameAttribute( getJndiLocation() ) );
         attributes.add( new GenderAttribute( getJndiLocation() ) );
         attributes.add( new GivenNameAttribute( getJndiLocation() ) );
         attributes.add( new MobileAttribute( getJndiLocation() ) );
         attributes.add( new PhoneAttribute( getJndiLocation() ) );
         attributes.add( new PostalCodeAttribute( getJndiLocation() ) );
         attributes.add( new StreetAndNumberAttribute( getJndiLocation() ) );
        attributes.add( new NrnAttribute( getJndiLocation() ) );
     }
 
     @Override
     public List<AttributeCore> listAttributes(final LinkIDService linkIDService, final String userId, final String attributeName,
                                               final boolean filterInvisible) {
 
         for (ProfileAttribute demoAttribute : attributes) {
             if (demoAttribute.getAttributeType().getName().equals( attributeName ))
                 return demoAttribute.listAttributes( linkIDService, userId );
         }
 
         throw new RuntimeException( String.format( "Attribute \"%s\" not supported.", attributeName ) );
     }
 
     @Nullable
     @Override
     public AttributeCore findAttribute(final LinkIDService linkIDService, final String userId, final String attributeName,
                                        final String attributeId) {
 
         for (ProfileAttribute demoAttribute : attributes) {
             if (demoAttribute.getAttributeType().getName().equals( attributeName ))
                 return demoAttribute.findAttribute( linkIDService, userId, attributeId );
         }
 
         throw new RuntimeException( String.format( "Attribute \"%s\" not supported.", attributeName ) );
     }
 
     @Override
     public AttributeCore findCompoundAttributeWhere(final LinkIDService linkIDService, final String userId,
                                                     final String parentAttributeName, final String memberAttributeName,
                                                     final Serializable memberValue) {
 
         throw new RuntimeException( String.format( "Attribute \"%s\" not supported.", parentAttributeName ) );
     }
 
     @Override
     public void removeAttributes(final LinkIDService linkIDService, final String userId, final String attributeName) {
 
         for (ProfileAttribute demoAttribute : attributes) {
             if (demoAttribute.getAttributeType().getName().equals( attributeName )) {
                 demoAttribute.removeAttributes( linkIDService, userId );
                 return;
             }
         }
 
         throw new RuntimeException( String.format( "Attribute \"%s\" not supported.", attributeName ) );
     }
 
     @Override
     public void removeAttribute(final LinkIDService linkIDService, final String userId, final String attributeName,
                                 final String attributeId)
             throws AttributeNotFoundException {
 
         for (ProfileAttribute demoAttribute : attributes) {
             if (demoAttribute.getAttributeType().getName().equals( attributeName )) {
                 demoAttribute.removeAttribute( linkIDService, userId, attributeId );
                 return;
             }
         }
 
         throw new RuntimeException( String.format( "Attribute \"%s\" not supported.", attributeName ) );
     }
 
     @Override
     public void removeAttributes(final LinkIDService linkIDService, final String attributeName) {
 
         for (ProfileAttribute demoAttribute : attributes) {
             if (demoAttribute.getAttributeType().getName().equals( attributeName )) {
                 demoAttribute.removeAttributes( linkIDService );
                 return;
             }
         }
 
         throw new RuntimeException( String.format( "Attribute \"%s\" not supported.", attributeName ) );
     }
 
     @Override
     public AttributeCore setAttribute(final LinkIDService linkIDService, final String userId, final AttributeCore attribute) {
 
         for (ProfileAttribute demoAttribute : attributes) {
             if (demoAttribute.getAttributeType().getName().equals( attribute.getAttributeType().getName() ))
                 return demoAttribute.setAttribute( linkIDService, userId, attribute );
         }
 
         throw new RuntimeException( String.format( "Attribute \"%s\" not supported.", attribute.getAttributeType().getName() ) );
     }
 
     @Override
     public List<AttributeType> getSupportedAttributeTypes() {
 
         List<AttributeType> attributeTypes = new LinkedList<AttributeType>();
         for (ProfileAttribute demoAttribute : attributes) {
             attributeTypes.add( demoAttribute.getAttributeType() );
         }
         return attributeTypes;
     }
 
     @Override
     public void intialize(final LinkIDService linkIDService) {
 
         for (ProfileAttribute demoAttribute : attributes) {
             demoAttribute.initialize( linkIDService );
         }
 
         // initialize localization
         InputStream localizationStream = getClass().getResourceAsStream( "/localization_profile.xml" );
         try {
             linkIDService.getLocalizationService().importXML( localizationStream );
         }
         catch (LocalizationImportException e) {
             throw new InternalInconsistencyException( e );
         }
     }
 
     @Override
     public Map<Serializable, Long> categorize(final LinkIDService linkIDService, final List<String> subjects, final String attributeName) {
 
         return new HashMap<Serializable, Long>();
     }
 
     @Override
     public AttributeInputPanel getAttributeInputPanel(final LinkIDService linkIDService, final String id, final String userId,
                                                       final AttributeCore attribute) {
 
         for (ProfileAttribute demoAttribute : attributes) {
             if (demoAttribute.getAttributeType().getName().equals( attribute.getAttributeType().getName() )) {
                 AttributeInputPanel attributeInputPanel = demoAttribute.findAttributeInputPanel( linkIDService, id, userId, attribute );
                 if (null != attributeInputPanel)
                     return attributeInputPanel;
                 else
                     return getDefaultAttributeInputPanel( id, attribute );
             }
         }
 
         throw new RuntimeException( String.format( "Attribute \"%s\" not supported.", attribute.getAttributeType().getName() ) );
     }
 
     @Override
     public AttributeProvider getAttributeProvider() {
 
         return new ProfileAttributeProvider();
     }
 
     @Override
     public String getName() {
 
         return "Demo";
     }
 
     public void contextInitialized(final ServletContextEvent sce) {
 
         LOG.debug( "bind Demo attribute provider" );
         register();
     }
 
     public void contextDestroyed(final ServletContextEvent sce) {
 
         LOG.debug( "unbind Demo attribute provider" );
         unregister();
     }
 }
