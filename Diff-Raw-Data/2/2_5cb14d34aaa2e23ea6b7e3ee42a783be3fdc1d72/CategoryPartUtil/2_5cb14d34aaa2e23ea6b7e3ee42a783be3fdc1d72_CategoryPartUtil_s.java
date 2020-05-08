 package de.hswt.hrm.component.ui.part;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.widgets.Shell;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.common.ui.swt.table.ColumnDescription;
 import de.hswt.hrm.component.model.Category;
 import de.hswt.hrm.component.ui.wizard.CategoryWizard;
 
 public class CategoryPartUtil {
 
     public CategoryPartUtil() {
         
     }
     
     public static Optional<Category> showWizard(Shell shell, Optional<Category> category) {
         CategoryWizard catWiz = new CategoryWizard(category);
         
         WizardDialog wizDiag = new WizardDialog(shell,catWiz);
         wizDiag.open();
         return catWiz.getCategory();
     }
     
     public static List<ColumnDescription<Category>> getColumns() {
         List<ColumnDescription<Category>> columns = new ArrayList<>();
         columns.add(getName());
         columns.add(getDefaultQuantifier());
         columns.add(getDefaultBoolRating());
         columns.add(getWidth());
         columns.add(getHeight());
         return columns;
     }
     
     private static ColumnDescription<Category> getName() {
         return new ColumnDescription<>("Name", new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 Category cat = (Category) element;
                 return cat.getName();
             }
         }, new Comparator<Category>() {
             @Override
             public int compare(Category c1, Category c2) {
                 return c1.getName().compareToIgnoreCase(c2.getName());
             }
         });
     }
 
     private static ColumnDescription<Category> getDefaultQuantifier() {
         return new ColumnDescription<>("Gewichtung", new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 Category cat = (Category) element;
                 return Integer.toString(cat.getDefaultQuantifier());
             }
         }, new Comparator<Category>() {
             @Override
             public int compare(Category c1, Category c2) {
                 String quant1 = Integer.toString(c1.getDefaultQuantifier());
                 String quant2 = Integer.toString(c2.getDefaultQuantifier());
                 return quant1.compareToIgnoreCase(quant2);
             }
         });
     }
 
     private static ColumnDescription<Category> getDefaultBoolRating() {
        return new ColumnDescription<>("bewertet", new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 Category cat = (Category) element;
                 if (cat.getDefaultBoolRating()) {
                     return "Ja";
                 } else {
                     return "Nein";
                 }
             }
         }, new Comparator<Category>() {
             @Override
             public int compare(Category c1, Category c2) {
                 String bRate1;
                 String bRate2;
                 if (c1.getDefaultBoolRating()) {
                     bRate1 = "Ja";
                 } else {
                     bRate1 = "Nein";
                 }
                 if (c2.getDefaultBoolRating()) {
                     bRate2 = "Ja";
                 } else {
                     bRate2 = "Nein";
                 }
                 return bRate1.compareToIgnoreCase(bRate2);
             }
         });
     }
 
     private static ColumnDescription<Category> getWidth() {
         return new ColumnDescription<>("Breite", new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 Category cat = (Category) element;
                 return Integer.toString(cat.getWidth());
             }
         }, new Comparator<Category>() {
             @Override
             public int compare(Category c1, Category c2) {
                 String w1 = Integer.toString(c1.getWidth());
                 String w2 = Integer.toString(c2.getWidth());
                 return w1.compareToIgnoreCase(w2);
             }
         });
     }
 
     private static ColumnDescription<Category> getHeight() {
         return new ColumnDescription<>("Höhe", new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 Category cat = (Category) element;
                 return Integer.toString(cat.getHeight());
             }
         }, new Comparator<Category>() {
             @Override
             public int compare(Category c1, Category c2) {
                 String h1 = Integer.toString(c1.getHeight());
                 String h2 = Integer.toString(c2.getHeight());
                 return h1.compareToIgnoreCase(h2);
             }
         });
     }
 
 }
