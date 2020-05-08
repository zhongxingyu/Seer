 package ch9k.core.settings.gui;
 
 import ch9k.core.settings.event.PreferencePaneEvent;
 import ch9k.eventpool.Event;
 import ch9k.eventpool.EventFilter;
 import ch9k.eventpool.EventListener;
 import ch9k.eventpool.EventPool;
 import java.awt.CardLayout;
import java.awt.EventQueue;
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.GroupLayout;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 /**
  *
  * @author toon
  */
 public class PreferencesFrame extends JFrame implements EventListener {
 
     private CardLayout layout;
     private DefaultListModel listModel;
     private JPanel prefPane;
 
     public PreferencesFrame() {
         super("Preferences");
         setName("Preferences");
 
         EventPool.getAppPool().addListener(this, new EventFilter(PreferencePaneEvent.class));
 
         JPanel mainPanel = new JPanel();
 
         layout = new CardLayout();
         prefPane = new JPanel(layout);
 
         listModel = new DefaultListModel();
         final JList list = new JList(listModel);
 
         JScrollPane listPane = new JScrollPane(list);
 
         list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         list.addListSelectionListener(new ListSelectionListener() {
             public void valueChanged(ListSelectionEvent e) {
                 layout.show(prefPane, (String) listModel.get(list.getSelectedIndex()));
             }
         });
 
         GroupLayout groupLayout = new GroupLayout(mainPanel);
         mainPanel.setLayout(groupLayout);
 
         groupLayout.setAutoCreateContainerGaps(true);
         groupLayout.setAutoCreateGaps(true);
 
         groupLayout.setHorizontalGroup(groupLayout.createSequentialGroup()
                 .addComponent(listPane)
                 .addComponent(prefPane));
 
         groupLayout.setVerticalGroup(groupLayout.createSequentialGroup()
                 .addGroup(groupLayout.createParallelGroup()
                     .addComponent(listPane)
                     .addComponent(prefPane)));
 
         setContentPane(mainPanel);
         pack();
     }
 
     @Override
     public void handleEvent(Event ev) {
         PreferencePaneEvent event = (PreferencePaneEvent)ev;
 
         if(event.isPaneActive()) {
             listModel.addElement(event.getTitle());
             event.getPanel().setBorder(BorderFactory.createTitledBorder(event.getTitle()));
             prefPane.add(event.getPanel(), event.getTitle());
         } else {
             listModel.removeElement(event.getTitle());
             prefPane.remove(event.getPanel());
         }
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                pack();
                repaint();
            }
        });
 
     }
 
 }
