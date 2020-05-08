 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package app.gui.detailspanel;
 
 import app.gui.borders.DoubleOvalBorder;
 import app.utils.OutputVerboseStream;
 import app.utils.Utils;
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.font.FontRenderContext;
 import java.awt.font.TextLayout;
 import java.awt.geom.RoundRectangle2D;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.RepaintManager;
 import javax.swing.SwingUtilities;
 import org.jdesktop.animation.timing.Animator;
 import org.jdesktop.animation.timing.Animator.Direction;
 import org.jdesktop.animation.timing.Animator.RepeatBehavior;
 import org.jdesktop.animation.timing.TimingTarget;
 
 /**
  * Created on 2008-12-08, 21:25:25
  * @author vara
  */
 public class DetailsPanelForSearchServices extends JPanel 
         implements MouseListener,MouseMotionListener,TimingTarget{
 
 
     private int sensitiveMouseReaction = 8;
     private float alpha = .9f;
     
     private Color colorBorder = Utils.colorAlpha(0,0,0,getAlpha());
     private DoubleOvalBorder mainBorder = new DoubleOvalBorder(20,20,Utils.colorAlpha(100,100,100,.45f),45,45,colorBorder);
 
     private OutputVerboseStream verboseStream = null;
 
     private boolean resizeWidthPanel = false;
     private boolean resizeHeghtPanel = false;
     private boolean cursorChanged = false;
 
     private boolean needRevalidate = false;
     private boolean dynamicRevalidate = false;
 
     private Dimension defaultSize = new Dimension(330,400);
 
     private static final Color clrGlowInnerHi = new Color(253, 239, 175, 148);
     private static final Color clrGlowInnerLo = new Color(255, 209, 0);
     private static final Color clrGlowOuterHi = new Color(253, 239, 175, 124);
     private static final Color clrGlowOuterLo = new Color(255, 179, 0);
 
     private boolean decoratePanel = true;
     private DecoratePanel decorate = new DecoratePanel();
 
     private Animator animator;
     private int animationDuration = 5000;
     
     public DetailsPanelForSearchServices(OutputVerboseStream l){
         
         verboseStream = l;
         setOpaque(false);
         setSize(defaultSize);
         addMouseListener(this);
         addMouseMotionListener(this);
         mainBorder.setInsetsOuter(new Insets(2,2,2,2));
         mainBorder.setInsetsInner(new Insets(35,11,11,11));
         setBorder(mainBorder);        
         setLayout(new GridLayout());
         //OpenCloseButton toggleButton = new OpenCloseButton("^", true,getSize(),30,l);
        
         //add(toggleButton);
         installRepaintManager();
         //add(decorate);
 
         animator = new Animator(animationDuration, 2,
                 RepeatBehavior.LOOP, this);    
         
         setDynamicRevalidate(true);
         super.setEnabled(false);
     }
 
     @Override
     public void setEnabled(final boolean aFlag) {
         super.setEnabled(aFlag);
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 displayPanel(aFlag);
             }
         });        
     }
 
     @Override
     public void setVisible(boolean aFlag) {
         super.setVisible(aFlag);
         System.err.println("Search Services Visible "+aFlag);
     }
     public void displayPanel(boolean val){
        if(!animator.isRunning() && !val)
            return;
        
        float frac = .0f;
        if(animator.isRunning()){
            animator.cancel();
            frac = getAlpha();
        }
         if(val){
           animator.setStartFraction(frac);
            animator.setStartDirection(Direction.FORWARD);
         }else{
            animator.setStartFraction(getAlpha());
            animator.setStartDirection(Direction.BACKWARD);
         }
         animator.start();
     }
     public void updateMyUI(){
         
         int width = getParent().getWidth();
         int height = getParent().getHeight();
         this.setLocation(width-getWidth(), (height-getHeight())/2 );
 
         if(isDynamicRevalidate())
             revalidate();
         //getVerboseStream().outputVerboseStream(getClass().getSimpleName()+" UpdateMyUI\n Parent size ["+width+","+height+"]" +
         //        "\tLocation on parent component ["+getLocation().getX()+","+getLocation().y+"]");
     }
 
     private void installRepaintManager() {
         MyRepaintManager manager = new MyRepaintManager();
         RepaintManager.setCurrentManager(manager);
     }
 
     public OutputVerboseStream getVerboseStream(){
 	    return verboseStream;
 	}
 
     @Override
     public void paintChildren(Graphics g){
         Graphics2D g2 = (Graphics2D)g;
         AlphaComposite newComposite =
               AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,getAlpha());
         g2.setComposite(newComposite);
 
         Rectangle bounds = getBounds();
         Insets ins = getInsets();
         Rectangle oldClip = g2.getClipBounds();
         Rectangle newClip = (Rectangle)oldClip.clone();
 
         int canX = ins.left;
         int canY = ins.top;
         int canWidth = bounds.width-ins.left-ins.right;
         int canHeight = bounds.height-ins.top-ins.bottom;
         SwingUtilities.computeIntersection(canX, canY, canWidth, canHeight, newClip);
 
         g.setClip(newClip);
         super.paintChildren(g);
         g.setClip(oldClip);
     }
 
     @Override
     public void paintComponent(Graphics g){
         
         if(isVisible() && getParent().isEnabled()){
             Graphics2D g2 = (Graphics2D)g.create();
 
             GradientPaint gp = new GradientPaint(0.0f, (float) getHeight()/2,Utils.colorAlpha(1,51,90,getAlpha()),
                                                 (float)getWidth()/2, 80.0f,Utils.colorAlpha(43,105,152,getAlpha()));
             GradientPaint gp2 = new GradientPaint(0.0f, (float) getHeight(),Utils.colorAlpha(0,0,0,getAlpha()),
                                                 0.0f, 0.0f,Utils.colorAlpha(90,122,166,getAlpha()));
             Insets outerIns = mainBorder.getInsetsOuter();
             Insets innerIns = mainBorder.getInsetsInner();
             RoundRectangle2D outerBorder = DoubleOvalBorder.createOuterShape(outerIns.left,outerIns.top,
                     getWidth()-outerIns.left-outerIns.right,getHeight()-outerIns.top-outerIns.bottom,
                     mainBorder.getRecW(), mainBorder.getRecH(),outerIns);
             RoundRectangle2D innerBorder = DoubleOvalBorder.createInnerShape(outerBorder.getX()-2,outerBorder.getY()-2,
                     outerBorder.getWidth()+3,outerBorder.getHeight()+3,mainBorder.getRoundInnerX(), mainBorder.getRoundInnerY(),
                     innerIns);
 
             g2.setPaint(gp2);
             g2.fill(outerBorder);
             g2.setPaint(gp);
             g2.fill(innerBorder);
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 RenderingHints.VALUE_ANTIALIAS_ON);
 
             paintBorderGlow(g2,5,outerBorder);
             paintBorderShadow(g2,2,innerBorder);
 /*
             //calculte child clip <check the mainClip must be outsider relative childClip>
             float widthStroke = ((BasicStroke)g2.getStroke()).getLineWidth();
             double clipX  = innerBorder.getX()+widthStroke+1;
             double clipY = innerBorder.getY()+widthStroke+1;
             double clipW = innerBorder.getWidth()-(widthStroke*2);
             double clipH = innerBorder.getHeight()-(widthStroke*2);
     
             clipX+=mainClip.getX();
             clipY+=mainClip.getY();
             if(clipW>mainClip.getWidth())
                 clipW =mainClip.getWidth();
             if(clipH>mainClip.getHeight())
                 clipH =mainClip.getHeight();
     */
 //            childClip = new RoundRectangle2D.Double(clipX,clipY,clipW,clipH,innerBorder.getArcWidth(), innerBorder.getArcHeight());
 
             //getVerboseStream().outputVerboseStream("childClip "+childClip.getBounds2D()+" innerBorder "+innerBorder.getBounds2D());
             //Color cInn = mainBorder.getColorForInnerBorder();
             //Color cOut = mainBorder.getColorForOuterBorder();
             //System.out.println("Color alpha "+(getAlpha()%.25f));
             //mainBorder.setColorForInnerBorder(Utils.colorAlpha(cInn, getAlpha()%.25f));
             //mainBorder.setColorForOuterBorder(Utils.colorAlpha(cOut, getAlpha()%.25f));
 
             g2.dispose();
         }
         
     }
 
     private void paintBorderShadow(Graphics2D g2, int shadowWidth,Shape clipShape) {        
         int sw = shadowWidth*2;
         for (int i=sw; i >= 2; i-=1) {
             float pct = (float)(sw - i) / (sw - 1);
             g2.setColor(getMixedColor(Color.LIGHT_GRAY, pct,
                                       Color.WHITE, 1.0f-pct));
             
             g2.setStroke(new BasicStroke(i));
             g2.draw(clipShape);
         }
     }    
 
     private void paintBorderGlow(Graphics2D g2, int glowWidth,Shape clipShape) {
 
         int gw = glowWidth*2;
         for (int i=gw; i >= 2; i-=2) {
             float pct = (float)(gw - i) / (gw - 1);
 /*
             Color mixHi = getMixedColor(clrGlowInnerHi, pct,
                                        clrGlowOuterHi, 1.0f - pct);
             Color mixLo = getMixedColor(clrGlowInnerLo, pct,
                                       clrGlowOuterLo, 1.0f - pct);
 
             g2.setPaint(new GradientPaint(0.0f, clipShape.getBounds().height*0.25f,  mixHi,
                                           0.0f, clipShape.getBounds().height, mixLo));
  */
             g2.setColor(Utils.colorAlpha(240,240,255,getAlpha()));
             g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pct));
             g2.setStroke(new BasicStroke(i));
             g2.draw(clipShape);
         }
     }
 
     private Color getMixedColor(Color c1, float pct1, Color c2, float pct2) {
         float[] clr1 = c1.getComponents(null);
         float[] clr2 = c2.getComponents(null);
         for (int i = 0; i < clr1.length-1; i++) {
             clr1[i] = (clr1[i] * pct1) + (clr2[i] * pct2);            
         }        
         return new Color(clr1[0], clr1[1], clr1[2], getAlpha());
     }  
 
     @Override
     public void mouseClicked(MouseEvent e) {
         
 //        if(!toggleButton.isVisible())
   //          toggleButton.showToggleButton();
     //    else
       //      toggleButton.hideToggleButton();
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
        
         if(e.getButton()==MouseEvent.BUTTON1 && e.getX()<sensitiveMouseReaction){
             getVerboseStream().outputVerboseStream("Resize X side "+getClass().getSimpleName());
             resizeWidthPanel = true;
         }else if(e.getButton()==MouseEvent.BUTTON1 && e.getY()<sensitiveMouseReaction){
             getVerboseStream().outputVerboseStream("Resize Y side "+getClass().getSimpleName());
             resizeHeghtPanel = true;
         }
     }
     @Override
     public void mouseReleased(MouseEvent e) {
 
         if(needRevalidate || !isDynamicRevalidate()){
             getVerboseStream().outputVerboseStream("Revalidate!");            
             revalidate();
             needRevalidate=false;
         }
         resizeWidthPanel = false;
         resizeHeghtPanel = false;
     }
     @Override
     public void mouseEntered(MouseEvent e) {}
     @Override
     public void mouseExited(MouseEvent e) {
         //resizeWidthPanel = false;
         //resizeHeghtPanel = false;
     }
     @Override
     public void mouseDragged(MouseEvent e) {
         if(resizeWidthPanel || resizeHeghtPanel){
             if(resizeWidthPanel)
                 setSize(getWidth()-e.getX()+2, getHeight());
             else
                 setSize(getWidth(), getHeight()-e.getY());            
             updateMyUI();
             needRevalidate=true;
         }
     }
     @Override
     public void mouseMoved(MouseEvent e) {
         
         if(e.getX()<sensitiveMouseReaction||e.getY()<sensitiveMouseReaction){
             setCursor(Cursor.getPredefinedCursor( e.getX()<sensitiveMouseReaction ?
                                       Cursor.W_RESIZE_CURSOR:Cursor.N_RESIZE_CURSOR));
             cursorChanged = true;           
         }else if(cursorChanged && !resizeWidthPanel&&!resizeHeghtPanel){
             setCursor(Cursor.getDefaultCursor());
         }
     }
 
     /**
      * @return the decoratePanel
      */
     public boolean isDecoratePanel() {
         return decoratePanel;
     }
 
     /**
      * @return the alpha
      */
     public float getAlpha() {
         return alpha;
     }
 
     /**
      * @param aAlpha the alpha to set
      */
     public void setAlpha(float aAlpha) {
         alpha = aAlpha;
     }
 
     /**
      * @param decoratePanel the decoratePanel to set
      */
     public void setDecoratePanel(boolean decoratePanel) {
         this.decoratePanel = decoratePanel;
     }
 
     /**
      * @return the dynamicRevalidate
      */
     public boolean isDynamicRevalidate() {
         return dynamicRevalidate;
     }
 
     /**
      * @param dynamicRevalidate the dynamicRevalidate to set
      */
     public void setDynamicRevalidate(boolean dynamicRevalidate) {
         this.dynamicRevalidate = dynamicRevalidate;
     }
 
     public void timingEvent(float arg0) {
         //System.out.println("Alpha on Details panel for SServ. "+arg0);
         if(arg0<0.7f && arg0>0){
             setAlpha(arg0);
             repaint();
         }else
             animator.stop();
     }
     public void begin() {
         if(isEnabled())
            setVisible(isEnabled());
     }
     public void end() {
         if(!isEnabled())
            setVisible(isEnabled());
     }
     public void repeat() {
     }
 
     class DecoratePanel extends JPanel{
         
         public static final int CENTER = 0;
         public static final int LEFT = 1;
         public static final int RIGHT = 2;
         
         private int layout = 0;
 
         private String title = "Unknown";
 
         public DecoratePanel(){
             setOpaque(false);
         }
 
         @Override
         public void paintComponent(Graphics g){
             super.paintComponent(g);
             Graphics2D g2 = (Graphics2D)g;
             FontRenderContext frc = g2.getFontRenderContext();
             Font font = g2.getFont();
             Point.Double  centerFont = findPositionForString(font,getDecoreteLayout(),getTitle(),frc);
             g2.drawString(getTitle(),(float)centerFont.getX(), (float)centerFont.getY());
 
         }
 
         private Point.Double findPositionForString(Font f,int mode,String text,FontRenderContext frc){
             if(mode==DecoratePanel.CENTER) {
                 TextLayout textTl = new TextLayout(text,f, frc);
                 Rectangle rec = textTl.getPixelBounds(frc,0,0);
 
                 FontMetrics fm = getFontMetrics(f);
                 //rozmiary stringu w pixelach
                 double strWidthInPix = (int)rec.getWidth();
                 double strHeightInPix = (int)rec.getHeight();
                 //size panel in pixels
                 int width = getSize().width;
                 int height = getSize().height;
 
                 double centPointX = (width-strWidthInPix-2)/2;
                 double centPointY = (height+strHeightInPix-2)/2;
                 //System.out.println("rozmiar Panelu "+width+","+height+
                 //				   "\nrozmiar stringu "+strWidthInPix+","+strHeightInPix+
                 //				   "\nSrodek "+centPointX+","+centPointY);
                 return new Point.Double(centPointX,centPointY);
             }
             return null;
         }
 
         /**
          * @return the layout
          */
         public int getDecoreteLayout() {
             return layout;
         }
         /**
          * @param layout the layout to set
          */
         public void setDecorateLayout(int layout) {
             this.layout = layout;
         }
         /**
          * @return the title
          */
         public String getTitle() {
             return title;
         }
         /**
          * @param title the title to set
          */
         public void setTitle(String title) {
             this.title = title;
         }
     }
 
     public class MyRepaintManager extends RepaintManager {
         
         @Override
         public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
 
             Rectangle dirtyRegion = getDirtyRegion(c);
             if (dirtyRegion.width == 0 && dirtyRegion.height == 0) {
                 int lastDeltaX = c.getX();
                 int lastDeltaY = c.getY();
                 Container parent = c.getParent();
                 while (parent instanceof JComponent) {
                     if (!parent.isVisible() || (parent.getPeer() == null)) {
                         return;
                     }
                     if (parent instanceof DetailsPanelForSearchServices &&
                             (((DetailsPanelForSearchServices)parent).getAlpha() < 1f ||
                             !parent.isOpaque())) {
                         x += lastDeltaX;
                         y += lastDeltaY;
                         lastDeltaX = lastDeltaY = 0;
                         c = (JComponent)parent;
                     }
                     lastDeltaX += parent.getX();
                     lastDeltaY += parent.getY();
                     parent = parent.getParent();
                 }
             }
             super.addDirtyRegion(c, x, y, w, h);
         }
     }//MyRepaintManager
 }
