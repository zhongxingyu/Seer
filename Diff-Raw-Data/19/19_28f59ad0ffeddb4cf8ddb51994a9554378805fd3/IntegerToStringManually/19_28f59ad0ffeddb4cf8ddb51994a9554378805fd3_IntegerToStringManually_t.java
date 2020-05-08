 
package org.codemetrics.testFiles.integerToStringManually;
 
 import java.util.Stack;
 
 /**
  *
  * @author Johanna 
  */
 public class IntegerToStringManually {
     private Stack stack = new Stack();
     
     public String integerToString(int number){
         while(number>=1){
            stack.push(number%10);
             number = number/10;
         }
        //int a = 5;
         return "hello";
     }
     
  // public String toStringWithStack(){
  //       String string = "";
  //       while(!stack.empty())
  //           string+= stack.pop();
  //       return string.trim();
  //   }   
 }
