 package operators.selection;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import org.apache.hadoop.mapred.JobConf;
 
 import relations.Schema;
 
 import java.util.Map.Entry;
 
 public class SelectionFilter {
 
 	private static final String CONF_KEY_NCOLS = "selection_NCols";
 	private static final String CONF_KEY_VALUES = "selection_values";
 	private static final String CONF_KEY_COLUMNS = "selection_cols";
 
 	ArrayList<SelectionEntry<Integer>> selectionArguments;
 
 	String filterType = "OR";
 
 	// TODO: we need relation name too!!!
 
 	/**
 	 * very simple selection implementation that checks for equality. TODO: add
 	 * ORs etc
 	 * 
 	 * @param tuple
 	 * @return
 	 */
 
 	public boolean checkSelection(String[] tuple) {
 		if ("OR".equals(filterType)) {
 			for (Entry<Integer, String> entry : selectionArguments) {
 
 				System.out.println(tuple[entry.getKey()] + " vs " + entry.getValue() + " tuple: " + new ArrayList<String>(Arrays.asList(tuple)));
 
 				if (tuple[entry.getKey()].equals(entry.getValue()))
 					return true;
 			}
 		}
 		if ("AND".equals(filterType)) {
 			for (Entry<Integer, String> entry : selectionArguments) {
 				if (!tuple[entry.getKey()].equals(entry.getValue()))
 					return false;
 			}
 			return true;
 		}
 		return false || selectionArguments.size() == 0;
 	}
 
 	/**
 	 * Constructor given Configuration. as join have multiple relations we add
 	 * relationPrefix field
 	 * 
 	 */
 	public SelectionFilter(JobConf conf, String relationPrefix) {
 		super();
 
 		int nSelectionCols = conf.getInt(relationPrefix + CONF_KEY_NCOLS, 0);
 		String[] selectionValues = conf.getStrings(relationPrefix + CONF_KEY_VALUES, new String[] {});
 		String[] sSelectionCols = conf.getStrings(relationPrefix + CONF_KEY_COLUMNS, new String[] {});
 
 		selectionArguments = new ArrayList<SelectionEntry<Integer>>();
 
 		System.out.println("SelectionFilter n=" + nSelectionCols);
 
 		for (int i = 0; i < nSelectionCols; i++) {
 			System.out.println("SelectionFilter key=" + sSelectionCols[i] + "--> " + selectionValues[i]);
 
 			selectionArguments.add(new SelectionEntry<Integer>(Integer.parseInt(sSelectionCols[i]), selectionValues[i]));
 		}
 	}
 
 	public static void addSelectionsToJob(JobConf conf, String relationPrefix, ArrayList<SelectionEntry<String>> selectionArgs, Schema schema) {
 		conf.setInt(relationPrefix + CONF_KEY_NCOLS, selectionArgs.size());
 
 		System.out.println("SelectionFilterCreate n=" + selectionArgs.size());
 
 		// using schema convert column names into their indexes
 		List<String> columnIndexes = new ArrayList<String>();
 		List<String> values = new ArrayList<String>();
 
 		for (SelectionEntry<String> entry : selectionArgs) {
 			columnIndexes.add(schema.columnIndex(entry.getKey()) + "");
 			values.add(entry.getValue());
 
 		}
 		conf.setStrings(relationPrefix + CONF_KEY_COLUMNS, columnIndexes.toArray(new String[0]));
 		conf.setStrings(relationPrefix + CONF_KEY_VALUES, values.toArray(new String[0]));
 
 	}
 }
