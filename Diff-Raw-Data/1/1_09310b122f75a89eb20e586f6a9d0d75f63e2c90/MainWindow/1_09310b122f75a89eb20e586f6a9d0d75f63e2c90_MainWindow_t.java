 package nl.PAINt;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 
 public class MainWindow extends JFrame {
 	private static final long serialVersionUID = -3312756899988117703L;
 	private final CanvasPanel canvas;
 	private final StatusbarPanel statusbar;
 	private final KnopjesPanel knopjes;
 	private OptiesPanel optiesPanel;
 	WaitPanel wpanel;
 
 	public MainWindow() {
 		
 		wpanel = new WaitPanel();
 		super.add(wpanel);
 		
 		this.canvas = new CanvasPanel();
 		this.knopjes = new KnopjesPanel(canvas);
 		this.statusbar = new StatusbarPanel(getWidth());
 		this.optiesPanel = new OptiesPanel(canvas);
 		
 		new MessageServer(this);
 
 	}
 	
 	public CanvasPanel getCanvas(){
 		return this.canvas;
 	}
 	
 	public void connected(){
 		super.remove(wpanel);
 		super.add(knopjes, BorderLayout.WEST);
 		super.add(optiesPanel, BorderLayout.EAST);
 		super.add(canvas);
 		super.add(statusbar, BorderLayout.SOUTH);
 		
 		initMenus();
 		
 		super.invalidate();
 		super.validate();
 		super.repaint();
 	}
 
 	private void initMenus() {
 		final MyMenuActionListener mmal = new MyMenuActionListener();
 
 		final JMenuBar menuBar = new JMenuBar();
 		final JMenu menu = new JMenu("File");
 
 		menuBar.add(menu);
 		// draw submenu
 		final JMenu subMenuDraw = new JMenu("Draw");
 
 		// ellipse
 		final JMenu subSubMenuEllipse = new JMenu("Ellipse");
 
 		final JMenuItem ellipseFilled = new JMenuItem("Filled ellipse");
 		ellipseFilled.addActionListener(mmal);
 		subSubMenuEllipse.add(ellipseFilled);
 
 		final JMenuItem ellipse = new JMenuItem("Non-filled ellipse");
 		ellipse.addActionListener(mmal);
 		subSubMenuEllipse.add(ellipse);
 		subMenuDraw.add(subSubMenuEllipse);
 
 		// Rectangle
 		final JMenu subSubMenuRect = new JMenu("Rectangle");
 
 		final JMenuItem rectFilled = new JMenuItem("Filled rectangle");
 		rectFilled.addActionListener(mmal);
 		subSubMenuRect.add(rectFilled);
 
 		final JMenuItem rect = new JMenuItem("Non-filled rectangle");
 		rect.addActionListener(mmal);
 		subSubMenuRect.add(rect);
 
 		subMenuDraw.add(subSubMenuRect);
 		menu.add(subMenuDraw);
 
 		// delete mode
 		JMenuItem menuItem = new JMenuItem("Delete mode");
 		menuItem.addActionListener(mmal);
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Resize mode");
 		menuItem.addActionListener(mmal);
 		menu.add(menuItem);
 
 		menuItem = new JMenuItem("Move mode");
 		menuItem.addActionListener(mmal);
 		menu.add(menuItem);
 
 		super.setJMenuBar(menuBar);
 	}
 
 	private void setMode(final PanelMode mode) {
 		canvas.setMode(mode);
 		statusbar.setText(mode.toString());
 	}
 
 	private class MyMenuActionListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(final ActionEvent e) {
 			switch (e.getActionCommand()) {
 			case "Delete mode":
 				setMode(PanelMode.DELETE);
 				break;
 			case "Move mode":
 				setMode(PanelMode.MOVE);
 				break;
 			case "Resize mode":
 				setMode(PanelMode.RESIZE);
 				break;
 			case "Non-filled rectangle":
 				setMode(PanelMode.RECTANGLE);
 				break;
 			case "Filled rectangle":
 				setMode(PanelMode.TRIANGLE);
 				break;
 			case "Non-filled ellipse":
 				setMode(PanelMode.ELLIPSE);
 				break;
 			case "Filled ellipse":
 				setMode(PanelMode.ELL_FILLED);
 				break;
 			}
 		}
 	}
 }
