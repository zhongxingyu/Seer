 package emcshop.gui;
 
 import static emcshop.util.NumberFormatter.formatRupeesWithColor;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.table.AbstractTableModel;
 import javax.swing.table.TableCellRenderer;
 
 import net.miginfocom.swing.MigLayout;
 import emcshop.db.BonusFee;
 import emcshop.db.DbDao;
 
 @SuppressWarnings("serial")
 public class BonusFeeTab extends JPanel {
 	private final DbDao dao;
 	private final DateFormat df = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT);
 
 	private final JLabel since;
 	private final BonusFeeTable table;
 	private final MyJScrollPane tableScrollPane;
 
 	/**
 	 * Defines all of the columns in this table. The order in which the enums
 	 * are defined is the order that they will appear in the table.
 	 */
 	private enum Column {
		DESCRIPTION("Item Name"), TOTAL("Total");
 
 		private final String name;
 
 		private Column(String name) {
 			this.name = name;
 		}
 
 		public String getName() {
 			return name;
 		}
 	}
 
 	public BonusFeeTab(DbDao dao) {
 		this.dao = dao;
 
 		table = new BonusFeeTable();
 		since = new JLabel();
 
 		///////////////////////
 
 		setLayout(new MigLayout("fillx, insets 5"));
 
 		add(new JLabel("Data collection start date:"), "split 2, align center");
 		add(since, "wrap");
 
 		tableScrollPane = new MyJScrollPane(table);
 		add(tableScrollPane, "align center");
 
 		refresh();
 	}
 
 	public void refresh() {
 		BonusFee bonusFee;
 		try {
 			bonusFee = dao.getBonusesFees();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 
 		Date sinceDate = bonusFee.getSince();
 		since.setText("<html><b><i><font color=navy>" + ((sinceDate == null) ? "never" : df.format(sinceDate)) + "</font></i></b></html>");
 
 		table.refresh(bonusFee);
 	}
 
 	private class Row {
 		private final String description;
 		private final int total;
 
 		public Row(String description, int total) {
 			this.description = description;
 			this.total = total;
 		}
 	}
 
 	private class BonusFeeTable extends JTable {
 		private Column prevColumnClicked;
 		private boolean ascending;
 		private List<Row> rows = new ArrayList<Row>();
 
 		public BonusFeeTable() {
 			prevColumnClicked = Column.DESCRIPTION;
 			ascending = true;
 
 			getTableHeader().setReorderingAllowed(false);
 			setColumnSelectionAllowed(false);
 			setRowSelectionAllowed(false);
 			setCellSelectionEnabled(false);
 			setRowHeight(24);
 
 			//allow columns to be sorted by clicking on the headers
 			getTableHeader().addMouseListener(new MouseAdapter() {
 				private final Column columns[] = Column.values();
 
 				@Override
 				public void mouseClicked(MouseEvent e) {
 					int index = convertColumnIndexToModel(columnAtPoint(e.getPoint()));
 					if (index < 0) {
 						return;
 					}
 
 					Column column = columns[index];
 
 					if (column == prevColumnClicked) {
 						ascending = !ascending;
 					} else {
 						prevColumnClicked = column;
 						ascending = true;
 					}
 
 					sortData();
 					redraw();
 				}
 			});
 
 			setDefaultRenderer(Row.class, new TableCellRenderer() {
 				private final Color evenRowColor = new Color(255, 255, 255);
 				private final Color oddRowColor = new Color(240, 240, 240);
 
 				@Override
 				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
 					final Row rowObj = (Row) value;
 
 					JLabel label = null;
 					if (col == Column.DESCRIPTION.ordinal()) {
 						label = new JLabel(rowObj.description);
 					} else if (col == Column.TOTAL.ordinal()) {
 						label = new JLabel("<html>" + formatRupeesWithColor(rowObj.total) + "</html>");
 					}
 
 					//set the background color of the row
 					Color color = (row % 2 == 0) ? evenRowColor : oddRowColor;
 					label.setOpaque(true);
 					label.setBackground(color);
 
 					return label;
 				}
 			});
 
 			setModel();
 
 		}
 
 		public void refresh(BonusFee bonusFee) {
 			setData(bonusFee);
 			redraw();
 		}
 
 		private void setData(BonusFee bonusFee) {
 			rows.clear();
 			rows.add(new Row("Horse Summoning", bonusFee.getHorse()));
 			rows.add(new Row("Chest Locking", bonusFee.getLock()));
 			rows.add(new Row("Eggifying animals", bonusFee.getEggify()));
 			rows.add(new Row("Vault fees", bonusFee.getVault()));
 			rows.add(new Row("Sign-in bonuses", bonusFee.getSignIn()));
 			rows.add(new Row("Voting bonuses", bonusFee.getVote()));
 
 			sortData(); //sort according to the current sorting rules
 		}
 
 		private void sortData() {
 			Collections.sort(rows, new Comparator<Row>() {
 				@Override
 				public int compare(Row a, Row b) {
 					if (!ascending) {
 						Row temp = a;
 						a = b;
 						b = temp;
 					}
 
 					switch (prevColumnClicked) {
 					case DESCRIPTION:
 						return a.description.compareToIgnoreCase(b.description);
 					case TOTAL:
 						return a.total - b.total;
 					default:
 						return 0;
 					}
 				}
 			});
 		}
 
 		private void setModel() {
 			setModel(new AbstractTableModel() {
 				private final Column columns[] = Column.values();
 
 				@Override
 				public int getColumnCount() {
 					return columns.length;
 				}
 
 				@Override
 				public String getColumnName(int index) {
 					Column column = columns[index];
 
 					String text = column.getName();
 					if (prevColumnClicked == column) {
 						String arrow = (ascending) ? "\u25bc" : "\u25b2";
 						text = arrow + " " + text;
 					}
 					return text;
 				}
 
 				@Override
 				public int getRowCount() {
 					return rows.size();
 				}
 
 				@Override
 				public Object getValueAt(int row, int col) {
 					return rows.get(row);
 				}
 
 				public Class<?> getColumnClass(int col) {
 					return Row.class;
 				}
 
 				@Override
 				public boolean isCellEditable(int row, int col) {
 					return false;
 				}
 			});
 		}
 
 		private void redraw() {
 			AbstractTableModel model = (AbstractTableModel) getModel();
 			model.fireTableStructureChanged();
 		}
 	}
 }
