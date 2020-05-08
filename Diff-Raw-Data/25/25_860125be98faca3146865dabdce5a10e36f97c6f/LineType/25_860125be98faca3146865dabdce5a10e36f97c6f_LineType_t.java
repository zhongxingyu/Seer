 package com.bitresolution.ledger.core.files;
 
 import com.google.common.base.Predicate;
 import org.apache.commons.lang3.StringUtils;
 
 import java.util.Arrays;
 
 import static com.bitresolution.ledger.core.files.LineType.ColumnMarkersPredicate.isColumnMarkers;
 import static com.bitresolution.ledger.core.files.LineType.StartsWithPredicate.startsWith;
 
 enum LineType {
     PERIOD_OF_REPORT(startsWith("CONFORMED PERIOD OF REPORT:")),
     REPORT_FILING_DATE(startsWith("FILED AS OF DATE:")),
     COLUMN_MARKERS(isColumnMarkers()),
     SKIPPABLE(null);
 
     private final Predicate<String> matcher;
 
     LineType(Predicate<String> matcher) {
         this.matcher = matcher;
     }
 
     boolean matches(String line) {
         return matcher.apply(line);
     }
 
     public static LineType of(String line) {
         if(PERIOD_OF_REPORT.matches(line)) {
             return PERIOD_OF_REPORT;
         }
         else if(REPORT_FILING_DATE.matches(line)) {
             return REPORT_FILING_DATE;
         }
        else if(COLUMN_MARKERS.matches(line)) {
            return COLUMN_MARKERS;
        }
         else {
             return SKIPPABLE;
         }
     }
 
     public static class StartsWithPredicate implements Predicate<String> {
         private final String prefix;
 
         public StartsWithPredicate(String prefix) {
             this.prefix = prefix;
         }
 
         @Override
         public boolean apply(String line) {
             return line.startsWith(prefix);
         }
 
         public static StartsWithPredicate startsWith(String prefix) {
             return new StartsWithPredicate(prefix);
         }
     }
 
     public static class ColumnMarkersPredicate implements Predicate<String> {
         private static final String[] MARKERS = {
                 "<S>", "<C>", "<C>", "<C>", "<C>", "<C>", "<C>", "<C>", "<C>", "<C>", "<C>", "<C>"};
 
         @Override
         public boolean apply(String line) {
             String[] markers = line.split("\\s+");
             return Arrays.equals(MARKERS, markers);
         }
 
         public static ColumnMarkersPredicate isColumnMarkers() {
             return new ColumnMarkersPredicate();
         }
     }
 }
