 package ihm;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.KeyStroke;
 
 import com.atticlabs.zonelayout.swing.ZoneLayout;
 import com.atticlabs.zonelayout.swing.ZoneLayoutFactory;
 
 import world.World;
 import world.WorldModel;
 import world.WorldModel.Direction;
 import world.WorldModel.GameState;
 
 public class GameView extends JFrame {
   OptionDialog od;
   
   public GameView(World world, WorldModel wm) {
     setTitle("JSnake ! The Game !");
     setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     
     setSize(700, 700);
     
     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
     Dimension frameSize = this.getSize();
     //--- Place la fentre de l'application
     setLocation((screenSize.width - frameSize.width) / 2,
     (screenSize.height - frameSize.height) / 5);
     
     GamePanel gp = new GamePanel(wm);
     
     KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
     gp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), key);
     gp.getActionMap().put(gp.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0)), new ArrowsAction(wm, Direction.UP));
     key = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
     gp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), key);
     gp.getActionMap().put(gp.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0)), new ArrowsAction(wm, Direction.DOWN));
     key = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
     gp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), key);
     gp.getActionMap().put(gp.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0)), new ArrowsAction(wm, Direction.LEFT));
     key = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
     gp.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), key);
     gp.getActionMap().put(gp.getInputMap().get(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0)), new ArrowsAction(wm, Direction.RIGHT));
 
     
     od = new OptionDialog(wm);
     od.setModal(true);
     
    wm.setState(GameState.PLAY);
     ZoneLayout layout = ZoneLayoutFactory.newZoneLayout();
     setLayout(layout);
     layout.addRow("a+*.......ab.b");
     gp.addComponentListener(new GamePanelResize(gp));
     add(gp, "a");
     add(new StatsPanel(wm), "b");
   }
   public void setVisible(boolean state){
     super.setVisible(state);
     od.setVisible(true);
   }
 }
