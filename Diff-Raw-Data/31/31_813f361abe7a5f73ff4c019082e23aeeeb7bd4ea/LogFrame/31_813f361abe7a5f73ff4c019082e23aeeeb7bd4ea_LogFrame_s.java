 package ms.ui;
 
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.KeyEvent;
 import java.io.ByteArrayOutputStream;
 import java.net.URL;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JRootPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.KeyStroke;
 
 import ms.utils.I18NManager;
 import ms.utils.ui.Button;
 import ms.utils.ui.Frame;
 import ms.utils.ui.ScrollPane;
 import ms.utils.ui.TextArea;
 
 /**
  * show log information from logger
  */
 public class LogFrame extends Frame implements Observer {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private I18NManager manager = I18NManager.getManager();
 	private JTextArea textArea;
 	private JScrollPane scrollArea;
 	private JButton cancelButton;
 	private String close = manager.getString("close");
 	private Class<? extends Constants> constants;
 
 	public LogFrame(Class<? extends Constants> constants) {
 		this.constants = constants;
 		initGUI();
 	}
 
 	private void initGUI() {
 		initFrame();
 
 		addButtons();
 		addTextArea();
 		addESCListener();
 	}
 
 	private void initFrame() {
 		String title = "";
 		try {
 			title = constants.getField("PROGRAM").get(constants) + " - " + manager.getString("Log.title");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		super.initFrame(title, getClass().getResource(Constants.UIIMAGE + Constants.ICON), new Dimension(500, 430), JFrame.DISPOSE_ON_CLOSE);
 		
 		componentListener();
 	}
 
 	private void componentListener() {
 		addComponentListener(new ComponentAdapter() {
 			private boolean isShown = false;
 			@Override
 			public void componentResized(ComponentEvent e) {
 				if (isShown) {
 					updateComponentBounds();
 				}
 			}
 			@Override
 			public void componentShown(ComponentEvent e) {
 				isShown = true;
 			}
 		});
 	}
 	
 	private void updateComponentBounds() {
 		int width = getWidth() - 15;
 		int height = getHeight() - 65;
 		scrollArea.setSize(width , height - 15);
 		scrollArea.revalidate();
 		cancelButton.setLocation(width - 125, height);
 	}
 
 	private void addTextArea() {
 		textArea = new TextArea();
 		textArea.setComponentPopupMenu(getPopUpMenu(textArea));
 		scrollArea = new ScrollPane(textArea, new Rectangle(5, 5, 485, 350));
 		add(scrollArea);
 	}
 
 	/**
 	 * add buttons
 	 */
 	private void addButtons() {
 		int x = 235;
 		int y = 365;
 		int width = 115;
 		int height = 25;
 		final Rectangle cancelBounds = new Rectangle(x + width + 10, y, width, height);
 		final int cancelMnemonic = KeyEvent.VK_C;
 		final URL icon = getClass().getResource(Constants.UIIMAGE + Constants.CANCEL); 
 		cancelButton = new Button(cancelBounds, close, cancelMnemonic, icon);
 		cancelButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				close();
 			}
 		});
 		add(cancelButton);
 	}
 	
 	/**
 	 * close
 	 */
 	private void close() {
 		setVisible(false);
 		dispose();
 	}
 	
 	/**
 	 * esc = close dialog
 	 */
 	private void addESCListener() {
 		ActionListener cancelListener = new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				close();
 			}
 		};
 		JRootPane rootPane = getRootPane();
 		rootPane.registerKeyboardAction(cancelListener, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
 	}
 
 	/**
 	 * PopupMenu
 	 * 
 	 * @param textField
 	 * @return JPopupMenu
 	 */
 	private JPopupMenu getPopUpMenu(final JTextArea textArea) {
 		JPopupMenu popupMenu = new JPopupMenu();
		final String copy = manager.getString("copy"), selectall = manager.getString("selectall");
		final String[] menuItems = { copy, selectall };
		for (int i = 0; i < menuItems.length; i++) {
			JMenuItem copyMenuItem = new JMenuItem(menuItems[i]);
			copyMenuItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(e.getActionCommand() == copy) {
						textArea.copy();
					} else {
						textArea.selectAll();
					}
				}
			});
			popupMenu.add(copyMenuItem);
		}
 		return popupMenu;
 	}
 
 	public void update(Observable o, Object arg) {
 		if(arg instanceof ByteArrayOutputStream) {
 			textArea.setText(((ByteArrayOutputStream) arg).toString());
 		}
 	}
 }
