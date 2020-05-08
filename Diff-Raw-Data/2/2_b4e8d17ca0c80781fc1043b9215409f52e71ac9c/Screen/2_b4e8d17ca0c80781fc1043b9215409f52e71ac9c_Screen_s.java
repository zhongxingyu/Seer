 package game.graphics;
 
 import game.Game;
 import game.State;
 import game.handlers.InputHandler;
 
 import java.awt.*;
 
 public class Screen {
 
     private Game game;
     private InputHandler input;
 
     public Screen(Game game, InputHandler input) {
         this.game = game;
         this.input = input;
     }
 
     public void render(Graphics2D g) {
         switch (game.getState()) {
             case MAIN_SCREEN:
                 Rectangle start = new Rectangle(Game.WIDTH * Game.SCALE / 2 - 400 / 2, 150, 400, 80);
                 Rectangle exit = new Rectangle(Game.WIDTH * Game.SCALE / 2 - 400 / 2, 150 + 80 + 25, 400, 80);
 
                 if (start.contains(input.clicked)) {
                     game.setState(State.PLAYING);
                     input.clicked = new Point();
                 }
 
                 if (exit.contains(input.clicked))
                     game.stop();
 
                 // Draw button
                 g.setColor(Color.DARK_GRAY);
                 g.fillRect(start.x, start.y, start.width, start.height);
                 g.fillRect(exit.x, exit.y, exit.width, exit.height);
 
                 // Draw outline
                 g.setColor(Color.BLACK);
                 g.setStroke(new BasicStroke(4.5f));
                 g.drawRect(start.x, start.y, start.width, start.height);
                 g.drawRect(exit.x, exit.y, exit.width, exit.height);
 
                 // Draw Text
                 Font prev = g.getFont();
                 g.setFont(new Font(prev.getName(), Font.BOLD, 38));
 
                 g.setColor(Color.WHITE);
                 g.drawString("Start Game", Game.WIDTH * Game.SCALE / 2 - g.getFontMetrics().stringWidth("Start Game") / 2, 150 + g.getFontMetrics().getHeight());
                 g.drawString("Exit", Game.WIDTH * Game.SCALE / 2 - g.getFontMetrics().stringWidth("Exit") / 2, 150 + 80 + 25 + g.getFontMetrics().getHeight());
 
                 g.setFont(prev);
 
                 break;
             case PLAYING:
                 break;
             case LOADING:
                 g.setColor(Color.BLACK);
                 g.fillRect(0, 0, game.getWidth(), game.getHeight());
                 g.setColor(Color.WHITE);
                 Font f = g.getFont();
                 g.setFont(f.deriveFont(32.0f));
                 g.drawString("Loading...", 400, 350);
                 g.setFont(f);
                 break;
             case COMPLETED:
                 g.setColor(Color.BLACK);
                 g.fillRect(0, 0, game.getWidth(), game.getHeight());
                 Font prevf = g.getFont();
                 g.setFont(prevf.deriveFont(32.0f).deriveFont(Font.BOLD));
                Text.drawOutline(g, "Congratulations!", Color.RED, 350, 400);
                 g.setColor(Color.WHITE);
                 g.drawString("Congratulations!", 350, 350);
                 g.setFont(prevf);
                 break;
         }
     }
 
 }
