 import processing.core.*; 
 //import processing.xml.*; 
 
 import com.rapplogic.xbee.examples.wpan.*; 
 import org.apache.log4j.or.*; 
 import org.apache.log4j.lf5.viewer.*; 
 import org.apache.log4j.varia.*; 
 import com.rapplogic.xbee.examples.zigbee.*; 
 import org.apache.log4j.lf5.viewer.configure.*; 
 import com.rapplogic.xbee.api.*; 
 
 import org.apache.log4j.*; 
 import com.rapplogic.xbee.*; 
 import netP5.*; 
 import org.apache.log4j.xml.*; 
 import org.apache.log4j.jmx.*; 
 import gnu.io.*; 
 import org.apache.log4j.lf5.*; 
 import org.apache.log4j.config.*; 
 import org.apache.log4j.helpers.*; 
 import oscP5.*; 
 import org.apache.log4j.chainsaw.*; 
 import org.apache.log4j.or.sax.*; 
 import com.rapplogic.xbee.api.zigbee.*; 
 import rwmidi.*; 
 
 import org.apache.log4j.spi.*; 
 import com.rapplogic.xbee.api.wpan.*; 
 import org.apache.log4j.jdbc.*; 
 import org.apache.log4j.nt.*; 
 import org.apache.log4j.or.jms.*; 
 import com.rapplogic.xbee.test.*; 
 import org.apache.log4j.lf5.util.*; 
 import org.apache.log4j.net.*; 
 import com.rapplogic.xbee.examples.*; 
 import processing.serial.*; 
 import org.apache.log4j.lf5.viewer.categoryexplorer.*; 
 import com.rapplogic.xbee.util.*; 
 
 import java.applet.*; 
 import java.awt.Color;
 import java.awt.Dimension; 
 import java.awt.Frame; 
 import java.awt.event.MouseEvent; 
 import java.awt.event.KeyEvent; 
 import java.awt.event.FocusEvent; 
 import java.awt.Image; 
 import java.io.*; 
 import java.net.*; 
 import java.text.*; 
 import java.util.*; 
 import java.util.zip.*; 
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.regex.*; 
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import controlP5.*;
 import rwmidi.*;
 
 public class Modulome extends processing.core.PApplet
 {
 	/* Fields */
 
 	private boolean xbeeOn = true;
 
 
 
 	//MIDI
 	private MidiOutput midiout;
 	private MidiInput midiin;
 
 	//OSC
 
 	private OscP5 oscP5;
 	private NetAddress myRemoteLocation;
 	private int port = 8080;
	public String pre = "40h";
 
 
 
 
 	private Module m;
 	private Map<Integer,Integer> modcol;
 	private List<Module> modules;
 	private int[] colours;
 	private int index = -1;
 	private int modsize = 320;
 	private boolean display;
 	private int buttondown;
 	private int mouseisin;
 	private boolean newmod = true;
 	public ControlP5 cp5;
 	public javax.swing.JFrame frameguy;
 	com.rapplogic.xbee.api.XBee xbee;
 	Queue<XBeeResponse> queue = new ConcurrentLinkedQueue<XBeeResponse>();
 
 	boolean message;
 	com.rapplogic.xbee.api.XBeeResponse response;
 
 	/* Constructors */
 	public Modulome(javax.swing.JFrame Modulome) {
 		frameguy = Modulome;
 
 		modcol = new HashMap<Integer,Integer>();
 		modcol.put(23, 0xFF041B76);
 		modcol.put(22, 0xFFAF8719);
 		modcol.put(0, 0xFF95310C);
 		//index = 1;
 	}
 
 
 	/* Methods */
 	public void setup() {
 
 		cp5 = new ControlP5(this);
 		cp5.setFont(createFont("ComicSansMS-Bold-22", (float) (modsize * 0.04), true));
 		modules = new ArrayList<Module>();
	//	this.println(RWMidi.getOutputDevices());
 
 
 
 
 		//SETUP MIDI JUNK
 		midiout = RWMidi.getOutputDevice("Modulome Apple Inc.").createOutput();
 		//midiout = RWMidi.getOutputDevices()[2].createOutput();
 		midiin = RWMidi.getInputDevice("PD OUT Apple Inc.").createInput();
 
 		//Setup my OSC JUNK
 		oscP5 = new OscP5(this, 8080);
 		myRemoteLocation = new NetAddress("127.0.0.1", 8000);
 
 		//OSC PLUGS FOR MONOME
 		oscP5.plug(this,"changeoscprefix","/sys/prefix");
 		oscP5.plug(this,"changeoscport","/sys/port");
 		oscP5.plug(this,"changeoscinfo","/sys/info");
 
 		
 
 		//ARC PLUGS
 		oscP5.plug(this,"ringrange", "/ring/range");
 		oscP5.plug(this,"ringall", "/ring/all");
 		oscP5.plug(this,"ringmap", "/ring/map");
 		oscP5.plug(this,"ringset", "/ring/set");
 
 		//GRID PLUGS
 		oscP5.plug(this,"gridledrow", "/grid/led/row");
 		oscP5.plug(this,"gridledcol", "/grid/led/col");
 		oscP5.plug(this,"gridledset", "/grid/led/set");
 		oscP5.plug(this,"gridledall", "/grid/led/all");
 		oscP5.plug(this,"gridledrow", "/osc/grid/led/row");
 		oscP5.plug(this,"gridledcol", "/osc/grid/led/col");
 		oscP5.plug(this,"gridledset", "/osc/grid/led/set");
 		oscP5.plug(this,"gridledall", "/osc/grid/led/all");
 
 
 
 
 
 
 
 		//setSize(modsize/3,modsize/2);
 		if (xbeeOn == true){
 			startxbee();
 		}
 		frameguy.setSize(modsize, modsize);
 
 	}
 
 	public void draw() {
 		background(0);
 
 		readPackets();
 
 		if ((modules.size() <= 0)){
 			stroke(255);
 			fill(255);
 			textSize(16);
 			textAlign(CENTER);
 			text("Waiting for Modules",modsize/2,modsize/2);
 		}
 		if ((modules.size() + 1) != 0)
 			for (Module m : modules) m.draw(this);
 		else {
 
 		}
 
 	}
 
 	public void noteOnReceived(Note thenote){
 
 		//	println(thenote.getPitch());
 
 
 	}
 
 	public void keyPressed() {
 
 
 		switch(key){
 
 		case 'm' :
 			index++;
 			//0xFF337463
 			if(random(2)>3.0){
 				modules.add(new EncoderModule(index,color(random(120),random(120),random(120)) , modsize, (int)random(200), this,cp5));
 
 			}
 			else{
 				modules.add(new ButtonPadModule(index,color(random(120),random(120),random(120)) , modsize, (int)random(200), this,cp5,midiout,midiin,oscP5,myRemoteLocation));
 			}
 			setSize((index >= 4) ? 4 * modsize : index * modsize + modsize, (index/4) * modsize+modsize);
 			frameguy.setSize(this.getWidth(), this.getHeight());
 
 			break;
 
 		case '1' : if(modules.size() >=1 ) moduleremover(0);
 		break;
 		case '2' : if(modules.size() >= 2)moduleremover(1);
 		break;
 		case '3' : if(modules.size() >= 3)moduleremover(2);
 		break;
 		case '4' :if(modules.size() >= 4) moduleremover(3);
 		break;
 		case '5' : if(modules.size() >= 5)moduleremover(4);
 		break;
 		case '6' : if(modules.size() >=6)moduleremover(5);
 		break;
 		case '7' : if(modules.size() >= 7)moduleremover(6);
 		break;
 		case '8' : if(modules.size() >= 8)moduleremover(7);
 		break;
 		case '9' : if(modules.size() >= 9)moduleremover(8);
 		break;
 		case '0' : if(modules.size() >= 10)moduleremover(9);
 		break;
 		case '-' : if(modules.size() >= 11)moduleremover(10);
 		break;
 		case '=' : if(modules.size() >= 12)moduleremover(11);
 		break;
 
 
 
 
 		}
 	}
 
 	static public void main(String args[]) {
 		//PApplet.main(new String[] { "--bgcolor=#000000", "Modulome"});
 		JFrame f = new JFrame("Modulome Serial Daemon");
 		f.setBackground(Color.black);
 
 		Modulome m = new Modulome(f);
 		f.setSize(m.modsize,m.modsize);
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		f.setFocusable(true);
 		m.init();
 		f.add(m);
 		f.setUndecorated(true);
 		f.setVisible(true);
 		f.setResizable(false);
 
 	}
 
 	public void mousePressed() {
 
 		if (modules.size() > 0){
 
 
 
 
 			mouseisin = (mouseX/modsize) + ((mouseY/modsize)*4);
 			if (mouseisin < modules.size()){
 				if (mouseButton == LEFT){
 					modules.get(mouseisin).mousePressed(mouseButton, 
 							mouseX - modules.get(mouseisin).getXposition(),
 							mouseY - modules.get(mouseisin).getYposition());
 					buttondown = 1;
 				}
 				else if (mouseButton == RIGHT){
 
 					modules.get(mouseisin).settings();	
 				}
 
 			}
 		}
 	}
 
 
 
 
 	public void mouseDragged() {
 
 		if(buttondown == 1){
 
 
 			if (modules.get(mouseisin) != null){
 				modules.get(mouseisin).mouseMoved(mouseButton, mouseX - modules.get(mouseisin).getXposition(),
 						mouseY - modules.get(mouseisin).getYposition());
 			}
 		}
 
 	}
 
 	public void mouseReleased() {
 		buttondown = 0;
 	}
 
 	public void moduleremover(int theid) {
 		modules.get(theid).displaySettings();
 
 		modules.get(theid).remove();
 
 		modules.remove(theid);
 
 		for ( int i = theid; i < modules.size();i++){
 			modules.get(i).setID(modules.get(i).getID()-1);
 			modules.get(i).modulemoved();
 			//println(modules.get(i).getID());
 		}
 		index = modules.size()-1;
 		println(index);
 		if (index >= 0){
 			setSize((index >= 4) ? 4 * modsize : index * modsize + modsize, (index/4) * modsize + modsize);
 		}
 
 		else if (index < 0)setSize(modsize,modsize);
 		frameguy.setSize(this.getWidth(), this.getHeight());
 
 	}
 
 	void readPackets() {
 
 		while ((response = queue.poll()) != null) {
 			// println(getProcessedPacketBytes());
 			try {
 				if (!(response instanceof RxResponse)) {
 					TxStatusResponse txresponse = (TxStatusResponse) response;
 					//TODO SOEMTHING EHSIDNFSRERE
 					System.out.println(response);
 
 					return;
 				} 
 				RxResponse ioSample = (RxResponse) response;
 
 				// println("We received a sample from " + ioSample.getData());
 
 				int[] data = ioSample.getProcessedPacketBytes();
 				int addr = data[4];
 				newmod = true;
 				for (int i = 0; i < modules.size(); i++){
 
 					if (addr == modules.get(i).getaddress()){
 						modules.get(i).processdata(data);
 
 						newmod = false;
 					}
 				}
 				if (newmod == true){
 					newmod = false;
 
 					index++;
 
 					int c; 
 					if (modcol.get(addr) != null)
 					{ 
 						c = modcol.get(addr);
 						println(modcol.get(addr));
 					}
 					else {
 						c = color(random(120),random(120),random(120));
 
 					}
 					if (addr < 8){ 
 						modules.add(new FaderModule(index,c , modsize, addr, this,cp5,midiout));
 
 
 					}
 
 					else if ((addr >= 8)&&(addr < 16 )){
 						modules.add(new EncoderModule(index,c , modsize, addr, this,cp5));
 					}
 
 					else if (addr >= 16) {
 						modules.add(new ButtonPadModule(index,c , modsize, addr, this,cp5,midiout,midiin,oscP5,myRemoteLocation));
 
 					}
 					//modules.get(addr).processdata(data);
 					setSize((index >= 4) ? 4 * modsize : index * modsize + modsize, (index/4) * modsize+modsize);
 					frameguy.setSize(this.getWidth(), this.getHeight());
 
 				}
 			}
 
 			catch (ClassCastException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	void startxbee() {
 		try { 
 			//optional.  set up logging
 			//  PropertyConfigurator.configure(dataPath("log4j.properties"));
 
 			xbee = new XBee();
 			// replace with your COM port
 			xbee.open("/dev/tty.usbserial-A601EM7Q", 57600);
 
 			xbee.addPacketListener(new PacketListener() {
 				public void processResponse(XBeeResponse response) {
 					queue.offer(response);
 				}
 			}
 					);
 		} 
 		catch (Exception e) {
 
 
 			Object[] options = {
 					"Exit", "Retry"
 			};
 			int n = JOptionPane.showOptionDialog(frame, 
 					"Check that it is plugged in", 
 					"Cannot Connect Xbee", 
 					JOptionPane.YES_NO_OPTION, 
 					JOptionPane.QUESTION_MESSAGE, 
 					null, //do not use a custom Icon
 					options, //the titles of buttons
 					options[0]); //default button title
 			if (n == 0) {
 				e.printStackTrace();
 				System.exit(1);
 			}
 			if (n == 1) {
 				startxbee();
 			}
 			// System.out.println("XBee failed to initialize");
 
 			System.exit(1);
 		} 
 	}
 
 	public void controlEvent(ControlEvent theevent) {
 		int address = (theevent.getId()/100);
 		for (Module m : modules){
 			if (m.getaddress() == address){
 
 				m.controlEvent(theevent);
 				break;
 			}
 		}
 
 
 	}
 
 	public void xbeeoutput(int address, int[] payload)
 	{
 		XBeeAddress16 destination = new XBeeAddress16(0x00, address);
 
 		TxRequest16 tx = new TxRequest16(destination, payload);
 		try {
 
 			xbee.sendAsynchronous(tx);
 
 		} 
 		catch(XBeeTimeoutException e) {
 			println("Request timed out, check if remote is actually on.");
 			e.printStackTrace();
 		}
 		catch(XBeeException e) {
 			println("Unknown XBee exception trying to send LED data");
 			e.printStackTrace();
 			xbeeoutput(address, payload);
 
 		}
 	}
 
 
 	private void controlReceived(Note thenote){
 
 		//	println(thenote);
 
 
 	}
 
 	private void changeoscprefix(String theprefix){
 		pre = theprefix;
 		for (Module m : modules){
 			m.setoscprefix(theprefix);
 		}
 		//ARC PLUGS
 		oscP5.plug(this,"ringrange", pre+"/ring/range");
 		oscP5.plug(this,"ringall", pre+"/ring/all");
 		oscP5.plug(this,"ringmap", pre+"/ring/map");
 		oscP5.plug(this,"ringset", pre+"/ring/set");
 
 		//GRID PLUGS
 		oscP5.plug(this,"gridledrow", pre+"/grid/led/row");
 		oscP5.plug(this,"gridledcol", pre+"/grid/led/col");
 		oscP5.plug(this,"gridledset", pre+"/grid/led/set");
 		oscP5.plug(this,"gridledall", pre+"/grid/led/all");
 	}
 private void changeoscport(int theport){
 	port = theport;
 	myRemoteLocation = new NetAddress("127.0.0.1", theport);
 	for (Module m : modules){
 		m.setnetaddress(myRemoteLocation);
 	}
 	println("The OSC PORT HAS BEEN CHANGED TO  :  " + theport);
 
 }
 private void changeoscinfo () {
     OscMessage myMessage = new OscMessage("/sys/port"); 
     myMessage.add(8080);       
     oscP5.send(myMessage, myRemoteLocation);
     
     myMessage = new OscMessage("/sys/id"); 
     myMessage.add("Modulome");       
     
     oscP5.send(myMessage, myRemoteLocation);
     
     myMessage = new OscMessage("/sys/prefix"); 
     //TODO change this
     myMessage.add("/mlr");  
     
     oscP5.send(myMessage, myRemoteLocation);
     myMessage = new OscMessage("/sys/size"); 
     myMessage.add(8);   
     myMessage.add(8);    
     oscP5.send(myMessage, myRemoteLocation);
     
     myMessage = new OscMessage("/sys/connect"); 
     myMessage.add(port);       
     oscP5.send(myMessage, myRemoteLocation);
     println("OSC INFO CHANGED");
   }
 	
 	
 	// GRID FORWARDING
 	private void gridledset(int x, int y, int l){
 		for (Module m : modules){
 			if (m instanceof ButtonPadModule){
 
 
 					((ButtonPadModule) m).processOSC(x, y, l);
 				
 			}
 		}
 	}
 	private void gridledall(int l){
 		for (Module m : modules){
 			if (m instanceof ButtonPadModule){
 
 				if (m.isoscon()){
 
 					((ButtonPadModule) m).processOSCall(l);
 
 				}
 			}
 		}
 	}
 	private void gridledcol(int x, int y, int l){
 		println("Received a column message  :  " +  x + "  /  " + y + "  /  " + l);
 
 		for (Module m : modules){
 			if (m instanceof ButtonPadModule){
 				if (m.isoscon()){
 
 					
 					((ButtonPadModule) m).processOSCcol(x, y, l);
 
 				}
 			}
 		}
 	}
 	private void gridledcol(int x, int y, int l, int p){
 		println("Received a column message  :  " +  x + "  /  " + y + "  /  " + l);
 
 		for (Module m : modules){
 			if (m instanceof ButtonPadModule){
 				if (m.isoscon()){
 
 					
 					((ButtonPadModule) m).processOSCcol(x, y, l);
 
 				}
 			}
 		}
 	}
 	private void gridledrow(int x, int y, int l){
 		for (Module m : modules){
 			if (m instanceof ButtonPadModule){
 
 				if (m.isoscon()){
 					((ButtonPadModule) m).processOSCrow(x, y, l);
 
 
 				}
 			}
 		}
 	}
 
 //RING FORWARDING	
 	private void ringset(){
 		for (Module m : modules){
 			if (m instanceof EncoderModule){
 
 				if (m.isoscon()){
 
 
 				}
 			}
 		}
 	}
 	private void ringall(){
 		for (Module m : modules){
 			if (m instanceof EncoderModule){
 
 				if (m.isoscon()){
 
 
 				}
 			}
 		}
 	}
 	private void ringmap(){
 		for (Module m : modules){
 			if (m instanceof EncoderModule){
 
 				if (m.isoscon()){
 
 
 				}
 			}
 		}
 	}
 	private void ringrange(){
 		for (Module m : modules){
 			if (m instanceof EncoderModule){
 
 				if (m.isoscon()){
 
 
 				}
 			}
 		}
 	}
 	//OTHER OSC MESSAGES
 	
 	void oscEvent(OscMessage theOscMessage) {
 		  /* with theOscMessage.isPlugged() you check if the osc message has already been
 		   * forwarded to a plugged method. if theOscMessage.isPlugged()==true, it has already 
 		   * been forwared to another method in your sketch. theOscMessage.isPlugged() can 
 		   * be used for double posting but is not required.
 		  */  
 		  if(theOscMessage.isPlugged()==false) {
 		  /* print the address pattern and the typetag of the received OscMessage */
 		  println("### received an osc message.");
 		  println("### addrpattern\t"+theOscMessage.addrPattern());
 		  println("### typetag\t"+theOscMessage.typetag());
 		  
 		  
 		  }
 		}
 
 
 }
 
 
 
 
