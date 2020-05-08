 package gui;
 
 import gui.ClickMouseOverTable.ClickMouseOverRenderer;
 import gui.View.AnimationSpeed;
 import gui.swing.ComponentFactory;
 import gui.util.CompoundPropertyHighlighter;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.RowSorter;
 import javax.swing.SortOrder;
 import javax.swing.UIManager;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableRowSorter;
 
 import main.Settings;
 import util.ListUtil;
 import util.SelectionModel;
 import util.ThreadUtil;
 import cluster.Clustering;
 import cluster.Compound;
 import dataInterface.CompoundProperty;
import dataInterface.CompoundProperty.Type;
 
 public class CompoundTable extends BlockableFrame
 {
 	boolean selfUpdate = false;
 	private Clustering clustering;
 	private ClickMouseOverTable table;
 	private TableRowSorter<DefaultTableModel> sorter;
 	private List<CompoundProperty> props;
 	private ViewControler viewControler;
 
 	private static final int NON_PROP_COLUMNS = 3;
 
 	@SuppressWarnings("unchecked")
 	public CompoundTable(final ViewControler viewControler, final Clustering clustering)
 	{
 		super();
 
 		((BlockableFrame) Settings.TOP_LEVEL_FRAME).addPropertyChangeListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				if (!CompoundTable.this.isVisible())
 					return;
 				if (BlockableFrame.BLOCKED.equals(evt.getPropertyName()))
 				{
 					CompoundTable.this.block(evt.getNewValue().toString());
 				}
 				else if (BlockableFrame.UN_BLOCKED.equals(evt.getPropertyName()))
 				{
 					CompoundTable.this.unblock(evt.getNewValue().toString());
 				}
 			}
 		});
 		Settings.TOP_LEVEL_FRAME.addWindowListener(new WindowAdapter()
 		{
 			@Override
 			public void windowClosing(WindowEvent e)
 			{
 				if (!CompoundTable.this.isVisible())
 					return;
 				CompoundTable.this.setVisible(false);
 			}
 		});
 		clustering.addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				if (!CompoundTable.this.isVisible())
 					return;
 				CompoundTable.this.setVisible(false);
 			}
 		});
 
 		this.clustering = clustering;
 		this.viewControler = viewControler;
 
 		props = ListUtil.concat(clustering.getProperties(), clustering.getFeatures());
 
 		final DefaultTableModel model = new DefaultTableModel()
 		{
 			@SuppressWarnings({ "rawtypes" })
 			@Override
 			public Class getColumnClass(int columnIndex)
 			{
 				if (columnIndex == 0)
 					return Integer.class;
 				if (columnIndex == 1)
 					return Compound.class;
 				if (columnIndex >= NON_PROP_COLUMNS
						&& props.get(columnIndex - NON_PROP_COLUMNS).getType() == Type.NUMERIC)
 					return Double.class;
 				return String.class;
 			}
 
 			@Override
 			public boolean isCellEditable(int row, int column)
 			{
 				return false;
 			}
 		};
 		table = new ClickMouseOverTable(model)
 		{
 			public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
 			{
 				Component c = super.prepareRenderer(renderer, row, column);
 				if (c instanceof JComponent)
 				{
 					JComponent jc = (JComponent) c;
 					jc.setToolTipText(getValueAt(row, 1).toString());
 				}
 				return c;
 			}
 		};
 		sorter = new TableRowSorter<DefaultTableModel>();
 		table.setRowSorter(sorter);
 		sorter.setModel(model);
 		model.addColumn("");
 		model.addColumn("Compound");
 		model.addColumn("Smiles");
 
 		for (CompoundProperty p : props)
 			model.addColumn(p);
 		table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 		int count = 0;
 		for (Compound m : clustering.getCompounds(false))
 		{
 			Object o[] = new Object[model.getColumnCount()];
 			int i = 0;
 			o[i++] = ++count;
 			o[i++] = m;
 			o[i++] = m.getSmiles();
 			for (CompoundProperty p : props)
 			{
				if (p.getType() == Type.NUMERIC)
 					o[i++] = m.getDoubleValue(p);
 				else
 					o[i++] = m.getStringValue(p);
 			}
 			model.addRow(o);
 		}
 
 		clustering.getCompoundActive().addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				if (!CompoundTable.this.isVisible())
 					return;
 				updateTableFromCompound(clustering.getCompoundActive(), null, table.getClickSelectionModel());
 			}
 		});
 		clustering.getCompoundWatched().addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				if (!CompoundTable.this.isVisible())
 					return;
 				updateTableFromCompound(clustering.getCompoundWatched(), table.getSelectionModel(), null);
 			}
 		});
 
 		viewControler.addViewListener(new PropertyChangeListener()
 		{
 
 			@Override
 			public void propertyChange(PropertyChangeEvent evt)
 			{
 				if (!CompoundTable.this.isVisible())
 					return;
 				if (evt.getPropertyName().equals(ViewControler.PROPERTY_HIGHLIGHT_CHANGED))
 					table.repaint();
 			}
 		});
 
 		//		viewControler.addViewListener(new PropertyChangeListener()
 		//		{
 		//
 		//			@Override
 		//			public void propertyChange(PropertyChangeEvent evt)
 		//			{
 		//				if (selfUpdate)
 		//					return;
 		//				if (!CompoundTable.this.isVisible())
 		//					return;
 		//				if (evt.getPropertyName().equals(ViewControler.PROPERTY_HIGHLIGHT_CHANGED))
 		//					updateFeatureSelection();
 		//			}
 		//		});
 
 		//		sorter.addRowSorterListener(new RowSorterListener()
 		//		{
 		//
 		//			@Override
 		//			public void sorterChanged(RowSorterEvent e)
 		//			{
 		//				if (selfUpdate)
 		//					return;
 		//				selfUpdate = true;
 		//				List<? extends SortKey> keys = sorter.getSortKeys();
 		//				if (keys.size() > 0 && keys.get(0).getColumn() > 2)
 		//				{
 		//					CompoundProperty p = props.get(keys.get(0).getColumn() - 2);
 		//					viewControler.setHighlighter(p);
 		//				}
 		//				selfUpdate = false;
 		//			}
 		//		});
 
 		table.getClickSelectionModel().addListener(new PropertyChangeListener()
 		{
 			@Override
 			public void propertyChange(PropertyChangeEvent e)
 			{
 				updateCompoundFromTable(table.getClickSelectionModel().getSelectedIndices(),
 						clustering.getCompoundActive());
 			}
 		});
 
 		table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
 		{
 			@Override
 			public void valueChanged(ListSelectionEvent e)
 			{
 				if (e.getValueIsAdjusting())
 					return;
 				updateCompoundFromTable(table.getSelectedRows(), clustering.getCompoundWatched());
 			}
 		});
 		ClickMouseOverRenderer renderer = new ClickMouseOverTable.ClickMouseOverRenderer(table)
 		{
 			@Override
 			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
 					boolean hasFocus, int row, int column)
 			{
 				Object val;
 				if (column > NON_PROP_COLUMNS)
 					val = ((Compound) table.getValueAt(row, 1)).getFormattedValue(props.get(column - NON_PROP_COLUMNS));
 				else
 					val = value;
 				return super.getTableCellRendererComponent(table, val, isSelected, hasFocus, row, column);
 			}
 		};
 		renderer.clickSelectedBackground = ComponentFactory.LIST_ACTIVE_BACKGROUND_WHITE;
 		renderer.mouseOverSelectedBackground = ComponentFactory.LIST_WATCH_BACKGROUND_WHITE;
 
 		//		renderer.clickSelectedBackground = ComponentFactory.LIST_ACTIVE_BACKGROUND;
 		//		renderer.mouseOverSelectedBackground = ComponentFactory.LIST_WATCH_BACKGROUND;
 		//		renderer.background = ComponentFactory.BACKGROUND;
 		//		renderer.clickSelectedForeground = ComponentFactory.LIST_SELECTION_FOREGROUND;
 		//		renderer.mouseOverSelectedForeground = ComponentFactory.LIST_SELECTION_FOREGROUND;
 		//		renderer.foreground = ComponentFactory.FOREGROUND;
 		table.setDefaultRenderer(Object.class, renderer);
 		table.setDefaultRenderer(Integer.class, renderer);
 		table.setDefaultRenderer(Double.class, renderer);
 		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 		table.setGridColor(ComponentFactory.BACKGROUND);
 		int width = 0;
 		for (int i = 0; i < table.getColumnCount(); i++)
 			width += Math.min(300, ComponentFactory.packColumn(table, i, 5, 150));
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
 		setTitle(Settings.text("compound-table.title"));
 		JPanel p = new JPanel(new BorderLayout(10, 10));
 		p.add(new JLabel("<html>" + Settings.text("compound-table.info") + "</html>"), BorderLayout.NORTH);
 		p.add(scroll);
 		p.setBorder(new EmptyBorder(10, 10, 10, 10));
 		getContentPane().add(p);
 		pack();
 		pack();
 		int scrollBarSize = ((Integer) UIManager.get("ScrollBar.width")).intValue();
 		setSize(new Dimension(Math.min(Settings.TOP_LEVEL_FRAME.getWidth(), width + 20 + scrollBarSize + 2),
 				getHeight()));
 		setLocationRelativeTo(Settings.TOP_LEVEL_FRAME);
 
 		updateTableFromCompound(clustering.getCompoundWatched(), table.getSelectionModel(), null);
 		updateTableFromCompound(clustering.getCompoundActive(), null, table.getClickSelectionModel());
 		updateFeatureSelection();
 
 		setVisible(true);
 	}
 
 	private void updateCompoundFromTable(int tableSelection[], SelectionModel modelSelection)
 	{
 		if (selfUpdate)
 			return;
 		selfUpdate = true;
 		final int row[] = tableSelection;
 		if (row == null || row.length == 0)
 			modelSelection.clearSelection();
 		else if (row.length == 1)
 			modelSelection.setSelected(sorter.convertRowIndexToModel(row[0]));
 		else
 		{
 			View.instance.suspendAnimation("manual zooming out");
 			CompoundTable.this.block("waiting for anim");
 			for (int i = 0; i < row.length; i++)
 				row[i] = sorter.convertRowIndexToModel(row[i]);
 			modelSelection.setSelectedIndices(row);
 			Thread th = new Thread(new Runnable()
 			{
 				public void run()
 				{
 					ThreadUtil.sleep(300);
 					View.instance.afterAnimation(new Runnable()
 					{
 						@Override
 						public void run()
 						{
 							View.instance.proceedAnimation("manual zooming out");
 							if (View.instance.getZoomTarget() != clustering)
 								View.instance.zoomTo(clustering, AnimationSpeed.SLOW, false);
 							View.instance.afterAnimation(new Runnable()
 							{
 
 								@Override
 								public void run()
 								{
 									CompoundTable.this.unblock("waiting for anim");
 								}
 							}, "wait for anim");
 						}
 					}, "manual zooming out");
 				}
 			});
 			th.start();
 		}
 		selfUpdate = false;
 	}
 
 	int sortColumn = 1;
 
 	private void updateFeatureSelection()
 	{
 		if (selfUpdate)
 			return;
 		selfUpdate = true;
 
 		if (viewControler.getHighlighter() instanceof CompoundPropertyHighlighter)
 		{
 			CompoundProperty prop = ((CompoundPropertyHighlighter) viewControler.getHighlighter()).getProperty();
 			int idx = props.indexOf(prop);
 			if (idx != -1)
 				sortColumn = idx + NON_PROP_COLUMNS;
 		}
 		List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
 		sortKeys.add(new RowSorter.SortKey(sortColumn, SortOrder.ASCENDING));
 		sorter.setSortKeys(sortKeys);
 
 		selfUpdate = false;
 	}
 
 	private void updateTableFromCompound(SelectionModel compoundSelection, ListSelectionModel tableSelectionA,
 			SelectionModel tableSelectionB)
 	{
 		if (selfUpdate)
 			return;
 		selfUpdate = true;
 
 		if (tableSelectionA != null)
 			tableSelectionA.clearSelection();
 		else
 			tableSelectionB.clearSelection();
 		int sel[] = compoundSelection.getSelectedIndices();
 
 		if (sel != null && sel.length > 0)
 			for (int i = 0; i < sel.length; i++)
 			{
 				int idx = sorter.convertRowIndexToView(sel[i]);
 				if (tableSelectionA != null)
 					tableSelectionA.addSelectionInterval(idx, idx);
 				else
 					tableSelectionB.setSelected(idx, false);
 				if (i == sel.length - 1)
 					table.scrollRectToVisible(new Rectangle(table.getCellRect(idx, 0, true)));
 			}
 		selfUpdate = false;
 	}
 }
