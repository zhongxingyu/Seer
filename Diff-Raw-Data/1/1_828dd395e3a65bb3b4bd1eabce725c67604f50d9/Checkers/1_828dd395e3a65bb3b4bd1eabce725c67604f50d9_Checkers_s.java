 package Checkers;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 public class Checkers extends JPanel
 {
     /**
     *
     */
     private static final long serialVersionUID = -6799064229355729609L;
     private static int x;//x coordinate of piece selected
     private static int y;//y coordinate of piece selected
     private static int x1;
     private static int y1;
     private static int[][] pieces=new int[][]{
     {1,0,0,0,0,0,-1,0},//pieces are on top
     {0,1,0,0,0,0,0,-1},
     {1,0,0,0,0,0,-1,0},
     {0,1,0,0,0,0,0,-1},
     {1,0,0,0,0,0,-1,0},
     {0,1,0,0,0,0,0,-1},
     {1,0,0,0,0,0,-1,0},
     {0,1,0,0,0,0,0,-1}};
     static int selected;
     static int newSelected;
     static int move=0;
     
     public void paintComponent(Graphics g)
     {
     	
         int height=getHeight();
         int width=getWidth();
         
         //horizontal lines
         g.drawLine(0, height/8, width, height/8);
         g.drawLine(0, height/4, width,height/4);
         g.drawLine(0, (3*height)/8, width, (3*height)/8);
         g.drawLine(0, height/2, width, height/2);
         g.drawLine(0, (5*height)/8, width, (5*height)/8);
         g.drawLine(0, (3*height)/4, width, (3*height)/4);
         g.drawLine(0, (7*height)/8, width, (7*height)/8);
         g.drawLine(0, height, width, height);
         //vertical lines
         g.drawLine(width/8,0,width/8,height);
         g.drawLine(width/4,0,width/4,height);
         g.drawLine((3*width)/8,0,(3*width)/8,height);
         g.drawLine(width/2,0,width/2,height);
         g.drawLine((5*width)/8,0,(5*width)/8,height);
         g.drawLine((3*width)/4,0,(3*width)/4,height);
         g.drawLine((7*width)/8,0,(7*width)/8,height);
         g.drawLine(width,0,width,height);
         
     }
     
     public Checkers()
     {
         addMouseListener(new MouseAdapter()
         {
             public void mousePressed(MouseEvent e)
             {
                 x=(int)e.getX()/74;//divide by 74 so the position of the cell can be used in a
                 y=(int)e.getY()/74;//2D array
                 
                 if(move==0)//this is when a new piece is selected to move
                 {
                 	x1=x;
                 	y1=y;
                 	selected=pieces[x1][y1];
                     if(selected==0)
                     {
                         JOptionPane.showMessageDialog(null, "Please select a piece");
                         System.out.println("seleceted:"+pieces[x1][y1]);
                     }
                     else
                     {
                         System.out.println("seleceted:"+pieces[x1][y1]);
                         System.out.println("x: "+x1);
                         System.out.println("y: "+y1);
                         move=1;
                     }       
                 }
                 else
                 {
 	                newSelected=pieces[x][y];
 	                if(newSelected==1||newSelected==-1)
 	                {
 	                	System.out.println(selected);
 	                	JOptionPane.showMessageDialog(null,"There is already a piece there");
 	                	System.out.println("There is already a piece there");
 	                
 	                }
 	                else
 	                {
 	                	if(check()==true)
 	                	{
 		                	pieces[x][y]=selected;
 		 	                System.out.println("new location: "+pieces[x][y]);
 		 	                pieces[x1][y1]=0;
 		 	                move=0;
 	                	}
 	                	else
 	                	{
 	                		JOptionPane.showMessageDialog(null, "Not a legal move");
 	                		move=0;
 	                	}
 	                }
                     
                 }
                 
             }
         });
     }
     
     public static boolean check()
     {
     	System.out.println("x: "+x);
     	System.out.println("y: "+y);
     	
     	if(x==x1+1&&y==y1+1)
     		return true;
     	if(x==x1-1&&y==y1+1)
     		return true;
     	else
     		return false;
    
     
     }
    
 
 
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
