 package ui.gui.graphical;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import logic.Architect;
 import logic.Direction;
 import logic.Dragon;
import logic.Eagle.EagleState;
 import logic.InanimatedObject;
 import logic.Maze;
 import logic.MazeGenerator;
 import logic.RandomMazeGenerator;
 import logic.Unit;
 import model.Position;
 import utils.Key;
 
 public class Main extends JPanel implements KeyListener
 {
     private static final long serialVersionUID = 1L;
 
     private static final int MAZE_SIZE = 10;
 
     private static final int WINDOW_WIDTH = 600;
     private static final int WINDOW_HEIGHT = 600;
 
     private static final int CELL_WIDTH = WINDOW_WIDTH / MAZE_SIZE;
     private static final int CELL_HEIGHT = WINDOW_HEIGHT / MAZE_SIZE;
 
     public static void main(String[] args)
     {
         JFrame frame = new JFrame("The Maze");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
         frame.setResizable(false);
 
         //frame.setLayout(new BorderLayout());
 
         Main m = new Main();
         frame.getContentPane().add(m);
 
         frame.setVisible(true);
     }
 
     private Maze _maze;
     private boolean _gameStarted = true;
     private Map<String, TiledImage> _sprites;
 
     public Main()
     {
         addKeyListener(this);
         setFocusable(true);
 
         Architect architect = new Architect();
         MazeGenerator mg = new RandomMazeGenerator();
 
         architect.SetMazeGenerator(mg);
         architect.ConstructMaze(MAZE_SIZE, 2, Dragon.Behaviour.Sleepy);
 
         _maze = architect.GetMaze();
 
         // Load sprites
         _sprites = new HashMap<String, TiledImage>();
         _sprites.put("dragon",     new TiledImage("resources/dragon_trans.png", 128, 128));
         _sprites.put("eagle",      new TiledImage("resources/eagle_trans.png",  64,  64));
         _sprites.put("hero",       new TiledImage("resources/hero_trans.png",   96,  96));
         _sprites.put("sword",      new TiledImage("resources/sword_trans.png",  96,  96));
         _sprites.put("grass",      new TiledImage("resources/grass.png",        96,  96));
         _sprites.put("stone",      new TiledImage("resources/stone.png",        96,  96));
         _sprites.put("dark_stone", new TiledImage("resources/dark_stone.png",   96,  96));
     }
 
     static int iter = 0;
 
     @Override
     public void paintComponent(Graphics g)
     {
         super.paintComponent(g);
 
         g.setColor(new Color(0, 100, 0)); // dark green
         g.fillRect(0, 0, getWidth(), getHeight());
 
         for (int x = 0; x < _maze.GetWidth(); ++x)
         {
             for (int y = 0; y < _maze.GetHeight(); ++y)
             {
                 InanimatedObject c = _maze.GetGrid().GetCellT(x, y);
                 if (c.IsPath())
                     DrawCellAt(g, _sprites.get("grass").GetTile(0, 0), x, y);
                 else if (c.IsWall())
                     DrawCellAt(g, _sprites.get("stone").GetTile(0, 0), x, y);
                 else if (c.IsExitPortal())
                     DrawCellAt(g, _sprites.get("dark_stone").GetTile(0, 0), x, y);
             }
         }
 
         for (Unit u : _maze.GetLivingObjects())
         {
             switch (u.Type)
             {
                 case Dragon:
                     DrawCellAt(g, _sprites.get("dragon").GetTile(iter % 9, u.ToDragon().IsSleeping() ? 11 : 0), u.GetPosition());
                     break;
                 case Eagle:
                    if (u.ToEagle().GetState() == EagleState.OnFloor)
                        DrawCellAt(g, _sprites.get("eagle").GetTile(16, 0), u.GetPosition());
                    else if (u.ToEagle().GetState() == EagleState.FollowingHero)
                        DrawHalfCellAt(g, _sprites.get("eagle").GetTile(iter % 16, 0), u.GetPosition(), false);
                    else
                        DrawCellAt(g, _sprites.get("eagle").GetTile(iter % 16, 0), u.GetPosition());
                     break;
                 case Hero:
                    if (_maze.FindEagle() != null && _maze.FindEagle().ToEagle().GetState() == EagleState.FollowingHero)
                        DrawHalfCellAt(g, _sprites.get("hero").GetTile(iter % 7, u.ToHero().IsArmed() ? 6 : 8), u.GetPosition(), true);
                    else
                        DrawCellAt(g, _sprites.get("hero").GetTile(iter % 7, u.ToHero().IsArmed() ? 6 : 8), u.GetPosition());
                     break;
                 case Sword:
                     DrawCellAt(g, _sprites.get("sword").GetTile(0, 0), u.GetPosition());
                     break;
                 default:
                     break;
             }
         }
 
         iter++;
     }
 
     public static void DrawCellAt(Graphics g, Image img, Position pos) { DrawCellAt(g, img, pos.X, pos.Y); }
 
     public static void DrawCellAt(Graphics g, Image img, int x, int y)
     {
         g.drawImage(img, x * CELL_WIDTH, y * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT, null);
     }
 
    public static void DrawHalfCellAt(Graphics g, Image img, Position pos, boolean left) { DrawHalfCellAt(g, img, pos.X, pos.Y, left); }

    public static void DrawHalfCellAt(Graphics g, Image img, int x, int y, boolean left)
    {
        g.drawImage(img, x * CELL_WIDTH + (left ? -(CELL_WIDTH / 4) : CELL_WIDTH / 4), y * CELL_HEIGHT + (!left ? 0 : 0), CELL_WIDTH, CELL_HEIGHT, null);
    }

     @Override
     public void keyTyped(KeyEvent e)
     {
         //repaint();
     }
 
     @Override
     public void keyPressed(KeyEvent e)
     {
         _maze.Update();
         repaint();
     }
 
     @Override
     public void keyReleased(KeyEvent e)
     {
         if (!_gameStarted)
             return;
 
         Key k = null;
 
         switch (e.getKeyCode())
         {
             case KeyEvent.VK_W:
                 k = Key.UP;
                 break;
             case KeyEvent.VK_S:
                 k = Key.DOWN;
                 break;
             case KeyEvent.VK_D:
                 k = Key.RIGHT;
                 break;
             case KeyEvent.VK_A:
                 k = Key.LEFT;
                 break;
             case KeyEvent.VK_F1:
                 _maze.SendEagleToSword();
                 break;
             default:
                 return;
         }
 
         if (k != null)
             _maze.MoveHero(Direction.FromKey(k));
         _maze.Update();
 
         if (_maze.IsFinished())
         {
             JOptionPane.showMessageDialog(this, "Game Over");
             _gameStarted = false;
         }
 
         repaint();
     }
 }
