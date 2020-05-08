 package cm.view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputMethodEvent;
 import java.awt.event.InputMethodListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextField;
 import javax.swing.JToolBar;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.filechooser.FileFilter;
 
 import cm.model.Settings;
 import cm.model.StatLibrary;
 import cm.model.Stats;
 import cm.util.DiceBag;
 
 /**
  * Display a list of stored {@link Stats} and allows for adding to an encounter.
  * 
  * @author Matthew Rinehart &lt;gomamon2k at yahoo.com&gt;
  * @since 1.0
  */
 // VS4E -- DO NOT REMOVE THIS LINE!
 public class Library extends JDialog {
 	/**
 	 * Generated.
 	 */
 	private static final long serialVersionUID = -2484054165656612936L;
 
 	/**
 	 * The library model.
 	 */
 	private StatLibrary _statLib = new StatLibrary();
 
 	/**
 	 * A list of {@link Stats} to add to the encounter.
 	 */
 	private List<Stats> _statsToAdd = new ArrayList<Stats>();
 
 	private JButton jButtonAdd;
 	private JButton jButtonATLoad;
 	private JButton jButtonCBLoad;
 	private JButton jButtonCopy;
 	private JButton jButtonDelete;
 	private JButton jButtonEdit;
 	private JButton jButtonNew;
 	private JButton jButtonRemove;
 	private JEditorPane jEditorPaneStatblock;
 	private JLabel jLabelBattleList;
 	private JLabel jLabelName;
 	private JLabel jLabelXPTotal;
 	private JList jListBattleList;
 	private JList jListEntries;
 	private JPanel jPanelBottomLeft;
 	private JPanel jPanelBottomRight;
 	private JPanel jPanelCenterLeft;
 	private JPanel jPanelRight;
 	private JScrollPane jScrollPaneBattleList;
 	private JScrollPane jScrollPaneEntries;
 	private JScrollPane jScrollPaneStatblock;
 	private JSplitPane jSplitPaneMain;
 	private JSplitPane jSplitPaneSub;
 	private JTextField jTextFieldName;
 	private JTextField jTextFieldXPTotal;
 	private JToolBar jToolBarTopLeft;
 	private JToolBar jToolBarTopRight;
 
 	/**
 	 * Creates a default library view.
 	 */
 	public Library() {
 		initComponents();
 	}
 
 	/**
 	 * Creates a library view using the provided statblock, and with the
 	 * provided frame as the parent.
 	 * 
 	 * @param statlib
 	 *            the library
 	 * @param c
 	 *            the parent frame
 	 */
 	public Library(StatLibrary statlib, Frame c) {
 		super(c);
 		initComponents();
 		setLocationRelativeTo(c);
 		setStatLib(statlib);
 		resetListFromClass();
 	}
 
 	private void initComponents() {
 		setTitle("Statblock Library");
 		setFont(new Font("Dialog", Font.PLAIN, 12));
 		setBackground(Color.white);
 		setModal(true);
 		setForeground(Color.black);
 		add(getJSplitPaneMain(), BorderLayout.CENTER);
 		addWindowListener(new WindowAdapter() {
 
 			@Override
 			public void windowClosing(WindowEvent event) {
 				windowWindowClosing(event);
 			}
 		});
 		setSize(800, 600);
 	}
 
 	private JButton getJButtonAdd() {
 		if (jButtonAdd == null) {
 			jButtonAdd = new JButton();
 			jButtonAdd.setFont(new Font("Dialog", Font.BOLD, 10));
 			jButtonAdd.setText("Add");
 			jButtonAdd.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 			jButtonAdd.setEnabled(false);
 			jButtonAdd.setDefaultCapable(false);
 			jButtonAdd.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent event) {
 					jButtonAddActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonAdd;
 	}
 
 	private JButton getJButtonATLoad() {
 		if (jButtonATLoad == null) {
 			jButtonATLoad = new JButton();
 			jButtonATLoad.setFont(new Font("Dialog", Font.BOLD, 10));
 			jButtonATLoad.setText("AT Load");
 			jButtonATLoad.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 			jButtonATLoad.setDefaultCapable(false);
 			jButtonATLoad.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent event) {
 					jButtonATLoadActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonATLoad;
 	}
 
 	private JButton getJButtonCBLoad() {
 		if (jButtonCBLoad == null) {
 			jButtonCBLoad = new JButton();
 			jButtonCBLoad.setFont(new Font("Dialog", Font.BOLD, 10));
 			jButtonCBLoad.setText("CB Load");
 			jButtonCBLoad.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 			jButtonCBLoad.setDefaultCapable(false);
 			jButtonCBLoad.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent event) {
 					jButtonCBLoadActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonCBLoad;
 	}
 
 	private JButton getJButtonCopy() {
 		if (jButtonCopy == null) {
 			jButtonCopy = new JButton();
 			jButtonCopy.setFont(new Font("Dialog", Font.BOLD, 10));
 			jButtonCopy.setText("Copy");
 			jButtonCopy.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 			jButtonCopy.setEnabled(false);
 			jButtonCopy.setDefaultCapable(false);
 			jButtonCopy.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent event) {
 					jButtonCopyActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonCopy;
 	}
 
 	private JButton getJButtonDelete() {
 		if (jButtonDelete == null) {
 			jButtonDelete = new JButton();
 			jButtonDelete.setFont(new Font("Dialog", Font.BOLD, 10));
 			jButtonDelete.setText("Delete");
 			jButtonDelete.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 			jButtonDelete.setEnabled(false);
 			jButtonDelete.setDefaultCapable(false);
 			jButtonDelete.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent event) {
 					jButtonDeleteActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonDelete;
 	}
 
 	private JButton getJButtonEdit() {
 		if (jButtonEdit == null) {
 			jButtonEdit = new JButton();
 			jButtonEdit.setFont(new Font("Dialog", Font.BOLD, 10));
 			jButtonEdit.setText("Edit");
 			jButtonEdit.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 			jButtonEdit.setEnabled(false);
 			jButtonEdit.setDefaultCapable(false);
 			jButtonEdit.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent event) {
 					jButtonEditActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonEdit;
 	}
 
 	private JButton getJButtonNew() {
 		if (jButtonNew == null) {
 			jButtonNew = new JButton();
 			jButtonNew.setFont(new Font("Dialog", Font.BOLD, 10));
 			jButtonNew.setText("New");
 			jButtonNew.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 			jButtonNew.setDefaultCapable(false);
 			jButtonNew.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent event) {
 					jButtonNewActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonNew;
 	}
 
 	private JButton getJButtonRemove() {
 		if (jButtonRemove == null) {
 			jButtonRemove = new JButton();
 			jButtonRemove.setFont(new Font("Dialog", Font.BOLD, 10));
 			jButtonRemove.setText("Remove");
 			jButtonRemove.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
 			jButtonRemove.setEnabled(false);
 			jButtonRemove.setDefaultCapable(false);
 			jButtonRemove.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent event) {
 					jButtonRemoveActionActionPerformed(event);
 				}
 			});
 		}
 		return jButtonRemove;
 	}
 
 	private JEditorPane getJEditorPaneStatblock() {
 		if (jEditorPaneStatblock == null) {
 			jEditorPaneStatblock = new JEditorPane();
 			jEditorPaneStatblock.setContentType("text/html");
 			jEditorPaneStatblock.setEditable(false);
 			jEditorPaneStatblock.addHyperlinkListener(new HyperlinkListener() {
 
 				@Override
 				public void hyperlinkUpdate(HyperlinkEvent e) {
 					jEditorPaneStatblockHyperlinkHyperlinkUpdate(e);
 				}
 			});
 		}
 		return jEditorPaneStatblock;
 	}
 
 	private JLabel getJLabelBattleList() {
 		if (jLabelBattleList == null) {
 			jLabelBattleList = new JLabel();
 			jLabelBattleList.setFont(new Font("Dialog", Font.BOLD, 10));
 			jLabelBattleList.setText("Battle List: ");
 		}
 		return jLabelBattleList;
 	}
 
 	private JLabel getJLabelName() {
 		if (jLabelName == null) {
 			jLabelName = new JLabel();
 			jLabelName.setFont(new Font("Dialog", Font.BOLD, 10));
 			jLabelName.setText("Name: ");
 		}
 		return jLabelName;
 	}
 
 	private JLabel getJLabelXPTotal() {
 		if (jLabelXPTotal == null) {
 			jLabelXPTotal = new JLabel();
 			jLabelXPTotal.setFont(new Font("Dialog", Font.BOLD, 10));
 			jLabelXPTotal.setText("XP Total: ");
 		}
 		return jLabelXPTotal;
 	}
 
 	private JList getJListBattleList() {
 		if (jListBattleList == null) {
 			jListBattleList = new JList();
 			DefaultListModel listModel = new DefaultListModel();
 			jListBattleList.setModel(listModel);
 			jListBattleList.addMouseListener(new MouseAdapter() {
 
 				@Override
 				public void mouseClicked(MouseEvent event) {
 					jListBattleListMouseMouseClicked(event);
 				}
 			});
 			jListBattleList.addListSelectionListener(new ListSelectionListener() {
 
 				@Override
 				public void valueChanged(ListSelectionEvent event) {
 					jListBattleListListSelectionValueChanged(event);
 				}
 			});
 		}
 		return jListBattleList;
 	}
 
 	private JList getJListEntries() {
 		if (jListEntries == null) {
 			jListEntries = new JList();
 			DefaultListModel listModel = new DefaultListModel();
 			jListEntries.setModel(listModel);
 			jListEntries.addMouseListener(new MouseAdapter() {
 
 				@Override
 				public void mouseClicked(MouseEvent event) {
 					jListEntriesMouseMouseClicked(event);
 				}
 			});
 			jListEntries.addListSelectionListener(new ListSelectionListener() {
 
 				@Override
 				public void valueChanged(ListSelectionEvent event) {
 					jListEntriesListSelectionValueChanged(event);
 				}
 			});
 		}
 		return jListEntries;
 	}
 
 	private JPanel getJPanelBottomLeft() {
 		if (jPanelBottomLeft == null) {
 			jPanelBottomLeft = new JPanel();
 			jPanelBottomLeft.setLayout(new BorderLayout());
 			jPanelBottomLeft.add(getJLabelName(), BorderLayout.WEST);
 			jPanelBottomLeft.add(getJTextFieldName(), BorderLayout.CENTER);
 		}
 		return jPanelBottomLeft;
 	}
 
 	private JPanel getJPanelBottomRight() {
 		if (jPanelBottomRight == null) {
 			jPanelBottomRight = new JPanel();
 			jPanelBottomRight.setLayout(new BorderLayout());
 			jPanelBottomRight.add(getJLabelXPTotal(), BorderLayout.WEST);
 			jPanelBottomRight.add(getJTextFieldXPTotal(), BorderLayout.CENTER);
 		}
 		return jPanelBottomRight;
 	}
 
 	private JPanel getJPanelCenterLeft() {
 		if (jPanelCenterLeft == null) {
 			jPanelCenterLeft = new JPanel();
 			jPanelCenterLeft.setLayout(new BorderLayout());
 			jPanelCenterLeft.add(getJToolBarTopLeft(), BorderLayout.NORTH);
 			jPanelCenterLeft.add(getJScrollPaneEntries(), BorderLayout.CENTER);
 			jPanelCenterLeft.add(getJPanelBottomLeft(), BorderLayout.SOUTH);
 		}
 		return jPanelCenterLeft;
 	}
 
 	private JPanel getJPanelRight() {
 		if (jPanelRight == null) {
 			jPanelRight = new JPanel();
 			jPanelRight.setLayout(new BorderLayout());
 			jPanelRight.add(getJToolBarTopRight(), BorderLayout.NORTH);
 			jPanelRight.add(getJPanelBottomRight(), BorderLayout.SOUTH);
 			jPanelRight.add(getJScrollPaneBattleList(), BorderLayout.CENTER);
 		}
 		return jPanelRight;
 	}
 
 	private JScrollPane getJScrollPaneBattleList() {
 		if (jScrollPaneBattleList == null) {
 			jScrollPaneBattleList = new JScrollPane();
 			jScrollPaneBattleList.setViewportView(getJListBattleList());
 		}
 		return jScrollPaneBattleList;
 	}
 
 	private JScrollPane getJScrollPaneEntries() {
 		if (jScrollPaneEntries == null) {
 			jScrollPaneEntries = new JScrollPane();
 			jScrollPaneEntries.setViewportView(getJListEntries());
 		}
 		return jScrollPaneEntries;
 	}
 
 	private JScrollPane getJScrollPaneStatblock() {
 		if (jScrollPaneStatblock == null) {
 			jScrollPaneStatblock = new JScrollPane();
 			jScrollPaneStatblock.setViewportView(getJEditorPaneStatblock());
 		}
 		return jScrollPaneStatblock;
 	}
 
 	private JSplitPane getJSplitPaneMain() {
 		if (jSplitPaneMain == null) {
 			jSplitPaneMain = new JSplitPane();
 			jSplitPaneMain.setDividerLocation(266);
 			jSplitPaneMain.setDividerSize(1);
 			jSplitPaneMain.setLeftComponent(getJPanelCenterLeft());
 			jSplitPaneMain.setRightComponent(getJSplitPaneSub());
 		}
 		return jSplitPaneMain;
 	}
 
 	private JSplitPane getJSplitPaneSub() {
 		if (jSplitPaneSub == null) {
 			jSplitPaneSub = new JSplitPane();
 			jSplitPaneSub.setDividerLocation(266);
 			jSplitPaneSub.setDividerSize(1);
 			jSplitPaneSub.setResizeWeight(1.0);
 			jSplitPaneSub.setLeftComponent(getJScrollPaneStatblock());
 			jSplitPaneSub.setRightComponent(getJPanelRight());
 		}
 		return jSplitPaneSub;
 	}
 
 	private JTextField getJTextFieldName() {
 		if (jTextFieldName == null) {
 			jTextFieldName = new JTextField();
 			jTextFieldName.addInputMethodListener(new InputMethodListener() {
 
 				@Override
 				public void caretPositionChanged(InputMethodEvent arg0) {
 					// do nothing
 				}
 
 				@Override
 				public void inputMethodTextChanged(InputMethodEvent event) {
 					jTextFieldNameInputMethodInputMethodTextChanged(event);
 				}
 			});
 		}
 		return jTextFieldName;
 	}
 
 	private JTextField getJTextFieldXPTotal() {
 		if (jTextFieldXPTotal == null) {
 			jTextFieldXPTotal = new JTextField();
 			jTextFieldXPTotal.setEnabled(false);
 		}
 		return jTextFieldXPTotal;
 	}
 
 	private JToolBar getJToolBarTopLeft() {
 		if (jToolBarTopLeft == null) {
 			jToolBarTopLeft = new JToolBar();
 			jToolBarTopLeft.setFloatable(false);
 			jToolBarTopLeft.add(getJButtonNew());
 			jToolBarTopLeft.addSeparator();
 			jToolBarTopLeft.add(getJButtonCopy());
 			jToolBarTopLeft.addSeparator();
 			jToolBarTopLeft.add(getJButtonEdit());
 			jToolBarTopLeft.addSeparator();
 			jToolBarTopLeft.add(getJButtonDelete());
 			jToolBarTopLeft.addSeparator();
 			jToolBarTopLeft.add(getJButtonCBLoad());
 			jToolBarTopLeft.addSeparator();
 			jToolBarTopLeft.add(getJButtonATLoad());
 		}
 		return jToolBarTopLeft;
 	}
 
 	private JToolBar getJToolBarTopRight() {
 		if (jToolBarTopRight == null) {
 			jToolBarTopRight = new JToolBar();
 			jToolBarTopRight.setFloatable(false);
 			jToolBarTopRight.add(getJLabelBattleList());
 			jToolBarTopRight.addSeparator();
 			jToolBarTopRight.add(getJButtonAdd());
 			jToolBarTopRight.addSeparator();
 			jToolBarTopRight.add(getJButtonRemove());
 		}
 		return jToolBarTopRight;
 	}
 
 	/**
 	 * Returns the stat library.
 	 * 
 	 * @return the stat library
 	 */
 	private StatLibrary getStatLib() {
 		return _statLib;
 	}
 
 	/**
 	 * Returns the list of stats to add to the library.
 	 * 
 	 * @return the list
 	 */
 	public List<Stats> getStatsToAdd() {
 		return _statsToAdd;
 	}
 
 	/**
 	 * Event Add clicked.
 	 * 
 	 * @param event
 	 */
 	private void jButtonAddActionActionPerformed(ActionEvent event) {
 		if (getJListEntries().getSelectedIndex() >= 0) {
 			for (Object item : getJListEntries().getSelectedValues()) {
 				((DefaultListModel) getJListBattleList().getModel()).addElement(item);
 			}
 			updateAddXPTotals();
 		}
 	}
 
 	/**
 	 * Event: AT Load clicked.
 	 * 
 	 * @param event
 	 */
 	private void jButtonATLoadActionActionPerformed(ActionEvent event) {
 		Stats stat;
 		JFileChooser fc = new JFileChooser();
 		fc.setDialogTitle("Load from Adventure Tools");
 		fc.setMultiSelectionEnabled(true);
 		fc.setCurrentDirectory(Settings.getWorkingDirectory());
 		fc.setFileFilter(new FileFilter() {
 
 			@Override
 			public boolean accept(File f) {
 				return (f.isDirectory() || f.getName().endsWith(".monster"));
 			}
 
 			@Override
 			public String getDescription() {
 				return "Adventure Tools monster files (*.monster)";
 			}
 		});
 
 		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
 			Settings.setWorkingDirectory(fc.getCurrentDirectory());
 			for (File f : fc.getSelectedFiles()) {
 				stat = new Stats();
 				if (stat.loadFromMonsterFile(f.getAbsolutePath())) {
 					if (stat.isValid()) {
 						if (getStatLib().contains(stat.getHandle())) {
 							int n = JOptionPane.showConfirmDialog(this, stat.getHandle() + " already exists in the library.\n"
 									+ "Overwrite?", "Pre-existing Statblock Found", JOptionPane.YES_NO_OPTION);
 							if (n == JOptionPane.CANCEL_OPTION) {
 								continue;
 							}
 						}
 						getStatLib().add(stat, true);
 					}
 				}
 			}
 			getJTextFieldName().setText("");
 			resetListFromClass();
 		}
 	}
 
 	/**
 	 * Event: CB Load clicked.
 	 * 
 	 * @param event
 	 */
 	private void jButtonCBLoadActionActionPerformed(ActionEvent event) {
 		Stats stat;
 		JFileChooser fc = new JFileChooser();
 		fc.setDialogTitle("Load Character File");
 		fc.setMultiSelectionEnabled(true);
 		fc.setCurrentDirectory(Settings.getWorkingDirectory());
 		fc.setFileFilter(new FileFilter() {
 
 			@Override
 			public boolean accept(File f) {
 				return (f.isDirectory() || f.getName().endsWith(".dnd4e"));
 			}
 
 			@Override
 			public String getDescription() {
 				return "Character files (*.dnd4e)";
 			}
 		});
 
 		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
 			Settings.setWorkingDirectory(fc.getCurrentDirectory());
 			for (File f : fc.getSelectedFiles()) {
 				stat = new Stats();
 				if (stat.loadFromCBFile(f.getAbsolutePath())) {
 					if (stat.isValid()) {
 						if (getStatLib().contains(stat.getHandle())) {
 							int n = JOptionPane.showConfirmDialog(this, stat.getHandle() + " already exists in the library.\n"
									+ "Overwrite?", "Pre-existing Statblock Found", JOptionPane.QUESTION_MESSAGE);
 							if (n == JOptionPane.CANCEL_OPTION) {
 								continue;
 							}
 						}
 						getStatLib().add(stat, true);
 					}
 				}
 			}
 			getJTextFieldName().setText("");
 			resetListFromClass();
 		}
 	}
 
 	/**
 	 * Event: Copy button pressed.
 	 * 
 	 * @param event
 	 */
 	private void jButtonCopyActionActionPerformed(ActionEvent event) {
 		if (getJListEntries().getSelectedIndex() >= 0) {
 			String handle = (String) getJListEntries().getSelectedValue();
 
 			if (getStatLib().contains(handle)) {
 				Stats stat = new Stats(getStatLib().get(handle));
 				Statblock statblockWin = new Statblock(stat, this);
 				statblockWin.setVisible(true);
 
 				if (statblockWin.getStat() != null) {
 					String newHandle = statblockWin.getStat().getHandle();
 					if (getStatLib().contains(newHandle)) {
 						int n = JOptionPane.showConfirmDialog(this, handle + " already exists in the library. Overwrite?");
 						if (n == JOptionPane.YES_OPTION) {
 							getStatLib().add(statblockWin.getStat(), true);
 						}
 					} else {
 						getStatLib().add(statblockWin.getStat());
 					}
 					getJTextFieldName().setText("");
 					resetListFromClass();
 				}
 				statblockWin.dispose();
 				getJListEntries().setSelectedIndex(-1);
 			}
 		}
 	}
 
 	/**
 	 * Event: Delete clicked.
 	 * 
 	 * @param event
 	 */
 	private void jButtonDeleteActionActionPerformed(ActionEvent event) {
 		if (getJListEntries().getSelectedIndex() >= 0) {
 			List<String> list = new ArrayList<String>();
 
 			for (Object entry : getJListEntries().getSelectedValues()) {
 				list.add((String) entry);
 			}
 
 			int n = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the "
 					+ "following entries from the library?\n\n" + list.toString(), "Confirm Statblock Delete",
 					JOptionPane.YES_NO_OPTION);
 
 			if (n == JOptionPane.YES_OPTION) {
 				for (String handle : list) {
 					getStatLib().remove(handle);
 					((DefaultListModel) getJListEntries().getModel()).removeElement(handle);
 					((DefaultListModel) getJListBattleList().getModel()).removeElement(handle);
 				}
 				resetListFromClass();
 				updateAddXPTotals();
 			}
 		}
 	}
 
 	/**
 	 * Event: Edit clicked.
 	 * 
 	 * @param event
 	 */
 	private void jButtonEditActionActionPerformed(ActionEvent event) {
 		if (getJListEntries().getSelectedIndex() >= 0) {
 			String handle = (String) getJListEntries().getSelectedValue();
 
 			if (getStatLib().contains(handle)) {
 				Stats stat = getStatLib().get(handle);
 				Statblock statblockWin = new Statblock(stat, this);
 				statblockWin.setVisible(true);
 
 				if (statblockWin.getStat() != null) {
 					Boolean flag = true;
 					String newHandle = statblockWin.getStat().getHandle();
 
 					if (!handle.contentEquals(newHandle) && getStatLib().contains(newHandle)) {
 						JOptionPane.showMessageDialog(this, newHandle + " already exists in the library.", "Duplicate Statblock",
 								JOptionPane.ERROR_MESSAGE);
 						flag = false;
 					}
 
 					if (flag) {
 						getStatLib().remove(handle);
 						getStatLib().add(statblockWin.getStat());
 						getJTextFieldName().setText("");
 						resetListFromClass();
 						getJListEntries().setSelectedValue(newHandle, true);
 						getJListEntries().dispatchEvent(new MouseEvent(this, 0, 0, 0, 0, 0, 1, false));
 
 						if (!handle.contentEquals(newHandle)) {
 							DefaultListModel model = (DefaultListModel) getJListBattleList().getModel();
 							while (model.indexOf(handle) >= 0) {
 								model.setElementAt(newHandle, model.indexOf(handle));
 							}
 						}
 					}
 					updateAddXPTotals();
 				}
 				statblockWin.dispose();
 				getJListEntries().setSelectedIndex(-1);
 			}
 		}
 	}
 
 	/**
 	 * Event: New clicked.
 	 * 
 	 * @param event
 	 */
 	private void jButtonNewActionActionPerformed(ActionEvent event) {
 		Stats stat = new Stats();
 		Statblock statblockWin = new Statblock(stat, this);
 		statblockWin.setVisible(true);
 
 		if (statblockWin.getStat() != null) {
 			String handle = statblockWin.getStat().getHandle();
 			if (getStatLib().contains(handle)) {
 				int n = JOptionPane.showConfirmDialog(this, handle + " already exists in the library. Overwrite?");
 				if (n == JOptionPane.YES_OPTION) {
 					getStatLib().add(statblockWin.getStat(), true);
 				}
 			} else {
 				getStatLib().add(statblockWin.getStat());
 			}
 			getJTextFieldName().setText("");
 			resetListFromClass();
 		}
 
 		statblockWin.dispose();
 		getJListEntries().setSelectedIndex(-1);
 	}
 
 	/**
 	 * Event: Remove clicked.
 	 * 
 	 * @param event
 	 */
 	private void jButtonRemoveActionActionPerformed(ActionEvent event) {
 		DefaultListModel model = (DefaultListModel) getJListBattleList().getModel();
 		while (getJListBattleList().getSelectedIndex() >= 0) {
 			model.removeElementAt(getJListBattleList().getSelectedIndex());
 		}
 		updateAddXPTotals();
 	}
 
 	/**
 	 * Event: Statblock display, hyperlink updated.
 	 * 
 	 * @param event
 	 */
 	private void jEditorPaneStatblockHyperlinkHyperlinkUpdate(HyperlinkEvent event) {
 		if (event.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
 			String dice = event.getDescription().substring(1).trim();
 			if (dice.startsWith("+")) {
 				JOptionPane.showMessageDialog(this, DiceBag.roll("1d20" + dice, 9));
 			} else {
 				JOptionPane.showMessageDialog(this, DiceBag.roll(dice));
 			}
 		}
 	}
 
 	/**
 	 * Event: Battle list selection changed.
 	 * 
 	 * @param event
 	 */
 	private void jListBattleListListSelectionValueChanged(ListSelectionEvent event) {
 		if (getJListBattleList().getModel().getSize() > 0) {
 			getJButtonRemove().setEnabled(true);
 		} else {
 			getJButtonRemove().setEnabled(false);
 		}
 	}
 
 	/**
 	 * Event: Battle list clicked.
 	 * 
 	 * @param event
 	 */
 	private void jListBattleListMouseMouseClicked(MouseEvent event) {
 		if (getJListBattleList().getSelectedIndex() >= 0) {
 			getJTextFieldName().setText("");
 			getJListEntries().setSelectedValue(getJListBattleList().getSelectedValue(), true);
 			getJListEntries().dispatchEvent(new MouseEvent(this, 0, 0, 0, 0, 0, 1, false));
 		}
 		if (event.getClickCount() == 2) {
 			getJButtonEdit().doClick();
 		}
 	}
 
 	/**
 	 * Event: List entries selection changed.
 	 * 
 	 * @param event
 	 */
 	private void jListEntriesListSelectionValueChanged(ListSelectionEvent event) {
 		if (getJListEntries().getSelectedIndex() >= 0) {
 			getJEditorPaneStatblock().setText(getStatLib().get((String) getJListEntries().getSelectedValue()).getStatsHTML());
 			getJButtonCopy().setEnabled(true);
 			getJButtonEdit().setEnabled(true);
 			getJButtonDelete().setEnabled(true);
 			getJButtonAdd().setEnabled(true);
 		} else {
 			getJEditorPaneStatblock().setText("");
 			getJButtonCopy().setEnabled(false);
 			getJButtonEdit().setEnabled(false);
 			getJButtonDelete().setEnabled(false);
 			getJButtonAdd().setEnabled(false);
 		}
 		getJEditorPaneStatblock().setCaretPosition(0);
 	}
 
 	/**
 	 * Event: Entries list clicked.
 	 * 
 	 * @param event
 	 */
 	private void jListEntriesMouseMouseClicked(MouseEvent event) {
 		if (event.getClickCount() == 2) {
 			getJButtonEdit().doClick();
 		}
 	}
 
 	/**
 	 * Event: Name field text changed.
 	 * 
 	 * @param event
 	 */
 	private void jTextFieldNameInputMethodInputMethodTextChanged(InputMethodEvent event) {
 		resetListFromClass();
 		getJListEntries().setSelectedValue(getJTextFieldName().getText(), true);
 		getJListEntries().dispatchEvent(new MouseEvent(this, 0, 0, 0, 0, 0, 1, false));
 	}
 
 	/**
 	 * Resets the list of fighters from whom the user can choose.
 	 */
 	private void resetListFromClass() {
 		String filter = getJTextFieldName().getText().toLowerCase();
 		DefaultListModel model = (DefaultListModel) getJListEntries().getModel();
 		model.clear();
 
 		for (Stats stat : getStatLib().values()) {
 			if (filter.isEmpty() || stat.getHandle().toLowerCase().contains(filter)) {
 				model.addElement(stat.getHandle());
 			}
 		}
 		getJEditorPaneStatblock().setText("");
 		getJButtonEdit().setEnabled(false);
 		getJButtonDelete().setEnabled(false);
 		getJButtonAdd().setEnabled(false);
 	}
 
 	/**
 	 * Sets the library view's StatLibrary.
 	 * 
 	 * @param statLib
 	 *            the StatLibrary
 	 */
 	private void setStatLib(StatLibrary statLib) {
 		_statLib = statLib;
 	}
 
 	/**
 	 * Update XP total for the encounter.
 	 */
 	private void updateAddXPTotals() {
 		Integer recalcXP = 0;
 		DefaultListModel model = (DefaultListModel) getJListBattleList().getModel();
 
 		for (Object handle : model.toArray()) {
 			if (getStatLib().contains((String) handle)) {
 				if (!getStatLib().get((String) handle).isPC()) {
 					recalcXP += getStatLib().get((String) handle).getXP();
 				}
 			}
 		}
 
 		getJTextFieldXPTotal().setText(recalcXP.toString());
 	}
 
 	/**
 	 * Event: Window closing.
 	 * 
 	 * @param event
 	 */
 	private void windowWindowClosing(WindowEvent event) {
 		DefaultListModel model = (DefaultListModel) getJListBattleList().getModel();
 		for (Object handle : model.toArray()) {
 			if (getStatLib().contains((String) handle)) {
 				getStatsToAdd().add(getStatLib().get((String) handle));
 			}
 		}
 	}
 }
