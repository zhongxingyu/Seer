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
     private double lastPos[] = new double[5];
     private boolean isCancelled = false;
     private boolean isHost;
 
 
     GameSocket() { }
 
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
         return socket == null ? false : socket.isConnected();
     }
     
     // returns true if the socket is closed NOT WORKING AS EXPECTED
     public boolean isClosed() {
        return (socket == null) ? true : socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown();
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
                 in  = socket.getInputStream();
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
             } catch (Exception e) { }
         } else {
             isCancelled = true;
         }
 
     }
 
     // writes the position of the ball and the bar in the stream and flushes it
     public void writePositions(double ballX, double ballY, double block, int[] score) {
         try {
             out.write(ByteBuffer.allocate(8).putDouble(ballX).array());
             out.write(ByteBuffer.allocate(8).putDouble(ballY).array());
             out.write(ByteBuffer.allocate(8).putDouble(block).array());
             out.write(ByteBuffer.allocate(4).putInt(score[0]).array());
             out.write(ByteBuffer.allocate(4).putInt(score[1]).array());
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
             while(in.available() >= 8*3 + 4*2) {
                 byte[] b1 = new byte[8];
                 byte[] b2 = new byte[8];
                 byte[] b3 = new byte[8];
                 byte[] b4 = new byte[4];
                 byte[] b5 = new byte[4];
                 if(in.read(b1) + in.read(b2) + in.read(b3) + in.read(b4) + in.read(b5) < 8*3 + 4*2) {
                     return lastPos;
                 }
                 ByteBuffer ballX = ByteBuffer.wrap(b1);
                 ByteBuffer ballY = ByteBuffer.wrap(b2);
                 ByteBuffer block = ByteBuffer.wrap(b3);
                 ByteBuffer score[] = new ByteBuffer[]{ByteBuffer.wrap(b4), ByteBuffer.wrap(b5)};
                 lastPos = new double[]{ballX.getDouble(), ballY.getDouble(), block.getDouble(), score[0].getInt(), score[1].getInt()};
                 return lastPos;
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
                 lastValue = ByteBuffer.wrap(b).getDouble();
                 return lastValue;
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
