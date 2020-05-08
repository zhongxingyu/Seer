 /**
  * 
  */
 package ui;
 
 import java.sql.Date;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 
 import model.TradeRecord;
 import util.Helper;
 
 /**
  * @author tuya
  * 
  */
 public class TradeRecordTable extends DefaultTableModel {
 	private final List<TradeRecord> myRecords = new ArrayList<TradeRecord>();
 	private final int COL_TRADE_TIME = 0;
 	private final int COL_TRADE_AMOUNT = 1;
 	private final int COL_TRADE_PRISE = 2;
 	private final int COL_TRADE_TYPE = 3;
 	private final int COL_TRADE_MONEY = 4;
 	private final int COL_MAX = 5;
 	private final TableColumn[] myColumns = new TableColumn[COL_MAX];
 	private JTable myTable;
 	private final boolean myColumnEditable[] = {
 			// COL_TRADE_TIME
 			false,
 			// COL_TRADE_AMOUNT
 			true,
 			// COL_TRADE_PRISE
 			true,
 			// COL_TRADE_TYPE
 			false,
 			// COL_TRADE_MONEY
 			false };
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3117841222887703411L;
 
 	public TradeRecordTable(JTable table) {
 		setTable(table);
 		table.setModel(this);
 		refresh();
 	}
 
 	public TradeRecordTable(Object[][] objects, String[] headers) {
 		super(objects, headers);
 	}
 
 	private void updateColumns() {
 		for (int col = 0; col < COL_MAX; col++) {
 			if (myColumns[col] == null) {
 				myColumns[col] = new TableColumn();
 				myColumns[col].setModelIndex(col);
 				myTable.getColumnModel().addColumn(myColumns[col]);
 			}
 		}
 		TableColumn column = null;
 		column = myColumns[COL_TRADE_TIME];
 		column.setPreferredWidth(200);
 		column.setResizable(true);
 	}
 
 	/**
 	 * @return the myTable
 	 */
 	public JTable getTable() {
 		return myTable;
 	}
 
 	/**
 	 * @param myTable
 	 *            the myTable to set
 	 */
 	public void setTable(JTable myTable) {
 		this.myTable = myTable;
 	}
 
 	@Override
 	public boolean isCellEditable(int row, int column) {
 		return myColumnEditable[column];
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public void setValueAt(Object val, int row, int col) {
 		TradeRecord tr = null;
 		if (myRecords.size() <= row) {
 			tr = new TradeRecord(System.currentTimeMillis(), 0, 0);
 			addTrade(tr);
 		} else {
 			tr = myRecords.get(row);
 		}
 		String valueStr = String.valueOf(val);
 		switch (col) {
 		case COL_TRADE_TIME: {
 			tr.setTime(Date.parse(valueStr));
 			break;
 		}
 		case COL_TRADE_PRISE:
 			tr.setPrise(Helper.getCurrencyValue(valueStr));
 			break;
 		case COL_TRADE_AMOUNT:
 			tr.setAmount(parseIntegerInput(valueStr));
 			break;
 		case COL_TRADE_TYPE:
 			break;
 		case COL_TRADE_MONEY:
 			break;
 		default:
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public Object getValueAt(int row, int col) {
 		if (myRecords.size() <= row) {
 			return "";
 		}
 
 		TradeRecord tr = myRecords.get(row);
 
 		switch (col) {
 		case COL_TRADE_TIME: {
 			Date date = new Date(tr.getTime());
 			return date.toLocaleString();
 		}
 		case COL_TRADE_PRISE:
 			return Helper.getCurrencyString(Math.abs(tr.getPrise()));
 		case COL_TRADE_AMOUNT:
 			return tr.getAmount();
 		case COL_TRADE_TYPE: {
 			switch (tr.getType()) {
 			case BUY:
 				return "买入";
 			case SELL:
 				return "卖出";
 			case NOT_A_TRADE:
 				return "无效交易";
 			}
 		}
            break;
        case COL_TRADE_MONEY:
 			return Helper.getCurrencyString(tr.getMoney());
 		default:
 		}
 		return "";
 	}
 
 	public void setDatas(List<TradeRecord> rList) {
 		myRecords.clear();
 		myRecords.addAll(rList);
 		refresh();
 	}
 
 	public void addTrade(TradeRecord tr) {
 		myRecords.add(tr);
 		refresh();
 	}
 
 	public void addTrade(long time, int amount, double prise) {
 		addTrade(new TradeRecord(time, amount, prise));
 	}
 
 	protected void refresh() {
 		updateColumns();
 		this.setRowCount(myRecords.size() + 1);
 	}
 
 	protected int parseIntegerInput(String str) {
 		if (str == null || str.isEmpty()) {
 			return 0;
 		}
 		return Integer.valueOf(str);
 	}
 }
