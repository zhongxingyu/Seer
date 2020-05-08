 package org.amanzi.awe.afp.wizards;
 
 import java.util.HashMap;
 
 import org.amanzi.awe.afp.models.AfpModel;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 public class AfpAvailableResourcesPage extends AfpWizardPage implements Listener {
 	
 	protected Text frequencies900;
 	protected Text frequencies1800;
 	protected Text frequencies850;
 	protected Text frequencies1900;
 	protected Button ncc[] = new Button[8];
 	protected Button bcc[] = new Button[8];
 	
 	private Label freq900Label;
 	private Label freq1800Label;
 	private Label freq850Label;
 	private Label freq1900Label;
 	private Button button900;
 	private Button button1800;
 	private Button button850;
 	private Button button1900;
 	
 	
 
 	public AfpAvailableResourcesPage(String pageName, AfpModel model, String desc) {
 		super(pageName, model);
         setTitle(AfpImportWizard.title);
         setDescription(desc);
         setPageComplete (false);
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		final  Shell parentShell = parent.getShell();
 		Composite thisParent = new Composite(parent, SWT.NONE);
    	 	thisParent.setLayout(new GridLayout(2, false));
 		
 		Group stepsGroup = AfpWizardUtils.getStepsGroup(thisParent, 2);
 		
 		Group main = new Group(thisParent, SWT.NONE);
 		main.setLayout(new GridLayout(1, true));
 		main.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1 ,2));
 		
 		Group frequenciesGroup = new Group(main, SWT.NONE);
 		frequenciesGroup.setLayout(new GridLayout(3, false));
 		frequenciesGroup.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false,1 ,5));
 		frequenciesGroup.setText("Frequencies");
 		
 		Label bandLabel = new Label(frequenciesGroup, SWT.LEFT);
 		bandLabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false, 3 , 1));
 		bandLabel.setText("Band");
     	AfpWizardUtils.makeFontBold(bandLabel);
 		
     	freq900Label = new Label (frequenciesGroup, SWT.LEFT);
     	freq900Label.setText("900: ");
     	frequencies900 = new Text (frequenciesGroup, SWT.BORDER | SWT.SINGLE);
     	frequencies900.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
     	frequencies900.addModifyListener(new ModifyListener(){
 
 			@Override
 			public void modifyText(ModifyEvent e) {
 
 				model.setAvailableFreq(AfpModel.BAND_900,frequencies900.getText());
 				button900.setSelection(false);
 			
 				setPageComplete(canFlipToNextPage());
 					
 			}
     		
     	});
     	
     	button900 = new Button(frequenciesGroup, GridData.END);
     	button900.setText("...");
     	button900.addSelectionListener(new SelectionAdapter(){
     		
     		
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				AfpWizardUtils.createFrequencySelector(parentShell, frequencies900, model.getFrequencyArray(AfpModel.BAND_900));
 				
 			}
     		
     	});
     	
     	freq1800Label = new Label (frequenciesGroup, SWT.LEFT);
     	freq1800Label.setText("1800: ");
     	frequencies1800 = new Text (frequenciesGroup, SWT.BORDER | SWT.SINGLE);
     	frequencies1800.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
     	frequencies1800.addModifyListener(new ModifyListener(){
 
 			@Override
 			public void modifyText(ModifyEvent e) {
 					model.setAvailableFreq(AfpModel.BAND_1800,frequencies1800.getText());
 				button1800.setSelection(false);
 				setPageComplete(canFlipToNextPage());	
 			}
     		
     	});
 		
     	button1800 = new Button(frequenciesGroup, GridData.END);
     	button1800.setText("...");
     	button1800.addSelectionListener(new SelectionAdapter(){
     		
     		
     		@Override
 			public void widgetSelected(SelectionEvent e) {
 				AfpWizardUtils.createFrequencySelector(parentShell, frequencies1800, model.getFrequencyArray(AfpModel.BAND_1800));				
 			}
     		
     	});
     	
     	freq850Label = new Label (frequenciesGroup, SWT.LEFT);
     	freq850Label.setText("850: ");
     	frequencies850 = new Text (frequenciesGroup, SWT.BORDER | SWT.SINGLE);
     	frequencies850.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
     	frequencies850.addModifyListener(new ModifyListener(){
 
 			@Override
 			public void modifyText(ModifyEvent e) {
 					model.setAvailableFreq(AfpModel.BAND_850,frequencies850.getText());
 				button850.setSelection(false);
 		
 				setPageComplete(canFlipToNextPage());
 					
 			}
     		
     	});
 		
     	button850 = new Button(frequenciesGroup, GridData.END);
     	button850.setText("...");
     	button850.addSelectionListener(new SelectionAdapter(){
 			
     		@Override
 			public void widgetSelected(SelectionEvent e) {
 				AfpWizardUtils.createFrequencySelector(parentShell, frequencies850, model.getFrequencyArray(AfpModel.BAND_850));
 			}
     		
     	});
     	
     	freq1900Label = new Label (frequenciesGroup, SWT.LEFT);
     	freq1900Label.setText("1900: ");
     	frequencies1900 = new Text (frequenciesGroup, SWT.BORDER | SWT.SINGLE);
     	frequencies1900.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
     	frequencies1900.addModifyListener(new ModifyListener(){
 
 			@Override
 			public void modifyText(ModifyEvent e) {
 				model.setAvailableFreq(AfpModel.BAND_1900, frequencies1900.getText());
 				
 				button1900.setSelection(false);
 				setPageComplete(canFlipToNextPage());
 					
 			}
     		
     	});
 		
     	button1900 = new Button(frequenciesGroup, GridData.END);
     	button1900.setText("...");
     	button1900.addSelectionListener(new SelectionAdapter(){
     		
     		
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				AfpWizardUtils.createFrequencySelector(parentShell, frequencies1900, model.getFrequencyArray(AfpModel.BAND_1900));
 				button1900.setSelection(false);
 			}
     		
     	});
     	
     	
     	/** Create BSIC Group*/
     	Group bsicGroup = new Group(main, SWT.NONE);
     	bsicGroup.setLayout(new GridLayout(9, false));
     	bsicGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false,1 ,5));
     	bsicGroup.setText("BSIC");
     	
     	String[] headers = { " ", "0", "1", "2", "3", "4", "5", "6", "7"};
 	    for (int i = 0; i < headers.length; i++) {
 	    	new Label(bsicGroup, GridData.BEGINNING).setText(headers[i]);
 	    }
 		
 	    new Label(bsicGroup, GridData.BEGINNING).setText("Available NCCs: ");
 	    for(int i=0; i< ncc.length;i++) {
 		    ncc[i] = new Button (bsicGroup, SWT.CHECK);
 		    ncc[i].addListener(SWT.Selection, this);
 		    ncc[i].setSelection(true);
 	    }
 	    
 	    new Label(bsicGroup, GridData.BEGINNING).setText("Available BCCs: ");
 	    for(int i=0; i< ncc.length;i++) {
 		    bcc[i] = new Button (bsicGroup, SWT.CHECK);
 		    bcc[i].addListener(SWT.Selection, this);
 		    bcc[i].setSelection(true);
 	    }
 	    
    	
 	    setPageComplete(true);
 		setControl (thisParent);
 		
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
 	      for(String band : AfpModel.BAND_NAMES) {
 	    	  Text txt = null;
 	    	  if(band.equals("900"))
 	    		  txt = frequencies900;
 	    	  if(band.equals("1800"))
 	    		  txt = frequencies1800;
 	    	  if(band.equals("850"))
 	    		  txt = frequencies850;
 	    	  if(band.equals("1900"))
 	    		  txt = frequencies1900;
 	    	  if (txt != null){
 	    		  if (txt.isEnabled()){
 	    		
 			    	  String text = model.getAvailableFreq(band);
 			    	  if (text.trim().equals("")){
 						setErrorMessage("Select frequencies for " + band + " band");
 						return false;
 			    	  }
 			    	  for(String frequencyGroups : text.split(",")){
 			    		  for (String frequencies : frequencyGroups.split("-")){
 			    			  try{
 			    				  Integer.parseInt(frequencies);
 			    			  }catch(NumberFormatException nfe){
 			    				  setErrorMessage("Only Numeric frequencies are allowed");
 			    				  return false;
 			    			  }
 			    		  }
 			    	  }
 			    	  
 	    		  }
 	    	  }
 	      }
 	      
 	      
 	      setErrorMessage(null);
 	      return true;
 	  }
 	  
 
 	@Override
 	public void handleEvent(Event event) {
 		boolean[] availableNCCs = new boolean[ncc.length];
 		boolean[] availableBCCs = new boolean[bcc.length];
 
 		for(int i=0;i< ncc.length;i++) {
 			availableNCCs[i] = ncc[i].getSelection();
 		}
 		for(int i=0;i< bcc.length;i++) {
 			availableBCCs[i] = bcc[i].getSelection();
 		}
 		model.setAvailableNCCs(availableNCCs);
 		model.setAvailableBCCs(availableBCCs);
 	}
 	
 	@Override
 	public void refreshPage(){
 		frequencies900.setEnabled(model.getFrequencyBands()[0]);
 		freq900Label.setEnabled(model.getFrequencyBands()[0]);
 		button900.setEnabled(model.getFrequencyBands()[0]);
 
 		frequencies1800.setEnabled(model.getFrequencyBands()[1]);
 		freq1800Label.setEnabled(model.getFrequencyBands()[1]);
 		button1800.setEnabled(model.getFrequencyBands()[1]);
 
 		frequencies850.setEnabled(model.getFrequencyBands()[2]);
 		freq850Label.setEnabled(model.getFrequencyBands()[2]);
 		button850.setEnabled(model.getFrequencyBands()[2]);
 
 		frequencies1900.setEnabled(model.getFrequencyBands()[3]);
 		freq1900Label.setEnabled(model.getFrequencyBands()[3]);
 		button1900.setEnabled(model.getFrequencyBands()[3]);
 
 		boolean[] availableNCCs = model.getAvailableNCCs();
 		boolean[] availableBCCs = model.getAvailableBCCs();
 
 		boolean bsicEnable = model.isOptimizeBSIC();
 		if(availableNCCs != null) {
 			for(int i=0;i< ncc.length;i++) {
 				if(bsicEnable) {
 					ncc[i].setEnabled(true);
 					ncc[i].setSelection(availableNCCs[i]);
 				} else {
 					ncc[i].setEnabled(false);
 				}
 			}
 		}
 		if(availableBCCs != null) {
 			for(int i=0;i< ncc.length;i++) {
 				if(bsicEnable) {
 					bcc[i].setEnabled(true);
 					bcc[i].setSelection(availableBCCs[i]);
 				} else {
 					bcc[i].setEnabled(false);
 				}
 			}
 		}
     	frequencies900.setText(model.getAvailableFreq(AfpModel.BAND_900));
     	frequencies1800.setText(model.getAvailableFreq(AfpModel.BAND_1800));
     	frequencies850.setText(model.getAvailableFreq(AfpModel.BAND_850));
     	frequencies1900.setText(model.getAvailableFreq(AfpModel.BAND_1900));
 	}
 
 }
