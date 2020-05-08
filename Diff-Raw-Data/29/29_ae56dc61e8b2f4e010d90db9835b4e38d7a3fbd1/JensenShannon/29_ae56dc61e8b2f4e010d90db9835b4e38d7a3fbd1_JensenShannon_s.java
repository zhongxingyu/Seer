 package token.probabilitydistribution.similarity;
 
 /**
  * Calcul de la distance classique de Jensen-Shannon entre deux distributions.
  * Attention : tout les éléments de first doivent être présents dans second et inversement. Si ce n'est pas le cas, penser à utiliser des distributions lissées
  * @author Ludovic Bonnefoy (ludovic.bonnefoy@gmail.com)
  */
 public class JensenShannon extends AbstractProbabilityDistributionSimilarity 
 {
 	public Double similarity() 
 	{
 		Double result = new Double(0); //score de similarité
 
 		for(String key : _first.keySet()) //pour chaque élément de la première distribution
 		{
 			Double pi = _first.get(key), qi = _second.get(key);
 			Double mi = (pi + qi)/2;
 
 			result += (pi * Math.log(pi / mi))/2 + (qi * Math.log(qi / mi))/2;
 		}
 
		return 1./result;
 	}
 }
