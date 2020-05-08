 package de.hswt.hrm.component.ui.part;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.sql.Blob;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.inject.Inject;
 import javax.sql.rowset.serial.SerialBlob;
 
 import org.eclipse.e4.core.contexts.ContextInjectionFactory;
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.widgets.Shell;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.common.ui.swt.table.ColumnDescription;
 import de.hswt.hrm.common.ui.swt.wizards.WizardCreator;
 import de.hswt.hrm.component.model.Component;
 import de.hswt.hrm.component.service.CategoryService;
 import de.hswt.hrm.component.service.ComponentService;
 import de.hswt.hrm.component.ui.wizard.ComponentWizard;
 
 
 
 public final class ComponentPartUtil {
 	
     private ComponentPartUtil() {
 
     }
 
     public static Optional<Component> showWizard(ComponentService compSer, CategoryService catSer, IEclipseContext context, Shell shell,
             Optional<Component> component) {
     	
         ComponentWizard cw = new ComponentWizard(component, compSer,catSer);
         ContextInjectionFactory.inject(cw, context);
 
         WizardDialog wd = WizardCreator.createWizardDialog(shell, cw);
         wd.open();
         return cw.getComponent();
     }
 
     public static List<ColumnDescription<Component>> getColumns() {
         List<ColumnDescription<Component>> columns = new ArrayList<>();
         columns.add(getName());
         columns.add(getQuantifier());
         columns.add(getCategory());
         columns.add(getRating());
 //        columns.add(getAttribute());
 
         return columns;
 
     }
 
     private static ColumnDescription<Component> getRating() {
         return new ColumnDescription<>("Rating", new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 Component c = (Component) element;
                if(c.getBoolRating() == true){
             	   return "Yes";
                }else{
             	   return "No";
                }
             }
         }, new Comparator<Component>() {
             @Override
             public int compare(Component c1, Component c2) {
                 String q = new Boolean(c1.getBoolRating()).toString();
                 String q2 = new Boolean(c2.getBoolRating()).toString();
                 return q.compareToIgnoreCase(q2);
             }
         });
     }
 
 	private static ColumnDescription<Component> getCategory() {
         return new ColumnDescription<>("Category", new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                	Component c = (Component) element;
                 return c.getCategory().get().getName();
             }
         }, new Comparator<Component>() {
             @Override
             public int compare(Component c1, Component c2) {
                 return c1.getCategory().get().getName().compareToIgnoreCase(c2.getCategory().get().getName());
             }
         });
     }
 
 	private static ColumnDescription<Component> getName() {
         return new ColumnDescription<>("Name", new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                	Component c = (Component) element;
                 return c.getName();
             }
         }, new Comparator<Component>() {
             @Override
             public int compare(Component c1, Component c2) {
                 return c1.getName().compareToIgnoreCase(c2.getName());
             }
         });
     }
     
     
     private static ColumnDescription<Component> getQuantifier() {
         return new ColumnDescription<>("Quantifier", new ColumnLabelProvider() {
             @Override
             public String getText(Object element) {
                 Component c = (Component) element;
                if (c.getQuantifier().isPresent()) {
                	return c.getQuantifier().get().toString();
                }
                
                return "";
             }
         }, new Comparator<Component>() {
             @Override
             public int compare(Component c1, Component c2) {
            	int q1 = c1.getQuantifier().or(Integer.MAX_VALUE);
            	int q2 = c2.getQuantifier().or(Integer.MAX_VALUE);
            	return q1 - q2;
             }
         });
     }
 }
