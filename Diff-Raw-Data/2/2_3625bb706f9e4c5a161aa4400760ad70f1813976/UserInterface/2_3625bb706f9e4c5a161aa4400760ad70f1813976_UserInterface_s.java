 package simplegui.objectron;
 
 import effect.Effect;
 import effect.IdentityDiskEffect;
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
 
 import javax.swing.JOptionPane;
 
 import obstacle.*;
 import controller.EndTurnController;
 import controller.GameController;
 import controller.MoveController;
 import controller.PickUpItemController;
 import controller.UseItemController;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import simplegui.Button;
 import simplegui.SimpleGUI;
 
 public class UserInterface {
 
 	public final static int DEFAULT_WIDTH_HEIGHT = 10;
 	
 	public static void main(String[] args) {		
 
 		// All code that accesses the simple GUI must run in the AWT event
 		// handling thread.
 		// A simple way to achieve this is to run the entire application in the
 		// AWT event handling thread.
 		// This is done by simply wrapping the body of the main method in a call
 		// of EventQueue.invokeLater.		
 			
 		java.awt.EventQueue.invokeLater(new Runnable() {
 
 			Image playerRed;
 			Image playerBlue;
 			Image cell;
 			Image pf;
 			Image pf2;
 			Image pf3;
 			
 			Image finishRed;
 			Image finishBlue;
 			Image lightGrenade;
 			Image wall;
 			Image lightTrailRed;
 			Image identityDisk;
 			Image chargedIdentityDisk;
 			Image teleporter;
 			Image forceFieldGeneratorActive;
 			Image forceFieldGeneratorInactive;
 			Image forceField;
 
 			
 
 			Image lightTrailBlue;	
 			Image redIndicator;
 			Image blueIndicator;
 			
 			Image marker;
 			
 			GameController game = GameController.getInstance();
 			
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
 					MoveController.getInstance().move(direction);
 					// systemMessage += "You have " + gameFacade.getActionsLeft()+ " actions left.";
 					
 				} catch (InvalidMoveException e) {
 					systemMessage = "Move forbidden!";
 				}
 				
 				gui.repaint();
 				checkEnd();
 				
 			}
 			
 			private String getStepOnMessage(Direction dir) {
 				String message = "";
 				Square stepOnSquare;
 				try {
 					stepOnSquare = MoveController.getInstance().getCurrentPlayerLocation().getNeighbour(dir);
 				} catch (OutsideTheGridException e) {
 					return message;
 				}
 				Item[] items = stepOnSquare.getItems();
 				
 				if(!stepOnSquare.hasPowerFailure()){
 				
 					if(items.length > 0){					
 						for(Item i: stepOnSquare.getItems()){
 							if(i instanceof LightGrenade){
 								LightGrenade lg = (LightGrenade) i;
 								LightGrenadeState lgState = lg.getState();
 								if(lgState instanceof ActiveLightGrenade){
 									message+= "You stepped on a light grenade," + 
 											"losing "+ LightGrenadeEffect.DAMAGE +" actions. ";
 								}
 								
 							} else if(i instanceof Teleporter) {
 								message += "You stepped on a teleporter and were teleported.";
 							} else if(i instanceof ForceFieldGenerator) {
 								message += "";
 							} else if(i instanceof IdentityDisk) {
 								for(Effect e: stepOnSquare.getEffects()) { // There is an identity disk effect on the square
 									if(e instanceof IdentityDiskEffect) {
 										message += "You were shot by an identity disk, losing " + 
 												IdentityDiskEffect.DAMAGE + " actions. ";
 									}
 								}
 								
 							} else {
 								throw new UnsupportedOperationException("Not yet implemented.");
 							}
 							
 						}
 					}
 					
 					return message;
 				}
 				
 				if(stepOnSquare.hasPowerFailure()){
 					
 					message += "You stepped on a square with a power failure. ";
 				
 					if(items.length == 0){
 						message += "The turn goes to the next player. ";
 					}
 					else{						
 						for(Item i: stepOnSquare.getItems()){
 							message+= "You stepped on a "+i.toString()+". ";
 							if(i instanceof LightGrenade){
 								LightGrenade lg = (LightGrenade) i;
 								LightGrenadeState lgState =lg.getState();
 								if(lgState instanceof ActiveLightGrenade){
 									message+= "You lose "+PowerFailureLightGrenadeEffect.DAMAGE+" actions. ";
 								}
 								else{
 									message += "The turn goes to the next player.";
 								}
 								
 							}
 							
 						}
 					}
 				}
 				
 				return message;
 				
 				
 				
 			}
 
 			public void showMainMenu(){
 				//show the main menu
 				Object[] options = {"Choose Generated Grid","Choose Grid From File",
 		        "Quit"};
 				int answer = JOptionPane.showOptionDialog(null,
 				"Start the game by selecting on what grid you want to play.",
 				"Objectron",
 				JOptionPane.YES_NO_OPTION,
 				JOptionPane.QUESTION_MESSAGE,
 				null,     //do not use a custom Icon
 				options,  //the titles of buttons
 				null); //default button title
 				
 				//default width and height
 				width = DEFAULT_WIDTH_HEIGHT;
 				height = DEFAULT_WIDTH_HEIGHT;
 				
 				//ask for the width and height of the grid
 				if(answer == 0 || answer == 1){
 					if(answer == 0){
 						while (true) {
 							String message;
 							message = "Choose the width of the grid.";
 							while (true) {
 								try {
 									String s = JOptionPane.showInputDialog(message);
 									if(s == null) showMainMenu();
 									width = Integer.parseInt(s);
 								
 									break;
 								} catch (NumberFormatException e) {
 									message += "\nPlease input an integer.";
 									continue;
 								}
 							}
 							message = "Choose the height of the grid.";
 							while (true) {
 								try {
 									String s = JOptionPane.showInputDialog(message);
 									if(s == null) showMainMenu();
 									height = Integer.parseInt(s);
 
 									break;
 								} catch (NumberFormatException e) {
 									message += "\nPlease input an integer.";
 									continue;
 								}
 							}
 							try {
 								game.startNewRandomGame(width,height);
 								systemMessage = "Welcome! Objectron starts when "+game.getCurrentPlayerColour()+" performs his first action.";
 								if(gui!=null){
 									gui.resize( 40 * width, 40 * (height + 4));
 									enableButtons();
 								}
 								break;
 							} catch (InvalidDimensionException e1) {
 								JOptionPane.showMessageDialog(null, "The given dimensions are invalid.");
 								continue;
 							}
 						}	
 					}
 					else if(answer == 1){
 						File file = null;
 						JFileChooser chooser = new JFileChooser();
 					    FileNameExtensionFilter filter = new FileNameExtensionFilter(
 					        "Grid Files", "grid");
 					    chooser.setFileFilter(filter);
 					    int returnVal = chooser.showOpenDialog(chooser);
 					    if(returnVal == JFileChooser.APPROVE_OPTION) {					   
 					           file = chooser.getSelectedFile();
 					    }
 					    else showMainMenu();
 		
 					    try {
 							game.startNewGameFromFile(file);
 						} catch (InvalidFileException e) {
 							System.out.println("INVALID FILE");
 						} catch (FileNotFoundException e) {
 							System.out.println("FILE NOT FOUND");
 						} catch (InvalidDimensionException e) {
 							System.out.println("INVALID DIMENSION");
 						}
 						systemMessage = "Welcome! Objectron starts when "+game.getCurrentPlayerColour()+" performs his first action.";
 
 						if(gui!=null){
 							gui.resize( 40 * width, 40 * (height + 4));
 							enableButtons();
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
 									
 									Square square = GameController.getInstance().getSquareAtCoordinate(i, j);
 									
 									
 									if(game.isStartingSquare(square)){
 										
 										// Draw finish square.
 										for(Player p:game.getPlayers()){
 											if(square.equals(p.getStartingPosition())){
 												if(p.getPlayerColour() == PlayerColour.RED){
 													graphics.drawImage(finishBlue, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 												}
 												else{
 													graphics.drawImage(finishRed, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 
 												}
 											}
 										}										
 									}
 									else if (square.hasPowerFailure()) {
 										for(PowerFailure p: square.getPowerFailures()){
 											if(p instanceof PrimaryPowerFailure){
 												graphics.drawImage(pf, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 												break;
 											}
 											else if(p instanceof SecondaryPowerFailure){
 												graphics.drawImage(pf2, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 											}
 											else if(p instanceof TertiaryPowerFailure){
 												graphics.drawImage(pf3, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 
 											}
 										}
 									}
 									else{
 										// Draw normal square.
 										graphics.drawImage(cell, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 
 									}
 									
 									if (square.marker) {
 										graphics.drawImage(marker, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 									}
 									
 									
 									
 									// Set the obstacles.
 									if (square.hasObstacle()) {
 										Obstacle obstacle = square.getObstacle();
 										
 										// Walls
 										if (obstacle instanceof Wall) {
 											graphics.drawImage(wall, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 										} else if (square.getObstacle() instanceof LightTrail){
 											LightTrail lightTrail = (LightTrail) square.getObstacle();
 											if(lightTrail.getPlayer().getPlayerColour() == PlayerColour.RED){
 												graphics.drawImage(lightTrailRed, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 											}
 											else{
 												graphics.drawImage(lightTrailBlue, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 											}
										} else if (square.getObstacle() instanceof ForceField){
 											graphics.drawImage(forceField, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 										}
 										
 									}
 								
 									// Set the items.
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
 										}
 									}
 									
 									// Set the players.
 									if (square.hasPlayer()) {
 										Player player = square.getPlayer();
 
 										if (player.getPlayerColour() == PlayerColour.RED) {
 											// Draw the player.
 											graphics.drawImage(playerRed, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 										}
 
 										if (player.getPlayerColour() == PlayerColour.BLUE) {
 											// Draw the player.
 											graphics.drawImage(playerBlue, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 										}
 										
 									}
 									
 									}
 
 								 catch (OutsideTheGridException e) {
 									// Can not occur because of bounded i and j.
 								}
 
 							}
 						}
 						
 						// draw the colour of the current player
 						if (game.getCurrentPlayerColour() == PlayerColour.RED){
 							graphics.drawImage(redIndicator, 40, 40, 40, 40, null);
 
 						}
 							
 						else{
 							graphics.drawImage(blueIndicator, 40, 40, 40, 40, null);
 						}
 						
 						graphics.drawString(systemMessage, 5, 145);
 
 						
 						
 						Font font1 = new Font("SansSerif", Font.BOLD, 30);
 						graphics.setFont(font1);
 						graphics.setColor(Color.WHITE);
 						
 						//draw the number of actions left
 						graphics.drawString(""+game.getCurrentActionsLeft(), 53, 71);
 
 					
 						
 						}
 
 				
 
 				};
 
 				cell = gui.loadImage("simplegui/objectron/cell.png", 40, 40);
 				pf = gui.loadImage("simplegui/objectron/cellpf.png", 40, 40);
 				pf2 = gui.loadImage("simplegui/objectron/cellpf2.png", 40, 40);
 				pf3 = gui.loadImage("simplegui/objectron/cellpf3.png", 40, 40);
 				wall = gui.loadImage("simplegui/objectron/wall.png", 40, 40);
 				lightGrenade = gui.loadImage("simplegui/objectron/lightgrenade.png", 40, 40);
 				playerRed = gui.loadImage("simplegui/objectron/player_red.png",40, 40);
 				playerBlue = gui.loadImage("simplegui/objectron/player_blue.png", 40, 40);
 				finishBlue = gui.loadImage("simplegui/objectron/cell_finish_blue.png", 40, 40);
 				finishRed = gui.loadImage("simplegui/objectron/cell_finish_red.png", 40, 40);
 				
 				identityDisk = gui.loadImage("simplegui/objectron/idDisk.png", 40, 40);
 				chargedIdentityDisk = gui.loadImage("simplegui/objectron/chargedidDisk.png", 40, 40);
 				teleporter = gui.loadImage("simplegui/objectron/teleporter.png", 40, 40);
 				
 				forceFieldGeneratorActive = gui.loadImage("simplegui/objectron/force_field_generator_active.png", 40, 40);
 				forceFieldGeneratorInactive = gui.loadImage("simplegui/objectron/force_field_generator_inactive.png", 40, 40);
 				forceField = gui.loadImage("simplegui/objectron/force_field.png", 40, 40);
 
 
 
 				lightTrailBlue = gui.loadImage("simplegui/objectron/cell_lighttrail_blue.png", 40, 40);
 				lightTrailRed = gui.loadImage("simplegui/objectron/cell_lighttrail_red.png", 40, 40);
 				
 				redIndicator = gui.loadImage("simplegui/objectron/red.png", 40, 40);
 				blueIndicator = gui.loadImage("simplegui/objectron/blue.png", 40, 40);
 				
 				
 				marker = gui.loadImage("simplegui/objectron/marker.png", 40, 40);
 
 				
 
 				
 
 				
 				/**
 				 * Button to pick up an item.
 				 */
 				pickUp = gui.createButton(120, 0, 120, 40, new Runnable() {
 					public void run() {
 						try {
 							PortableItem[] items = PickUpItemController.getInstance().getVisiblePortableItemsAtCurrentLocation();
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
 								PickUpItemController.getInstance().pickUpItem(item);
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
 							PortableItem[] items = UseItemController.getInstance().getItemsInInventory();
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
 								if(!(item instanceof IdentityDisk))
 									UseItemController.getInstance().useItem(item);
 								else{
 									Direction direction = (Direction)JOptionPane.showInputDialog(
 											null,
 											"Choose a direction:",
 											null,
 											JOptionPane.PLAIN_MESSAGE,
 											null,
 											IdentityDisk.getPossibleDirections(),
 											null);
 									UseItemController.getInstance().useItem((IdentityDisk)item,direction);
 								}
 								systemMessage = "You used a "+item.toString() + ". ";
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
 						if(!EndTurnController.getInstance().hasMoved()){
 							int answer = JOptionPane.showConfirmDialog(null,
 									"You will lose if you did not yet move. Continue?",
 									null,
 									JOptionPane.YES_NO_OPTION);
 							if(answer==0){
 								EndTurnController.getInstance().endTurn();
 							}
 						}
 						else{
 							int answer = JOptionPane.showConfirmDialog(null,
 									"Are you sure you want to end this turn?",
 									null,
 									JOptionPane.YES_NO_OPTION);
 							if(answer==0){
 								EndTurnController.getInstance().endTurn();
 								systemMessage = game.getCurrentPlayerColour() +" ended his turn." +
 										" Turn switched.";
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
 
 
 
