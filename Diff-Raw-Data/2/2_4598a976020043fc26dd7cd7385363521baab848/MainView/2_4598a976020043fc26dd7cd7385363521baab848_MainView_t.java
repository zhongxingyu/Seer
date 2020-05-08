 package ch.bli.mez.view;
 
 import java.awt.CardLayout;
 import java.awt.Dimension;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.UIManager;
 import javax.swing.event.ChangeListener;
 
 /**
  * MainFrame beinhaltet die oberen Tabs, ursprünglich "Zeiten erfassen", "Mitarbeiter verwalten", "Auswertungen", "Verwaltung"
  * @author dave
  * @version 1.0
  */
 public class MainView extends JFrame {
 
 	private static final long serialVersionUID = -8484150056391154851L;
 	private JTabbedPane tabbedPaneMain;
 
 	public MainView() {
 
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		getContentPane().setLayout(new CardLayout(0, 0));
 		setMinimumSize(new Dimension(1000, 600));
 		
 		try {
 			UIManager
 					.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		tabbedPaneMain = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add("Main", tabbedPaneMain);
 
 		// Tab Platzhalter hinzufügen
 		tabbedPaneMain.insertTab("Zeiten erfassen", null, new JPanel(), null, 0);
 		tabbedPaneMain.insertTab("Mitarbeiter verwalten", null, new JPanel(), null, 1);
 		tabbedPaneMain.insertTab("Auswertungen", null, new JPanel(), null, 2);
 		tabbedPaneMain.insertTab("Verwaltung", null, new JPanel(), null, 3);
 	}
 
 	/**
 	 * @@@Auskommentieren, sobald Klasse TimeMgmtView erstellt ist Panel
 	 * "Zeiten erfassen" setzen
 	 * 
 	 * @param timeMgmtView TimeMgmtView Objekt welches im Tab "Zeiten erfassen"
 	 * eingesetzt werden soll
 	 */
 	// public void setTimeMgmtPanel(TimeMgmtView timeMgmtView) {
 	// tabbedPaneMain.setComponentAt(0, timeMgmtView);
 	// }
 
 	/**
 	 * @@@Auskommentieren, sobald Klasse TimeMgmtView erstellt ist Panel
 	 * "Zeiten erfassen" ausgeben
 	 * 
 	 * @return TimeMgmtView das aktuell eingesetzte "Zeiten erfassen" Panel
 	 */
 	// public TimeMgmtView getTimeMgmtPanel() {
 	// return tabbedPaneMain.getTabComponentAt(0);
 	// }
 
 	/**
 	 * Panel "Mitarbeiter verwalten" setzen
 	 * 
 	 * @param employeeView EmployeeView Objekt welches im Tab
 	 * "Mitarbeiter verwalten" eingesetzt werden soll
 	 */
 	public void setEmployeePanel(EmployeeView employeeView) {
 		tabbedPaneMain.setComponentAt(1, employeeView);
 	}
 
 	/**
 	 * Panel "Mitarbeiter verwalten" ausgeben
 	 * 
 	 * @return EmployeeView das aktuell eingesetzte "Mitarbeiter verwalten"
 	 * Panel
 	 */
 	public EmployeeView getEmployeePanel() {
 		return (EmployeeView) tabbedPaneMain.getTabComponentAt(1);
 	}
 
 
 	public void setManagementPanel(ManagementView managementView){
 	  tabbedPaneMain.setComponentAt(3, managementView);
 	}
 	
 	public ManagementView getManagementPanel(){
 	  return (ManagementView) tabbedPaneMain.getTabComponentAt(3);
 	}
 	//Setter und Getter für "Auswertung" muss noch erstellt werden (internerKommentar)
 	
 	public void setTabChangeListener(ChangeListener cl){
 		tabbedPaneMain.addChangeListener(cl);
 	}
 
 }
