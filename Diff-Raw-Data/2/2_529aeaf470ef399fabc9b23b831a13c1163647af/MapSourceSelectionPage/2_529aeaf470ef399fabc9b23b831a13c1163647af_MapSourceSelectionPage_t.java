 /**
  * 
  */
 package org.tomale.id.gis.wizards;
 
 import java.util.ArrayList;
 
 import org.eclipse.jface.fieldassist.ControlDecoration;
 import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.tomale.id.gis.Activator;
 import org.tomale.id.gis.preferences.MapSourceFactoryConfiguration;
 
 /**
  * @author ferd
  *
  */
 public class MapSourceSelectionPage extends WizardPage {
 
 	public static final String PAGE_TITLE = "New Map Layer";
 	public static final String PAGE_TEXT = "Map Layer";
 	public static final String PAGE_DESC = "Define a new map layer";
 	
 	Text _name;
 	Combo _sources;
 	
 	ModifyListener _modify = new ModifyListener() {
 		
 		@Override
 		public void modifyText(ModifyEvent e) {
 			updatePageComplete();
 		}
 	};
 	
 	public MapSourceSelectionPage(){
 		super(PAGE_TITLE,PAGE_TEXT,Activator.imageDescriptorFromPlugin(
 				Activator.PLUGIN_ID,"icons/alt_window_16.gif"));
 		setDescription(PAGE_DESC);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	public void createControl(Composite parent) {
 		Composite comp = new Composite(parent,SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		comp.setLayout(layout);
 		
 		Label l;
 		ControlDecoration decoration;
 		
 		l = new Label(comp,SWT.NONE);
 		l.setText("Name");
 		l.setLayoutData(new GridData(SWT.LEFT,SWT.FILL,false,false));
 		
 		_name = new Text(comp,SWT.BORDER);
 		decoration = new ControlDecoration(_name, SWT.LEFT);
 		decoration.setDescriptionText("Please enter a name for this map source");
 		decoration.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED).getImage());
 		_name.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
 		_name.addModifyListener(_modify);
 		
 		l = new Label(comp,SWT.NONE);
 		l.setText("Map Source");
 		l.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,false,false));
 		
 		_sources = new Combo(comp, SWT.DROP_DOWN | SWT.READ_ONLY);
 		initSources();
 		decoration = new ControlDecoration(_sources,SWT.LEFT);
 		decoration.setDescriptionText("Please select a map source provider");
 		decoration.setImage(FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_REQUIRED).getImage());
 		_sources.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,false));
 		_sources.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				
 				// add page for configuration of 
 				// map source provider here
 				
 				updatePageComplete();
 			}
 			
 		});
		
		setControl(comp);
 	}
 
 	private void initSources(){
 		ArrayList<MapSourceFactoryConfiguration> sources = Activator.getMapSourceFactories();
 		for(MapSourceFactoryConfiguration conf : sources){
 			_sources.add(conf.getName());
 			_sources.setData(conf.getName(),conf);
 		}
 	}
 
 	@Override
 	public boolean isPageComplete() {
 		return !_name.getText().isEmpty() & !_sources.getText().isEmpty();
 	}
 	
 	private void updatePageComplete(){
 		setPageComplete(isPageComplete());
 	}
 	
 	public String getName(){
 		return _name.getText();
 	}
 	
 	public MapSourceFactoryConfiguration getConfiguration(){
 		return (MapSourceFactoryConfiguration) _sources.getData(_sources.getText());
 	}
 }
