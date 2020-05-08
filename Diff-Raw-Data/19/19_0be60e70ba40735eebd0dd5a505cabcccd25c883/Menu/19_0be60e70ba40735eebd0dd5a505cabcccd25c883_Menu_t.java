 /**
  * 
  * @author Drew Ingram 16680076
  *
  */
 public class Menu {
 	MenuItem[] menu;
	MenuItem iName;
	Bill currentBill = new Bill();
 	public Menu(){
 		menu = new MenuItem[10];
 		menu[0] = new MenuItem("Chicken Fries", 10.21);
 		menu[1] = new MenuItem("Bull Testicle", 3.99);
 		menu[3] = new MenuItem("Stir Fry", 4.12);
 		menu[4] = new MenuItem("Stir Fry with Bull Tesitcle", 5.99);
 		menu[5] = new MenuItem("Sushi", 5.13);
 		menu[6] = new MenuItem("Mozzarella Sticks", 2.98);
 		menu[7] = new MenuItem("Dumplings", 6.38);
 		menu[8] = new MenuItem("Calamari", 12.99);
 		menu[9] = new MenuItem("Pork Chops", .12);
 	}
 	
	public MenuItem[] getMenu(){
		return menu;
	}
	
	public void chooseItem(MenuItem iName) {
		this.iName = iName;
		currentBill.addItem(iName);
	}
	
 }
