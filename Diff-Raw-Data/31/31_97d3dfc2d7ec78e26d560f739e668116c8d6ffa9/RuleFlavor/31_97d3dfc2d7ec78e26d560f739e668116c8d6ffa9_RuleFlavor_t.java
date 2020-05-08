 package enums;
 
 /**
  * Flavors are represented by values between 0 and 100, where 100 is full affinity
  * with the associated property. (Eg. a rule with COMPLEX flavor of 100 is very complex.)
  * 0 Means the rule opposes that flavor and 50 means the rule is irrelevant to that flavor.
 * @author Stuart Holland
  * 
  * Need to discuss possibility of dynamic flavors in future implementations
  *
  */
 public enum RuleFlavor {
 	/**
 	 * Involve significant computations or make the game more difficult to follow
 	 * Useful in working out how far into the future an agent needs to visualize a rule change's outcome properly
 	 */
 	COMPLEX,
 	/**
 	 * 'Destroys' the flow of the game, making the game unplayable/unbalanced
 	 * Makes it less likely for there to be a winner
 	 */
 	DESTRUCTIVE,
 	/**
 	 * Makes easily understood, non-complex changes
 	 * Effects can be seen quite quickly in predictions
 	 */
 	SIMPLE,
 	/**
 	 * Last resort changes that can be used to swing the game/when there is nothing else good to do
 	 */
 	DESPERATION,
 	/**
 	 * Good for everyone
 	 */
 	BENEFICIAL,
 	/**
 	 * Introduces/removes some kind of win condition
 	 * Higher values are more likely to occur
 	 */
 	WINCONDITION,
 	/**
 	 * Makes the game behave in a regular manner
 	 * Without stable rules, the game will tend to not function correctly
 	 */
 	STABLE,
 	/**
 	 * Causes agents to get farther from winning the game
 	 */
 	DETRIMENTAL,
 }
