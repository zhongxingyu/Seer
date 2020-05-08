 package org.jboss.tools.drools.reddeer.wizard;
 
 import org.jboss.reddeer.eclipse.jface.wizard.NewWizardDialog;
 
 public class NewDslWizard extends NewWizardDialog {
 
     public NewDslWizard() {
         super("Drools", "Domain Specific Language");
     }
 
     public NewDslWizardPage getFirstPage() {
        selectPage(0);
         return new NewDslWizardPage();
     }
 
     public NewDslSamplesWizardPage getSamplesPage() {
        selectPage(1);
         return new NewDslSamplesWizardPage();
     }
 
     public void createDefaultDsl(String path, String name) {
         open();
         NewDslWizardPage page = getFirstPage();
         page.setParentFolder(path);
         page.setFileName(name);
         getSamplesPage().setAddSampleDsl(true);
         finish();
     }
 }
