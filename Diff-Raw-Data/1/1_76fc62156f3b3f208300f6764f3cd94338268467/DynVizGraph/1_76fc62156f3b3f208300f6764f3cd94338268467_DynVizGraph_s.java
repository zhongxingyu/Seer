 import javax.swing.JApplet;
 import javax.swing.JPanel;
 import javax.swing.border.LineBorder;
 import javax.swing.SwingUtilities;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Container;
 import java.awt.GridLayout;
 import java.awt.GraphicsEnvironment;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsConfiguration;
 import java.awt.Transparency;
 
 
 /**
  *
  * @author gmcwhirt
  */
 public class DynVizGraph extends JApplet {
 
     private int chartWidth = 210;  // The width of the canvas panel
     private int chartHeight = 210; // The height of the canvas panel
     private int chartPadding = 5;
 
     private CanvasPanel BRChart;  // JPanel canvas for graphics drawing
     private CanvasPanel DtRChart;
     private CanvasPanel CtRChart;
 
     private int payoffA;
     private int payoffB;
     private int payoffC;
     private int payoffD;
 
     private Thread BRThread;
     private Thread DtRThread;
     private Thread CtRThread;
 
     @Override
     public void init(){
         try {
             SwingUtilities.invokeAndWait(new Runnable() {
                 @Override
                 public void run() {
                     goBabyGo();
                 }
             });
         } catch (Exception e) {
             System.err.println("goBabyGo failed.");
             e.printStackTrace();
         }
     }
 
     public void goBabyGo(){
         Container c = getContentPane();
 
         setPreferredSize(new Dimension(chartWidth * 3 + 2, chartHeight));
         c.setPreferredSize(new Dimension(chartWidth * 3 + 2, chartHeight));
 
         JPanel panel = new JPanel(new GridLayout(1,3));
 
         BRChart = new CanvasPanel(chartWidth, chartHeight, chartPadding);
         DtRChart = new CanvasPanel(chartWidth, chartHeight, chartPadding);
         CtRChart = new CanvasPanel(chartWidth, chartHeight, chartPadding);
 
         panel.setBorder(new LineBorder(Color.LIGHT_GRAY));
         panel.setPreferredSize(new Dimension(chartWidth * 3 + 2, chartHeight));
 
         BRChart.setPreferredSize(new Dimension(chartWidth, chartHeight));
         DtRChart.setPreferredSize(new Dimension(chartWidth, chartHeight));
         CtRChart.setPreferredSize(new Dimension(chartWidth, chartHeight));
         panel.add(BRChart);
         panel.add(DtRChart);
         panel.add(CtRChart);
 
         c.add(panel);
 
         try {
             String pAS = getParameter("A");
             payoffA = Integer.parseInt(pAS);
         } catch (NumberFormatException e) {
             payoffA = 0;
         } catch (NullPointerException e) {
             payoffA = 0;
         }
 
         try {
             String pBS = getParameter("B");
             payoffB = Integer.parseInt(pBS);
         } catch (NumberFormatException e) {
             payoffB = 0;
         } catch (NullPointerException e) {
             payoffB = 0;
         }
 
         try {
             String pCS = getParameter("C");
             payoffC = Integer.parseInt(pCS);
         } catch (NumberFormatException e) {
             payoffC = 0;
         } catch (NullPointerException e) {
             payoffC = 0;
         }
 
         try {
             String pDS = getParameter("D");
             payoffD = Integer.parseInt(pDS);
         } catch (NumberFormatException e) {
             payoffD = 0;
         } catch (NullPointerException e) {
             payoffD = 0;
         }
 
         BRThread = new Thread(new BRGraphGenerator(payoffA, payoffB, payoffC, payoffD, BRChart.getRealWidth(), BRChart.getRealHeight()));
         DtRThread = new Thread(new DtRGraphGenerator(payoffA, payoffB, payoffC, payoffD, DtRChart.getRealWidth(), DtRChart.getRealHeight()));
         CtRThread = new Thread(new CtRGraphGenerator(payoffA, payoffB, payoffC, payoffD, CtRChart.getRealWidth(), CtRChart.getRealHeight()));
     }
 
     @Override
     public void start(){
         BRThread.start();
         DtRThread.start();
         CtRThread.start();
     }
 
     @Override
     public void stop(){
         BRThread.interrupt();
         DtRThread.interrupt();
         CtRThread.interrupt();
     }
 
     private void BRGraphInfo(CanvasImage ci){
         BRChart.setCImage(ci);
         BRChart.flush();
     }
     
     private void DtRGraphInfo(CanvasImage ci){
         DtRChart.setCImage(ci);
         DtRChart.flush();
     }
     
     private void CtRGraphInfo(CanvasImage ci){
         CtRChart.setCImage(ci);
         CtRChart.flush();
     }
 
     class BRGraphGenerator implements Runnable {
         private CanvasImage ci;
         private int A, B, C, D;
 
         public BRGraphGenerator(int Ap, int Bp, int Cp, int Dp, int width, int height){
             A = Ap;
             B = Bp;
             C = Cp;
             D = Dp;
 
             GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
             GraphicsDevice gs = ge.getDefaultScreenDevice();
             GraphicsConfiguration gc = gs.getDefaultConfiguration();
 
             ci = new CanvasImage(gc.createCompatibleImage(width, height, Transparency.BITMASK));
 
             BRGraphInfo(ci);
         }
 
         private float _lrespy(){
             if (A + C > 0){
                 return 1f;
             } else if (A + C < 0){
                 return 0f;
             } else if (0 <= A) {
                 return 1f;
             } else {
                 return 0f;
             }
         }
 
         private float _lrespx(){
             if (B + D > 0){
                 return 0f;
             } else if (B + D < 0){
                 return 1f;
             } else if (0 >= D){
                 return 0f;
             } else {
                 return 1f;
             }
         }
 
         @Override
         public void run(){
             //draw stuff
             float lrespx = _lrespx();
             float lrespy = _lrespy();
             
             float qlim = (float)A / (float)(A + C);
 
             float plim = (float)D / (float)(B + D);
 
             int dots = 11; //effectively 10
             for (int x = 0; x <= dots; x++){
                 for (int y = 0; y <= dots; y++){
                     float xf = (float)x / (float)dots;
                     float yf = (float)y / (float)dots;
 
                     float xxf;
                     float yyf;
 
                     if (xf < qlim || Float.isNaN(qlim)){
                         yyf = lrespy;
                     } else if (xf > qlim){
                         yyf = 1f - lrespy;
                     } else {
                         yyf = yf;
                     }
 
                     if (yf < plim || Float.isNaN(plim)){
                         xxf = lrespx;
                     } else if (yf > qlim){
                         xxf = 1f - lrespx;
                     } else {
                         xxf = xf;
                     }
 
                     ci.drawArrow(xf, yf, xxf, yyf, Color.green, Color.black);
                     ci.drawLine(xf, yf, xf, yf, Color.black);
                 }
             }
 
                         if (A + C > 0){
                 if (qlim <= 1f && qlim >= 0f){
                     ci.drawLine(0f, 1f, qlim, 1f, Color.red);
                     ci.drawLine(qlim, 0f, 1f, 0f, Color.red);
                     ci.drawLine(qlim, 0f, qlim, 1f, Color.red);
                 } else if (qlim > 1f) {
                     //play this
                     ci.drawLine(0f, 1f, 1f, 1f, Color.red);
                 } else {
                     //play other
                     ci.drawLine(0f, 0f, 1f, 0f, Color.red);
                 }
             } else if (A + C < 0){
                 if (qlim <= 1f && qlim >= 0f){
                     ci.drawLine(0f, 0f, qlim, 0f, Color.red);
                     ci.drawLine(qlim, 1f, 1f, 1f, Color.red);
                     ci.drawLine(qlim, 0f, qlim, 1f, Color.red);
                 } else if (qlim > 1f) {
                     //play other
                     ci.drawLine(0f, 0f, 1f, 0f, Color.red);
                 } else {
                     //play this
                     ci.drawLine(0f, 1f, 1f, 1f, Color.red);
                 }
             } else if (0 <= A) {
                 //play this
                 ci.drawLine(0f, 1f, 1f, 1f, Color.red);
             } else {
                 //play other
                 ci.drawLine(0f, 0f, 1f, 0f, Color.red);
             }
 
             if (B + D > 0){
                 if (plim >= 0f && plim <= 1f){
                     ci.drawLine(0f, 1f, 0f, plim, Color.blue);
                     ci.drawLine(1f, plim, 1f, 0f, Color.blue);
                     ci.drawLine(0f, plim, 1f, plim);
                 } else if (plim > 1f){
                     //play other
                     ci.drawLine(1f, 1f, 1f, 0f, Color.blue);
                 } else {
                     //play this
                     ci.drawLine(0f, 1f, 0f, 0f, Color.blue);
                 }
             } else if (B + D < 0){
                 if (plim >= 0f && plim <= 1f){
                     ci.drawLine(0f, 0f, 0f, plim, Color.blue);
                     ci.drawLine(1f, plim, 1f, 1f, Color.blue);
                     ci.drawLine(0f, plim, 1f, plim, Color.blue);
                 } else if (plim > 1f) {
                     //play this
                     ci.drawLine(0f, 1f, 0f, 0f, Color.blue);
                 } else {
                     //play other
                     ci.drawLine(1f, 1f, 1f, 0f, Color.blue);
                 }
             } else if (0 >= D) {
                 //play this
                 ci.drawLine(0f, 1f, 0f, 0f, Color.blue);
             } else {
                 //play other
                 ci.drawLine(1f, 1f, 1f, 0f, Color.blue);
             }
 
             ci.flush();
         }
     }
 
     class DtRGraphGenerator implements Runnable {
         private CanvasImage ci;
         private int A, B, C, D;
 
         public DtRGraphGenerator(int Ap, int Bp, int Cp, int Dp, int width, int height){
             A = Ap;
             B = Bp;
             C = Cp;
             D = Dp;
 
             GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
             GraphicsDevice gs = ge.getDefaultScreenDevice();
             GraphicsConfiguration gc = gs.getDefaultConfiguration();
 
             ci = new CanvasImage(gc.createCompatibleImage(width, height, Transparency.BITMASK));
 
             DtRGraphInfo(ci);
         }
 
         @Override
         public void run(){
             //todo
         }
     }
 
     class CtRGraphGenerator implements Runnable {
         private CanvasImage ci;
         private int A, B, C, D;
 
         public CtRGraphGenerator(int Ap, int Bp, int Cp, int Dp, int width, int height){
             A = Ap;
             B = Bp;
             C = Cp;
             D = Dp;
 
             GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
             GraphicsDevice gs = ge.getDefaultScreenDevice();
             GraphicsConfiguration gc = gs.getDefaultConfiguration();
 
             ci = new CanvasImage(gc.createCompatibleImage(width, height, Transparency.BITMASK));
 
             CtRGraphInfo(ci);
         }
 
         @Override
         public void run(){
             //todo
         }
     }
 }
