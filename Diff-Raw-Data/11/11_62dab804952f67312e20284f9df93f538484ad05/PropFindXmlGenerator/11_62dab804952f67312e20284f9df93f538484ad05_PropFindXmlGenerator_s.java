 package com.bradmcevoy.http.webdav;
 
 import com.bradmcevoy.http.XmlWriter;
 import com.bradmcevoy.http.values.ValueAndType;
 import com.bradmcevoy.http.values.ValueWriters;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.xml.namespace.QName;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author brad
  */
 public class PropFindXmlGenerator {
 
     private static final Logger log = LoggerFactory.getLogger( PropFindXmlGenerator.class );
     private final Helper helper;
     private final ValueWriters valueWriters;
 
     public PropFindXmlGenerator( ValueWriters valueWriters ) {
         helper = new Helper();
         this.valueWriters = valueWriters;
     }
 
     PropFindXmlGenerator( Helper helper, ValueWriters valueWriters ) {
         this.helper = helper;
         this.valueWriters = valueWriters;
     }
 
     public String generate( List<PropFindResponse> propFindResponses ) {
         ByteArrayOutputStream responseOutput = new ByteArrayOutputStream();
         Map<String, String> mapOfNamespaces = helper.findNameSpaces( propFindResponses );
         ByteArrayOutputStream generatedXml = new ByteArrayOutputStream();
         XmlWriter writer = new XmlWriter( generatedXml );
         writer.writeXMLHeader();
         writer.open( "D:multistatus" + helper.generateNamespaceDeclarations( mapOfNamespaces ) );
         writer.newLine();
         helper.appendResponses( writer, propFindResponses, mapOfNamespaces );
         writer.close( "D:multistatus" );
         writer.flush();
 //        log.debug( generatedXml.toString() );
         helper.write( generatedXml, responseOutput );
         try {
             return responseOutput.toString( "UTF-8" );
         } catch( UnsupportedEncodingException ex ) {
             throw new RuntimeException( ex );
         }
     }
 
     class Helper {
 
         /**
          *
          * @param propFindResponses
          * @return - map where key is the uri, and value is the prefix
          */
         Map<String, String> findNameSpaces( List<PropFindResponse> propFindResponses ) {
             int i = 1;
             Map<String, String> map = new HashMap<String, String>();
             for( PropFindResponse r : propFindResponses ) {
                 for( QName p : r.getKnownProperties().keySet() ) {
                     String uri = p.getNamespaceURI();
 //                    if( uri.endsWith( ":" ) ) uri = uri.substring( 0, uri.length() - 1 ); // strip trailing :
                     if( !map.containsKey( uri ) ) {
                        if( uri.equals( WebDavProtocol.NS_DAV ) ) {
                            map.put( uri, "D" );
                        } else {
                            map.put( uri, "ns" + i++ );
                        }
                     }
                 }
             }
             return map;
         }
 
         String generateNamespaceDeclarations( Map<String, String> mapOfNamespaces ) {
             String decs = "";
             for( String uri : mapOfNamespaces.keySet() ) {
                 String prefix = mapOfNamespaces.get( uri );
                 decs += " xmlns:" + prefix + "=\"" + uri + "\"";
             }
             return decs;
         }
 
         void appendResponses( XmlWriter writer, List<PropFindResponse> propFindResponses, Map<String, String> mapOfNamespaces ) {
 //            log.debug( "appendResponses: " + propFindResponses.size() );
             for( PropFindResponse r : propFindResponses ) {
                 XmlWriter.Element el = writer.begin( "D:response" );
                 el.open();
                 writer.writeProperty( "D", "href", r.getHref() );
                 sendKnownProperties( writer, mapOfNamespaces, r.getKnownProperties(), r.getHref() );
                 sendUnknownProperties( writer, mapOfNamespaces, r.getUnknownProperties() );
                 el.close();
             }
         }
 
         private void sendKnownProperties( XmlWriter writer, Map<String, String> mapOfNamespaces, Map<QName, ValueAndType> properties, String href ) {
 //            log.debug( "sendKnownProperties: " + properties.size() );
             if( !properties.isEmpty() ) {
                 XmlWriter.Element elPropStat = writer.begin( "D:propstat" ).open();
                 XmlWriter.Element elProp = writer.begin( "D:prop" ).open();
                 for( QName qname : properties.keySet() ) {
                     String prefix = mapOfNamespaces.get( qname.getNamespaceURI() );
                     ValueAndType val = properties.get( qname );
                     valueWriters.writeValue( writer, qname, prefix, val, href, mapOfNamespaces );
                 }
                 elProp.close();
                 writer.writeProperty( "D", "status", "HTTP/1.1 200 OK" );
                 elPropStat.close();
             }
         }
 
         private void sendUnknownProperties( XmlWriter writer, Map<String, String> mapOfNamespaces, List<QName> properties ) {
 //            log.debug( "sendUnknownProperties: " + properties.size() );
             if( !properties.isEmpty() ) {
                 XmlWriter.Element elPropStat = writer.begin( "D:propstat" ).open();
                 XmlWriter.Element elProp = writer.begin( "D:prop" ).open();
                 for( QName qname : properties ) {
                     String prefix = mapOfNamespaces.get( qname.getNamespaceURI() );
                     writer.writeProperty( prefix, qname.getLocalPart() );
                 }
                 elProp.close();
                 writer.writeProperty( "D", "status", "HTTP/1.1 404 Not Found" );
                 elPropStat.close();
             }
         }
 
         void write( ByteArrayOutputStream out, OutputStream outputStream ) {
             try {
                 String xml = out.toString( "UTF-8" );
                 outputStream.write( xml.getBytes() ); // note: this can and should write to the outputstream directory. but if it aint broke, dont fix it...
             } catch( UnsupportedEncodingException ex ) {
                 throw new RuntimeException( ex );
             } catch( IOException ex ) {
                 throw new RuntimeException( ex );
             }
         }
     }
 }
