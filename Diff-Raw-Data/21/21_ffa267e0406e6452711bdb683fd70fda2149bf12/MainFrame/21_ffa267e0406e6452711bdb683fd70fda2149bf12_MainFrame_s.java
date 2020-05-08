 import javax.swing.*;
 import javax.swing.table.*;
 
 import java.awt.*;
 
 /**
  * Main class for GUI
  * 
  * @author cmcl
  * @version 1.0
  */
 public class MainFrame extends JFrame {
 	private static JPanel mainPanel;
 
 	private static final long serialVersionUID = 1L;
 
 	public MainFrame() {
 		super();
 	}
 
 	public void setupGui() {
 		setTitle("IS3 Calendar");
 		setSize(new Dimension(900, 700));
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         setJMenuBar(Common.createMenuBar());
         
 		mainPanel = new JPanel();
 		mainPanel.setLayout(new BorderLayout());
         
         // Create an upper panel to achieve the correct
         // layout.
         JPanel upperPanel = new JPanel(new BorderLayout());
         JPanel viewPanel = new JPanel(new BorderLayout());
         addViewButtons(viewPanel);
         upperPanel.add(viewPanel, BorderLayout.EAST);
         
         // Populate the main panel.
         mainPanel.add(upperPanel, BorderLayout.NORTH);
 		generateMonthGrid();
         
         getContentPane().add(mainPanel);
 		setVisible(true);
 	}
 
 	private void generateMonthGrid() {
		TableModel monthView = new MonthDataModel(3);
 		JTable month = new JTable(monthView);
         month.getTableHeader().setReorderingAllowed(false);
 		JScrollPane scrollPane = new JScrollPane(month);
 		month.setRowHeight(100);
 		mainPanel.add(scrollPane, BorderLayout.CENTER);
 	}
     
     private void addViewButtons(JPanel viewPanel) {
          // Create view buttons.
         JButton weekButton = new JButton("Week");
         JButton monthButton = new JButton("Month");
         JButton yearButton = new JButton("Year");
         
         viewPanel.add(weekButton, BorderLayout.WEST);
         viewPanel.add(monthButton, BorderLayout.CENTER);
         viewPanel.add(yearButton, BorderLayout.EAST);
     }
 }
