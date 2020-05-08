 /**
  * 
  */
 package org.promasi.desktop_swing;
 
 import java.awt.BorderLayout;
 import java.awt.Rectangle;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 
 import javax.swing.Icon;
 import javax.swing.JDesktopPane;
 import javax.swing.JPopupMenu;
 
 import org.promasi.desktop_swing.application.ADesktopApplication;
 import org.promasi.desktop_swing.application.TaskBarIcon;
 import org.promasi.game.IGame;
 import org.promasi.utils_swing.Colors;
 import org.promasi.utils_swing.GuiException;
 
 
 /**
  * @author alekstheod
  * Represent the workspace place on ProMaSi desktop.
  * the workspace place can accept some internal frames
  * as a Desktop applications.
  */
 public class PromasiJDesktopPane extends JDesktopPane  implements IDesktop {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	/**
 	 * Instance of {@link JPopupMenu} represents
 	 * the start menu on ProMaSi system.
 	 */
 	private JPopupMenu _startMenu;
 	
 	/**
 	 * Instance of {@link IDesktop} interface implementation
 	 * represents the desktop workspace.
 	 */
 	private IDesktop _desktop;
 	
 	/**
 	 * 
 	 */
 	private WidgetsFrame _widgetsFrame;
 	
 	/**
 	 * Constructor will initialize the object.
 	 * @throws GuiException in case of invalid arguments.
 	 */
 	public PromasiJDesktopPane( IGame game, String username , IDesktop desktop) throws GuiException{
 		super();
 		
 		if ( desktop == null ){
 			throw new GuiException("Wrong argument desktop == null");
 		}
 		
 		if( game == null ){
 			throw new GuiException("Wrong argument game == null");
 		}
 		
 		_startMenu = new JPopupMenu();
 		_startMenu.setBackground(Colors.White.alpha(0.8f));
 		_startMenu.setLayout(new BorderLayout());
 		_desktop = desktop;
 		_widgetsFrame = new WidgetsFrame();
 
 		add(_widgetsFrame);
 		_widgetsFrame.show();
		//addWidget(new ClockWidget());
 		
 		StartMenuJPanel startPanel = new StartMenuJPanel( game, username, this );
 		_startMenu.add(startPanel, BorderLayout.CENTER);
 		
 		addComponentListener(new ComponentListener() {
 			
 			@Override
 			public void componentShown(ComponentEvent e) {}
 			
 			@Override
 			public void componentResized(ComponentEvent e) {
 				_widgetsFrame.setBounds(new Rectangle(getWidth() - _widgetsFrame.getWidth() - 10, 
 										getHeight() - _widgetsFrame.getHeight() , 
 										_widgetsFrame.getWidth(), _widgetsFrame.getHeight()));
 			}
 			
 			@Override
 			public void componentMoved(ComponentEvent e) {}
 			
 			@Override
 			public void componentHidden(ComponentEvent e) {}
 		});
 	}
 
 	public void showStartMenu() {
 		_startMenu.show(this , 5, 0 );
 	}
 
 	@Override
 	public void shutDown() {
 		_startMenu.setVisible(false);
 		_desktop.shutDown();
 	}
 
 	@Override
 	public void runApplication( ADesktopApplication application ) {
 		_startMenu.setVisible(false);
 		_desktop.runApplication(application);
 	}
 
 	@Override
 	public boolean addQuickStartButton(TaskBarIcon button) {
 		return _desktop.addQuickStartButton(button);
 	}
 
 	@Override
 	public boolean showMessageBox(Object message, String title, int messageType, Icon icon) {
 		return _desktop.showMessageBox(message, title, messageType, icon);
 	}
 
 	@Override
 	public JDesktopPane getDesktopPane() {
 		return this;
 	}
 
 	@Override
 	public boolean addWidget(Widget widget) {
 		return _widgetsFrame.addWidget(widget);
 	}
 }
