 package org.freecode.irc.event;
 
 import org.freecode.irc.event.internal.RawIrcListener;
 
 /**
  * User: Shivam
  * Date: 16/06/13
  * Time: 23:12
  */
public abstract class NumericListener implements RawIrcListener {
     public abstract int getNumeric();
 
     @Override
     public boolean qualifies(final String raw) {
         String[] parts = raw.split(" ");
         return parts.length > 2 && parts[1].matches("\\d+") && Integer.parseInt(parts[1]) == getNumeric();
     }
 }
