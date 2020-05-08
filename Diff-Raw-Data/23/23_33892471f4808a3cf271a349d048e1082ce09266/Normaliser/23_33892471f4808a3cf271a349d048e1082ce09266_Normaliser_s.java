 //
// Author: Mark Triggs <mtriggs@nla.gov.au>
 //
 
 import java.io.*;
 import java.util.*;
 import java.util.regex.*;
 import au.gov.nla.util.*;
 
 
 
 class Normaliser
 {
     protected DiacriticStripper ds = new DiacriticStripper ();
 
     protected Pattern junkregexp =
         Pattern.compile ("\\([^a-z0-9\\p{L} ]\\)");
 
     public String normalise (String s)
     {
         return junkregexp.matcher (ds.fix (s
                                            .toLowerCase ()
                                            .replace ("(", "")
                                            .replace (")", "")
                                            .replace ("-", "")))
             .replaceAll ("")
             .replaceAll (" +", " ");
     }
 
 }
 
