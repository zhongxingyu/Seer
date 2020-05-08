 package org.adaptsuite.sorter;
 
 public class AdaptSuiteSorter {
 
 	private boolean[] chosenTests;
 	private Long[][] knaspackTabble;
 	
 	public boolean[]  FindTests (Long[] testTime, Long[] testFailure, int queueSize, Long maxTime) {
 		
 		
 		chosenTests = new boolean[queueSize];
		knaspackTabble = new Long [queueSize][maxTime.intValue()];
 		TestsValue(testTime, testFailure, queueSize, maxTime);
 		ChooseTests(testTime, queueSize, maxTime);
 		return chosenTests;
 		
 	}
 	
 	private void  TestsValue (Long[] testTime, Long[] testFailure, int queueSize, Long maxTime) {
 		
 		Long a, b;
 		
		for (int time = 0; time < maxTime; time++) {
 			knaspackTabble[0][time] = (Long)0L;
 			
			for (int i = 1; i < queueSize; i++) {
 				a = knaspackTabble[i-1][time];
				if (testTime[i].intValue() > time)
 					b = (Long)0L;
 				else
					b = knaspackTabble[i-1][time - testTime[i].intValue()] + testFailure[i];
 				
 				knaspackTabble[i][time] = Max(a, b);
 			}
 		}
 	}
 	
 	private Long Max (Long a, Long b) {
 		
 		return a.longValue() >= b.longValue() ? a : b;
 	}
 	
 	
 	private void ChooseTests (Long[] testTime, int queueSize, Long maxTime) {
 		
		Long time = maxTime - 1;
 		
 		for (int i = queueSize - 1; i >= 1; i--)
 			if(knaspackTabble[i][time.intValue()] == knaspackTabble[i-1][time.intValue()])
 				chosenTests[i] = false;
 			else {
 				chosenTests[i] = true;
 				time -= testTime[i];
 			}
 	}
 	
 }
