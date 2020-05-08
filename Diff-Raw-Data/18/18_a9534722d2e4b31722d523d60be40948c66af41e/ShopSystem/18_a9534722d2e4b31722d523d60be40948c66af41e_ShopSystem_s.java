 package dungeonCrawler;
 
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.HeadlessException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.util.Locale;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 
 import dungeonCrawler.GameElements.Inventar;
 import dungeonCrawler.GameElements.Player;
 import dungeonCrawler.GameObjects.HealthPotion;
 import dungeonCrawler.GameObjects.ManaPotion;
 
 
 /**
  * Shop of the dungeon crawler create the Layout and fill it with content.
  * 
  * @author Hucke
  *
  */
 public class ShopSystem {
 	private int gridy=0;
 	private int[] times = new int[100];
 	private int vermoegen;
 	private int[] price = new int[100];
 	private String[] names = new String[100];
 	private int number;
 	private JTextField[] text = new JTextField[100];
 	private JTextField actualMoney;
 	private JButton[] button = new JButton[100];
 	private JDialog dialog;
 	private Player player;
 	private Inventar inventar;
 	private int preBuyVermoegen;
 	
 
 	public ShopSystem(Player p) {
 		this.player = p;
 	}
 	
 	public void setPlayer(Player p){
 		this.player = p;
 	}
 	/**
 	 * Add components to the GridBagLayout. The method simplified adding components to the pane.
 	 *  	
 	 * @param cont The container to which the components are added 
 	 * @param gbl The definition of the GridBagLayout
 	 * @param c The adding component 
 	 * @param x The x coordinate of the grid
 	 * @param y The y coordinate of the grid
 	 * @param width The width of the component in the grid
 	 * @param height The height of the component in the grid
 	 * @param weightx The horizontal component weight 
 	 * @param weighty The vertical component weight
 	 */
 	static void addComponent( Container cont,GridBagLayout gbl,Component c,int x, int y,int width, int height,double weightx, double weighty ){
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.fill = GridBagConstraints.BOTH;
 		gbc.gridx = x; gbc.gridy = y;
 		gbc.gridwidth = width; gbc.gridheight = height;
 		gbc.weightx = weightx; gbc.weighty = weighty;
 		gbl.setConstraints( c, gbc );
 		cont.add( c );
 	}
 	
 		
 	public int getvermoegen(){
 		return this.vermoegen;
 	}
 	public void setvermoegen(int vermoegen){
 		this.vermoegen=vermoegen;
 	}
 	
 	public int getPrice(int number){
 		return this.price[number];
 	}
 	public void setPrice(int number, int price){
 		this.price[number]=price;
 	}
 	
 	public String getName(int number){
 		return this.names[number];
 	}
 	public void setName(int number, String name){
 		this.names[number]=name;
 	}
 	
 	
 	/**
 	 * Fill the shop with Items
 	 * 
 	 * Create a Dialog and fill the shop with a indefinite number of item define in theShopItem class
 	 * and draw them to the dialog.
 	 * 
 	 * @param money The money the Player have
 	 * @param item The items which are available in the shop
 	 */
 	public void fillShop(int money, ShopItem ...item){
 		dialog = new JDialog(dialog, "Shop");
 		dialog.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
 		dialog.setLocationRelativeTo(dialog);
 			
 
 		Container c = dialog.getContentPane();
 		setvermoegen(money);
 		
 		GridBagLayout gbl = new GridBagLayout();
 		c.setLayout( gbl );
 		actualMoney = new JTextField("Sie haben noch: " + getvermoegen()+" Geld");
 		addComponent(c, gbl, actualMoney, 0, 0, 1, 1, 0, 0); 
 		preBuyVermoegen = getvermoegen();
 
 		for(ShopItem x:item){
 			gridy=gridy+2;
 			number=gridy/2;
 			setPrice(number, x.getItemPrice());
 			setName(number, x.getItemName());
 			
 			//ItemName
 			
 			addComponent(c, gbl, new JTextField(x.getItemName()), 0, gridy, 2, 2, 0.0, 0.0);
 			
 			//ItemPrice
 			addComponent(c, gbl, new JTextField(getPrice(number) + " Geld"), 3, gridy, 1, 2, 0, 0);
 
 	
 			
 			/**
 			 * Buy Action
 			 */
 			button[number]=new JButton(x.getItemName() + " auswaehlen?");
 			addComponent(c, gbl, button[number], 20, gridy, 2, 2, 0, 0);
 			text[number]=new JTextField(times[number] + "x ausgewaehlt");
 			addComponent(c, gbl, text[number], 22, gridy, 10, 1, 2.0, 0);
 			//button.addActionListener(this);
 			button[number].addActionListener(new ActionListener() {
 		
 				public void actionPerformed(ActionEvent e){
 					for(int i=1;i<=number;i++){
 						
 						if(e.getSource()==button[i]){
 			
 							if ((getvermoegen()-getPrice(i))>=0){
 								times[i]++;				    
 								setvermoegen(getvermoegen()-getPrice(i));
 								actualMoney.setText("Geld: " + getvermoegen());
 								text[i].setText(times[i] + "x Gekauft");
 							}
 							else if ((getvermoegen()-getPrice(i))<0){
 								JOptionPane.showMessageDialog(null, "Du hast noch "+getvermoegen()+" Geld, das Item kostest aber "+getPrice(i)+" Geld" ,"Ohne Geld gibts keine Ware!" , 0);				
 							}
 						}
 							
 					}
 				}
 			});
 		}
 
 		JButton cancel = new JButton("Abbruch");
 		addComponent(c, gbl, cancel, 1, 100, 2, 1, 0, 0);
 		
 		JButton buy = new JButton("Auswahl einkaufen?");
 		addComponent(c, gbl, buy, 10, 100, 2, 1, 0, 0);
 		
 		cancel.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				setvermoegen(preBuyVermoegen);
 				dialog.dispose();
 				GameLogic.timer.start();
 				
 			}
 		});
 		
 		buy.addActionListener(new ActionListener() {
 			
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				inventar = new Inventar();
 				for(int n=0; n<number+1;n++){
 					inventar.inventar(getName(n), times[n]);
 					inventar.printInventar(getName(n), times[n]);
 					player.setMoney(getvermoegen());
 					for(int i=1;i<=times[n]; i++)
 						switch (getName(n)){
 							//case "Armor": player.addItem(new );System.out.println("Amor done"); break;
 							case "Mana"	: player.addItem(new ManaPotion(50));System.out.println("Mana done"); break;
 							case "Health": player.addItem(new HealthPotion(50));System.out.println("Health done"); break;
 						}
 				}
 				dialog.dispose();
 				GameLogic.timer.start();
 			}
 		});
 		dialog.pack();
 		dialog.setVisible(true);
 	}
 }
