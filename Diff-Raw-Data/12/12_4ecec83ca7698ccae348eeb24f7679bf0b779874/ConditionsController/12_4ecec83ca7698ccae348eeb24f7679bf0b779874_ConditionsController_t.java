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
 package es.eucm.eadventure.editor.control.controllers;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import es.eucm.eadventure.common.data.chapter.conditions.Condition;
 import es.eucm.eadventure.common.data.chapter.conditions.Conditions;
 import es.eucm.eadventure.common.data.chapter.conditions.FlagCondition;
 import es.eucm.eadventure.common.data.chapter.conditions.VarCondition;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.tools.general.conditions.AddConditionTool;
 import es.eucm.eadventure.editor.control.tools.general.conditions.DeleteConditionTool;
 import es.eucm.eadventure.editor.control.tools.general.conditions.DuplicateConditionTool;
 import es.eucm.eadventure.editor.control.tools.general.conditions.SetConditionTool;
 import es.eucm.eadventure.editor.control.tools.general.conditions.SetEvalFunctionTool;
 import es.eucm.eadventure.editor.data.support.VarFlagSummary;
 
 /**
  * Controller for the conditions. This class operates on the conditions blocks
  * to add new single conditions, and either conditions blocks.
  * 
  * @author Javier Torrente Vigil
  */
 public class ConditionsController {
 
     public static final int VAR_CONDITION = Condition.VAR_CONDITION;
 
     public static final int FLAG_CONDITION = Condition.FLAG_CONDITION;
 
     public static final int GLOBAL_STATE_CONDITION = Condition.GLOBAL_STATE_CONDITION;
 
     /*
      * Constants for KEYs (and some VALUES) of the HashMap getCondition returns
      */
     // FLAG; VAR; GLOBAL_STATE
     public static final String CONDITION_TYPE = "condition-type";
 
     public static final String CONDITION_TYPE_VAR = "var";
 
     public static final String CONDITION_TYPE_FLAG = "flag";
 
     public static final String CONDITION_TYPE_GS = "global-state";
 
     public static final String CONDITION_ID = "condition-id";
 
     public static final String CONDITION_VALUE = "condition-value";
 
     //active|inactive|<,<=,>,>=,=
     public static final String CONDITION_STATE = "condition-state";
 
     /*
      * Constants for setEvalFunction (param value)
      */
     public static final int EVAL_FUNCTION_AND = 0;
 
     public static final int EVAL_FUNCTION_OR = 1;
 
     // Constant for setEvalFunction (index)
     public static final int INDEX_NOT_USED = -1;
 
     // KEYS FOR Context HashMap
     public static String CONDITION_GROUP_TYPE = "condition-type";
 
     public static String CONDITION_RESTRICTIONS = "condition-restrictions";
 
     public static String CONDITION_OWNER = "condition-owner";
 
     public static String CONDITION_CUSTOM_MESSAGE = "condition-custom-message";
 
     /**
      * String values for the states of a condition
      */
     public static final String[] STATE_VALUES = { "Active", "Inactive", ">", ">=", "<", "<=", "=" };
 
     /**
      * String values for the states of a flag condition
      */
     public static final String[] STATE_VALUES_FLAGS = { "Active", "Inactive" };
 
     /**
      * String values for the states of a varcondition
      */
     public static final String[] STATE_VALUES_VARS = { ">", ">=", "<", "<=", "=" };
 
     /**
      * Returns the int value of the string state given.
      * 
      * @param stringState
      *            Condition state in String form
      * @return Int value of state
      */
     public static int getStateFromString( String stringState ) {
 
         int state = Condition.NO_STATE;
 
         if( stringState.equals( STATE_VALUES[0] ) )
             state = FlagCondition.FLAG_ACTIVE;
 
         else if( stringState.equals( STATE_VALUES[1] ) )
             state = FlagCondition.FLAG_INACTIVE;
 
         else if( stringState.equals( STATE_VALUES[2] ) )
             state = VarCondition.VAR_GREATER_THAN;
 
         else if( stringState.equals( STATE_VALUES[3] ) )
             state = VarCondition.VAR_GREATER_EQUALS_THAN;
 
         else if( stringState.equals( STATE_VALUES[4] ) )
             state = VarCondition.VAR_LESS_THAN;
 
         else if( stringState.equals( STATE_VALUES[5] ) )
             state = VarCondition.VAR_LESS_EQUALS_THAN;
 
         else if( stringState.equals( STATE_VALUES[6] ) )
             state = VarCondition.VAR_EQUALS;
         return state;
     }
 
     public static int getTypeFromString( String newType ) {
 
         int newTypeInt = -1;
         if( newType.equals( ConditionsController.CONDITION_TYPE_FLAG ) )
             newTypeInt = Condition.FLAG_CONDITION;
         if( newType.equals( ConditionsController.CONDITION_TYPE_VAR ) )
             newTypeInt = Condition.VAR_CONDITION;
         if( newType.equals( ConditionsController.CONDITION_TYPE_GS ) )
             newTypeInt = Condition.GLOBAL_STATE_CONDITION;
         return newTypeInt;
     }
 
     /**
      * Updates the given flag summary, adding the flag references contained in
      * the given conditions.
      * 
      * @param varFlagSummary
      *            Flag summary to update
      * @param conditions
      *            Set of conditions to search in
      */
     public static void updateVarFlagSummary( VarFlagSummary varFlagSummary, Conditions conditions ) {
 
         // First check the main block of conditions
         for( Condition condition : conditions.getSimpleConditions( ) ) {
             if( condition.getType( ) == Condition.FLAG_CONDITION )
                 varFlagSummary.addFlagReference( condition.getId( ) );
             else if( condition.getType( ) == Condition.VAR_CONDITION )
                 varFlagSummary.addVarReference( condition.getId( ) );
         }
 
         // Then add the references from the either blocks
         for( int i = 0; i < conditions.getEitherConditionsBlockCount( ); i++ )
             for( Condition condition : conditions.getEitherConditions( i ) ) {
                 if( condition.getType( ) == Condition.FLAG_CONDITION )
                     varFlagSummary.addFlagReference( condition.getId( ) );
                 else if( condition.getType( ) == Condition.VAR_CONDITION )
                     varFlagSummary.addVarReference( condition.getId( ) );
             }
     }
 
     private static HashMap<String, ConditionContextProperty> createContextFromOwner( int ownerType, String ownerName ) {
 
         HashMap<String, ConditionContextProperty> context1 = new HashMap<String, ConditionContextProperty>( );
         ConditionOwner owner = new ConditionOwner( ownerType, ownerName );
         context1.put( ConditionsController.CONDITION_OWNER, owner );
 
         if( TC.containsConditionsContextText( ownerType, TC.NORMAL_SENTENCE ) && TC.containsConditionsContextText( ownerType, TC.NO_CONDITION_SENTENCE ) ) {
             ConditionCustomMessage cMessage = new ConditionCustomMessage( TC.getConditionsContextText( ownerType, TC.NORMAL_SENTENCE ), TC.getConditionsContextText( ownerType, TC.NO_CONDITION_SENTENCE ) );
             context1.put( CONDITION_CUSTOM_MESSAGE, cMessage );
         }
 
         return context1;
     }
 
     private static HashMap<String, ConditionContextProperty> createContextFromOwnerMessage( int ownerType, String ownerName, String message1, String message2 ) {
 
         HashMap<String, ConditionContextProperty> context1 = new HashMap<String, ConditionContextProperty>( );
         ConditionOwner owner = new ConditionOwner( ownerType, ownerName );
 
         ConditionCustomMessage cMessage = new ConditionCustomMessage( message1, message2 );
         context1.put( CONDITION_CUSTOM_MESSAGE, cMessage );
         context1.put( ConditionsController.CONDITION_OWNER, owner );
         return context1;
     }
 
     // Attributes
     private HashMap<String, ConditionContextProperty> context;
 
     /**
      * Conditions data.
      */
     private Conditions conditions;
 
     // Constructors
 
     /**
      * Creates a new conditions controller with an empty context. Thus the
      * controller will not know anything about its context (where it is, the
      * parent it belongs to, special restrictions, etc.)
      */
     public ConditionsController( Conditions conditions ) {
 
         this( conditions, new HashMap<String, ConditionContextProperty>( ) );
     }
 
     /**
      * Creates a new conditions controller and sets the context with information
      * about the parent of the conditions (that is, the node of the data tree
      * which owns the conditions). The information provided is the type of the
      * owner (e.g. Controller.ACTIVE_AREA) and its name. This information will
      * only be used by the ConditionsPanel
      * 
      * @param conditions
      * @param ownerType
      * @param ownerName
      */
     public ConditionsController( Conditions conditions, int ownerType, String ownerName ) {
 
         this( conditions, createContextFromOwner( ownerType, ownerName ) );
     }
 
     /**
      * Creates a new conditions controller and sets the context with information
      * about the parent of the conditions (that is, the node of the data tree
      * which owns the conditions). In addition customized messages for
      * ConditionsPanel are provided. The information provided is the type of the
      * owner (e.g. Controller.ACTIVE_AREA) and its name. This information will
      * only be used by the ConditionsPanel, along with the messages used to
      * format the text to be displayed
      * 
      * @param conditions
      * @param ownerType
      * @param ownerName
      * @param message1
      * @param message2
      */
     public ConditionsController( Conditions conditions, int ownerType, String ownerName, String message1, String message2 ) {
 
         this( conditions, createContextFromOwnerMessage( ownerType, ownerName, message1, message2 ) );
     }
 
     /**
      * Constructor.
      * 
      * @param conditions
      *            Conditions block of data
      */
     public ConditionsController( Conditions conditions, HashMap<String, ConditionContextProperty> context ) {
 
         this.conditions = conditions;
         this.context = context;
         if( context.containsKey( CONDITION_OWNER ) ) {
             ConditionOwner owner = (ConditionOwner) context.get( CONDITION_OWNER );
             if( owner.getOwnerType( ) == Controller.GLOBAL_STATE ) {
                 ConditionRestrictions restrictions = new ConditionRestrictions( new String[] { owner.getOwnerName( ) } );
                 this.context.put( CONDITION_RESTRICTIONS, restrictions );
             }
         }
     }
 
     /**
      * Returns the number of blocks of the condition group. Simple conditions
      * and either groups are considered as blocks.
      * 
      * @return The number of blocks
      */
     public int getBlocksCount( ) {
 
         return conditions.size( );
     }
 
     /**
      * Returns true if the conditions group has no blocks. False otherwise
      * 
      * @return
      */
     public boolean isEmpty( ) {
 
         return conditions.isEmpty( );
     }
 
     /**
      * Returns the number of simple conditions a block with the given index has.
      * If index1 is not in the range -1 is returned. This method can be used to
      * determine if a block is a simple condition (size==1) or an either block
      * (size>1). This method never returns 0
      * 
      * @param index1
      * @return
      */
     public int getConditionCount( int index1 ) {
 
         // Check index
         if( index1 < 0 || index1 >= conditions.size( ) )
             return -1;
 
         return conditions.get( index1 ).size( );
     }
 
     /**
      * Deletes the selected condition, if indexes are in range
      * 
      * @param index1
      * @param index2
      * @return
      */
     public boolean deleteCondition( int index1, int index2 ) {
 
         // Check index
         if( index1 < 0 || index1 >= conditions.size( ) )
             return false;
 
         if( index2 < 0 || index2 >= conditions.get( index1 ).size( ) )
             return false;
 
         return Controller.getInstance( ).addTool( new DeleteConditionTool( conditions, index1, index2 ) );
     }
 
     /**
      * Duplicates the selected condition
      * 
      * @param index1
      * @param index2
      * @return
      */
     public boolean duplicateCondition( int index1, int index2 ) {
 
         // Check index
         if( index1 < 0 || index1 >= conditions.size( ) )
             return false;
 
         if( index2 < 0 || index2 >= conditions.get( index1 ).size( ) )
             return false;
 
         return Controller.getInstance( ).addTool( new DuplicateConditionTool( conditions, index1, index2 ) );
     }
 
     public boolean setCondition( int index1, int index2, HashMap<String, String> properties ) {
 
         // Check index
         if( index1 < 0 || index1 >= conditions.size( ) )
             return false;
 
         if( index2 < 0 || index2 >= conditions.get( index1 ).size( ) )
             return false;
         // here CONDITION_STATE has a default value in "Global State Condition", not is necessary
         if( properties.get( CONDITION_TYPE ) == null || properties.get( CONDITION_ID ) == null || properties.get( CONDITION_STATE ) == null )
             return false;
 
         if( properties.get( CONDITION_TYPE ).equals( ConditionsController.CONDITION_TYPE_VAR ) && properties.get( CONDITION_VALUE ) == null )
             return false;
 
         return Controller.getInstance( ).addTool( new SetConditionTool( conditions, index1, index2, properties ) );
     }
 
     /**
      * Delete all global states with "id"
      * 
      * @param id
      *            the global state identifier to delete
      */
     public void deleteIdentifierReferences( String id ) {
 
         Iterator<List<Condition>> listIt = conditions.getConditionsList( ).iterator( );
         while( listIt.hasNext( ) ) {
             List<Condition> wrapper = listIt.next( );
             Iterator<Condition> it = wrapper.iterator( );
             while( it.hasNext( ) ) {
                 Condition condition = it.next( );
                 if( condition.getType( ) == Condition.GLOBAL_STATE_CONDITION && condition.getId( ).equals( id ) ) {
                     it.remove( );
                 }
             }
             if( wrapper.size( ) == 0 ) {
                 listIt.remove( );
             }
 
         }
     }
 
     /**
      * Replace identifiers, if oldId is found.
      * 
      * @param oldId
      * @param newId
      */
     public void replaceIdentifierReferences( String oldId, String newId ) {
        
       for( int i = 0; i < conditions.size( );i++) {
            for( int j = 0; j < conditions.get( i ).size( ); j++ ) {
                if (conditions.get( i ).get( j ).getId( ).equals( oldId ))
                        conditions.get( i ).get( j ).setId( newId );
            }
         }
         
     }
 
     /**
      * Count the number of times that id appears in conditions
      * 
      * @param id
      * @return
      */
     public int countIdentifierReferences( String id ) {
 
         int count = 0;
         for( String conditionId : conditions.getGloblaStateIds( ) ) {
             if( conditionId.equals( id ) )
                 count++;
         }
 
         return count;
     }
 
     public HashMap<String, String> getCondition( int index1, int index2 ) {
 
         HashMap<String, String> conditionProperties = new HashMap<String, String>( );
 
         // Check index
         if( index1 < 0 || index1 >= conditions.size( ) )
             return null;
 
         List<Condition> conditionsList = conditions.get( index1 );
         // Check index2
         if( index2 < 0 || index2 >= conditionsList.size( ) )
             return null;
 
         Condition condition = conditionsList.get( index2 );
         // Put ID
         conditionProperties.put( CONDITION_ID, condition.getId( ) );
 
         // Put State
         conditionProperties.put( CONDITION_STATE, Integer.toString( condition.getState( ) ) );
         // Put Type
         if( condition.getType( ) == Condition.FLAG_CONDITION ) {
             conditionProperties.put( CONDITION_TYPE, CONDITION_TYPE_FLAG );
             //Put value
             conditionProperties.put( CONDITION_VALUE, Integer.toString( condition.getState( ) ) );
         }
         else if( condition.getType( ) == Condition.VAR_CONDITION ) {
             conditionProperties.put( CONDITION_TYPE, CONDITION_TYPE_VAR );
             //Put value
             VarCondition varCondition = (VarCondition) condition;
             conditionProperties.put( CONDITION_VALUE, Integer.toString( varCondition.getValue( ) ) );
         }
         else if( condition.getType( ) == Condition.GLOBAL_STATE_CONDITION ) {
             conditionProperties.put( CONDITION_TYPE, CONDITION_TYPE_GS );
             //Put value
             conditionProperties.put( CONDITION_VALUE, Integer.toString( condition.getState( ) ) );
         }
 
         return conditionProperties;
     }
 
     /**
      * Adds a new condition to the given block.
      * 
      * @param blockIndex
      *            The index of the conditions block. Use MAIN_CONDITIONS_BLOCK
      *            (-1) to select the main block of conditions, and values from 0
      *            to getEitherConditionsBlockCount( ) to access the either
      *            blocks of conditions
      * @param conditionId
      *            Id of the condition
      * @param conditionState
      *            State of the condition
      */
     public boolean addCondition( int index1, int index2, String conditionType, String conditionId, String conditionState, String value ) {
 
         if( index1 < 0 || index1 > conditions.size( ) )
             return false;
 
         if( conditionType == null || conditionId == null || conditionState == null )
             return false;
 
         if( conditionType.equals( ConditionsController.CONDITION_TYPE_VAR ) && value == null )
             return false;
 
         return Controller.getInstance( ).addTool( new AddConditionTool( conditions, index1, index2, conditionType, conditionId, conditionState, value ) );
     }
 
     /**
      * Sets the evaluation function at the given index
      * 
      * @param index1
      *            Int value in rage
      * @param index2
      *            Int value in rage or {@link #INDEX_NOT_USED} if not applicable
      * @param value
      *            {@link #EVAL_FUNCTION_AND} | {@link #EVAL_FUNCTION_OR}
      * @return
      */
     public boolean setEvalFunction( int index1, int index2, int value ) {
 
         // Check value
         if( value != EVAL_FUNCTION_AND && value != EVAL_FUNCTION_OR )
             return false;
 
         // Check index
         // Check if the algorithm must search deeper (index2==-1 means no search inside blocks must be carried out)
         if( index2 == -1 ) {
             if( index1 < 0 || index1 >= conditions.size( ) - 1 )
                 return false;
         }
         else if( index2 >= 0 ) {
             if( index1 < 0 || index1 >= conditions.size( ) )
                 return false;
         }
 
         return Controller.getInstance( ).addTool( new SetEvalFunctionTool( conditions, index1, index2, value ) );
     }
 
     //Condition type. Values: GLOBAL_STATE | CONDITION
 
     /**
      * The HashMap of the context
      */
     public HashMap<String, ConditionContextProperty> getContext( ) {
 
         return context;
     }
 
     public static abstract class ConditionContextProperty {
 
         public ConditionContextProperty( String type ) {
 
             this.type = type;
         }
 
         private String type;
 
         public String getType( ) {
 
             return type;
         }
     }
 
     public static class ConditionRestrictions extends ConditionContextProperty {
 
         private String[] forbiddenIds;
 
         public ConditionRestrictions( String[] forbiddenIds ) {
 
             super( CONDITION_RESTRICTIONS );
             this.forbiddenIds = forbiddenIds;
         }
 
         public ConditionRestrictions( String forbiddenId ) {
 
             this( new String[] { forbiddenId } );
         }
 
         public String[] getForbiddenIds( ) {
 
             return forbiddenIds;
         }
     }
 
     /**
      * @author Javier
      * 
      */
     public static class ConditionCustomMessage extends ConditionContextProperty {
 
         public static final String ELEMENT_TYPE = "{#ELEMENT_TYPE$}";
 
         public static final String ELEMENT_ID = "{#ELEMENT_ID$}";
 
         private String sentence;
 
         private String sentenceNoConditions;
 
         public ConditionCustomMessage( String[] sentenceStrings, String[] noConditionStrings ) {
 
             super( CONDITION_CUSTOM_MESSAGE );
 
             sentence = "";
             for( String string : sentenceStrings ) {
                 sentence += string + " ";
             }
             if( sentenceStrings.length > 0 ) {
                 sentence = sentence.substring( 0, sentence.length( ) - 1 );
             }
 
             sentenceNoConditions = "";
             for( String string : noConditionStrings ) {
                 sentenceNoConditions += string + " ";
             }
             if( noConditionStrings.length > 0 ) {
                 sentenceNoConditions = sentenceNoConditions.substring( 0, sentenceNoConditions.length( ) - 1 );
             }
         }
 
         public ConditionCustomMessage( List<String> sentenceStrings, List<String> noConditionStrings ) {
 
             this( sentenceStrings.toArray( new String[] {} ), noConditionStrings.toArray( new String[] {} ) );
         }
 
         public ConditionCustomMessage( String sentence, String noConditionSentence ) {
 
             super( CONDITION_CUSTOM_MESSAGE );
             this.sentence = sentence;
             this.sentenceNoConditions = noConditionSentence;
         }
 
         private String formatSentence( ConditionOwner owner, String sentence ) {
 
             String formattedSentence = new String( sentence );
             if( sentence.contains( ELEMENT_TYPE ) ) {
                 formattedSentence = formattedSentence.replace( ELEMENT_TYPE, "<i>" + TC.getElement( owner.getOwnerType( ) ) + "</i>" );
             }
             if( sentence.contains( ELEMENT_ID ) ) {
                 formattedSentence = formattedSentence.replace( ELEMENT_ID, "<b>\"" + owner.getOwnerName( ) + "\"</b>" );
             }
             return formattedSentence;
         }
 
         public String getFormattedSentence( ConditionOwner owner ) {
 
             return formatSentence( owner, sentence );
         }
 
         public String getNoConditionFormattedSentence( ConditionOwner owner ) {
 
             return formatSentence( owner, sentenceNoConditions );
         }
 
     }
 
     /**
      * Class associated with KEY #CONDITION_OWNER
      * 
      * @author Javier
      * 
      */
     public static class ConditionOwner extends ConditionContextProperty {
 
         private int ownerType;
 
         private String ownerName;
 
         private ConditionOwner parent;
 
         public ConditionOwner( int ownerType, String ownerName, ConditionOwner parent ) {
 
             super( CONDITION_OWNER );
             this.ownerType = ownerType;
             this.ownerName = ownerName;
             this.parent = parent;
         }
 
         public ConditionOwner( int ownerType, String ownerName ) {
 
             this( ownerType, ownerName, null );
         }
 
         /**
          * @return the ownerType
          */
         public int getOwnerType( ) {
 
             return ownerType;
         }
 
         /**
          * @return the owner name
          */
         public String getOwnerName( ) {
 
             return ownerName;
         }
 
         /**
          * @return the owner name
          */
         public ConditionOwner getParent( ) {
 
             return parent;
         }
 
     }
 }
