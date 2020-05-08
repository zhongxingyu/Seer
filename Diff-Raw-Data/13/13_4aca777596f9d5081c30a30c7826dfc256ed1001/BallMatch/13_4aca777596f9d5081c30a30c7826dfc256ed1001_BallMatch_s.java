 import java.io.*;
 import java.util.*;
 import java.awt.*;
 import java.awt.image.*;
 import javax.swing.*;
 import java.lang.*;
 import java.awt.event.*;
 
 import lcm.lcm.*;
 import april.jcam.*;
 import april.util.*;
 import april.jmat.*;
 import april.jmat.Matrix;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Vector;
 
 
 public class BallMatch implements MouseListener
 {
 
     //Arm Length Constants
     static double L1 = 12; //7.5cm + 4.5cm for Base + Pivot1
     static double L2 = 10.5;
     static double L3 = 10.0;
     static double L4 = 8.0;
     //This doesn't seem right but I'm just going by the dims
     static double L5 = 18.5; //8 + 2 + 8.5
 
     //For sending the robot commands
     RobotDriver rd;
 
     //Image Globals
     ImageSource is;
     BufferedImage template = null;
     BufferedImage im = null;
 
     //Template creation globals
     boolean clicked, getTemplate, onScreen, setTemplate = true, first = true;
     int X1 = 0, X2 = 1, Y1 = 0, Y2 = 1;
     int sMaxX, sMinX, sMaxY, sMinY;
     int edge = 12;
 
 
     //Error Bar Variables
     double error = 0;
     double errorK = 80;
     int scalefactor = 2;
     int pRange = 20;
 
     //Calibration Globals
     boolean calibrate = false, notcalibrated = true;
     boolean cal1 = false, cal2 = false, cal3 = false, cal4 = false;
     Location CalPt1 = new Location();
     Location CalPt2 = new Location();
     Location CalPt3 = new Location();
     Location CalPt4 = new Location();
     double bDistance = 24*2.54;
     Matrix calibration;
 
     //Globals for runing
     boolean run = false;
     double Range1 = 21;
     double Range2 = 37;
 
     boolean test = true;
 
     //GUI Gloabals
     JFrame jf = new JFrame();
     JImage jim = new JImage();
     ParameterGUI pg = new ParameterGUI();
 
 
     //Locations Found
     public static class Location{
         double x = 0;
         double y = 0;
     }
 
     public static class rTheta{
         double r;
         double theta;
     }
     Vector<Location> found = new Vector<Location>();
     Vector<rTheta> located = new Vector<rTheta>();
 
     StateMachine sm;
 
     public BallMatch(ImageSource _is)
     {
         is = _is;
 
         // Determine which slider values we want
         pg.addDoubleSlider("errork","error Threshold",0,100, errorK);
         pg.addButtons("getTemplateButton", "Grab new template");
         pg.addButtons("acceptTemplate", "Accept Template");
         pg.addButtons("calibrate", "Calibrate");
         pg.addButtons("test", "test done");
         pg.addButtons("start", "Start");
 
         jim.setFit(true);
 
         // Setup window layout
         jf.setLayout(new BorderLayout());
         jf.add(jim, BorderLayout.NORTH);
         jf.add(pg, BorderLayout.SOUTH);
         jf.setSize(800, 800);
         jf.setVisible(true);
         jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         jim.addMouseListener(this);
 
         sm = new StateMachine();
         LCM.getSingleton().subscribe("ARM_STATUS", sm);
     }
 
 
     public void mouseClicked(MouseEvent e) {}
 
     public void mouseEntered(MouseEvent e) {
         onScreen = true;
     }
 
     public void mouseExited(MouseEvent e) {
         onScreen = false;
     }
 
     public void mousePressed(MouseEvent me) {
         if(getTemplate & onScreen & first){
             X1 = me.getX();
             Y1 = me.getY();
             first = false;
         }
         else if(getTemplate & onScreen ){
             X2 = me.getX();
             Y2 = me.getY();
             getTemplate = false;
             template = im;
             int bounds[] = {X1, Y1, X2, Y2};
             mark(im, bounds, 0xff0000ff);
             jim.setImage(im);
         }
 
         if(calibrate & !cal1 & onScreen ){
             CalPt1.x = me.getX();
             CalPt1.y = me.getY();
             cal1 = true;
         }
         else if(calibrate & onScreen & !cal2){
             CalPt2.x = me.getX();
             CalPt2.y = me.getY();
             cal2 = true;
         }
         else if(calibrate & onScreen & !cal3){
             CalPt3.x = me.getX();
             CalPt3.y = me.getY();
             cal3 = true;
         }
         else if(calibrate & onScreen & !cal4){
             CalPt4.x = me.getX();
             CalPt4.y = me.getY();
             cal4 = true;
             calibrate = false;
             notcalibrated = false;
             System.out.println("Calibrating4" + notcalibrated);
         }
     }
 
     public void mouseReleased(MouseEvent me) {}
 
     public void screenOutput(){
         //Place template in the top left corner of the screeen
         for (int ty = 0; ty < Y2-Y1; ty++) {
             for (int tx = 0; tx < X2-X1; tx++) {
                 im.setRGB(tx, ty, template.getRGB(X1 + tx, Y1 + ty));
             }
         }
         int [] bounds = {0, 0, X2-X1, Y2-Y1};
         mark(im, bounds, 0xff0000ff);
 
         //mark calibration points
         int [] bounds1 = {(int)CalPt1.x - 1, (int)CalPt1.y - 1, (int)CalPt1.x + 1, (int)CalPt1.y + 1};
         mark(im, bounds1, 0xffff0000);
 
         int [] bounds2 = {(int)CalPt2.x - 1, (int)CalPt2.y - 1, (int)CalPt2.x + 1, (int)CalPt2.y + 1};
         mark(im, bounds2, 0xffff0000);
 
         int [] bounds3 = {(int)CalPt3.x - 1, (int)CalPt3.y - 1, (int)CalPt3.x + 1, (int)CalPt3.y + 1};
         mark(im, bounds3, 0xffff0000);
 
         int [] bounds4 = {(int)CalPt4.x - 1, (int)CalPt4.y - 1, (int)CalPt4.x + 1, (int)CalPt4.y + 1};
         mark(im, bounds4, 0xffff0000);
 
 
         //display image/
         jim.setImage(im);
     }
 
     public void mark(BufferedImage img, int[] bounds, int color){
         // draw the horizontal lines
         for (int x = bounds[0]; x <=bounds[2]; x++) {
             img.setRGB(x,bounds[1], color); //Go Blue!
             img.setRGB(x,bounds[3], color); //Go Blue!
         }
 
         // draw the horizontal lines
         for (int y = bounds[1]; y <=bounds[3]; y++) {
             img.setRGB(bounds[0],y, color); //Go Blue!
             img.setRGB(bounds[2],y, color); //Go Blue
         }
      }
 
 
     // Returns the bounds of the pixel coordinates of the led: {min_x, min_y, max_x, max_y}
     public void matchBall()
     {
         final ImageSourceFormat fmt = is.getCurrentFormat();
 
         // read a frame
         byte buf[] = is.getFrame().data;
             if (buf == null)
                 return;
 
         im = ImageConvert.convertToImage(fmt.format, fmt.width, fmt.height, buf);
 
         errorK = pg.gd("errork");
         int tsizeX = X2 - X1;
         int tsizeY = Y2 - Y1;
         error = 0;
         boolean nskiped = true;
 
         for (int y = sMinY; y < sMaxY - tsizeY; y += scalefactor){
             for (int x = sMinX; x < sMaxX - tsizeX; x += scalefactor){
                 for (int ty = 0; ty < tsizeY; ty += scalefactor) {
                   for (int tx = 0; tx < tsizeX; tx += scalefactor) {
                         int templateRGB = template.getRGB(X1 + tx, Y1 + ty);
                         int imageRGB = im.getRGB(x + tx, y + ty);
 
                         int templateRed = (templateRGB>>16)&0xff;
                         int imageRed = (imageRGB>>16)&0xff;
                         int templateGreen = (templateRGB>>8)&0xff;
                         int imageGreen = (imageRGB>>8)&0xff;
                         int templateBlue = (templateRGB>>0)&0xff;
                         int imageBlue = (imageRGB>>0)&0xff;
                         error += Math.sqrt(Math.pow(templateRed - imageRed,2) +
                                             Math.pow(templateGreen - imageGreen,2) +
                                             Math.pow(templateBlue - imageBlue,2));
                     }
                     if (error > (errorK * tsizeX * tsizeY)/ (2 * scalefactor)){
                         nskiped = false;
                         break;
                     }
                 }
                 if (error < (errorK * tsizeX * tsizeY)/(2*scalefactor) & nskiped){
                     int [] bounds = {x, y, x + X2 - X1, y + Y2 - Y1};
                     mark(im, bounds, 0xff0000ff);
                     Location loc = new Location();
                     if(!test){
                         loc.x = (x + x + X2 - X1)/2;
                         loc.y = (y +y + Y2 - Y1)/2;
                         found.add(loc);
                     }
                 }
                 error = 0;
                 nskiped = true;
             }
         }
 
         if(test){
             screenOutput();
         }
 
         int X = 0, Y = 0, totalX = 0, totalY = 0, count = 0;
         int compX = 0, compY = 0;
         int bound[] = new int[4];
 
         while(0 != found.size()){
             X = (int)found.get(0).x;
             Y = (int)found.get(0).y;
             totalX += X;
             totalY += Y;
             count++;
             found.remove(0);
 
             for(int i = 0; i < found.size(); i++){
                 compX = (int)found.get(i).x;
                 compY = (int)found.get(i).y;
 
                 if(((compX <= X) & (compX + pRange >= X)) |
                     ((compX >= X) & (compX- pRange <= X))){
                     if(((compY <= Y) & (compY + pRange >= Y)) |
                         ((compY >= Y) & (compY - pRange <= Y))){
                         totalX += compX;
                         totalY += compY;
                         count++;
                         found.remove(i);
                         i--;
                     }
                 }
             }
             Location temp = new Location();
             Location tempb = new Location();
             rTheta board = new rTheta();
 
             temp.x = totalX / count;
             temp.y = totalY / count;
 
             totalX = 0;
             totalY = 0;
             count = 0;
 
             tempb = mapToBoard(temp);
 
             double armDistance = Math.sqrt(Math.pow(tempb.x, 2) + Math.pow(tempb.y, 2));
 
             if(armDistance < Range2){
                 if(! (tempb.x < 0 & ( tempb.y < 9 & tempb.y > -9))){
                     bound[0] = (int)temp.x -1;
                     bound[1] = (int)temp.y -1;
                     bound[2] = (int)temp.x + 1;
                     bound[3] = (int)temp.y+1;
                     mark(im, bound, 0xff00ff00);
 
                     board.r = armDistance;
                     board.theta = Math.atan2( tempb.y, tempb.x);
                     System.out.println("YYY " + board.r + " " + board.theta);
 
                     located.add(board);
                 }
             }
         }
     }
 
 //======================================================================//
 // calibrate() ecoding stro //
 // Uses the affine transform to map the pixels to the cordiates of the //
 // board. Stores solution in calibration. //
 //======================================================================//
     public void calibrate(){
 
         //create matrix for the 6 equations need to solve for affine
         double[][] A = {
         {CalPt1.x, CalPt1.y, 1, 0,	0, 0},
         {0,	0,	0,	CalPt1.x,	CalPt1.y, 1},
         {CalPt2.x, CalPt2.y, 1, 0,	0, 0},
         {0,	0,	0,	CalPt2.x,	CalPt2.y, 1},
         {CalPt3.x, CalPt3.y, 1, 0,	0, 0},
         {0,	0,	0,	CalPt3.x,	CalPt3.y, 1}};
         Matrix matA = new Matrix(A);
 
         //Create matrix for the solutions to the 6 equations
         double[][] B = {
         {-bDistance/2},
         {-bDistance/2},
         {bDistance/2},
         {-bDistance/2},
         {bDistance/2},
         {bDistance/2}};
 
         Matrix matB = new Matrix(B);
 
         //invert matix A mutiple with B for solution
         Matrix matAInverse = matA.inverse();
         Matrix calibrationSolution = matAInverse.times(matB);
 
         matAInverse.print();
         calibrationSolution.print();
 
         //make calibration matrix that can be used for mapping pixels
         double tempCal[][] = new double[6][1];
         calibrationSolution.copyToArray(tempCal);
 
         double[][] calibrationArray = {
         {tempCal[0][0], tempCal[1][0], tempCal[2][0]},
         {tempCal[3][0], tempCal[4][0], tempCal[5][0]},
         {0,	0,	1	}};
 
         calibration = new Matrix(calibrationArray);
 
         //setup the range though which template matching should happen
         if(CalPt1.x < CalPt4.x){
             sMinX = (int)CalPt1.x - edge;
         }
         else{
             sMinX = (int)CalPt4.x - edge;
         }
 
         if(CalPt2.x > CalPt3.x){
             sMaxX = (int)CalPt2.x + edge;
         }
         else{
             sMaxX = (int)CalPt3.x + edge;
         }
 
         if(CalPt1.y < CalPt2.y){
             sMinY = (int)CalPt1.y - edge;
         }
         else{
             sMinY = (int)CalPt2.y - edge;
         }
 
         if(CalPt3.y > CalPt4.y){
             sMaxY = (int)CalPt3.y + edge;
         }
         else{
             sMaxY = (int)CalPt4.y + edge;
         }
     }
 
 
 //======================================================================//
 // mapToBoard() //
 // Mutiple the pixel location by the inverse of the calibration to get //
 // the ob board location. //
 //======================================================================//
     public Location mapToBoard(Location pixel){
 
         double[][]B = {
                 {pixel.x},
                 {pixel.y},
                 {1}};
                 Matrix matB = new Matrix(B);
 
         //Mutiple A by pixel to get on board location
         Matrix boardLoc = calibration.times(matB);
 
         //return location as a Location
         double temp[][] = new double[3][1];
         boardLoc.copyToArray(temp);
 
         Location location = new Location();
         location.x = temp[0][0];
         location.y = temp[1][0];
 
         System.out.println("XXXXXXXXXXXXXXXX " + location.x + " " + location.y);
 
         return location;
     }
 
 
 //======================================================================//
 // ballPickUp() //
 // Determins how far a ball is away from the arm. Depending on it's //
 // different pick functions are called. //
 //======================================================================//
     public void ballPickUp(){
 
         while(true){
             matchBall();
 
             screenOutput();
 
             if( located.isEmpty()){
                 System.out.println("No more balls found!");
                 run = false;
                 return;
             }
             while( ! located.isEmpty()){
     
                 rTheta curBall = new rTheta();
                 curBall = located.get(0);
 
                System.out.println("XXXXXXXXXXXXX!: " + curBall.theta + " " + curBall.r/10 );
                sm.startMachine(-curBall.theta, curBall.r/10);
 
                 located.remove(0);
             }
            sm.stop();
         }
     }
 
 //======================================================================//
 // end()                                                                //
 //                                                                      //
 // Shows that the program is done by making the template soild red      //
 //======================================================================//
     public void end(){
 
         final ImageSourceFormat fmt = is.getCurrentFormat();
 
         // read a frame
         byte buf[] = is.getFrame().data;
             if (buf == null)
                 return;
 
         im = ImageConvert.convertToImage(fmt.format, fmt.width, fmt.height, buf);
 
         for (int x = 0; x < X2-X1; x++) {
             for (int y = 0; y <=Y2-Y1; y++) {
                 im.setRGB(x,y, 0xffff0000); //Go Blue!
             }
         }
         jim.setImage(im);
     }
 
 
 
 
 //======================================================================//
 // run()                                                                //
 //                                                                      //
 // Gets template, calibrates and maps pixels to board.                  //
 // Upon start being pressed begins template matching and picking up     //
 // balls. After all balls that can be picked up have been picked up     //
 // ends operatation.                                                    //
 //======================================================================//
     public void run(){
         is.start();
         final ImageSourceFormat fmt = is.getCurrentFormat();
 
         // Initialize visualization environment now that we know
         // the image dimensions
         pg.addListener(new ParameterListener() {
             public void parameterChanged(ParameterGUI pg, String name)
             {
                 if (name.equals("getTemplateButton")){
                     first = true;
                     getTemplate = true;
                     // read a frame
                     byte buf[] = is.getFrame().data;
                     if (buf != null){
                         im = ImageConvert.convertToImage(fmt.format, fmt.width, fmt.height, buf);
                         jim.setImage(im);
                      }
                 }
                 if (name.equals("acceptTemplate")){
                     System.out.println("Template has been accepted!");
                     setTemplate = false;
                 }
                 if (name.equals("calibrate")){
                     System.out.println("Calibration Time");
                     calibrate = true;
                 }
                 if (name.equals("test")){
                     System.out.println("Running Time");
                     test = false;
                 }
                 if (name.equals("start")){
                     System.out.println("Running Time");
                     run = true;
                 }
 
             }
         });
 
 
         //Waiting for Template
         System.out.println("Waiting to Template.");
         while(setTemplate){System.out.println(setTemplate);}
 
 
         //Waiting for Calibration
         System.out.println("Waiting to Calibrating");
         while(notcalibrated){System.out.print(".");}
 
         //Maps pixel locations to the board
         calibrate();
 
         //Waiting to Start
         while(test){
             matchBall();
             System.out.println("testing");
         }
 
         while(!run){
             System.out.println("waiting");
         }
 
 
         //Begin picking up balls
         while(run) {
             ballPickUp();
         }
 
         end();
     }
 
     public static void main(String args[]) throws IOException
     {
         ArrayList<String> urls = ImageSource.getCameraURLs();
 
         String url = null;
 
         if (urls.size()==1)
          url = urls.get(0);
 
         if (args.length > 0)
             url = args[0];
 
         if (url == null) {
            System.out.printf("Cameras found:\n");
            for (String u : urls)
                System.out.printf(" %s\n", u);
          System.out.printf("Please specify one on the command line.\n");
                return;
         }
 
         ImageSource is = ImageSource.make(url);
         new BallMatch(is).run();
     }
 }
