 package ch.ethz.inf.dbproject.database.simpledatabase.operators;
 
 import java.io.InputStream;
 import java.util.Scanner;
 import java.util.logging.Logger;
 
 import ch.ethz.inf.dbproject.database.simpledatabase.Tuple;
 import ch.ethz.inf.dbproject.database.simpledatabase.TupleSchema;
 import ch.ethz.inf.utils.FacesContextUtils;
 import ch.ethz.inf.utils.StringUtils;
 
 /**
  * The scan operator reads tuples from a file. The lines in the file contain the
  * values of a tuple. The line a comma separated.
  */
 public class Scan extends Operator {
 
 	private static final Logger logger = Logger.getLogger(Scan.class.getName());
 
 	private TupleSchema schema;
 	private Scanner scanner;
 	
 	
 	/**
 	 * Contructs a new scan operator.
 	 * 
 	 * @param tableName
 	 *            file to read tuples from
 	 * @param columNames
 	 *            The plain names of the colum
 	 */
 	public Scan(final String tableName, String[] columnNames){
 		this(tableName, columnNames, null);
 	}
 
 	/**
 	 * Contructs a new scan operator.
 	 * 
 	 * @param tableName
 	 *            file to read tuples from
 	 * @param columNames
 	 *            The plain names of the colum
 	 * @param prefix
 	 *            A prefix which is appended before each colum name. This is
 	 *            required for a hash operation, since two different relations
 	 *            could have a column with the same name!
 	 * 
 	 */
 	public Scan(final String tableName, String[] columnNames, String prefix) {
 
 		this.schema = new TupleSchema(columnNames, prefix);
 
 		InputStream inStream = FacesContextUtils.getInputStreamToDb(tableName);
 		if (inStream != null)
 			scanner = new Scanner(inStream);
 	}
 
 	@Override
 	public boolean moveNext() {
 
 		if (scanner == null) // in case that the file doesn't exist
 			return false;
 
 		try {
 
 			if (scanner.hasNextLine()) {
 				String nextLine = scanner.nextLine();
 				if(StringUtils.isNotNullNorEmpty(nextLine)){
 					String[] values = nextLine.split(",");
 					current = new Tuple(schema, values);
 					return true;
 				}
 			}
 			scanner.close();
 		} catch (Exception e) {
 			logger.warning("failed to close scanner");
 		}
 		return false;
 	}
 }
