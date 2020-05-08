 package vahdin.view;
 
 import com.vaadin.ui.CustomLayout;
 
 public class MarksSubview extends Subview {
 
     public MarksSubview() {
        CustomLayout layout = new CustomLayout("marks-list");
         setCompositionRoot(layout);
         setSizeFull();
     }
 }
