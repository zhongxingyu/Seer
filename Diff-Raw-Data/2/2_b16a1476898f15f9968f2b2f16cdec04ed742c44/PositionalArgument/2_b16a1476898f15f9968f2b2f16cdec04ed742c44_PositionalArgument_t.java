 package com.zetapuppis.arguments;
 
 /**
  * A positional keyword. Positional keywords are arguments whose value
  * is identified by its position in the argument list.
  *
  * Positional keyword indices start from 1, that is, the first argument
  * corresponds to the first positional keyword, the second argument to
  * the second positional keyword and so on.
  */
 public class PositionalArgument implements Comparable<PositionalArgument>, ArgumentItem {
     private final String mName;
     private final int mPosition;
 
     public PositionalArgument(final String name, final int position) {
         mName = name;
         mPosition = position;
     }
 
     @Override
     public String getName() {
         return mName;
     }
 
     public int getPosition() {
         return mPosition;
     }
 
     @Override
     public int compareTo(PositionalArgument positionalArgument) {
        return mPosition - positionalArgument.getPosition();
     }
 }
