 // Memory.java
 
 package ph.sm.memory;
 
 import ch.aplu.android.Actor;
 import ch.aplu.android.GGTouch;
 import ch.aplu.android.GGTouchListener;
 import ch.aplu.android.GameGrid;
 import ch.aplu.android.L;
 import ch.aplu.android.Location;
 
 public class Memory extends GameGrid implements GGTouchListener {
 	private boolean firstCard = true;
 	private MemoryCard card1;
 	private MemoryCard card2;
 	private MemoryCard[] cards = new MemoryCard[16];
 
 	public Memory() {
 		super(4, 4, cellZoom(115));
 	}
 
 	public void main() {

 		for (int i = 0; i < 8; i++) {
 			//add it twice
 			cards[i] = new MemoryCard(i);
 			cards[15-i] = new MemoryCard(i);
 		}
 		addTouchListener(this, GGTouch.click);
 		reset();
 	}
 
 	public void reset() {
 		for (int i = 0; i < 16; i++) {
 			cards[i].show(1);
 			addActorNoRefresh(cards[i], getRandomEmptyLocation());
 		}
 		L.d("reseting");
 		refresh();
 	}
 
 	public void flipCardsBack() {
 		setTouchEnabled(false); // Disable mouse events
		delay(1000);
		card1.show(1); // Flip cards back
		card2.show(1);
		refresh();
		setTouchEnabled(true);
 	}
 
 	public boolean touchEvent(GGTouch mouse) {
 		Location location = toLocation(mouse.getX(), mouse.getY());
 		MemoryCard card = (MemoryCard) getOneActorAt(location);
 		if (card == null || card.getIdVisible() == 0) //Outside of grid or card already flipped->no action
 			return true;
 		card.show(0); // Show picture
 		refresh();
 		if (firstCard) {
 			firstCard = false;
 			card1 = card;
 		} else {	
 			firstCard = true;
 			card2 = card;
 			if (card1.getId() != card2.getId())
 			{	
 				flipCardsBack();
 			}
 		}
 		return true;
 	}
 }
 
 class MemoryCard extends Actor {
 	private int id;
 
 	public MemoryCard(int id) {
 		super("card" + id, "cardback");
 		this.id = id;
 	}
 
 	public int getId() {
 		return id;
 	}
 }
