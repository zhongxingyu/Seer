 package ch.uzh.ddis.katts.bolts.aggregate.component;
 
 import java.util.List;
 
 import ch.uzh.ddis.katts.bolts.aggregate.PartitionerComponent;
 
 /**
  * This class provides the functionality to compute the maximal value in a partition.
  * 
  * @author Thomas Hunziker
  * 
  */
 public class MaxPartitionerComponent implements PartitionerComponent {
 
 	@Override
 	public Object resetBucket() {
 		return new NullObject();
 	}
 
 	@Override
 	public Object updateBucket(Object storage, double number) {
 		Double internalStorage;
		if (storage == null) {
			storage = new NullObject();
		}
		
 		if (storage instanceof NullObject) {
 			internalStorage = number;
 		} else {
 			internalStorage = (Double) storage;
 			if (internalStorage < number) {
 				internalStorage = number;
 			}
 		}
 		return internalStorage;
 	}
 
 	@Override
 	public Double calculateAggregate(List<Object> componentBuckets) {
 		Double max = null;
 		for (Object bucketValue : componentBuckets) {
 			if (bucketValue == null || bucketValue instanceof NullObject) {
 				continue;
 			}
 			Double internalBucketValue = (Double) bucketValue;
 			if (max == null) {
 				max = internalBucketValue;
 			} else if (max < internalBucketValue) {
 				max = internalBucketValue;
 			}
 		}
 
 		return max;
 	}
 
 	@Override
 	public String getName() {
 		return "max";
 	}
 
 }
