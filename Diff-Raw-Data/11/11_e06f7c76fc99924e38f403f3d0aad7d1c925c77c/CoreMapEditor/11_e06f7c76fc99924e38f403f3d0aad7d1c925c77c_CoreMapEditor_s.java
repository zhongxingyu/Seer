 package hhu.propra_2013.gruppe_13;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 
 import javax.swing.ButtonGroup;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import javax.swing.JRadioButton;
 
 import java.io.*;
 
 public class CoreMapEditor {
	/**
	 * Bietet eine Grafische Oberfläche zum Designen einzelner Räume sowie die Möglichkeit diese zu speichern
	 * @author	gruppe13
	 */	
 	
 	//TODO Mehr Dokumentation
 	static char obj;
 	public char room[][] = new char[23][14];
 	private static JFrame game;
 	private static JPanel mapCreator;
 	private static MapPane mapPane;
 //	private static RadioPane radioPane;
 	
 	
 	/**
 	 * Grafische Oberfläche mit den Panels und Buttons zum setzen der Objekte
 	 * @param gameWindow Das Spielfenster
 	 */
 	
 	static void showMapCreator(JFrame gameWindow){
 		
 		
 		//System.out.println("reached the map creator");
 		
 		/*--------------------------------------------creating variables--------------------------------------------------------*/
 		game = gameWindow;
 		mapCreator = new JPanel();
 		
 		mapPane = new CoreMapEditor().new MapPane();
 		mapPane.setEnabled(true);
 		mapPane.setVisible(true);
 		mapPane.setPreferredSize(new Dimension(560, 350));
 		
 //		radioPane = new RadioPane();		
 		
 
 		
 		
 		/*--------------------------------------------create the needed Buttons-------------------------------------------------*/
 		
 		ButtonGroup brush = new ButtonGroup();
 		
 		JRadioButton wall = new JRadioButton("Wall");
 		JRadioButton enemy1 = new JRadioButton("Nahkampf-Gegner");
 		JRadioButton enemy2 = new JRadioButton("Feuer");
 		JRadioButton enemy3 = new JRadioButton("Boss");
 		JRadioButton item1 = new JRadioButton("Zufälliges Pickup");
 		JRadioButton item2 = new JRadioButton("Geld");
 		JRadioButton item3 = new JRadioButton("Wiederauferstehungs-Item");
 		JRadioButton delete = new JRadioButton("Del");
 		
 		brush.add(wall);
 		brush.add(enemy1);
 		brush.add(enemy2);
 		brush.add(enemy3);
 		brush.add(item1);
 		brush.add(item2);
 		brush.add(item3);
 		brush.add(delete);
 		
 		wall.setSelected(true);
 		
 		JButton backToMenu 	= new JButton("Menu");
 		JButton reset		= new JButton("Reset");
 		JButton test		= new JButton("Test");
 		JButton save		= new JButton("Save");
 		
 		wall.addActionListener(new ActionListener() {
 			
 			@Override	// terminate the program
 			public void actionPerformed(ActionEvent e) {
 				CoreMapEditor.setObj('W');
 			}
 		});
 		
 		enemy1.addActionListener(new ActionListener() {
 			
 			@Override	// terminate the program
 			public void actionPerformed(ActionEvent e) {
 				CoreMapEditor.setObj('E');
 			}
 		});
 		
 		enemy2.addActionListener(new ActionListener() {
 			
 			@Override	// terminate the program
 			public void actionPerformed(ActionEvent e) {
 				CoreMapEditor.setObj('F');
 			}
 		});
 		
 		enemy3.addActionListener(new ActionListener() {
 			
 			@Override	// terminate the program
 			public void actionPerformed(ActionEvent e) {
 				CoreMapEditor.setObj('B');
 			}
 		});
 		
 		item1.addActionListener(new ActionListener() {
 			
 			@Override	// terminate the program
 			public void actionPerformed(ActionEvent e) {
 				CoreMapEditor.setObj('I');
 			}
 		});
 		
 		item2.addActionListener(new ActionListener() {
 			
 			@Override	// terminate the program
 			public void actionPerformed(ActionEvent e) {
 				CoreMapEditor.setObj('G');
 			}
 		});
 		
 		item3.addActionListener(new ActionListener() {
 			
 			@Override	// terminate the program
 			public void actionPerformed(ActionEvent e) {
 				CoreMapEditor.setObj('R');
 			}
 		});
 		
 		delete.addActionListener(new ActionListener() {
 			
 			@Override	// terminate the program
 			public void actionPerformed(ActionEvent e) {
 				CoreMapEditor.setObj(' ');
 			}
 		});
 		
 		backToMenu.addActionListener(new ActionListener() {
 			
 			@Override	//go back to menu
 			//TODO:maybe ask if saved or warn at least
 			public void actionPerformed(ActionEvent e) {
 				ProPra.initMenu();
 			}
 		});
 		
 		test.addActionListener(new ActionListener() {
 			
 			@Override	// terminate the program
 			public void actionPerformed(ActionEvent e) {
 				CoreMapEditor.save();
 				CoreMapEditor.run();
 			}
 		});
 
 		save.addActionListener(new ActionListener() {
 	
 			@Override	// save array to file
 			public void actionPerformed(ActionEvent e) {
 				CoreMapEditor.save();
 			}
 		});
 
 		reset.addActionListener(new ActionListener() {
 	
 			@Override	// reset the whole array
 			public void actionPerformed(ActionEvent e) {
 				char room[][] = new char[23][14];
 			}
 		});
 		
 		/*-----------------------------------------build the buttons in the display--------------------------------------------*/
 		
 		mapCreator.setLayout(new GridBagLayout());
 		GridBagConstraints layout = new GridBagConstraints();
 		layout.weightx		= 1;
 		layout.weighty		= 1;
 		layout.gridheight 	= 9;
 		layout.gridwidth	= 3;
 		layout.gridx		= 0;
 		layout.gridy 		= 0;
 		mapCreator.add(mapPane, layout);
 		
 		layout.weightx		= 0.4;
 		layout.weighty		= 0.4;
 		layout.gridheight 	= 1;
 		layout.gridwidth	= 1;
 		layout.gridx		= 4;
 		layout.gridy 		= 0;
 		mapCreator.add(wall, layout);
 		
 //		layout.insets = new Insets(0,0,0,0);
 		layout.gridy		= 1;
 		mapCreator.add(item1, layout);
 		
 		layout.gridy		= 2;
 //		layout.insets = new Insets(0,0,0,0);
 		mapCreator.add(item1, layout);
 		
 		layout.gridy		= 3;
 //		layout.insets = new Insets(0,0,0,0);
 		mapCreator.add(item2, layout);
 		
 		layout.gridy		= 4;
 //		layout.insets = new Insets(0,0,0,0);
 		mapCreator.add(item3, layout);
 		
 		layout.gridy		= 5;
 //		layout.insets = new Insets(0,0,0,0);
 		mapCreator.add(enemy1, layout);
 		
 		layout.gridy		= 6;
 //		layout.insets = new Insets(0,0,0,0);
 		mapCreator.add(enemy2, layout);
 		
 		layout.gridy		= 7;
 //		layout.insets = new Insets(0,0,0,0);
 		mapCreator.add(enemy3, layout);
 		
 		layout.gridy		= 8;
 //		layout.insets = new Insets(0,0,0,0);
 		mapCreator.add(delete, layout);
 		
 		layout.gridx		= 0;
 		layout.gridy 		= 9;
 		mapCreator.add(reset, layout);
 		
 //		layout.insets = new Insets(0,0,0,0);
 		layout.gridx		= 1;
 		layout.gridy 		= 9;
 		mapCreator.add(save, layout);
 		
 //		layout.insets = new Insets(0,0,0,0);
 //		layout.gridx		= 2;
 //		layout.gridy 		= 9;
 //		mapCreator.add(test, layout);
 		
 //		layout.insets = new Insets(0,0,0,0);
 		layout.gridx		= 4;
 		layout.gridy 		= 9;
 		mapCreator.add(backToMenu, layout);
 		
 		
 
 //		/*---------------------------------------------make layout-------------------------------------------------------------*/
 		
 		
 		mapCreator.setSize(game.getContentPane().getSize());
 		game.setContentPane(mapCreator);
 	}
 	
 //	public void editArray(int x, int y){
 //		int i = game.getWidth()/x;
 //		int j = game.getHeight()/y;
 //		room[i][j] = getObj();
 //	}
 	
 	
 	private char getObj(){
 		return obj;
 	}
 	
 	private static void setObj(char inObj){
 		obj = inObj;
 	}
 	
 	/**
 	 * Methode zum Speichern des Raums in einer Textdatei
 	 * @see SaveClass
 	 */
 	
 	private static void save(){
 		
 		SaveClass saveIt;
 		saveIt = new CoreMapEditor().new SaveClass();
 		saveIt.countRaum();
 		saveIt.makeRoom();
 		
 	}
 	
 	private static void run(){
 		//TODO:call propra init game
 	}
 	
 	/**
 	 * Innere Klasse zur Darstellung des 'Platzierungsbereichs' sowie der Überführung desselben in ein char Array
 	 * zur einfacheren Abspeicherung in einer Textdatei
 	 * @author Gruppe13
 	 *
 	 */
 	
 	
 	//panel to show the map
 	private class MapPane extends JPanel{
 		
 		
 		private static final long serialVersionUID = -5409709899594619477L;
 		//blabla make array of elements of type mapelements
 		public MapElement mapElement[][] = new MapElement[24][14];
 		
 		public MapPane(){
 			
 			//System.out.println("reached the map pane");
 			
 			this.setPreferredSize(new Dimension(560, 350));
 			this.setEnabled(true);
 			this.setVisible(true);
 			this.setBackground(Color.darkGray);
 			
 			this.setLayout(new GridBagLayout());
 			GridBagConstraints layout = new GridBagConstraints();
 			
 			
 			for(int i = 0; i < 24 ; i++){
 				for(int j = 0; j < 14; j++){
 					mapElement[i][j] = new MapElement(i ,j);
 					layout.gridx = i;
 					layout.gridy = j;
 					this.add(mapElement[i][j], layout);
 					this.repaint();
 				}
 			}
 		}
 		
 		char getMapElement(int i, int j){
 			return mapElement[i][j].getState();
 		}
 		
 			//single map elements
 			private class MapElement extends JPanel{
 				
 				
 				private char actualState = ' ';
 				
 				
 				private static final long serialVersionUID = 1539840262942070244L;
 //				private int i, j;
 //				private char room[][] = new char[23][14];
 				
 				
 				public char getState(){
 					return actualState;
 				}
 				
 				/**
 				 * Methode um das setzen eines Objekts im 'Platzierungsbereich' in das char Array zu übersetzen
 				 * @param x X-Koordinate des angeklickten Map-Elements im Editor
 				 * @param y Y-Koordinate des angeklickten Map-Elements im Editor
 				 */
 				
 				
 				public void editArray(int x, int y){
 					
 					//Klicks am Rand werden ignoriert da sich dort in der fertigen Datei nur die Begrenzungs-X'e befinden werden
 					if (x != 0 && x != 23 && y != 0 && y != 13){
 						
 
 						room[x][y] = getObj();
 						char curr;
 						curr = getObj();
 						actualState = curr;
 						//System.out.println("just wrote "+ curr + " to" + x +" " + y);
 						//System.out.println("just read "+ room[x][y] + " from" + x +" " + y);
 						
 	//					for(int i = 0; i < 14 ; i++){
 	//						for(int j = 0; j < 24; j++){
 	//							System.out.println(room[i][j]+""+i+""+j);
 	//						}
 	//					}
 	
 						Color bg;
 						bg = Color.CYAN;
 
 						switch (curr) {
 						case 'R':
 							bg = Color.cyan;
 							break;
 						case 'G':
 							bg = Color.yellow;
 							break;
 						case 'W':
 							bg = Color.GREEN;
 							break;
 						case 'E':
 							bg = Color.RED;
 							break;
 							
 						case 'F':
 							bg = Color.LIGHT_GRAY;
 							break;
 							
 						case 'B':
 							bg = Color.magenta;
 							break;
 		
 						case 'I':
 							bg = Color.ORANGE;
 							break;
 							
 						default: //türen bei 7 (links rechts) und 11 (oben unten)
 							bg = Color.white;
 						}
 						this.setBackground(bg);
 						this.repaint();
 						
 					}//Ende if block
 				}//Ende edit Array
 				
 				/**
 				 * Konstruktor
 				 * Die Ränder werden als Begrenzung gekennzeichnet und können nicht verändert werden
 				 * Ebenso werden die Positionen der Türen in den Wänden angegeben
 				 * @param i X-Koordinate des Map-Elements im Platzierungsbereich
 				 * @param j Y_Koordinate des Map-Elements im Platzierungsbereich
 				 */
 				
 				public MapElement(int i, int j) {
 					
 					final int x = i;
 					final int y = j;
 					
 					//System.out.println("reached the map element");
 					
 					this.setPreferredSize(new Dimension(22, 22));
 					this.setEnabled(true);
 					this.setVisible(true);
 					if ( x == 0 && y != 7){
 						this.setBackground(Color.BLACK);
 					}
 					else if ( x == 0 && y == 7){
 						this.setBackground(Color.GRAY);
 					}
 					else if ( x == 23 && y != 7){
 						this.setBackground(Color.BLACK);
 					}
 					else if ( x == 23 && y == 7){
 						this.setBackground(Color.GRAY);
 					}
 					else if ( x != 11 && y == 0){
 						this.setBackground(Color.BLACK);
 					}
 					else if ( x == 11 && y == 0){
 						this.setBackground(Color.GRAY);
 					}
 					else if ( x != 11 && y == 13){
 						this.setBackground(Color.BLACK);
 					}
 					else if ( x == 11 && y == 13){
 						this.setBackground(Color.GRAY);
 					}
 					else if ( x != 0 && x != 23 && y != 0 && y != 13){
 						this.setBackground(Color.WHITE);
 					}
 					
 					
 					this.addMouseListener(new MouseListener(){
 					
 								@Override
 								public void mouseClicked(MouseEvent arg0) {
 									editArray( x , y );
 								}
 					
 								@Override
 								public void mouseEntered(MouseEvent arg0) {
 									// TODO Auto-generated method stub
 									
 								}
 					
 								@Override
 								public void mouseExited(MouseEvent arg0) {
 									// TODO Auto-generated method stub
 									
 								}
 					
 								@Override
 								public void mousePressed(MouseEvent arg0) {
 									
 									
 								}
 					
 								@Override
 								public void mouseReleased(MouseEvent arg0) {
 									// TODO Auto-generated method stub
 									
 								}
 								
 							});
 						}	
 					}
 			
 	}
 	
 	/**
 	 * Innere Klasse zum Abspeichern, da das Abspeichern in den anderen (statischen) Klassen nicht möglch war
 	 * Erst werden die vorhanden Dateien gezählt, dann ein Dateiname festgesetzt
 	 * und das Char Array per Outputstream in eine Datei geschrieben, wobei an den passenden Stellen die
 	 * Begrenzungswände und Türen automatisch gesetzt werden 
 	 * @author Gruppe13
 	 *
 	 */
 	
 	private class SaveClass {
 	
 		int anzahlRaum;
 		
 		//Konstruktor
 		SaveClass() {
 			
 			anzahlRaum = this.countRaum();
 		}
 		
 		//Methode zum speichern des erstellten Raums als Textdatei
 		private void makeRoom() {
 			
 			//PrintWriter roomWriter;
 			
 			char temp;
 			
 
 			
 			try {
 				PrintWriter roomWriter = new PrintWriter (new FileWriter("Level/Raum/Raum"+anzahlRaum+".txt"), true);//das true lässt ihn an eine Datei anhängen statt sie zu überschreiben, sonst gäbe es knapp 200 dateien die alle von ihrem nachfolger überschrieben werden bis nurnoch ein println bleibt
 				//durchgehen des Arrays
 				for (int i = 0; i < 14;i++){
 					for (int j = 0; j < 24;j++){
 						//setzen der statischen Elemente
 						if (j == 0) { 
 							if (i == 7) roomWriter.print('D');
 							else roomWriter.print('X');
 						}
 	
 						else if (j ==23){
 							if(i == 7) roomWriter.print('D');
 							else roomWriter.print('X');
 						}
 						
 						else if (i == 0 && j != 0 && j != 23){
 							if(j == 11) roomWriter.print('D');
 							else roomWriter.print('X');
 						}
 						
 						else if (i == 13 && j != 0 && j != 23){
 							if(j == 11) roomWriter.print('D');
 							else roomWriter.print('X');
 						} // Ab jetzt sind der Rand aus X'en und die Türen behandelt
 						
 						//Setzen der vom User festgelegten Raumelemente
 						else if (j != 0 && j != 23 && i != 0 && i != 13){
 							
 							temp = mapPane.getMapElement(j, i);
 							
 							roomWriter.print(temp);
 							
 							
 
 						}
 					}//Ende innere for-Schleife
 					
 					roomWriter.println("");//Zeilenumbruch
 					roomWriter.flush();
 				}
 				
 				roomWriter.close();
 				
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			
 		}
 
 
 		//Zähler der Raumdateien, um den Dateinamen festzulegen	
 		private int countRaum (){
 			File directory = new File ("Level/Raum"); //gehe zum Ordner der die zu zählenden Dateien enthält
 			int count = 0;
 			for (File counter : directory.listFiles()){ //gehe die Dateien im Ordner directory durch
 				if (counter.isFile()){ //für jede Datei den counter erhöhen, Ordner sollten ignoriert werden (es sollte dort ohnehin keine geben)
 					count++;
 				}		
 			}
 			return count;
 		}
 		
 	}
 	
 	
 	
 	
 //	private class RadioPane extends JPanel{
 //		
 //	}
 }
