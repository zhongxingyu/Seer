 //Stephanie Reagle
 //CS 200
 package factory.swing;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 
 import factory.managers.KitManager;
 import factory.managers.LaneManager;
 
 public class KitManPanel extends JPanel implements ActionListener{
 	
 	private static final long serialVersionUID = -5760501420685052853L;
 	
 	String[] kits = { "Default Kit", "Mr. Potato Head", "Mrs. Potato Head"}; //NEED REAL
 	String[] items = { "None", "Head", "Hand", "Nose", "Eyes", "Feet", "Ears" }; //NEED REAL
 	
 	//for Create Kit Tab
 	JLabel cLabel1 = new JLabel ("Item 1");
 	JLabel cLabel2 = new JLabel ("Item 2");
 	JLabel cLabel3 = new JLabel ("Item 3");
 	JLabel cLabel4 = new JLabel ("Item 4");
 	JLabel cLabel5 = new JLabel ("Item 5");
 	JLabel cLabel6 = new JLabel ("Item 6");
 	JLabel cLabel7 = new JLabel ("Item 7");
 	JLabel cLabel8 = new JLabel ("Item 8");
 
 	JComboBox cItemComboBox1 = new JComboBox(items);
 	JComboBox cItemComboBox2 = new JComboBox(items);
 	JComboBox cItemComboBox3 = new JComboBox(items);
 	JComboBox cItemComboBox4 = new JComboBox(items);
 	JComboBox cItemComboBox5 = new JComboBox(items);
 	JComboBox cItemComboBox6 = new JComboBox(items);
 	JComboBox cItemComboBox7 = new JComboBox(items);
 	JComboBox cItemComboBox8 = new JComboBox(items);
 
 	JTextField cKitName = new JTextField( "Kit Name");
 	JLabel cMessages = new JLabel ("Messages:");
 	JButton cSave = new JButton("Save Kit Configuration");
 
 	//for Modify Kit Tab
 
 
 
 	JComboBox mKitComboBox = new JComboBox(kits);
 
 	JLabel mLabel1 = new JLabel ("Item 1");
 	JLabel mLabel2 = new JLabel ("Item 2");
 	JLabel mLabel3 = new JLabel ("Item 3");
 	JLabel mLabel4 = new JLabel ("Item 4");
 	JLabel mLabel5 = new JLabel ("Item 5");
 	JLabel mLabel6 = new JLabel ("Item 6");
 	JLabel mLabel7 = new JLabel ("Item 7");
 	JLabel mLabel8 = new JLabel ("Item 8");
 
 	JComboBox mItemComboBox1 = new JComboBox(items);
 	JComboBox mItemComboBox2 = new JComboBox(items);
 	JComboBox mItemComboBox3 = new JComboBox(items);
 	JComboBox mItemComboBox4 = new JComboBox(items);
 	JComboBox mItemComboBox5 = new JComboBox(items);
 	JComboBox mItemComboBox6 = new JComboBox(items);
 	JComboBox mItemComboBox7 = new JComboBox(items);
 	JComboBox mItemComboBox8 = new JComboBox(items);
 
 	JTextField mKitName = new JTextField( "Default Kit");
 	JLabel mMessages = new JLabel ("Messages:");
 	JButton mSave = new JButton("Save Kit Configuration");
 	JButton mRemove = new JButton("Remove Kit");
 	
 	KitManager kitManager;
 
 	public KitManPanel(KitManager k){
 		kitManager = k;
 		
 		//Create Panel
 		cItemComboBox1.addActionListener(this);
 		cItemComboBox2.addActionListener(this);
 		cItemComboBox3.addActionListener(this);
 		cItemComboBox4.addActionListener(this);
 		cItemComboBox5.addActionListener(this);
 		cItemComboBox6.addActionListener(this);
 		cItemComboBox7.addActionListener(this);
 		cItemComboBox8.addActionListener(this);
 		
 		JTabbedPane tabbedPane = new JTabbedPane();
 	    GridBagConstraints c = new GridBagConstraints();
 
 		JPanel createKit = new JPanel();
 		createKit.setLayout(new GridBagLayout());
 		c.fill = GridBagConstraints.VERTICAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		createKit.add(cLabel1, c);
 
 		c.gridy = 1;
 		createKit.add(cLabel2, c);
 		
 		c.gridy = 2;
 		createKit.add(cLabel3, c);
 
 		c.gridy = 3;
 		createKit.add(cLabel4, c);
 
 		c.gridy = 4;
 		createKit.add(cLabel5, c);
 
 		c.gridy = 5;
 		createKit.add(cLabel6, c);
 
 		c.gridy = 6;
 		createKit.add(cLabel7, c);
 
 		c.gridy = 7;
 		createKit.add(cLabel8, c);
 
 		c.gridy = 9;
 		createKit.add(cKitName, c);
 
 		c.gridy = 10;
 		createKit.add(cSave, c);
 
 		c.gridx = 1;
 		c.gridy = 0;
 		createKit.add(cItemComboBox1, c);
 
 		c.gridy = 1;
 		createKit.add(cItemComboBox2, c);
 
 		c.gridy = 2;
 		createKit.add(cItemComboBox3, c);
 
 		c.gridy = 3;
 		createKit.add(cItemComboBox4, c);
 
 		c.gridy = 4;
 		createKit.add(cItemComboBox5, c);
 
 		c.gridy = 5;
 		createKit.add(cItemComboBox6, c);
 
 		c.gridy = 6;
 		createKit.add(cItemComboBox7, c);
 
 		c.gridy = 7;
 		createKit.add(cItemComboBox8, c);
 		
 		c.gridy = 8;
 		createKit.add(cMessages, c);
 		
 		JPanel modifyKit = new JPanel();
 		modifyKit.setLayout(new GridBagLayout());
 
 		c.fill = GridBagConstraints.VERTICAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		modifyKit.add(mKitComboBox, c);
 
 		c.gridy = 1;
 		modifyKit.add(mLabel1, c);
 
 		c.gridy = 2;
 		modifyKit.add(mLabel2, c);
 
 		c.gridy = 3;
 		modifyKit.add(mLabel3, c);
 
 		c.gridy = 4;
 		modifyKit.add(mLabel4, c);
 
 		c.gridy = 5;
 		modifyKit.add(mLabel5, c);
 
 		c.gridy = 6;
 		modifyKit.add(mLabel6, c);
 
 		c.gridy = 7;
 		modifyKit.add(mLabel7, c);
 
 		c.gridy = 8;
 		modifyKit.add(mLabel8, c);
 
 		c.gridx = 1;
 		c.gridy = 1;
 		modifyKit.add(mItemComboBox1, c);
 
 		c.gridy = 2;
 		modifyKit.add(mItemComboBox2, c);
 
 		c.gridy = 3;
 		modifyKit.add(mItemComboBox3, c);
 
 		c.gridy = 4;
 		modifyKit.add(mItemComboBox4, c);
 
 		c.gridy = 5;
 		modifyKit.add(mItemComboBox5, c);
 
 		c.gridy = 6;
 		modifyKit.add(mItemComboBox6, c);
 
 		c.gridy = 7;
 		modifyKit.add(mItemComboBox7, c);
 
 		c.gridy = 8;
 		modifyKit.add(mItemComboBox8, c);
 
 		c.gridy = 9;
 		modifyKit.add(mKitName, c);
 
 		c.gridy = 10;
 		modifyKit.add(mSave, c);
 
 		c.gridx = 1;
 		c.gridy = 10;
 		modifyKit.add(mRemove, c);
 
 		tabbedPane.addTab("Create Kit", createKit);
 		tabbedPane.addTab("Modify Kit", modifyKit);
 		
 		add(tabbedPane);	
 	}
 
 	  public void actionPerformed(ActionEvent ae) {
 		  String set = new String();
 		if (ae.getSource() == cSave) {
 			for (int i = 0; i < kits.length; i++ ){
 				if (kits[i] == cKitName.getText()){
 					cMessages.setText("Kit Name already exists, try another name");
 				}
 			}
			else if (){
 				
 			}
 			else {
 				set = "km multi cmd addkitname " +
 					(String)cItemComboBox1.getSelectedItem() + " " + (String)cItemComboBox2.getSelectedItem() + " " + 
 					(String)cItemComboBox3.getSelectedItem() + " " + (String)cItemComboBox4.getSelectedItem() + " " + 
 					(String)cItemComboBox4.getSelectedItem() + " " + (String)cItemComboBox5.getSelectedItem() + " " + 
 					(String)cItemComboBox6.getSelectedItem() + " " + (String)cItemComboBox7.getSelectedItem() + " " + 
 					(String)cItemComboBox8.getSelectedItem(); //#kitname #partname1 #partname2 ... #partname8";
 				kitManager.sendCommand(set);
			}
 		}
 		else if (ae.getSource() == mSave) {
 		}
 		  
 		else if (ae.getSource() == mRemove){
 
 		}
 		
 	}
 		
 }
