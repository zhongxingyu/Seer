 package dungeonCrawler;
 
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 
 import dungeonCrawler.GameElements.Player;
 import dungeonCrawler.GameObjects.HealthPotion;
 import dungeonCrawler.GameObjects.ManaPotion;
 
 public class DirtyShopSystem{
 	private JDialog dialog;
 	private JButton button_hp;
 	private JButton button_mana;
	private JButton button_weapon;
 	private JButton btn_exit;
 	public JTextField number_hp;
 	public JTextField number_mana;
 	public JTextField number_weapon;
 	public int times_hp=0;
 	public int times_mana=0;
 	public int times_weapon=0;
 	private int vermoegen;
 	private int price;
 	public int item_number;
 	private Player player;
 
 	public DirtyShopSystem(Player p) {
 		this.player = p;
 	}
 
 	public int getvermoegen(){
 		return this.vermoegen;
 	}
 
 	public void setvermoegen(int vermoegen){
 		this.vermoegen=vermoegen;
 	}
 
 	public int gettimes_hp(){
 		return this.times_hp;
 	}
 
 	public void settimes_hp(int times){
 		this.times_hp=times;
 	}
 
 	public int gettimes_mana(){
 		return this.times_mana;
 	}
 
 	public void settimes_mana(int times){
 		this.times_mana=times;
 	}
 
 	public int gettimes_weapon(){
 		return this.times_weapon;
 	}
 
 	public void settimes_weapon(int times){
 		this.times_weapon=times;
 	}
 
 
 	public void gui(int vermoegen){
 		setvermoegen(vermoegen);
 
 		//Dialog init
 
 		dialog = new JDialog(dialog, "Shop");
 		//dialog.setUndecorated(true);
 		dialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);	
 
 
 		//Set layout to Grid
 		GridBagLayout layout = new GridBagLayout();
 
 		dialog.setLayout(layout);
 		GridBagConstraints gbc=new GridBagConstraints();
 
 
 		//HealthPack
 		int gridy=2;
 		String  item_name = "HealthPack";
 
 		//Item description
 		gbc.gridheight=1;
 		gbc.gridx = 1;  
 		gbc.gridy = gridy;
 		JTextPane itemName_hp = new JTextPane();
 		itemName_hp.setText(item_name);
 		itemName_hp.setBackground(dialog.getContentPane().getBackground());
 		itemName_hp.setEditable(false);
 		layout.setConstraints(itemName_hp, gbc);
 		dialog.add(itemName_hp);
 
 		//Price
 		gbc.gridheight=1;
 		gbc.gridwidth=2;
 		gbc.gridx = 2;
 		gbc.gridy = gridy;
 		JTextPane priceInGame_hp = new JTextPane();
 		priceInGame_hp.setText("2 Geld");
 		priceInGame_hp.setBackground(dialog.getContentPane().getBackground());
 		priceInGame_hp.setEditable(false);
 		layout.setConstraints(priceInGame_hp, gbc);
 		dialog.add(priceInGame_hp);
 
 
 		gbc.gridheight=1;
 		gbc.gridwidth=2;
 		gbc.gridx = 20;
 		gbc.gridy =gridy;
 
 		button_hp = new JButton("Kaufen?");
 		layout.setConstraints(button_hp, gbc);
 		//button.addActionListener(this);
 		button_hp.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e){
 
 				price=2;
 				if ((getvermoegen()-price)>=0){
 					settimes_hp(times_hp+1);
 					setvermoegen(getvermoegen()-price);
 					System.out.println("in " + getvermoegen() + " " + price + " " + gettimes_hp());
 					player.setMoney(getvermoegen());
 					player.addItem(new HealthPotion(200));
 
 				}
 				else if ((getvermoegen()-price)<0){
 					JOptionPane.showMessageDialog(null, "Komm doch mit Geld wieder..." ,"Ohne Geld gibts keine Ware!" , 0);				
 				}
 
 			}
 		});
 		dialog.add(button_hp);
 		//End HeathPack
 
 
 
 		//Manatrank
 		gridy=4;
 		item_name="Manatrank";
 
 
 		//Item description
 		gbc.gridheight=1;
 		gbc.gridx = 1;  
 		gbc.gridy = gridy;
 		JTextPane itemName_mana = new JTextPane();
 		itemName_mana.setText(item_name);
 		itemName_mana.setBackground(dialog.getContentPane().getBackground());
 		itemName_mana.setEditable(false);
 		layout.setConstraints(itemName_mana, gbc);
 		dialog.add(itemName_mana);
 
 		//Price
 		gbc.gridheight=1;
 		gbc.gridwidth=2;
 		gbc.gridx = 2;
 		gbc.gridy = gridy;
 		JTextPane priceInGame_mana = new JTextPane();
 		priceInGame_mana.setText("7 Geld");
 		priceInGame_mana.setBackground(dialog.getContentPane().getBackground());
 		priceInGame_mana.setEditable(false);
 		layout.setConstraints(priceInGame_mana, gbc);
 		dialog.add(priceInGame_mana);
 
 
 		gbc.gridheight=1;
 		gbc.gridwidth=2;
 		gbc.gridx = 20;
 		gbc.gridy =gridy;
 
 		button_mana = new JButton("Kaufen?");
 		layout.setConstraints(button_mana, gbc);
 		//button.addActionListener(this);
 		button_mana.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e){
 
 				price=7;
 				if ((getvermoegen()-price)>=0){
 					settimes_mana(times_mana+1);
 					setvermoegen(getvermoegen()-price);
 					System.out.println("in " +getvermoegen() + " " + price + " " + gettimes_mana());
 					player.setMoney(getvermoegen());
 					player.addItem(new ManaPotion(50));
 				}
 				else if ((getvermoegen()-price)<0){
 					JOptionPane.showMessageDialog(null, "Komm doch mit Geld wieder..." ,"Ohne Geld gibts keine Ware!" , 0);				
 				}
 
 			}
 		});
 		dialog.add(button_mana);
 		//ManaTrank End
 
 		//Weapon
 
 		gridy=6;
 		item_name = "Armor";
 
 		//Item description
 		gbc.gridheight=1;
 		gbc.gridx = 1;  
 		gbc.gridy = gridy;
 		JTextPane itemName_weapon = new JTextPane();
 		itemName_weapon.setText(item_name);
 		itemName_weapon.setBackground(dialog.getContentPane().getBackground());
 		itemName_weapon.setEditable(false);
 		layout.setConstraints(itemName_weapon, gbc);
 		dialog.add(itemName_weapon);
 
 		//Price
 		gbc.gridheight=1;
 		gbc.gridwidth=2;
 		gbc.gridx = 2;
 		gbc.gridy = gridy;
 		JTextPane priceInGame_weapon = new JTextPane();
 		priceInGame_weapon.setText("10 Geld");
 		priceInGame_weapon.setBackground(dialog.getContentPane().getBackground());
 		priceInGame_weapon.setEditable(false);
 		layout.setConstraints(priceInGame_weapon, gbc);
 		dialog.add(priceInGame_weapon);
 
 
 		gbc.gridheight=1;
 		gbc.gridwidth=2;
 		gbc.gridx = 20;
 		gbc.gridy =gridy;
 
		button_weapon = new JButton("Kaufen?");
		layout.setConstraints(button_weapon, gbc);
 		//button.addActionListener(this);
		button_weapon.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e){
 
 				if (player.getArmor() < player.maxArmor) {
 					price=10;
 					if ((getvermoegen()-price)>=0){
 						settimes_weapon(times_weapon+1);
 						setvermoegen(getvermoegen()-price);
 						System.out.println("in " + getvermoegen() + " " + price + " " + gettimes_weapon());
 						player.setMoney(getvermoegen());
 						player.increaseArmor(100);
 					}
 					else if ((getvermoegen()-price)<0){
 						JOptionPane.showMessageDialog(null, "Komm doch mit Geld wieder..." ,"Ohne Geld gibts keine Ware!" , 0);				
 					}
 				}
 				else System.out.println("Ruestung 100%");
 
 			}
 		});
 		//End Weapon
 		//Exit Button
 		gbc.gridx=7; 
 		gbc.gridy=20;
 		gbc.gridwidth=2;
 		gbc.weightx = 1.0;
 		btn_exit=new JButton("shop close");
 		layout.setConstraints(btn_exit, gbc);
 		dialog.add(btn_exit);
 		btn_exit.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent arg0) {
 				dialog.setVisible(false);
 				System.out.println("Shop Invisable");
 
 
 			}
 
 		});
 
 		System.out.println("ShopSystem Started");
		dialog.add(button_weapon);
 
 		dialog.pack();
 	}
 
 	public void startDirtyShop(){
 		dialog.setVisible(true);
 
 	}
 
 	public void stopDirtyShop(){
 		dialog.setVisible(false);
 	}
 
 
 }
