 package game.planet;
 
 import game.commands.handler.CloseHandler;
 import game.commands.handler.ClsHandler;
 import game.commands.handler.ConnectHandler;
 import game.commands.handler.HelpHandler;
 import game.commands.handler.PeersHandler;
 import game.messages.handler.HelloCommandHandler;
 import game.messages.handler.OllehCommandHandler;
 import game.messages.handler.PeersCommandHandler;
 import game.messages.handler.SreepCommandHandler;
 import game.networking.GameMessage;
 import game.networking.UdpChannelFactory;
 
 import java.net.InetAddress;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import vsFramework.Channel;
 import console.Console;
 import console.Console.StdFd;
 import console.planet.PlanetConsole;
 
 public class Planet implements CloseHandler, ClsHandler, ConnectHandler,
 		HelpHandler, PeersHandler, HelloCommandHandler, OllehCommandHandler,
 		PeersCommandHandler, SreepCommandHandler {
 	
 	protected Console con;
 	private Map<Channel, String> connectedPeers = new HashMap<Channel, String>();
 	private List<Channel> pendingPeers = new LinkedList<Channel>();
 	
 
 	final protected String name;
 	
 
 	final private PlanetCommandRegistration reg;
 	final private PlanetMessageRegistration mreg;
 
 	private int silly = 0;
 
 	public Planet(int port) {
 		reg = new PlanetCommandRegistration(this);
 		mreg = new PlanetMessageRegistration(this, port);
 		createGUI();
 		name = con.waitForName();
 		con.setVisible(true);
 		con.println("Welcome! You are located at planet \"" + name + "\"");
 		con.println("Your current Port is: "+port);
 		con.println(StdFd.Planets, "Planetlist: \n\n>> No planets in reach.");
 	}
 
 	private void createGUI() {
 		con = new PlanetConsole();
 		con.setInputHandler(reg);
 	}
 
 	Console getConsole() {
 		return this.con;
 	}
 
 
 	// ----------------- Jump Points for Incomming Message ------------------
 
 	public void onHello(Channel c, String name) {
		connectedPeers.put(c, name);
 		mreg.addPeer(c);
 		String[] myName = {this.name};
 		c.send(GameMessage.OLLEH.toMessage(myName));
 	}
 
 	public void onOlleh(Channel c, String name) {
 		if(!pendingPeers.contains(c)) return;
 		
 		connectedPeers.put(c, name);
 		pendingPeers.remove(c);
 		this.con.println("A new planet was discoverd right next to us.");
 		this.con.println(StdFd.Planets, name);
 	}
 
 	public void onPeers(Channel c, String[] inc) {
 		// TODO
 	}
 
 	public void onSreep(Channel c, String[] inc) {
 		// TODO
 	}
 
 	
 	
 	// ----------------- Jump Points for Command Classes ----------------------
 
 	public void onPeers() {
 		switch (this.silly) {
 		case 0:
 			con.println("No planets found.");
 			++silly;
 			break;
 		case 1:
 			con.clear(StdFd.Planets);
 			con.println(StdFd.Planets, "No planets found here either.");
 			++silly;
 			break;
 		case 2:
 			con.println("STOP IT.");
 			++silly;
 			break;
 		case 3:
 			con.println("If yout don't ...");
 			++silly;
 			break;
 		case 4:
 			con.clear();
 			con.clear(StdFd.Planets);
 			con.println("ok, Ok, OK\nHere are your damn planets.\nHave fun with them.");
 			con.println(StdFd.Planets, "Planetlist: \n\n");
 			con.println(StdFd.Planets, "Alpha Centauri");
 			con.println(StdFd.Planets, "Beta Geuze");
 			con.println(StdFd.Planets, "Mars");
 			con.println(StdFd.Planets, "Snickers");
 			con.println(StdFd.Planets, "Kinderriegel");
 			++silly;
 			break;
 		default:
 			con.println("I don't talk to you anymore.\n...\nGo away!");
 			break;
 		}
 	}
 
 	public void onConnect(InetAddress host, int port) {
 		Channel chan = UdpChannelFactory.newUdpChannel(mreg.LOCALPORT, host, port);
 		pendingPeers.add(chan);
 		mreg.addPeer(chan);
 		String[] name = {this.name};
 		chan.send(GameMessage.HELLO.toMessage(name));
 
 		this.con.println("connect executed");
 	}
 
 	public void onClose() {
 		this.con.println("Bye");
 		System.exit(0);
 	}
 
 	public void onHelp() {
 		this.con.println(this.reg.helpText());
 	}
 
 	public void onCls() {
 		this.con.clear();
 	}
 }
