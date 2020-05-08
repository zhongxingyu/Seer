 package courseProject.view.twoD;
 
 import javax.swing.*;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import courseProject.controller.Command;
 import courseProject.controller.CommandWord;
 import courseProject.controller.InputEvent2D;
 import courseProject.model.ModelChangeEvent;
 import courseProject.view.twoD.drawable.Drawable2D;
 import courseProject.view.twoD.drawable.Drawable2DArea;
 import courseProject.view.twoD.drawable.Item2D;
 import courseProject.view.twoD.drawable.Monster2D;
 import courseProject.view.twoD.drawable.Player2D;
 import courseProject.view.twoD.drawable.Room2D;
 import courseProject.view.textD.ViewText;
 
 
 /**
  * View2D is responsible for drawing all the elements to the screen
  * @author Denis Dionne
  * @version 04/11/2012
  *
  */
 public class View2D extends ViewText implements MouseListener, ActionListener{
 
 	private JFrame mainWindow;
 	//private JPanel gamePanel;
 	private Drawable2DArea drawArea;
 	private Player2D player;
 	private List<Drawable2D> drawList;
 	
 	private JButton inventoryButton;
 	private JButton characterButton;
 	private JButton helpButton;
 	private JButton quitButton;
 	private JTextArea textArea;
 	private JTextField inputField;
 	
 	private Drawable2D collidingWithObject; //used for making it when you collide with an object only one collision happens
 	
 	/**
 	 * Constructor for the 2D view, creates JPanel within a frame and initializes
 	 * the list of listeners (input and drawable)
 	 */
 	public View2D(){
 		super();
 		
 		drawList = new ArrayList<Drawable2D>();
 		
 		mainWindow = new JFrame("World of the Nameless");
 		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		mainWindow.setBounds(100, 100, 730, 455);
 		mainWindow.setResizable(false);
 		mainWindow.setLayout(new BorderLayout());
 		mainWindow.setVisible(true);
 		
 		drawArea = new Drawable2DArea();
 		drawArea.addMouseListener(this);
 		
 		JPanel buttonPanel = new JPanel(new GridLayout(1,4));
 		
 		inventoryButton = new JButton("Inventory");
 		inventoryButton.addActionListener(this);
 		
 		characterButton = new JButton("Character");
 		characterButton.addActionListener(this);
 		
 		helpButton = new JButton("Help");
 		helpButton.addActionListener(this);
 		
 		quitButton = new JButton("Quit");
 		quitButton.addActionListener(this);
 		
 		buttonPanel.add(inventoryButton);
 		buttonPanel.add(characterButton);
 		buttonPanel.add(helpButton);
 		buttonPanel.add(quitButton);
 		
 		JPanel textAreaPanel = new JPanel(new BorderLayout());
 		
 		
 		textArea = new JTextArea();
 		textArea.setEditable(false);
 		textArea.setCaretPosition(textArea.getDocument().getLength());
 		textArea.setToolTipText("What is happening to me");
 		
 		JScrollPane scrollPane = new JScrollPane(textArea);
 		scrollPane.setAutoscrolls(true);
 		
 		JPanel inputFieldPane = new JPanel(new BorderLayout());
 		
 		inputField = new JTextField();
 		inputField.addActionListener(this);
 		inputField.setToolTipText("Input Text commands here");
 		
 		JLabel inputLabel = new JLabel(" >");
 		
 		inputFieldPane.add(inputLabel, BorderLayout.WEST);
 		inputFieldPane.add(inputField, BorderLayout.CENTER);		
 		
 		textAreaPanel.add(scrollPane, BorderLayout.CENTER);
 		textAreaPanel.add(inputFieldPane, BorderLayout.SOUTH);
 		
 		JPanel gameContent = new JPanel(new GridLayout(1,2));
 		
 		gameContent.add(drawArea);
 		gameContent.add(textAreaPanel);
 		gameContent.setToolTipText("Game Visuals");
 		
 		mainWindow.add(gameContent, BorderLayout.CENTER);
 		mainWindow.add(buttonPanel, BorderLayout.SOUTH);
 	}
 	
 	/**
 	 * Update the window bounds if a room is a different size
 	 * @param rect The new rectangle bounds of the window.
 	 */
 	public void updateBounds(Rectangle rect) {
 		mainWindow.setBounds(rect);
 	}
 	
 	/**
 	 * Prints messages to text area
 	 * @param message the text to be printed
 	 */
 	@Override
 	public void displayMessage(String message) {
 		textArea.append(message);
 		textArea.append("\n");
 	}
 	
 	/**
 	 * updates all the drawn objects
 	 * @param delta time since the last update
 	 */
 	@Override
 	public void update(double delta) {
 		
 		for(Drawable2D drawable : drawList){
 			drawable.update(delta); //update the drawable
 			
 			if(!(drawable.equals(player))) { //if the drawable is not the player
 				
 				if(drawable.getClass().equals(Room2D.class)) {
 					String direction = ((Room2D)drawable).inExitBounds(player.getBounds());
 					if(direction!=null) { //if the player is in the exit bounds
 						notifyInputListeners(new InputEvent2D(new Command(CommandWord.go, direction)));
 						Point newPlayerLocation = new Point(drawable.getBounds().width/2, drawable.getBounds().height/2);
 						player.setLocation(newPlayerLocation); //set player to the middle of the room
 					}
 					continue;
 				}
 				
 				//if the drawable is not the player and collides with the player
 				if(collidingWithObject == null) {
 					if(player.collidesWith(drawable)) {
 						if(drawable.getClass().equals(Monster2D.class)) { //if player collides with a monster
 							String monsterName = ((Monster2D)drawable).getName(); //send input messages if it does collide
 							notifyInputListeners(new InputEvent2D(new Command(CommandWord.attack, monsterName)));
 							
 							collidingWithObject = drawable;
 						}
 						else if(drawable.getClass().equals(Item2D.class)) { //if player collides with an item
 							String itemName = ((Item2D)drawable).getName();
 							notifyInputListeners(new InputEvent2D(new Command(CommandWord.take, itemName)));
 	
 							collidingWithObject = drawable;
 						}
 					}
 				}
 				else {
 					if(!player.collidesWith(collidingWithObject)) { //check if you move out of the colliding objects bounds
 						collidingWithObject = null; //we are off the other object, set it to null
 					}
 				}
 			}
 			
 		}
 		
 		drawArea.repaint();
 	}
 	
 	/**
 	 * Moves the character to the coordinates specified by the mouse event
 	 * @param e the event which contains the coordinates to move to
 	 */
 	public void moveCharacter(InputEvent2D e){
 		for(Drawable2D drawable : drawList){
 			if(drawable.getClass().equals(Player2D.class)){
 				drawable.moveTo(e.getCoordinates());
 			}
 		}
 	}
 	
 	/**
 	 * handles all events
 	 * @param e the event to handle
 	 */
 	@Override
 	public void handleModelChangeEvent(ModelChangeEvent e){
 		displayMessage(e.getMessage());
 		drawList = e.getDrawable();
 		for(Drawable2D drawable : drawList) {
 			if(drawable.getClass().equals(Player2D.class)) {
 				player = (Player2D)drawable;
 			}
 		}
 		drawArea.updateDrawable(drawList);
 	}
 	/**
 	 * When you press the mouse, it generates an event that gets sent to all inputListeners, notifying them of the coordinates
 	 * of the mouse at the time it was released
 	 */
 	@Override
 	public void mousePressed(MouseEvent mouse) {
 		notifyInputListeners(new InputEvent2D(new Point(mouse.getX(),mouse.getY())));
 	}
 
 	/**
 	 * doesn't do anything, needed to implement mouseListener
 	 */
 	@Override
 	public void mouseClicked(MouseEvent mouse) {
 	}
 
 	/**
 	 * doesn't do anything, needed to implement mouseListener
 	 */
 	@Override
 	public void mouseEntered(MouseEvent mouse) {
 	}
 
 	/**
 	 * doesn't do anything, needed to implement mouseListener
 	 */
 	@Override
 	public void mouseExited(MouseEvent mouse) {
 		
 	}
 
 	/**
 	 * doesn't do anything, needed to implement mouseListener
 	 */
 	@Override
 	public void mouseReleased(MouseEvent mouse) {
 		
 	}
 	/**
 	 * closes the window on game end
 	 */
 	@Override
 	public void dispose(){
 		mainWindow.dispose();
 	}
 
 	/**
 	 * converts from actionEvent (from inventory button) to InputEvent2D and notifies
 	 * @param arg0 the ActionEvent
 	 */
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		if(event.getSource().getClass().equals(JButton.class)) {
 			JButton pressed = (JButton)(event.getSource());
 			if(pressed.equals(inventoryButton)) {
 				notifyInputListeners(new InputEvent2D(new Command(CommandWord.inventory,null)));
 			}
 			else if(pressed.equals(characterButton)){
 				notifyInputListeners(new InputEvent2D(new Command(CommandWord.character,null)));
 			}
 			else if(pressed.equals(helpButton)){
 				notifyInputListeners(new InputEvent2D(new Command(CommandWord.help,null)));
 			}
 			else if(pressed.equals(quitButton)){
 				notifyInputListeners(new InputEvent2D(new Command(CommandWord.quit,null)));
 			}
 		}
 		if(event.getSource().getClass().equals(JTextField.class)) {
 			JTextField source = (JTextField)(event.getSource());
 			
 			displayMessage(source.getText());
 			
 			String word1 = null;
 	        String word2 = null;
 
 	        Scanner tokenizer = new Scanner(source.getText());
 	        if(tokenizer.hasNext()) {
 	        	word1 = tokenizer.next();      // get first word
 	        	if(tokenizer.hasNext()) {
 	        		word2 = tokenizer.next();      // get second word
 	        		// note: we just ignore the rest of the input line.
 	        	}
 	        }
 	        tokenizer.close();
 
 	        Command toNotify = new Command(CommandWord.getCommandFromString(word1), word2);
 			notifyInputListeners(new InputEvent2D(toNotify));
 			
 			source.setText("");
 		}
 	}
 
 	
 	
 	
 }
