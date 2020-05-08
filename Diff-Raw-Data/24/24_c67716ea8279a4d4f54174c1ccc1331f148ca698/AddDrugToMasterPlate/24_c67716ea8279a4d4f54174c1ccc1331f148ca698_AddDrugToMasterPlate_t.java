 package net.bioclipse.brunn.ui.dialogs;
 
 import net.bioclipse.brunn.Springcontact;
 import net.bioclipse.brunn.pojos.ConcUnit;
 import net.bioclipse.brunn.ui.explorer.model.nonFolders.MasterPlate;
 
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 
 
 public class AddDrugToMasterPlate extends TitleAreaDialog {
 
 	private Combo concUnitCombo;
 	private Text dilutionFactorText;
 	private Text concentrationText;
 	private String[] markers;
 	private ConcUnit concUnit;
 	private boolean doingHorizontalDilutionSeries;
 
 	
 	private double dilutionFactor = lastDilutionFactor;
 	private double concentration = lastConcentration;
	private final ConcUnit[] concUnits = ConcUnit.all();
 	
 	private static double lastDilutionFactor = 1;
 	private static double lastConcentration = 100;
 	private static int lastConcentrationIndex = 1;
 	private static boolean lastDoingHorizontalDilutionSeries = true;
 	private Button verticalDilutionSeriesButton;
 	private Button horizontalDilutionSeriesButton;
 	
 	public double getConcentration() {
 		return concentration;
 	}
 
 	public double getDilutionFactor() {
 		return dilutionFactor;
 	}
 
 	/**
 	 * Create the dialog
 	 * @param parentShell
 	 */
 	public AddDrugToMasterPlate(Shell parentShell) {
 		super(parentShell);
 		
 	}
 
 	/**
 	 * Create contents of the dialog
 	 * @param parent
 	 */
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		Composite area = (Composite) super.createDialogArea(parent);
 		Composite container = new Composite(area, SWT.NONE);
 		container.setLayout(new FormLayout());
 		container.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		final Label startingConcentrationLabel = new Label(container, SWT.NONE);
 		final FormData fd_startingConcentrationLabel = new FormData();
 		startingConcentrationLabel.setLayoutData(fd_startingConcentrationLabel);
 		startingConcentrationLabel.setText("Starting concentration:");
 
 		concentrationText = new Text(container, SWT.BORDER);
 		fd_startingConcentrationLabel.bottom = new FormAttachment(concentrationText, 0, SWT.BOTTOM);
 		fd_startingConcentrationLabel.right = new FormAttachment(concentrationText, -5, SWT.LEFT);
 		final FormData fd_concentrationText = new FormData();
 		fd_concentrationText.right = new FormAttachment(100, -54);
 		fd_concentrationText.bottom = new FormAttachment(0, 39);
 		fd_concentrationText.left = new FormAttachment(0, 197);
 		concentrationText.setLayoutData(fd_concentrationText);
 		concentrationText.setText(concentration + "");
 
 		final Label dilutionFactorLabel = new Label(container, SWT.NONE);
 		final FormData fd_dilutionFactorLabel = new FormData();
 		fd_dilutionFactorLabel.bottom = new FormAttachment(0, 150);
 		dilutionFactorLabel.setLayoutData(fd_dilutionFactorLabel);
 		dilutionFactorLabel.setText("Dilution factor:");
 
 		dilutionFactorText = new Text(container, SWT.BORDER);
 		final FormData fd_dilutionFactorText = new FormData();
 		fd_dilutionFactorText.right = new FormAttachment(concentrationText, 0, SWT.RIGHT);
 		fd_dilutionFactorText.left = new FormAttachment(0, 197);
 		fd_dilutionFactorText.bottom = new FormAttachment(100, -64);
 		dilutionFactorText.setLayoutData(fd_dilutionFactorText);
 		dilutionFactorText.setText(dilutionFactor + "");
 
 		concUnitCombo = new Combo(container, SWT.NONE|SWT.READ_ONLY);
 		fd_dilutionFactorLabel.right = new FormAttachment(concUnitCombo, -5, SWT.LEFT);
 		final FormData fd_concUnitCombo = new FormData();
 		fd_concUnitCombo.right = new FormAttachment(dilutionFactorText, 0, SWT.RIGHT);
 		fd_concUnitCombo.bottom = new FormAttachment(100, -22);
 		fd_concUnitCombo.left = new FormAttachment(0, 197);
 		concUnitCombo.setLayoutData(fd_concUnitCombo);
 		
 		String[] items = new String[concUnits.length];
 		for (int i = 0; i < items.length; i++) {
 			items[i] = concUnits[i].toString();
 		}
 		concUnitCombo.setItems(items);
 		concUnitCombo.select(lastConcentrationIndex);
 
 		Label concentrationUnitLabel;
 		concentrationUnitLabel = new Label(container, SWT.NONE);
 		final FormData fd_concentrationUnitLabel = new FormData();
 		fd_concentrationUnitLabel.bottom = new FormAttachment(concUnitCombo, 0, SWT.BOTTOM);
 		fd_concentrationUnitLabel.right = new FormAttachment(concUnitCombo, -5, SWT.LEFT);
 		concentrationUnitLabel.setLayoutData(fd_concentrationUnitLabel);
 		concentrationUnitLabel.setText("Concentration unit:");
 
 		horizontalDilutionSeriesButton = new Button(container, SWT.RADIO);
 		final FormData fd_dilutionSeriesButton = new FormData();
 		fd_dilutionSeriesButton.bottom = new FormAttachment(0, 75);
 		fd_dilutionSeriesButton.left = new FormAttachment(startingConcentrationLabel, 5, SWT.RIGHT);
 		horizontalDilutionSeriesButton.setLayoutData(fd_dilutionSeriesButton);
 		horizontalDilutionSeriesButton.setText("Horizontal dilution series");
 
 		verticalDilutionSeriesButton = new Button(container, SWT.RADIO);
 		final FormData fd_verticalDilutionSeriesButton = new FormData();
 		fd_verticalDilutionSeriesButton.bottom = new FormAttachment(0, 110);
 		fd_verticalDilutionSeriesButton.left = new FormAttachment(horizontalDilutionSeriesButton, 0, SWT.LEFT);
 		verticalDilutionSeriesButton.setLayoutData(fd_verticalDilutionSeriesButton);
 		verticalDilutionSeriesButton.setText("Vertical dilution series");
 		setTitle("Add dilution series to masterPlate");
 		//
 		if ( lastDoingHorizontalDilutionSeries ) {
 			horizontalDilutionSeriesButton.setSelection(true);
 		}
 		else {
 			verticalDilutionSeriesButton.setSelection(true);
 		}
 		return area;
 	}
 
 	/**
 	 * Create contents of the button bar
 	 * @param parent
 	 */
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
 				true);
 		createButton(parent, IDialogConstants.CANCEL_ID,
 				IDialogConstants.CANCEL_LABEL, false);
 	}
 
 	/**
 	 * Return the initial size of the dialog
 	 */
 	@Override
 	protected Point getInitialSize() {
 		return new Point(447, 379);
 	}
 
 	protected void buttonPressed(int buttonId) {
 		if (buttonId == IDialogConstants.OK_ID) {
 			try {
 				concentration  = Double.parseDouble( concentrationText.getText()  );
 				dilutionFactor = Double.parseDouble( dilutionFactorText.getText() );
 				lastConcentration = concentration;
 				lastDilutionFactor = dilutionFactor;
 				lastConcentrationIndex = concUnitCombo.getSelectionIndex();
 				doingHorizontalDilutionSeries = !verticalDilutionSeriesButton.getSelection();
 				lastDoingHorizontalDilutionSeries = doingHorizontalDilutionSeries;
 			}
 			catch(NumberFormatException e) {
 				MessageDialog.openInformation( PlatformUI
 						                       .getWorkbench()
 						                       .getActiveWorkbenchWindow()
 						                       .getShell(), 
 						                       "Could not parse double", 
 						                       "Concentration and dilution factor should be doubles (e.g. 3.14)" );
 				return;
 			}
 			concUnit = concUnits[concUnitCombo.getSelectionIndex()];
 		}
 		super.buttonPressed(buttonId);
 	}
 
 	public ConcUnit getConcUnit() {
 		return concUnit;
 	}
 
 	public boolean isDoingHorizontalDilutionSeries() {
 		return doingHorizontalDilutionSeries;
 	}
 }
