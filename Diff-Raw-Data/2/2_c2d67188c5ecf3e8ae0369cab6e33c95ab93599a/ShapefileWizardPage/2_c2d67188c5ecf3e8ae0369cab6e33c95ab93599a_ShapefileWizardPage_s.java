 /**
  * 
  */
 package org.tomale.id.gis.mapsources.shapefile.wizards;
 
 import org.eclipse.jface.fieldassist.ControlDecoration;
 import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.tomale.id.gis.Activator;
 
 /**
  * @author ftomale
  *
  */
 public class ShapefileWizardPage extends WizardPage {
 
 	public static final String PAGE_TITLE = "New Shapefile Map Source";
 	public static final String PAGE_TEXT = "Shapefile Map Source";
 	public static final String PAGE_DESC = "Add shapefile map source";
 	
 	Text _file;
 	Button _browse;
 	
 	public ShapefileWizardPage(){
 		super(PAGE_TITLE,PAGE_TEXT,Activator.imageDescriptorFromPlugin(
 				Activator.PLUGIN_ID,"icons/alt_window_16.gif"));
 		setDescription(PAGE_DESC);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	public void createControl(Composite parent) {
 		Composite comp = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 3;
 		comp.setLayoutData(comp);
 		
 		Label l;
 		ControlDecoration decoration;
 		
 		l = new Label(comp, SWT.NONE);
 		l.setText("File");
 		l.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
 		
 		_file = new Text(comp, SWT.BORDER);
 		decoration = new ControlDecoration(_file, SWT.LEFT);
 		decoration.setDescriptionText("Please enter a shapefile name");
 		decoration.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED).getImage());
 		_file.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
 		
 		_browse = new Button(comp, SWT.PUSH);
 		_browse.setText("...");
 		_browse.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,false));
 		_browse.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				chooseFile();
 			}
 			
 		});
 	}
 	
 	private void chooseFile(){
 		
 		FileDialog dlg = new FileDialog(getShell(),SWT.OPEN);
 		dlg.setFilterExtensions(new String[]{"*.prj","*.shp","*.dbf"});
 		dlg.setText("Please select a shape file");
 		String f = dlg.open();
 		
 		_file.setText(f);
 		
 	}
 
 }
