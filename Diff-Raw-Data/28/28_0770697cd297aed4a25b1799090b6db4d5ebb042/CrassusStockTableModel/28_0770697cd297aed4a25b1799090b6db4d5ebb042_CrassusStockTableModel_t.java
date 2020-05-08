 package edu.brown.cs32.atian.crassus.gui;
 
 import java.util.List;
 
 import javax.swing.table.AbstractTableModel;
 
 import edu.brown.cs32.atian.crassus.backend.Stock;
 import edu.brown.cs32.atian.crassus.backend.StockList;
 import edu.brown.cs32.atian.crassus.backend.StockListImpl;
 import edu.brown.cs32.atian.crassus.backend.StockRealTimeData;
 
 public class CrassusStockTableModel extends AbstractTableModel {
 	
 	private StockList _stocks;
 	
 	public CrassusStockTableModel(StockList stocks){
 		_stocks = stocks;
 	}
 
 	@Override
 	public int getColumnCount() {
 		return 5;
 	}
 	
 	@Override
 	public String getColumnName(int col) {
 		switch(col){
 		case 0:
 			return "ticker";
 		case 1:
 			return "price";
 		case 2:
 			return "open";
 		case 3:
 			return "high";
 		case 4:
 			return "low";
 		default:
 			return "ERR";
 		}
 	}
 
 	@Override
 	public int getRowCount() {
 		return _stocks.getStockList().size();
 	}
 	
 	private String format(double in){
 		return String.format("%1$,.2f", in);
 	}
 
 	@Override
 	public Object getValueAt(int row, int col) {
 		switch(col){
 		case 0://ticker
 			return _stocks.getStockList().get(row).getTicker();
 		case 1://price
 			//System.out.println("price of "+_stocks.getStockList().get(row).getTicker()+": "+_stocks.getStockList().get(row).getStockRealTimeData().getCurrPrice());
 			return format(_stocks.getStockList().get(row).getStockRealTimeData().getCurrPrice());
 		case 2://open
 			return format(_stocks.getStockList().get(row).getStockRealTimeData().getOpenPrice());
 		case 3://high
			return format(_stocks.getStockList().get(row).getStockRealTimeData().getTodayHigh());
 		case 4://low
			return format(_stocks.getStockList().get(row).getStockRealTimeData().getTodayLow());
 		default:
 			return "ERR";
 		}
 	}
 
 	public void addStock(Stock stock) {
 		_stocks.add(stock);
 		this.fireTableRowsInserted(_stocks.getStockList().size()-1, _stocks.getStockList().size()-1);
 	}
 
 	public void removeStock(int i) {
 		if(i!=-1){
 			_stocks.getStockList().remove(i);
 			this.fireTableRowsDeleted(i,i);
 		}
 	}
 
 	public void refresh() {
 		//System.out.println("refresh called");
 		this.fireTableRowsUpdated(0,_stocks.getStockList().size()-1);
 	}
 
 }
