 package yeti.stats;
 
 /**
 
 YETI - York Extensible Testing Infrastructure
 
 Copyright (c) 2009-2011, Manuel Oriol <manuel.oriol@gmail.com> - University of York
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:
 1. Redistributions of source code must retain the above copyright
 notice, this list of conditions and the following disclaimer.
 2. Redistributions in binary form must reproduce the above copyright
 notice, this list of conditions and the following disclaimer in the
 documentation and/or other materials provided with the distribution.
 3. All advertising materials mentioning features or use of this software
 must display the following acknowledgement:
 This product includes software developed by the University of York.
 4. Neither the name of the University of York nor the
 names of its contributors may be used to endorse or promote products
 derived from this software without specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 **/ 
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import yeti.YetiLog;
 
 
 /**
  * 
  * Class that represents a data set for evaluating the evolution of Yeti.
  * 
  * @author Manuel Oriol (manuel@cs.york.ac.uk)
  * @date Mar 15, 2011
  *
  */
 public class YetiDataSet {
 	
 	/**
 	 * The precision of the approximation for the hill-climbing algorithm (the precision will be 10^precision).
 	 * Default value is 5. 
 	 */
 	public static int precision = 5;
 	
 	/**
 	 * The vector containing all x values.
 	 */
 	public ArrayList<Double> xVector = null;
 
 	/**
 	 * The vector containing all y values.
 	 */
 	public ArrayList<Double> yVector = null;
 	
 	
 	
 	/**
 	 * Simple constructor. 
 	 * 
 	 * @param xs the x values.
 	 * @param ys the y values.
 	 */
 	public YetiDataSet(ArrayList<Double> xs, ArrayList<Double> ys) {
 		xVector =xs;
 		yVector =ys;
 		
 	}
 	
 	/**
 	 * Calculates the square redisuals for a given equation.
 	 * 
 	 * @param e the equation.
 	 * @return the square residuals.
 	 */
 	public double squaredResiduals (YetiEquation e) {
 		double sum = 0.0;
 		int max = xVector.size();
 		// we simply iterate through all the xs
 		// we then aggregate each (xi-yi)^2
 		for (int i = 0; i<max; i++) {
 			double residual =  (yVector.get(i)-e.valueOf(xVector.get(i)));
 			sum += residual*residual;
 		}
 		// we return the sum
 		return sum;
 	}
 	
 	
 	/**
 	 * The SStot for the fitting.
 	 */
 	public double SStot;
 
 	/**
 	 * The SSerr for the fitting.
 	 */
 	public double SSerr;
 
 	/**
 	 * A method to calculate the R^2 with respect to an equation.
 	 * 
 	 * @param e the equation.
 	 * @return the R^2
 	 */
 	public double coeffOfDetermination (YetiEquation e) {
 		// we first calculate the square residuals
 		SSerr = squaredResiduals(e);
 		double total = 0;
 		// we then calculate the mean of the ys
 		int max = xVector.size();
 		for (int i = 0; i<max; i++) {
 			total +=  yVector.get(i);
 		}		
 		double mean = total/max;
 		
 		// we then calculate the sum of the differences with the mean
 		SStot = 0.0;
 		for (int i = 0; i<max; i++) {
 			double diffWithMean =  (yVector.get(i)-mean);
 			SStot += diffWithMean*diffWithMean;
 		}		
		
		if ((SStot==0)&&(SSerr==0))
			return 1.0;
 		// we finally calculate the coefficient of determination and return it
 		return 1.0-(SSerr/SStot);
 	}
 	
 	/**
 	 * This method returns the first approximation of the dataSet as a Michaelis Menten equation
 	 * (simple hyperbol function of the form f(x)=Max*x/(K+x)).
 	 * 
 	 * The calculation uses the last y as a first approximation for Max and K as the first value such as f(K)/K<f'(0) as a first approximation of K.
 	 *  
 	 * 
 	 * @return
 	 */
 	public YetiMichaelisMentenEquation firstFitMichaelisMenten() {
 		double Max = 0.0;
 		double K = 0.0;
 		int i=0;
 		
 		// we simply assign Max
 		Max = yVector.get(yVector.size()-1);
 
 		// we approximate f'(0)
 		double fPrimeZero;
 		int size = xVector.size();
 
 		if (xVector.get(1)!=0)
 			fPrimeZero = yVector.get(1)/xVector.get(1);
 		else if(xVector.get(size-1)!=0&&yVector.get(size-1)!=0) {
 			fPrimeZero=yVector.get(size-1)/xVector.get(size-1);
 		}
 		else
 			fPrimeZero = 1.0;
 		
 		// finally we iterate through the values to find where f(K)/K = f'(0)/2
 		for (i = 2; i<size; i++) {
 			if ((yVector.get(i)/xVector.get(i))<fPrimeZero/2){
 				// this is another way of approximating Max, but it is not as good...
 				//Max = yVector.get(i)*2;
 				K = xVector.get(i);
 				break;
 			}
 		}
 		YetiLog.printDebugLog("Initial values: Max="+Max+" K="+K, this);
 		// we return the equation
 		return new YetiMichaelisMentenEquation(Max, K);
 		
 	}
 	
 	
 	/**
 	 * This method performs a simple hill climbing algorithm. It first calculate an approximation 
 	 * and then plays with the parameters of that equation to find the variant with the lowest squared residuals.
 	 * 
 	 * @return the best Michaelis-Menten fitting equation.
 	 */
 	public YetiMichaelisMentenEquation fitMichaelisMenten() {
 
		if (yVector.get(yVector.size()-1)==0.0) 
			return new YetiMichaelisMentenEquation(0,1.0);
 		// we first fit the equation with a broad evaluation
 		YetiMichaelisMentenEquation e=firstFitMichaelisMenten();
 		
 		// we use the current precision to refine our approximation
		int currentPrecision = -10;
 
 		boolean finished = false;
 		
 		// we iterate through the actual precisions
 		while (currentPrecision++<precision) {
 			// we iterate the hill-climbing approximation with the desired granularity
 			while (!finished) {
 				// we calculate the squared residuals of the current equation
 				double res = squaredResiduals(e);
				YetiLog.printDebugLog("Trying with: "+e, this);
 				// we calculate the neighbours for the equation
 				YetiMichaelisMentenEquation eM0 = new YetiMichaelisMentenEquation(e.getMax()+1.0/((double)(10^currentPrecision)),e.getK());
 				YetiMichaelisMentenEquation em0 = new YetiMichaelisMentenEquation(e.getMax()-1.0/((double)(10^currentPrecision)),e.getK());
 				YetiMichaelisMentenEquation e0M = new YetiMichaelisMentenEquation(e.getMax(),e.getK()+1.0/((double)(10^currentPrecision)));
 				YetiMichaelisMentenEquation e0m = new YetiMichaelisMentenEquation(e.getMax(),e.getK()-1.0/((double)(10^currentPrecision)));
 
 				// for each neighbour we compare with the current solution
 				// if the squared residuals is lower, we simply switch and continue in the loop
 				if (squaredResiduals(eM0)<res) {
 					e = eM0;
 					continue;
 				}
 				if (squaredResiduals(em0)<res) {
 					e = em0;
 					continue;
 				}
 				if (squaredResiduals(e0M)<res) {
 					e = e0M;
 					continue;
 				}
 				if (squaredResiduals(e0m)<res) {
 					e = e0m;
 					continue;
 				}
 				// if we reach this point no other neighbour is better for this precision and we 
 				// improve the precision.
 				finished = true;
 			}
 			YetiLog.printDebugLog("Precision: "+currentPrecision, this);
 
 		}
 		return e;
 		
 	}
 	
 	/**
 	 * We can also apply these algorithms by reading two series of csv values.
 	 * The for of the file is to have a x,y couple per line.
 	 * 
 	 * @param args the only argument should be the file to read.
 	 */
 	public static void main(String []args) {
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(args[0]));
 			String s = null;
 			ArrayList<Double> xVector = new ArrayList<Double>();
 			ArrayList<Double> yVector = new ArrayList<Double>();
 			while((s=br.readLine())!=null) {
 				String []values = s.split(",");
 				xVector.add(Double.parseDouble(values[0]));
 				yVector.add(Double.parseDouble(values[1]));
 			}
 			YetiDataSet ds=new YetiDataSet(xVector,yVector);
 			System.out.println("Data set read");
 			YetiMichaelisMentenEquation e = ds.fitMichaelisMenten();
 			System.out.println(e);
 			System.out.println("R^2 = "+ds.coeffOfDetermination(e));
 		} catch (FileNotFoundException e) {			
 			System.out.println("File not found: "+args[1]);
 		} catch (IOException e) {
 			// should not happen unless the file is not compliant with the intended format
 			e.printStackTrace();
 		}
 		
 		
 	}
 
 }
