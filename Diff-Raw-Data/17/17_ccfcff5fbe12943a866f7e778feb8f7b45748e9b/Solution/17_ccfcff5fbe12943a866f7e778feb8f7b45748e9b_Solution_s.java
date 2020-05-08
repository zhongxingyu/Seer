 public class Solution {
     public String addBinary(String a, String b) {
         if(a == null)
             return b;
         if(b == null)
             return a;
         char [] src = a.toCharArray();
         char [] dest = b.toCharArray();
         int srcLen = a.length();
         int destLen = b.length();
         int maxSize = (srcLen > destLen)?(srcLen):(destLen);
         char [] sum = new char[maxSize+1];
         int carry = 0;
         int i = 0;
         for(; i<maxSize || carry != 0; ++i){
             int tmp = carry;
             if(i < srcLen)
                tmp +=src[srcLen-1-i];
             if(i < destLen)
                tmp +=dest[destLen-1-i];
            if(tmp >1)
                 carry = 1;
            sum[i]= tmp - 0 + '0';
         }
         --i;
         for(int j = 0; j<i; j++,i--){
             char tmp = sum[j];
             sum[j] = sum[i];
             sum[i] = tmp;
         }
         return new String(sum);
     }
 }
