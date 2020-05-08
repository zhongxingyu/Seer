 package de.elendis.jpert.webapp.view;
 
 import com.vaadin.data.Container;
 import com.vaadin.data.util.BeanItemContainer;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.Window;
 import de.elendis.jpert.entities.Task;
 import de.elendis.jpert.service.Registry;
 import de.elendis.jpert.service.TaskService;
 import java.util.List;
 
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
        dataTable.setVisibleColumns( new String[] { "taskName", "taskDescription", "optimisticEstimate", "pessimisticEstimate", "nominalEstimate", "expectedDuration", "standardDeviation" });
        dataTable.setColumnHeaders( new String[] { "taskName", "taskDescription", "optimisticEstimate", "pessimisticEstimate", "nominalEstimate", "expectedDuration", "standardDeviation" });
         return dataTable;
     }
 
     private Container getTaskContainer() {
         List<Task> allTasks = ( (TaskService) Registry.get(Registry.RegistryKeys.TASK_SERVICE)).findAllTasks();
 
         BeanItemContainer c = new BeanItemContainer(Task.class);
         
         c.addAll(allTasks);
         
         return c;
     }
 
 }
