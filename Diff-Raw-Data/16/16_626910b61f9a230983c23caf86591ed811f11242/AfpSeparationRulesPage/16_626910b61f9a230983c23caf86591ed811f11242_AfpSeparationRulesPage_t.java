 package org.amanzi.awe.afp.wizards;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Vector;
 
 import org.amanzi.awe.afp.filters.AfpColumnFilter;
 import org.amanzi.awe.afp.filters.AfpRowFilter;
 import org.amanzi.awe.afp.models.AfpDomainModel;
 import org.amanzi.awe.afp.models.AfpFrequencyDomainModel;
 import org.amanzi.awe.afp.models.AfpHoppingMALDomainModel;
 import org.amanzi.awe.afp.models.AfpModel;
 import org.amanzi.awe.afp.models.AfpModelUtils;
 import org.amanzi.awe.afp.models.AfpSeparationDomainModel;
 import org.amanzi.neo.services.INeoConstants;
 import org.amanzi.neo.services.enums.NetworkRelationshipTypes;
 import org.amanzi.neo.services.enums.NodeTypes;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser;
 import org.neo4j.graphdb.Traverser.Order;
 
 public class AfpSeparationRulesPage extends AfpWizardPage  implements FilterListener{
 	
 	private Group sectorDomainsGroup;
 	private Group siteDomainsGroup;
 	protected HashMap<String, Label[]> sectorDomainLabels;
 	protected HashMap<String, Label[]> siteDomainLabels;
 	private final String[] headers = { "BSC", "Site", "Sector", "Layer"};
 	private final String[] noListenerHeadersSite = { "Sector"};
 	private final HashMap<String,String> headersNodeType = new HashMap<String,String>(); 
 	private final HashMap<String,String> headers_prop = new HashMap<String,String>();
 
 	private Table filterTableSector;
 	private Table filterTableSite;
 	protected AfpRowFilter siteRowFilter;
 	protected AfpRowFilter sectorRowFilter;
 	protected  Shell parentShell;
 	private int siteCount;
     private int sectorCount;
     int remainingSectors = 0;
     int remainingSites =0;
     private Group main;
     private boolean disablePage;
 	
 	TabFolder tabFolder;
 	
     HashMap<String,Set<Object>> uniqueSitePropertyValuesSite = new HashMap<String,Set<Object>>();
     HashMap<String,Set<Object>> uniqueSectorPropertyValuesSite = new HashMap<String,Set<Object>>();
     HashMap<String,Set<Object>> uniqueTrxPropertyValuesSite = new HashMap<String,Set<Object>>();
     HashMap<String,Set<Object>> uniqueSitePropertyValuesSector = new HashMap<String,Set<Object>>();
     HashMap<String,Set<Object>> uniqueSectorPropertyValuesSector = new HashMap<String,Set<Object>>();
     HashMap<String,Set<Object>> uniqueTrxPropertyValuesSector = new HashMap<String,Set<Object>>();
 
 
 	public AfpSeparationRulesPage(String pageName, AfpModel model, String desc) {
 		super(pageName, model);
 
 		headers_prop.put("BSC", "bsc");
 		headers_prop.put("Site", INeoConstants.PROPERTY_NAME_NAME);
 		headers_prop.put("Sector", INeoConstants.PROPERTY_NAME_NAME);
 		headers_prop.put("Layer", "Layer");
 
 		headersNodeType.put("BSC", NodeTypes.SITE.getId());
 		headersNodeType.put("Site", NodeTypes.SITE.getId());
 		headersNodeType.put("Sector", NodeTypes.SECTOR.getId());
 		headersNodeType.put("Layer", NodeTypes.TRX.getId());
 		
 	    uniqueSitePropertyValuesSector = this.uniqueSitePropertyValues;
 	    uniqueSectorPropertyValuesSector = this.uniqueSectorPropertyValues;
 	    uniqueTrxPropertyValuesSector = this.uniqueTrxPropertyValues;
 
 	    for(String p: AfpModel.sitePropertiesName) {
 	    	uniqueSitePropertyValuesSite.put(p, new HashSet<Object>());
 		}
 		for(String p: AfpModel.sectorPropertiesName) {
 			uniqueSectorPropertyValuesSector.put(p, new HashSet<Object>());
 		}
 		
 		for(String p: AfpModel.trxPropertiesName) {
 			uniqueTrxPropertyValuesSector.put(p, new HashSet<Object>());
 		}
 
 		setTitle(AfpImportWizard.title);
         setDescription(desc);
         setPageComplete (false);
         siteRowFilter = new AfpRowFilter();
         sectorRowFilter = new AfpRowFilter();
         
         
 	}
 	
 	@Override
 	public void createControl(Composite parent) {
 		
 		parentShell = parent.getShell();
 		Composite thisParent = new Composite(parent, SWT.NONE);
    	 	thisParent.setLayout(new GridLayout(2, false));
 		
 		Group stepsGroup = AfpWizardUtils.getStepsGroup(thisParent, 5);
 
 		main = new Group(thisParent, SWT.NONE);
 		main.setLayout(new GridLayout(1, true));
 		main.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1 ,1));
 
 		tabFolder =new TabFolder(main, SWT.NONE);// | SWT.BORDER);
 		tabFolder.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		TabItem item2 =new TabItem(tabFolder,SWT.NONE);
 		item2.setText("Site");
 		
     	item2.setControl(new SiteGroup(tabFolder, this));
 
     	TabItem item1 =new TabItem(tabFolder,SWT.NONE);
 		item1.setText("Sector");
 		
 		
 		item1.setControl(new SectorGroup(tabFolder, this));
 		
 		
     	
     	this.tabFolder.addSelectionListener(new SelectionAdapter() {
     		public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
     			// interchange the unique value cache
     			if(tabFolder.getSelectionIndex() == 1) {
     				updateLabels();
     				interChangeUniquePropertySet(true);
     			} else {
     				updateLabels();
     				interChangeUniquePropertySet(false);
     			}
     			
     		}
     	});
     	
     	
     	setPageComplete (true);
 		setControl(thisParent);
 
 	}
 	
 	class SectorGroup extends Composite {
 		public SectorGroup(Composite parent, AfpSeparationRulesPage pPage) {
 			super(parent, SWT.NONE);
 			this.setLayout(new GridLayout(1, true)); 
 
 			Group sectorMain = new Group(this, SWT.NONE);
 			sectorMain.setLayout(new GridLayout(1, false));
 			sectorMain.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1 ,2));
 			
 			sectorDomainsGroup = new Group(sectorMain, SWT.NONE);
 			sectorDomainsGroup.setLayout(new GridLayout(3, true));
 			sectorDomainsGroup.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true,1 ,10));
 			
 			Label sectorDomainsLabel = new Label(sectorDomainsGroup, SWT.LEFT);
 			sectorDomainsLabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 1 , 1));
 			sectorDomainsLabel.setText("Domains");
 	    	AfpWizardUtils.makeFontBold(sectorDomainsLabel);
 	    	
 	    	Label sectorsLabel = new Label(sectorDomainsGroup, SWT.LEFT);
 	    	sectorsLabel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1 , 1));
 	    	sectorsLabel.setText("Assigned Sectors");
 	    	AfpWizardUtils.makeFontBold(sectorsLabel);
 	    	
 	    	AfpWizardUtils.createButtonsGroup(pPage, sectorDomainsGroup, "Sector SeparationRules", model);
 	    	sectorDomainLabels = new HashMap<String, Label[]>();
 			
 	    	filterTableSector = addTRXFilterGroup(sectorMain, headers,10, false, pPage, new String[0]);
 		}
 	}
 	class SiteGroup extends Composite {
 		public SiteGroup(Composite parent, AfpSeparationRulesPage pPage) {
 			super(parent, SWT.NONE);
 			this.setLayout(new GridLayout(1, true)); 
 			Group siteMain = new Group(this, SWT.NONE);
 			siteMain.setLayout(new GridLayout(1, false));
 			siteMain.setLayoutData(new GridData(GridData.FILL, SWT.FILL, true, true, 1 ,2));
 			
 			siteDomainsGroup = new Group(siteMain, SWT.NONE);
 			siteDomainsGroup.setLayout(new GridLayout(3, true));
 			siteDomainsGroup.setLayoutData(new GridData(GridData.FILL, SWT.FILL, true, true,1 ,10));
 			
 			Label siteDomainsLabel = new Label(siteDomainsGroup, SWT.LEFT);
 			siteDomainsLabel.setLayoutData(new GridData(GridData.FILL, SWT.BEGINNING, true, false, 1 , 1));
 			siteDomainsLabel.setText("Domains");
 	    	AfpWizardUtils.makeFontBold(siteDomainsLabel);
 	    	
 	    	Label sitesLabel = new Label(siteDomainsGroup, SWT.LEFT);
 	    	sitesLabel.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, 1 , 1));
 	    	sitesLabel.setText("Assigned Sites");
 	    	AfpWizardUtils.makeFontBold(sitesLabel);
 	    	
 	    	AfpWizardUtils.createButtonsGroup(pPage, siteDomainsGroup, "Site SeparationRules", model);
 	    	siteDomainLabels = new HashMap<String, Label[]>();
 	    	
 	    	filterTableSite = addTRXFilterGroup(siteMain, headers,10, true, pPage, noListenerHeadersSite);
 			
 
 		}
 	}
 	
 	private void interChangeUniquePropertySet(boolean sector) {
 		if(sector) {
 		    uniqueSitePropertyValues = uniqueSitePropertyValuesSector;
 		    uniqueSectorPropertyValues = uniqueSectorPropertyValuesSector;
 		    uniqueTrxPropertyValues = uniqueTrxPropertyValuesSector;
 		} else {
 		    uniqueSitePropertyValues = uniqueSitePropertyValuesSite;
 		    uniqueSectorPropertyValues = uniqueSectorPropertyValuesSite;
 		    uniqueTrxPropertyValues = uniqueTrxPropertyValuesSite;
 		}
 	}
 	
 	public void deleteSectorDomainLabels(String domainName){
 		if (sectorDomainLabels.containsKey(domainName)){
 			Label[] labels = sectorDomainLabels.get(domainName);
 			sectorDomainLabels.remove(domainName);
 			for (Label label : labels){
 				label.dispose();
 			}
 		}
 //		sectorDomainsGroup.layout();
 	}
 	
 	public void deleteSiteDomainLabels(String domainName){
 		if (siteDomainLabels.containsKey(domainName)){
 			Label[] labels = siteDomainLabels.get(domainName);
 			siteDomainLabels.remove(domainName);
 			for (Label label : labels){
 				label.dispose();
 			}
 		}
 //		siteDomainsGroup.layout();
 	}
 
 	
 	@Override
 	public void refreshPage(){
 //		super.refreshPage();
 		
 		disablePage = true;
 		for (boolean b : model.getChanneltypes()){
 			if (b){
 				disablePage = false;
 				break;
 			}
 		}
 		
 		if (disablePage){
 			main.setEnabled(false);
 			model.setSectorSeparationDomains(new HashMap<String,AfpSeparationDomainModel>());
 			model.setSiteSeparationDomains(new HashMap<String,AfpSeparationDomainModel>());
 			remainingSectors = 0;
 		    remainingSites =0;
 		}
 		else{
 			main.setEnabled(true);
 		}
 		
 		updateLabels();
 //		updateSectorDomainLabels();
 //		updateSiteDomainLabels();
 
 		this.interChangeUniquePropertySet(false);
 		loadSiteData(filterTableSite, siteRowFilter);
 		this.interChangeUniquePropertySet(true);
 		loadData(filterTableSector, sectorRowFilter);
 		if(tabFolder.getSelectionIndex() ==1) {
 			this.interChangeUniquePropertySet(true);
 		} else {
 			this.interChangeUniquePropertySet(false);
 		}
 		sectorDomainsGroup.layout();
 		siteDomainsGroup.layout();
 	}
 
 	
 	public void loadData(Table table, AfpRowFilter rowFilter) {
 		if(table != null) {
 			table.removeAll();
 			
 			if (disablePage){
 				this.updateSectorFilterLabel(0, 0);
 				updateLabels();
 			}
 			
 			else{
 			
 			    Iterable<Node> sectorTraverser = model.getTRXList(null);
 			    
 			    this.clearAllUniqueValuesForProperty();
 			    
 			    sectorCount = 0;
 			    for(AfpSeparationDomainModel mod: model.getSectorSeparationDomains(false)){
 			    	mod.setNumTRX(0);
 			    }
 			    remainingSectors = 0;
 	
 			    for (Node sectorNode : sectorTraverser) {
 			    	// temp fix.
 			    	// find the one TRX node under
 			    	Node siteNode = sectorNode.getSingleRelationship(NetworkRelationshipTypes.CHILD, Direction.INCOMING).getStartNode();
 			    	Traverser trxTraverser = AfpModelUtils.getTrxTraverser(sectorNode);
 
 			    	boolean includeSector = false;
 			    	int hoppingType = (Integer)sectorNode.getProperty(INeoConstants.PROPERTY_HOPPING_TYPE_NAME, 0);
 			    	
 			    	if (!model.getChanneltypes()[AfpModel.CHANNEL_NON_HOPIING]){
 			    		if (hoppingType < 1)
 			    			continue;
 			    	}
 			    	
 			    	if (!model.getChanneltypes()[AfpModel.CHANNEL_HOPPING]){
 			    		if (hoppingType > 0)
 			    			continue;
 			    	}
 			    	
 			    	Node trxNode = null;
 			    	for(Node n  :trxTraverser) {
 			    		trxNode = n;
 			    		
 			    		if (!model.getChanneltypes()[AfpModel.CHANNEL_BCCH]){
 				    		if (!((Boolean)trxNode.getProperty(INeoConstants.PROPERTY_BCCH_NAME, false))){
 				    			includeSector = true;
 				    			break;
 				    		}
 				    	}
 			    		else
 			    			includeSector = true;
 				    
 			    	}
 			    	
 			    	if (!includeSector)
 			    		continue;
 			    	
 			    	if(trxNode == null)
 			    		continue;
 			    	
 			    	boolean includeFlag = true;
 			    	for(AfpSeparationDomainModel mod: model.getSectorSeparationDomains(false)){
 			    		String filterString = mod.getFilters();
 			    		if (filterString != null && !filterString.trim().isEmpty()){
 				    		AfpRowFilter rf = AfpRowFilter.getFilter(mod.getFilters());
 				    		if (rf != null){
 					    		if (rf.equal(trxNode)){
 					    			mod.setNumTRX(mod.getNumTRX() + 1);
 					    			model.updateSectorSepDomain(mod);
 					    			includeFlag = false;
 					    			break;
 					    		}
 				    		}
 			    		}
 			    	}
 				    	
 			    	if (!includeFlag)
 			    		continue;
 			    	
 			    	remainingSectors++;
 			    			
 		    		if (rowFilter != null){
 			    		if (!rowFilter.equal(trxNode)) 
 			    			continue;
 			    	}
 			    	this.addSectorUniqueProperties(sectorNode);
 					this.addSiteUniqueProperties(siteNode);
 			    	this.addTrxUniqueProperties(trxNode);
 	    		    	
 			    	
 			    	if(sectorCount <= 10) {  
 				    	TableItem item = new TableItem(table, SWT.NONE);
 				    	int j=0;
 				    	for (String prop_name : headers){
 				    		Object val = null;
 				    		try {
 				    			String type = this.headersNodeType.get(prop_name);
 				    			if(NodeTypes.SITE.getId().equals(type)) {
 				    				if (siteNode.getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals("site"))
 				    					val = (String)siteNode.getProperty(headers_prop.get(prop_name), "");
 	
 				    			} else if( NodeTypes.SECTOR.getId().equals(type)) {
 				    				val = sectorNode.getProperty(headers_prop.get(prop_name), "");
 				    			} else {
 				    				val = trxNode.getProperty(headers_prop.get(prop_name), "");
				    			    
 				    			}
 	
 				    			if(val == null) val ="";
 				    			
 				    			item.setText(j, val.toString());
 				    		} catch(Exception e) {
 				    			item.setText(j, "");
 				    		}
 				    		j++;
 				    	}
 			    	}
 			    	sectorCount++;
 			    }
 			}
 		    for (int i = 0; i < headers.length; i++) {
 		    	table.getColumn(i).pack();
 		    }
 		    updateLabels();
 		    this.updateSectorFilterLabel(sectorCount, remainingSectors);
 		}
 	}
 	
 	public void loadSiteData(Table table, AfpRowFilter rowFilter) {
 		if(table != null) {
 			table.removeAll();
 			
 			if (disablePage){
 				this.updateSectorFilterLabel(0, 0);
 				updateLabels();
 			}
 			
 			else{
 			
 			    Iterable<Node> sectorTraverser = model.getTRXList(null);
 			    
 			    this.clearAllUniqueValuesForProperty();
 			    
 			    siteCount = 0;
 			    for(AfpSeparationDomainModel mod: model.getSiteSeparationDomains(false)){
 			    	mod.setNumTRX(0);
 			    }
 			    remainingSites = 0;
 	
 			    for (Node sectorNode : sectorTraverser) {
 			    	// temp fix.
 			    	// find the one TRX node under
 			    	Node siteNode = sectorNode.getSingleRelationship(NetworkRelationshipTypes.CHILD, Direction.INCOMING).getStartNode();
 			    	Traverser trxTraverser = AfpModelUtils.getTrxTraverser(sectorNode);
 			    	
 			    	boolean includeSite = false;
 			    	int hoppingType = (Integer)sectorNode.getProperty(INeoConstants.PROPERTY_HOPPING_TYPE_NAME, 0);
 			    	
 			    	if (!model.getChanneltypes()[AfpModel.CHANNEL_NON_HOPIING]){
 			    		if (hoppingType < 1)
 			    			continue;
 			    	}
 			    	
 			    	if (!model.getChanneltypes()[AfpModel.CHANNEL_HOPPING]){
 			    		if (hoppingType > 0)
 			    			continue;
 			    	}
 			    	
 			    	Node trxNode = null;
 			    	for(Node n  :trxTraverser) {
 			    		trxNode = n;
 			    		
 			    		if (!model.getChanneltypes()[AfpModel.CHANNEL_BCCH]){
 				    		if (!((Boolean)trxNode.getProperty(INeoConstants.PROPERTY_BCCH_NAME, false))){
 				    			includeSite = true;
 				    			break;
 				    		}
 				    	}
 			    		else
 			    			includeSite = true;
 				    
 			    	}
 			    	
 			    	if (!includeSite)
 			    		continue;
 			    	
 			    	
 			    	if(trxNode == null)
 			    		continue;
 			    	
 			    	boolean includeFlag = true;
 			    	for(AfpSeparationDomainModel mod: model.getSiteSeparationDomains(false)){
 			    		String filterString = mod.getFilters();
 			    		if (filterString != null && !filterString.trim().isEmpty()){
 				    		AfpRowFilter rf = AfpRowFilter.getFilter(mod.getFilters());
 				    		if (rf != null){
 					    		if (rf.equal(trxNode)){
 					    			mod.setNumTRX(mod.getNumTRX() + 1);
 					    			model.updateSiteSepDomain(mod);
 					    			includeFlag = false;
 					    			break;
 					    		}
 				    		}
 			    		}
 			    	}
 				    	
 			    	if (!includeFlag)
 			    		continue;
 			    	
 			    	remainingSites++;
 			    		
 		    		if (rowFilter != null){
 			    		if (!rowFilter.equal(trxNode)) 
 			    			continue;
 			    	}
 			    	this.addSectorUniqueProperties(sectorNode);
 					this.addSiteUniqueProperties(siteNode);
 			    	this.addTrxUniqueProperties(trxNode);
 	    		    	
 			    	
 			    	if(siteCount <= 10) {  
 				    	TableItem item = new TableItem(table, SWT.NONE);
 				    	int j=0;
 				    	for (String prop_name : headers){
 				    		Object val = null;
 				    		try {
 				    			String type = this.headersNodeType.get(prop_name);
 				    			if(NodeTypes.SITE.getId().equals(type)) {
 				    				if (siteNode.getProperty(INeoConstants.PROPERTY_TYPE_NAME).equals("site"))
 				    					val = (String)siteNode.getProperty(headers_prop.get(prop_name), "");
 	
 				    			} else if( NodeTypes.SECTOR.getId().equals(type)) {
 				    				val = sectorNode.getProperty(headers_prop.get(prop_name), "");
 				    			} else {
 				    				val = trxNode.getProperty(headers_prop.get(prop_name), "");
 				    			}
 	
 				    			if(val == null) val ="";
 				    			
 				    			item.setText(j, val.toString());
 				    		} catch(Exception e) {
 				    			item.setText(j, "");
 				    		}
 				    		j++;
 				    	}
 			    	}
 			    	siteCount++;
 			    }
 			}
 		    for (int i = 0; i < headers.length; i++) {
 		    	table.getColumn(i).pack();
 		    }
 		    updateLabels();
 		    this.updateSiteFilterLabel(siteCount, remainingSites);
 		}
 	}
 
 	@Override
 	public void onFilterSelected(String columnName,	ArrayList<String> selectedValues) {
 		
 	String val = headers_prop.get(columnName);
 		
 		if(val != null ) {
 			AfpColumnFilter colFilter = new AfpColumnFilter(val, this.headersNodeType.get(columnName));
 			for (String value: selectedValues){
 				colFilter.addValue(value);
 			}
 			if(tabFolder.getSelectionIndex() ==0) {
 				siteRowFilter.addColumn(colFilter);
 				loadSiteData(filterTableSite, siteRowFilter);
 			} else {
 				sectorRowFilter.addColumn(colFilter);
 				loadData(filterTableSector, sectorRowFilter);
 			}
 		}
 	}
 	
 
 	@Override
 	public Object[] getColumnUniqueValues(String colName){
 		
 		String type  = headersNodeType.get(colName);
 		if(NodeTypes.SITE.getId().equals(type)) {
 			return this.getSiteUniqueValuesForProperty(headers_prop.get(colName));
 		} else if(NodeTypes.SECTOR.getId().equals(type)) {
 			return this.getSectorUniqueValuesForProperty(headers_prop.get(colName));
 		} else {
 			return this.getTrxUniqueValuesForProperty(headers_prop.get(colName));
 		}
 	}
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		if (e.widget.getData().equals(AfpWizardPage.ASSIGN)){
 			final Shell subShell = new Shell(parentShell, SWT.PRIMARY_MODAL|SWT.TITLE);
 			subShell.setText("Assign to Domain");
 			subShell.setLayout(new GridLayout(2, false));
 			subShell.setLocation(200, 200);
 			
 			Label infoLabel = new Label (subShell, SWT.LEFT);
 			//TODO update label to show correct no. of TRXs
 			//infoLabel.setText("Selected " + trxCount + " TRXs will be assigned to:");
 			//infoLabel.setLayoutData(new GridData(GridData.FILL, SWT.LEFT, true, false,2 ,1));
 			
 			Label domainLabel = new Label (subShell, SWT.LEFT);
 			domainLabel.setText("Select Domain");
 			domainLabel.setLayoutData(new GridData(GridData.FILL, SWT.LEFT, true, false,2 ,1));
 			
 			final Combo domainCombo = new Combo(subShell, SWT.DROP_DOWN | SWT.READ_ONLY);
 			ArrayList<String> modelNames = new ArrayList<String>();
 			if(tabFolder.getSelectionIndex() ==1) {
 				for (AfpSeparationDomainModel dm : model.getSectorSeparationDomains(false)){
 					modelNames.add(dm.getName());
 				}
 			} else {
 				for (AfpSeparationDomainModel dm : model.getSiteSeparationDomains(false)){
 					modelNames.add(dm.getName());
 				}
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
 					if(tabFolder.getSelectionIndex() ==1) {
 						AfpDomainModel sepModel = model.findDomainByName(model.DOMAIN_TYPES[2], domainName);
 						sepModel.setFilters(sectorRowFilter.toString());
 						sepModel.setNumTRX(sectorCount);
 						sectorRowFilter.clear();
 						loadData(filterTableSector, sectorRowFilter);
 					} else {
 						AfpDomainModel malModel = model.findDomainByName(model.DOMAIN_TYPES[3], domainName);
 						malModel.setFilters(siteRowFilter.toString());
 						malModel.setNumTRX(siteCount);
 						siteRowFilter.clear();
 						loadSiteData(filterTableSite, siteRowFilter);
 						
 					}
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
 			if(tabFolder.getSelectionIndex() ==1) {
 				sectorRowFilter.clear();
 				loadData(filterTableSector, sectorRowFilter);
 			}
 			else{
 				siteRowFilter.clear();
 				loadSiteData(filterTableSite, siteRowFilter);
 			}
 		}
 			
 	}
 	
 	public void updateLabels(){
 		if(tabFolder.getSelectionIndex() ==1) {
 			for(Label[] labels: sectorDomainLabels.values() ){
 				for (Label label : labels){
 					label.dispose();
 				}
 			}
 			sectorDomainLabels.clear();
 			
 			for(AfpSeparationDomainModel domainModel :model.getSectorSeparationDomains(true)) {
 				Label defaultDomainLabel = new Label(sectorDomainsGroup, SWT.LEFT);
 				defaultDomainLabel.setText(domainModel.getName());
 				Label defaultTRXsLabel = new Label(sectorDomainsGroup, SWT.RIGHT);
 				defaultTRXsLabel.setText("" + domainModel.getNumTRX());
 				if (domainModel.getName().equals(AfpModel.DEFAULT_SECTOR_SEP_NAME))
 					defaultTRXsLabel.setText("" + remainingSectors);
 				sectorDomainLabels.put(domainModel.getName(), new Label[]{defaultDomainLabel, defaultTRXsLabel});
 			}
 			
 			sectorDomainsGroup.layout();
 		} else {
 			for(Label[] labels: siteDomainLabels.values() ){
 				for (Label label : labels){
 					label.dispose();
 				}
 			}
 			siteDomainLabels.clear();
 			
 			for(AfpSeparationDomainModel domainModel :model.getSiteSeparationDomains(true)) {
 				Label defaultDomainLabel = new Label(siteDomainsGroup, SWT.LEFT);
 				defaultDomainLabel.setText(domainModel.getName());
 				Label defaultTRXsLabel = new Label(siteDomainsGroup, SWT.RIGHT);
 				defaultTRXsLabel.setText("" + domainModel.getNumTRX());
 				if (domainModel.getName().equals(AfpModel.DEFAULT_SITE_SEP_NAME))
 					defaultTRXsLabel.setText("" + remainingSites);
 				siteDomainLabels.put(domainModel.getName(), new Label[]{defaultDomainLabel, defaultTRXsLabel});
 			}
 			
 			siteDomainsGroup.layout();
 
 		}
 	}
 
 }
