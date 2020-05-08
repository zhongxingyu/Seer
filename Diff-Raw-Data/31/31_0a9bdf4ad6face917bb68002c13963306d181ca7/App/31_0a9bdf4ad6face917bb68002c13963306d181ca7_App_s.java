 package edu.cmu.pandaa.framework;
 
 import java.awt.image.DirectColorModel;
 import java.io.File;
 import java.net.ServerSocket;
 import java.util.*;
 
 import edu.cmu.pandaa.desktop.LiveAudioStream;
 import edu.cmu.pandaa.header.StreamHeader;
 import edu.cmu.pandaa.header.StreamHeader.StreamFrame;
 import edu.cmu.pandaa.module.StreamModule;
 import edu.cmu.pandaa.stream.*;
 
 // server app
 public class App {
   static final int STARTUP_DELAY = 500;
   static final int SERVER_PORT = 12345;
   MultiFrameStream combiner = new MultiFrameStream("combiner");
   final Map<StreamHeader, PipeHandler> inHeaders = new HashMap<StreamHeader, PipeHandler>();
   private int basePort = 8000;
   private int nextDevicePort = basePort + 20;
   private int nextCombinePort = basePort + 40;
   private final int SEGMENT_LENGTH_MS = 100 * 1000;
 
   public static final String TRACE_DIR = "trace/";
 
   public App(String[] args) throws Exception {
     new File(TRACE_DIR).mkdir();
 
     if (args.length == 0) {
       new AcceptClients().start();
     } else if (args[0].equalsIgnoreCase("live")) {
       List<LiveAudioStream> streams = LiveAudioStream.getLiveAudioStreams(TRACE_DIR, -1, SEGMENT_LENGTH_MS);
       for (LiveAudioStream in : streams) {
         activateNewDevice(in);
       }
     } else if (new File(args[0]).isDirectory()) {
       if (args.length == 1) {
         args = processDirectory(args[0]);
       }
       for (String fileBase : args) {
         List<String> fileNames = getFileSet(fileBase);
         RawAudioFileStream in = new RawAudioFileStream(fileNames);
        in.setTimeDialtion(1.0);
         activateNewDevice(in);
       }
     } else {
       if (args.length == 1) {
         args = expandFiles(args[0]);
       }
       String file1 = args[0];
       for (String file : args) {
         RawAudioFileStream in = new RawAudioFileStream(file, file1, 60);
         in.setTimeDialtion(1.0);
         activateNewDevice(in);
       }
     }
 
     PipeHandler combpipe = new PipeHandler(combiner, new MergePipeline(),
             new GeometryFileStream(TRACE_DIR + "output.txt", true), basePort, true);
     new Thread(combpipe, "combiner").start();
   }
 
   private String[] processDirectory(String base) {
     SortedSet<String> files = new TreeSet<String>();
     String[] flist = new File(base).list();
     if (!base.endsWith(File.separator)) {
       base = base + File.separator;
     }
     for (String file : flist) {
       int index = file.indexOf('_');
       files.add(base + file.substring(0,index));
     }
     return files.toArray(new String[] {});
   }
 
   private List<String> getFileSet(String base) {
     SortedSet<String> files = new TreeSet<String>();
     String dir = new File(base).getParent();
     base = base.substring(dir.length()+1);
     String[] flist = new File(dir).list();
     for (String file : flist) {
       if (file.startsWith(base)) {
         files.add(dir + File.separatorChar + file);
       }
     }
     List<String> retlist = new LinkedList<String>();
     retlist.addAll(files);
     return retlist;
   }
 
   private String[] expandFiles(String base) {
     ArrayList<String> files = new ArrayList<String>();
     String dir = new File(base).getParent();
     base = base.substring(dir.length()+1);
     String[] flist = new File(dir).list();
     for (String file : flist) {
       if (file.startsWith(base) && file.endsWith(".wav")) {
         files.add(dir + File.separatorChar + file);
       }
     }
     return files.toArray(new String[] {});
   }
 
   public static void main(String[] args) {
     try {
       new App(args);
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
 
   private String makeId(StreamHeader a, StreamHeader b) {
     String aid = a.id;
     String bid = b.id;
     if (aid.equals(bid)) {
       throw new IllegalArgumentException("Streamin IDs must be unique!");
     }
 
     if (aid.compareTo(bid) < 0) {
       String tmp = bid;
       bid = aid;
       aid = tmp;
     }
 
     return aid + "+" + bid;
   }
 
   private synchronized void activateNewDevice(FrameStream in) throws Exception {
     StreamHeader inHeader = in.getHeader();
     System.out.println("Activating device " + inHeader.id + " on " + nextDevicePort);
     StreamModule pipeline = new SinglePipeline();
     PipeHandler pipe = new PipeHandler(in, pipeline, null, nextDevicePort++);
     new Thread(pipe, inHeader.id).start();
 
     synchronized (inHeaders) {
       for (StreamHeader other : inHeaders.keySet()) {
         String id = makeId(inHeader, other);
         MultiFrameStream mixer = new MultiFrameStream(id);
         PipeHandler otherPipe = inHeaders.get(other);
         pipe.addOutput(mixer);
         otherPipe.addOutput(mixer);
         PipeHandler dualPipe = new PipeHandler(mixer, new DualPipeline(inHeaders.keySet()),
                 combiner, nextCombinePort++);
         new Thread(dualPipe, id).start();
       }
 
       inHeaders.put(inHeader, pipe);
     }
   }
 
 // server thread, spawning off one client thread per connection
 class AcceptClients extends Thread {
   ServerSocket server;
 
   @Override
   public void run() {
     try {
       server = new ServerSocket(SERVER_PORT);   // launch server on any free port and start listening for incoming connections
       System.out.println("Server started at " + server.getInetAddress().getHostAddress() + ":" + server.getLocalPort());
 
       while (true) {
         activateNewDevice(new SocketStream(server.accept()));
       }
     }
     catch (Exception e) {
       System.out.println("Error, closing connection.");
       e.printStackTrace();
     }
   }
 }
 
 class PipeHandler implements Runnable {
   private final FrameStream in;
   private final Set<FrameStream> outSet = new HashSet<FrameStream>();
   private final StreamModule pipeline;
   private final String id;
   private StreamHeader outHeader;
   private boolean closed = false;
   private int count = 0;
   private boolean trace;
   private WebViewStream view;
 
   PipeHandler(FrameStream in, StreamModule pipeline, FrameStream out, int port) throws Exception {
     this(in, pipeline, out, port, false);
   }
 
   PipeHandler(FrameStream in, StreamModule pipeline, FrameStream out, int port, boolean trace) throws Exception {
     this.trace = trace;
     if (in == null)
       throw new IllegalArgumentException("argument can not be null");
 
     this.in = in;
     if (out != null) {
       outSet.add(out);
     }
     String pipeName = pipeline.getClass().getSimpleName();
     id = pipeName + '.' + in.getHeader().id;
     this.pipeline = pipeline;
     if (trace)
       System.err.println("Pipeline " + id + " created with " + outSet.size());
     view = new WebViewStream(port);
   }
 
   public void addOutput(MultiFrameStream out) throws Exception {
     if (trace)
       System.err.println("Adding stream " + out.id + " to " + id);
     if (closed) {
       throw new IllegalStateException("Pipeline closed");
     }
     String outId = out.id;
     synchronized(outSet) {
       System.out.println("Adding output " + outId + " from pipe " + id + " at frame " + count);
       outSet.add(out);
       if (outHeader != null)
         out.setHeader(outHeader);
     }
   }
 
   @Override
   public void run() {
     try {
       if (trace)
         System.err.println("Running stream " + id);
       Thread.sleep(STARTUP_DELAY);
 
       synchronized(outSet) {
         outHeader = pipeline.init(in.getHeader());
         for (FrameStream out : outSet) {
           out.setHeader(outHeader);
         }
         view.setHeader(outHeader);
       }
 
       try {
         while (true) {
           StreamFrame frame = in.recvFrame();
           if (frame == null)
             break;
           if (trace) {
             System.err.println(frame.toString());
           }
           frame = pipeline.process(frame);
           view.sendFrame(frame);
           count++;
           synchronized(outSet) {
             for (FrameStream out : outSet) {
               out.sendFrame(frame);
             }
           }
         }
       } catch (Exception e) {
         e.printStackTrace();
       }
 
       synchronized(outSet) {
         System.out.println("Done with pipe " + id + " count=" + count);
         closed = true;
         for (FrameStream out : outSet) {
           out.close();
         }
         pipeline.close();
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
 }
 }
