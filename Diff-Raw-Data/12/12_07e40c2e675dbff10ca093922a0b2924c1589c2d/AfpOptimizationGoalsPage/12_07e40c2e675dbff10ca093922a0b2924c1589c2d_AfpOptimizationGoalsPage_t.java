 package org.amanzi.awe.afp.wizards;
 
 
 import java.util.HashMap;
 
 import org.amanzi.awe.afp.models.AfpModel;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 public class AfpOptimizationGoalsPage extends AfpWizardPage implements Listener {
 
 	protected GridData gridData;
 	protected Button frequenciesButton;
 	protected Button bsicButton;
 	protected Button hsnButton;
 	protected Button maioButton;
 	protected Button freq900Button;
 	protected Button freq1800Button;
 	protected Button freq850Button;
 	protected Button freq1900Button;
 	protected Button bcchButton;
 	protected Button bbHoppingButton;
 	protected Button sYHoppingButton;
 	protected Button analyzeCurrentButton;
 	private Group summaryGroup;
 	
 
 	
 	public AfpOptimizationGoalsPage(String pageName,AfpModel model, String desc) {
 		super(pageName, model);
         setPageComplete(false);
         setTitle(AfpImportWizard.title);
         setDescription(desc);
     }
 	
 	@Override
 	public void createControl(Composite parent) {
 		
 		Composite thisParent = new Composite(parent, SWT.NONE);
    	 	thisParent.setLayout(new GridLayout(2, false));
 		
 		Group stepsGroup = AfpWizardUtils.getStepsGroup(thisParent, 1);
 		
 		Group main = new Group(thisParent, SWT.NONE);
 		main.setLayout(new GridLayout(3, true));
 		main.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1 ,3));
 		
 		
 		
 		Group paramGroup = new Group(main, SWT.NONE);
 		paramGroup.setLayout(new GridLayout(1, false));
 		paramGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true,1 ,4));
 		paramGroup.setText("Optimization Parameters");
 		frequenciesButton = new Button(paramGroup, SWT.CHECK);
 		frequenciesButton.setText("Frequencies");
 		frequenciesButton.addListener(SWT.Selection, this);
 		frequenciesButton.setSelection(model.isOptimizeFrequency());
 		
 		
 		bsicButton = new Button(paramGroup, SWT.CHECK);
 		bsicButton.setText("BSIC");
 		bsicButton.addListener(SWT.Selection, this);
 		bsicButton.setSelection(model.isOptimizeBSIC());
 		
 		hsnButton = new Button(paramGroup, SWT.CHECK);
 		hsnButton.setText("HSN");
 		hsnButton.addListener(SWT.Selection, this);
 		hsnButton.setSelection(model.isOptimizeHSN());
 		
 		maioButton = new Button(paramGroup, SWT.CHECK);
 		maioButton.setText("MAIO");
 		maioButton.addListener(SWT.Selection, this);
 		maioButton.setSelection(model.isOptimizeMAIO());
 		
 		
 		Group frequencyBandGroup = new Group(main, SWT.NONE);
 		frequencyBandGroup.setLayout(new GridLayout(1, false));
 		frequencyBandGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true,1 ,4));
 		frequencyBandGroup.setText("Frequency Band");
 		freq900Button = new Button(frequencyBandGroup, SWT.CHECK);
 		freq900Button.setText("900");
 		freq900Button.addListener(SWT.Selection, this);
 		freq900Button.setSelection(model.getFrequencyBands()[0]);
 		
 		freq1800Button = new Button(frequencyBandGroup, SWT.CHECK);
 		freq1800Button.setText("1800");
 		freq1800Button.addListener(SWT.Selection, this);
 		freq1800Button.setSelection(model.getFrequencyBands()[1]);
 		
 		freq850Button = new Button(frequencyBandGroup, SWT.CHECK);
 		freq850Button.setText("850");
 		freq850Button.addListener(SWT.Selection, this);
 		freq850Button.setSelection(model.getFrequencyBands()[2]);
 		
 		freq1900Button = new Button(frequencyBandGroup, SWT.CHECK);
 		freq1900Button.setText("1900");
 		freq1900Button.addListener(SWT.Selection, this);
 		freq1900Button.setSelection(model.getFrequencyBands()[3]);
 		
 		Group channelGroup = new Group(main, SWT.NONE);
 		channelGroup.setLayout(new GridLayout(1, false));
 		channelGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true,1 ,4));
 		channelGroup.setText("Channel Type");
 		bcchButton = new Button(channelGroup, SWT.CHECK);
 		bcchButton.setText("BCCH");
 		bcchButton.addListener(SWT.Selection, this);
 		bcchButton.setSelection(model.getChanneltypes()[0]);
 		
 		bbHoppingButton = new Button(channelGroup, SWT.CHECK);
 		bbHoppingButton.setText("TCH Non/BB Hopping");
 		bbHoppingButton.addListener(SWT.Selection, this);
 		bbHoppingButton.setSelection(model.getChanneltypes()[1]);
 		
 		sYHoppingButton = new Button(channelGroup, SWT.CHECK);
 		sYHoppingButton.setText("TCH SY Hopping");
 		sYHoppingButton.addListener(SWT.Selection, this);
 		sYHoppingButton.setSelection(model.getChanneltypes()[2]);
 		
 		analyzeCurrentButton = new Button(main, SWT.CHECK);
 		analyzeCurrentButton.setText("Analyze Current Frequency Allocation");
 		analyzeCurrentButton.addListener(SWT.Selection, this);
 		analyzeCurrentButton.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false,3 ,1));
 		analyzeCurrentButton.setSelection(model.isAnalyzeCurrentFreqAllocation());
 		
 		summaryGroup = new Group(main, SWT.NONE);
 		summaryGroup.setLayout(new GridLayout(6, false));
 		summaryGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true,3 ,1));
 		summaryGroup.setText("Summary");
 		
		String[] headers = { " ", "Total", "900", "1800", "850", "1900"};
 	    for (int i = 0; i < headers.length; i++) {
 	    	Label label = new Label(summaryGroup, SWT.LEFT);
 	    	label.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1 , 1));
 	    	label.setText(headers[i]);
 	    	AfpWizardUtils.makeFontBold(label);
 	    }
 	    
 	    setPageComplete(true);
 		setControl (thisParent);
 		
 	}
 	
 	@Override
     public void setVisible(boolean visible) {
         
         super.setVisible(visible);
     }
 	
 	 public boolean canFlipToNextPage(){
   	   if (isValidPage())
   	        return true;
   	    return false;
   	}
 
 
   /**
    * Checks if is valid page.
    * 
    * @return true, if is valid page
    */
   protected boolean isValidPage() {
       
       return true;
   }
   
 
 	@Override
 	public void handleEvent(Event event) {
 		model.setOptimizeFrequency(frequenciesButton.getSelection());
 		model.setOptimizeBSIC(bsicButton.getSelection());
 		model.setOptimizeHSN(hsnButton.getSelection());
 		model.setOptimizeMAIO(maioButton.getSelection());
 		model.setFrequencyBands(new boolean[]{freq900Button.getSelection(), freq1800Button.getSelection(), freq850Button.getSelection(), freq1900Button.getSelection()});
 		model.setChanneltypes(new boolean[]{bcchButton.getSelection(), bbHoppingButton.getSelection(), sYHoppingButton.getSelection()});
 		model.setAnalyzeCurrentFreqAllocation(analyzeCurrentButton.getSelection());
 		
 	}
 	
 	public void refreshPage(){
 		
 		String rowHeaders[] = AfpModel.GOALS_SUMMARY_ROW_HEADERS;
 
 	    int[][] items = model.getSelectedCount();
 	    
 	    for (int i = 0; i < rowHeaders.length; i++){
 	    	new Label(summaryGroup, SWT.LEFT).setText(rowHeaders[i]);
 	    	Label totalLabel = new Label(summaryGroup, SWT.LEFT);
 	    	int total = 0;
 	    	for (int j = 0; j < items[i].length; j++){
 	    		new Label(summaryGroup, SWT.LEFT).setText(Integer.toString(items[i][j]));
 	    		total += items[i][j];
 	    	}
 	    	totalLabel.setText(Integer.toString(total));
 	    }
 	    
 	    summaryGroup.layout();
 	    
 	    frequenciesButton.setSelection(model.isOptimizeFrequency());
 	    bsicButton.setSelection(model.isOptimizeBSIC());
 	    hsnButton.setSelection(model.isOptimizeHSN());
 	    maioButton.setSelection(model.isOptimizeMAIO());
 	    freq900Button.setSelection(model.getFrequencyBands()[0]);
 	    freq1800Button.setSelection(model.getFrequencyBands()[1]);
 	    freq850Button.setSelection(model.getFrequencyBands()[2]);
 	    freq1900Button.setSelection(model.getFrequencyBands()[3]);
 	    bcchButton.setSelection(model.getChanneltypes()[0]);
 	    bbHoppingButton.setSelection(model.getChanneltypes()[1]);
 	    sYHoppingButton.setSelection(model.getChanneltypes()[2]);
 	    analyzeCurrentButton.setSelection(model.isAnalyzeCurrentFreqAllocation());
 	    
 	}
 
 	@Override
 	public IWizardPage getPreviousPage() {
 		return null;
 	}
 	
 	
 
 }
