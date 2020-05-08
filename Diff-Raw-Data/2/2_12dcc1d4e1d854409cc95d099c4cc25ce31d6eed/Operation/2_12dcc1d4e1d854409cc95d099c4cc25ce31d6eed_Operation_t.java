 package simulator;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import org.neo4j.graphdb.GraphDatabaseService;
 
 public abstract class Operation {
 	protected static final String ID_TAG = "id";
 	protected static final String TYPE_TAG = "type";
 	protected static final String ARGS_TAG = "args";
 	protected static final String HOP_TAG = "hop";
 	protected static final String INTERHOP_TAG = "interhop";
 	protected static final String TRAFFIC_TAG = "traffic";
 	protected static final String GIS_PATH_LENGTH_TAG = "pathlen";
 	protected static final String GIS_DISTANCE_TAG = "distance";
 
 	public static String[] getInfoHeader() {
		String[] res = new String[8];
 		res[0] = ID_TAG;
 		res[1] = TYPE_TAG; // FIXME can remove this, its in ARGS anyway
 		res[2] = ARGS_TAG;
 		res[3] = HOP_TAG;
 		res[4] = INTERHOP_TAG;
 		res[5] = TRAFFIC_TAG;
 		res[6] = GIS_PATH_LENGTH_TAG;
 		res[7] = GIS_DISTANCE_TAG;
 		return res;
 	}
 
 	protected final String[] args;
 	protected HashMap<String, String> info;
 
 	public String getType() {
 		return (String) info.get(TYPE_TAG);
 	}
 
 	public long getId() {
 		return Long.parseLong(info.get(ID_TAG));
 	}
 
 	public Operation(long id, String[] args) {
 		this.info = new HashMap<String, String>();
 		for (String key : getInfoHeader()) {
 			info.put(key, "");
 		}
 		this.info.put(ID_TAG, Long.toString(id));
 		this.info.put(TYPE_TAG, getClass().getName());
 		this.info.put(ARGS_TAG, Arrays.toString(args));
 		this.info.put(HOP_TAG, Long.toString(0));
 		this.info.put(INTERHOP_TAG, Long.toString(0));
 		this.info.put(TRAFFIC_TAG, Long.toString(0));
 		this.info.put(GIS_PATH_LENGTH_TAG, Long.toString(0));
 		this.info.put(GIS_DISTANCE_TAG, Long.toString(0));
 
 		this.args = args;
 
 		if (!args[0].equals(getType())) {
 			throw new Error("Wrong Type " + args[0] + " called " + getType());
 		}
 
 	}
 
 	public final boolean executeOn(GraphDatabaseService db) {
 		return onExecute(db);
 	}
 
 	public abstract boolean onExecute(GraphDatabaseService db);
 
 	private String appendix = "";
 
 	public String getApendix() {
 		return appendix;
 	}
 
 	public final void appendToLog(String item) {
 		appendix += item;
 	}
 }
