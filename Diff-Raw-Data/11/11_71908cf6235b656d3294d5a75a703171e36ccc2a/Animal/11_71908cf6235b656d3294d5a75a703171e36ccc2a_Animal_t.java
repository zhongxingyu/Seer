 package game.interactables;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import game.AIState;
 import game.Enemy;
 import game.GameObject;
 import game.gameplayStates.GamePlayState;
 import game.player.Player;
 
 /**
  * Animals inherit patrol/movement behaviors from Enemy, but
  * they only have one sprite.
  *
  */
 public class Animal extends Enemy implements Interactable {
 
 	
 	private Image m_normalIm;
 		
 	public Animal(String imagePath, GamePlayState room, Player player, float x, float y,
 		int xTarget, int yTarget) throws SlickException {
 		super(room, player, x, y);
 		this.setName("cat");
 		this.setSprite(new Image(imagePath));
 		this.setLeadTo(xTarget, yTarget);
 		this.m_ai = AIState.LEAD;
 		m_normalIm = new Image(imagePath);
 		this.m_sprite =  new Animation(new Image[] {m_normalIm}, 1000, false);
 	}
 	
 	public Animal(String name, String imagePath, GamePlayState room, Player player, float x, float y,
 			int xTarget, int yTarget) throws SlickException {
 		super(room, player, x, y);
 		this.setName(name);
 		this.setSprite(new Image(imagePath));
 		this.setLeadTo(xTarget, yTarget);
 		this.m_ai = AIState.LEAD;
		m_normalIm = new Image(imagePath);
 		this.m_sprite = new Animation(new Image[] {new Image(imagePath)}, 1000, false);
 	} 
 
 	@Override
 	protected void updateSprite() {
 		
 	}
 	
 	@Override
 	//TODO - return cat to normal animation?
 	public Interactable fireAction(GamePlayState state, Player p) {
 		// TODO Auto-generated method stub
 		state.displayDialogue(new String[] {"Fortunately, you are well trained " +
 				"in cat tranquilization techniques.",
 				"MROOOOWWWWWWWWWW",
 				"*scritch*",
 				"Maybe you need to brush up on your training"
 		});
 		p.getHealth().updateHealth(-5);
 		this.setSprite(m_normalIm);
 		System.out.println("Sprites set");
 		this.m_sprite = new Animation(new Image[] {m_normalIm}, 1000, false);
 		return this;
 	}
 
 	@Override
 	public void writeAttributes(XMLStreamWriter writer) throws XMLStreamException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public Types getType() {
 		return GameObject.Types.ANIMAL;
 	}
 	@Override
 	protected void arriveEvent(){
 		try {
 			this.setSprite(new Image("assets/cat4.png"));
 			this.m_sprite = new Animation(new Image[]{ new Image("assets/cat4.png")}, 1000, false);
 		} catch (SlickException e) {
 		}
 	}
 	public int[] getSquare(){
 		int[] square = new int[2];
 		square[0] = (int)(m_x+32)/SIZE;
 		square[1] = (int)(m_y+32)/SIZE;
 		return square;
 	}
 
 }
