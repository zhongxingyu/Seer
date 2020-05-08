 package edu.cmu.pandaa.module;
 
 import edu.cmu.pandaa.header.DistanceHeader;
 import edu.cmu.pandaa.header.DistanceHeader.DistanceFrame;
 import edu.cmu.pandaa.header.GeometryHeader;
 import edu.cmu.pandaa.header.GeometryHeader.GeometryFrame;
 import edu.cmu.pandaa.header.ImpulseHeader;
 import edu.cmu.pandaa.header.ImpulseHeader.ImpulseFrame;
 import edu.cmu.pandaa.header.StreamHeader;
 import edu.cmu.pandaa.header.StreamHeader.StreamFrame;
 import edu.cmu.pandaa.stream.DistanceFileStream;
 import edu.cmu.pandaa.stream.FileStream;
 import edu.cmu.pandaa.stream.GeometryFileStream;
 import edu.cmu.pandaa.stream.ImpulseFileStream;
 
 /**
  * Created by IntelliJ IDEA.
  * User: peringknife
  * Date: 10/21/11
  * Time: 8:56 AM
  */
 
 public class ConsolidateModule implements StreamModule {
   final int combine, rolling, weight;
   final StreamFrame[] frames;
   StreamHeader header;
   Factory factory;
   int frameCnt;
   FileStream out, in;
  char consolidateType;
 
  public ConsolidateModule(int combine, int rolling) {
     this.combine = combine;
     this.rolling = rolling;
    this.weight = 1;
     if (combine < 1 || rolling < 1) {
       throw new IllegalArgumentException("Arguments both need to be >0");
     }
     frames = new StreamFrame[combine];
   }
 
   public StreamHeader init(StreamHeader inHeader) {
     String nid = inHeader.id + "." + combine + "-" + rolling;
     int frameTime = inHeader.frameTime * rolling;
     long startTime = inHeader.startTime;
 
     switch (consolidateType) {
       case 'i':
         header = new ImpulseHeader(nid, startTime, frameTime * rolling, rolling);
         factory = new impulseFactory();
         break;
       case 'd':
         DistanceHeader dHeader = (DistanceHeader) inHeader;
         header = new DistanceHeader(nid, startTime, frameTime * rolling, rolling, dHeader.deviceIds);
         factory = new distanceFactory();
         break;
       case 'm':
         GeometryHeader gHeader = (GeometryHeader) inHeader;
         header = new GeometryHeader(nid, startTime, frameTime * rolling, gHeader.rows, gHeader.cols);
         factory = new geometryFactory((GeometryHeader) header);
         break;
       default:
        throw new IllegalArgumentException("Don't know how to consolidate " + inHeader.getClass().getSimpleName());
     }
     return header;
   }
 
   public synchronized StreamFrame process(StreamFrame inFrame) {
     if (inFrame == null)
       return null;
 
     frames[frameCnt % combine] = inFrame;
     frameCnt++;
     return (frameCnt >= combine) && ((frameCnt-combine) % rolling == 0) ? factory.makeFrame() : null;
   }
 
   private StreamFrame getFrame(int offset) {
     return frames[(frameCnt + offset)%combine];
   }
 
   public void close() {
     out.close();
     in.close();
   }
 
   interface Factory {
     StreamFrame makeFrame();
   }
 
   class distanceFactory implements Factory {
     public StreamFrame makeFrame() {
       double distanceSum = 0;
       double magnitudeSum = 0;
       for (int i = 0; i < combine; i++) {
         DistanceFrame frame = (DistanceFrame) getFrame(i);
         if (frame == null)
           break;
         for (int j = 0;j < frame.peakDeltas.length;j++) {
           distanceSum += frame.peakDeltas[j] * frame.peakMagnitudes[j];
           magnitudeSum += frame.peakMagnitudes[j];
         }
       }
 
       double deltas[] = { distanceSum / magnitudeSum };
       double mags[] = { magnitudeSum };
       return ((DistanceHeader) header).makeFrame(deltas, mags);
     }
   }
 
   class geometryFactory implements Factory {
     double[][] geometry;
     int rows, cols;
 
     public geometryFactory(GeometryHeader gHeader) {
       rows = gHeader.rows;
       cols = gHeader.cols;
       geometry = new double[rows][cols];
       for (int i = 0;i < rows; i++)
         for (int j = 0; j < cols; j++) {
           geometry[i][j] = Double.NaN;
         }
     }
 
     public StreamFrame makeFrame() {
       for (int f = 0; f < combine; f++) {
         GeometryFrame frame = (GeometryFrame) getFrame(f);
         if (frame == null)
           break;
 
         double[][] input = frame.geometry;
         for (int i = 0;i < rows; i++)
           for (int j = 0; j < cols; j++)
             if (Double.isNaN(geometry[i][j]))
               geometry[i][j] = input[i][j];
             else if (!Double.isNaN(input[i][j]))
               geometry[i][j] = (geometry[i][j]*weight + input[i][j])/( weight + 1);
             else
               geometry[i][j] = geometry[i][j]; // NOP placeholder
 
       }
       return ((GeometryHeader) header).makeFrame(geometry);
     }
   }
 
   class impulseFactory implements Factory {
     public StreamFrame makeFrame() {
       int size = 0;
       for (int i = 0; i < combine; i++) {
         ImpulseFrame frame = (ImpulseFrame) getFrame(i);
         size += frame != null ? frame.peakMagnitudes.length : 0;
       }
       int frameTime = frames[0].getHeader().frameTime;
       int[] offs = new int[size];
       short[] mags = new short[size];
       int pos = 0, shift = 0;
       for (int i = 0; i < combine; i++) {
         // because of circular buffer, adding 1 to index gives us the oldest frame
         ImpulseFrame frame = (ImpulseFrame) getFrame(i);
         if (frame == null)
           break;
         int length = frame.peakMagnitudes.length;
 
         System.arraycopy(frame.peakOffsets, 0, offs, pos, length);
         System.arraycopy(frame.peakMagnitudes, 0, mags, pos, length);
 
         for (int j = 0; j < length; j++) {
           offs[pos + j] += shift;
         }
         pos += length;
         shift += frameTime;
       }
 
       return ((ImpulseHeader) header).makeFrame(offs, mags);
     }
   }
 
   public void go() throws Exception {
     out.setHeader(init(in.getHeader()));
     StreamFrame frame;
     while ((frame = in.recvFrame()) != null) {
       out.sendFrame(process(frame));
     }
     close();
   }
 
  public void open_streams(String type, String outName, String inName) throws Exception {
    consolidateType = type.charAt(0);
     switch(consolidateType) {
       case 'd':
         out = new DistanceFileStream(outName, true);
         in = new DistanceFileStream(inName);
         break;
       case 'i':
         out = new ImpulseFileStream(outName, true);
         in = new ImpulseFileStream(inName);
         break;
       case 'm':
         out = new GeometryFileStream(outName, true);
         in = new GeometryFileStream(inName);
         break;
       default:
        throw new IllegalArgumentException("Consolidate type not recognized: " + type);
     }
   }
 
   public static void main(String[] args) throws Exception {
     int arg = 0;
     String type = args[arg++];
     String[] opts = args[arg++].split("-");
     String outName = args[arg++];
     String inName = args[arg++];
     if (args.length > arg) {
       throw new IllegalArgumentException("Too many input arguments");
     }
     System.out.println("Consolidate " + type + ": " + outName + " " + inName);
     int combine = Integer.parseInt(opts[0]);
     int rolling = Integer.parseInt(opts[1]);
    ConsolidateModule consolidate = new ConsolidateModule(combine, rolling);
    consolidate.open_streams(type, outName, inName);
     consolidate.go();
   }
 }
