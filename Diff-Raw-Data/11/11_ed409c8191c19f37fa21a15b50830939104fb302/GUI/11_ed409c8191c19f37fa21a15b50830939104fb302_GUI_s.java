 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.AbstractButton;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
@SuppressWarnings("serial")
public class GUI extends JFrame implements ActionListener{
 
 	public JFrame interfaceFrame = new JFrame("GUI");
 	
 	public GUI() {
 		Variable.GUIopen = true;
 		Variable.status = "Waiting for GUI settings";
 		initComponents();
 	}
 
	private void btnStartActionPerformed(ActionEvent e) {
 		String s = cmbFood.getSelectedItem().toString();
 
 		if (s.equals("Swordfish")) {
 			Variable.food = Variable.SWORDFISH;
 		}
 		if (s.equals("Lobster")) {
 			Variable.food = Variable.LOBSTER;
 		}
 		if (s.equals("Tuna")) {
 			Variable.food = Variable.TUNA;
 		}
 		if (s.equals("Salmon")) {
 			Variable.food = Variable.SALMON;
 		}
 		if (s.equals("Trout")) {
 			Variable.food = Variable.TROUT;
 		}
 
 		Variable.foodAmount = Integer.parseInt((String) cmbFoodAmount.getSelectedItem());
 		
 		switch(String.valueOf(cmbFightPlane.getSelectedItem())){
 		case "Floor 1":
 			Variable.currentArea = Variable.ROACHES1;
 			break;
 		case "Floor 2":
 			Variable.currentArea = Variable.ROACHES2;
 			break;
 		default:
 			Variable.status="Something went wrong in the setup..";
 			break;
 		}
 		
 		switch(String.valueOf(cmbCombatStyle.getSelectedItem())){
 		case "Meele":
 			Variable.meele = true;
 			break;
 		case "Ranged":
 			Variable.range = true;
 			break;
 		case "Mage":
 			Variable.mage = true;
 			break;
 		default:
 			Variable.status="Something went wrong in the setup..";
 			break;
 		}
 		
 		switch(String.valueOf(cmbSpellChoice.getSelectedItem())){
 		case "Air":
 			Variable.currentSpell = Variable.AIRSPELL;
 			break;
 		case "Water":
 			Variable.currentSpell = Variable.WATERSPELL;
 			break;
 		case "Earth":
 			Variable.currentSpell = Variable.EARTHSPELL;
 			break;
 		case "Fire":
 			Variable.currentSpell = Variable.FIRESPELL;
 			break;
 		case "None":
 			Variable.currentSpell = Variable.NULLSPELL;
 			break;
 		default:
 			Variable.status="Something went wrong in the setup..";
 			break;
 		}
 
 		if(chkLowTierLoot.isSelected()){
 			Variable.lootTier = Variable.LOOTTIERLOW;
 		}else{
 			Variable.lootTier = Variable.LOOTTIERHIGH;
 		}
 		
 		Variable.GUIopen = false;
 		Variable.status = "Starting script..";
 		interfaceFrame.dispose();
 	}
 	
 	private void initComponents() {
 
 		btnStart = new JButton();
 		lblFood = new JLabel();
 		cmbFood = new JComboBox<>();
 		lblFoodAmount = new JLabel();
 		cmbFoodAmount = new JComboBox<>();
 		lblFightPlane = new JLabel();
 		cmbFightPlane = new JComboBox<>();
 		lblLowTierLoot = new JLabel();
 		chkLowTierLoot = new JCheckBox();
 		lblCombatStyle = new JLabel();
 		cmbCombatStyle = new JComboBox<>();
 		lblSpellChoice = new JLabel();
 		cmbSpellChoice = new JComboBox<>();
 
 		interfaceFrame.setVisible(true);
 		interfaceFrame.setTitle("RoachKiller F2P");
 		interfaceFrame.setSize(294, 157);
 		interfaceFrame.setLayout(new java.awt.FlowLayout());
 		interfaceFrame.setLocationRelativeTo(null);
 		
 		lblFood.setText("Food: ");
 		interfaceFrame.add(lblFood);
 		
 		cmbFood.setModel(new DefaultComboBoxModel<>(new String[] { "Swordfish",
 				"Lobster", "Tuna", "Salmon", "Trout" }));
 		interfaceFrame.add(cmbFood);
 		
 		lblFoodAmount.setText("Food Amount: ");
 		interfaceFrame.add(lblFoodAmount);
 		
 		cmbFoodAmount.setModel(new DefaultComboBoxModel<>(new String[] { "28",
 				"27", "26", "25", "24", "23", "22", "21", "20", "19", "18",
 				"17", "16", "15", "14", "13", "12", "11", "10", "9", "8", "7",
 				"6", "5" }));
 		interfaceFrame.add(cmbFoodAmount);
 		
 		lblFightPlane.setText("Location: ");
 		interfaceFrame.add(lblFightPlane);
 		
 		cmbFightPlane.setModel(new DefaultComboBoxModel<>(new String[] { "Floor 1", "Floor 2" }));
 		interfaceFrame.add(cmbFightPlane);
 		
 		lblLowTierLoot.setText("     Low Tier Loot: ");
 		lblLowTierLoot.setToolTipText("Pick up low grade items (mith plate, uncut saphires, etc");
 		interfaceFrame.add(lblLowTierLoot);
 		
 		chkLowTierLoot.setToolTipText("Pick up low grade items (mith plate, uncut saphires, etc");
 		interfaceFrame.add(chkLowTierLoot);
 		
 		lblCombatStyle.setText("Combat Style: ");
 		interfaceFrame.add(lblCombatStyle);
 		
 		cmbCombatStyle.setModel(new DefaultComboBoxModel<>(new String[] { "Meele", "Ranged", "Mage" }));
 		interfaceFrame.add(cmbCombatStyle);
 		
 		lblSpellChoice.setText("Spell Choice: ");
 		interfaceFrame.add(lblSpellChoice);
 		
 		cmbSpellChoice.setModel(new DefaultComboBoxModel<>(new String[] { "None", "Air", "Water", "Earth", "Fire" }));
 		interfaceFrame.add(cmbSpellChoice);
 		
 		btnStart.setText("Start");
 		btnStart.setPreferredSize(new Dimension(80,32));
 		btnStart.setVerticalTextPosition(AbstractButton.CENTER);
 		btnStart.setHorizontalTextPosition(AbstractButton.CENTER);
 		btnStart.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				btnStartActionPerformed(e);
 			}
 		});
 		interfaceFrame.add(btnStart);
 	}
 
 	private JButton btnStart;
 	private JLabel lblFood;
 	private JComboBox<String> cmbFood;	
 	private JLabel lblFoodAmount;
 	private JComboBox<String> cmbFoodAmount;
 	private JLabel lblFightPlane;
 	private JComboBox<String> cmbFightPlane;
 	private JLabel lblCombatStyle;
 	private JComboBox<String> cmbCombatStyle;
 	private JLabel lblLowTierLoot;
 	private JCheckBox chkLowTierLoot;
 	private JLabel lblSpellChoice;
 	private JComboBox<String> cmbSpellChoice;
	
	@Override
	public void actionPerformed(ActionEvent e) {
	}
 }
