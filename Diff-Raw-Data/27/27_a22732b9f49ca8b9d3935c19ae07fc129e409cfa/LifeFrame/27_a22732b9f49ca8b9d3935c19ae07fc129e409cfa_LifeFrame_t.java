 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileNotFoundException;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JToggleButton;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import logic.LifeBoard;
 import util.LifeBoardSaveAndLoad;
 
 /**
  * This class is in charge of managing a LifeDisplay. It animates it and lets the user interact with
  * the board.
  * 
  * @author Justin
  * 
  */
 public class LifeFrame extends JFrame {
     private static final long serialVersionUID = -83611558260904001L;
     private LifeDisplay board;
     private boolean go;
     private Thread runner;
    private long msPerFrame = 60;
 
     public LifeFrame(LifeDisplay board) {
         setLayout(new BorderLayout());
         setDefaultCloseOperation(EXIT_ON_CLOSE);
         this.board = board;
         add(board, BorderLayout.CENTER);
         JPanel controls = buildControlPanel();
         add(controls, BorderLayout.SOUTH);
         pack();
         setVisible(true);
         runner = new Thread() {
             @Override
             public void run() {
                 while (true) {
                     long startTime = System.currentTimeMillis();
                     if (go) {
                         try {
                             SwingUtilities.invokeAndWait(new Runnable() {
                                 @Override
                                 public void run() {
                                     LifeFrame.this.board.update();
                                 }
                             });
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                     }
                     long endTime = System.currentTimeMillis();
                     long elapsed = endTime - startTime;
                     long remain = msPerFrame - elapsed;
                     // long timeTaken = Math.max(msPerFrame, elapsed);
                     if (remain > 0) {
                         try {
                             Thread.sleep(remain);
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                         }
                     }
                 }
             }
         };
         runner.start();
     }
 
     private final JPanel buildControlPanel() {
         final JToggleButton tgl = new JToggleButton("Start/Stop");
         tgl.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 go = !go;
                 LifeFrame.this.board.setGrids(!go && board.sqWidth() != 1);
                 repaint();
             }
         });
         final JSlider squareSize = new JSlider(1, 20, 10);
         squareSize.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 LifeFrame.this.board.setGrids(!go && board.sqWidth() != 1);
                 board.sqWidth(squareSize.getValue());
                 board.repaint();
             }
         });
         final JButton save = new JButton("Save");
         save.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 JFileChooser chooser = new JFileChooser();
                 int choice = chooser.showSaveDialog(LifeFrame.this);
                 if (choice == JFileChooser.APPROVE_OPTION) {
                     LifeBoardSaveAndLoad saver = new LifeBoardSaveAndLoad(board.getLogic());
                     try {
                         saver.saveToFile(chooser.getSelectedFile().getAbsolutePath());
                     } catch (FileNotFoundException e1) {
                         e1.printStackTrace();
                     }
                 }
             }
         });
         final JButton load = new JButton("Load");
         load.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 JFileChooser chooser = new JFileChooser();
                 int choice = chooser.showOpenDialog(LifeFrame.this);
                 if (choice == JFileChooser.APPROVE_OPTION) {
                     try {
                         LifeBoard board = LifeBoardSaveAndLoad.load(chooser.getSelectedFile()
                                 .getAbsolutePath());
                         LifeFrame.this.board.applyBoard(board);
                         repaint();
                     } catch (FileNotFoundException e1) {
                         e1.printStackTrace();
                     }
                 }
             }
         });
         JPanel control = new JPanel();
         control.add(tgl);
         control.add(squareSize);
         control.add(load);
         control.add(save);
         return control;
     }
 
     public void pause() {
         go = false;
     }
 
     public void go() {
         go = true;
     }
 }
