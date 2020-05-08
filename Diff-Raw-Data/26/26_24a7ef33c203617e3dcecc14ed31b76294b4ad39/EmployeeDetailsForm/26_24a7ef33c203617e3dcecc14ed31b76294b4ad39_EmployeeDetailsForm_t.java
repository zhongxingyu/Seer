 package src.view.backing;
 
 import javax.faces.event.ActionEvent;
 
import javax.faces.event.ValueChangeEvent;

 import oracle.binding.OperationBinding;
 
 public class EmployeeDetailsForm extends BaseForm {
   private static String EMPLOYEE_LIST_TASK_FLOW = "/WEB-INF/flows/employee-list-task-flow.xml#employee-list-task-flow";
     
   public void saveAndClose(ActionEvent actionEvent) {
       save();
       setCurrentTabClean();
       closeCurrentTab();
       activateTab(EMPLOYEE_LIST_TASK_FLOW);
   }
 
   private void save() {
       OperationBinding oper = getOperationBinding("mergeEmployee");
       oper.execute();
   }
   
   public void cancel(ActionEvent actionEvent) {
       setCurrentTabClean();
       closeCurrentTab();
       //tries to activate the Overview tab if present
       activateTab(EMPLOYEE_LIST_TASK_FLOW);
   }
  
  public void jobChanged(ValueChangeEvent e){
      System.out.println("Success");
  }
 }
