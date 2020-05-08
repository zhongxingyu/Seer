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
 package es.eucm.eadventure.editor.control.controllers.general;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import es.eucm.eadventure.common.auxiliar.ReportDialog;
 import es.eucm.eadventure.common.data.chapter.Action;
 import es.eucm.eadventure.common.data.chapter.CustomAction;
 import es.eucm.eadventure.common.data.chapter.effects.TriggerConversationEffect;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.controllers.DataControl;
 import es.eucm.eadventure.editor.control.controllers.Searchable;
 import es.eucm.eadventure.editor.control.controllers.character.NPCDataControl;
 import es.eucm.eadventure.editor.control.controllers.item.ItemDataControl;
 import es.eucm.eadventure.editor.control.controllers.scene.ActiveAreaDataControl;
 import es.eucm.eadventure.editor.data.support.VarFlagSummary;
 
 public class ActionsListDataControl extends DataControl {
 
     /**
      * List of actions.
      */
     private List<Action> actionsList;
 
     /**
      * List of action controllers.
      */
     private List<ActionDataControl> actionsDataControlList;
 
     private DataControl parent;
 
     /**
      * Constructor.
      * 
      * @param actionsList
      *            List of actions
      */
     public ActionsListDataControl( List<Action> actionsList, DataControl parent ) {
 
         this.actionsList = actionsList;
         this.parent = parent;
 
         // Create subcontrollers
         actionsDataControlList = new ArrayList<ActionDataControl>( );
         for( Action action : actionsList ) {
             if( action.getType( ) == Action.CUSTOM || action.getType( ) == Action.CUSTOM_INTERACT ) {
                 actionsDataControlList.add( new CustomActionDataControl( (CustomAction) action ) );
             }
             else if (action.getType( ) == Action.TALK_TO)
                 actionsDataControlList.add( new ActionDataControl( action , ((NPCDataControl)parent).getId( )) );
             else 
                 actionsDataControlList.add( new ActionDataControl( action ) );
         }
     }
 
     /**
      * Returns the list of action controllers.
      * 
      * @return Action controllers
      */
     public List<ActionDataControl> getActions( ) {
 
         return actionsDataControlList;
     }
 
     /**
      * Returns the last action controller of the list.
      * 
      * @return Last action controller
      */
     public ActionDataControl getLastAction( ) {
 
         return actionsDataControlList.get( actionsDataControlList.size( ) - 1 );
     }
 
     /**
      * Returns the info of the actions contained in the list.
      * 
      * @return Array with the information of the actions. It contains the type
      *         of the action, and information about whether they have conditions
      *         and effects
      */
     public String[][] getActionsInfo( ) {
 
         String[][] actionsInfo = null;
 
         // Create the list for the actions
         actionsInfo = new String[ actionsList.size( ) ][ 3 ];
 
         // Fill the array with the info
         for( int i = 0; i < actionsList.size( ); i++ ) {
             Action action = actionsList.get( i );
 
             if( action.getType( ) == Action.EXAMINE )
                 actionsInfo[i][0] = TC.get( "ActionsList.ExamineAction" );
             else if( action.getType( ) == Action.GRAB )
                 actionsInfo[i][0] = TC.get( "ActionsList.GrabAction" );
             else if( action.getType( ) == Action.CUSTOM )
                 actionsInfo[i][0] = TC.get( "ActionsList.CustomAction", ( (CustomAction) action ).getName( ) );
             else if( action.getType( ) == Action.GIVE_TO )
                 actionsInfo[i][0] = TC.get( "ActionsList.GiveToAction", action.getTargetId( ) );
             else if( action.getType( ) == Action.USE_WITH )
                 actionsInfo[i][0] = TC.get( "ActionsList.UseWithAction", action.getTargetId( ) );
             else if( action.getType( ) == Action.CUSTOM_INTERACT )
                 actionsInfo[i][0] = TC.get( "ActionsList.CustomInteractAction", action.getTargetId( ) );
             else if( action.getType( ) == Action.USE )
                 actionsInfo[i][0] = TC.get( "ActionsList.UseAction" );
             else if( action.getType( ) == Action.TALK_TO )
                 actionsInfo[i][0] = TC.get( "ActionsList.TalkToAction" );
             else if( action.getType( ) == Action.DRAG_TO )
                 actionsInfo[i][0] = TC.get( "ActionsList.DragToAction" );
 
             if( action.getConditions( ).isEmpty( ) )
                 actionsInfo[i][1] = TC.get( "GeneralText.No" );
             else
                 actionsInfo[i][1] = TC.get( "GeneralText.Yes" );
 
             if( action.getEffects( ).isEmpty( ) )
                 actionsInfo[i][2] = TC.get( "GeneralText.No" );
             else
                 actionsInfo[i][2] = TC.get( "GeneralText.Yes" );
         }
 
         return actionsInfo;
     }
 
     @Override
     public Object getContent( ) {
 
         return actionsList;
     }
 
     @Override
     public int[] getAddableElements( ) {
 
         if( parent instanceof ItemDataControl )
             return new int[] { Controller.ACTION_EXAMINE, Controller.ACTION_GRAB, Controller.ACTION_USE, Controller.ACTION_CUSTOM_INTERACT, Controller.ACTION_USE_WITH, Controller.ACTION_GIVE_TO, Controller.ACTION_DRAG_TO };
         if( parent instanceof NPCDataControl )
             return new int[] { Controller.ACTION_EXAMINE, Controller.ACTION_USE, Controller.ACTION_CUSTOM, Controller.ACTION_TALK_TO,  Controller.ACTION_DRAG_TO  };
         if( parent instanceof ActiveAreaDataControl )
             return new int[] { Controller.ACTION_EXAMINE, Controller.ACTION_GRAB, Controller.ACTION_USE, Controller.ACTION_CUSTOM_INTERACT };
         return new int[] { Controller.ACTION_EXAMINE, Controller.ACTION_GRAB, Controller.ACTION_USE, Controller.ACTION_CUSTOM, Controller.ACTION_USE_WITH, Controller.ACTION_GIVE_TO, Controller.ACTION_TALK_TO };
     }
 
     @Override
     public boolean canAddElement( int type ) {
 
         return type == Controller.ACTION_EXAMINE || type == Controller.ACTION_GRAB || type == Controller.ACTION_USE || type == Controller.ACTION_CUSTOM || type == Controller.ACTION_USE_WITH || type == Controller.ACTION_GIVE_TO || type == Controller.ACTION_CUSTOM_INTERACT || type == Controller.ACTION_TALK_TO || type == Controller.ACTION_DRAG_TO;
     }
 
     @Override
     public boolean canBeDeleted( ) {
 
         return false;
     }
 
     @Override
     public boolean canBeMoved( ) {
 
         return false;
     }
 
     @Override
     public boolean canBeRenamed( ) {
 
         return false;
     }
 
     @Override
     public boolean addElement( int type, String id ) {
 
         Action newAction = null;
 
         if( type == Controller.ACTION_EXAMINE )
             newAction = new Action( Action.EXAMINE );
 
         else if( type == Controller.ACTION_GRAB )
             newAction = new Action( Action.GRAB );
 
         else if( type == Controller.ACTION_USE )
             newAction = new Action( Action.USE );
 
         else if( type == Controller.ACTION_TALK_TO ) {
             String[] conversations = controller.getIdentifierSummary( ).getConversationsIds( );
             if( conversations.length > 0 ) {
                 String selectedElement = controller.showInputDialog( TC.get( "Action.OperationAddAction" ), TC.get( "TalkToAction.MessageSelectConversation" ), conversations );
                 if( selectedElement != null ) {
                     newAction = new Action( Action.TALK_TO );
                     newAction.getEffects( ).add( new TriggerConversationEffect( selectedElement ) );
                 }
             }
             else
                 controller.showErrorDialog( TC.get( "Action.OperationAddAction" ), TC.get( "Action.ErrorNoItems" ) );
 
         }
         else if( type == Controller.ACTION_CUSTOM_INTERACT ) {
             //FIX: 
             //String name = JOptionPane.showInputDialog( null, TC.get( "CustomAction.GetNameMessage" ), TC.get( "CustomAction.GetNameTitle" ), JOptionPane.QUESTION_MESSAGE );
             String name = controller.showInputDialog(  TC.get( "CustomAction.GetNameTitle" ), TC.get( "CustomAction.GetNameMessage"));
             if( name == null || name.equals( "" ) ) {
                 name = "NONAME_" + ( new Random( ) ).nextInt( 1000 );
             }
 
            String[] options = { "Action", "Interaction" };
            //int option = JOptionPane.showOptionDialog( null, TC.get( "CustomAction.SelectTypeMessage" ), TC.get( "CustomAction.SelectTypeTitle" ), JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, 0 );
            int option = controller.showOptionDialog( TC.get( "CustomAction.SelectTypeTitle" ), TC.get( "CustomAction.SelectTypeMessage" ), options ) ;
             if( option == 0 ) {
                 newAction = new CustomAction( Action.CUSTOM );
                 ( (CustomAction) newAction ).setName( name );
             }
             else {
                 String[] items = controller.getIdentifierSummary( ).getItemAndActiveAreaIds( );
                 String[] npcs = controller.getIdentifierSummary( ).getNPCIds( );
                 String[] elements = new String[ items.length + npcs.length ];
                 for( int i = 0; i < elements.length; i++ ) {
                     if( i < items.length ) {
                         elements[i] = items[i];
                     }
                     else {
                         elements[i] = npcs[i - items.length];
                     }
                 }
 
                 // If the list has elements, show the dialog with the options
                 if( elements.length > 0 ) {
                     String selectedElement = controller.showInputDialog( TC.get( "Action.OperationAddAction" ), TC.get( "CustomAction.MessageSelectInteraction" ), elements );
 
                     // If some value was selected
                     if( selectedElement != null ) {
                         newAction = new CustomAction( Action.CUSTOM_INTERACT, selectedElement );
                         ( (CustomAction) newAction ).setName( name );
                     }
                 }
 
                 // If the list had no elements, show an error dialog
                 else
                     controller.showErrorDialog( TC.get( "Action.OperationAddAction" ), TC.get( "Action.ErrorNoItems" ) );
 
             }
         }
         else if( type == Controller.ACTION_CUSTOM ) {
             String name = controller.showInputDialog(  TC.get( "CustomAction.GetNameMessage" ), TC.get( "CustomAction.GetNameTitle" ) );
             if( name.equals( "" ) ) {
                 name = "NONAME_" + ( new Random( ) ).nextInt( 1000 );
             }
 
             newAction = new CustomAction( Action.CUSTOM );
             ( (CustomAction) newAction ).setName( name );
         }
 
         // If the type of action is use-with, we must ask for a second item
         else if( type == Controller.ACTION_USE_WITH ) {
             // Take the list of the items
             String[] items = controller.getIdentifierSummary( ).getItemAndActiveAreaIds( );
 
             // If the list has elements, show the dialog with the options
             if( items.length > 0 ) {
                 String selectedItem = controller.showInputDialog( TC.get( "Action.OperationAddAction" ), TC.get( "Action.MessageSelectItem" ), items );
 
                 // If some value was selected
                 if( selectedItem != null )
                     newAction = new Action( Action.USE_WITH, selectedItem );
             }
 
             // If the list had no elements, show an error dialog
             else
                 controller.showErrorDialog( TC.get( "Action.OperationAddAction" ), TC.get( "Action.ErrorNoItems" ) );
         }
 
         // If the type of action is drag-to, we must ask for a second item
         else if( type == Controller.ACTION_DRAG_TO ) {
             // Take the list of the items
             String[] items = controller.getIdentifierSummary( ).getItemActiveAreaNPCIds( );
 
             // If the list has elements, show the dialog with the options
             if( items.length > 0 ) {
                 String selectedItem = controller.showInputDialog( TC.get( "Action.OperationAddAction" ), TC.get( "Action.MessageSelectItem" ), items );
 
                 // If some value was selected
                 if( selectedItem != null )
                     newAction = new Action( Action.DRAG_TO, selectedItem );
             }
 
             // If the list had no elements, show an error dialog
             else
                 controller.showErrorDialog( TC.get( "Action.OperationAddAction" ), TC.get( "Action.ErrorNoItems" ) );
         }
 
         // If the type of action is give-to, we must ask for a character
         else if( type == Controller.ACTION_GIVE_TO ) {
             // Take the list of the characters
             String[] npcs = controller.getIdentifierSummary( ).getNPCIds( );
 
             // If the list has elements, show the dialog with the options
             if( npcs.length > 0 ) {
                 String selectedNPC = controller.showInputDialog( TC.get( "Action.OperationAddAction" ), TC.get( "Action.MessageSelectNPC" ), npcs );
 
                 // If some value was selected
                 if( selectedNPC != null )
                     newAction = new Action( Action.GIVE_TO, selectedNPC );
             }
 
             // If the list had no elements, show an error dialog
             else
                 controller.showErrorDialog( TC.get( "Action.OperationAddAction" ), TC.get( "Action.ErrorNoNPCs" ) );
         }
 
         // If an action was added, create a controller and store it
         if( newAction != null ) {
             actionsList.add( newAction );
             if( newAction.getType( ) == Action.CUSTOM || newAction.getType( ) == Action.CUSTOM_INTERACT )
                 actionsDataControlList.add( new CustomActionDataControl( (CustomAction) newAction ) );
             else
                 actionsDataControlList.add( new ActionDataControl( newAction ) );
             //controller.dataModified( );
         }
 
         return newAction != null;
     }
 
     @Override
     public boolean deleteElement( DataControl dataControl, boolean askConfirmation ) {
 
         boolean elementDeleted = false;
 
         if( actionsList.remove( dataControl.getContent( ) ) ) {
             actionsDataControlList.remove( dataControl );
             //controller.dataModified( );
             elementDeleted = true;
         }
 
         return elementDeleted;
     }
 
     @Override
     public boolean duplicateElement( DataControl dataControl ) {
 
         if( !( dataControl instanceof ActionDataControl ) )
             return false;
 
         try {
             Action newElement = (Action) ( ( (Action) ( dataControl.getContent( ) ) ).clone( ) );
             actionsList.add( newElement );
             actionsDataControlList.add( new ActionDataControl( newElement ) );
             return true;
         }
         catch( CloneNotSupportedException e ) {
             ReportDialog.GenerateErrorReport( e, true, "Could not clone action" );
             return false;
         }
     }
 
     @Override
     public boolean moveElementUp( DataControl dataControl ) {
 
         boolean elementMoved = false;
         int elementIndex = actionsList.indexOf( dataControl.getContent( ) );
 
         if( elementIndex > 0 ) {
             actionsList.add( elementIndex - 1, actionsList.remove( elementIndex ) );
             actionsDataControlList.add( elementIndex - 1, actionsDataControlList.remove( elementIndex ) );
             controller.dataModified( );
             elementMoved = true;
               
         }
         
         return elementMoved;
     }
 
     @Override
     public boolean moveElementDown( DataControl dataControl ) {
 
         boolean elementMoved = false;
         int elementIndex = actionsList.indexOf( dataControl.getContent( ) );
 
         if( elementIndex < actionsList.size( ) - 1 ) {
             actionsList.add( elementIndex + 1, actionsList.remove( elementIndex ) );
             actionsDataControlList.add( elementIndex + 1, actionsDataControlList.remove( elementIndex ) );
             controller.dataModified( );
             elementMoved = true;
         }
 
         return elementMoved;
     }
 
     @Override
     public String renameElement( String name ) {
 
         return null;
     }
 
     @Override
     public void updateVarFlagSummary( VarFlagSummary varFlagSummary ) {
 
         // Iterate through each action
         for( ActionDataControl actionDataControl : actionsDataControlList )
             actionDataControl.updateVarFlagSummary( varFlagSummary );
     }
 
     @Override
     public boolean isValid( String currentPath, List<String> incidences ) {
 
         boolean valid = true;
 
         // Iterate through the actions
         for( int i = 0; i < actionsDataControlList.size( ); i++ ) {
             String actionPath = currentPath + " >> " + TC.get( "Element.Action" ) + " #" + ( i + 1 ) + " (" + TC.getElement( actionsDataControlList.get( i ).getType( ) ) + ")";
             valid &= actionsDataControlList.get( i ).isValid( actionPath, incidences );
         }
 
         return valid;
     }
 
     @Override
     public int countAssetReferences( String assetPath ) {
 
         int count = 0;
 
         // Iterate through each action
         for( ActionDataControl actionDataControl : actionsDataControlList )
             count += actionDataControl.countAssetReferences( assetPath );
 
         return count;
     }
 
     @Override
     public void getAssetReferences( List<String> assetPaths, List<Integer> assetTypes ) {
 
         // Iterate through each action
         for( ActionDataControl actionDataControl : actionsDataControlList )
             actionDataControl.getAssetReferences( assetPaths, assetTypes );
     }
 
     @Override
     public void deleteAssetReferences( String assetPath ) {
 
         // Iterate through each action
         for( ActionDataControl actionDataControl : actionsDataControlList )
             actionDataControl.deleteAssetReferences( assetPath );
     }
 
     @Override
     public int countIdentifierReferences( String id ) {
 
         int count = 0;
 
         // Iterate through each action
         for( ActionDataControl actionDataControl : actionsDataControlList )
             count += actionDataControl.countIdentifierReferences( id );
 
         return count;
     }
 
     @Override
     public void replaceIdentifierReferences( String oldId, String newId ) {
 
         // Iterate through each action
         for( ActionDataControl actionDataControl : actionsDataControlList )
             actionDataControl.replaceIdentifierReferences( oldId, newId );
     }
 
     @Override
     public void deleteIdentifierReferences( String id ) {
 
         int i = 0;
 
         // Check every action
         while( i < actionsList.size( ) ) {
             Action action = actionsList.get( i );
 
             // If the action has a reference to the identifier, delete it
             if( ( action.getType( ) == Action.GIVE_TO || action.getType( ) == Action.USE_WITH || action.getType() == Action.DRAG_TO) && action.getTargetId( ).equals( id ) ) {
                 actionsList.remove( i );
                 actionsDataControlList.remove( i );
             }
             else if( ( action.getType( ) == Action.CUSTOM_INTERACT && action.getTargetId( ).equals( id ) ) ) {
                 actionsList.remove( i );
                 actionsDataControlList.remove( i );
             }
 
             // If not, spread the call and increase the counter
             else {
                 actionsDataControlList.get( i ).deleteIdentifierReferences( id );
                 i++;
             }
         }
 
     }
 
     @Override
     public boolean canBeDuplicated( ) {
 
         return false;
     }
 
     @Override
     public void recursiveSearch( ) {
 
         for( DataControl dc : this.actionsDataControlList )
             dc.recursiveSearch( );
     }
 
     @Override
     public List<Searchable> getPathToDataControl( Searchable dataControl ) {
 
         return getPathFromChild( dataControl, actionsDataControlList );
     }
 
     public List<Action> getActionsList( ) {
 
         return this.actionsList;
     }
 
 }
