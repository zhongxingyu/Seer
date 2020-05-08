 package org.mosaic.util.xml.impl;
 
 import com.google.common.reflect.TypeToken;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardOpenOption;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import javax.xml.XMLConstants;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.validation.Schema;
 import javax.xml.xpath.*;
 import org.mosaic.lifecycle.impl.util.ServiceUtils;
 import org.mosaic.util.convert.ConversionService;
 import org.mosaic.util.xml.StrictErrorHandler;
 import org.mosaic.util.xml.XmlDocument;
 import org.mosaic.util.xml.XmlElement;
 import org.mosaic.util.xml.XmlParser;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceRegistration;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.InitializingBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import static java.util.Collections.emptyList;
 
 /**
  * @author arik
  */
 public class XmlParserImpl implements XmlParser, InitializingBean, DisposableBean
 {
     @Nonnull
     private static final String USER_DATA_XMLELEMENT_KEY = XmlDocumentImpl.XmlElementImpl.class.getName();
 
     @Nullable
     private final BundleContext bundleContext;
 
     @Nonnull
     private final ConversionService conversionService;
 
     @Nullable
     private ServiceRegistration<XmlParser> serviceRegistration;
 
     public XmlParserImpl( @Nullable BundleContext bundleContext, @Nonnull ConversionService conversionService )
     {
         this.bundleContext = bundleContext;
         this.conversionService = conversionService;
     }
 
     @Override
     public void afterPropertiesSet() throws Exception
     {
         if( this.bundleContext != null )
         {
             this.serviceRegistration = ServiceUtils.register( this.bundleContext, XmlParser.class, this );
         }
     }
 
     @Override
     public void destroy() throws Exception
     {
         if( this.bundleContext != null && this.serviceRegistration != null )
         {
             this.serviceRegistration = ServiceUtils.unregister( this.serviceRegistration );
         }
     }
 
     @Nonnull
     @Override
     public XmlDocument parse( @Nonnull Path file ) throws ParserConfigurationException, IOException, SAXException
     {
         try( InputStream is = Files.newInputStream( file, StandardOpenOption.READ ) )
         {
             return parse( is, file.toUri().toString() );
         }
     }
 
     @Nonnull
     @Override
     public XmlDocument parse( @Nonnull Path file, @Nonnull Schema schema )
             throws ParserConfigurationException, IOException, SAXException
     {
         try( InputStream is = Files.newInputStream( file, StandardOpenOption.READ ) )
         {
             return parse( is, file.toUri().toString(), schema );
         }
     }
 
     @Nonnull
     @Override
     public XmlDocument parse( @Nonnull InputStream is,
                               @Nonnull String systemId ) throws ParserConfigurationException, IOException, SAXException
     {
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         dbf.setNamespaceAware( true );
 
         DocumentBuilder db = dbf.newDocumentBuilder();
         db.setErrorHandler( StrictErrorHandler.INSTANCE );
         return new XmlDocumentImpl( db.parse( is, systemId ) );
     }
 
     @Nonnull
     @Override
     public XmlDocument parse( @Nonnull InputStream is,
                               @Nonnull String systemId,
                               @Nonnull Schema schema ) throws ParserConfigurationException, IOException, SAXException
     {
         DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
         dbf.setNamespaceAware( true );
         dbf.setSchema( schema );
 
         DocumentBuilder db = dbf.newDocumentBuilder();
         db.setErrorHandler( StrictErrorHandler.INSTANCE );
         return new XmlDocumentImpl( db.parse( is, systemId ) );
     }
 
     private class XmlDocumentImpl implements XmlDocument, NamespaceContext
     {
         @Nonnull
         private final Map<String, String> namespaces = new ConcurrentHashMap<>( 5 );
 
         @Nonnull
         private final Document document;
 
         @Nonnull
         private final XmlElement rootElement;
 
         private XmlDocumentImpl( @Nonnull Document document )
         {
             this.namespaces.put( XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI );
             this.namespaces.put( XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI );
             this.document = document;
             this.rootElement = new XmlElementImpl( this.document.getDocumentElement() );
         }
 
         @Override
         public void addNamespace( @Nonnull String prefix, @Nonnull String uri )
         {
             this.namespaces.put( prefix, uri );
         }
 
         @Nonnull
         @Override
         public XmlElement getRoot()
         {
             return this.rootElement;
         }
 
         @Override
         public String getNamespaceURI( String prefix )
         {
             if( prefix == null )
             {
                 throw new NullPointerException();
             }
 
             String uri = this.namespaces.get( prefix );
             if( uri != null )
             {
                 return uri;
             }
             else
             {
                 return XMLConstants.NULL_NS_URI;
             }
         }
 
         @Override
         public String getPrefix( String namespaceURI )
         {
             throw new UnsupportedOperationException();
         }
 
         @Override
         public Iterator getPrefixes( String namespaceURI )
         {
             throw new UnsupportedOperationException();
         }
 
         private XPathExpression getXPathExpression( String xpath ) throws XPathException
         {
             XPathFactory xpf = XPathFactory.newInstance();
             XPath xp = xpf.newXPath();
             xp.setNamespaceContext( this );
             return xp.compile( xpath );
         }
 
         public class XmlElementImpl implements XmlElement
         {
             @Nonnull
             private final Element element;
 
             public XmlElementImpl( @Nonnull Element element )
             {
                 this.element = element;
                 this.element.setUserData( USER_DATA_XMLELEMENT_KEY, this, null );
             }
 
             @Override
             @Nullable
             public String getValue()
             {
                 return this.element.getTextContent();
             }
 
             @Nonnull
             @Override
             public String requireValue()
             {
                 String value = getValue();
                 if( value == null )
                 {
                     throw new IllegalStateException( "Element '" + this + "' does not have a value" );
                 }
                 else
                 {
                     return value;
                 }
             }
 
             @Override
             @Nullable
             public <T> T getValue( @Nonnull TypeToken<T> type )
             {
                 String value = getValue();
                 return value == null ? null : XmlParserImpl.this.conversionService.convert( value, type );
             }
 
             @Override
             @Nullable
             public String getAttribute( @Nonnull String name )
             {
                return this.element.getAttribute( name );
             }
 
             @Override
             @Nullable
             public <T> T getAttribute( @Nonnull String name, @Nonnull TypeToken<T> type )
             {
                 String value = getAttribute( name );
                 return value == null ? null : XmlParserImpl.this.conversionService.convert( value, type );
             }
 
             @Nonnull
             @Override
             public String requireAttribute( @Nonnull String name )
             {
                 String value = getAttribute( name );
                 if( value == null )
                 {
                     throw new IllegalStateException( "Element '" + this + "' does not have the '" + name + "' attribute" );
                 }
                 else
                 {
                     return value;
                 }
             }
 
             @Nonnull
             @Override
             public <T> T requireAttribute( @Nonnull String name, @Nonnull TypeToken<T> type )
             {
                 return XmlParserImpl.this.conversionService.convert( requireAttribute( name ), type );
             }
 
             @Nonnull
             @Override
             public <T> T requireAttribute( @Nonnull String name, @Nonnull TypeToken<T> type, @Nonnull T defaultValue )
             {
                 String value = getAttribute( name );
                 return value == null ? defaultValue : XmlParserImpl.this.conversionService.convert( value, type );
             }
 
             @Override
             @Nullable
             public <T> T find( @Nonnull String xpath, @Nonnull TypeToken<T> type ) throws XPathException
             {
                 XPathExpression expr = getXPathExpression( xpath );
 
                String text = ( String ) expr.evaluate( this.element, XPathConstants.STRING );
                return text == null ? null : XmlParserImpl.this.conversionService.convert( text, type );
             }
 
             @Nonnull
             @Override
             public <T> T find( @Nonnull String xpath, @Nonnull TypeToken<T> type, @Nonnull T defaultValue )
                     throws XPathException
             {
                 T value = find( xpath, type );
                 return value != null ? value : defaultValue;
             }
 
             @Override
             @Nullable
             public XmlElement getFirstChildElement()
             {
                 NodeList nodes = this.element.getChildNodes();
                 for( int i = 0; i < nodes.getLength(); i++ )
                 {
                     Node node = nodes.item( i );
                     if( node instanceof Element )
                     {
                         return getXmlElementFromNode( node );
                     }
                 }
                 return null;
             }
 
             @Override
             @Nullable
             public XmlElement getFirstChildElement( @Nonnull String name )
             {
                 NodeList nodes = this.element.getChildNodes();
                 for( int i = 0; i < nodes.getLength(); i++ )
                 {
                     Node node = nodes.item( i );
                     if( node instanceof Element )
                     {
                         Element element = ( Element ) node;
                         if( element.getLocalName().equals( name ) )
                         {
                             return getXmlElementFromNode( node );
                         }
                     }
                 }
                 return null;
             }
 
             @Override
             @Nonnull
             public List<XmlElement> getChildElements()
             {
                 return getXmlElementsFromNodeList( this.element.getChildNodes() );
             }
 
             @Override
             @Nonnull
             public List<XmlElement> getChildElements( @Nonnull String name )
             {
                 NodeList nodes = this.element.getChildNodes();
                 List<XmlElement> children = null;
                 for( int i = 0; i < nodes.getLength(); i++ )
                 {
                     Node node = nodes.item( i );
                     if( node instanceof Element )
                     {
                         Element element = ( Element ) node;
                         if( element.getLocalName().equals( name ) )
                         {
                             if( children == null )
                             {
                                 children = new LinkedList<>();
                             }
                             children.add( getXmlElementFromNode( node ) );
                         }
                     }
                 }
                 return children == null ? Collections.<XmlElement>emptyList() : Collections.unmodifiableList( children );
             }
 
             @Override
             @Nonnull
             public List<String> findTexts( @Nonnull String xpath ) throws XPathException
             {
                 XPathExpression expr = getXPathExpression( xpath );
                 NodeList nodes = ( NodeList ) expr.evaluate( this.element, XPathConstants.NODESET );
                 if( nodes.getLength() > 0 )
                 {
                     List<String> values = null;
                     for( int i = 0; i < nodes.getLength(); i++ )
                     {
                         Node node = nodes.item( i );
                         String textContent = node.getTextContent();
                         if( textContent != null )
                         {
                             textContent = textContent.trim();
                             if( !textContent.isEmpty() )
                             {
                                 if( values == null )
                                 {
                                     values = new LinkedList<>();
                                 }
                                 values.add( textContent );
                             }
                         }
                     }
                     return values == null ? Collections.<String>emptyList() : values;
                 }
                 else
                 {
                     return emptyList();
                 }
             }
 
             @Override
             @Nullable
             public XmlElement findFirstElement( @Nonnull String xpath ) throws XPathException
             {
                 XPathExpression expr = getXPathExpression( xpath );
                 NodeList nodes = ( NodeList ) expr.evaluate( this.element, XPathConstants.NODESET );
                 for( int i = 0; i < nodes.getLength(); i++ )
                 {
                     Node node = nodes.item( i );
                     if( node instanceof Element )
                     {
                         return getXmlElementFromNode( node );
                     }
                 }
                 return null;
             }
 
             @Override
             @Nonnull
             public List<XmlElement> findElements( @Nonnull String xpath ) throws XPathException
             {
                 XPathExpression expr = getXPathExpression( xpath );
                 NodeList nodes = ( NodeList ) expr.evaluate( this.element, XPathConstants.NODESET );
                 return getXmlElementsFromNodeList( nodes );
             }
 
             private List<XmlElement> getXmlElementsFromNodeList( @Nonnull NodeList nodeList )
             {
                 List<XmlElement> children = null;
                 for( int i = 0; i < nodeList.getLength(); i++ )
                 {
                     Node node = nodeList.item( i );
                     if( node instanceof Element )
                     {
                         if( children == null )
                         {
                             children = new LinkedList<>();
                         }
                         children.add( getXmlElementFromNode( node ) );
                     }
                 }
                 return children == null ? Collections.<XmlElement>emptyList() : Collections.unmodifiableList( children );
             }
 
             @Nonnull
             private XmlElement getXmlElementFromNode( @Nonnull Node node )
             {
                 XmlElementImpl xmlElement = ( XmlElementImpl ) node.getUserData( USER_DATA_XMLELEMENT_KEY );
                 if( xmlElement == null )
                 {
                     xmlElement = new XmlElementImpl( ( Element ) node );
                 }
                 return xmlElement;
             }
         }
     }
 }
