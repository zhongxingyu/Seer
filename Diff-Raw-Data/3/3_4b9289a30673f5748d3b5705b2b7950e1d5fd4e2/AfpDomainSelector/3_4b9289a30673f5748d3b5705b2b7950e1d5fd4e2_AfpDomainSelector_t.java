 package org.amanzi.awe.afp.wizards;
 
 import org.amanzi.awe.afp.models.AfpDomainModel;
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
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 public class AfpDomainSelector {
 	final Shell subShell;
 	protected String domainName;
 	protected Group freqGroup;
 	protected AfpModel model;
 	protected WizardPage page;
 	
 	protected boolean newDomain = true;
 	protected AfpDomainModel domain2Edit = null;
 	
 	private Button actionButton;
 
 	public AfpDomainSelector(final WizardPage page, Shell parentShell, final Group parentGroup, final AfpModel model){
 
 		int selectedBand =0;
 		subShell = new Shell(parentShell, SWT.PRIMARY_MODAL|SWT.TITLE);
 		this.model = model;
 		this.page = page;
 	}
 	
 	public void createUI(final String action, String title,String[] editNames) {
 		subShell.setText(action +  title);
 		subShell.setLayout(new GridLayout(3, false));
 		subShell.setLocation(200, 200);
 		
 		Label nameLabel = new Label(subShell, SWT.NONE);
 		nameLabel.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 3, 1));
 		nameLabel.setText("Domain Name");
 
 		if (action.equals("Add")){
 			Text nameText = new Text (subShell, SWT.BORDER | SWT.SINGLE);
 			nameText.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 3, 1));
 			nameText.addModifyListener(new ModifyListener(){
 
 				@Override
 				public void modifyText(ModifyEvent e) {
 					domainName = ((Text)e.widget).getText();
 				}
 			});
 		}
 		
 		if (action.equals("Edit") || action.equals("Delete")|| action.equals("Clear")){
 			newDomain = false;
 			Combo nameCombo = new Combo(subShell, SWT.DROP_DOWN | SWT.READ_ONLY);
 //			String names[] = model.getAllFrequencyDomainNames();
 			if(editNames == null) {
 				return;
 			}
 			if(editNames.length ==0) {
 				return;
 			}
 			nameCombo.setItems(editNames);
 			nameCombo.select(0);
 			nameCombo.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false, 3, 1));
 			nameCombo.addModifyListener(new ModifyListener(){
 
 				@Override
 				public void modifyText(ModifyEvent e) {
 					handleDomainNameSection(((Combo)e.widget).getSelectionIndex(), ((Combo)e.widget).getItem(((Combo)e.widget).getSelectionIndex()));
 				}
 				
 			});
 			
 			nameCombo.addSelectionListener(new SelectionListener(){
 
 				@Override
 				public void widgetDefaultSelected(SelectionEvent e) {
 					widgetSelected(e);					
 				}
 
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					domainName = ((Combo)e.widget).getText();
 				}
 				
 			});
 		}
 		
 	}
 	
 	protected void addButtons(final String action) {
 		actionButton = new Button(subShell, SWT.PUSH);
 		actionButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.BEGINNING, true, false, 2, 1));
 		
 		actionButton.setText(action);
 		actionButton.addSelectionListener(new SelectionAdapter(){
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				boolean ret = true;
 				if (action.equals("Add")){
 				    
 					ret = handleAddDomain();
 					
 					((AfpWizardPage)page).refreshPage();
 				}
 				if (action.equals("Edit")){
 					handleEditDomain();
 					((AfpWizardPage)page).refreshPage();
 				}
 				
 				if (action.equals("Delete")){
 					try {
                         handleDeleteDomain();
                     } catch (InterruptedException e1) {
                         // TODO Auto-generated catch block
                         e1.printStackTrace();
                     }
 					((AfpWizardPage)page).refreshPage();
 				}
 				
 				if (action.equals("Clear")){
                     try {
                         handleClearDomain();
                     } catch (InterruptedException e1) {
                         // TODO Auto-generated catch block
                         e1.printStackTrace();
                     }
                     ((AfpWizardPage)page).refreshPage();
                 }
 				if(ret)
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
 	
 	protected void setStateToAddButton(boolean state) {
	    if (actionButton.getText().equals("Add"))
	        actionButton.setEnabled(state);
 	}
 
 	protected void handleDomainNameSection(int selection, String name) {
 	}
 	protected boolean handleAddDomain() {
 		return false;
 	}
 	protected void handleEditDomain() {
 	}
 	protected void handleDeleteDomain() throws InterruptedException {
 	}
 	protected void handleClearDomain() throws InterruptedException{
 	    
 	}
 	public void setDomainModel(AfpDomainModel domainModel) {
 		this.domain2Edit = domainModel;
 	}
 
 }
