 package org.eclipse.dltk.validators.internal.ui.eternalchecker;
 
import org.eclipse.dltk.validators.internal.ui.ValidatorBlock;
import org.eclipse.dltk.validators.internal.ui.ValidatorsUI;
 import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 
 public class CheckerOutputRulesConfigurationPage 
 	extends PreferencePage implements IWorkbenchPreferencePage {
 	
 	private ParseRuleBlock fRulesBlock;
 	
 	public CheckerOutputRulesConfigurationPage(){
 		super();
 	}
 
 	public void init(IWorkbench workbench){
 		
 	}
 	protected Control createContents(Composite ancestor) {
 		initializeDialogUnits(ancestor);
 		
 		noDefaultAndApplyButton();
 		
 		GridLayout layout= new GridLayout();
 		layout.numColumns= 1;
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		ancestor.setLayout(layout);
 					
 		fRulesBlock = createRulesBlock();
 		fRulesBlock.createControl(ancestor);
 		Control control = fRulesBlock.getControl();
 		GridData data = new GridData(GridData.FILL_BOTH);
 		data.horizontalSpan = 1;
 		control.setLayoutData(data);
 		
 		//fRulesBlock.restoreColumnSettings(ValidatorsUI.getDefault().getDialogSettings(), 
 		//	null);
 						
 		fRulesBlock.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 			}
 		});	
 		applyDialogFont(ancestor);
 		return ancestor;
 	
 	}
 	
 	private ParseRuleBlock createRulesBlock() {
 		return new ParseRuleBlock(); 
 	}
 
 }
