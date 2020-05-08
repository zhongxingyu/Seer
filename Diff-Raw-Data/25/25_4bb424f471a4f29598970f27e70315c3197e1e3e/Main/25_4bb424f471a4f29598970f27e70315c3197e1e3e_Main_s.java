 package editor;
 
 import javax.vecmath.Vector3f;
 
 import editor.Main;
 
 import editor.action_listener.ActionEvent;
 import editor.action_listener.ActionListener;
 import editor.window.GridWindow;
 import editor.window.LayerMenu;
 import editor.window.PaletteWindow;
 //import editor.window.ToolBox;
 import engine.Engine;
 import engine.entity.*;
 
 public class Main implements ActionListener {
 	// A filthy hack to get around the combobox sending to events on select
 	private Boolean combobox_hack = true;
 
 	private Engine engine;
 
 	private GridWindow grid_window;
 	private PaletteWindow palette_window;
 	private LayerMenu layer_menu;
 	// private ToolBox tool_box;
 
 	private Entity edit_model;
 	private Camera camera;
 	
 	private boolean multiThreaded = false;
 
 	public static void main(String args[]) {
 		Main m = new Main();
 		m.run();
 	}
 
 	public Main() {
 		engine = new Engine();
 
 		engine.addModel("box", "resources/models/misc/box.xgl");
 		
 		int num_layers = 8;
 		grid_window = new GridWindow(num_layers);
 		layer_menu = new LayerMenu();
 		layer_menu.populateLayers(num_layers);
 		palette_window = new PaletteWindow(216);
 		// tool_box = new ToolBox();
 
 		engine.addWindow(grid_window, 400, 400);
 		engine.addWindow(palette_window, 300, 300);
 		engine.addWindow(layer_menu, 200, 30);
 		// engine.addWindow(tool_box, 200, 300);
 		
 		edit_model = engine.addEntity("model", 1f, true, "box", "default");
 		edit_model.setProperty(Entity.NAME, "model");
 		edit_model.setPosition(new Vector3f(0,0,0));
 		//edit_model.setGravity(new Vector3f(0,0,0));
 
		camera = engine.addCamera(1f,false, "box");
		camera.setDistance(5.0f);		
		camera.setPosition(new Vector3f(0, 0, 5));
 		camera.focusOn(edit_model);
 	}
 
 	public void run() {
 		grid_window.addActionListener(this);
 		palette_window.addActionListener(this);
 		layer_menu.addActionListener(this);
 		// tool_box.addActionListener(this);
 		
 		if(multiThreaded == true)
 			runMultiThread();
 		else
 			runSingleThread();
 
 	}
 
 	public void runMultiThread() {
 		engine.run();
 	}
 	
 	public void runSingleThread() {
 		while(true){
 			engine.renderOnce();
 			engine.physicsOnce();
 		}
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 		if (ae.getSource() == palette_window) {
 			grid_window.setCurrentColor(((PaletteWindow) ae.getSource()).getPrimaryColor());
 		} else if (ae.getSource() == grid_window) {
 			edit_model.setModel(grid_window.getGrid().getModel(engine.getModelByName("box")));
 			engine.updateEntity(edit_model);
 		} else if (ae.getSource() == layer_menu) {
 			if (combobox_hack == true) {
 				grid_window.loadLayer(layer_menu.getSelection());
 				combobox_hack = false;
 			} else if (combobox_hack == false) {
 				combobox_hack = true;
 			}
 		}
 	}
 }
