 package what.sp_chart_creation;
 
 // java imports
 import java.util.ArrayList;
 
 // intern imports
 import what.Printer;
 import what.sp_config.RowEntry;
 
 /**
  * This class represents a measure of a chart request.
  * It stores the information about the requested measure 
  * and provides methods to get information about them for the warehouse.<br>
  * It also provides static Strings describing possible aggregations.
  * 
  * @author Jonathan, PSE Gruppe 14
  * @see DimChart
  */
 public class Measure {
 	
 	/* Possible aggregations. */
 	/** Constant measure name. */
 	public static final String COUNT = "#";
 	/** Constant measure name. */
 	public static final String SUM = "SUM";
 	/** Constant measure name. */
 	public static final String MAX = "MAX";
 	/** Constant measure name. */
 	public static final String AVG = "AVG";
 	/** Constant measure name. */
 	public static final String NONE = "NONE";
 	
 	/** The aggregation in MySQL for COUNT. */
 	public static final String COUNT_SQL = "count(*)";
 	
 	/** Left bracket. */
 	private static final String LBR = "(";
 	/** Right bracket. */
 	private static final String RBR = ") ";
 	
 	/** Static collection of the possible aggregations. */
 	private static ArrayList<String> aggregations = new ArrayList<String>();
 	static {
 		aggregations.add(COUNT);
 		aggregations.add(SUM);
 		aggregations.add(MAX);
 		aggregations.add(AVG);
		//aggregations.add(NONE);
 	}
 
 	/** The aggregation of this Measure. */
 	private String aggregation;
 	
 	/** The RowEntry of this Measure. */
 	private RowEntry row;
 
 	/**
 	 * Private constructor for Measure.<br>
 	 * They are produced vie static factory getMeasure(..).
 	 * 
 	 * @param aggregation String aggregation of the measure
 	 * @param row RowEntry of this measure
 	 */
 	private Measure(String aggregation, RowEntry row) {
 		assert (aggregation != null);
 		assert (row != null);
 		
 		this.row = row;
 		this.aggregation = aggregation;
 	}
 
 	/**
 	 * Returns a Measure for the given parameters.<br>
 	 * 
 	 * @param aggregation must equal one of the static aggregations (ignore case)
 	 * @param row RowEntry for this measure, just null allowed if count() is selected
 	 * @return a Measure for the given parameters
 	 */
 	public static Measure getMeasure(String aggregation, RowEntry row) {
 		if (aggregation == null) {
 			throw new IllegalArgumentException();
 		}
 		String agg = getRightAggregation(aggregation);
 		if (agg == null) {
 			Printer.perror("Aggregation is not legal.");
 			return null;
 		}
 		
 		if ((row == null) && (agg != COUNT)) {
 				throw new IllegalArgumentException();
 		}
 
 		return new Measure(agg, row);
 	}
 	
 	/**
 	 * Returns the right static aggregation for the given String.
 	 * 
 	 * @param agg String for which the aggregation is searched
 	 * @return the right static aggregation for the given String
 	 */
 	private static String getRightAggregation(String agg) {
 		assert (agg != null);
 		
 		if (agg.equalsIgnoreCase(COUNT)) {
 			return COUNT;
 		} else if (agg.equalsIgnoreCase(SUM)) {
 			return SUM;
 		} else if (agg.equalsIgnoreCase(MAX)) {
 			return MAX;
 		} else if (agg.equalsIgnoreCase(AVG)) {
 			return AVG;
 		} else if (agg.equalsIgnoreCase(NONE)) {
 			return NONE;
 		}
 			
 		return null;
 	}
 
 	/**
 	 * Returns the select statement for the Measure.
 	 * 
 	 * @return the select statement for the measure
 	 */
 	protected String getMeasureSelect() {
 		if (getAggregation() == COUNT) {
 			return COUNT_SQL;
 		} else if (getAggregation() == SUM) {
 			return SUM + LBR + getMeasureRow() + RBR;
 		} else if (getAggregation() == MAX) {
 			return MAX + LBR + getMeasureRow() + RBR;
 		} else if (getAggregation() == AVG) {
 			return AVG + LBR + getMeasureRow() + RBR;
 		}  else if (getAggregation() == NONE) {
 			return getMeasureRow();
 		}
 			
 		Printer.pfail("Finding a fitting measure.");		
 		
 		return null;
 	}
 	
 	/**
 	 * Returns the aggregation of this Measure.
 	 * 
 	 * @return the aggregation of this Measure
 	 */
 	protected String getAggregation() {
 		return aggregation;
 	}
 
 	/**
 	 * Returns the row in the warehouse of this Measure.
 	 * 
 	 * @return the row in the warehouse of this Measure
 	 */
 	protected String getMeasureRow() {
 		return row.getColumnName();
 	}
 	
 	/**
 	 * Returns the name of this Measure.
 	 * 
 	 * @return the name of this Measure
 	 */
 	protected String getName() {
 		return row.getName();
 	}
 	
 	/**
 	 * Return a collection of the static aggregations.
 	 * 
 	 * @return a collection of the static aggregations
 	 */
 	public static ArrayList<String> getAggregations() {
 		return aggregations;
 	}
 	
 }
