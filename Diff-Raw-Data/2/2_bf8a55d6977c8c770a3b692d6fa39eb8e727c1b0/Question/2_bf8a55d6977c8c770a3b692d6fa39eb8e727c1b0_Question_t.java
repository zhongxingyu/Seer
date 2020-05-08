 package levelEditor;
 
 import java.awt.Point;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.util.ArrayList;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import weapons.DamagingProjectile;
 import weapons.ScatterPattern;
 import weapons.SidePattern;
 import weapons.SinglePattern;
 import weapons.UnlimitedGun;
 import weapons.Weapon;
 
 import maps.Map;
 
 import com.golden.gamedev.Game;
 
 public class Question {
 	public static int enemies = 1;
 	private String movement=null;
 	public static ArrayList<String>fileData;
 	public static ArrayList<UnlimitedGun> weapons;
 	public static ArrayList<String> playerMove;
 	int count =1;
 	int count1 =1;
 	int count2 =1;
 	public boolean done;
 	public String enemyMovement(){
 		done =false;
 		if(count==1) fileData = new ArrayList<String>();
 		count++;
 		Object[] options = {"Back and Forth","Targeted", "Path", "Diamond"};
 
 		String input = (String) JOptionPane.showInputDialog(new JFrame(),
 				"Pick Your Enemy Movement:", "Top Down Demo'",
 				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
 		if (input.equals("Back and Forth")) movement ="BF,100,200,.2 ";
 		if (input.equals("Targeted")) movement = "T ";
 		if (input.equals("Diamond")) {
 			String[] temp = getCoordinate();
 			double x = Double.parseDouble(temp[0]);
 			double y = Double.parseDouble(temp[1]);
 			movement = "D,"+ x +","+ y+" "; 
 		}
 		if (input.equals("Path")){
 
 			movement = "Path";
 			count=1;
 			JOptionPane
 			.showMessageDialog(new JFrame(),
 					"Click on the coordinates for your Enemy's Path Movement. Press 'D' when done.");
 			return movement;
 		}
 		String a = addState(movement);
 		fileData.add(a);
 		System.out.println(a);
 		int ans =addAnotherMovement();
 		if (ans ==1) 
 			count=1;
 		
 		return movement;
 	}
 
 
 	private String[] getCoordinate() {
 		String coordinate = JOptionPane.showInputDialog(null, "Enter X and Y coordinate to move around: (ex. 100,2000) Screen Dimensions: 400 x 3000 ", 
 				"Enemy Movement", 1);
 		String[] temp =coordinate.split(",");
 		return temp;
 	}
 
 	public int addAnotherMovement() {
 		String[] yon = { "yes", "no" };
 		int ans = JOptionPane.showOptionDialog(new JFrame(),
 				"Do you want to add another Movement?","Level Editor", JOptionPane.YES_NO_CANCEL_OPTION,
 				JOptionPane.QUESTION_MESSAGE, null, yon, yon[0]);
 		if (ans==0) enemyMovement();
 		return ans;
 	}
 
 	public String addState(String move) {
 		String state ="";
 		Object[] options1 = {"Full Health", "Low Health", "Proximity"};
 		String input1 = (String) JOptionPane.showInputDialog(new JFrame(),
 				"Pick State", "Enemy Movement",
 				JOptionPane.PLAIN_MESSAGE, null, options1, options1[0]);
 				if (input1.equals("Full Health")) state="FH,";
 				if (input1.equals("Low Health")) state="LH,";
 				if (input1.equals("Proximity")) state="PR,0,100,";
 		String prior = JOptionPane.showInputDialog(null, "Enter Priority Level number: (the lower the number, the higher the priority)", 
 						"Enemy Movement", 1);
 		state=state+Integer.parseInt(prior) + " ";
 		
 		return move +state;
 	}
 	
 //	public void enemyWeapon(){
 //		if (count1==1) weapons = new ArrayList<UnlimitedGun>();
 //		count1++;
 //		DamagingProjectile proj = new DamagingProjectile("resources/fire.png", null, 3);
 //		Object[] options = {"Straight", "Side", "Scatter"};
 //		String input1 = (String) JOptionPane.showInputDialog(new JFrame(),
 //				"Pick Weapon Pattern:", "Enemy Weapon",
 //				JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
 //		if (input1.equals("Straight")) {
 //			SinglePattern single = new SinglePattern(-1);
 //			UnlimitedGun ug = new UnlimitedGun(1, proj, single);
 //			weapons.add(ug);
 //		}
 //		if (input1.equals("Side")) {
 //			SidePattern side = new SidePattern(1,1);
 //			UnlimitedGun ug = new UnlimitedGun(1, proj, side);
 //			weapons.add(ug);
 //		}
 //		if (input1.equals("Scatter")) {
 //			ScatterPattern scatter = new ScatterPattern(1,1,100);
 //			UnlimitedGun ug = new UnlimitedGun(1, proj, scatter);
 //			weapons.add(ug);
 //		}
 //		
 //		
 //	}
 	
 	public void writeEnemy(ArrayList<String> data){
 		
 		try{
 			  // Create file 
 			 FileWriter fstream = new FileWriter("StateInfo" + enemies +".txt");
 			  BufferedWriter out = new BufferedWriter(fstream);	  
 			  for (String str:data){
 			  out.write(str);
 			  out.write("\n");
 			  }
 			  //Close the output stream
 			  out.close();
 			  }catch (Exception e){//Catch exception if any
 			  System.err.println("Error: " + e.getMessage());
 			  }
 			  enemies++;
 		}
 	
 	public ArrayList<String> playerMovement(){
 		if (count2==1) playerMove = new ArrayList<String>();
 		count2++;
 		playerMove.add(playerDecor());
 		addAnotherPlayerMovement();
 		return playerMove;
 		
 		
 	}
 	public int addAnotherPlayerMovement() {
 		String[] yon = { "yes", "no" };
 		int ans = JOptionPane.showOptionDialog(new JFrame(),
 				"Do you want to add another Movement?","Level Editor", JOptionPane.YES_NO_CANCEL_OPTION,
 				JOptionPane.QUESTION_MESSAGE, null, yon, yon[1]);
 		if (ans==0) playerMovement();
 		return ans;
 	}
 	public String playerDecor(){
 		 Object[] options1 = {"Vertical", "Horizontal"};
 			String input1 = (String) JOptionPane.showInputDialog(new JFrame(),
 					"Pick Decorator:", "Player Movement",
					JOptionPane.PLAIN_MESSAGE, null, options1, options1[1]);
 		 if (input1.equals("Vertical")) 
 			 return "VerticalDecorator";
 		 else
 			 return "HorizontalDecorator";
 			 
 	}
 	
 	public ArrayList<String> getFileData() {
 		return fileData;
 	}
 	
 	public ArrayList<UnlimitedGun> getWeapons() {
 		return weapons;
 	}
 	// asks user if he is happy with his location
 	public int yesOrNo(String type) {
 		String[] options = { "yes", "no" };
 		int option = JOptionPane.showOptionDialog(new JFrame(),
 				"Would you like to place the " + type + " here?",
 				"Level Editor", JOptionPane.YES_NO_CANCEL_OPTION,
 				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
 
 		return option;
 	}
 	
 	
 }
 
 	
 	
 	
 
