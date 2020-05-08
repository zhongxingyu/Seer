 package cha.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.util.ArrayList;
 import javax.swing.JPanel;
 import javax.swing.border.BevelBorder;
 import cha.domain.Board;
 import cha.domain.Categories.Category;
 import cha.domain.Piece;
 import cha.domain.Tile;
 import cha.event.Event;
 import cha.event.EventBus;
 import cha.event.IEventHandler;
 
 @SuppressWarnings("serial")
 public class TileContainerPanel extends JPanel implements IEventHandler {
 
 	private static final int MAX_AMOUNT_TILES = 44;
 	private static TilePanel[] tilePanels = new TilePanel[MAX_AMOUNT_TILES];
 	private JPanel northPanel = new JPanel();
 	private JPanel eastPanel = new JPanel();
 	private JPanel southPanel = new JPanel();
 	private JPanel westPanel = new JPanel();
 
 	private ArrayList<PiecePanel> piecePanels = new ArrayList<PiecePanel>();
 
 	private static int temporaryBet;
 	private static boolean betable = false;
 
 	// Constructor & initilization
 
 	public TileContainerPanel() {
 		EventBus.getInstance().register(this);
 		init();
 	}
 
 	private void init() {
 
 		setLayout(new GridBagLayout());
 
 		// Set layouts of the tile panels.
 
 		eastPanel.setPreferredSize(new Dimension(50, 0));
 		FlowLayout flowLayout_1 = (FlowLayout) eastPanel.getLayout();
 		flowLayout_1.setAlignment(FlowLayout.LEFT);
 		flowLayout_1.setVgap(0);
 		flowLayout_1.setHgap(0);
 		FlowLayout flowLayout_2 = (FlowLayout) southPanel.getLayout();
 		flowLayout_2.setAlignment(FlowLayout.RIGHT);
 		flowLayout_2.setVgap(0);
 		flowLayout_2.setHgap(0);
 		westPanel.setPreferredSize(new Dimension(50, 0));
 		FlowLayout flowLayout_3 = (FlowLayout) westPanel.getLayout();
 		flowLayout_3.setAlignment(FlowLayout.RIGHT);
 		flowLayout_3.setVgap(0);
 		flowLayout_3.setHgap(0);
 		FlowLayout flowLayout_4 = (FlowLayout) northPanel.getLayout();
 		flowLayout_4.setAlignment(FlowLayout.LEFT);
 		flowLayout_4.setVgap(0);
 		flowLayout_4.setHgap(0);
 
 		GridBagConstraints c = new GridBagConstraints();
 		c.weightx = c.weighty = 0;
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.anchor = GridBagConstraints.NORTH;
 		this.add(northPanel, c);
 		c.weighty = 1;
 		c.fill = GridBagConstraints.VERTICAL;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		c.anchor = GridBagConstraints.WEST;
 		this.add(westPanel, c);
 		c.gridx = 2;
 		c.anchor = GridBagConstraints.EAST;
 		this.add(eastPanel, c);
 		c.weighty = 0;
 		c.gridx = 0;
 		c.gridy = 2;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridwidth = GridBagConstraints.REMAINDER;
 		c.anchor = GridBagConstraints.SOUTH;
 		this.add(southPanel, c);
 
 	}
 
 	// Create new game, update all GUI to match current game
 
 	public void newGame(ArrayList<Tile> tiles) {
 
 		setTiles(tiles);
 
 		int numberOfPieces = Board.getInstance().getNumberOfPieces();
 
 		piecePanels.clear();
 		for (int i = 0; i < numberOfPieces; i++) {
 			Piece piece = Board.getInstance().getPiece(i);
 			piecePanels.add(new PiecePanel(piece, piece.getTeam().getColor()));
 		}
 
 		for (PiecePanel piece : piecePanels) {
 			tilePanels[0].addPiecePanel(piece);
 		}
 
 		Board.getInstance().setActivePiece(0);
 		// currentPiece = 0;
 
 	}
 
 	// Methods
 
 	private void setTiles(ArrayList<Tile> tiles) {
 		northPanel.removeAll();
 		eastPanel.removeAll();
 		southPanel.removeAll();
 		westPanel.removeAll();
 
 		TilePanel start = new StartTilePanel(tiles.get(0).getCategory());
 		tilePanels[0] = start;
 		northPanel.add(start);
 
 		TilePanel goal = new GoalTilePanel();
 		tilePanels[43] = goal;
 		westPanel.add(goal);
 
 		for (int i = 1; i < 14; i++) {
 			TilePanel p = createTile(tiles.get(i), i);
 			tilePanels[i] = p;
 			northPanel.add(p);
 		}
 		for (int i = 14; i < 22; i++) {
 			TilePanel p = createTile(tiles.get(i), i);
 			tilePanels[i] = p;
 			eastPanel.add(p);
 		}
 		for (int i = 35; i > 21; i--) {
 			TilePanel p = createTile(tiles.get(i), i);
 			tilePanels[i] = p;
 			southPanel.add(p);
 		}
 
 		for (int i = 42; i > 35; i--) {
 			TilePanel p = createTile(tiles.get(i), i);
 			tilePanels[i] = p;
 			westPanel.add(p);
 		}
 		repaint();
 	}
 
 	private TilePanel createTile(Tile t, int position) {
 		TilePanel tile;
 		Category c = t.getCategory();
 
 		if (c == Category.BACKWARDS) {
 			tile = new NormalTilePanel(Color.RED, position);
 		} else if (c == Category.BODYTOBODY) {
 			tile = new NormalTilePanel(Color.YELLOW, position);
 		} else if (c == Category.SAMECLASS) {
 			tile = new NormalTilePanel(Color.BLUE, position);
 		} else {
 			tile = new NormalTilePanel(Color.GREEN, position);
 		}
 		if (t.isChallenge()) {
 			tile = new NormalTilePanel(Color.WHITE, position);
 		}
 		return tile;
 	}
 	//La in det i PlayerPanel d�r den tar hand om att byta tur redan...
 	//	private void nextPlayer() {
 	//		Board.getInstance().changeActivePiece();
 	//	}
 
 	public static TilePanel[] getTilePanels() {
 		return tilePanels;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void action(Event e, Object o, Object p) {
 		if (e == Event.CreateBoard) {
 			ArrayList<Tile> tiles = (ArrayList<Tile>) o;
 
 			newGame(tiles);
 		} else if (e == Event.ShowBet) {
 				showBet();
 				setBetable(false);
 		} else if (e == Event.MakeBet) {
 			if (Board
 					.getInstance().getTile(Board.getInstance().getActivePiece().getPosition()).isChallenge()) {
 				setBetable(false);} 
 
 			else {
 				int pos = Board.getInstance().getActivePiece().getPosition();
 				for (int i = pos + 1; i < pos + 8; i++) {
 					if (i > 43) {
 						return;
 					}
 					tilePanels[i].betable();
 					repaint();
 				}
 				showBet();
 				setBetable(false);
 				temporaryBet = (Integer) o;
 				TileContainerPanel.getTilePanels()[temporaryBet
 						+ Board.getInstance().getActivePiece().getPosition()]
 						.setBorder(new BevelBorder(BevelBorder.LOWERED));
 				repaint();
 			}
 		}		
 		else if (e == Event.OldPosition) {
 			int pos = (Integer) o;
 			int index = (Integer) p;
 			tilePanels[pos].removePiece(piecePanels.get(index));
 		} else if (e == Event.NewPosition) {
 			int pos = (Integer) o;
 			int index = (Integer) p;
 			tilePanels[pos].addPiecePanel(piecePanels.get(index));
 			tilePanels[pos].repaint();
 			//nextPlayer();
 		}
 	}
 
 	public static int getTemporaryBet() {
 		return temporaryBet;
 	}
 
 	public static boolean getBetable() {
 		return betable;
 	}
 
 	public static void setBetable(boolean i) {
 		betable = i;
 	}
 
 	private void showBet() {
 		int pos = Board.getInstance().getActivePiece().getPosition();
 		
 		for(TilePanel t : tilePanels){
 			t.setBorder(new BevelBorder(BevelBorder.LOWERED));
 		}
 
 		for (int i = pos + 1; i < pos + 8; i++) {
 			if (i > 43) {
 				return;
 			}
 			tilePanels[i].betable();
 			repaint();
 		}
 	}
 }
