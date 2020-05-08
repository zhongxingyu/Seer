 package ch.bli.mez.view.management;
 
 import java.awt.FlowLayout;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyListener;
 
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import ch.bli.mez.view.DefaultSearchPanel;
 
 /**
  * SearchPanel ist das kleine Suchfeld welches oberhalb der Tabs mit
  * Mitarbeiternamen platziert wird Der SearchPanel wird für "Zeiten erfassen"
  * sowie "Mitarbeiter verwalten" verwendet
  * 
  * @author dave
  * @version 1.0
  */
 
 public class MissionSearchPanel extends DefaultSearchPanel {
 
   private static final long serialVersionUID = 5735685030723299266L;
   private JTextField missionName;
   private JPanel spacer;
 
   public MissionSearchPanel() {
     build();
   }
 
   private void build() {
     setLayout(new FlowLayout(FlowLayout.CENTER, 15, 0));
 
     missionName = new JTextField("Search...");
     this.add(missionName);
     missionName.setColumns(10);
     missionName.addFocusListener(new FocusListener() {
 
       public void focusLost(FocusEvent e) {
       }
 
       public void focusGained(FocusEvent e) {
         missionName.setText(null);
       }
     });
 
     spacer = new JPanel();
     FlowLayout flowLayout = (FlowLayout) spacer.getLayout();
    flowLayout.setHgap(235);
     flowLayout.setVgap(0);
     add(spacer);
   }
 
   public String getSearchText() {
     return "missionName=" + missionName.getText();
   }
 
   public void setKeyListener(KeyListener keyListener) {
     missionName.addKeyListener(keyListener);
   }
 }
