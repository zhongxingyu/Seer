 package org.amanzi.awe.afp.wizards;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Vector;
 
 import org.amanzi.awe.afp.filters.AfpColumnFilter;
 import org.amanzi.awe.afp.filters.AfpRowFilter;
 import org.amanzi.awe.afp.models.AfpDomainModel;
 import org.amanzi.awe.afp.models.AfpFrequencyDomainModel;
 import org.amanzi.awe.afp.models.AfpModel;
 import org.amanzi.awe.console.AweConsolePlugin;
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser;
 import org.neo4j.graphdb.Traverser.Order;
 
 public class AfpFrequencyTypePage extends AfpWizardPage implements FilterListener{
 	
 	private Group frequencyDomainsGroup;
 	protected Label free900Freq;
 	protected Label free900Trx;
 	protected Label free1800Freq;
 	protected Label free1800Trx;
 	
 	protected Label bcch900Freq;
 	protected Label bcch900Trx;
 	protected Label bcch1800Freq;
 	protected Label bcch1800Trx;
 	
 	protected Label tch900Freq;
 	protected Label tch900Trx;
 	protected Label tch1800Freq;
 	protected Label tch1800Trx;
 	
 	protected static HashMap<String, Label[]> domainLabels;
 	private final String[] headers = { "BSC", "Site", "Sector", "Layer", "Subcell", "TRX_ID", "Band", "Extended", "Hopping Type", "BCCH"};
 	private final String[] prop_name = { "bsc", "Site", "Sector", "Layer", 
 			"Subcell", "trx_id", "band", "Extended", "hopping_type", INeoConstants.PROPERTY_BCCH_NAME};
 	
 	private Table filterTable;
 	protected AfpRowFilter rowFilter;
 	private int trxCount;
 
 	public AfpFrequencyTypePage(String pageName, AfpModel model, String desc) {
 		super(pageName, model);
         setTitle(AfpImportWizard.title);
         setDescription(desc);
         setPageComplete (false);
         rowFilter = new AfpRowFilter();
 	}
 	
 	@Override
 	public void createControl(Composite parent) {
 		final  Shell parentShell = parent.getShell();
 		Composite thisParent = new Composite(parent, SWT.NONE);
    	 	thisParent.setLayout(new GridLayout(2, false));
 		
 		Group stepsGroup = AfpWizardUtils.getStepsGroup(thisParent, 3);
 		
 		Group main = new Group(thisParent, SWT.NONE);
 		main.setLayout(new GridLayout(1, true));
 		main.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1 ,2));
 		
 		frequencyDomainsGroup = new Group(main, SWT.NONE);
 		frequencyDomainsGroup.setLayout(new GridLayout(4, true));
 		frequencyDomainsGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true,1 ,10));
 		frequencyDomainsGroup.setText("Frequency Type Domains");
 		
 		Label domainsLabel = new Label(frequencyDomainsGroup, SWT.LEFT);
 		domainsLabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1 , 1));
 		domainsLabel.setText("Domains");
     	AfpWizardUtils.makeFontBold(domainsLabel);
     	
     	Label frequenciesLabel = new Label(frequencyDomainsGroup, SWT.LEFT);
     	frequenciesLabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1 , 1));
     	frequenciesLabel.setText("Assigned Frequencies");
     	AfpWizardUtils.makeFontBold(frequenciesLabel);
     	
     	Label trxsLabel = new Label(frequencyDomainsGroup, SWT.LEFT);
     	trxsLabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1 , 1));
     	trxsLabel.setText("Assigned TRXs");
     	AfpWizardUtils.makeFontBold(trxsLabel);
     	
     	AfpWizardUtils.createButtonsGroup(this, frequencyDomainsGroup, "FrequencyType", model);
     	domainLabels = new HashMap<String, Label[]>();
     	
     	filterTable = this.addTRXFilterGroup(main, headers,10, false, this);
     	
 
 		
     	setPageComplete(true);
     	setControl (thisParent);    	
 	}
 	
 	public void loadData() {
 		if(filterTable != null) {
 			filterTable.removeAll();
 			
 			HashMap<String, String> bandFilters = new HashMap<String, String> ();
 			for (int i = 0; i < model.getFrequencyBands().length; i++){
 				if (model.getFrequencyBands()[i])
 					if (bandFilters.get("band") == null)
 						bandFilters.put("band", model.BAND_NAMES[i]);
 					else
 						bandFilters.put("band", bandFilters.get("band") + "," + model.BAND_NAMES[i]);
 			}
 			
 		    Traverser sectorTraverser = model.getTRXList(bandFilters);
 		    
 		    trxCount =0;
 		    for (Node node : sectorTraverser) {
 		    	Traverser trxTraverser = node.traverse(Order.DEPTH_FIRST, StopEvaluator.DEPTH_ONE, new ReturnableEvaluator(){
 
 					@Override
 					public boolean isReturnableNode(TraversalPosition currentPos) {
 						if (currentPos.currentNode().getProperty(INeoConstants.PROPERTY_TYPE_NAME,"").equals(NodeTypes.TRX.getId())){
 							return true;
 						}
 							
 						return false;
 					}
 		    		
 		    	}, NetworkRelationshipTypes.CHILD, Direction.OUTGOING);
 		    	
 		    	for (Node trxNode: trxTraverser){
			    	if (rowFilter != null){
 			    		if (!rowFilter.equal(trxNode)) 
 			    			continue;
 			    	}
 			    	if(trxCount <= 100){ 
 				    	TableItem item = new TableItem(filterTable, SWT.NONE);
 				    	for (int j = 0; j < headers.length; j++){
 				    		String val = "";
 				    		try {
 				    			if (prop_name[j].equals("bsc")){
 				    				val = (String)node.getProperty(prop_name[j], "bsc");
 				    			}
 				    			else if (prop_name[j].equals("Site")){
 				    				Node siteNode = node.getSingleRelationship(NetworkRelationshipTypes.CHILD, Direction.INCOMING).getStartNode();
 				    				if (siteNode.getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals("site"))
 				    					val = (String)siteNode.getProperty(INeoConstants.PROPERTY_NAME_NAME, "");
 				    			}
 				    			
 				    			else if (prop_name[j].equals("Sector")){
 				    				val = (String)node.getProperty(INeoConstants.PROPERTY_NAME_NAME, "");
 				    			}
 				    			
 				    			else if (prop_name[j].equals("trx_id")){
 				    				val = Integer.toString((Integer)trxNode.getProperty(prop_name[j], "0"));
 				    			}
 				    			else if (prop_name[j].equals("band")){
 				    				val = (String)trxNode.getProperty(prop_name[j], "");
 				    			}
 				    			else if (prop_name[j].equals("Extended")){
 				    				val = (String)node.getProperty(prop_name[j], "NA");
 				    			}
 				    			else if (prop_name[j].equals("hopping_type")){
 				    				int type = (Integer)trxNode.getProperty(prop_name[j], 0);
 				    				val = type == 0 ? "Non" : "SY";
 				    			}
 				    			else if (prop_name[j].equals(INeoConstants.PROPERTY_BCCH_NAME)){
 				    				boolean bcch = (Boolean)trxNode.getProperty(prop_name[j], false);
 				    				val = bcch ? "1" : "0";
 				    			}
 				    			else 
 				    				val = (String)node.getProperty(prop_name[j], "");
 				    			item.setText(j, val);
 				    		} catch(Exception e) {
 				    			item.setText(j, "");
 				    		}
 				    	}
 			    	}
 			    	trxCount++;
 		    	}
 		    }
 		    for (int i = 0; i < headers.length; i++) {
 		    	filterTable.getColumn(i).pack();
 		    }
 		    this.updateTRXFilterLabel(trxCount);
 		}
 	}
 	public static void deleteDomainLabels(String domainName){
 		if (domainLabels.containsKey(domainName)){
 			Label[] labels = domainLabels.get(domainName);
 			domainLabels.remove(domainName);
 			for (Label label : labels){
 				label.dispose();
 			}
 		}
 //		frequencyDomainsGroup.layout();
 	}
 	
 	public void refreshPage(){
 //		super.refreshPage();
 		
 		updateLabels();
 		loadData();
 		frequencyDomainsGroup.layout();
 	}
 	
 	public void updateLabels(){
 		for(Label[] labels:domainLabels.values()){
 			for (Label label : labels){
 				label.dispose();
 			}
 		}
 		domainLabels.clear();
 		
 		int counts[] = model.getFreqDomainsTrxCount(true);
 		int i=0;
 		for(AfpFrequencyDomainModel domainModel: model.getFreqDomains(true)) {
 			if(domainModel != null) {
 				Label freePlanLabel = new Label(frequencyDomainsGroup, SWT.LEFT);
 				freePlanLabel.setText(domainModel.getName());
 				Label frequenciesFreePlanLabel = new Label(frequencyDomainsGroup, SWT.RIGHT);
 				frequenciesFreePlanLabel.setText("" + domainModel.getCount());
 				Label TRXsFreePlanLabel = new Label(frequencyDomainsGroup, SWT.RIGHT);
 				TRXsFreePlanLabel.setText("" + domainModel.getNumTRX());//counts[i]);
 				domainLabels.put(domainModel.getName(), new Label[]{freePlanLabel, frequenciesFreePlanLabel, TRXsFreePlanLabel});
 			}
 			i++;
 		}
 		
 		frequencyDomainsGroup.layout();
 	}
 
 	@Override
 	public void onFilterSelected(String columnName, ArrayList<String> selectedValues) {
 		for(int i = 0; i < headers.length; i++){
 			if (headers[i].equals(columnName)){
 				columnName = prop_name[i];
 				break;
 			}
 		}
 		AfpColumnFilter colFilter = new AfpColumnFilter(columnName);
 		for (String value: selectedValues){
 			colFilter.addValue(value);
 		}
 		rowFilter.addColumn(colFilter);
 		loadData();
 		
 	}
 	
 	@Override
 	public void widgetDefaultSelected(SelectionEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		if (e.widget.getData().equals(AfpWizardPage.ASSIGN)){
 			final Shell subShell = new Shell(e.widget.getDisplay(), SWT.PRIMARY_MODAL);
 			subShell.setText("Assign to Domain");
 			subShell.setLayout(new GridLayout(2, false));
 			subShell.setLocation(200, 200);
 			
 			Label infoLabel = new Label (subShell, SWT.LEFT);
 			//TODO update label to show correct no. of TRXs
 			infoLabel.setText("Selected " + trxCount + " TRXs will be assigned to:");
 			infoLabel.setLayoutData(new GridData(GridData.FILL, SWT.LEFT, true, false,2 ,1));
 			
 			Label domainLabel = new Label (subShell, SWT.LEFT);
 			domainLabel.setText("Select Domain");
 			domainLabel.setLayoutData(new GridData(GridData.FILL, SWT.LEFT, true, false,2 ,1));
 			
 			final Combo domainCombo = new Combo(subShell, SWT.DROP_DOWN | SWT.READ_ONLY);
 			ArrayList<String> modelNames = new ArrayList<String>();
 			for (AfpFrequencyDomainModel dm : model.getFreqDomains(false)){
 				modelNames.add(dm.getName());
 			}
 			domainCombo.setItems(modelNames.toArray(new String[0]));
 			domainCombo.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 2, 1));
 			
 			Button selectButton = new Button(subShell, SWT.PUSH);
 			selectButton.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, false, false, 1, 1));
 			selectButton.setText("Assign");
 			selectButton.addSelectionListener(new SelectionAdapter(){
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					String domainName = domainCombo.getText();
 					AfpDomainModel freqModel = model.findDomainByName(model.DOMAIN_TYPES[0], domainName);
 					freqModel.setFilters(rowFilter.toString());
 					freqModel.setNumTRX(trxCount);
 					updateLabels();
 					subShell.dispose();
 				}
 			});
 			
 			Button cancelButton = new Button(subShell, SWT.PUSH);
 			cancelButton.setLayoutData(new GridData(GridData.END, GridData.BEGINNING, false, false, 1, 1));
 			cancelButton.setText("Cancel");
 			cancelButton.addSelectionListener(new SelectionAdapter(){
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					subShell.dispose();
 				}
 			});
 			
 			subShell.pack();
 			subShell.open();
 		}
 		
 		else if (e.widget.getData().equals(AfpWizardPage.CLEAR)){
 			rowFilter.clear();
 			loadData();
 		}
 			
 	}
 
 }
