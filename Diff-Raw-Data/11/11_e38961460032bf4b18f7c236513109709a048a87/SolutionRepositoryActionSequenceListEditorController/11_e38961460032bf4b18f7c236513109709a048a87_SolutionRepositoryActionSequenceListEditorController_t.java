 package org.pentaho.pac.client.scheduler.ctlr;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.pentaho.gwt.widgets.client.controls.TableEditor;
import org.pentaho.gwt.widgets.client.filechooser.FileChooserListener;
 import org.pentaho.gwt.widgets.client.ui.ICallback;
 import org.pentaho.gwt.widgets.client.utils.StringUtils;
 import org.pentaho.pac.client.PacServiceFactory;
 import org.pentaho.pac.client.PentahoAdminConsole;
 import org.pentaho.pac.client.common.ui.dialog.MessageDialog;
 import org.pentaho.pac.client.i18n.PacLocalizedMessages;
 import org.pentaho.pac.client.scheduler.model.SolutionRepositoryModel;
 import org.pentaho.pac.client.scheduler.view.ActionSequencePicker;
 import org.pentaho.pac.client.scheduler.view.ActionSequencePickerDialog;
 import org.pentaho.pac.client.scheduler.view.SolutionRepositoryActionSequenceListEditor;
 
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.xml.client.Document;
 import com.google.gwt.xml.client.XMLParser;
 
 public class SolutionRepositoryActionSequenceListEditorController {
 
   private SolutionRepositoryActionSequenceListEditor solRepActionSequenceEditor = null;
   private ActionSequencePickerDialog actionSequencePickerDialog = null;
   private SolutionRepositoryModel solutionRepositoryModel = null;
   private boolean isInitialized = false;
   private static final PacLocalizedMessages MSGS = PentahoAdminConsole.getLocalizedMessages();
   
   public SolutionRepositoryActionSequenceListEditorController(
       SolutionRepositoryActionSequenceListEditor solRepActionSequenceEditor,
       ActionSequencePickerDialog actionSequencePickerDialog ) {
     
     this.solRepActionSequenceEditor = solRepActionSequenceEditor;
     this.actionSequencePickerDialog = actionSequencePickerDialog;
 
     solRepActionSequenceEditor.setAddBtnEnabled( false );
     solRepActionSequenceEditor.setDeleteBtnEnabled( false );
     
     actionSequencePickerDialog.setOkBtnEnabled( false );
     // set dialog's on ok callback
     final SolutionRepositoryActionSequenceListEditorController localThis = this;
     actionSequencePickerDialog.setOnOkHandler( new ICallback<MessageDialog>() {
       public void onHandle(MessageDialog dlg ) {
         if ( localThis.actionSequencePickerDialog.getOkBtnLabel().equals( ActionSequencePickerDialog.OPEN_LABEL )) {
           String path = "/" + localThis.actionSequencePickerDialog.getActionSequencePicker().getFullPath(); //$NON-NLS-1$
           localThis.actionSequencePickerDialog.getActionSequencePicker().changeToPath( path );
           localThis.actionSequencePickerDialog.setOkBtnEnabled( false );
         } else {
           // must be in select mode
           localThis.actionSequencePickerDialog.setOkBtnEnabled( false );  // prevents double submit
           // unselect current selection
           localThis.copyActionsFromPickerToActionSequenceEditor();
           localThis.actionSequencePickerDialog.hide();
         }
       }
     });
     
     actionSequencePickerDialog.getActionSequencePicker().addFileChooserListener( new FileChooserListener () {
       /**
        * called when the user picks a file by double clicking the file
        * or clicking the ok button.
        * solution,path, and name are NOT friendly names
        */
       public void fileSelected(String solution, String path, String name) {
         if ( !StringUtils.isEmpty( name ) ) {
           localThis.copyActionsFromPickerToActionSequenceEditor();
           localThis.actionSequencePickerDialog.hide();
         }
       }
       /**
        * called when the user selects a file or folder by single clicking
        * solution,path, and name are NOT friendly names
        */
       public void fileSelectionChanged(String solution, String path, String name) {
         if ( StringUtils.isEmpty( name ) ) {
           // they selected a folder
           localThis.actionSequencePickerDialog.setOkBtnEnabled( true );
           localThis.actionSequencePickerDialog.setOkBtnLabel( ActionSequencePickerDialog.OPEN_LABEL );
         } else {
           // the selected a file
           String selectedPath = createActionSequencePath( solution, path, name );
           boolean bAlreadyContainsAction = localThis.solRepActionSequenceEditor.getActionsAsList().contains( selectedPath );
           localThis.actionSequencePickerDialog.setOkBtnEnabled( !bAlreadyContainsAction );
           localThis.actionSequencePickerDialog.setOkBtnLabel( ActionSequencePickerDialog.SELECT_LABEL );
         }
       }
     });
     
     solRepActionSequenceEditor.setOnAddClickedHandler( new ICallback<TableEditor>() {
       public void onHandle(TableEditor tableEditor ) {
         localThis.actionSequencePickerDialog.center();
       }
     });
     
     solRepActionSequenceEditor.setOnDeleteClickedHandler( new ICallback<TableEditor>() {
       public void onHandle(TableEditor tableEditor ) {
         tableEditor.setDeleteBtnEnabled( tableEditor.getNumSelectedItems() > 0 ); 
       }
     });
     
     solRepActionSequenceEditor.setOnSelectHandler( new ICallback<TableEditor>() {
       public void onHandle(TableEditor tableEditor ) {
         tableEditor.setDeleteBtnEnabled( tableEditor.getNumSelectedItems() > 0 ); 
       }
     });
   }
   
   private static String createActionSequencePath( String solution, String path, String action ) {
     System.out.println( solution + path + ( !StringUtils.isEmpty(action) ? "/" + action : "" ) );
     return solution + path + ( !StringUtils.isEmpty(action) ? "/" + action : "" );
   }
   
   private void copyActionsFromPickerToActionSequenceEditor() {
     ActionSequencePicker actionSequencePicker = actionSequencePickerDialog.getActionSequencePicker();
     String path = actionSequencePicker.getFullPath();
     // do the copy
     String friendlyName = solutionRepositoryModel.getFriendlyNameFromName( path );
     solRepActionSequenceEditor.addAction( friendlyName, path );
     
   }
   
   public void init( List<String> actionList ) {
     if ( !isInitialized ) {
       loadSolutionRepository( actionList ); // async
     } else {
       if ( null != actionList ) {
         loadActionListIntoItemPicker( actionList );
       }
     }
   }
   
   private void loadActionListIntoItemPicker( List<String> actionList ) {
     List<String> friendlyNames = getFriendlyNamesFromNames( actionList );
     solRepActionSequenceEditor.setActionsAsList( friendlyNames, actionList );
   }
   
   private List<String> getFriendlyNamesFromNames( List<String> names ) {
 
     // translate the terse names actions into friendly names
     List<String> friendlyNames = null;
     if ( null != names ) {
       friendlyNames = new ArrayList<String>();
       for ( String name : names ) {
         String friendlyName = solutionRepositoryModel.getFriendlyNameFromName( name );
         friendlyNames.add( friendlyName );
       }
     }
     return friendlyNames;
   }
 
   private void showLoadingMessage() {
     solRepActionSequenceEditor.setMessage( MSGS.loading() );
   }
   private void hideLoadingMessage() {
     solRepActionSequenceEditor.clearMessage();
   }
   private void loadSolutionRepository( final List<String> actionList ) {
 
     showLoadingMessage();
     solRepActionSequenceEditor.setAddBtnEnabled( false );
     
     AsyncCallback<String> solutionRepositoryCallback = new AsyncCallback<String>() {
       public void onSuccess( String strXml ) {
         Document solutionRepositoryDocument = XMLParser.parse( strXml );
         solutionRepositoryModel = new SolutionRepositoryModel( solutionRepositoryDocument );
         hideLoadingMessage();
         solRepActionSequenceEditor.setAddBtnEnabled( true );
         if ( null != actionList ) {
           loadActionListIntoItemPicker( actionList );
         }
         actionSequencePickerDialog.getActionSequencePicker().setSolutionRepositoryDocument( solutionRepositoryModel.getDocument() );
         solRepActionSequenceEditor.fireLoadingComplete();
         isInitialized = true;
       } // end onSuccess
 
       public void onFailure(Throwable caught) {
         MessageDialog messageDialog = new MessageDialog( MSGS.error(), 
             caught.getMessage() );
         messageDialog.center();
         solutionRepositoryModel = null;
         isInitialized = false;
         hideLoadingMessage();
       } // end onFailure
     }; // end 
       
     PacServiceFactory.getSolutionRepositoryService().getSolutionRepositoryAsXml( solutionRepositoryCallback );
   }
 }
