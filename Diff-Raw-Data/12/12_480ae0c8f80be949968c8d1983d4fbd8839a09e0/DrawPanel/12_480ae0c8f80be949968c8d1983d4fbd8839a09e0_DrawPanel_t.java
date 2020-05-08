 import javax.swing.JPanel;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Color;
 
 import java.awt.geom.GeneralPath;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 
 public class DrawPanel extends JPanel
 {
     DrawPanel()
     {
         //Cam pe aici vom avea arraylist-ul de puncte, la inceput fiind gol.
         //Odata ce utilizatorul clickeaza pe fereastra, vom adauga puncte in arraylist,
         //apoi vom gasi vreun mod sa desenam curba bezier.
 
        //Dupa ce omul nostru termina de clickat poligonul de control, va apasa o combinatie
        //(ma gandesc eu: CTRL+RCLICK) si va clicka poligonul de control al celei de-a doua
         //curbe bezier (apoi vom vedea cum le-om compara noi).
         this.addMouseListener(new MouseListener()
         {
             public void mouseClicked(MouseEvent e)
             {
                 System.out.println("Clicked!");
             }
             
             public void mouseExited(MouseEvent e)
             {
                 System.out.println("Exited Panel!");
             }
 
             public void mouseEntered(MouseEvent e)
             {
                 System.out.println("Entered Panel!");
             }
 
             public void mousePressed(MouseEvent e)
             {
                 Rectangle2D.Float pct = new Rectangle2D.Float(e.getX(), e.getY(), 0, 0);
                 Graphics2D g2d = (Graphics2D) getGraphics();
                 g2d.draw(pct);
                 //Deseneaza un dreptunghi, in punctu in care se afla mouse-ul cand
                 //s-a facut click, care are o lungime si latime de 0px, adica va desena 
                 //un punct.
 
                 //Pana acum se deseneaza un punct pentru orice click (fie el LEFT sau RIGHT sau
                 //chiar MIDDLE).
                 System.out.println("Pressed!");
             }
             
             public void mouseReleased(MouseEvent e)
             {
                 System.out.println("Released!");
             }
         });
 
         //Inca mai e mult de lucru la DrawPanel
     }
 
     protected void paintComponent(Graphics g)
     {
         //Functia asta se apeleaza automat de interpretor, oferindu-ti obiectul 'g'
         //care te va lasa sa desenezi ce vrei tu in Panel.
         //
         //Cand vom termina proiectul (doamne-ajuta!), nu vom mai avea nevoie
         //de functia asta, pentru ca, dupa cum ati observat in metoda mousePressed() de mai sus,
         //vom lua noi obiectul 'Graphics' folosind metoda getGraphics() si vom desena in
         //interiorul Event-Handler-ului. Functia asta am scris-o doar sa vad daca aveam
         //dreptate cu GeneralPath :).
 
         Graphics2D g2d;
         g2d = (Graphics2D) g;
 
         GeneralPath path = new GeneralPath();
         path.moveTo(0,0);
         path.curveTo(10, 15, 1, 80, 50, 50);
 
         g2d.draw(path);
     }
 }
