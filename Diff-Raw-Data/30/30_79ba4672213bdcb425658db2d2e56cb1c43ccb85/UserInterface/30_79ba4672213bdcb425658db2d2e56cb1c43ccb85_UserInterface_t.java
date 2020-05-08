 package simplegui.objectron;
 
 import exception.*;
 import game.*;
 import grid.*;
 import item.*;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.RenderingHints;
 import java.util.ArrayList;
 
 import javax.swing.JOptionPane;
 
 import access.GameFacade;
 
 import obstacle.*;
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
 			Image finishRed;
 			Image finishBlue;
 			Image lightGrenade;
 			Image wall;
 			Image lightTrailRed;
 
 			Image lightTrailBlue;	
 			Image redIndicator;
 			Image blueIndicator;
 			
 			GameFacade gameFacade;
 			
 			long startTime;			
 			long endTime;
 			
 			int width;
 			int height;
 			
 			// Initial message.
 			String systemMessage;
 			
 			// Game User Interface.
 			SimpleGUI gui;
 
 			Button pickUp;
 			Button use;
 			Button endTurn;
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
 			 * Disable all buttons 
 			 */
 			public void disableButtons(){
 				for(Button b: buttons){
 					b.setEnabled(false);
 				}
 			}
 			
 			public String getStats(){
 				String time = "Time: "+((endTime - startTime)/1000.0) + " s";
 				return time;
 			}
 			
 
 			/**
 			 * Test if the game has ended and show the winner if that's the case.
 			 */
 			public void testWin(){
 				if(gameFacade.isGameEnded()){
 					endTime = System.currentTimeMillis();
 					String winMessage = gameFacade.getWinnerPlayerColour()+" has won the game!";
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
 					if(gameFacade.getActionsLeft() == 1){
 						systemMessage +="Turn switched.";
 					}
 					gameFacade.move(direction);
 					systemMessage += "You have " + gameFacade.getActionsLeft()+ " actions left.";
 					
 				} catch (InvalidMoveException e) {
 					systemMessage = "Move forbidden!";
 				}
 				
 				
 				gui.repaint();
 				testWin();
 				
 			}
 			
 			private String getStepOnMessage(Direction dir) {
 				String message = "";
 				Square stepOnSquare;
 				try {
 					stepOnSquare = gameFacade.getCurrentLocation().getNeighbour(dir);
 				} catch (OutsideTheGridException e) {
 					return message;
 				}
 				Item[] items = stepOnSquare.getItems();
 				
 				if(stepOnSquare.getState() instanceof NormalSquare){
 					NormalSquare state = (NormalSquare) stepOnSquare.getState();
 				
 					if(items.length == 0){
 						message += "Nothing happened.";
 					}
 					else{						
 						for(Item i: stepOnSquare.getItems()){
 							message+= "You stepped on "+i.toString()+". ";
 							if(i instanceof LightGrenade){
 								LightGrenade lg = (LightGrenade) i;
 								LightGrenadeState lgState =lg.getState();
 								if(lgState instanceof ActiveLightGrenade){
 									ActiveLightGrenade algState = (ActiveLightGrenade) lgState;
 									message+= "You lose "+algState.NORMAL_DAMAGE+" actions.";
 								}
 								else{
 									message += "Nothing happened.";
 								}
 								
 							}
 							else{
 								throw new UnsupportedOperationException("Not yet implemented.");
 							}
 							
 						}
 					}
 					
 					return message;
 				}
 				
 				if(stepOnSquare.getState() instanceof PowerFailureSquare){
 					PowerFailureSquare state = (PowerFailureSquare) stepOnSquare.getState();
 					
 					systemMessage += "Watch out! You stepped on a square with a power failure. ";
 				
 					if(items.length == 0){
 						systemMessage += "The turn goes to the next player.";
 					}
 					else{						
 						for(Item i: stepOnSquare.getItems()){
 							systemMessage+= "You stepped on "+i.toString()+". ";
 							if(i instanceof LightGrenade){
 								LightGrenade lg = (LightGrenade) i;
 								LightGrenadeState lgState =lg.getState();
 								if(lgState instanceof ActiveLightGrenade){
 									ActiveLightGrenade algState = (ActiveLightGrenade) lgState;
 									systemMessage+= "You lose "+algState.POWERFAILURE_DAMAGE+" actions. ";
 								}
 								else{
 									systemMessage += "The turn goes to the next player.";
 								}
 								
 							}
 							
 						}
 					}
 				}
 				
 				return systemMessage;
 				
 				
 				
 			}
 
 			public GameFacade showMainMenu(){
 				//show the main menu
 				Object[] options = {"Start game with default dimensions","Start custom game",
 		        "Quit"};
 				int answer = JOptionPane.showOptionDialog(null,
 				"Do you want to play?",
 				"Objectron",
 				JOptionPane.YES_NO_OPTION,
 				JOptionPane.QUESTION_MESSAGE,
 				null,     //do not use a custom Icon
 				options,  //the titles of buttons
 				null); //default button title
 				
 				int width = DEFAULT_WIDTH_HEIGHT;
 				int height = DEFAULT_WIDTH_HEIGHT;
 				GameFacade gameFacade = null;
 				
 				//ask for the width and height of the grid
 				if(answer == 0 || answer == 1){
 					if(answer == 1){
 						while (true) {
 							String message;
 							message = "Choose the width of the grid.";
 							while (true) {
 								try {
 									width = Integer.parseInt(JOptionPane
 											.showInputDialog(message));
 									break;
 								} catch (NumberFormatException e1) {
 									message += "\nPlease input an integer.";
 									continue;
 								}
 							}
 							message = "Choose the height of the grid.";
 							while (true) {
 								try {
 									height = Integer.parseInt(JOptionPane
 											.showInputDialog(message));
 									break;
 								} catch (NumberFormatException e1) {
 									message += "\nPlease input an integer.";
 									continue;
 								}
 							}
 							
 							try {
 								gameFacade = new GameFacade(new Game(width, height));
 								return gameFacade;
 							} catch (InvalidDimensionException e1) {
 								JOptionPane.showMessageDialog(null, "The given dimensions are invalid.");
 								continue;
 							}
 						}	
 					}
 					else if(answer == 0){
 						try {
 							gameFacade = new GameFacade(new Game(width, height));
 							return gameFacade;
 						} catch (InvalidDimensionException e1) {
 							// Cannot happen
 							e1.printStackTrace();
 						}
 					}
 				}
 				return null;
 			}
 
 			public void run() {
 				
 				gameFacade = showMainMenu();
 				
 				// user chose to quit
 				if(gameFacade == null){
 					return;
 				}
 				
 				width = gameFacade.getWidth();
 				height = gameFacade.getHeight();
 				
 				
 				systemMessage = "Welcome! Objectron starts when "+gameFacade.getCurrentPlayerColour()+" performs his first action.";
 
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
 									
 									Square square = gameFacade.getSquareAtCoordinate(i, j);
 									
 									if(gameFacade.isStartingSquare(square)){
 										// Draw finish square.
 										for(Player p:gameFacade.getPlayers()){
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
 									else{
 										// Draw normal square.
 										graphics.drawImage(cell, (i - 1) * 40, (height - j + 4) * 40, 39, 39, null);
 
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
 						if (gameFacade.getCurrentPlayerColour() == PlayerColour.RED){
 							graphics.drawImage(redIndicator, 40, 40, 40, 40, null);
 
 						}
 							
 						else{
 							graphics.drawImage(blueIndicator, 40, 40, 40, 40, null);
 						}
 						
 						
 						graphics.drawString(systemMessage, 10, 145);
 
 						
 						
 						Font font1 = new Font("SansSerif", Font.BOLD, 30);
 						graphics.setFont(font1);
 						graphics.setColor(Color.WHITE);
 						
 						//draw the number of actions left
 						graphics.drawString(""+gameFacade.getActionsLeft(), 53, 71);
 
 					
 						
 						}
 
 				
 
 				};
 
 				cell = gui.loadImage("simplegui/objectron/cell.png", 40, 40);
 				wall = gui.loadImage("simplegui/objectron/wall.png", 40, 40);
 				lightGrenade = gui.loadImage("simplegui/objectron/lightgrenade.png", 40, 40);
 				playerRed = gui.loadImage("simplegui/objectron/player_red.png",40, 40);
 				playerBlue = gui.loadImage("simplegui/objectron/player_blue.png", 40, 40);
 				finishBlue = gui.loadImage("simplegui/objectron/cell_finish_blue.png", 40, 40);
 				finishRed = gui.loadImage("simplegui/objectron/cell_finish_red.png", 40, 40);
 
 				lightTrailBlue = gui.loadImage("simplegui/objectron/cell_lighttrail_blue.png", 40, 40);
 				lightTrailRed = gui.loadImage("simplegui/objectron/cell_lighttrail_red.png", 40, 40);
 				
 				redIndicator = gui.loadImage("simplegui/objectron/red.png", 40, 40);
 				blueIndicator = gui.loadImage("simplegui/objectron/blue.png", 40, 40);
 
 				
 
 				
 
 				
 				/**
 				 * Button to pick up an item.
 				 */
 				pickUp = gui.createButton(120, 0, 120, 40, new Runnable() {
 					public void run() {
 						try {
 							Item[] items = gameFacade.getItemsAtCurrentLocation();
 							Item item = (Item)JOptionPane.showInputDialog(
 				                    null,
 				                    "Choose an item:",
 				                    null,
 				                    JOptionPane.PLAIN_MESSAGE,
 				                    null,
 				                    items,
 				                    null);
 							gameFacade.pickUpItem(item);
 							systemMessage = "You picked up a "+item.toString() + ".";
 						} catch (NoItemException e) {
 							systemMessage = "There are no items on this square! Please perform another action.";
 						}
 						catch (OverCapacityException e){
 							systemMessage = "There is no place left in your inventory. Please perform another action.";
 						}
 						
 						gui.repaint();
 					}
 				});
 				pickUp.setText("Pick Up");
 
 				/**
 				 * Button to use an item.
 				 */
 				use = gui.createButton(120, 40, 120, 40, new Runnable() {
 					public void run() {						
 						try {
 							Item[] items = gameFacade.getItemsInInventory();
 							if(items.length == 0) throw new NoItemException();
 							
 							Item item = (Item)JOptionPane.showInputDialog(
 				                    null,
 				                    "Choose an item:",
 				                    null,
 				                    JOptionPane.PLAIN_MESSAGE,
 				                    null,
 				                    items,
 				                    null);
 							gameFacade.useItem(item);
 							systemMessage = "You used a "+item.toString() + ".";
 						}
 					 catch (NoItemException e) {
 						systemMessage = "You have no items in your inventory! Please perform another action.";
 					}
 					gui.repaint();
 					}
 				});
 				use.setText("Use Item");
 
 				/**
 				 * Button to end the turn.
 				 */
 				endTurn = gui.createButton(120, 80, 120, 40, new Runnable() {
 					public void run() {
 						if(!gameFacade.hasMoved()){
 							int answer = JOptionPane.showConfirmDialog(null,
 									"You will lose if you did not yet move. Continue?",
 									null,
 									JOptionPane.YES_NO_OPTION);
 							if(answer==0){
 								gameFacade.endTurn();
 							}
 						}
 						else{
 							int answer = JOptionPane.showConfirmDialog(null,
 									"Are you sure you want to end this turn?",
 									null,
 									JOptionPane.YES_NO_OPTION);
 							if(answer==0){
 								gameFacade.endTurn();
 							}
 						}
 						
 						
 						gui.repaint();
 						testWin();
 
 					}
 				});
 				endTurn.setText("End Turn");
 
 
 
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
 
 
 
