 /*
  * MainGame.java
  *
  * Created on May 20, 2008, 6:34 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package slickbrickles.ui;
 
 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.util.ArrayList;
 import net.phys2d.raw.Body;
 import net.phys2d.raw.BodyList;
 import net.phys2d.raw.StaticBody;
 import net.phys2d.raw.World;
 import net.phys2d.raw.collide.ColliderFactory;
 import net.phys2d.raw.shapes.Line;
 import net.phys2d.raw.strategies.QuadSpaceStrategy;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.BigImage;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.lwjgl.LWJGLException;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import slickbrickles.Button;
 import slickbrickles.Addable;
 import slickbrickles.bodies.Brick;
 import slickbrickles.bodies.Ball;
 import slickbrickles.bodies.Pad;
 import slickbrickles.bodies.SplitBrick;
 import slickbrickles.bodies.UnbreakableBrick;
 import slickbrickles.util.LevelMaker;
 import static slickbrickles.util.Utils.*;
 /**
  *
  * @author henry
  */
 public class MainGame extends BasicGameState{
     
     /** Creates a new instance of MainGame */
     private org.newdawn.slick.Color myBackground;
    private StateHandler game;
     private static Pad thePad;
     private World world;
     private Font menuFont;
     private int ID = 22222;
     private LevelMaker maker;
    public MainGame(StateHandler sbg) {
         super();
         game = sbg;
         world = new World(v(0,0), 10, new QuadSpaceStrategy(20,5));
 	world.enableRestingBodyDetection(.1f, .1f, .1f);
         world.setDamping(1);
         maker = new LevelMaker(world);
         maker.createLevel1();
         myBackground = new org.newdawn.slick.Color(100,100,100);
     }
     
     public void remove(Body b)
     {
         world.remove(b);
     }
     public void add(Body b)
     {
         world.add(b);
     }
     public void restart()
     {
         world.clear();
         maker.createLevel1();
     }
     public void init(GameContainer gameContainer, StateBasedGame stateBasedGame) throws SlickException{
         menuFont = new TrueTypeFont(new java.awt.Font(null, java.awt.Font.BOLD, 20), true);
     }
     public void enter(GameContainer container, StateBasedGame game) throws SlickException {
         game.getContainer().getInput().addListener(this);
     }
     public void leave(GameContainer container, StateBasedGame game) throws SlickException {
         game.getContainer().getInput().removeListener(this);
     }
     public void render(GameContainer gameContainer, StateBasedGame stateBasedGame, Graphics g) throws SlickException {stateBasedGame.getContainer().getInput().addListener(this);
        preWorld(gameContainer, g);
        g.setBackground(myBackground);
        BodyList bodies = world.getBodies();
        for(int i = 0; i<bodies.size(); i++)
        {
            if(bodies.get(i) instanceof Addable)
             ((Addable)bodies.get(i)).render(g);
        }
        postWorld(gameContainer, g);
     }
     public void update(GameContainer gameContainer, StateBasedGame stateBasedGame, int i) throws SlickException {
         world.step();
         BodyList bodies = world.getBodies();
        for(int j = 0; j<bodies.size(); j++)
        {
            if(bodies.get(j) instanceof Addable)
             ((Addable)bodies.get(j)).update();
        }
     }
 
     public void mouseMoved(int oldx, int oldy, int newx, int newy) {
     }
 
     public void mouseClicked(int button, int x, int y, int clickCount) {
     }
 
     public void keyPressed(int key, char c)
     {
        if(key == Input.KEY_Q)
            System.exit(0);
        else if(key == Input.KEY_R)
            restart();
        else if(key == Input.KEY_P)
            game.getContainer().pause();
        else if(key == Input.KEY_U)
            game.getContainer().resume();
        else if(key == Input.KEY_M)
            game.enterState(11111);
        else
        {
            BodyList bodies = world.getBodies();
            for(int i = 0; i<bodies.size(); i++)
            {
                if(bodies.get(i) instanceof Addable)
                 ((Addable)bodies.get(i)).keyPressed(key,c);
            }
        }
     }
     public void keyReleased(int key, char c)
     {
        BodyList bodies = world.getBodies();
        for(int i = 0; i<bodies.size(); i++)
        {
            if(bodies.get(i) instanceof Addable)
             ((Addable)bodies.get(i)).keyReleased(key,c);
        }
     }
 
     private void preWorld(GameContainer gameContainer, Graphics g) {
         
     }
 
     private void postWorld(GameContainer gameContainer, Graphics g) {
         g.setColor(Color.red);
         drawMenuItem("Q - Quit", menuFont, g, 1);
         drawMenuItem("R - Restart", menuFont, g, 2);
         drawMenuItem("P - Pause", menuFont, g, 3);
         drawMenuItem("U - Unpause (resume)", menuFont, g, 4);
         drawMenuItem("M - Main Menu", menuFont, g, 5);
     }
 
 
     public int getID() {
         return ID;
     }
 }
