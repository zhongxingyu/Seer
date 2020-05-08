 package editor;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SpringLayout;
 import javax.swing.UIManager;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeSelectionModel;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import util.Compressor;
 import util.SpringUtilities;
 import entity.EntityBuilder;
 import entity.EntityRegistry;
 import entity.util.EntityLoader;
 
 public class Editor extends JFrame implements TreeSelectionListener
 {
 	private static final long serialVersionUID = 1L;
 
 	File mapFile;
 	File entListFile;
 	JScrollPane treePanel;
 	JTree tree;
 	JPanel uiPanel;
 	JSONArray entities;
 	JSpinner entityPosX, entityPosY, entityPosZ;
 	JSpinner entityRotX, entityRotY, entityRotZ;
 	JTextField entityID;
 	JTable entityCustomValues;
 	JMenuItem saveFile;
 	JMenuItem saveUFile;
 	JMenu view;
 
 	public Editor()
 	{
 		super("BOLT Editor");
 		try
 		{
 			EntityLoader.findEntities("test/entities/testList.entlist");
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		initComponents();
 		setResizable(false);
 		setLocationRelativeTo(null);
 		setVisible(true);
 	}
 
 	public void initComponents()
 	{
 		JMenuBar menu = new JMenuBar();
 		JMenu file = new JMenu("File");
 		JMenuItem newFile = new JMenuItem(new AbstractAction("New")
 		{
 			private static final long serialVersionUID = 1L;
 
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
 		});
 		newFile.setAccelerator(KeyStroke.getKeyStroke("ctrl N"));
 		file.add(newFile);
 		JMenuItem openFile = new JMenuItem(new AbstractAction("Open...")
 		{
 			private static final long serialVersionUID = 1L;
 
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
 		});
 		openFile.setAccelerator(KeyStroke.getKeyStroke("ctrl O"));
 		file.add(openFile);
 		saveFile = new JMenuItem(new AbstractAction("Save")
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				saveMap();
 			}
 		});
 		saveFile.setEnabled(false);
 		saveFile.setAccelerator(KeyStroke.getKeyStroke("ctrl S"));
 		file.add(saveFile);
 		saveUFile = new JMenuItem(new AbstractAction("Save as...")
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				saveUMap();
 			}
 		});
 		saveUFile.setEnabled(false);
 		saveUFile.setAccelerator(KeyStroke.getKeyStroke("ctrl shift S"));
 		file.add(saveUFile);
 		menu.add(file);
 
 		view = new JMenu("View");
 		JMenuItem raw = new JMenuItem(new AbstractAction("Raw file...")
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				showRawFile();
 			}
 		});
 		view.add(raw);
 		view.setEnabled(false);
 		menu.add(view);
 
 		JMenu mode = new JMenu("Mode");
 		JMenuItem ent = new JMenuItem(new AbstractAction("Entity Editor")
 		{
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(ActionEvent e)
 			{
 				new EntityEditor();
 			}
 		});
 		mode.add(ent);
 		menu.add(mode);
 
 		setJMenuBar(menu);
 
 		JPanel panel = new JPanel(new BorderLayout());
 
 		treePanel = new JScrollPane(null, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 		treePanel.setPreferredSize(new Dimension(200, 0));
 		panel.add(treePanel, BorderLayout.LINE_START);
 		tree = new JTree(new DefaultMutableTreeNode("World"));
 		tree.setModel(null);
 		tree.setEnabled(false);
 		tree.setShowsRootHandles(true);
 		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 		tree.addTreeSelectionListener(this);
 		tree.setExpandsSelectedPaths(true);
 		tree.addMouseListener(new MouseAdapter()
 		{
 			public void mouseClicked(MouseEvent e)
 			{
 				if (e.getButton() != 3) return;
 
 				final int row = tree.getRowForLocation(e.getX(), e.getY());
 
 				if (row <= 1) return;
 
 				tree.setSelectionRow(row);
 
 				JPopupMenu menu = new JPopupMenu();
 				JMenuItem del = new JMenuItem(new AbstractAction("Delete")
 				{
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public void actionPerformed(ActionEvent e)
 					{
 						entities.remove(row - 2);
 						DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
 						dtm.removeNodeFromParent((DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent());
 						refresh();
 					}
 				});
 				menu.add(del);
 
 				menu.show(e.getComponent(), e.getX(), e.getY());
 			}
 		});
 
 		treePanel.setViewportView(tree);
 
 		uiPanel = new JPanel(new FlowLayout());
 		uiPanel.setEnabled(false);
 		uiPanel.setPreferredSize(new Dimension(600, 600));
 		panel.add(uiPanel, BorderLayout.LINE_END);
 
 		setContentPane(panel);
 		pack();
 	}
 
 	public boolean isChanged()
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
 
 	public void showRawFile()
 	{
 		try
 		{
 			JDialog frame = new JDialog(this, true);
 			frame.setTitle("BOLT Editor - Raw File Preview");
 			frame.setSize(400, 400);
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
 
 	public void setTitle(String s)
 	{
 		super.setTitle(((s != null) ? s + " - " : "") + "BOLT Editor");
 	}
 
 	public String getTitle()
 	{
 		return super.getTitle().replaceAll("( - )(BOLT Editor)", "");
 	}
 
 	public void newMap()
 	{
 		mapFile = null;
 		reset();
 	}
 
 	private void reset()
 	{
 		saveFile.setEnabled(true);
 		saveUFile.setEnabled(true);
 		view.setEnabled(true);
 		tree.setEnabled(true);
 		entities = new JSONArray();
 		DefaultMutableTreeNode root = new DefaultMutableTreeNode("World");
 		DefaultMutableTreeNode entities = new DefaultMutableTreeNode("Entities");
 		root.add(entities);
 		tree.setModel(new DefaultTreeModel(root));
 
 		uiPanel.setEnabled(true);
 	}
 
 	public void openMap()
 	{
 		JFileChooser jfc = new JFileChooser("C:/");
 		jfc.setFileFilter(new FileNameExtensionFilter("BOLT Map-Files", "map"));
 		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		jfc.setMultiSelectionEnabled(false);
 		if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
 		{
 			mapFile = jfc.getSelectedFile();
 			try
 			{
 				reset();
 
 				setTitle(mapFile.getPath());
 				JSONObject data = new JSONObject(Compressor.decompressFile(mapFile));
 				entities = data.getJSONArray("entities");
 				DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
 				for (int i = 0; i < entities.length(); i++)
 				{
 					dtm.insertNodeInto(new DefaultMutableTreeNode("Entity" + i), (DefaultMutableTreeNode) tree.getPathForRow(1).getLastPathComponent(), i);
 					refresh();
 				}
 				tree.expandRow(1);
 			}
 			catch (JSONException e)
 			{
 				e.printStackTrace();
 				JOptionPane.showMessageDialog(Editor.this, "Could not open file: \"" + mapFile.getPath() + "\"!", "Error!", JOptionPane.ERROR_MESSAGE);
 				mapFile = null;
 				return;
 			}
 		}
 	}
 
 	private JSONObject getData()
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
 
 	public void saveMap()
 	{
 		if (mapFile == null)
 		{
 			saveUMap();
 			return;
 		}
 
 		String string = writeValue(getData());
 
 		Compressor.compressFile(mapFile, string);
 		refresh();
 	}
 
 	public void saveUMap()
 	{
 		JFileChooser jfc = new JFileChooser("C:/");
 		jfc.setFileFilter(new FileNameExtensionFilter("BOLT Map-Files", "map"));
 		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		jfc.setMultiSelectionEnabled(false);
 		if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
 		{
 
 			mapFile = new File(jfc.getSelectedFile().getPath().replace(".map", "") + ".map");
 			saveMap();
 		}
 	}
 
 	private void refresh()
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
 	}
 
 	private String[] loadEntityList()
 	{
 		ArrayList<String> list = new ArrayList<>();
 		list.add("-- Choose an Entity --");
 
 		EntityLoader.findEntities("test/entities/testList.entlist");
 		for (String key : EntityLoader.entitiesFound.keySet())
 		{
 			list.add(key);
 		}
 
 		return list.toArray(new String[] {});
 	}
 
 	@Override
 	public void valueChanged(TreeSelectionEvent e)
 	{
 		uiPanel.setLayout(new FlowLayout());
 		uiPanel.removeAll();
 		refresh();
 
 		final DefaultMutableTreeNode s = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
 
 		if (tree.getRowForPath(e.getPath()) == 1) // Entities
 		{
 			final JButton newEntity = new JButton();
 
 			final JComboBox<String> entities = new JComboBox<>(loadEntityList());
 			entities.setSelectedIndex(0);
 			entities.addItemListener(new ItemListener()
 			{
 				@Override
 				public void itemStateChanged(ItemEvent e)
 				{
 					if (e.getStateChange() == ItemEvent.SELECTED)
 					{
 						newEntity.setEnabled(entities.getSelectedIndex() > 0);
 					}
 				}
 			});
 			uiPanel.add(entities);
 
 			newEntity.setAction(new AbstractAction("New Entity")
 			{
 				private static final long serialVersionUID = 1L;
 
 				@Override
 				public void actionPerformed(ActionEvent e)
 				{
 					DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
 					dtm.insertNodeInto(new DefaultMutableTreeNode(s.getChildCount()), s, s.getChildCount());
 					tree.expandRow(1);
 					try
 					{
 						EntityBuilder builder = EntityLoader.loadEntity(entities.getSelectedItem().toString().replace(".entity", ""));
 						EntityRegistry.registerEntityBuilder(builder);
 						JSONObject object = new JSONObject();
 						object.put("name", entities.getSelectedItem().toString().replace(".entity", ""));
 						object.put("id", "" + (s.getChildCount() - 1));
 						object.put("pos", new JSONArray(new Double[] { 0d, 0d, 0d }));
 						object.put("rot", new JSONArray(new Double[] { 0d, 0d, 0d }));
 						JSONObject custom = new JSONObject();
 						for (String key : builder.customValues.keySet())
 						{
 							custom.put(key, builder.customValues.get(key));
 						}
 						object.put("custom", custom);
 						Editor.this.entities.put(object);
 						refresh();
 					}
 					catch (Exception e1)
 					{
 						e1.printStackTrace();
 					}
 				}
 			});
 			newEntity.setEnabled(false);
 			newEntity.setPreferredSize(new Dimension(300, 24));
 			uiPanel.add(newEntity);
 			refresh();
 		}
 
 		if (tree.getRowForPath(e.getPath()) > 1) // Entity1, Entity2, ...
 		{
 			try
 			{
 				if (entities.length() == 0) return;
 
 				final int entityIndex = tree.getRowForPath(e.getPath()) - 2;
 
 				if (tree.getRowForPath(e.getPath()) - 2 < 0) return;
 
 				JSONObject entity = entities.getJSONObject(tree.getRowForPath(e.getPath()) - 2);
 
 				EntityBuilder builder = EntityRegistry.entries.get(entity.getString("name"));
 				if (builder == null)
 				{
 					builder = EntityLoader.loadEntity(entity.getString("name"));
 					EntityRegistry.registerEntityBuilder(builder);
 				}
 
 				JPanel uiP = new JPanel(new SpringLayout());
 
 				uiP.add(new JLabel("Name:"));
 				JTextField name = new JTextField(builder.fullName + " (" + builder.name + ")");
 				name.setEditable(false);
 				uiP.add(name);
 
 				uiP.add(new JLabel("Parent:"));
 				JTextField parent = new JTextField(builder.parent);
 				parent.setEditable(false);
 				uiP.add(parent);
 
 				// TODO work
 
 				uiP.add(new JLabel("ID:"));
 				entityID = new JTextField(entity.getString("id"));
 				uiP.add(entityID);
 
 				uiP.add(new JLabel("Position:"));
 				JPanel panel = new JPanel();
 				entityPosX = new JSpinner(new SpinnerNumberModel(entity.getJSONArray("pos").getDouble(0), -1000000, 1000000, 1));
 				panel.add(entityPosX);
 				entityPosY = new JSpinner(new SpinnerNumberModel(entity.getJSONArray("pos").getDouble(1), -1000000, 1000000, 1));
 				panel.add(entityPosY);
 				entityPosZ = new JSpinner(new SpinnerNumberModel(entity.getJSONArray("pos").getDouble(2), -1000000, 1000000, 1));
 				panel.add(entityPosZ);
 				uiP.add(panel);
 
 				uiP.add(new JLabel("Rotation:"));
 				panel = new JPanel();
 				entityRotX = new JSpinner(new SpinnerNumberModel(entity.getJSONArray("rot").getDouble(0), -1000000, 1000000, 1));
 				panel.add(entityRotX);
 				entityRotY = new JSpinner(new SpinnerNumberModel(entity.getJSONArray("rot").getDouble(1), -1000000, 1000000, 1));
 				panel.add(entityRotY);
 				entityRotZ = new JSpinner(new SpinnerNumberModel(entity.getJSONArray("rot").getDouble(2), -1000000, 1000000, 1));
 				panel.add(entityRotZ);
 				uiP.add(panel);
 
 				uiP.add(new JLabel("Custom Values:"));
 
 				final String[][] data = new String[builder.customValues.size()][2];
 				ArrayList<String> keys = new ArrayList<>(builder.customValues.keySet());
 				for (int i = 0; i < data.length; i++)
 				{
 					data[i] = new String[] { keys.get(i) + " (" + builder.customValues.get(keys.get(i)).getClass().getSimpleName() + ")", ((entity.getJSONObject("custom").has(keys.get(i))) ? entity.getJSONObject("custom").get(keys.get(i)).toString() : builder.customValues.get(keys.get(i)).toString()).toString() };
 				}
 
 				final JButton browse = new JButton("Browse...");
 				browse.setEnabled(false);
 
 				entityCustomValues = new JTable(new DefaultTableModel(data, new String[] { "Name (Type)", "Value" }))
 				{
 					private static final long serialVersionUID = 1L;
 
 					public boolean isCellEditable(int row, int column)
 					{
 						if (column == 0) return false; // name column
 
 						if (column == 1 && entityCustomValues.getValueAt(row, 0).toString().contains("(File)")) return false; // file type
 
 						return true;
 					}
 				};
 				entityCustomValues.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
 				JScrollPane jsp = new JScrollPane(entityCustomValues, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 				entityCustomValues.setFillsViewportHeight(true);
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
 				uiP.add(jsp);
 				uiP.add(new JLabel());
 				browse.addActionListener(new ActionListener()
 				{
 					@Override
 					public void actionPerformed(ActionEvent e)
 					{
 						JFileChooser jfc = new JFileChooser("C:/");
 						jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 						jfc.setMultiSelectionEnabled(false);
						if (jfc.showSaveDialog(Editor.this) == JFileChooser.APPROVE_OPTION) entityCustomValues.setValueAt(jfc.getSelectedFile().getPath().replace("\\", "/"), entityCustomValues.getSelectedRow(), 1);
 
 					}
 				});
 				uiP.add(browse);
 				uiP.add(new JLabel());
 				uiP.add(new JButton(new AbstractAction("Apply")
 				{
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public void actionPerformed(ActionEvent e)
 					{
 						try
 						{
 							if (entityID.getText().length() == 0)
 							{
 								JOptionPane.showMessageDialog(Editor.this, "Please enter a unique identifier for that entity!", "Error!", JOptionPane.ERROR_MESSAGE);
 								return;
 							}
 
 							entities.getJSONObject(entityIndex).put("id", entityID.getText());
 							entities.getJSONObject(entityIndex).put("pos", new JSONArray(new Double[] { (double) entityPosX.getValue(), (double) entityPosY.getValue(), (double) entityPosZ.getValue() }));
 							entities.getJSONObject(entityIndex).put("rot", new JSONArray(new Double[] { (double) entityRotX.getValue(), (double) entityRotY.getValue(), (double) entityRotZ.getValue() }));
 							EntityBuilder builder = EntityRegistry.entries.get(entities.getJSONObject(entityIndex).getString("name"));
 							JSONObject custom = new JSONObject();
 							boolean valid = true;
 							String message = "";
 							for (int i = 0; i < entityCustomValues.getModel().getRowCount(); i++)
 							{
 								String name = entityCustomValues.getModel().getValueAt(i, 0).toString().replaceAll("( )(\\(.{1,}\\))", "");
 								String type = builder.customValues.get(name).getClass().getSimpleName();
 								String content = entityCustomValues.getModel().getValueAt(i, 1).toString();
 
 								if (type.equals("Integer"))
 								{
 									try
 									{
 										custom.put(name, Integer.parseInt(content));
 									}
 									catch (Exception e1)
 									{
 										message = "\"" + entityCustomValues.getModel().getValueAt(i, 0).toString() + "\": " + e1.getMessage();
 										valid = false;
 										break;
 									}
 								}
 								else if (type.equals("Float"))
 								{
 									try
 									{
 										custom.put(name, Float.parseFloat(content));
 									}
 									catch (Exception e1)
 									{
 										message = "\"" + entityCustomValues.getModel().getValueAt(i, 0).toString() + "\": " + e1.getMessage();
 										valid = false;
 										break;
 									}
 								}
 								else if (type.equals("Byte"))
 								{
 									try
 									{
 										custom.put(name, Byte.parseByte(content));
 									}
 									catch (Exception e1)
 									{
 										message = "\"" + entityCustomValues.getModel().getValueAt(i, 0).toString() + "\": " + e1.getMessage();
 										valid = false;
 										break;
 									}
 								}
 								else if (type.equals("Boolean"))
 								{
 									try
 									{
 										custom.put(name, Boolean.parseBoolean(content));
 									}
 									catch (Exception e1)
 									{
 										message = "\"" + entityCustomValues.getModel().getValueAt(i, 0).toString() + "\": " + e1.getMessage();
 										valid = false;
 										break;
 									}
 								}
 								else if (type.equals("File"))
 								{
 									try
 									{
 										custom.put(name, content);
 									}
 									catch (Exception e1)
 									{
 										message = "\"" + entityCustomValues.getModel().getValueAt(i, 0).toString() + "\": " + e1.getMessage();
 										valid = false;
 										break;
 									}
 								}
 							}
 
 							if (!valid)
 							{
 								JOptionPane.showMessageDialog(Editor.this, "Please enter your custom values in the same data type as specified in brackets!\n  at " + message, "Error!", JOptionPane.ERROR_MESSAGE);
 								return;
 							}
 
 							entities.getJSONObject(entityIndex).put("custom", custom);
 							int selectedRow = tree.getSelectionRows()[0];
 							((DefaultMutableTreeNode) tree.getSelectionPath().getLastPathComponent()).setUserObject(entityID.getText());
 							((DefaultTreeModel) tree.getModel()).reload();
 							tree.expandRow(1);
 							tree.setSelectionRow(selectedRow);
 							refresh();
 
 						}
 						catch (JSONException e1)
 						{
 							e1.printStackTrace();
 						}
 					}
 				}));
 
 				SpringUtilities.makeCompactGrid(uiP, 8, 2, 6, 6, 6, 6);
 
 				uiPanel.add(uiP);
 				refresh();
 
 			}
 			catch (Exception e1)
 			{
 				e1.printStackTrace();
 			}
 		}
 	}
 }
