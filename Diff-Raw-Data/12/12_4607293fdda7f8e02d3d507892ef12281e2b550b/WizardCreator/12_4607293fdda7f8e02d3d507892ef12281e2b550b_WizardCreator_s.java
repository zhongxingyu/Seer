 package de.hswt.hrm.common.ui.swt.wizards;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.wizard.IWizard;
 import org.eclipse.jface.wizard.ProgressMonitorPart;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 
 import de.hswt.hrm.i18n.I18n;
 import de.hswt.hrm.i18n.I18nFactory;
 
 public class WizardCreator {
     
     private static final I18n I18N = I18nFactory.getI18n(WizardCreator.class);
 
 	public static final WizardDialog createWizardDialog(Shell shell, IWizard wizard) {
 		return new WizardDialog(shell, wizard)  {
             
             @Override
             protected Control createDialogArea(Composite parent) {
                 Control ctrl = super.createDialogArea(parent);
                 getProgressMonitor();
                 
                 return ctrl;
             }
            
             @Override
             protected void createButtonsForButtonBar(Composite parent) {
                 super.createButtonsForButtonBar(parent);
                 getButton(IDialogConstants.CANCEL_ID).setText(I18N.tr("Cancel"));
                 getButton(IDialogConstants.FINISH_ID).setText(I18N.tr("Finish"));
                getButton(IDialogConstants.NEXT_ID).setText(I18N.tr("Next"));
                getButton(IDialogConstants.BACK_ID).setText(I18N.tr("Back"));
             }
 
             @Override
             protected IProgressMonitor getProgressMonitor() {
                 ProgressMonitorPart monitor = (ProgressMonitorPart) super.getProgressMonitor();
                 GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
                 gridData.heightHint = 0;
                 monitor.setLayoutData(gridData);
                 monitor.setVisible(false);
                 return monitor;
             }
         }; 
 	}
 }
