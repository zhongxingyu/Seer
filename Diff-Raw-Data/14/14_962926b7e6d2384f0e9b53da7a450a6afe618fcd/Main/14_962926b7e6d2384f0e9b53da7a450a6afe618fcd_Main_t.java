 package gameoflife.ui;
 
 import gameoflife.game.*;
 import gameoflife.templates.CellBlock;
 import gameoflife.templates.Pattern;
 
 import javax.swing.*;
 import javax.swing.Timer;
 import javax.swing.event.MouseInputAdapter;
 import java.awt.*;
 import java.awt.Point;
 import java.awt.event.*;
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.*;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicReference;
 
 public class Main extends JDialog {
 
     private Timer _timer;
 
     public static void main(String[] args) {
         Main dialog = new Main();
         dialog.pack();
         dialog.start();
         dialog.setVisible(true);
     }
 
     private JPanel contentPane;
     private JPanel _panel;
     private JLabel currentGeneration;
 
     private final int _width = 200;
     private final int _height = 200;
     private int _sizePerCell = 1;
 
     private Generation _currentGeneration;
     private final Object _currentGenerationMonitor = new Object();
     private AtomicBoolean _moveAction = new AtomicBoolean();
     private AtomicReference<gameoflife.game.Point> _startPoint = new AtomicReference<gameoflife.game.Point>();
     private AtomicReference<gameoflife.game.Point> _currentTopLeftCorner = new AtomicReference<gameoflife.game.Point>(new gameoflife.game.Point(0, 0));
 
     public Main() {
         setContentPane(contentPane);
         setModal(true);
         final Dimension dimension = new Dimension(_width * _sizePerCell, _height * _sizePerCell);
         _currentGeneration = new Generation(_width, _height);
         initWithPattern();
 //        initRandom();
         setMinimumSize(dimension);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                 _timer.stop();
                 System.exit(0);
             }
 
         });
         final MouseInputAdapter l = new MouseInputAdapter() {
             @Override
             public void mousePressed(MouseEvent e) {
                 _moveAction.set(true);
                 _startPoint.set(new gameoflife.game.Point(e.getXOnScreen(), e.getYOnScreen()));
             }
 
             @Override
             public void mouseReleased(MouseEvent e) {
                 _moveAction.set(false);
             }
 
             @Override
             public void mouseMoved(MouseEvent e) {
                 mouseDragged(e);
             }
 
             @Override
             public void mouseDragged(MouseEvent e) {
                 synchronized (_currentGenerationMonitor) {
                     if (_moveAction.get()) {
                         final gameoflife.game.Point point = new gameoflife.game.Point(e.getXOnScreen(), e.getYOnScreen());
                         point.setLocation(
                                 point.x - _startPoint.get().x,
                                 point.y - _startPoint.get().y
                         );
                         final gameoflife.game.Point currentTopLeftCorner = _currentTopLeftCorner.get();
                         final int x = (int) (currentTopLeftCorner.getX() + point.getX());
                         final int y = (int) (currentTopLeftCorner.getY() + point.getY());
                         _currentTopLeftCorner.set(
                                 new gameoflife.game.Point(
                                         x,
                                         y)
                         );
                         _startPoint.set(new gameoflife.game.Point(e.getXOnScreen(), e.getYOnScreen()));
                     }
                 }
             }
 
             @Override
             public void mouseWheelMoved(MouseWheelEvent e) {
                 if (e.getWheelRotation() > 0 && _sizePerCell < 30) {
                     _sizePerCell++;
                 } else if (_sizePerCell > 1) {
                     _sizePerCell--;
                 }
             }
         };
         addMouseListener(l);
         addMouseMotionListener(l);
         addMouseWheelListener(l);
     }
 
 
     private void initWithPattern() {
         final InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("loggrow.lif");
         final Pattern pattern = new Pattern(new BufferedReader(new InputStreamReader(resourceAsStream)));
         for (int i = 0; i < pattern.getNumberOfCellBlocks(); ++i) {
             final CellBlock cellBlock = pattern.getCellBlock(i);
             for (gameoflife.game.Point livingCell : cellBlock.getLivingCells()) {
                 _currentGeneration.setAlive(
                         livingCell.x + (_width / 2) + cellBlock.getXOffset(),
                         livingCell.y + (_height / 2) + cellBlock.getYOffset()
                 );
             }
         }
     }
 
     private void initRandom() {
         for (int x = 0; x < _width; ++x) {
             for (int y = 0; y < _height; ++y) {
                 final double v = Math.random() * 2;
                 if (v > 1.7) {
                     _currentGeneration.setAlive(x, y);
                 }
             }
         }
     }
 
     private void start() {
         _timer = new Timer(1, new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 synchronized (_currentGenerationMonitor) {
                     final long start = System.currentTimeMillis();
                     _currentGeneration = _currentGeneration.next();
                     System.out.println("generation: " + _currentGeneration.generationNumber() + " with " + _currentGeneration.getLivingCells().size() + " living cells calculated in " + (System.currentTimeMillis() - start) + " ms");
                     _panel.repaint();
                     currentGeneration.setText("" + _currentGeneration.generationNumber());
                 }
             }
         });
         new Thread(new Runnable() {
             public void run() {
                 try {
                     Thread.sleep(3000L);
                 } catch (InterruptedException e) {
                     throw new RuntimeException(e);
                 }
                 _timer.start();
             }
         }).start();
     }
 
     private void createUIComponents() {
         _panel = new JPanel() {
             @Override
             public void paint(Graphics g) {
                 g.clearRect(0, 0, getWidth(), getHeight());
                 g.setColor(Color.WHITE);
                 g.fillRect(0, 0, getWidth(), getHeight());
                 g.setColor(Color.BLACK);
                 final Set<gameoflife.game.Point> livingCells = _currentGeneration.getLivingCells();
                 final int minX = _currentTopLeftCorner.get().x * -1;
                 final int maxX = minX + getWidth() / _sizePerCell;
                 final int minY = _currentTopLeftCorner.get().y * -1;
                 final int maxY = minY + getHeight() / _sizePerCell;
                 for (Point livingCell : livingCells) {
                     if (livingCell.x > minX && livingCell.x < maxX && livingCell.y > minY && livingCell.y < maxY) {
                         g.fillRect(
                                 (livingCell.x * _sizePerCell) + (_currentTopLeftCorner.get().x * _sizePerCell),
                                (livingCell.y * _sizePerCell) + (_currentTopLeftCorner.get().y * _sizePerCell),
                                 _sizePerCell,
                                 _sizePerCell);
                     }
                 }
             }
         };
         _panel.setVisible(true);
     }
 }
