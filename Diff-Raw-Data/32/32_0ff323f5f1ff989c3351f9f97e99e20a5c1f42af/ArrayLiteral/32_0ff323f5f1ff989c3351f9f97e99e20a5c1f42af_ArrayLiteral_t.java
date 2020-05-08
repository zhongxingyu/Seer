 package com.josephcrawley.Spitfire.entities;
 
 import java.util.*;
 
 import com.josephcrawley.util.Log;
 
 /**
  * An expression directly constructing an array, such as
  * <ul>
  * <li>new int[]{35, 22, 19}
  * <li>new char[]{'3', '%'}
  * <li>new string[]{}
  * </ul>
  */
public class ArrayLiteral extends Expression {
 
 
     private List<Expression> args;
 
    public ArrayLiteral(List<Expression> args) {
         this.args = args;
     }

    public ArrayLiteral(ArrayList<Expression> args) {
         this.args = args;
     }
     public String getTypename() {
        if(args.size() <= 0){
            return "<arbitrary array>";
        }
        else{ return args.get(0).getType().getName();}
     }
 
     public List<Expression> getArgs() {
         return args;
     }
 
    public void analyze(Log log, SymbolTable table, Subroutine owner,
            boolean inLoop) {
        // TODO Auto-generated method stub

    }

 }
