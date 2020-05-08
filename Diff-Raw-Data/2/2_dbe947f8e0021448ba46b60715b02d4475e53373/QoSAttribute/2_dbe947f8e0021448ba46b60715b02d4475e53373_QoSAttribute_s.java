 package aco;
 
 /**
  * 
  * This class represents a QoS attribute, which is composed by:
  * 
  * 		- A value for each concrete service, normalized between 0
  * 			and 1 ("e crescente");
  * 
  * 		- An aggregation function;
  * 
  * 		- A corresponding weight, provided by the user. 
  * 
  * @author Andre Luiz Verucci da Cunha
  */
 public class QoSAttribute {
 
 	/* The possible aggregation functions. */
 	public static final int AGGREGATE_BY_SUM = 0x00;
 	public static final int AGGREGATE_BY_PRODUCT = 0x01;
 	public static final int AGGREGATE_BY_AVERAGE = 0x02;
 
 	/**
 	 * The QoS values.
 	 */
 	private float[][] mValues;
 
 	/**
 	 * The function to be used to compute the aggregated QoS.
 	 * 	It must be one of the following values:
 	 * 		- AGGREGATE_BY_SUM;
 	 * 		- AGGREGATE_BY_PRODUCT;
 	 * 		- AGGREGATE_BY_AVERAGE.
 	 */
 	private int mAggregationMethod;
 	
 	/**
 	 * The weight of this attribute.
 	 */
 	private float mWeight;
 	
 	/**
 	 * The maximum possible QoS for the current
 	 * 	the aggregation function and the current number of
 	 * 	virtual services.
 	 */
 	private float mMaximumQoS;
 
 	/**
 	 * Creates a QoSAttribute instance.
 	 * @param values The QoS values.
 	 * @param aggregationMethod The aggregation method to be used. 
 	 * @param weight The weight of this attribute.
 	 */
	public QoSAttribute(float[][] values, int aggregationMethod, int weight) {
 		
 		/* Check for the validity of the given values. */
 		for (int i = 0; i < values.length; i++) {
 			for (int j = 0; j < values[i].length; j++) {
 				if (values[i][j] > 1f || values[i][j] < 0f) {
 					throw new IllegalArgumentException(
 							String.format("values[%d][%d] is invalid: %d", i,
 									j, values[i][j]));
 				}
 			}
 		}
 		
 		mValues = values;
 		mAggregationMethod = aggregationMethod;
 		mWeight = weight;
 
 		switch (aggregationMethod) {
 		case AGGREGATE_BY_SUM:
 			mMaximumQoS = values.length;
 			break;
 		case AGGREGATE_BY_PRODUCT:
 			mMaximumQoS = 1f;
 			break;
 		case AGGREGATE_BY_AVERAGE:
 			mMaximumQoS = 1f;
 			break;
 		default:
 			mMaximumQoS = -1;
 			break;
 		}
 	}
 
 	/**
 	 * Evaluates the aggregated QoS of a composition. 
 	 * @param composition A vector containing the index of the
 	 * 	concrete service corresponding to each abstract service.
 	 * @return The aggregated QoS value.
 	 */
 	public float getAggregatedQoS(int[] composition) {
 		float aggregatedQoS;
 
 		if (composition.length != mValues.length) {
 			throw new IllegalArgumentException(String.format(
 					"Dimensions mismatch. Expected %d, got %d.", mValues.length,
 					composition.length));
 		}
 		for (int i = 0; i < composition.length; i++) {
 			if ((composition[i] < 0) || (composition[i] >= mValues[i].length)) {
 				throw new IllegalArgumentException(String.format(
 						"Composition[%d] is invalid: ", i, composition[i]));
 			}
 		}
 
 		switch (mAggregationMethod) {
 		case AGGREGATE_BY_SUM:
 			aggregatedQoS = 0f;
 			for (int i = 0; i < composition.length; i++) {
 				aggregatedQoS += mValues[i][composition[i]];
 			}
 			break;
 		case AGGREGATE_BY_PRODUCT:
 			aggregatedQoS = 1f;
 			for (int i = 0; i < composition.length; i++) {
 				aggregatedQoS *= mValues[i][composition[i]];
 			}
 			break;
 		case AGGREGATE_BY_AVERAGE:
 			aggregatedQoS = 0f;
 			for (int i = 0; i < composition.length; i++) {
 				aggregatedQoS += mValues[i][composition[i]];
 			}
 			aggregatedQoS /= composition.length;
 			break;
 		default:
 			aggregatedQoS = -1;
 			break;
 		}
 
 		return aggregatedQoS;
 	}
 	
 	/**
 	 * 
 	 * @return The maximum QoS value.
 	 */
 	public float getMaximumQoS() {
 		return mMaximumQoS;
 	}
 	
 	/**
 	 * 
 	 * @return The weight of this attribute.
 	 */
 	public float getWeight() {
 		return mWeight;
 	}
 	
 	/**
 	 * 
 	 * @return The QoS values.
 	 */
 	public float[][] getValues() {
 		return mValues;
 	}
 }
