 /*
  * Acacia - GS-FLX & Titanium read error-correction and de-replication software.
  * Copyright (C) <2011>  <Lauren Bragg and Glenn Stone - CSIRO CMIS & University of Queensland>
  * 
  * 	This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package pyromaniac.Algorithm;
 
 import pyromaniac.IO.AcaciaLogger;
 import umontreal.iro.lecuyer.probdist.BinomialDist;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class BinomialTest.
  */
 public class BinomialTest extends HypothesisTest
 {
 	
 	/**
 	 * Instantiates a new binomial test.
 	 *
 	 * @param obsAbove the number of observations above the mode
 	 * @param obsBelow the number of observations below the mode
 	 * @param obsMode the number  observed modal homopolymer length
 	 * @param modeLength the mode homopolymer length
 	 * @param P the p-value
 	 * @param alpha the significance threshold
 	 * @param avgFlowPos the average flow position
 	 * @param logger the logger
 	 * @param verbose the verbose
 	 */
 	public BinomialTest(int obsAbove, int obsBelow, int obsMode,int modeLength, double [] P,  double alpha, double avgFlowPos, AcaciaLogger logger, boolean verbose)
 	{
 		super(obsAbove, obsBelow, obsMode, modeLength, P, alpha, avgFlowPos,  logger, verbose);
 	}
 	
 	/* (non-Javadoc)
 	 * @see pyromaniac.Algorithm.HypothesisTest#runTest()
 	 */
 	public void runTest() throws Exception 
 	{
 		if (observationsBelowMode == 0 && observationsAboveMode == 0) 
 		{
 			this.p = 1;
 		}
 		else if (alpha > 0 && (observationsAboveMode == observationsAtMode
 				|| observationsBelowMode == observationsAtMode)) 
 		{
 			this.significantAbove = true;
 			this.significantBelow = true;
 			this.p = alpha;
 		}
 		else
 		{
 			double obsErrorFreqAbove = (double) observationsAboveMode;
 			double errorProbFreqAbove = P[2];
 			
			//P(X >= x)
 			BinomialDist binomialDistOvercall = new BinomialDist(this.N, errorProbFreqAbove);
 			double pAbove = binomialDistOvercall.barF(obsErrorFreqAbove);
			
 				
			this.p = pAbove; //regardless of significance
			
 			if (this.alpha > 0 ) 
 			{
				this.significantAbove = (pAbove <= this.alpha);	
				if(verbose)
				{
					logger.writeLog("Significant above: p=" + pAbove + "and alpha= " + this.significantAbove, AcaciaLogger.LOG_DEBUG);
				}
 			}
 			else
 			{
 				this.significantAbove = false;
 			}	
 			
 			double obsErrorFreqBelow = (double) observationsBelowMode;
 			double errorProbModeBelow = P[1];
 			
 			BinomialDist binomialDistUndercall = new BinomialDist(this.N, errorProbModeBelow);
 			
 			double pBelow = binomialDistUndercall.barF(obsErrorFreqBelow);
 			
			
 			if (this.alpha > 0 ) 
 			{
 				this.significantBelow = (pBelow <= this.alpha);
				if(this.significantBelow)//if significant
					this.p = pBelow; 	
				if(verbose)
				{
					logger.writeLog("Significant below: p=" + pBelow + "and alpha= " + this.significantBelow, AcaciaLogger.LOG_DEBUG);
				}
 			}
 			else
 			{
 				this.significantBelow = false;
 			}				
 		}
 	}
 }
