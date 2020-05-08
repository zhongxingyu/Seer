 package Builders;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 import zuul.Humanoid;
 import zuul.Item;
 import zuul.Monster;
 import zuul.Room;
 
 
 public class MonsterBuilder extends JPanel implements ActionListener{
 
   private static final long serialVersionUID = 1L;
   private JButton b0, b1, b2, b3, b4, b5, b6, b7, b8, b10, b11, b12, b13, b14, b15;
 	private JButton done;
 	private Room r0, r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15;
 	private JPanel b9;
 	private boolean[] rooms;
 	MapBuilder mb;
 	Monster alien;
 	Monster boss;
 	ArrayList<Monster> mArray= new ArrayList<Monster>();
 	boolean isDone = false;
 	
 	public MonsterBuilder(boolean[] b){
 		this.setLayout(new GridLayout(4, 4));
 		initButtons();
 		rooms = new boolean[16];
 		initRooms(b);
 		initMonsters();
 		mb = new MapBuilder(rooms, 9); //9 is the starting room
 	}
 	
 	public Room[] getRooms(){
 		Room[] returnedRooms = new Room[16];
 		returnedRooms[0] = r0;
 		returnedRooms[1] = r1;
 		returnedRooms[2] = r2;
 		returnedRooms[3] = r3;
 		returnedRooms[4] = r4;
 		returnedRooms[5] = r5;
 		returnedRooms[6] = r6;
 		returnedRooms[7] = r7;
 		returnedRooms[8] = r8;
 		returnedRooms[9] = r9;
 		returnedRooms[10] = r10;
 		returnedRooms[11] = r11;
 		returnedRooms[12] = r12;
 		returnedRooms[13] = r13;
 		returnedRooms[14] = r14;
 		returnedRooms[15] = r15;
 		return returnedRooms;
 	}
 	
 	public void initButtons(){
 
 		b0 = new JButton(); 
 		b1 = new JButton();  
 		b2 = new JButton(); 
 		b3 = new JButton(); 
 		b4 = new JButton(); 
 		b5 = new JButton(); 
 		b6 = new JButton(); 
 		b7 = new JButton(); 
 		b8 = new JButton(); 
 		
 		b9 = new JPanel(); 
 		
 		b10 = new JButton(); 
 		b11 = new JButton(); 
 		b12 = new JButton(); 
 		b13 = new JButton(); 
 		b14 = new JButton(); 
 		b15 = new JButton(); 
 		
 		b9.setLayout(new FlowLayout());
 		JTextArea f = new JTextArea("Now editing: Monsters\nThis is the starting room.");
 		f.setEditable(false);
 		b9.add(f);
 		done = new JButton();
 		done.setText("Click here if done editing Monsters");
 		b9.add(done);
 		
 		done.addActionListener(this);
 		
 		b0.addActionListener(this);
 		b1.addActionListener(this);
 		b2.addActionListener(this);
 		b3.addActionListener(this);
 		b4.addActionListener(this); 
 		b5.addActionListener(this);
 		b6.addActionListener(this);
 		b7.addActionListener(this); 
 		b8.addActionListener(this);
 		b10.addActionListener(this);
 		b11.addActionListener(this);
 		b12.addActionListener(this);
 		b13.addActionListener(this);
 		b14.addActionListener(this);
 		b15.addActionListener(this);
 		
 		this.add(b0);
 		this.add(b1);
 		this.add(b2);
 		this.add(b3);
 		this.add(b4);
 		this.add(b5);
 		this.add(b6);
 		this.add(b7);
 		this.add(b8);
 		this.add(b9);
 		this.add(b10);
 		this.add(b11);
 		this.add(b12);
 		this.add(b13);
 		this.add(b14);
 		this.add(b15);
 	}
 
 	public void initMonsters(){
 		Room temp = new Room("Temp room");
 		alien = new Monster(Humanoid.MAX_HEALTH, Monster.DEFAULT_LEVEL, "Alien", null);
 	    alien.addItem(new Item("Map", 0, 0, true));
 	    alien.addItem(new Item("Claws", 10, 0, true));
 	    alien.setRoom(temp);
 	    boss = new Monster(100, 2, "Boss", null);
 	    boss.addItem(new Item("Flamethrower", 30, 0, true));
 	    boss.addItem(new Item("Key", 0, 0, true));
 	    boss.setRoom(temp);
 	    mArray.add(alien);
 	    mArray.add(boss);
 	}
 	
 	public void initRooms(boolean[] b){
 		rooms[0] = b[0];
 		if(rooms[0] == false){
 			b0.setEnabled(false);
 		}
 		rooms[1] = b[1];
 		if(rooms[1] == false){
 			b1.setEnabled(false);
 		}
 		rooms[2] = b[2];
 		if(rooms[2] == false){
 			b2.setEnabled(false);
 		}
 		rooms[3] = b[3];
 		if(rooms[3] == false){
 			b3.setEnabled(false);
 		}
 		rooms[4] = b[4];
 		if(rooms[4] == false){
 			b4.setEnabled(false);
 		}
 		rooms[5] = b[5];
 		if(rooms[5] == false){
 			b5.setEnabled(false);
 		}
 		rooms[6] = b[6];
 		if(rooms[6] == false){
 			b6.setEnabled(false);
 		}
 		rooms[7] = b[7];
 		if(rooms[7] == false){
 			b7.setEnabled(false);
 		}
 		rooms[8] = b[8];
 		if(rooms[8] == false){
 			b8.setEnabled(false);
 		}
 		rooms[9] = true;	//always true, starting room
 		rooms[10] = b[10];
 		if(rooms[10] == false){
 			b10.setEnabled(false);
 		}
 		rooms[11] = b[11];
 		if(rooms[11] == false){
 			b11.setEnabled(false);
 		}
 		rooms[12] = b[12];
 		if(rooms[12] == false){
 			b12.setEnabled(false);
 		}
 		rooms[13] = b[13];
 		if(rooms[13] == false){
 			b13.setEnabled(false);
 		}
 		rooms[14] = b[14];
 		if(rooms[14] == false){
 			b14.setEnabled(false);
 		}
 		rooms[15] = b[15];
 		if(rooms[15] == false){
 			b15.setEnabled(false);
 		}
 		
 		r0 = new Room("0");
 		r1 = new Room("1");
 		r2 = new Room("2");
 		r3 = new Room("3");
 		r4 = new Room("4");
 		r5 = new Room("5");
 		r6 = new Room("6");
 		r7 = new Room("7");
 		r8 = new Room("8");
 		r9 = new Room("9");
 		r10 = new Room("10");
 		r11 = new Room("11");
 		r12 = new Room("12");
 		r13 = new Room("13");
 		r14 = new Room("14");
 		r15 = new Room("15");
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		//TODO different option dialog for no monsters
 		if(e.getSource() == b0){
 				 int popup = JOptionPane.showOptionDialog(this, "Select a Monster to put in this room:",
 			              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 			              JOptionPane.INFORMATION_MESSAGE, null, mArray.toArray() , null);
 				 if(popup == 0 || popup ==1){
 					 if(mArray.get(popup).equals(alien)){
 						 r0.addMonster(alien);
 						 mArray.remove(alien);
 					 } else {
 						 r0.addMonster(boss);
 						 mArray.remove(boss);
 					 }
 					 b0.setEnabled(false);
 					 b0.setText("" + getRooms()[0].getMonster().getName() + " Placed!");
 				 } 
 		} else if(e.getSource() == b1){
 				 int popup = JOptionPane.showOptionDialog(this, "Select a Monster to put in this room:",
 			              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 			              JOptionPane.INFORMATION_MESSAGE, null, mArray.toArray() , null);
 				 if(popup == 0 || popup ==1){
 					 if(mArray.get(popup).equals(alien)){
 						 r1.addMonster(alien);
 						 mArray.remove(alien);
 					 } else {
 						 r1.addMonster(boss);
 						 mArray.remove(boss);
 					 }
 					 b1.setEnabled(false);
 					 b1.setText("" + getRooms()[1].getMonster().getName() + " Placed!");
 				 } 
 		} else if(e.getSource() == b2){
 				 int popup = JOptionPane.showOptionDialog(this, "Select a Monster to put in this room:",
 			              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 			              JOptionPane.INFORMATION_MESSAGE, null, mArray.toArray() , null);
 				 if(popup == 0 || popup ==1){
 					 if(mArray.get(popup).equals(alien)){
 						 r2.addMonster(alien);
 						 mArray.remove(alien);
 					 } else {
 						 r2.addMonster(boss);
 						 mArray.remove(boss);
 					 }
 					 b2.setEnabled(false);
 					 b2.setText("" + getRooms()[2].getMonster().getName() + " Placed!");
 				 } 
 		} else if(e.getSource() == b3){
 				 int popup = JOptionPane.showOptionDialog(this, "Select a Monster to put in this room:",
 			              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 			              JOptionPane.INFORMATION_MESSAGE, null, mArray.toArray() , null);
 				 if(popup == 0 || popup ==1){
 					 if(mArray.get(popup).equals(alien)){
 						 r3.addMonster(alien);
 						 mArray.remove(alien);
 					 } else {
 						 r3.addMonster(boss);
 						 mArray.remove(boss);
 					 }
 					 b3.setEnabled(false);
 					 b3.setText("" + getRooms()[3].getMonster().getName() + " Placed!");
 				 } 
 		} else if(e.getSource() == b4){
 				 int popup = JOptionPane.showOptionDialog(this, "Select a Monster to put in this room:",
 			              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 			              JOptionPane.INFORMATION_MESSAGE, null, mArray.toArray() , null);
 				 if(popup == 0 || popup ==1){
 					 if(mArray.get(popup).equals(alien)){
 						 r4.addMonster(alien);
 						 mArray.remove(alien);
 					 } else {
 						 r4.addMonster(boss);
 						 mArray.remove(boss);
 					 }
 					 b4.setEnabled(false);
 					 b4.setText("" + getRooms()[4].getMonster().getName() + " Placed!");
 				 } 
 		} else if(e.getSource() == b5){
 				 int popup = JOptionPane.showOptionDialog(this, "Select a Monster to put in this room:",
 			              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 			              JOptionPane.INFORMATION_MESSAGE, null, mArray.toArray() , null);
 				 if(popup == 0 || popup ==1){
 					 if(mArray.get(popup).equals(alien)){
 						 r5.addMonster(alien);
 						 mArray.remove(alien);
 					 } else {
 						 r5.addMonster(boss);
 						 mArray.remove(boss);
 					 }
 					 b5.setEnabled(false);
 					 b5.setText("" + getRooms()[5].getMonster().getName() + " Placed!");
 				 } 
 		} else if(e.getSource() == b6){
 				 int popup = JOptionPane.showOptionDialog(this, "Select a Monster to put in this room:",
 			              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 			              JOptionPane.INFORMATION_MESSAGE, null, mArray.toArray() , null);
 				 if(popup == 0 || popup ==1){
 					 if(mArray.get(popup).equals(alien)){
 						 r6.addMonster(alien);
 						 mArray.remove(alien);
 					 } else {
 						 r6.addMonster(boss);
 						 mArray.remove(boss);
 					 }
 					 b6.setEnabled(false);
 					 b6.setText("" + getRooms()[6].getMonster().getName() + " Placed!");
 				 } 
 		} else if(e.getSource() == b7){
 				 int popup = JOptionPane.showOptionDialog(this, "Select a Monster to put in this room:",
 			              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 			              JOptionPane.INFORMATION_MESSAGE, null, mArray.toArray() , null);
 				 if(popup == 0 || popup ==1){
 					 if(mArray.get(popup).equals(alien)){
 						 r7.addMonster(alien);
 						 mArray.remove(alien);
 					 } else {
 						 r7.addMonster(boss);
 						 mArray.remove(boss);
 					 }
 					 b7.setEnabled(false);
 					 b7.setText("" + getRooms()[7].getMonster().getName() + " Placed!");
 				 } 
 		} else if(e.getSource() == b8){
 				 int popup = JOptionPane.showOptionDialog(this, "Select a Monster to put in this room:",
 			              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 			              JOptionPane.INFORMATION_MESSAGE, null, mArray.toArray() , null);
 				 if(popup == 0 || popup ==1){
 					 if(mArray.get(popup).equals(alien)){
 						 r8.addMonster(alien);
 						 mArray.remove(alien);
 					 } else {
 						 r8.addMonster(boss);
 						 mArray.remove(boss);
 					 }
 					 b8.setEnabled(false);
 					 b8.setText("" + getRooms()[8].getMonster().getName() + " Placed!");
 				 } 
 		} else if(e.getSource() == b10){
 				 int popup = JOptionPane.showOptionDialog(this, "Select a Monster to put in this room:",
 			              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 			              JOptionPane.INFORMATION_MESSAGE, null, mArray.toArray() , null);
 				 if(popup == 0 || popup ==1){
 					 if(mArray.get(popup).equals(alien)){
 						 r10.addMonster(alien);
 						 mArray.remove(alien);
 					 } else {
 						 r10.addMonster(boss);
 						 mArray.remove(boss);
 					 }
 					 b10.setEnabled(false);
 					 b10.setText("" + getRooms()[10].getMonster().getName() + " Placed!");
 				 } 
 		} else if(e.getSource() == b11){
 				 int popup = JOptionPane.showOptionDialog(this, "Select a Monster to put in this room:",
 			              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 			              JOptionPane.INFORMATION_MESSAGE, null, mArray.toArray() , null);
 				 if(popup == 0 || popup ==1){
 					 if(mArray.get(popup).equals(alien)){
 						 r11.addMonster(alien);
 						 mArray.remove(alien);
 					 } else {
 						 r11.addMonster(boss);
 						 mArray.remove(boss);
 					 }
 					 b11.setEnabled(false);
 					 b11.setText("" + getRooms()[11].getMonster().getName() + " Placed!");
 				 } 
 		} else if(e.getSource() == b12){
 				 int popup = JOptionPane.showOptionDialog(this, "Select a Monster to put in this room:",
 			              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 			              JOptionPane.INFORMATION_MESSAGE, null, mArray.toArray() , null);
 				 if(popup == 0 || popup ==1){
 					 if(mArray.get(popup).equals(alien)){
 						 r12.addMonster(alien);
 						 mArray.remove(alien);
 					 } else {
 						 r12.addMonster(boss);
 						 mArray.remove(boss);
 					 }
 					 b12.setEnabled(false);
 					 b12.setText("" + getRooms()[12].getMonster().getName() + " Placed!");
 				 } 
 		} else if(e.getSource() == b13){
 				 int popup = JOptionPane.showOptionDialog(this, "Select a Monster to put in this room:",
 			              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 			              JOptionPane.INFORMATION_MESSAGE, null, mArray.toArray() , null);
 				 if(popup == 0 || popup ==1){
 					 if(mArray.get(popup).equals(alien)){
 						 r13.addMonster(alien);
 						 mArray.remove(alien);
 					 } else {
 						 r13.addMonster(boss);
 						 mArray.remove(boss);
 					 }
 					 b13.setEnabled(false);
 					 b13.setText("" + getRooms()[13].getMonster().getName() + " Placed!");
 				 } 
 		} else if(e.getSource() == b14){
 				 int popup = JOptionPane.showOptionDialog(this, "Select a Monster to put in this room:",
 			              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 			              JOptionPane.INFORMATION_MESSAGE, null, mArray.toArray() , null);
 				 if(popup == 0 || popup ==1){
 					 if(mArray.get(popup).equals(alien)){
 						 r14.addMonster(alien);
 						 mArray.remove(alien);
 					 } else {
 						 r14.addMonster(boss);
 						 mArray.remove(boss);
 					 }
 					 b14.setEnabled(false);
 					 b14.setText("" + getRooms()[14].getMonster().getName() + " Placed!");
 				 } 
 		} else if(e.getSource() == b15){
 				 int popup = JOptionPane.showOptionDialog(this, "Select a Monster to put in this room:",
 			              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 			              JOptionPane.INFORMATION_MESSAGE, null, mArray.toArray() , null);
 				 if(popup == 0 || popup ==1){
 					 if(mArray.get(popup).equals(alien)){
 						 r15.addMonster(alien);
 						 mArray.remove(alien);
 					 } else {
 						 r15.addMonster(boss);
 						 mArray.remove(boss);
 					 }
 					 b15.setEnabled(false);
 					 b15.setText("" + getRooms()[15].getMonster().getName() + " Placed!");
 				 } 
 		} else if (e.getSource() == done){
 			this.setVisible(false);
 			isDone = true;
 			System.out.println("Done with monster building");
 		}
 		mb = new MapBuilder(rooms, 9); //9 is the starting room
 		
 	}
 	
 		
 	public boolean isDone(){
 		return isDone;
 	}
 }
