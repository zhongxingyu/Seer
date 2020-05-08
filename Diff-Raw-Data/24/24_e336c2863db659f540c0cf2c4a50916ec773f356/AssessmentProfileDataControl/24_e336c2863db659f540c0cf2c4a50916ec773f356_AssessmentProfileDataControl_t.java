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
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import es.eucm.eadventure.common.auxiliar.ReportDialog;
 import es.eucm.eadventure.common.data.assessment.AssessmentProfile;
 import es.eucm.eadventure.common.data.assessment.AssessmentRule;
 import es.eucm.eadventure.common.data.assessment.TimedAssessmentRule;
 import es.eucm.eadventure.common.gui.TC;
 import es.eucm.eadventure.editor.control.Controller;
 import es.eucm.eadventure.editor.control.controllers.DataControl;
 import es.eucm.eadventure.editor.control.controllers.Searchable;
 import es.eucm.eadventure.editor.control.tools.animation.ChangeAssessmentProfileTypeTool;
 import es.eucm.eadventure.editor.control.tools.assessment.ChangeReportSettingsTool;
 import es.eucm.eadventure.editor.data.support.VarFlagSummary;
 import es.eucm.eadventure.editor.gui.displaydialogs.InvalidReportDialog;
 
 public class AssessmentProfileDataControl extends DataControl {
 
     
     
     
     /**
      * Controllers for each assessment rule
      */
     private List<AssessmentRuleDataControl> dataControls;
 
     /**
      * The profile
      */
     private AssessmentProfile profile;
 
     public AssessmentProfileDataControl( AssessmentProfile profile ) {
 
         dataControls = new ArrayList<AssessmentRuleDataControl>( );
         this.profile = profile;
         for( AssessmentRule rule : profile.getRules( ) ) {
             dataControls.add( new AssessmentRuleDataControl( rule, this.profile ) );
         }
     }
 
     public AssessmentProfileDataControl( List<AssessmentRule> assessmentRules, String name ) {
 
         this( new AssessmentProfile( assessmentRules, name ) );
     }
     
     
 
     public String getFileName( ) {
 
         return profile.getName( ).substring( Math.max( profile.getName( ).lastIndexOf( "/" ), profile.getName( ).lastIndexOf( "\\" ) ) + 1 );
     }
 
     @Override
     public boolean addElement( int type, String assRuleId ) {
 
         boolean added = false;
 
         if( type == Controller.ASSESSMENT_RULE || type == Controller.TIMED_ASSESSMENT_RULE ) {
             // Show a dialog asking for the ass rule id
             if( assRuleId == null )
                 assRuleId = controller.showInputDialog( TC.get( "Operation.AddAssessmentRuleTitle" ), TC.get( "Operation.AddAssessmentRuleMessage" ), TC.get( "Operation.AddAssessmentRuleDefaultValue" ) );
             
             // If some value was typed and the identifier is valid
             if( assRuleId != null && controller.isElementIdValid(assRuleId ) ) {
                 // Add thew new ass rule
                 AssessmentRule assRule = null;
                 
                 if( type == Controller.TIMED_ASSESSMENT_RULE ) {
                     assRule = new TimedAssessmentRule( assRuleId, AssessmentRule.IMPORTANCE_NORMAL );
                 }
                 else {
                     assRule = new AssessmentRule( assRuleId, AssessmentRule.IMPORTANCE_NORMAL );
                 }
                 this.profile.getRules( ).add( assRule );
                 dataControls.add( new AssessmentRuleDataControl( assRule, profile ) );
                 controller.getIdentifierSummary( ).addAssessmentRuleId( assRuleId, profile.getName() );
                 //controller.dataModified( );
                 added = true;
             }
 
         }
         return added;
     }
     
     public DataControl getLastDatacontrol(){
         return dataControls.get( dataControls.size()-1 );
     }
     
     public boolean addElement( int type, String assRuleId, AssessmentRule assRule ) {
 
         boolean added = false;
 
         if( type == Controller.ASSESSMENT_RULE || type == Controller.TIMED_ASSESSMENT_RULE ) {
              // If some value was typed and the identifier is valid
             if( assRuleId != null && controller.isElementIdValid( assRuleId ) ) {
                 // Add thew new ass rule 
                 this.profile.getRules( ).add( assRule );
                 dataControls.add( new AssessmentRuleDataControl( assRule, profile ) );
                 controller.getIdentifierSummary( ).addAssessmentRuleId( assRuleId, profile.getName() );
                 //controller.dataModified( );
                 added = true;
             }
         }
 
        
         return added;
     }
 
     public AssessmentRuleDataControl getLastAssessmentRule( ) {
 
         return dataControls.get( dataControls.size( ) - 1 );
     }
 
     @Override
     public boolean canAddElement( int type ) {
 
         return type == Controller.ASSESSMENT_RULE || type == Controller.TIMED_ASSESSMENT_RULE;
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
     public int countIdentifierReferences( String id ) {
 
         int count = 0;
         if( id.equals( profile.getName( ) ) )
             count++;
         for( AssessmentRuleDataControl rule : dataControls ) {
             count += rule.countIdentifierReferences( id );
         }
         return count;
     }
 
     @Override
     public void getAssetReferences( List<String> assetPaths, List<Integer> assetTypes ) {
 
         // Do nothing
     }
 
     @Override
     public void deleteAssetReferences( String assetPath ) {
 
     }
 
     @Override
     public boolean duplicateElement( DataControl dataControl ) {
 
         if( !( dataControl instanceof AssessmentRuleDataControl ) )
             return false;
 
         try {
             AssessmentRule newRule = (AssessmentRule) ( ( (AssessmentRule) ( dataControl.getContent( ) ) ).clone( ) );
             String id = newRule.getId( );
            int i = 0;
             do {
                String lastIndex;
                if (i<10){
                    lastIndex = id.substring( id.length( ) - 1, id.length( ) );
                } else{
                    lastIndex = id.substring( id.length( ) - 2, id.length( ) );
                }
                try {
                    Integer.parseInt( lastIndex );
                    i++;
                    if (i<10){
                        id = id.substring( 0, id.length( ) - 1 );
                    }else{
                        id = id.substring( 0, id.length( ) - 2 );
                    }
                    id += i;
                }
                catch( NumberFormatException e ) {
                    id += 1;
                }
             } while( controller.getIdentifierSummary( ).isAssessmentRuleId(id, profile.getName()) );
             newRule.setId( id );
             dataControls.add( new AssessmentRuleDataControl( newRule, profile ) );
             profile.addRule(newRule);
             controller.getIdentifierSummary( ).addAssessmentRuleId( newRule.getId( ), profile.getName() );
             return true;
         }
         catch( CloneNotSupportedException e ) {
             ReportDialog.GenerateErrorReport( e, true, "Could not clone action" );
             return false;
         }
     }
 
     @Override
     public boolean deleteElement( DataControl dataControl, boolean askConfirmation ) {
 
         boolean deleted = false;
 
         String assRuleId = ( (AssessmentRuleDataControl) dataControl ).getId( );
         String references = String.valueOf( controller.countIdentifierReferences( assRuleId ) );
 
         // Ask for confirmation
         if( !askConfirmation || controller.showStrictConfirmDialog( TC.get( "Operation.DeleteElementTitle" ), TC.get( "Operation.DeleteElementWarning", new String[] { assRuleId, references } ) ) ) {
             if( this.profile.getRules( ).remove( dataControl.getContent( ) ) ) {
                 dataControls.remove( dataControl );
                 controller.deleteIdentifierReferences( assRuleId );
                 controller.getIdentifierSummary( ).deleteAssessmentRuleId( assRuleId, profile.getName() );
                 //controller.dataModified( );
                 deleted = true;
 
             }
         }
 
         return deleted;
     }
 
     @Override
     public void deleteIdentifierReferences( String id ) {
 
         // profiles identifiers are deleted in assessmentProfilesDataControl
         Iterator<AssessmentRuleDataControl> itera = this.dataControls.iterator( );
 
         while( itera.hasNext( ) ) {
             itera.next( ).deleteIdentifierReferences( id );
             // the rule ID are unique, do not look in rule's IDs
    
         }
 
     }
 
     @Override
     public int[] getAddableElements( ) {
 
         return new int[] { Controller.ASSESSMENT_RULE, Controller.TIMED_ASSESSMENT_RULE };
     }
 
     @Override
     public Object getContent( ) {
 
         return profile;
     }
 
     public List<AssessmentRuleDataControl> getAssessmentRules( ) {
 
         return this.dataControls;
     }
 
     @Override
     public boolean isValid( String currentPath, List<String> incidences ) {
 	
 	for (Iterator<AssessmentRuleDataControl> it = dataControls.iterator();it.hasNext();){
 	    if (it.next().getId().equals(currentPath))
 		return false;
 	}
         return true;
     }
 
     @Override
     public boolean moveElementDown( DataControl dataControl ) {
 
         boolean elementMoved = false;
         int elementIndex = profile.getRules( ).indexOf( dataControl.getContent( ) );
 
         if( elementIndex < profile.getRules( ).size( ) - 1 ) {
             profile.getRules( ).add( elementIndex + 1, profile.getRules( ).remove( elementIndex ) );
             dataControls.add( elementIndex + 1, dataControls.remove( elementIndex ) );
             //controller.dataModified( );
             elementMoved = true;
         }
 
         return elementMoved;
     }
 
     @Override
     public boolean moveElementUp( DataControl dataControl ) {
 
         boolean elementMoved = false;
         int elementIndex = profile.getRules( ).indexOf( dataControl.getContent( ) );
 
         if( elementIndex > 0 ) {
             profile.getRules( ).add( elementIndex - 1, profile.getRules( ).remove( elementIndex ) );
             dataControls.add( elementIndex - 1, dataControls.remove( elementIndex ) );
             //controller.dataModified( );
             elementMoved = true;
         }
 
         return elementMoved;
     }
 
     @Override
     public String renameElement( String name ) {
 
         boolean renamed = false;
         String oldName = null;
         if( this.profile.getName( ) != null ) {
             oldName = this.profile.getName( );
         }
 
         // Show confirmation dialog.
         if( name != null || controller.showStrictConfirmDialog( TC.get( "Operation.RenameAssessmentFile" ), TC.get( "Operation.RenameAssessmentFile.Message" ) ) ) {
 
             //Prompt for file name:
             String fileName = name;
             if( name == null || name.equals( "" ) )
                 fileName = controller.showInputDialog( TC.get( "Operation.RenameAssessmentFile.FileName" ), TC.get( "Operation.RenameAssessmentFile.FileName.Message" ), getFileName( ) );
 
             if( fileName != null && !fileName.equals( oldName ) && controller.isElementIdValid( fileName ) ) {
                 if( !controller.getIdentifierSummary( ).isAssessmentProfileId( name ) ) {
                     //controller.dataModified( );
                     profile.setName( fileName );
                     
                     controller.getIdentifierSummary( ).renameAssessmentProfile(oldName, fileName);
                     controller.replaceIdentifierReferences(oldName, fileName);
                     renamed = true;
                 }
                 else {
                     controller.showErrorDialog( TC.get( "Operation.CreateAdaptationFile.FileName.ExistValue.Title" ), TC.get( "Operation.CreateAdaptationFile.FileName.ExistValue.Message" ) );
                 }
             }
 
         }
 
         if( renamed )
             return oldName;
 
         return null;
     }
 
     @Override
     public void replaceIdentifierReferences( String oldId, String newId ) {
 
         for( AssessmentRuleDataControl rule : dataControls )
             rule.replaceIdentifierReferences(oldId, newId);
     }
 
     @Override
     public void updateVarFlagSummary( VarFlagSummary varFlagSummary ) {
 
         for( AssessmentRuleDataControl dataControl : dataControls )
             dataControl.updateVarFlagSummary( varFlagSummary );
 
     }
 
     /**
      * String values for the different importance values (for printing)
      */
     public static final String[] IMPORTANCE_VALUES_PRINT = { "Very low", "Low", "Normal", "High", "Very high" };
 
     public String[][] getAssessmentRulesInfo( ) {
 
         String[][] info = new String[ this.profile.getRules( ).size( ) ][ 3 ];
 
         for( int i = 0; i < profile.getRules( ).size( ); i++ ) {
             info[i][0] = profile.getRules( ).get( i ).getId( );
             info[i][1] = IMPORTANCE_VALUES_PRINT[profile.getRules( ).get( i ).getImportance( )];
             info[i][2] = ( profile.getRules( ).get( i ).getConditions( ).isEmpty( ) ) ? TC.get( "GeneralText.No" ) : TC.get( "GeneralText.Yes" );
         }
         return info;
     }
 
     /**
      * @return the path
      */
     public String getName( ) {
 
         return profile.getName( );
     }
 
     @Override
     public boolean canBeDuplicated( ) {
 
         return true;
     }
 
     /**
      * @return the showReportAtEnd
      */
     public boolean isShowReportAtEnd( ) {
 
         return profile.isShowReportAtEnd( );
     }
 
     /**
      * @param showReportAtEnd
      *            the showReportAtEnd to set
      */
     public void setShowReportAtEnd( boolean showReportAtEnd ) {
 
         controller.addTool( new ChangeReportSettingsTool( profile, showReportAtEnd, ChangeReportSettingsTool.SHOW_REPORT ) );
     }
 
     public boolean isSendByEmail( ) {
 
         return profile.isSendByEmail( );
     }
 
     public void setSendByEmail( boolean sendByEmail ) {
 
         controller.addTool( new ChangeReportSettingsTool( profile, sendByEmail, ChangeReportSettingsTool.SEND ) );
     }
 
     public String getEmail( ) {
 
         return profile.getEmail( );
     }
 
     public void setEmail( String email ) {
 
         controller.addTool( new ChangeReportSettingsTool( profile, email, ChangeReportSettingsTool.EMAIL ) );
     }
 
     public String getSmtpServer( ) {
 
         return profile.getSmtpServer( );
     }
 
     public boolean isSmtpSSL( ) {
 
         return profile.isSmtpSSL( );
     }
 
     public String getSmtpPort( ) {
 
         return profile.getSmtpPort( );
     }
 
     public String getSmtpUser( ) {
 
         return profile.getSmtpUser( );
     }
 
     public String getSmtpPwd( ) {
 
         return profile.getSmtpPwd( );
     }
 
     public void setSmtpServer( String smtpServer ) {
 
         controller.addTool( new ChangeReportSettingsTool( profile, smtpServer, ChangeReportSettingsTool.SMTP_SERVER ) );
     }
 
     public void setSmtpSSL( boolean smtpSSL ) {
 
         controller.addTool( new ChangeReportSettingsTool( profile, smtpSSL, ChangeReportSettingsTool.SMTP_SSL ) );
     }
 
     public void setSmtpPort( String smtpPort ) {
 
         controller.addTool( new ChangeReportSettingsTool( profile, smtpPort, ChangeReportSettingsTool.SMTP_PORT ) );
     }
 
     public void setSmtpUser( String smtpUser ) {
 
         controller.addTool( new ChangeReportSettingsTool( profile, smtpUser, ChangeReportSettingsTool.SMTP_USER ) );
     }
 
     public void setSmtpPwd( String smtpPwd ) {
 
         controller.addTool( new ChangeReportSettingsTool( profile, smtpPwd, ChangeReportSettingsTool.SMTP_PWD ) );
     }
 
     public boolean isScorm12( ) {
 
         return profile.isScorm12( );
     }
 
     public boolean isScorm2004( ) {
 
         return profile.isScorm2004( );
     }
 
     public void changeToScorm2004Profile( ) {
 
         controller.addTool( new ChangeAssessmentProfileTypeTool( profile, ChangeAssessmentProfileTypeTool.SCORM2004, profile.isScorm12( ), profile.isScorm2004( ) ) );
         checkRulesDataModel();
     }
 
     public void changeToScorm12Profile( ) {
 
         controller.addTool( new ChangeAssessmentProfileTypeTool( profile, ChangeAssessmentProfileTypeTool.SCORM12, profile.isScorm12( ), profile.isScorm2004( ) ) );
         checkRulesDataModel();
     }
 
     public void changeToNormalProfile( ) {
 
         controller.addTool( new ChangeAssessmentProfileTypeTool( profile, ChangeAssessmentProfileTypeTool.NORMAL, profile.isScorm12( ), profile.isScorm2004( ) ) );
         checkRulesDataModel();
     }
     
     public void checkRulesDataModel(){
         
      // Create a list to store the incidences
         List<String> incidences = new ArrayList<String>( );
         boolean valid = true;
         for (AssessmentRuleDataControl assRule : dataControls){
             valid &= assRule.checkRulesDataModel( assRule.getId( ) , incidences, profile.isScorm12( ), profile.isScorm2004( ));   
         }
         if (!valid)
             //TC
             new InvalidReportDialog( incidences , "Al cambiar el tipo de perfil, hay partes del modelo de datos que no concuerdan. El juego se podr exportar correctamente, pero la comunicacin puede que no funcione como se desea. ");
     }
 
     @Override
     public void recursiveSearch( ) {
 
         for( DataControl dc : dataControls )
             dc.recursiveSearch( );
         check( getEmail( ), TC.get( "Search.EMail" ) );
         check( this.getName( ), TC.get( "Search.Name" ) );
         check( this.getSmtpPort( ), TC.get( "Search.SMTPPort" ) );
         check( this.getSmtpServer( ), TC.get( "Search.SMTPServer" ) );
         check( this.getSmtpUser( ), TC.get( "Search.SMTPUser" ) );
     }
 
     @Override
     public List<Searchable> getPathToDataControl( Searchable dataControl ) {
 
         return getPathFromChild( dataControl, dataControls );
     }
 
     /**
      * @return the dataControls
      */
     public List<AssessmentRuleDataControl> getDataControls( ) {
 
         return dataControls;
     }
 
     /**
      * @param dataControls
      *            the dataControls to set
      */
     public void setDataControlsAndData( List<AssessmentRuleDataControl> dataControls ) {
 
         this.dataControls = dataControls;
         ArrayList<AssessmentRule> rules = new ArrayList<AssessmentRule>( );
         for( AssessmentRuleDataControl dataControl : dataControls )
             rules.add( (AssessmentRule) dataControl.getContent( ) );
 
         this.profile.setRules( rules );
     }
 
 }
