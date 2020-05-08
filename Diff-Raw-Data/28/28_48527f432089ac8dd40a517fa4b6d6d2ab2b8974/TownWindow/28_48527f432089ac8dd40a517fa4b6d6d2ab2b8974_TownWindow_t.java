 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.TiledMap;
 
 public class TownWindow extends NodeWindow {
     private Double timer;
 
     public TownWindow(Player[] players) throws SlickException {
         super(players);
     }
 
     @Override
     public void render(GameContainer container, StateBasedGame game,
             Graphics g, Player[] player) throws SlickException {
         //Rendering things has.. an interesting order.
         for (int i = 0; i < player.length; i++) {
             if (inNode[i]) {
                 this.displayMinigameBackground(g, player[i]);
             }
         }
         for (int i = 0; i < players.length; i++) {
             if(inNode[1]){
                 //Note that player.render defines the persons location.
                 //so it has to render in this order. We may want to change this.
                 player[i].render(container, game, g, playerPos[i][0]
                        + container.getWidth() / 2 +10 , playerPos[i][1]);
             }
             if (inNode[0]) {
                 player[i].render(container, game, g, playerPos[i][0],
                         playerPos[i][1]);
             }
             if(inNode[i]) {
                 GameState state = (GameState) (game.getCurrentState());
                 g.setFont(state.uFont);
                 g.setColor(Color.black);
                 g.drawString(Double.toString(Math.ceil(timer)),
                         players[i].windowPos[0] + container.getWidth() / 4,
                         players[i].windowPos[1]);
             }
         }
         // not necessary once the images are imported correctly
         for (MiniGame mgame : miniGames) {
             Rectangle r = mgame.location;
             if(inNode[0]){
                 g.draw(r);
             }
             if(inNode[1]){
             g.drawRect(r.getX() +9 + container.getWidth() / 2, r.getY(), r
                     .getWidth(), r.getHeight());
             }
         }
     }
 
     @Override
     public void init(GameContainer container, StateBasedGame game,
             Player[] players) throws SlickException {
         super.init(container, game, players);
        Music loop = new Music("Assets/Town2.wav");
        loop.loop();
         bgImage = new Image("Assets/Hub 1/FinalImageRef.png");
         miniGames = new MiniGame[2];
         miniGames[0] = new MiniGame(new Rectangle(53, 500, 30, 20), 0);
         miniGames[1] = new MiniGame(new Rectangle(228, 435, 50, 20), 1);
         for (int i = 0; i < players.length; i++) {
             /*
              * playerPos[i][0] = players[i].windowPos[0] +
              * players[i].windowSize[0] - players[i].pWidth; playerPos[i][1] =
              * players[i].windowPos[1] + (int) players[i].windowSize[1] / 2;
              */
             playerPos[i][0] = 0;
             playerPos[i][1] = 0;
             timer = 30.0;
         }
     }
 
     @Override
     public void update(GameContainer container, StateBasedGame game, int delta,
             Player[] player) throws SlickException {
         Input input = container.getInput();
         for (int i = 0; i < players.length; i++) {
             if (inNode[i]) {
                 float moveValue = delta * .2f;
                 movePlayer(input, moveValue, i);
             }
             for (int j = 0; j < miniGames.length; j++) {
                 if (miniGames[j].location.intersects(player[i].boundingBox)) {
                     GameState state = (GameState) game.getCurrentState();
                     if(miniGames[j].enterGame(container, game, players, i)){
                         inNode[i] = false;
                     }
                 }
             }
         }
 
         timer -= 1.0 / 60;
         if (timer <= 0) {
             this.over = true;
         }
     }
 }
