 /*******************************************************************************
  * <e-Adventure> (formerly <e-Game>) is a research project of the <e-UCM>
  *          research group.
  *   
  *    Copyright 2005-2012 <e-UCM> research group.
  *  
  *     <e-UCM> is a research group of the Department of Software Engineering
  *          and Artificial Intelligence at the Complutense University of Madrid
  *          (School of Computer Science).
  *  
  *          C Profesor Jose Garcia Santesmases sn,
  *          28040 Madrid (Madrid), Spain.
  *  
  *          For more info please visit:  <http://e-adventure.e-ucm.es> or
  *          <http://www.e-ucm.es>
  *  
  *  ****************************************************************************
  * This file is part of <e-Adventure>, version 1.4.
  * 
  *   You can access a list of all the contributors to <e-Adventure> at:
  *          http://e-adventure.e-ucm.es/contributors
  *  
  *  ****************************************************************************
  *       <e-Adventure> is free software: you can redistribute it and/or modify
  *      it under the terms of the GNU Lesser General Public License as published by
  *      the Free Software Foundation, either version 3 of the License, or
  *      (at your option) any later version.
  *  
  *      <e-Adventure> is distributed in the hope that it will be useful,
  *      but WITHOUT ANY WARRANTY; without even the implied warranty of
  *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *      GNU Lesser General Public License for more details.
  *  
  *      You should have received a copy of the GNU Lesser General Public License
  *      along with <e-Adventure>.  If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package es.eucm.eadventure.common.loader.subparsers;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xml.sax.Attributes;
 
 import es.eucm.eadventure.common.data.chapter.Chapter;
 import es.eucm.eadventure.common.data.chapter.InfluenceArea;
 import es.eucm.eadventure.common.data.chapter.conditions.Conditions;
 import es.eucm.eadventure.common.data.chapter.effects.Effects;
 import es.eucm.eadventure.common.data.chapter.elements.ActiveArea;
 import es.eucm.eadventure.common.data.chapter.elements.Description;
 import es.eucm.eadventure.common.data.chapter.scenes.Scene;
 
 /**
  * Class to subparse items.
  */
 public class ActiveAreaSubParser extends SubParser {
 
     /* Attributes */
 
     /**
      * Constant for reading nothing.
      */
     private static final int READING_NONE = 0;
 
     /**
      * Constant for subparsing nothing.
      */
     private static final int SUBPARSING_NONE = 0;
 
     /**
      * Constant for subparsing condition tag.
      */
     private static final int SUBPARSING_CONDITION = 1;
 
     /**
      * Constant for subparsing effect tag.
      */
     private static final int SUBPARSING_EFFECT = 2;
 
     private static final int SUBPARSING_ACTIONS = 3;
     
     private static final int SUBPARSING_DESCRIPTION = 4;
 
     /**
      * Store the current element being parsed.
      */
     private int reading = READING_NONE;
 
     /**
      * Stores the current element being subparsed.
      */
     private int subParsing = SUBPARSING_NONE;
 
     /**
      * ActiveArea being parsed.
      */
     private ActiveArea activeArea;
 
     /**
      * Current conditions being parsed.
      */
     private Conditions currentConditions;
 
     /**
      * Current effects being parsed.
      */
     private Effects currentEffects;
 
     /**
      * Subparser for effects and conditions.
      */
     private SubParser subParser;
 
     /**
      * Stores the scene where the area should be attached
      */
     private Scene scene;
 
     private int nAreas;
     
     
     private List<Description> descriptions;
     
     private Description description;
 
     /* Methods */
 
     /**
      * Constructor.
      * 
      * @param chapter
      *            Chapter data to store the read data
      */
     public ActiveAreaSubParser( Chapter chapter, Scene scene, int nAreas ) {
 
         super( chapter );
         this.nAreas = nAreas;
         this.scene = scene;
         
     }
 
     private String generateId( ) {
 
         return "area" + Integer.toString( nAreas + 1 ) + "scene" + scene.getId( );
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see es.eucm.eadventure.engine.cargador.subparsers.SubParser#startElement(java.lang.String, java.lang.String,
      *      java.lang.String, org.xml.sax.Attributes)
      */
     @Override
     public void startElement( String namespaceURI, String sName, String qName, Attributes attrs ) {
 
         // If no element is being subparsed
         if( subParsing == SUBPARSING_NONE ) {
             // If it is a object tag, create the new object (with its id)
             if( qName.equals( "active-area" ) ) {
 
                 int x = 0, y = 0, width = 0, height = 0;
                 String id = null;
                boolean rectangular = true;
                 int influenceX = 0, influenceY = 0, influenceWidth = 0, influenceHeight = 0;
                 boolean hasInfluence = false;
 
                 for( int i = 0; i < attrs.getLength( ); i++ ) {
                     if( attrs.getQName( i ).equals( "rectangular" ) )
                         rectangular = attrs.getValue( i ).equals( "yes" );
                     if( attrs.getQName( i ).equals( "x" ) )
                         x = Integer.parseInt( attrs.getValue( i ) );
                     if( attrs.getQName( i ).equals( "y" ) )
                         y = Integer.parseInt( attrs.getValue( i ) );
                     if( attrs.getQName( i ).equals( "width" ) )
                         width = Integer.parseInt( attrs.getValue( i ) );
                     if( attrs.getQName( i ).equals( "height" ) )
                         height = Integer.parseInt( attrs.getValue( i ) );
                     if( attrs.getQName( i ).equals( "id" ) )
                         id = attrs.getValue( i );
                     if( attrs.getQName( i ).equals( "hasInfluenceArea" ) )
                         hasInfluence = attrs.getValue( i ).equals( "yes" );
                     if( attrs.getQName( i ).equals( "influenceX" ) )
                         influenceX = Integer.parseInt( attrs.getValue( i ) );
                     if( attrs.getQName( i ).equals( "influenceY" ) )
                         influenceY = Integer.parseInt( attrs.getValue( i ) );
                     if( attrs.getQName( i ).equals( "influenceWidth" ) )
                         influenceWidth = Integer.parseInt( attrs.getValue( i ) );
                     if( attrs.getQName( i ).equals( "influenceHeight" ) )
                         influenceHeight = Integer.parseInt( attrs.getValue( i ) );
 
                 }
 
                 activeArea = new ActiveArea( ( id == null ? generateId( ) : id ), rectangular, x, y, width, height );
                 if( hasInfluence ) {
                     InfluenceArea influenceArea = new InfluenceArea( influenceX, influenceY, influenceWidth, influenceHeight );
                     activeArea.setInfluenceArea( influenceArea );
                 }
                 descriptions = new ArrayList<Description>();
                 activeArea.setDescriptions( descriptions );
             }
 
             else if( qName.equals( "point" ) ) {
                 if( activeArea != null ) {
                     int x = 0, y = 0;
 
                     for( int i = 0; i < attrs.getLength( ); i++ ) {
                         if( attrs.getQName( i ).equals( "x" ) )
                             x = Integer.parseInt( attrs.getValue( i ) );
                         if( attrs.getQName( i ).equals( "y" ) )
                             y = Integer.parseInt( attrs.getValue( i ) );
                     }
 
                     Point point = new Point( x, y );
                     activeArea.addPoint( point );
                 }
             }  
          
          // If it is a description tag, create the new description (with its id)
             else if( qName.equals( "description" ) ) {
                 description = new Description();
                 subParser = new DescriptionsSubParser(description, chapter);
                 subParsing = SUBPARSING_DESCRIPTION; 
             }
             else if( qName.equals( "actions" ) ) {
                 subParser = new ActionsSubParser( chapter, activeArea );
                 subParsing = SUBPARSING_ACTIONS;
             }
 
             // If it is a condition tag, create new conditions and switch the state
             else if( qName.equals( "condition" ) ) {
                 currentConditions = new Conditions( );
                 subParser = new ConditionSubParser( currentConditions, chapter );
                 subParsing = SUBPARSING_CONDITION;
             }
 
             // If it is a effect tag, create new effects and switch the state
             else if( qName.equals( "effect" ) ) {
                 subParser = new EffectSubParser( currentEffects, chapter );
                 subParsing = SUBPARSING_EFFECT;
             }
         }
 
         // If it is reading an effect or a condition, spread the call
         if( subParsing != SUBPARSING_NONE ) {
             subParser.startElement( namespaceURI, sName, qName, attrs );
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see es.eucm.eadventure.engine.cargador.subparsers.SubParser#endElement(java.lang.String, java.lang.String,
      *      java.lang.String)
      */
     @Override
     public void endElement( String namespaceURI, String sName, String qName ) {
 
         // If no element is being subparsed
         if( subParsing == SUBPARSING_NONE ) {
 
             // If it is an object tag, store the object in the game data
             if( qName.equals( "active-area" ) ) {
                 scene.addActiveArea( activeArea );
             }
 
             // If it is a documentation tag, hold the documentation in the current element
             else if( qName.equals( "documentation" ) ) {
                 activeArea.setDocumentation( currentString.toString( ).trim( ) );
             }
             
             // Reset the current string
             currentString = new StringBuffer( );
         }
 
         // If a condition is being subparsed
         else if( subParsing == SUBPARSING_CONDITION ) {
             // Spread the call
             subParser.endElement( namespaceURI, sName, qName );
 
             // If the condition tag is being closed
             if( qName.equals( "condition" ) ) {
                 if( reading == READING_NONE ) {
                     this.activeArea.setConditions( currentConditions );
                 }
                 // Switch state
                 subParsing = SUBPARSING_NONE;
             }
         }
 
         // If an effect is being subparsed
         else if( subParsing == SUBPARSING_EFFECT ) {
             // Spread the call
             subParser.endElement( namespaceURI, sName, qName );
 
             // If the effect tag is being closed, switch the state
             if( qName.equals( "effect" ) ) {
                 subParsing = SUBPARSING_NONE;
             }
         }
 
         else if( subParsing == SUBPARSING_ACTIONS ) {
             subParser.endElement( namespaceURI, sName, qName );
             if( qName.equals( "actions" ) ) {
                 subParsing = SUBPARSING_NONE;
             }
         }
         
         // If it is a description tag, create the new description (with its id)
         else if( subParsing == SUBPARSING_DESCRIPTION ) {
          // Spread the call
             subParser.endElement( namespaceURI, sName, qName );
             if( qName.equals( "description" ) ) {
                 this.descriptions.add( description );
                 subParsing = SUBPARSING_NONE;
             }
             
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see es.eucm.eadventure.engine.loader.subparsers.SubParser#characters(char[], int, int)
      */
     @Override
     public void characters( char[] buf, int offset, int len ) {
 
         // If no element is being subparsed
         if( subParsing == SUBPARSING_NONE )
             super.characters( buf, offset, len );
 
         // If it is reading an effect or a condition, spread the call
         else
             subParser.characters( buf, offset, len );
     }
 }
