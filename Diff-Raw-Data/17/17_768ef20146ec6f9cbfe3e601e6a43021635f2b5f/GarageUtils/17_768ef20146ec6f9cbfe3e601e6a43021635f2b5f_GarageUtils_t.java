 package ie.tech.talk.utils;
 
 import ie.tech.talk.domain.SparkPlug;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class GarageUtils
 {
 	public static boolean checkSparkPlugs(List<SparkPlug> sparkPlugs)
 	{
 		boolean sparkPlusOk = true;
 
 		for (SparkPlug sparkPlug : sparkPlugs)
 		{
 			if (!sparkPlug.isWorking())
 			{
 				return false;
 			}
 		}
 
 		return sparkPlusOk;
 	}
 
 	public static List<SparkPlug> getNewSparkPlugs()
 	{
 		List<SparkPlug> sparkPlugs = new ArrayList<SparkPlug>();
 
		for (int i = 0; i < 8; i++)
		{
			sparkPlugs.add(new SparkPlug());
		}

 		return sparkPlugs;
 	}
 }
