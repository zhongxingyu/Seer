 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.DropMode;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.event.ListSelectionListener;
 
 public class svgEpubMainPanel extends JFrame implements ActionListener {
 
 	private static final long serialVersionUID = 1L;
 	private JSplitPane jSplitPane1;
 	private JSplitPane jSplitPaneLeft;
 
 	private CustomSVGCanvas svgCanvas;
 
 	private JPanel jPanelMain;
 	private JPanel jPanelFileListButton;
 	private JPanel jPanelFileList;
 
 	private JScrollPane jScrollPaneFile;
 	private JList jListFile;
 	private ItemSelectionListener itemSelectionListener;
 	
 	private JScrollPane jScrollPaneClip;
 	private JList jListClip;
 	private ClipListModel clipListModel;
 	private ClipListItemSelectionListener clipListItemSelectionListener;
 	
 	private JButton jButtonRemove;
 	private JButton jButtonClear;
 	private JButton jButtonCheck;
 	private JButton jButtonUncheck;
 
 	private JPanel jPanelNorth;
 	private JButton jButtonCreateEpub;
 	private JCheckBox jCheckBoxPreview;
 	
 	private SaveDialog saveDialog;
 	private ConfigDialog configDialog;
 	private ClipTemplateDialog clipTemplateDialog;
 	
 	
 	private static CustomProperties properties = new CustomProperties();
 	private JButton jButtonConfig;
 	private JButton jButtonResetClip;
 	private JPanel jPanel0;
 	
 	private JPopupMenu jListPopupMenu;
 	private Rectangle copyClipRectangle = null;
 	
 	private JButton jButtonAutoClip;
 	private JButton jButtonClipTemplate;
 	private static final String PREFERRED_LOOK_AND_FEEL = "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel";
 	static public CustomProperties getProperty() {
 		return properties;
 	}
 	
 	public svgEpubMainPanel() {
 		initComponents();
 	}
 
 	private void initComponents() {
 		try {
 			File config = new File("config.xml");
 			if (config.exists()) {
 				properties.loadFromXML(new FileInputStream(config));
 			} else {
 				properties.loadFromXML(svgEpubMainPanel.class.getResourceAsStream("config.xml"));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		Epub.setProperty(properties);
 		add(getJPanelMain());
 		setSize(640, 452);
 		
 		ImageUtil.initialize(properties.getProperty("enable_opencv", "no").equals("yes"));		
 		Runtime.getRuntime().addShutdownHook(new Shutdown());
 		
 		String workingDir = System.getProperty("user.dir");
 		System.setProperty("jna.library.path", workingDir);
 	}
 	
 	private JButton getJButtonClipTemplate() {
 		if (jButtonClipTemplate == null) {
 			jButtonClipTemplate = new JButton();
 			jButtonClipTemplate.setText("Clip Template");
 			jButtonClipTemplate.setActionCommand("ClipTemplate");
 			jButtonClipTemplate.addActionListener(this);
 		}
 		return jButtonClipTemplate;
 	}
 
 	private JButton getJButtonAutoClip() {
 		if (jButtonAutoClip == null) {
 			jButtonAutoClip = new JButton();
 			jButtonAutoClip.setText("Auto Clip");
 			jButtonAutoClip.setActionCommand("AutoClip");
 			jButtonAutoClip.addActionListener(this);
 			jButtonAutoClip.setEnabled(properties.getProperty("enable_opencv", "no").equals("yes"));
 		}
 		return jButtonAutoClip;
 	}
 
 	private JPanel getJPanel0() {
 		if (jPanel0 == null) {
 			jPanel0 = new JPanel();
 		}
 		return jPanel0;
 	}
 
 	private JPopupMenu getJListPopupMenu() {
 		if (jListPopupMenu == null) {
 			jListPopupMenu = new JPopupMenu();
 			
 			JMenuItem menuItem1 = new JMenuItem("Copy clip area");
 			menuItem1.setActionCommand("CopyClip");
 			menuItem1.addActionListener(this);
 			jListPopupMenu.add(menuItem1);
 
 			JMenuItem menuItem2 = new JMenuItem("Paste clip area");
 			menuItem2.setActionCommand("PasteClip");
 			menuItem2.addActionListener(this);
 			jListPopupMenu.add(menuItem2);
 			
 			JMenuItem menuItem3 = new JMenuItem("OCR");
 			menuItem3.setActionCommand("OCR");
 			menuItem3.addActionListener(this);
 			jListPopupMenu.add(menuItem3);
 		}
 		return jListPopupMenu;
 	}
 
 	private JButton getJButtonResetClip() {
 		if (jButtonResetClip == null) {
 			jButtonResetClip = new JButton();
 			jButtonResetClip.setText("Reset Clip");
 			jButtonResetClip.setActionCommand("ResetClip");
 			jButtonResetClip.addActionListener(this);
 		}
 		return jButtonResetClip;
 	}
 
 	class Shutdown extends Thread {
 		public void run() {
 			if (svgCanvas != null) {
 				svgCanvas.setURI(null);
 			}
 		}
 	}
 
 	private JButton getJButtonConfig() {
 		if (jButtonConfig == null) {
 			jButtonConfig = new JButton();
 			jButtonConfig.setText("Config");
 			jButtonConfig.setActionCommand("Config");
 			jButtonConfig.addActionListener(this);
 		}
 		return jButtonConfig;
 	}
 
 	private JCheckBox getJCheckBoxPreview() {
 		if (jCheckBoxPreview == null) {
 			jCheckBoxPreview = new JCheckBox();
 			jCheckBoxPreview.setText("Preview");
 			jCheckBoxPreview.addItemListener(new ItemListener() {
 	
 				public void itemStateChanged(ItemEvent event) {
 					getClipListItemSelectionListener().setEnabledPreview(jCheckBoxPreview.isSelected());
 					if (itemSelectionListener != null) {
 						itemSelectionListener.setEnabledPreview(jCheckBoxPreview.isSelected());
 						if (jListFile != null) {
 							int index = jListFile.getSelectedIndex();
 							itemSelectionListener.updateItem(index);
 						}
 					}
 					jButtonResetClip.setEnabled(!jCheckBoxPreview.isSelected());
 				}
 			});
 		}
 		return jCheckBoxPreview;
 	}
 
 	private CustomSVGCanvas getSvgCanvas() {
 		if (svgCanvas == null) {
 			svgCanvas = new CustomSVGCanvas();
 		}
 		return svgCanvas;
 	}
 
 
 	private JButton getJButtonCreateEpub() {
 		if (jButtonCreateEpub == null) {
 			jButtonCreateEpub = new JButton();
 			jButtonCreateEpub.setText("Create EPUB");
 			jButtonCreateEpub.setActionCommand("Create");
 			jButtonCreateEpub.addActionListener(this);
 		}
 		return jButtonCreateEpub;
 	}
 	
 
 	private JButton getJButtonClear() {
 		if (jButtonClear == null) {
 			jButtonClear = new JButton();
 			jButtonClear.setText("Clear");
 			jButtonClear.setActionCommand("Clear");
 			jButtonClear.addActionListener(this);
 //			jButton1.setToolTipText("Clear all images");
 		}
 		return jButtonClear;
 	}
 	
 	private JButton getJButtonRemove() {
 		if (jButtonRemove == null) {
 			jButtonRemove = new JButton();
 			jButtonRemove.setText("Remove");
 			jButtonRemove.setActionCommand("Remove");
 			jButtonRemove.addActionListener(this);
 //			jButton0.setToolTipText("Remove selected images");
 		}
 		return jButtonRemove;
 	}
 
 	private JButton getJButtonCheck() {
 		if (jButtonCheck == null) {
 			jButtonCheck = new JButton();
 			jButtonCheck.setText("Check");
 			jButtonCheck.setActionCommand("Check");
 			jButtonCheck.addActionListener(this);
 		}
 		return jButtonCheck;
 	}
 	
 	private JButton getJButtonUncheck() {
 		if (jButtonUncheck == null) {
 			jButtonUncheck = new JButton();
 			jButtonUncheck.setText("Uncheck");
 			jButtonUncheck.setActionCommand("Uncheck");
 			jButtonUncheck.addActionListener(this);
 		}
 		return jButtonUncheck;
 	}
 	
 	private JScrollPane getJScrollPaneFile() {
 		if (jScrollPaneFile == null) {
 			jScrollPaneFile = new JScrollPane();
 			jScrollPaneFile.setViewportView(getJListFile());
 		}
 		return jScrollPaneFile;
 	}
 
 	private JList getJListFile() {
 		if (jListFile == null) {
 			BookListModel fileListModel = new BookListModel();
 			jListFile = new JList(fileListModel);
 		    jListFile.setTransferHandler(new FileDropHandler(fileListModel));
 		    jListFile.setCellRenderer(new FileRenderer());
 		    jListFile.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		    jListFile.setDropMode(DropMode.INSERT);
 		    jListFile.setDragEnabled(true);
 		    jListFile.setBackground(Color.WHITE);
 		    jListFile.setSelectionBackground(new Color(200, 200, 255));
 		    ItemSelectionListener listner = getItemSelectionListener(fileListModel);
 		    jListFile.addListSelectionListener(listner);
 		    jListFile.addMouseListener(new ListMouseListener(listner));
 //		    jListFile.setToolTipText("Drop image files here");
 		    jListFile.addMouseListener(new PopClickListener());
 		}
 		return jListFile;
 	}
 	
 	private JScrollPane getJScrollPaneClip() {
 		if (jScrollPaneClip == null) {
 			jScrollPaneClip = new JScrollPane();
 			jScrollPaneClip.setViewportView(getJListClip());
 		}
 		return jScrollPaneClip;
 	}
 
 	private ClipListModel getClipListModel() {
 		if (clipListModel == null) {
 			clipListModel = new ClipListModel();
 		}
 		return clipListModel;
 	}
 	
 	private JList getJListClip() {
 		if (jListClip == null) {
 			jListClip = new JList(clipListModel);
 			jListClip.setModel(getClipListModel());
 //			jListClip.setTransferHandler(new FileDropHandler(fileListModel));
 //			jListClip.setCellRenderer(new FileRenderer());
 			jListClip.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 			jListClip.setDropMode(DropMode.INSERT);
 			jListClip.setDragEnabled(true);
 			jListClip.setForeground(Color.BLACK);
 			jListClip.setBackground(Color.WHITE);
 			jListClip.setSelectionForeground(Color.BLACK);
 			jListClip.setSelectionBackground(new Color(200, 200, 255));
 			jListClip.addListSelectionListener(getClipListItemSelectionListener());
 //			jListClip.addMouseListener(new ListMouseListener(getItemSelectionListener(fileListModel)));
 //		    jListClip.setToolTipText("Drop image files here");
 //			jListClip.addMouseListener(new PopClickListener());
 		}
 		return jListClip;
 	}
 
 	private ClipListItemSelectionListener getClipListItemSelectionListener() {
 		if (clipListItemSelectionListener == null) {
 			clipListItemSelectionListener = new ClipListItemSelectionListener(getSvgCanvas(), getJListFile(), getClipListModel());
 			clipListItemSelectionListener.setEnabledPreview(false);
 		}
 		return clipListItemSelectionListener;
 	}
 
 	private ItemSelectionListener getItemSelectionListener(DefaultListModel fileListModel) {
 		if (itemSelectionListener == null) {
 			itemSelectionListener = new ItemSelectionListener(getSvgCanvas(), fileListModel, getClipListModel(), getJListClip());
 			itemSelectionListener.setEnabledPreview(false);
 		}
 		return itemSelectionListener;
 	}
 
 	private JPanel getJPanelMain() {
 		if (jPanelMain == null) {
 			jPanelMain = new JPanel();
 			jPanelMain.setLayout(new BorderLayout());
 			jPanelMain.add(getJPanelNorth(), BorderLayout.NORTH);
 			jPanelMain.add(getJSplitPane1(), BorderLayout.CENTER);
 		}
 		return jPanelMain;
 	}
 	
 	private JPanel getJPanelNorth() {
 		if (jPanelNorth == null) {
 			jPanelNorth = new JPanel();
 			jPanelNorth.setLayout(new BoxLayout(jPanelNorth, BoxLayout.X_AXIS));
 			jPanelNorth.add(getJButtonCreateEpub());
 			jPanelNorth.add(getJCheckBoxPreview());
 			jPanelNorth.add(getJButtonResetClip());
 			jPanelNorth.add(getJButtonAutoClip());
 			jPanelNorth.add(getJButtonClipTemplate());
 			jPanelNorth.add(getJPanel0());
 			jPanelNorth.add(getJButtonConfig());
 		}
 		return jPanelNorth;
 	}
 
 	private JPanel getJPanelFileListButton() {
 		if (jPanelFileListButton == null) {
 			jPanelFileListButton = new JPanel();
 			jPanelFileListButton.add(getJButtonRemove());
 			jPanelFileListButton.add(getJButtonClear());
 			jPanelFileListButton.add(getJButtonCheck());
 			jPanelFileListButton.add(getJButtonUncheck());
 		}
 		return jPanelFileListButton;
 	}
 
 	private JSplitPane getJSplitPane1() {
 		if (jSplitPane1 == null) {
 			jSplitPane1 = new JSplitPane();
 			jSplitPane1.setDividerLocation(380);
 			jSplitPane1.setDividerSize(5);
 			jSplitPane1.setLeftComponent(getJSplitPaneLeft());
 			jSplitPane1.setRightComponent(getSvgCanvas());
 		}
 		return jSplitPane1;
 	}
 	
 	private JSplitPane getJSplitPaneLeft() {
 		if (jSplitPaneLeft == null) {
 			jSplitPaneLeft = new JSplitPane();
 			jSplitPaneLeft.setDividerLocation(300);
 			jSplitPaneLeft.setDividerSize(5);
 			jSplitPaneLeft.setLeftComponent(geJPanelFileList());
 			jSplitPaneLeft.setRightComponent(getJScrollPaneClip());
 		}
 		return jSplitPaneLeft;
 	}
 	
 	private Component geJPanelFileList() {
 		if (jPanelFileList == null) {
 			jPanelFileList = new JPanel();
 			jPanelFileList.setLayout(new BorderLayout());
 			jPanelFileList.add(getJScrollPaneFile());
 			jPanelFileList.add(getJPanelFileListButton(), BorderLayout.SOUTH);
 		}
 		return jPanelFileList;
 	}
 
 	private static void installLnF() {
 		try {
 			String lnfClassname = PREFERRED_LOOK_AND_FEEL;
 			UIManager.setLookAndFeel(lnfClassname);
 		} catch (Exception e) {
 			System.err.println("Cannot install " + PREFERRED_LOOK_AND_FEEL
 					+ " on this platform:" + e.getMessage());
 		}
 	}
 
 	/**
 	 * Main entry of the class.
 	 * Note: This class is only created so that you can easily preview the result at runtime.
 	 * It is not expected to be managed by the designer.
 	 * You can modify it as you like.
 	 */
 	public static void main(String[] args) {
 		installLnF();
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				svgEpubMainPanel frame = new svgEpubMainPanel();
 				frame.setDefaultCloseOperation(svgEpubMainPanel.EXIT_ON_CLOSE);
 				frame.setTitle("svgEpub");
 				frame.getContentPane().setPreferredSize(frame.getSize());
 				frame.pack();
 				frame.setLocationRelativeTo(null);
 				frame.setVisible(true);
 			}
 		});
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals("Remove")) {
 			if (!jListFile.isSelectionEmpty()) {
 				clipListModel.setUpdating(true);
 				int indices[] = jListFile.getSelectedIndices();
 				DefaultListModel model = (DefaultListModel)jListFile.getModel();
 				int numRemoved = 0;
 				for (int index : indices) {
 					model.remove(index - numRemoved++);
 				}
 				clipListModel.clear();
 				clipListModel.setUpdating(false);
 				itemSelectionListener.updateItem(-1);
 			}
 		} else if (e.getActionCommand().equals("Clear")) {
 			DefaultListModel model = (DefaultListModel)jListFile.getModel();
 			clipListModel.setUpdating(true);
 			model.clear();
 			clipListModel.setUpdating(false);
 			clipListModel.clear();
 		} else if (e.getActionCommand().equals("Create")) {
 			DefaultListModel model = (DefaultListModel) jListFile.getModel();
 			@SuppressWarnings("unchecked")
 			Enumeration<ListItem> elist = (Enumeration<ListItem>) model.elements();
 			ArrayList<ListItem> list = (ArrayList<ListItem>) Collections.list(elist);
 
 			getSaveDialog().setLocationRelativeTo(this);
 			getSaveDialog().setList(list);
 			getSaveDialog().setVisible(true);
 		} else if (e.getActionCommand().equals("Config")) {
 			getConfigDialog().setMainPanel(this);
 			getConfigDialog().setLocationRelativeTo(this);
 			getConfigDialog().setVisible(true);
 		} else if (e.getActionCommand().equals("ResetClip")) {
 			int index = jListFile.getSelectedIndex();
 			if (index == -1) return;
 			ListItem item = (ListItem) jListFile.getModel().getElementAt(index);
			item.setClipRect(null);
 			itemSelectionListener.updateItem(index);
 		} else if (e.getActionCommand().equals("AutoClip")) {
 			int index = jListFile.getSelectedIndex();
 			if (index == -1) return;
 			ListItem item = (ListItem) jListFile.getModel().getElementAt(index);
 			item.setClipRect(ImageUtil.getContentArea(item));
 			itemSelectionListener.updateItem(index);
 		} else if (e.getActionCommand().equals("ClipTemplate")) {
 			getClipTemplateDialog().setMainPanel(this);
 			getClipTemplateDialog().setLocationRelativeTo(this);
 			getClipTemplateDialog().setVisible(true);
 		} else if (e.getActionCommand().equals("CopyClip")) {
 			int index = jListFile.getSelectedIndex();
 			if (index == -1) return;
 			ListItem item = (ListItem) jListFile.getModel().getElementAt(index);
 			if (item.getClipRect() == null) {
 				copyClipRectangle = null;
 			} else {
 				copyClipRectangle  = new Rectangle(item.getClipRect());
 			}
 		} else if (e.getActionCommand().equals("PasteClip")) {
 			if (jListFile.isSelectionEmpty()) return;
 			Object[] items = jListFile.getSelectedValues();
 			for (Object item : items) {
 				((ListItem)item).setClipRect(copyClipRectangle);
 			}
 			itemSelectionListener.updateItem(jListFile.getSelectedIndex());
 		} else if (e.getActionCommand().equals("Check")) {
 			checkItems(true);
 		} else if (e.getActionCommand().equals("Uncheck")) {
 			checkItems(false);
 		} else if (e.getActionCommand().equals("OCR")) {
 			int index = jListFile.getSelectedIndex();
 			if (index == -1) return;
 			ListItem item = (ListItem) jListFile.getModel().getElementAt(index);
 			Ocr.test(item);
 		}
 
 	}
 	
 	private void checkItems(boolean check) {
 		if (jListFile.isSelectionEmpty()) return;
 		Object[] items = jListFile.getSelectedValues();
 		for (Object item : items) {
 			((ListItem)item).setSelected(check);
 		}
 		itemSelectionListener.updateItem(jListFile.getSelectedIndex());
 		
 		int[] indicies = jListFile.getSelectedIndices();
 		int begin = indicies[0];
 		int end = indicies[indicies.length-1];
 		BookListModel model = (BookListModel)jListFile.getModel();
 		model.notifyModelUpdate(begin, end);
 	}
 
 	private SaveDialog getSaveDialog() {
 		if (saveDialog == null) {
 			saveDialog = new SaveDialog(this , "Save EPUB" , true, properties);
 		}
 		return saveDialog;
 	}
 	
 	private ConfigDialog getConfigDialog() {
 		if (configDialog == null) {
 			configDialog = new ConfigDialog(this, "Config", true, properties);
 		}
 		return configDialog;
 	}
 	
 	private ClipTemplateDialog getClipTemplateDialog() {
 		if (clipTemplateDialog == null) {
 			clipTemplateDialog = new ClipTemplateDialog(this, "Select clip template", true, properties);
 		}
 		return clipTemplateDialog;
 	}
 
 
 	public void updateConfig() {
 		DefaultListModel model = (DefaultListModel) jListFile.getModel();
 		@SuppressWarnings("unchecked")
 		Enumeration<ListItem> elist = (Enumeration<ListItem>) model.elements();
 		ArrayList<ListItem> list = (ArrayList<ListItem>) Collections.list(elist);
 		for (ListItem item : list) {
 			item.setSvgFile(null);
 		}
 
 		if (itemSelectionListener != null) {
 			itemSelectionListener.updateItem(getJListFile().getSelectedIndex());
 		}
 		ImageUtil.initialize(properties.getProperty("enable_opencv", "no").equals("yes"));
 		jButtonAutoClip.setEnabled(properties.getProperty("enable_opencv", "no").equals("yes"));
 	}
 	
 	public void applyClipTemplate(ClipTemplate template) {
 		if (jListFile.isSelectionEmpty()) return;
 		Object[] items = jListFile.getSelectedValues();
 		for (Object item : items) {
 			ListItem listItem = (ListItem)item;
 			ArrayList<ClipListItem> list = template.getClipRect(listItem);
 			listItem.setClipList(list);
 		}
 		itemSelectionListener.updateItem(jListFile.getSelectedIndex());
 	}
 	
 	class PopClickListener extends MouseAdapter {
 	    public void mousePressed(MouseEvent e){
 	        if (e.isPopupTrigger())
 	            doPop(e);
 	    }
 
 	    public void mouseReleased(MouseEvent e){
 	        if (e.isPopupTrigger())
 	            doPop(e);
 	    }
 
 	    private void doPop(MouseEvent e){
 	    	getJListPopupMenu().show(e.getComponent(), e.getX(), e.getY());
 	    }
 	}
 
 }
