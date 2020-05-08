 package be.cegeka.rsvz;
 
 import org.richfaces.component.UIExtendedDataTable;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.event.AjaxBehaviorEvent;
 import java.io.Serializable;
import java.sql.SQLOutput;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 @ManagedBean(name = "dataBean")
 @SessionScoped
 public class DataBean implements Serializable {
 
     private final int SIZE = 40;
 
     private List<Child> children = new ArrayList<Child>();
     private Collection<Object> selection;
     private List<Child> selectedChildren;
 
     /*
     *  FOR EDITABLE TABLE
     */
     private List<Child> childrenEditableList = new ArrayList<Child>();
     private int currentChildIndex;
     private Child currentChild = new Child();
    private boolean childInAddMode = false;
 
     public DataBean() {
         loadChildrenForDataTable();
         loadChildrenForEditableList();
     }
 
     public void loadChildrenForDataTable() {
         populateList(children, 100);
     }
 
     public void loadChildrenForEditableList() {
         populateList(childrenEditableList, 10);
     }
 
     private void populateList(List<Child> children, int count) {
         for (int i = 1; i <= count; i++) {
             children.add(new Child(i, "child " + i + " fname", "child " + i + " lname", i, "M"));
         }
     }
 
     public void storeChild() {
         childrenEditableList.set(currentChildIndex, currentChild);
         System.out.println("Stored child: [" + currentChildIndex + "]" + currentChild.toString());
     }
 
     public void removeChild() {
         System.out.println("Deleted child: [" + currentChildIndex + "]" + childrenEditableList.get(currentChildIndex).toString());
         childrenEditableList.remove(childrenEditableList.get(currentChildIndex));
     }
 
    public void cancelChild() {
        System.out.println("Add mode: "+isChildInAddMode());
        if (isChildInAddMode()) {
            removeTempPositionInChildrenList();
        }
    }

    private void removeTempPositionInChildrenList() {
        childrenEditableList.remove(childrenEditableList.size() - 1);
    }


    public void createNewChild() {
         currentChild = new Child();
         childrenEditableList.add(currentChild);
         currentChildIndex = childrenEditableList.size() - 1;
     }
 
 
     public void showSelectionDetails(AjaxBehaviorEvent event) {
         UIExtendedDataTable table = (UIExtendedDataTable) event.getComponent();
         selectedChildren = new ArrayList<Child>();
         selection = table.getSelection();
         Object storedRowKey = table.getRowKey();
         System.out.println("Selected children:");
         for (Object rowKey : selection) {
             table.setRowKey(rowKey);
             selectedChildren.add((Child) table.getRowData());
             System.out.println("Child: " + table.getRowData());
         }
         table.setRowKey(storedRowKey);
     }
 
     public int getSize() {
         return SIZE;
     }
 
     public List<Child> getChildren() {
         return children;
     }
 
     public void setChildren(List<Child> children) {
         this.children = children;
     }
 
     public Collection<Object> getSelection() {
         return selection;
     }
 
     public void setSelection(Collection<Object> selection) {
         this.selection = selection;
     }
 
     public List<Child> getSelectedChildren() {
         return selectedChildren;
     }
 
     public void setSelectedChildren(List<Child> selectedChildren) {
         this.selectedChildren = selectedChildren;
     }
 
     public int getCurrentChildIndex() {
         return currentChildIndex;
     }
 
     public void setCurrentChildIndex(int currentChildIndex) {
         this.currentChildIndex = currentChildIndex;
     }
 
     public Child getCurrentChild() {
         return currentChild;
     }
 
     public void setCurrentChild(Child currentChild) {
         this.currentChild = currentChild;
     }
 
     public List<Child> getChildrenEditableList() {
         return childrenEditableList;
     }
 
     public void setChildrenEditableList(List<Child> childrenEditableList) {
         this.childrenEditableList = childrenEditableList;
     }

    public void setChildInAddMode(boolean childInAddMode) {
        this.childInAddMode = childInAddMode;
    }

    private boolean isChildInAddMode() {
        return childInAddMode;
    }

 }
