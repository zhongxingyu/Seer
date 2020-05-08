 package token.probabilitydistribution.similarity;
 
 public class DiceCoefficient extends AbstractFreqsProbabilityDistributionSimilarity 
 {
 	/**
 	 * Calcul du score du Dice Coefficient entre deux distributions. Cette version ne prend pas juste présent ou absent mais utilise les fréquences.
 	 * On suppose que les deux distributions ont été définie précédemment.<br/>
 	 * Dice(A,B) = 2Jaccard / (1 + Jaccard)
 	 * @return Score de similarité de Jaccard
 	 */
 	@Override
 	public Double similarity() 
 	{
 		Jaccard jaccard = new Jaccard(_first, _second);
 		Double jaccardSimilarity = jaccard.similarity();
 		
 		Double dice = (2 * jaccardSimilarity) / (1 + jaccardSimilarity);
 		
 		return dice;
 	}
 }
