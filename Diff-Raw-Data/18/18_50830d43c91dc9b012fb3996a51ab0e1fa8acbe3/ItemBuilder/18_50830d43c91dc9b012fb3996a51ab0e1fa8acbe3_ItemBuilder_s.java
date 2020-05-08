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
 
 import zuul.Item;
 import zuul.Room;
 
 
 public class ItemBuilder extends JPanel implements ActionListener{
 
   private static final long serialVersionUID = 1L;
   private JButton b0, b1, b2, b3, b4, b5, b6, b7, b8, b10, b11, b12, b13, b14, b15;
 	private JButton done;
 	private Room r0, r1, r2, r3, r4, r5, r6, r7, r8, r9, r10, r11, r12, r13, r14, r15;
 	private JPanel b9;
 	private boolean[] rooms;
 	
 	private Item bread, sword, apple, orange, pear, treasure;
 	
 	ArrayList<Item> iArray= new ArrayList<Item>();
 	boolean isDone = false;
 	
 	public ItemBuilder(boolean[] b){
 		this.setLayout(new GridLayout(4, 4));
 		initButtons();
 		rooms = new boolean[16];
 		initRooms(b);
 		initItems();
 	}
 	
 	public Room[] getRooms() {
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
 		JTextArea f = new JTextArea("Now editing: Items\nThis is the starting room.");
 		f.setEditable(false);
 		b9.add(f);
 		done = new JButton();
 		done.setText("Click here if done editing Items");
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
 
 	public void initItems(){
 		bread = new Item("Bread", 30, 0, false);
 		sword = new Item("Sword", 50, 0, true);
 		apple = new Item("Apple", 10, 0, false);
 		orange = new Item("Orange", 15, 0, false);
 		pear = new Item("Pear", 20, 0, false);
 		treasure = new Item("Treasure", 100, 0, true);
 		
 	    iArray.add(bread);
 	    iArray.add(sword);
 	    iArray.add(apple);
 	    iArray.add(orange);
 	    iArray.add(pear);
 	    iArray.add(treasure);
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
 		if(e.getSource() == b0){
 				 int popup = JOptionPane.showOptionDialog(this, "Select an Item to put in this room:",
 			              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 			              JOptionPane.INFORMATION_MESSAGE, null, iArray.toArray() , null);
 				 if(popup == 0 || popup ==1 || popup == 2 || popup == 3 || popup == 4 || popup == 5){
 					 if(iArray.get(popup).equals(bread)){
 						 r0.addItem(bread);
 					 } else if (iArray.get(popup).equals(sword)){
 						 r0.addItem(sword);
 					 } else if (iArray.get(popup).equals(apple)){
 						 r0.addItem(apple);
 					 } else if (iArray.get(popup).equals(orange)){
 						 r0.addItem(orange);
 					 } else if (iArray.get(popup).equals(pear)){
 						 r0.addItem(pear);
 					 } else if (iArray.get(popup).equals(treasure)){
 						 r0.addItem(treasure);
 						 iArray.remove(treasure);
 						 b0.setEnabled(false);
 					 }
 					 b0.setText("" + getRooms()[0].getItems());
 				 } 
 		} else if(e.getSource() == b1){
 			 int popup = JOptionPane.showOptionDialog(this, "Select an Item to put in this room:",
 		              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 		              JOptionPane.INFORMATION_MESSAGE, null, iArray.toArray() , null);
 			 if(popup == 0 || popup ==1 || popup == 2 || popup == 3 || popup == 4 || popup == 5){
 				 if(iArray.get(popup).equals(bread)){
 					 r1.addItem(bread);
 				 } else if (iArray.get(popup).equals(sword)){
 					 r1.addItem(sword);
 				 } else if (iArray.get(popup).equals(apple)){
 					 r1.addItem(apple);
 				 } else if (iArray.get(popup).equals(orange)){
 					 r1.addItem(orange);
 				 } else if (iArray.get(popup).equals(pear)){
 					 r1.addItem(pear);
 				 } else if (iArray.get(popup).equals(treasure)){
 					 r1.addItem(treasure);
 					 iArray.remove(treasure);
 					 b1.setEnabled(false);
 				 }
 				 b1.setText("" + getRooms()[1].getItems());
 			 } 
 		} else if(e.getSource() == b2){
 			 int popup = JOptionPane.showOptionDialog(this, "Select an Item to put in this room:",
 		              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 		              JOptionPane.INFORMATION_MESSAGE, null, iArray.toArray() , null);
 			 if(popup == 0 || popup ==1 || popup == 2 || popup == 3 || popup == 4 || popup == 5){
 				 if(iArray.get(popup).equals(bread)){
 					 r2.addItem(bread);
 				 } else if (iArray.get(popup).equals(sword)){
 					 r2.addItem(sword);
 				 } else if (iArray.get(popup).equals(apple)){
 					 r2.addItem(apple);
 				 } else if (iArray.get(popup).equals(orange)){
 					 r2.addItem(orange);
 				 } else if (iArray.get(popup).equals(pear)){
 					 r2.addItem(pear);
 				 } else if (iArray.get(popup).equals(treasure)){
 					 r2.addItem(treasure);
 					 iArray.remove(treasure);
 					 b2.setEnabled(false);
 				 }
 				 b2.setText("" + getRooms()[2].getItems());
 			 } 
 		} else if(e.getSource() == b3){
 			 int popup = JOptionPane.showOptionDialog(this, "Select an Item to put in this room:",
 		              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 		              JOptionPane.INFORMATION_MESSAGE, null, iArray.toArray() , null);
 			 if(popup == 0 || popup ==1 || popup == 2 || popup == 3 || popup == 4 || popup == 5){
 				 if(iArray.get(popup).equals(bread)){
 					 r3.addItem(bread);
 				 } else if (iArray.get(popup).equals(sword)){
 					 r3.addItem(sword);
 				 } else if (iArray.get(popup).equals(apple)){
 					 r3.addItem(apple);
 				 } else if (iArray.get(popup).equals(orange)){
 					 r3.addItem(orange);
 				 } else if (iArray.get(popup).equals(pear)){
 					 r3.addItem(pear);
 				 } else if (iArray.get(popup).equals(treasure)){
 					 r3.addItem(treasure);
 					 iArray.remove(treasure);
 					 b3.setEnabled(false);
 				 }
 				 b3.setText("" + getRooms()[3].getItems());
 			 } 
 		} else if(e.getSource() == b4){
 			 int popup = JOptionPane.showOptionDialog(this, "Select an Item to put in this room:",
 		              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 		              JOptionPane.INFORMATION_MESSAGE, null, iArray.toArray() , null);
 			 if(popup == 0 || popup ==1 || popup == 2 || popup == 3 || popup == 4 || popup == 5){
 				 if(iArray.get(popup).equals(bread)){
 					 r4.addItem(bread);
 				 } else if (iArray.get(popup).equals(sword)){
 					 r4.addItem(sword);
 				 } else if (iArray.get(popup).equals(apple)){
 					 r4.addItem(apple);
 				 } else if (iArray.get(popup).equals(orange)){
 					 r4.addItem(orange);
 				 } else if (iArray.get(popup).equals(pear)){
 					 r4.addItem(pear);
 				 } else if (iArray.get(popup).equals(treasure)){
 					 r4.addItem(treasure);
 					 iArray.remove(treasure);
 					 b4.setEnabled(false);
 				 }
 				 b4.setText("" + getRooms()[4].getItems());
 			 } 
 		} else if(e.getSource() == b5){
 			 int popup = JOptionPane.showOptionDialog(this, "Select an Item to put in this room:",
 		              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 		              JOptionPane.INFORMATION_MESSAGE, null, iArray.toArray() , null);
 			 if(popup == 0 || popup ==1 || popup == 2 || popup == 3 || popup == 4 || popup == 5){
 				 if(iArray.get(popup).equals(bread)){
 					 r5.addItem(bread);
 				 } else if (iArray.get(popup).equals(sword)){
 					 r5.addItem(sword);
 				 } else if (iArray.get(popup).equals(apple)){
 					 r5.addItem(apple);
 				 } else if (iArray.get(popup).equals(orange)){
 					 r5.addItem(orange);
 				 } else if (iArray.get(popup).equals(pear)){
 					 r5.addItem(pear);
 				 } else if (iArray.get(popup).equals(treasure)){
 					 r5.addItem(treasure);
 					 iArray.remove(treasure);
 					 b5.setEnabled(false);
 				 }
 				 b5.setText("" + getRooms()[5].getItems());
 			 } 
 		} else if(e.getSource() == b6){
 			 int popup = JOptionPane.showOptionDialog(this, "Select an Item to put in this room:",
 		              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 		              JOptionPane.INFORMATION_MESSAGE, null, iArray.toArray() , null);
 			 if(popup == 0 || popup ==1 || popup == 2 || popup == 3 || popup == 4 || popup == 5){
 				 if(iArray.get(popup).equals(bread)){
 					 r6.addItem(bread);
 				 } else if (iArray.get(popup).equals(sword)){
 					 r6.addItem(sword);
 				 } else if (iArray.get(popup).equals(apple)){
 					 r6.addItem(apple);
 				 } else if (iArray.get(popup).equals(orange)){
 					 r6.addItem(orange);
 				 } else if (iArray.get(popup).equals(pear)){
 					 r6.addItem(pear);
 				 } else if (iArray.get(popup).equals(treasure)){
 					 r6.addItem(treasure);
 					 iArray.remove(treasure);
 					 b6.setEnabled(false);
 				 }
 				 b6.setText("" + getRooms()[6].getItems());
 			 } 
 		} else if(e.getSource() == b7){
 			 int popup = JOptionPane.showOptionDialog(this, "Select an Item to put in this room:",
 		              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 		              JOptionPane.INFORMATION_MESSAGE, null, iArray.toArray() , null);
 			 if(popup == 0 || popup ==1 || popup == 2 || popup == 3 || popup == 4 || popup == 5){
 				 if(iArray.get(popup).equals(bread)){
 					 r7.addItem(bread);
 				 } else if (iArray.get(popup).equals(sword)){
 					 r7.addItem(sword);
 				 } else if (iArray.get(popup).equals(apple)){
 					 r7.addItem(apple);
 				 } else if (iArray.get(popup).equals(orange)){
 					 r7.addItem(orange);
 				 } else if (iArray.get(popup).equals(pear)){
 					 r7.addItem(pear);
 				 } else if (iArray.get(popup).equals(treasure)){
 					 r7.addItem(treasure);
 					 iArray.remove(treasure);
 					 b7.setEnabled(false);
 				 }
 				 b7.setText("" + getRooms()[7].getItems());
 			 } 
 		} else if(e.getSource() == b8){
 			 int popup = JOptionPane.showOptionDialog(this, "Select an Item to put in this room:",
 		              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 		              JOptionPane.INFORMATION_MESSAGE, null, iArray.toArray() , null);
 			 if(popup == 0 || popup ==1 || popup == 2 || popup == 3 || popup == 4 || popup == 5){
 				 if(iArray.get(popup).equals(bread)){
 					 r8.addItem(bread);
 				 } else if (iArray.get(popup).equals(sword)){
 					 r8.addItem(sword);
 				 } else if (iArray.get(popup).equals(apple)){
 					 r8.addItem(apple);
 				 } else if (iArray.get(popup).equals(orange)){
 					 r8.addItem(orange);
 				 } else if (iArray.get(popup).equals(pear)){
 					 r8.addItem(pear);
 				 } else if (iArray.get(popup).equals(treasure)){
 					 r8.addItem(treasure);
 					 iArray.remove(treasure);
 					 b8.setEnabled(false);
 				 }
 				 b8.setText("" + getRooms()[8].getItems());
 			 } 
 		} else if(e.getSource() == b10){
 			 int popup = JOptionPane.showOptionDialog(this, "Select an Item to put in this room:",
 		              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 		              JOptionPane.INFORMATION_MESSAGE, null, iArray.toArray() , null);
 			 if(popup == 0 || popup ==1 || popup == 2 || popup == 3 || popup == 4 || popup == 5){
 				 if(iArray.get(popup).equals(bread)){
 					 r10.addItem(bread);
 				 } else if (iArray.get(popup).equals(sword)){
 					 r10.addItem(sword);
 				 } else if (iArray.get(popup).equals(apple)){
 					 r10.addItem(apple);
 				 } else if (iArray.get(popup).equals(orange)){
 					 r10.addItem(orange);
 				 } else if (iArray.get(popup).equals(pear)){
 					 r10.addItem(pear);
 				 } else if (iArray.get(popup).equals(treasure)){
 					 r10.addItem(treasure);
 					 iArray.remove(treasure);
 					 b10.setEnabled(false);
 				 }
 				 b10.setText("" + getRooms()[10].getItems());
 			 } 
 		} else if(e.getSource() == b11){
 			 int popup = JOptionPane.showOptionDialog(this, "Select an Item to put in this room:",
 		              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 		              JOptionPane.INFORMATION_MESSAGE, null, iArray.toArray() , null);
 			 if(popup == 0 || popup ==1 || popup == 2 || popup == 3 || popup == 4 || popup == 5){
 				 if(iArray.get(popup).equals(bread)){
 					 r11.addItem(bread);
 				 } else if (iArray.get(popup).equals(sword)){
 					 r11.addItem(sword);
 				 } else if (iArray.get(popup).equals(apple)){
 					 r11.addItem(apple);
 				 } else if (iArray.get(popup).equals(orange)){
 					 r11.addItem(orange);
 				 } else if (iArray.get(popup).equals(pear)){
 					 r11.addItem(pear);
 				 } else if (iArray.get(popup).equals(treasure)){
 					 r11.addItem(treasure);
 					 iArray.remove(treasure);
 					 b11.setEnabled(false);
 				 }
 				 b11.setText("" + getRooms()[11].getItems());
 			 } 
 		} else if(e.getSource() == b12){
 			 int popup = JOptionPane.showOptionDialog(this, "Select an Item to put in this room:",
 		              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 		              JOptionPane.INFORMATION_MESSAGE, null, iArray.toArray() , null);
 			 if(popup == 0 || popup ==1 || popup == 2 || popup == 3 || popup == 4 || popup == 5){
 				 if(iArray.get(popup).equals(bread)){
 					 r12.addItem(bread);
 				 } else if (iArray.get(popup).equals(sword)){
 					 r12.addItem(sword);
 				 } else if (iArray.get(popup).equals(apple)){
 					 r12.addItem(apple);
 				 } else if (iArray.get(popup).equals(orange)){
 					 r12.addItem(orange);
 				 } else if (iArray.get(popup).equals(pear)){
 					 r12.addItem(pear);
 				 } else if (iArray.get(popup).equals(treasure)){
 					 r12.addItem(treasure);
 					 iArray.remove(treasure);
 					 b12.setEnabled(false);
 				 }
 				 b12.setText("" + getRooms()[12].getItems());
 			 } 
 		} else if(e.getSource() == b13){
 			 int popup = JOptionPane.showOptionDialog(this, "Select an Item to put in this room:",
 		              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 		              JOptionPane.INFORMATION_MESSAGE, null, iArray.toArray() , null);
 			 if(popup == 0 || popup ==1 || popup == 2 || popup == 3 || popup == 4 || popup == 5){
 				 if(iArray.get(popup).equals(bread)){
 					 r13.addItem(bread);
 				 } else if (iArray.get(popup).equals(sword)){
 					 r13.addItem(sword);
 				 } else if (iArray.get(popup).equals(apple)){
 					 r13.addItem(apple);
 				 } else if (iArray.get(popup).equals(orange)){
 					 r13.addItem(orange);
 				 } else if (iArray.get(popup).equals(pear)){
 					 r13.addItem(pear);
 				 } else if (iArray.get(popup).equals(treasure)){
 					 r13.addItem(treasure);
 					 iArray.remove(treasure);
 					 b13.setEnabled(false);
 				 }
 				 b13.setText("" + getRooms()[13].getItems());
 			 } 
 		} else if(e.getSource() == b14){
 			 int popup = JOptionPane.showOptionDialog(this, "Select an Item to put in this room:",
 		              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 		              JOptionPane.INFORMATION_MESSAGE, null, iArray.toArray() , null);
 			 if(popup == 0 || popup ==1 || popup == 2 || popup == 3 || popup == 4 || popup == 5){
 				 if(iArray.get(popup).equals(bread)){
 					 r14.addItem(bread);
 				 } else if (iArray.get(popup).equals(sword)){
 					 r14.addItem(sword);
 				 } else if (iArray.get(popup).equals(apple)){
 					 r14.addItem(apple);
 				 } else if (iArray.get(popup).equals(orange)){
 					 r14.addItem(orange);
 				 } else if (iArray.get(popup).equals(pear)){
 					 r14.addItem(pear);
 				 } else if (iArray.get(popup).equals(treasure)){
 					 r14.addItem(treasure);
 					 iArray.remove(treasure);
 					 b14.setEnabled(false);
 				 }
 				 b14.setText("" + getRooms()[14].getItems());
 			 } 
 		} else if(e.getSource() == b15){
 			 int popup = JOptionPane.showOptionDialog(this, "Select an Item to put in this room:",
 		              "Selected Room", JOptionPane.YES_NO_CANCEL_OPTION,
 		              JOptionPane.INFORMATION_MESSAGE, null, iArray.toArray() , null);
 			 if(popup == 0 || popup ==1 || popup == 2 || popup == 3 || popup == 4 || popup == 5){
 				 if(iArray.get(popup).equals(bread)){
 					 r15.addItem(bread);
 				 } else if (iArray.get(popup).equals(sword)){
 					 r15.addItem(sword);
 				 } else if (iArray.get(popup).equals(apple)){
 					 r15.addItem(apple);
 				 } else if (iArray.get(popup).equals(orange)){
 					 r15.addItem(orange);
 				 } else if (iArray.get(popup).equals(pear)){
 					 r15.addItem(pear);
 				 } else if (iArray.get(popup).equals(treasure)){
 					 r15.addItem(treasure);
 					 iArray.remove(treasure);
 					 b15.setEnabled(false);
 				 }
 				 b15.setText("" + getRooms()[15].getItems());
 			 } 
 		} else if (e.getSource() == done){
 			this.setVisible(false);
 			isDone = true;
 			System.out.println("Done with item building");
 		}
 	}
 	
 		
 	public boolean isDone(){
 		return isDone;
 	}
 }
