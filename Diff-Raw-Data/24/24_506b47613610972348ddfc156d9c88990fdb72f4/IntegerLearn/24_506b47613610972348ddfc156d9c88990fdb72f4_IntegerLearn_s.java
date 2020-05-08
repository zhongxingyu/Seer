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
        System.out.println(Integer.highestOneBit(i));
     }
 }
