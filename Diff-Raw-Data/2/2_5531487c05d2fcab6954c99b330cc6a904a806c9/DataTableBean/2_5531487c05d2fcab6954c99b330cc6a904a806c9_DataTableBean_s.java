 package be.cegeka.rsvz;
 
 import org.primefaces.component.api.UIData;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.component.UIColumn;
 import javax.faces.component.UIComponent;
 import javax.faces.event.ActionEvent;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 @ManagedBean(name = "dataBean")
 @SessionScoped
 public class DataTableBean implements Serializable {
 
     private static final int CAPACITY = 10;
    private List<Child> children = new ArrayList<Child>();
 
     public DataTableBean() {
         loadData();
     }
 
     public void loadData() {
         for (int i = 1; i <= CAPACITY; i++) {
             getChildren().add(new Child(i, "child " + i + " fname", "child " + i + " lname", i, "M"));
         }
     }
     public void createNewRow() {
         Child child = new Child();
         getChildren().add(child);
     }
 
 
     public List<Child> getChildren() {
         return children;
     }
 
     public void setChildren(List<Child> children) {
         this.children = children;
     }
 
     public int getSize() {
         return CAPACITY;
     }
 
     public void actionDeleteRow(ActionEvent actionEvent) {
         UIData component = (UIData)actionEvent.getComponent().getParent().getParent();
         Child child = (Child)component.getRowData();
         children.remove(child);
 //        printList("actionDeleteRow");
     }
     public void printList(String string) {
         System.out.println("=================="+string+"==================");
         for(Child child:children) {
             System.out.println("child = " + child);
         }
         System.out.println("=============================================================");
     }
 }
