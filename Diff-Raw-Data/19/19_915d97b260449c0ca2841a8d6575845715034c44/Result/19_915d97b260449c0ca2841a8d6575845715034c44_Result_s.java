 package internal.parser.resolve;
 
 import internal.tree.IWorldTree;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.ConcurrentModificationException;
 import java.util.List;
 import static test.ui.UIDebugEngine.multiLine;
 
 /**
  * Result class is used to represent the output produced by the ResolutionEngine. <br>
  * It is designed to behave like a table
  * @author guru
  *
  */
 public class Result extends ArrayList<Column> {
 	
 	@Override
 	public boolean add(Column column) {
 		for(Column t : this) {
 			if(t.name.equals(column.name)) {
 				int index = this.indexOf(t);
 				this.remove(index);
 				this.add(index, column);
 				return true;
 			}
 		}
 		return super.add(column);
 	}
 	
 	/**
 	 * Check if this result contains a {@code Column} with the specified name
 	 * @param name {@code String} specifying the name to check
 	 * @return {@code true} if there exists a {@code Column} with this name <br>
 	 * {@code false} otherwise
 	 */
 	public boolean contains(String name) {
 		for(Column t : this) {
 			if(t.name.equals(name))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Obtain a reference to a {@code Column} from its name
 	 * @param name {@code String} specifying the name of the {@code Column}
 	 * @return {@code Column} corresponding to the specified name <br>
 	 * <b> null </b> otherwise
 	 */
 	public Column get(String name) {
 		for(Column t : this) {
 			if(t.name.equals(name))
 				return t;
 		}
 		return null;
 	}
 
 	/**
 	 * Add a new row to this result <br>
 	 * If the row is already present, return immediately
 	 * @param row {@code List<IWorldTree>} containing the elements to add
 	 */
 	public void add(List<IWorldTree> row) {
 		if(contains(row))
 			return;
 		
 		int index = 0;
 		for(Column t : this) {
 			t.add(row.get(index));
 			index++;
 		}
 	}
 	
 	/**
 	 * Check if a particular row exists in this result
 	 * @param row {@code List<IWorldTree>} containing the elements to check for
 	 * @return {@code true} if this result contains the specified row <br>
 	 * {@code false} otherwise
 	 */
 	public boolean contains(List<IWorldTree> row) {
 		StringBuffer errorMsg = new StringBuffer("Trying to insert " + row.toString() + " into ");
 		
 		List<String> stringList = new ArrayList<String>();
 		for(Column c : this) {
 			stringList.add(c.toString());
 		}
 		errorMsg.append(multiLine(stringList));
 		
 		assert row.size() == size() : errorMsg.toString();
 		
 		int rowIndex = 0;
 		while(rowIndex < get(0).size()) {
 			boolean result 	= true;
 			int columnIndex	= 0;
 			while(columnIndex < size()) {
 				if(!get(columnIndex).get(rowIndex).equals(row.get(columnIndex)))
 					result = false;
 				columnIndex++;
 			}
 			if(result)
 				return true;
 			rowIndex++;
 		}
 		return false;
 	}
 	
 	/**
 	 * Obtain the index of a {@code Column} by name
 	 * @param name {@code String} containing the name of the {@code Column}
 	 * @return {@code int} representing the index of the {@code Column} if found <br>
 	 * {@code -1} otherwise
 	 */
 	public int indexOf(String name) {
 		for(int i = 0; i < size(); i++) {
 			if(get(i).name.equals(name))
 				return i;
 		}
 		return -1;
 	}
 	
 	/**
 	 * Obtain a String representation of this result
 	 */
 	@Override
 	public String toString() {
 		List<String> stringList = new ArrayList<String>();
 		for(Column t : this) {
 			stringList.add(t.toString());
 		}
 		return multiLine(stringList) + "\n";
 	}
 
 	/**
	 * This method is used to remove a row corresponding to the given {@code Column} and an element in that column <br>
	 * The element specified is not removed from this {@code Result} and must be done manually to avoid
	 * {@code ConcurrentModificationException}
	 * @param column {@code Column} used to specify the element index
	 * @param node {@code IWorldTree} object in the given {@code Column}
 	 */
	public void removeRow(Column c, IWorldTree node) {
		int index = c.indexOf(node);
 		for(Column column : this) {
			if(column.equals(c))
				continue;
			c.remove(index);
 		}
 	}

 	/**
 	 * Obtain a row from this {@code Result} object
 	 * @param rowIndex {@code int} specifying the row index to fetch
 	 * @return {@code List<IWorldTree>} containing the elements of this row
 	 */
 	public List<IWorldTree> getRow(int rowIndex) {
 		List<IWorldTree> result = new ArrayList<IWorldTree>();
 		for(Column c : this) {
 			if(rowIndex < c.size())
 			result.add(c.get(rowIndex));
 		}
 		if(result.size() > 0)
 			return result;
 		else
 			return null;
 	}
 
 	/**
 	 * Create a new {@code Result} object that has the same columns as the <b> result </b> parameter <br>
 	 * <b> No column values are copied! </b>
 	 * @param result {@code Result} object to clone
 	 * @return {@code Result} object containing the same fields as <b> result </b>
 	 */
 	public static Result newCopy(Result result) {
 		Result newResult = new Result();
 		for(Column c : result)
 			newResult.add(new Column(c.name));
 		return newResult;
 	}
 }
