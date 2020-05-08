 package main1;
 
 public class Pow_x_n {
 	public static  double solve(double x,int n){
 		if(n==1){
 			return x;
 		}
 		else if(n==0){
 			return 1;
 		}
 		if(n%2==0){
 			double ans=solve(x,n/2);
 			return ans*ans;	
 		}
 		else{
 			double ans=solve(x,n/2);
 			return ans*ans*x;
 		}
 	}
 	
 	
     public double pow(double x, int n) {
    	if(abs(x-0)<0.0000001&&n<0)
    	     invalid
     	//n 负数
     	if(n<0){
     		double ans= solve(x,n);
     		return 1/ans;
     	}
     	else
         return solve(x,n);
     }
 
 }
