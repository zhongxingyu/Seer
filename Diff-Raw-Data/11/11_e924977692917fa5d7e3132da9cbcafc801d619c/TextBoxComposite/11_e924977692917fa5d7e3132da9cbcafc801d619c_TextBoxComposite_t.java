 package ui.composites;
 
 import hexapixel.cache.ImageCache;
 
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 
 import connection.Settings;
 
 import shared.NSAlertBox;
 
 public class TextBoxComposite extends Composite
 {
 	private Text timestampFormatText;
 	private Button btnEnableTimestamps;
 
 	/**
 	 * Create the composite.
 	 * @param parent
 	 * @param style
 	 */
 	public TextBoxComposite(Composite parent, int style)
 	{
 		super(parent, style);
 		setLayout(new GridLayout(4, false));
 		
 		Label lblTimestamp = new Label(this, SWT.NONE);
 		lblTimestamp.setText("Timestamps");
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		
 		btnEnableTimestamps = new Button(this, SWT.CHECK);
 		btnEnableTimestamps.setText("Enable timestamps");
 		btnEnableTimestamps.setSelection(Settings.getSettings().isTimestampsEnabled());
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		
 		Label lblTimestampFormat = new Label(this, SWT.NONE);
 		lblTimestampFormat.setText("Timestamp format: ");
 		
 		timestampFormatText = new Text(this, SWT.BORDER);
 		GridData gd_timestampFormatText = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		gd_timestampFormatText.widthHint = 97;
 		timestampFormatText.setLayoutData(gd_timestampFormatText);
 		timestampFormatText.setText(Settings.getSettings().getTimestampFormatPattern());
 		
 		Button btnTimestampHelp = new Button(this, SWT.NONE);
		btnTimestampHelp.setImage(ImageCache.getImage("info_small.png"));
 		GridData gd_btnNewButton = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnNewButton.heightHint = 28;
		gd_btnNewButton.widthHint = 28;
		gd_btnNewButton.verticalIndent = -1; //one pixel up because Darkblizer
 		btnTimestampHelp.setLayoutData(gd_btnNewButton);
 		btnTimestampHelp.addSelectionListener(new SelectionAdapter(){
 			@Override
 			public void widgetSelected(SelectionEvent e)
 			{
 				super.widgetSelected(e);
 				NSAlertBox infoAlert = new NSAlertBox("Timestamp Formatting","The formatting is based on the SimpleDateFormat in Java. Here are some common pattern letters:\n\nY = Year\nM = Month in year\nd = Day in month\nE = Day name in week\na = Am/pmmarker\nH = Hour in day(0-23)\nh = Hour in am/pm(1-12)\nm = Minute in hour\ns = Second inminute\nS = Millisecond\n\nFor more information, check http://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html",SWT.ICON_QUESTION|SWT.OK);
 				infoAlert.go();
 				
 			}
 		});
 		
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
 		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
 		
 //		Button btnSave = new Button(this, SWT.NONE);
 //		btnSave.addSelectionListener(new SelectionAdapter() {
 //			@Override
 //			public void widgetSelected(SelectionEvent e) {
 //				
 //			}
 //		});
 //		btnSave.setLayoutData(new GridData(SWT.RIGHT, SWT.BOTTOM, false, true, 1, 1));
 //		btnSave.setText("Save");
 
 	}
 
 	@Override
 	public void dispose()
 	{
 		if(timestampFormatText.getText().isEmpty())
 			timestampFormatText.setText("[hh:mm:ss]");
 		Settings.getSettings().setTimestampsEnabled(btnEnableTimestamps.getSelection());
 		Settings.getSettings().setTimestampFormatPattern(timestampFormatText.getText());
 		Settings.writeToFile();
 		super.dispose();
 	}
 
 	@Override
 	protected void checkSubclass()
 	{
 		// Disable the check that prevents subclassing of SWT components
 	}
 }

