 package de.graphics.uni_konstanz.wordle;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.GraphicsEnvironment;
import java.awt.Panel;
 import java.awt.event.ActionEvent;
 import java.awt.geom.Rectangle2D;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.AbstractAction;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.WindowConstants;
 
 public class Main {
 
   private final JFrame fenster;
 
   public Main() {
     fenster = new JFrame("Fenster");
    fenster.setSize(300, 300);
    fenster.setLocation(300, 300);
    fenster.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    fenster.setVisible(true);
 
     final Canvas canvas = new Canvas(new WordlePainter() {
 
       @Override
       public void paint(final Graphics2D g) {
         g.setColor(Color.RED);
         g.fill(new Rectangle2D.Double(0, 0, 100, 100));
       }
 
     });
 
    final BorderLayout borderLayout = new BorderLayout();
     final JPanel guiPanel = new JPanel();
     guiPanel.setLayout(new BoxLayout(guiPanel, BoxLayout.Y_AXIS));
     guiPanel.add(new JButton("yes"));
     guiPanel.add(new JButton("no"));
     guiPanel.add(new JButton(new AbstractAction("Save SVG...") {
 
       private static final long serialVersionUID = -9119742082960796042L;
 
       @Override
       public void actionPerformed(final ActionEvent ae) {
         final BatikSVG svg = new BatikSVG();
         final File file = svg.saveSVGDialog(canvas);
         if(file == null) {
           return;
         }
         final Graphics2D g = svg.getGraphics("WordleIntern");
         canvas.paint(g);
         g.dispose();
         try {
           svg.write(file, g);
         } catch(final IOException e) {
           e.printStackTrace();
         }
       }
 
     }));
     guiPanel.setMinimumSize(new Dimension(200, 0));
    final Panel panel = new Panel(borderLayout);
    panel.add(guiPanel, BorderLayout.WEST);

    panel.add(canvas);

    fenster.add(panel);
 
     fenster.pack();
 
     fenster.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
     fenster.setLocationRelativeTo(null);
   }
 
   public static void main(final String[] args) {
 
     final Main main = new Main();
     main.fenster.setVisible(true);
 
     final String fonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment()
         .getAvailableFontFamilyNames();
 
     for(int i = 0; i < fonts.length; i++) {
       System.out.println(fonts[i]);
     }
   }
 
 }
