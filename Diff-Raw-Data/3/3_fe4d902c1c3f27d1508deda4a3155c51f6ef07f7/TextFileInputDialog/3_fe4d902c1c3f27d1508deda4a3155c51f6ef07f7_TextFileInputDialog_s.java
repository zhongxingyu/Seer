  /**********************************************************************
  **                                                                   **
  **               This code belongs to the KETTLE project.            **
  **                                                                   **
  ** It belongs to, is maintained by and is copyright 1999-2005 by     **
  **                                                                   **
  **      i-Bridge bvba                                                **
  **      Fonteinstraat 70                                             **
  **      9400 OKEGEM                                                  **
  **      Belgium                                                      **
  **      http://www.kettle.be                                         **
  **      info@kettle.be                                               **
  **                                                                   **
  **********************************************************************/
  
 /*
  * Created on 18-mei-2003
  *
  */
 
 package be.ibridge.kettle.trans.step.textfileinput;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.Locale;
 import java.util.Vector;
 import java.util.zip.ZipInputStream;
 
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.custom.CTabFolder;
 import org.eclipse.swt.custom.CTabItem;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.ShellAdapter;
 import org.eclipse.swt.events.ShellEvent;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 
 import be.ibridge.kettle.core.ColumnInfo;
 import be.ibridge.kettle.core.Const;
 import be.ibridge.kettle.core.Props;
 import be.ibridge.kettle.core.dialog.EnterNumberDialog;
 import be.ibridge.kettle.core.dialog.EnterSelectionDialog;
 import be.ibridge.kettle.core.dialog.EnterTextDialog;
 import be.ibridge.kettle.core.dialog.ErrorDialog;
 import be.ibridge.kettle.core.dialog.PreviewRowsDialog;
 import be.ibridge.kettle.core.exception.KettleException;
 import be.ibridge.kettle.core.util.StringUtil;
 import be.ibridge.kettle.core.value.Value;
 import be.ibridge.kettle.core.widget.TableView;
 import be.ibridge.kettle.trans.Trans;
 import be.ibridge.kettle.trans.TransMeta;
 import be.ibridge.kettle.trans.TransPreviewFactory;
 import be.ibridge.kettle.trans.dialog.TransPreviewProgressDialog;
 import be.ibridge.kettle.trans.step.BaseStepDialog;
 import be.ibridge.kettle.trans.step.BaseStepMeta;
 import be.ibridge.kettle.trans.step.StepDialogInterface;
 import be.ibridge.kettle.trans.step.StepMeta;
 import be.ibridge.kettle.trans.step.fileinput.FileInputList;
 
 
 public class TextFileInputDialog extends BaseStepDialog implements StepDialogInterface
 {
 	private static final String[] YES_NO_COMBO = new String[] { Messages.getString("System.Combo.No"), Messages.getString("System.Combo.Yes") };
 	
 	private CTabFolder   wTabFolder;
 	private FormData     fdTabFolder;
 	
 	private CTabItem     wFileTab;
     private CTabItem     wContentTab;
     private CTabItem     wErrorTab;
     private CTabItem     wFilterTab;
     private CTabItem     wFieldsTab;
 
     private ScrolledComposite wFileSComp;
     private ScrolledComposite wContentSComp;
     private ScrolledComposite wErrorSComp;
 
 	private Composite wFileComp;
     private Composite wContentComp;
     private Composite wErrorComp;
     private Composite wFilterComp;
     private Composite wFieldsComp;
     
 	private FormData     fdFileComp;
     private FormData     fdContentComp;
     private FormData     fdErrorComp;
     private FormData     fdFilterComp;
     private FormData     fdFieldsComp;
 
     private Label        wlAccFilenames;
     private Button       wAccFilenames;
     private FormData     fdlAccFilenames, fdAccFilenames;
     
     private Label        wlAccField;
     private Text         wAccField;
     private FormData     fdlAccField, fdAccField;
 
     private Label        wlAccStep;
     private CCombo       wAccStep;
     private FormData     fdlAccStep, fdAccStep;
     
 	private Label        wlFilename;
 	private Button       wbbFilename; // Browse: add file or directory
 	private Button       wbvFilename; // Variable
 	private Button       wbdFilename; // Delete
 	private Button       wbeFilename; // Edit
 	private Button       wbaFilename; // Add or change
 	private Text         wFilename;
 	private FormData     fdlFilename, fdbFilename, fdbvFilename, fdbdFilename, fdbeFilename, fdbaFilename, fdFilename;
 
 	private Label        wlFilenameList;
 	private TableView    wFilenameList;
 	private FormData     fdlFilenameList, fdFilenameList;
 
 	private Label        wlFilemask;
 	private Text         wFilemask;
 	private FormData     fdlFilemask, fdFilemask;
 
 	private Button       wbShowFiles;
 	private FormData     fdbShowFiles;
 
     private Button       wFirst;
     private FormData     fdFirst;
     private Listener     lsFirst;
 
     private Button       wFirstHeader;
     private FormData     fdFirstHeader;
     private Listener     lsFirstHeader;
 
 	private Label        wlFiletype;
 	private CCombo       wFiletype;
 	private FormData     fdlFiletype, fdFiletype;
 
 	private Label        wlSeparator;
 	private Button       wbSeparator;
 	private Text         wSeparator;
 	private FormData     fdlSeparator, fdbSeparator, fdSeparator;
 
 	private Label        wlEnclosure;
 	private Text         wEnclosure;
 	private FormData     fdlEnclosure, fdEnclosure;
     
     private Label        wlEnclBreaks;
     private Button       wEnclBreaks;
     private FormData     fdlEnclBreaks, fdEnclBreaks;
 
     private Label        wlEscape;
     private Text         wEscape;
     private FormData     fdlEscape, fdEscape;
 
 	private Label        wlHeader;
 	private Button       wHeader;
 	private FormData     fdlHeader, fdHeader;
 
     private Label        wlNrHeader;
     private Text         wNrHeader;
     private FormData     fdlNrHeader, fdNrHeader;
 
 	private Label        wlFooter;
 	private Button       wFooter;
 	private FormData     fdlFooter, fdFooter;
 
     private Label        wlNrFooter;
     private Text         wNrFooter;
     private FormData     fdlNrFooter, fdNrFooter;
 
     private Label        wlWraps;
     private Button       wWraps;
     private FormData     fdlWraps, fdWraps;
 
     private Label        wlNrWraps;
     private Text         wNrWraps;
     private FormData     fdlNrWraps, fdNrWraps;
 
     private Label        wlLayoutPaged;
     private Button       wLayoutPaged;
     private FormData     fdlLayoutPaged, fdLayoutPaged;
 
     private Label        wlNrLinesPerPage;
     private Text         wNrLinesPerPage;
     private FormData     fdlNrLinesPerPage, fdNrLinesPerPage;
 
     private Label        wlNrLinesDocHeader;
     private Text         wNrLinesDocHeader;
     private FormData     fdlNrLinesDocHeader, fdNrLinesDocHeader;
 
 	private Label        wlZipped;
 	private Button       wZipped;
 	private FormData     fdlZipped, fdZipped;
 	
 	private Label        wlNoempty;
 	private Button       wNoempty;
 	private FormData     fdlNoempty, fdNoempty;
 
 	private Label        wlInclFilename;
 	private Button       wInclFilename;
 	private FormData     fdlInclFilename, fdInclFilename;
 
 	private Label        wlInclFilenameField;
 	private Text         wInclFilenameField;
 	private FormData     fdlInclFilenameField, fdInclFilenameField;
 
 	private Label        wlInclRownum;
 	private Button       wInclRownum;
 	private FormData     fdlInclRownum, fdRownum;
 
 	private Label        wlInclRownumField;
 	private Text         wInclRownumField;
 	private FormData     fdlInclRownumField, fdInclRownumField;
 	
 	private Label        wlFormat;
 	private CCombo       wFormat;
 	private FormData     fdlFormat, fdFormat;
 
     private Label        wlEncoding;
     private CCombo       wEncoding;
     private FormData     fdlEncoding, fdEncoding;
 
 	private Label        wlLimit;
 	private Text         wLimit;
 	private FormData     fdlLimit, fdLimit;
     
     private Label        wlDateLenient;
     private Button       wDateLenient;
     private FormData     fdlDateLenient, fdDateLenient;
 
 	private Label        wlDateLocale;
 	private CCombo       wDateLocale;
 	private FormData     fdlDateLocale, fdDateLocale;
     
 
 
     // ERROR HANDLING...
     private Label        wlErrorIgnored;
     private Button       wErrorIgnored;
     private FormData     fdlErrorIgnored, fdErrorIgnored;
     
     private Label        wlSkipErrorLines;
     private Button       wSkipErrorLines;
     private FormData     fdlSkipErrorLines, fdSkipErrorLines;
 
     private Label        wlErrorCount;
     private Text         wErrorCount;
     private FormData     fdlErrorCount, fdErrorCount;
 
     private Label        wlErrorFields;
     private Text         wErrorFields;
     private FormData     fdlErrorFields, fdErrorFields;
 
     private Label        wlErrorText;
     private Text         wErrorText;
     private FormData     fdlErrorText, fdErrorText;
 
     // New entries for intelligent error handling AKA replay functionality
     // Bad files destination directory
     private Label        wlWarnDestDir;
     private Button       wbbWarnDestDir; // Browse: add file or directory
     private Button       wbvWarnDestDir; // Variable
     private Text         wWarnDestDir;
     private FormData     fdlWarnDestDir, fdbBadDestDir, fdbvWarnDestDir, fdBadDestDir;
     private Label        wlWarnExt;
     private Text         wWarnExt;
     private FormData     fdlWarnDestExt, fdWarnDestExt;
 
     // Error messages files destination directory
     private Label        wlErrorDestDir;
     private Button       wbbErrorDestDir; // Browse: add file or directory
     private Button       wbvErrorDestDir; // Variable
     private Text         wErrorDestDir;
     private FormData     fdlErrorDestDir, fdbErrorDestDir, fdbvErrorDestDir, fdErrorDestDir;
     private Label        wlErrorExt;
     private Text         wErrorExt;
     private FormData     fdlErrorDestExt, fdErrorDestExt;
 
     // Line numbers files destination directory
     private Label        wlLineNrDestDir;
     private Button       wbbLineNrDestDir; // Browse: add file or directory
     private Button       wbvLineNrDestDir; // Variable
     private Text         wLineNrDestDir;
     private FormData     fdlLineNrDestDir, fdbLineNrDestDir, fdbvLineNrDestDir, fdLineNrDestDir;
     private Label        wlLineNrExt;
     private Text         wLineNrExt;
     private FormData     fdlLineNrDestExt, fdLineNrDestExt;
 
     private TableView    wFilter;
     private FormData     fdFilter;
     
 	private TableView    wFields;
 	private FormData     fdFields;
 
 	private TextFileInputMeta input;
 
 	// Wizard info...
 	private Vector fields;
     
     private int middle, margin;
     private ModifyListener lsMod;
 		
 	public static final int dateLengths[] = new int[]
 		{
 			23, 19, 14, 10, 10, 10, 10, 8, 8, 8, 8, 6, 6
 		}
 		;
     
     private boolean gotEncodings = false;
 
 	public TextFileInputDialog(Shell parent, Object in, TransMeta transMeta, String sname)
 	{
 		super(parent, (BaseStepMeta)in, transMeta, sname);
 		input=(TextFileInputMeta)in;
 	}
 
 	public String open()
 	{
 		Shell parent = getParent();
 		Display display = parent.getDisplay();
 
 		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
  		props.setLook(shell);
 
 		lsMod = new ModifyListener() 
 		{
 			public void modifyText(ModifyEvent e) 
 			{
 				input.setChanged();
 			}
 		};
 		changed         = input.hasChanged();
 		
 		FormLayout formLayout = new FormLayout ();
 		formLayout.marginWidth  = Const.FORM_MARGIN;
 		formLayout.marginHeight = Const.FORM_MARGIN;
 
 		shell.setLayout(formLayout);
 		shell.setText(Messages.getString("TextFileInputDialog.DialogTitle"));
 		
 		middle = props.getMiddlePct();
 		margin = Const.MARGIN;
 
 		// Stepname line
 		wlStepname=new Label(shell, SWT.RIGHT);
 		wlStepname.setText(Messages.getString("System.Label.StepName"));
  		props.setLook(wlStepname);
 		fdlStepname=new FormData();
 		fdlStepname.left = new FormAttachment(0, 0);
 		fdlStepname.top  = new FormAttachment(0, margin);
 		fdlStepname.right= new FormAttachment(middle, -margin);
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
 
 		wTabFolder = new CTabFolder(shell, SWT.BORDER);
  		props.setLook(wTabFolder, Props.WIDGET_STYLE_TAB);
 
  		addFilesTab();
 		addContentTab();
         addErrorTab();
 		addFiltersTabs();
         addFieldsTabs();
 
 		fdTabFolder = new FormData();
 		fdTabFolder.left  = new FormAttachment(0, 0);
 		fdTabFolder.top   = new FormAttachment(wStepname, margin);
 		fdTabFolder.right = new FormAttachment(100, 0);
 		fdTabFolder.bottom= new FormAttachment(100, -50);
 		wTabFolder.setLayoutData(fdTabFolder);
 		
 		wOK=new Button(shell, SWT.PUSH);
 		wOK.setText(Messages.getString("System.Button.OK"));
 
 		wPreview=new Button(shell, SWT.PUSH);
 		wPreview.setText(Messages.getString("TextFileInputDialog.Preview.Button"));
 		
 		wCancel=new Button(shell, SWT.PUSH);
 		wCancel.setText(Messages.getString("System.Button.Cancel"));
 		
 		setButtonPositions(new Button[] { wOK, wPreview, wCancel }, margin, wTabFolder);
 
 		// Add listeners
 		lsOK          = new Listener() { public void handleEvent(Event e) { ok();           } };
 		lsFirst       = new Listener() { public void handleEvent(Event e) { first(false);   } };
         lsFirstHeader = new Listener() { public void handleEvent(Event e) { first(true);    } };
 		lsGet         = new Listener() { public void handleEvent(Event e) { get();          } };
 		lsPreview     = new Listener() { public void handleEvent(Event e) { preview();      } };
 		lsCancel      = new Listener() { public void handleEvent(Event e) { cancel();       } };
 		
 		wOK.addListener           (SWT.Selection, lsOK          );
 		wFirst.addListener        (SWT.Selection, lsFirst       );
         wFirstHeader.addListener  (SWT.Selection, lsFirstHeader );
 		wGet.addListener          (SWT.Selection, lsGet         );
 		wPreview.addListener      (SWT.Selection, lsPreview     );
 		wCancel.addListener       (SWT.Selection, lsCancel      );
 		
 		lsDef=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
 		
         wAccFilenames.addSelectionListener( lsDef );
 		wStepname.addSelectionListener( lsDef );
 		// wFilename.addSelectionListener( lsDef );
 		wSeparator.addSelectionListener( lsDef );
 		wLimit.addSelectionListener( lsDef );
 		wInclRownumField.addSelectionListener( lsDef );
 		wInclFilenameField.addSelectionListener( lsDef );
         wNrHeader.addSelectionListener( lsDef );
         wNrFooter.addSelectionListener( lsDef );
         wNrWraps.addSelectionListener( lsDef );
         wWarnDestDir.addSelectionListener( lsDef );
         wWarnExt.addSelectionListener( lsDef );
         wErrorDestDir.addSelectionListener( lsDef );
         wErrorExt.addSelectionListener( lsDef );
         wLineNrDestDir.addSelectionListener( lsDef );
         wLineNrExt.addSelectionListener( lsDef );
 
 		// Add the file to the list of files...
 		SelectionAdapter selA = new SelectionAdapter()
 		{
 			public void widgetSelected(SelectionEvent arg0)
 			{
 				wFilenameList.add(new String[] { wFilename.getText(), wFilemask.getText() } );
 				wFilename.setText("");
 				wFilemask.setText("");
 				wFilenameList.removeEmptyRows();
 				wFilenameList.setRowNums();
                 wFilenameList.optWidth(true);
 			}
 		};
 		wbaFilename.addSelectionListener(selA);
 		wFilename.addSelectionListener(selA);
 		
 		// Delete files from the list of files...
 		wbdFilename.addSelectionListener(new SelectionAdapter()
 		{
 			public void widgetSelected(SelectionEvent arg0)
 			{
 				int idx[] = wFilenameList.getSelectionIndices();
 				wFilenameList.remove(idx);
 				wFilenameList.removeEmptyRows();
 				wFilenameList.setRowNums();
 			}
 		});
 
 		// Edit the selected file & remove from the list...
 		wbeFilename.addSelectionListener(new SelectionAdapter()
 		{
 			public void widgetSelected(SelectionEvent arg0)
 			{
 				int idx = wFilenameList.getSelectionIndex();
 				if (idx>=0)
 				{
 					String string[] = wFilenameList.getItem(idx);
 					wFilename.setText(string[0]);
 					wFilemask.setText(string[1]);
 					wFilenameList.remove(idx);
 				}
 				wFilenameList.removeEmptyRows();
 				wFilenameList.setRowNums();
 			}
 		});
 
 		// Show the files that are selected at this time...
 		wbShowFiles.addSelectionListener(new SelectionAdapter() 
 			{
 				public void widgetSelected(SelectionEvent e) 
 				{
 					TextFileInputMeta tfii = new TextFileInputMeta();
 					getInfo(tfii);
 					String files[] = tfii.getFilePaths();
 					if (files!=null && files.length>0)
 					{
 						EnterSelectionDialog esd = new EnterSelectionDialog(shell, props, files, "Files read", "Files read:");
 						esd.setViewOnly();
 						esd.open();
 					}
 					else
 					{
 						MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
 						mb.setMessage(Messages.getString("TextFileInputDialog.NoFilesFound.DialogMessage"));
 						mb.setText(Messages.getString("System.Dialog.Error.Title"));
 						mb.open(); 
 					}
 				}
 			}
 		);
 		// Allow the insertion of tabs as separator...
 		wbSeparator.addSelectionListener(new SelectionAdapter() 
 			{
 				public void widgetSelected(SelectionEvent se) 
 				{
 					wSeparator.insert("\t");
 				}
 			}
 		);
         
         SelectionAdapter lsFlags = new SelectionAdapter() 
         {
             public void widgetSelected(SelectionEvent e) 
             {
                 setFlags();
             }
         };
         
 		// Enable/disable the right fields...
         wInclFilename.addSelectionListener( lsFlags );
         wInclRownum.addSelectionListener( lsFlags );
         wErrorIgnored.addSelectionListener(lsFlags);
         wHeader.addSelectionListener(lsFlags);
         wFooter.addSelectionListener(lsFlags);
         wWraps.addSelectionListener(lsFlags);
         wLayoutPaged.addSelectionListener(lsFlags);
 
 		// Listen to the Browse... button
 		wbbFilename.addSelectionListener
 		(
 			new SelectionAdapter()
 			{
 				public void widgetSelected(SelectionEvent e) 
 				{
 					if (wFilemask.getText()!=null && wFilemask.getText().length()>0) // A mask: a directory!
 					{
 						DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
 						if (wFilename.getText()!=null)
 						{
 							String fpath = StringUtil.environmentSubstitute(wFilename.getText());
 							dialog.setFilterPath( fpath );
 						}
 						
 						if (dialog.open()!=null)
 						{
 							String str= dialog.getFilterPath();
 							wFilename.setText(str);
 						}
 					}
 					else
 					{
 						FileDialog dialog = new FileDialog(shell, SWT.OPEN);
 						if (wZipped.getSelection())
 						{
 							dialog.setFilterExtensions(new String[] {"*.zip", "*.txt;*.csv", "*.csv", "*.txt", "*"});
 						}
 						else
 						{
 							dialog.setFilterExtensions(new String[] {"*.txt;*.csv", "*.csv", "*.txt", "*"});
 						}
 						if (wFilename.getText()!=null)
 						{
 							String fname = StringUtil.environmentSubstitute(wFilename.getText());
 							dialog.setFileName( fname );
 						}
 						
 						if (wZipped.getSelection())
 						{
 							dialog.setFilterNames(new String[] {Messages.getString("System.FileType.ZipFiles"), Messages.getString("TextFileInputDialog.FileType.TextAndCSVFiles"), Messages.getString("System.FileType.CSVFiles"), Messages.getString("System.FileType.TextFiles"), Messages.getString("System.FileType.AllFiles")});
 						}
 						else
 						{
 							dialog.setFilterNames(new String[] {Messages.getString("TextFileInputDialog.FileType.TextAndCSVFiles"), Messages.getString("System.FileType.CSVFiles"), Messages.getString("System.FileType.TextFiles"), Messages.getString("System.FileType.AllFiles")});
 						}
 						
 						if (dialog.open()!=null)
 						{
 							String str = dialog.getFilterPath()+System.getProperty("file.separator")+dialog.getFileName();
 							wFilename.setText(str);
 						}
 					}
 				}
 			}
 		);
 		
 		// Detect X or ALT-F4 or something that kills this window...
 		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
 
 		wTabFolder.setSelection(0);
 
 		// Set the shell size, based upon previous time...
 		getData(input);
 
         setSize();
 
 		shell.open();
 		while (!shell.isDisposed())
 		{
 				if (!display.readAndDispatch()) display.sleep();
 		}
 		return stepname;
 	}
 
 	private void addFilesTab()
     {
         //////////////////////////
         // START OF FILE TAB   ///
         //////////////////////////
         
         wFileTab=new CTabItem(wTabFolder, SWT.NONE);
         wFileTab.setText(Messages.getString("TextFileInputDialog.FileTab.TabTitle"));
         
         wFileSComp = new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
         wFileSComp.setLayout(new FillLayout());
         
         wFileComp = new Composite(wFileSComp, SWT.NONE );
         props.setLook(wFileComp);
 
         FormLayout fileLayout = new FormLayout();
         fileLayout.marginWidth  = 3;
         fileLayout.marginHeight = 3;
         wFileComp.setLayout(fileLayout);
         
         // Get from chef checkbox
         wlAccFilenames=new Label(wFileComp, SWT.RIGHT);
         wlAccFilenames.setText(Messages.getString("TextFileInputDialog.AcceptFilenames.Label"));
         props.setLook(wlAccFilenames);
         fdlAccFilenames=new FormData();
         fdlAccFilenames.left = new FormAttachment(0, 0);
         fdlAccFilenames.top  = new FormAttachment(0,0);
         fdlAccFilenames.right= new FormAttachment(middle, -margin);
         wlAccFilenames.setLayoutData(fdlAccFilenames);
         wAccFilenames=new Button(wFileComp, SWT.CHECK);
         wAccFilenames.setToolTipText(Messages.getString("TextFileInputDialog.AcceptFilenames.Tooltip"));
         props.setLook(wAccFilenames);
         fdAccFilenames=new FormData();
         fdAccFilenames.left = new FormAttachment(middle, 0);
         fdAccFilenames.right= new FormAttachment(100, 0);
         fdAccFilenames.top  = new FormAttachment(0, 0);
         wAccFilenames.setLayoutData(fdAccFilenames);
         wAccFilenames.addSelectionListener(new SelectionAdapter()
             {
                 public void widgetSelected(SelectionEvent arg0)
                 {
                     setFlags();
                 }
             }
         );
         
         // Which field?
         wlAccField=new Label(wFileComp, SWT.RIGHT);
         wlAccField.setText(Messages.getString("TextFileInputDialog.AcceptField.Label"));
         props.setLook(wlAccField);
         fdlAccField=new FormData();
         fdlAccField.left = new FormAttachment(0, 0);
         fdlAccField.top  = new FormAttachment(wAccFilenames, margin);
         fdlAccField.right= new FormAttachment(middle, -margin);
         wlAccField.setLayoutData(fdlAccField);
         wAccField=new Text(wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         wAccField.setToolTipText(Messages.getString("TextFileInputDialog.AcceptField.Tooltip"));
         props.setLook(wAccField);
         fdAccField=new FormData();
         fdAccField.left = new FormAttachment(middle, 0);
         fdAccField.right= new FormAttachment(100, 0);
         fdAccField.top  = new FormAttachment(wAccFilenames, margin);
         wAccField.setLayoutData(fdAccField);
         
         // Which step to read from?
         wlAccStep=new Label(wFileComp, SWT.RIGHT);
         wlAccStep.setText(Messages.getString("TextFileInputDialog.AcceptStep.Label"));
         props.setLook(wlAccStep);
         fdlAccStep=new FormData();
         fdlAccStep.left = new FormAttachment(0, 0);
         fdlAccStep.top  = new FormAttachment(wAccField, margin);
         fdlAccStep.right= new FormAttachment(middle, -margin);
         wlAccStep.setLayoutData(fdlAccStep);
         wAccStep=new CCombo(wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         wAccStep.setToolTipText(Messages.getString("TextFileInputDialog.AcceptStep.Tooltip"));
         props.setLook(wAccStep);
         fdAccStep=new FormData();
         fdAccStep.left = new FormAttachment(middle, 0);
         fdAccStep.right= new FormAttachment(100, 0);
         fdAccStep.top  = new FormAttachment(wAccField, margin);
         wAccStep.setLayoutData(fdAccStep);
         
         // Fill in the source steps...
         StepMeta[] prevSteps = transMeta.getPrevSteps(transMeta.findStep(stepname));
         for (int i=0;i<prevSteps.length;i++)
         {
             wAccStep.add(prevSteps[i].getName());
         }
 
         // Filename line
         wlFilename=new Label(wFileComp, SWT.RIGHT);
         wlFilename.setText(Messages.getString("TextFileInputDialog.Filename.Label"));
         props.setLook(wlFilename);
         fdlFilename=new FormData();
         fdlFilename.left = new FormAttachment(0, 0);
         fdlFilename.top  = new FormAttachment(wAccStep, margin*3);
         fdlFilename.right= new FormAttachment(middle, -margin);
         wlFilename.setLayoutData(fdlFilename);
 
         wbbFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
         props.setLook(wbbFilename);
         wbbFilename.setText(Messages.getString("System.Button.Browse"));
         wbbFilename.setToolTipText(Messages.getString("System.Tooltip.BrowseForFileOrDirAndAdd"));
         fdbFilename=new FormData();
         fdbFilename.right= new FormAttachment(100, 0);
         fdbFilename.top  = new FormAttachment(wAccStep,margin*3);
         wbbFilename.setLayoutData(fdbFilename);
 
         wbvFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
         props.setLook(wbvFilename);
         wbvFilename.setText(Messages.getString("System.Button.Variable"));
         wbvFilename.setToolTipText(Messages.getString("System.Tooltip.VariableToFileOrDir"));
         fdbvFilename=new FormData();
         fdbvFilename.right= new FormAttachment(wbbFilename, -margin);
         fdbvFilename.top  = new FormAttachment(wAccStep,margin*3);
         wbvFilename.setLayoutData(fdbvFilename);
 
         wbaFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
         props.setLook(wbaFilename);
         wbaFilename.setText(Messages.getString("TextFileInputDialog.FilenameAdd.Button"));
         wbaFilename.setToolTipText(Messages.getString("TextFileInputDialog.FilenameAdd.Tooltip"));
         fdbaFilename=new FormData();
         fdbaFilename.right= new FormAttachment(wbvFilename, -margin);
         fdbaFilename.top  = new FormAttachment(wAccStep,margin*3);
         wbaFilename.setLayoutData(fdbaFilename);
 
         wFilename=new Text(wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         props.setLook(wFilename);
         wFilename.addModifyListener(lsMod);
         fdFilename=new FormData();
         fdFilename.left = new FormAttachment(middle, 0);
         fdFilename.right= new FormAttachment(wbaFilename, -margin);
         fdFilename.top  = new FormAttachment(wAccStep,margin*3);
         wFilename.setLayoutData(fdFilename);
 
         wlFilemask=new Label(wFileComp, SWT.RIGHT);
         wlFilemask.setText(Messages.getString("TextFileInputDialog.Filemask.Label"));
         props.setLook(wlFilemask);
         fdlFilemask=new FormData();
         fdlFilemask.left = new FormAttachment(0, 0);
         fdlFilemask.top  = new FormAttachment(wFilename, margin);
         fdlFilemask.right= new FormAttachment(middle, -margin);
         wlFilemask.setLayoutData(fdlFilemask);
         wFilemask=new Text(wFileComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         props.setLook(wFilemask);
         wFilemask.addModifyListener(lsMod);
         fdFilemask=new FormData();
         fdFilemask.left = new FormAttachment(middle, 0);
         fdFilemask.top  = new FormAttachment(wFilename, margin);
         fdFilemask.right= new FormAttachment(100, 0);
         wFilemask.setLayoutData(fdFilemask);
 
         // Whenever something changes, set the tooltip to the expanded version of the filename:
         wFilename.addModifyListener(getModifyListenerTooltipText(wFilename));
         
         // Listen to the Variable... button
         wbvFilename.addSelectionListener(VariableButtonListenerFactory.getSelectionAdapter(shell, wFilename));
 
         
         // Filename list line
         wlFilenameList=new Label(wFileComp, SWT.RIGHT);
         wlFilenameList.setText(Messages.getString("TextFileInputDialog.FilenameList.Label"));
         props.setLook(wlFilenameList);
         fdlFilenameList=new FormData();
         fdlFilenameList.left = new FormAttachment(0, 0);
         fdlFilenameList.top  = new FormAttachment(wFilemask, margin);
         fdlFilenameList.right= new FormAttachment(middle, -margin);
         wlFilenameList.setLayoutData(fdlFilenameList);
 
         // Buttons to the right of the screen...
         wbdFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
         props.setLook(wbdFilename);
         wbdFilename.setText(Messages.getString("TextFileInputDialog.FilenameDelete.Button"));
         wbdFilename.setToolTipText(Messages.getString("TextFileInputDialog.FilenameDelete.Tooltip"));
         fdbdFilename=new FormData();
         fdbdFilename.right = new FormAttachment(100, 0);
         fdbdFilename.top  = new FormAttachment (wFilemask, 40);
         wbdFilename.setLayoutData(fdbdFilename);
 
         wbeFilename=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
         props.setLook(wbeFilename);
         wbeFilename.setText(Messages.getString("TextFileInputDialog.FilenameEdit.Button"));
         wbeFilename.setToolTipText(Messages.getString("TextFileInputDialog.FilenameEdit.Tooltip"));
         fdbeFilename=new FormData();
         fdbeFilename.right = new FormAttachment(100, 0);
         fdbeFilename.top  = new FormAttachment (wbdFilename, margin);
         wbeFilename.setLayoutData(fdbeFilename);
 
         wbShowFiles=new Button(wFileComp, SWT.PUSH| SWT.CENTER);
         props.setLook(wbShowFiles);
         wbShowFiles.setText(Messages.getString("TextFileInputDialog.ShowFiles.Button"));
         fdbShowFiles=new FormData();
         fdbShowFiles.left   = new FormAttachment(middle, 0);
         fdbShowFiles.bottom = new FormAttachment(100, 0);
         wbShowFiles.setLayoutData(fdbShowFiles);
 
         wFirst=new Button(wFileComp, SWT.PUSH);
         wFirst.setText(Messages.getString("TextFileInputDialog.First.Button"));
         fdFirst=new FormData();
         fdFirst.left=new FormAttachment(wbShowFiles, margin*2);
         fdFirst.bottom =new FormAttachment(100, 0);
         wFirst.setLayoutData(fdFirst);
 
         wFirstHeader=new Button(wFileComp, SWT.PUSH);
         wFirstHeader.setText(Messages.getString("TextFileInputDialog.FirstHeader.Button"));
         fdFirstHeader=new FormData();
         fdFirstHeader.left=new FormAttachment(wFirst, margin*2);
         fdFirstHeader.bottom =new FormAttachment(100, 0);
         wFirstHeader.setLayoutData(fdFirstHeader);
 
         
         ColumnInfo[] colinfo=new ColumnInfo[3];
         colinfo[ 0]=new ColumnInfo(Messages.getString("TextFileInputDialog.FileDirColumn.Column"),  ColumnInfo.COLUMN_TYPE_TEXT,    false);
         colinfo[ 1]=new ColumnInfo(Messages.getString("TextFileInputDialog.WildcardColumn.Column"),        ColumnInfo.COLUMN_TYPE_TEXT,    false );
         colinfo[ 1].setToolTip(Messages.getString("TextFileInputDialog.RegExpColumn.Column"));
         colinfo[ 2]=new ColumnInfo(Messages.getString("TextFileInputDialog.RequiredColumn.Column"),        ColumnInfo.COLUMN_TYPE_CCOMBO,  YES_NO_COMBO );
         colinfo[ 2].setToolTip(Messages.getString("TextFileInputDialog.RequiredColumn.Tooltip"));
         
         wFilenameList = new TableView(wFileComp, 
                               SWT.FULL_SELECTION | SWT.SINGLE | SWT.BORDER, 
                               colinfo, 
                               3,  
                               lsMod,
                               props
                               );
         props.setLook(wFilenameList);
         fdFilenameList=new FormData();
         fdFilenameList.left   = new FormAttachment(middle, 0);
         fdFilenameList.right  = new FormAttachment(wbdFilename, -margin);
         fdFilenameList.top    = new FormAttachment(wFilemask, margin);
         fdFilenameList.bottom = new FormAttachment(wbShowFiles, -margin);
         wFilenameList.setLayoutData(fdFilenameList);
 
     
         fdFileComp=new FormData();
         fdFileComp.left  = new FormAttachment(0, 0);
         fdFileComp.top   = new FormAttachment(0, 0);
         fdFileComp.right = new FormAttachment(100, 0);
         fdFileComp.bottom= new FormAttachment(100, 0);
         wFileComp.setLayoutData(fdFileComp);
     
         wFileComp.pack();
         Rectangle bounds = wFileComp.getBounds();
         
         wFileSComp.setContent(wFileComp);
         wFileSComp.setExpandHorizontal(true);
         wFileSComp.setExpandVertical(true);
         wFileSComp.setMinWidth(bounds.width);
         wFileSComp.setMinHeight(bounds.height);
         
         wFileTab.setControl(wFileSComp);
         
         /////////////////////////////////////////////////////////////
         /// END OF FILE TAB
         /////////////////////////////////////////////////////////////
     }
 
     private void addContentTab()
     {
         //////////////////////////
         // START OF CONTENT TAB///
         ///
         wContentTab=new CTabItem(wTabFolder, SWT.NONE);
         wContentTab.setText(Messages.getString("TextFileInputDialog.ContentTab.TabTitle"));
 
         FormLayout contentLayout = new FormLayout ();
         contentLayout.marginWidth  = 3;
         contentLayout.marginHeight = 3;
         
         wContentSComp = new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
         wContentSComp.setLayout(new FillLayout());
 
         wContentComp = new Composite(wContentSComp, SWT.NONE );
         props.setLook(wContentComp);
         wContentComp.setLayout(contentLayout);
 
         // Filetype line
         wlFiletype=new Label(wContentComp, SWT.RIGHT);
         wlFiletype.setText(Messages.getString("TextFileInputDialog.Filetype.Label"));
         props.setLook(wlFiletype);
         fdlFiletype=new FormData();
         fdlFiletype.left = new FormAttachment(0, 0);
         fdlFiletype.top  = new FormAttachment(0, 0);
         fdlFiletype.right= new FormAttachment(middle, -margin);
         wlFiletype.setLayoutData(fdlFiletype);
         wFiletype=new CCombo(wContentComp, SWT.BORDER | SWT.READ_ONLY);
         wFiletype.setText(Messages.getString("TextFileInputDialog.Filetype.Label"));
         props.setLook(wFiletype);
         wFiletype.add("CSV");
         wFiletype.add("Fixed");
         wFiletype.select(0);
         wFiletype.addModifyListener(lsMod);
         fdFiletype=new FormData();
         fdFiletype.left = new FormAttachment(middle, 0);
         fdFiletype.top  = new FormAttachment(0, 0);
         fdFiletype.right= new FormAttachment(100, 0);
         wFiletype.setLayoutData(fdFiletype);
 
         wlSeparator=new Label(wContentComp, SWT.RIGHT);
         wlSeparator.setText(Messages.getString("TextFileInputDialog.Separator.Label"));
         props.setLook(wlSeparator);
         fdlSeparator=new FormData();
         fdlSeparator.left = new FormAttachment(0, 0);
         fdlSeparator.top  = new FormAttachment(wFiletype, margin);
         fdlSeparator.right= new FormAttachment(middle, -margin);
         wlSeparator.setLayoutData(fdlSeparator);
 
         wbSeparator=new Button(wContentComp, SWT.PUSH| SWT.CENTER);
         props.setLook(wbSeparator);
         wbSeparator.setText(Messages.getString("TextFileInputDialog.Separator.Button"));
         fdbSeparator=new FormData();
         fdbSeparator.right= new FormAttachment(100, 0);
         fdbSeparator.top  = new FormAttachment(wFiletype, 0);
         wbSeparator.setLayoutData(fdbSeparator);
 
         wSeparator=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         props.setLook(wSeparator);
         wSeparator.addModifyListener(lsMod);
         fdSeparator=new FormData();
         fdSeparator.left = new FormAttachment(middle, 0);
         fdSeparator.top  = new FormAttachment(wFiletype, margin);
         fdSeparator.right= new FormAttachment(wbSeparator, -margin);
         wSeparator.setLayoutData(fdSeparator);
 
         // Enclosure
         wlEnclosure=new Label(wContentComp, SWT.RIGHT);
         wlEnclosure.setText(Messages.getString("TextFileInputDialog.Enclosure.Label"));
         props.setLook(wlEnclosure);
         fdlEnclosure=new FormData();
         fdlEnclosure.left = new FormAttachment(0, 0);
         fdlEnclosure.top  = new FormAttachment(wSeparator, margin);
         fdlEnclosure.right= new FormAttachment(middle, -margin);
         wlEnclosure.setLayoutData(fdlEnclosure);
         wEnclosure=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         props.setLook(wEnclosure);
         wEnclosure.addModifyListener(lsMod);
         fdEnclosure=new FormData();
         fdEnclosure.left = new FormAttachment(middle, 0);
         fdEnclosure.top  = new FormAttachment(wSeparator, margin);
         fdEnclosure.right= new FormAttachment(100, 0);
         wEnclosure.setLayoutData(fdEnclosure);
 
         // Allow Enclosure breaks checkbox
         wlEnclBreaks=new Label(wContentComp, SWT.RIGHT);
         wlEnclBreaks.setText(Messages.getString("TextFileInputDialog.EnclBreaks.Label"));
         props.setLook(wlEnclBreaks);
         fdlEnclBreaks=new FormData();
         fdlEnclBreaks.left = new FormAttachment(0, 0);
         fdlEnclBreaks.top  = new FormAttachment(wEnclosure, margin);
         fdlEnclBreaks.right= new FormAttachment(middle, -margin);
         wlEnclBreaks.setLayoutData(fdlEnclBreaks);
         wEnclBreaks=new Button(wContentComp, SWT.CHECK);
         props.setLook(wEnclBreaks);
         fdEnclBreaks=new FormData();
         fdEnclBreaks.left = new FormAttachment(middle, 0);
         fdEnclBreaks.top  = new FormAttachment(wEnclosure, margin);
         wEnclBreaks.setLayoutData(fdEnclBreaks);
         
         // Disable until the logic works...
         wlEnclBreaks.setEnabled(false);
         wEnclBreaks.setEnabled(false);
 
         // Escape
         wlEscape=new Label(wContentComp, SWT.RIGHT);
         wlEscape.setText(Messages.getString("TextFileInputDialog.Escape.Label"));
         props.setLook(wlEscape);
         fdlEscape=new FormData();
         fdlEscape.left = new FormAttachment(0, 0);
         fdlEscape.top  = new FormAttachment(wlEnclBreaks, margin);
         fdlEscape.right= new FormAttachment(middle, -margin);
         wlEscape.setLayoutData(fdlEscape);
         wEscape=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         props.setLook(wEscape);
         wEscape.addModifyListener(lsMod);
         fdEscape=new FormData();
         fdEscape.left = new FormAttachment(middle, 0);
         fdEscape.top  = new FormAttachment(wlEnclBreaks, margin);
         fdEscape.right= new FormAttachment(100, 0);
         wEscape.setLayoutData(fdEscape);
 
         // Header checkbox
         wlHeader=new Label(wContentComp, SWT.RIGHT);
         wlHeader.setText(Messages.getString("TextFileInputDialog.Header.Label"));
         props.setLook(wlHeader);
         fdlHeader=new FormData();
         fdlHeader.left = new FormAttachment(0, 0);
         fdlHeader.top  = new FormAttachment(wEscape, margin);
         fdlHeader.right= new FormAttachment(middle, -margin);
         wlHeader.setLayoutData(fdlHeader);
         wHeader=new Button(wContentComp, SWT.CHECK);
         props.setLook(wHeader);
         fdHeader=new FormData();
         fdHeader.left = new FormAttachment(middle, 0);
         fdHeader.top  = new FormAttachment(wEscape, margin);
         wHeader.setLayoutData(fdHeader);
 
         // NrHeader
         wlNrHeader=new Label(wContentComp, SWT.RIGHT);
         wlNrHeader.setText(Messages.getString("TextFileInputDialog.NrHeader.Label"));
         props.setLook(wlNrHeader);
         fdlNrHeader=new FormData();
         fdlNrHeader.left = new FormAttachment(wHeader, margin);
         fdlNrHeader.top  = new FormAttachment(wEscape, margin);
         wlNrHeader.setLayoutData(fdlNrHeader);
         wNrHeader=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         wNrHeader.setTextLimit(3);
         props.setLook(wNrHeader);
         wNrHeader.addModifyListener(lsMod);
         fdNrHeader=new FormData();
         fdNrHeader.left = new FormAttachment(wlNrHeader, margin);
         fdNrHeader.top  = new FormAttachment(wEscape, margin);
         fdNrHeader.right  = new FormAttachment(100, 0);
         wNrHeader.setLayoutData(fdNrHeader);
         
         wlFooter=new Label(wContentComp, SWT.RIGHT);
         wlFooter.setText(Messages.getString("TextFileInputDialog.Footer.Label"));
         props.setLook(wlFooter);
         fdlFooter=new FormData();
         fdlFooter.left = new FormAttachment(0, 0);
         fdlFooter.top  = new FormAttachment(wHeader, margin);
         fdlFooter.right= new FormAttachment(middle, -margin);
         wlFooter.setLayoutData(fdlFooter);
         wFooter=new Button(wContentComp, SWT.CHECK);
         props.setLook(wFooter);
         fdFooter=new FormData();
         fdFooter.left = new FormAttachment(middle, 0);
         fdFooter.top  = new FormAttachment(wHeader, margin);
         wFooter.setLayoutData(fdFooter);
 
         // NrFooter
         wlNrFooter=new Label(wContentComp, SWT.RIGHT);
         wlNrFooter.setText(Messages.getString("TextFileInputDialog.NrFooter.Label"));
         props.setLook(wlNrFooter);
         fdlNrFooter=new FormData();
         fdlNrFooter.left = new FormAttachment(wFooter, margin);
         fdlNrFooter.top  = new FormAttachment(wHeader, margin);
         wlNrFooter.setLayoutData(fdlNrFooter);
         wNrFooter=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         wNrFooter.setTextLimit(3);
         props.setLook(wNrFooter);
         wNrFooter.addModifyListener(lsMod);
         fdNrFooter=new FormData();
         fdNrFooter.left = new FormAttachment(wlNrFooter, margin);
         fdNrFooter.top  = new FormAttachment(wHeader, margin);
         fdNrFooter.right  = new FormAttachment(100, 0);
         wNrFooter.setLayoutData(fdNrFooter);
 
         // Wraps
         wlWraps=new Label(wContentComp, SWT.RIGHT);
         wlWraps.setText(Messages.getString("TextFileInputDialog.Wraps.Label"));
         props.setLook(wlWraps);
         fdlWraps=new FormData();
         fdlWraps.left = new FormAttachment(0, 0);
         fdlWraps.top  = new FormAttachment(wFooter, margin);
         fdlWraps.right= new FormAttachment(middle, -margin);
         wlWraps.setLayoutData(fdlWraps);
         wWraps=new Button(wContentComp, SWT.CHECK);
         props.setLook(wWraps);
         fdWraps=new FormData();
         fdWraps.left = new FormAttachment(middle, 0);
         fdWraps.top  = new FormAttachment(wFooter, margin);
         wWraps.setLayoutData(fdWraps);
 
         // NrWraps
         wlNrWraps=new Label(wContentComp, SWT.RIGHT);
         wlNrWraps.setText(Messages.getString("TextFileInputDialog.NrWraps.Label"));
         props.setLook(wlNrWraps);
         fdlNrWraps=new FormData();
         fdlNrWraps.left = new FormAttachment(wWraps, margin);
         fdlNrWraps.top  = new FormAttachment(wFooter, margin);
         wlNrWraps.setLayoutData(fdlNrWraps);
         wNrWraps=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         wNrWraps.setTextLimit(3);
         props.setLook(wNrWraps);
         wNrWraps.addModifyListener(lsMod);
         fdNrWraps=new FormData();
         fdNrWraps.left = new FormAttachment(wlNrWraps, margin);
         fdNrWraps.top  = new FormAttachment(wFooter, margin);
         fdNrWraps.right  = new FormAttachment(100, 0);
         wNrWraps.setLayoutData(fdNrWraps);
 
         // Pages
         wlLayoutPaged=new Label(wContentComp, SWT.RIGHT);
         wlLayoutPaged.setText(Messages.getString("TextFileInputDialog.LayoutPaged.Label"));
         props.setLook(wlLayoutPaged);
         fdlLayoutPaged=new FormData();
         fdlLayoutPaged.left = new FormAttachment(0, 0);
         fdlLayoutPaged.top  = new FormAttachment(wWraps, margin);
         fdlLayoutPaged.right= new FormAttachment(middle, -margin);
         wlLayoutPaged.setLayoutData(fdlLayoutPaged);
         wLayoutPaged=new Button(wContentComp, SWT.CHECK);
         props.setLook(wLayoutPaged);
         fdLayoutPaged=new FormData();
         fdLayoutPaged.left = new FormAttachment(middle, 0);
         fdLayoutPaged.top  = new FormAttachment(wWraps, margin);
         wLayoutPaged.setLayoutData(fdLayoutPaged);
 
         // Nr of lines per page
         wlNrLinesPerPage=new Label(wContentComp, SWT.RIGHT);
         wlNrLinesPerPage.setText(Messages.getString("TextFileInputDialog.NrLinesPerPage.Label"));
         props.setLook(wlNrLinesPerPage);
         fdlNrLinesPerPage=new FormData();
         fdlNrLinesPerPage.left = new FormAttachment(wLayoutPaged, margin);
         fdlNrLinesPerPage.top  = new FormAttachment(wWraps, margin);
         wlNrLinesPerPage.setLayoutData(fdlNrLinesPerPage);
         wNrLinesPerPage=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         wNrLinesPerPage.setTextLimit(3);
         props.setLook(wNrLinesPerPage);
         wNrLinesPerPage.addModifyListener(lsMod);
         fdNrLinesPerPage=new FormData();
         fdNrLinesPerPage.left = new FormAttachment(wlNrLinesPerPage, margin);
         fdNrLinesPerPage.top  = new FormAttachment(wWraps, margin);
         fdNrLinesPerPage.right  = new FormAttachment(100, 0);
         wNrLinesPerPage.setLayoutData(fdNrLinesPerPage);
 
         // NrPages
         wlNrLinesDocHeader=new Label(wContentComp, SWT.RIGHT);
         wlNrLinesDocHeader.setText(Messages.getString("TextFileInputDialog.NrLinesDocHeader.Label"));
         props.setLook(wlNrLinesDocHeader);
         fdlNrLinesDocHeader=new FormData();
         fdlNrLinesDocHeader.left = new FormAttachment(wLayoutPaged, margin);
         fdlNrLinesDocHeader.top  = new FormAttachment(wNrLinesPerPage, margin);
         wlNrLinesDocHeader.setLayoutData(fdlNrLinesDocHeader);
         wNrLinesDocHeader=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         wNrLinesDocHeader.setTextLimit(3);
         props.setLook(wNrLinesDocHeader);
         wNrLinesDocHeader.addModifyListener(lsMod);
         fdNrLinesDocHeader=new FormData();
         
         fdNrLinesDocHeader.left = new FormAttachment(wlNrLinesPerPage, margin);
         fdNrLinesDocHeader.top  = new FormAttachment(wNrLinesPerPage, margin);
         fdNrLinesDocHeader.right = new FormAttachment(100, 0);
         wNrLinesDocHeader.setLayoutData(fdNrLinesDocHeader);
 
         // Zipped?
         wlZipped=new Label(wContentComp, SWT.RIGHT);
         wlZipped.setText(Messages.getString("TextFileInputDialog.Zipped.Label"));
         props.setLook(wlZipped);
         fdlZipped=new FormData();
         fdlZipped.left = new FormAttachment(0, 0);
         fdlZipped.top  = new FormAttachment(wNrLinesDocHeader, margin);
         fdlZipped.right= new FormAttachment(middle, -margin);
         wlZipped.setLayoutData(fdlZipped);
         wZipped=new Button(wContentComp, SWT.CHECK );
         props.setLook(wZipped);
         wZipped.setToolTipText(Messages.getString("TextFileInputDialog.Zipped.Tooltip"));
         fdZipped=new FormData();
         fdZipped.left = new FormAttachment(middle, 0);
         fdZipped.top  = new FormAttachment(wNrLinesDocHeader, margin);
         fdZipped.right= new FormAttachment(100, 0);
         wZipped.setLayoutData(fdZipped);
 
         wlNoempty=new Label(wContentComp, SWT.RIGHT);
         wlNoempty.setText(Messages.getString("TextFileInputDialog.NoEmpty.Label"));
         props.setLook(wlNoempty);
         fdlNoempty=new FormData();
         fdlNoempty.left = new FormAttachment(0, 0);
         fdlNoempty.top  = new FormAttachment(wZipped, margin);
         fdlNoempty.right= new FormAttachment(middle, -margin);
         wlNoempty.setLayoutData(fdlNoempty);
         wNoempty=new Button(wContentComp, SWT.CHECK );
         props.setLook(wNoempty);
         wNoempty.setToolTipText(Messages.getString("TextFileInputDialog.NoEmpty.Tooltip"));
         fdNoempty=new FormData();
         fdNoempty.left = new FormAttachment(middle, 0);
         fdNoempty.top  = new FormAttachment(wZipped, margin);
         fdNoempty.right= new FormAttachment(100, 0);
         wNoempty.setLayoutData(fdNoempty);
 
         wlInclFilename=new Label(wContentComp, SWT.RIGHT);
         wlInclFilename.setText(Messages.getString("TextFileInputDialog.InclFilename.Label"));
         props.setLook(wlInclFilename);
         fdlInclFilename=new FormData();
         fdlInclFilename.left = new FormAttachment(0, 0);
         fdlInclFilename.top  = new FormAttachment(wNoempty, margin);
         fdlInclFilename.right= new FormAttachment(middle, -margin);
         wlInclFilename.setLayoutData(fdlInclFilename);
         wInclFilename=new Button(wContentComp, SWT.CHECK );
         props.setLook(wInclFilename);
         wInclFilename.setToolTipText(Messages.getString("TextFileInputDialog.InclFilename.Tooltip"));
         fdInclFilename=new FormData();
         fdInclFilename.left = new FormAttachment(middle, 0);
         fdInclFilename.top  = new FormAttachment(wNoempty, margin);
         wInclFilename.setLayoutData(fdInclFilename);
 
         wlInclFilenameField=new Label(wContentComp, SWT.LEFT);
         wlInclFilenameField.setText(Messages.getString("TextFileInputDialog.InclFilenameField.Label"));
         props.setLook(wlInclFilenameField);
         fdlInclFilenameField=new FormData();
         fdlInclFilenameField.left = new FormAttachment(wInclFilename, margin);
         fdlInclFilenameField.top  = new FormAttachment(wNoempty, margin);
         wlInclFilenameField.setLayoutData(fdlInclFilenameField);
         wInclFilenameField=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         props.setLook(wInclFilenameField);
         wInclFilenameField.addModifyListener(lsMod);
         fdInclFilenameField=new FormData();
         fdInclFilenameField.left = new FormAttachment(wlInclFilenameField, margin);
         fdInclFilenameField.top  = new FormAttachment(wNoempty, margin);
         fdInclFilenameField.right= new FormAttachment(100, 0);
         wInclFilenameField.setLayoutData(fdInclFilenameField);
 
         wlInclRownum=new Label(wContentComp, SWT.RIGHT);
         wlInclRownum.setText(Messages.getString("TextFileInputDialog.InclRownum.Label"));
         props.setLook(wlInclRownum);
         fdlInclRownum=new FormData();
         fdlInclRownum.left = new FormAttachment(0, 0);
         fdlInclRownum.top  = new FormAttachment(wInclFilenameField, margin);
         fdlInclRownum.right= new FormAttachment(middle, -margin);
         wlInclRownum.setLayoutData(fdlInclRownum);
         wInclRownum=new Button(wContentComp, SWT.CHECK );
         props.setLook(wInclRownum);
         wInclRownum.setToolTipText(Messages.getString("TextFileInputDialog.InclRownum.Tooltip"));
         fdRownum=new FormData();
         fdRownum.left = new FormAttachment(middle, 0);
         fdRownum.top  = new FormAttachment(wInclFilenameField, margin);
         wInclRownum.setLayoutData(fdRownum);
 
         wlInclRownumField=new Label(wContentComp, SWT.RIGHT);
         wlInclRownumField.setText(Messages.getString("TextFileInputDialog.InclRownumField.Label"));
         props.setLook(wlInclRownumField);
         fdlInclRownumField=new FormData();
         fdlInclRownumField.left = new FormAttachment(wInclRownum, margin);
         fdlInclRownumField.top  = new FormAttachment(wInclFilenameField, margin);
         wlInclRownumField.setLayoutData(fdlInclRownumField);
         wInclRownumField=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         props.setLook(wInclRownumField);
         wInclRownumField.addModifyListener(lsMod);
         fdInclRownumField=new FormData();
         fdInclRownumField.left = new FormAttachment(wlInclRownumField, margin);
         fdInclRownumField.top  = new FormAttachment(wInclFilenameField, margin);
         fdInclRownumField.right= new FormAttachment(100, 0);
         wInclRownumField.setLayoutData(fdInclRownumField);
 
         wlFormat=new Label(wContentComp, SWT.RIGHT);
         wlFormat.setText(Messages.getString("TextFileInputDialog.Format.Label"));
         props.setLook(wlFormat);
         fdlFormat=new FormData();
         fdlFormat.left = new FormAttachment(0, 0);
         fdlFormat.top  = new FormAttachment(wInclRownumField, margin);
         fdlFormat.right= new FormAttachment(middle, -margin);
         wlFormat.setLayoutData(fdlFormat);
         wFormat=new CCombo(wContentComp, SWT.BORDER | SWT.READ_ONLY);
         wFormat.setText(Messages.getString("TextFileInputDialog.Format.Label"));
         props.setLook(wFormat);
         wFormat.add("DOS");
         wFormat.add("Unix");
         wFormat.add("mixed");
         wFormat.select(0);
         wFormat.addModifyListener(lsMod);
         fdFormat=new FormData();
         fdFormat.left = new FormAttachment(middle, 0);
         fdFormat.top  = new FormAttachment(wInclRownumField, margin);
         fdFormat.right= new FormAttachment(100, 0);
         wFormat.setLayoutData(fdFormat);
 
         wlEncoding=new Label(wContentComp, SWT.RIGHT);
         wlEncoding.setText(Messages.getString("TextFileInputDialog.Encoding.Label"));
         props.setLook(wlEncoding);
         fdlEncoding=new FormData();
         fdlEncoding.left = new FormAttachment(0, 0);
         fdlEncoding.top  = new FormAttachment(wFormat, margin);
         fdlEncoding.right= new FormAttachment(middle, -margin);
         wlEncoding.setLayoutData(fdlEncoding);
         wEncoding=new CCombo(wContentComp, SWT.BORDER | SWT.READ_ONLY);
         wEncoding.setEditable(true);
         props.setLook(wEncoding);
         wEncoding.addModifyListener(lsMod);
         fdEncoding=new FormData();
         fdEncoding.left = new FormAttachment(middle, 0);
         fdEncoding.top  = new FormAttachment(wFormat, margin);
         fdEncoding.right= new FormAttachment(100, 0);
         wEncoding.setLayoutData(fdEncoding);
         wEncoding.addFocusListener(new FocusListener()
             {
                 public void focusLost(org.eclipse.swt.events.FocusEvent e)
                 {
                 }
             
                 public void focusGained(org.eclipse.swt.events.FocusEvent e)
                 {
                     Cursor busy = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
                     shell.setCursor(busy);
                     setEncodings();
                     shell.setCursor(null);
                     busy.dispose();
                 }
             }
         );
 
         wlLimit=new Label(wContentComp, SWT.RIGHT);
         wlLimit.setText(Messages.getString("TextFileInputDialog.Limit.Label"));
         props.setLook(wlLimit);
         fdlLimit=new FormData();
         fdlLimit.left = new FormAttachment(0, 0);
         fdlLimit.top  = new FormAttachment(wEncoding, margin);
         fdlLimit.right= new FormAttachment(middle, -margin);
         wlLimit.setLayoutData(fdlLimit);
         wLimit=new Text(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         props.setLook(wLimit);
         wLimit.addModifyListener(lsMod);
         fdLimit=new FormData();
         fdLimit.left = new FormAttachment(middle, 0);
         fdLimit.top  = new FormAttachment(wEncoding, margin);
         fdLimit.right= new FormAttachment(100, 0);
         wLimit.setLayoutData(fdLimit);
 
         // Date Lenient checkbox
         wlDateLenient=new Label(wContentComp, SWT.RIGHT);
         wlDateLenient.setText(Messages.getString("TextFileInputDialog.DateLenient.Label"));
         props.setLook(wlDateLenient);
         fdlDateLenient=new FormData();
         fdlDateLenient.left = new FormAttachment(0, 0);
         fdlDateLenient.top  = new FormAttachment(wLimit, margin);
         fdlDateLenient.right= new FormAttachment(middle, -margin);
         wlDateLenient.setLayoutData(fdlDateLenient);
         wDateLenient=new Button(wContentComp, SWT.CHECK);
         wDateLenient.setToolTipText(Messages.getString("TextFileInputDialog.DateLenient.Tooltip"));
         props.setLook(wDateLenient);
         fdDateLenient=new FormData();
         fdDateLenient.left = new FormAttachment(middle, 0);
         fdDateLenient.top  = new FormAttachment(wLimit, margin);
         wDateLenient.setLayoutData(fdDateLenient);
 
         wlDateLocale=new Label(wContentComp, SWT.RIGHT);
         wlDateLocale.setText(Messages.getString("TextFileInputDialog.DateLocale.Label"));
         props.setLook(wlDateLocale);
         fdlDateLocale=new FormData();
         fdlDateLocale.left = new FormAttachment(0, 0);
         fdlDateLocale.top  = new FormAttachment(wDateLenient, margin);
         fdlDateLocale.right= new FormAttachment(middle, -margin);
         wlDateLocale.setLayoutData(fdlDateLocale);
         wDateLocale=new CCombo(wContentComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         wDateLocale.setToolTipText(Messages.getString("TextFileInputDialog.DateLocale.Tooltip"));
         props.setLook(wDateLocale);
         wDateLocale.addModifyListener(lsMod);
         fdDateLocale=new FormData();
         fdDateLocale.left = new FormAttachment(middle, 0);
         fdDateLocale.top  = new FormAttachment(wDateLenient, margin);
         fdDateLocale.right= new FormAttachment(100, 0);
         wDateLocale.setLayoutData(fdDateLocale);
         
         Runnable runnable = new Runnable()
 		{
 			public void run()
 			{
 		        Locale locale[] =  Locale.getAvailableLocales();
 		        for (int i=0;i<locale.length;i++)
 		        {
 		        	if (!wDateLocale.isDisposed()) wDateLocale.add( locale[i].toString());
 		        }
 			}
 		};
         shell.getDisplay().asyncExec(runnable);
         
         wContentComp.pack();
         // What's the size: 
         Rectangle bounds = wContentComp.getBounds();
         
         wContentSComp.setContent(wContentComp);
         wContentSComp.setExpandHorizontal(true);
         wContentSComp.setExpandVertical(true);
         wContentSComp.setMinWidth(bounds.width);
         wContentSComp.setMinHeight(bounds.height);
         
         fdContentComp = new FormData();
         fdContentComp.left  = new FormAttachment(0, 0);
         fdContentComp.top   = new FormAttachment(0, 0);
         fdContentComp.right = new FormAttachment(100, 0);
         fdContentComp.bottom= new FormAttachment(100, 0);
         wContentComp.setLayoutData(fdContentComp);
 
         wContentTab.setControl(wContentSComp);
 
         /////////////////////////////////////////////////////////////
         /// END OF CONTENT TAB
         /////////////////////////////////////////////////////////////
 
     }
 
     private void addErrorTab()
     {
         //////////////////////////
         // START OF ERROR TAB  ///
         ///
         wErrorTab=new CTabItem(wTabFolder, SWT.NONE);
         wErrorTab.setText(Messages.getString("TextFileInputDialog.ErrorTab.TabTitle"));
 
         wErrorSComp = new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
         wErrorSComp.setLayout(new FillLayout());
 
         FormLayout errorLayout = new FormLayout ();
         errorLayout.marginWidth  = 3;
         errorLayout.marginHeight = 3;
 
         wErrorComp = new Composite(wErrorSComp, SWT.NONE);
         props.setLook(wErrorComp);
         wErrorComp.setLayout(errorLayout);
         
         // ERROR HANDLING...
         // ErrorIgnored?
         wlErrorIgnored = new Label(wErrorComp, SWT.RIGHT);
         wlErrorIgnored.setText(Messages.getString("TextFileInputDialog.ErrorIgnored.Label"));
         props.setLook(wlErrorIgnored);
         fdlErrorIgnored = new FormData();
         fdlErrorIgnored.left = new FormAttachment(0, 0);
         fdlErrorIgnored.top = new FormAttachment(0, margin);
         fdlErrorIgnored.right = new FormAttachment(middle, -margin);
         wlErrorIgnored.setLayoutData(fdlErrorIgnored);
         wErrorIgnored = new Button(wErrorComp, SWT.CHECK);
         props.setLook(wErrorIgnored);
         wErrorIgnored.setToolTipText(Messages.getString("TextFileInputDialog.ErrorIgnored.Tooltip"));
         fdErrorIgnored = new FormData();
         fdErrorIgnored.left = new FormAttachment(middle, 0);
         fdErrorIgnored.top = new FormAttachment(0, margin);
         wErrorIgnored.setLayoutData(fdErrorIgnored);
         
         // Skip error lines?
         wlSkipErrorLines = new Label(wErrorComp, SWT.RIGHT);
         wlSkipErrorLines.setText(Messages.getString("TextFileInputDialog.SkipErrorLines.Label"));
         props.setLook(wlSkipErrorLines);
         fdlSkipErrorLines = new FormData();
         fdlSkipErrorLines.left = new FormAttachment(0, 0);
         fdlSkipErrorLines.top = new FormAttachment(wErrorIgnored, margin);
         fdlSkipErrorLines.right = new FormAttachment(middle, -margin);
         wlSkipErrorLines.setLayoutData(fdlSkipErrorLines);
         wSkipErrorLines = new Button(wErrorComp, SWT.CHECK);
         props.setLook(wSkipErrorLines);
         wSkipErrorLines.setToolTipText(Messages.getString("TextFileInputDialog.SkipErrorLines.Tooltip"));
         fdSkipErrorLines = new FormData();
         fdSkipErrorLines.left = new FormAttachment(middle, 0);
         fdSkipErrorLines.top = new FormAttachment(wErrorIgnored, margin);
         wSkipErrorLines.setLayoutData(fdSkipErrorLines);
 
         wlErrorCount=new Label(wErrorComp, SWT.RIGHT);
         wlErrorCount.setText(Messages.getString("TextFileInputDialog.ErrorCount.Label"));
         props.setLook(wlErrorCount);
         fdlErrorCount=new FormData();
         fdlErrorCount.left = new FormAttachment(0, 0);
         fdlErrorCount.top  = new FormAttachment(wSkipErrorLines, margin);
         fdlErrorCount.right= new FormAttachment(middle, -margin);
         wlErrorCount.setLayoutData(fdlErrorCount);
         wErrorCount=new Text(wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         props.setLook(wErrorCount);
         wErrorCount.addModifyListener(lsMod);
         fdErrorCount=new FormData();
         fdErrorCount.left = new FormAttachment(middle, 0);
         fdErrorCount.top  = new FormAttachment(wSkipErrorLines, margin);
         fdErrorCount.right= new FormAttachment(100, 0);
         wErrorCount.setLayoutData(fdErrorCount);
 
         wlErrorFields=new Label(wErrorComp, SWT.RIGHT);
         wlErrorFields.setText(Messages.getString("TextFileInputDialog.ErrorFields.Label"));
         props.setLook(wlErrorFields);
         fdlErrorFields=new FormData();
         fdlErrorFields.left = new FormAttachment(0, 0);
         fdlErrorFields.top  = new FormAttachment(wErrorCount, margin);
         fdlErrorFields.right= new FormAttachment(middle, -margin);
         wlErrorFields.setLayoutData(fdlErrorFields);
         wErrorFields=new Text(wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         props.setLook(wErrorFields);
         wErrorFields.addModifyListener(lsMod);
         fdErrorFields=new FormData();
         fdErrorFields.left = new FormAttachment(middle, 0);
         fdErrorFields.top  = new FormAttachment(wErrorCount, margin);
         fdErrorFields.right= new FormAttachment(100, 0);
         wErrorFields.setLayoutData(fdErrorFields);
 
         wlErrorText=new Label(wErrorComp, SWT.RIGHT);
         wlErrorText.setText(Messages.getString("TextFileInputDialog.ErrorText.Label"));
         props.setLook(wlErrorText);
         fdlErrorText=new FormData();
         fdlErrorText.left = new FormAttachment(0, 0);
         fdlErrorText.top  = new FormAttachment(wErrorFields, margin);
         fdlErrorText.right= new FormAttachment(middle, -margin);
         wlErrorText.setLayoutData(fdlErrorText);
         wErrorText=new Text(wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         props.setLook(wErrorText);
         wErrorText.addModifyListener(lsMod);
         fdErrorText=new FormData();
         fdErrorText.left = new FormAttachment(middle, 0);
         fdErrorText.top  = new FormAttachment(wErrorFields, margin);
         fdErrorText.right= new FormAttachment(100, 0);
         wErrorText.setLayoutData(fdErrorText);
         
         
         
         
         // Bad lines files directory + extention
         Control previous = wErrorText;
         
         // BadDestDir line
         wlWarnDestDir=new Label(wErrorComp, SWT.RIGHT);
         wlWarnDestDir.setText(Messages.getString("TextFileInputDialog.WarnDestDir.Label"));
         props.setLook(wlWarnDestDir);
         fdlWarnDestDir=new FormData();
         fdlWarnDestDir.left = new FormAttachment(0, 0);
         fdlWarnDestDir.top  = new FormAttachment(previous, margin*4);
         fdlWarnDestDir.right= new FormAttachment(middle, -margin);
         wlWarnDestDir.setLayoutData(fdlWarnDestDir);
 
         wbbWarnDestDir=new Button(wErrorComp, SWT.PUSH| SWT.CENTER);
         props.setLook(wbbWarnDestDir);
         wbbWarnDestDir.setText(Messages.getString("System.Button.Browse"));
         wbbWarnDestDir.setToolTipText(Messages.getString("System.Tooltip.BrowseForDir"));
         fdbBadDestDir=new FormData();
         fdbBadDestDir.right= new FormAttachment(100, 0);
         fdbBadDestDir.top  = new FormAttachment(previous, margin*4);
         wbbWarnDestDir.setLayoutData(fdbBadDestDir);
 
         wbvWarnDestDir=new Button(wErrorComp, SWT.PUSH| SWT.CENTER);
         props.setLook(wbvWarnDestDir);
         wbvWarnDestDir.setText(Messages.getString("System.Button.Variable"));
         wbvWarnDestDir.setToolTipText(Messages.getString("System.Tooltip.VariableToDir"));
         fdbvWarnDestDir=new FormData();
         fdbvWarnDestDir.right= new FormAttachment(wbbWarnDestDir, -margin);
         fdbvWarnDestDir.top  = new FormAttachment(previous, margin*4);
         wbvWarnDestDir.setLayoutData(fdbvWarnDestDir);
 
         wWarnExt=new Text(wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         props.setLook(wWarnExt);
         wWarnExt.addModifyListener(lsMod);
         fdWarnDestExt=new FormData();
         fdWarnDestExt.left = new FormAttachment(wbvWarnDestDir, -150);
         fdWarnDestExt.right= new FormAttachment(wbvWarnDestDir, -margin);
         fdWarnDestExt.top  = new FormAttachment(previous, margin*4);
         wWarnExt.setLayoutData(fdWarnDestExt);
 
         wlWarnExt=new Label(wErrorComp, SWT.RIGHT);
         wlWarnExt.setText(Messages.getString("System.Label.Extension"));
         props.setLook(wlWarnExt);
         fdlWarnDestExt=new FormData();
         fdlWarnDestExt.top  = new FormAttachment(previous, margin*4);
         fdlWarnDestExt.right= new FormAttachment(wWarnExt, -margin);
         wlWarnExt.setLayoutData(fdlWarnDestExt);
 
         wWarnDestDir=new Text(wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         props.setLook(wWarnDestDir);
         wWarnDestDir.addModifyListener(lsMod);
         fdBadDestDir=new FormData();
         fdBadDestDir.left = new FormAttachment(middle, 0);
         fdBadDestDir.right= new FormAttachment(wlWarnExt, -margin);
         fdBadDestDir.top  = new FormAttachment(previous, margin*4);
         wWarnDestDir.setLayoutData(fdBadDestDir);
         
         // Listen to the Browse... button
         wbbWarnDestDir.addSelectionListener(DirectoryDialogButtonListenerFactory.getSelectionAdapter(shell, wWarnDestDir));
 
         // Listen to the Variable... button
         wbvWarnDestDir.addSelectionListener(VariableButtonListenerFactory.getSelectionAdapter(shell, wWarnDestDir));        
         
         // Whenever something changes, set the tooltip to the expanded version of the directory:
         wWarnDestDir.addModifyListener(getModifyListenerTooltipText(wWarnDestDir));
         
         
 
 
         
         // Error lines files directory + extention
         previous = wWarnDestDir;
         
         // ErrorDestDir line
         wlErrorDestDir=new Label(wErrorComp, SWT.RIGHT);
         wlErrorDestDir.setText(Messages.getString("TextFileInputDialog.ErrorDestDir.Label"));
         props.setLook(wlErrorDestDir);
         fdlErrorDestDir=new FormData();
         fdlErrorDestDir.left = new FormAttachment(0, 0);
         fdlErrorDestDir.top  = new FormAttachment(previous, margin);
         fdlErrorDestDir.right= new FormAttachment(middle, -margin);
         wlErrorDestDir.setLayoutData(fdlErrorDestDir);
 
         wbbErrorDestDir=new Button(wErrorComp, SWT.PUSH| SWT.CENTER);
         props.setLook(wbbErrorDestDir);
         wbbErrorDestDir.setText(Messages.getString("System.Button.Browse"));
         wbbErrorDestDir.setToolTipText(Messages.getString("System.Tooltip.BrowseForDir"));
         fdbErrorDestDir=new FormData();
         fdbErrorDestDir.right= new FormAttachment(100, 0);
         fdbErrorDestDir.top  = new FormAttachment(previous, margin);
         wbbErrorDestDir.setLayoutData(fdbErrorDestDir);
 
         wbvErrorDestDir=new Button(wErrorComp, SWT.PUSH| SWT.CENTER);
         props.setLook(wbvErrorDestDir);
         wbvErrorDestDir.setText(Messages.getString("System.Button.Variable"));
         wbvErrorDestDir.setToolTipText(Messages.getString("System.Tooltip.VariableToDir"));
         fdbvErrorDestDir=new FormData();
         fdbvErrorDestDir.right= new FormAttachment(wbbErrorDestDir, -margin);
         fdbvErrorDestDir.top  = new FormAttachment(previous, margin);
         wbvErrorDestDir.setLayoutData(fdbvErrorDestDir);
 
         wErrorExt=new Text(wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         props.setLook(wErrorExt);
         wErrorExt.addModifyListener(lsMod);
         fdErrorDestExt=new FormData();
         fdErrorDestExt.left = new FormAttachment(wbvErrorDestDir, -150);
         fdErrorDestExt.right= new FormAttachment(wbvErrorDestDir, -margin);
         fdErrorDestExt.top  = new FormAttachment(previous, margin);
         wErrorExt.setLayoutData(fdErrorDestExt);
 
         wlErrorExt=new Label(wErrorComp, SWT.RIGHT);
         wlErrorExt.setText(Messages.getString("System.Label.Extension"));
         props.setLook(wlErrorExt);
         fdlErrorDestExt=new FormData();
         fdlErrorDestExt.top  = new FormAttachment(previous, margin);
         fdlErrorDestExt.right= new FormAttachment(wErrorExt, -margin);
         wlErrorExt.setLayoutData(fdlErrorDestExt);
 
         wErrorDestDir=new Text(wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         props.setLook(wErrorDestDir);
         wErrorDestDir.addModifyListener(lsMod);
         fdErrorDestDir=new FormData();
         fdErrorDestDir.left = new FormAttachment(middle, 0);
         fdErrorDestDir.right= new FormAttachment(wlErrorExt, -margin);
         fdErrorDestDir.top  = new FormAttachment(previous, margin);
         wErrorDestDir.setLayoutData(fdErrorDestDir);
         
         // Listen to the Browse... button
         wbbErrorDestDir.addSelectionListener(DirectoryDialogButtonListenerFactory.getSelectionAdapter(shell, wErrorDestDir));
 
         // Listen to the Variable... button
         wbvErrorDestDir.addSelectionListener(VariableButtonListenerFactory.getSelectionAdapter(shell, wErrorDestDir));        
         
         // Whenever something changes, set the tooltip to the expanded version of the directory:
         wErrorDestDir.addModifyListener(getModifyListenerTooltipText(wErrorDestDir));        
         
         // Data Error lines files directory + extention
         previous = wErrorDestDir;
                 
         // LineNrDestDir line
         wlLineNrDestDir=new Label(wErrorComp, SWT.RIGHT);
         wlLineNrDestDir.setText(Messages.getString("TextFileInputDialog.LineNrDestDir.Label"));
         props.setLook(wlLineNrDestDir);
         fdlLineNrDestDir=new FormData();
         fdlLineNrDestDir.left = new FormAttachment(0, 0);
         fdlLineNrDestDir.top  = new FormAttachment(previous, margin);
         fdlLineNrDestDir.right= new FormAttachment(middle, -margin);
         wlLineNrDestDir.setLayoutData(fdlLineNrDestDir);
 
         wbbLineNrDestDir=new Button(wErrorComp, SWT.PUSH| SWT.CENTER);
         props.setLook(wbbLineNrDestDir);
         wbbLineNrDestDir.setText(Messages.getString("System.Button.Browse"));
         wbbLineNrDestDir.setToolTipText(Messages.getString("System.Tooltip.Browse"));
         fdbLineNrDestDir=new FormData();
         fdbLineNrDestDir.right= new FormAttachment(100, 0);
         fdbLineNrDestDir.top  = new FormAttachment(previous, margin);
         wbbLineNrDestDir.setLayoutData(fdbLineNrDestDir);
 
         wbvLineNrDestDir=new Button(wErrorComp, SWT.PUSH| SWT.CENTER);
         props.setLook(wbvLineNrDestDir);
         wbvLineNrDestDir.setText("&Variable");
         wbvLineNrDestDir.setToolTipText("System.Tooltip.VariableToDir");
         fdbvLineNrDestDir=new FormData();
         fdbvLineNrDestDir.right= new FormAttachment(wbbLineNrDestDir, -margin);
         fdbvLineNrDestDir.top  = new FormAttachment(previous, margin);
         wbvLineNrDestDir.setLayoutData(fdbvLineNrDestDir);
 
         wLineNrExt=new Text(wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         props.setLook(wLineNrExt);
         wLineNrExt.addModifyListener(lsMod);
         fdLineNrDestExt=new FormData();
         fdLineNrDestExt.left = new FormAttachment(wbvLineNrDestDir, -150);
         fdLineNrDestExt.right= new FormAttachment(wbvLineNrDestDir, -margin);
         fdLineNrDestExt.top  = new FormAttachment(previous, margin);
         wLineNrExt.setLayoutData(fdLineNrDestExt);
 
         wlLineNrExt=new Label(wErrorComp, SWT.RIGHT);
         wlLineNrExt.setText(Messages.getString("System.Label.Extension"));
         props.setLook(wlLineNrExt);
         fdlLineNrDestExt=new FormData();
         fdlLineNrDestExt.top  = new FormAttachment(previous, margin);
         fdlLineNrDestExt.right= new FormAttachment(wLineNrExt, -margin);
         wlLineNrExt.setLayoutData(fdlLineNrDestExt);
 
         wLineNrDestDir=new Text(wErrorComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
         props.setLook(wLineNrDestDir);
         wLineNrDestDir.addModifyListener(lsMod);
         fdLineNrDestDir=new FormData();
         fdLineNrDestDir.left = new FormAttachment(middle, 0);
         fdLineNrDestDir.right= new FormAttachment(wlLineNrExt, -margin);
         fdLineNrDestDir.top  = new FormAttachment(previous, margin);
         wLineNrDestDir.setLayoutData(fdLineNrDestDir);
         
         // Listen to the Browse... button
         wbbLineNrDestDir.addSelectionListener(DirectoryDialogButtonListenerFactory.getSelectionAdapter(shell, wLineNrDestDir));
 
         // Listen to the Variable... button
         wbvLineNrDestDir.addSelectionListener(VariableButtonListenerFactory.getSelectionAdapter(shell, wLineNrDestDir));        
         
         // Whenever something changes, set the tooltip to the expanded version of the directory:
         wLineNrDestDir.addModifyListener(getModifyListenerTooltipText(wLineNrDestDir));
 
         
         fdErrorComp = new FormData();
         fdErrorComp.left  = new FormAttachment(0, 0);
         fdErrorComp.top   = new FormAttachment(0, 0);
         fdErrorComp.right = new FormAttachment(100, 0);
         fdErrorComp.bottom= new FormAttachment(100, 0);
         wErrorComp.setLayoutData(fdErrorComp);
 
         wErrorComp.pack();
         // What's the size: 
         Rectangle bounds = wErrorComp.getBounds();
         
         wErrorSComp.setContent(wErrorComp);
         wErrorSComp.setExpandHorizontal(true);
         wErrorSComp.setExpandVertical(true);
         wErrorSComp.setMinWidth(bounds.width);
         wErrorSComp.setMinHeight(bounds.height);
 
        
        wErrorTab.setControl(wErrorComp);
 
 
         /////////////////////////////////////////////////////////////
         /// END OF CONTENT TAB
         /////////////////////////////////////////////////////////////
 
     }
 
     private void addFiltersTabs()
     {
         // Filters tab...
         //
         wFilterTab = new CTabItem(wTabFolder, SWT.NONE);
         wFilterTab.setText(Messages.getString("TextFileInputDialog.FilterTab.TabTitle"));
         
         FormLayout FilterLayout = new FormLayout ();
         FilterLayout.marginWidth  = Const.FORM_MARGIN;
         FilterLayout.marginHeight = Const.FORM_MARGIN;
         
         wFilterComp = new Composite(wTabFolder, SWT.NONE );
         wFilterComp.setLayout(FilterLayout);
         props.setLook(wFilterComp);
         
         final int FilterRows=input.getFilter().length;
         
         ColumnInfo[] colinf=new ColumnInfo[]
             {
              new ColumnInfo(Messages.getString("TextFileInputDialog.FilterStringColumn.Column"),      ColumnInfo.COLUMN_TYPE_TEXT,    false),
              new ColumnInfo(Messages.getString("TextFileInputDialog.FilterPositionColumn.Column"),    ColumnInfo.COLUMN_TYPE_TEXT,    false),
              new ColumnInfo(Messages.getString("TextFileInputDialog.StopOnFilterColumn.Column"),     ColumnInfo.COLUMN_TYPE_CCOMBO,  YES_NO_COMBO )
             };
         
         colinf[2].setToolTip(Messages.getString("TextFileInputDialog.StopOnFilterColumn.Tooltip"));
         
         wFilter=new TableView(wFilterComp, 
                               SWT.FULL_SELECTION | SWT.MULTI, 
                               colinf, 
                               FilterRows,  
                               lsMod,
                               props
                               );
 
         fdFilter=new FormData();
         fdFilter.left  = new FormAttachment(0, 0);
         fdFilter.top   = new FormAttachment(0, 0);
         fdFilter.right = new FormAttachment(100, 0);
         fdFilter.bottom= new FormAttachment(100, 0);
         wFilter.setLayoutData(fdFilter);
 
         fdFilterComp=new FormData();
         fdFilterComp.left  = new FormAttachment(0, 0);
         fdFilterComp.top   = new FormAttachment(0, 0);
         fdFilterComp.right = new FormAttachment(100, 0);
         fdFilterComp.bottom= new FormAttachment(100, 0);
         wFilterComp.setLayoutData(fdFilterComp);
         
         wFilterComp.layout();
         wFilterTab.setControl(wFilterComp);
     }
 
     
     private void addFieldsTabs()
     {
         // Fields tab...
         //
         wFieldsTab = new CTabItem(wTabFolder, SWT.NONE);
         wFieldsTab.setText(Messages.getString("TextFileInputDialog.FieldsTab.TabTitle"));
         
         FormLayout fieldsLayout = new FormLayout ();
         fieldsLayout.marginWidth  = Const.FORM_MARGIN;
         fieldsLayout.marginHeight = Const.FORM_MARGIN;
         
         wFieldsComp = new ScrolledComposite(wTabFolder, SWT.V_SCROLL | SWT.H_SCROLL );
         wFieldsComp.setLayout(fieldsLayout);
         props.setLook(wFieldsComp);
         
         wGet=new Button(wFieldsComp, SWT.PUSH);
         wGet.setText(Messages.getString("System.Button.GetFields"));
         fdGet=new FormData();
         fdGet.left=new FormAttachment(50, 0);
         fdGet.bottom =new FormAttachment(100, 0);
         wGet.setLayoutData(fdGet);
 
         final int FieldsRows=input.getInputFields().length;
         
         // Prepare a list of possible formats...
         String formats[] = Const.getConversionFormats();
         
         ColumnInfo[] colinf=new ColumnInfo[]
             {
              new ColumnInfo(Messages.getString("TextFileInputDialog.NameColumn.Column"),       ColumnInfo.COLUMN_TYPE_TEXT,    false),
              new ColumnInfo(Messages.getString("TextFileInputDialog.TypeColumn.Column"),       ColumnInfo.COLUMN_TYPE_CCOMBO,  Value.getTypes(), true ),
              new ColumnInfo(Messages.getString("TextFileInputDialog.FormatColumn.Column"),     ColumnInfo.COLUMN_TYPE_CCOMBO,  formats),
              new ColumnInfo(Messages.getString("TextFileInputDialog.PositionColumn.Column"),   ColumnInfo.COLUMN_TYPE_TEXT,    false),
              new ColumnInfo(Messages.getString("TextFileInputDialog.LengthColumn.Column"),     ColumnInfo.COLUMN_TYPE_TEXT,    false),
              new ColumnInfo(Messages.getString("TextFileInputDialog.PrecisionColumn.Column"),  ColumnInfo.COLUMN_TYPE_TEXT,    false),
              new ColumnInfo(Messages.getString("TextFileInputDialog.CurrencyColumn.Column"),   ColumnInfo.COLUMN_TYPE_TEXT,    false),
              new ColumnInfo(Messages.getString("TextFileInputDialog.DecimalColumn.Column"),    ColumnInfo.COLUMN_TYPE_TEXT,    false),
              new ColumnInfo(Messages.getString("TextFileInputDialog.GroupColumn.Column"),      ColumnInfo.COLUMN_TYPE_TEXT,    false),
              new ColumnInfo(Messages.getString("TextFileInputDialog.NullIfColumn.Column"),    ColumnInfo.COLUMN_TYPE_TEXT,    false),
              new ColumnInfo(Messages.getString("TextFileInputDialog.IfNullColumn.Column"),    ColumnInfo.COLUMN_TYPE_TEXT,    false),
              new ColumnInfo(Messages.getString("TextFileInputDialog.TrimTypeColumn.Column"),  ColumnInfo.COLUMN_TYPE_CCOMBO,  TextFileInputMeta.trimTypeDesc, true ),
              new ColumnInfo(Messages.getString("TextFileInputDialog.RepeatColumn.Column"),     ColumnInfo.COLUMN_TYPE_CCOMBO,  new String[] { Messages.getString("System.Combo.Yes"), Messages.getString("System.Combo.No") }, true )
             };
         
         colinf[12].setToolTip(Messages.getString("TextFileInputDialog.RepeatColumn.Tooltip"));
         
         wFields=new TableView(wFieldsComp, 
                               SWT.FULL_SELECTION | SWT.MULTI, 
                               colinf, 
                               FieldsRows,  
                               lsMod,
                               props
                               );
 
         fdFields=new FormData();
         fdFields.left  = new FormAttachment(0, 0);
         fdFields.top   = new FormAttachment(0, 0);
         fdFields.right = new FormAttachment(100, 0);
         fdFields.bottom= new FormAttachment(wGet, -margin);
         wFields.setLayoutData(fdFields);
 
         fdFieldsComp=new FormData();
         fdFieldsComp.left  = new FormAttachment(0, 0);
         fdFieldsComp.top   = new FormAttachment(0, 0);
         fdFieldsComp.right = new FormAttachment(100, 0);
         fdFieldsComp.bottom= new FormAttachment(100, 0);
         wFieldsComp.setLayoutData(fdFieldsComp);
         
         wFieldsComp.layout();
         wFieldsTab.setControl(wFieldsComp);
     }
     
     public ModifyListener getModifyListenerTooltipText(final Text textField)
     {
         return new ModifyListener()
         {
             public void modifyText(ModifyEvent e)
             {
                 textField.setToolTipText(StringUtil.environmentSubstitute( textField.getText() ) );
             }
         };
     }
 
     public void setFlags()
 	{
     	boolean accept = wAccFilenames.getSelection();
     	wlAccField.setEnabled(accept);
     	wAccField.setEnabled(accept);
     	wlAccStep.setEnabled(accept);
     	wAccStep.setEnabled(accept);
 
     	wlFilename.setEnabled(!accept);
     	wbbFilename.setEnabled(!accept); // Browse: add file or directory
     	wbvFilename.setEnabled(!accept); // Variable
     	wbdFilename.setEnabled(!accept); // Delete
     	wbeFilename.setEnabled(!accept); // Edit
     	wbaFilename.setEnabled(!accept); // Add or change
     	wFilename.setEnabled(!accept);
     	wlFilenameList.setEnabled(!accept);
     	wFilenameList.setEnabled(!accept);
     	wlFilemask.setEnabled(!accept);
     	wFilemask.setEnabled(!accept);
     	wbShowFiles.setEnabled(!accept);
         wFirst.setEnabled(!accept);        
         wFirstHeader.setEnabled(!accept);
         wPreview.setEnabled(!accept);
         
         wlInclFilenameField.setEnabled(wInclFilename.getSelection());
         wInclFilenameField.setEnabled(wInclFilename.getSelection());
 
         wlInclRownumField.setEnabled(wInclRownum.getSelection());
         wInclRownumField.setEnabled(wInclRownum.getSelection());
 
         // Error handling tab...
         wlSkipErrorLines.setEnabled( wErrorIgnored.getSelection() );
         wSkipErrorLines.setEnabled( wErrorIgnored.getSelection() );
         wlErrorCount.setEnabled( wErrorIgnored.getSelection() );
         wErrorCount.setEnabled( wErrorIgnored.getSelection() );
         wlErrorFields.setEnabled( wErrorIgnored.getSelection() );
         wErrorFields.setEnabled( wErrorIgnored.getSelection() );
         wlErrorText.setEnabled( wErrorIgnored.getSelection() );
         wErrorText.setEnabled( wErrorIgnored.getSelection() );
 
         wlWarnDestDir.setEnabled( wErrorIgnored.getSelection() );
         wWarnDestDir.setEnabled( wErrorIgnored.getSelection() );
         wlWarnExt.setEnabled( wErrorIgnored.getSelection() );
         wWarnExt.setEnabled( wErrorIgnored.getSelection() );
         wbbWarnDestDir.setEnabled( wErrorIgnored.getSelection() );
         wbvWarnDestDir.setEnabled( wErrorIgnored.getSelection() );
 
         wlErrorDestDir.setEnabled( wErrorIgnored.getSelection() );
         wErrorDestDir.setEnabled( wErrorIgnored.getSelection() );
         wlErrorExt.setEnabled( wErrorIgnored.getSelection() );
         wErrorExt.setEnabled( wErrorIgnored.getSelection() );
         wbbErrorDestDir.setEnabled( wErrorIgnored.getSelection() );
         wbvErrorDestDir.setEnabled( wErrorIgnored.getSelection() );
          
 
         wlLineNrDestDir.setEnabled( wErrorIgnored.getSelection() );
         wLineNrDestDir.setEnabled( wErrorIgnored.getSelection() );
         wlLineNrExt.setEnabled( wErrorIgnored.getSelection() );
         wLineNrExt.setEnabled( wErrorIgnored.getSelection() );
         wbbLineNrDestDir.setEnabled( wErrorIgnored.getSelection() );
         wbvLineNrDestDir.setEnabled( wErrorIgnored.getSelection() );
        
         wlNrHeader.setEnabled( wHeader.getSelection() );
         wNrHeader.setEnabled( wHeader.getSelection() );
         wlNrFooter.setEnabled( wFooter.getSelection() );
         wNrFooter.setEnabled( wFooter.getSelection() );
         wlNrWraps.setEnabled( wWraps.getSelection() );
         wNrWraps.setEnabled( wWraps.getSelection() );
 
         wlNrLinesPerPage.setEnabled( wLayoutPaged.getSelection() );
         wNrLinesPerPage.setEnabled( wLayoutPaged.getSelection() );
         wlNrLinesDocHeader.setEnabled( wLayoutPaged.getSelection() );
         wNrLinesDocHeader.setEnabled( wLayoutPaged.getSelection() );
     }
 
 	/**
 	 * Read the data from the TextFileInputMeta object and show it in this dialog.
 	 * 
 	 * @param meta The TextFileInputMeta object to obtain the data from.
 	 */
 	public void getData(TextFileInputMeta meta)
 	{
         final TextFileInputMeta in = meta;
         
         wAccFilenames.setSelection(meta.isAcceptingFilenames());
         if (meta.getAcceptingField()!=null) wAccField.setText(meta.getAcceptingField());
         if (meta.getAcceptingStep()!=null) wAccStep.setText(meta.getAcceptingStep().getName());
         
 		if (in.getFileName() !=null) 
 		{
 			wFilenameList.removeAll();
 			for (int i=0;i<in.getFileName().length;i++) 
 			{
 				wFilenameList.add(new String[] { in.getFileName()[i], in.getFileMask()[i], in.getFileRequired()[i] } );
 			}
 			wFilenameList.removeEmptyRows();
 			wFilenameList.setRowNums();
 			wFilenameList.optWidth(true);
 		}
 		if (in.getFileType() !=null) wFiletype.setText(in.getFileType());
 		if (in.getSeparator()!=null) wSeparator.setText(in.getSeparator());
 		if (in.getEnclosure()!=null) wEnclosure.setText(in.getEnclosure());
         if (in.getEscapeCharacter()!=null) wEscape.setText(in.getEscapeCharacter());
         wAccFilenames.setSelection(in.isAcceptingFilenames());
 		wHeader.setSelection(in.hasHeader());
         wNrHeader.setText( ""+in.getNrHeaderLines() );
 		wFooter.setSelection(in.hasFooter());
         wNrFooter.setText( ""+in.getNrFooterLines() );
         wWraps.setSelection(in.isLineWrapped());
         wNrWraps.setText( ""+in.getNrWraps() );
         wLayoutPaged.setSelection(in.isLayoutPaged());
         wNrLinesPerPage.setText( ""+in.getNrLinesPerPage() );
         wNrLinesDocHeader.setText( ""+in.getNrLinesDocHeader() );
 		wZipped.setSelection(in.isZipped());
 		wNoempty.setSelection(in.noEmptyLines());
 		wInclFilename.setSelection(in.includeFilename());
 		wInclRownum.setSelection(in.includeRowNumber());
         wDateLenient.setSelection(in.isDateFormatLenient());
 		
         if (in.getFilenameField()!=null) wInclFilenameField.setText(in.getFilenameField());
 		if (in.getRowNumberField()!=null) wInclRownumField.setText(in.getRowNumberField());
 		if (in.getFileFormat()   !=null) wFormat.setText(in.getFileFormat());
 		wLimit.setText(""+in.getRowLimit());
 		
 		log.logDebug(toString(), "getting fields info...");
 		for (int i=0;i<in.getInputFields().length;i++)
 		{
 		    TextFileInputField field = in.getInputFields()[i];
 		    
 			TableItem item = wFields.table.getItem(i);
 			item.setText(1, field.getName());
 			String type     = field.getTypeDesc();
 			String format   = field.getFormat();
 			String position = ""+field.getPosition();
 			String length   = ""+field.getLength();
 			String prec     = ""+field.getPrecision();
 			String curr     = field.getCurrencySymbol();
 			String group    = field.getGroupSymbol();
 			String decim    = field.getDecimalSymbol();
 			String def      = field.getNullString();
             String ifNull   = field.getIfNullValue();
 			String trim     = field.getTrimTypeDesc();
 			String rep      = field.isRepeated()?Messages.getString("System.Combo.Yes"):Messages.getString("System.Button.No");
 			
 			if (type    !=null) item.setText( 2, type    );
 			if (format  !=null) item.setText( 3, format  );
 			if (position!=null && !"-1".equals(position)) item.setText( 4, position);
 			if (length  !=null && !"-1".equals(length  )) item.setText( 5, length  );
 			if (prec    !=null && !"-1".equals(prec    )) item.setText( 6, prec    );
 			if (curr    !=null) item.setText( 7, curr    );
 			if (decim   !=null) item.setText( 8, decim   );
 			if (group   !=null) item.setText( 9, group   );
 			if (def     !=null) item.setText(10, def     );
             if (ifNull  !=null) item.setText(11, ifNull     );
 			if (trim    !=null) item.setText(12, trim    );
 			if (rep     !=null) item.setText(13, rep     );
 		}
 		
         if ( in.getEncoding()!=null ) wEncoding.setText( in.getEncoding() );
         
         // Error handling fields...
         wErrorIgnored.setSelection( in.isErrorIgnored() );
         wSkipErrorLines.setSelection( in.isErrorLineSkipped() );
         if (in.getErrorCountField()!=null) wErrorCount.setText( in.getErrorCountField() );
         if (in.getErrorFieldsField()!=null) wErrorFields.setText( in.getErrorFieldsField() );
         if (in.getErrorTextField()!=null) wErrorText.setText( in.getErrorTextField() );
 
         if (in.getWarningFilesDestinationDirectory()!=null) wWarnDestDir.setText(in.getWarningFilesDestinationDirectory());
         if (in.getWarningFilesExtension()!=null) wWarnExt.setText(in.getWarningFilesExtension());
 
         if (in.getErrorFilesDestinationDirectory()!=null) wErrorDestDir.setText(in.getErrorFilesDestinationDirectory());
         if (in.getErrorLineFilesExtension()!=null) wErrorExt.setText(in.getErrorLineFilesExtension());
 
         if (in.getLineNumberFilesDestinationDirectory()!=null) wLineNrDestDir.setText(in.getLineNumberFilesDestinationDirectory());
         if (in.getLineNumberFilesExtension()!=null) wLineNrExt.setText(in.getLineNumberFilesExtension());
 
         for (int i=0;i<in.getFilter().length;i++)
         {
             TableItem item = wFilter.table.getItem(i);
             
             TextFileFilter filter = in.getFilter()[i];
             if (filter.getFilterString()  !=null) item.setText(1, filter.getFilterString());
             if (filter.getFilterPosition()>=0   ) item.setText(2, ""+filter.getFilterPosition());
             item.setText(3, filter.isFilterLastLine()?Messages.getString("System.Button.Yes"):Messages.getString("System.Button.No"));
         }
         
         // Date locale
         wDateLocale.setText(in.getDateFormatLocale().toString());
         
         wFields.removeEmptyRows();
         wFields.setRowNums();
         wFields.optWidth(true);
 
         wFilter.removeEmptyRows();
         wFilter.setRowNums();
         wFilter.optWidth(true);
 
         setFlags();
         
 		wStepname.selectAll();
 	}
 	
 	private void setEncodings()
     {
         // Encoding of the text file:
         if (!gotEncodings)
         {
             gotEncodings = true;
             
             wEncoding.removeAll();
             ArrayList values = new ArrayList(Charset.availableCharsets().values());
             for (int i=0;i<values.size();i++)
             {
                 Charset charSet = (Charset)values.get(i);
                 wEncoding.add( charSet.displayName() );
             }
             
             // Now select the default!
             String defEncoding = Const.getEnvironmentVariable("file.encoding", "UTF-8");
             int idx = Const.indexOfString(defEncoding, wEncoding.getItems() );
             if (idx>=0) wEncoding.select( idx );
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
 		getInfo(input);
 		dispose();
 	}
 	
 	private void getInfo(TextFileInputMeta in)
 	{
 		stepname = wStepname.getText(); // return value
 
 		// copy info to TextFileInputMeta class (input)
         in.setAcceptingFilenames( wAccFilenames.getSelection() );
         in.setAcceptingField( wAccField.getText() );
         in.setAcceptingStep( transMeta.findStep( wAccStep.getText() ) );
         
 		in.setFileType( wFiletype.getText() );
 		in.setFileFormat( wFormat.getText() );
 		in.setSeparator( wSeparator.getText() );
 		in.setEnclosure( wEnclosure.getText() );
         in.setEscapeCharacter( wEscape.getText() );
 		in.setRowLimit( Const.toLong(wLimit.getText(), 0L) );
 		in.setFilenameField( wInclFilenameField.getText() );
 		in.setRowNumberField( wInclRownumField.getText() );
 				
 		in.setIncludeFilename( wInclFilename.getSelection() );
 		in.setIncludeRowNumber( wInclRownum.getSelection() );
 		in.setHeader( wHeader.getSelection() );
         in.setNrHeaderLines( Const.toInt( wNrHeader.getText(), 1) );
 		in.setFooter( wFooter.getSelection() );
         in.setNrFooterLines( Const.toInt( wNrFooter.getText(), 1) );
         in.setLineWrapped( wWraps.getSelection() );
         in.setNrWraps( Const.toInt( wNrWraps.getText(), 1) );
         in.setLayoutPaged( wLayoutPaged.getSelection() );
         in.setNrLinesPerPage( Const.toInt( wNrLinesPerPage.getText(), 80) );
         in.setNrLinesDocHeader( Const.toInt( wNrLinesDocHeader.getText(), 0) );
 		in.setZipped( wZipped.getSelection() );
 		in.setDateFormatLenient( wDateLenient.getSelection() );
 		in.setNoEmptyLines( wNoempty.getSelection() );
 
         String encoding = wEncoding.getText();
         if (encoding.length()>0) 
         {
             in.setEncoding(encoding);
         }
         
 		int nrfiles    = wFilenameList.getItemCount();
 		int nrfields   = wFields.nrNonEmpty();
         int nrfilters  = wFilter.nrNonEmpty();
 		in.allocate(nrfiles, nrfields, nrfilters);
 
 		in.setFileName( wFilenameList.getItems(0) );
 		in.setFileMask( wFilenameList.getItems(1) );
 		in.setFileRequired( wFilenameList.getItems(2) );
 
 		for (int i=0;i<nrfields;i++)
 		{
 		    TextFileInputField field = new TextFileInputField();
 		    
 			TableItem item  = wFields.getNonEmpty(i);
 			field.setName( item.getText(1) );
 			field.setType( Value.getType(item.getText(2)) );
 			field.setFormat( item.getText(3) );
 			field.setPosition( Const.toInt(item.getText(4), -1) );
 			field.setLength( Const.toInt(item.getText(5), -1) );
 			field.setPrecision( Const.toInt(item.getText(6), -1) );
 			field.setCurrencySymbol( item.getText(7) );
 			field.setDecimalSymbol( item.getText(8) );
 			field.setGroupSymbol( item.getText(9) );
 			field.setNullString( item.getText(10) );
             field.setIfNullValue(item.getText(11));
 			field.setTrimType( TextFileInputMeta.getTrimTypeByDesc(item.getText(12)) );
 			field.setRepeated( Messages.getString("System.Button.Yes").equalsIgnoreCase(item.getText(13)) );		
 			
 			in.getInputFields()[i] = field;
 		}
         
         for (int i=0;i<nrfilters;i++)
         {
             TableItem item = wFilter.getNonEmpty(i);
             TextFileFilter filter = new TextFileFilter();
             in.getFilter()[i] = filter;
             
             filter.setFilterString( item.getText(1) );
             filter.setFilterPosition( Const.toInt(item.getText(2), -1) );
             filter.setFilterLastLine( Messages.getString("System.Button.Yes").equalsIgnoreCase( item.getText(3) ) );
         }
         // Error handling fields...
         in.setErrorIgnored( wErrorIgnored.getSelection() );
         in.setErrorLineSkipped( wSkipErrorLines.getSelection() );
         in.setErrorCountField( wErrorCount.getText() );
         in.setErrorFieldsField( wErrorFields.getText() );
         in.setErrorTextField( wErrorText.getText() );
         
         in.setWarningFilesDestinationDirectory( wWarnDestDir.getText() );
         in.setWarningFilesExtension( wWarnExt.getText() );
         in.setErrorFilesDestinationDirectory( wErrorDestDir.getText() );
         in.setErrorLineFilesExtension( wErrorExt.getText() ); 
         in.setLineNumberFilesDestinationDirectory( wLineNrDestDir.getText() );
         in.setLineNumberFilesExtension( wLineNrExt.getText() );
         
         // Date format Locale
         Locale locale = new Locale(wDateLocale.getText());
         if (!locale.equals(Locale.getDefault()))
         {
         	in.setDateFormatLocale(locale);
         }
         else
         {
         	in.setDateFormatLocale(Locale.getDefault());
         }
 	}
 	
 	private void get()
 	{
 		if (wFiletype.getText().equalsIgnoreCase("CSV"))
 		{
 			getCSV();
 		}
 		else
 		{
 			getFixed();
 		}
 	}
 	
 	// Get the data layout
 	private void getCSV()
 	{
 		TextFileInputMeta meta = new TextFileInputMeta();
 		getInfo(meta);
 						
 		FileInputList    textFileList = meta.getTextFileList();
 		FileInputStream fileInputStream = null;
 		ZipInputStream  zipInputStream = null ;
 		InputStream     inputStream  = null;
         String          fileFormat = wFormat.getText();
         
 		if (textFileList.nrOfFiles()>0)
 		{
 			int clearFields = meta.hasHeader()?SWT.YES:SWT.NO;
 			int nrInputFields = meta.getInputFields().length;
 
 			if (meta.hasHeader() && nrInputFields>0)
 			{
 				MessageBox mb = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION );
 				mb.setMessage(Messages.getString("TextFileInputDialog.ClearFieldList.DialogMessage"));
 				mb.setText(Messages.getString("TextFileInputDialog.ClearFieldList.DialogTitle"));
 				clearFields = mb.open();
 			}
 
 			try
 			{
 				if (clearFields == SWT.YES)
 				{
 					wFields.table.removeAll();
 				}
 
 				fileInputStream = new FileInputStream(textFileList.getFile(0));
 				Table table = wFields.table;
 				
 				if (meta.isZipped())
 				{
 					zipInputStream = new ZipInputStream(fileInputStream);
 					zipInputStream.getNextEntry();
 					inputStream=zipInputStream;
 				}
 				else
 				{
 					inputStream=fileInputStream;
 				}
                 
                 InputStreamReader reader;
                 if (meta.getEncoding()!=null && meta.getEncoding().length()>0)
                 {
                     reader = new InputStreamReader(inputStream, meta.getEncoding());
                 }
                 else
                 {
                     reader = new InputStreamReader(inputStream);
                 }
 	
 				if (clearFields == SWT.YES || !meta.hasHeader() || nrInputFields >0)
 				{
                     // Scan the header-line, determine fields...
                     String line = null;
                     if (meta.hasHeader() || meta.getInputFields().length == 0)
                     {
                         line = TextFileInput.getLine(log, reader, fileFormat);
                         if (line != null)
                         {
                             ArrayList fields = TextFileInput.convertLineToStrings(log, line.toString(), meta);
 
                             for (int i = 0; i < fields.size(); i++)
                             {
                                 String field = (String) fields.get(i);
                                 if (field == null || field.length() == 0 || (nrInputFields == 0 && !meta.hasHeader()))
                                 {
                                     field = "Field" + (i + 1);
                                 } else
                                 {
                                     // Trim the field
                                     field = Const.trim(field);
                                     // Replace all spaces & - with underscore _
                                     field = Const.replace(field, " ", "_");
                                     field = Const.replace(field, "-", "_");
                                 }
 
                                 TableItem item = new TableItem(table, SWT.NONE);
                                 item.setText(1, field);
                                 item.setText(2, "String"); // The default type is String...
                                 
                                 // Copy it...
                                 getInfo(meta);
                             }
                         }
                     }
 
                     // Sample a few lines to determine the correct type of the fields...
                     String shellText = Messages.getString("TextFileInputDialog.LinesToSample.DialogTitle");
                     String lineText = Messages.getString("TextFileInputDialog.LinesToSample.DialogMessage");
                     EnterNumberDialog end = new EnterNumberDialog(shell, props, 100, shellText, lineText);
                     int samples = end.open();
                     if (samples >= 0)
                     {
                         getInfo(meta);
 
     			        TextFileCSVImportProgressDialog pd = new TextFileCSVImportProgressDialog(log, props, shell, meta, reader, samples, clearFields);
                         String message = pd.open();
                         if (message!=null)
                         {
                             // OK, what's the result of our search?
                             getData(meta);
                             wFields.removeEmptyRows();
                             wFields.setRowNums();
                             wFields.optWidth(true);
     
         					EnterTextDialog etd = new EnterTextDialog(shell, Messages.getString("TextFileInputDialog.ScanResults.DialogTitle"), Messages.getString("TextFileInputDialog.ScanResults.DialogMessage"), message, true);
         					etd.setReadOnly();
         					etd.open();
                         }
                     }
 				}
 				else
 				{
                     MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
 					mb.setMessage(Messages.getString("TextFileInputDialog.UnableToReadHeaderLine.DialogMessage"));
 					mb.setText(Messages.getString("System.DialogTitle.Error"));
 					mb.open(); 
 				}
 			}
 			catch(IOException e)
 			{
                 new ErrorDialog(shell, props, Messages.getString("TextFileInputDialog.IOError.DialogTitle"), Messages.getString("TextFileInputDialog.IOError.DialogMessage"), e);
 			}
             catch(KettleException e)
             {
                 new ErrorDialog(shell, props, Messages.getString("System.DialogTitle.Error"), Messages.getString("TextFileInputDialog.ErrorGettingFileDesc.DialogMessage"), e);
             }
 			finally
 			{
 				try
 				{
 					if (meta.isZipped() && zipInputStream!=null)
 					{
 						zipInputStream.closeEntry();
 						zipInputStream.close();
 					}
 					inputStream.close();
 				}
 				catch(Exception e)
 				{					
 				}
 			}
 		}
 		else
 		{
             MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
 			mb.setMessage(Messages.getString("TextFileInputDialog.NoValidFileFound.DialogMessage"));
 			mb.setText(Messages.getString("System.DialogTitle.Error"));
 			mb.open(); 
 		}
 	}
 	
 	public static final int guessPrecision(double d)
 	{
 		// Round numbers
 		long frac = Math.round(( d - Math.floor(d) ) * 1E10); // max precision : 10
 		int precision = 10;
 		
 		//  0,34 -->  3400000000 
 		//  0 to the right --> precision -1!
 		//  0 to the right means frac%10 == 0
 				
 		while (precision>=0 && (frac%10)==0)
 		{
 			frac/=10;
 			precision--;
 		}
 		precision++;
 				
 		return precision;
 	}
 
 	public static final int guessIntLength(double d)
 	{
 		double flr = Math.floor(d);
 		int len = 1;
 
 		while (flr>9)
 		{
 			flr/=10;
 			flr = Math.floor(flr);
 			len++;
 		}
 				
 		return len;
 	}
 	
 	public static final int guessLength(double d)
 	{	
 		int intlen = guessIntLength(d);
 		int precis = guessPrecision(d);
 		int length = 1;
 		
 		if (precis>0)
 		{
 			length = intlen + 1 + precis;	
 		}
 		else
 		{
 			length = intlen;
 		}
 		
 		return length;
 	}
 
     // Preview the data
     private void preview()
     {
         // Create the XML input step
         TextFileInputMeta oneMeta = new TextFileInputMeta();
         getInfo(oneMeta);
 
         TransMeta previewMeta = TransPreviewFactory.generatePreviewTransformation(oneMeta, wStepname.getText());
         
         EnterNumberDialog numberDialog = new EnterNumberDialog(shell, props, 500, Messages.getString("TextFileInputDialog.PreviewSize.DialogTitle"), Messages.getString("TextFileInputDialog.PreviewSize.DialogMessage"));
         int previewSize = numberDialog.open();
         if (previewSize>0)
         {
             TransPreviewProgressDialog progressDialog = new TransPreviewProgressDialog(shell, previewMeta, new String[] { wStepname.getText() }, new int[] { previewSize } );
             progressDialog.open();
 
             Trans trans = progressDialog.getTrans();
             String loggingText = progressDialog.getLoggingText();
 
             if (!progressDialog.isCancelled())
             {
                 if (trans.getResult()!=null && trans.getResult().getNrErrors()>0)
                 {
                 	EnterTextDialog etd = new EnterTextDialog(shell, Messages.getString("System.Dialog.PreviewError.Title"),  
                 			Messages.getString("System.Dialog.PreviewError.Message"), loggingText, true );
                 	etd.setReadOnly();
                 	etd.open();
                 }
             }
             
             PreviewRowsDialog prd =new PreviewRowsDialog(shell, SWT.NONE, wStepname.getText(), progressDialog.getPreviewRows(wStepname.getText()), loggingText);
             prd.open();
         }
     }
 
 	// Get the first x lines
 	private void first(boolean skipHeaders)
 	{
 		TextFileInputMeta info = new TextFileInputMeta();
 		getInfo(info);
 
         try
         {
     		if (info.getTextFileList().nrOfFiles()>0)
     		{
     			String shellText = Messages.getString("TextFileInputDialog.LinesToView.DialogTitle");
     			String lineText = Messages.getString("TextFileInputDialog.LinesToView.DialogMessage");
     			EnterNumberDialog end = new EnterNumberDialog(shell, props, 100, shellText, lineText);
     			int nrLines = end.open();
     			if (nrLines>=0)
     			{
                     ArrayList linesList = getFirst(nrLines, skipHeaders);
                     if (linesList!=null && linesList.size()>0)
                     {
                         String firstlines="";
                         for (int i=0;i<linesList.size();i++)
                         {
                             firstlines+=(String)linesList.get(i)+Const.CR;
                         }
                         EnterTextDialog etd = new EnterTextDialog(shell, Messages.getString("TextFileInputDialog.ContentOfFirstFile.DialogTitle"), (nrLines==0?Messages.getString("TextFileInputDialog.ContentOfFirstFile.AllLines.DialogMessage") : Messages.getString("TextFileInputDialog.ContentOfFirstFile.NLines.DialogMessage",""+nrLines)), firstlines, true);
                         etd.setReadOnly();
                         etd.open();
                     }
                     else
                     {
                         MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
                         mb.setMessage(Messages.getString("TextFileInputDialog.UnableToReadLines.DialogMessage"));
                         mb.setText(Messages.getString("TextFileInputDialog.UnableToReadLines.DialogTitle"));
                         mb.open(); 
     				}
     			}
             }
             else
             {
                 MessageBox mb = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR );
                 mb.setMessage(Messages.getString("TextFileInputDialog.NoValidFile.DialogMessage"));
                 mb.setText(Messages.getString("System.DialogTitle.Error"));
                 mb.open(); 
             }
         }
 		catch(KettleException e)
 		{
             new ErrorDialog(shell, props, Messages.getString("System.DialogTitle.Error"), Messages.getString("TextFileInputDialog.ErrorGettingData.DialogMessage"), e);
 		}
 	}
 	
 
 	// Get the first x lines
 	private ArrayList getFirst(int nrlines, boolean skipHeaders) throws KettleException
 	{
 		TextFileInputMeta info = new TextFileInputMeta();
 		getInfo(info);
 		FileInputList textFileList = info.getTextFileList();
 		
         FileInputStream fi = null;
 		ZipInputStream  zi = null ;
 		InputStream     f  = null;
 		
 		ArrayList retval = new ArrayList();
 		
 		if (textFileList.nrOfFiles()>0)
 		{
 			File file = textFileList.getFile(0);
 			try
 			{
 				fi = new FileInputStream(file);
 				
 				if (info.isZipped())
 				{
 					zi = new ZipInputStream(fi);
 					zi.getNextEntry();
 					f=zi;
 				}
 				else
 				{
 					f=fi;
 				}
                 
                 InputStreamReader reader;
                 if (info.getEncoding()!=null && info.getEncoding().length()>0)
                 {
                     reader = new InputStreamReader(f, info.getEncoding());
                 }
                 else
                 {
                     reader = new InputStreamReader(f);
                 }
 
 
 				String firstlines="";
 				int    linenr=0;
 				int    maxnr = nrlines+(info.hasHeader()?info.getNrHeaderLines():0);
 				
                 if (skipHeaders)
                 {
                     // Skip the header lines first if more then one, it helps us position
                     if (info.isLayoutPaged() && info.getNrLinesDocHeader()>0)
                     {
                         int skipped = 0;
                         String line = TextFileInput.getLine(log, reader, wFormat.getText());
                         while (line!=null && skipped<info.getNrLinesDocHeader()-1)
                         {
                             skipped++;
                             line = TextFileInput.getLine(log, reader, wFormat.getText());
                         }
                     }
                     
                     // Skip the header lines first if more then one, it helps us position
                     if (info.hasHeader() && info.getNrHeaderLines()>0)
                     {
                         int skipped = 0;
                         String line = TextFileInput.getLine(log, reader, wFormat.getText());
                         while (line!=null && skipped<info.getNrHeaderLines()-1)
                         {
                             skipped++;
                             line = TextFileInput.getLine(log, reader, wFormat.getText());
                         }
                     }
                 }
                 
 				String line = TextFileInput.getLine(log, reader, wFormat.getText());
 				while(line!=null && (linenr<maxnr || nrlines==0))
 				{
 					retval.add(line);
 					firstlines+=line+Const.CR;
 					linenr++;
 					line = TextFileInput.getLine(log, reader, wFormat.getText());
 				}
 			}
 			catch(Exception e)
 			{
                 throw new KettleException(Messages.getString("TextFileInputDialog.Exception.ErrorGettingFirstLines", ""+nrlines, file.getPath()), e);
 			}
 			finally
 			{
 				try
 				{
 					if (info.isZipped() && zi!=null)
 					{
 						zi.closeEntry();
 						zi.close();
 					}
 					f.close();
 				}
 				catch(Exception e)
 				{					
 				}
 			}
 		}
 		
 		return retval;
 	}
 	
 
 	
 	private void getFixed()
 	{
 		TextFileInputMeta info = new TextFileInputMeta();
 		getInfo(info);
 		
 		Shell sh = new Shell(shell, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
 
         try
         {
     		ArrayList rows = getFirst(50, false);
     		fields = getFields(info, rows);
     		
     		final TextFileImportWizardPage1 page1 = new TextFileImportWizardPage1("1", props, rows, fields);
     		page1.createControl(sh);
     		final TextFileImportWizardPage2 page2 = new TextFileImportWizardPage2("2", props, rows, fields);
     		page2.createControl(sh);
     
     		Wizard wizard = new Wizard() 
     		{
     			public boolean performFinish() 
     			{
     				wFields.clearAll(false);
     				
     				for (int i=0;i<fields.size();i++)
     				{
     					TextFileInputField field = (TextFileInputField)fields.get(i);
     					if (!field.isIgnored() && field.getLength()>0)
     					{
     						TableItem item = new TableItem(wFields.table, SWT.NONE);
     						item.setText( 1,   field.getName());
     						item.setText( 2,""+field.getTypeDesc());
     						item.setText( 3,""+field.getFormat());
     						item.setText( 4,""+field.getPosition());
     						item.setText( 5,""+field.getLength());
     						item.setText( 6,""+field.getPrecision());
     						item.setText( 7,""+field.getCurrencySymbol());
     						item.setText( 8,""+field.getDecimalSymbol());
     						item.setText( 9,""+field.getGroupSymbol());
     						item.setText(10,""+field.getNullString());
     						item.setText(11,""+field.getTrimTypeDesc());
     						item.setText(12,   field.isRepeated()?Messages.getString("System.Combo.Yes"):Messages.getString("System.Combo.No"));
     					}
     					
     				}
     				int size = wFields.table.getItemCount(); 
     				if (size==0)
     				{
     					new TableItem(wFields.table, SWT.NONE);
     				}
     
     				wFields.removeEmptyRows();				
     				wFields.setRowNums();
     				wFields.optWidth(true);
     				
     				input.setChanged();
     				
     				return true;
     			}
     		};
     				
     		wizard.addPage(page1);
     		wizard.addPage(page2);
     				
     		WizardDialog wd = new WizardDialog(shell, wizard);
     		wd.setMinimumPageSize(700,375);
     		wd.open();
         }
         catch(Exception e)
         {
             new ErrorDialog(shell, props, Messages.getString("TextFileInputDialog.ErrorShowingFixedWizard.DialogTitle"), Messages.getString("TextFileInputDialog.ErrorShowingFixedWizard.DialogMessage"), e);
         }
 	}
 	
 	private Vector getFields(TextFileInputMeta info, ArrayList rows)
 	{
 		Vector fields = new Vector();
 
 		int maxsize=0;
 		for (int i=0;i<rows.size();i++) 
 		{
 			int len = ((String)rows.get(i)).length();
 			if (len>maxsize) maxsize=len;
 		}
 
 		int prevEnd = 0;
 		int dummynr = 1;
 
 		for (int i=0;i<info.getInputFields().length;i++)
 		{
 		    TextFileInputField f = info.getInputFields()[i];
 		    
 			// See if positions are skipped, if this is the case, add dummy fields...
 			if (f.getPosition()!=prevEnd) // gap
 			{
 				TextFileInputField field = new TextFileInputField("Dummy"+dummynr, prevEnd, f.getPosition()-prevEnd);
 				field.setIgnored(true); // don't include in result by default.
 				fields.add(field);
 				dummynr++;
 			}
 
 			TextFileInputField field = new TextFileInputField(f.getName(), f.getPosition(), f.getLength());
 			field.setType(f.getType());
 			field.setIgnored(false);
 			field.setFormat(f.getFormat());
 			field.setPrecision(f.getPrecision());
 			field.setTrimType(f.getTrimType());
 			field.setDecimalSymbol(f.getDecimalSymbol());
 			field.setGroupSymbol(f.getGroupSymbol());
 			field.setCurrencySymbol(f.getCurrencySymbol());
 			field.setRepeated(f.isRepeated());
 			field.setNullString(f.getNullString());
 			
 			fields.add(field);
 			
 			prevEnd = field.getPosition()+field.getLength();
 		}
 		
 		if (info.getInputFields().length==0)
 		{
 			TextFileInputField field = new TextFileInputField("Field1", 0, maxsize);
 			fields.add(field);
 		}
 		else
 		{		    
 			// Take the last field and see if it reached until the maximum...
 		    TextFileInputField f = info.getInputFields()[info.getInputFields().length-1];
 
 			int pos = f.getPosition();
 			int len = f.getLength();
 			if (pos+len<maxsize)
 			{
 				// If not, add an extra trailing field!
 				TextFileInputField field = new TextFileInputField("Dummy"+dummynr, pos+len, maxsize-pos-len);
 				field.setIgnored(true); // don't include in result by default.
 				fields.add(field);
 				dummynr++;
 			}
 		}
 		
 		quickSort(fields);
 		
 		return fields;
 	}
 
     
 	/** Sort the entire vector, if it is not empty
 	 */
 	public synchronized void quickSort(Vector elements)
 	{
 		if (! elements.isEmpty())
 		{ 
 			this.quickSort(elements, 0, elements.size()-1);
 		}
 	}
 
 
 	/**
 	 * QuickSort.java by Henk Jan Nootenboom, 9 Sep 2002
 	 * Copyright 2002-2003 SUMit. All Rights Reserved.
 	 *
 	 * Algorithm designed by prof C. A. R. Hoare, 1962
 	 * See http://www.sum-it.nl/en200236.html
 	 * for algorithm improvement by Henk Jan Nootenboom, 2002.
 	 *
 	 * Recursive Quicksort, sorts (part of) a Vector by
 	 *  1.  Choose a pivot, an element used for comparison
 	 *  2.  dividing into two parts:
 	 *      - less than-equal pivot
 	 *      - and greater than-equal to pivot.
 	 *      A element that is equal to the pivot may end up in any part.
 	 *      See www.sum-it.nl/en200236.html for the theory behind this.
 	 *  3. Sort the parts recursively until there is only one element left.
 	 *
 	 * www.sum-it.nl/QuickSort.java this source code
 	 * www.sum-it.nl/quicksort.php3 demo of this quicksort in a java applet
 	 *
 	 * Permission to use, copy, modify, and distribute this java source code
 	 * and its documentation for NON-COMMERCIAL or COMMERCIAL purposes and
 	 * without fee is hereby granted.
 	 * See http://www.sum-it.nl/security/index.html for copyright laws.
 	 */
 	  private synchronized void quickSort(Vector elements, int lowIndex, int highIndex)
 	  { 
 		int lowToHighIndex;
 		int highToLowIndex;
 		int pivotIndex;
 		TextFileInputField pivotValue;  // values are Strings in this demo, change to suit your application
 		TextFileInputField lowToHighValue;
 		TextFileInputField highToLowValue;
 		TextFileInputField parking;
 		int newLowIndex;
 		int newHighIndex;
 		int compareResult;
 
 		lowToHighIndex = lowIndex;
 		highToLowIndex = highIndex;
 		/** Choose a pivot, remember it's value
 		 *  No special action for the pivot element itself.
 		 *  It will be treated just like any other element.
 		 */
 		pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
 		pivotValue = (TextFileInputField)elements.elementAt(pivotIndex);
 
 		/** Split the Vector in two parts.
 		 *
 		 *  The lower part will be lowIndex - newHighIndex,
 		 *  containing elements <= pivot Value
 		 *
 		 *  The higher part will be newLowIndex - highIndex,
 		 *  containting elements >= pivot Value
 		 * 
 		 */
 		newLowIndex = highIndex + 1;
 		newHighIndex = lowIndex - 1;
 		// loop until low meets high
 		while ((newHighIndex + 1) < newLowIndex) // loop until partition complete
 		{ // loop from low to high to find a candidate for swapping
 		  lowToHighValue = (TextFileInputField)elements.elementAt(lowToHighIndex);
 		  while (lowToHighIndex < newLowIndex
 			& lowToHighValue.compare(pivotValue)<0 )
 		  { 
 			newHighIndex = lowToHighIndex; // add element to lower part
 			lowToHighIndex ++;
 			lowToHighValue = (TextFileInputField)elements.elementAt(lowToHighIndex);
 		  }
 
 		  // loop from high to low find other candidate for swapping
 		  highToLowValue = (TextFileInputField)elements.elementAt(highToLowIndex);
 		  while (newHighIndex <= highToLowIndex
 			& (highToLowValue.compare(pivotValue)>0)
 			)
 		  { 
 			newLowIndex = highToLowIndex; // add element to higher part
 			highToLowIndex --;
 			highToLowValue = (TextFileInputField)elements.elementAt(highToLowIndex);
 		  }
 
 		  // swap if needed
 		  if (lowToHighIndex == highToLowIndex) // one last element, may go in either part
 		  { 
 			newHighIndex = lowToHighIndex; // move element arbitrary to lower part
 		  }
 		  else if (lowToHighIndex < highToLowIndex) // not last element yet
 		  { 
 			compareResult = lowToHighValue.compare(highToLowValue);
 			if (compareResult >= 0) // low >= high, swap, even if equal
 			{ 
 			  parking = lowToHighValue;
 			  elements.setElementAt(highToLowValue, lowToHighIndex);
 			  elements.setElementAt(parking, highToLowIndex);
 
 			  newLowIndex = highToLowIndex;
 			  newHighIndex = lowToHighIndex;
 
 			  lowToHighIndex ++;
 			  highToLowIndex --;
 			}
 		  }
 		}
 
 		// Continue recursion for parts that have more than one element
 		if (lowIndex < newHighIndex)
 		{ 
 			this.quickSort(elements, lowIndex, newHighIndex); // sort lower subpart
 		}
 		if (newLowIndex < highIndex)
 		{ 
 			this.quickSort(elements, newLowIndex, highIndex); // sort higher subpart
 		}
 	  }
 
 	
 	public String toString()
 	{
 		return this.getClass().getName();
 	}
 
 }
