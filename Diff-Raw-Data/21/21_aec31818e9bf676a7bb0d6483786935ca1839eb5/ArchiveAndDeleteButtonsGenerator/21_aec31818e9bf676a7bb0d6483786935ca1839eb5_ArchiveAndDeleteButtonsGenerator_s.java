 package com.jtbdevelopment.e_eye_o.ria.vaadin.components.filterabletables.generatedcolumns;
 
 import com.google.common.eventbus.EventBus;
 import com.jtbdevelopment.e_eye_o.DAO.ReadWriteDAO;
 import com.jtbdevelopment.e_eye_o.entities.AppUserOwnedObject;
 import com.jtbdevelopment.e_eye_o.ria.vaadin.utils.AllItemsBeanItemContainer;
import com.vaadin.server.ThemeResource;
 import com.vaadin.ui.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
 /**
  * Date: 3/16/13
  * Time: 6:42 PM
  */
 public class ArchiveAndDeleteButtonsGenerator<T extends AppUserOwnedObject> implements Table.ColumnGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ArchiveAndDeleteButtonsGenerator.class);

    private static final ThemeResource ARCHIVE_ICON = new ThemeResource("icons/16/power_off.png");
    private static final ThemeResource ACTIVATE_ICON = new ThemeResource("icons/16/power_on.png");
    private static final ThemeResource DELETE_ICON = new ThemeResource("icons/16/delete.png");

     private final ReadWriteDAO readWriteDAO;
     private final EventBus eventBus;
     private final AllItemsBeanItemContainer<T> entities;
 
     public ArchiveAndDeleteButtonsGenerator(final ReadWriteDAO readWriteDAO, final EventBus eventBus, final AllItemsBeanItemContainer<T> entities) {
         this.readWriteDAO = readWriteDAO;
         this.eventBus = eventBus;
         this.entities = entities;
     }
 
     @Override
     public Object generateCell(final Table source, final Object itemId, final Object columnId) {
         final T entity = entities.getItem(itemId).getBean();
 
         return new ArchiveAndDeleteButtons<>(readWriteDAO, eventBus, entity);
     }
 }
