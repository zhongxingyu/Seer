 package org.dawnsci.plotting.tools.preference.diffraction;
 
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchWizard;
 
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrantSpacing;
 import uk.ac.diamond.scisoft.analysis.crystallography.CalibrationStandards;
import uk.ac.diamond.scisoft.analysis.crystallography.HKL;
 
 public class AddCalibrantWizard extends Wizard implements IWorkbenchWizard{
 
 	public static final String ID = "org.dawb.workbench.plotting.preference.diffraction.addCalibrantWizard";
 		
 	private CalibrationStandards calibrationStandards;
 	
 	public AddCalibrantWizard() {
 		super();
 		addPage(new AddCalibrantPage("Add Calibrant Spacing"));
 		setWindowTitle("Add Calibrant");
 	}
 
 	@Override
 	public boolean performFinish() {
 		
 		final AddCalibrantPage page = (AddCalibrantPage)getPages()[0];
 		final CalibrantSpacing cs = page.isCopy ? calibrationStandards.getCalibrant().clone()
				                                : createEmpty();
 		cs.setName(page.name);
 		
 		calibrationStandards.addCalibrant(cs);
 		calibrationStandards.setSelectedCalibrant(cs.getName(), true);
 		
 	    return true;
 	}
 
	private CalibrantSpacing createEmpty() {
		CalibrantSpacing cs = new CalibrantSpacing();
		cs.addHKL(new HKL());
		return cs;
	}

 	private final class AddCalibrantPage extends WizardPage {
 
 
 		private boolean isCopy;
 		private String  name;
 		private Text    nameTxt;
 		private Button  copyButton;
 		
 		protected AddCalibrantPage(String pageName) {
 			super(pageName);
 		}
 
 		@Override
 		public void createControl(Composite parent) {
 			
 			Composite container = new Composite(parent, SWT.NULL);
 			GridLayout layout = new GridLayout();
 			container.setLayout(layout);
 			layout.numColumns = 2;
 			layout.verticalSpacing = 9;
 
 			Label label = new Label(container, SWT.NONE);
 			label.setText("Calibrant Name");
 			this.nameTxt = new Text(container, SWT.BORDER);
 			nameTxt.setTextLimit(64);
 			nameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
 			nameTxt.addModifyListener(new ModifyListener() {
 				
 				@Override
 				public void modifyText(ModifyEvent e) {
 					nameChanged();
 				}
 			});
 			
 			this.copyButton = new Button(container, SWT.CHECK);
 			copyButton.setSelection(true);
 			copyButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
 			copyButton.setText("Copy '"+calibrationStandards.getSelectedCalibrant()+"' ");
			copyButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					nameChanged();
				}			
			});
 
 			nameChanged();
 			setControl(container);
 
 		}
 
 		/**
 		 * Ensures that both text fields are set.
 		 */
 
 		private void nameChanged() {
 
             this.name = nameTxt.getText().trim();
             this.isCopy=copyButton.getSelection();
             
             if (name==null||"".equals(name)) {
             	updateStatus("Please set a name for the new calibrant.");
             	return;
             }
             if (calibrationStandards.getCalibrantList().contains(name)) {
             	updateStatus("The calibrant '"+name+"' already exists. Please choose a unique calibrant name.");
             	return;
             }
 
 			updateStatus(null);
 		}
 
 		private void updateStatus(String message) {
 			setErrorMessage(message);
 			setPageComplete(message == null);
 		}
 
 		@SuppressWarnings("unused")
 		public boolean isCopy() {
 			return isCopy;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 	}
 
 	public CalibrationStandards getCalibrationStandards() {
 		return calibrationStandards;
 	}
 
 	public void setCalibrationStandards(CalibrationStandards calibrationStandards) {
 		this.calibrationStandards = calibrationStandards;
 	}
 
 	@Override
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
