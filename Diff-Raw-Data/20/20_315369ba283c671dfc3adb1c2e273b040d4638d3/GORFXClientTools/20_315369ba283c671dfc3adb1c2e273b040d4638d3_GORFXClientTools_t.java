 package de.zib.gndms.GORFX.common;
 
import de.zib.gndms.model.gorfx.types.GORFXConstantURIs;
 import org.apache.axis.description.ElementDesc;
import org.apache.axis.description.FieldDesc;
import org.apache.axis.message.MessageElement;
import org.apache.axis.types.NormalizedString;
import org.apache.axis.types.URI;
 import types.*;
 
 import javax.xml.namespace.QName;
 import javax.xml.soap.SOAPException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.concurrent.ConcurrentHashMap;
 
 
 /**
  * @author: Maik Jorra <jorra@zib.de>
  * @version: $Id$
  * <p/>
  * User: mjorra, Date: 27.08.2008, Time: 13:50:52
  *
  * If one introduces new ORQT types or changes an element type of an ORQT to an type which isn't listed in the
  * TypeMap. an new entry must be added to that map, consisting of the QName of that type an a matching class.
  */
 public class GORFXClientTools {
 
     private static ConcurrentHashMap<String, URI> UriTable = new ConcurrentHashMap<String, URI>( );
     private static final HashMap<QName, Class> TypeMap = new HashMap<QName, Class>( );
 
     static {
         TypeMap.put( new QName( "http://www.w3.org/2001/XMLSchema", "anyURI" ), URI.class );
         TypeMap.put( new QName( "http://proptest.zib.de/some/types", "DataDescriptorT" ), DataDescriptorT.class );
         TypeMap.put( new QName( "http://www.w3.org/2001/XMLSchema", "normalizedString" ), NormalizedString.class );
         TypeMap.put( new QName( "http://gndms.zib.de/common/types", "FileMappingSeqT"), FileMappingSeqT.class );
         TypeMap.put( new QName( "http://gndms.zib.de/common/types", "SliceReferenceT"), SliceReferenceT.class );
         TypeMap.put( new QName( "http://lofis.gndms.zib.de/LOFIS/LofiSet/types", ">LofiSetReference"), LofiSetReference.class );
         TypeMap.put( new QName( "http://gndms.zib.de/c3grid/types", ">PinORQT>Target"), PinORQTTarget.class );
         TypeMap.put( new QName( "http://dspace.gndms.zib.de/DSpace/Slice/types", ">SliceReference"), SliceReference.class );
     }
 
     // helpers for provider stage in
     
     public static URI getProviderStageInURI( ) {
 
         return getURI( GORFXConstantURIs.PROVIDER_STAGE_IN_URI );
     }
 
 
     public static ProviderStageInORQT createEmptyProviderStageInORQT ( ) throws SOAPException, IllegalAccessException, InstantiationException {
 
         ProviderStageInORQT orq = new ProviderStageInORQT();
         orq.setOfferType( getProviderStageInURI() );
 
         orq.set_any( getMessageElementsForFieldDescs( ProviderStageInORQT.getTypeDesc().getFields() ) );
 
         return orq;
     }
 
     
     // helpers for slice stage in
 
     public static URI getSliceStageInURI ( ) {
 
         return getURI( GORFXConstantURIs.SLICE_STAGE_IN_URI );
     }
 
 
     public static SliceStageInORQT createEmptySliceStageInORQT ( ) throws SOAPException, IllegalAccessException, InstantiationException {
 
         SliceStageInORQT orq = new SliceStageInORQT();
         orq.setOfferType( getProviderStageInURI() );
 
         orq.set_any( getMessageElementsForFieldDescs( SliceStageInORQT.getTypeDesc().getFields() ) );
 
         return orq;
     }
 
 
     public static URI getInterSliceTransferURI( ) {
 
         return getURI( GORFXConstantURIs.INTER_SLICE_TRANSFER_URI );
     }
     
 
     public static InterSliceTransferORQT createEmptyInterSliceTransferORQT( ) throws SOAPException, IllegalAccessException, InstantiationException {
 
         InterSliceTransferORQT orq = new InterSliceTransferORQT();
         orq.setOfferType( getProviderStageInURI() );
 
         orq.set_any( getMessageElementsForFieldDescs( InterSliceTransferORQT.getTypeDesc().getFields() ) );
 
         return orq;
     }
 
     
     public static URI getRePublishSliceURI( ) {
 
         return getURI( GORFXConstantURIs.RE_PUBLISH_SLICE_URI );
     }
 
 
     public static RePublishSliceORQT createEmptyRePublishSliceORQT( ) throws SOAPException, IllegalAccessException, InstantiationException {
 
         RePublishSliceORQT orq = new RePublishSliceORQT();
         orq.setOfferType( getProviderStageInURI() );
 
         orq.set_any( getMessageElementsForFieldDescs( RePublishSliceORQT.getTypeDesc().getFields() ) );
 
         return orq;
     }
 
 
     public static URI getFileTransferURI( ) {
 
         return getURI( GORFXConstantURIs.FILE_TRANSFER_URI );
     }
     
 
     public static FileTransferORQT createEmptyFileTransferORQT( ) throws SOAPException, IllegalAccessException, InstantiationException {
 
         FileTransferORQT orq = new FileTransferORQT();
         orq.setOfferType( getProviderStageInURI() );
 
         orq.set_any( getMessageElementsForFieldDescs( FileTransferORQT.getTypeDesc().getFields() ) );
 
         return orq;
     }
 
 
     public static URI getLofisSetStageInURI ( ) {
 
         return getURI( GORFXConstantURIs.LOFI_SET_STAGE_IN_URI );
     }
 
 
     public static LofiSetStageInORQT createEmptyLofisSetStageInORQT ( ) throws SOAPException, IllegalAccessException, InstantiationException {
 
         LofiSetStageInORQT orq = new LofiSetStageInORQT();
         orq.setOfferType( getProviderStageInURI() );
 
         orq.set_any( getMessageElementsForFieldDescs( LofiSetStageInORQT.getTypeDesc().getFields() ) );
 
         return orq;
     }
 
 
     public static URI getPinURI ( ) {
 
         return getURI( GORFXConstantURIs.PIN_URI );
     }
 
 
     public static PinORQT createEmptyPinORQT ( ) throws SOAPException, IllegalAccessException, InstantiationException {
 
         PinORQT orq = new PinORQT();
         orq.setOfferType( getProviderStageInURI() );
 
         orq.set_any( getMessageElementsForFieldDescs( PinORQT.getTypeDesc().getFields() ) );
 
         return orq;
     }
 
 
     public static URI getRePublishLofiSetURI( ) {
 
         return getURI( GORFXConstantURIs.RE_PUBLISH_LOFI_SET_URI );
     }
 
 
     public static RePublishLofiSetORQT createEmptyRePublishLofiSetORQT( ) throws SOAPException, IllegalAccessException, InstantiationException {
 
         RePublishLofiSetORQT orq = new RePublishLofiSetORQT();
         orq.setOfferType( getProviderStageInURI() );
 
         orq.set_any( getMessageElementsForFieldDescs( RePublishLofiSetORQT.getTypeDesc().getFields() ) );
 
         return orq;
     }
 
 
     public static MessageElement[] getMessageElementsForFieldDescs( FieldDesc[] fds ) throws SOAPException, InstantiationException, IllegalAccessException {
 
         // prune offerType
         ArrayList<MessageElement> mes = new ArrayList( fds.length - 1 );
         for( FieldDesc fd: fds ) {
 
             if( fd instanceof ElementDesc ) {
                 ElementDesc ed = (ElementDesc) fd;
 
                 for( int i=0; i < ed.getMinOccurs(); ++i )
                     mes.add( getMessageElementForFieldDesc( ed ) );
             }
         }
 
         return mes.toArray( new MessageElement[0] );
     }
 
 
     public static MessageElement getMessageElementForFieldDesc( FieldDesc fd ) throws SOAPException, IllegalAccessException, InstantiationException {
 
         if( fd == null )
             throw new IllegalStateException( "null field desc received" );
 
         return createElementForField( fd, TypeMap.get( fd.getXmlType() ).newInstance() );
     }
 
 
     public static MessageElement createElementForField( FieldDesc fd, Object val ) throws SOAPException {
 
         if( val == null )
             throw new IllegalStateException( "no value received" );
 
         MessageElement me =  new MessageElement( fd.getXmlName( ) );
         me.setType( fd.getXmlType( ) );
         me.setObjectValue( val );
 
         return me;
     }
 
 
     private static URI getURI( String key ) {
 
         if(! UriTable.containsKey( key ) ) {
             try {
                 UriTable.put( key, new URI( key ) );
             } catch (URI.MalformedURIException e) {
                 // this cannot happen
                 e.printStackTrace();
             }
         }
         return UriTable.get( key );
     }
 
     // just for debuging
     public static void showMES( MessageElement[] mes ) {
 
         if( mes != null ) {
             System.out.println( "mes.length: " + mes.length );
             for( int i=0; i < mes.length; ++i ) {
                 MessageElement me = mes[i];
                 if ( me != null ) {
                     System.out.println( i+ ". " + me.getName( )+": " + me.getType( ) );
                     System.out.println( "toString: " + me.toString() );
                 } else
                     System.out.println( i+": me is null" );
             }
         } else
             System.out.println( "no elements received" );
     }
 
 }
