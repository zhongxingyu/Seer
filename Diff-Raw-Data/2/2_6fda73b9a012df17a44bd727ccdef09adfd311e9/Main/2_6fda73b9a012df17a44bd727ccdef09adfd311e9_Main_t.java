 import java.awt.BorderLayout;
 import java.awt.Dimension;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 
 import listener.XMLUploadedListener;
 
 
 /**
  * 
  */
 
 /**
  * @author Luuk
  *
  */
 public class Main extends JFrame implements XMLUploadedListener {
 	/**
 	 * Panels voor elke fieldset
 	 */
 	private JPanel xmlPanel = new XMLPanel();
 	private JPanel executionPanel = new ExecutionPanel();
 	private JPanel customerPanel = new CustomerPanel();
 	private JPanel orderPanel = new OrderPanel();
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// zet de look and feel naar windows of osx
 		try
 		{
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch (Exception e)
 		{
 			System.out.println("Unable to load Windows look and feel");
 		}
 		
 		// creeer het scherm
 		JFrame main = new Main();
 		// TODO Auto-generated method stub
 	}
 	
 	public Main()
 	{
 		setSize(1500, 900);
 		
 		// sluit het proces als je op kruisje drukt
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		/**
 		 * Het scherm is op te delen in 2 kolommen
 		 * De flowlayout zorgt voor de kolommen en de panels 
 		 * zorgen dat we meerdere componenten in 1 kant kunnen stoppen
 		 */
 		setLayout(new BorderLayout());
 		
 		buildUI();
 		
 		// als laatste, maak hem zichtbaar
 		setVisible(true);
 	}
 	
 	private void buildUI()
 	{
 		/**
 		 * Linker en rechterkant van het scherm
 		 */
 		JPanel leftPanel = new JPanel();
 		JPanel rightPanel = new JPanel();
 		
 		/** 
 		 * zet de leftpanel op een breedte
 		 * de rechter heeft dit niet nodig omdat daar maar 1 panel in zit
 		 */
 		leftPanel.setPreferredSize(new Dimension(500, 900));
 
 		leftPanel.add(xmlPanel);
 		leftPanel.add(executionPanel);
 		leftPanel.add(customerPanel);
 		rightPanel.add(orderPanel);
 	
 		// plaats de panels
 		add(leftPanel, BorderLayout.WEST);
 		add(rightPanel, BorderLayout.CENTER);
 	}
 
 	@Override
	public void xmlUploaded(String xmlFileLocation) {
 		System.out.println(xmlFileLocation);
 	}
 
 }
