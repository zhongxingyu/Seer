 package package1;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.text.DateFormat;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import javax.print.attribute.standard.OutputDeviceAssigned;
 import javax.swing.*;
 
 import VariableInputApi.*;
 
 public class GUICampingReg extends JFrame implements ActionListener, Comparable<Site> {
 	
 	/** the serial version UID */
 	private static final long serialVersionUID = 1L;
 
 	/** JMenu for file */
 	private JMenu fileMenu;
 
 	/** JMenu for check in menu */
 	private JMenu checkInMenu;
 
 	/** JMenu for check out menu */
 	private JMenu checkOutMenu;
 
 	/** JMenu Item for saving serializable file */
 	private JMenuItem saveS;
 
 	/** JMenu Item for opening serializable file */
 	private JMenuItem openS;
 
 	/** JMenu Item for saving text file */
 	private JMenuItem saveT;
 
 	/** JMenu Item for opening text file */
 	private JMenuItem openT;
 
 	/** JMenu Item for exiting */
 	private JMenuItem quit;
 
 	/** JMenu Item for checking in a tent */
 	private JMenuItem checkInTent;
 
 	/** JMenu Item for checking in a RV */
 	private JMenuItem checkInRV;
 
 	/** JMenu Item for checking out */
 	private JMenuItem checkOut;
 
 	/** JMenu bar to hold the menus */
 	private JMenuBar menus;
 
 	/** JTable for holding the information */
 	private JTable table;
 
 	/** instance of SiteModel */
 	private SiteModel siteTableModel;
 
 	/** JScrollPanel to allow for scrolling */
 	private JScrollPane scrollPane;
 
 	/** Default Name */
 	private final String DEFAULT_NAME;
 
 	/** Default Site Number */
 	private final int DEFAULT_SITE_NUMBER;
 
 	/** Default Date */
 	private final String DEFAULT_DATE;
 
 	/** Default number of days staying */
 	private final int DEFAULT_DAYS_STAYING;
 
 	/** Default power use  */
 	private final int DEFAULT_POWER_USED;
 
 	/** Default number of days staying */
 	private final int DEFAULT_TENTERS;
 
 	/** Inputed value for name */
 	private String name;
 
 	/** Inputed value for site number */
 	private int siteNumber;
 
 	/** Inputed value for days staying */
 	private int daysStaying;
 
 	/** Inputed value for number of tenters */
 	private int numTenters;
 
 	/** Inputed value for power needed */
 	private int power;
 
 	/** Inputed value for date checked in */
 	private GregorianCalendar checkInDate;
 
 	/** Cost for the stay */
 	private double cost;
 
 	/******************************************************************
 	 * Sets up the GUI
 	 *****************************************************************/
 	public GUICampingReg(){
 		DEFAULT_NAME = "John Doe";
 		DEFAULT_SITE_NUMBER = 1;
 		DEFAULT_DATE = "10/15/2013";
 		DEFAULT_DAYS_STAYING = 1;
 		DEFAULT_POWER_USED = 30;
 		DEFAULT_TENTERS = 1;
 
 		// Instantiate the menus and menu items
 		fileMenu = new JMenu("File:");
 		checkInMenu = new JMenu("Check In:");
 		checkOutMenu = new JMenu("Check Out:");
 		saveS = new JMenuItem("Save Serializable");
 		openS = new JMenuItem("Open Serializable");
 		openT = new JMenuItem("Open Text");		
 		saveT = new JMenuItem("Save Text");
 		quit = new JMenuItem("Quit");
 		checkInTent = new JMenuItem("Check In Tent");
 		checkInRV = new JMenuItem("Check In RV");
 		checkOut = new JMenuItem("Check Out");
 		menus = new JMenuBar();
 
 		// add the menus and menu items to the frame
 		menus.add(fileMenu);
 		fileMenu.add(saveS);
 		fileMenu.add(openS);
 		fileMenu.add(saveT);
 		fileMenu.add(openT);
 		fileMenu.add(quit);
 		menus.add(checkInMenu);
 		checkInMenu.add(checkInTent);
 		checkInMenu.add(checkInRV);
 		menus.add(checkOutMenu);
 		checkOutMenu.add(checkOut);
 
 		// add ActionListeners
 		quit.addActionListener(this);
 		openT.addActionListener(this);
 		saveT.addActionListener(this);
 		openS.addActionListener(this);
 		saveS.addActionListener(this);
 		checkInTent.addActionListener(this);
 		checkInRV.addActionListener(this);
 		checkOut.addActionListener(this);
 
 		// set the menu bar
 		setJMenuBar(menus);
 
 		// instantiate the SiteModel and add it to the frame
 		siteTableModel = new SiteModel();
 		table = new JTable(siteTableModel);
 		scrollPane = new JScrollPane(table);
 		add(scrollPane);
 
 		// set the default close operation
 		setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
 
 		setSize(700,300);
 		setVisible(true);
 	}
 
 	/******************************************************************
 	 * Main method to run the GUI
 	 * @param args
 	 *****************************************************************/
 	public static void main(String[] args){
 		new GUICampingReg();
 	}
 
 	/******************************************************************
 	 * actionPerformed method for the buttons
 	 * @param event listens for buttons to be clicked
 	 *****************************************************************/
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		JComponent comp = (JComponent) event.getSource();
 
 		if(comp == quit){
 			System.exit(1);
 		}
 
 		if(comp == openS){
 			
 			JFileChooser fc = new JFileChooser();
 			File file;
 			int returnVal = fc.showOpenDialog(this);
 	        if (returnVal == JFileChooser.APPROVE_OPTION) {
 	            file = fc.getSelectedFile();
 	            siteTableModel.loadDatabase(file.getName());
 	        }
 			
 		}
 
 		if(comp == saveS){
 			siteTableModel.saveDatabase("siteDB");
 		}
 
 		if(comp == openT){
 			siteTableModel.loadFromText("siteDBText");
 		}
 
 		if(comp == saveT){
 			siteTableModel.saveAsText("siteDBText");
 		}
 
 		if(comp == checkInTent){
 			String[] labelsTent = {"Name Reserving:", "Site Number:", "Occupied On:", "Number of Tenters:", "Days Staying:"};
 
 			VarInputPanel vT = new VarInputPanel(labelsTent, DEFAULT_NAME, DEFAULT_SITE_NUMBER, DEFAULT_DATE, DEFAULT_TENTERS, DEFAULT_DAYS_STAYING);
 			int resultTent;
 
 			do {
				resultTent = JOptionPane.showConfirmDialog(null, vT, "New Game", 
 						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
 			} while(!checkInputForError(vT, resultTent));
 			if(resultTent == JOptionPane.OK_OPTION){
 				// got this to work
 				Object[] varResult = vT.getUpdatedVars();
 				
 				Tent t = new Tent(varResult);
 				
 				cost = t.getDaysStaying() * t.getNumOfTenters() * 3;
 				DecimalFormat df = new DecimalFormat("#.00");
 				JOptionPane.showMessageDialog(null, "You owe $" + df.format(cost));
 				
 				siteTableModel.addSite(t);
 			}
 		}
 
 		if(comp == checkInRV){
 
 			//DialogCheckInRV x = new DialogCheckInRV(this, r);
 			String[] labelsRV = {"Name Reserving:", "Site Number:", "Occupied On:", "Power needed:", "Days Staying:"};
 
 			VarInputPanel vR = new VarInputPanel(labelsRV, DEFAULT_NAME, DEFAULT_SITE_NUMBER, DEFAULT_DATE, DEFAULT_POWER_USED, DEFAULT_DAYS_STAYING);
 			int resultRV;
 
 			do {
				resultRV = JOptionPane.showConfirmDialog(null, vR, "New Game", 
 						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
 			} while(!checkInputForError(vR, resultRV));
 			if(resultRV == JOptionPane.OK_OPTION){
 				// got this to work
 				Object[] varResult = vR.getUpdatedVars();
 				
 				RV r = new RV(varResult);
 				
 				cost = r.getDaysStaying() * 30;
 				DecimalFormat df = new DecimalFormat("#.00");
 				JOptionPane.showMessageDialog(null, "You owe $" + df.format(cost));
 				
 				siteTableModel.addSite(r);
 			}
 
 		}
 
 		if(comp == checkOut){
 			int index = table.getSelectedRow();
 			if (index == -1)
 				JOptionPane.showMessageDialog(this,"NO");
 			else
 				siteTableModel.checkOut(index);
 		}
 
 	}
 
 	private boolean checkInputForError(VarInputPanel p, int i) {
		if (i==JOptionPane.OK_CANCEL_OPTION){
 			if(p.doUpdatedVarsMatchInput()) {
 				Object[] varResult = p.getUpdatedVars(); 				
 
 				if ((Integer)varResult[1] < 1) {
					JOptionPane.showMessageDialog(null, "MSG1.");
 					return false;
 				}
 				if ((Integer)varResult[1] > 5) {
					JOptionPane.showMessageDialog(null, "MSG2.");
 					return false;
 				}
 				return true;
 			}
 
 			JOptionPane.showMessageDialog(null, "Numbers out of range. " +
 					" Please check your inputs.");
 			return false;
 		}
 
 
 		return true;
 	}
 
 	public int compareTo(Site s) {
 		final int BEFORE = -1;
 		final int EQUAL = 0;
 		final int AFTER = 1;
 		if(this.name.equals( s.getNameReserving()))
 			return EQUAL;
 		else if(this.name.charAt(0) < s.getNameReserving().charAt(0))
 			return BEFORE;
 		else
 			return AFTER;
 	}
 
 
 }
