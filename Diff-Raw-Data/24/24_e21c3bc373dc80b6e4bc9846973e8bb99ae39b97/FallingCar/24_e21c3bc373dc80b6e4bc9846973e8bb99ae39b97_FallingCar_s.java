 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 @SuppressWarnings("serial")
 public class FallingCar extends JFrame {
 
     private final Wheel wheel1;
     private final Grass grass;
     private final Wheel wheel2;
     private int centerX;
     private int centerY;
 
     public FallingCar() {
         super("Physics test");
         setContentPane(new ViewPort());
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setSize(1600, 1200);
         setVisible(true);
         requestFocus();
         addKeyListener(new KeyListener() {
             
             @Override
             public void keyTyped(KeyEvent e) {
             }
             
             @Override
             public void keyReleased(KeyEvent e) {
             }
             
             @Override
             public void keyPressed(KeyEvent e) {
                 wheel1.dy = -20;
             }
         });
 
         wheel1 = new Wheel();
         wheel2 = new Wheel();
         wheel1.x += 200;
         wheel1.y = 300;
         wheel2.y = 300;
         grass = new Grass();
 
         new Timer(1000 / 30, new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 updateWorld();
                 repaint();
             }
         }).start();
     }
 
     protected void updateWorld() {
         restrictWheelDistance(wheel1, wheel2);
         wheel1.update();
         wheel2.update();
 
         if (wheel1.dy > 0 && wheel1.y > grass.getGrassHeight(wheel1.x)) {
             wheel1.dy = (int) -(wheel1.dy * 0.5);
             wheel1.y = grass.getGrassHeight(wheel1.x);
         }
 
         if (wheel2.dy > 0 && wheel2.y > grass.getGrassHeight(wheel2.x)) {
             wheel2.dy = (int) -(wheel2.dy * 0.5);
             wheel2.y = grass.getGrassHeight(wheel2.x);
         }
 
     }
 
     private void restrictWheelDistance(Wheel wheel1, Wheel wheel2) {
 
         centerX = (wheel1.x + wheel2.x) / 2;
         centerY = (wheel1.y + wheel2.y) / 2;
 
         double carLength = 200;
         double distance = Math.sqrt((wheel1.x - wheel2.x) * (wheel1.x - wheel2.x) + (wheel1.y - wheel2.y) * (wheel1.y - wheel2.y));
 
        double delta = Math.signum(wheel1.x - wheel2.x) * (distance - carLength);
         System.out.println(delta);
        wheel1.x -= delta / 2;
        wheel2.x += delta / 2;
     }
 
     class ViewPort extends JPanel {
         @Override
         public void paintComponent(Graphics g) {
             grass.draw(g);
 
             g.setColor(Color.RED);
             wheel1.draw(g);
             g.setColor(Color.BLUE);
             wheel2.draw(g);
             
             g.setColor(Color.BLACK);
             g.fillOval(centerX, centerY, 5, 5);
             g.fillOval(wheel1.x, wheel1.y, 5, 5);
             g.fillOval(wheel2.x, wheel2.y, 5, 5);
         }
     }
 
     public static void main(String args[]) {
         new FallingCar();
     }
 }
