 package thesis.honeycomb.test;
 
 import com.trolltech.qt.gui.QPainter.RenderHint;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.util.Vector;
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.border.BevelBorder;
 
 /**
  * Created on 2008-11-25, 11:32:47
  * @author vara
  */
 
 public class HoneyCombCanvas extends JComponent implements MouseListener,
                                                              MouseMotionListener{
     private HoneyCombVector structure;
     private BufferedImage image = null;
     private HoneyComb clickedArea=null;
 
     public HoneyCombCanvas(Dimension size){
 
         setBounds(0,0,size.width,size.height);
         setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED,
                                                   Color.BLUE, Color.DARK_GRAY));
         addMouseListener(this);
         addMouseMotionListener(this);
         init();
     }
 
     private void init(){
 
         HoneyComb.Verbose.setEnable(false);
         HoneyComb.setBase(36);
         
         structure = new HoneyCombVector();
     }
 
     public void createStructureHoneyComb(){
 
         double x = getWidth()/2;
         double y = getHeight()/2;        
 
         HoneyComb firstHC = new HoneyComb(x,y);
         firstHC.setBorderColor(Color.RED);
         firstHC.setFillShape(false);
         structure.add(firstHC);//1
 
         for (int j=1;j<=4;j++){
 
             y=y-(2*HoneyComb.getHeight());
             structure.add(new HoneyComb(x,y));//2, second round 9 ...
 
             for(int i=0; i<j;i++){
                 y=y+HoneyComb.getHeight();
                 x=x+(3*HoneyComb.getHalfBase());
                 structure.add(new HoneyComb(x,y));//3, s.r. 10 and 10i1+1
             }
 
             for(int i=0; i<j;i++){
                 y=y+(2*HoneyComb.getHeight());
                 structure.add(new HoneyComb(x,y));//4 s.r 11 and 11i1+1
             }
 
             for(int i=0; i<j;i++){
                 y=y+HoneyComb.getHeight();
                 x=x-(3*HoneyComb.getHalfBase());
                 structure.add(new HoneyComb(x,y));//5
             }
 
             for(int i=0; i<j;i++){
                 y=y-HoneyComb.getHeight();
                 x=x-(3*HoneyComb.getHalfBase());
                 structure.add(new HoneyComb(x,y));//6
             }
 
             for(int i=0; i<j;i++){
                 y=y-(2*HoneyComb.getHeight());
                 structure.add(new HoneyComb(x,y));//7
             }
 
             for(int i=0;i<j;i++)
             {
                 y=y-HoneyComb.getHeight();
                 x=x+(3*HoneyComb.getHalfBase());
                 if(i<j-1)
                     structure.add(new HoneyComb(x,y));//8
             }
         }
     }
 
     @Override
     public void paintComponent(Graphics g){
         //System.out.println("Paint component");
         super.paintComponent(g);
         Graphics2D g2 = (Graphics2D)g.create();
 
         if(image==null || getWidth()!=image.getWidth() ||
                                         getHeight()!=image.getHeight()){            
             structure.clear();            
             createStructureHoneyComb();
             if(clickedArea!=null)
                 clickedArea=structure.get(clickedArea.getId());
             image = (BufferedImage) createImage(getWidth(), getHeight());
             Graphics2D gimage = image.createGraphics();            
             gimage.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
             for (HoneyComb honeyComb : structure) {
                 //gimage.setColor(honeyComb.getBorderColor());
                 if(honeyComb.isFillShape()){
                     gimage.fill(honeyComb.getGenPath());
                 }else
                     gimage.draw(honeyComb.getGenPath());
 
                 gimage.drawString(honeyComb.getId()+"",(int)honeyComb.getCenter().x,(int)honeyComb.getCenter().y);
                 
             }
             gimage.dispose();
         }
         g2.drawImage(image,null,0,0);
 
         if(clickedArea!=null){
             g2.setColor(Color.red);
             g2.fill(clickedArea.getGenPath());
             g2.setColor(Color.BLACK);
             g2.drawString(clickedArea.getId()+"",(int)clickedArea.getCenter().x,(int)clickedArea.getCenter().y);
         }
         g2.dispose();
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
        
         for (int i = 0; i < structure.size(); i++) {
             HoneyComb hc = structure.get(i);
             if(hc.getGenPath().contains(e.getPoint()) ){
                 //System.out.println("index vec ="+i+" id="+hc.getId());
                 if(e.getButton()==MouseEvent.BUTTON1){
                     clickedArea = hc;
                 }else if(clickedArea!=null && e.getButton()==MouseEvent.BUTTON3 &&
                             clickedArea.getId()==hc.getId()){
                     clickedArea=null;
                 }
                 //System.out.println("Click Area="+clickedArea);
                 repaint();
                 break;
             }
         }
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
         mouseClicked(e);
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
     }
 
     @Override
    public void mouseMoved(MouseEvent e) {        
     }
     @Override
    public void mouseDragged(MouseEvent e) {        
     }
 
     private class HoneyCombVector extends Vector<HoneyComb>{
 
         @Override
         public synchronized boolean add(HoneyComb e) {
             boolean retVal = super.add(e);
             e.setId(structure.size()-1);
             return retVal;
         }
 
     }
 
 }
