 package de.zib.gndms.model.gorfx.types.io;
 
 import de.zib.gndms.model.gorfx.types.DataDescriptor;
 
 import java.util.Properties;
 import java.util.Set;
 
 /**
  * Reads a data descriptor form a property file.
  *
  * @author: Maik Jorra <jorra@zib.de>
  * @version: $Id$
  * <p/>
  * User: mjorra, Date: 18.09.2008, Time: 10:36:23
  */
 public class DataDescriptorPropertyReader extends AbstractPropertyReader<DataDescriptor> {
 
 
     public DataDescriptorPropertyReader() {
         super( DataDescriptor.class );
     }
 
 
     public DataDescriptorPropertyReader( Properties properties ) {
         super(  DataDescriptor.class, properties );
     }
 
 
     /**
      * Reads a provided property file.
     * PRECONDITION  begin( ) must have been called.
      */
     @Override
     public void read( ) {
 
         getProduct( ).setObjectList(
             PropertyReadWriteAux.readListMultiLine( getProperties(), SfrProperty.OBJECT_ITEMS.key ) );
 
        if(! getProperties().containsKey( SfrProperty.JUST_DOWNLOAD.key ) )
             getProduct().setConstrains( DataConstraintsPropertyReader.readDataConstraints( getProperties() ) );
 
         getProduct( ).setDataFormat( getProperties().getProperty( SfrProperty.FILE_FORMAT.key ) );
         getProduct( ).setDataArchiveFormat( getProperties().getProperty( SfrProperty.FILE_ARCHIVE_FORMAT.key ) );
         getProduct( ).setMetaDataFormat( getProperties().getProperty( SfrProperty.META_FILE_FORMAT.key ) );
         getProduct( ).setMetaDataArchiveFormat( getProperties().getProperty( SfrProperty.META_FILE_ARCHIVE_FORMAT.key ) );
     }
 
 
     public void done() {
     }
 }
