 package com.bluespot.collections.observable;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import org.jdesktop.application.Action;
 import org.jdesktop.application.ApplicationContext;
 import org.jdesktop.application.SingleFrameApplication;
 
 import com.bluespot.collections.observable.list.ObservableList;
 import com.bluespot.demonstration.Demonstration;
 
 /**
  * A very simple demo showing an {@link ObservableList} acting as a list model.
  * 
  * @author Aaron Faanes
  * 
  */
 public final class ObservableListDemonstration extends SingleFrameApplication {
 
     private final ObservableList<String> strings = new ObservableList<String>();
 
     private final JList list = new JList(this.strings);
 
    private final JButton addButton = new JButton("Add");
 
    private final JButton removeButton = new JButton("Remove");
 
     public ObservableListDemonstration(final ApplicationContext context) {
         super(context);
     }
 
     @Action
     protected void addElement() {
         this.strings.add("Hello, world! This is element " + this.strings.size());
         this.removeButton.setEnabled(true);
     }
 
     @Action
     protected void removeElement() {
         assert !this.strings.isEmpty();
         this.strings.remove(0);
         this.removeButton.setEnabled(!this.strings.isEmpty());
     }
 
     @Override
     protected void startup() {
         final JPanel panel = new JPanel(new BorderLayout());
         panel.setPreferredSize(new Dimension(400, 400));
 
         panel.add(new JScrollPane(this.list), BorderLayout.CENTER);
 
         final JPanel buttons = new JPanel();
         panel.add(buttons, BorderLayout.SOUTH);
        buttons.add(this.addButton);
        buttons.add(this.removeButton);
 
         this.addButton.setAction(this.getContext().getActionMap().get("addElement"));
         this.removeButton.setAction(this.getContext().getActionMap().get("removeElement"));
         this.removeButton.setEnabled(false);
 
         this.show(panel);
     }
 
     /**
      * Creates a new {@link ObservableListDemonstration} using the
      * {@link Demonstration} framework.
      * 
      * @param args
      *            unused
      */
     public static void main(final String[] args) {
         launch(ObservableListDemonstration.class, args);
     }
 
 }
