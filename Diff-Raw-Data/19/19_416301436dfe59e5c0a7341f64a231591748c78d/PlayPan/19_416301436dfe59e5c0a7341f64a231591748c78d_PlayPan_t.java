 /**
 * @author Alexandre Vienne
 * @version 0.1
 * PlayPan est le composant de l'ihm représentant le plateau de jeu, il y dessine
 * les différents composant du jeu.
 */
 
 package brickapp.ihm;
 
import javax.swing.*;
 import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.util.List;
 
 import brickapp.items.Ball;
 import brickapp.items.Bar;
 import brickapp.items.Brick;
 
 public class PlayPan extends JPanel implements MouseMotionListener {
 	private List<Brick> brickList;
     private Ball ball;
     private Bar bar;
    private Timer moveBall = new Timer(25,new ActionListener() {
        public void actionPerformed(ActionEvent actionEvent) {
           // moveBall();
            repaint();
        }
    });


 	/**
 	* Methode privée qui dessine les briques
 	*/
 	private void paintBrick(Graphics g){
         g.setColor(Color.white);
         g.fillRect(0,0,400,600);
 		for (Brick b : this.brickList){
             g.setColor(Color.red);
             g.fillRect(b.getPosition().getx(),b.getPosition().gety(),Brick.WIDTH,Brick.HEIGHT);
 		}
         g.setColor(Color.blue);
         g.fillOval(ball.getPosition().getx(), ball.getPosition().gety(), ball.getRayon(), ball.getRayon());
         g.setColor(Color.black);
         g.fillRoundRect(bar.getPosition().getx(),bar.getPosition().gety(),Brick.WIDTH,Brick.HEIGHT/2,10, 10);
 	}
 
 
 	/**
 	* Instancie le playground
 	* @param bl liste des briques du niveau
 	*/
 	public PlayPan(List<Brick> bl,Ball b, Bar bar){
 		brickList = bl;
         ball=b;
         this.bar=bar;
         addMouseMotionListener(this);
        moveBall.start();
 	}
 
 	/**
 	* Dessine le playground (appelé à chaque événement sur l'ihm)
 	*/
 	public void paintComponent(Graphics g){
 
 		paintField(g);
 	}
 
 	
 	/**
 	* Dessine les briques à l'écran
 	*/
 	public void paintField(Graphics g){
 		paintBrick(g);
 	}
 
     public void mouseDragged(MouseEvent mouseEvent) {
 
     }
 
     public void mouseMoved(MouseEvent mouseEvent) {
         this.bar.setBarPosition(mouseEvent.getX());
       // this.repaint();
 
 
     }
 }
