 package ua.kharkov.kture.ot.view.swing.mainwindow.popupmenu;
 
 import com.google.inject.name.Named;
 import ua.kharkov.kture.ot.common.localization.MessageBundle;
 import ua.kharkov.kture.ot.common.math.Logic;
 import ua.kharkov.kture.ot.common.menu.AbstractPopupMenu;
 import ua.kharkov.kture.ot.view.swing.changemodel.events.*;
 
 import javax.inject.Inject;
 
 /**
  * @author: Stanislav Kurilin
  * @mail: st dot kurilin at gmail dot com
  * Date: 07.05.11
  */
 public class HazardMenu extends AbstractPopupMenu {
     @Inject
     public HazardMenu(@Named("popup menu") MessageBundle messageBundle) {
         super(messageBundle);
     }
 
     @Override
     protected void configure() {
         //TODO: [stas] deal with double mapping from string to logic
         //TODO: [stas] reimplement localization mechanism
         bind("Type").submenu()
                 .bind(messageBundle.getMessage("And")).toEvent(new ChangeLogicEvent(Logic.AND))
                .bind(messageBundle.getMessage("Or")).toEvent(new ChangeLogicEvent(Logic.OR))
                .bind(messageBundle.getMessage("Not")).toEvent(new ChangeLogicEvent(Logic.NOT))
                .bind(messageBundle.getMessage("MagicAnd")).toEvent(new ChangeLogicEvent(Logic.AND_WITH_ORDER));
         bind("AddComponent").toEvent(CreateNewComponentEvent.class);
         bind("AddHazard").submenu()
                 .bind(messageBundle.getMessage("And")).toEvent(new CreateNewHazard(Logic.AND))
                .bind(messageBundle.getMessage("Or")).toEvent(new CreateNewHazard(Logic.OR))
                .bind(messageBundle.getMessage("Not")).toEvent(new CreateNewHazard(Logic.NOT))
                .bind(messageBundle.getMessage("MagicAnd")).toEvent(new ChangeLogicEvent(Logic.AND_WITH_ORDER));
         bind("Twins").toEvent(new ChangeTwinsEvent(2));
         bind("Remove").toEvent(DeleteSelectedElement.class);
     }
 }
