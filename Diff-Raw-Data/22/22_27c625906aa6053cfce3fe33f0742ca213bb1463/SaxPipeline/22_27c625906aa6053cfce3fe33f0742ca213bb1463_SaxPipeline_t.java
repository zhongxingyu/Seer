 /*
  * Copyright (C) 2008 Laurent Caillette
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
 
 import java.util.List;
 
 import com.google.common.collect.Lists;
 import org.xml.sax.Attributes;
 import org.xml.sax.ContentHandler;
 import org.xml.sax.Locator;
 import org.xml.sax.SAXException;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 /**
  * Chains multiple {@link ContentHandler}s as {@link Stage}s with the responsability for
  * each {@link Stage} of calling the next one, allowing various kind of SAX methd calls
  * interception.
  * One {@link Stage} can only belong to one {@link org.novelang.outfit.xml.SaxPipeline} at
  * a time.
  *
  * This class is not thread-safe.
  *
  * @author Laurent Caillette
  */
 public class SaxPipeline extends DelegatingContentHandler {
 
   private final List< Stage > stages = Lists.newArrayList() ;
   private final ContentHandler end ;
 
   public SaxPipeline( final ContentHandler end ) {
     this.end = checkNotNull( end ) ;
   }
 
   @Override
   protected final ContentHandler getDelegate() {
     return getContentHandlerAt( 0 ) ;
   }
 
   /**
    * Includes the {@link #end}.
    *
    * @return a value strictly greater than 0.
    */
   protected final int getContentHandlerCount() {
     return stages.size() + 1 ;
   }
 
   protected final ContentHandler getContentHandlerAt( final int position ) {
     checkArgument( position >= 0, "Postion of %s lower than 0", position ) ;
     checkArgument( position <= getContentHandlerCount(),
         "Position of %s greater than ContentHandler count of %s", getContentHandlerCount() ) ;
     if( position == getContentHandlerCount() - 1 ) {
       return end ;
     } else {
       return stages.get( position ) ;
     }
   }
 
   public final void add( final Stage stage, final int position ) {
     checkNotNull( stage ) ;
     checkArgument( position >= 0 ) ;
     checkArgument( position < getContentHandlerCount() ) ;
     checkArgument(
         ! isExplicitelySet( stage.delegate ), "Stage %s already belongs to a pipeline", stage ) ;
     stages.add( position, stage ) ;
     chainStagesAround( position ) ;
   }
 
   /**
    * Replace an existing {@link Stage} (can't replace end). 
    */
   public final void replace( final int position, final Stage replacement ) {
     checkNotNull( replacement ) ;
     checkArgument( position >= 0 ) ;
     checkArgument( position < getContentHandlerCount(),
         "There must be at least %s stage(s), wrong position: %s", stages.size(), position ) ;
     stages.get( position ).delegate = UNASSIGNED ;
     stages.set( position, replacement ) ;
     chainStagesAround( position ) ;
   }
 
   private void chainStagesAround( final int position ) {
     if( position > 0 ) {
       stages.get( position - 1 ).delegate = getContentHandlerAt( position ) ;
     }
     if( position < getContentHandlerCount() ) {
       stages.get( position ).delegate = getContentHandlerAt( position + 1 ) ;
     }
   }
 
   // Don't need more methods by now, we're not building a framework here.
 
 // =====
 // Stage
 // =====
 
   private static boolean isExplicitelySet( final ContentHandler contentHandler ) {
     return contentHandler != null && contentHandler != UNASSIGNED ;
   }
 
   /**
    * Magic non-null value to tell that a {@link Stage} doesn't belong to a {@link SaxPipeline} while
    * keeping its {@link Stage#delegate} value non null for usage outside of a {@link SaxPipeline}.
    */
   private static final ContentHandler UNASSIGNED = new ContentHandlerAdapter() {
     @Override
     public String toString() {
       return SaxPipeline.class.getSimpleName() + ".UNASSIGNED" ;
     }
   } ;
 
   public abstract static class Stage extends DelegatingContentHandler {
 
     /**
      * Set directly by the owning {@link SaxPipeline}. Never null.
      */
     private ContentHandler delegate = UNASSIGNED ;
 
     @Override
     protected final ContentHandler getDelegate() {
       return delegate ;
     }
 
     @Override
     public String toString() {
       return getClass().getSimpleName() + "{delegate=" + delegate + "}" ;
     }
   }
 
   public static final class HollowStage extends Stage { }
 
 
   /**
    * A kind of {@link Stage} that forks SAX events into a simple {@link ContentHandler}
    * before forwarding them to other {@link Stage}s unconditionally.
    */
   public static class ForkingStage extends Stage {
 
     private final ContentHandler fork ;
 
     protected final ContentHandler getFork() {
       return fork ;
     }
 
     public ForkingStage( final ContentHandler fork ) {
       this.fork = checkNotNull( fork ) ;
     }
 
 
     @Override
     public void setDocumentLocator( final Locator locator ) {
       super.setDocumentLocator( locator ) ;
      fork.setDocumentLocator( getDocumentLocator() ) ;
     }
 
     @Override
     public void startPrefixMapping( final String prefix, final String uri ) throws SAXException {
       fork.startPrefixMapping( prefix, uri ) ;
       super.startPrefixMapping( prefix, uri );
     }
 
     @Override
     public void endPrefixMapping( final String prefix ) throws SAXException {
       fork.endPrefixMapping( prefix ) ;
       super.endPrefixMapping( prefix );
     }
 
     @Override
     public void startDocument() throws SAXException {
       fork.startDocument() ;
       super.startDocument();
     }
 
     @Override
     public void endDocument() throws SAXException {
       fork.endDocument() ;
       super.endDocument();
     }
 
     @Override
     public void startElement(
         final String uri,
         final String localName,
         final String qName,
         final Attributes attributes
     ) throws SAXException {
       fork.startElement( uri, localName, qName, attributes ) ;
       super.startElement( uri, localName, qName, attributes ) ;
     }
 
     @Override
     public void endElement( final String uri, final String localName, final String qName )
         throws SAXException
     {
       fork.endElement( uri, localName, qName ) ;
       super.endElement( uri, localName, qName );
     }
 
     @Override
     public void characters( final char[] chars, final int start, final int length )
         throws SAXException
     {
       fork.characters( chars, start, length ) ;
       super.characters( chars, start, length );
     }
 
     @Override
     public void ignorableWhitespace( final char[] chars, final int start, final int length )
         throws SAXException
     {
       fork.ignorableWhitespace( chars, start, length ) ;
       super.ignorableWhitespace( chars, start, length ) ;
     }
 
     @Override
     public void processingInstruction( final String target, final String data )
         throws SAXException
     {
       fork.processingInstruction( target, data ) ;
       super.processingInstruction( target, data );
     }
 
     @Override
     public void skippedEntity( final String name ) throws SAXException {
       fork.skippedEntity( name ) ;
       super.skippedEntity( name );
     }
   }
 
 }
