 package org.sleepfactory.sleeplog.scale;
 
 import java.util.Collection;
 
 import org.sleepfactory.sleeplog.util.SleepUtils;
 
 public enum Activity implements SleepAttributeEnum 
 {
 	BIKING (1L), WEIGHTS (2L), HIKING (3L), 
 	WALKING (4L), WORKOUT (5L);
 	
 	private Long value;
 	
 	private Activity (Long value)
 	{
 		this.value = value;
 	}
 	
 	public Long valueOf()
 	{
 		return value;
 	}
 	
 	public static Activity enumValueOf (Long value)
 	{
 		int intVal = value.intValue();
 		
 		switch (intVal)
 		{
 			case 1:
 				return BIKING;
 			case 2: 
 				return WEIGHTS;
 			case 3:
 				return HIKING;
 			case 4:
 				return WALKING;
 			case 5:
 				return WORKOUT;
 			default:
 				return null;
 		}
 	}
 	
 	public String qualitative()
 	{
 		return SleepUtils.qualitative (this);
 	}
 
 	public static String asString (Collection<Long> activities) 
 	{
		if (activities == null)
			return "";
		
 		String str = "";
 		
 		for (Long actId : activities)
 		{
 			Activity act = enumValueOf (actId);
 			str += act.qualitative() + ", ";
 		}
 		
 		if (str.contains (","))
 			str = str.substring (0, str.length()-2);
 		
 		return str;
 	}
 
 }
