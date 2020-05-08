 /*******************************************************************************
  * <e-Adventure> (formerly <e-Game>) is a research project of the <e-UCM>
  *         research group.
  *  
  *   Copyright 2005-2010 <e-UCM> research group.
  * 
  *   You can access a list of all the contributors to <e-Adventure> at:
  *         http://e-adventure.e-ucm.es/contributors
  * 
  *   <e-UCM> is a research group of the Department of Software Engineering
  *         and Artificial Intelligence at the Complutense University of Madrid
  *         (School of Computer Science).
  * 
  *         C Profesor Jose Garcia Santesmases sn,
  *         28040 Madrid (Madrid), Spain.
  * 
  *         For more info please visit:  <http://e-adventure.e-ucm.es> or
  *         <http://www.e-ucm.es>
  * 
  * ****************************************************************************
  * 
  * This file is part of <e-Adventure>, version 1.2.
  * 
  *     <e-Adventure> is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU Lesser General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  * 
  *     <e-Adventure> is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU Lesser General Public License for more details.
  * 
  *     You should have received a copy of the GNU Lesser General Public License
  *     along with <e-Adventure>.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.common.loader.parsers;
 
 import java.io.InputStream;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import es.eucm.eadventure.common.data.animation.Animation;
 import es.eucm.eadventure.common.data.animation.ImageLoaderFactory;
 import es.eucm.eadventure.common.data.chapter.resources.Resources;
 import es.eucm.eadventure.common.loader.InputStreamCreator;
 import es.eucm.eadventure.common.loader.subparsers.FrameSubParser;
 import es.eucm.eadventure.common.loader.subparsers.TransitionSubParser;
 
 public class AnimationHandler extends DefaultHandler {
 
     /**
      * String to store the current string in the XML file
      */
     StringBuffer currentString;
 
     /**
      * Resources to store the current resources being read
      */
     Resources currentResources;
 
     /**
      * Constant for reading nothing
      */
     private static final int READING_NONE = 0;
 
     /**
      * Constant for reading transition
      */
     private static final int READING_TRANSITION = 1;
 
     /**
      * Constant for reading frame
      */
     private static final int READING_FRAME = 2;
 
     /**
      * Stores the current element being read.
      */
     private int reading = READING_NONE;
 
     /**
      * Current subparser being used
      */
    private DefaultHandler subParser;
 
     /**
      * Animation being read.
      */
     private Animation animation;
 
     /**
      * InputStreamCreator used in resolveEntity to find dtds (only required in
      * Applet mode)
      */
     private InputStreamCreator isCreator;
     
     private ImageLoaderFactory factory;
 
     public AnimationHandler( InputStreamCreator isCreator, ImageLoaderFactory imageloader ) {
 
         this.factory = imageloader;
         this.isCreator = isCreator;
     }
 
     @Override
     public void startElement( String namespaceURI, String sName, String qName, Attributes attrs ) throws SAXException {
 
         if( this.reading == READING_NONE ) {
 
             if( qName.equals( "animation" ) ) {
                 for( int i = 0; i < attrs.getLength( ); i++ ) {
                     if( attrs.getQName( i ).equals( "id" ) ) {
                         animation = new Animation( attrs.getValue( i ), factory );
                         animation.getFrames( ).clear( );
                         animation.getTransitions( ).clear( );
                     }
 
                     if( attrs.getQName( i ).equals( "slides" ) ) {
                         if( attrs.getValue( i ).equals( "yes" ) )
                             animation.setSlides( true );
                         else
                             animation.setSlides( false );
                     }
 
                     if( attrs.getQName( i ).equals( "usetransitions" ) ) {
                         if( attrs.getValue( i ).equals( "yes" ) )
                             animation.setUseTransitions( true );
                         else
                             animation.setUseTransitions( false );
                     }
                 }
             }
 
             if( qName.equals( "documentation" ) ) {
                 currentString = new StringBuffer( );
             }
 
             if( qName.equals( "resources" ) ) {
                 currentResources = new Resources( );
                 
                 for (int i = 0; i < attrs.getLength( ); i++) {
                     if (attrs.getQName( i ).equals( "name" ))
                         currentResources.setName( attrs.getValue( i ) );
                 }
             }
 
             else if( qName.equals( "asset" ) ) {
                 String type = "";
                 String path = "";
 
                 for( int i = 0; i < attrs.getLength( ); i++ ) {
                     if( attrs.getQName( i ).equals( "type" ) )
                         type = attrs.getValue( i );
                     if( attrs.getQName( i ).equals( "uri" ) )
                         path = attrs.getValue( i );
                 }
 
                 currentResources.addAsset( type, path );
             }
 
             if( qName.equals( "frame" ) ) {
                 subParser = new FrameSubParser( animation );
                 reading = READING_FRAME;
             }
 
             if( qName.equals( "transition" ) ) {
                 subParser = new TransitionSubParser( animation );
                 reading = READING_TRANSITION;
             }
         }
         if( reading != READING_NONE ) {
             subParser.startElement( namespaceURI, sName, qName, attrs );
         }
 
     }
 
     @Override
     public void endElement( String namespaceURI, String sName, String qName ) {
 
         if( qName.equals( "documentation" ) ) {
             if( reading == READING_NONE )
                 animation.setDocumentation( currentString.toString( ).trim( ) );
         }
         else if( qName.equals( "resources" ) ) {
             animation.addResources( currentResources );
         }
 
         if( reading != READING_NONE ) {
            try {
                 subParser.endElement( namespaceURI, sName, qName );
            }
            catch( SAXException e ) {
                 e.printStackTrace( );
            }
            reading = READING_NONE;
         }
 
     }
 
     @Override
     public void error( SAXParseException exception ) throws SAXParseException {
 
         // On validation, propagate exception
         exception.printStackTrace( );
         throw exception;
     }
 
     @Override
     public void characters( char[] buf, int offset, int len ) throws SAXException {
 
         // Append the new characters
         currentString.append( new String( buf, offset, len ) );
     }
 
     public Animation getAnimation( ) {
 
         return animation;
     }
 
     /*
      *  (non-Javadoc)
      * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
      */
     @Override
     public InputSource resolveEntity( String publicId, String systemId ) {
 
         int startFilename = systemId.lastIndexOf( "/" ) + 1;
         String filename = systemId.substring( startFilename, systemId.length( ) );
         InputStream inputStream = isCreator.buildInputStream( filename );
         return new InputSource( inputStream );
     }
 }
