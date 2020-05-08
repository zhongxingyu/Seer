 package com.github.adeshmukh.ps4j.cli;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Strings.padStart;
 import static com.google.common.collect.Maps.newTreeMap;
 import static com.google.common.collect.Maps.uniqueIndex;
 import static java.lang.Math.max;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.SortedMap;
 
 import com.github.adeshmukh.ps4j.Measure;
 import com.github.adeshmukh.ps4j.Record;
<<<<<<< HEAD
 import com.google.common.base.Function;
 import com.google.common.collect.FluentIterable;
=======
import com.google.common.collect.ImmutableList;
>>>>>>> 142b46c4c9144d553494fc1ed6a78ca1dbf8cd00
 import com.google.common.collect.Iterables;
 
 public class DisplayRecords implements Iterable<String[]> {
 
     private static final Iterator<String[]> EMPTY_ITERATOR = Collections.<String[]> emptyList().iterator();
 
     private static final char PADCHAR = ' ';
 
     private static final Function<Measure, String> MEASURE_NAME = new Function<Measure, String>() {
 
         @Override
         public String apply(Measure input) {
             return input.getMetric().getName();
         }
     };
 
     private int numRecords;
     private int numCols;
     private int[] colWidths;
     private List<String[]> displayValues;
     private Iterable<String> orderedKeys;
 
     DisplayRecords(Iterable<Record> records) {
         if (Iterables.isEmpty(records)) {
             return;
         }
         // get the measures in the first record to size the record
         Iterable<? extends Measure> canonicalMeasures = records.iterator().next().getMeasures();
         orderedKeys = FluentIterable.from(canonicalMeasures).transform(MEASURE_NAME);
         numCols = Iterables.size(orderedKeys);
 
         colWidths = new int[numCols];
         numRecords = Iterables.size(records);
         displayValues = new ArrayList<String[]>(numRecords);
 
         // populate colWidths
         for (Record record : records) {
             SortedMap<String, Measure> measures = newTreeMap();
             measures.putAll(uniqueIndex(record.getMeasures(), MEASURE_NAME));
             int i = 0;
             for (String key : orderedKeys) {
                 colWidths[i] = max(max(colWidths[i], key.length()), measures.get(key).getDisplayValue().length());
                 i++;
             }
         }
 
         // populate col headers in displayValues
         String[] headers = new String[numCols];
         int i = 0;
         for (String key : orderedKeys) {
             headers[i] = padAround(key, colWidths[i] + 1, PADCHAR);
             i++;
         }
         displayValues.add(headers);
 
         // populate vals in displayValues
         for (Record record : records) {
             SortedMap<String, Measure> measures = newTreeMap();
             measures.putAll(uniqueIndex(record.getMeasures(), MEASURE_NAME));
             String[] vals = new String[numCols];
             int j = 0;
             for (String key : orderedKeys) {
                 vals[j] = padStart(measures.get(key).getDisplayValue(), colWidths[j] + 1, PADCHAR);
                 j++;
             }
             displayValues.add(vals);
         }
     }
 
     private static String padAround(String s, int minLength, char padchar) {
         checkNotNull(s);
         if (s.length() >= minLength) {
             return s;
         }
         int len = s.length();
         int rpad = (minLength - len) / 2;
         int lpad = (minLength - len - rpad);
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < lpad; i++) {
             sb.append(padchar);
         }
         sb.append(s);
         for (int i = 0; i < rpad; i++) {
             sb.append(padchar);
         }
         return sb.toString();
     }
 
     @Override
     public Iterator<String[]> iterator() {
         return displayValues == null ? EMPTY_ITERATOR : displayValues.iterator();
     }
 }
