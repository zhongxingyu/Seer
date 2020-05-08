 package screenCore;
 
 import java.net.InetAddress;
 import java.util.List;
 import graphics.Color;
 import math.Vector2;
 import GUI.Button;
 import GUI.Label;
 import GUI.ListBox;
 import content.ContentManager;
 import utils.EventArgs;
 import utils.IEventListener;
 import utils.Rectangle;
 import utils.time.TimeSpan;
 
 public class ConnectScreen extends TransitioningGUIScreen{
 	private ListBox<String> serverListBox;
 	private List<InetAddress> addresses;
 	private Label infoLabel;
 	private Button connectBtn;
 
 	public ConnectScreen(String lookAndFeelPath) {
 		super(false, TimeSpan.fromSeconds(2.0f), TimeSpan.fromSeconds(1.0f), lookAndFeelPath);
 	}
 	
 	public void initialize(ContentManager manager) {
 		super.initialize(manager);
 		
 		Rectangle vp = this.ScreenManager.getViewport();
 		int x = vp.Width - 250;
 		
 		this.serverListBox = new ListBox<>();
 		serverListBox.setBounds(-1000, -1000, 1016, 400);
 		serverListBox.setFont(manager.loadFont("Andale Mono20.xml"));
 		serverListBox.setBgColor(new Color(0,0,0,255));
 		serverListBox.setFgColor(Color.White);
 		this.addGuiControl(serverListBox, new Vector2(50, -400), new Vector2(50, 30), new Vector2(50, 800));
 		
 		this.connectBtn = new Button();
 		connectBtn.setBounds(-1000, -1000, 200, 50);
 		connectBtn.setText("Connect to server");
 		this.addGuiControl(connectBtn, new Vector2(vp.Width + 200, 50), new Vector2(x,50),new Vector2(-200, 50));
 		
 		Button refreshBtn = new Button();
 		refreshBtn.setBounds(-1000, -1000,200,50);
 		refreshBtn.setText("Refresh the list");
 		this.addGuiControl(refreshBtn, new Vector2(vp.Width + 200,140), new Vector2(x,140),new Vector2(-200,140));
 		
 		Button backBtn = new Button();
 		backBtn.setBounds(-1000, -1000,200,50);
 		backBtn.setText("Back");
 		this.addGuiControl(backBtn, new Vector2(vp.Width + 200,350), new Vector2(x,350),new Vector2(-200,350));
 		
 		this.infoLabel = new Label();
 		infoLabel.setBounds(-1000, -1000, 400, 30);
 		this.addGuiControl(infoLabel, new Vector2(100,1366), new Vector2(100, 450), new Vector2(100,1366));
 
 		connectBtn.addClicked(new IEventListener<EventArgs>() {
 			public void onEvent(Object sender, EventArgs e) {
 				connectToServer();
 			}
 		});
 		
 		refreshBtn.addClicked(new IEventListener<EventArgs>() {
 			public void onEvent(Object sender, EventArgs e) {
 				onTransitionFinished();		
 			}
 		});
 		
 		backBtn.addClicked(new IEventListener<EventArgs>() {
 			public void onEvent(Object sender, EventArgs e) {
 				gotoTest2();
 			}
 		});
 	}
 	
 	protected void connectToServer() {
 		int selected = serverListBox.getSelecteItemIndex();
 		if(selected != -1) {
 			this.ScreenManager.getNetwork().connectToHost(this.addresses.get(selected));
 			System.out.println("OP!");
 		}
 		
 		this.ScreenManager.removeAllScreens();
 		this.ScreenManager.addScreen("Lobby");
		this.ScreenManager.addScreen("ChatScreen");
 		this.ScreenManager.addScreen("LobbyGUI");
 	}
 
 	@Override
 	public void onTransitionFinished() {
 		serverListBox.clear();
 		
 		List<InetAddress> addresses = this.ScreenManager.getNetwork().getAvalibleLanHosts();
 		this.addresses = addresses;
 		if(addresses.size() > 0) {
 			for(int i = 0; i < addresses.size(); i++) {
 				connectBtn.setEnabled(true);
 				serverListBox.addItem("      " + addresses.get(i).getHostName());
 				infoLabel.setFgColor(Color.Green);
 				infoLabel.setText(addresses.size() + " server(s) online.");
 			}
 		} else {
 			connectBtn.setEnabled(false);
 			infoLabel.setFgColor(Color.Red);
 			infoLabel.setText("No servers online.");
 		}
 	}
 	
 	protected void gotoTest2() {
 		this.ScreenManager.getNetwork().reset();
 		this.exitScreen();
 	}
 }
