 package gui;
 
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JToolBar;
 import javax.swing.JButton;
 import javax.swing.JList;
 import javax.swing.DefaultListModel;
 import javax.swing.SwingUtilities;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.BoxLayout;
 import javax.swing.Action;
 import javax.swing.AbstractAction;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Component;
 
 import java.awt.event.ActionEvent;
 
 import base.Organization;
 import base.Person;
 import base.Area;
 import base.XMLOrgLoader;
 
 import test.TestLoader;
 
 /**
  * The MayoSeater is the base structure of the seating application.
  * It consists of the frame and outer interface of the app.
  *
  * @author Oren Ely
  *
  */
 public class MayoSeater extends JPanel {
   /**
   *
   */
   private static final long serialVersionUID = -1065761336704032574L;
   /**
    * The preferred width of the Application Panel.
    */
   static final int PREFERRED_APP_WIDTH = 800;
   /**
    * The preferred height of the Application Panel.
    */
   static final int PREFERRED_APP_HEIGHT = 600;
   /**
    * The starting width of the area and people panels.
    */
   static final int STARTING_PANEL_WIDTH = 160;
   /**
    * The height of the tool bar panel.
    */
   static final int TOOLBAR_HEIGHT = 40;
   /**
    * The height of the buttons.
    */
   static final int BUTTON_HEIGHT = 20;
   /**
    * The name of the application. Used in the title bar.
    */
   static final String APPLICATION_NAME = "Mayo Seater";
   /**
    * A reference to the containing frame.
    */
   private final JFrame frame;
   /**
    * A reference to the organization being manipulated.
    */
   private Organization organization;
   /**
    * Represents an up-to-date model of the People in the organization.
    */
   private DefaultListModel<Person> personModel;
   /**
    * Represents an up-to-date model of the Areas in the organization.
    */
   private DefaultListModel<Area> areaModel;
 
   /**
    * This Action creates a new blank Organization.
    */
   private final Action newAction = new AbstractAction() {
     /** The SVUID */
     private static final long serialVersionUID = 3003587364492370843L;
 
     public void actionPerformed(final ActionEvent e) {
       setOrganization(new Organization("New Organization"));
     }
   };
 
   /**
    * This Action quits the Application.
    */
   private final Action quitAction = new AbstractAction() {
     /** The SVUID*/
     private static final long serialVersionUID = -4841054297554920462L;
 
     public void actionPerformed(final ActionEvent e) {
       System.exit(0);
     }
   };
 
   /**
    * Builds and initializes the application.  Requires a reference to the
    * containing frame.
    * @param mFrame The parent JFrame.
    */
   public MayoSeater(final JFrame mFrame) {
     super();
     //Create the frame;
     this.frame = mFrame;
     frame.setJMenuBar(makeMenuBar());
 
     setLayout(new BorderLayout());
     setPreferredSize(
         new Dimension(PREFERRED_APP_WIDTH, PREFERRED_APP_HEIGHT));
 
   //Create and add the tool bar
     JToolBar toolBar = new JToolBar();
     toolBar.setPreferredSize(
         new Dimension(PREFERRED_APP_WIDTH, TOOLBAR_HEIGHT));
     add(toolBar, BorderLayout.NORTH);
     toolBar.add(new JButton("Delete Area"));
     toolBar.add(new JButton("Delete Person"));
 
     //Create and add the Person Panel
     personModel = new DefaultListModel<Person>();
     JPanel personPanel = buildPanel("Add Person");
 
     JList<Person> list = new JList<Person>(personModel);
     list.setAlignmentX(JList.CENTER_ALIGNMENT);
     list.setMaximumSize(new Dimension(STARTING_PANEL_WIDTH,
         PREFERRED_APP_HEIGHT - BUTTON_HEIGHT));
     personPanel.add(list);
 
     add(personPanel, BorderLayout.EAST);
 
     //Create and add the Area Panel
     areaModel = new DefaultListModel<Area>();
     JPanel areaPanel = buildPanel("Add Area");
     areaPanel.add(new JList<Area>(areaModel));
     add(areaPanel, BorderLayout.WEST);
     areaPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
 
     setOrganization(new Organization("New Organization"));
   }
 
   /**
    * Builds a JPanel with a vertical BoxLayout containing a button.
    * @param buttonName The name of the button being placed in the panel.
    * @return the JPanel.
    */
   private JPanel buildPanel(final String buttonName) {
     JPanel panel = new JPanel();
     panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
     panel.setBorder(BorderFactory.createEtchedBorder());
     panel.setPreferredSize(
         new Dimension(STARTING_PANEL_WIDTH, PREFERRED_APP_HEIGHT));
     panel.setVisible(true);
 
     JButton button = new JButton(buttonName);
     button.setAlignmentX(JButton.CENTER_ALIGNMENT);
     button.setMaximumSize(new Dimension(STARTING_PANEL_WIDTH, BUTTON_HEIGHT));
 
     panel.add(button);
 
     return panel;
   }
 
   /**
    * Sets the organization being viewed in the application.
    * @param org The organization to be viewed in the application.
    */
   private void setOrganization(final Organization org) {
     this.organization = org;
     personModel.clear();
     for (Person bloke : org.getPeople()) {
       personModel.addElement(bloke);
     }
     areaModel.clear();
     for (Area area : org.getAreas()) {
       areaModel.addElement(area);
     }
     frame.setTitle(APPLICATION_NAME + ": " + org.getName());
   }
 
   /**
    * Creates and returns a Menu Bar customized to work with the application.
    * @return the created Menu Bar.
    */
   private JMenuBar makeMenuBar() {
     JMenuBar menuBar = new JMenuBar();
     JMenu fileMenu = new JMenu("File");
     fileMenu.add(makeMenuItem("New", newAction));
     fileMenu.add(makeMenuItem("Open", null));
     fileMenu.add(makeMenuItem("Save", null));
     fileMenu.add(makeMenuItem("Quit", quitAction));
     menuBar.add(fileMenu);
 
     JMenu editMenu = new JMenu("Edit");
     editMenu.add(makeMenuItem("Add Area", null));
     editMenu.add(makeMenuItem("Add Person", null));
     menuBar.add(editMenu);
 
     return menuBar;
   }
 
   /**
    * Makes and returns a menuItem with the given name. The application is then
    * added as an action listener to the created item.
    * @param name The name of the menu item.
    * @param action The action associated with the menu item.
    * @return The created MenuItem.
    */
   private JMenuItem makeMenuItem(final String name, final Action action) {
     JMenuItem item = new JMenuItem(name);
     item.addActionListener(action);
     return item;
   }
 
   /**
    * Sets the area being viewed in the AreaCanvas.
    * @param areaKey The ID of the Area to be viewed.
    */
   private void viewArea(final String areaKey) {
     add(new AreaCanvas(organization.getArea(areaKey)));
   }
 
   /**
    * Creates and begins the application.
    */
   private static void createAndShowGUI() {
     //Build the frame and start the mayoApp
     JFrame frame = new JFrame("Mayo Seater");
     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     MayoSeater ms = new MayoSeater(frame);
     frame.setContentPane(ms);
     frame.setJMenuBar(ms.makeMenuBar());
 
     //Create an organization using the test loader
     Organization org = new Organization(new TestLoader());
     ms.setOrganization(org);
     frame.setTitle(APPLICATION_NAME + ": " + org.getName());
     frame.pack();
     frame.setVisible(true);
   }
 
   /**
    * Runs the application.
    * @param args No purpose here.
    */
   public static void main(final String[] args) {
     //No idea what this does
     SwingUtilities.invokeLater(new Runnable() {
       public void run() {
 	
 
      createAndShowGUI();
      
      
      
      
         
         XMLOrgLoader xload = new XMLOrgLoader();
         
         xload.clear();	// this clears out binding
 		xload.test();	// this creates a test sample "test2.xml"
 		
 		
         xload.read("test2.xml");
 	
 		if (xload.personStackEmpty() != true)
 		{
 			System.out.println("Popping a person");
 			System.out.println(xload.popPerson().getName());
 		}
 		
 		                
       }
     });
   }
 }
