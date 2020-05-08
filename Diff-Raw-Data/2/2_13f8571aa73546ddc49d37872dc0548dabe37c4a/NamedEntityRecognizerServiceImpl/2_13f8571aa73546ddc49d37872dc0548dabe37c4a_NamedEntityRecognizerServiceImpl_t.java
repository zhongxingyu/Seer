 package com.imaginea.ner.service.impl;
 
 import com.google.common.collect.Lists;
 import com.imaginea.ner.service.NamedEntityRecognizerService;
 import org.apache.commons.lang.math.NumberUtils;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.KeyValue;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.log4j.Logger;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.*;
 
 
 public class NamedEntityRecognizerServiceImpl implements NamedEntityRecognizerService {
 
     private static Logger logger = Logger.getLogger(NamedEntityRecognizerServiceImpl.class);
 
     private static final String HBASE_MASTER_DETAILS = "localhost:60000";
     private static final String WORD_COUNT_TABLE_NAME = "WordCount";
     private static final String WORD_URL_TABLE_NAME = "WordUrl";
     private static final String COLUMN_FAMILY = "cf";
     private static final String COUNT_QUALIFIER = "count";
     private static final String URLS_QUALIFIER = "urls";
 
     private static final int MAX_COMBINATION_WORDS = 2;
     private static final int INTERVAL_SIZE = 100;
 
     private static Set<String> stopWords = new HashSet<String>();
     private static Map<Long, Long> countVsNumberOfWords = new TreeMap<Long, Long>();
     private static Map<Long, Long> countVsAggregateSum = new TreeMap<Long, Long>();
     private static Long totalWords;
 
     static {
         initStopWords();
         initCountVsNumberOfWordsMap();
         initCountVsAggregateSumMap();
     }
 
     private static synchronized void initStopWords() {
         InputStream inputStream = NamedEntityRecognizerServiceImpl.class.getClassLoader().getResourceAsStream("stop-words.txt");
         if(inputStream != null) {
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             String line;
             try {
                 while ((line = reader.readLine()) != null) {
                     if(!line.isEmpty()) {
                         stopWords.add(line.trim().toLowerCase());
                     }
                 }
             } catch (IOException e) {
                 logger.error("Couldn't read Stop Words File", e);
             } finally {
                 if(reader != null) {
                     try {
                         reader.close();
                     } catch (IOException e) {
                         logger.error("Failed to close the reader of stop words text", e);
                     }
                 }
             }
         }
     }
 
     private static void initCountVsNumberOfWordsMap() {
         InputStream inputStream = NamedEntityRecognizerServiceImpl.class.getClassLoader().getResourceAsStream("countVsNumberOfWords.txt");
         if(inputStream != null) {
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             String line;
             try {
                 while ((line = reader.readLine()) != null) {
                     if(!line.isEmpty()) {
                         String[] split = line.split("\t");
                         if(split.length == 2) {
                             Long count = Long.valueOf(split[0]);
                             if(count%INTERVAL_SIZE == 0) {
                                 Long noOfWords = new Double((Math.log(Long.valueOf(split[1]))+2)*1000).longValue();
                                 countVsNumberOfWords.put(count, noOfWords);
                             }
                         }
                     }
                 }
             } catch (IOException e) {
                 logger.error("Couldn't read countVsNumberOfWords text File", e);
             } finally {
                 if(reader != null) {
                     try {
                         reader.close();
                     } catch (IOException e) {
                         logger.error("Failed to close the reader of countVsNumberOfWords text file", e);
                     }
                 }
             }
         }
     }
 
     private static void initCountVsAggregateSumMap() {
         if(countVsNumberOfWords != null) {
             Map<Long, Long> countVsNumberOfWordsInReverseOrder = new TreeMap<Long, Long>(new Comparator<Long>() {
                 @Override
                 public int compare(Long o1, Long o2) {
                     return o1.compareTo(o2);
                 }
             });
             countVsNumberOfWordsInReverseOrder.putAll(countVsNumberOfWords);
             long sum =0;
             for(Map.Entry<Long,Long> entry : countVsNumberOfWordsInReverseOrder.entrySet()) {
                 Long count = entry.getKey();
                 sum += entry.getValue();
                 countVsAggregateSum.put(count, sum);
             }
             totalWords = sum;
         }
     }
 
     @Override
     public List<String> getUrls(String word) {
        Get get = new Get(word.toLowerCase().getBytes());
         get.addColumn(COLUMN_FAMILY.getBytes(), URLS_QUALIFIER.getBytes());
         HTable hTable = getWordUrlHTable();
         Result result;
         try {
             result = hTable.get(get);
         } catch (IOException e) {
             logger.error("Couldn't get the content from HTable " + WORD_URL_TABLE_NAME, e);
             throw new RuntimeException("Couldn't get the content from HTable " + WORD_URL_TABLE_NAME, e);
         }
         List<KeyValue> resultList = result.list();
         if (resultList != null && !resultList.isEmpty()) {
             String urls = new String(resultList.iterator().next().getValue());
             String[] urlsSplit = urls.split(",");
             return Lists.newArrayList(urlsSplit);
         }
         return Collections.emptyList();
     }
 
     @Override
     public Long getWordCount(String word) {
         Map<String, Long> wordCount = getWordCountInList(word);
         Long count = wordCount.get(word.trim());
         return (count ==null)?0: count;
     }
 
     @Override
     public Map<String, Long> getWordCountInParagraph(String paragraph) {
         String[] words = paragraph.split("[ .,!()\\[\\]:]");
         Set<String> aggregateList = new HashSet<String>();
         List<String> tempList = new ArrayList<String>();
         for(String word : words) {
             word = word.trim();
             if(!isStopWord(word)) {
                 tempList.add(word);
             } else {
                 if(!tempList.isEmpty()) {
                     aggregateList.addAll(getAggregateList(tempList));
                     tempList.clear();
                 }
             }
         }
         if(!tempList.isEmpty()) {
             aggregateList.addAll(getAggregateList(tempList));
         }
         String[] updatedWords = aggregateList.toArray(new String[]{});
         return getWordCountInList(updatedWords);
     }
 
     @Override
     public Map<Long, Long> getCountVsNumberOfWordsMap() {
         return countVsNumberOfWords;
     }
 
     @Override
     public Map<Long, Long> getCountVsAggregateSumMap() {
         return countVsAggregateSum;
     }
 
     @Override
     public Long getTotalWords() {
         return totalWords;
     }
 
     private Set<String> getAggregateList(List<String> tempList) {
         Set<String> aggregateList = new HashSet<String>();
         int tempListSize = tempList.size();
         for(int currentSize=1;currentSize<=Math.min(MAX_COMBINATION_WORDS, tempListSize); currentSize++) {
             for(int startIndex=0;startIndex<=tempListSize-currentSize;startIndex++) {
                 StringBuilder stringBuilder = new StringBuilder(tempList.get(startIndex));
                 for(int currentIndex=startIndex+1;currentIndex<startIndex+currentSize;currentIndex++) {
                     stringBuilder.append(" ").append(tempList.get(currentIndex));
                 }
                 aggregateList.add(stringBuilder.toString());
             }
         }
         return aggregateList;
     }
 
     private boolean isStopWord(String word) {
         word = word.toLowerCase();
         if(word.length() == 1) {
             char c = word.charAt(0);
             if (!Character.isUpperCase(c) && !Character.isLowerCase(c)) {
                 return true;
             }
         }
         return stopWords.contains(word) || NumberUtils.isNumber(word);
     }
 
     private Map<String, Long> getWordCountInList(String... words) {
         Set<String> uniqueWordsSet = new HashSet<String>();
         for(String word : Arrays.asList(words)) {
             String trimmedWord = word.trim();
             if(trimmedWord.length() != 0) {
                 uniqueWordsSet.add(trimmedWord);
             }
         }
         Map<String, Long> wordCount = new HashMap<String, Long>();
         if(uniqueWordsSet != null && !uniqueWordsSet.isEmpty()) {
             Result[] resultArray = getResult(uniqueWordsSet);
             if (resultArray != null && resultArray.length != 0) {
                 for (Result result : resultArray) {
                     List<KeyValue> resultList = result.list();
                     if (resultList != null && !resultList.isEmpty()) {
                         String key = new String(result.getRow());
                         wordCount.put(key,Long.valueOf(new String(resultList.iterator().next().getValue())));
                     }
                 }
             }
         }
         return wordCount;
     }
 
     private Result[] getResult(Set<String> words) {
         try {
             List<Get> gets = new ArrayList<Get>(words.size());
             for(String word : words) {
                 Get get = new Get(word.getBytes());
                 get.addColumn(COLUMN_FAMILY.getBytes(), COUNT_QUALIFIER.getBytes());
                 gets.add(get);
             }
             HTable hTable = getWordCountHTable();
             return hTable.get(gets);
         } catch (IOException e) {
             logger.error("Couldn't get the content from HTable " + WORD_COUNT_TABLE_NAME, e);
             throw new RuntimeException("Couldn't get the content from HTable " + WORD_COUNT_TABLE_NAME, e);
         }
     }
 
     private HTable getWordCountHTable() {
         return getHTable(WORD_COUNT_TABLE_NAME);
     }
 
     private HTable getWordUrlHTable() {
         return getHTable(WORD_URL_TABLE_NAME);
     }
 
     private HTable getHTable(String hTableName) {
         try {
             return new HTable(getConfiguration(), hTableName);
         } catch (IOException e) {
             logger.error("Couldn't connect to HTable " + hTableName, e);
             throw new RuntimeException("Couldn't connect to HTable " + hTableName, e);
         }
     }
 
     private Configuration getConfiguration() {
         Configuration conf = HBaseConfiguration.create();
         //conf.set("hbase.zookeeper.quorum","ec2-23-22-125-138.compute-1.amazonaws.com");
         conf.set("hbase.master", HBASE_MASTER_DETAILS);
         return conf;
     }
 
     /*public void insert() {
         if(!initialized) {
             initStopWords();
         }
         InputStream inputStream = NamedEntityRecognizerServiceImpl.class.getClassLoader().getResourceAsStream("sample_file.txt");
         if(inputStream != null) {
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             String line;
             try {
                 Set<String> aggregateList = new HashSet<String>();
                 List<String> tempList = new ArrayList<String>();
                 while ((line = reader.readLine()) != null) {
                     if(!line.isEmpty()) {
                         String[] words = line.split("[ ,!]");
                         for(String word : words) {
                             word = word.trim();
                             if(!isStopWord(word)) {
                                 tempList.add(word);
                             } else {
                                 if(!tempList.isEmpty()) {
                                     aggregateList.addAll(getAggregateList(tempList));
                                     tempList.clear();
                                 }
                             }
                         }
                     }
                 }
                 int i=0;
                 List<Put> puts = new ArrayList<Put>();
                 Random random = new Random();
                 for(String word : aggregateList) {
                     Put put = new Put(word.getBytes());
                     put.add(COLUMN_FAMILY.getBytes(), COUNT_QUALIFIER.getBytes(), Bytes.toBytes(String.valueOf(random.nextInt(50))));
                     puts.add(put);
                     if(i++ >= 10) {
                         getWordCountHTable().put(puts);
                         puts.clear();
                         i=0;
                     }
 
                 }
             } catch (IOException e) {
                 logger.error("Couldn't read Stop Words File", e);
             } finally {
                 if(reader != null) {
                     try {
                         reader.close();
                     } catch (IOException e) {
                         logger.error("Failed to close the reader of sample file text", e);
                     }
                 }
             }
         }
     }*/
 }
