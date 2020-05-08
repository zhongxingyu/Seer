public class Solution {
    public double pow(double x, int n) {
        
        if(x == 0)
            return 0;

        boolean isNeg = false;
        if( n < 0) {
            isNeg = true;
            n *= -1;
        }
        double val = 1; 
        for(int i = 0; i<n; ++i)
            val *=x;
        if(isNeg)
            val = 1/val;
        return val;
    }
 }

