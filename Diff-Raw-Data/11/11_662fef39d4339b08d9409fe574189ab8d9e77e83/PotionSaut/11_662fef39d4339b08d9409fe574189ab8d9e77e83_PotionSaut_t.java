 package item_joueurs;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import personnages.Robot;
 
 /**
  * 
  * @author Antoine Potion augmentant la hauteur du Saut
  */
 public class PotionSaut extends Potion {
 
 	/**
 	 * Constructeur de la classe PotionSaut
 	 * 
 	 * @param x
 	 *            : position en x de la potion
 	 * @param y
 	 *            : position en y de la potion
 	 * @param width
 	 *            : largeur de la potion
 	 * @param height
 	 *            : hauteur de la potion
 	 * @param mass
 	 *            : masse de la potion
 	 * @param player
 	 *            : passe en parametre le joueur pour savoir quand il a ramasser
 	 *            la potion ....
 	 * @param valeurRegen
 	 *            : la valeur que regénère la potion
 	 */
 	public PotionSaut(float x, float y, float width, float height, float mass,
 			Robot player, int valeur) {
 		super(x, y, width, height, mass, player, valeur);
 	}
 
 	/**
 	 * Constructeur de la classe PotionSaut avec valeur de regen à 1 par
 	 * défaut
 	 * 
 	 * @param x
 	 *            : position en x de la potion
 	 * @param y
 	 *            : position en y de la potion
 	 * @param width
 	 *            : largeur de la potion
 	 * @param height
 	 *            : hauteur de la potion
 	 * @param mass
 	 *            : masse de la potion
 	 * @param player
 	 *            : passe en parametre le joueur pour savoir quand il a ramasser
 	 *            la potion ....
 	 */
 	public PotionSaut(float x, float y, float width, float height, float mass,
 			Robot player) {
 		super(x, y, width, height, mass, player, 1);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		image = new Image("res/potionSaut.png");
 
 	}
 
 	/**
 	 * {@inheritDoc} <br/>
 	 * <b>Comportement :</b><br />
 	 * Ici on restaure la hauteur du saut du Robot
 	 */
 	@Override
 	public boolean effect(Robot player) {
 		player.modifierVitesseY(valeur);
 		player.setEstomperEffetPotion(0);
		System.out.println("llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll");
 		return true;	
 	}
 
 }
