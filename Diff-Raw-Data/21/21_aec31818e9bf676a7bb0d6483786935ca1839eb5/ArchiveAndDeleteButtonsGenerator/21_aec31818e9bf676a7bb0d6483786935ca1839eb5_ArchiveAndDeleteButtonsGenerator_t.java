 package com.jtbdevelopment.e_eye_o.ria.vaadin.components.filterabletables.generatedcolumns;
 
 import com.google.common.eventbus.EventBus;
 import com.jtbdevelopment.e_eye_o.DAO.ReadWriteDAO;
 import com.jtbdevelopment.e_eye_o.entities.AppUserOwnedObject;
 import com.jtbdevelopment.e_eye_o.ria.vaadin.utils.AllItemsBeanItemContainer;
 import com.vaadin.ui.Table;
 
 /**
  * Date: 3/16/13
  * Time: 6:42 PM
  */
 public class ArchiveAndDeleteButtonsGenerator<T extends AppUserOwnedObject> implements Table.ColumnGenerator {
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
