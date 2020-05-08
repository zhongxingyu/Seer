 package org.fit.cvut.mvi;
 
 import java.util.ArrayList;
 import java.util.List;
 
import org.fit.cvut.mvi.model.Function;
 import org.fit.cvut.mvi.model.functions.Addition;
 
 public class Main {
 
     /**
      * @param args
      */
     public static void main(String[] args) {
         Function add = new Addition();
         List<String> arg = new ArrayList<>();
 
         arg.add("1.2");
         arg.add("6.3");
         System.out.println(add.code(arg));
     }
 
 }
