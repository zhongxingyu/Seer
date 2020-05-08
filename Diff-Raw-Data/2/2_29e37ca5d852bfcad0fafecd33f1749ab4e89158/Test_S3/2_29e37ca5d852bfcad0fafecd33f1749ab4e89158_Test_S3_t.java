 
public class Test_S3
 {
 
 	/**
 	 * @param args
 	 */
 	public static void main (String[] args)
 	{
 		Tape tape = null;
 		try
 		{	// error: 101 0 => add: 110 ; divide: inf. loop
 			//        0  10 => divide: error -> if(dividend>divisor)
 			//        0   0 => add: 1 ; substract: [ ] ; multiply: [ ] ; divide: error -> if(dividend>divisor)
 			//        10 10 => subtract: [ ]; divide: error -> if(dividend>divisor)
 			// nombre très très grand: => ils utilisent des longs (alors qu'ils ne peuvent pas ;) )
 			String cTestTapes[] = {"101 10", "101 0", "0 10", "0 0", "10 10"};
 			String cMethods[] = {"add", "substract", "multiply", "divide"};
 			for (String cTape : cTestTapes)
 			{
 				System.out.println ("\n" + cTape);
 				for (String cMeth : cMethods)
 				{
 					if (cTape.compareTo (cTestTapes[1]) == 0 && cMeth.compareTo (cMethods[3]) == 0)
 						continue; // avoid inf. loop
 					System.out.print (cMeth + ": ");
 					tape = new TapeA_B (cTape);
 					try
 					{
 						MTA_B.class.getDeclaredMethod (cMeth, new Class[]{Tape.class}).invoke (null, tape);
 					} catch (java.lang.reflect.InvocationTargetException e)
 					{
 						System.out.print ("Exception... ");
 					}
 					System.out.println (tape);
 				}
 			}
 		} catch (Exception e) // if error in TapeA_B
 		{
 			e.printStackTrace();
 		}
 	}
 
 }
 
