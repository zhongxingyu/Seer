 package de.elendis.jpert.webapp.view;
 
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
 import com.vaadin.data.Container;
 import com.vaadin.data.util.BeanItemContainer;
 import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.Window;
 import de.elendis.jpert.entities.Task;
 import de.elendis.jpert.service.Registry;
 import de.elendis.jpert.service.TaskService;
 import java.util.List;
import static de.elendis.jpert.webapp.view.I18N.*;
 
 /**
  *
  * @author skywalker
  */
 public class JPertMainWindow extends Window { 
     
     public JPertMainWindow() {
         super("jPERT");
     }
     
     public void init() {        
         HorizontalLayout layout = new HorizontalLayout();
         Table dataTable = createDataTable();
         layout.addComponent(dataTable);
         setContent(layout);
         setClosable(false);
         setResizable(false);
     }
 
     private Table createDataTable() {
         Table dataTable = new Table(null, getTaskContainer());
        dataTable.setVisibleColumns( new String[] { TASK_NAME.getProp(), TASK_DESCRIPTION.getProp(), OPTIMISTIC_ESTIMATE.getProp(), PESSIMISTIC_ESMITATE.getProp(), 
            NOMINAL_ESTIMATE.getProp(), EXPECTED_DURATION.getProp(), STANDARD_DEVIATION.getProp() });
        dataTable.setColumnHeaders(new String[] { TASK_NAME.getValue(), TASK_DESCRIPTION.getValue(), OPTIMISTIC_ESTIMATE.getValue(), PESSIMISTIC_ESMITATE.getValue(), 
            NOMINAL_ESTIMATE.getValue(), EXPECTED_DURATION.getValue(), STANDARD_DEVIATION.getValue() });
         return dataTable;
     }
 
     private Container getTaskContainer() {
         List<Task> allTasks = ( (TaskService) Registry.get(Registry.RegistryKeys.TASK_SERVICE)).findAllTasks();
 
         BeanItemContainer c = new BeanItemContainer(Task.class);
         
         c.addAll(allTasks);
         
         return c;
     }
 
 }
