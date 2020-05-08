 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.util.ArrayList;
 
 public class Vertex implements Drawable {
 
     private int x, y, radius, ID;
 
     public int getID() {
         return ID;
     }
 
     public void setID(int ID) {
         this.ID = ID;
     }
 
     public int getX() {
         return x;
     }
 
     public int getY() {
         return y;
     }
 
     public void setX(int x) {
         this.x = x;
     }
 
     public void setY(int y) {
         this.y = y;
     }
 
     public int getRadius() {
         return radius;
     }
 
     ArrayList<Edge> edges;
     Program program;
 
     public Vertex(int x, int y) {
         this(x,y,0);
     }
 
     public Vertex(int x, int y, int ID) {
         edges = new ArrayList<Edge>();
         this.x = x;
         this.y = y;
         this.ID = ID;
         radius = 11;
     }
 
     void send(Message message) {
     	message.setEdge(edges.get(message.fromPort));
         message.toPort = message.edge.to.edges
                 .indexOf(message.edge.oppositeEdge);
         MessageQueue.getInstance().pushMessage(message);
     }
 
    void recieve(Message message) {
        program.recieve(message);
     }
 
     @Override
     public void draw(Graphics g) {
         g.setColor(new Color(0, 0, 0));
         g.fillOval(x - radius, y - radius, 2 * radius, 2 * radius);
         g.setColor(new Color(0, 255, 0));
         g.fillOval(x - radius + 1, y - radius + 1, 2 * radius - 2,
                 2 * radius - 2);
         g.setFont(new Font(Font.MONOSPACED, Font.ITALIC, 14));
         g.setColor(new Color(0, 0, 0));
         if (ID < 10) {
             g.drawString(((Integer) ID).toString(), x - radius / 2 + 1, y
                     + radius / 2);
         } else if (ID < 100) {
             g.drawString(((Integer) ID).toString(), x - radius / 2 - 3, y
                     + radius / 2);
         } else {
             g.drawString("V", x - radius / 2 + 1, y + radius / 2);
         }
     }
 
     public boolean isOnPoint(int x1, int y1) {
         return isNearPoint(x1, y1, 0);
     }
 
     public boolean isNearPoint(int x1, int y1, int distance) {
         return (x1 - x) * (x1 - x) + (y1 - y) * (y1 - y) < (radius + distance)
                 * (radius + distance);
     }
 
     public void repaint(Canvas canvas) {
         canvas.repaint(x - 2 * radius, x + 2 * radius, 4 * radius, 4 * radius);
     }
 
 }
