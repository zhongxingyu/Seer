 // %2863750181:de.hattrickorganizer.gui.lineup%
 package ho.module.lineup;
 
 import ho.core.db.DBManager;
 import ho.core.gui.RefreshManager;
 import ho.core.gui.comp.entry.ColorLabelEntry;
 import ho.core.gui.comp.renderer.BooleanTableCellRenderer;
 import ho.core.gui.comp.renderer.HODefaultTableCellRenderer;
 import ho.core.gui.comp.table.TableSorter;
 import ho.core.gui.comp.table.ToolTipHeader;
 import ho.core.gui.comp.table.UserColumn;
 import ho.core.gui.model.UserColumnController;
 import ho.core.gui.model.UserColumnFactory;
 import ho.core.model.HOVerwaltung;
 import ho.core.model.UserParameter;
 import ho.core.model.player.Spieler;
 import ho.core.util.Helper;
 import ho.module.playerOverview.PlayerTable;
 
 import java.awt.event.MouseAdapter;
 
 import javax.swing.JTable;
 import javax.swing.table.TableColumnModel;
 
 /**
  * TODO Missing Class Documentation
  * 
  * @author TODO Author Name
  */
 public final class AustellungSpielerTable extends JTable implements ho.core.gui.Refreshable,
 		PlayerTable {
 
 	private static final long serialVersionUID = -8295456454328467793L;
 
 	// ~ Instance fields
 	// ----------------------------------------------------------------------------
 	private LineupTableModel tableModel;
 	private TableSorter tableSorter;
 
 	// ~ Constructors
 	// -------------------------------------------------------------------------------
 	protected AustellungSpielerTable() {
 		super();
 
 		initModel();
 		setDefaultRenderer(Boolean.class, new BooleanTableCellRenderer());
 		setDefaultRenderer(Object.class, new HODefaultTableCellRenderer());
 		setSelectionBackground(HODefaultTableCellRenderer.SELECTION_BG);
 		setBackground(ColorLabelEntry.BG_STANDARD);
 		RefreshManager.instance().registerRefreshable(this);
 	}
 
 	// ~ Methods
 	// ------------------------------------------------------------------------------------
 
 	/**
 	 * TODO Missing Method Documentation
 	 * 
 	 * @param spielerid
 	 *            TODO Missing Method Parameter Documentation
 	 */
 	@Override
 	public void setSpieler(int spielerid) {
 		int index = tableSorter.getRow4Spieler(spielerid);
 		if (index >= 0) {
 			this.setRowSelectionInterval(index, index);
 		}
 	}
 
 	@Override
 	public Spieler getSpieler(int row) {
 		return this.tableSorter.getSpieler(row);
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 */
 	@Override
 	public void reInit() {
 		initModel();
 		repaint();
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 */
 	public void reInitModel() {
 		((LineupTableModel) (this.getSorter()).getModel()).reInitData();
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 */
 	@Override
 	public void refresh() {
 		reInitModel();
 		repaint(); // TODO fire TableModelEvent instead
 	}
 
 	/**
 	 * Breite der BestPosSpalte zurückgeben
 	 * 
 	 * @return TODO Missing Return Method Documentation
 	 */
 	protected int getBestPosWidth() {
 		return getColumnModel().getColumn(getColumnModel().getColumnIndex(Integer.valueOf(3)))
 				.getWidth();
 	}
 
 	/**
 	 * Gibt die Spalte für die Sortierung zurück
 	 * 
 	 * @return TODO Missing Return Method Documentation
 	 */
 	protected int getSortSpalte() {
 		switch (ho.core.model.UserParameter.instance().standardsortierung) {
 		case UserParameter.SORT_NAME:
 			return tableModel.getPositionInArray(UserColumnFactory.NAME);
 
 		case UserParameter.SORT_BESTPOS:
 			return tableModel.getPositionInArray(UserColumnFactory.BEST_POSITION);
 
 		case UserParameter.SORT_AUFGESTELLT:
 			return tableModel.getPositionInArray(UserColumnFactory.LINUP);
 
 		case UserParameter.SORT_GRUPPE:
 			return tableModel.getPositionInArray(UserColumnFactory.GROUP);
 
 		case UserParameter.SORT_BEWERTUNG:
 			return tableModel.getPositionInArray(UserColumnFactory.RATING);
 
 		default:
 			return tableModel.getPositionInArray(UserColumnFactory.BEST_POSITION);
 
 		}
 	}
 
 	/**
 	 * TODO Missing Method Documentation
 	 * 
 	 * @return TODO Missing Return Method Documentation
 	 */
 	protected TableSorter getSorter() {
 		return tableSorter;
 	}
 
 	/**
 	 * DOCUMENT ME!
 	 * 
 	 * @return int[spaltenanzahl][2] mit 0=ModelIndex und 1=ViewIndex
 	 */
 	protected int[][] getSpaltenreihenfolge() {
 		final int[][] reihenfolge = new int[tableModel.getColumnCount()][2];
 
 		for (int i = 0; i < tableModel.getColumnCount(); i++) {
 			// Modelindex
 			reihenfolge[i][0] = i;
 
 			// ViewIndex
 			reihenfolge[i][1] = convertColumnIndexToView(i);
 		}
 
 		return reihenfolge;
 	}
 
 	public final void saveColumnOrder() {
 		final UserColumn[] columns = tableModel.getDisplayedColumns();
 		final TableColumnModel tableColumnModel = getColumnModel();
 		for (int i = 0; i < columns.length; i++) {
 			columns[i].setIndex(convertColumnIndexToView(i));
 			columns[i].setPreferredWidth(tableColumnModel.getColumn(convertColumnIndexToView(i))
 					.getWidth());
 		}
 		tableModel.setCurrentValueToColumns(columns);
 		DBManager.instance().saveHOColumnModel(tableModel);
 	}
 
 	/**
 	 * Initialisiert das Model
 	 */
 	private void initModel() {
 		setOpaque(false);
 
 		if (tableModel == null) {
 			tableModel = UserColumnController.instance().getLineupModel();// ();
 
 			tableModel.setValues(HOVerwaltung.instance().getModel().getAllSpieler());
 			tableSorter = new TableSorter(tableModel,
 					tableModel.getPositionInArray(UserColumnFactory.ID), getSortSpalte());
 
 			ToolTipHeader header = new ToolTipHeader(getColumnModel());
 			header.setToolTipStrings(tableModel.getTooltips());
 			header.setToolTipText("");
 			setTableHeader(header);
 			setModel(tableSorter);
 
 			final TableColumnModel columnModel = getColumnModel();
 
 			for (int i = 0; i < tableModel.getColumnCount(); i++) {
 				columnModel.getColumn(i).setIdentifier(new Integer(i));
 			}
 
 			int[][] targetColumn = tableModel.getColumnOrder();
 			// Reihenfolge -> nach [][1] sortieren
 			targetColumn = Helper.sortintArray(targetColumn, 1);
 
 			if (targetColumn != null) {
 				for (int i = 0; i < targetColumn.length; i++) {
 					this.moveColumn(
 							getColumnModel().getColumnIndex(Integer.valueOf(targetColumn[i][0])),
 							targetColumn[i][1]);
 				}
 			}
 
 			tableSorter.addMouseListenerToHeaderInTable(this);
 			tableModel.setColumnsSize(getColumnModel());
 		} else {
 			// Werte neu setzen
 			tableModel.setValues(HOVerwaltung.instance().getModel().getAllSpieler());
 			tableSorter.reallocateIndexes();
 		}
 
 		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 		setSelectionMode(0);
 		setRowSelectionAllowed(true);
 		tableSorter.initsort();
 	}
 
 	private void addListeners() {
 		// TODO not sure what this is for. should be done by the editor??
 		addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
 				if (getSelectedRow() > -1) {
 					tableModel.setSpielberechtigung();
 				}
 			}
 		});
 	}
 
 }
