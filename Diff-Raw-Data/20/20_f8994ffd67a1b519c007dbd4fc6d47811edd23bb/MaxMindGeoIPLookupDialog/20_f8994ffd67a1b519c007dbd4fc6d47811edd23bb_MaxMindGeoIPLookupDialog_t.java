 /*
  * @author Daniel Einspanjer and Doug Moran
  * @since  April-16-2008
  * 
  */
 
 package com.maxmind.geoip;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.ShellAdapter;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.pentaho.di.core.Const;
 import org.pentaho.di.core.exception.KettleException;
 import org.pentaho.di.core.row.RowMetaInterface;
 import org.pentaho.di.trans.TransMeta;
 import org.pentaho.di.trans.step.BaseStepMeta;
 import org.pentaho.di.trans.step.StepDialogInterface;
 import org.pentaho.di.ui.core.dialog.ErrorDialog;
 import org.pentaho.di.ui.core.widget.ColumnInfo;
 import org.pentaho.di.ui.core.widget.TableView;
 import org.pentaho.di.ui.core.widget.TextVar;
 import org.pentaho.di.ui.trans.step.BaseStepDialog;
 
 
 public class MaxMindGeoIPLookupDialog extends BaseStepDialog implements StepDialogInterface
 {
   private Label        wlFieldname;
   private CCombo       wFieldname;
   private FormData     fdlFieldname, fdFieldname;
 
   private Label        wlDbType;
   private CCombo       wDbType;
   private FormData     fdlDbType;
 
   private TextVar      wFilename;
   private Button       wbbFilename; // Browse for a file
   
   private Button       wbDbInfo;
   private Label        wlDbInfo;
   private FormData     fdlDbInfo, fdDbInfo;
   
 	private Label        wlFields;
 	private TableView    wFields;
 	private FormData     fdlFields, fdFields;
 
 	private ColumnInfo[] colinf;
 	private MaxMindGeoIPLookupMeta  input;
 	private boolean gotPreviousFields=false;
 	
 	public MaxMindGeoIPLookupDialog(Shell parent, Object in, TransMeta transMeta, String sname)
 	{
 		super(parent, (BaseStepMeta)in, transMeta, sname);
 		input=(MaxMindGeoIPLookupMeta)in;
 	}
 
 	public String open()
 	{
 		Shell parent = getParent();
 		Display display = parent.getDisplay();
 
         shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
  		props.setLook(shell);
         setShellImage(shell, input);
 
 		ModifyListener lsMod = new ModifyListener() 
 		{
 			public void modifyText(ModifyEvent e) 
 			{
 				input.setChanged();
 			}
 		};
 		changed = input.hasChanged();
 
 		FormLayout formLayout = new FormLayout ();
 		formLayout.marginWidth  = Const.FORM_MARGIN;
 		formLayout.marginHeight = Const.FORM_MARGIN;
 
 		shell.setLayout(formLayout);
 		shell.setText(Messages.getString("MaxMindGeoIPLookupDialog.Shell.Title")); //$NON-NLS-1$
 		
 		int middle = props.getMiddlePct();
 		int margin = Const.MARGIN;
 
 		// Stepname line
 		wlStepname=new Label(shell, SWT.RIGHT);
 		wlStepname.setText(Messages.getString("MaxMindGeoIPLookupDialog.StepName.Label")); //$NON-NLS-1$
  		props.setLook(wlStepname);
 		fdlStepname=new FormData();
 		fdlStepname.left = new FormAttachment(0, 0);
 		fdlStepname.right= new FormAttachment(middle, -margin);
 		fdlStepname.top  = new FormAttachment(0, margin);
 		wlStepname.setLayoutData(fdlStepname);
 		wStepname=new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
 		wStepname.setText(stepname);
  		props.setLook(wStepname);
 		wStepname.addModifyListener(lsMod);
 		fdStepname=new FormData();
 		fdStepname.left = new FormAttachment(middle, 0);
 		fdStepname.top  = new FormAttachment(0, margin);
 		fdStepname.right= new FormAttachment(100, 0);
 		wStepname.setLayoutData(fdStepname);
     Control lastControl = wStepname;
 
     // Fieldname line
     wlFieldname=new Label(shell, SWT.RIGHT);
     wlFieldname.setText("Incoming IP address field to lookup"); //$NON-NLS-1$
     wlFieldname.setToolTipText("IP Address field must be a String type (Only String fields are shown)"); //$NON-NLS-1$
     props.setLook(wlFieldname);
     fdlFieldname=new FormData();
     fdlFieldname.left = new FormAttachment(0, 0);
     fdlFieldname.right= new FormAttachment(middle, -margin);
     fdlFieldname.top  = new FormAttachment(lastControl, margin);
     wlFieldname.setLayoutData(fdlFieldname);
 
     wFieldname=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
     props.setLook(wFieldname);
     wFieldname.addModifyListener(lsMod);
     fdFieldname=new FormData();
     fdFieldname.left = new FormAttachment(middle, 0);
     fdFieldname.top  = new FormAttachment(lastControl, margin);
     fdFieldname.right= new FormAttachment(100, 0);
     wFieldname.setLayoutData(fdFieldname);
     lastControl = wFieldname;
     
     // Filename...
     // The filename browse button
     wbbFilename = new Button(shell, SWT.PUSH | SWT.CENTER);
     props.setLook(wbbFilename);
     wbbFilename.setText(Messages.getString("System.Button.Browse")); //$NON-NLS-1$
     wbbFilename.setToolTipText(Messages.getString("System.Tooltip.BrowseForFileOrDirAndAdd")); //$NON-NLS-1$
     FormData fdbFilename = new FormData();
     fdbFilename.top = new FormAttachment(lastControl, margin);
     fdbFilename.right = new FormAttachment(100, 0);
     wbbFilename.setLayoutData(fdbFilename);
 
     // The field itself...
     Label wlFilename = new Label(shell, SWT.RIGHT);
     wlFilename.setText("Maxmind DB File");
     props.setLook(wlFilename);
     FormData fdlFilename = new FormData();
     fdlFilename.top = new FormAttachment(lastControl, margin);
     fdlFilename.left = new FormAttachment(0, 0);
     fdlFilename.right = new FormAttachment(middle, -margin);
     wlFilename.setLayoutData(fdlFilename);
     wFilename = new TextVar(transMeta, shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
     props.setLook(wFilename);
     wFilename.addModifyListener(lsMod);
     FormData fdFilename = new FormData();
     fdFilename.top = new FormAttachment(lastControl, margin);
     fdFilename.left = new FormAttachment(middle, 0);
     fdFilename.right = new FormAttachment(wbbFilename, -margin);
     wFilename.setLayoutData(fdFilename);
     lastControl = wFilename;
 
     // DB Type
     wlDbType=new Label(shell, SWT.RIGHT);
     wlDbType.setText("Maxmind Database Type"); //$NON-NLS-1$
     props.setLook(wlDbType);
     fdlDbType=new FormData();
     fdlDbType.left = new FormAttachment(0, 0);
     fdlDbType.right= new FormAttachment(middle, -margin);
     fdlDbType.top  = new FormAttachment(lastControl, margin);
     wlDbType.setLayoutData(fdlDbType);
 
     wDbType=new CCombo(shell, SWT.BORDER | SWT.READ_ONLY);
     props.setLook(wDbType);
     wDbType.addModifyListener(lsMod);
     fdlDbType=new FormData();
     fdlDbType.left = new FormAttachment(middle, 0);
     fdlDbType.top  = new FormAttachment(lastControl, margin);
     fdlDbType.right= new FormAttachment(100, 0);
     wDbType.setLayoutData(fdlDbType);
     wDbType.setItems( MaxMindGeoIP.getDatabaseTypes() );
     lastControl = wDbType;
     
     // DBInfo line
     wbDbInfo=new Button(shell, SWT.PUSH| SWT.CENTER);
     wbDbInfo.setText("Get DB Info"); //$NON-NLS-1$
     wbDbInfo.setToolTipText("Examine the DB File and describe it");
     props.setLook(wbDbInfo);
     fdlDbInfo=new FormData();
     fdlDbInfo.left = new FormAttachment(0, 0);
     fdlDbInfo.right= new FormAttachment(middle, -margin);
     fdlDbInfo.top  = new FormAttachment(lastControl, margin);
     wbDbInfo.setLayoutData(fdlDbInfo);
     wlDbInfo=new Label(shell, SWT.LEFT);
    wlDbInfo.setText("  "); //$NON-NLS-1$
     props.setLook(wlDbInfo);
     fdDbInfo=new FormData();
     fdDbInfo.left = new FormAttachment(middle, 0);
     fdDbInfo.top  = new FormAttachment(lastControl, margin);
     fdDbInfo.right= new FormAttachment(100, 0);
     wlDbInfo.setLayoutData(fdDbInfo);
     lastControl = wbDbInfo;
     
     wlFields=new Label(shell, SWT.RIGHT);
 		wlFields.setText(Messages.getString("MaxMindGeoIPLookupDialog.Fields.Label")); //$NON-NLS-1$
  		props.setLook(wlFields);
 		fdlFields=new FormData();
 		fdlFields.left = new FormAttachment(0, 0);
 		fdlFields.top  = new FormAttachment(lastControl, margin);
 		wlFields.setLayoutData(fdlFields);
 
 		wOK=new Button(shell, SWT.PUSH);
 		wOK.setText(Messages.getString("System.Button.OK")); //$NON-NLS-1$
 		wCancel=new Button(shell, SWT.PUSH);
 		wCancel.setText(Messages.getString("System.Button.Cancel")); //$NON-NLS-1$
 
 		setButtonPositions(new Button[] { wOK, wCancel }, margin, null);
 
 		//TODO: Support list of fields to add
 		//TODO: Need an enum of geoip data types
     final int fieldsRows = 0;
 
     colinf = new ColumnInfo[] {
         new ColumnInfo(
             Messages.getString("MaxMindGeoIPLookupDialog.ColumnInfo.NewField"), ColumnInfo.COLUMN_TYPE_TEXT, false), //$NON-NLS-1$
         new ColumnInfo(
             Messages.getString("MaxMindGeoIPLookupDialog.ColumnInfo.LookupType"), ColumnInfo.COLUMN_TYPE_CCOMBO, new String[] { "" }), //$NON-NLS-1$ //$NON-NLS-2$
         new ColumnInfo(
             Messages.getString("MaxMindGeoIPLookupDialog.ColumnInfo.IfNull"), ColumnInfo.COLUMN_TYPE_TEXT, false) }; //$NON-NLS-1$
     wFields = new TableView(transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, colinf, fieldsRows, lsMod,
         props);
 
     fdFields = new FormData();
     fdFields.left = new FormAttachment(0, 0);
     fdFields.top = new FormAttachment(wlFields, margin);
 		fdFields.right  = new FormAttachment(100, 0);
 		fdFields.bottom = new FormAttachment(wOK, -2*margin);
 		wFields.setLayoutData(fdFields);
 
 		// Add listeners
 		lsOK       = new Listener() { public void handleEvent(Event e) { ok();     } };
 		lsCancel   = new Listener() { public void handleEvent(Event e) { cancel(); } };
 		
 		wOK.addListener    (SWT.Selection, lsOK    );
 		wCancel.addListener(SWT.Selection, lsCancel);
 		
 		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
 		
 		wStepname.addSelectionListener( lsDef );
 				
 		// Detect X or ALT-F4 or something that kills this window...
 		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
 
     // Lookup field Selector
     wFieldname.addFocusListener(new FocusListener() {
         public void focusLost(org.eclipse.swt.events.FocusEvent e){}
     
         public void focusGained(org.eclipse.swt.events.FocusEvent e) {
             Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
             shell.setCursor(busy);
             getInputFields();
             shell.setCursor(null);
             busy.dispose();
         }
       }
     );
       
     // DBTYPE field Selector - set the field selections when DB type changes
     wDbType.addModifyListener(new ModifyListener() {
       public void modifyText(ModifyEvent arg0) {
         colinf[1].setComboValues( MaxMindGeoIP.getDbFieldanmes( wDbType.getText() ) );
       }
     } );
       
     // Listen to the browse button next to the file name
     wbbFilename.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent e) {
           FileDialog dialog = new FileDialog(shell, SWT.OPEN);
           dialog.setFilterExtensions(new String[] { "*.dat", "*" });  //$NON-NLS-1$//$NON-NLS-2$
           dialog.setFilterNames(new String[] { "Maxmind GeoIP data", Messages.getString("System.FileType.AllFiles") }); //$NON-NLS-2$
 
           if (wFilename.getText() != null) {
             String fname = transMeta.environmentSubstitute(wFilename.getText());
             dialog.setFileName(fname);
           }
   
           if (dialog.open() != null) {
             String str = dialog.getFilterPath() + System.getProperty("file.separator") + dialog.getFileName(); //$NON-NLS-1$
             wFilename.setText(str);
             updateDbInfo();
           }
         }
       }
     );
 
     // Get info about the selected database
     wbDbInfo.addSelectionListener(new SelectionAdapter() {
         public void widgetSelected(SelectionEvent se) {
           updateDbInfo();
         }
       }
     );
 
 		// Set the shell size, based upon previous time...
 		setSize();
 		
 		getData();
 		input.setChanged(changed);
 
 	
 		shell.open();
 		while (!shell.isDisposed())
 		{
 				if (!display.readAndDispatch()) display.sleep();
 		}
 		return stepname;
 	}
 	
   public void getData() 
   {
 
     if (input.getIpAddressFieldName() != null)
       wFieldname.setText(input.getIpAddressFieldName());
     if (input.getDbLocation() != null) {
       wFilename.setText(input.getDbLocation());
       updateDbInfo();
     }
     if (input.getDbType() != null) {
       wDbType.setText(input.getDbType());
     }
     
     
     Table table = wFields.table;
     if (input.getFieldName().length>0) table.removeAll();
     for (int i=0;i<input.getFieldName().length;i++)
     {
       TableItem ti = new TableItem(table, SWT.NONE);
       ti.setText(0, ""+(i+1)); //$NON-NLS-1$
       ti.setText(1, (input.getFieldName()[i] == null) ? "" : input.getFieldName()[i] ); //$NON-NLS-1$
       ti.setText(2, (input.getFieldLookupType()[i] == null) ? "" : input.getFieldLookupType()[i] ); //$NON-NLS-1$
       ti.setText(3, (input.getFieldIfNull()[i] == null) ? "" : input.getFieldIfNull()[i] ); //$NON-NLS-1$
     }
     wFields.setRowNums();
     wFields.optWidth(true);
 
     wStepname.selectAll();
   }
 
 	private void updateDbInfo() {
     wlDbInfo.setText( MaxMindGeoIP.getDbInfo( wFilename.getText() ) );
 	}
 	
   private void getInputFields()
   {
    if(!gotPreviousFields)
    {
      gotPreviousFields=true;
     try
     {
       String fieldname=wFieldname.getText();
       
       wFieldname.removeAll();
       RowMetaInterface r = transMeta.getPrevStepFields(stepname);
       if (r!=null)
       { // Only let them select the string fields
         for ( int i = 0; i < r.size(); ++i ) {
            if ( r.getValueMeta(i).isString() ) {
              wFieldname.add( r.getValueMeta(i).getName() );
            }
         }
         if(fieldname!=null) wFieldname.setText(fieldname);
       }
     }
     catch(KettleException ke)
     {
       new ErrorDialog(shell, Messages.getString("ValueMapperDialog.FailedToGetFields.DialogTitle"), Messages.getString("ValueMapperDialog.FailedToGetFields.DialogMessage"), ke); //$NON-NLS-1$ //$NON-NLS-2$
     }
    }
   }
 
 	
 	private void cancel()
 	{
 		stepname=null;
 		input.setChanged(changed);
 		dispose();
 	}
 	
   private void ok()
   {
   	if (Const.isEmpty(wStepname.getText())) return;
 
     stepname = wStepname.getText(); // return value
 
     input.setIpAddressFieldName(wFieldname.getText());
     input.setDbLocation(wFilename.getText());
     input.setDbType(wDbType.getText());
 
     //Table table = wFields.table;
     int nrfields = wFields.nrNonEmpty();
 
     input.allocate(nrfields);
 
  	wFields.nrNonEmpty();
     for (int i = 0; i < input.getFieldName().length; i++)
     {
       final TableItem ti = wFields.getNonEmpty(i);
       input.getFieldName()[i] = ti.getText(1);
       input.getFieldLookupType()[i] = ti.getText(2);
       input.getFieldIfNull()[i] = ti.getText(3);
     }
 
     dispose();
   }
 }
