 package editor.window;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.prefs.Preferences;
 
 import de.matthiasmann.twl.Button;
 import de.matthiasmann.twl.DialogLayout;
 import de.matthiasmann.twl.DialogLayout.Group;
 import editor.action_listener.ActionEvent;
 import editor.action_listener.ActionListener;
 import engine.window.components.LoadFileSelector;
 import engine.window.components.Window;
 
 public class FileMenu  extends Window implements ActionListener {
 	private ArrayList<ActionListener> action_listeners;
 	
 	private LoadFileSelector load_file;
 	private Button new_file;
 	private Button open_file;
 	private Button save_file;
 	LoadFileSelector.Callback callback = new LoadFileSelector.Callback() {
 		@Override
 		public void fileSelected(File file) {/* TODO Auto-generated method stub*/}
 		@Override
 		public void canceled() {/* TODO Auto-generated method stub*/}
 	};
 	
 
 	public FileMenu() {
 		new_file = new Button();
 		open_file = new Button();
 		save_file = new Button();
 		
 		action_listeners = new ArrayList<ActionListener>();
 		setTitle("File Menu");
 
		load_file = new LoadFileSelector(this, Preferences.userNodeForPackage(LoadFileSelector.class), "what", "fucking dumb", callback, "cockass");
 		//save_file = 
 		
 		createMenu();
 		createButtonCallbacks();		
 		
 		setSize(300, 150);
 	}
 	
 	public void createMenu() {
 		// Create the layout and button instances
 		DialogLayout layout = new DialogLayout();
 
 		new_file = new Button("New");
 		new_file.setTheme("new_file");
 		open_file = new Button("Open");
 		open_file.setTheme("open_file");
 		save_file = new Button("Save");
 		save_file.setTheme("save_file");
 		new_file.setSize(200, 30);
 		open_file.setSize(200, 30);
 		save_file.setSize(200, 30);
 		
 		// !!!EXAMPLE OF DIALOG LAYOUT!!!//
 		// Sequential groups are like a Swing boxlayout and just lists from top
 		// to bottom
 		// Parallel groups align each start and size and can be cascaded
 		//
 		// Group for holding the Horizontal alignment of the buttons
 		Group button_hgroup = layout
 			.createSequentialGroup()
 			.addGap()
 			// Keeps all the buttons in a single vertical line as opposed to
 			// staggering
 			// left to right per row
 			.addGroup(layout.createParallelGroup(new_file, open_file, save_file))
 			.addGap();
 
 		// Group for holding the Vertical alignment of the buttons
 		Group button_vgroup = layout.createSequentialGroup()
 			.addGap()
 			// Add each widget without forming a group so that they rest one
 			// under the other
 			.addWidget(new_file).addWidget(open_file).addWidget(save_file);
 
 		// All Dialog layout groups must have both a HorizontalGroup and
 		// VerticalGroup
 		// Otherwise "incomplete" exception is thrown and layout is not applied
 		layout.setHorizontalGroup(button_hgroup);
 		layout.setVerticalGroup(button_vgroup);
 
 		// Make sure to add the layout to the frame
 		add(layout);
 	}
 	
 	private void createButtonCallbacks() {
 		new_file.addCallback(new Runnable() {			
 			@Override
 			public void run() { /* create a new file somehow? */}
 		});
 		open_file.addCallback(new Runnable() {			
 			@Override
 			public void run() { load_file.openPopup(); }
 		});
 		save_file.addCallback(new Runnable() {			
 			@Override
 			public void run() { /* save_file.openPopup */}
 		});
 	}
 	
 	/*Public Callback methods*/
 	public void addActionListener(ActionListener listener) {
 		action_listeners.add(listener);
 	}
 	private void fireActionEvent() {
 		for (ActionListener ae : action_listeners) {
 			ae.actionPerformed(new ActionEvent(this));
 		}
 	}
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		fireActionEvent();
 	}
 	/*End Public Callback methods*/
 }
