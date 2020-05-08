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
 package org.novelang.outfit.xml;
 
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 
 /**
  * Base class for implementing a SAX {@code ContentHandler},
  * with {@link org.novelang.outfit.xml.NamespaceAwareness} feature.
  *
  * @author Laurent Caillette
  */
 public abstract class NamespaceAwareContentHandlerAdapter extends ContentHandlerAdapter {
 
   private final NamespaceAwareness namespaceAwareness ;
 
 
   protected NamespaceAwareContentHandlerAdapter( final String namespaceUri ) {
     this.namespaceAwareness = new NamespaceAwareness( namespaceUri ) ;
   }
 
   /**
    * @return a non-null object.
    */
   public final NamespaceAwareness getNamespaceAwareness() {
     return namespaceAwareness ;
   }
 
 
   @Override
   public void setDocumentLocator( final Locator locator ) {
     super.setDocumentLocator( locator );
    namespaceAwareness.setDocumentLocator( getDocumentLocator() ) ;
   }
 
   @Override
   public void startPrefixMapping( final String prefix, final String uri ) throws SAXException {
     namespaceAwareness.startPrefixMapping( prefix, uri ) ;
   }
 
   @Override
   public void endPrefixMapping( final String prefix ) throws SAXException {
     namespaceAwareness.endPrefixMapping( prefix ) ;
   }
 
 
 
 
 }
