 package se.itchie.kinect.handtrack;
 
 import org.OpenNI.*;
 import se.itchie.kinect.Queue;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferByte;
 import java.awt.image.Raster;
 import java.nio.ShortBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * Handtrack is a re-implementation of the Kinect Sample Handtracker.
  * It displays nothing, only sends data to a thread safe queue object.
  *
  *
  * This code is free and open source.
  * Use it as you wish, at own risk and without warranty.
  *
  * @author mathias
  *         Date: 12/18/11
  */
 public class Handtrack extends Component {
 //public class Handtrack extends Thread{
 
     Queue queue;
 
     private OutArg<ScriptNode> scriptNode;
     private Context context;
     private DepthGenerator depthGenerator;
     private GestureGenerator gestureGenerator;
     private HandsGenerator handsGenerator;
     private HashMap<Integer, ArrayList<Point3D>> history;
     private byte[] imgbytes;
     private float histogram[];
 
     private BufferedImage bimg;
     int width, height;
 
     int historySize = 10;
     boolean done = false;
 
     private final String SAMPLE_XML_FILE = "/Users/mathias/code/java/KinectSamples/src/se/itchie/kinect/SamplesConfig.xml";
 
     public Handtrack(Queue queue, String threadName){
 //        super(threadName);
         this.queue = queue;
         init();
     }
 
 
     private void init(){
 
         System.out.println("Handtrack.init()");
         try{
             scriptNode = new OutArg<ScriptNode>();
             context = Context.createFromXmlFile(SAMPLE_XML_FILE, scriptNode);
 
             gestureGenerator = GestureGenerator.create(context);
             gestureGenerator.addGesture("Wave");
             gestureGenerator.getGestureRecognizedEvent().addObserver(new MyGestureRecognized());
 
             handsGenerator = HandsGenerator.create(context);
             handsGenerator.getHandCreateEvent().addObserver(new MyHandCreateEvent());
             handsGenerator.getHandUpdateEvent().addObserver(new MyHandUpdateEvent());
             handsGenerator.getHandDestroyEvent().addObserver(new MyHandDestroyEvent());
 
 
             depthGenerator = DepthGenerator.create(context);
             DepthMetaData depthMetaData = depthGenerator.getMetaData();
 
             // starts the usb driver (among other things)
             context.startGeneratingAll();
 
             history = new HashMap<Integer, ArrayList<Point3D>>();
 
             histogram = new float[10000];
             width = depthMetaData.getFullXRes();
             height = depthMetaData.getFullYRes();
 
             imgbytes = new byte[width*height];
 
             // Graphics setup
             DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width*height);
             Raster raster = Raster.createPackedRaster(dataBuffer, width, height, 8, null);
             bimg = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
             bimg.setData(raster);
 
         } catch (GeneralException e) {
             e.printStackTrace();
             System.out.println("Something went wrong, exiting...");
             System.exit(1);
         }
     }
 
     private void calcHist(ShortBuffer depth){
         // reset
         for(int i=0; i<histogram.length; i++){
             histogram[i] = 0;
         }
 
         depth.rewind();
 
         int points = 0;
         while(depth.remaining() > 0){
             short depthVal = depth.get();
             if(depthVal != 0){
                 histogram[depthVal]++;
                 points++;
             }
         }
 
         for(int i=1; i<histogram.length; i++){
             histogram[i] += histogram[i-1];
         }
 
         if(points > 0){
             for(int i=1; i<histogram.length; i++){
                 histogram[i] = (int)(256* (1.0f -(histogram[i] / (float)points)));
             }
         }
     }
 
 
     void updateDepth(){
         try{
             DepthMetaData depthMetaData = depthGenerator.getMetaData();
             context.waitAnyUpdateAll();
 
             ShortBuffer depth = depthMetaData.getData().createShortBuffer();
             calcHist(depth);
             depth.rewind();
 
             while(depth.remaining() > 0){
                 int pos = depth.position();
                 short pixel = depth.get();
                 imgbytes[pos] = (byte)histogram[pixel];
             }
         }catch (GeneralException e){
             e.printStackTrace();
         }
     }
 
 
     public Dimension getPreferredSize(){
         return new Dimension(width, height);
     }
 
 
     Color colors[] = {Color.RED, Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.PINK, Color.YELLOW};
     public void paint(Graphics g) {
         DataBufferByte dataBuffer = new DataBufferByte(imgbytes, width*height);
         Raster raster = Raster.createPackedRaster(dataBuffer, width, height, 8, null);
         bimg.setData(raster);
 
         g.drawImage(bimg, 0, 0, null);
 
         int[] user_id = new int[4];
 
         int counter = 1;
 
         for (Integer id : history.keySet()){
             try{
                 ArrayList<Point3D> points = history.get(id);
                 g.setColor(colors[id%colors.length]);
                 int[] xPoints = new int[points.size()];
                 int[] yPoints = new int[points.size()];
                 for (int i = 0; i < points.size(); ++i){
                     Point3D proj = depthGenerator.convertRealWorldToProjective(points.get(i));
                     xPoints[i] = (int)proj.getX();
                     yPoints[i] = (int)proj.getY();
                 }
                 g.drawPolyline(xPoints, yPoints, points.size());
                 Point3D proj = depthGenerator.convertRealWorldToProjective(points.get(points.size()-1));
                 g.drawArc((int)proj.getX(), (int)proj.getY(), 5, 5, 0, 360);
 
                 System.out.println("NOF hands: " + history.size());
                 System.out.println("id: " + id + " x: " + proj.getX() + " y: " + proj.getY());
                 if(counter<5){
                     // send to socket
                     int socket_val = this.format_to_int(counter, proj.getX(), proj.getY());
                     System.out.println("data to socket: " + socket_val);
                 }
                 counter++;
 
         	} catch (StatusException e)
         	{
         		e.printStackTrace();
         	}
         }
 
     }
 
 
     private int format_to_int(int id, float x, float y){
 
         String _x = this.padRight((int)x, 3);
         String _y = this.padRight((int)y, 3);
         String finalString = ""+id + "" + _x + "" + "" + _y;
 //        return Integer.parseInt(finalString);
         return Integer.parseInt(String.format("%s%s%s", id, _x, _y));
     }
 
     private String padRight(int v, int n) {
         return String.format("%1$-" + n + "s", v);
     }
 
 
     // replaces the regular paint() method
     public void run(){
 
         while(!done){
             System.out.println("kinect.run()");
 
//           paint(g);
        }
     }
 
 
     /**
      * Event classes
      */
     class MyGestureRecognized implements IObserver<GestureRecognizedEventArgs>{
 
         @Override
         public void update(IObservable<GestureRecognizedEventArgs> observable, GestureRecognizedEventArgs args) {
             try{
                 handsGenerator.StartTracking(args.getEndPosition());
                 gestureGenerator.removeGesture("Click");
             } catch (StatusException e) {
                 e.printStackTrace();
             }
         }
     }
 
     class MyHandCreateEvent implements IObserver<ActiveHandEventArgs>{
 
         @Override
         public void update(IObservable<ActiveHandEventArgs> observable, ActiveHandEventArgs args) {
             ArrayList<Point3D> newList = new ArrayList<Point3D>();
             newList.add(args.getPosition());
             history.put(args.getId(), newList);
         }
     }
 
     class MyHandUpdateEvent implements IObserver<ActiveHandEventArgs>{
 
         @Override
         public void update(IObservable<ActiveHandEventArgs> observable, ActiveHandEventArgs args) {
             ArrayList<Point3D> historyList = history.get(args.getId());
             historyList.add(args.getPosition());
             while(historyList.size() > historySize){
                 historyList.remove(0);
             }
         }
     }
 
 
 
     class MyHandDestroyEvent implements IObserver<InactiveHandEventArgs>{
 
         @Override
         public void update(IObservable<InactiveHandEventArgs> observable, InactiveHandEventArgs args) {
 
             history.remove(args.getId());
             if(history.isEmpty()){
                 try{
                     gestureGenerator.addGesture("Click");
                 } catch (StatusException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     public static void main(String[] args){
 
     }
 
 }
