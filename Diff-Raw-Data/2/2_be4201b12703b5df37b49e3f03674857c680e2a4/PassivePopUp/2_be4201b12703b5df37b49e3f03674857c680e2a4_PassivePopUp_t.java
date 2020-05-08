 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JCheckBox;
 import javax.swing.JMenuBar;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 
 import java.io.*;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 
 public class PassivePopUp extends JDialog {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	public JMenu[] passiveCapacity; //number of weapons an enemy ship can carry
 	public JMenuItem[] availablePassives; //array of ALL weapons available
 	public String[] passiveNames; //names of all the weapons the enemy has
 	public String[] availablePassiveNames; //name of ALL weapons available
 	PassivePopUp self = null;
 	
 	private final JPanel contentPanel = new JPanel();
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) throws IOException{
 		try {
 			PassivePopUp dialog = new PassivePopUp(1);
 			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 			dialog.setVisible(true);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Create the dialog.
 	 * @throws FileNotFoundException 
 	 */
 	public PassivePopUp(int passives) throws FileNotFoundException {
 		self = this;
 		this.setTitle("Select Passive(s) for Enemy To Be Placed");
 		final String dir = System.getProperty("user.dir");
         System.out.println("current dir = " + dir);
 		Scanner in = new Scanner(new File("Passives"));
 		
 		int lengthOfArray = Integer.parseInt(in.nextLine()); //grab the first line and convert to an int
 		availablePassives = new JMenuItem[lengthOfArray];
 		availablePassiveNames = new String[lengthOfArray];
 		
 		for(int i = 0; i < lengthOfArray; i++){
 			availablePassiveNames[i] = in.nextLine(); //parse over all the weaponNames to build the menu items
 		}
 		
 		passiveCapacity = new JMenu[passives];
 		passiveNames = new String[passives];
 		for(int i = 0; i < passiveCapacity.length; i++){
 			passiveCapacity[i] = new JMenu("Passive " + (i+1));
 		}
		for(int i = 0; i < passiveNames.length; i++){ passiveNames[i] = ""; }
 		
 		in.close();
 		
 		setBounds(100, 100, 450, 300);
 		getContentPane().setLayout(new BorderLayout());
 		contentPanel.setLayout(new FlowLayout());
 		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
 		getContentPane().add(contentPanel, BorderLayout.CENTER);
 		{
 			JPanel buttonPane = new JPanel();
 			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
 			getContentPane().add(buttonPane, BorderLayout.SOUTH);
 			/*
 			{
 				
 				JButton okButton = new JButton("OK");
 				okButton.setActionCommand("OK");
 				buttonPane.add(okButton);
 				getRootPane().setDefaultButton(okButton);
 				okButton.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent arg0) {
 						
 						
 					}
 				});
 				
 			}
 			*/
 			/*
 			{
 				
 				JButton cancelButton = new JButton("Cancel");
 				cancelButton.setActionCommand("Cancel");
 				buttonPane.add(cancelButton);
 				
 			}
 			 */
 		}
 		{
 
 			JMenuBar menuBar = new JMenuBar();
 			setJMenuBar(menuBar);
 			{
 				for (int i = 0; i < passiveCapacity.length; i++){
 					final int menuWeapon = i;
 	
 					menuBar.add(passiveCapacity[i]);
 					{
 						for(int j = 0; j < availablePassives.length; j++){
 							final int selectedPassive = j;
 							
 							String s = availablePassiveNames[j];
 							String name = s.substring(s.lastIndexOf(".")+1);
 							
 							//availablePassives[j] = new JMenuItem(availablePassiveNames[j]);
 							availablePassives[j] = new JMenuItem(name);
 							availablePassives[j].addActionListener(new ActionListener(){
 								@Override
 								public void actionPerformed(ActionEvent e) {
 									passiveNames[menuWeapon] = availablePassiveNames[selectedPassive];
 									String s = availablePassiveNames[selectedPassive];
 									String displayName = s.substring(s.lastIndexOf(".")+1);
 									passiveCapacity[menuWeapon].setText(displayName);
 								}
 							});
 							passiveCapacity[i].add(availablePassives[j]);
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	//return the list from PopUp
 	public List<String> returnList(){
 		ArrayList<String> weapons = new ArrayList<String>();
 		for (int i = 0; i < passiveNames.length; i++){
 			weapons.add(passiveNames[i]);
 		}
 		return weapons;
 	}
 
 }
