 package de.hswt.hrm.catalog.ui.wizzard;
 
 import javax.inject.Inject;
 
 import org.eclipse.jface.wizard.Wizard;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.catalog.model.Activity;
 import de.hswt.hrm.catalog.model.Current;
 import de.hswt.hrm.catalog.model.ICatalogItem;
 import de.hswt.hrm.catalog.model.Target;
 import de.hswt.hrm.catalog.service.CatalogService;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.SaveException;
 
 public class CatalogWizard extends Wizard {
 
     private static final Logger LOG = LoggerFactory.getLogger(CatalogWizard.class);
     private CatalogWizzardPageOne first;
     private Optional<ICatalogItem> item;
 
     @Inject
     private CatalogService catalogService;
 
     public CatalogWizard(Optional<ICatalogItem> item) {
         this.item = item;
 
         if (item.isPresent()) {
             setWindowTitle("Soll/Ist/Maßnahme bearbeiten");
         }
         else {
             setWindowTitle("Neue Soll/Ist/Maßnahme hinzufügen");
         }
     }
 
     public Optional<ICatalogItem> getItem() {
         return item;
     }
 
     @Override
     public void addPages() {
         first = new CatalogWizzardPageOne("First Page", item);
         addPage(first);
     }
 
     @Override
     public boolean canFinish() {
         return first.isPageComplete();
     }
 
     @Override
     public boolean performFinish() {
         if (item.isPresent()) {
             return editExistingItem();
         }
         return insertNewItem();
     }
 
     private boolean editExistingItem() {
         if (catalogService == null) {
             LOG.error("CatalogService not injected to CatalogWizard");
         }
 
         try {
 
             ICatalogItem i = first.getItem();
             if (i instanceof Activity) {
                 catalogService.updateActivity((Activity) i);

             }
             else if (i instanceof Current) {
                 catalogService.updateCurrent((Current) i);
                 System.out.println("item instance of Current");
             }
             else if (i instanceof Target) {
                 catalogService.updateTarget((Target) i);

             }

             item = Optional.of(i);
 
         }
 
         catch (DatabaseException e) {
             LOG.error("Could not update CatalogItem", e);
             return false;
 
         }
         return true;
     }
 
     private boolean insertNewItem() {
         if (catalogService == null) {
             LOG.error("CatalogService not inject to CatalogWizard.");
         }
 
         try {
             ICatalogItem i = first.getItem();
             if (i instanceof Activity) {
                 item = Optional.of((ICatalogItem) catalogService.insertActivity((Activity) (i)));
             }
             else if (i instanceof Current) {
                 item = Optional.of((ICatalogItem) catalogService.insertCurrent(((Current) (i))));
             }
             else if (i instanceof Target) {
                 item = Optional.of((ICatalogItem) catalogService.insertTarget(((Target) (i))));
             }
 
         }
         catch (SaveException e) {
             LOG.error("Could not insert CatalogItem into database", e);
             return false;
         }
 
         return true;
     }
 }
