 package ee.ut.math.tvt.salessystem.ui.model;
 
 import ee.ut.math.tvt.salessystem.domain.data.SoldItem;
 
 public class HistoryTableModel extends SalesSystemTableModel<SoldItem>{
 
 	private static final long serialVersionUID = 1L;
	
 	public HistoryTableModel() {
 		super(new String[] { "Date", "Time", "Total Price"});
 	}
 
 	@Override
 	protected Object getColumnValue(SoldItem item, int columnIndex) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
