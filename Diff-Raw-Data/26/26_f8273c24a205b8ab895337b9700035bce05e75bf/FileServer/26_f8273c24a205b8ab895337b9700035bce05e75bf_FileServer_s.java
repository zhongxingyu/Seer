 package dk.itu.grp11.main;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Date;
 
 import dk.itu.grp11.enums.MinMax;
 
 
 public class FileServer {
   private int height;
   private int width;
   private int x;
   private int y;
   
   private int xStart;
   private int yStart;
   private int xDiff;
   private int yDiff;
   
   private int port;
   private Map map;
 
   private Socket con;
   private BufferedReader in;
   private OutputStream out;
   private PrintStream pout;
 
   public FileServer(int port, Map map) {
     this.port = port;
     this.map = map;
   }
   
   private String getOutput() {
     return 
     		"<html>\n" +
     		"  <body>\n" +
     		"    <h2>Det virker!</h2>\n" +
     		"    <span>x: "+x+" y: "+y+" & height: "+height+" & width: "+width+"</span>\n" +
     		"    <div>\n" +
     		"      <svg id=\"map\" xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" height=\"1080\" width=\"1920\" viewbox=\""+xStart+" "+0+" "+xDiff+" "+yDiff+"\">\n" +
     		map.getPart(x, y, width, height) +
     		"      </svg>\n" +
     		"    </div>\n" + 
     		"  </body>\n" +
     		"</html>";
   }
 
   public void run() {
     ServerSocket ss = null; 
     try {
       ss = new ServerSocket(port); 
     } catch (IOException e) {
       System.err.println("Could not start server: "+e);
       System.exit(-1);
     }
     System.out.println("FileServer accepting connections on port "+
                        port);
             
     while (true) {
       try {
         con = ss.accept();
         in = new BufferedReader
             (new InputStreamReader(con.getInputStream()));
         out = new BufferedOutputStream(con.getOutputStream());
         pout = new PrintStream(out);
 
         String request = in.readLine();
         con.shutdownInput(); // ignore the rest
         log(con, request);
         boolean loadData = splitRequest(request);
         
         processRequest(loadData);
                 
         pout.flush();
       } catch (IOException e) { 
         System.err.println(e); 
       }
       try {
         if (con!=null) 
           con.close(); 
       } catch (IOException e) { 
         System.err.println(e); 
       }
     }
   }
   
   private boolean splitRequest(String request) {
     if (request.indexOf("favicon.ico") != -1) return false;
     if (request.getClass() != String.class) return false;
     String[] tmp = request.split(" ");
     String[] requestSplit = tmp[1].split("\\?");
     if (requestSplit.length == 2) {
       String params = requestSplit[1];
       String[] paramSplit = params.split("&");
       for (String param : paramSplit) {
         String[] parameter = param.split("=");
         switch (parameter[0]) {
         case "height":
           height = Integer.parseInt(parameter[1]);
         break;
         case "width":
           width = Integer.parseInt(parameter[1]);
         break;
         case "x":
           x = Integer.parseInt(parameter[1]);
         break;
         case "y":
           y = Integer.parseInt(parameter[1]);
         break;
         }
       }
     }
     xStart = (int)Math.floor(map.getMinMax(MinMax.MINX.id()));
     yStart = (int)Math.floor(map.getMinMax(MinMax.MINY.id()));
     xDiff = (int)Math.ceil(map.getMinMax(MinMax.MAXX.id())-map.getMinMax(MinMax.MINX.id()));
     yDiff = (int)Math.ceil(map.getMinMax(MinMax.MAXY.id())-map.getMinMax(MinMax.MINY.id()));
     if (height == 0) height = yDiff;
     if (width == 0) width = xDiff;
     if (x == 0) x = xStart;
     if (y == 0) y = yStart;
     return true;
   }
 
   private void processRequest(boolean loadData) throws IOException {
     InputStream outStream = null;
     if (loadData) outStream = new ByteArrayInputStream(getOutput().getBytes("UTF-8"));
     else outStream = new ByteArrayInputStream("".getBytes("UTF-8"));
     pout.print("HTTP/1.0 200 OK\r\n");
    pout.print("Date: "+new Date()+"\r\n"+
                "Server: IXWT FileServer 1.0\r\n\r\n");
     sendOutput(outStream, out); // send raw output
     log(con, "200 OK");
   }
     
   private void log(Socket con, String msg) {
     System.err.println(new Date()+" ["+
                        con.getInetAddress().getHostAddress()+
                        ":"+con.getPort()+"] "+msg);
   }
   
   private void sendOutput(InputStream outStream, OutputStream out) 
       throws IOException {
     byte[] buffer = new byte[1000];
     while (outStream.available()>0) 
       out.write(buffer, 0, outStream.read(buffer));
   }
 }
 
