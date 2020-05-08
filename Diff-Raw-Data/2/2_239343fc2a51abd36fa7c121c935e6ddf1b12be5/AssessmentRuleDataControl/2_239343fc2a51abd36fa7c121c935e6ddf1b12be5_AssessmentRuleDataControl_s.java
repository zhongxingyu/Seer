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
 package es.eucm.eadventure.editor.control.controllers.assessment;
 
 import java.util.HashMap;
 import java.util.List;
 
 import es.eucm.eadventure.common.data.assessment.AssessmentProfile;
 import es.eucm.eadventure.common.data.assessment.AssessmentProperty;
 import es.eucm.eadventure.common.data.assessment.AssessmentRule;
 import es.eucm.eadventure.common.data.assessment.TimedAssessmentEffect;
 import es.eucm.eadventure.common.data.assessment.TimedAssessmentRule;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.config.SCORMConfigData;
 import es.eucm.eadventure.editor.control.controllers.ConditionsController;
 import es.eucm.eadventure.editor.control.controllers.DataControl;
 import es.eucm.eadventure.editor.control.controllers.Searchable;
 import es.eucm.eadventure.editor.control.controllers.ConditionsController.ConditionContextProperty;
 import es.eucm.eadventure.editor.control.controllers.ConditionsController.ConditionCustomMessage;
 import es.eucm.eadventure.editor.control.controllers.ConditionsController.ConditionOwner;
 import es.eucm.eadventure.editor.control.tools.assessment.AddAssessmentPropertyTool;
 import es.eucm.eadventure.editor.control.tools.assessment.AddEffectTool;
 import es.eucm.eadventure.editor.control.tools.assessment.ChangeAssessmentPropertyTool;
 import es.eucm.eadventure.editor.control.tools.assessment.ChangeMinTimeValueTool;
 import es.eucm.eadventure.editor.control.tools.assessment.ChangeUsesEndCondition;
 import es.eucm.eadventure.editor.control.tools.assessment.DeleteAssessmentPropertyTool;
 import es.eucm.eadventure.editor.control.tools.assessment.DeleteEffectTool;
 import es.eucm.eadventure.editor.control.tools.assessment.RenameRuleTool;
 import es.eucm.eadventure.editor.control.tools.general.commontext.ChangeIdTool;
 import es.eucm.eadventure.editor.control.tools.generic.ChangeIntegerValueTool;
 import es.eucm.eadventure.editor.control.tools.generic.ChangeStringValueTool;
 import es.eucm.eadventure.editor.control.tools.generic.MoveObjectTool;
 import es.eucm.eadventure.editor.data.support.VarFlagSummary;
 import es.eucm.eadventure.editor.gui.editdialogs.SCORMAttributeDialog;
 
 public class AssessmentRuleDataControl extends DataControl {
     
     
     
     private AssessmentRule assessmentRule;
 
     private ConditionsController conditionsController;
 
     private ConditionsController initConditionsController;
 
     private ConditionsController endConditionsController;
     
     private AssessmentProfile profile;
 
 
     public AssessmentRuleDataControl( AssessmentRule assessmentRule, AssessmentProfile profile ) {
 
         this.assessmentRule = assessmentRule;
         this.profile = profile;
 
         // Create subcontrollers
         if( this.isTimedRule( ) ) {
             TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
 
             HashMap<String, ConditionContextProperty> context1 = new HashMap<String, ConditionContextProperty>( );
             ConditionOwner owner = new ConditionOwner( Controller.TIMED_ASSESSMENT_RULE, assessmentRule.getId( ) );
             context1.put( ConditionsController.CONDITION_OWNER, owner );
             ConditionCustomMessage cMessage1 = new ConditionCustomMessage( TC.get( "Conditions.Context.TimedAssessmentRuleA1" ), TC.get( "Conditions.Context.TimedAssessmentRuleA2" ) );
             context1.put( ConditionsController.CONDITION_CUSTOM_MESSAGE, cMessage1 );
 
             HashMap<String, ConditionContextProperty> context2 = new HashMap<String, ConditionContextProperty>( );
             context2.put( ConditionsController.CONDITION_OWNER, owner );
             ConditionCustomMessage cMessage2 = new ConditionCustomMessage( TC.get( "Conditions.Context.TimedAssessmentRuleB1" ), TC.get( "Conditions.Context.TimedAssessmentRuleB2" ) );
             context2.put( ConditionsController.CONDITION_CUSTOM_MESSAGE, cMessage2 );
 
             initConditionsController = new ConditionsController( tRule.getInitConditions( ), context1 );
             endConditionsController = new ConditionsController( tRule.getEndConditions( ), context2 );
         }
         else {
             conditionsController = new ConditionsController( assessmentRule.getConditions( ), Controller.ASSESSMENT_RULE, assessmentRule.getId( ) );
         }
     }
 
     @Override
     public boolean addElement( int type, String id ) {
 
         return false;
     }
 
     @Override
     public boolean canAddElement( int type ) {
 
         return false;
     }
 
     @Override
     public boolean canBeDeleted( ) {
 
         return true;
     }
 
     @Override
     public boolean canBeMoved( ) {
 
         return true;
     }
 
     @Override
     public boolean canBeRenamed( ) {
 
         return true;
     }
 
     @Override
     public int countAssetReferences( String assetPath ) {
 
         return 0;
     }
 
     @Override
     public void getAssetReferences( List<String> assetPaths, List<Integer> assetTypes ) {
 
         // Do nothing
     }
 
     @Override
     public int countIdentifierReferences( String id ) {
 
         int count = 0;
         if( id.equals(profile.getName()+"."+assessmentRule.getId( )) ) {
             count++;
         }
         if( this.isTimedRule( ) ) {
             count += initConditionsController.countIdentifierReferences( id );
             count += endConditionsController.countIdentifierReferences( id );
         }
         else {
             count += conditionsController.countIdentifierReferences( id );
         }
         return count;
     }
 
     @Override
     public void deleteAssetReferences( String assetPath ) {
 
     }
 
     @Override
     public boolean deleteElement( DataControl dataControl, boolean askConfirmation ) {
 
         return false;
     }
 
     @Override
     public void deleteIdentifierReferences( String id ) {
 
         if( this.isTimedRule( ) ) {
             initConditionsController.deleteIdentifierReferences( id );
             endConditionsController.deleteIdentifierReferences( id );
         }
         else {
             conditionsController.deleteIdentifierReferences( id );
         }
     }
 
     @Override
     public int[] getAddableElements( ) {
 
         return new int[ 0 ];
     }
 
     @Override
     public Object getContent( ) {
 
         return assessmentRule;
     }
 
     @Override
     public boolean isValid( String currentPath, List<String> incidences ) {
 
         return true;
     }
 
     @Override
     public boolean moveElementDown( DataControl dataControl ) {
 
         return false;
     }
 
     @Override
     public boolean moveElementUp( DataControl dataControl ) {
 
         return false;
     }
 
     @Override
     public String renameElement( String name ) {
 
         String oldName = assessmentRule.getId( );
 
         // Show a dialog asking for the ass rule id
         String assRuleId = name;
         if( name == null )
             assRuleId = controller.showInputDialog( TC.get( "Operation.RenameAssessmentRuleTitle" ), TC.get( "Operation.RenameAssessmentRuleMessage" ), TC.get( "Operation.AddAssessmentRuleDefaultValue" ) );
 
         // If some value was typed and the identifier is valid
         // To control the identifiers properly, the id must be composed by "profileName.asRuleId"
         if( assRuleId != null && controller.isElementIdValid( profile.getName() + "." + assRuleId ) ) {
       	    //controller.replaceIdentifierReferences( assessmentRule.getId( ), assRuleId );
             controller.getIdentifierSummary( ).deleteAssessmentRuleId( oldName, profile.getName() );
             controller.getIdentifierSummary( ).addAssessmentRuleId( assRuleId, profile.getName() );
             assessmentRule.setId( assRuleId );
             return oldName;
         }
         return null;
     }
 
     @Override
     public void replaceIdentifierReferences( String oldId, String newId ) {
 
         if( this.isTimedRule( ) ) {
             initConditionsController.replaceIdentifierReferences( oldId, newId );
             endConditionsController.replaceIdentifierReferences( oldId, newId );
         }
         else {
 
             conditionsController.replaceIdentifierReferences( oldId, newId );
         }
     }
 
     @Override
     public void updateVarFlagSummary( VarFlagSummary varFlagSummary ) {
 
         if( this.isTimedRule( ) ) {
             TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
             ConditionsController.updateVarFlagSummary( varFlagSummary, tRule.getInitConditions( ) );
             ConditionsController.updateVarFlagSummary( varFlagSummary, tRule.getEndConditions( ) );
         }
         else
             ConditionsController.updateVarFlagSummary( varFlagSummary, assessmentRule.getConditions( ) );
 
     }
 
     public String getConcept( ) {
 
         return assessmentRule.getConcept( );
     }
 
     public String getEffectText( int effect ) {
 
         if( assessmentRule instanceof TimedAssessmentRule ) {
             TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
             return tRule.getEffects( ).get( effect ).getText( );
         }
         else {
             return assessmentRule.getText( );
         }
 
     }
 
     public void setConcept( String text ) {
 
         controller.addTool( new ChangeStringValueTool( assessmentRule, text, "getConcept", "setConcept" ) );
 
     }
 
     public void setEffectText( String text ) {
 
         controller.addTool( new ChangeStringValueTool( assessmentRule, text, "getText", "setText" ) );
     }
 
     public void setEffectText( int effect, String text ) {
 
         if( assessmentRule instanceof TimedAssessmentRule ) {
             TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
             if( effect >= 0 && effect < tRule.getEffectsCount( ) ) {
                 controller.addTool( new ChangeStringValueTool( tRule.getEffects( ).get( effect ), text, "getText", "setText" ) );
             }
         }
         else {
             setEffectText( text );
         }
     }
 
     public void setImportance( int importance ) {
 
         controller.addTool( new ChangeIntegerValueTool( assessmentRule, importance, "getImportance", "setImportance" ) );
     }
 
     public int getImportance( ) {
 
         return assessmentRule.getImportance( );
 
     }
 
     public ConditionsController getConditions( ) {
 
         return this.conditionsController;
     }
 
     public ConditionsController getInitConditions( ) {
 
         if( assessmentRule instanceof TimedAssessmentRule ) {
             return this.initConditionsController;
         }
         return this.conditionsController;
     }
 
     public ConditionsController getEndConditions( ) {
 
         if( assessmentRule instanceof TimedAssessmentRule ) {
             return this.endConditionsController;
         }
         return this.conditionsController;
     }
 
     public boolean movePropertyUp( int selectedRow ) {
 
         return controller.addTool( new MoveObjectTool( assessmentRule.getAssessmentProperties( ), selectedRow, MoveObjectTool.MODE_UP ) );
     }
 
     public boolean movePropertyUp( int selectedRow, int effect ) {
 
         if( assessmentRule instanceof TimedAssessmentRule ) {
             TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
             if( effect >= 0 && effect < tRule.getEffectsCount( ) && selectedRow > 0 ) {
                 return controller.addTool( new MoveObjectTool( tRule.getEffects( ).get( effect ).getAssessmentProperties( ), selectedRow, MoveObjectTool.MODE_UP ) );
             }
         }
         else {
             return movePropertyUp( selectedRow );
         }
 
         return false;
 
     }
 
     public boolean movePropertyDown( int selectedRow ) {
 
         return controller.addTool( new MoveObjectTool( assessmentRule.getAssessmentProperties( ), selectedRow, MoveObjectTool.MODE_DOWN ) );
     }
 
     public boolean movePropertyDown( int selectedRow, int effect ) {
 
         if( assessmentRule instanceof TimedAssessmentRule ) {
             TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
             if( effect >= 0 && effect < tRule.getEffectsCount( ) && selectedRow < tRule.getEffects( ).get( effect ).getAssessmentProperties( ).size( ) - 1 ) {
                 return controller.addTool( new MoveObjectTool( tRule.getEffects( ).get( effect ).getAssessmentProperties( ), selectedRow, MoveObjectTool.MODE_UP ) );
             }
         }
         else {
             return movePropertyDown( selectedRow );
         }
 
         return false;
     }
 
     public boolean addBlankProperty( int selectedRow, int effect ) {
 
         boolean added = false;
         if( assessmentRule instanceof TimedAssessmentRule ) {
             TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
             if( effect >= 0 && effect < tRule.getEffectsCount( ) ) {
                 TimedAssessmentEffect currentEffect = tRule.getEffects( ).get( effect );
                 added = controller.addTool( new AddAssessmentPropertyTool( currentEffect.getAssessmentProperties( ), selectedRow ) );
 
             }
         }
         else {
 
             added = controller.addTool( new AddAssessmentPropertyTool( assessmentRule.getAssessmentProperties( ), selectedRow ) );
             ;
         }
 
         return added;
     }
 
     public boolean deleteProperty( int selectedRow ) {
 
         return controller.addTool( new DeleteAssessmentPropertyTool( assessmentRule.getAssessmentProperties( ), selectedRow ) );
     }
 
     public boolean deleteProperty( int selectedRow, int effect ) {
 
         if( assessmentRule instanceof TimedAssessmentRule ) {
             TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
             if( effect >= 0 && effect < tRule.getEffectsCount( ) ) {
                 return controller.addTool( new DeleteAssessmentPropertyTool( tRule.getEffects( ).get( effect ).getAssessmentProperties( ), selectedRow ) );
             }
         }
         else {
             return deleteProperty( selectedRow );
         }
         return false;
     }
 
     public int getPropertyCount( int effect ) {
 
         if( assessmentRule instanceof TimedAssessmentRule ) {
             TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
             if( effect >= 0 && effect < tRule.getEffectsCount( ) )
                 return tRule.getEffects( ).get( effect ).getAssessmentProperties( ).size( );
             else
                 return 0;
         }
         else
             return assessmentRule.getAssessmentProperties( ).size( );
     }
 
     public void setPropertyValue( int rowIndex, String string ) {
 
         if( rowIndex >= 0 && rowIndex < assessmentRule.getAssessmentProperties( ).size( ) ) {
             //Check only integers are set
 
             try {
                 controller.addTool( new ChangeAssessmentPropertyTool( assessmentRule.getAssessmentProperties( ), string, rowIndex, ChangeAssessmentPropertyTool.SET_VALUE ) );
             }
             catch( Exception e ) {
                 //Display error message
                 controller.showErrorDialog( TC.get( "AssessmentRule.InvalidPropertyID" ), TC.get( "AssessmentRule.InvalidPropertyID.Message" ) );
             }
 
         }
 
     }
 
     public void setPropertyValue( int rowIndex, int effect, String string ) {
 
         if( assessmentRule instanceof TimedAssessmentRule ) {
             TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
             AssessmentProperty property = tRule.getProperty( rowIndex, effect );
             if( property != null ) {
                 try {
                    controller.addTool( new ChangeAssessmentPropertyTool( tRule.getEffects( ).get( effect ).getAssessmentProperties( ), string, rowIndex+1, ChangeAssessmentPropertyTool.SET_VALUE ) );
 
                 }
                 catch( Exception e ) {
                     //Display error message
                     e.printStackTrace( );
                     controller.showErrorDialog( TC.get( "AssessmentRule.InvalidPropertyID" ), TC.get( "AssessmentRule.InvalidPropertyID.Message" ) );
                 }
             }
         }
         else {
             setPropertyValue( rowIndex, string );
 
         }
     }
 
     public void setPropertyId( int rowIndex, String string ) {
 
         if( rowIndex >= 0 && rowIndex < assessmentRule.getAssessmentProperties( ).size( ) ) {
             if( controller.isElementIdValid( string ) ) {
                 controller.addTool( new ChangeIdTool( assessmentRule.getAssessmentProperties( ).get( rowIndex ), string ) );
             }
         }
 
     }
     
    
 
     public void setPropertyId( int rowIndex, int effect, String string ) {
 
         if( assessmentRule instanceof TimedAssessmentRule ) {
             TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
             AssessmentProperty property = tRule.getProperty( rowIndex, effect );
             if( property != null ) {
         	// check if it is a especial SCORM attribute
         	if (SCORMConfigData.isArrayAttribute(string)){
         	  //check if "string" has a previous value of the same kind of selected attribute
         	    if (assessmentRule.getAssessmentProperties().get(rowIndex).getId().startsWith(string))
         		string = assessmentRule.getAssessmentProperties().get(rowIndex).getId();
         	    string = SCORMAttributeDialog.showAttributeDialogForWrite(getProfileType(), string );
         	}
         	if (!SCORMConfigData.isArrayAttribute(string))
         	    controller.addTool( new ChangeAssessmentPropertyTool( ((TimedAssessmentRule)assessmentRule).getEffects( ).get( effect ).getAssessmentProperties( ), string, rowIndex, ChangeAssessmentPropertyTool.SET_ID ) );
             }
         }
         else {
             if( rowIndex >= 0 && rowIndex < assessmentRule.getAssessmentProperties( ).size( ) ) {
                 if( controller.isElementIdValid( string ) ) {
                  // check if it is a especial SCORM attribute
                     if (SCORMConfigData.isArrayAttribute(string)){
                 	//check if "string" has a previous value of the same kind of selected attribute
                 	if (assessmentRule.getAssessmentProperties().get(rowIndex).getId().startsWith(string))
             		string = assessmentRule.getAssessmentProperties().get(rowIndex).getId();
             	    string = SCORMAttributeDialog.showAttributeDialogForWrite(getProfileType(), string );
                     }
                     if (!SCORMConfigData.isArrayAttribute(string))
             		controller.addTool( new ChangeAssessmentPropertyTool( assessmentRule.getAssessmentProperties( ), string, rowIndex, ChangeAssessmentPropertyTool.SET_ID ) );
                 }
             }
 
         }
     }
     
     public int getProfileType(){
 	if (profile.isScorm12())
 	    return SCORMConfigData.SCORM_V12;
 	else if (profile.isScorm2004())
 	    return SCORMConfigData.SCORM_2004;
 	else
 	    return -1;
 	
     }
     
     public boolean checkRulesDataModel( String currentRule, List<String> incidences, boolean isSCORM12, boolean isSCORM2004 ){
         boolean valid=true;
         for (AssessmentProperty prop:assessmentRule.getAssessmentProperties( )){
             if (isSCORM12)
                 if ( !SCORMConfigData.isPartOfTheModel12( prop.getId( ) )){
                     incidences.add( TC.get( "AssessmentRule.CheckRule.Rule" )+": "+currentRule+" "+TC.get( "AssessmentRule.CheckRule.Property")+" "+prop.getId( )+" "+TC.get( "AssessmentRule.CheckRule.Datav12" ) );
                     valid &= false;
                 }
             if (isSCORM2004)  
                 if ( !SCORMConfigData.isPartOfTheModel2004( prop.getId( ) )){
                     incidences.add( TC.get( "AssessmentRule.CheckRule.Rule" )+": "+currentRule+" "+TC.get( "AssessmentRule.CheckRule.Property")+" "+prop.getId( )+" "+TC.get( "AssessmentRule.CheckRule.Data2004" ) );
                     valid &= false;
                 }
         }
         return valid;
         
     }
 
     public String getPropertyId( int rowIndex, int effect ) {
 
         if( assessmentRule instanceof TimedAssessmentRule ) {
             TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
             AssessmentProperty property = tRule.getProperty( rowIndex, effect );
             if( property != null ) {
                 return property.getId( );
             }
             else
                 return null;
         }
         else {
             return this.assessmentRule.getAssessmentProperties( ).get( rowIndex ).getId( );
         }
 
     }
 
     public String getPropertyValue( int rowIndex, int effect ) {
 
         if( assessmentRule instanceof TimedAssessmentRule ) {
             TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
             AssessmentProperty property = tRule.getProperty( rowIndex, effect );
             if( property != null ) {
                 return property.getValue( );
             }
             else
                 return "";
         }
         else {
             return this.assessmentRule.getAssessmentProperties( ).get( rowIndex ).getValue( );
         }
     }
 
     public String getId( ) {
 
         return assessmentRule.getId( );
     }
 
     public boolean isTimedRule( ) {
 
         return assessmentRule != null && assessmentRule instanceof TimedAssessmentRule;
     }
 
     public int getMinTime( int effect ) {
 
         TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
         return tRule.getMinTime( effect );
     }
 
     public int getMaxTime( int effect ) {
 
         TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
         return tRule.getMaxTime( effect );
 
     }
 
     public void setMinTime( int time, int effect ) {
 
         TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
         if( effect >= 0 && effect < tRule.getEffectsCount( ) ) {
             controller.addTool( new ChangeMinTimeValueTool( tRule.getEffects( ).get( effect ), time ) );
         }
     }
 
     public void setMaxTime( int time, int effect ) {
 
         TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
         if( effect >= 0 && effect < tRule.getEffectsCount( ) ) {
             controller.addTool( new ChangeIntegerValueTool( tRule.getEffects( ).get( effect ), time, "getMaxTime", "setMaxTime" ) );
         }
     }
 
     public int getEffectsCount( ) {
 
         TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
         return tRule.getEffectsCount( );
     }
 
     public String[] getEffectNames( ) {
 
         TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
         String[] names = new String[ tRule.getEffectsCount( ) ];
         for( int i = 1; i <= names.length; i++ ) {
             names[i - 1] = Integer.toString( i );
         }
         return names;
     }
 
     public void addEffectBlock( int index ) {
 
         TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
         controller.addTool( new AddEffectTool( tRule, index ) );
     }
 
     public void removeEffectBlock( int currentIndex ) {
 
         TimedAssessmentRule tRule = (TimedAssessmentRule) assessmentRule;
         controller.addTool( new DeleteEffectTool( tRule, currentIndex ) );
     }
 
     @Override
     public boolean canBeDuplicated( ) {
 
         return true;
     }
 
     @Override
     public void recursiveSearch( ) {
 
         check( this.getConcept( ), TC.get( "Search.Concept" ) );
         check( this.getId( ), "ID" );
 
         check( this.getEndConditions( ), TC.get( "Search.EndConditions" ) );
 
         if( assessmentRule instanceof TimedAssessmentRule ) {
             for( int i = 0; i < this.getEffectsCount( ); i++ ) {
                 check( this.getEffectNames( )[i], TC.get( "Search.EffectName" ) );
                 check( this.getEffectText( i ), TC.get( "Search.EffectText" ) );
                 for( int j = 0; j < this.getPropertyCount( i ); j++ ) {
                     check( this.getPropertyId( j, i ), TC.get( "Search.PropertyID" ) );
                     check( this.getPropertyValue( j, i ), TC.get( "Search.PropertyValue" ) );
                 }
                 check( this.getInitConditions( ), TC.get( "Search.Conditions" ) );
 
             }
         }
         else if( assessmentRule instanceof AssessmentRule ) {
             for( int j = 0; j < this.getPropertyCount( -1 ); j++ ) {
                 check( this.getPropertyId( j, -1 ), TC.get( "Search.PropertyID" ) );
                 check( this.getPropertyValue( j, -1 ), TC.get( "Search.PropertyValue" ) );
             }
             check( this.getEffectText( -1 ), TC.get( "Search.EffectText" ) );
             check( this.getConditions( ), TC.get( "Search.Conditions" ) );
         }
 
     }
 
     public boolean isUsesEndConditions( ) {
 
         if( assessmentRule instanceof TimedAssessmentRule ) {
             return ( (TimedAssessmentRule) assessmentRule ).isUsesEndConditions( );
         }
         return false;
     }
 
     public void setUsesEndConditions( boolean selected ) {
 
         Controller.getInstance( ).addTool( new ChangeUsesEndCondition( (TimedAssessmentRule) assessmentRule, selected ) );
     }
 
     @Override
     public List<Searchable> getPathToDataControl( Searchable dataControl ) {
 
         return null;
     }
 
     public void setId( String value ) {
 
         //Controller.getInstance( ).addTool( new ChangeIdTool( assessmentRule, value ) );
 	Controller.getInstance( ).addTool( new RenameRuleTool( this, value ) );
     }
 
 }
