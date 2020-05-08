 package edu.msoe.se2800.h4.administrationFeatures;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.DropMode;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 
 public class UserListUI extends JDialog {
 
 	/** Generated serialVersionUID */
 	private static final long serialVersionUID = -6457636746676490029L;
 	
 	/**
 	 * UserListController object
 	 */
 	private UserListController controller;
 	
 	/**
 	 * JPanel for all of the content
 	 */
 	private JPanel contentPane;
 	
 	/**
 	 * JLists for the Observers, Programmers, and Administrators
 	 */
     private JList listObservers, listProgrammers, listAdministrators;
     
     /**
      * Constructor that initializes all of the UI elements
      * @param observers List<String>
      * @param programmers List<String>
      * @param administrators List<String>
      * @param controller UserListController object
      */
     public UserListUI(List<String> observers, List<String> programmers, List<String> administrators, UserListController controller) {
     	this.controller = controller;
     	//Create and set up the window.
         setTitle("User List");
         setModal(true);
         
         setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
         //leave the controller the responsibility of closing the dialog and updating users
         addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 UserListUI.this.controller.onClose((DefaultListModel)listObservers.getModel(), (DefaultListModel)listProgrammers.getModel(), (DefaultListModel)listAdministrators.getModel());
                 dispose();
             }
         });
 
         setResizable(false);
         setPreferredSize(new Dimension(900,500));
         
         contentPane = new JPanel(new BorderLayout());
         JPanel leftPanel = createVerticalBoxPanel();
         JPanel centerPanel = createVerticalBoxPanel();
         JPanel rightPanel = createVerticalBoxPanel();
 
         //Create a table model.
         DefaultListModel lmObservers = new DefaultListModel();
         for (String s : observers) {
         	lmObservers.addElement(s);
         }
         
         //Create a table model.
         DefaultListModel lmProgrammers = new DefaultListModel();
         for (String s : programmers) {
         	lmProgrammers.addElement(s);
         }
         
         //Create a table model.
         DefaultListModel lmAdministrators = new DefaultListModel();
         for (String s : administrators) {
         	lmAdministrators.addElement(s);
         }
         
         listObservers = new JList(lmObservers);
         listProgrammers = new JList(lmProgrammers);
         listAdministrators = new JList(lmAdministrators);
 
         //LEFT COLUMN
         listObservers.setDragEnabled(true);
         listObservers.setPreferredSize(new Dimension(300,500));
         listObservers.setDropMode(DropMode.INSERT);
         listObservers.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         listObservers.setTransferHandler(new ListTransferHandler(listObservers));
         leftPanel.add(createPanelForComponent(listObservers, "Observers"));
         listObservers.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent evt) {
                 JList list = (JList)evt.getSource();
                 if (evt.getClickCount() == 2 || evt.getClickCount() == 3) {
                     int index = list.locationToIndex(evt.getPoint());
                     UserListUI.this.controller.showChangePassword(listObservers.getModel().getElementAt(index).toString());
                 }
             }
         });
         setKeyBindings(listObservers);
         
         //CENTER COLUMN
         listProgrammers.setDragEnabled(true);
         listProgrammers.setPreferredSize(new Dimension(300,500));
         listProgrammers.setDropMode(DropMode.INSERT);
         listProgrammers.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         listProgrammers.setTransferHandler(new ListTransferHandler(listProgrammers));
         centerPanel.add(createPanelForComponent(listProgrammers, "Programmers"));
         listProgrammers.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent evt) {
                 JList list = (JList)evt.getSource();
                 if (evt.getClickCount() == 2 || evt.getClickCount() == 3) {
                     int index = list.locationToIndex(evt.getPoint());
                     UserListUI.this.controller.showChangePassword(listProgrammers.getModel().getElementAt(index).toString());
                 }
             }
         });
         setKeyBindings(listProgrammers);
         
         //RIGHT COLUMN
         listAdministrators.setDragEnabled(true);
         listAdministrators.setPreferredSize(new Dimension(300,500));
         listAdministrators.setDropMode(DropMode.INSERT);
         listAdministrators.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         listAdministrators.setTransferHandler(new ListTransferHandler(listAdministrators));
         rightPanel.add(createPanelForComponent(listAdministrators, "Administrators"));
         listAdministrators.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent evt) {
                 JList list = (JList)evt.getSource();
                 if (evt.getClickCount() == 2 || evt.getClickCount() == 3) {
                     int index = list.locationToIndex(evt.getPoint());
                     UserListUI.this.controller.showChangePassword(listAdministrators.getModel().getElementAt(index).toString());
                 }
             }
         });
         setKeyBindings(listAdministrators);
         
         contentPane.add(leftPanel, BorderLayout.WEST);
         contentPane.add(centerPanel, BorderLayout.CENTER);
         contentPane.add(rightPanel, BorderLayout.EAST);
         contentPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
         
         contentPane.setOpaque(true); //content panes must be opaque
         setContentPane(contentPane);
         
         //Display the window.
         pack();
         setVisible(true);
     }
     
     /**
      * Gets the list of observers
      * @return DefaultListModel of Observers
      */
     public DefaultListModel getObserversListModel() {
     	return (DefaultListModel)listObservers.getModel();
     }
     
     /**
      * Gets the list of Programmers
      * @return DefaultListModel of Programmers
      */
     public DefaultListModel getProgrammersListModel() {
     	return (DefaultListModel)listProgrammers.getModel();
     }
     
     /**
      * Gets the list of Administrators
      * @return DefaultListModel of Administrators
      */
     public DefaultListModel getAdministratorsListModel() {
     	return (DefaultListModel)listAdministrators.getModel();
     }
 
     /**
      * Protected method that creates a vertical box pane
      * @return JPanel 
      */
     protected JPanel createVerticalBoxPanel() {
         JPanel p = new JPanel();
         p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
         p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
         return p;
     }
 
     /**
      * Creats a panel for the parameters comp and title
      * @param comp JComponent
      * @param title String
      * @return JPanel
      */
     public JPanel createPanelForComponent(JComponent comp, String title) {
         JPanel panel = new JPanel(new BorderLayout());
         panel.add(comp, BorderLayout.CENTER);
         if (title != null) {
             panel.setBorder(BorderFactory.createTitledBorder(title));
         }
         return panel;
     }
     
     private void setKeyBindings(final JList list) {
         list.getInputMap(JComponent.WHEN_FOCUSED)
         	.put(KeyStroke.getKeyStroke("DELETE"), "clickDelete");
         list.getActionMap().put("clickDelete", new EnterListener(list));
         list.getInputMap(JComponent.WHEN_FOCUSED)
         	.put(KeyStroke.getKeyStroke("BACK_SPACE"), "clickBackSpace");
         list.getActionMap().put("clickBackSpace", new EnterListener(list));
     }
     
     @SuppressWarnings("serial")
 	public class EnterListener extends AbstractAction {
     	
     	private JList list;
     	
     	public EnterListener(JList list) {
     		this.list = list;
     	}
     	
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			try {
				System.out.println("You pushed the delete button mr mother fucker on the name "+list.getModel().getElementAt(list.getSelectedIndex()));
 				int index = list.getSelectedIndex();
 				boolean success = controller.deleteUser(list.getModel().getElementAt(index).toString());
 				if (success) {
 					((DefaultListModel)list.getModel()).remove(index);
 				}
 			} catch (ArrayIndexOutOfBoundsException aioobe) {
 				//pass this happens when no one is selected and the delete button is pressed
 			}
 		}
     }
 
 }
