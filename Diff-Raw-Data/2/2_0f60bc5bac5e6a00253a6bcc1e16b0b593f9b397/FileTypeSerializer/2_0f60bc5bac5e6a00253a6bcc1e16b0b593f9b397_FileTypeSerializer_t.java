 package com.cedarsoft.serialization;
 
 import com.cedarsoft.Version;
 import com.cedarsoft.VersionRange;
 import com.cedarsoft.file.Extension;
 import com.cedarsoft.file.FileType;
 import com.cedarsoft.serialization.stax.AbstractStaxMateSerializer;
 import com.google.inject.Inject;
 import org.codehaus.staxmate.out.SMOutputElement;
 import org.jetbrains.annotations.NonNls;
 import org.jetbrains.annotations.NotNull;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import java.io.IOException;
 import java.util.List;
 
 /**
  *
  */
 public class FileTypeSerializer extends AbstractStaxMateSerializer<FileType> {
   @NotNull
   @NonNls
   private static final String ATTRIBUTE_DEPENDENT = "dependent";
   @NotNull
   @NonNls
   private static final String ELEMENT_ID = "id";
   @NotNull
   @NonNls
   private static final String ELEMENT_EXTENSION = "extension";
   @NotNull
   @NonNls
   private static final String ATTRIBUTE_DEFAULT = "default";
 
   @Inject
   public FileTypeSerializer( @NotNull ExtensionSerializer extensionSerializer ) {
    super( "fileType", "http://www.cedarsoft.com/file/type", new VersionRange( new Version( 1, 0, 0 ), new Version( 1, 0, 0 ) ) );
 
     add( extensionSerializer ).responsibleFor( Extension.class )
       .map( 1, 0, 0 ).toDelegateVersion( 1, 0, 0 )
       ;
 
     getDelegatesMappings().verify();
   }
 
   @Override
   public void serialize( @NotNull SMOutputElement serializeTo, @NotNull FileType object ) throws IOException, XMLStreamException {
     serializeTo.addAttribute( ATTRIBUTE_DEPENDENT, String.valueOf( object.isDependentType() ) );
     serializeTo.addElement( serializeTo.getNamespace(), ELEMENT_ID ).addCharacters( object.getId() );
 
     for ( Extension extension : object.getExtensions() ) {
       SMOutputElement extensionElement = serializeTo.addElement( serializeTo.getNamespace(), ELEMENT_EXTENSION );
 
       if ( object.isDefaultExtension( extension ) ) {
         extensionElement.addAttribute( ATTRIBUTE_DEFAULT, String.valueOf( true ) );
       }
 
       serialize( extension, Extension.class, extensionElement );
     }
   }
 
   @NotNull
   @Override
   public FileType deserialize( @NotNull XMLStreamReader deserializeFrom, @NotNull Version formatVersion ) throws IOException, XMLStreamException {
     boolean dependent = Boolean.parseBoolean( deserializeFrom.getAttributeValue( null, ATTRIBUTE_DEPENDENT ) );
     String id = getChildText( deserializeFrom, ELEMENT_ID );
 
     List<? extends Extension> extensions = deserializeCollection( deserializeFrom, Extension.class, formatVersion );
     return new FileType( id, dependent, extensions );
   }
 }
