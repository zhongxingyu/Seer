 package token.probabilitydistribution.similarity;
 
 /**
  * Calcul de la divergence classique de Kullback-Leibler entre deux distributions.
  * Attention : retourne 1/KL pour avoir une similarité et non une divergence.
  * Attention : tout les éléments de first doivent être présents dans second. Si ce n'est pas le cas, penser à utiliser des distributions lissées.
  * @author Ludovic Bonnefoy (ludovic.bonnefoy@gmail.com)
  */
 public class KullbackLeibler extends AbstractProbabilityDistributionSimilarity 
 {
 	/**
 	 * Retourne l'inverse de KL afin d'avoir une similarité et non une divergence
 	 * @return Retourne 1/KL
 	 */
 	public Double similarity() 
 	{
 		Double result = new Double(0); //score de divergence
 
 		for(String key : _first.keySet()) //pour chaque élément de la première distribution
 			result += _first.get(key) * Math.log(_first.get(key) / _second.get(key)); //mise à jour du score
 
		return -result;
 	}
 }
