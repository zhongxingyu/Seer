 package net.premereur.mvp.example.swing.categorymgt;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 import net.premereur.mvp.core.View;
 
 /**
  * The work view to show when no single category is selected. I.e. an empty panel.
  * 
  * @author gpremer
  * 
  */
 public class DefaultCategoryPanel extends JPanel implements View {
 
     private static final long serialVersionUID = 1L;
 
    private JButton createBtn = new JButton("Add category");
 
     public DefaultCategoryPanel() {
         setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
         JPanel buttonPnl = new JPanel();
         buttonPnl.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10)); // NOCS MAGIC 0
         buttonPnl.add(createBtn);
         add(buttonPnl);
     }
 
     public final void setCreateButtonListener(final DefaultCategoryPanelPresenter presenter) {
         for (ActionListener listener : createBtn.getActionListeners()) {
             createBtn.removeActionListener(listener);
         }
         createBtn.addActionListener(new ActionListener() {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 presenter.createNewCategory();
             }
 
         });
     }
 
     protected void init() {
     }
 }
