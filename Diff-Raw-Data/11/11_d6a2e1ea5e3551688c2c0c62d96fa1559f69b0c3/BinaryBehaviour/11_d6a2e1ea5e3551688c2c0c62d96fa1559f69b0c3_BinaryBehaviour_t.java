 package algorithm;
 
 import org.apache.commons.math3.distribution.NormalDistribution;
 
 import crowdtrust.BinaryR;
 
 public class BinaryBehaviour {
 	/*
 	 * Represents how biased the member is towards 0/1 1(low threshold)
 	 * 0 (low threshold) No bias if = 0
 	 */
 	protected double threshold;
 	/*
 	 * Represents how well the member can distinguish between the two classes of answer
 	 */
 	protected double sensitivityIndex;
 	/*
 	 * Assumed in the paper
 	 */
 	protected int variance    = 1;
 	protected int standardDev = 1;
 	/*
 	 * Records the members answering statistics used to calculate t, d and
 	 * therefore ukj
 	 */
 	protected int truePos;
 	protected int trueNeg;
 	protected int totalPos;
 	protected int totalNeg;
 	protected double truePosRate;
 	protected double trueNegRate;
 	/*
 	 * Seed the crowd memeber with these inital values from this the true positive
 	 * and negative rates can be calculated which are used to calculate the inital
 	 * t and d, if you seeded the member with a t and d and truepos/neg rates the
 	 * rates wouldn't actually reflect that t and d.
 	 */
 	public BinaryBehaviour(int truePos, int trueNeg, int totalPos, int totalNeg){
 		this.truePos  = truePos ; this.trueNeg = trueNeg ;
 		this.totalPos = totalPos; this.totalNeg = totalNeg;
 		this.updateRates();      //Set up the inital true pos/neg rates
 //		this.updateSensThresh(); //Set up the inital threshold and sensetivity index
 	}
 	
 	public int generateAnswer(BinaryR response){
		int realanswer;
 		if (response.isTrue()){
			realanswer = 1;
 		}else{
			realanswer = 0;
 		}
 		//update the sensory index and threshold for the calculation of ujk
 	//	this.updateRates();
 		this.updateSensThresh();
 		//generate ujk
 		double ujk = calculateujk(response.isTrue() ? 1 : 0);
 		//generate the appropriate normal distribution
 		NormalDistribution dist = new NormalDistribution(ujk, this.standardDev);
 		/*
 		 * generate a random number sampled from this distribution representing
 		 * the annotators signal
 		 */
 
 		double signal = dist.sample();
		System.out.println("Rate" + this.truePosRate + " Answer " +  realanswer + " Signal " + signal );
 		int answer = (Double.compare(signal, this.threshold) > 0) ? 1 : 0;
 		//this.updateNumbers(answer, actualAnswer);	//Update truePos/neg and totalpos/neg
 		this.updateRates();		//Update truePos/negRates
 		return answer;
 	}
 
 	/*
 	 * Update the total positive/negative answers and the true 
 	 * positive/negative answers if they got it correct.
 	 */
 	private void updateNumbers(int answer, int actualAnswer){
 		if(actualAnswer == 1){
 			this.totalPos++;
 			if(answer == 1){
 				this.truePos++;
 			}
 		}else{
 			this.totalNeg++;
 			if(answer == 0){
 				this.trueNeg++;
 			}
 		}
 	}
 	
 	//Calculates the new true pos/neg rates
 	private void updateRates(){
 		//System.out.println("~~~~~~~~~~~~Calculating rates~~~~~~~~~~~~~");
 		//System.out.println("Total Pos: " + this.totalPos + " Total neg " + this.totalNeg);
 		//System.out.println("True  Pos: " + this.truePos + " true neg " + this.trueNeg);
 		//System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
 		this.truePosRate = ((this.truePos * 1.0) / (this.totalPos * 1.0));
 		this.trueNegRate = ((this.trueNeg * 1.0) / (this.totalNeg * 1.0));
 	}
 	
 	/*
 	 * 	|1  1/2|   |t| | inv(aj0)     |  
 	 *	|1 -1/2| = |d| | inv(1 - aj1) |
 	 *
 	 *	From this...
 	 *
 	 *	t + d/2 = inv(aj0)
 	 *  t - d/2 = inv(1 - aj1)
 	 */ 
 	private void updateSensThresh(){
 		//System.out.println("~~~~~~~~~~Calculating sens/thresh~~~~~~~~~~~");
 		//Create a standard normal distribution
 		//System.out.println("True neg rate = " + this.trueNegRate);
 		//System.out.println("True pos rate = " + this.truePosRate);
 		NormalDistribution standardNormal = new NormalDistribution();
 		double invTrueNeg = standardNormal.inverseCumulativeProbability(this.trueNegRate); 
 		double invTruePos = standardNormal.inverseCumulativeProbability(1 - this.truePosRate);
 		
 		this.sensitivityIndex = invTrueNeg - invTruePos;
 		this.threshold = invTrueNeg - (this.sensitivityIndex / (2 * 1.0));
 		//System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
 	}
 	
 	private double calculateujk(int actualAnswer){
 		if(actualAnswer == 1){
 			return (this.sensitivityIndex / 2);
 		}else{
 			return -(this.sensitivityIndex / 2);
 		}
 	}
 	
 	/*
 	 * 	Get methods mainly used for testing
 	 */
 	
 	public int getTotalPos(){
 		return this.totalPos;
 	}
 	
 	public int getTotalNeg(){
 		return this.totalNeg;
 	}
 	
 	public int getTruePos(){
 		return this.truePos;
 	}
 	
 	public int getTrueNeg(){
 		return this.trueNeg;
 	}
 	
 	public int getFalseNeg(){
 		return (this.totalNeg - this.trueNeg);
 	}
 	
 	public int getFalsePos(){
 		return(this.totalPos - this.truePos);
 	}
 	
 	public double getThreshold(){
 		return this.threshold;
 	}
 	
 	public double getSensIndex(){
 		return this.sensitivityIndex;
 	}
 	
 	public double getTruePosRate(){
 		return this.truePosRate;
 	}
 	
 	public double getTrueNegRate(){
 		return this.trueNegRate;
 	}
 	
 }
