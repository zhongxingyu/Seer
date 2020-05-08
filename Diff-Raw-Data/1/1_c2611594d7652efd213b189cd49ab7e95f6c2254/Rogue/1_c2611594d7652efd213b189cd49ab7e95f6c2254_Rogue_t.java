 package rogue;
 
 import jade.core.World;
 import jade.ui.TiledTermPanel;
 import jade.util.datatype.ColoredChar;
 import java.awt.Color;
 import java.util.Collection;
 
 import rogue.creature.Monster;
 import rogue.creature.Player;
 import rogue.level.Level;
 import rogue.screen.Screen;
 
 public class Rogue {
 
     public static void main(String[] args) throws InterruptedException {
 
         TiledTermPanel term = TiledTermPanel.getFramedTerminal("The Final Exam - Die Anwesenheitspflicht schlägt zurück");
 
         // Der neue Spieler
         Player player = new Player(term);
         // Die neue Welt, in der sich nun der neue Spieler befindet
         World world = new Level(70, 30, player);
         // Der Drache kommt hinzu
         world.addActor(new Monster(ColoredChar.create('D', Color.yellow)));
 
         Screen.showImage(term,world,"src/rogue/screen/startscreen.txt");
 
         while(!player.expired()) {       
                 // prüft ob der Drache mich berührt.
                 Collection<Monster> monsters = world.getActorsAt(Monster.class, player.pos());
                 if (!monsters.isEmpty()) {
                         Screen.showImage(term, world, "src/rogue/screen/endscreen.txt");
                         player.expire();
                        continue;
                 }
                 // nun wird der Screen neu gebildet
                 term.clearBuffer();
                 for(int x = 0; x < world.width(); x++) {
                     for(int y = 0; y < world.height(); y++) {
                         term.bufferChar(x/* + 11*/, y, world.look(x, y));
 		    }
                 }
                 term.bufferCameras();
                 term.refreshScreen();
 
                 world.tick();
         }
         System.exit(0);
     }
 }
