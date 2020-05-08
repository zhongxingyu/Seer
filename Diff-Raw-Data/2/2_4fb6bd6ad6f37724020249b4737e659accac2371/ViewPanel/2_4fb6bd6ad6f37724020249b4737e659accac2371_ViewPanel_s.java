 package view;
 import java.awt.Color;
 import java.awt.Dimension;
 import controller.Controller;
 import javax.swing.*;
 import java.awt.*;
 import java.util.Random;
 
 /**
  * @author Stephen Bos
  * June, 2013
  *
  * The ViewPanel class is the primary game board view class and provides the interface through which the 
  * the game board is controlled. When instantiated, ViewPanel objects become the base level JPanel on which 
  * the rest of the game board components are arranged. ViewPanel objects are responsible for creating and storing 
  * all of the game's tiles and provide methods for a controller objects to find individual tiles and change
  * their state.
  */
 public class ViewPanel extends JPanel {
 
 	public static final int WIDTH = 800;
 	public static final int HEIGHT = 650;
 	private static final long serialVersionUID = 1L;
 	
 	public static final Color PLAYER_1_COLOR = Color.RED;
 	public static final Color PLAYER_1_BOARD_COLOR = new Color(242,111,111);
 	public static final Color PLAYER_2_COLOR = new Color(53,91,242);
 	public static final Color PLAYER_2_BOARD_COLOR = new Color(108,117,217);
 	public static final Color PLAYER_3_COLOR = new Color(242,228,29);
 	public static final Color PLAYER_3_BOARD_COLOR = new Color(235,221,136);
 	public static final Color PLAYER_4_COLOR = new Color(33,194,52);
 	public static final Color PLAYER_4_BOARD_COLOR = new Color(120,222,121);
 	public static final Color BLANK_COLOR = Color.WHITE;
 	public static final Color LOOP_COLOR = Color.GRAY;
 	
 	/*===================================
  	FIELDS
  	===================================*/
 	
 	private Controller controller;
 	private FieldTile[] boardLoop;
 	private FieldTile[][] homes;
 	private FieldTile[][] goals;
 	private DieComponent die;
 	private Animator animator;
 	
 	/*===================================
 	 CONSTRUCTORS
 	 ===================================*/
 	
 	public ViewPanel(Controller controller){
 		this.setBackground(Color.BLACK);
 		this.setPreferredSize(new Dimension(ViewPanel.WIDTH,ViewPanel.HEIGHT));
 		this.setLayout(new GridBagLayout());
 		
 		this.controller = controller;
 		this.boardLoop = new FieldTile[40];
 		this.homes = new FieldTile[4][4];
 		this.goals = new FieldTile[4][4];
 		this.animator = new Animator();
 		
 		this.die = new DieComponent(6);
 		this.die.toggleIsActive();
 		this.die.addActionListener(controller.getDiceListener());
 
 		initializeTiles();
 		layoutTileStrips();
 	}
 	
 	/**
 	 * Initializes the home, goal, and board tile arrays.
 	 */
 	private void initializeTiles(){
 		// Initialize loop tiles
 		for(int i=0;i<boardLoop.length;i++){
 			FieldTile newTile = new FieldTile(ViewPanel.BLANK_COLOR);
 			newTile.addActionListener(controller.getFieldTileListener());
 			newTile.setId("B|"+i);
 			this.boardLoop[i] = newTile;
 		}
 		
 		// Initialize home tiles
 		for(int i=0;i<4;i++){
 			for(int j=0;j<4;j++){
 				FieldTile newTile = new FieldTile(ViewPanel.BLANK_COLOR);
 				newTile.addActionListener(controller.getFieldTileListener());
 				newTile.setColor(getColorForPlayer(i+1));
 				newTile.setId("H|"+(i+1)+"|"+j);
 				this.homes[i][j]=newTile;
 			}
 		}
 		
 		// Initialize goal tiles
 		for(int i=0;i<4;i++){
 			for(int j=0;j<4;j++){
 				FieldTile newTile = new FieldTile(ViewPanel.BLANK_COLOR);
 				newTile.addActionListener(controller.getFieldTileListener());
 				newTile.setId("G|"+(i+1)+"|"+j);
 				this.goals[i][j]=newTile;
 			}
 		}
 	}
 	
 	/**
 	 * This method uses the TileStripFactor to get the necessary tile strips and lays these tile strips out in their proper positions.
 	 */
 	private void layoutTileStrips(){
 		
 		// Center board components.
 		
 		JPanel p1Goal = TileStripFactory.getGoalForBottom(this.goals[0], ViewPanel.PLAYER_1_BOARD_COLOR);
 		JPanel p2Goal = TileStripFactory.getGoalForLeft(this.goals[1], ViewPanel.PLAYER_2_BOARD_COLOR);
 		JPanel p3Goal = TileStripFactory.getGoalForTop(this.goals[2], ViewPanel.PLAYER_3_BOARD_COLOR);
 		JPanel p4Goal = TileStripFactory.getGoalForRight(this.goals[3], ViewPanel.PLAYER_4_BOARD_COLOR);
 		
 		JPanel centerSquare = new JPanel(new GridBagLayout());
 		centerSquare.setBackground(Color.BLACK);
 		centerSquare.setPreferredSize(new Dimension(FieldTile.WIDTH+10,FieldTile.HEIGHT+10));
 		centerSquare.add(this.die);
 		
 		JPanel center = new JPanel(new GridBagLayout());
 		center.setBackground(Color.BLACK);
 		GridBagConstraints gb1 = new GridBagConstraints();
 		gb1.gridx=0;
 		gb1.gridy=0;
 		center.add(Box.createGlue(),gb1);
 		gb1.gridx=1;
 		gb1.gridy=0;
 		center.add(p3Goal,gb1);
 		gb1.gridx=2;
 		gb1.gridy=0;
 		center.add(Box.createGlue(),gb1);
 		gb1.gridx=0;
 		gb1.gridy=1;
 		center.add(p2Goal,gb1);
 		gb1.gridx=1;
 		gb1.gridy=1;
 		center.add(centerSquare,gb1);
 		gb1.gridx=2;
 		gb1.gridy=1;
 		center.add(p4Goal,gb1);
 		gb1.gridx=0;
 		gb1.gridy=2;
 		center.add(Box.createGlue(),gb1);
 		gb1.gridx=1;
 		gb1.gridy=2;
 		center.add(p1Goal,gb1);
 		gb1.gridx=2;
 		gb1.gridy=2;
 		center.add(Box.createGlue(),gb1);
 		
 		// Middle board components.
 		JPanel leftStrip = TileStripFactory.getLeftBoardStrip(this.boardLoop, ViewPanel.LOOP_COLOR);
 		JPanel topStrip = TileStripFactory.getTopBoardStrip(this.boardLoop, ViewPanel.LOOP_COLOR);
 		JPanel rightStrip = TileStripFactory.getRightBoardStrip(this.boardLoop, ViewPanel.LOOP_COLOR);
 		JPanel bottomStrip = TileStripFactory.getBottomBoardStrip(this.boardLoop, ViewPanel.LOOP_COLOR);
 		
 		JPanel middlePanel = new JPanel(new GridBagLayout());
 		middlePanel.setBackground(Color.BLACK);
 		GridBagConstraints gb2 = new GridBagConstraints();
 		
 		gb2.gridx = 0;
 		gb2.gridy = 0;
 		gb2.gridheight=3;
 		middlePanel.add(leftStrip, gb2);
 		gb2.gridx = 1;
 		gb2.gridy = 0;
 		gb2.gridheight = 1;
 		middlePanel.add(topStrip, gb2);
 		gb2.gridx = 2;
 		gb2.gridy = 0;
 		gb2.gridheight = 3;
 		middlePanel.add(rightStrip, gb2);
 		gb2.gridx = 1;
 		gb2.gridy = 1;
 		gb2.gridheight = 1;
 		middlePanel.add(center,gb2);
 		gb2.gridx = 1;
 		gb2.gridy = 2;
 		middlePanel.add(bottomStrip, gb2);
 		
 		// Outer board components
 		JPanel p1Home = TileStripFactory.getHomeForBottom(this.homes[0], ViewPanel.PLAYER_1_BOARD_COLOR);
 		JPanel p2Home = TileStripFactory.getHomeForLeft(this.homes[1], ViewPanel.PLAYER_2_BOARD_COLOR);
 		JPanel p3Home = TileStripFactory.getHomeForTop(this.homes[2], ViewPanel.PLAYER_3_BOARD_COLOR);
 		JPanel p4Home = TileStripFactory.getHomeForRight(this.homes[3], ViewPanel.PLAYER_4_BOARD_COLOR);
 		
 		JPanel outerPanel = new JPanel(new GridBagLayout());
 		outerPanel.setBackground(Color.BLACK);
 		GridBagConstraints gb3 = new GridBagConstraints();
 		
 		gb3.gridx = 0;
 		gb3.gridy = 0;
 		gb3.gridwidth = 2;
 		outerPanel.add(p3Home, gb3);
 		gb3.gridx = 0;
 		gb3.gridy = 1;
 		gb3.gridwidth = 1;
 		gb3.gridheight = 2;
 		outerPanel.add(p2Home, gb3);
 		
 		gb3.gridx = 1;
 		gb3.gridy = 1;
 		gb3.gridwidth = 1;
 		gb3.gridheight = 1;
 		outerPanel.add(middlePanel, gb3);
 		
 		gb3.gridx = 2;
 		gb3.gridy = 0;
 		gb3.gridheight = 2;
 		gb3.gridwidth = 1;
 		outerPanel.add(p4Home, gb3);
 		
 		gb3.gridx = 1;
 		gb3.gridy = 2;
 		gb3.gridwidth = 2;
 		gb3.gridheight = 1;
 		outerPanel.add(p1Home, gb3);
 			
 		this.add(outerPanel);
 	}
 	
 	/*===================================
 	 GETTERS & SETTERS
 	 ===================================*/
 	
 	/**
 	 * @param player - an integer between 1 and 4. Any other integer returns the default blank color.
 	 * @return Returns the color of that specified player.
 	 */
 	private Color getColorForPlayer(int player){
 		switch(player){
 		
 		case 1:
 			return ViewPanel.PLAYER_1_COLOR;
 		case 2:
 			return ViewPanel.PLAYER_2_COLOR;
 		case 3:
 			return ViewPanel.PLAYER_3_COLOR;
 		case 4:
 			return ViewPanel.PLAYER_4_COLOR;
 		default:
 			return ViewPanel.BLANK_COLOR;
 		}
 	}
 	
 	/**
 	 * @param position - The position of the tile on the main board. Can be an integer between 0 and 39.
 	 * @return Returns the FieldTile at the passed position.
 	 */
 	public FieldTile getBoardTileAt(int position){
 		if(position>39 || position<0){
 			return null;
 		}
 		else{
 			return this.boardLoop[position];
 		}
 	}
 	
 	/**
 	 * @param player - The player number of the player who owns this home. Can be an integer between 1 and 4.
 	 * @param position - The position of the tile within the home. Can be an integer between 0 and 3
 	 * @return Returns the FieldTile at the passed position in the home belonging to the passed player.
 	 */
 	public FieldTile getHomeTileForPlayerAt(int player, int position){
 		if(position>3 || position<0 || player<1 || player>4){
 			return null;
 		}
 		else{
 			return this.homes[player-1][position];
 		}
 	}
 	
 	/**
 	 * @param player - The player number of the player who owns this goal. Can be an integer between 1 and 4.
 	 * @param position - The position of the tile within the goal. Can be an integer between 0 and 3.
 	 * @return Returns the FieldTile at the passed position in the goal belonging to the passed player.
 	 */
 	public FieldTile getGoalTileForPlayerAt(int player, int position){	
 		if(position>3 || position<0 || player<1 || player>4){
 			return null;
 		}
 		else{
 			return this.goals[player-1][position];
 		}
 	}
 	
 	/**
 	 * @param player - The player number of the player to set at this tile. Can be an integer between 1 and 4 or 0 to clear the tile.
 	 * @param position - The position of the tile on the main board that the player is being assigned to.
 	 */
 	public void setColorAtBoardTile(int player,int position){
 		FieldTile tile = getBoardTileAt(position);
 		if(tile!=null){
 			tile.setColor(getColorForPlayer(player));
 		}	
 	}
 	
 	/**
 	 * @param player - The player number of the player who owns this home.Can be an integer between 1 and 4 or 0 to clear the tile.
 	 * @param position - The position of the tile in the player's home.
 	 */
 	public void setPlayerAtHomeTile(int player, int position){
 		FieldTile tile = getHomeTileForPlayerAt(player,position);
 		if(tile!=null){
 			tile.setColor(getColorForPlayer(player));
 		}
 	}
 	
 	/**
 	 * @param player - The player number of the player who owns this goal.Can be an integer between 1 and 4 or 0 to clear the tile.
 	 * @param position - The position of the title in the player's goal.
 	 */
 	public void setPlayerAtGoalTile(int player, int position){
 		FieldTile tile = getGoalTileForPlayerAt(player,position);
 		if(tile!=null){
 			tile.setColor(getColorForPlayer(player));
 		}
 	}
 	
 	/**
 	 * Sets the number displayed on the die.
 	 * @param roll - an integer between 1 and 6 representing the die roll.
 	 */
 	public void setDieRoll(int roll){
 		this.animator.animateDieRoll(roll);
 	}
 	
 	public Animator getAnimator(){
 		return this.animator;
 	}
 	
 	/*===================================
 	 OTHER METHODS
 	 ===================================*/
 	
 	/**
 	 * This method resets the board to a new game state, with all the pawns in their home fields.
 	 */
 	public void resetBoard(){
 		for(int i=0;i<boardLoop.length;i++){
 			boardLoop[i].setColor(ViewPanel.BLANK_COLOR);
 		}
 		for(int i=0;i<4;i++){
 			for(int j=0;j<4;j++){
 				goals[i][j].setColor(ViewPanel.BLANK_COLOR);
 				homes[i][j].setColor(getColorForPlayer(i+1));
 			}
 		}
 	}
 	
 	/*===================================
 	 ANIMATOR
 	 ===================================*/
 	
 	/**
 	 * A nested helper class responsible for animating the ViewPanel
 	 */
 	private class Animator{
 		
 		/**
 		 * Displays random die numbers over 3 seconds before arriving at the correct roll.
 		 * @param toNumber - the number the die should display when it finishes rolling.
 		 */
 		public void animateDieRoll(int toNumber){
 			Random r = new Random();
 			
 			long startTime = System.currentTimeMillis();
 			long currentTime = startTime;
			int divisor = 400;
 			
 			while(currentTime - startTime < 3000L){	
 				
 				if(currentTime%divisor<10){
 					ViewPanel.this.die.setDieRoll(r.nextInt(6)+1);
 				}		
 				currentTime = System.currentTimeMillis();
 			}
 			ViewPanel.this.die.setDieRoll(toNumber);
 		}
 	}
 
 }
