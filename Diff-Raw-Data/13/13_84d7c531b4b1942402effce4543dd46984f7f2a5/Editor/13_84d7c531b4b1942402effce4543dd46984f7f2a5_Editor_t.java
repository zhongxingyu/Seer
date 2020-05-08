 package editor;
 
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Vector;
 
 import javax.imageio.ImageIO;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.DefaultCellEditor;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JToolBar;
 import javax.swing.JTree;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SpringLayout;
 import javax.swing.ToolTipManager;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.MutableTreeNode;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreeSelectionModel;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.lwjgl.util.vector.Vector3f;
 
 import util.Compressor;
 import util.FileUtilities;
 import util.JSONUtilities;
 import util.JSuggestField;
 import util.SpringUtilities;
 import entity.EntityBuilder;
 import entity.EntityRegistry;
 import entity.util.EntityIO;
 import game.Game;
 import game.TestGame;
 
 /**
  * Map Editor GUI
  * 
  * @author Dakror
  * 
  */
 
 // TODO: add comments to customvalues in entity files
 public class Editor extends JFrame implements TreeSelectionListener
 {
 	public static final FileFilter FILE_FILTER_ENTLIST = new FileNameExtensionFilter("BOLT EntityList-Files (*.entlist)", "entlist");
 	public static final FileFilter FILE_FILTER_ENTITY = new FileNameExtensionFilter("BOLT Entity-Files (*.entity)", "entity");
 	public static final FileFilter FILE_FILTER_OBJECT = new FileNameExtensionFilter("Wavefront geometry file (*.obj)", "obj");
 	public static final FileFilter FILE_FILTER_MAP = new FileNameExtensionFilter("BOLT Map-Files (*.map)", "map");
 
 	static final long serialVersionUID = 1L;
 
 	public static class EditorTreeCellRenderer extends DefaultTreeCellRenderer
 	{
 		static final long serialVersionUID = 1L;
 		Icon entity;
 		Icon folder;
 		int skipped;
 
 		public EditorTreeCellRenderer(int skipped)
 		{
 			this.skipped = skipped;
 
 			Image i = ((ImageIcon) Editor.getIcon("enum_obj")).getImage();
 			entity = new ImageIcon(i.getScaledInstance(16, 16, Image.SCALE_FAST));
 
 			i = ((ImageIcon) Editor.getIcon("fldr_obj")).getImage();
 
 			folder = new ImageIcon(i.getScaledInstance(16, 16, Image.SCALE_FAST));
 
 			setOpenIcon(folder);
 			setClosedIcon(folder);
 		}
 
 		@Override
 		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
 		{
 			if (row > skipped) setLeafIcon(entity);
 			else setLeafIcon(folder);
 
 			return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
 		}
 	}
 
 	public static class EntityDummy
 	{
 		public Vector3f pos;
 		public String name;
 
 		public EntityDummy(Vector3f p, String n)
 		{
 			pos = p;
 			name = n;
 		}
 	}
 
 	File mapFile;
 	File entListFile;
 
 	// -- Components -- //
 	JScrollPane treePanel;
 	JTree tree;
 	JPanel uiPanel;
 	JDialog ui;
 
 	public Canvas canvas;
 
 	// -- Entity Tab -- //
 	JSONArray entities;
 	JSpinner entityPosX, entityPosY, entityPosZ;
 	JSpinner entityRotX, entityRotY, entityRotZ;
 	JTextField entityID;
 	JTable entityCustomValues, entityGroups;
 
 	JTabbedPane tabs;
 
 	JButton apply;
 
 	// -- Events Tab -- //
 	JTable eventEvents;
 	JDialog eventDialog;
 	JSuggestField eventTarget;
 	JComboBox<String> eventFunction;
 
 	// -- toolbar -- //
 	JButton save, saveAs, rawFile, create, clone, delete;
 
 	public Editor()
 	{
 		super("BOLT Editor");
 
 		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 
 		ToolTipManager.sharedInstance().setInitialDelay(0);
 
 		Game.setCurrentGame(new TestGame());
 		Game.getCurrentGame().prepareGame();
 		EntityIO.findEntities(Game.getCurrentGame().entListFilePath);
 
 		addWindowListener(new WindowAdapter()
 		{
 			@Override
 			public void windowClosing(WindowEvent e)
 			{
 				if (isChanged())
 				{
 					int r = JOptionPane.showConfirmDialog(Editor.this, "\"" + mapFile.getName() + "\" has been modified. Save changes?", "Save Resource", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
 					if (r == JOptionPane.YES_OPTION) saveMap();
 					else if (r == JOptionPane.CANCEL_OPTION) return;
 				}
 
 				System.exit(0);
 			}
 		});
 		initComponents();
 		setLocationRelativeTo(null);
 		setVisible(true);
 	}
 
 	public void setTitle(String s)
 	{
 		super.setTitle(((s != null) ? s + " - " : "") + "BOLT Editor");
 	}
 
 	public String getTitle()
 	{
 		return super.getTitle().replaceAll("( - )(BOLT Editor)", "");
 	}
 
 	public ArrayList<EntityDummy> getEntitiesAsDummies()
 	{
 		if (entities == null) return new ArrayList<>();
 
 		ArrayList<EntityDummy> dummies = new ArrayList<>();
 		try
 		{
 			for (int i = 0; i < entities.length(); i++)
 			{
 				JSONObject o = entities.getJSONObject(i);
 				JSONArray pos = o.getJSONArray("pos");
 				dummies.add(new EntityDummy(new Vector3f((float) pos.getDouble(0), (float) pos.getDouble(1), (float) pos.getDouble(2)), o.getString("name")));
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return dummies;
 	}
 
 	void initComponents()
 	{
 		// -- toolbar -- //
 		final JToolBar toolBar = new JToolBar();
 		toolBar.setFloatable(false);
 		toolBar.setRollover(true);
 		toolBar.add(createToolBarButton("New Map", "new_con", new AbstractAction()
 		{
 			static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				refresh();
 				if (isChanged())
 				{
 					int r = JOptionPane.showConfirmDialog(Editor.this, "\"" + mapFile.getName() + "\" has been modified. Save changes?", "Save Resource", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
 					if (r == JOptionPane.YES_OPTION) newMap();
 					else if (r == JOptionPane.CANCEL_OPTION) return;
 				}
 				newMap();
 			}
 		}));
 		toolBar.add(createToolBarButton("Open", "fldr_obj", new AbstractAction()
 		{
 			static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				refresh();
 				if (isChanged())
 				{
 					int r = JOptionPane.showConfirmDialog(Editor.this, "\"" + mapFile.getName() + "\" has been modified. Save changes?", "Save Resource", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
 					if (r == JOptionPane.YES_OPTION) saveMap();
 					else if (r == JOptionPane.CANCEL_OPTION) return;
 				}
 				openMap();
 			}
 		}));
 		save = createToolBarButton("Save", "save_edit", new AbstractAction()
 		{
 			static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				saveMap();
 			}
 		});
 		save.setEnabled(false);
 		toolBar.add(save);
 
 		saveAs = createToolBarButton("Save As", "saveas_edit", new AbstractAction()
 		{
 			static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				saveUMap();
 			}
 		});
 		saveAs.setEnabled(false);
 		toolBar.add(saveAs);
 
 		toolBar.addSeparator();
 
 		rawFile = createToolBarButton("Raw file", "file_obj", new AbstractAction()
 		{
 			static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				showRawFile();
 			}
 		});
 		rawFile.setEnabled(false);
 		toolBar.add(rawFile);
 
 		toolBar.addSeparator();
 
 		toolBar.add(createToolBarButton("Entity Editor", "entity", new AbstractAction()
 		{
 			static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				new EntityEditor();
 			}
 		}));
 
 		toolBar.addSeparator();
 
 		create = createToolBarButton("New Entity", "newenum_wiz", new AbstractAction()
 		{
 			static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				newEntity();
 			}
 		});
 		create.setEnabled(false);
 		toolBar.add(create);
 		clone = createToolBarButton("Clone Entity", "copy_edit", new AbstractAction()
 		{
 			static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				int sel = tree.getSelectionRows()[0] - 2;
 				DefaultMutableTreeNode s = (DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent();
 				((DefaultTreeModel) tree.getModel()).insertNodeInto(new DefaultMutableTreeNode(s.getUserObject().toString() + " (2)"), (MutableTreeNode) s.getParent(), s.getParent().getChildCount());
 				try
 				{
 					JSONObject o = entities.getJSONObject(sel);
 					o.put("id", s.getUserObject().toString() + " (2)");
 					entities.put(o);
 				}
 				catch (Exception e1)
 				{
 					e1.printStackTrace();
 				}
 			}
 		});
 		clone.setEnabled(false);
 		toolBar.add(clone);
 		delete = createToolBarButton("Remove Entity", "enum_obj", new AbstractAction()
 		{
 			static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				int sel = tree.getSelectionRows()[0] - 2;
 				((DefaultTreeModel) tree.getModel()).removeNodeFromParent((MutableTreeNode) tree.getSelectionPath().getLastPathComponent());
 				entities.remove(sel);
 			}
 		});
 		delete.setEnabled(false);
 		toolBar.add(delete);
 
 		// -- components -- //
 		JPanel contentPanel = new JPanel(new BorderLayout());
 
 		contentPanel.add(toolBar, BorderLayout.PAGE_START);
 
 		treePanel = new JScrollPane(null, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		treePanel.setPreferredSize(new Dimension(200, 600));
 
 		contentPanel.add(treePanel, BorderLayout.LINE_START);
 		tree = new JTree(new DefaultMutableTreeNode("World"));
 		tree.setCellRenderer(new EditorTreeCellRenderer(1));
 		tree.setModel(null);
 		tree.setEnabled(false);
 		tree.setShowsRootHandles(true);
 		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 		tree.addTreeSelectionListener(this);
 		tree.setExpandsSelectedPaths(true);
 
 		treePanel.setViewportView(tree);
 
 		canvas = new Canvas();
 		canvas.setPreferredSize(new Dimension(600, 600));
 
 		contentPanel.add(canvas, BorderLayout.EAST);
 
 		setContentPane(contentPanel);
 		pack();
 		setMinimumSize(getSize());
 
 		ui = new JDialog(this, "BOLT Editor", false);
 
 		uiPanel = new JPanel(new FlowLayout());
 		uiPanel.setEnabled(false);
 		uiPanel.setPreferredSize(new Dimension(600, 600));
 
 		ui.setResizable(false);
 		ui.setContentPane(uiPanel);
 		ui.pack();
 		ui.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 		ui.setLocationRelativeTo(null);
 		ui.setVisible(false);
 	}
 
 	boolean isChanged()
 	{
 		if (mapFile == null) return false;
 
 		try
 		{
 			return !writeValue(getData()).equals(writeValue(new JSONObject(Compressor.decompressFile(mapFile))));
 		}
 		catch (JSONException e)
 		{
 			e.printStackTrace();
 			return true;
 		}
 	}
 
 	void showRawFile()
 	{
 		try
 		{
 			JDialog frame = new JDialog(this, true);
 			frame.setTitle("BOLT Editor - Raw File Preview");
 			frame.setSize(getWidth(), getHeight());
 			frame.setDefaultCloseOperation(HIDE_ON_CLOSE);
 			JTextArea area = new JTextArea(getData().toString(4));
 
 			frame.setContentPane(new JScrollPane(area, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS));
 			frame.setLocationRelativeTo(null);
 			frame.setVisible(true);
 		}
 		catch (JSONException e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	void newMap()
 	{
 		mapFile = null;
 		reset();
 	}
 
 	void reset()
 	{
 		rawFile.setEnabled(true);
 		create.setEnabled(true);
 		tree.setEnabled(true);
 		entities = new JSONArray();
 		DefaultMutableTreeNode root = new DefaultMutableTreeNode("World");
 		DefaultMutableTreeNode entities = new DefaultMutableTreeNode("Entities");
 		root.add(entities);
 		tree.setModel(new DefaultTreeModel(root));
 
 	}
 
 	void openMap()
 	{
 		File f = getDefaultJFileChooser(true, this, FILE_FILTER_MAP);
 		if (f == null) return;
 		mapFile = f;
 		try
 		{
 			reset();
 
 			setTitle(mapFile.getPath());
 			JSONObject data = new JSONObject(Compressor.decompressFile(mapFile));
 			entities = data.getJSONArray("entities");
 			DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
 
 			for (int i = 0; i < entities.length(); i++)
 			{
 				dtm.insertNodeInto(new DefaultMutableTreeNode(entities.getJSONObject(i).getString("id")), (DefaultMutableTreeNode) tree.getPathForRow(1).getLastPathComponent(), i);
 				refresh();
 			}
 			tree.expandRow(1);
 		}
 		catch (Exception e)
 		{
 			JOptionPane.showMessageDialog(Editor.this, "Could not open file: \"" + mapFile.getPath() + "\"!", "Error!", JOptionPane.ERROR_MESSAGE);
 			mapFile = null;
 
 			tree.setModel(null);
 
 			return;
 		}
 	}
 
 	JSONObject getData()
 	{
 		try
 		{
 			JSONObject data = new JSONObject();
 			data.put("entities", entities);
 			return data;
 		}
 		catch (JSONException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	JSONObject validateCustomValues(JSONObject customValues, String entityName) throws JSONException
 	{
 		HashMap<String, Object> cv = EntityRegistry.getEntityBuilder(entityName).customValues;
 		if (customValues.length() == cv.size()) return customValues;
 
 		if (customValues.length() > cv.size())
 		{
 			for (int i = 0; i < customValues.length(); i++)
 			{
 				String key = customValues.names().getString(i);
 				if (!cv.containsKey(customValues.get(key))) customValues.remove(key);
 			}
 		}
 
 		for (int i = 0; i < cv.size(); i++)
 		{
 			String key = new ArrayList<String>(cv.keySet()).get(i);
 			if (!customValues.has(key)) customValues.put(key, cv.get(key));
 		}
 
 		return customValues;
 	}
 
 	void saveMap()
 	{
 		if (mapFile == null)
 		{
 			saveUMap();
 			return;
 		}
 
 		try
 		{
 			JSONArray entities = getData().getJSONArray("entities");
 			for (int i = 0; i < entities.length(); i++)
 			{
 				JSONObject object = entities.getJSONObject(i);
 				object.put("custom", validateCustomValues(object.getJSONObject("custom"), object.getString("name")));
 
 				entities.put(i, object);
 			}
 			this.entities = entities;
 
 		}
 		catch (Exception e)
 		{
 		}
 
 		String string = writeValue(getData());
 
 		Compressor.compressFile(mapFile, string);
 		refresh();
 	}
 
 	void saveUMap()
 	{
 		File f = getDefaultJFileChooser(false, this, FILE_FILTER_MAP);
 		if (f == null) return;
 
 		if (f.exists())
 		{
 			int r = JOptionPane.showConfirmDialog(Editor.this, "This file already exists! By creating a new map in that file, it's old content will be lost!", "Warning!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
 			if (r == JOptionPane.CANCEL_OPTION) return;
 		}
 
 		mapFile = new File(f.getPath().replace(".map", "") + ".map");
 		saveMap();
 	}
 
 	void refresh()
 	{
 		if (isChanged())
 		{
 			if (!getTitle().startsWith("*")) setTitle("*" + getTitle());
 		}
 		else
 		{
 			if (mapFile != null) setTitle(mapFile.getPath());
 			else setTitle(null);
 		}
 		revalidate();
 		repaint();
 		treePanel.revalidate();
 
 		boolean isChanged = isChanged();
 		save.setEnabled(isChanged);
 		saveAs.setEnabled(isChanged);
 
 		if (tree.getSelectionRows().length > 0)
 		{
 			clone.setEnabled(tree.getSelectionRows()[0] > 1);
 			delete.setEnabled(tree.getSelectionRows()[0] > 1);
 		}
 		else
 		{
 			clone.setEnabled(false);
 			delete.setEnabled(false);
 		}
 
 	}
 
 	String[] loadEntityList()
 	{
 		EntityIO.entitiesFound.clear();
 		EntityIO.findEntities(Game.getCurrentGame().entListFilePath);
 
 		ArrayList<String> list = new ArrayList<>(EntityIO.entitiesFound.keySet());
 		Collections.sort(list);
 		return list.toArray(new String[] {});
 	}
 
 	void newEntity()
 	{
 		final JDialog dialog = new JDialog(this, "New Entity", true);
 		dialog.setSize(400, 170);
 		final JList<String> list = new JList<>(loadEntityList());
 		list.addListSelectionListener(new ListSelectionListener()
 		{
 			@Override
 			public void valueChanged(ListSelectionEvent e)
 			{
 				DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
 				DefaultMutableTreeNode s = (DefaultMutableTreeNode) tree.getPathForRow(1).getLastPathComponent();
 
 				dtm.insertNodeInto(new DefaultMutableTreeNode(s.getChildCount()), s, s.getChildCount());
 				tree.expandRow(1);
 				try
 				{
 					EntityBuilder entity = EntityRegistry.getEntityBuilder(list.getSelectedValue().replace(".entity", ""));
 
 					JSONObject object = new JSONObject();
 					object.put("name", list.getSelectedValue().replace(".entity", ""));
 					object.put("id", "" + (s.getChildCount() - 1));
 					object.put("pos", new JSONArray(new Double[] { 0d, 0d, 0d }));
 					object.put("rot", new JSONArray(new Double[] { 0d, 0d, 0d }));
 					object.put("events", new JSONArray());
 					object.put("groups", new JSONArray());
 					JSONObject custom = new JSONObject();
 					for (String key : entity.customValues.keySet())
 					{
 						custom.put(key, entity.customValues.get(key));
 					}
 					object.put("custom", custom);
 					Editor.this.entities.put(object);
 
 					dialog.dispose();
 
 					refresh();
 				}
 				catch (Exception e1)
 				{
 					e1.printStackTrace();
 				}
 			}
 		});
 		dialog.setContentPane(new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
 		dialog.setDefaultCloseOperation(HIDE_ON_CLOSE);
 		dialog.setResizable(false);
 		dialog.setLocationRelativeTo(null);
 		dialog.setVisible(true);
 	}
 
 	@Override
 	public void valueChanged(TreeSelectionEvent e)
 	{
 		/*
 		 * uiPanel.setLayout(new FlowLayout()); uiPanel.removeAll(); refresh();
 		 */
 		ui.setVisible(tree.getRowForPath(e.getPath()) > 1);
 
 		// -- toolbar -- //
 
 		if (tree.getRowForPath(e.getPath()) > 1) // Entity1, Entity2, ...
 		{
 			if (entities.length() == 0) return;
 
 			try
 			{
 				showEntityUI(e);
 			}
 			catch (Exception e1)
 			{
 				e1.printStackTrace();
 			}
 		}
 	}
 
 	void showEntityUI(TreeSelectionEvent e) throws Exception
 	{
 		uiPanel.setLayout(null);
 
 		tabs = new JTabbedPane();
 		tabs.setBounds(0, -1, uiPanel.getWidth() + 3, uiPanel.getHeight() - 28);
 		final int entityIndex = tree.getSelectionRows()[0] - 2;
 
 		JSONObject entityData = entities.getJSONObject(entityIndex);
 
 		EntityBuilder entity = EntityRegistry.getEntityBuilder(entityData.getString("name"));
 
 		// -- Entity Tab -- //
 
 		JPanel entityPanel = new JPanel(new SpringLayout());
 		entityPanel.setPreferredSize(new Dimension(uiPanel.getWidth(), 473));
 		entityPanel.add(new JLabel("Name:"));
 		JTextField name = new JTextField(entity.fullName + " (" + entity.name + ")");
 		name.setEditable(false);
 		entityPanel.add(name);
 
 		entityPanel.add(new JLabel("ID:"));
 		entityID = new JTextField(entityData.getString("id"));
 		entityPanel.add(entityID);
 
 		entityPanel.add(new JLabel("Position:"));
 		JPanel panel = new JPanel();
 		entityPosX = new JSpinner(new SpinnerNumberModel(entityData.getJSONArray("pos").getDouble(0), -1000000, 1000000, 1));
 		panel.add(entityPosX);
 		entityPosY = new JSpinner(new SpinnerNumberModel(entityData.getJSONArray("pos").getDouble(1), -1000000, 1000000, 1));
 		panel.add(entityPosY);
 		entityPosZ = new JSpinner(new SpinnerNumberModel(entityData.getJSONArray("pos").getDouble(2), -1000000, 1000000, 1));
 		panel.add(entityPosZ);
 		entityPanel.add(panel);
 
 		entityPanel.add(new JLabel("Rotation:"));
 		panel = new JPanel();
 		entityRotX = new JSpinner(new SpinnerNumberModel(entityData.getJSONArray("rot").getDouble(0), -1000000, 1000000, 1));
 		panel.add(entityRotX);
 		entityRotY = new JSpinner(new SpinnerNumberModel(entityData.getJSONArray("rot").getDouble(1), -1000000, 1000000, 1));
 		panel.add(entityRotY);
 		entityRotZ = new JSpinner(new SpinnerNumberModel(entityData.getJSONArray("rot").getDouble(2), -1000000, 1000000, 1));
 		panel.add(entityRotZ);
 		entityPanel.add(panel);
 
 		entityPanel.add(new JLabel("Custom Values:"));
 
 		final String[][] data = new String[entity.customValues.size()][2];
 		ArrayList<String> keys = new ArrayList<>(entity.customValues.keySet());
 		Collections.sort(keys);
 		for (int i = 0; i < data.length; i++)
 		{
 			data[i] = new String[] { keys.get(i) + " (" + entity.customValues.get(keys.get(i)).getClass().getSimpleName() + ")", ((entityData.getJSONObject("custom").has(keys.get(i))) ? entityData.getJSONObject("custom").get(keys.get(i)).toString() : entity.customValues.get(keys.get(i)).toString()).toString() };
 		}
 		final JButton browse = new JButton("Browse...");
 		browse.setEnabled(false);
 
 		entityCustomValues = new JTable(new DefaultTableModel(data, new String[] { "Name (Type)", "Value" }))
 		{
 			static final long serialVersionUID = 1L;
 
 			public boolean isCellEditable(int row, int column)
 			{
 				if (column == 0) return false; // name column
 
 				if (column == 1 && entityCustomValues.getValueAt(row, 0).toString().contains("(File)")) return false; // file type
 
 				return true;
 			}
 		};
 		entityCustomValues.getTableHeader().setResizingAllowed(false);
 		entityCustomValues.getTableHeader().setReorderingAllowed(false);
 		entityCustomValues.putClientProperty("terminateEditOnFocusLost", true);
 		JScrollPane jsp = new JScrollPane(entityCustomValues, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		entityCustomValues.setFillsViewportHeight(true);
 		entityCustomValues.setRowHeight(22);
 		entityCustomValues.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		entityCustomValues.getSelectionModel().addListSelectionListener(new ListSelectionListener()
 		{
 			@Override
 			public void valueChanged(ListSelectionEvent e)
 			{
 				if (e.getValueIsAdjusting() || entityCustomValues.getSelectedRow() == -1) return;
 
 				browse.setEnabled(data[entityCustomValues.getSelectedRow()][0].contains("(File)"));
 			}
 		});
 		jsp.setPreferredSize(new Dimension(entityCustomValues.getWidth(), 150));
 		entityPanel.add(jsp);
 
 		entityPanel.add(new JLabel());
 		browse.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				File f = getDefaultJFileChooser(false, Editor.this, null);
 				if (f != null) entityCustomValues.setValueAt(FileUtilities.getRelativePath(FileUtilities.getJarFile().getParentFile(), f).replace("\\", "/"), entityCustomValues.getSelectedRow(), 1);
 			}
 		});
 		entityPanel.add(browse);
 
 		entityPanel.add(new JLabel("Groups:"));
 		String[][] groupData = new String[entityData.getJSONArray("groups").length()][];
 		for (int i = 0; i < groupData.length; i++)
 		{
 			groupData[i] = new String[] { entityData.getJSONArray("groups").getString(i) };
 		}
 		entityGroups = new JTable(new DefaultTableModel(groupData, new String[] { "Name" }));
 		entityGroups.getTableHeader().setResizingAllowed(false);
 		entityGroups.getTableHeader().setReorderingAllowed(false);
 		entityGroups.putClientProperty("terminateEditOnFocusLost", true);
 		jsp = new JScrollPane(entityGroups, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		entityGroups.setFillsViewportHeight(true);
 		entityGroups.setRowHeight(22);
 		entityGroups.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		jsp.setPreferredSize(new Dimension(entityGroups.getWidth(), 150));
 		entityPanel.add(jsp);
 
 		entityPanel.add(new JLabel());
 		JPanel panel2 = new JPanel(new GridLayout(1, 2));
 		panel2.setPreferredSize(new Dimension(uiPanel.getWidth() / 2, 22));
 		panel2.add(new JButton(new AbstractAction("Remove")
 		{
 			static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				if (entityGroups.getSelectedRow() > -1) ((DefaultTableModel) entityGroups.getModel()).removeRow(entityGroups.getSelectedRow());
 			}
 		}));
 		panel2.add(new JButton(new AbstractAction("Add")
 		{
 			static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				((DefaultTableModel) entityGroups.getModel()).addRow(new String[] { "" });
 			}
 		}));
 		entityPanel.add(panel2);
 
 		SpringUtilities.makeCompactGrid(entityPanel, 8, 2, 6, 6, 6, 6);
 		JPanel wrap = new JPanel();
 		wrap.add(entityPanel);
 
 		tabs.addTab("Entity", wrap);
 
 		// -- Events Tab -- //
 
 		if (entity.triggers.size() > 0)
 		{
 
 			JPanel eventPanel = new JPanel(new FlowLayout());
 			String[][] eventData = new String[entityData.getJSONArray("events").length()][5];
 			for (int i = 0; i < eventData.length; i++)
 			{
 				JSONObject o = entityData.getJSONArray("events").getJSONObject(i);
 				eventData[i][0] = o.getString("trigger");
 				eventData[i][1] = o.getString("target");
 				eventData[i][2] = o.getString("function");
 				eventData[i][3] = o.getJSONArray("params").join(", ").replace("\"", "");
 				eventData[i][4] = o.getJSONArray("flags").join(", ").replace("\"", "");
 			}
 
 			eventEvents = new JTable(new DefaultTableModel(eventData, new String[] { "Trigger", "Target", "Function", "Parameters", "Flags" }))
 			{
 				static final long serialVersionUID = 1L;
 
 				@Override
 				public boolean isCellEditable(int row, int column)
 				{
 					if (column > 0) return false; // value column
 					return true;
 				}
 			};
 			eventEvents.getTableHeader().setResizingAllowed(false);
 			eventEvents.getTableHeader().setReorderingAllowed(false);
 			final JComboBox<String> trigger = new JComboBox<String>(entity.triggers.toArray(new String[] {}));
 			trigger.setSelectedIndex(0);
 			eventEvents.getColumnModel().getColumn(0).setCellEditor(new DefaultCellEditor(trigger));
 			eventEvents.putClientProperty("terminateEditOnFocusLost", true);
 			eventEvents.setRowHeight(22);
 			eventEvents.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			jsp = new JScrollPane(eventEvents, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 			jsp.setPreferredSize(new Dimension(tabs.getWidth(), tabs.getHeight() - 30 - 35));
 			eventEvents.setFillsViewportHeight(true);
 			eventPanel.add(jsp);
 			eventPanel.add(new JButton(new AbstractAction("New")
 			{
 				static final long serialVersionUID = 1L;
 
 				@Override
 				public void actionPerformed(ActionEvent e)
 				{
 					((DefaultTableModel) eventEvents.getModel()).addRow(new String[] { trigger.getItemAt(0), "", "", "", "", "" });
 				}
 			}));
 			eventPanel.add(new JButton(new AbstractAction("Edit...")
 			{
 				static final long serialVersionUID = 1L;
 
 				@Override
 				public void actionPerformed(ActionEvent e)
 				{
 					if (eventEvents.getSelectedRow() > -1)
 					{
 						try
 						{
 							editEvent();
 						}
 						catch (JSONException e1)
 						{
 							e1.printStackTrace();
 						}
 					}
 				}
 			}));
 			eventPanel.add(new JButton(new AbstractAction("Delete")
 			{
 				static final long serialVersionUID = 1L;
 
 				@Override
 				public void actionPerformed(ActionEvent e)
 				{
 					if (eventEvents.getSelectedRow() > -1) ((DefaultTableModel) eventEvents.getModel()).removeRow(eventEvents.getSelectedRow());
 				}
 			}));
 
 			tabs.addTab("Events", eventPanel);
 		}
 		// -- Final -- //
 		uiPanel.add(tabs);
 
 		apply = new JButton(new AbstractAction("Apply")
 		{
 			static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				try
 				{
 					// -- Entity Tab Data -- //
 					if (entityID.getText().length() == 0)
 					{
 						JOptionPane.showMessageDialog(Editor.this, "Please enter a unique identifier for that entity!", "Error!", JOptionPane.ERROR_MESSAGE);
 						return;
 					}
 
 					JSONObject data = entities.getJSONObject(entityIndex);
 					boolean valid = true;
 					String message = "";
 
 					data.put("id", entityID.getText());
 					data.put("pos", new JSONArray(new Double[] { (double) entityPosX.getValue(), (double) entityPosY.getValue(), (double) entityPosZ.getValue() }));
 					data.put("rot", new JSONArray(new Double[] { (double) entityRotX.getValue(), (double) entityRotY.getValue(), (double) entityRotZ.getValue() }));
 					EntityBuilder builder = EntityRegistry.entries.get(entities.getJSONObject(entityIndex).getString("name"));
 					JSONObject custom = new JSONObject();
 					for (int i = 0; i < entityCustomValues.getModel().getRowCount(); i++)
 					{
 						String name = entityCustomValues.getValueAt(i, 0).toString().split(" ")[0];
 						String type = builder.customValues.get(name).getClass().getSimpleName();
 						String content = entityCustomValues.getValueAt(i, 1).toString();
 
 						if (type.equals("Integer")) custom.put(name, Integer.parseInt(content));
 						else if (type.equals("Double")) custom.put(name, Double.parseDouble(content));
 						else if (type.equals("Byte")) custom.put(name, Byte.parseByte(content));
 						else if (type.equals("Boolean")) custom.put(name, Boolean.parseBoolean(content));
 						else if (type.equals("File")) custom.put(name, content);
 					}
 					data.put("custom", custom);
 
 					JSONArray groups = new JSONArray();
 					for (int i = 0; i < entityGroups.getRowCount(); i++)
 					{
 						String s = entityGroups.getValueAt(i, 0).toString();
 						if (s.length() == 0) continue;
 
 						groups.put(s);
 					}
 					data.put("groups", groups);
 
 					// -- Events Tab Data -- //
 
 					JSONArray events = new JSONArray();
 					if (tabs.getTabCount() > 1)
 					{
 						for (int i = 0; i < eventEvents.getRowCount(); i++)
 						{
 							String trigger = eventEvents.getValueAt(i, 0).toString();
 							String target = eventEvents.getValueAt(i, 1).toString();
 							String function = eventEvents.getValueAt(i, 2).toString();
 							String params = eventEvents.getValueAt(i, 3).toString();
 							String flags = eventEvents.getValueAt(i, 4).toString();
 
 							if (target.length() == 0 || function.length() == 00)
 							{
 								valid = false;
 								message = "Please edit or remove Event #" + i;
 								break;
 							}
 
 							if (valid)
 							{
 								JSONObject o = new JSONObject();
 								o.put("trigger", trigger);
 								o.put("target", target);
 								o.put("function", function);
 								o.put("params", new JSONArray("[" + params + "]"));
 								o.put("flags", new JSONArray("[" + flags + "]"));
 
 								events.put(o);
 							}
 						}
 					}
 
 					data.put("events", events);
 
 					if (!valid)
 					{
 						JOptionPane.showMessageDialog(Editor.this, message, "Error!", JOptionPane.ERROR_MESSAGE);
 						return;
 					}
 
 					entities.put(entityIndex, data);
 					int selectedTab = tabs.getSelectedIndex();
 					((DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent()).setUserObject(entityID.getText());
 					((DefaultTreeModel) tree.getModel()).reload((TreeNode) tree.getSelectionPath().getLastPathComponent());
 					tabs.setSelectedIndex(selectedTab);
 					refresh();
 				}
 				catch (JSONException e1)
 				{
 					e1.printStackTrace();
 				}
 			}
 		});
 		apply.setBounds(0, uiPanel.getHeight() - 27, uiPanel.getPreferredSize().width, 25);
 		uiPanel.add(apply);
 
 		refresh();
 	}
 
 	void editEvent() throws JSONException
 	{
 		final int entityIndex = tree.getSelectionRows()[0] - 2;
 
 		JSONObject entityData = entities.getJSONObject(entityIndex);
 		final EntityBuilder entity = EntityRegistry.getEntityBuilder(entityData.getString("name"));
 
 		eventDialog = new JDialog(this, "BOLT Event Editor", true);
 		JPanel panel = new JPanel(new SpringLayout());
 
 		panel.add(new JLabel("Target:"));
 
 		Vector<String> data = new Vector<>();
 		for (int i = 0; i < entities.length(); i++)
 			data.add(entities.getJSONObject(i).getString("id"));
 
 		data.addAll(getGroups());
 
 		eventTarget = new JSuggestField((Window) eventDialog, data);
 		eventTarget.setPreferredSize(new Dimension(150, 22));
 		panel.add(eventTarget);
 
 		panel.add(new JLabel("Function:"));
 
 		ArrayList<String> functions = new ArrayList<>();
 		functions.add("-- Choose a function --");
 		eventFunction = new JComboBox<>(functions.toArray(new String[] {}));
 
 		eventTarget.getDocument().addDocumentListener(new DocumentListener()
 		{
 
 			@Override
 			public void removeUpdate(DocumentEvent e)
 			{
 				updateEditEventFunction();
 			}
 
 			@Override
 			public void insertUpdate(DocumentEvent e)
 			{
 				updateEditEventFunction();
 			}
 
 			@Override
 			public void changedUpdate(DocumentEvent e)
 			{
 				updateEditEventFunction();
 			}
 		});
 
 		panel.add(eventFunction);
 
 		panel.add(new JLabel("Parameters:"));
 
 		final JTable params = new JTable(new DefaultTableModel(new String[] { "Name (Type)", "Value" }, 0))
 		{
 			static final long serialVersionUID = 1L;
 
 			@Override
 			public boolean isCellEditable(int row, int column)
 			{
 				if (column == 0) return false; // name column
 				return true;
 			}
 		};
 		params.getTableHeader().setResizingAllowed(false);
 		params.getTableHeader().setReorderingAllowed(false);
 
 		eventFunction.addItemListener(new ItemListener()
 		{
 			@Override
 			public void itemStateChanged(ItemEvent e)
 			{
 				if (e.getStateChange() == ItemEvent.DESELECTED) return;
 
 				DefaultTableModel dtm = (DefaultTableModel) params.getModel();
 				dtm.setRowCount(0);
 
 				if (eventFunction.getSelectedIndex() == 0) return;
 
 				String function = entity.functions.get(eventFunction.getSelectedIndex() - 1);
 
 				String[] params = function.substring(function.indexOf("(") + 1, function.indexOf(")")).split(", ");
 				for (String param : params)
 					dtm.addRow(new String[] { param.split(" ")[1] + " (" + param.split(" ")[0] + ")", "" });
 			}
 		});
 
 		params.putClientProperty("terminateEditOnFocusLost", true);
 		params.setRowHeight(22);
 		Vector<String> cusVals = new Vector<>();
 		for (String key : entity.customValues.keySet())
 		{
 			cusVals.add("@" + key);
 		}
 		JSuggestField value = new JSuggestField(eventDialog, cusVals);
 		value.setSuggestMatcher(new JSuggestField.StartsWithMatcher());
 		params.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(value));
 		params.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		JScrollPane jsp = new JScrollPane(params, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		jsp.setPreferredSize(new Dimension(300, 120));
 		params.setFillsViewportHeight(true);
 		panel.add(jsp);
 
 		panel.add(new JLabel("Flags:"));
 
 		ArrayList<String> flagKeys = new ArrayList<>();
 		flagKeys.add("onlyOnce");
 		for (String key : entity.customValues.keySet())
 		{
 			if (entity.customValues.get(key) instanceof Boolean) flagKeys.add(key);
 		}
 
 		String[][] flagData = new String[flagKeys.size()][2];
 		for (int i = 0; i < flagKeys.size(); i++)
 		{
 			String v = "false";
 
 			String flagString = eventEvents.getValueAt(eventEvents.getSelectedRow(), 4).toString();
 			if (flagString.length() > 0)
 			{
 				JSONArray array = new JSONArray("[" + flagString + "]");
 				v = Boolean.toString(JSONUtilities.containsValue(array, flagKeys.get(i)));
 			}
 
 			flagData[i] = new String[] { flagKeys.get(i), v };
 		}
 
 		final JTable flags = new JTable(new DefaultTableModel(flagData, new String[] { "Name", "Value" }))
 		{
 			static final long serialVersionUID = 1L;
 
 			@Override
 			public boolean isCellEditable(int row, int column)
 			{
 				if (column == 0) return false; // name column
 				return true;
 			}
 		};
 		flags.getTableHeader().setResizingAllowed(false);
 		flags.getTableHeader().setReorderingAllowed(false);
 		flags.putClientProperty("terminateEditOnFocusLost", true);
 		flags.setRowHeight(22);
 		flags.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(new JCheckBox()));
 		flags.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		jsp = new JScrollPane(flags, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		jsp.setPreferredSize(new Dimension(300, 120));
 		flags.setFillsViewportHeight(true);
 		panel.add(jsp);
 
 		panel.add(new JLabel());
 
 		JButton apply = new JButton(new AbstractAction("Apply")
 		{
 			static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				boolean valid = true;
 				String message = "";
 				if (eventTarget.getText().length() == 0)
 				{
 					valid = false;
 					message = "Please enter a target ID!\n";
 				}
 
 				if (valid && eventFunction.getSelectedIndex() == 0)
 				{
 					valid = false;
 					message = "Please select a function!\n";
 				}
 
 				if (valid)
 				{
 					for (int i = 0; i < params.getRowCount(); i++)
 					{
 						if (params.getValueAt(i, 1).toString().length() == 0)
 						{
 							valid = false;
 							message = "Please enter a value for the parameter \"" + params.getValueAt(i, 0) + "\"!\n";
 							break;
 						}
 					}
 				}
 
 				if (!valid)
 				{
 					JOptionPane.showMessageDialog(eventDialog, message, "Error!", JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 
 				eventEvents.setValueAt(eventTarget.getText(), eventEvents.getSelectedRow(), 1);
 				eventEvents.setValueAt(eventFunction.getSelectedItem().toString().replaceAll("\\(.{1,}\\)", ""), eventEvents.getSelectedRow(), 2);
 
 				String p = "";
 				for (int i = 0; i < params.getRowCount(); i++)
 					p += params.getValueAt(i, 1).toString() + ", ";
 
 				if (p.length() > 2) p = p.substring(0, p.length() - 2);
 				eventEvents.setValueAt(p, eventEvents.getSelectedRow(), 3);
 
 				String f = "";
 				for (int i = 0; i < flags.getRowCount(); i++)
 				{
 					if (flags.getValueAt(i, 1).toString().equals("true")) f += flags.getValueAt(i, 0).toString() + ", ";
 				}
 
 				if (f.length() > 2) f = f.substring(0, f.length() - 2);
 				eventEvents.setValueAt(f, eventEvents.getSelectedRow(), 4);
 
 				eventDialog.dispose();
 			}
 		});
 		panel.add(apply);
 
 		eventTarget.setText(eventEvents.getValueAt(eventEvents.getSelectedRow(), 1).toString());
 		updateEditEventFunction();
 		eventFunction.setSelectedItem(eventEvents.getValueAt(eventEvents.getSelectedRow(), 2).toString());
 
 		String paramString = eventEvents.getValueAt(eventEvents.getSelectedRow(), 3).toString();
 		if (paramString.length() > 0)
 		{
 			JSONArray array = new JSONArray("[" + paramString + "]");
 			for (int i = 0; i < array.length(); i++)
 			{
 				params.setValueAt(array.get(i), i, 1);
 			}
 		}
 
 		SpringUtilities.makeCompactGrid(panel, 5, 2, 6, 6, 6, 6);
 
 		eventDialog.setContentPane(panel);
 		eventDialog.pack();
 		eventDialog.setResizable(false);
 		eventDialog.setDefaultCloseOperation(HIDE_ON_CLOSE);
 		eventDialog.setLocationRelativeTo(null);
 		eventDialog.setVisible(true);
 	}
 
 	void updateEditEventFunction()
 	{
 		try
 		{
 			for (int i = 0; i < entities.length(); i++)
 			{
 				if (entities.getJSONObject(i).getString("id").equals(eventTarget.getText()))
 				{
 					EntityBuilder entity = EntityRegistry.getEntityBuilder(entities.getJSONObject(i).getString("name"));
 					ArrayList<String> functions = new ArrayList<>();
 					functions.add("-- Choose a function --");
 					for (String f : entity.functions)
 					{
 						functions.add(f.substring(0, f.indexOf("(")).trim());
 					}
 					eventFunction.setModel(new DefaultComboBoxModel<String>(functions.toArray(new String[] {})));
 				}
 			}
 
 			if (eventTarget.getText().startsWith("$"))
 			{
 				ArrayList<String> functions = new ArrayList<>();
 				functions.add("-- Choose a function --");
 				for (String f : getGroupFunctions(eventTarget.getText().substring(1)))
 				{
 					functions.add(f.substring(0, f.indexOf("(")).trim());
 				}
 				eventFunction.setModel(new DefaultComboBoxModel<String>(functions.toArray(new String[] {})));
 			}
 		}
 		catch (JSONException e1)
 		{
 			e1.printStackTrace();
 		}
 	}
 
 	/**
 	 * @param group
 	 *            Shall not contain the $ group indicator
 	 */
 	ArrayList<String> getGroupFunctions(String group)
 	{
 		ArrayList<String> initFunctions = new ArrayList<>();
 		ArrayList<String> functions = new ArrayList<>();
 
 		try
 		{
 			for (int i = 0; i < entities.length(); i++)
 			{
 				JSONObject o = entities.getJSONObject(i);
 				JSONArray gr = o.getJSONArray("groups");
 				if (!JSONUtilities.containsValue(gr, group)) continue;
 
 				EntityBuilder entity = EntityRegistry.getEntityBuilder(o.getString("name"));
 				if (initFunctions.size() == 0)
 				{
 					initFunctions.addAll(entity.functions);
 					functions.addAll(entity.functions);
 				}
 
 				else for (String function : initFunctions)
 				{
 					if (!entity.functions.contains(function))
 					{
 						System.out.println(entity.functions);
 						System.out.println("removing: " + function);
 						functions.remove(function);
 					}
 
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 
 		return functions;
 	}
 
 	ArrayList<String> getGroups()
 	{
 		ArrayList<String> groups = new ArrayList<>();
 		try
 		{
 			for (int i = 0; i < entities.length(); i++)
 			{
 				JSONObject o = entities.getJSONObject(i);
 				JSONArray gr = o.getJSONArray("groups");
 				for (int j = 0; j < gr.length(); j++)
 				{
 					if (!groups.contains("$" + gr.getString(j))) groups.add("$" + gr.getString(j));
 				}
 			}
 		}
 		catch (Exception e)
 		{
 		}
 		Collections.sort(groups);
 		return groups;
 	}
 
 	// -- statics -- //
 
 	public static File getDefaultJFileChooser(boolean open, Window parent, FileFilter filter)
 	{
 		JFileChooser jfc = new JFileChooser(FileUtilities.getJarFile().getParentFile());
 		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		jfc.setMultiSelectionEnabled(false);
 		if (filter != null) jfc.setFileFilter(filter);
 		if (((open) ? jfc.showOpenDialog(parent) : jfc.showSaveDialog(parent)) == JFileChooser.APPROVE_OPTION)
 		{
 			if (!FileUtilities.getHardDrive(jfc.getSelectedFile()).equals(FileUtilities.getHardDrive(FileUtilities.getJarFile())))
 			{
 				JOptionPane.showMessageDialog(parent, "Please choose a file stored on the harddrive \"" + FileUtilities.getHardDrive(FileUtilities.getJarFile()).toString() + "\"!", "Error!", JOptionPane.ERROR_MESSAGE);
 				return null;
 			}
 
 			return jfc.getSelectedFile();
 		}
 
 		return null;
 	}
 
 	public static String writeValue(Object value)
 	{
 		String string = "null";
 		try
 		{
 			if (value instanceof Integer || value instanceof Double || value instanceof Boolean) return value.toString();
 			else if (value instanceof JSONArray)
 			{
 				string = "[";
 				if (((JSONArray) value).length() > 0)
 				{
 					for (int i = 0; i < ((JSONArray) value).length(); i++)
 						string += writeValue((((JSONArray) value).get(i))) + ",";
 
 					string = string.substring(0, string.length() - 1);
 				}
 
 				return string + "]";
 			}
 			else if (value instanceof JSONObject)
 			{
 				string = "{";
 				String[] keys = JSONObject.getNames((JSONObject) value);
 				if (keys != null && keys.length > 0)
 				{
 					Arrays.sort(keys);
 					for (String s : keys)
 						string += "\"" + s + "\":" + writeValue(((JSONObject) value).get(s)) + ",";
 
 					string = string.substring(0, string.length() - 1);
 				}
 				return string + "}";
 			}
 			else return "\"" + value.toString() + "\"";
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return string;
 	}
 
 	public static Icon getIcon(String name)
 	{
 		try
 		{
 			return new ImageIcon(ImageIO.read(Editor.class.getResource("/editor/icons/" + name + ".png")).getScaledInstance(20, 20, Image.SCALE_SMOOTH));
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static JButton createToolBarButton(String tooltip, String icon, Action action)
 	{
 		JButton button = new JButton();
 		button.setPreferredSize(new Dimension(24, 24));
 		button.setIcon(Editor.getIcon(icon));
 		action.putValue(Action.SMALL_ICON, Editor.getIcon(icon));
 		action.putValue(Action.SHORT_DESCRIPTION, tooltip);
 		button.setAction(action);
 		button.setFocusPainted(false);
 
 		return button;
 	}
 }
