 import java.util.ArrayList;
 
 public class Statistics {
 	public Statistics(){
 	}
 	
 	//Ordinary least squares determines functional dependency between two variables
 	//http://www.weibull.com/DOEWeb/simple_linear_regression_analysis.htm#Calculation%20of%20the%20Fitted%20Line%20Using%20Least%20Square%20Estimates
 	public double[] determineRegressionBetweenTwoVariables(ArrayList<Double> first, ArrayList<Double> second)
 	{
 		double[] res = new double[2];
 		double avX = 0.0, avY = 0.0; 
 		double sumX = 0.0, sumXY = 0.0, sumY = 0.0;
 		double XAv = 0.0;
 		int n  = Math.min(first.size(), second.size());
		
 		for(int i = 0; i < n; i++){
			sumX+=first.get(i); // sumata na wsi4ki ot purwata
 		}
		avX = sumX/n; //sredno-aritmeti4no na wsi4ki purwata
 		
 		for(int i =0; i <n; i++){
 			sumY+= second.get(i);
 			sumXY+= second.get(i) * first.get(i);
 			XAv+= (first.get(i) - avX) * (first.get(i) - avX); 
 		}
 		
		avY = sumY/n;
		
 		res[0] = (sumXY - (sumX*sumY)/n)/XAv;
 		res[1] = avY - res[0]* avX;
 		return res;
 	}
 	
 	//returns the correlation between two double arrays
 	public double Correlation (ArrayList<Double> first, ArrayList<Double> second)
 	{
 		double avX = 0.0, avY = 0.0; 
 
 		double up = 0.0;
 		double f = 0.0, s = 0.0;
 		int n  = Math.min(first.size(), second.size());
 		for(int i =0; i <  n; i++){
 			avX+=first.get(i);
 			avY+=second.get(i);
 		}
 		
		avX/=n; avY/=n;
 		for(int i =0; i < n; i++){
 			up+= (first.get(i)- avX)* (second.get(i)- avY);
 			f+=(first.get(i)- avX)*(first.get(i)- avX);
 			s+=(second.get(i)- avY)*(second.get(i)- avY);
 		}
 		return up / Math.sqrt(f*s);
 	}
 	                                         
 }
 
