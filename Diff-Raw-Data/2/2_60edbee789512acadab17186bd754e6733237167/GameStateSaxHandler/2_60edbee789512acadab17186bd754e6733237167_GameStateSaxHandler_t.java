 package service;
 
 import java.util.ArrayList;
 import model.GameState;
 import model.core.InvaderType;
 import model.core.PlayerIndex;
 import model.elements.Bonus;
 import model.elements.Bunker;
 import model.elements.Invader;
 import model.elements.Player;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 
 /**
  * XML Parser that recieves our level info 
  */
 public class GameStateSaxHandler extends AbstractSaxHandler {
     
     private ArrayList<GameState> levels  = new ArrayList<>();
 
     @Override
     public void startElement(String uri, String localName, String qName, Attributes atts)
     throws SAXException {
         super.startElement(uri, localName, qName, atts);
 
         switch (qName) {
             case "level":
                 levels.add(new GameState(Integer.valueOf(atts.getValue("id"))));
                 break;
             case "player":
                 levels.get(levels.size()-1).setPlayer(PlayerIndex.One, new Player(Integer.valueOf(atts.getValue("health")),"view/sprites/player.png"));
                 levels.get(levels.size()-1).getPlayer(PlayerIndex.One).getPosition().x = Double.valueOf(atts.getValue("x"));
                 levels.get(levels.size()-1).getPlayer(PlayerIndex.One).getPosition().y = Double.valueOf(atts.getValue("y"));
                 break;
             case "bunker":
                Bunker b = new Bunker(Integer.valueOf(atts.getValue("health")));
                 b.getPosition().x = Double.valueOf(atts.getValue("x"));
                 b.getPosition().y = Double.valueOf(atts.getValue("y"));
                 levels.get(levels.size()-1).getBunkers().add(b);
                 break;
             case "invader":
                 Invader invader = new Invader(
                         InvaderType.valueOf(atts.getValue("type")), 
                         Integer.valueOf(atts.getValue("health")));
                 invader.getPosition().x = Double.valueOf(atts.getValue("x"));
                 invader.getPosition().y = Double.valueOf(atts.getValue("y"));
                 levels.get(levels.size()-1).getInvaders().add(invader);
                 break;
             case "bonus":
                 levels.get(levels.size()-1).getBonuss().add(new Bonus(
                         Integer.valueOf(atts.getValue("points")), 
                         Integer.valueOf(atts.getValue("health"))));
                 break;
         }
     }
 
     public ArrayList<GameState> getLevels() {
             return levels;
     }
     
     
 }
