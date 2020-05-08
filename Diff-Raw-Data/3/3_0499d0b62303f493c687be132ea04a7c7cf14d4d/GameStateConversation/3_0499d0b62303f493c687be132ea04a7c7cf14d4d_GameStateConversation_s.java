 /**
  * <e-Adventure> is an <e-UCM> research project. <e-UCM>, Department of Software
  * Engineering and Artificial Intelligence. Faculty of Informatics, Complutense
  * University of Madrid (Spain).
  * 
  * @author Del Blanco, A., Marchiori, E., Torrente, F.J. (alphabetical order) *
  * @author Lpez Maas, E., Prez Padilla, F., Sollet, E., Torijano, B. (former
  *         developers by alphabetical order)
  * @author Moreno-Ger, P. & Fernndez-Manjn, B. (directors)
  * @year 2009 Web-site: http://e-adventure.e-ucm.es
  */
 
 /*
  * Copyright (C) 2004-2009 <e-UCM> research group
  * 
  * This file is part of <e-Adventure> project, an educational game & game-like
  * simulation authoring tool, available at http://e-adventure.e-ucm.es.
  * 
  * <e-Adventure> is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option) any
  * later version.
  * 
  * <e-Adventure> is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * <e-Adventure>; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place, Suite 330, Boston, MA 02111-1307 USA
  * 
  */
 package es.eucm.eadventure.engine.core.control.gamestate;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 
 import es.eucm.eadventure.common.data.chapter.conversation.line.ConversationLine;
 import es.eucm.eadventure.common.data.chapter.conversation.node.ConversationNode;
 import es.eucm.eadventure.common.data.chapter.conversation.node.ConversationNodeView;
 import es.eucm.eadventure.common.data.chapter.conversation.node.OptionConversationNode;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.engine.core.control.DebugLog;
 import es.eucm.eadventure.engine.core.control.Game;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalConditions;
 import es.eucm.eadventure.engine.core.control.functionaldata.FunctionalPlayer;
 import es.eucm.eadventure.engine.core.control.functionaldata.TalkingElement;
 import es.eucm.eadventure.engine.core.control.functionaldata.functionaleffects.FunctionalEffects;
 import es.eucm.eadventure.engine.core.gui.GUI;
 
 /**
  * A game main loop during a conversation
  */
 public class GameStateConversation extends GameState {
 
     /**
      * Number of response lines to display
      */
     private final int RESPONSE_TEXT_NUMBER_LINES;
 
     /**
      * Height of the text of the responses
      */
     private final int RESPONSE_TEXT_HEIGHT;
 
     /**
      * Ascent of the text of the response
      */
     private final int RESPONSE_TEXT_ASCENT;
 
     /**
      * Current conversational node being played
      */
     private ConversationNode currentNode;
 
     /**
      * Index of the line being played
      */
     private int currentLine;
 
     /**
      * Index of the first line displayed in an option node
      */
     private int firstLineDisplayed;
 
     /**
      * Index of the option currently highlighted
      */
     private int optionHighlighted;
 
     /**
      * Last mouse button pressed
      */
     private int mouseClickedButton = MouseEvent.NOBUTTON;
 
     /**
      * Variable to control the access to doRandom()
      */
     private boolean firstTime;
 
     /**
      * Store the option selected to use it when it come back to the running
      * effects Game State
      */
     private int optionSelected;
 
     /**
      * Indicates if an option was selected
      */
     private boolean isOptionSelected;
 
     /**
      * Indicates if a key was pressed
      */
     private boolean keyPressed;
 
     /**
      * Number of options that has been displayed in the screen
      */
     private int numberDisplayedOptions;
 
     /**
      * An array list that match the number "i" option with its real position in
      * option node.
      */
     // Remember that only will be show the options lines which achieves its conditions
     private ArrayList<Integer> correspondingIndex;
 
     /**
      * Store only the option which has all conditions OK
      */
     private ArrayList<ConversationLine> optionsToShow;
 
     /**
      * The name of conversation
      */
     private String convID;
 
     /**
      * Creates a new GameStateConversation
      */
     public GameStateConversation( ) {
 
         RESPONSE_TEXT_NUMBER_LINES = GUI.getInstance( ).getResponseTextNumberLines( );
         RESPONSE_TEXT_ASCENT = GUI.getInstance( ).getGraphics( ).getFontMetrics( ).getAscent( );
         RESPONSE_TEXT_HEIGHT = RESPONSE_TEXT_ASCENT + 2;
 
         currentNode = game.getConversation( ).getRootNode( );
         currentLine = 0;
         firstLineDisplayed = 0;
         optionHighlighted = -1;
         optionsToShow = new ArrayList<ConversationLine>( );
         isOptionSelected = false;
         convID = new String( );
 
     }
 
     @Override
     public synchronized void mainLoop( long elapsedTime, int fps ) {
 
         Graphics2D g = setUpGUI( elapsedTime );
 
         if( currentNode.getType( ) == ConversationNodeView.DIALOGUE )
             processDialogNode( );
         else if( currentNode.getType( ) == ConversationNodeView.OPTION )
             processOptionNode( g );
 
         GUI.getInstance( ).endDraw( );
         g.dispose( );
     }
 
     /**
      * Set up the basic gui of the scene.
      * 
      * @param elapsedTime
      *            The time elapsed since the last update
      * @return The graphics element for the scene
      */
     private Graphics2D setUpGUI( long elapsedTime ) {
 
         GUI.getInstance( ).toggleHud( false );
         GUI.getInstance( ).setDefaultCursor( );
 
         game.getFunctionalScene( ).update( elapsedTime );
         GUI.getInstance( ).update( elapsedTime );
 
         Graphics2D g = GUI.getInstance( ).getGraphics( );
         g.clearRect( 0, 0, GUI.WINDOW_WIDTH, GUI.WINDOW_HEIGHT );
 
         game.getFunctionalScene( ).draw( );
         GUI.getInstance( ).drawScene( g, elapsedTime );
 
         return g;
     }
 
     /**
      * Processed mouse clicks when in a dialog node. If no button was pressed,
      * the conversation goes on normally (goes to the next line only if no
      * character is talking or the one talking has finished). If the left button
      * (BUTTON1) was pressed, the current line is skipped. If the right button
      * (BUTTON3) was pressed, all the lines are skipped.
      */
     private void processDialogNode( ) {
 
         if( mouseClickedButton == MouseEvent.NOBUTTON ) {
             if( game.getCharacterCurrentlyTalking( ) == null || ( game.getCharacterCurrentlyTalking( ) != null && !game.getCharacterCurrentlyTalking( ).isTalking( ) ) ) {
                 playNextLine( );
             }
         }
         else if( mouseClickedButton == MouseEvent.BUTTON1 ) {
             DebugLog.user( "Skipped line in conversation" );
             playNextLine( );
             mouseClickedButton = MouseEvent.NOBUTTON;
         }
         else if( mouseClickedButton == MouseEvent.BUTTON3 ) {
             DebugLog.user( "Skipped conversation" );
             currentLine = currentNode.getLineCount( );
             playNextLine( );
             mouseClickedButton = MouseEvent.NOBUTTON;
         }
         firstTime = true;
     }
 
     /**
      * Process all the information available when in an option node. Two cases
      * are distinguished, when there is a selected option and where there is no
      * selected options.
      * 
      * @param g
      *            The graphics in which to draw the options
      */
     private void processOptionNode( Graphics2D g ) {
 
         if( !isOptionSelected )
             optionNodeNoOptionSelected( g );
         else
             optionNodeWithOptionSelected( );
     }
 
     /**
      * When in an option node, if no option is selected all the possible options
      * must be displayed on screen.
      * 
      * @param g
      *            The graphics to draw the options in.
      */
     private void optionNodeNoOptionSelected( Graphics2D g ) {
 
         if( firstTime ) {
             ( (OptionConversationNode) currentNode ).doRandom( );
             firstTime = false;
         }
         numberDisplayedOptions = 0;
 
         storeOKConditionsConversationLines( );
         if( optionsToShow.size( ) <= RESPONSE_TEXT_NUMBER_LINES ) {
             for( int i = 0; i < optionsToShow.size( ); i++ ) {
                 drawLine( g, optionsToShow.get( i ).getText( ), i, i );
                 numberDisplayedOptions++;
             }
         }
         else {
             int i, indexLastLine = Math.min( firstLineDisplayed + RESPONSE_TEXT_NUMBER_LINES - 1, optionsToShow.size( ) );
             for( i = firstLineDisplayed; i < indexLastLine; i++ ) {
                 drawLine( g, optionsToShow.get( i ).getText( ), ( i - firstLineDisplayed ), i );
                 numberDisplayedOptions++;
             }
             drawLine( g, TC.get( "GameText.More" ), ( i - firstLineDisplayed ), i );
         }
 
         // if there are not options to draw, finalize the conversation
         if( numberDisplayedOptions == 0 )
             endConversation( );
     }
 
     /**
      * Returns the number of conversation lines in current option node which has
      * all conditions OK
      * 
      * @return number of lines with achieve its conditions.
      */
     private void storeOKConditionsConversationLines( ) {
 
         optionsToShow = new ArrayList<ConversationLine>( );
         correspondingIndex = new ArrayList<Integer>( );
         for( int i = 0; i < currentNode.getLineCount( ); i++ ) {
             if( ( new FunctionalConditions( currentNode.getLine( i ).getConditions( ) ).allConditionsOk( ) ) ) {
                 optionsToShow.add( currentNode.getLine( i ) );
                 // Store the real position in node of recent inserted conversation line
                 correspondingIndex.add( i );
             }
         }
 
     }
 
     /**
      * Draw an option line in a given graphics object.
      * 
      * @param g
      *            The graphics where to draw the line
      * @param text
      *            The text of the option
      * @param optionIndex
      *            The index of the option
      */
     private void drawLine( Graphics2D g, String text, int optionIndex, int lineIndex ) {
 
         Color textColor = Game.getInstance( ).getFunctionalPlayer( ).getTextFrontColor( );
         if( optionIndex == optionHighlighted ) {
             int red = textColor.getRed( );
             int green = textColor.getGreen( );
             int blue = textColor.getBlue( );
             textColor = new Color( 255 - red, 255 - green, 255 - blue );
         }
         int y = GUI.getInstance( ).getResponseTextY( ) + optionIndex * RESPONSE_TEXT_HEIGHT + RESPONSE_TEXT_ASCENT;
         int x = GUI.getInstance( ).getResponseTextX( );
         String fullText = ( lineIndex + 1 ) + ".- " + text;
         GUI.drawStringOnto( g, fullText, x, y, false, textColor, Game.getInstance( ).getFunctionalPlayer( ).getTextBorderColor( ), true );
     }
 
     /**
      * In an option node with an option selected: If there is a character
      * talking, do nothing. If the current node has effects, pile them. If the
      * current node has consumed it's effects or doesn't have them, finish the
      * conversation. If the current node isn't terminal, follow the selected
      * option.
      */
     private void optionNodeWithOptionSelected( ) {
 
         if( game.getCharacterCurrentlyTalking( ) != null && game.getCharacterCurrentlyTalking( ).isTalking( ) ) {
             if( mouseClickedButton == MouseEvent.BUTTON1) {
                 DebugLog.user( "Skipped line in conversation" );
                 game.getCharacterCurrentlyTalking( ).stopTalking( );
                 mouseClickedButton = MouseEvent.NOBUTTON;
             } else if ( mouseClickedButton == MouseEvent.BUTTON3 ) {
                 DebugLog.user( "Skipped conversation" );
                 game.getCharacterCurrentlyTalking( ).stopTalking( );
             } else
                 return;
         }
 
         if( currentNode.hasValidEffect( ) && !currentNode.isEffectConsumed( ) ) {
             currentNode.consumeEffect( );
             game.pushCurrentState( this );
             FunctionalEffects.storeAllEffects( currentNode.getEffects( ), true );
             GUI.getInstance( ).toggleHud( true );
         }
         else if( ( !currentNode.hasValidEffect( ) || currentNode.isEffectConsumed( ) ) && currentNode.isTerminal( ) ) {
             endConversation( );
         }
         else if( !currentNode.isTerminal( ) ) {
             if( optionSelected >= 0 && optionSelected < currentNode.getChildCount( ) ) {
                 currentNode = currentNode.getChild( correspondingIndex.get( optionSelected ) );
                 isOptionSelected = false;
             }
         }
     }
 
     /**
      * Finalize the conversation
      */
     private void endConversation( ) {
 
         for( ConversationNode node : game.getConversation( ).getAllNodes( ) ) {
             node.resetEffect( );
         }
         GUI.getInstance( ).toggleHud( true );
         game.endConversation( );
     }
 
     /**
      * If the user chooses a valid option, it is selected and its text show.
      */
     private void selectDisplayedOption( ) {
 
         if( optionSelected >= 0 && optionSelected < optionsToShow.size( ) ) {
             if( game.getCharacterCurrentlyTalking( ) != null && game.getCharacterCurrentlyTalking( ).isTalking( ) )
                 game.getCharacterCurrentlyTalking( ).stopTalking( );
 
             FunctionalPlayer player = game.getFunctionalPlayer( );
             ConversationLine line = currentNode.getLine( correspondingIndex.get( optionSelected ) );
 
             if( line.isValidAudio( ) ) {
                 player.speak( line.getText( ), line.getAudioPath( ) );
             }
             else if( line.getSynthesizerVoice( ) || player.isAlwaysSynthesizer( ) ) {
                 player.speakWithFreeTTS( line.getText( ), player.getPlayerVoice( ) );
             }
             else if (!game.isTransparent( )) {
                 player.speak( line.getText( ) );
             }
             else
                 player.speak( "" );
 
             game.setCharacterCurrentlyTalking( player );
             isOptionSelected = true;
             keyPressed = false;
         }
     }
 
     /**
      * Select an option when all options do not fit in the screen
      */
     private void selectNoAllDisplayedOption( ) {
 
         if( !keyPressed )
             optionSelected += firstLineDisplayed;
 
         int indexLastLine = Math.min( firstLineDisplayed + RESPONSE_TEXT_NUMBER_LINES - 1, currentNode.getLineCount( ) );
 
         if( optionSelected == indexLastLine ) {
             firstLineDisplayed += RESPONSE_TEXT_NUMBER_LINES - 1;
             if( firstLineDisplayed >= currentNode.getLineCount( ) )
                 firstLineDisplayed = 0;
         }
         else
             selectDisplayedOption( );
     }
 
     @Override
     public synchronized void mouseClicked( MouseEvent e ) {
 
         if( currentNode.getType( ) == ConversationNodeView.OPTION &&
                 GUI.getInstance( ).getResponseTextY( ) <= e.getY( ) &&
                 GUI.getInstance( ).getResponseTextY( ) + currentNode.getLineCount( ) * RESPONSE_TEXT_HEIGHT + RESPONSE_TEXT_ASCENT >= e.getY( ) &&
                 !isOptionSelected) {
             optionSelected = ( e.getY( ) - GUI.getInstance( ).getResponseTextY( ) ) / RESPONSE_TEXT_HEIGHT;
             if( optionsToShow.size( ) <= RESPONSE_TEXT_NUMBER_LINES )
                 selectDisplayedOption( );
             else
                 selectNoAllDisplayedOption( );
         }
         else if( currentNode.getType( ) == ConversationNodeView.DIALOGUE || isOptionSelected) {
             if( e.getButton( ) == MouseEvent.BUTTON1 )
                 mouseClickedButton = MouseEvent.BUTTON1;
             else if( e.getButton( ) == MouseEvent.BUTTON3 )
                 mouseClickedButton = MouseEvent.BUTTON3;
         }
     }
 
     @Override
     public void keyPressed( KeyEvent e ) {
 
         if( currentNode.getType( ) == ConversationNodeView.OPTION && !isOptionSelected ) {
             if( e.getKeyCode( ) >= KeyEvent.VK_1 && e.getKeyCode( ) <= KeyEvent.VK_9 )
                 optionSelected = e.getKeyCode( ) - KeyEvent.VK_1;
             else
                 optionSelected = -1;
             keyPressed = true;
 
             if( this.optionsToShow.size( ) <= RESPONSE_TEXT_NUMBER_LINES )
                 selectDisplayedOption( );
             else if( optionSelected >= firstLineDisplayed && optionSelected <= numberDisplayedOptions + firstLineDisplayed )
                 selectNoAllDisplayedOption( );
         }
     }
 
     @Override
     public void mouseMoved( MouseEvent e ) {
 
         if( GUI.getInstance( ).getResponseTextY( ) <= e.getY( ) )
             optionHighlighted = ( e.getY( ) - GUI.getInstance( ).getResponseTextY( ) ) / RESPONSE_TEXT_HEIGHT;
         else
             optionHighlighted = -1;
     }
 
     /**
      * Jumps to the next conversation line. If the current line was the last,
      * end the conversation and trigger the efects or jump to the next node
      */
     private void playNextLine( ) {
 
         if( game.getCharacterCurrentlyTalking( ) != null && game.getCharacterCurrentlyTalking( ).isTalking( ) )
             game.getCharacterCurrentlyTalking( ).stopTalking( );
 
         if( currentLine < currentNode.getLineCount( ) )
             playNextLineInNode( );
         else
             skipToNextNode( );
     }
 
     /**
      * Play the next line in the current conversation Nod
      */
     private void playNextLineInNode( ) {
 
         ConversationLine line = currentNode.getLine( currentLine );
         TalkingElement talking = null;
 
         // Only talk if all conditions in current line are OK
         if( ( new FunctionalConditions( currentNode.getLine( currentLine ).getConditions( ) ).allConditionsOk( ) ) ) {
 
             if( line.isPlayerLine( ) )
                 talking = game.getFunctionalPlayer( );
             else {
                 if( line.getName( ).equals( "NPC" ) )
                     talking = game.getCurrentNPC( );
                 else
                     talking = game.getFunctionalScene( ).getNPC( line.getName( ) );
             }
 
             if( talking != null ) {
                 if( line.isValidAudio( ) )
                     talking.speak( line.getText( ), line.getAudioPath( ) );
                 else if( line.getSynthesizerVoice( ) || talking.isAlwaysSynthesizer( ) )
                     talking.speakWithFreeTTS( line.getText( ), talking.getPlayerVoice( ) );
                 else
                     talking.speak( line.getText( ) );
             }
             game.setCharacterCurrentlyTalking( talking );
         }
         currentLine++;
     }
 
     /**
      * Skip to the next node in the conversation of finish the conversation.
      * Consume the effects of the current node.
      */
     private void skipToNextNode( ) {
 
         if( currentNode.hasValidEffect( ) && !currentNode.isEffectConsumed( ) ) {
             currentNode.consumeEffect( );
             game.pushCurrentState( this );
             FunctionalEffects.storeAllEffects( currentNode.getEffects( ), true );
             GUI.getInstance( ).toggleHud( true );
         }
         else if( ( !currentNode.hasValidEffect( ) || currentNode.isEffectConsumed( ) ) && currentNode.isTerminal( ) ) {
             endConversation( );
         }
         else if( !currentNode.isTerminal( ) ) {
             currentNode = currentNode.getChild( 0 );
             firstLineDisplayed = 0;
             currentLine = 0;
         }
     }
 
     /**
      * @param convName
      *            the convName to set
      */
     public void setConvID( String convName ) {
 
         this.convID = convName;
     }
 
     /**
      * @return the convID
      */
     public String getConvID( ) {
 
         return convID;
     }
 }
