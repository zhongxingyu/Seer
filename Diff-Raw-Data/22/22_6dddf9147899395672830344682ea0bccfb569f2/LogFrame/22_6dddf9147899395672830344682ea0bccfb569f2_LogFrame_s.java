 package ms.server.ui;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.util.HashMap;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 import javax.swing.JRootPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.KeyStroke;
 import javax.swing.border.LineBorder;
 import javax.swing.filechooser.FileFilter;
 
 import ms.server.filesys.FileIO;
 import ms.server.log.Log;
 import ms.server.utils.ConfigHandler;
 import ms.server.utils.Constants;
 import ms.server.utils.I18NManager;
 
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

 /**
  * show log information from logger
  * 
  * @author david
  *
  */
 public class LogFrame extends JFrame implements Runnable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private I18NManager manager = I18NManager.getManager();
 	private ConfigHandler config = ConfigHandler.getHandler();
 	private JTextArea textArea;
 	private JScrollPane scrollArea;
 	private JCheckBox box;
 	private HashMap<String, JButton> buttonMap = new HashMap<String, JButton>();
 	private final String save = manager.getString("save"), close = manager.getString("close");
 	private boolean suspendThread = false;
 
 	public LogFrame() {
 		initGUI();
 	}
 
 	private void initGUI() {
 		initFrame();
 
 		addButtons();
 		addRefreshBox();
 		addTextArea();
 		addLogListener();
 		addESCListener();
 		
 		loadProperties();
 	}
 
 	private void initFrame() {
 		setTitle(Constants.PROGRAM + " - " + manager.getString("Log.title"));
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setSize(500, 430);
 		setMinimumSize(new Dimension(getWidth(), getHeight()));
 		setLayout(null);
 		setIconImage(new ImageIcon(getClass().getResource(Constants.UIIMAGE + Constants.ICON)).getImage());
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		setLocation((dim.width - getWidth()) / 2, (dim.height - getHeight()) / 2);
 		
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
 			@Override
 			public void componentHidden(ComponentEvent e) {
 				suspendListener();
 			}
 		});
 	}
 	
 	private void updateComponentBounds() {
 		int width = getWidth() - 15;
 		int height = getHeight() - 65;
 		scrollArea.setSize(width , height - 15);
 		scrollArea.revalidate();
 		
 		buttonMap.get(save).setLocation(width - 250, height);
 		buttonMap.get(close).setLocation(width - 125, height);
 		
 		box.setLocation(10, height + 5);
 	}
 
 	private void addTextArea() {
 		textArea = new JTextArea();
 		textArea.setEditable(false);
 		textArea.setBorder(LineBorder.createBlackLineBorder());
 		textArea.setWrapStyleWord(true);
 		textArea.setLineWrap(true);
 		textArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
 		textArea.setComponentPopupMenu(getPopUpMenu(textArea));
 		scrollArea = new JScrollPane(textArea);
 		scrollArea.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		scrollArea.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 		scrollArea.setBounds(5, 5, 485, 350);
 
 		add(scrollArea);
 	}
 
 	private void addRefreshBox() {
 		box = new JCheckBox(manager.getString("Log.autorefresh"));
 		box.setSelected(true);
 		box.setBounds(10, 370, 150, 20);
 		box.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				if (box.isSelected()) {
 					resumeListener();
 				} else {
 					suspendListener();
 				}
 			}
 		});
 		add(box);
 	}
 
 	private void addLogListener() {
 		Thread logListener = new Thread(this);
 		logListener.start();
 	}
 
 	private void suspendListener() {
 		suspendThread = true;
 	}
 
 	private synchronized void resumeListener() {
 		suspendThread = false;
 		notify();
 	}
 
 	/**
 	 * add buttons
 	 */
 	private void addButtons() {
 		int x = 235;
 		int y = 365;
 		int width = 115;
 		int height = 25;
 		final String[] buttonText = { save, close };
 		final String[] icons = { Constants.SAVE, Constants.CANCEL };
 		final Rectangle sendBounds = new Rectangle(x, y, width, height);
 		final Rectangle cancelBounds = new Rectangle(x + width + 10, y, width, height);
 		final Rectangle[] bounds = { sendBounds, cancelBounds };
 		final int okMnemonic = KeyEvent.VK_S, cancelMnemonic = KeyEvent.VK_C;
 		final int[] mnemonic = { okMnemonic, cancelMnemonic };
 		for (int i = 0; i < buttonText.length; i++) {
 			JButton button = new JButton();
 			button.setBounds(bounds[i]);
 			button.setText(buttonText[i]);
 			button.setMnemonic(mnemonic[i]);
 			button.setIcon(new ImageIcon(getClass().getResource(Constants.UIIMAGE + icons[i])));
 		    button.setVerticalTextPosition(JButton.CENTER);
 		    button.setHorizontalTextPosition(JButton.RIGHT);
 			button.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					if (e.getActionCommand() == save) {
 						saveAsTXT();
 					} else if (e.getActionCommand() == close) {
 						close();
 					}
 				}
 			});
 			add(button);
 			buttonMap.put(buttonText[i], button);
 		}
 	}
 
 	private void saveAsTXT() {
 		JFileChooser fileChooser = getFileChooser();
 		fileChooser.setSelectedFile(new File(Constants.LOGFILE));
 		fileFilter(fileChooser);
 
 		int returnVal = fileChooser.showSaveDialog(null);
 		if (returnVal == JFileChooser.APPROVE_OPTION) {
 			String filename = fileChooser.getSelectedFile().getName();
 			String path = fileChooser.getCurrentDirectory().toString();
 			String file = addTXTPostfix(filename, path);
 			FileIO.write(new File(file), textArea.getText().trim());
 		}
 	}
 
 	private String addTXTPostfix(String filename, String path) {
 		String file = path + File.separator + filename;
 		if (!filename.endsWith("txt")) {
 			file += ".txt";
 		}
 		return file;
 	}
 
 	private JFileChooser getFileChooser() {
 		JFileChooser fileChooser = new JFileChooser() {
 
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void approveSelection() {
 				File f = getSelectedFile();
 				if (f.exists() && getDialogType() == SAVE_DIALOG) {
 					int result = JOptionPane.showConfirmDialog(
 							getTopLevelAncestor(),
 							manager.getString("Log.fileoverwritemessage"),
 							manager.getString("Log.fileoverwritetitle"),
 							JOptionPane.YES_NO_CANCEL_OPTION,
 							JOptionPane.QUESTION_MESSAGE);
 					switch (result) {
 					case JOptionPane.YES_OPTION:
 						super.approveSelection();
 						return;
 					case JOptionPane.NO_OPTION:
 						return;
 					case JOptionPane.CANCEL_OPTION:
 						cancelSelection();
 						return;
 					}
 				}
 				super.approveSelection();
 			}
 		};
 		return fileChooser;
 	}
 
 	private void fileFilter(JFileChooser fileChooser) {
 		fileChooser.setFileFilter(new FileFilter() {
 			public boolean accept(File file) {
 				return file.getName().toLowerCase().endsWith(".txt")
 						|| file.isDirectory();
 			}
 
 			public String getDescription() {
 				return "*.txt";
 			}
 		});
 	}
 	
 	private void saveProperties() {
 		saveValues();
 		config.save();
 	}
 	
 	private void saveValues() {
 		config.setProperty(Constants.LOGCFG, String.valueOf(box.isSelected()));
 	}
 	
 	/**
 	 * load properties.
 	 */
 	private void loadProperties() {
 		if(config.containsKey(Constants.LOGCFG))
 			box.setSelected(Boolean.parseBoolean(config.getProperty(Constants.LOGCFG)));
 	}
 
 	/**
 	 * close
 	 */
 	private void close() {
 		saveProperties();
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
 
 	public void run() {
 		while(true) {
 			try {
 				readLogContent();
 				Thread.sleep(2000);
 				synchronized (this) {
 					while (suspendThread) {
 						wait();
 					}
 				}
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 
 	private void readLogContent() {
		ByteOutputStream bos = Log.getOutputStream();
 		textArea.setText(bos.toString());
 		if(box.isSelected()) {
 			textArea.setCaretPosition(textArea.getDocument().getLength());
 		}
 	}
 }
