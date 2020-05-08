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
 package es.eucm.eadventure.editor.control.writer.domwriters;
 
 import java.util.List;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 import es.eucm.eadventure.common.data.adaptation.AdaptationRule;
 import es.eucm.eadventure.common.data.adaptation.AdaptedState;
 import es.eucm.eadventure.common.data.adaptation.UOLProperty;
 
 public class AdaptationDOMWriter {
 
     /**
      * Private constructor.
      */
     private AdaptationDOMWriter( ) {
 
     }
 
 
     
     
     /**
      * Returns the DOM element for the chapter
      * 
      * @param chapter
      *            Chapter data to be written
      * @return DOM element with the chapter data
      */
     public static Element buildDOM( List<AdaptationRule> rules, AdaptedState initialState, boolean scorm12, boolean scorm2004, String name, Document doc ) {
 
         Element adaptationNode = null;
 
         // Create the root node
         adaptationNode = doc.createElement( "adaptation" );
 
         if( scorm12 ) {
             adaptationNode.setAttribute( "scorm12", "yes" );
         }
         else {
             adaptationNode.setAttribute( "scorm12", "no" );
         }
         if( scorm2004 ) {
             adaptationNode.setAttribute( "scorm2004", "yes" );
         }
         else {
             adaptationNode.setAttribute( "scorm2004", "no" );
         }
         adaptationNode.setAttribute( "name", name );
 
         // Append the initial state, when available
         if( initialState != null && !initialState.isEmpty( ) ) {
             Node initialStateNode = doc.createElement( "initial-state" );
 
             // Append initial scene if available
             if( initialState.getTargetId( ) != null && !initialState.getTargetId( ).equals( "" ) ) {
                 Element initialScene = doc.createElement( "initial-scene" );
                 initialScene.setAttribute( "idTarget", initialState.getTargetId( ) );
                 initialStateNode.appendChild( initialScene );
             }
 
             // Append activate / deactivate flags or set value var
             Element actionFlag;
             for( int i = 0; i < initialState.getFlagsVars( ).size( ); i++ ) {
              // check the operation's type
                 actionFlag = null;
                 if( initialState.isFlag( i ) ) {
                     if( AdaptedState.isActivateOp( initialState.getAction( i ) ) )
                         actionFlag = doc.createElement( "activate" );
                     if( AdaptedState.isDeactivateOp( initialState.getAction( i ) ) )
                         actionFlag = doc.createElement( "deactivate" );
                     
                     actionFlag.setAttribute( "flag", initialState.getFlagVar( i ) );
                 }
                 else {
                     if( AdaptedState.isSetValueOp( initialState.getAction( i ) ) )
                         // get only the title of the operation
                         actionFlag = doc.createElement( "set-value" );
                     else if( AdaptedState.isIncrementOp( initialState.getAction( i ) ) )
                         actionFlag = doc.createElement( "increment" );
                     else if( AdaptedState.isDecrementOp( initialState.getAction( i ) ) )
                         actionFlag = doc.createElement( "decrement" );
 
                     //set the name of the current var
                     actionFlag.setAttribute( "var", initialState.getFlagVar( i ) );
                     // set the value
                     actionFlag.setAttribute( "value", initialState.getValueForVar( i ) );
                 }
                 initialStateNode.appendChild( actionFlag );
             }
 
             //Append the node
             adaptationNode.appendChild( initialStateNode );
         }
 
         // Append the adaptation rules
         for( AdaptationRule rule : rules ) {
 
             //Create the rule node and set attributes
             Node ruleNode = doc.createElement( "adaptation-rule" );
 
             //Append rule description
             Node descriptionNode = doc.createElement( "ruleDescription" );
             if( rule.getDescription( ) != null )
                 descriptionNode.appendChild( doc.createTextNode( rule.getDescription( ) ) );
             else
                 descriptionNode.appendChild( doc.createTextNode( "" ) );
             ruleNode.appendChild( descriptionNode );
 
             //Append uol-state
             Node uolStateNode = doc.createElement( "uol-state" );
             for( UOLProperty property : rule.getUOLProperties( ) ) {
                 Element propertyElement = doc.createElement( "property" );
                 propertyElement.setAttribute( "id", property.getId( ) );
                 propertyElement.setAttribute( "value", property.getValue( ) );
                 uolStateNode.appendChild( propertyElement );
                 propertyElement.setAttribute( "operation", property.getOperation( ) );
             }
             ruleNode.appendChild( uolStateNode );
 
             //Append game-state
             // Append the initial state, when available
             Node gameStateNode = doc.createElement( "game-state" );
             if( rule.getAdaptedState( ) != null && !rule.getAdaptedState( ).isEmpty( ) ) {
 
                 // Append initial scene if available
                 if( rule.getAdaptedState( ).getTargetId( ) != null && !rule.getAdaptedState( ).getTargetId( ).equals( "" ) ) {
                     Element initialScene = doc.createElement( "initial-scene" );
                     initialScene.setAttribute( "idTarget", rule.getAdaptedState( ).getTargetId( ) );
                     gameStateNode.appendChild( initialScene );
                 }
 
                 // Append activate / deactivate flags or set value var
                 Element actionFlag = null;
                 for( int i = 0; i < rule.getAdaptedState( ).getFlagsVars( ).size( ); i++ ) {
                     if( rule.getAdaptedState( ).isFlag( i ) ) {
                        actionFlag = doc.createElement( rule.getAdaptedState( ).getAction( i ) );
                         actionFlag.setAttribute( "flag", rule.getAdaptedState( ).getFlagVar( i ) );
 
                     }
                     else {
                         // check if this operation is "set-value"
                         if( AdaptedState.isSetValueOp( rule.getAdaptedState( ).getAction( i ) ) )
                             // get only the title of the operation
                            actionFlag = doc.createElement( AdaptedState.VALUE );
                         // check if this operation is "increment"
                         else if( AdaptedState.isIncrementOp( rule.getAdaptedState( ).getAction( i ) ) )
                             // get only the title of the operation
                            actionFlag = doc.createElement( AdaptedState.INCREMENT );
                         // check if this operation is "decrement"
                         else if( AdaptedState.isDecrementOp( rule.getAdaptedState( ).getAction( i ) ) )
                             // get only the title of the operation
                            actionFlag = doc.createElement( AdaptedState.DECREMENT );
 
                         //set the name of the current var
                         actionFlag.setAttribute( "var", rule.getAdaptedState( ).getFlagVar( i ) );
                         // store the future value
                         actionFlag.setAttribute( "value", rule.getAdaptedState( ).getValueForVar( i ) );
 
                     }
                     gameStateNode.appendChild( actionFlag );
                 }
 
             }
             //Append the node
             ruleNode.appendChild( gameStateNode );
 
             //Append the rule
             adaptationNode.appendChild( ruleNode );
 
         }
 
         return adaptationNode;
     }
 }
