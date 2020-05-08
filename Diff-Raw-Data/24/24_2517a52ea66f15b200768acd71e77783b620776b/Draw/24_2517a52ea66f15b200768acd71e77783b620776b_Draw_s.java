 /* This is a port of the JGroups Draw demo
  * by Yann Sionneau <yann.sionneau@telecom-sudparis.eu>
  */
 
package net.sionneau;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Random;
 
 import net.sionneau.TouchSurface.MySurface;
 
 import org.jgroups.*;
 import org.jgroups.util.Util;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Point;
 import android.util.Log;
 
 public class Draw extends ExtendedReceiverAdapter implements ChannelListener {
 
     String                         groupname="DrawGroupDemo";
     private Channel                channel=null;
     private int                    member_size=1;
     static final boolean           first=true;
     boolean                        no_channel=false;
     private DrawPanel              panel=null;
     boolean                        jmx;
     private boolean                use_state=false;
     private final Integer          draw_color;
     private final Random           random=new Random(System.currentTimeMillis());
     private long                   state_timeout=5000;
     private Paint paint;
     private Canvas surface;
     private MySurface view;
     
     public Draw(String props, boolean no_channel, boolean jmx, boolean use_state, long state_timeout,
             boolean use_blocking, Canvas surface, MySurface view) throws Exception {
     this.paint = new Paint();
     draw_color = selectColor();
     this.no_channel=no_channel;
     this.jmx=jmx;
     this.use_state=use_state;
     this.state_timeout=state_timeout;
     this.panel = new DrawPanel(use_state, surface, view);
     this.surface = surface;
     this.view = view;
     
     paint.setColor(0xFF0000FF);
     
     if(no_channel)
         return;
 
     channel=new JChannel(props);
     if(use_blocking)
         channel.setOpt(Channel.BLOCK, Boolean.TRUE);
     channel.setOpt(Channel.AUTO_RECONNECT, Boolean.TRUE);
     channel.setReceiver(this);
     channel.addChannelListener(this);
 }
 
 public void TouchEvent(float x, float y) {
 	if (panel == null)
 		Log.e("TouchSurface", "panel == null dans Draw.TouchEvent");
 	panel.mouseDragged(x,y);
 	
 }
     
 public Draw(Channel channel, Canvas surface, MySurface view) throws Exception {
     this.channel = channel;
     this.surface = surface;
     this.view = view;
     draw_color = selectColor();
     this.panel = new DrawPanel(use_state, surface, view);
     channel.setOpt(Channel.AUTO_RECONNECT, Boolean.TRUE);
     channel.setReceiver(this);
     channel.addChannelListener(this);
 }
 
 
 public Draw(Channel channel, boolean use_state, long state_timeout, Canvas surface, MySurface view) throws Exception {
     this.channel=channel;
     draw_color = selectColor();
     this.use_state=use_state;
     this.state_timeout=state_timeout;
     this.surface = surface;
     this.view = view;
     this.panel = new DrawPanel(use_state, surface, view);
     channel.setOpt(Channel.AUTO_RECONNECT, Boolean.TRUE);
     channel.setReceiver(this);
     channel.addChannelListener(this);
 }
 
 
 public String getGroupName() {
     return groupname;
 }
 
 public void setGroupName(String groupname) {
     if(groupname != null)
         this.groupname=groupname;
 }
 
 static void help() {
     System.out.println("\nDraw [-help] [-no_channel] [-props <protocol stack definition>]" +
                        " [-groupname <name>] [-state] [-use_blocking] [-timeout <state timeout>] [-bind_addr <addr>]");
     System.out.println("-no_channel: doesn't use JGroups at all, any drawing will be relected on the " +
                        "whiteboard directly");
     System.out.println("-props: argument can be an old-style protocol stack specification, or it can be " +
                        "a URL. In the latter case, the protocol specification will be read from the URL\n");
 }
 
 private Integer selectColor() {
     int red=(Math.abs(random.nextInt()) % 255);
     int green=(Math.abs(random.nextInt()) % 255);
     int blue=(Math.abs(random.nextInt()) % 255);
     return new Integer(Color.rgb(red, green, blue));
 }
 
 public void go() throws Exception {
     if(!no_channel && !use_state) {
         channel.connect(groupname);
     }
     if(!no_channel && use_state) {
         channel.connect(groupname,null,null, state_timeout);
     }
     setTitle();
 }
 
 
 
 
 void setTitle(String title) {
     String tmp="";
     if(no_channel) {
         return;
     }
     if(title == null) {
         if(channel.getLocalAddress() != null)
             tmp+=channel.getLocalAddress();
         tmp+=" (" + member_size + ")";
     }
 }
 
 void setTitle() {
     setTitle(null);
 }
 
 
 
 public void receive(Message msg) {
     byte[] buf=msg.getRawBuffer();
     if(buf == null) {
     	Log.e("touchsurface", "[" + channel.getLocalAddress() + "] received null buffer from " + msg.getSrc() +
                 ", headers: " + msg.printHeaders());
         System.err.println("[" + channel.getLocalAddress() + "] received null buffer from " + msg.getSrc() +
                 ", headers: " + msg.printHeaders());
         return;
     }
 
     try {
         DrawCommand comm=(DrawCommand)Util.streamableFromByteBuffer(DrawCommand.class, buf, msg.getOffset(), msg.getLength());
         switch(comm.mode) {
             case DrawCommand.DRAW:
                 if(panel != null)
                     panel.drawPoint(comm);
                 break;
             case DrawCommand.CLEAR:
                 clearPanel();
                 break;
             default:
             	Log.e("touchsurface", "***** received invalid draw command " + comm.mode);
                 System.err.println("***** received invalid draw command " + comm.mode);
                 break;
         }
     }
     catch(Exception e) {
         e.printStackTrace();
     }
 }
 
 public void viewAccepted(View v) {
     if(v instanceof MergeView)
         System.out.println("** MergeView=" + v);
     else
         System.out.println("** View=" + v);
     member_size=v.size();
     setTitle();
 }
 
 public void block() {
     System.out.println("--  received BlockEvent");
 }
 
 public void unblock() {
     System.out.println("-- received UnblockEvent");
 }
 
 
 public byte[] getState() {
     return panel.getState();
 }
 
 public void setState(byte[] state) {
     panel.setState(state);
 }
 
 
 public void getState(OutputStream ostream) {
     try {
         try {
             panel.writeState(ostream);
         }
         catch(IOException e) {
             e.printStackTrace();
         }
     }
     finally {
         Util.close(ostream);
     }
 }
 
 public void setState(InputStream istream) {
     try {
         try {
             panel.readState(istream);
         }
         catch(IOException e) {
             e.printStackTrace();
         }
     }
     finally {
         Util.close(istream);
     }
 }
 
 /* --------------- Callbacks --------------- */
 
 
 
 public void clearPanel() {
     if(panel != null)
         panel.clear();
 }
 
 public void sendClearPanelMsg() {
     DrawCommand comm=new DrawCommand(DrawCommand.CLEAR);
 
     try {
         byte[] buf=Util.streamableToByteBuffer(comm);
         channel.send(new Message(null, null, buf));
     }
     catch(Exception ex) {
         System.err.println(ex);
     }
 }
 
 public void stop() {
     if(!no_channel) {
         try {
             channel.close();
         }
         catch(Exception ex) {
             System.err.println(ex);
         }
     }
 //    mainFrame.setVisible(false);
 //    mainFrame.dispose();
 }
 
 
 /* ------------------------------ ChannelListener interface -------------------------- */
 
 public void channelConnected(Channel channel) {
 
 }
 
 public void channelDisconnected(Channel channel) {
 
 }
 
 public void channelClosed(Channel channel) {
 
 }
 
 public void channelShunned() {
     System.out.println("-- received EXIT, waiting for ChannelReconnected callback");
     setTitle(" Draw Demo - shunned ");
 }
 
 public void channelReconnected(Address addr) {
     setTitle();
 }
 
 
 /* --------------------------- End of ChannelListener interface ---------------------- */
 
 
 
 private class DrawPanel {
     final Map<Point, Integer>  state;
 	private Canvas surface;
 	private MySurface view;
 
 
     public DrawPanel(boolean use_state, Canvas surface, MySurface view) {
         if(use_state)
             state=new LinkedHashMap<Point, Integer>();
         else
             state=null;
         
         this.surface = surface;
         this.view = view;
 /*        createOffscreenImage(false);
         addMouseMotionListener(this);
         addComponentListener(new ComponentAdapter() {
             public void componentResized(ComponentEvent e) {
                 if(getWidth() <= 0 || getHeight() <= 0) return;
                 createOffscreenImage(false);
             }
         });*/
     }
 
 
     public byte[] getState() {
         byte[] retval=null;
         if(state == null) return null;
         synchronized(state) {
             try {
                 retval=Util.objectToByteBuffer(state);
             }
             catch(Exception e) {
                 e.printStackTrace();
             }
         }
         return retval;
     }
 
 
     public void setState(byte[] buf) {
         synchronized(state) {
             try {
                 Map<Point, Integer> tmp=(Map<Point,Integer>)Util.objectFromByteBuffer(buf);
                 state.clear();
                 state.putAll(tmp);
                 Log.e("TouchSurface", "received state: " + buf.length + " bytes, " + state.size() + " entries");
                 System.out.println("received state: " + buf.length + " bytes, " + state.size() + " entries");
               //  createOffscreenImage(true);
             }
             catch(Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     public void writeState(OutputStream outstream) throws IOException {
         synchronized(state) {
             if(state != null) {
                 DataOutputStream dos=new DataOutputStream(outstream);
                 dos.writeInt(state.size());
                 Point point;
                 for(Map.Entry<Point,Integer> entry: state.entrySet()) {
                     point=entry.getKey();
                     int col = entry.getValue();
                     dos.writeInt(point.x);
                     dos.writeInt(point.y);
                     dos.writeInt(Color.red(col));
                     dos.writeInt(Color.green(col));
                     dos.writeInt(Color.blue(col));
                 }
                 dos.flush();
                 Log.e("touchsurface", "On crit !");
             }
         }
     }
 
 
     public void readState(InputStream instream) throws IOException {
         DataInputStream in=new DataInputStream(instream);
         Map<Point,Integer> new_state=new HashMap<Point,Integer>();
         int num=in.readInt();
         Point point;
         Integer col;
         for(int i=0; i < num; i++) {
             point = new Point(in.readInt(), in.readInt());
             col = new Integer(in.readInt());
             new_state.put(point, col);
         }
 
         synchronized(state) {
             state.clear();
             state.putAll(new_state);
             Log.e("touchsurface", "read state: " + state.size() + " entries");
             System.out.println("read state: " + state.size() + " entries");
             //createOffscreenImage(true);
         }
     }
 
 
     /* ---------------------- MouseMotionListener interface------------------------- */
     public void mouseDragged(float x, float y) {
         DrawCommand         comm=new DrawCommand(DrawCommand.DRAW, (int)x, (int)y, Color.red(draw_color), Color.green(draw_color), Color.blue(draw_color));
 
         if(no_channel) {
             drawPoint(comm);
             return;
         }
 
         try {
             byte[] buf=Util.streamableToByteBuffer(comm);
             channel.send(new Message(null, null, buf));
             // Thread.yield();
         }
         catch(Exception ex) {
             System.err.println(ex);
         }
     }
 
     /* ------------------- End of MouseMotionListener interface --------------------- */
 
     public void drawPoint(DrawCommand c) {
         if(c == null) return;
         Integer col=new Integer(Color.rgb(c.r, c.g, c.b));
         
         // We draw a circle and then we refresh
         Paint p = new Paint();
         p.setColor(col.intValue());
         if (this.surface == null)
         		Log.e("TouchSurface", "surface == null dans Draw.drawPoint() class == " + this.getClass());
         	else
         		this.surface.drawCircle(c.x, c.y, 5, p);
         this.view.invalidate();
 
         if(state != null) {
             synchronized(state) {
                 state.put(new Point(c.x, c.y), col);
             }
         }
     }
     
     public void clear() {
     	
     	// Here we clean the screen
     	
     	surface.drawColor(Color.WHITE);
     	view.invalidate();
         if(state != null) {
             synchronized(state) {
                 state.clear();
             }
         }
     }
     
     
 }
 }
