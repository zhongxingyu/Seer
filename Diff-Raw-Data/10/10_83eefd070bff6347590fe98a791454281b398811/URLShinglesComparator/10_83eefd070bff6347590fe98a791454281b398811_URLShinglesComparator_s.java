 package ru.ifmo.ctddev.larionov.bach.comparator;
 
 import ru.ifmo.ctddev.larionov.bach.common.site.ISite;
 import ru.ifmo.ctddev.larionov.bach.common.site.WeightedPair;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import static java.lang.Math.log;
 import static java.lang.Math.min;
 
 /**
  * User: Oleg Larionov
  * Date: 06.05.13
  * Time: 23:33
  */
 public class URLShinglesComparator implements IComparator {
 
     private static final int DEFAULT_SHINGLES_COUNT = 2;
     private static final int MAX_SHINGLES_FROM_SITE = 100;
 
     @Override
     public List<WeightedPair> createPairs(Iterable<ISite> list) {
         Map<String, Set<ISite>> data = getShinglesData(list);
 
         Map<WeightedPair, Double> similarity = calculateSimilarity(data);
 
         return normalizeSimilarity(similarity);
     }
 
     private List<WeightedPair> normalizeSimilarity(Map<WeightedPair, Double> similarity) {
         List<WeightedPair> result = new ArrayList<>();
         for (Map.Entry<WeightedPair, Double> entry : similarity.entrySet()) {
             WeightedPair pair = entry.getKey();
             double value = entry.getValue();
 
             long firstPageCount = pair.getFirstHost().getLinks().size();
             long secondPageCount = pair.getSecondHost().getLinks().size();
             value /= 1 + 0.1 * (log(firstPageCount) + log(secondPageCount));
 
             pair.setWeight(value);
 
             result.add(pair);
         }
 
         return result;
     }
 
     private Map<WeightedPair, Double> calculateSimilarity(Map<String, Set<ISite>> data) {
         Map<WeightedPair, Double> similarity = new HashMap<>();
 
         for (Map.Entry<String, Set<ISite>> entry : data.entrySet()) {
             if (entry.getValue().size() > 1) {
                 ISite[] hosts = entry.getValue().toArray(new ISite[0]);
                 for (int i = 0; i < hosts.length - 1; i++) {
                     for (int j = i + 1; j < hosts.length; j++) {
                         WeightedPair pair = new WeightedPair(hosts[i], hosts[j]);
                         if (!similarity.containsKey(pair)) {
                             similarity.put(pair, 0d);
                         }
 
                         double value = similarity.get(pair);
                         similarity.put(pair, value + (1.0 / hosts.length));
                     }
                 }
             }
         }
 
         return similarity;
     }
 
     private Map<String, Set<ISite>> getShinglesData(Iterable<ISite> list) {
         Map<String, Set<ISite>> data = new HashMap<>();
 
         for (ISite site : list) {
             addShingles(data, site);
         }
 
         return data;
     }
 
     private void addShingles(Map<String, Set<ISite>> data, ISite site) {
         Map<String, Long> shinglesCount = new HashMap<>();
 
         calculateShingles(site, shinglesCount);
 
         List<Map.Entry<String, Long>> entries = sortShinglesByFrequency(shinglesCount);
 
         getMostFrequentedShingles(data, site, entries);
     }
 
     private void calculateShingles(ISite site, Map<String, Long> shinglesCount) {
         for (URL page : site.getLinks()) {
             String[] urlParts = getParts(page);
             String[] shingles = getShingles(urlParts, DEFAULT_SHINGLES_COUNT);
 
             for (String shingle : shingles) {
                 if (!shinglesCount.containsKey(shingle)) {
                     shinglesCount.put(shingle, 0l);
                 }
 
                 long currentValue = shinglesCount.get(shingle);
                 shinglesCount.put(shingle, currentValue + 1);
             }
         }
     }
 
     private List<Map.Entry<String, Long>> sortShinglesByFrequency(Map<String, Long> shinglesCount) {
         Set<Map.Entry<String, Long>> entries = shinglesCount.entrySet();
         List<Map.Entry<String, Long>> list = new ArrayList<>(entries);
 
         Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {
             @Override
             public int compare(Map.Entry<String, Long> o1, Map.Entry<String, Long> o2) {
                 if (o1.getValue() < o2.getValue()) {
                     return 1;
                 } else if (o1.getValue() > o2.getValue()) {
                     return -1;
                 } else {
                     return 0;
                 }
             }
         });
 
         return list;
     }
 
     private void getMostFrequentedShingles(Map<String, Set<ISite>> data, ISite site, List<Map.Entry<String, Long>> entries) {
         int shinglesCount = min(MAX_SHINGLES_FROM_SITE, entries.size() / 4);
         for (int i = 0; i < shinglesCount; i++) {
             String shingle = entries.get(i).getKey();
             if (!data.containsKey(shingle)) {
                 data.put(shingle, new HashSet<ISite>());
             }
 
             data.get(shingle).add(site);
         }
     }
 
     private String[] getParts(URL page) {
         String url = page.getHost() + page.getPath();
         url = url.replaceAll("[0-9]+", "\\*");
 
        return url.split("[./]");
     }
 
     private String[] getShingles(String[] urlParts, int partsInShingle) {
         int shinglesCount = urlParts.length - partsInShingle + 1;
         String[] shingles = new String[shinglesCount];
 
         for (int i = 0; i < shinglesCount; i++) {
             StringBuffer buffer = new StringBuffer();
             for (int j = 0; j < partsInShingle; j++) {
                 buffer.append(urlParts[i + j]);
                 buffer.append("_");
             }
             buffer.append(i);
 
             shingles[i] = buffer.toString();
         }
 
         return shingles;
     }
 }
