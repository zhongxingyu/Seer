 package simplegui.objectron;
 
 import effect.LightGrenadeEffect;
 import effect.PowerFailureLightGrenadeEffect;
 import exception.*;
 import game.*;
 import grid.*;
 import item.*;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.RenderingHints;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.JOptionPane;
 
 import obstacle.*;
 import controller.*;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import simplegui.Button;
 import simplegui.SimpleGUI;
 
 public class UserInterface {
 	
 	public static void main(String[] args) {		
 
 		// All code that accesses the simple GUI must run in the AWT event
 		// handling thread.
 		// A simple way to achieve this is to run the entire application in the
 		// AWT event handling thread.
 		// This is done by simply wrapping the body of the main method in a call
 		// of EventQueue.invokeLater.		
 			
 		java.awt.EventQueue.invokeLater(new Runnable() {
 			
 			HashMap<Colour,Image> players = new HashMap<Colour,Image>();
 			HashMap<Colour,Image> cellStarts = new HashMap<Colour,Image>();
 			HashMap<Colour,Image> lightTrails = new HashMap<Colour,Image>();		
 			HashMap<Colour,Image> indicators = new HashMap<Colour,Image>();		
 			HashMap<Colour,Image> flags = new HashMap<Colour,Image>();
 			
 			Image cell;
 			Image pf;
 			Image pf2;
 			Image pf3;			
 			
 			Image lightGrenade;
 			Image wall;
 			Image identityDisk;
 			Image chargedIdentityDisk;
 			Image teleporter;
 			Image forceFieldGeneratorActive;
 			Image forceFieldGeneratorInactive;
 			Image forceField;
 			
 			Image marker;
 			
 			GameController game = new GameController();
 			
 			long startTime;			
 			long endTime;
 			
 			int width;
 			int height;
 			
 			boolean quit;
 			
 			// Initial message.
 			String systemMessage;
 			
 			// Game User Interface.
 			SimpleGUI gui;
 
 			Button pickUp;
 			Button use;
 			Button endTurn;
 			Button startNewGame;
 			Button e;
 			Button w;
 			Button n;
 			Button nE;
 			Button nW;
 			Button s;
 			Button sE;
 			Button sW;
 			
 			Button[] buttons;
 			
 			/**
 			 * Disable all buttons except start new game
 			 */
 			public void disableButtons(){
 				for(Button b: buttons){
 					b.setEnabled(false);
 				}
 				startNewGame.setEnabled(true);
 			}
 			/**
 			 * Enable all buttons
 			 */
 			public void enableButtons(){
 				for(Button b: buttons){
 					b.setEnabled(true);
 				}
 			}
 			
 			public String getStats(){
 				String time = "Time: "+((endTime - startTime)/1000.0) + " s";
 				return time;
 			}
 			
 
 			/**
 			 * Test if the game has ended and show the winner if that's the case.
 			 */
 			public void checkEnd(){
 				if(game.isGameEnded()){
 					if(game.getWinnerColour() != null) {
 						endTime = System.currentTimeMillis();
 						String winMessage = game.getWinnerColour()+" has won the game!";
 						String stats = getStats();
 						JOptionPane.showMessageDialog(null,
 								winMessage+"\n"+stats);
 						systemMessage = "The game has ended. "+winMessage;
 						disableButtons();
 						gui.repaint();
 					}
 				}
 			}
 			
 			private void undecidedGame(){
 				endTime = System.currentTimeMillis();
 				String winMessage = "The game has ended undecided. All players died.";
 				String stats = getStats();
 				JOptionPane.showMessageDialog(null,
 						winMessage+"\n"+stats);
 				systemMessage = "The game has ended.";
 				disableButtons();
 				gui.repaint();
 			}
 			
 			/**
 			 * Move the current player in the given direction
 			 * @param direction The direction to move
 			 */
 			public void move(Direction direction){
 				try {
 					systemMessage = "You moved "+direction.toString()+". ";
 					systemMessage += getStepOnMessage(direction);
 					if(game.getCurrentActionsLeft() == 1){
 						systemMessage +="Turn switched.";
 					}
 					try {
 						game.getMoveController().move(direction);
 					} catch (NoPlayersLeftException e) {
 						undecidedGame();
 					}
 					
 					// Message for flag capturing.
					if(game.getGame().getMode() instanceof CaptureTheFlag) {
						Square sq = game.getCurrentPlayer().getLocation();
						Coordinate start = sq.getGrid().getStartingPositions().get(game.getCurrentPlayerColour());
						if(sq == sq.getGrid().getSquareAtCoordinate(start)) {
							for(int i=0;i<game.getUseItemController().getItemsInInventory().length;i++) {
								if(game.getUseItemController().getItemsInInventory()[i] instanceof Flag) {
									Flag flag = (Flag) game.getUseItemController().getItemsInInventory()[i];
									systemMessage +="You captured the " + flag.getColour().toString() + " flag.";
								}
 							}
 						}
 					}
 					
 				} catch (InvalidMoveException e) {
 					systemMessage = "Move forbidden!";
 				} catch (OutsideTheGridException e) {
 					// Will not occur.
 				}
 				
 				gui.repaint();
 				checkEnd();
 				
 			}
 			
 			private String getStepOnMessage(Direction dir) {
 				String message = "";
 				Square stepOnSquare;
 				try {
 					stepOnSquare = game.getMoveController().getCurrentPlayerLocation().getNeighbour(dir);
 				} catch (OutsideTheGridException e) {
 					return message;
 				}
 				Item[] items = stepOnSquare.getItems();
 				
 				boolean hasPf = false;
 				for(Item item:items) {	
 					if(item instanceof PowerFailure) hasPf = true;
 				}
 				if(!hasPf) {
 					if(items.length > 0){					
 						for(Item i: stepOnSquare.getItems()){
 							if(i instanceof LightGrenade){
 								LightGrenade lg = (LightGrenade) i;
 								LightGrenadeState lgState = lg.getState();
 								if(lgState instanceof ActiveLightGrenade){
 									message+= "You stepped on a light grenade and lose "
 											+ LightGrenadeEffect.DAMAGE +" actions. ";
 								}					
 							} else if(i instanceof Teleporter) {
 								message += "You stepped on a teleporter and were teleported.";
 							} else if(i instanceof ForceFieldGenerator) {
 								message += "";
 							} else if(i instanceof IdentityDisk) {
 								message += "";
 //								for(Effect e: stepOnSquare.getEffects()) { // There is an identity disk effect on the square
 //									if(e instanceof IdentityDiskEffect) {
 //										message += "You were shot by an identity disk. ";
 //									}
 //								}	
 							} else if(i instanceof Flag) {
 								message += "";
 							} else {
 								throw new UnsupportedOperationException("Not yet implemented.");
 							}			
 						}
 					}			
 					return message;
 				}	
 				else{
 					message += "You stepped on a square with a power failure. ";
 				
 					if(items.length == 1){ // Only a power failure item.
 						message += "The turn goes to the next player. ";
 					}
 					else{						
 						for(Item i: stepOnSquare.getItems()){
 							if(!(i instanceof PowerFailure)) {
 								if(i instanceof LightGrenade){
 									LightGrenade lg = (LightGrenade) i;
 									LightGrenadeState lgState =lg.getState();
 									if(lgState instanceof ActiveLightGrenade){
 										message+= "You also stepped on a "+ i.toString() + ". You lost " + PowerFailureLightGrenadeEffect.DAMAGE+" actions. ";
 									}
 								} 
 								else if(i instanceof Teleporter) {
 									message += "You stepped on a teleporter and were teleported.";
 								}	
 							}
 						}
 					}
 				}
 				
 				return message;
 				
 				
 				
 			}
 			
 			public int showIntegerDialog(String message){
 				int result;
 				while (true) {
 					try {
 						String s = JOptionPane.showInputDialog(message);
 						if(s == null) {
 							s = "999";
 						}
 						result = Integer.parseInt(s);
 						break;
 					} catch (NumberFormatException e) {
 						message += "\nPlease input an integer.";
 						continue;
 					}
 				}
 				return result;
 			}
 			
 			public int showGameTypeDialog(){
 				Object[] options = {"Choose Race Mode","Choose Capture The Flag Mode",
 		        "Quit"};
 				int answer = JOptionPane.showOptionDialog(null,
 				"Start the game by selecting the game mode.",
 				"Objectron",
 				JOptionPane.YES_NO_OPTION,
 				JOptionPane.QUESTION_MESSAGE,
 				null,     //do not use a custom Icon
 				options,  //the titles of buttons
 				null); //default button title
 				return answer;
 			}
 
 			public void showMainMenu() {
 				//show the main menu
 				Object[] options = {"Choose Generated Grid","Choose Grid From File",
 		        "Quit"};
 				int answer = JOptionPane.showOptionDialog(null,
 				"Choose the grid on which you want to play.",
 				"Objectron",
 				JOptionPane.YES_NO_OPTION,
 				JOptionPane.QUESTION_MESSAGE,
 				null,     //do not use a custom Icon
 				options,  //the titles of buttons
 				null); //default button title
 				
 				//ask for the width and height of the grid
 				if (answer == 0) {
 					while (true) {
 						width = showIntegerDialog("Choose the width of the grid.");
 						if(width == 999) {
 							showMainMenu();
 							break;
 						}		
 						else {
 							height = showIntegerDialog("Choose the height of the grid.");
 							if(height == 999) {
 								showMainMenu();
 								break;
 							}	
 							else {
 								try {
 									int answer2 = showGameTypeDialog();
 									if (answer2 == 0) {
 										game.startNewRandomRace(width, height);
 										systemMessage = "Welcome! Objectron starts when "
 												+ game.getCurrentPlayerColour()
 												+ " performs his first action.";
 										if (gui != null) {
 											gui.resize(40 * width, 40 * (height + 4));
 											enableButtons();
 										}
 									} else if (answer2 == 1) {
 										int nbPlayers = 999;
 										boolean validNb = false;
 										while(!validNb) {
 											nbPlayers = showIntegerDialog("Choose the number of players.");
 											if (!(nbPlayers >= 2 && nbPlayers <= 9)) {
 												if (nbPlayers == 999) validNb = true;
 												else {
 												JOptionPane.showMessageDialog(null,
 														"The given number of players is invalid.");
 												continue;
 												}
 											}
 											else validNb = true;
 										}
 										if (nbPlayers == 999) {
 											showMainMenu();
 											break;
 										}
 										else {
 											game.startNewRandomCTF(width, height, nbPlayers);
 											systemMessage = "Welcome! Objectron starts when "
 													+ game.getCurrentPlayerColour()
 													+ " performs his first action.";
 											if (gui != null) {
 												gui.resize(40 * width, 40 * (height + 4));
 												enableButtons();
 											}
 										}
 									} else {
 										if (gui != null)
 											gui.quit();
 										quit = true;
 									}
 									break;
 								} catch (InvalidDimensionException e1) {
 									JOptionPane.showMessageDialog(null,
 											"The given dimensions are invalid.");
 									continue;
 								} catch (InvalidNbPlayersException e) {
 									// will not occur
 								}
 							}
 						}
 					}
 				} else if (answer == 1) {
 					while (true) {
 						File file = null;
 						JFileChooser chooser = new JFileChooser();
 						FileNameExtensionFilter filter = new FileNameExtensionFilter(
 								"Grid Files", "grid");
 						chooser.setFileFilter(filter);
 						int returnVal = chooser.showOpenDialog(chooser);
 						if (returnVal == JFileChooser.APPROVE_OPTION) {
 							file = chooser.getSelectedFile();
 						} else {
 							showMainMenu();
 							break;
 						}
 						try {
 							int answer2 = showGameTypeDialog();
 							if (answer2 == 0) {
 								game.startNewRaceFromFile(file);
 
 								systemMessage = "Welcome! Objectron starts when "
 										+ game.getCurrentPlayerColour()
 										+ " performs his first action.";
 
 								if (gui != null) {
 									gui.resize(40 * width,
 											40 * (height + 4));
 									enableButtons();
 								}
 							} else if (answer2 == 1) {
 								int nbPlayers = 999;
 								boolean validNb = false;
 								while(!validNb) {
 									nbPlayers = showIntegerDialog("Choose the number of players [2-9].");
 									if (!(nbPlayers >= 2 && nbPlayers <= 9)) {
 										if (nbPlayers == 999) validNb = true;
 										else {
 										JOptionPane.showMessageDialog(null,
 												"The given number of players is invalid.");
 										continue;
 										}
 									}
 									else validNb = true;
 								}
 								if (nbPlayers == 999) {
 									showMainMenu();
 									break;
 								}
 								else {
 									game.startNewCTFFromFile(file, nbPlayers);
 									systemMessage = "Welcome! Objectron starts when "
 											+ game.getCurrentPlayerColour()
 											+ " performs his first action.";
 									if (gui != null) {
 										gui.resize(40 * width,
 												40 * (height + 4));
 										enableButtons();
 									}
 								}
 							} else {
 								if (gui != null)
 									gui.quit();
 								quit = true;
 							}
 						} catch (InvalidFileException e) {
 							JOptionPane.showMessageDialog(null,
 									"The given file is invalid.");
 							continue;
 						} catch (FileNotFoundException e) {
 							JOptionPane.showMessageDialog(null,
 									"The file was not found.");
 							continue;
 						} catch (InvalidDimensionException e) {
 							JOptionPane.showMessageDialog(null,
 									"The given dimensions are invalid.");
 							continue;
 						} catch (InvalidNbPlayersException e) {
 							// will not occur
 						}
 
 					}
 				}	
 				else{
 					if(gui!=null) gui.quit();
 					quit = true;
 				}
 			}
 
 			public void run() {				
 				showMainMenu();
 				
 				if(quit){
 					return;
 				}
 				
 				gui = new SimpleGUI("Objectron", 40 * width, 40 * (height + 4)) {
 
 					@Override
 					public void paint(Graphics2D graphics) {
 						
 						
 						
 						//activate anti aliasing
 						graphics.setRenderingHint
 						  (RenderingHints.KEY_ANTIALIASING,
 						   RenderingHints.VALUE_ANTIALIAS_ON);
 						
 						
 						
 						
 						// Draw the squares.
 						for (int i = 1; i < width + 1; i++) {
 							for (int j = 1; j < height + 1; j++) {
 
 								try {
 									
 									Square square = game.getSquareAtCoordinate(i, j);
 									
 									
 									if(game.isStartingSquare(square)){	
 												Colour pC = ((StartingPosition) square).getUser();
 											
 												Image cellStart = cellStarts.get(pC);
 												graphics.drawImage(cellStart, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 																		
 									}
 									//draw the power failures
 									else {
 										boolean normal = true;
 										for(Item item: square.getItems()){
 											if(item instanceof PrimaryPowerFailure){
 												graphics.drawImage(pf, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 												normal = false;
 												break;
 											}
 											else if(item instanceof SecondaryPowerFailure){
 												graphics.drawImage(pf2, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 												normal = false;
 											}
 											else if(item instanceof TertiaryPowerFailure){
 												graphics.drawImage(pf3, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 												normal = false;
 											}
 										}
 										if(normal){
 											// Draw normal square.
 											graphics.drawImage(cell, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 
 										}
 									}
 								
 									
 									if (square.marker) {
 										graphics.drawImage(marker, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 									}
 									
 									
 									
 									// Set the obstacles.
 									if (square.hasObstacle()) {
 										Obstacle[] obstacles = square.getObstacles();
 										
 										for(Obstacle obstacle : obstacles){
 											// Walls
 											if (obstacle instanceof Wall) {
 												graphics.drawImage(wall, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 											} else if (obstacle instanceof LightTrail){
 												LightTrail lightTrail = (LightTrail) obstacle;
 												Image lt = lightTrails.get(lightTrail.getOwner().getPlayerColour());
 												graphics.drawImage(lt, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 
 											} else if ((obstacle instanceof ForceField) && ((ForceField)obstacle).getState().isActive()){
 												graphics.drawImage(forceField, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 											}
 											
 										}
 									}
 									// draw the items.
 									if (square.getNbItems()>0) {
 										for (Item item: square.getItems()){
 											if (item instanceof LightGrenade) {
 												if (((LightGrenade) item).getState().isVisible()) {
 													graphics.drawImage(lightGrenade, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 												}
 											}
 											else if (item instanceof IdentityDisk){
 												if(item instanceof ChargedIdentityDisk){
 													graphics.drawImage(chargedIdentityDisk, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 
 												}
 												else{
 													graphics.drawImage(identityDisk, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 
 												}
 											}
 											else if (item instanceof Teleporter){
 												graphics.drawImage(teleporter, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 
 											}
 											else if (item instanceof ForceFieldGenerator){
 												if(((ForceFieldGenerator) item).isActive()){
 													graphics.drawImage(forceFieldGeneratorActive, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 
 												}
 												else{
 													graphics.drawImage(forceFieldGeneratorInactive, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 
 												}
 											}
 											else if (item instanceof Flag){
 
 												Image flag = flags.get(((Flag)item).getColour());
 												graphics.drawImage(flag, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 
 											}
 										}
 									}
 									
 									// draw the players.
 									if (square.hasPlayer()) {
 										Image player = players.get(square.getPlayer().getPlayerColour());
 										graphics.drawImage(player, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 										
 									}
 									
 									}
 
 								 catch (OutsideTheGridException e) {
 									// Can not occur because of bounded i and j.
 								}
 
 							}
 						}
 						
 						// draw the colour of the current player
 						Image indicator = indicators.get(game.getCurrentPlayerColour());
 						graphics.drawImage(indicator, 40, 40, 40, 40, null);
 						
 						graphics.drawString(systemMessage, 5, 145);
 						
 						Font font1 = new Font("SansSerif", Font.BOLD, 30);
 						graphics.setFont(font1);
 						graphics.setColor(Color.WHITE);
 						
 						//draw the number of actions left
 						graphics.drawString(""+game.getCurrentActionsLeft(), 53, 71);
 
 					
 						
 						}
 
 				
 
 				};
 
 				cell = gui.loadImage("simplegui/objectron/black.png", 40, 40);
 				pf = gui.loadImage("simplegui/objectron/yellow.png", 40, 40);
 				pf2 = gui.loadImage("simplegui/objectron/darkeryellow.png", 40, 40);
 				pf3 = gui.loadImage("simplegui/objectron/darkestyellow.png", 40, 40);
 				wall = gui.loadImage("simplegui/objectron/wall.png", 40, 40);
 				lightGrenade = gui.loadImage("simplegui/objectron/lightgrenade.png", 40, 40);
 				
 				players.put(Colour.RED,gui.loadImage("simplegui/objectron/player_red.png",40, 40));
 				players.put(Colour.BLUE,gui.loadImage("simplegui/objectron/player_blue.png", 40, 40));
 				players.put(Colour.GREEN,gui.loadImage("simplegui/objectron/player_green.png", 40, 40));
 				players.put(Colour.YELLOW,gui.loadImage("simplegui/objectron/player_yellow.png", 40, 40));
 				players.put(Colour.BROWN,gui.loadImage("simplegui/objectron/player_brown.png", 40, 40));
 				players.put(Colour.WHITE,gui.loadImage("simplegui/objectron/player_white.png", 40, 40));
 				players.put(Colour.BLACK,gui.loadImage("simplegui/objectron/player_black.png", 40, 40));
 				players.put(Colour.PURPLE,gui.loadImage("simplegui/objectron/player_purple.png", 40, 40));
 				players.put(Colour.ORANGE,gui.loadImage("simplegui/objectron/player_orange.png", 40, 40));
 
 				cellStarts.put(Colour.BLUE, gui.loadImage("simplegui/objectron/cell_start_blue.png", 40, 40));
 				cellStarts.put(Colour.RED,gui.loadImage("simplegui/objectron/cell_start_red.png", 40, 40));
 				cellStarts.put(Colour.GREEN,gui.loadImage("simplegui/objectron/cell_start_green.png", 40, 40));
 				cellStarts.put(Colour.YELLOW,gui.loadImage("simplegui/objectron/cell_start_yellow.png", 40, 40));
 				cellStarts.put(Colour.BROWN,gui.loadImage("simplegui/objectron/cell_start_brown.png", 40, 40));
 				cellStarts.put(Colour.WHITE,gui.loadImage("simplegui/objectron/cell_start_white.png", 40, 40));
 				cellStarts.put(Colour.BLACK,gui.loadImage("simplegui/objectron/cell_start_black.png", 40, 40));
 				cellStarts.put(Colour.PURPLE,gui.loadImage("simplegui/objectron/cell_start_purple.png", 40, 40));
 				cellStarts.put(Colour.ORANGE,gui.loadImage("simplegui/objectron/cell_start_orange.png", 40, 40));
 
 				
 				identityDisk = gui.loadImage("simplegui/objectron/idDisk.png", 40, 40);
 				chargedIdentityDisk = gui.loadImage("simplegui/objectron/chargedidDisk.png", 40, 40);
 				teleporter = gui.loadImage("simplegui/objectron/teleporter.png", 40, 40);
 				
 				forceFieldGeneratorActive = gui.loadImage("simplegui/objectron/force_field_generator_active.png", 40, 40);
 				forceFieldGeneratorInactive = gui.loadImage("simplegui/objectron/force_field_generator_inactive.png", 40, 40);
 				forceField = gui.loadImage("simplegui/objectron/force_field.png", 40, 40);
 
 
 
 				lightTrails.put(Colour.BLUE,gui.loadImage("simplegui/objectron/cell_lighttrail_blue.png", 40, 40));
 				lightTrails.put(Colour.RED,gui.loadImage("simplegui/objectron/cell_lighttrail_red.png", 40, 40));
 				lightTrails.put(Colour.GREEN,gui.loadImage("simplegui/objectron/cell_lighttrail_green.png", 40, 40));
 				lightTrails.put(Colour.YELLOW,gui.loadImage("simplegui/objectron/cell_lighttrail_yellow.png", 40, 40));
 				lightTrails.put(Colour.BROWN,gui.loadImage("simplegui/objectron/cell_lighttrail_brown.png", 40, 40));
 				lightTrails.put(Colour.WHITE,gui.loadImage("simplegui/objectron/cell_lighttrail_white.png", 40, 40));
 				lightTrails.put(Colour.BLACK,gui.loadImage("simplegui/objectron/cell_lighttrail_black.png", 40, 40));
 				lightTrails.put(Colour.PURPLE,gui.loadImage("simplegui/objectron/cell_lighttrail_purple.png", 40, 40));
 				lightTrails.put(Colour.ORANGE,gui.loadImage("simplegui/objectron/cell_lighttrail_orange.png", 40, 40));
 				
 				flags.put(Colour.BLUE,gui.loadImage("simplegui/objectron/flag_blue.png", 40, 40));
 				flags.put(Colour.RED,gui.loadImage("simplegui/objectron/flag_red.png", 40, 40));
 				flags.put(Colour.GREEN,gui.loadImage("simplegui/objectron/flag_green.png", 40, 40));
 				flags.put(Colour.YELLOW,gui.loadImage("simplegui/objectron/flag_yellow.png", 40, 40));
 				flags.put(Colour.BROWN,gui.loadImage("simplegui/objectron/flag_brown.png", 40, 40));
 				flags.put(Colour.WHITE,gui.loadImage("simplegui/objectron/flag_white.png", 40, 40));
 				flags.put(Colour.BLACK,gui.loadImage("simplegui/objectron/flag_black.png", 40, 40));
 				flags.put(Colour.PURPLE,gui.loadImage("simplegui/objectron/flag_purple.png", 40, 40));
 				flags.put(Colour.ORANGE,gui.loadImage("simplegui/objectron/flag_orange.png", 40, 40));
 				
 				
 				indicators.put(Colour.RED,gui.loadImage("simplegui/objectron/red.png", 40, 40));
 				indicators.put(Colour.BLUE,gui.loadImage("simplegui/objectron/blue.png", 40, 40));
 				indicators.put(Colour.GREEN,gui.loadImage("simplegui/objectron/green.png", 40, 40));
 				indicators.put(Colour.YELLOW,gui.loadImage("simplegui/objectron/yellow.png", 40, 40));
 				indicators.put(Colour.BROWN,gui.loadImage("simplegui/objectron/brown.png", 40, 40));
 				indicators.put(Colour.PURPLE,gui.loadImage("simplegui/objectron/purple.png", 40, 40));
 				indicators.put(Colour.ORANGE,gui.loadImage("simplegui/objectron/orange.png", 40, 40));
 				indicators.put(Colour.WHITE,gui.loadImage("simplegui/objectron/white.png", 40, 40));
 				indicators.put(Colour.BLACK,gui.loadImage("simplegui/objectron/black.png", 40, 40));
 
 				
 				
 				marker = gui.loadImage("simplegui/objectron/marker.png", 40, 40);
 
 				
 
 				
 
 				
 				/**
 				 * Button to pick up an item.
 				 */
 				pickUp = gui.createButton(120, 0, 120, 40, new Runnable() {
 					public void run() {
 						try {
 							PortableItem[] items = game.getPickUpItemController().getVisiblePortableItemsAtCurrentLocation();
 							if(items.length == 0) throw new NoItemException();
 							PortableItem item = (PortableItem)JOptionPane.showInputDialog(
 				                    null,
 				                    "Choose an item:",
 				                    null,
 				                    JOptionPane.PLAIN_MESSAGE,
 				                    null,
 				                    items,
 				                    null);
 							if(item==null) {} //systemMessage = Cancelled pick up.}
 							else {
 								try {
 									game.getPickUpItemController().pickUpItem(item);
 								} catch (NoPlayersLeftException e) {
 									undecidedGame();
 								}
 								systemMessage = "You picked up a "+item.toString() + ".";
 							}
 							
 						} catch (NoItemException e) {
 							systemMessage = "There are no items on this square! Please perform another action.";
 						}
 						catch (OverCapacityException e){
 							systemMessage = "There is no place left in your inventory. Please perform another action.";
 						}
 						
 						gui.repaint();
 						checkEnd();
 					}
 				});
 				pickUp.setText("Pick Up");
 
 				/**
 				 * Button to use an item.
 				 */
 				use = gui.createButton(120, 40, 120, 40, new Runnable() {
 					public void run() {						
 						try {
 							PortableItem[] items = game.getUseItemController().getItemsInInventory();
 							if(items.length == 0) throw new NoItemException();
 							
 							PortableItem item = (PortableItem)JOptionPane.showInputDialog(
 				                    null,
 				                    "Choose an item:",
 				                    null,
 				                    JOptionPane.PLAIN_MESSAGE,
 				                    null,
 				                    items,
 				                    null);
 							if(item==null) { systemMessage = "You cancelled.";}
 							else {
 								if(!(item instanceof IdentityDisk)) {
 									try {
 										game.getUseItemController().useItem(item);
 									} catch (NoPlayersLeftException e) {
 										undecidedGame();
 									}
 									systemMessage = "You used a "+item.toString() + ". ";
 								}
 								else{
 									Direction direction = (Direction)JOptionPane.showInputDialog(
 											null,
 											"Choose a direction:",
 											null,
 											JOptionPane.PLAIN_MESSAGE,
 											null,
 											IdentityDisk.getPossibleDirections(),
 											null);
 									try {
 										game.getUseItemController().useItem((IdentityDisk)item,direction);
 									} catch (NoPlayersLeftException e) {
 										undecidedGame();
 									}
 									systemMessage = "You used a "+item.toString() + ". ";
 									if(item.getLocation() != null) {
 										if (item.getLocation().getPlayer() != null) {	 
 											systemMessage = "You hit the " + item.getLocation().getPlayer().getPlayerColour() + " player! He loses his next turn.";	
 										}
 									}
 								}
 								
 							}
 						}
 					 catch (NoItemException e) {
 						systemMessage = "You have no items in your inventory! Please perform another action.";
 					} catch(InvalidDirectionException e){
 						systemMessage = "This is not a valid direction.";
 					}
 					gui.repaint();
 					checkEnd();
 					}
 				});
 				use.setText("Use Item");
 
 				/**
 				 * Button to end the turn.
 				 */
 				endTurn = gui.createButton(120, 80, 120, 40, new Runnable() {
 					public void run() {
 						if(!game.getEndTurnController().hasMoved()){
 							int answer = JOptionPane.showConfirmDialog(null,
 									"You will lose if you did not yet move. Continue?",
 									null,
 									JOptionPane.YES_NO_OPTION);
 							if(answer==0){
 								try {
 									game.getEndTurnController().endTurn();
 								} catch (NoPlayersLeftException e) {
 									undecidedGame();
 								}
 							}
 						}
 						else{
 							int answer = JOptionPane.showConfirmDialog(null,
 									"Are you sure you want to end this turn?",
 									null,
 									JOptionPane.YES_NO_OPTION);
 							if(answer==0){
 								systemMessage = game.getCurrentPlayerColour() +" ended his turn." +
 										" Turn switched.";
 								try {
 									game.getEndTurnController().endTurn();
 								} catch (NoPlayersLeftException e) {
 									undecidedGame();
 								}
 								
 							}
 						}
 						
 						
 						gui.repaint();
 						checkEnd();
 
 					}
 				});
 				endTurn.setText("End Turn");
 				
 				/**
 				 * Start new game.
 				 */
 				startNewGame = gui.createButton(240, 0, 120, 40, new Runnable() {
 					public void run() {
 						showMainMenu();
 					}
 				});
 				startNewGame.setText("New game");
 
 
 
 				/**
 				 * Moving East.
 				 */
 				e = gui.createButton(80, 40, 40, 40, new Runnable() {
 					public void run() {
 						move(Direction.EAST);
 					}
 				});
 				e.setImage(gui.loadImage("simplegui/objectron/arrow_E.png", 40,
 						40));
 
 				/**
 				 * Moving West.
 				 */
 				w = gui.createButton(0, 40, 40, 40, new Runnable() {
 					public void run() {
 						move(Direction.WEST);
 					}
 				});
 				w.setImage(gui.loadImage("simplegui/objectron/arrow_W.png", 40,
 						40));
 
 				/**
 				 * Moving South.
 				 */
 				s = gui.createButton(40, 80, 40, 40, new Runnable() {
 					public void run() {
 						move(Direction.SOUTH);
 					}
 				});
 				s.setImage(gui.loadImage("simplegui/objectron/arrow_S.png", 40,
 						40));
 
 				/**
 				 * Moving South West.
 				 */
 				sW = gui.createButton(0, 80, 40, 40, new Runnable() {
 					public void run() {
 						move(Direction.SOUTHWEST);					}
 				});
 				sW.setImage(gui.loadImage("simplegui/objectron/arrow_SW.png",
 						40, 40));
 
 				/**
 				 * Moving South East.
 				 */
 				sE = gui.createButton(80, 80, 40, 40, new Runnable() {
 					public void run() {
 						move(Direction.SOUTHEAST);
 					}
 				});
 				sE.setImage(gui.loadImage("simplegui/objectron/arrow_SE.png",
 						40, 40));
 
 				/**
 				 * Moving North.
 				 */
 				n = gui.createButton(40, 0, 40, 40, new Runnable() {
 					public void run() {
 						move(Direction.NORTH);
 					}
 				});
 				n.setImage(gui.loadImage("simplegui/objectron/arrow_N.png", 40,
 						40));
 
 				/**
 				 * Moving North East.
 				 */
 				nE = gui.createButton(80, 0, 40, 40, new Runnable() {
 					public void run() {
 						move(Direction.NORTHEAST);
 					}
 				});
 				nE.setImage(gui.loadImage("simplegui/objectron/arrow_NE.png", 40, 40));
 
 				/**
 				 * Moving North West.
 				 */
 				nW = gui.createButton(0, 0, 40, 40, new Runnable() {
 					public void run() {
 						move(Direction.NORTHWEST);
 					}
 				});
 				nW.setImage(gui.loadImage("simplegui/objectron/arrow_NW.png",
 						40, 40));
 				
 				
 				
 				ArrayList<Button> buttonList = new ArrayList<Button>();
 				buttonList.add(pickUp);
 				buttonList.add(use);
 				buttonList.add(endTurn);
 				buttonList.add(startNewGame);
 				buttonList.add(e);
 				buttonList.add(w);
 				buttonList.add(n);
 				buttonList.add(s);
 				buttonList.add(nE);
 				buttonList.add(nW);
 				buttonList.add(sE);
 				buttonList.add(sW);
 				
 
 				
 				buttons = buttonList.toArray(new Button[buttonList.size()]);
 				
 				startTime = System.currentTimeMillis();
 
 			}
 			
 
 		});
 		
 		
 		}
 	}
 
 
 
