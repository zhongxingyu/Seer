 package com.hsgui.lang;
 
 /**
  * Created with IntelliJ IDEA.
  * User: hsgui
  * Date: 13-10-16
  * Time: 下午9:12
  * To change this template use File | Settings | File Templates.
  */
 public class IntegerLearn {
 
     public static void main(String[] args){
         IntegerLearn learn = new IntegerLearn();
         learn.learnHighestOneBit();
     }
 
     public void learnHighestOneBit(){
         //when i < 0, highestOneBit always return Integer.MIN_VALUE
         //explain: http://blog.csdn.net/jessenpan/article/details/9617749
         //always considering the highest one bit in i.
         int i = -1;
         System.out.println("highestOneBit(-1) = " + Integer.highestOneBit(i));
         System.out.println("highestOneBit(-4) = " + Integer.highestOneBit(-4));
         System.out.println("highestOneBit(6) = " + Integer.highestOneBit(6));
 
         System.out.println("minPower2(int i) is very similar to Integer.highestOneBit");
         System.out.println("minPower2(5) = " + IntegerLearn.minPower2(5));
         System.out.println("minPower2(8) = " + IntegerLearn.minPower2(8));
         System.out.println("minPower2(0) = " + IntegerLearn.minPower2(0));
         System.out.println("minPower2(-3) = " + IntegerLearn.minPower2(-3));
     }
 
     //very similar to Integer.highestOneBit(int i);
     // 3 -- minPower2 -- 4
     // 4 -- minPower2 -- 4
     // 5 -- minPower2 -- 8
    //also cite: http://my.oschina.net/shaorongjie/blog/132543
     public static int minPower2(int i){
         i -= 1;
         i |= (i >> 1);
         i |= (i >> 2);
         i |= (i >> 4);
         i |= (i >> 8);
         i |= (i >> 16);
         return i + 1;
     }
 }
