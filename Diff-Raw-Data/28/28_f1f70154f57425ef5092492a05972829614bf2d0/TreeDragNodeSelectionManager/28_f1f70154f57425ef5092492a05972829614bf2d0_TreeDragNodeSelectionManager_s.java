 package editor.window;
 
 import de.matthiasmann.twl.Event;
 import de.matthiasmann.twl.TableBase;
 import de.matthiasmann.twl.TableSelectionManager;
 import de.matthiasmann.twl.model.TableSelectionModel;
 
 public class TreeDragNodeSelectionManager implements TableSelectionManager {

 	@Override
 	public TableSelectionModel getSelectionModel() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void setAssociatedTable(TableBase base) {
		// TODO Auto-generated method stub

 	}
 
 	@Override
 	public SelectionGranularity getSelectionGranularity() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean handleKeyStrokeAction(String action, Event event) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean handleMouseEvent(int row, int column, Event event) {
		if( row == -1 && !(column == -1))
			System.out.println(row + ":" + column + " " + event.getMouseX() + ":" + event.getMouseY());
		
		return true;
 	}
 
 	@Override
 	public boolean isRowSelected(int row) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean isCellSelected(int row, int column) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public int getLeadRow() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int getLeadColumn() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public void modelChanged() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void rowsInserted(int index, int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void rowsDeleted(int index, int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void columnInserted(int index, int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void columnsDeleted(int index, int count) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
