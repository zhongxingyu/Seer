 /**
  * 
  */
 package Tag;
 
 /**
  * This class represents a table in html. It is solely used to make 
  * table creation simple on our end.
  * @author Eric Majchrzak
  *
  */
 public class Table implements TagInterface {
 	
 	//Row and columns in the table
 	int rows;
 	int columns;
 	
 	//Name of the custom table
 	String name = "Custom Table";
 	
 	//the tab
 	String tab = "";
 	
 	/**
 	 * Constructor. Takes in the number of columns, number of rows, 
 	 * and the current length of the tab so that it can be inserted 
 	 * into the document.
 	 */
 	public Table(int numRows, int numCols, int tabSize){
 		rows = numRows;
 		columns = numCols;
 		
 		//make a tab String for easy use.
 		for(int i = 0; i > tabSize; i++){
 			tab = tab + " ";
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see Tag.TagInterface#print()
 	 */
 	@Override
 	public String print() {
 		
 		String temp = "<table>\n";
 		
 		for(int i=0;i<rows;i++){
 			temp = temp + printRow(columns);
 		}
 		
 		return temp + "</table>\n";
 	}
 
 	/* (non-Javadoc)
 	 * @see Tag.TagInterface#compare(java.lang.String)
 	 */
 	@Override
 	public boolean compare(String toCompare) {
 		return false;
 	}
 
 	/* (non-Javadoc)
 	 * @see Tag.TagInterface#getClosing()
 	 */
 	@Override
 	public String getClosing() {
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see Tag.TagInterface#getOpening()
 	 */
 	@Override
 	public String getOpening() {
 		// TODO Auto-generated method stub
 		return this.print();
 	}
 
 	/* (non-Javadoc)
 	 * @see Tag.TagInterface#getName()
 	 */
 	@Override
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * This is a function to print a set of html tags for a row. It will take 
 	 * in the number of columns the row should have and return a string 
 	 * representation of the row.
 	 * @return String
 	 */
 	public String printRow(int col){
 		String temp = tab + "<tr>\n";
 		
 		for(int i=0;i<col;i++){
			temp = temp + tab + tab + "<tc></tc>\n";
 		}
 		
 		return temp + tab + "</tr>\n";
 	}
 
 }
