 /*
  * Object: MainWindow
  * The view of the scrolling text portion of the game.
  */
 package run;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.state.StateBasedGame;
 
 import core.Player;
 import core.TextBlock;
 
 public class MainWindow extends Window {
 
     private final int LINE_HEIGHT = 120; // Distance between baselines of successive lines of text.
     private final int END_LINE_HEIGHT = 90; // Distance between lines after acceleration.
     private final double TICK_LENGTH = 11; // Milliseconds for text to move one pixel.
     private final int MARGIN = 30;
     private final int MIN_GAP = 50;
     private final int Y_OFFSET = 300;
     private BufferedReader reader;
     private ArrayList<TextBlock> lines;
     private int firstMiniGame;
     private int secondMiniGame;
 
     public MainWindow(Player player) {
         super(player);
         lines = new ArrayList<TextBlock>(300);
     }
 
     @Override
     public void render(GameContainer container, StateBasedGame game, Graphics g, Player player) throws SlickException {
         for (int i = 0; i < lines.size(); i++) {
             TextBlock line = lines.get(i);
             if (-500 < line.pos[1] && line.pos[1] < 1500) {
                 line.render(container, game, g, player);
             }
         }
 
         player.render(container, game, g, playerPos[0], playerPos[1]);
 
         PlayGameState state = (PlayGameState) (game.getCurrentState());
         UnicodeFont uFont = state.uFont;
         if (state.wonPlayer != null) {
             g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.6f));
             g.fillRect(player.windowPos[0], player.windowPos[1], player.windowSize[0], player.windowSize[1]);
             g.setColor(Color.black);
 
             g.setFont(state.uFont);
             String message;
             if (state.wonPlayer == player) {
                 message = "You win!";
             } else {
                 message = "You lose. :'(";
             }
             float messageX = player.windowPos[0] + (player.windowSize[0] - uFont.getWidth(message)) / 2;
             float messageY = player.windowPos[1] + (player.windowSize[1] - uFont.getHeight(message)) / 2;
             g.drawString(message, messageX, messageY);
         }
     }
 
     private void addLine(Player player, String text, UnicodeFont uFont, int counter, float x, float y) {
         if (text.length() == 0)
             return;
 
         if (text.contains("SwitchGame1")) {
             if (player.tag == 1) {
                 this.firstMiniGame = 1;
             } else {
                 this.secondMiniGame = 1;
             }
             return;
         } else if (text.contains("SwitchGame2")) {
             if (player.tag == 1) {
                 this.firstMiniGame = 2;
             } else {
                 this.secondMiniGame = 2;
             }
             return;
         } else if (text.contains("SwitchGame3")) {
             if (player.tag == 1) {
                 this.firstMiniGame = 3;
             } else {
                 this.secondMiniGame = 3;
             }
             return;
         } else if (text.contains("SwitchGame4")) {
             if (player.tag == 1) {
                 this.firstMiniGame = 4;
             } else {
                 this.secondMiniGame = 4;
             }
             return;
         }
 
         String[] words = text.split(" ");
         int splitWord = (int) (Math.random() * (words.length - 1)) + 1;
 
         StringBuilder firstBuilder = new StringBuilder();
         for (int i = 0; i < splitWord; i++) {
             firstBuilder.append(words[i]);
             if (i < splitWord - 1) {
                 firstBuilder.append(" ");
             }
         }
         String first = firstBuilder.toString();
         String second = text.substring(first.length() + 1);
 
         // Render the second string right-justified.
         float secondX = x - MARGIN + player.windowSize[0] - uFont.getWidth(second);
         int gameTag;
         if (player.tag == 1) {
             gameTag = this.firstMiniGame;
         } else {
             gameTag = this.secondMiniGame;
         }
         lines.add(new TextBlock(first, uFont, x + MARGIN, y + MARGIN + Y_OFFSET + LINE_HEIGHT * counter, gameTag));
         lines.add(new TextBlock(second, uFont, secondX, y + MARGIN + Y_OFFSET + LINE_HEIGHT * counter, gameTag));
     }
 
     @Override
     public void init(GameContainer container, StateBasedGame game, Player player) throws SlickException {
         playerPos[0] = player.windowPos[0] + 50;
         playerPos[1] = player.windowPos[1] + 50;
         try {
             this.reader = new BufferedReader(new FileReader("resources/story.txt"));
         } catch (FileNotFoundException e) {
             throw new SlickException(e.getMessage());
         }
         StringBuilder currentString = new StringBuilder(120); // Capacity of buffer.
 
         UnicodeFont uFont = ((PlayGameState) (game.getCurrentState())).uFont;
 
         try {
             int counter = 0;
             float x = player.windowPos[0];
             float y = player.windowPos[1];
             for (String words = reader.readLine(); words != null; words = reader.readLine()) {
                 for (String word : words.split("\\s")) {
                     if (word.length() == 0)
                         continue;
 
                     if (currentString.length() > 0) {
                         currentString.append(' ');
                     }
                     currentString.append(word);
 
                     if (uFont.getWidth(currentString.toString()) > player.windowSize[0] - 2 * MARGIN - MIN_GAP) {
                         // this will cause problems with one-word lines.
                         int prevLength = currentString.length() - word.length() - 1;
                         addLine(player, currentString.substring(0, prevLength), uFont, counter, x, y);
                         counter++;
                         currentString.delete(0, prevLength + 1);
                     }
                 }
             }
             addLine(player, currentString.toString(), uFont, counter, x, y);
             counter++;
         } catch (IOException e) {
             throw new SlickException(e.getMessage());
         }
     }
 
     @Override
     public void update(GameContainer container, StateBasedGame game, int delta, Player player) throws SlickException {
 
         Input input = container.getInput();
         if (input.isKeyDown(player.getButton("left"))) {
             playerPos[0] -= delta * .2f;
         }
         if (input.isKeyDown(player.getButton("right"))) {
             playerPos[0] += delta * .2f;
         }
         if (input.isKeyDown(player.getButton("up"))) {
             playerPos[1] -= delta * .2f;
         }
         if (input.isKeyDown(player.getButton("down"))) {
             playerPos[1] += delta * .2f;
         }
         playerPos[0] = Math.max(playerPos[0], player.windowPos[0] + player.pWidth);
         playerPos[0] = Math.min(playerPos[0], player.windowPos[0] + player.windowSize[0] - player.pWidth);
         playerPos[1] = Math.max(playerPos[1], player.windowPos[1] + player.pWidth);
         playerPos[1] = Math.min(playerPos[1], player.windowPos[1] + player.windowSize[1] - player.pWidth);
 
         float movement = -(float) delta / (float) TICK_LENGTH;
         PlayGameState state = (PlayGameState) (game.getCurrentState());
         for (int i = 0; i < lines.size(); i++) {
             TextBlock line = lines.get(i);
             boolean result = line.update(movement, player);
             if (result && line.color != Color.red) {
                 double rand = Math.random();
 
                 if (line.miniGame == 0) {
                     if (rand < 0.5) {
                         state.triggerMinigame(container, game, player, new TutorialRightWindow(player));
                     } else {
                         state.triggerMinigame(container, game, player, new TutorialLeftWindow(player));
                     }
                 } else if (line.miniGame == 1) {
                     state.triggerMinigame(container, game, player, new MazeWindow(player));
                 } else if (line.miniGame == 2) {
                     state.triggerMinigame(container, game, player, new MashWindow(player));
                 } else if (line.miniGame == 3) {
                     state.triggerMinigame(container, game, player, new CatchWindow(player));
                 } else if (line.miniGame == 4) {
                     state.triggerMinigame(container, game, player, new DodgeWindow(player));
                 }
             }
         }
         if (lines.get(lines.size() - 1).pos[1] <= playerPos[1]) {
             state.wonPlayer = player;
         }
     }
 
     @Override
     public void enter(GameContainer container, StateBasedGame game, Player player) {
 
     }
 }
