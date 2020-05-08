 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.net.URL;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JMenuBar;
 
 class TutorialWindow extends JFrame implements ActionListener, MouseListener {
 
 	GameWindow gw = null;
 	GameWindow game;
 	JButton nextButton;
 	int step = 1;
 	public TutorialWindow(GameWindow gw) {
 		this.gw = gw;
 	}
 	public void display() {
 		//Make a new GameWindow
 		gw.setVisible(false);
 		game = new GameWindow();
 		game.mf.boardHeight = 9;
 		game.mf.boardWidth = 9;
 		game.mf.mineCount = 3;
 		int [][] tutorialField = {{0,0,9,0,0,0,0,0,0,0},
 		                    	  {0,0,0,0,0,0,0,0,0,0},
 		                    	  {0,0,0,0,9,0,0,0,0,0},
 		                    	  {0,0,0,0,0,0,0,0,0,0},
 		                    	  {0,0,0,0,0,0,0,0,0,9},
 		                    	  {0,0,0,0,0,0,0,0,0,0},
 		                    	  {0,0,0,0,0,0,0,0,0,0},
 		                    	  {0,0,0,0,0,0,0,0,0,0},
 		                    	  {0,0,0,0,0,0,0,0,0,0},
 		                    	  {0,0,0,0,0,0,0,0,0,0}};
 		//Set up the GameWindow with my mines
 		int [] [] tutorialMines = {{0,2},{2,4},{4,8}};
 		
 		
 		game.mf.mines = tutorialField;
 		game.mf.minePos = tutorialMines;
 		game.mf.generateNumbers();
 		game.setUpLayout();
 		game.remove(game.menuBar);
 		game.setJMenuBar(new JMenuBar());
 		game.lInfo.setText("This is the tutorial! Press the 'next' button to continue");
		
 		
 		//Have this window give hints and have a "next" button
 		nextButton = new JButton("Next");
 		nextButton.addActionListener(this);
 		game.pInfo.add(nextButton);
 		game.pack();
 		
 		
 		for(MineButton[] mbRow : game.mineButtons) {
 			for(MineButton mb : mbRow) {
 				mb.setEnabled(false);
 				mb.removeMouseListener(game);
 			}
 		}
 
 	}
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 			step++;
 			switch(step) {
 				case 2:
 					game.lInfo.setText("Welcome to YAJSM! This is the tutorial mode");
 					break;
 				case 3:
 					game.lInfo.setText("Above, you have the Minesweeper board");
 					break;
 				case 4:
 					game.lInfo.setText("Way up on top you have \"Time\" which refers to how many seconds have elapsed");
 					break;
 				case 5:
 					game.lInfo.setText("and \"Mines\" which refers to how many mines are left. As you can see, there are 3 left.");
 					break;
 				case 6:
 					game.lInfo.setText("The major interaction you will have with this game is by clicking on the field above");
 					break;
 				case 7:
 					game.mineButtons[5][3].setEnabled(true);
 					game.mineButtons[5][3].addMouseListener(game);
 					game.mineButtons[5][3].addActionListener(this);
 					game.lInfo.setText("Would you kindly click on the highlighted box above?");
 					nextButton.setEnabled(false);
 					nextButton.removeActionListener(this);
 					break;
 				case 8:
 					for(MineButton[] mbRow : game.mineButtons) {
 						for(MineButton mb : mbRow) {
 							
 							if(mb.getIcon().toString().contains("floor")) {
 								mb.setEnabled(true);
 								mb.addMouseListener(game);
 							}
 						}
 						invalidate();
 						validate();
 					}
 					game.lInfo.setText("See how a bunch of mines were cleared? That's because you clicked on a empty spot.");
 					nextButton.setEnabled(true);
 					nextButton.addActionListener(this);
 					break;
 				case 9:
 					game.lInfo.setText("Now take a look at the highlighted tile and the numbers surrounding it.");
 					game.mineButtons[4][8].setEnabled(true);
 					break;
 				case 10:
 					game.lInfo.setText("Notice all the numbers surrounding the tile are 1's?");
 					break;
 				case 11:
 					game.lInfo.setText("That means that all of those tiles are only touching 1 mine.");
 					break;
 				case 12:
 					game.lInfo.setText("I'm guessing that means that the tile the 1's are touching is the mine. Try right clicking it.");
 					game.mineButtons[4][8].addActionListener(this);
 					game.mineButtons[4][8].addMouseListener(this);
 					nextButton.setEnabled(false);
 					nextButton.removeActionListener(this);
 					break;
 				//case 13 is down below in mousePressed
 				case 13:
 					step = 12;
 					break;
 				case 14:
 					game.lInfo.setText("Flagging all the mines is one way to win. Another way to win is to uncover all non-mine tiles.");
 					break;
 				case 15:
 					game.lInfo.setText("You can also do a combination of both.");
 					break;
 				case 16:
 					game.lInfo.setText("Now take a look at the numbers surrounding the remaining three highlighted tiles.");
 					game.mineButtons[0][2].setEnabled(true);
 					game.mineButtons[1][3].setEnabled(true);
 					game.mineButtons[2][4].setEnabled(true);
 					break;
 				case 17:
 					game.lInfo.setText("Notice that the middle mine has several 1's around it.");
 					
 					break;
 				case 18:
 					game.lInfo.setText("Also carefully observe that the other two mines also have several 1's surrounding them.");
 					break;
 				case 19:
 					game.lInfo.setText("Which of these tiles is safe to click? Remember, the numbers indicate how many mines they are touching");
 					game.mineButtons[0][2].addMouseListener(this);
 					game.mineButtons[1][3].addMouseListener(this);
 					game.mineButtons[2][4].addMouseListener(this);
 					nextButton.setEnabled(false);
 					nextButton.removeActionListener(this);
 					break;
 				case 20:
 					step = 19;
 					break;
 				case 21:
 					game.lInfo.setText("Now that you've understood how to play, enjoy your victory!");
 					nextButton.setEnabled(true);
 					nextButton.addActionListener(this);
 					break;
 				case 22:
 					game.checkWin();
 					this.setVisible(false);
 					game.setVisible(false);
 					gw.time = 0;
 					gw.setVisible(true);
 					break;
 			}
 	}
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		
 	}
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		
 	}
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		
 	}
 	@Override
 	public void mousePressed(MouseEvent e) {
 		Object o = e.getSource();
 		MineButton mb = (MineButton) o;
 		if(step == 12){
 			if(e.getButton() == MouseEvent.BUTTON1) {
 				game.lInfo.setText("Oh you... come on, right click it!");
 				step = 12;
 			} else {
 				String mbType = mb.getIcon().toString();
 				if(mbType.contains("tile")) {
 					try{
 					    URL uFloor = null;
 					    ImageIcon icon = null;
 						if(mbType.contains("tile")) {
 							uFloor = getClass().getResource("res/images/" + game.currentTheme + "/flag.png");
 							icon = new ImageIcon(uFloor, "A flag");
 							game.flagCount++;
 						}
 						
 						if(uFloor != null) {
 							mb.setIcon(icon);
 						} else {
 							game.imagesMissing();
 						}
 						
 					} catch (NullPointerException npe) {
 						game.imagesMissing();
 					} 
 				} 
 				game.lInfo.setText("Whoo! That mine is now safely taken care of!");
 				game.mineButtons[4][8].removeMouseListener(this);
 				game.mineButtons[4][8].removeActionListener(this);
 				nextButton.setEnabled(true);
 				nextButton.addActionListener(this);
 				step++;
 			}
 		} else if (step == 19) {
 			if(mb == game.mineButtons[0][2]) {
 				game.lInfo.setText("Uh, not quite...");
 			} else if (mb == game.mineButtons[1][3]) {
 				game.revealButton(mb);
 				game.lInfo.setText("Huzzah! You're not as dumb as you look!");
 				nextButton.setEnabled(true);
 				nextButton.addActionListener(this);
 				step++;
 			} else if (mb == game.mineButtons[2][4]) {
 				game.lInfo.setText("Nope! You would've lost if you clicked that mine!");
 				
 			}
 		}
 	}
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		
 	}
 }
