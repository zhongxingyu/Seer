 import java.util.Random;
 
 public class RandomClusterAlgorithm extends Algorithm
 {
 	public RandomClusterAlgorithm(int minimumClusters, int maximumClusters, Field field)
 	{
 		super(minimumClusters, maximumClusters, field);
 	}
 
 	@Override
 	public void run()
 	{
 		Random random = new Random();
 		
 		for(Point p : field)
 		{
 			int index = random.nextInt(maximumClusters);
 			if(index == 0)
 			{
 				p.assignToPointCategory(field.getNoise());
 			}
 			else
 			{
 				PointCategory c;
				c = field.getPointCategoryWithIndex(index);
 				if(c == null)
 					c = field.createCluster();
 				
 				p.assignToPointCategory(c);
 			}
 		}
 	}
 }
