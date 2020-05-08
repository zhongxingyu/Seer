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
        //remember to reset!!!
         int carry = 0;
         int i = 0;
         for(; i<maxSize || carry != 0; ++i){
             int tmp = carry;
            carry = 0;
            //remember to convert from int to char
             if(i < srcLen)
                tmp +=src[srcLen-1-i]-'0';
             if(i < destLen)
                tmp +=dest[destLen-1-i]-'0';
            if(tmp >1) {
                 carry = 1;
                tmp -=2;
            }
            sum[i]= (char)(tmp - 0 + '0');
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
