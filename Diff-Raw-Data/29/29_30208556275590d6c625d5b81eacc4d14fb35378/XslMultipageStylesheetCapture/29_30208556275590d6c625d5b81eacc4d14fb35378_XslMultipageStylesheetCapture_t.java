 /*
  * Copyright (C) 2010 Laurent Caillette
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation, either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.novelang.rendering.multipage;
 
 import java.util.Map;
 
 import com.google.common.base.Preconditions;
 import org.apache.xml.utils.MutableAttrListImpl;
 import org.dom4j.Document;
 import org.dom4j.io.SAXContentHandler;
 import org.novelang.logger.Logger;
 import org.novelang.logger.LoggerFactory;
 import org.novelang.outfit.xml.NamespaceAwareness;
 import org.novelang.outfit.xml.SaxPipeline;
 import org.novelang.outfit.xml.XmlNamespaces;
 import org.xml.sax.Attributes;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.AttributesImpl;
 
 /**
  * Captures the XML document inside a &lt;{@value #MULTIPAGE_STYLESHEET_LOCALNAME}> element
  * (inside the "{@value org.novelang.outfit.xml.XmlNamespaces#XSL_META_NAMESPACE_URI}" namespace)
  * as a standalone XML document.
  * The purpose is to use it as a stylesheet afterwards.
  *
  * @author Laurent Caillette
  */
 public abstract class XslMultipageStylesheetCapture extends SaxPipeline.Stage {
 
   private SAXContentHandler documentBuilder = null ;
 
   private final EntityResolver entityResolver ;
 
 
   /**
    * Override to do something with freshly-parsed stylesheet.
    *
    * @param stylesheetDocument a non-null object.
    */
   protected abstract void onStylesheetDocumentBuilt( final Document stylesheetDocument ) ;
 
 
   protected XslMultipageStylesheetCapture( final EntityResolver entityResolver ) {
     this.entityResolver = Preconditions.checkNotNull( entityResolver ) ;
   }
 
   private boolean insideNestedStylesheet() {
     return documentBuilder != null ;
   }
 
 // ==================
 // NamespaceAwareness
 // ==================
 
   private final NamespaceAwareness namespaceAwareness =
       new NamespaceAwareness( XmlNamespaces.XSL_META_NAMESPACE_URI ) ;
 
   private NamespaceAwareness getNamespaceAwareness() {
     return namespaceAwareness ;
   }
 
   private static final String MULTIPAGE_STYLESHEET_LOCALNAME = "multipage" ;
 
   private boolean isNestedStylesheetRootElement( final String uri, final String localName ) {
     return getNamespaceAwareness().isMetaPrefix( uri )
         && MULTIPAGE_STYLESHEET_LOCALNAME.equals( localName ) ;
   }
 
   private String getXsltPrefixMapping() {
     return getNamespaceAwareness().getPrefixMappings().inverse()
         .get( XmlNamespaces.XSL_NAMESPACE_URI ) ;
   }
 
 
 // ==============
 // ContentHandler
 // ==============
 
   @Override
   public void startElement(
       final String uri,
       final String localName,
       final String qName,
       final Attributes attributes
   ) throws SAXException {
     if( isNestedStylesheetRootElement( uri, localName ) ) {
       if( documentBuilder == null ) {
         documentBuilder = new SAXContentHandler() ;
         documentBuilder.setDocumentLocator( getDocumentLocator() ) ;
         documentBuilder.setEntityResolver( entityResolver ) ;
         documentBuilder.startDocument() ;
         for( final Map.Entry< String, String > prefixMapping
             : getNamespaceAwareness().getPrefixMappings().entrySet()
         ) {
           documentBuilder.startPrefixMapping( prefixMapping.getKey(), prefixMapping.getValue() ) ;
         }
       } else {
         getNamespaceAwareness().throwException( "Not allowed: nested " +
             getNamespaceAwareness().getNamespacePrefix() + ":" + MULTIPAGE_STYLESHEET_LOCALNAME ) ;
       }
     }
     if( documentBuilder != null ) {
       if( isNestedStylesheetRootElement( uri, localName ) ) {
 
         final AttributesImpl attributesWithVersion = new MutableAttrListImpl( attributes ) ;
         // String uri, String localName, String qName, String type, String value
         attributesWithVersion.addAttribute( "", "version", "version", "", "1.0" ) ;
 
         documentBuilder.startElement(
             XmlNamespaces.XSL_NAMESPACE_URI,
             "stylesheet",
             getXsltPrefixMapping() + ":" + "stylesheet",
             attributesWithVersion
         ) ;
       } else {
         documentBuilder.startElement( uri, localName, qName, attributes ) ;
       }
     }
     if( ! insideNestedStylesheet() ) {
       super.startElement( uri, localName, qName, attributes ) ;
     }
 
   }
 
   @Override
   public void endElement(
       final String uri,
       final String localName,
       final String qName
   ) throws SAXException {
    // Remain symmetrical with startElement, evaluate this before changing documentBuilder.
    final boolean wasInsideNestedStylesheet = insideNestedStylesheet() ;

     if( documentBuilder != null ) {
       if( isNestedStylesheetRootElement( uri, localName ) ) {
         documentBuilder.endElement(
             XmlNamespaces.XSL_NAMESPACE_URI,
             "stylesheet",
             getXsltPrefixMapping() + ":" + "stylesheet"
         ) ;
       } else {
         documentBuilder.endElement( uri, localName, qName ) ;
       }
       if( isNestedStylesheetRootElement( uri, localName ) ) {
         documentBuilder.endDocument() ;
         onStylesheetDocumentBuilt( documentBuilder.getDocument() ) ;
         documentBuilder = null ;
       }
     }
    if( !wasInsideNestedStylesheet ) {
       super.endElement( uri, localName, qName ) ;
     }
   }
 
   @Override
   protected void afterDocumentLocatorSet() {
     namespaceAwareness.setDocumentLocator( getDocumentLocator() ) ;
     if( documentBuilder != null ) {
       documentBuilder.setDocumentLocator( getDocumentLocator() ) ;
     }
   }
 
   @Override
   public void startPrefixMapping( final String prefix, final String uri ) throws SAXException {
     namespaceAwareness.startPrefixMapping( prefix, uri ) ;
     if( documentBuilder != null ) {
       documentBuilder.startPrefixMapping( prefix, uri ) ;
     }
     if( ! insideNestedStylesheet() ) {
       super.startPrefixMapping( prefix, uri ) ;
     }
   }
 
   @Override
   public void endPrefixMapping( final String prefix ) throws SAXException {
     namespaceAwareness.endPrefixMapping( prefix ) ;
     if( documentBuilder != null ) {
       documentBuilder.endPrefixMapping( prefix ) ;
     }
     if( ! insideNestedStylesheet() ) {
       super.endPrefixMapping( prefix ) ;
     }
   }
 
   @Override
   public void characters(
       final char[] characters,
       final int start,
       final int length
   ) throws SAXException {
     if( documentBuilder != null ) {
       documentBuilder.characters( characters, start, length ) ;
     }
     if( ! insideNestedStylesheet() ) {
       super.characters( characters, start, length ) ;
     }
 
   }
 
   @Override
   public void ignorableWhitespace(
       final char[] characters,
       final int start,
       final int length
   ) throws SAXException {
     if( documentBuilder != null ) {
       documentBuilder.ignorableWhitespace( characters, start, length ) ;
     }
     if( ! insideNestedStylesheet() ) {
       super.ignorableWhitespace( characters, start, length ) ;
     }
   }
 
   @Override
   public void processingInstruction(
       final String target,
       final String data
   ) throws SAXException {
     if( documentBuilder != null ) {
       documentBuilder.processingInstruction( target, data ) ;
     }
     if( ! insideNestedStylesheet() ) {
       super.processingInstruction( target, data ) ;
     }
 
   }
 
   @Override
   public void skippedEntity( final String name ) throws SAXException {
     if( documentBuilder != null ) {
       documentBuilder.skippedEntity( name ) ;
     }
     if( ! insideNestedStylesheet() ) {
       super.skippedEntity( name ) ;
     }
   }
 }
