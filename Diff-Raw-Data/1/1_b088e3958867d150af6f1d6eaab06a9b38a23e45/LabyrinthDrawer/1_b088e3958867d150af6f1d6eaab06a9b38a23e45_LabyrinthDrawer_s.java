 package ch.zhaw.labyrinth.gui;
 
 import ch.zhaw.labyrinth.utils.Cell;
 import ch.zhaw.labyrinth.utils.Coordinate;
 import ch.zhaw.labyrinth.utils.Labyrinth;
 import javax.swing.*;
 import java.awt.*;
 import java.util.HashMap;
 
 
 public class LabyrinthDrawer extends JPanel implements Runnable {
    
 	// serial version UID
 	private static final long serialVersionUID = 623347107962887545L;
 	
 	// instance var
 	private JPanel canvas;
     private Labyrinth labyrinth;
     private int zoom;
     private boolean fast;
     private Cell curCell;
     private Coordinate curCoordinate;
     private String mode;
     
     // construktor
     public LabyrinthDrawer(Labyrinth labyrinth, boolean fast, int zoom, String mode) {
         this.labyrinth = labyrinth;
         this.zoom = zoom;
         this.fast = fast;
         this.mode = mode;
         
         buildFrame();
     }
     
     
     private void buildFrame(){
     	
     	// create the frame
     	JFrame frame = new JFrame("Labyrinth Solver");   
     	
         // create the JPanel
         canvas = new JPanel();
         canvas.setBorder(BorderFactory.createEmptyBorder(1000, 1000, 1000, 1000));
         canvas.setPreferredSize(new Dimension(labyrinth.getDimension()*zoom, labyrinth.getDimension()*zoom));
         
         // add the canvas to the frame and make it visible
         frame.add(canvas);
         frame.pack();
         frame.setLocationRelativeTo(null);
         frame.setVisible(true); 
         
     }
 
     @Override
 	protected void paintComponent(Graphics g){
     	super.paintComponent(g);
 
         if(mode.equals("create")) {
             if (!curCell.isPath()){
                 g.setColor(Color.black);
                 g.fillRect(curCoordinate.getX()*zoom, curCoordinate.getY()*zoom, 1*zoom, 1*zoom );
             }
         } else if (mode.equals("solve")) {
             if (curCell.isPath()){
                 g.setColor(Color.green);
                 g.fillRect(curCoordinate.getX()*zoom, curCoordinate.getY()*zoom, 1*zoom, 1*zoom );
             }
         }
       
     }
     
     @Override
     public Dimension getPreferredSize() {
         return new Dimension(labyrinth.getDimension()*zoom, labyrinth.getDimension()*zoom);
     }
     
     @Override
     public void run() {
    
         HashMap<Coordinate, Cell> maze = labyrinth.getMaze();
         
         for(int i=0; i< labyrinth.getDimension(); i++) {
             for (int j=0; j< labyrinth.getDimension(); j++) {
             	
             	curCoordinate = new Coordinate(i,j);
                 curCell = maze.get(curCoordinate);
 
                 if(!(curCell == null)) {
                     paintComponent(canvas.getGraphics());
                     if(!fast) {
                         try {
                             Thread.sleep(5);
                         } catch(InterruptedException ex) {
                             Thread.currentThread().interrupt();
                         }
                     }
                 }
             }
         } 
     }
 
     public String getMode() {
         return mode;
     }
 
     public void setMode(String mode) {
         this.mode = mode;
     }
 
     public Labyrinth getLabyrinth() {
         return labyrinth;
     }
 
     public void setLabyrinth(Labyrinth labyrinth) {
         this.labyrinth = labyrinth;
     }
 }
