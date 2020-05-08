 import java.io.*;
 import java.net.*;
 import java.nio.ByteBuffer;
 
 public class GameSocket implements Runnable {
 
     private Socket socket;
     private ServerSocket listener;
     private String host;
     private int port = 1100;
 
     private OutputStream out;
     private InputStream in;
 
     private double lastValue = 0;
     private double lastPos[] = new double[3];
     private boolean isCancelled = false;
     private boolean isHost;
 
 
     GameSocket() {
 
     }
 
     // attempts to connect to the host
     // does not block until a connection is established
     public void connect(String host, boolean isHost) {
         this.isHost = isHost;
         socket = new Socket();
         isCancelled = false;
         this.host = host;
         System.out.println("starting");
         new Thread(this).start();
     }
 
     // returns true if the socket is connected
     public boolean isConnected() {
         if(socket == null) {
             return false;
         }
         return socket.isConnected();
     }
 
     // does the actual connecting
     public void run() {
         try {
             while(!isCancelled) {
                 System.out.println("Waiting");
                 try {
                     if(isHost) {
                         System.out.println("Waiting for socket");
                         listener = new ServerSocket(port);
                         socket = listener.accept();
                     } else {
                         System.out.println("creating Socket");
                         socket = new Socket();
                         socket.connect(new InetSocketAddress(this.host, this.port));
                     }
                 } catch (Exception e) {
                     System.out.println(e.toString());
                     Thread.sleep(1000);
                     continue;
                 }
                 out = socket.getOutputStream();
                in = socket.getInputStream();
                 return;
             }
             System.out.println("Cancel");
         } catch (Exception e) {
             System.out.println("error: " + e.toString());
         }
     }
 
     // cancels the current connection attempt
     public void cancel() {
         if(isHost) {
             try {
                 listener.close();
            } catch (Exception e) {

            }

         } else {
             isCancelled = true;
         }
 
     }
 
     // writes the position of the ball and the bar in the stream and flushes it
     public void writePositions(double ballX, double ballY, double block) {
         try {
             out.write(ByteBuffer.allocate(8).putDouble(ballX).array());
             out.write(ByteBuffer.allocate(8).putDouble(ballY).array());
             out.write(ByteBuffer.allocate(8).putDouble(block).array());
             out.flush();
         } catch (IOException e) {
             // will probably never happen
         }
     }
 
     public void writeBar(double block) {
         try {
             out.write(ByteBuffer.allocate(8).putDouble(block).array());
             out.flush();
         } catch (IOException e) {
             // will probably never happen
         }
     }
 
     // returns the current position of the other player's bar
     public double[] getPositions() {
         try {
             while(in.available() >= 8*3) {
                 byte[] b1 = new byte[8];
                 byte[] b2 = new byte[8];
                 byte[] b3 = new byte[8];
                 if(in.read(b1) + in.read(b2) + in.read(b3) < 8*3) {
                     return lastPos;
                 }
                 ByteBuffer ballX = ByteBuffer.wrap(b1);
                 ByteBuffer ballY = ByteBuffer.wrap(b2);
                 ByteBuffer block = ByteBuffer.wrap(b3);
                 return new double[]{ballX.getDouble(), ballY.getDouble(), block.getDouble()};
             }
             return lastPos;
         } catch (IOException e) {
             return lastPos;
         }
     }
 
     // returns the current position of the other player's bar
     public double getBlock() {
         try {
             while(in.available() >= 8) {
                 byte[] b = new byte[8];
                 if(in.read(b) < 8) {
                     return lastValue;
                 }
                 ByteBuffer block = ByteBuffer.wrap(b);
                 return block.getDouble();
             }
             return lastValue;
         } catch (IOException e) {
             return lastValue;
         }
     }
 
     public boolean isHost() {
         return isHost;
     }
 
     // disconnects
     public void disconnect() {
         try {
             if(isHost) {
                 listener.close();
             }
             in.close();
             out.close();
             socket.close();
         }
         catch (IOException e) { }
         catch (NullPointerException e) { }
     }
 
 }
