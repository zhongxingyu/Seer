 package warborn.main;
 
 
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.*;
 
 import warborn.controller.*;
 import warborn.model.Warborn;
 import warborn.view.*;
 import warborn.view.map.GothenburgMapView;
 import warborn.view.map.HellboundArchipelagoMapView;
 import warborn.view.map.Map;
 
 public class Launcher implements Observer{
 
 	private MainFrame frame;
 	private Warborn model;
 	private KeyAction keyAction;
 	private MenuView menu;
 	//private ScreenManager screen;
 	private HudView hud;
 	private Map map;
 	//private DisplayMode compatibleDM;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		
 		new Launcher();
 	}
 	
 	/**
 	 * Create the application.
 	 */
 	public Launcher() {
 		//screen = new ScreenManager();
 		model = new Warborn();
 		//compatibleDM = screen.getHighestResolutionDisplayMode();
 		model.setDimensions(/*compatibleDM.getWidth(), compatibleDM.getHeight()*/1366, 768);
 		frame = new MainFrame(/*screen.getDefaultGraphicsConfiguration(),*/ model);
 		initialize();
 		model.addObserver(this);
 	}
 	
 	/**
 	 * Create the application.
 	 */
 	public Launcher(MainFrame frame) {
 		//screen = new ScreenManager();
 		model = new Warborn();
 		//compatibleDM = screen.getHighestResolutionDisplayMode();
 		model.setDimensions(/*compatibleDM.getWidth(), compatibleDM.getHeight()*/1366, 768);
 		this.frame = frame;
 		initialize();
 		model.addObserver(this);
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		GridBagConstraints c = new GridBagConstraints();
 		c.weightx = 1;
 		c.ipady = (int) (model.getHeight());
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weighty = 1;
 		menu = new MenuView(model);
 		new MenuController(model, menu);
 		frame.add(menu, c);
 		//screen.setFullScreen(compatibleDM, frame);
 		frame.setVisible(true);
 	}
 	
 	/**
 	 * Initialize all the views and controllers
 	 */
 	private void init(){
 		MoveView move = new MoveView(frame);
 		new MoveController(model, move);
 		BattleView battle = new BattleView(frame);
 		new BattleController(model, frame, battle);
 		EndGameView end = new EndGameView(model, frame, System.currentTimeMillis());
 		new EndGameController(end, frame, this);
 		
 		model.addObserver(move);
 		model.addObserver(battle);
 		model.addObserver(end);
 	}
 
 	
 	public void createGame(int mapIndex){
 		
 		frame.remove(menu);
 		model.addObserver(frame);
 		keyAction = new KeyAction(model, this, frame);
 		model.addObserver(new StatePanelViewCreator(frame));
 		init();
 		
 		GridBagConstraints c = new GridBagConstraints();
 		
 		Map[] mapList = getMapList();
 		map = mapList[mapIndex];
 		new MapController(model, map);
 		model.addObserver(map);
 		c.weightx = 1;
 		c.ipady = (int) (model.getHeight()*0.8);
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weighty = 0.75;
 		frame.add(map, c);
 		map.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "options");
 		map.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "options");
 		map.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), "options");
 		map.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0), "options");
 		map.getActionMap().put("options", keyAction);
 		
 		
 		hud = new HudView(model);
 		new HudController(model, frame, hud);
 		model.addObserver(hud);
 		c.ipady = (int) (model.getHeight()*0.25);
 		c.gridy = 1;
 		c.weighty = 0.2;
 		frame.add((JPanel)hud, c);
 		hud.update(model, null);
 		
 		
 	}
 
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		if(model.getState() == 0){
 			model.deleteObserver(this);
 			createGame(model.getMapIndex());
 		}
 	}
 	
 	public void reset(){
 		//screen.restoreScreen();
 		frame.remove(map);
 		frame.remove(hud);
		menu.killMusic();
 		new Launcher(frame);
 		
 	}
 	
 	public Map[] getMapList(){
 		return new Map[] {
 				new GothenburgMapView(model),
 				new HellboundArchipelagoMapView(model),
 		};
 	}
 	
 	public void exit(){
 		//screen.restoreScreen();
 		System.exit(0);
 	}
 
 }
