 package org.tekkotsu.wizards;
 
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Text;
 import org.tekkotsu.api.BehaviorWriter;
 import org.tekkotsu.api.DefaultClassReader;
 import org.tekkotsu.api.MapBuilderWizard;
 import org.tekkotsu.api.MapBuilderWriter;
 import org.tekkotsu.api.MapRequestObject;
 import org.tekkotsu.api.NodeClass;
 import org.tekkotsu.api.NodeInstance;
 import org.tekkotsu.api.Parameter;
 import org.tekkotsu.api.TransitionClass;
 import org.tekkotsu.api.TransitionInstance;
 
 public class TransWizardPage extends WizardPage {
 
 	//Data structures
 	Map nodeMap = new HashMap<String, NodeInstance>();
 	Map transMap = new HashMap<String, TransitionClass>();
 	
 	//Components
 	private Composite container;
 	private Label lTrans, lNodes, lParameter, lSources, lTargets;
 	private Text parameter ;
 	private Combo transCombo, nodesCombo;
 	private Button addSource, addTarget;
 	private List sources, targets;
 	private DefaultClassReader reader;
 	
 	public TransWizardPage() {
 		super("Transition Instance Wizard");
 		setTitle("New Transition Instance");
 		setDescription("This wizard helps you create a new transition instance for the setup machine of your behavior.");
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 	
 		//Composite for the wizardpage
 	    container = new Composite(parent, SWT.NONE);
 	    GridLayout layout = new GridLayout();
 	    container.setLayout(layout);
 	    layout.numColumns = 2;
 	    layout.makeColumnsEqualWidth = true;	  
 	  
 	    //First row
 	    lTrans = new Label(container, SWT.NONE);
 	    lTrans.setText("Transitions");
 	    lTrans.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1,1));
 
 	    lNodes = new Label(container, SWT.NONE);
 	    lNodes.setText("Nodes");
 	    lNodes.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1,1));
 	    
 	    //Second Row
 	    transCombo = new Combo(container, SWT.READ_ONLY);
 	    try {
 			reader = new DefaultClassReader();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	
 	    for(int i = 0; i < reader.getTransitions().size(); i++){
 	    	transCombo.add(reader.getTransitions().get(i).getName());
 	    	transMap.put(reader.getTransitions().get(i).getName(), reader.getTransitions().get(i));
 	    }
     
 	    transCombo.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 1, 1));
 	    
 	    nodesCombo = new Combo(container, SWT.READ_ONLY);
 	    try {
 			reader = new DefaultClassReader();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	    
 	    ArrayList<NodeInstance> nInstances = composer.ClassView.getNodeClass().getSetupMachine().getNodes();
 	
 	    for(int i = 0; i < nInstances.size(); i++){
 	    	nodesCombo.add(nInstances.get(i).getLabel());
 	    	nodeMap.put(nInstances.get(i).getLabel(), nInstances.get(i));
 	    }
     
 	    nodesCombo.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 1, 1));
 	    
 	    //Third Row
 	    lParameter = new Label(container, SWT.NONE);
 	    lParameter.setText("Parameter");
 	    lParameter.setLayoutData(new GridData(SWT.BEGINNING, SWT.BOTTOM, false, false, 1,1));
 	    
 	    addSource = new Button(container, SWT.PUSH);
 	    addSource.setText("ADD Source");
 	    addSource.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1,1));
 	    
 	    //Fourth Row
 	    parameter = new Text(container, SWT.BORDER);
 	    parameter.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1,1));
 	    
 	    addTarget = new Button(container, SWT.PUSH);
 	    addTarget.setText("ADD Target");
 	    addTarget.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1,1));
 	    
 	    //Fifth row
 	    lSources = new Label(container, SWT.NONE);
 	    lSources.setText("Sources");
 	    lSources.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1,1));
 
 	    lTargets = new Label(container, SWT.BORDER);
 	    lTargets.setText("Targets");
 	    lTargets.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false, 1,1));
 
 	    //Sixth Row
 	    sources = new List(container, SWT.BORDER);
 	    sources.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,1));
 	    
 	    targets = new List(container, SWT.BORDER);
 	    targets.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,1));
 	    
 		//Operation for the add source button.
 		addSource.addSelectionListener(new SelectionAdapter(){
 			
 			@Override
 			public void widgetSelected(SelectionEvent e){
 					
 				if(!nodesCombo.getText().isEmpty()){
 					sources.add(nodesCombo.getText());
 				}
 				
 				//set the finish button active if there is a source and a target
 				if(sources.getItems().length > 0 && targets.getItems().length > 0){
 					setPageComplete(true);
 				}
 						
 			}
 					
 		});	    
 		
 		//Operation for the add target button.
 		addTarget.addSelectionListener(new SelectionAdapter(){
 			
 			@Override
 			public void widgetSelected(SelectionEvent e){
 					
 				if(!nodesCombo.getText().isEmpty()){
 					targets.add(nodesCombo.getText());
 				}
 				
 				//set the finish button active if there is a source and a target
 				if(sources.getItems().length > 0 && targets.getItems().length > 0){
 					setPageComplete(true);
 				}
 						
 			}
 					
 		});	    
 		
 	    
    
     // Required to avoid an error in the system
     setControl(container);
     setPageComplete(false);
 
 	}
 	
 	//Methods to return results
 	public TransitionInstance getTransition(){
 		
 		//Create instance
 		TransitionInstance instance = new TransitionInstance((TransitionClass)transMap.get(transCombo.getText()));
 		
 		//If there is a parameter add it to the instance
 		if(parameter.getVisible() && !parameter.getText().isEmpty())
 			instance.getParameters().get(0).setValue(parameter.getText()); //TODO
 		
 		//Set the sources
 		for(int i = 0; i < sources.getItems().length; i++){
			instance.addSource((NodeInstance)transMap.get(sources.getItems()[i]));
 		}
 		
 		//Set the targets
 		for(int i = 0; i < targets.getItems().length; i++){
			instance.addSource((NodeInstance)transMap.get(sources.getItems()[i]));
 		}
 		
 		
 		return instance;
 		
 	}
 
 	
  
 }
 
