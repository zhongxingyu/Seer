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
         if ( perbandmults ) b = band;
         if ( permodemults ) m = mode;
         if (le.getRcvd().getMultiplierField().length() == 3 &&
             ! le.getRcvd().getMultiplierField().equalsIgnoreCase("MAR")) {
             return (b + " " + m + " " + "ks");
         } else {
             return (super.makeMultKey(le, band, mode));
         }
     }
}
