 package GUI;
 
 import Source.CObstacle;
 import Source.CWalker;
 import Source.CWorld;
 import Util.CEdge;
 import Util.CPosition;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.text.DecimalFormat;
 import java.util.Vector;
 
 /**
  * Copyright 2013 blastbeat syndicate gmbh
  * Author: Roger Jaggi <roger.jaggi@blastbeatsyndicate.com>
  * Date: 04.10.13
  * Time: 10:30
  */
 public class SimulationPanel extends JPanel implements ActionListener, KeyListener {
 
     /**
      * Holds the informaticon if the simulation is running
      */
     //protected boolean isRunning = false;
     protected boolean showGraph = true;
     protected boolean showGrid = false;
     protected boolean showWalkerCoordinates = false;
     protected boolean showWalkerIDs = false;
 
     protected Timer timer = null;
 
     protected Vector<File> files = new Vector<File>();
 
     protected CWorld simulationWorld = null;
 
     public SimulationPanel() {
         super();
 
         // We want to positionate our Elements with x und y coordinates
         setLayout(null);
 
         reloadConfigfiles();
 
         this.timer = new Timer(20, this);
 
         this.repaint();
     }
 
     public void reloadConfigfiles() {
         File dir = new File("./XML");
 
         FilenameFilter filter = new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 return name.endsWith(".xml");
             }
         };
 
         int i = 0;
         for(File file : dir.listFiles(filter)) {
             i += 1;
             if(i >= 10) {
                 break;
             }
 
             this.files.add(file);
         }
     }
 
     public void setupWorld(File configFile) {
         if(configFile != null ) {
             this.simulationWorld = new CWorld();
 
             this.simulationWorld.loadConfig(configFile);
 
             this.setBounds(0,0, this.simulationWorld.getWorldWidth(), this.simulationWorld.getWorldHeight());
 
             this.simulationWorld.buildGraph();
 
             this.timer.start();
         }
         else {
             this.timer.stop();
             this.simulationWorld = null;
             this.setBounds(0,0, Application.INSTANCE.getWidth(),  Application.INSTANCE.getHeight());
 
             this.repaint();
         }
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         this.runOneStep();
     }
 
     @Override
     public void keyTyped(KeyEvent e) {
         // currently do nothing
     }
 
     /*
      * (non-Javadoc)
      * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
      */
     public void keyPressed(KeyEvent e) {
 
         File fileToLoad = null;
         switch(e.getKeyCode()) {
             case KeyEvent.VK_0:
                 final JFileChooser fc = new JFileChooser();
                 fc.showOpenDialog(this);
                 fileToLoad = fc.getSelectedFile();
                 break;
 
             case KeyEvent.VK_1:
             case KeyEvent.VK_2:
             case KeyEvent.VK_3:
             case KeyEvent.VK_4:
             case KeyEvent.VK_5:
             case KeyEvent.VK_6:
             case KeyEvent.VK_7:
             case KeyEvent.VK_8:
             case KeyEvent.VK_9:
                 try {
                     fileToLoad = this.files.elementAt(e.getKeyCode() - KeyEvent.VK_1);
                 }
                 catch(ArrayIndexOutOfBoundsException ex) {
                     //System.out.println(ex);
                 }
                 break;
 
             case KeyEvent.VK_P:
                 this.toggleRunningState();
                 break;
 
             case KeyEvent.VK_RIGHT:
                 this.runOneStep();
                 break;
 
             case KeyEvent.VK_G:
                 this.toggleShowGraph();
                 break;
 
             case KeyEvent.VK_H:
                 this.toggleShowGrid();
                 break;
 
             case KeyEvent.VK_J:
                 this.toggleShowWalkerCoordinates();
                 break;
             case KeyEvent.VK_I:
                 this.toggleShowWalkerIDs();
                 break;
 
             case KeyEvent.VK_ESCAPE:
                 if(this.simulationWorld == null) {
                     System.exit(0);
                 }
                 else {
                     this.setupWorld(null);
                 }
                 break;
         }
 
         if(fileToLoad != null ) {
             this.setupWorld(fileToLoad);
         }
     }
 
     @Override
     public void keyReleased(KeyEvent e) {
         // currently do nothing
     }
 
     /**
      * paintComponent is called, every time the window gets drawed by the application
      *
      * @param g contains the reference to the Java2D graphics object
      */
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         doDrawing((Graphics2D)g);
     }
 
     /**
      * doDrawing does the actual drawing
      * @param g2d contains the reference to the Java2D graphics object
      */
     private void doDrawing(Graphics2D g2d) {
 
         //Customize the main world
         g2d.setBackground(Color.BLACK);
         g2d.fillRect(getX(), getY(), getWidth(), getHeight());
 
         if(simulationWorld == null) {
 
             g2d.setColor(Color.WHITE);
             g2d.drawString("Bitte wähle eine Welt, die geladen werden soll:", 100, 50);
             Integer i = 0;
             for(File file : this.files) {
                 i += 1;
                 g2d.drawString(i.toString() + " - " + file.getName(), 100, 50 + (i * 30));
                 if(i >= 9) {
                     break;
                 }
             }
 
             i += 1;
             g2d.drawString("0 - XML-Datei selber wählen ...", 100, 50 + (i * 30));
 
         }
         else {
            if(this.showGrid) {
 
                g2d.setColor(Color.LIGHT_GRAY);
 
                // draw vertical and horizontal lines
                int i = 1;
                int max = Math.max(this.getWidth(), this.getHeight());
                while(i < max) {
                    i += this.simulationWorld.getGridSize();
 
                    // the vertical line
                    g2d.drawLine(i, 0, i, this.getHeight());
 
                    // the horizontal line
                    g2d.drawLine(0, i, this.getWidth(), i);
                }
 
                int max_x = this.getWidth() / this.simulationWorld.getGridSize();
                int max_y = this.getHeight() / this.simulationWorld.getGridSize();
                for(int x = 0; x < max_x; x++) {
                    for(int y = 0; y < max_y; y++) {
                        if( this.simulationWorld.getGrid().hasCellObject(x,y) ) {
                            g2d.fillRect(x * this.simulationWorld.getGridSize() + 3, y * this.simulationWorld.getGridSize() + 3, this.simulationWorld.getGridSize() - 3, this.simulationWorld.getGridSize() - 3);
                        }
                    }
                }
            }
 
            //draw the obstacles
            g2d.setColor(Color.WHITE);
            for(CObstacle obstacle : simulationWorld.getObstacles()) {
                Vector<CPosition> positions = obstacle.getPositions();
                int[] xCoordinates = new int[positions.size()];
                int[] yCoordinates = new int[positions.size()];
 
                int i = 0;
                for(CPosition position : positions) {
                    xCoordinates[i] = position.getX().intValue();
                    yCoordinates[i] = position.getY().intValue();
                    i += 1;
                }
 
                g2d.fillPolygon(xCoordinates, yCoordinates, positions.size());
            }
 //               Vector<CPosition> positions2 = obstacle.getWaypoints();
 //               int[] xCoordinates2 = new int[positions.size()];
 //               int[] yCoordinates2 = new int[positions.size()];
 //
 //               int j = 0;
 //               for(CPosition position : positions2) {
 //                   xCoordinates2[j] = position.getX().intValue();
 //                   yCoordinates2[j] = position.getY().intValue();
 //                   j += 1;
 //               }
 //
 //               g.setColor(Color.GREEN);
 //               g.drawPolygon(xCoordinates2, yCoordinates2, positions2.size());
 
              if(this.showGraph) {
                  g2d.setColor(Color.GREEN);
                  for(CEdge edge : this.simulationWorld.getGraph().getEdges()) {
                      g2d.drawLine(edge.getSource().getX().intValue(), edge.getSource().getY().intValue(), edge.getDestination().getX().intValue(), edge.getDestination().getY().intValue());
                      //this.drawArrowLine(g2d, edge.getSource().getX().intValue(), edge.getSource().getY().intValue(), edge.getDestination().getX().intValue(), edge.getDestination().getY().intValue());
                  }
 
 //                 g2d.setColor(Color.RED);
 //                 for(CEdge edge : this.simulationWorld.getGraph().getTrashEdges()) {
 //                     g2d.drawLine(edge.getSource().getX().intValue(), edge.getSource().getY().intValue(), edge.getDestination().getX().intValue(), edge.getDestination().getY().intValue());
 //                     this.drawArrowLine((Graphics2D)g, edge.getSource().getX().intValue(), edge.getSource().getY().intValue(), edge.getDestination().getX().intValue(), edge.getDestination().getY().intValue());
 //                 }
              }
 
 
             for(CWalker walker : simulationWorld.getWalkers().values()) {
                 CPosition position = walker.getPosition();
 
                g2d.setColor(( walker.hasCollisions() ? Color.RED : Color.ORANGE) );
                 g2d.fillOval(((Double)(position.getX() - walker.getHalfWalkerSize())).intValue(),
                              ((Double)(position.getY() - walker.getHalfWalkerSize())).intValue(),
                              ((Double)(walker.getHalfWalkerSize() * 2)).intValue(),
                              ((Double)(walker.getHalfWalkerSize() * 2)).intValue());
 
 
                 // draw the target as x, because the X marks the point =)
                 g2d.setColor(Color.ORANGE);
                 int upperleftX = ((Double)(walker.getTarget().getX() - walker.getHalfWalkerSize())).intValue();
                 int upperleftY = ((Double)(walker.getTarget().getY() - walker.getHalfWalkerSize())).intValue();
                 int width = ((Double)(walker.getHalfWalkerSize() * 2)).intValue();
                 int height = ((Double)(walker.getHalfWalkerSize() * 2)).intValue();
                 g2d.drawLine(upperleftX, upperleftY, upperleftX + width, upperleftY + height);
                 g2d.drawLine(upperleftX + width, upperleftY, upperleftX, upperleftY + height);
 
                 if(this.showWalkerIDs) {
                     g2d.setColor(Color.BLACK);
                     g2d.setFont(new Font("Serif", Font.BOLD, 9));
                     g2d.drawString(walker.getId().toString(), position.getX().intValue() - (walker.getHalfWalkerSize().intValue() / 2), position.getY().intValue() + (walker.getHalfWalkerSize().intValue()));
                 }
 
                 if(this.showWalkerCoordinates) {
                     DecimalFormat df = new DecimalFormat("#.00");
                     g2d.setColor(Color.CYAN);
                     g2d.drawString("x" + df.format(walker.getPosition().getX()) + "/y" + df.format(walker.getPosition().getY()), position.getX().intValue() + width + 10, position.getY().intValue() );
 
                     //if(walker.getDesiredPath().size() > 0 ) {
                     //    g2d.drawString("x" + df.format(walker.getDesiredPath().getFirst().getX()) + "/y" + df.format(walker.getDesiredPath().getFirst().getY()), ((Double)(walker.getDesiredPath().getFirst().getX() + 100.0)).intValue(), walker.getDesiredPath().getFirst().getY().intValue());
                     //}
                 }
             }
         }
     }
 
     /*
     private void drawArrowLine(Graphics2D g2d, int x1, int y1, int x2, int y2) {
 
         AffineTransform tx = new AffineTransform();
         Line2D.Double line = new Line2D.Double(x1,y1,x2,y2);
 
         Polygon arrowHead = new Polygon();
         arrowHead.addPoint( 0,0);
         arrowHead.addPoint( -5, -10);
         arrowHead.addPoint( 5,-10);
 
         tx.setToIdentity();
         double angle = Math.atan2(line.y2-line.y1, line.x2-line.x1);
         tx.translate(line.x2, line.y2);
         tx.rotate((angle-Math.PI/2d));
 
         Graphics2D g = (Graphics2D) g2d.create();
         g.setTransform(tx);
         g.fill(arrowHead);
         g.dispose();
         //g.drawLine(x1,y1,x2,y2);
     }  */
 
     public void runOneStep() {
         if(this.simulationWorld != null) {
             this.simulationWorld.stepSimulation();
             this.repaint();
         }
     }
 
     /**
      * pauses/plays the simulation if a simulation is loaded
      */
     public void toggleRunningState() {
         if(this.simulationWorld != null) {
             if( this.timer.isRunning() ) {
                 this.timer.stop();
             }
             else {
                 this.timer.start();
             }
         }
     }
 
     public void toggleShowGraph() {
         if(this.simulationWorld != null) {
             this.showGraph = !this.showGraph;
 
             // if the timer is not running, fire a repaint event
             if(!this.timer.isRunning()) {
                 this.repaint();
             }
         }
     }
 
     public void toggleShowGrid() {
         if(this.simulationWorld != null) {
             this.showGrid = !this.showGrid;
 
             // if the timer is not running, fire a repaint event
             if(!this.timer.isRunning()) {
                 this.repaint();
             }
         }
     }
 
     public void toggleShowWalkerCoordinates() {
         if(this.simulationWorld != null) {
             this.showWalkerCoordinates = !this.showWalkerCoordinates;
 
             // if the timer is not running, fire a repaint event
             if(!this.timer.isRunning()) {
                 this.repaint();
             }
         }
     }
 
     public void toggleShowWalkerIDs() {
         if(this.simulationWorld != null) {
             this.showWalkerIDs = !this.showWalkerIDs;
 
             // if the timer is not running, fire a repaint event
             if(!this.timer.isRunning()) {
                 this.repaint();
             }
         }
     }
 }
