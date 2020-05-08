 import java.util.ArrayList;
 
 public class Threshold
 {
 	int stepCount = 100; // TODO: veranderen door testen
 
 	int previousNumberOfPoints;
 	int numberOfPoints;
 	float currentThreshold;
 	float maxThreshold;
 
 	/**
 	 * Returns a Threshold value. Everything BELOW this threshold should be cut
 	 * off (noise).
 	 * 
 	 * @return
 	 */
 	public float findThreshold(KDE KDE)
 	{
 		// Tussen element 0 en 1 zit een switch, tussen element 2 en 3 een
 		// switch, etc.
 		ArrayList<Float> switches = new ArrayList<Float>();
 
 		maxThreshold = KDE.getMaxDensity();
 		previousNumberOfPoints = 0;
 
 		boolean inCluster = false; // Variabele die aangeeft of we op het moment
 									// 'in' een cluster zitten
 		float step = maxThreshold / stepCount;
 
 		if (Utils.floatAlmostEquals(step, 0))
 			return 0;
 
 		for (currentThreshold = maxThreshold; currentThreshold >= 0; currentThreshold -= step)
 		{
 			numberOfPoints = KDE.getPointCountAboveThreshold(currentThreshold);
 
 			Utils.Log("NumberOfPoints: "  + numberOfPoints + " | PreviousNumberOfPoints: " + previousNumberOfPoints);
 			
 			if (numberOfPoints > previousNumberOfPoints)
 			{
 				// Er is een verandering
 				if (!inCluster)
 				{
 					// Beginpunt van nieuwe cluster
 					switches.add(currentThreshold);
 				}
 
 				inCluster = true;
 			} else if (inCluster)
 			{
 				// Er is geen verandering meer, dus einde van cluster gevonden
 				switches.add(currentThreshold);
 
 				inCluster = false;
 			}
 			
 			previousNumberOfPoints = numberOfPoints;
 		}
 		
 		Utils.Log("Switches Size: " + switches.size());
 
		for(int i = 0; i < switches.size() - 2; i += 2)
 		{
 			Utils.Log("Switch " + i + ": " + switches.get(i) + " | Switch " + (i+1) + ": " + switches.get(i+1));
 		}
 		
 		/*
 		 * Om de noise eraf te knallen: We detecteren of de laatste cluster veel
 		 * 'steps' bevat in vergelijking met de rest, en de cluster die daarvoor
 		 * komt kunnen we samenvoegen wanneer de hoeveelheid steps tussen die
 		 * twee klein is.
 		 */
 
 		boolean noiseDetected = false;
 
 		// Merge last thresholds
 		float mergeThreshold = (float) Math.PI / stepCount * maxThreshold; // TODO:
 																			// aanpassen
 																			// door
 																			// testen
 		
 		Utils.Log("MergeThreshold: " + mergeThreshold);
 		
 		for (int i = switches.size() - 2; i >= 0; i -= 2)
 		{
 			if (Math.abs(switches.get(i) - switches.get(i + 1)) > mergeThreshold)
 				break; // Als het een cluster is dan breakt ie altijd. Als het
 						// noise is kn hij breaken.
 
 			// Als hij niet breekt is het sowieso noise.
 			noiseDetected = true;
 
 			switches.remove(i + 1);
 			switches.remove(i);
 		}
 
 		if (noiseDetected) // we hebben sowieso noise, dus laatste eraf halen
 		{
 			// Hak laatste eraf
 			switches.remove(switches.size() - 1);
 			switches.remove(switches.size() - 1);
 		} else
 		// het is niet zeker of we noise hebben, bepaal dit
 		{
 			//calculate average
 			float totalDifference = 0;
 			int totalSteps = 0;
 			for (int i = 0; i < switches.size() - 3; i += 2)
 			{
 				totalDifference += Math.abs(switches.get(i)
 						+ switches.get(i + 1));
 				totalSteps++;
 			}
 
 			// de average van alle switches behalve de laatste
 			int averageSteps = (int) Math.ceil(totalDifference / totalSteps);
 			float maximalDeviation = averageSteps * 1.4f; // TODO: Deviation
 															// 1.4f bepalen door
 															// testen
 			
 			if (switches.size() >= 2 && Math.abs(switches.get(switches.size() - 1)
 					- switches.get(switches.size() - 2)) > maximalDeviation)
 			{
 				// Laatste is noise dus die hakken we eraf
 				switches.remove(switches.size() - 1);
 				switches.remove(switches.size() - 1);
 			}
 		}
 
 		return switches.get(switches.size() - 1);
 	}
 }
