 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package algs;
 
 import java.util.HashMap;
 
 /**
  *
  * @author rob
  */
 public class BaseConversion{
     HashMap<Character, Integer> mapping = new HashMap<Character, Integer>();
     char arr[] = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
 
    public BaseConversion() {
         mapping.put('0', 0);
         mapping.put('1', 1);
         mapping.put('2', 2);
         mapping.put('3', 3);
         mapping.put('4', 4);
         mapping.put('5', 5);
         mapping.put('6', 6);
         mapping.put('7', 7);
         mapping.put('8', 8);
         mapping.put('9', 9);
         mapping.put('A', 10);
         mapping.put('B', 11);
         mapping.put('C', 12);
         mapping.put('D', 13);
         mapping.put('E', 14);
         mapping.put('F', 15);
     }
 
     public String betweenBases(String input,int b1,int b2){
         int dec = ToDecimal(input, b1);
         return toBase(dec, b2);
     }
 
     public String toBase(int decVal,int b){
         StringBuilder sb = new StringBuilder();
         while(decVal>0){
             sb.append(arr[decVal%b]);
             decVal = decVal/b;
         }
         sb.reverse();
         return sb.toString();
     }
 
     public int ToDecimal(String val, int b){
         int multiplier = 1;
         int result = 0;
 
         for(int i = val.length()-1;i>=0;i--){
             result += mapping.get(val.charAt(i)) * multiplier;
             multiplier *=b;
         }
         return result;
     }
 
 
     public int ToDecimal(int n,int b){
         int multiplier = 1;
         int result = 0;
 
         while(n>0)
         {
             result += (n%10)*multiplier;
             n/=10;
             multiplier *=b;
         }
         return result;
     }
 
     public static void main(String args[]){
 //        System.out.println(123%10);
 //        System.out.println(new BaseConversation().ToDecimal(1101, 2));
         BaseConversion baseConversion = new BaseConversion();
 //        System.out.println(baseConversation.betweenBases("1000011011000100", 2, 16));
         System.out.println(baseConversion.betweenBases("8", 10, 2));
     }
 }
 
 
 
 
 
 
 
 
 
 
 
 
