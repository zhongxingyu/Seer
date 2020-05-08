 package gui;
 
 import gui.swing.ComponentFactory;
 import gui.util.CompoundPropertyHighlighter;
 import gui.util.Highlighter;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableRowSorter;
 
 import main.Settings;
 import util.CountedSet;
 import util.DoubleArraySummary;
 import util.StringUtil;
 import cluster.Clustering;
 import dataInterface.CompoundProperty;
 
 public class FeatureTable extends BlockableFrame
 {
 	List<Highlighter> highlighters = new ArrayList<Highlighter>();
 	private ViewControler viewControler;
 	private JTable table;
 	private boolean selfUpdate;
 	private TableRowSorter<DefaultTableModel> sorter;
 
 	@SuppressWarnings("unchecked")
 	public FeatureTable(final ViewControler viewControler, Clustering clustering)
 	{
 		super();
 		this.viewControler = viewControler;
 
 		((BlockableFrame) Settings.TOP_LEVEL_FRAME).addPropertyChangeListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				if (!FeatureTable.this.isVisible())
 					return;
 				if (BlockableFrame.BLOCKED.equals(evt.getPropertyName()))
 				{
 					FeatureTable.this.block(evt.getNewValue().toString());
 				}
 				else if (BlockableFrame.UN_BLOCKED.equals(evt.getPropertyName()))
 				{
 					FeatureTable.this.unblock(evt.getNewValue().toString());
 				}
 			}
 		});
 		Settings.TOP_LEVEL_FRAME.addWindowListener(new WindowAdapter()
 		{
 			@Override
 			public void windowClosing(WindowEvent e)
 			{
 				if (!FeatureTable.this.isVisible())
 					return;
 				FeatureTable.this.setVisible(false);
 			}
 		});
 		clustering.addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				if (!FeatureTable.this.isVisible())
 					return;
 				FeatureTable.this.setVisible(false);
 			}
 		});
 
 		final DefaultTableModel model = new DefaultTableModel()
 		{
 			@SuppressWarnings("rawtypes")
 			@Override
 			public Class getColumnClass(int columnIndex)
 			{
 				if (columnIndex == 0)
 					return Integer.class;
 				if (columnIndex == 1)
 					return Highlighter.class;
 				if (columnIndex == 4 || columnIndex == 5)
 					return Integer.class;
 				if (columnIndex >= 6 || columnIndex <= 8)
 					return Double.class;
 				//				if (columnIndex >= 5 || columnIndex <= 8)
 				//					return CompoundPropertyEmbedQuality.class;
 				return String.class;
 			}
 
 			@Override
 			public boolean isCellEditable(int row, int column)
 			{
 				return false;
 			}
 		};
 		table = new JTable(model);
 		sorter = new TableRowSorter<DefaultTableModel>();
 		table.setRowSorter(sorter);
 		sorter.setModel(model);
 		model.addColumn("");
 		model.addColumn("Feature");
 		model.addColumn("Used for Embedding");
 		model.addColumn("Type");
 		model.addColumn("#Missing");
 		model.addColumn("#Distinct");
 		model.addColumn("Min");
 		model.addColumn("Median");
 		model.addColumn("Max");
 		model.addColumn("Values");
 		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
 		{
 			@Override
 			public void valueChanged(ListSelectionEvent e)
 			{
 				if (selfUpdate)
 					return;
 
 				int row = table.getSelectedRow();
 				if (row != -1)
 				{
 					row = sorter.convertRowIndexToModel(row);
 					Highlighter p = (Highlighter) model.getValueAt(row, 1);
 					viewControler.setHighlighter(p);
 					SwingUtilities.invokeLater(new Runnable()
 					{
 						@Override
 						public void run()
 						{
 							table.requestFocus();
 						}
 					});
 				}
 			}
 		});
 		table.setDefaultRenderer(Double.class, new DefaultTableCellRenderer()
 		{
 			@Override
 			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
 					boolean hasFocus, int row, int column)
 			{
 				return super.getTableCellRendererComponent(table,
 						value instanceof Double ? StringUtil.formatDouble((Double) value) : value, isSelected,
 						hasFocus, row, column);
 			}
 		});
 		viewControler.addViewListener(new PropertyChangeListener()
 		{
 
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				if (selfUpdate)
 					return;
 				if (!FeatureTable.this.isVisible())
 					return;
 				if (evt.getPropertyName().equals(ViewControler.PROPERTY_HIGHLIGHT_CHANGED))
 					updateFeatureSelection();
 			}
 		});
 		//		for (Dimensions d : Dimensions.values())
 		//			model.addColumn(d + " Emb-Qual");
 		for (Highlighter[] hh : viewControler.getHighlighters().values())
 			for (Highlighter h : hh)
 				highlighters.add(h);
 
 		//		final DoubleKeyHashMap<Dimensions, CompoundProperty, CompoundPropertyEmbedQuality> embMap = new DoubleKeyHashMap<EmbedUtil.Dimensions, CompoundProperty, EmbedUtil.CompoundPropertyEmbedQuality>();
 		int count = 0;
 		for (Highlighter h : highlighters)
 		{
 			Object o[] = new Object[10];// + Dimensions.values().length];
 			int i = 0;
 			o[i++] = ++count;
 			o[i++] = h;
 			if (h instanceof CompoundPropertyHighlighter)
 			{
 				CompoundProperty p = ((CompoundPropertyHighlighter) h).getProperty();
 				o[i++] = (clustering.getFeatures().contains(p) ? "Yes" : "no");
				o[i++] = p.getType() == CompoundProperty.Type.NUMERIC ? "Numeric"
						: (p.getType() == CompoundProperty.Type.NOMINAL ? "Nominal" : "undef.");
 				o[i++] = clustering.numMissingValues(p);
 				o[i++] = clustering.numDistinctValues(p);
				if (p.getType() == CompoundProperty.Type.NUMERIC)
 				{
 					DoubleArraySummary s = DoubleArraySummary.create(clustering.getDoubleValues(p));
 					o[i++] = s.getMin();
 					o[i++] = s.getMedian();
 					o[i++] = s.getMax();
 					o[i++] = null;
 				}
 				else
 				{
 					o[i++] = null;
 					o[i++] = null;
 					o[i++] = null;
 					CountedSet<String> set = CountedSet.fromArray(clustering.getStringValues(p, null));
 					o[i++] = set.toString();
 				}
 				//			for (Dimensions d : Dimensions.values())
 				//			{
 				//				embMap.put(d, p, embedQuality.get(p).clone());
 				//				o[i++] = embMap.get(d, p);
 				//			}
 			}
 			else
 			{
 				for (; i < o.length; i++)
 					o[i] = null;
 			}
 			model.addRow(o);
 		}
 
 		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 		int width = 0;
 		for (int i = 0; i < table.getColumnCount(); i++)
 			width += Math.min(300, ComponentFactory.packColumn(table, i, 5));
 		JScrollPane scroll = new JScrollPane(table);
 		//		Thread th = new Thread(new Runnable()
 		//		{
 		//			@Override
 		//			public void run()
 		//			{
 		//				while (!table.isShowing())
 		//					ThreadUtil.sleep(100);
 		//				for (CompoundProperty p : props)
 		//				{
 		//					if (!table.isShowing())
 		//						break;
 		//					for (Dimensions d : Dimensions.values())
 		//						embMap.get(d, p).compute(d);
 		//					SwingUtilities.invokeLater(new Runnable()
 		//					{
 		//						@Override
 		//						public void run()
 		//						{
 		//							table.repaint();
 		//						}
 		//					});
 		//				}
 		//			}
 		//		});
 		//		th.start();
 		setTitle(Settings.text("feature-table.title"));
 		JPanel p = new JPanel(new BorderLayout(10, 10));
 		p.add(new JLabel("<html>" + Settings.text("feature-table.info") + "</html>"), BorderLayout.NORTH);
 		p.add(scroll);
 		p.setBorder(new EmptyBorder(10, 10, 10, 10));
 		getContentPane().add(p);
 		pack();
 		pack();
 		int scrollBarSize = ((Integer) UIManager.get("ScrollBar.width")).intValue();
 		setSize(new Dimension(Math.min(Settings.TOP_LEVEL_FRAME.getWidth(), width + 20 + scrollBarSize + 2),
 				getHeight()));
 		setLocationRelativeTo(Settings.TOP_LEVEL_FRAME);
 
 		updateFeatureSelection();
 
 		setVisible(true);
 		SwingUtilities.invokeLater(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				table.requestFocus();
 			}
 		});
 	}
 
 	private void updateFeatureSelection()
 	{
 		selfUpdate = true;
 		int index = sorter.convertRowIndexToView(highlighters.indexOf(viewControler.getHighlighter()));
 		table.setRowSelectionInterval(index, index);
 		selfUpdate = false;
 	}
 }
