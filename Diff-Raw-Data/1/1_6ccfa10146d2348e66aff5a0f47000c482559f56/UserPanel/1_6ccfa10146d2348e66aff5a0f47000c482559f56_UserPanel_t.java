 // %2224380850:de.hattrickorganizer.gui.menu.option%
 package de.hattrickorganizer.gui.menu.option;
 
 import de.hattrickorganizer.gui.templates.ImagePanel;
 import de.hattrickorganizer.model.HOVerwaltung;
 import de.hattrickorganizer.model.User;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import java.util.ArrayList;
 import java.util.regex.Pattern;
 
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 
 /**
  * option panel for usermanagement
  *
  * @author Thorsten Dietz
  */
 public class UserPanel extends ImagePanel implements ActionListener {
     //~ Static / Instance fields ----------------------------------------------------------------------------
 
 	private static final long serialVersionUID = 1L;
 	private JButton m_jbDeleteUser = null;
     private JButton m_jbNewUser = null;
     private JButton m_jbSaveUser = null;
     private JTable table;
     private String[] columnNames = new String[]{
 			HOVerwaltung.instance().getLanguageString("Benutzername"),
 			HOVerwaltung.instance().getLanguageString("Datenbank"),
 			HOVerwaltung.instance().getLanguageString("AnzahlHRF").replaceAll("HRF","ZIP")
 	};
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * Panel for usermanagement
      * @author Thorsten Dietz
      * @since 1.36
      */
     protected UserPanel() {
         initComponents();
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * TODO Missing Method Documentation
      *
      * @param e TODO Missing Method Parameter Documentation
      */
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() == m_jbNewUser) {
             User.addNewUser();
             refresh();
         }
 
         if (e.getSource() == m_jbDeleteUser) {
             ArrayList<User> users = User.getAllUser();
             users.remove(table.getSelectedRow());
             refresh();
         }
 
        	if(e.getSource() == m_jbSaveUser){
     			ArrayList<User> users = User.getAllUser();
     			
     			for (int i = 0; i < table.getRowCount(); i++) {
     				User user = users.get(i);
     				
     				if(Pattern.matches("\\w*", table.getValueAt(i,0).toString()))
     						user.setName(table.getValueAt(i,0).toString());
     				else{
     					JOptionPane.showMessageDialog(getTopLevelAncestor(),table.getValueAt(i,0)+" is incorrect!");
     					return;
     				}
     				
     				if (user.isHSQLDB()) {
 						user.setPath(table.getValueAt(i,1).toString());
     				}    				
     				
     				try{
     				
     					int level = Integer.parseInt(table.getValueAt(i,2).toString().trim());
     					user.setBackupLevel(level);
     				
     				} catch (Exception ex){
     					JOptionPane.showMessageDialog(getTopLevelAncestor(),table.getValueAt(i,2)+" is not a number!");
     					return;
     				}
     				}
     			User.save();
         }
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     protected DefaultTableModel getModel() {
         Object[] users = User.getAllUser().toArray();
         Object[][] value = new Object[users.length][3];
         User tmp = null;
 
         for (int i = 0; i < users.length; i++) {
             tmp = ((User) users[i]);
             value[i][0] = tmp.getName();
             if (tmp.isHSQLDB()) {
 				value[i][1] = tmp.getDBPath();
 				value[i][2] = tmp.getBackupLevel()+"";				
             } else {
 				value[i][1] = "Real DB";
 				value[i][2] = "0";
             }            
         }
 
         DefaultTableModel model = new DefaultTableModel(value, columnNames);
         return model;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private JPanel getMiddlePanel() {
         table = new JTable(getModel());
         table.getTableHeader().setVisible(true);
         table.getTableHeader().setReorderingAllowed(false);
         table.setOpaque(false);
         JScrollPane scroll = new JScrollPane(table);
         scroll.setOpaque(false);
         JPanel panel = new ImagePanel();
         panel.setLayout(new BorderLayout());
         panel.add(scroll, BorderLayout.CENTER);
         panel.setPreferredSize(new Dimension(100, 300));
         return panel;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private JPanel getTopPanel() {
         JPanel topPanel = new ImagePanel();
         m_jbNewUser = new JButton(HOVerwaltung.instance().getLanguageString("Hinzufuegen"));
         m_jbNewUser.addActionListener(this);
         m_jbDeleteUser = new JButton(HOVerwaltung.instance().getLanguageString("loeschen"));
         m_jbDeleteUser.addActionListener(this);
         m_jbSaveUser = new JButton(HOVerwaltung.instance().getLanguageString("Speichern"));
         m_jbSaveUser.addActionListener(this);
 
         topPanel.add(m_jbNewUser);
         topPanel.add(m_jbDeleteUser);
         topPanel.add(m_jbSaveUser);
 
         return topPanel;
     }
 
     /**
      * TODO Missing Method Documentation
      */
     private void initComponents() {
         setLayout(new BorderLayout());
         add(getTopPanel(), BorderLayout.NORTH);
         add(getMiddlePanel(), BorderLayout.CENTER);
     }
 
     /**
      * TODO Missing Method Documentation
      */
     private void refresh() {
         table.setModel(getModel());
     }
 }
