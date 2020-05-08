 package net.jodreport.ooAddon.ui.script;
 
 import com.sun.star.awt.XButton;
 import com.sun.star.awt.XCheckBox;
 import com.sun.star.lang.XComponent;
 import com.sun.star.awt.XControl;
 import com.sun.star.awt.XControlModel;
 import com.sun.star.awt.XControlContainer;
 import com.sun.star.awt.XDialog;
 import com.sun.star.awt.XListBox;
 import com.sun.star.awt.XTextComponent;
 import com.sun.star.awt.XToolkit;
 import com.sun.star.awt.XWindow;
 import com.sun.star.beans.XPropertySet;
 import com.sun.star.container.XNameContainer;
 import com.sun.star.lang.XMultiComponentFactory;
 import com.sun.star.lang.XMultiServiceFactory;
 import com.sun.star.uno.UnoRuntime;
 import com.sun.star.uno.XComponentContext;
 import net.jodreport.ooAddon.ui.StandardActionListener;
 import net.jodreport.ooAddon.ui.StandardDialog;
 
 /**
  *
  * @author tedliang
  */
 
 public class ScriptDialog implements StandardDialog {
 
     private static final String _textFieldName = "TextInput";
     private static final String _buttonName = "Button1";
     private static final String _checkboxName = "CheckBox";
     private static final String _cancelButtonName = "CancelButton";
     private static final String _labelName = "Label1";
     private static final String _shortcutName = "Shortcut1";
     
     private final XComponentContext _xComponentContext;
 
     private XDialog xDialog;
     private Object dialog;
 
     private Script script;
     
     
     public ScriptDialog( XComponentContext xComponentContext) {
         _xComponentContext = xComponentContext;
     }
 
     /** method for creating a dialog at runtime
      */
     public Script createDialog() throws com.sun.star.uno.Exception {
 
         // get the service manager from the component context
         XMultiComponentFactory xMultiComponentFactory = _xComponentContext.getServiceManager();
         
         // create the dialog model and set the properties
         Object dialogModel = xMultiComponentFactory.createInstanceWithContext(
             "com.sun.star.awt.UnoControlDialogModel", _xComponentContext );
         XPropertySet xPSetDialog = UnoRuntime.queryInterface(
             XPropertySet.class, dialogModel );      
         xPSetDialog.setPropertyValue( "PositionX", new Integer( 100 ) );
         xPSetDialog.setPropertyValue( "PositionY", new Integer( 100 ) );
         xPSetDialog.setPropertyValue( "Width", new Integer( 240 ) );
         xPSetDialog.setPropertyValue( "Height", new Integer( 170 ) );
         xPSetDialog.setPropertyValue( "Title", new String( "Insert JOOScript" ) );
 
         // get the service manager from the dialog model
         XMultiServiceFactory xMultiServiceFactory = UnoRuntime.queryInterface(
             XMultiServiceFactory.class, dialogModel );
 
         // create the label model and set the properties
         Object labelModel = xMultiServiceFactory.createInstance("com.sun.star.awt.UnoControlFixedTextModel" );
         XPropertySet xPSetLabel = UnoRuntime.queryInterface(
             XPropertySet.class, labelModel );
         xPSetLabel.setPropertyValue( "PositionX", new Integer( 10 ) );
         xPSetLabel.setPropertyValue( "PositionY", new Integer( 10 ) );
         xPSetLabel.setPropertyValue( "Width", new Integer( 100 ) );
         xPSetLabel.setPropertyValue( "Height", new Integer( 14 ) );
         xPSetLabel.setPropertyValue( "Name", _labelName );
         xPSetLabel.setPropertyValue( "Label", "Please input JOOScript:" );
 
         Object shortcutModel = xMultiServiceFactory.createInstance("com.sun.star.awt.UnoControlListBoxModel" );
         XPropertySet xPSetList = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, shortcutModel);
         xPSetList.setPropertyValue("PositionX", new Integer(160));
         xPSetList.setPropertyValue("PositionY", new Integer(10));
         xPSetList.setPropertyValue("Width", new Integer(70));
         xPSetList.setPropertyValue("Height", new Integer(12));
         xPSetList.setPropertyValue("Name", _shortcutName);
         xPSetList.setPropertyValue("Dropdown", Boolean.TRUE);
         xPSetList.setPropertyValue("MultiSelection", Boolean.FALSE);
         xPSetList.setPropertyValue("HelpText", "Script hint");
 
 
         Object oTFModel = xMultiServiceFactory.createInstance("com.sun.star.awt.UnoControlEditModel");
         // Set the properties at the model - keep in mind to pass the property names in alphabetical order!
         XPropertySet xFCModelMPSet = UnoRuntime.queryInterface(XPropertySet.class, oTFModel);
         xFCModelMPSet.setPropertyValue( "PositionX", new Integer( 10 ) );
         xFCModelMPSet.setPropertyValue( "PositionY", new Integer( 25 ) );
         xFCModelMPSet.setPropertyValue( "Width", new Integer( 220 ) );
         xFCModelMPSet.setPropertyValue( "Height", new Integer( 110 ) );
         xFCModelMPSet.setPropertyValue( "Name", _textFieldName );
         xFCModelMPSet.setPropertyValue( "TabIndex", new Short( (short)10 ) );
         xFCModelMPSet.setPropertyValue( "MultiLine", true );
         xFCModelMPSet.setPropertyValue( "AutoVScroll", true );
         xFCModelMPSet.setPropertyValue( "VScroll", true );
 
         // create the button model and set the properties
         Object checkboxModel = xMultiServiceFactory.createInstance(
             "com.sun.star.awt.UnoControlCheckBoxModel" );
         XPropertySet xPSetCheck = UnoRuntime.queryInterface(
             XPropertySet.class, checkboxModel );
         xPSetCheck.setPropertyValue( "PositionX", new Integer( 10 ) );
         xPSetCheck.setPropertyValue( "PositionY", new Integer( 150 ) );
         xPSetCheck.setPropertyValue( "Width", new Integer( 100 ) );
         xPSetCheck.setPropertyValue( "Height", new Integer( 14 ) );
         xPSetCheck.setPropertyValue( "Name", _checkboxName );
         xPSetCheck.setPropertyValue( "TabIndex", new Short( (short)20 ) );
        xPSetCheck.setPropertyValue( "Label", "Visiable" );
         xPSetCheck.setPropertyValue( "State", new Short((short) 1));
 
         // create the button model and set the properties
         Object buttonModel = xMultiServiceFactory.createInstance(
             "com.sun.star.awt.UnoControlButtonModel" );
         XPropertySet xPSetButton = UnoRuntime.queryInterface(
             XPropertySet.class, buttonModel );
         xPSetButton.setPropertyValue( "PositionX", new Integer( 120 ) );
         xPSetButton.setPropertyValue( "PositionY", new Integer( 150 ) );
         xPSetButton.setPropertyValue( "Width", new Integer( 50 ) );
         xPSetButton.setPropertyValue( "Height", new Integer( 14 ) );
         xPSetButton.setPropertyValue( "Name", _buttonName );
         xPSetButton.setPropertyValue( "TabIndex", new Short( (short)30 ) );
         xPSetButton.setPropertyValue( "Label", new String( "Insert" ) );
       
         // create a Cancel button model and set the properties
         Object cancelButtonModel = xMultiServiceFactory.createInstance(
             "com.sun.star.awt.UnoControlButtonModel" );
         XPropertySet xPSetCancelButton = UnoRuntime.queryInterface(
             XPropertySet.class, cancelButtonModel );
         xPSetCancelButton.setPropertyValue( "PositionX", new Integer( 180 ) );
         xPSetCancelButton.setPropertyValue( "PositionY", new Integer( 150 ) );
         xPSetCancelButton.setPropertyValue( "Width", new Integer( 50 ) );
         xPSetCancelButton.setPropertyValue( "Height", new Integer( 14 ) );
         xPSetCancelButton.setPropertyValue( "Name", _cancelButtonName );
         xPSetCancelButton.setPropertyValue( "TabIndex", new Short( (short)40 ) );
         xPSetCancelButton.setPropertyValue( "PushButtonType", new Short( (short)2 ) );
         xPSetCancelButton.setPropertyValue( "Label", new String( "Cancel" ) );
         
         // insert the control models into the dialog model
         XNameContainer xNameCont = UnoRuntime.queryInterface(
             XNameContainer.class, dialogModel );
         xNameCont.insertByName( _labelName, labelModel );
         xNameCont.insertByName( _shortcutName, shortcutModel );
         xNameCont.insertByName( _textFieldName, oTFModel );
         xNameCont.insertByName( _checkboxName, checkboxModel );
         xNameCont.insertByName( _buttonName, buttonModel );
         xNameCont.insertByName( _cancelButtonName, cancelButtonModel );
       
         // create the dialog control and set the model
         dialog = xMultiComponentFactory.createInstanceWithContext(
             "com.sun.star.awt.UnoControlDialog", _xComponentContext );
         XControl xControl = UnoRuntime.queryInterface(
             XControl.class, dialog );
         XControlModel xControlModel = UnoRuntime.queryInterface(
             XControlModel.class, dialogModel );      
         xControl.setModel( xControlModel );
 
         // add an action listener to the button control
         XButton xButton = UnoRuntime.queryInterface(XButton.class, getControl(_buttonName));
         xButton.addActionListener(new StandardActionListener( this ));
 
         XListBox shortcutList = (XListBox)UnoRuntime.queryInterface(XListBox.class, getControl(_shortcutName));
         shortcutList.addItemListener(new ShortcutSelectListener(this));
         shortcutList.setDropDownLineCount((short)15);
 
         shortcutList.addItem("Please select ...", (short)0 );
         shortcutList.addItem("Variable", (short)1 );
         shortcutList.addItem("List - begin", (short)2 );
         shortcutList.addItem("Break", (short)3 );
         shortcutList.addItem("List - end", (short)4 );
         shortcutList.addItem("If - begin", (short)5 );
         shortcutList.addItem("Else if", (short)6 );
         shortcutList.addItem("Else", (short)7 );
         shortcutList.addItem("If - end", (short)8 );
         shortcutList.addItem("Assign", (short)9 );
         shortcutList.addItem("Before table row", (short)10 );
         shortcutList.addItem("After table row", (short)11 );
 
 
         // create a peer
         Object toolkit = xMultiComponentFactory.createInstanceWithContext(
             "com.sun.star.awt.ExtToolkit", _xComponentContext );      
         XToolkit xToolkit = UnoRuntime.queryInterface(
             XToolkit.class, toolkit );
         XWindow xWindow = UnoRuntime.queryInterface(
             XWindow.class, xControl );
         xWindow.setVisible( false );
         xControl.createPeer( xToolkit, null );
       
         // execute the dialog
         xDialog = UnoRuntime.queryInterface(
             XDialog.class, dialog );
         xDialog.execute();
       
         // dispose the dialog
         XComponent xComponent = UnoRuntime.queryInterface(
             XComponent.class, dialog );
         xComponent.dispose();
 
         return script;
     }
 
     public void insertScript(int selected){
         if (selected == 0) {
             return;
         }
         String jooscript = "";
         switch (selected) {
             case 1:
                 jooscript = "${}";
                 break;
             case 2:
                 jooscript = "[#list  as ]";
                 break;
             case 3:
                 jooscript = "[#break]";
                 break;
             case 4:
                 jooscript = "[/#list]";
                 break;
             case 5:
                 jooscript = "[#if ]";
                 break;
             case 6:
                 jooscript = "[#elseif ]";
                 break;
             case 7:
                 jooscript = "[#else]";
                 break;
             case 8:
                 jooscript = "[/#if]";
                 break;
             case 9:
                 jooscript = "[#assign ]";
                 break;
             case 10:
                 jooscript = "@table:table-row\n";
                 break;
             case 11:
                 jooscript = "@/table:table-row\n";
                 break;
             default:
                 return;
         }
         XTextComponent xTextComponent = UnoRuntime.queryInterface(XTextComponent.class, getControl(_textFieldName));
         xTextComponent.setText(xTextComponent.getText() + jooscript);
     }
 
     public void performOkAction(){
         script = new Script();
         XTextComponent xTextComponent = UnoRuntime.queryInterface(XTextComponent.class, getControl( _textFieldName ));
         script.setValue(xTextComponent.getText());
         XCheckBox xCheckboxComponent = UnoRuntime.queryInterface(XCheckBox.class, getControl( _checkboxName ));
         script.setVisiable(xCheckboxComponent.getState()==1);
         this.xDialog.endExecute();
     }
 
     private Object getControl(String name){
         XControlContainer xControlCont = UnoRuntime.queryInterface(
             XControlContainer.class, dialog );
         return xControlCont.getControl( name );
     }
     
 }
