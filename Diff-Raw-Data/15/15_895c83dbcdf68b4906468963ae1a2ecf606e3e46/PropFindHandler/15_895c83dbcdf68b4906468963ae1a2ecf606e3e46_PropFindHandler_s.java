 package com.bradmcevoy.http;
 
 import java.io.ByteArrayOutputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 import com.bradmcevoy.http.Request.Method;
 import com.bradmcevoy.http.XmlWriter.Element;
 import com.bradmcevoy.io.StreamToStream;
 import java.io.ByteArrayInputStream;
 import java.net.URI;
 
 public class PropFindHandler extends ExistingEntityHandler {
 
     private Logger log = LoggerFactory.getLogger( PropFindHandler.class );
 
     private static final String CUSTOM_NS_PREFIX = "R";
 //    private Namespace nsWebDav = new Namespace();
     final Map<String, PropertyWriter> writersMap = new HashMap<String,PropertyWriter>();
 
 
     {
         add( new ContentLengthPropertyWriter() );
         add( new ContentTypePropertyWriter() );
         add( new CreationDatePropertyWriter() );
         add( new DisplayNamePropertyWriter() );
         add( new LastModifiedDatePropertyWriter() );
         add( new ResourceTypePropertyWriter() );
         add( new EtagPropertyWriter() );
 
         add( new SupportedLockPropertyWriter() );
         add( new LockDiscoveryPropertyWriter() );
 
         add( new MSHrefPropertyWriter() );
         add( new MSIsCollectionPropertyWriter() );
         add( new MSNamePropertyWriter() );
     }
 
     public PropFindHandler( HttpManager manager ) {
         super( manager );
     }
 
     private void add( PropertyWriter pw ) {
         writersMap.put( pw.fieldName(), pw );
     }
 
     @Override
     public Request.Method method() {
         return Method.PROPFIND;
     }
 
     @Override
     protected boolean isCompatible( Resource handler ) {
         return ( handler instanceof PropFindableResource );
     }
 
     @Override
     protected void process( HttpManager milton, Request request, Response response, Resource resource ) {
         PropFindableResource pfr = (PropFindableResource) resource;
         int depth = request.getDepthHeader();
         response.setStatus( Response.Status.SC_MULTI_STATUS );
         response.setContentTypeHeader( Response.XML );
         Set<String> requestedFields;
         try {
             requestedFields = getRequestedFields( request );
         } catch( IOException ex ) {
             throw new RuntimeException( ex );
         } catch( SAXException ex ) {
             throw new RuntimeException( ex );
         }
         String url = request.getAbsoluteUrl();
         process( url, pfr, depth, requestedFields, response );
     }
 
     protected String generateNamespaceDeclarations() {
 //            return " xmlns:" + nsWebDav.abbrev + "=\"" + nsWebDav.url + "\"";
         return " xmlns:D" + "=\"DAV:\"";
     }
 
 
     class XmlWriterPropertyConsumer implements PropertyConsumer {
 
         final XmlWriter writer;
 
         public XmlWriterPropertyConsumer( XmlWriter writer ) {
             this.writer = writer;
         }
 
         public void startResource( String href ) {
             writeProp( "D:href", href );
         }
 
         public Element open( String elementName ) {
             return writer.begin( elementName ).open();
         }
 
         void writeProp( String elementName, String value ) {
             writer.writeProperty( null, elementName, value );
         }
 
         public void consumeProperties( Set<PropertyWriter> knownProperties, Set<PropertyWriter> unknownProperties, String href, PropFindableResource resource, int depth ) {
             XmlWriter.Element el = writer.begin( "D:response" );
             if( resource instanceof CustomPropertyResource) {
                 CustomPropertyResource cpr = (CustomPropertyResource) resource;
                 el.writeAtt( "xmlns:" + CUSTOM_NS_PREFIX, cpr.getNameSpaceURI());
             }
             el.open();
             startResource( href );
             sendResponseProperties( resource, knownProperties, href, "HTTP/1.1 200 Ok" );
             sendResponseProperties( resource, unknownProperties, href, "HTTP/1.1 404 Not Found" );
             el.close();
         }
 
         void sendResponseProperties( PropFindableResource resource, Set<PropertyWriter> properties, String href, String status ) {
             if( !properties.isEmpty() ) {
                 XmlWriter.Element elPropStat = writer.begin( "D:propstat" ).open();
                 XmlWriter.Element elProp = writer.begin( "D:prop" ).open();
                 for( final PropertyWriter pw : properties ) {
                     pw.append( writer, resource, href );
                 }
                 elProp.close();
                 writeProp( "D:status", status );
                 elPropStat.close();
             }
         }
     }
 
     public void appendResponses( PropertyConsumer consumer, PropFindableResource resource, int requestedDepth, Set<String> requestedFields, String encodedCollectionUrl ) {
         log.debug( "appendresponses: fields size: " + requestedFields.size());
         try {
             String collectionHref = suffixSlash( encodedCollectionUrl );
             URI parentUri = new URI( collectionHref );
 
             collectionHref = parentUri.toASCIIString();
             sendResponse( consumer, resource, requestedFields, collectionHref, requestedDepth, 0, collectionHref );
 
         } catch( URISyntaxException ex ) {
             throw new RuntimeException( ex );
         }
     }
 
     void sendResponse( PropertyConsumer consumer, PropFindableResource resource, Set<String> requestedFields, String href, int requestedDepth, int currentDepth, String collectionHref ) {
 
         final Set<PropertyWriter> unknownProperties = new HashSet<PropertyWriter>();
         final Set<PropertyWriter> knownProperties = new HashSet<PropertyWriter>();
         if( resource instanceof CollectionResource ) {
             if( !href.endsWith( "/" ) ) {
                 href = href + "/";
             }
         }
         CustomPropertyResource cpr = null;
         if( resource instanceof CustomPropertyResource) {
             cpr = (CustomPropertyResource) resource;
         }
         for( String field : requestedFields ) {
             PropertyWriter pw = null;
             CustomProperty customProp = null;
             if( cpr != null ) customProp = cpr.getProperty( field);
             if( customProp == null ) pw = writersMap.get( field );
             if( customProp != null  ) {
                 PropertyWriter customPw = new CustomPropertyWriter( field, customProp );
                 knownProperties.add( customPw);
             } else if( pw != null ) {
                 knownProperties.add( pw );
             } else {
                 unknownProperties.add( new UnknownPropertyWriter( field ) );
             }
         }
 
         consumer.consumeProperties( knownProperties, unknownProperties, href, resource, currentDepth );
 
         if( requestedDepth > currentDepth && resource instanceof CollectionResource ) {
             CollectionResource col = (CollectionResource) resource;
             List<? extends Resource> list = col.getChildren();
             list = new ArrayList<Resource>( list );
             for( Resource child : list ) {
                 if( child instanceof PropFindableResource ) {
                     String childHref = collectionHref + Utils.percentEncode( child.getName() );
                     sendResponse( consumer, (PropFindableResource) child, requestedFields, childHref, requestedDepth, currentDepth+1, href+col.getName() );
                 }
             }
         }
 
     }
 
     private void process( String url, PropFindableResource pfr, int depth, Set<String> requestedFields, Response response ) {
         try {
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             XmlWriter writer = new XmlWriter( out );
             PropertyConsumer consumer = new XmlWriterPropertyConsumer( writer );
             writer.writeXMLHeader();
             writer.open( "D:multistatus" + generateNamespaceDeclarations() );
             writer.newLine();
             appendResponses( consumer, pfr, depth, requestedFields, url );
             writer.close( "D:multistatus" );
             writer.flush();
//            log.debug( out.toString() );
            response.getOutputStream().write( out.toByteArray() ); // note: this can and should write to the outputstream directory. but if it aint broke, dont fix it...
         } catch( IOException ex ) {
             log.warn( "ioexception sending output", ex );
         }
     }
 
     private String suffixSlash( String s ) {
         if( !s.endsWith( "/" ) ) {
             s = s + "/";
         }
         return s;
     }
 
     private String nameEncode( String s ) {
         //return Utils.encode(href, false); // see MIL-31
         return Utils.escapeXml( s );
     //return href.replaceAll("&", "&amp;");  // http://www.ettrema.com:8080/browse/MIL-24
     }
 
     protected void sendStringProp( XmlWriter writer, String name, String value ) {
         String s = value;
         if( s == null ) {
             writer.writeProperty( null, name );
         } else {
             writer.writeProperty( null, name, s );
         }
     }
 
     void sendDateProp( XmlWriter writer, String name, Date date ) {
         sendStringProp( writer, name, ( date == null ? null : DateUtils.formatDate( date ) ) );
     }
 
     protected boolean isFolder( PropFindableResource resource ) {
         return ( resource instanceof CollectionResource );
     }
 
 
     private Set<String> getRequestedFields( Request request ) throws IOException, SAXException, FileNotFoundException {
         final Set<String> set = new LinkedHashSet<String>();
         InputStream in = request.getInputStream();
         ByteArrayOutputStream bout = new ByteArrayOutputStream();
         StreamToStream.readTo( in, bout, false, true );
         byte[] arr = bout.toByteArray();
         if( arr.length > 1 ) {
             ByteArrayInputStream bin = new ByteArrayInputStream( arr );
             XMLReader reader = XMLReaderFactory.createXMLReader();
             PropFindSaxHandler handler = new PropFindSaxHandler();
             reader.setContentHandler( handler );
             try {
                 reader.parse( new InputSource( bin ) );
                 set.addAll( handler.getAttributes().keySet() );
             } catch( IOException e ) {
                 log.warn( "exception reading requested fields", e);
             } catch( SAXException e ) {
                 log.warn( "exception reading requested fields", e);
             }
         }
 
         if( set.size() == 0 ) {
             set.add( "creationdate" );
             set.add( "getlastmodified" );
             set.add( "displayname" );
             set.add( "resourcetype" );
             set.add( "getcontenttype" );
             set.add( "getcontentlength" );
             set.add( "getetag" );
         }
         return set;
     }
 
     class DisplayNamePropertyWriter implements PropertyWriter<String> {
 
         public void append( XmlWriter writer, PropFindableResource res, String href ) {
            sendStringProp( writer, "D:" + fieldName(), nameEncode( getValue( res,href ) ) );
         }
 
         public String getValue( PropFindableResource res, String href ) {
             return res.getName();
         }
 
         public String fieldName() {
             return "displayname";
         }
     }
 
     class LastModifiedDatePropertyWriter implements PropertyWriter<Date> {
 
         public void append( XmlWriter xmlWriter, PropFindableResource res, String href ) {
             //sendDateProp(xmlWriter, "D:" + fieldName(), res.getModifiedDate());
             Date dt = res.getModifiedDate();
             String f;
             if( dt == null ) {
                 f = "";
             } else {
                 f = DateUtils.formatForWebDavModifiedDate( res.getModifiedDate() );
             }
             sendStringProp( xmlWriter, "D:" + fieldName(), f );
         }
 
         public String fieldName() {
             return "getlastmodified";
         }
 
         public Date getValue( PropFindableResource res, String href ) {
             return res.getModifiedDate();
         }
     }
 
     class CreationDatePropertyWriter implements PropertyWriter<Date> {
 
         public void append( XmlWriter xmlWriter, PropFindableResource res, String href ) {
             sendDateProp( xmlWriter, "D:" + fieldName(), getValue( res,href ) );
         }
 
         public Date getValue( PropFindableResource res, String href ) {
             return res.getCreateDate();
         }
 
         public String fieldName() {
             return "creationdate";
         }
     }
 
     class ResourceTypePropertyWriter implements PropertyWriter<Boolean> {
 
         public void append( XmlWriter writer, PropFindableResource resource, String href ) {
             String rt = getValue( resource,href ) ? "<D:collection/>" : "";
             sendStringProp( writer, "D:resourcetype", rt );
         }
 
         public Boolean getValue( PropFindableResource res, String href ) {
             return isFolder( res );
         }
 
         public String fieldName() {
             return "resourcetype";
         }
     }
 
     class ContentTypePropertyWriter implements PropertyWriter<String> {
 
         public void append( XmlWriter xmlWriter, PropFindableResource res, String href ) {
             String ct = getValue( res,href );
             sendStringProp( xmlWriter, "D:" + fieldName(), ct );
         }
 
         public String getValue( PropFindableResource res, String href ) {
             if( res instanceof GetableResource ) {
                 GetableResource getable = (GetableResource) res;
                 return getable.getContentType( null );
             } else {
                 return "";
             }
         }
 
         public String fieldName() {
             return "getcontenttype";
         }
     }
 
     class ContentLengthPropertyWriter implements PropertyWriter<Long> {
 
         public void append( XmlWriter xmlWriter, PropFindableResource res, String href ) {
             Long ll = getValue( res,href );
             sendStringProp( xmlWriter, "D:" + fieldName(), ll == null ? "" : ll.toString() );
         }
 
         public Long getValue( PropFindableResource res, String href ) {
             if( res instanceof GetableResource ) {
                 GetableResource getable = (GetableResource) res;
                 Long l = getable.getContentLength();
                 return l;
             } else {
                 return null;
             }
         }
 
         public String fieldName() {
             return "getcontentlength";
         }
     }
 
     class EtagPropertyWriter implements PropertyWriter<String> {
 
         public void append( XmlWriter writer, PropFindableResource resource, String href ) {
             String etag = getValue( resource,href );
             if( etag != null ) {
                 sendStringProp( writer, "D:getetag", etag );
             }
         }
 
         public String getValue( PropFindableResource res, String href ) {
             String etag = DefaultResponseHandler.generateEtag( res );
             return etag;
         }
 
         public String fieldName() {
             return "getetag";
         }
     }
 
 //    <D:supportedlock/><D:lockdiscovery/>
     class LockDiscoveryPropertyWriter implements PropertyWriter<LockToken> {
 
         public void append( XmlWriter writer, PropFindableResource resource, String href ) {
             LockToken token = getValue( resource, href );
             if( token == null ) return;
             Element lockentry = writer.begin( "D:lockdiscovery" ).open();
             if( token != null ) {
                 LockInfo info = token.info;
                 writer.begin( "D:lockscope" ).open().writeText( "<D:" + info.scope.name().toLowerCase() + "/>" ).close();
                 writer.begin( "D:locktype" ).open().writeText( "<D:" + info.type.name().toLowerCase() + "/>" ).close();
                 writer.begin( "D:depth" ).open().writeText( "0" ).close();
                 writer.begin( "D:owner" ).open().writeText( info.owner ).close();
                 writer.begin( "D:timeout" ).open().writeText( token.timeout.toString() ).close();
 
                 Element elToken = writer.begin( "D:locktoken" ).open();
                 writer.begin( "D:href" ).open().writeText( "urn:uuid:" + token.tokenId ).close();
                 writer.begin( "D:lockroot" ).open().writeText( href ).close();
                 elToken.close();
             }
             lockentry.close();
         }
 
         public LockToken getValue( PropFindableResource res, String href ) {
             if( !( res instanceof LockableResource ) ) return null;
             LockableResource lr = (LockableResource) res;
             LockToken token = lr.getCurrentLock();
             return token;
         }
 
         public String fieldName() {
             return "supportedlock";
         }
     }
 
     class SupportedLockPropertyWriter implements PropertyWriter<Object> {
 
         public void append( XmlWriter writer, PropFindableResource resource, String href ) {
             if( resource instanceof LockableResource ) {
                 Element lockentry = writer.begin( "lockentry" ).open();
                 writer.begin( "lockscope" ).open().writeText( "<D:exclusive/>" ).close();
                 writer.begin( "locktype" ).open().writeText( "<D:write/>" ).close();
                 lockentry.close();
             }
         }
 
         public Object getValue( PropFindableResource res, String href ) {
             return null;
         }
 
         public String fieldName() {
             return "supportedlock";
         }
     }
 
     // MS specific fields
     class MSNamePropertyWriter extends DisplayNamePropertyWriter {
 
         @Override
         public String fieldName() {
             return "name";
         }
     }
 
     class MSHrefPropertyWriter implements PropertyWriter<String> {
 
         public void append( XmlWriter writer, PropFindableResource res, String href ) {
             sendStringProp( writer, "D:" + fieldName(), href );
         }
 
         public String getValue( PropFindableResource res, String href ) {
             return href;
         }
 
         public String fieldName() {
             return "href";
         }
     }
 
 
     class MSIsCollectionPropertyWriter extends ResourceTypePropertyWriter {
 
         @Override
         public void append( XmlWriter writer, PropFindableResource res, String href ) {
             String s = getValue( res,href ) ? "true" : "false";
             sendStringProp( writer, "D:" + fieldName(), s );
         }
 
         @Override
         public String fieldName() {
             return "iscollection";
         }
     }
 
     class UnknownPropertyWriter implements PropertyWriter<String> {
 
         final String name;
 
         public UnknownPropertyWriter( String name ) {
             this.name = name;
         }
 
         public void append( XmlWriter writer, PropFindableResource res, String href ) {
             sendStringProp( writer, "D:" + fieldName(), null );
         }
 
         public String getValue( PropFindableResource res, String href ) {
             return null;
         }
 
         public String fieldName() {
             return name;
         }
     }
 
     class CustomPropertyWriter implements PropertyWriter<Object> {
         final String fieldName;
         final CustomProperty prop;
 
         public CustomPropertyWriter( String fieldName, CustomProperty prop ) {
             this.fieldName = fieldName;
             this.prop = prop;
         }
 
         public String fieldName() {
             return fieldName;
         }
 
         public void append( XmlWriter writer, PropFindableResource res, String href ) {
             String s = prop.getFormattedValue();
             s = Utils.escapeXml( s );
             //s = Utils.percentEncode(s);
             sendStringProp( writer, CUSTOM_NS_PREFIX + ":" + fieldName(), s );
         }
 
         public Object getValue( PropFindableResource res, String href ) {
             return prop.getTypedValue();
         }
     }
 }
