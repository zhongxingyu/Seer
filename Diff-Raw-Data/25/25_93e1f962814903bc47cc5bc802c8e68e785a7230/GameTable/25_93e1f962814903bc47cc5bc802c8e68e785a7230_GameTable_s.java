 package NotSoEasy;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Stack;
 
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.event.MouseInputAdapter;
 
 import State.GameState;
 import State.StateManager;
 
 import Card.Card;
 import Card.CardStack;
 
 public class GameTable extends JPanel implements Observer {
 
 	private static final long serialVersionUID = 1L;
 
 	private StateManager sm;
 	private ImageManager im;
 
 	private CardStack dragStack;
 	private CardStack sourceStack;
 
 	private Point dragStackLoc;
 	private Point[] playStackLoc;
 	private Point[] spareStackLoc;
 
 	private Dimension cardSize;
 
 	public static int STACK_SPREAD = 20;
 	public static int FACEUP_SPREAD = 30;
 	public static int FACEDOWN_SPREAD = 15;
 
 	/**
 	 * Default GameTable constructor.
 	 */
 	public GameTable() {
 		super();

 		// Initialize the state manager
 		sm = new StateManager();
 		sm.addObserver(this);
 		sm.init();
 
 		im = ImageManager.getInstance();
 		cardSize = new Dimension(im.getImage("Cover").getWidth(), im.getImage(
 				"Cover").getHeight());
 
		playStackLoc = new Point[sm.getCurrentState().getPlayStacks().length];
		spareStackLoc = new Point[sm.getCurrentState().getPlayStacks().length];

 		// Add the resize listener
 		addComponentListener(new ComponentAdapter() {
 
 			@Override
 			public void componentResized(ComponentEvent e) {
 				resized();
 			}
 		});
 
 		// Add the mouse listener
 		MyMouseInputAdapter adapter = new MyMouseInputAdapter();
 		addMouseListener(adapter);
 		addMouseMotionListener(adapter);
 
		// Set the panel minimum size
 		setMinimumSize(new Dimension(cardSize.width * 6 + STACK_SPREAD * 10,
 				cardSize.height * 4));
 	}
 
 	public StateManager getStateManager() {
 		return sm;
 	}
 
 	public void resized() {
 		BufferedImage coverImage = im.getImage("Cover");
 
 		double cardWidth = getSize().width / 11;
 		double cardHeight = coverImage.getHeight()
 				* (cardWidth / coverImage.getWidth());
 
 		cardSize = new Dimension((int) cardWidth, (int) cardHeight);
 		FACEUP_SPREAD = (cardSize.height * 30) / 100;
 
 		// Resize the playStackLocs
 		for (int i = 0; i < playStackLoc.length; i++) {
 			playStackLoc[i] = new Point(STACK_SPREAD + i
 					* (cardSize.width + STACK_SPREAD), STACK_SPREAD);
 		}
 
 		// Resize the spareStackLocs
 		for (int i = 0; i < spareStackLoc.length; i++) {
 			spareStackLoc[i] = new Point(STACK_SPREAD + playStackLoc.length
 					* (cardSize.width + STACK_SPREAD), STACK_SPREAD + i
 					* (cardSize.height + STACK_SPREAD));
 		}
 
 		repaint();
 	}
 
 	public void doMove() {
 		boolean validMove = false;
 		if (sourceStack != null) {
 			if (dragStack != null) {
 				Rectangle dragRect = getRectangle(dragStack, dragStackLoc);
 
 				CardStack[] playStacks = sm.getCurrentState().getPlayStacks();
 				for (int i = 0; i < playStacks.length; i++) {
 					Rectangle targetRect = getRectangle(playStacks[i],
 							playStackLoc[i]);
 					if (sourceStack != playStacks[i]
 							&& targetRect.intersects(dragRect)
 							&& playStacks[i].tryPush(dragStack)) {
 						validMove = true;
 						break;
 					}
 				}
 
 				if (!validMove)
 					sourceStack.push(dragStack);
 			} else {
 				Card lastCard = sourceStack.top();
 				if (lastCard != null && lastCard.isFacingDown()) {
 					validMove = true;
 					lastCard.flip();
 				}
 			}
 
 		}
 
 		if (validMove) {
 			sm.addState();
 			checkEndOfGame();
 		} else {
 			repaint();
 		}
 	}
 
 	public void checkEndOfGame() {
 		if (sm.getCurrentState().isCompleted()) {
 			JDialog endOfGameDialog = new BobonneDialog(this,
 					"Congratulations",
 					"Congratulations dear, you have succesfully completed my challenge!");
 			endOfGameDialog.setVisible(true);
 		}
 	}
 
 	@Override
 	public void update(Observable stateManager, Object arg) {
 		repaint();
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponents(g);
 
 		Dimension size = this.getSize();
 
 		// Draw the background
 		g.drawImage(im.getImage("Background"), 0, 0, size.width, size.height,
 				null);
 
 		GameState currentState = sm.getCurrentState();
 
 		// Draw the playstacks
 		for (int i = 0; i < currentState.getPlayStacks().length; i++) {
 			paintComponent(g, currentState.getPlayStacks()[i], playStackLoc[i]);
 		}
 
 		// Draw the sparestacks
 		for (int i = 0; i < currentState.getSpareStacks().length; i++) {
 			paintComponent(g, currentState.getSpareStacks()[i],
 					spareStackLoc[i]);
 		}
 
 		// Draw the dragstack
 		if (dragStack != null && !dragStack.isEmpty()) {
 			paintComponent(g, dragStack, dragStackLoc);
 		}
 	}
 
 	private void paintComponent(Graphics g, CardStack cs, Point p) {
 		if (cs.isEmpty()) {
 			g.setColor(Color.GRAY);
 			g.fillRect(p.x, p.y, cardSize.width - 1, cardSize.height - 1);
 			g.setColor(Color.BLACK);
 			g.drawRect(p.x, p.y, cardSize.width - 1, cardSize.height - 1);
 		} else {
 			Point q = p;
 			Stack<Card> cards = cs.getCards();
 			for (int i = 0; i < cards.size(); i++) {
 				Card currentCard = cards.get(i);
 				paintComponent(g, currentCard, q);
 
 				int spread = 0;
 				switch (currentCard.getFacing()) {
 				case FaceDown:
 					spread = FACEDOWN_SPREAD;
 					break;
 				case FaceUp:
 					spread = FACEUP_SPREAD;
 					break;
 				}
 
 				q = new Point(q.x, q.y + spread);
 			}
 		}
 	}
 
 	private void paintComponent(Graphics g, Card c, Point p) {
 		Graphics2D g2 = (Graphics2D) g;
 		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
 				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
 		g2.setRenderingHint(RenderingHints.KEY_RENDERING,
 				RenderingHints.VALUE_RENDER_QUALITY);
 		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 
 		BufferedImage cardImage = im.getImage(c.toString());
 		g2.drawImage(cardImage, p.x, p.y, cardSize.width, cardSize.height, null);

 	}
 
 	private Dimension getDimension(CardStack cs) {
 		int width = cardSize.width;
 		int height = cardSize.height;
 
 		Stack<Card> cards = cs.getCards();
 		for (int i = 1; i < cards.size(); i++) {
 			switch (cards.get(i - 1).getFacing()) {
 			case FaceDown:
 				height += FACEDOWN_SPREAD;
 				break;
 			case FaceUp:
 				height += FACEUP_SPREAD;
 				break;
 			}
 		}
 		return new Dimension(width, height);
 	}
 
 	private Rectangle getRectangle(CardStack cs, Point p) {
 		return new Rectangle(p, getDimension(cs));
 	}
 
 	private Rectangle getRectangle(CardStack cs, Point p, int index) {
 		Point q = new Point(p.x, p.y);
 		Stack<Card> cards = cs.getCards();
 		for (int i = 0; i < index; i++) {
 			switch (cards.get(i).getFacing()) {
 			case FaceDown:
 				q.y += FACEDOWN_SPREAD;
 				break;
 			case FaceUp:
 				q.y += FACEUP_SPREAD;
 				break;
 			}
 		}
 		return new Rectangle(q, cardSize);
 	}
 
 	class MyMouseInputAdapter extends MouseInputAdapter {
 		private Point relativeDragLoc;
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			if (e.getButton() == MouseEvent.BUTTON1) {
 				Point mouseLoc = e.getPoint();
 
 				GameState currentState = sm.getCurrentState();
 				for (int i = 0; i < currentState.getPlayStacks().length; i++) {
 					CardStack currentStack = currentState.getPlayStacks()[i];
 					Rectangle currentStackRect = getRectangle(currentStack,
 							playStackLoc[i]);
 					if (mousePressedOnStack(currentStack, currentStackRect,
 							mouseLoc))
 						return;
 				}
 
 				for (int i = 0; i < currentState.getSpareStacks().length; i++) {
 					CardStack currentStack = currentState.getSpareStacks()[i];
 					Rectangle currentStackRect = getRectangle(currentStack,
 							spareStackLoc[i]);
 					if (mousePressedOnStack(currentStack, currentStackRect,
 							mouseLoc))
 						return;
 				}
 			}
 		}
 
 		private boolean mousePressedOnStack(CardStack cs, Rectangle stackRect,
 				Point mouseLoc) {
 			if (stackRect.contains(mouseLoc)) {
 				sourceStack = cs;
 
 				int counter = 0;
 				Point p = new Point(stackRect.x, stackRect.y);
 				for (int i = cs.getSize() - 1; i >= 0; i--) {
 					counter++;
 					Rectangle cardRect = getRectangle(cs, p, i);
 
 					if (!cs.getCards().get(i).isFacingDown()
 							&& cardRect.contains(mouseLoc)) {
 						dragStackLoc = new Point(cardRect.x, cardRect.y);
 						relativeDragLoc = new Point(cardRect.x - mouseLoc.x,
 								cardRect.y - mouseLoc.y);
 						dragStack = cs.pop(counter);
 						break;
 					}
 				}
 				return true;
 			} else
 				return false;
 		}
 
 		@Override
 		public void mouseDragged(MouseEvent e) {
 			if (dragStack != null) {
 				Rectangle rect = getRectangle(dragStack, dragStackLoc);
 
 				Point mouseLoc = e.getPoint();
 				dragStackLoc = new Point(mouseLoc.x + relativeDragLoc.x,
 						mouseLoc.y + relativeDragLoc.y);
 
 				rect.add(getRectangle(dragStack, dragStackLoc));
 				repaint(rect);
 			}
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent e) {
 			if (e.getButton() == MouseEvent.BUTTON1) {
 				if (sourceStack != null) {
 					doMove();
 				}
 				sourceStack = null;
 				dragStack = null;
 			}
 		}
 	}
 }
