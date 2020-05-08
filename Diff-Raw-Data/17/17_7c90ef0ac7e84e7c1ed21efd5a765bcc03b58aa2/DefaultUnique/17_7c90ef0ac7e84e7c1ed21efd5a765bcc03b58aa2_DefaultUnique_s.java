 /*
   This file is licensed through the GNU public License.  Please read it.
   Basically you can modify this code as you wish, but you need to distribute
   the source code for all applications that use this code.
 
   I'd appreciate comments/suggestions on the code schewe@tcfreenet.org
 */
 package org.tcfreenet.schewe.utils;
 
 /**
   Default implementation of hte {@link Unique} interface.  Can be used as a
    delegate.
 **/
 public class DefaultUnique implements Unique {
 
  static private long _nextUID = Long.MIN_VALUE;

  public long getUID() {
     if(_nextUID == Long.MAX_VALUE) {
       throw new RuntimeException("You have run out of Unique IDs!");
     }
    return _nextUID++;
   }
   
 
 }
