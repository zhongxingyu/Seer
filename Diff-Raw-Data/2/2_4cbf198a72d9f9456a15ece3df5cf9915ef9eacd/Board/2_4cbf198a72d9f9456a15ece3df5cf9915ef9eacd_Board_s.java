 package com.github.abalone.view;
 
 import com.github.abalone.Config;
 import com.github.abalone.elements.Ball;
 import com.github.abalone.util.Coords;
 import com.kitfox.svg.app.beans.SVGIcon;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.net.URISyntaxException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JPanel;
 
 /**
  *
  * @author sardemff7
  */
 class Board extends JPanel {
     private final SVGIcon board;
     private final Window window;
     private Double boardScale = -1.0;
     private final SVGIcon whiteBall;
     private final SVGIcon blackBall;
     private Integer origX = 0;
     private Integer origY = 0;
 
     public Board(Window window)
     {
         this.window = window;
         this.board = new SVGIcon();
         this.board.setScaleToFit(true);
         this.board.setAntiAlias(true);
         this.whiteBall = new SVGIcon();
         this.whiteBall.setScaleToFit(true);
         this.whiteBall.setAntiAlias(true);
         this.blackBall = new SVGIcon();
         this.blackBall.setScaleToFit(true);
         this.blackBall.setAntiAlias(true);
         try
         {
             this.board.setSvgURI(getClass().getResource("game/" + Config.get("theme") + "/board.svg").toURI());
             this.whiteBall.setSvgURI(getClass().getResource("game/" + Config.get("theme") + "/white-ball.svg").toURI());
             this.blackBall.setSvgURI(getClass().getResource("game/" + Config.get("theme") + "/black-ball.svg").toURI());
         }
         catch (URISyntaxException ex)
         {
             Logger.getLogger(Board.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     private void computeBoardScale()
     {
         Dimension target = new Dimension(this.board.getIconWidth(), this.board.getIconHeight());
         Dimension container = this.getSize();
         Double s = 1.0;
         if ( ( target.width <= container.width ) && ( target.height <= container.height ) )
             {} // It already fits in container
         else if ( (target.width > container.width) && (target.height <= container.height) )
             s = (double)container.width / (double)target.width; // It does not fit horizontaly
         else if ( (target.width <= container.width) && (target.height > container.height) )
             s = (double)container.height / (double)target.height; // It does not fit verticaly
         else if(target.width == target.height)
         {
             if(container.width <= container.height)
                 s = (double)container.width / (double)target.width;
             else
                 s = (double)container.height / (double)target.height;
         }
         
         Dimension scaled = new Dimension((int)(target.width * s), (int)(target.height * s));
         this.board.setPreferredSize(scaled);
 
         this.boardScale = s;
         this.origX = (container.width - scaled.width) / 2;
         this.origY = (container.height - scaled.height) / 2;
 
         Dimension ballSize = new Dimension((int)(100.0 * s), (int)(100.0 * s));
         this.whiteBall.setPreferredSize(ballSize);
         this.blackBall.setPreferredSize(ballSize);
     }
     
     @Override
     public void paintComponent(Graphics g)
     {
         this.computeBoardScale();
         this.board.paintIcon(this, g, this.origX, this.origY);
         this.paintBalls(g);
     }
 
     private void paintBalls(Graphics g)
     {
         Double ballSize = (double)this.whiteBall.getIconHeight() * this.boardScale;
         
         Set<Ball> balls = com.github.abalone.elements.Board.getInstance().getBalls();
         Iterator<Ball> i = balls.iterator();
         while ( i.hasNext() )
         {
             Ball b = i.next();
             Coords coords = b.getCoords();
             Integer r = coords.getRow();
             Integer c = coords.getCol();
            Double bX = 65.0 - (ballSize / 2.0) + Math.abs(r) * 65.0+ c * 130.0;
             Double bY = (700.0 + 110.0 * r);
             Integer x = this.origX + (int)(bX * this.boardScale);
             Integer y = this.origY + (int)(bY * this.boardScale);
             switch ( b.getColor() )
             {
                 case WHITE:
                     this.whiteBall.paintIcon(this, g, x, y);
                 break;
                 case BLACK:
                     this.blackBall.paintIcon(this, g, x, y);
                 break;
 
             }
         }
     }
 }
