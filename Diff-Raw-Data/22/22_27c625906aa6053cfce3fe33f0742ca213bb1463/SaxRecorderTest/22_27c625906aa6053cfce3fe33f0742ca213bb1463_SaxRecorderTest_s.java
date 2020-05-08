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
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.Iterator;
 
 import com.google.common.collect.ImmutableList;
 import org.junit.Test;
 import org.mockito.Matchers;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.*;
 
 /**
  * Tests for {@link SaxRecorder}.
  *
  * @author Laurent Caillette
  */
 public class SaxRecorderTest {
 
   @Test
   public void parseOk() throws SAXException, IOException {
 
     final SaxRecorder recorder = new SaxRecorder() ;
     final XMLReader xmlReader = XMLReaderFactory.createXMLReader() ;
     xmlReader.setContentHandler( recorder ) ;
     xmlReader.parse( new InputSource( new StringReader( XML ) ) ) ;
 
    final ContentHandlerAdapter target = mock( ContentHandlerAdapter.class ) ;
     final LocationGrabber locationGrabber = installLocationGrabber( target ) ;
 
     final SaxRecorder.Player player = recorder.getPlayer() ;
     player.playOn( target ) ;
 
    final Iterator<ImmutableSourceLocator> locations = locationGrabber.getLocationsIterator() ;
 
     verify( target ).startDocument() ;
     assertThat( locations.next() ).isEqualTo( new ImmutableSourceLocator( 1, 1 ) ) ;
 
     verify( target ).startElement( "", "root", "root", new ImmutableAttributes() ) ;
     assertThat( locations.next() ).isEqualTo( new ImmutableSourceLocator( 2, 7 ) ) ;
 
 
 /*
     final ArgumentCaptor< Object > argument0 = ArgumentCaptor.forClass( Object.class ) ;
     final ArgumentCaptor< Integer > argument1 = ArgumentCaptor.forClass( Integer.class ) ;
     final ArgumentCaptor< Integer > argument2 = ArgumentCaptor.forClass( Integer.class ) ;
     verify( target ).characters(
         ( char[] ) argument0.capture(), argument1.capture(), argument2.capture() ) ;
     final String text = new String(
         ( char[] ) argument0.getValue(), argument1.getValue(), argument2.getValue() ) ;
 */
 /*
     final ArgumentCaptor< Object > argument = ArgumentCaptor.forClass( Object.class ) ;
     verify( target ).characters(
         ( char[] ) argument.capture(),
         ( Integer ) argument.capture(),
         ( Integer ) argument.capture()
     ) ;
     final List< Object > arguments = argument.getAllValues() ;
     final String text = new String(
         ( char[] ) arguments.get( 0 ),
         ( Integer ) arguments.get( 1 ),
         ( Integer ) arguments.get( 2 )
     ) ;
     assertThat( text ).isEqualTo( "text" ) ;
 */
 
     // Too bad, Mockito doesn't seem to handle multiple captures for the same method call.
     verify( target, atLeastOnce() ).characters(
         Matchers.< char[] >anyObject(), anyInt(), anyInt() ) ;
 
     verify( target ).startElement(
         "", "child", "child",
         new ImmutableAttributes.Builder().add( "", "a", "a", "CDATA", "value" ).build()
     ) ;
     assertThat( locations.next() ).isEqualTo( new ImmutableSourceLocator( 4, 21 ) ) ;
 
     verify( target ).endElement( "", "child", "child" ) ;
     assertThat( locations.next() ).isEqualTo( new ImmutableSourceLocator( 4, 21 ) ) ;
 
     verify( target ).endElement( "", "root", "root" ) ;
     assertThat( locations.next() ).isEqualTo( new ImmutableSourceLocator( 5, 8 ) ) ;
 
     verify( target ).endDocument() ;
     assertThat( locations.next() ).isEqualTo( new ImmutableSourceLocator( 5, 8 ) ) ;
 
     assertThat( locations.hasNext() ).isFalse() ;
 
     assertThat( SaxRecorder.asXml( player ) ).isEqualTo( XML ) ;
   }
 
 // =======
 // Fixture
 // =======
 
   private static final String BREAK = "\n" ;
   private static final String XML =
       "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + BREAK  // 1
     + "<root>" + BREAK                                      // 2
     + " text " + BREAK                                      // 3
     + "  <child a=\"value\"/>" + BREAK                      // 4
     + "</root>"                                             // 5
   ;
 
 
   private static LocationGrabber installLocationGrabber( final ContentHandlerAdapter target )
       throws SAXException
   {
     final LocationGrabber locationGrabber = new LocationGrabber() ;
     doAnswer( locationGrabber ).when( target ).startDocument() ;
     doAnswer( locationGrabber ).when( target ).startElement(
         anyString(), anyString(), anyString(), anyAttributes() ) ;
     doAnswer( locationGrabber ).when( target ).endElement( anyString(), anyString(), anyString() ) ;
     doAnswer( locationGrabber ).when( target ).endDocument() ;
     return locationGrabber;
   }
 
   /**
    * Performs a side-effect for storing current location in the {@code ContentHandler}
    * upon call of stubbed method.
    */
   private static class LocationGrabber implements Answer {
 
     private final ImmutableList.Builder< ImmutableSourceLocator > locations =
         ImmutableList.builder() ;
 
     @Override
     public Object answer( final InvocationOnMock invocation ) throws Throwable {
       final ContentHandlerAdapter locatorOwner = ( ContentHandlerAdapter ) invocation.getMock() ;
       locations.add( ImmutableSourceLocator.create( locatorOwner.getDocumentLocator() ) ) ;
       return null ;
     }
 
     public ImmutableList<ImmutableSourceLocator> getLocations() {
       return locations.build() ;
     }
 
     public Iterator<ImmutableSourceLocator> getLocationsIterator() {
       return getLocations().iterator() ;
     }
   }
 
   /**
    * Type sugar.
    */
   private static Attributes anyAttributes() {
     return Matchers.any() ;
   }
 
 }
