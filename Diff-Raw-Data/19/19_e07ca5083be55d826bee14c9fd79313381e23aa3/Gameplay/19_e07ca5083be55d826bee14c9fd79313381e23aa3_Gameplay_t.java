 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package projetx;
 
 /**
  *
  * @author anisbenyoub
  */
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 import org.newdawn.slick.*;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.gui.TextField;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class Gameplay extends BasicGameState {
 
     Image background;
     static double partyDuration = 200 ;
     double actualTime;
     int stateID = -1;
     
     Sound Music;
     
     List<Player> players;
     List<Obstacle> obstacles;
     List<String> ficObs;
     List<Rectangle> power;
     List<Rectangle> fillThePower;
     
     int elapsedTimeSinceLastNewFieldG;
     int elapsedTimeSinceLastNewFieldD;
     int elapsedTimeSinceLastNewFieldM;
     int randApparitionD;
     int randApparitionG;
 
     Physics physics = new Physics(0.0022);
     int randApparitionM;
     boolean newGame = true;
 
     Gameplay(int stateID) {
         this.stateID = stateID;
     }
 
     @Override
     public int getID() {
         return stateID;
     }
 
     public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
         players = new ArrayList<Player>(4);
         obstacles = new ArrayList<Obstacle>();
         ficObs = new ArrayList<String>();
         power = new ArrayList<Rectangle>();
         fillThePower = new ArrayList<Rectangle>();
 
         initField();
         
         actualTime=1;
 
         background= new Image("./ressources/sprites/Fond/Fond.jpg");
         Music = new Sound("ressources/audio/musicGame.wav");
         Music.play();
     }
 
     public void render(GameContainer gc, StateBasedGame sbg, Graphics gr) throws SlickException 
     {
         int Decalage=(int)(( 650-background.getHeight())*((partyDuration -actualTime)/(double)partyDuration));
 
         
         //System.out.println(Decalage);
         //System.out.println(actualTime);
         background.draw(0,Decalage);
         
         for (Obstacle o : obstacles) {
             o.getImage().draw((int) o.getCoords().getX(), (int) o.getCoords().getY());
         }
 
         for (Player o : players) {
             o.getImage().draw((int) o.getCoords().getX(), (int) o.getCoords().getY());
         }
         
         for (Rectangle r : power) {
             gr.draw(r);
         }
         
         for (Rectangle r : fillThePower) {
             gr.draw(r);
             gr.fill(r);
         }
         
         showInformation(gr);
 
     }
 
     public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
         if (newGame) {
             newGame = false;
             initPlayers();
         }
         managePowerBar(delta);
         manageField(delta);
         manageInput(gc, sbg, delta);
         managePhysics();
         manageDeath();
         manageGravityBoom() ;
         actualTime+=(double)delta/1000.0;
         
         
         // Fin de partie
         if(actualTime>partyDuration)
         {
             Game.playerScores=players;
             sbg.enterState(2);
             actualTime=0;
             newGame = true;
             initField();
         }
 
         physics.updateMovable(players);
         physics.updatePlatforms(obstacles);
         physics.computePhysics(delta);
 
     }
     
     private void managePowerBar(int elapsedTime) {
         power.clear();
         fillThePower.clear();
         
         for (Player p:players) {
             power.add(new Rectangle((float)(p.getCoords().getX()-10), (float)(p.getCoords().getY()-30), 82.f, 8.f));
             fillThePower.add(new Rectangle((float)(p.getCoords().getX()-9), (float)(p.getCoords().getY()-29), p.GetTailleBarre(), 6.f));
             if(p.GetTimeSinceLastPower()<5000) {
                 p.SetTimeSinceLastPower(p.GetTimeSinceLastPower()+elapsedTime);
             }
         }
     }
 
     private void manageField(int elapsedTime) throws SlickException {
         Point2D coords;
         int tailleEcranY = 700;
 
         double deplacement = 0.2f * elapsedTime;
 
 
         for (int i = 0; i < obstacles.size(); i++) {
             Obstacle o = obstacles.get(i);
             coords = o.getCoords();
             if (coords.getY() > tailleEcranY) {
                 obstacles.remove(o);
                 i--;
             } else {
                 coords.setLocation(coords.getX(), coords.getY() + deplacement);
                 o.setCoords(coords);
             }
         }
 
         for (Player p : players) {
             coords = p.getCoords();
             coords.setLocation(coords.getX(), coords.getY() + deplacement);
             p.setCoords(coords);
         }
 
 //        int lower = 500;
 //        int higher = 2500;
         int lower = 200;
         int higher = 1500;
 
         if (elapsedTimeSinceLastNewFieldG > randApparitionG) {
             Obstacle o = new Obstacle(ficObs.get((int) (Math.random() * 2)));
             obstacles.add(o);
             int randX = (int) (Math.random() * (250 + 350 - 120 + 1 - 250)) + 250;
             o.setCoords(new Point2D.Double(randX, -70));
             elapsedTimeSinceLastNewFieldG = 0;
 
             randApparitionG = (int) (Math.random() * (higher + 1 - lower)) + lower;
 
         }
         if (elapsedTimeSinceLastNewFieldD > randApparitionD) {
             Obstacle o = new Obstacle(ficObs.get((int) (Math.random() * 2)));
             obstacles.add(o);
             int randX = (int) (Math.random() * (250 + 350 - 120 + 1 - 250)) + 250;
             o.setCoords(new Point2D.Double(randX + 193, -70));
             elapsedTimeSinceLastNewFieldD = 0;
 
             randApparitionD = (int) (Math.random() * (higher + 1 - lower)) + lower;
         }
 
         if (elapsedTimeSinceLastNewFieldM > randApparitionM) {
             Obstacle o = new Obstacle(ficObs.get((int) (Math.random() * 2)));
             obstacles.add(o);
             int randX = (int) (Math.random() * (250 + 350 - 120 + 1 - 250)) + 250;
             o.setCoords(new Point2D.Double(randX + 387, -70));
             elapsedTimeSinceLastNewFieldM = 0;
 
             randApparitionM = (int) (Math.random() * (higher + 1 - lower)) + lower;
         }
 
         elapsedTimeSinceLastNewFieldD += elapsedTime;
         elapsedTimeSinceLastNewFieldG += elapsedTime;
         elapsedTimeSinceLastNewFieldM += elapsedTime;
 
 
     }
 
     private void manageInput(GameContainer gc, StateBasedGame sbg, int delta) {
         // Input managing du personnage 1
         Input input = gc.getInput();
         int it=0;
         if(Game.selectedPlayers[0]) {
             if (input.isKeyPressed(Input.KEY_Z)) {
                 players.get(it).iWouldLikeToJump();
             }
 
             if (input.isKeyDown(Input.KEY_D)) {
                 players.get(it).iWouldLikeToGoRight();
             }
 
             if (input.isKeyDown(Input.KEY_Q)) {
                 players.get(it).iWouldLikeToGoLeft();
             }
 
             if (input.isKeyDown(Input.KEY_S)) {
                 if(players.get(it).GetTimeSinceLastPower()>=5000){
                     players.get(it).SetTimeSinceLastPower(0);
                     players.get(it).setHasUsedGravityBoom(true);
                     //manage
                 }
             }
             it++;
         }
 
         if (Game.selectedPlayers[1]) {
             // Input managing du personnage 2
             if (input.isKeyPressed(Input.KEY_UP)) {
                 players.get(it).iWouldLikeToJump();
             }
 
             if (input.isKeyDown(Input.KEY_RIGHT)) {
                 players.get(it).iWouldLikeToGoRight();
             }
 
             if (input.isKeyDown(Input.KEY_LEFT)) {
                 players.get(it).iWouldLikeToGoLeft();
             }
             
             if (input.isKeyDown(Input.KEY_DOWN)) {
                 if(players.get(it).GetTimeSinceLastPower()>=5000){
                     players.get(it).SetTimeSinceLastPower(0);
                     players.get(it).setHasUsedGravityBoom(true);
 
                 }
             }
             it++;
         }
 
         if (Game.selectedPlayers[2]) {
             // Input managing du personnage 2
             if (input.isKeyPressed(Input.KEY_T)) {
                 players.get(it).iWouldLikeToJump();
             }
 
             if (input.isKeyDown(Input.KEY_H)) {
                 players.get(it).iWouldLikeToGoRight();
             }
 
             if (input.isKeyDown(Input.KEY_F)) {
                 players.get(it).iWouldLikeToGoLeft();
             }
             if (input.isKeyDown(Input.KEY_G)) {
                 if(players.get(it).GetTimeSinceLastPower()>=5000){
                     players.get(it).SetTimeSinceLastPower(0);
                 players.get(it).setHasUsedGravityBoom(true);
 
                 }
             }
             it++;
         }
 
         if(Game.selectedPlayers[3]) {
             // Input managing du personnage 2
             if (input.isKeyPressed(Input.KEY_I)) {
                 players.get(it).iWouldLikeToJump();
             }
 
             if (input.isKeyDown(Input.KEY_L)) {
                 players.get(it).iWouldLikeToGoRight();
             }
 
             if (input.isKeyDown(Input.KEY_J)) {
                 players.get(it).iWouldLikeToGoLeft();
             }
             if (input.isKeyDown(Input.KEY_K)) {
                 if(players.get(it).GetTimeSinceLastPower()>=5000){
                     players.get(it).SetTimeSinceLastPower(0);
                     players.get(it).setHasUsedGravityBoom(true);
                 }
             }
             it++;
         }
     }
 
     private void managePhysics() {
     }
 
     
     private void showInformation( Graphics gr) 
     {
         int step = 120;
         int p =players.size();
           //System.out.println(p);
         for(int i=0;i<p;i++)
         {
             Player a=players.get(i);
             if(a!=null)
             {
                gr.drawString("Player "+(i+1), 20, 20+i*step);
                a.getImage().draw( 20, 40+i*step);
                gr.drawString("Deaths :"+a.getNumberOfDeaths(), 80, 50+i*step);
                gr.drawString("Kills : "+a.getNumberOfKills(), 80, 70+i*step);
         
             }
         }
         
     }
 
     
     private void manageDeath() 
     {
         int p =players.size();
           //System.out.println(p);
         for(int i=0;i<p;i++)
         {
             Point2D a=players.get(i).getCoords();
             if(a!=null)
             {
                if (a.getY()>700)
                {
                    players.get(i).Die();
                    players.get(i).setCoords(new Point2D.Double(obstacles.get(obstacles.size()-1).getCoords().getX()+ 50, obstacles.get(obstacles.size()-1).getCoords().getY() - 150));
                }
             }
         }
     }
     
     
     private void manageGravityBoom() 
     {
         int p =players.size();
         for(int i=0;i<p;i++)
         {
             Player a=players.get(i);
             if(a!=null)
             {
                if (a.isHasUsedGravityBoom())
                {
                   for(int j=0;j<p;j++)
                   {
                         Player b=players.get(j);
                         if(b!=null)
                         {
                             if(b!=a)
                             {
                                //System.out.println("Je le pousse");
                                 double aX=b.getCoords().getX()-a.getCoords().getX() ;
                                 double aY=b.getCoords().getY()-a.getCoords().getY();
                                 double rayon =Math.sqrt(aX*aX +aY*aY);
                                 if(rayon<300)
                                 {
                                        b.setSpeed(1.5*aX*(1/Math.pow(1+rayon, 1)), 1.5*aY*(1/Math.pow(1+rayon,1)));
                                 }
                             }
                         }
                         
                   }
                   a.setHasUsedGravityBoom(false);
                }
             }
         }
     }
         
     private void initField() throws SlickException {
         obstacles.clear();
         ficObs.clear();
         ficObs.add("ressources/sprites/Plateforme/plateformeNuage1.png");
         ficObs.add("ressources/sprites/Plateforme/plateformeNuage1.png");
         Obstacle platInit = new Obstacle("ressources/initPlateforme.png");
         obstacles.add(platInit);
 
         platInit.setCoords(new Point2D.Double(250, 200));
 
         /*randApparitionD=0;
         randApparitionG=0;*/
     }
 
     private void initPlayers() throws SlickException {
         int decalage = 0;
 
         players.clear();
 
         for (String s : Game.players) {
             Player p = new Player(s);
             p.setCoords(new Point2D.Double(300 + decalage, 200 - p.getImage().getHeight()));
             decalage += 200;
             players.add(p);
         }
     }
 }
