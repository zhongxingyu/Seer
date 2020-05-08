 package org.amanzi.awe.afp.wizards;
 
 import java.util.Collection;
 
 import org.amanzi.awe.afp.models.AfpDomainModel;
 import org.amanzi.awe.afp.models.AfpFrequencyDomainModel;
 import org.amanzi.awe.afp.models.AfpModel;
 import org.amanzi.awe.afp.services.DomainRelations;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
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
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.Traverser.Order;
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.ui.NeoUtils;
 import org.amanzi.neo.services.utils.Pair;
 
 public class AfpFrequencySelector extends AfpDomainSelector{
 	AfpDomainModel domainModel;
 	Combo bandCombo;
 	List selectedList;
 	List freqList; 
 	Label selectionLabel;
 	private static String[] selectedArray;
 	int bandIndexes[];
 	Button leftArrowButton;
 	Button rightArrowButton;
 
 	
 	public AfpFrequencySelector(final WizardPage page, Shell parentShell, final String action, final Group parentGroup, final AfpModel model){
 		super( page, parentShell, parentGroup, model);
 		createUI(action, " Frequency Domain", model.getAllFrequencyDomainNames());
 		bandIndexes = model.getAvailableFrequencyBandsIndexs();
 
 		int selectedBand =0;
 
 
 		if (action.equals("Edit") || action.equals("Delete") || action.equals("Clear")){
 			for(AfpFrequencyDomainModel d: model.getFreqDomains(false)) {
 			    
 				this.domain2Edit = d;
 				break;
 			}
 		}else {
 			domainModel = new AfpFrequencyDomainModel();
 		}
 		
 		freqGroup = new Group(subShell, SWT.NONE);
 		freqGroup.setLayout(new GridLayout(3, false));
 		freqGroup.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false,3 ,1));
 		freqGroup.setText("Frequency Selector");
 		
 		Label bandLabel = new Label(freqGroup, SWT.LEFT);
 		bandLabel.setText("Band");
 		bandLabel.setLayoutData(new GridData(GridData.FILL, SWT.LEFT, true, false,3 ,1));
 		
 		bandCombo = new Combo(freqGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
 		bandCombo.setItems(model.getAvailableBands());
 		bandCombo.setLayoutData(new GridData(GridData.FILL, SWT.LEFT, true, false,3 ,1));
 		bandCombo.addSelectionListener(new SelectionListener(){
 
 				@Override
 				public void widgetDefaultSelected(SelectionEvent e) {
 					widgetSelected(e);					
 				}
 
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					if(domain2Edit != null) {
 						((AfpFrequencyDomainModel)domain2Edit).setBand(AfpModel.BAND_NAMES[bandIndexes[bandCombo.getSelectionIndex()]]);
 						((AfpFrequencyDomainModel)domain2Edit).setFrequencies(null);
 					}
 					updateFreqList();
 				}
 				
 		});
 		bandCombo.select(selectedBand);
 		
 		Label freqLabel = new Label (freqGroup, SWT.LEFT);
 		freqLabel.setText("Frequencies");
 		freqLabel.setLayoutData(new GridData(GridData.FILL, SWT.LEFT, true, false,2 ,1));
 		
 		selectionLabel = new Label (freqGroup, SWT.LEFT);
 		selectionLabel.setText("0 Frequencies Selected");
 		selectionLabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false,1 ,1));
 
 		//updateFreqList();
 		createListSelector(new String[0],new String[0]);
 		updateFreqList();
 		if (action.equals("Delete")||action.equals("Clear")){
 			bandCombo.setEnabled(false);
 			selectedList.setEnabled(false);
 			freqList.setEnabled(false);
 			this.leftArrowButton.setEnabled(false);
 			this.rightArrowButton.setEnabled(false);
 		}
 
 		super.addButtons(action);
 	 }
 
 	void updateFreqList() {
 		//TODO update the frequencies on basis of selection
 		//String leftFreq[] = model.getFrequencyArray(bandIndexes[0]);
 		String frequencies[];
 		if(this.domain2Edit == null) {
 			frequencies = AfpModel.rangeArraytoArray(model.getAvailableFreq(bandCombo.getItem(bandCombo.getSelectionIndex())).split(","));
 			selectedList.removeAll();
 		} else {
 			//select the band
 			for(int i=0;i< bandIndexes.length;i++) {
 				if(AfpModel.BAND_NAMES[bandIndexes[i]].compareTo(((AfpFrequencyDomainModel)domain2Edit).getBand())==0) {
 					bandCombo.select(i);
 					break;
 				}
 			}
 			frequencies = AfpModel.rangeArraytoArray(model.getAvailableFreq(bandCombo.getItem(bandCombo.getSelectionIndex())).split(","));
 
 			selectedList.removeAll();
 			Pair<String[], String[]> p = AfpModel.convertFreqString2Array(((AfpFrequencyDomainModel)domain2Edit).getFrequenciesAsString(),frequencies);
 			String[] selected = p.getLeft();
 			frequencies = p.getRight();
 			if(selected.length >0) {
 				selectedList.setItems(selected);
 			}
 		}
 		
 		freqList.setItems(frequencies);
 	}
 	public void createListSelector(String[] leftList, String[] rightList){
 		
 		int numSelected = 0;
 		final Label thisSelectionLabel = selectionLabel;
 		freqList = new List(freqGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
 		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true, true, 1, 2);
 		int listHeight = freqList.getItemHeight() * 12;
 		int listWidth = selectionLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
 		Rectangle trim = freqList.computeTrim(0, 0, 0, listHeight);
 		gridData.heightHint = trim.height;
 		gridData.widthHint = listWidth;
 		freqList.setLayoutData(gridData);
 		//freqList.setItems(leftList);
 		
 		rightArrowButton = new Button (freqGroup, SWT.ARROW | SWT.RIGHT | SWT.BORDER);
 		GridData arrowGridData = new GridData(GridData.FILL, GridData.END, true, false,1 ,1);
 		arrowGridData.verticalIndent = trim.height/2;
 		rightArrowButton.setLayoutData(arrowGridData);
 		
 		
 		selectedList = new List(freqGroup, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
 		gridData = new GridData(GridData.FILL, GridData.FILL, true, true, 1, 2);
 		gridData.heightHint = trim.height;
 		gridData.widthHint = listWidth;
 		selectedList.setLayoutData(gridData);
 		selectedList.setItems(AfpModel.rangeArraytoArray(rightList));
 		
 		rightArrowButton.addSelectionListener(new SelectionAdapter(){
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (freqList.getSelectionCount() > 0){
 					String selectedNew[] = freqList.getSelection();
 					for (String item: selectedNew){//int i = 0; i < selectedNew.length; i++){
 						selectedList.add(item);
 						freqList.remove(item);
 					}
 					selectedArray = selectedList.getItems();
 					selectedList.setItems(selectedArray);
 					thisSelectionLabel.setText("" + selectedArray.length + " Frequencies selected");
 				}
 				
 			}
 		});
 		
 		
 		leftArrowButton = new Button (freqGroup, SWT.ARROW | SWT.LEFT | SWT.BORDER);
 		leftArrowButton.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false,1 ,1));
 		leftArrowButton.addSelectionListener(new SelectionAdapter(){
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (selectedList.getSelectionCount() > 0){
 					String deSelected[] = selectedList.getSelection();
 					for (String item: deSelected){//int i = 0; i < deSelected.length; i++){
 						freqList.add(item);
 						selectedList.remove(item);
 					}
 					selectedArray = selectedList.getItems();
 					String array[] = AfpModel.rangeArraytoArray(selectedArray);
 					thisSelectionLabel.setText("" + array.length + " Frequencies selected");
 					String notSelected[] = AfpModel.rangeArraytoArray(freqList.getItems());
 					freqList.setItems(notSelected);
 				}
 				
 			}
 		});
 	}
 	protected void handleDomainNameSection(int selection, String name) {
 		int j=0;
 		for(AfpFrequencyDomainModel d: model.getFreqDomains(false)) {
 			if(j ==selection) {
 			    
 				domain2Edit = d;
 				break;
 			}
 			j++;
 		}
 		updateFreqList();
 	}
 	protected boolean handleAddDomain() {
 		if(domainName == null || bandCombo.getText() == null || selectedArray == null) {
 			return false;
 		}
 		if(domainName.trim().length() == 0 || bandCombo.getText().trim().length() == 0 || selectedArray.length == 0) {
 			return false;
 		}
 		
 		((AfpFrequencyDomainModel)domainModel).setName(domainName);
 		((AfpFrequencyDomainModel)domainModel).setBand(bandCombo.getText());
 		((AfpFrequencyDomainModel)domainModel).setFrequencies(selectedArray);
 		model.addFreqDomain(((AfpFrequencyDomainModel)domainModel));
 		return true;
 	}
 	protected void handleEditDomain() {
 		selectedArray = selectedList.getItems();
 		((AfpFrequencyDomainModel)domain2Edit).setFrequencies(selectedArray);
 		model.editFreqDomain(((AfpFrequencyDomainModel)domain2Edit));
 	}
 	protected void handleClearDomain() throws InterruptedException{
 	    
 	    model.removeFrequencyDomainFromQueue((AfpFrequencyDomainModel)domain2Edit);
         domain2Edit.setFilters(null);
         domain2Edit.setNumTRX(0);
         model.editFreqDomain((AfpFrequencyDomainModel)domain2Edit);
         
         Job job = new Job("ClearDomain"){
 
             @Override
             protected IStatus run(IProgressMonitor monitor) {
                 // TODO Auto-generated method stub
                 if (model.getAfpNode().hasRelationship(DomainRelations.DOMAINS, Direction.OUTGOING)){
                     Collection<Node> tr = model.getAfpNode().getSingleRelationship(DomainRelations.DOMAINS, Direction.OUTGOING).getEndNode()
                     .traverse(Order.BREADTH_FIRST,
                             StopEvaluator.END_OF_GRAPH,
                             ReturnableEvaluator.ALL_BUT_START_NODE,
                             DomainRelations.NEXT,
                             Direction.OUTGOING).getAllNodes();
 
                     Node domainNode = null;
                     for (Node node : tr){
                         if (node.getProperty(INeoConstants.PROPERTY_NAME_NAME, "").equals(domain2Edit.getName()))
                             domainNode = node;
                     }
                     if (domainNode!=null){
                         Transaction tx = NeoUtils.beginTx(domainNode.getGraphDatabase());
                         try{
                             if (domainNode.hasRelationship(DomainRelations.ASSIGNED_NEXT, Direction.OUTGOING)){
                                 domainNode.removeProperty(INeoConstants.AFP_PROPERTY_FILTERS_NAME);
                                 domainNode.getSingleRelationship(DomainRelations.ASSIGNED_NEXT, Direction.INCOMING).getStartNode()
                                 .createRelationshipTo(domainNode.getSingleRelationship(DomainRelations.ASSIGNED_NEXT, Direction.OUTGOING).getEndNode(), DomainRelations.ASSIGNED_NEXT);
                                 domainNode.getSingleRelationship(DomainRelations.ASSIGNED_NEXT, Direction.INCOMING).delete();
                                 domainNode.getSingleRelationship(DomainRelations.ASSIGNED_NEXT, Direction.OUTGOING).delete();
                             }
                             else if (domainNode.hasRelationship(DomainRelations.ASSIGNED_NEXT,Direction.INCOMING)){
                                 domainNode.removeProperty(INeoConstants.AFP_PROPERTY_FILTERS_NAME);
                                 domainNode.getSingleRelationship(DomainRelations.ASSIGNED_NEXT, Direction.INCOMING).delete();
                             }
                             tx.success();
                         }
                         finally{
                             tx.finish();
                         }
                     }
                 }
                 return Status.OK_STATUS;
                 
             }
 
         };
         job.join();
 
         
 	}
 	protected void handleDeleteDomain() throws InterruptedException {
 		if (domain2Edit == null){
 			//TODO Do some error handling here;
 			return;
 		}
 //		model.setTotalRemainingTRX(model.getTotalRemainingTRX() + domain2Edit.getNumTRX());
 		model.deleteFreqDomain(domain2Edit.getName());
 		Job job = new Job("DeleteDomain"){
 
             @Override
             protected IStatus run(IProgressMonitor monitor) {
                 // TODO Auto-generated method stub
                 if (model.getAfpNode().hasRelationship(DomainRelations.DOMAINS, Direction.OUTGOING)){
                     Collection<Node> tr = model.getAfpNode().getSingleRelationship(DomainRelations.DOMAINS, Direction.OUTGOING).getEndNode()
                     .traverse(Order.BREADTH_FIRST,
                             StopEvaluator.END_OF_GRAPH,
                             ReturnableEvaluator.ALL_BUT_START_NODE,
                             DomainRelations.NEXT,
                             Direction.OUTGOING).getAllNodes();
 
                     Node domainNode = null;
                     for (Node node : tr){
                         if (node.getProperty(INeoConstants.PROPERTY_NAME_NAME, "").equals(domain2Edit.getName()))
                             domainNode = node;
                     }
                     if (domainNode!=null){
                         Transaction tx = NeoUtils.beginTx(domainNode.getGraphDatabase());
                         try{
                             if (domainNode.hasRelationship(DomainRelations.ASSIGNED_NEXT, Direction.OUTGOING)){
 
                                 domainNode.getSingleRelationship(DomainRelations.ASSIGNED_NEXT, Direction.INCOMING).getStartNode()
                                 .createRelationshipTo(domainNode.getSingleRelationship(DomainRelations.ASSIGNED_NEXT, Direction.OUTGOING).getEndNode(), DomainRelations.ASSIGNED_NEXT);
                                 domainNode.getSingleRelationship(DomainRelations.ASSIGNED_NEXT, Direction.INCOMING).delete();
                                 domainNode.getSingleRelationship(DomainRelations.ASSIGNED_NEXT, Direction.OUTGOING).delete();
                             }
                             else if (domainNode.hasRelationship(DomainRelations.ASSIGNED_NEXT,Direction.INCOMING)){
 
                                 domainNode.getSingleRelationship(DomainRelations.ASSIGNED_NEXT, Direction.INCOMING).delete();
                             }
                             if (domainNode.hasRelationship(DomainRelations.NEXT, Direction.OUTGOING)){
 
                                 domainNode.getSingleRelationship(DomainRelations.NEXT, Direction.INCOMING).getStartNode()
                                 .createRelationshipTo(domainNode.getSingleRelationship(DomainRelations.NEXT, Direction.OUTGOING).getEndNode(), DomainRelations.NEXT);
                                 domainNode.getSingleRelationship(DomainRelations.NEXT, Direction.INCOMING).delete();
                                 domainNode.getSingleRelationship(DomainRelations.NEXT, Direction.OUTGOING).delete();
                             }
                             else if (domainNode.hasRelationship(DomainRelations.NEXT,Direction.INCOMING)){
 
                                 domainNode.getSingleRelationship(DomainRelations.NEXT, Direction.INCOMING).delete();
                             }
                             domainNode.delete();
                             tx.success();
                         }
                         finally{
                             tx.finish();
                         }
                     }
                 }
                 return Status.OK_STATUS;
             }
 
 		};
 		job.join();
 		
 	}
 
 }
