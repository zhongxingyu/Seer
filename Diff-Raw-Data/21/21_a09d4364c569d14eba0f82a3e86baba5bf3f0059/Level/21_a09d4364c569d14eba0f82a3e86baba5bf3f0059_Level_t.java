 package zombiefu.level;
 
 import jade.core.Actor;
 import jade.core.World;
 import jade.gen.Generator;
 import jade.ui.TermPanel;
 import jade.util.Dice;
 import jade.util.datatype.ColoredChar;
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import zombiefu.creature.DozentZombie;
 import zombiefu.creature.Monster;
 import zombiefu.creature.Player;
 import zombiefu.creature.Zombie;
 import zombiefu.items.Item;
 import zombiefu.items.HealingItem;
 import zombiefu.items.Teleporter;
 import zombiefu.items.Waffe;
 import zombiefu.items.Waffentyp;
 import zombiefu.map.RoomBuilder;
 import zombiefu.util.DamageAnimation;
 import zombiefu.util.TargetIsNotInThisWorldException;
 
 public class Level extends World {
 
     private int freeFields;
 
     public Level(int width, int height, Generator gen) {
         super(width, height);
         gen.generate(this);
         fillWithItems();
         calculateFreeFields();
 
         drawOrder = new ArrayList<Class<? extends Actor>>();
         drawOrder.add(DamageAnimation.class);
         drawOrder.add(Player.class);
         drawOrder.add(Monster.class);
         drawOrder.add(Item.class);
         drawOrder.add(Teleporter.class);
     }
 
     public static Level levelFromFile(String file) {
         RoomBuilder builder = new RoomBuilder(file, "src/sources/CharSet.txt");
         return new Level(builder.width(), builder.height(), builder);
     }
 
     public Player getPlayer() throws TargetIsNotInThisWorldException {
         Player pl = super.getActor(Player.class);
         if (pl == null) {
             throw new TargetIsNotInThisWorldException();
         }
         return pl;
     }
 
     public void calculateFreeFields() {
         for (int x = 0; x < width(); x++) {
             for (int y = 0; y < height(); y++) {
                 freeFields += passableAt(x, y) ? 1 : 0;
             }
         }
     }
 
     @Override
     public void tick() {
         // Der Player führt IMMER die erste Aktion aus.
         try {
             getPlayer().act();
         } catch (TargetIsNotInThisWorldException ex) {
         }
         for (Class<? extends Actor> cls : super.getActOrder()) {
             for (Actor actor : getActors(cls)) {
                 if (!(actor instanceof Player) && !actor.expired()) {
                     actor.act();
                 }
             }
         }
         removeExpired();
     }
 
     public void fillWithEnemies() throws TargetIsNotInThisWorldException {
         int oldEnemies = getActors(Monster.class).size();
         int semester = getPlayer().getSemester();
         int newEnemies = (int) (semester * 0.005 * freeFields * Dice.global.nextInt(40, 60) / 50);
         // 6 normale Zombies kommen hinzu
         for (int i = oldEnemies; i <= newEnemies; i++) {
             addActor(new Zombie());
         }
     }

 
     private void fillWithItems() {
         addActor(new HealingItem(ColoredChar.create('☕', new Color(80, 0, 0)),
                 "Kaffee", 40));
         addActor(new Waffe(ColoredChar.create('⚩', new Color(90, 90, 90)),
                 "Kettensäge", 15));
         addActor(new Waffe(ColoredChar.create('S', new Color(90, 90, 90)),
                 "Flammenwerfer", 40, Waffentyp.FERNKAMPF));
     }
 
     public void refresh(TermPanel term) {
         term.clearBuffer();
         term.bufferCameras();
         term.refreshScreen();
     }
 }
