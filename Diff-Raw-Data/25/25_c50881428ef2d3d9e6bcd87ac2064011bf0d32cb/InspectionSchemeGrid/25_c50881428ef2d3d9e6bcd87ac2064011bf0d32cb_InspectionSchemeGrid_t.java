 package de.hswt.hrm.inspection.ui.grid;
 
 import java.util.Collection;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.component.model.Category;
 import de.hswt.hrm.scheme.model.SchemeComponent;
import de.hswt.hrm.scheme.ui.Colorbox;
 import de.hswt.hrm.scheme.ui.ItemClickListener;
 import de.hswt.hrm.scheme.ui.SchemeGrid;
 import de.hswt.hrm.scheme.ui.SchemeGridItem;
 
 /**
  * A SchemeGrid where Components can be colored. The Components can also be selected.
  * 
  * @author Michael Sieger
  * 
  */
 public class InspectionSchemeGrid {
 
     private final static Logger LOG = LoggerFactory.getLogger(InspectionSchemeGrid.class);
 
     private static final int PPG = 30;
 
     private final SchemeGrid grid;
     private SchemeComponent selected;
    private Colorbox selectionBox;
     private SchemeComponentSelectionListener listener;
 
     public InspectionSchemeGrid(final Composite parent, int style) {
         grid = new SchemeGrid(parent, style, 1, 1, PPG);
         grid.setItemClickListener(new ItemClickListener() {
 
             @Override
             public void itemClicked(MouseEvent e, SchemeGridItem item) {
                 setSelected(item.asSchemeComponent());
             }
         });
     }
 
     /**
      * Sets the Listener that is notified if a component is selected.
      * 
      * @param listener
      */
     public void setSelectionListener(SchemeComponentSelectionListener listener) {
         this.listener = listener;
         if (listener != null) {
             listener.selected(selected);
         }
     }
 
     /**
      * Sets the selected item. A selection event is fired, if the component differs from the one
      * before.
      * 
      * @param selected
      */
     public void setSelected(SchemeComponent selected) {
         if (this.selected != selected) {
             this.selected = selected;
             if (listener != null) {
                 listener.selected(selected);
             }
            grid.removeColorbox(selectionBox);
             if (selected != null) {
                 Optional<Category> c = selected.getComponent().getCategory();
                 if (!c.isPresent()) {
                     // Shouldnt be possible
                     throw new RuntimeException("Internal Error");
                 }
                selectionBox = grid.setColorGrid(getColor(), selected.getX(), selected.getY(), c.get().getWidth(),
                         c.get().getHeight(), false, false);
            }else{
            	selectionBox = null;
             }
         }
     }
 
     public void setItems(Collection<SchemeGridItem> items) {
         setSelected(null);
         grid.setItems(items);
     }
 
     private Color getColor() {
         return grid.getDisplay().getSystemColor(SWT.COLOR_GREEN);
     }
    
    public void clearColors(){
    	grid.clearColors();
    	if(selectionBox != null){
    		grid.addColorbox(selectionBox);
    	}
    }
 
 	public Control getControl() {
 		return grid;
 	}
 
 	public void setColor(SchemeComponent c, Color color){
 		grid.setColorGrid(color, c.getX(), c.getY(), c.getWidth(), c.getHeight(), true, true);
 	}
 
 }
