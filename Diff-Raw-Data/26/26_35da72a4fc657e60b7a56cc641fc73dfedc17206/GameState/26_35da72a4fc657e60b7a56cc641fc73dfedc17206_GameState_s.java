 package it.twl.examples;
 
 import it.twl.util.BasicTWLGameState;
 import it.twl.util.RootPane;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import de.matthiasmann.twl.Button;
 import de.matthiasmann.twl.DialogLayout;
 import de.matthiasmann.twl.DialogLayout.Group;
 import de.matthiasmann.twl.Label;
 
 public class GameState extends BasicTWLGameState {
 	private DialogLayout grid;
 	private DialogLayout helloPanel;
 	private DialogLayout buttonPanel;
 	private DialogLayout statusBar;
 
 	@Override
 	protected RootPane createRootPane() {
 		RootPane rp = super.createRootPane();
 		rp.setTheme("");
 		createHelloPanel(rp);
 		createGridPanel(rp);
 		createButton(rp);
 		createStatusBar(rp);
 		return rp;
 	}
 
 	private void createStatusBar(RootPane rp) {
 		statusBar = new DialogLayout();
 		statusBar.setTheme("statusbar");
 
 		Button button = new Button("Click ME");
 		button.addCallback(new Runnable() {
 			public void run() {
 				System.out.println("some action!");
 			}
 		});
 
		// statusBar.setHorizontalGroup(statusBar.createSequentialGroup()
		// .addGap("statusRight")
		// .addGroups(statusBar.createParallelGroup(button)));
		// statusBar.setVerticalGroup(statusBar.createSequentialGroup(button));

 		Label label = new Label();
 		label.setText("Info 1");
 
		Group hz = statusBar.createParallelGroup(label);
		Group vz = statusBar.createParallelGroup(button);
 
 		Group horizontal = statusBar.createSequentialGroup(hz, vz);
 
 		Group hz2 = statusBar.createSequentialGroup(label);
 		Group vz2 = statusBar.createSequentialGroup(button);
 
 		Group vertical = statusBar.createParallelGroup(hz2, vz2);
 
 		statusBar.setHorizontalGroup(horizontal);
 		statusBar.setVerticalGroup(vertical);
 
 		rp.add(statusBar);
 	}
 	private void createButton(RootPane rp) {
 		buttonPanel = new DialogLayout();
 		buttonPanel.setTheme("panel");
 
 		Button button = new Button("Click ME");
 		button.addCallback(new Runnable() {
 			public void run() {
 				System.out.println("some action!");
 			}
 		});
 
 		buttonPanel.setHorizontalGroup(buttonPanel.createParallelGroup(button));
 		buttonPanel.setVerticalGroup(buttonPanel.createParallelGroup(button));
 
 		rp.add(buttonPanel);
 	}
 
 	private void createHelloPanel(RootPane rp) {
 		helloPanel = new DialogLayout();
 		helloPanel.setTheme("panel");
 
 		Label label = new Label();
 		label.setText("Hello world Panel");
 
 		helloPanel.setHorizontalGroup(helloPanel.createParallelGroup(label));
 		helloPanel.setVerticalGroup(helloPanel.createParallelGroup(label));
 
 		rp.add(helloPanel);
 	}
 
 	private void createGridPanel(RootPane rp) {
 		grid = new DialogLayout();
 		grid.setTheme("panel");
 
 		Label label = new Label();
 		label.setText("Hello world Panel");
 
 		Label label2 = new Label();
 		label2.setText("This is line two");
 
 		Label label3 = new Label();
 		label3.setText(" right of helloWorld");
 
 		Label label4 = new Label();
 		label4.setText(" right of two");
 
 		Group hz = grid.createParallelGroup(label, label2);
 		Group vz = grid.createParallelGroup(label3, label4);
 
 		Group firstLine = grid.createSequentialGroup(hz, vz);
 
 		Group hz2 = grid.createSequentialGroup(label, label2);
 		Group vz2 = grid.createSequentialGroup(label3, label4);
 
 		Group secondLine = grid.createParallelGroup(hz2, vz2);
 
 		grid.setHorizontalGroup(firstLine);
 		grid.setVerticalGroup(secondLine);
 
 		rp.add(grid);
 	}
 	@Override
 	protected void layoutRootPane() {
 		helloPanel.setPosition(100, 100);
 		helloPanel.adjustSize();
 
 		grid.setPosition(600, 100);
 		grid.adjustSize();
 
 		buttonPanel.setPosition(100, 500);
 		buttonPanel.adjustSize();
 
 		statusBar.setPosition(0, 0);
		statusBar.setSize(1024, 50);
 	}
 
 	@Override
 	public void init(GameContainer arg0, StateBasedGame arg1)
 			throws SlickException {
		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void render(GameContainer arg0, StateBasedGame arg1, Graphics arg2)
 			throws SlickException {
		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public void update(GameContainer arg0, StateBasedGame arg1, int arg2)
 			throws SlickException {
		// TODO Auto-generated method stub
 	}
 
 	@Override
 	public int getID() {
 		return 0;
 	}
 }
