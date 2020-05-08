 package uk.ac.aber.dcs.cs12420.aberpizza.pipeline;
 
 import java.io.*;
 import java.math.BigDecimal;
 import java.util.LinkedList;
 
 import uk.ac.aber.dcs.cs12420.aberpizza.data.*;
 
 public class InventoryEditor {
 
 	private enum State { EXIT, START, NEW, LOAD, SAVE, EDITOR, QUIT,
 		HELP, LIST, EXAMINE, ADD, REMOVE,
 		ADD_PIZZA, ADD_DRINK, ADD_SIDE} ;
 	private static State state = State.START;
 	private static Inventory inventory = null;
 	private static BufferedReader input = null;
 	private static Item item = null;
 	
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException {
 		char c;
 		String s;
 		int i;
 		input = new BufferedReader(new InputStreamReader(System.in));
 		
 		while (true) {
 			switch (state) {
 			case START:
 				System.out.println("Inventory editor, generates an inventory.xml file.");
 				System.out.println("(1) New");
 				System.out.println("(2) Load");
 				c = input.readLine().charAt(0);
 				if (c == '1') {
 					state = State.NEW;
 				}
 				if (c == '2') {
 					state = State.LOAD;
 				}
 				continue;
 			case NEW:
 				inventory = new Inventory();
 				state = State.EDITOR;
 				continue;
 			case LOAD:
 				inventory = Inventory.load(null);
 				state = State.EDITOR;
 				continue;
 			case SAVE:
 				inventory.save(null);
 				System.out.println("Saved.");
 				state = State.EDITOR;
 				continue;
 			case EDITOR:
 				System.out.println("? to print commands.");
 				c = input.readLine().charAt(0);
 				switch (c) {
 				case '?':
 					state = State.HELP;
 					continue;
 				case 'l':
 				case 'L':
 					state = State.LIST;
 					continue;
 				case 'a':
 				case 'A':
 					state = State.ADD;
 					continue;
 				case 'x':
 				case 'X':
 				case 'e':
 				case 'E':
 					state = State.EXAMINE;
 					continue;
 				case 'r':
 				case 'R':
 					state = State.REMOVE;
 					continue;
 				case 's':
 				case 'S':
 					state = State.SAVE;
 					continue;
 				case 'q':
 				case 'Q':
 					state = State.QUIT;
 					continue;
 				}
 			case HELP:
 				System.out.println("? help");
 				System.out.println("List");
 				System.out.println("Add");
 				System.out.println("EXamine");
 				System.out.println("Remove");
 				System.out.println("Save");
 				System.out.println("Quit");
 				state = State.EDITOR;
 				continue;
 			case LIST:
 				for (i = 0; i < inventory.getItems().size(); i++) {
 					System.out.println("("+i+") "+inventory.getItems().get(i).toString() );
 				}
 				state = State.EDITOR;
 				continue;
 			case ADD:
 				System.out.println("(1) Add Pizza");
 				System.out.println("(2) Add Drink");
 				System.out.println("(3) Add Side");
 				System.out.println("(D) Cancel");
 				c = input.readLine().charAt(0);
 				if (c == '1') { state = State.ADD_PIZZA; }
 				if (c == '2') { state = State.ADD_DRINK; }
 				if (c == '3') { state = State.ADD_SIDE; }
 				if (c == 'D' || c == 'd') { state = State.EDITOR; } 
 				continue;
 			case EXAMINE:
 				System.out.println("Enter item number or D to cancel.");
 				s = input.readLine();
 				if (s.charAt(0) == 'D' || s.charAt(0) == 'd') { 
 					state = State.EDITOR; 
 					continue; 
 				}
 				i = Integer.parseInt(s);
 				System.out.println(inventory.getItems().get(i).toString() );
 				state = State.EDITOR;
 				continue;
 			case REMOVE:
 				System.out.println("Enter item number ot D to cancel.");
 				s = input.readLine();
 				if (s.charAt(0) == 'D' || s.charAt(0) == 'd') {
 					state = State.EDITOR;
 					continue;
 				}
 				i = Integer.parseInt(s);
 				inventory.getItems().remove(i);
 				state = State.EDITOR;
 				continue;
 			case ADD_PIZZA:
 			case ADD_DRINK:
 			case ADD_SIDE:
 				
 				if (state == State.ADD_PIZZA) {
 					item = new PizzaItem();
 				}
 				if (state == State.ADD_DRINK) {
 					item = new DrinkItem();
 				}
 				if (state == State.ADD_SIDE) {
 					item = new SideItem();
 				}
 				
 				System.out.println("Enter description or 'D' to cancel.");
 				s = input.readLine();
 				if (s.compareTo("D") == 0) {
 					state = State.EDITOR;
 					continue;
 				}
 				item.setDescription(s);
 				
 				if (state == State.ADD_PIZZA) {
					System.out.println("Enter size, in inches of radius");
 				}
 				if (state == State.ADD_DRINK) {
					System.out.println("Enter size, in real liters");
 				}
 				if (state == State.ADD_SIDE) {
 					System.out.println("Enter size, in number of pieces");
 				}
 				s = input.readLine();
 				if (s.compareTo("D") == 0) {
 					state = State.EDITOR;
 					continue;
 				}
 				((AbstractItem) item).setSize(Double.parseDouble(s));
 				
 				if (state == State.ADD_PIZZA) {
 					((PizzaItem) item).setToppings(toppings() );
 				}
 				
 				System.out.println("Enter price, as a decimal number of pounds.");
 				s = input.readLine();
 				if (s.compareTo("D") == 0) {
 					state = State.EDITOR;
 					continue;
 				}
 				item.setPrice(new BigDecimal(s) );
 				
 				inventory.getItems().add(item);
 				System.out.println("Item added.");
 				state = State.EDITOR;
 				continue;
 				
 				
 			default:
 				return;
 			
 			}
 		}
 
 	}
 	
 	public static LinkedList<String> toppings() throws IOException {
 		LinkedList<String> list = new LinkedList<String>();
 		while (true) {
 			System.out.println("Enter topping as string or @ to terminate.");
 			String s = input.readLine();
 			if (s.compareTo("@" ) ==0) {
 				return list;
 			}
 			list.add(s);
 		}
 		
 		
 	}
 
 }
