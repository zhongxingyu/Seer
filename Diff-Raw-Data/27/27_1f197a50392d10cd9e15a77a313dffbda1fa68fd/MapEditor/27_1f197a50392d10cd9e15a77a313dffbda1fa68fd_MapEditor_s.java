 package editor;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.File;
import java.io.IOException;
 import java.util.ArrayList;
 
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
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.KeyStroke;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SpringLayout;
 import javax.swing.UIManager;
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
 
 public class MapEditor extends JFrame implements TreeSelectionListener
 {
 	private static final long serialVersionUID = 1L;
 
 	File mapFile;
 
 	// -- tree -- //
 	JTree tree;
 
 	// -- components -- //
 	JPanel uiPanel;
 
 	JSONArray entities;
 
 	JSpinner entityPosX, entityPosY, entityPosZ;
 	JSpinner entityRotX, entityRotY, entityRotZ;
 	JTable entityCustomValues;
 
 	// -- menubar -- //
 	JMenuItem saveFile;
 	JMenuItem saveUFile;
 	JMenu view;
 
 	public MapEditor()
 	{
 		super("BOLT MapEditor");
 		try
 		{
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
 
 		setJMenuBar(menu);
 
 		JPanel panel = new JPanel(new BorderLayout());
 
 		tree = new JTree(new DefaultMutableTreeNode("World"));
 		tree.setEnabled(false);
 		tree.setShowsRootHandles(true);
 		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 		tree.setPreferredSize(new Dimension(200, 600));
 		tree.addTreeSelectionListener(this);
 		tree.setExpandsSelectedPaths(true);
 		panel.add(tree, BorderLayout.LINE_START);
 
 		uiPanel = new JPanel(new FlowLayout());
 		uiPanel.setEnabled(false);
 		uiPanel.setPreferredSize(new Dimension(600, 600));
 		panel.add(uiPanel, BorderLayout.LINE_END);
 
 		setContentPane(panel);
 
 		pack();
 	}
 
 	public void showRawFile()
 	{
 		try
 		{
 			JDialog frame = new JDialog(this, true);
 			frame.setTitle("BOLT MapEditor - Raw File Preview");
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
 		super.setTitle("BOLT MapEditor" + ((s != null) ? " - " + s : ""));
 	}
 
 	public void newMap()
 	{
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
 		reset();
 
 		JFileChooser jfc = new JFileChooser("C:/");
 		jfc.setFileFilter(new FileNameExtensionFilter("BOLT Map-Files", "map"));
 		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
 		{
 			mapFile = jfc.getSelectedFile();
 			try
 			{
 				JSONObject data = new JSONObject(Compressor.decompressFile(mapFile));
 				entities = data.getJSONArray("entities");
 				DefaultTreeModel dtm = (DefaultTreeModel) tree.getModel();
 				for (int i = 0; i < entities.length(); i++)
 				{
 					dtm.insertNodeInto(new DefaultMutableTreeNode("Entity" + i), (DefaultMutableTreeNode) tree.getPathForRow(1).getLastPathComponent(), i);
 				}
 				tree.expandRow(1);
 				refresh();
 			}
			catch (JSONException e)
 			{
 				JOptionPane.showMessageDialog(MapEditor.this, "Could not open file: \"" + mapFile.getPath() + "\"!", "Error!", JOptionPane.ERROR_MESSAGE);
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
 
 		Compressor.compressFile(mapFile, getData().toString());
 
 	}
 
 	public void saveUMap()
 	{
 		JFileChooser jfc = new JFileChooser("C:/");
 		jfc.setFileFilter(new FileNameExtensionFilter("BOLT Map-Files", "map"));
 		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
 		if (jfc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION)
 		{
 			mapFile = new File(jfc.getSelectedFile().getPath() + ".map");
 			saveMap();
 		}
 	}
 
 	private void refresh()
 	{
 		revalidate();
 		repaint();
 	}
 
 	private String[] loadEntityList()
 	{
 		ArrayList<String> list = new ArrayList<>();
 		list.add("-- Choose an Entity --");
 
		try
 		{
			EntityLoader.findEntities("test/entities/testList.entlist");
			for (String key : EntityLoader.entitiesFound.keySet())
			{
				list.add(key);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
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
 					dtm.insertNodeInto(new DefaultMutableTreeNode("Entity" + (s.getChildCount())), s, s.getChildCount());
 					tree.expandRow(1);
 
 					try
 					{
 						EntityBuilder builder = EntityLoader.loadEntity(entities.getSelectedItem().toString().replace(".entity", ""));
 						EntityRegistry.registerEntityBuilder(builder);
 						JSONObject object = new JSONObject();
 						object.put("name", entities.getSelectedItem().toString().replace(".entity", ""));
 						object.put("pos", new JSONArray(new Double[] { 0d, 0d, 0d }));
 						object.put("rot", new JSONArray(new Double[] { 0d, 0d, 0d }));
 						object.put("custom", new JSONObject());
 						MapEditor.this.entities.put(object);
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
 
 		if (s.toString().startsWith("Entity")) // Entity1, Entity2, ...
 		{
 			try
 			{
 				final int entityIndex = tree.getRowForPath(e.getPath()) - 2;
 				JSONObject entity = entities.getJSONObject(tree.getRowForPath(e.getPath()) - 2);
 
 				EntityBuilder builder = EntityRegistry.entries.get(entity.getString("name"));
 
 				JPanel uiP = new JPanel(new SpringLayout());
 
 				uiP.add(new JLabel("Name:"));
 				JTextField name = new JTextField(builder.fullName + " (" + builder.name + ")");
 				name.setEditable(false);
 				uiP.add(name);
 
 				uiP.add(new JLabel("Parent:"));
 				JTextField parent = new JTextField(builder.parent);
 				parent.setEditable(false);
 				uiP.add(parent);
 
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
 
 				String[][] data = new String[builder.customValues.size()][2];
 				ArrayList<String> keys = new ArrayList<>(builder.customValues.keySet());
 				for (int i = 0; i < data.length; i++)
 				{
 					data[i] = new String[] { keys.get(i) + " (" + builder.customValues.get(keys.get(i)).getClass().getSimpleName() + ")", builder.customValues.get(keys.get(i)).toString() };
 				}
 				entityCustomValues = new JTable(new DefaultTableModel(data, new String[] { "Name (Type)", "Value" }));
 				entityCustomValues.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
 				JScrollPane jsp = new JScrollPane(entityCustomValues, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 				entityCustomValues.setFillsViewportHeight(true);
 				jsp.setPreferredSize(new Dimension(entityCustomValues.getWidth(), 150));
 				uiP.add(jsp);
 
 				uiP.add(new JLabel());
 				uiP.add(new JButton(new AbstractAction("Apply")
 				{
 					private static final long serialVersionUID = 1L;
 
 					@Override
 					public void actionPerformed(ActionEvent e)
 					{
 
 						try
 						{
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
 							}
 
 							if (!valid)
 							{
 								JOptionPane.showMessageDialog(MapEditor.this, "Please enter your custom values in the same data type as specified in brackets!\n  at " + message, "Error!", JOptionPane.ERROR_MESSAGE);
 								return;
 							}
 
 							entities.getJSONObject(entityIndex).put("custom", custom);
 						}
 						catch (JSONException e1)
 						{
 							e1.printStackTrace();
 						}
 					}
 				}));
 
 				SpringUtilities.makeCompactGrid(uiP, 6, 2, 6, 6, 6, 6);
 
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
