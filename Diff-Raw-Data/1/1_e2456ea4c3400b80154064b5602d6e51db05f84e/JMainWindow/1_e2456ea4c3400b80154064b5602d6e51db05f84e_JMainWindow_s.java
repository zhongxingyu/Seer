 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gui;
 
 import Mazes.Maze;
 import Mazes.Maze.MazeException;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.HeadlessException;
 import java.awt.LayoutManager;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.print.PrinterException;
 import java.awt.print.PrinterJob;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JButton;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingUtilities;
 
 /**
  *
  * @author seth
  */
 public class JMainWindow extends JFrame implements ActionListener {
 
     private JSpinner spColumns;
     private MazePanel pMaze;
     private JProgressBar pbGeneration;
     private JButton btnPrint;
     private JButton btnStart;
     private Maze maze;
 
     public JMainWindow() throws HeadlessException {
         super();
         this.center();
         this.init();
     }
 
     private void init() {
         this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 
         JPanel p = new JPanel(new MigLayout());
 
         pMaze = new MazePanel();
         JPanel pControls = new JPanel(new MigLayout());
         spColumns = new JSpinner();
         ((SpinnerNumberModel) (spColumns.getModel())).setMinimum(10);
         ((SpinnerNumberModel) (spColumns.getModel())).setMaximum(99);
         ((JFormattedTextField) ((JSpinner.NumberEditor) spColumns.getEditor()).getTextField()).setColumns(2);
         spColumns.setValue(85);
 
         pbGeneration = new JProgressBar();
         pbGeneration.setVisible(false);
 
         btnStart = new JButton("Generate");
         btnStart.addActionListener(this);
 
         btnPrint = new JButton("Print");
         btnPrint.addActionListener(this);
         btnPrint.setEnabled(false);
 
         p.add(pMaze, "w 80%, h 100%");
 
         pControls.add(new JLabel("Number of columns:"));
         pControls.add(spColumns, "wrap");
         pControls.add(btnStart, "span, growx, wrap");
         pControls.add(pbGeneration, "span, growx, wrap");
         pControls.add(btnPrint, "span, growx, wrap");
 
         p.add(pControls, "w 20%, h 100%");
 
         this.add(p);
         this.setVisible(true);
     }
 
     private void center() {
         Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
         this.setSize(3 * dScreen.width / 4, 3 * dScreen.height / 4);
         Dimension dThis = this.getSize();
         this.setLocation((dScreen.width - dThis.width) / 2, (dScreen.height - dThis.height) / 2);
     }
 
     private Runnable generateProgressBarUpdater(final int value) {
         return new Runnable() {
 
             @Override
             public void run() {
                 pbGeneration.setValue(value);
             }
         };
     }
 
     private void generateMaze() {
         pbGeneration.setVisible(true);
         btnPrint.setEnabled(false);
         btnStart.setEnabled(false);
         final int nMazeWidth = (Integer) spColumns.getValue();
         final int nMazeHeight = (int) (nMazeWidth / 1.4212);
         pbGeneration.setMaximum(nMazeWidth * nMazeHeight);
 
         final Maze.ProcessHandler handler = new Maze.ProcessHandler() {
 
             @Override
             public void generationUpdate(int nCellsGenerated, int nTotalCells) {
                 SwingUtilities.invokeLater(generateProgressBarUpdater(nCellsGenerated));
             }
         };
 
         new Thread(new Runnable() {
 
             @Override
             public void run() {
                 try {
                     maze = new Maze(nMazeWidth, nMazeHeight, handler);
                     final Maze m = maze;
                     SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             pMaze.setMaze(m);
                             pbGeneration.setVisible(false);
                             btnPrint.setEnabled(true);
                             btnStart.setEnabled(true);
                         }
                     });
 
                 } catch (MazeException ex) {
                     Logger.getLogger(JMainWindow.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }).start();
 
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() == btnStart) {
             generateMaze();
         } else if (e.getSource() == btnPrint) {
             PrinterJob job = PrinterJob.getPrinterJob();
             job.setPrintable(maze);
             boolean bPrint = job.printDialog();
             if (bPrint) {
                 try {
                     job.print();
                 } catch (PrinterException ex) {
                     Logger.getLogger(JMainWindow.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
     }
 }
