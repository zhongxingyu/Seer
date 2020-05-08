 package game.interactables;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 import game.Collectable;
 import game.GameObject;
 import game.Person;
 import game.StaticObject;
 import game.GameObject.Types;
 import game.collectables.HairVial;
 import game.collectables.Sledgehammer;
 import game.gameplayStates.GamePlayState;
 import game.player.Player;
 
 public class TraderCounter extends StaticObject implements Interactable{
 	
 	private int m_tradeState = 0;
 	
 	public TraderCounter(String name, int xLoc, int yLoc) throws SlickException {
 		super(name, xLoc, yLoc, "assets/colors/clear.png");
 	}
 
 	@Override
 	public Interactable fireAction(GamePlayState state, Player p) {
 		//System.out.println("interacting with trader counter");
 		if (m_tradeState == 0) {
 			if (p.getUsing() instanceof HairVial) {
 				state.displayDialogue(new String[] {"\"Ah! Looks like you have just what I need.\"",
 					"\"Here's the wrench that I said I'd barter off. Don't get too crazy with it.\""});
 				p.getInventory().removeItem(Types.HAIR_VIAL);
 				try {
 					p.addToInventory(new Wrench(-1, -1));
 				} catch (SlickException e) {
 					e.printStackTrace();
 				}
 				m_tradeState++;
 			}
 			else {
 				state.displayDialogue(new String[] {"\"Welcome to my barter shop. We trade items for items!\"",
 					"\"I'm currently looking for some hair-remover. Would you happen to have that on you?\"" ,
 					"\"I got this new wrench the other day and would glady trade you for it.\""});	
 			}
 		} 
 		else if (m_tradeState == 1) {
 			if (p.getInventory().contains(Types.CHICKEN_WING)) {
 				state.displayDialogue(new String[] {"\"Ah! Looks like you have just what I need.\"",
 						"\"Here, take this sledgehammer. It might be useful someday.\""});
 				p.getInventory().removeItem(Types.CHICKEN_WING);
 				p.addToInventory(new Sledgehammer());
 				m_tradeState++;
 			}
 			else {
 				state.displayDialogue(new String[] {"\"I'm looking for some chicken... " +
 						"Would you happen to want to trade that?\""});
 			}
 		}
 		
 		else {
			state.displayDialogue(new String[] {"\"Sorry, but I don't have anything to trade with you.\""});
 		}
 		
 		return null;
 	}
 
 
 	@Override
 	public void writeToXML(XMLStreamWriter writer) throws XMLStreamException {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
