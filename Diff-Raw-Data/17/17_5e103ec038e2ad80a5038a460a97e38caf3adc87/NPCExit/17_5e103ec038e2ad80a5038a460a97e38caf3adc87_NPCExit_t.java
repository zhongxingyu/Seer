 package npcs;
 
 import fap_java.Cell;
 import fap_java.Game;
 import fap_java.NPC;
 
 import gui.Fapplication;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 
 import java.util.ArrayList;
 
 import npcs.actions.AStartGame;
 import npcs.actions.Action;
 
 public class NPCExit extends NPC {
     public NPCExit(Cell cell, Game game) {
         super(cell, true, false, null, game, 0,0,null);
        //actions = new ArrayList<Action>();
         //Action ac = new AStartGame(new Game(0));
        //actions.add(new AStartGame(Fapplication.getWorldMap()));
     }
     public void paintComponent(Graphics g){}
    
    public void execute(){
        new AStartGame(Fapplication.getWorldMap()).execute();
    }
 }
