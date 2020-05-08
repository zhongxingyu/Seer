 package org.noip.staticattic.GUI;
 
 import java.awt.Dimension;
 import java.awt.KeyboardFocusManager;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import org.noip.staticattic.GUI.Animation.AnimationHandler;
 import org.noip.staticattic.GUI.Events.ShowCharacterCreationScreen;
 import org.noip.staticattic.GUI.Events.ShowMenuScreen;
 import org.noip.staticattic.GUI.Events.ShowTitleScreen;
 import org.noip.staticattic.entities.Player;
 import org.noip.staticattic.entities.Human.Gender;
 import org.noip.staticattic.fileutils.TextureHandler;
 import org.noip.staticattic.world.Environment;
 import org.noip.staticattic.world.EnvironmentHandler;
 
 public class MainWindow extends JFrame implements ActionListener {
 	
 	private static final long serialVersionUID = 1L;
 	public Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
 	ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(3);
 	private UIHandler UIhandler = new UIHandler();
 	private EnvironmentHandler ENhandler = new EnvironmentHandler(this);
 	private AnimationHandler ANhandler = new AnimationHandler(this);
 	private JCheckBox checkbox;
 	public JPanel mainpanel;
 	private Gender gender = null;
 	private Player player;
 	
 	public MainWindow() {
 		
 		this.setTitle("Village");
 		this.setUndecorated(true);
 		this.setResizable(false);
         this.setBounds(0,0,screensize.width, screensize.height);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setLocationRelativeTo(null);
 		
 		this.setIconImage(TextureHandler.getIcon().getImage());
 		
 		mainpanel = new JPanel();
 		mainpanel.setVisible(true);
 		mainpanel.setLayout(null);
 		
 		KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
         manager.addKeyEventDispatcher(new KeyEventHandler(this));
 		
 		this.add(mainpanel);
 		this.setVisible(true);
 
 		executor.scheduleAtFixedRate(UIhandler, 0L, 20L, TimeUnit.MILLISECONDS);
 		
 		UIhandler.addToQueue(new ShowTitleScreen(this, 0));
 		UIhandler.addToQueue(new ShowMenuScreen(this, 4000));
 		
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent a) {
 		
 		if (a.getActionCommand().equals("exit")) {
 			
 			ExitApplication();
 			
 		} else if (a.getActionCommand().equals("newgame")) {
 			
 			UIhandler.addToQueue(new ShowCharacterCreationScreen(this, 0));
 			
 		} else if (a.getActionCommand().equals("selectmale")) {
 			
 			gender = Gender.MALE;
 			
 			checkbox = (JCheckBox) mainpanel.getComponent(2);
 			checkbox.setSelected(false);
 			
 		} else if (a.getActionCommand().equals("selectfemale")) {
 			
 			gender = Gender.FEMALE;
 			
 			checkbox = (JCheckBox) mainpanel.getComponent(1);
 			checkbox.setSelected(false);
 			
 		} else if (a.getActionCommand().equals("createcharacter")) {
 			
 			ENhandler.setMainEnvironment(new Environment());
 			player = new Player(5,5);
 			
 			if (gender.equals(Gender.MALE)) {
 				
 				player.setIcon(TextureHandler.getMPlayerDown());
 				
 			} else {
 				
 				player.setIcon(TextureHandler.getFPlayerDown());
 				
 			}
 			
 			player.setCurrentEnvironment(ENhandler.getMainEnvironment());
 			player.getCurrentEnvironment().addToEntityList(player);
 			
 			mainpanel.removeAll();
 			
 			executor.scheduleAtFixedRate(ENhandler, 0L, 20L, TimeUnit.MILLISECONDS);
 			executor.scheduleAtFixedRate(ANhandler, 0L, 20L, TimeUnit.MILLISECONDS);
 			
 		}
 		
 	}
 	
 	public void ExitApplication() {
 		
 		executor.shutdownNow();
 		System.exit(0);
 		
 	}
 
 	public Player getPlayer() {
 		
 		return player;
 		
 	}
 
 	public void setPlayer(Player player) {
 		
 		this.player = player;
 		
 	}
 	
 	public EnvironmentHandler getENhandler() {
 		
 		return ENhandler;
 		
 	}
 	
 	public void setENhandler(EnvironmentHandler handler) {
 		
 		this.ENhandler = handler;
 		
 	}
 	
 	public AnimationHandler getANhandler() {
 		
 		return ANhandler;
 		
 	}
 	
 	public void setANhandler(AnimationHandler handler) {
 		
 		this.ANhandler = handler;
 		
 	}
 	
 	public UIHandler getUIhandler() {
 		
 		return UIhandler;
 		
 	}
 	
 	public void setUIhandler(UIHandler handler) {
 		
 		this.UIhandler = handler;
 		
 	}
 	
 }
