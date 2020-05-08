 package meew0.ld;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.KeyStroke;
 import javax.swing.UIManager;
 
 import meew0.ld.level.Level;
 
 public class MainFrame extends JFrame implements DesignPanelListener {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	public static String VERSION = "02";
 	
 	public static final int CURSOR = 0;
 	public static final int PIXEL = 1;
 	public static final int LINE = 2;
 	public static final int ALL = 3;
 	
 	private DesignPanel panel;
 	private int tool, v = 0;
 	
 	public MainFrame() throws IOException {
 		super("ld"+VERSION);
 		
 		this.setBackground(Color.white);
 		
 		JMenuBar menuBar = new JMenuBar();
		
 		JMenu fileMenu = new JMenu("File");
 		JMenuItem newFileItem = new JMenuItem("New"); newFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK)); 
 		JMenuItem openFileItem = new JMenuItem("Load"); openFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
 		JMenuItem saveFileItem = new JMenuItem("Save"); saveFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
 		JMenuItem saveAsFileItem = new JMenuItem("Save as");
 		JMenuItem genFileItem = new JMenuItem("Generate string"); genFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));
 		JMenuItem exitFileItem = new JMenuItem("Exit"); exitFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
		
 		fileMenu.add(newFileItem);
 		fileMenu.addSeparator();
 		fileMenu.add(openFileItem);
 		fileMenu.add(saveFileItem);
 		fileMenu.add(saveAsFileItem);
 		fileMenu.add(genFileItem);
 		fileMenu.addSeparator();
 		fileMenu.add(exitFileItem);
 		
 		menuBar.add(fileMenu);
		
 		JMenu toolsMenu = new JMenu("Tools");
 		JMenuItem undoToolItem = new JMenuItem("Undo"); undoToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.CTRL_MASK)); 
 		JMenuItem redoToolItem = new JMenuItem("Redo"); redoToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
 		JMenuItem noToolItem = new JMenuItem("Cursor"); noToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0)); 
 		JMenuItem setToolItem = new JMenuItem("Set pixel"); setToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
 		JMenuItem setLineToolItem = new JMenuItem("Set line"); setLineToolItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0));
 		JMenuItem setAllItem = new JMenuItem("Fill"); setAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
 		
 		noToolItem.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent arg0) { tool = CURSOR; }});
 		setToolItem.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent arg0) { tool = PIXEL; }});
 		setLineToolItem.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent arg0) { tool = LINE; }});
 		setAllItem.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent arg0) { tool = ALL; }});
 		
 		toolsMenu.add(undoToolItem);
 		toolsMenu.add(redoToolItem);
 		toolsMenu.addSeparator();
 		toolsMenu.add(noToolItem);
 		toolsMenu.add(setToolItem);
 		toolsMenu.add(setLineToolItem);
 		toolsMenu.add(setAllItem);
 		
 		menuBar.add(fileMenu);
 		menuBar.add(toolsMenu);
 		
 		this.setJMenuBar(menuBar);
 		
 		String[] levelRows = Files.readAllLines(Paths.get("testlevel.ldl"), Charset.defaultCharset()).toArray(new String[]{});
 		String[] palRows = Files.readAllLines(Paths.get("testpal.ldp"), Charset.defaultCharset()).toArray(new String[]{});
 		Level lv = new Level(levelRows, palRows);
 		
 		panel = new DesignPanel(lv);
 		panel.registerPanelListener(this);
 		this.add(panel);
 		
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		
 		this.setSize(600, 400);
 		this.setVisible(true);
 		
 	}
 
 	public static void main(String[] args) throws Throwable {
 		// TODO Auto-generated method 
 		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		new MainFrame();
 	}
 
 	@Override
 	public void onPanelClick(int x, int y) {
 		// TODO Auto-generated method stub
 		System.out.println("You clicked on pixel " + x + ", " + y);
 		if(x < panel.l.getWidth() && y < panel.l.getHeight()) {
 			switch(tool) {
 			case PIXEL:
				panel.l.setDataAt(x, y, v);
 				break;
 			case LINE:
				for(int i = 0; i < panel.l.getWidth(); i++) panel.l.setDataAt(i, y, v);
 				break;
 			case ALL:
 				for(int i = 0; i < panel.l.getWidth(); i++)
 					for(int j = 0; j < panel.l.getHeight(); j++)
 						panel.l.setDataAt(i, j, v);
 				break;
 			default:
 				System.out.println("Cursor or invalid tool");
 			}
 			System.out.println("Pixel value: " + panel.l.getDataAt(x, y));
 		} else {
 			System.out.println("Pixel outside of bounds!");
 		}
 		panel.repaint();
 	}
 
 }
