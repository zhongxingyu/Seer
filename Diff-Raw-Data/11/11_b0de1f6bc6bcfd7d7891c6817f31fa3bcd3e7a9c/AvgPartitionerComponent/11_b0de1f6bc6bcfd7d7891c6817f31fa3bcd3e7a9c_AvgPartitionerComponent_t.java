 package ch.uzh.ddis.katts.bolts.aggregate.component;
 
 import java.util.List;
 
 import ch.uzh.ddis.katts.bolts.aggregate.PartitionerBolt;
 import ch.uzh.ddis.katts.bolts.aggregate.PartitionerComponent;
 
 /**
  * The average partition component builds the average of a partition.
  * 
  * @see PartitionerBolt
  * 
  * @author Thomas Hunziker
  * 
  */
 public class AvgPartitionerComponent implements PartitionerComponent {
 
 	@Override
 	public Object resetBucket() {
 		return new AvgStorageItem();
 	}
 
 	@Override
 	public Object updateBucket(Object storage, double number) {
 		AvgStorageItem internalStorage = (AvgStorageItem) storage;
 		internalStorage.addItem(number);
 		return internalStorage;
 	}
 
 	@Override
 	public Double calculateAggregate(List<Object> componentBuckets) {
 		double totalSum = 0;
 		double totalCount = 0;
 		for (Object bucketValue : componentBuckets) {
 			if (bucketValue == null) {
 				continue;
 			}
 			AvgStorageItem internalBucketValue = (AvgStorageItem) bucketValue;
			totalSum += internalBucketValue.getSum();
			totalCount += internalBucketValue.getCount();
 		}
 
		if (totalCount == 0) {
			return null;
		} else {
			return totalSum / totalCount;
		}
 	}
 
 	@Override
 	public String getName() {
 		return "avg";
 	}
 
 }
