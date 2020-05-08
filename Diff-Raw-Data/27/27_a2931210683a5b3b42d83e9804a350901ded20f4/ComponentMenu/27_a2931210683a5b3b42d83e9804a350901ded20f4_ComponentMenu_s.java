 package ua.kharkov.kture.ot.view.swing.mainwindow.popupmenu;
 
 import com.google.inject.name.Named;
 import ua.kharkov.kture.ot.common.localization.MessageBundle;
 import ua.kharkov.kture.ot.common.math.Logic;
 import ua.kharkov.kture.ot.common.menu.AbstractPopupMenu;
 import ua.kharkov.kture.ot.view.swing.changemodel.events.DecomposeComponent;
 import ua.kharkov.kture.ot.view.swing.changemodel.events.DeleteSelectedElement;
 
 import javax.inject.Inject;
 
 /**
  * @author: Stanislav Kurilin
  * @mail: st dot kurilin at gmail dot com
  * Date: 07.05.11
  */
 public class ComponentMenu extends AbstractPopupMenu {
     @Inject
     public ComponentMenu(@Named("popup menu") MessageBundle messageBundle) {
         super(messageBundle);
     }
 
     @Override
     protected void configure() {
         bind("Remove").toEvent(DeleteSelectedElement.class);
         bind("Decompose").submenu()
                 .bind(messageBundle.getMessage("And")).toEvent(new DecomposeComponent(Logic.AND))
                .bind(messageBundle.getMessage("Or")).toEvent(new DecomposeComponent(Logic.OR))
                .bind(messageBundle.getMessage("Not")).toEvent(new DecomposeComponent(Logic.NOT))
                .bind(messageBundle.getMessage("MagicAnd")).toEvent(new DecomposeComponent(Logic.AND_WITH_ORDER));
     }
 }
