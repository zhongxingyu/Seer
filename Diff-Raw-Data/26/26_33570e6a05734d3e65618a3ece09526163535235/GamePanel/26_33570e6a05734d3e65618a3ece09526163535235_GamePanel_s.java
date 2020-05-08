 package ihm;
 
 import java.awt.*;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.PathIterator;
 import java.awt.geom.Rectangle2D;
 
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 
 import java.util.*;
 
 
 
 import world.SnakeController;
 import world.WorldModel;
 import world.GridPoint;
 
 public class GamePanel extends JPanel {
   private Image img;
   
   boolean runSnake = true;
   GeneralPath path = new GeneralPath();
   int width        = 400;
   int height       = 400;
   GridPoint head;
   WorldModel wm;
   
   public GamePanel(WorldModel wm) {
     
     //setPreferredSize(new Dimension(400, 400));
     
     setBackground(Color.BLACK);
     img = new ImageIcon("background.jpg").getImage();
     
     this.wm = wm;
     wm.addObserver(new GamePanelObserver(this));
     new SnakeController(wm);
   }
   private GridPoint convert(GridPoint p){
     return new GridPoint(height/WorldModel.GRID_WIDTH*p.getX(), width/WorldModel.GRID_HEIGHT*p.getY());
   }
   public void updateSize(){
     Dimension d = getSize();
     if (d.width<d.height){
       setPreferredSize(new Dimension(d.width, d.width));
       width  = d.width;
       height = d.width;
     }else{
       setPreferredSize(new Dimension(d.height, d.height));
       width  = d.height;
       height = d.height;
     }
   }
   public void updatePath(Queue<GridPoint> gpq){
     path = new GeneralPath();
     Iterator<GridPoint> itr = gpq.iterator();
     GridPoint point = convert((GridPoint)itr.next());
     head = point;
     double epsilon = (width/WorldModel.GRID_WIDTH)/2;
     
     path.moveTo(point.getX()+epsilon, point.getY()+epsilon);
     
     while(itr.hasNext()) {
       point = convert((GridPoint)itr.next());
       path.lineTo(point.getX()+epsilon, point.getY()+epsilon);
     }
   }
   protected void paintComponent(Graphics g) {
     super.paintComponent(g);
     Graphics2D g2 = (Graphics2D) g;
     
     g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
         RenderingHints.VALUE_ANTIALIAS_ON);
     //g2.translate(50, getHeight() - 50); // Move the origin to the lower left
     //g2.scale(1.0, -1.0); // Flip the sign of the coordinate system
 
     Rectangle2D box = new Rectangle2D.Double(0, 0, width, height);
     g2.setPaint(Color.WHITE);
     g2.fill(box);
     g.drawImage(img, 0, 0, width, height, null);
 
     g2.setPaint(Color.BLUE);
     g2.setStroke(new BasicStroke(width/WorldModel.GRID_WIDTH));
     g2.draw(path);
     
     
     /////////////////
    {
     GridPoint point = head;
     double epsilon = (width/WorldModel.GRID_WIDTH)/7;
     Rectangle2D eye1 = new Rectangle2D.Double(point.getX()+epsilon, point.getY()+epsilon, epsilon, epsilon);
     g2.setPaint(Color.BLACK);
     Rectangle2D eye2 = new Rectangle2D.Double(point.getX()-epsilon*2+(width/WorldModel.GRID_WIDTH), point.getY()+epsilon, epsilon, epsilon);
     g2.fill(eye1);
     g2.fill(eye2);
     
     g2.setPaint(Color.BLACK);
     Rectangle2D nose = new Rectangle2D.Double(point.getX()+3*epsilon, point.getY()+epsilon*3, epsilon, epsilon);
     g2.fill(nose);
     
     g2.setPaint(Color.BLACK);
     Rectangle2D mouth = new Rectangle2D.Double(point.getX()+epsilon, point.getY()+epsilon*5, epsilon*6, epsilon);
     g2.fill(mouth);
    }
     //////////////////////////
     
     
     
     
     GridPoint insect = convert(wm.getInsect());
     Rectangle2D ins = new Rectangle2D.Double(insect.getX(), insect.getY(), width/WorldModel.GRID_WIDTH, height/WorldModel.GRID_HEIGHT);
     g2.setPaint(Color.RED);
     g2.fill(ins);
     
     double epsilon = (width/WorldModel.GRID_WIDTH)/7;
     Rectangle2D eye1 = new Rectangle2D.Double(insect.getX()+epsilon, insect.getY()+epsilon, epsilon, epsilon);
     g2.setPaint(Color.BLACK);
     Rectangle2D eye2 = new Rectangle2D.Double(insect.getX()-epsilon*2+(width/WorldModel.GRID_WIDTH), insect.getY()+epsilon, epsilon, epsilon);
     g2.fill(eye1);
     g2.fill(eye2);
     
     g2.setPaint(Color.BLACK);
     Rectangle2D nose = new Rectangle2D.Double(insect.getX()+3*epsilon, insect.getY()+epsilon*3, epsilon, epsilon);
     g2.fill(nose);
     
     g2.setPaint(Color.BLACK);
     Rectangle2D mouth = new Rectangle2D.Double(insect.getX()+epsilon, insect.getY()+epsilon*5, epsilon*6, epsilon);
     g2.fill(mouth);
     
   }
 }
