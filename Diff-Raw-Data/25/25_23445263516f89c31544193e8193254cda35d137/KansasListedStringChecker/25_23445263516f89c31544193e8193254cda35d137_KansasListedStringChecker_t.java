 /* A checker that recognizes that counties don't count towards mult points for residents. */
 package com.loukides.jl.checkers;
 import com.loukides.jl.jl.*;
 import com.loukides.jl.gadgets.*;
 import com.loukides.jl.util.*;
 
 import java.util.*;
 import java.io.*;
 import javax.swing.*;
 import javax.swing.table.*;
 
 public class KansasListedStringChecker extends ListedStringChecker {
     protected String makeMultKey(LogEntry le, String band, String mode) {
         String b = "";
         String m = "";
        // System.out.println("makemultkey input" + le.getRcvd().getMultiplierField().toLowerCase());
         if ( perbandmults ) b = band;
         if ( permodemults ) m = mode;
         if (le.getRcvd().getMultiplierField().length() == 3 &&
             ! le.getRcvd().getMultiplierField().equalsIgnoreCase("MAR")) {
            // System.out.println("ks makeMultKey " + le.getRcvd().getMultiplierField() + " " + b + " " + m);
             return (b + " " + m + " " + "ks");
         } else {
             return (super.makeMultKey(le, band, mode));
         }
     }
    public void addEntry(LogEntry le) {
        String k = makeMultKey(le);
        super.addEntry(le);
        // System.out.println("in addentry " + k);
        if (k.endsWith(" ks")) {
            // Remove all the other counties
            Iterator iter = mults.keySet().iterator();
            while (iter.hasNext()) {
                String thisItem = (String) iter.next();
                if (thisItem.length() == 3 &&
                    ! thisItem.equalsIgnoreCase("MAR")) {
                    updateDisplay(le, (String) mults.get(thisItem));
                }
            }
        }
    }

}

