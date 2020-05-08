 package ui.gui.graphical;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import logic.Architect;
 import logic.Direction;
 import logic.InanimatedObject;
 import logic.Maze;
 import logic.MazeGenerator;
 import logic.RandomMazeGenerator;
 import logic.Unit;
 import model.Position;
 import utils.Key;
 
 public class Game extends JPanel implements KeyListener, MazeGame
 {
     private Configuration CONFIG = new Configuration("maze.config");
 
     private static final long serialVersionUID = 1L;
 
     private final int WINDOW_WIDTH = 600;
     private final int WINDOW_HEIGHT = 600;
 
    private int CELL_WIDTH = WINDOW_WIDTH / CONFIG.GetMazeSize();
    private int CELL_HEIGHT = WINDOW_HEIGHT / CONFIG.GetMazeSize();

     private int MAZE_SIZE = 10;
 
     private void UpdateMazeSizes()
     {
         int min = Math.min(getWidth(), getHeight());
         CELL_WIDTH = min / MAZE_SIZE;
         CELL_HEIGHT = min / MAZE_SIZE;
     }
 
     public static void main(String[] args)
     {
         final Game m = new Game();
 
         final JFrame frame = new JFrame("The Maze");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setSize(m.WINDOW_WIDTH, m.WINDOW_HEIGHT);
         //frame.setExtendedState(Frame.MAXIMIZED_BOTH);
         //frame.setUndecorated(true);
 
         frame.setLayout(new BorderLayout());
 
         final JButton newGameButton = new JButton("New Game");
         final JButton saveButton = new JButton("Save/Load");
         final JButton exitButton = new JButton("Quit");
         final JButton settingsButton = new JButton("Settings");
 
         frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
         frame.addWindowListener(new WindowAdapter()
         {
             @Override
             public void windowClosing(WindowEvent we)
             {
                 m.CONFIG.TrySaveToFile();
                 System.exit(0);
             }
         });
 
         newGameButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 int result = JOptionPane.showConfirmDialog(frame, "Are you sure you want to start a new game?");
 
                 if (result == JOptionPane.YES_OPTION)
                     m.NewGame();
             }
         });
 
         exitButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
             }
         });
 
         settingsButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 SettingsDialog d = new SettingsDialog(frame, m.CONFIG);
                 d.setVisible(true);
                 m.CONFIG = d.GetNewConfiguration();
             }
         });
 
         saveButton.addActionListener(new ActionListener()
         {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 SaveLoadDialog d = new SaveLoadDialog(frame, m, false);
                 d.setVisible(true);
             }
         });
 
         newGameButton.addFocusListener(new FocusListener()
         {
             @Override public void focusLost(FocusEvent e) { m.requestFocusInWindow(); }
             @Override public void focusGained(FocusEvent e) { m.requestFocusInWindow(); }
         });
 
         saveButton.addFocusListener(new FocusListener()
         {
             @Override public void focusLost(FocusEvent e) { m.requestFocusInWindow(); }
             @Override public void focusGained(FocusEvent e) { m.requestFocusInWindow(); }
         });
 
         settingsButton.addFocusListener(new FocusListener()
         {
             @Override public void focusLost(FocusEvent e) { m.requestFocusInWindow(); }
             @Override public void focusGained(FocusEvent e) { m.requestFocusInWindow(); }
         });
 
         frame.getContentPane().add(m, BorderLayout.CENTER);
 
         JPanel southPanel = new JPanel(new GridLayout(1, 4));
         frame.getContentPane().add(southPanel, BorderLayout.SOUTH);
 
         southPanel.add(newGameButton);
         southPanel.add(saveButton);
         southPanel.add(settingsButton);
         southPanel.add(exitButton);
 
         frame.setVisible(true);
     }
 
     private Maze _maze;
    public Maze GetMaze() { return _maze; }
    public void SetMaze(Maze maze) { _maze = maze; }
     private final Map<String, TiledImage> _sprites;
     private boolean _gameFinished = false;
 
     public void NewGame()
     {
         _gameFinished = false;
 
         Architect architect = new Architect();
         MazeGenerator mg = new RandomMazeGenerator();
 
         architect.SetMazeGenerator(mg);
         architect.ConstructMaze(CONFIG.GetMazeSize(), CONFIG.GetNumberOfDragons(), CONFIG.GetDragonMode());
         MAZE_SIZE = CONFIG.GetMazeSize();
         SetMaze(architect.GetMaze());
         repaint();
     }
 
     public Game()
     {
         addKeyListener(this);
         setFocusable(true);
 
         NewGame();
 
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
         UpdateMazeSizes();
         requestFocusInWindow();
 
         g.setColor(new Color(0, 100, 0)); // dark green
         g.fillRect(0, 0, getWidth(), getHeight());
 
         for (int x = 0; x < GetMaze().GetWidth(); ++x)
         {
             for (int y = 0; y < GetMaze().GetHeight(); ++y)
             {
                 InanimatedObject c = GetMaze().GetGrid().GetCellT(x, y);
                 if (c.IsPath())
                     DrawCellAt(g, _sprites.get("grass").GetTile(0, 0), x, y);
                 else if (c.IsWall())
                     DrawCellAt(g, _sprites.get("stone").GetTile(0, 0), x, y);
                 else if (c.IsExitPortal())
                     DrawCellAt(g, _sprites.get("dark_stone").GetTile(0, 0), x, y);
             }
         }
 
         for (Unit u : GetMaze().GetLivingObjects())
         {
             switch (u.Type)
             {
             case Dragon:
                 DrawCellAt(g, _sprites.get("dragon").GetTile(iter % 9, u.ToDragon().IsSleeping() ? 11 : 0), u.GetPosition());
                 break;
             case Eagle:
                 if (!u.ToEagle().IsFlying() && !u.ToEagle().IsFollowingHero())
                     DrawCellAt(g, _sprites.get("eagle").GetTile(16, 0), u.GetPosition());
                 else if (u.ToEagle().IsFollowingHero())
                     DrawHalfCellAt(g, _sprites.get("eagle").GetTile(iter % 16, 0), u.GetPosition(), false);
                 else
                     DrawCellAt(g, _sprites.get("eagle").GetTile(iter % 16, 0), u.GetPosition());
                 break;
             case Hero:
                 if (GetMaze().FindEagle() != null && GetMaze().FindEagle().ToEagle().IsFollowingHero())
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
 
     public void DrawCellAt(Graphics g, Image img, Position pos) { DrawCellAt(g, img, pos.X, pos.Y); }
 
     public void DrawCellAt(Graphics g, Image img, int x, int y)
     {
        g.drawImage(img, (getWidth() - CELL_WIDTH * CONFIG.GetMazeSize()) / 2 + x * CELL_WIDTH, (getHeight() - CELL_HEIGHT * CONFIG.GetMazeSize()) / 2 + y * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT, null);
     }
 
     public void DrawHalfCellAt(Graphics g, Image img, Position pos, boolean left) { DrawHalfCellAt(g, img, pos.X, pos.Y, left); }
 
     public void DrawHalfCellAt(Graphics g, Image img, int x, int y, boolean left)
     {
        g.drawImage(img, (getWidth() - CELL_WIDTH * CONFIG.GetMazeSize()) / 2 + x * CELL_WIDTH + (left ? -(CELL_WIDTH / 4) : CELL_WIDTH / 4), (getHeight() - CELL_HEIGHT * CONFIG.GetMazeSize()) / 2 + y * CELL_HEIGHT, CELL_WIDTH, CELL_HEIGHT, null);
     }
 
     @Override
     public void keyTyped(KeyEvent e)
     {
         //repaint();
     }
 
     @Override
     public void keyPressed(KeyEvent e)
     {
         //repaint();
     }
 
     @Override
     public void keyReleased(KeyEvent e)
     {
         if (_gameFinished)
             return;
 
         Key k = null;
         Action a = CONFIG.GetAction(e.getKeyCode());
         if (a != null)
         {
             switch (a)
             {
             case HERO_UP:
                 k = Key.UP;
                 break;
             case HERO_DOWN:
                 k = Key.DOWN;
                 break;
             case HERO_RIGHT:
                 k = Key.RIGHT;
                 break;
             case HERO_LEFT:
                 k = Key.LEFT;
                 break;
             case SEND_EAGLE:
                 GetMaze().SendEagleToSword();
                 break;
             default:
                 return;
             }
         }
 
         if (k != null)
             GetMaze().MoveHero(Direction.FromKey(k));
         GetMaze().Update();
 
         if (GetMaze().IsFinished())
         {
             _gameFinished = true;
 
             if (GetMaze().FindHero() != null)
                 JOptionPane.showMessageDialog(this, "You won!");
             else
                 JOptionPane.showMessageDialog(this, "You lost, game over.");
 
             int result = JOptionPane.showConfirmDialog(this, "Do you wish to start a new game?");
             if (result == JOptionPane.YES_OPTION)
                 NewGame();
         }
 
         repaint();
     }
 }
