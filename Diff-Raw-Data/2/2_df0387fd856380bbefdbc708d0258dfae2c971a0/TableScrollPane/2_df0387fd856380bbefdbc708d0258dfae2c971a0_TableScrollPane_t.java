 package net.karlmartens.ui.widget;
 
 import java.awt.Color;
 import java.awt.Point;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseListener;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.TreeSet;
 
 import javax.swing.JTable;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableModel;
 
 import net.karlmartens.platform.util.UiThreadUtil;
 
 import com.jidesoft.grid.AutoFilterTableHeader;
 import com.jidesoft.grid.CachedTableModel;
 import com.jidesoft.grid.CellStyleTable;
 import com.jidesoft.grid.ColumnStripeTableStyleProvider;
 import com.jidesoft.grid.FilterableTableModel;
 import com.jidesoft.grid.JideTable;
 import com.jidesoft.grid.MultiTableModel;
 import com.jidesoft.grid.SortableTable;
 import com.jidesoft.grid.SortableTableModel;
 import com.jidesoft.grid.TableModelWrapperUtils;
 import com.jidesoft.grid.TableSelectionListener;
 
 final class TableScrollPane extends com.jidesoft.grid.TableScrollPane {
 	private static final long serialVersionUID = 1L;
 	
 	TableScrollPane(MultiTableModel model) {
		super(new SortableTableModel(new FilterableTableModel(new CachedTableModel(model))), true);
 		UiThreadUtil.assertSwingThread();
         setAutoscrolls(true);
         setAllowMultiSelectionInDifferentTable(true);
         setCellSelectionEnabled(true);
         setColumnSelectionAllowed(true);
         setRowSelectionAllowed(true);
         setHorizontalScrollBarCoversWholeWidth(true);
         setVerticalScrollBarCoversWholeHeight(true);
         setKeepCornerVisible(false);
         setNonContiguousCellSelectionAllowed(true);
         setWheelScrollingEnabled(true);
         initHeaderTableOptions();
         initSeriesTableOptions();
 	}
 	
 	public void addTableSelectionListener(TableSelectionListener listener) {
 		((JideTable)getMainTable()).getTableSelectionModel().addTableSelectionListener(listener);
 		((JideTable)getRowHeaderTable()).getTableSelectionModel().addTableSelectionListener(listener);
 	}
 
 	public void removeTableSelectionListener(
 			TableSelectionListener listener) {
 		((JideTable)getMainTable()).getTableSelectionModel().removeTableSelectionListener(listener);
 		((JideTable)getRowHeaderTable()).getTableSelectionModel().removeTableSelectionListener(listener);
 	}
 	
 	public void addKeyListener(KeyListener listener) {
 		getMainTable().addKeyListener(listener);
 		getRowHeaderTable().addKeyListener(listener);
 	}
 	
 	public void removeKeyListener(KeyListener listener) {
 		getMainTable().removeKeyListener(listener);
 		getRowHeaderTable().removeKeyListener(listener);
 	}
 	
 	public void addMouseListener(MouseListener listener) {
 		getMainTable().addMouseListener(listener);
 		getRowHeaderTable().addMouseListener(listener);
 	}
 	
 	public void removeMouseListener(MouseListener listener) {
 		getMainTable().removeMouseListener(listener);
 		getRowHeaderTable().removeMouseListener(listener);
 	}
 	
 	public int[] getSelectionIndices() {
 		UiThreadUtil.assertSwingThread();
 		
 		// retrieve selected indices
 		final int mCount = getMainTable().getSelectedRowCount();
 		final int hCount = getRowHeaderTable().getSelectedRowCount();
 		final int[] selected = new int[mCount + hCount];
 		System.arraycopy(getMainTable().getSelectedRows(), 0, selected, 0, mCount);
 		System.arraycopy(getRowHeaderTable().getSelectedRows(), 0, selected, mCount, hCount);
 		Arrays.sort(selected);
 		
 		// Convert to model index and remove duplicates
 		final TreeSet<Integer> actualSelected = new TreeSet<Integer>();
 		for (int index : selected) {
 			actualSelected.add(TableModelWrapperUtils.getActualRowAt(getModel(), index));
 		}
 		
 		// Convert to int array
 		final Integer[] is = actualSelected.toArray(new Integer[] {});
 		final int[] result = new int[is.length];
 		for (int i=0; i<is.length; i++) {
 			result[i] = is[i].intValue();
 		}
 		return result;
 	}
 
 	public int getRowAt(int x, int y) {
 		final Point mPoint = new Point(x - getMainTable().getX(), y - getMainTable().getY());
 		int rowIndex = getMainTable().rowAtPoint(mPoint);
 		if (rowIndex < 0) {
 			final Point hPoint = new Point(x - getRowHeaderTable().getX(), y - getRowHeaderTable().getY());
 			rowIndex = getRowHeaderTable().rowAtPoint(hPoint);
 		}
 		if (rowIndex < 0)
 			return -1;
 		
 		return TableModelWrapperUtils.getActualRowAt(getModel(), rowIndex);
 	}
 
 	public void scrollToRow(int index) {
 		final int rowIndex = TableModelWrapperUtils.getRowAt(getModel(), index);
 		if (rowIndex < 0)
 			return;
 		
 		scrollToRow(rowIndex);
 	}
 
     @Override
     protected JTable createTable(TableModel model, boolean sortable) {
         final SortableTableModel sortableModel = (SortableTableModel) model;
         sortableModel.setAutoResort(false);
         
         final SortableTable table = new SortableTable(model);
         Enumeration<TableColumn> columns = table.getColumnModel().getColumns();
         while (columns.hasMoreElements())
             columns.nextElement().setMaxWidth(600);
         
         final AutoFilterTableHeader header = new AutoFilterTableHeader(table);
         header.setAutoFilterEnabled(true);
         header.setShowFilterName(false);
         header.setShowFilterNameAsToolTip(true);
         header.setShowFilterIcon(true);
         table.setTableHeader(header);
         return table;
     }
 
     private void initHeaderTableOptions() {
         final CellStyleTable headerTable = (CellStyleTable) getRowHeaderTable();
         headerTable.setTableStyleProvider(new ColumnStripeTableStyleProvider(
             new Color[] { new Color(253, 253, 244) }));
     }
     
     private void initSeriesTableOptions() {
         final JideTable mainTable = (JideTable) getMainTable();
         mainTable.getTableHeader().setReorderingAllowed(false);
         mainTable.setClickCountToStart(2);
     }
 }
