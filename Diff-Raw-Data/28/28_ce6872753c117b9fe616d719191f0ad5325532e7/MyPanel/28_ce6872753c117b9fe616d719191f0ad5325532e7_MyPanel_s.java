 package game;
 
 import static java.lang.Math.PI;
 import static java.lang.Math.cos;
 import static java.lang.Math.sin;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 final class MyPanel extends JPanel
 {
     private static final long serialVersionUID = 1L;
 
     final static int NUM_PLAYERS = 4;
     final static String[] PLAYER_NAMES =
     { "Kevin", "Fanhal", "Computer1", "Computer2" };
     final static Color[] PLAYER_COLORS =
     { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW };
     final static Control[] PLAYER_CONTROL =
     { Control.HUMAN, Control.HUMAN, Control.COMPUTER, Control.COMPUTER };
     final static KeySet[] PLAYER_KEYS =
     { KeySet.WASD, KeySet.ARROWS, KeySet.IJKL, KeySet.ARROWS };
     final static int SPEED = 40;
     final static int TILE_SIZE = 10;
     final static Color BACKGROUND = Color.BLACK;
     final static Color PORTAL_COLOR = new Color(1, 1, 1); // extra
     final static Color INVINCIBILITY_COLOR = new Color(1, 1, 2); // extra
     final static Color BOMB_COLOR = new Color(1, 1, 3); // extra
     final static double PORTAL_PROBABILITY = .004; // extra
     final static double INVINCIBILITY_PROBABILITY = .001; // extra
     final static double BOMB_PROBABILITY = .004; // extra
     final static int PORTAL_TIME_OUT = 200; // extra
     final static int INVINCIBILITY_TIME_OUT = 400; // extra
     final static int BOMB_RANGE = 16; // extra
     final static boolean SPECIALS = true;
 
     enum Control
     {
         HUMAN, COMPUTER
     }
 
     Grid grid;
     Player[] players;
     Timer timer;
     Point[] portals; // extra
     int portalTimer; // extra
     Image portalImage; // extra
     Image invincibilityImage; // extra
     Image bombImage; // extra
 
     MyPanel()
     {
         grid = new Grid(getSize().width / TILE_SIZE, getSize().height
                 / TILE_SIZE);
 
         players = new Player[NUM_PLAYERS];
 
         int distance = getHeight() / TILE_SIZE / 4;
         for (int i = 0; i < NUM_PLAYERS; i++)
         {
             Point p = new Point(getSize().width / 2 / TILE_SIZE
                     + (int) (distance * cos(2 * PI * i / NUM_PLAYERS)),
                     getSize().height / 2 / TILE_SIZE
                             + (int) (distance * sin(2 * PI * i / NUM_PLAYERS)));
 
             String name = i >= PLAYER_NAMES.length ? "" : PLAYER_NAMES[i];
             Color color = i >= PLAYER_COLORS.length ? Color.GRAY
                     : PLAYER_COLORS[i];
             Control control = i >= PLAYER_CONTROL.length ? Control.COMPUTER
                     : PLAYER_CONTROL[i];
             if (control == Control.HUMAN)
                 players[i] = new Player(name, color, p);
             else
                 players[i] = new ComputerPlayer(this, name, color, p);
         }
 
         // extra
         try
         {
            portalImage = ImageIO.read(new File("portal.gif"));
            invincibilityImage = ImageIO.read(new File("invincibility.gif"));
            bombImage = ImageIO.read(new File("bomb.gif"));
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
     }
 
     public void paintComponent(Graphics g)
     {
        for (int i = 0; i < getSize().height / TILE_SIZE; i++)
            for (int j = 0; j < getSize().width / TILE_SIZE; j++)
                 if (SPECIALS && grid.colors[i][j].equals(PORTAL_COLOR))
                     g.drawImage(portalImage, j * TILE_SIZE, i * TILE_SIZE, null);
                 else if (SPECIALS
                         && grid.colors[i][j].equals(INVINCIBILITY_COLOR))
                     g.drawImage(invincibilityImage, j * TILE_SIZE, i
                             * TILE_SIZE, null);
                 else if (SPECIALS && grid.colors[i][j].equals(BOMB_COLOR))
                     g.drawImage(bombImage, j * TILE_SIZE, i * TILE_SIZE, null);
                 else
                 {
                     g.setColor(grid.colors[i][j]);
                     g.fillRect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE,
                             TILE_SIZE);
                 }
     }
 
     void reset()
     {
         grid = new Grid(getSize().width / TILE_SIZE, getSize().height
                 / TILE_SIZE);
         initializePlayers();
         portals = null;
 
         addKeyListener(new KeyAdapter()
         {
             public void keyPressed(KeyEvent e)
             {
                 for (int i = 0; i < NUM_PLAYERS; i++)
                 {
                     if (!(players[i] instanceof ComputerPlayer))
                     {
                         if (i >= PLAYER_KEYS.length)
                             return;
 
                         if (PLAYER_KEYS[i].UP == e.getKeyCode())
                             players[i].setDirection(Player.UP);
                         else if (PLAYER_KEYS[i].LEFT == e.getKeyCode())
                             players[i].setDirection(Player.LEFT);
                         else if (PLAYER_KEYS[i].DOWN == e.getKeyCode())
                             players[i].setDirection(Player.DOWN);
                         else if (PLAYER_KEYS[i].RIGHT == e.getKeyCode())
                             players[i].setDirection(Player.RIGHT);
                     }
                 }
             }
         });
 
         ActionListener listener = new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 boolean death = false;
 
                 if (SPECIALS)
                 {
                     // extra
                     if (portals == null && Math.random() < PORTAL_PROBABILITY)
                     {
                         portals = new Point[2];
                         portalTimer = 0;
                         for (int i = 0; i < 2; i++)
                         {
                             while (portals[i] == null
                                     || !grid.isValid(portals[i]))
                                 portals[i] = new Point(
                                         (int) (Math.random() * grid.width),
                                         (int) (Math.random() * grid.height));
 
                             grid.setColor(portals[i], PORTAL_COLOR);
                         }
                     }
                     else if (portals != null)
                     {
                         portalTimer++;
                         if (portalTimer >= PORTAL_TIME_OUT)
                         {
                             for (Point point : portals)
                                 grid.setColor(point, BACKGROUND);
 
                             portals = null;
                         }
                     }
 
                     if (Math.random() < INVINCIBILITY_PROBABILITY)
                     {
                         Point p = null;
                         while (p == null || !grid.isValid(p))
                             p = new Point((int) (Math.random() * grid.width),
                                     (int) (Math.random() * grid.height));
 
                         grid.setColor(p, INVINCIBILITY_COLOR);
                     }
 
                     if (Math.random() < BOMB_PROBABILITY)
                     {
                         Point p = null;
                         while (p == null || !grid.isValid(p))
                             p = new Point((int) (Math.random() * grid.width),
                                     (int) (Math.random() * grid.height));
 
                         grid.setColor(p, BOMB_COLOR);
                     }
                     //
                 }
 
                 for (Player player : players)
                     if (!player.isDead)
                     {
                         player.move();
 
                         if (SPECIALS)
                         {
                             // extra
                             if (grid.inBounds(player.point)
                                     && grid.colors[player.point.y][player.point.x]
                                             .equals(PORTAL_COLOR))
                             {
                                 if (player.point.equals(portals[0]))
                                     player.point = portals[1];
                                 else
                                     player.point = portals[0];
 
                                 for (Point point : portals)
                                     grid.setColor(point, BACKGROUND);
 
                                 portals = null;
                             }
 
                             if (grid.inBounds(player.point)
                                     && grid.colors[player.point.y][player.point.x]
                                             .equals(INVINCIBILITY_COLOR))
                             {
                                 player.invincible = true;
                                 player.invincibilityTimer = 0;
                             }
 
                             if (player.invincible)
                             {
                                 player.invincibilityTimer++;
 
                                 if (player.invincibilityTimer >= INVINCIBILITY_TIME_OUT)
                                     player.invincible = false;
                             }
 
                             if (grid.inBounds(player.point)
                                     && grid.colors[player.point.y][player.point.x]
                                             .equals(BOMB_COLOR))
                                 bomb(player.point);
                             //
                         }
 
                         if (grid.isValid(player.point) || player.invincible
                                 && grid.inBounds(player.point))
                         {
                             if (player.invincible)
                                 grid.setColor(player.point, Color.WHITE);
                             else
                                 grid.setColor(player.point, player.color);
 
                             getGraphics().setColor(player.color);
                             getGraphics().fillRect(player.point.x * TILE_SIZE,
                                     player.point.y * TILE_SIZE, TILE_SIZE,
                                     TILE_SIZE);
                         }
                         else
                         {
                             player.isDead = true;
                             death = true;
                         }
                     }
 
                 if (death)
                 {
                     List<Player> left = playersLeft();
                     if (left.isEmpty())
                     {
                         timer.stop();
                         if (JOptionPane.showConfirmDialog(null, scoreList()
                                 + "CONTINUE?") == JOptionPane.NO_OPTION)
                             Bmtron.menu();
                         else
                             reset();
                     }
                     else if (left.size() == 1)
                     {
                         timer.stop();
                         left.get(0).score++;
                         if (JOptionPane.showConfirmDialog(null,
                                 left.get(0).name + " WINS!\n\n" + scoreList()
                                         + "CONTINUE?") == JOptionPane.NO_OPTION)
                         {
                             Bmtron.menu();
                         }
                         else
                             reset();
                     }
 
                     if (onlyComputers())
                         timer.setDelay(1);
                 }
 
                 repaint();
             }
         };
 
         timer = new Timer(SPEED, listener);
         timer.start();
         repaint();
     }
 
     private void initializePlayers()
     {
         int distance = getHeight() / TILE_SIZE / 4;
 
         for (int i = 0; i < NUM_PLAYERS; i++)
         {
             Point p = new Point(getSize().width / 2 / TILE_SIZE
                     + (int) (distance * cos(2 * PI * i / NUM_PLAYERS)),
                     getSize().height / 2 / TILE_SIZE
                             + (int) (distance * sin(2 * PI * i / NUM_PLAYERS)));
 
             players[i].point = p;
             players[i].isDead = false;
             players[i].direction = Player.UP;
         }
     }
 
     private List<Player> playersLeft()
     {
         List<Player> left = new ArrayList<Player>();
 
         for (Player player : players)
             if (!player.isDead)
                 left.add(player);
 
         return left;
     }
 
     private boolean onlyComputers()
     {
         for (Player player : players)
             if (!player.isDead && !(player instanceof ComputerPlayer))
                 return false;
 
         return true;
     }
 
     private void bomb(Point point)
     {
         for (int i = 0; i < grid.height; i++)
             for (int j = 0; j < grid.width; j++)
                 if (Math.abs(point.x - j) + Math.abs(point.y - i) <= BOMB_RANGE)
                     grid.colors[i][j] = BACKGROUND;
     }
 
     private String scoreList()
     {
         StringBuilder builder = new StringBuilder("SCORES\n");
 
         for (Player player : players)
             builder.append(player.name + ":  \t" + player.score + "\n");
 
         return builder.toString();
     }
 
     public boolean isFocusable()
     {
         return true;
     }
 }
