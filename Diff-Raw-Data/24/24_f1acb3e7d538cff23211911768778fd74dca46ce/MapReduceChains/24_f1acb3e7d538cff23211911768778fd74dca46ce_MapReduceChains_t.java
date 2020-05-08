 package mapreduce;
 
 
 import com.google.common.base.Optional;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Multiset;
 import common.Util;
 import crosscuttings.AppConstants;
 import crosscuttings.CrosscuttingsException;
 import crosscuttings.jobs_processors.ImmutableJobsFormer;
 import crosscuttings.jobs_processors.ImmutableProcessorTargets;
 import com.google.common.base.Joiner;
 import com.google.common.io.Closer;
 import com.google.gson.Gson;
 import parsers.ImmutableBECParser;
 
 import java.io.BufferedWriter;
 import java.io.CharArrayReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.*;
 
 /** */
 public class MapReduceChains {
   private MapReduceChains() {}
 
   public static void main(String [] args) {
     MapReduceChains.runBECChain();
   }
 
   public static void runSentencesLevelProcess () {
     /*
     // Получаем работы
     List<List<String>> jobs = ImmutableJobsFormer.getJobs();
 
     // Map Stage
     List<List> result_map_stage = new ArrayList<List>();
     for (List<String> job : jobs) {
       result_map_stage.add(Mappers.mapper_sentences_level(job));
       //break;  // DEVELOP
     }
 
     // Shuffle Stage - сейчас фактически нет - один узел - один файл
     List<List> result_shuffle_stage  = result_map_stage;
 
     // Reduce Stage  - так же нет, т.к. - один узел - один файл
     Map<String, Map<String, String>> result_reduce_stage =
         new HashMap<String, Map<String, String>>();
     for (List task: result_shuffle_stage) {
       Map<String, String> one = Reduces.reduce_sentences_level(task);
 
       String node_name = (String)task.get(Mappers.IDX_NODE_NAME);
       result_reduce_stage.put(node_name, one);
     }
 
 
     // Save result
     try {
       Closer closer = Closer.create();
       try {
         Gson gson = new GsonBuilder().setPrettyPrinting().create();
         String resultInJson = gson.toJson(result_reduce_stage);
         // Write
         String path_for_save = Joiner.on(AppConstants.PATH_SPLITTER)
           .join(
             ImmutableProcessorTargets.getPathToIndex(),
             AppConstants.STATIC_NOTES_FILENAME);
 
         BufferedWriter out = closer.register(new BufferedWriter(new FileWriter(path_for_save)));
         out.write(resultInJson);
         Util.print(resultInJson);
 
         Util.print("Notes write to file : "+path_for_save);
 
       } catch (Throwable e) {
         closer.rethrow(e);
       } finally {
         closer.close();
       }
     } catch (IOException e) {
       e.printStackTrace();
     }
     Util.print("Done. Sentences level processing.");
     */
   }
 
 
   public void runWordLevelProcess () {
     Optional<List<List<String>>> jobs = Optional.absent();
     try {
       // Получаем работы
       jobs = Optional.of(ImmutableJobsFormer.getJobs());
 
     } catch (CrosscuttingsException e) {
       Util.print(e.getMessage());
 
     }
     // Map Stage
     List<List> resultMapStage = new ArrayList<List>();
     for (List<String> job : jobs.get()) {
       List one = Mappers.mapperWordLevel(job);
       resultMapStage.add(one);
       //break;  // DEVELOP
     }
 
     // Shuffle Stage - сейчас фактически нет - один узел - один файл
     List<List> resultShuffleStage  = resultMapStage;
 
     // Reduce Stage  - так же нет, т.к. - один узел - один файл
     List<List> resultReduceStage = new ArrayList<List>();
     for (List task: resultShuffleStage) {
       List one = Reduces.reduce_word_level_base(task);
       resultReduceStage.add(one);
     }
 
     // Save results
     for (final List reduceItem: resultReduceStage) {
       // TODO(zaqwes) TOTH: в защитной секции должно быть только то что нужно, или разное?
       // Сохраняем сортированные индекс
       List<String> sortedByFreqIdxForSave =
         (ArrayList<String>)reduceItem.get(Reduces.IDX_SORTED_IDX);
 
       // Может быть они тоже нормально сереализуются?
       // Вообще наверное лучше хранить в базе данных, а не в файлах.
       Multiset<String> frequencyIdx =
         (Multiset<String>)reduceItem.get(Mappers.IDX_FREQ_INDEX);
       Multimap<String, String> restWords =
         (Multimap<String, String>)reduceItem.get(Mappers.IDX_RESTS_MAP);
 
       // Это массив чисел!
       Map<String, Collection<Integer>> sentencesInvIdx =
         ((Multimap<String, Integer>)reduceItem.get(Mappers.IDX_SENT_MAP)).asMap();
 
       // Это другие представления индексов?
       Map<String, Integer> idxForSave = new HashMap<String, Integer>();
       Map<String, String> restIdxForSave = new HashMap<String, String>();
       Map<String, List<Integer>> sentencesIdxForSave = new HashMap<String, List<Integer>>();
 
       // Получение сохраняемых структур.
       for (final String word: sortedByFreqIdxForSave) {
         idxForSave.put(
             word, frequencyIdx.count(word));
         restIdxForSave.put(
             word, Joiner.on(" ").join(restWords.get(word)));
         sentencesIdxForSave.put(
             word, new ArrayList<Integer>(sentencesInvIdx.get(word)));
       }
 
       // Само сохранение
       try {
         Closer closer = Closer.create();
         try {
           String path_for_save_sorted_idx = Joiner.on(AppConstants.PATH_SPLITTER)
             .join(
               ImmutableProcessorTargets.getPathToIndex(),
               AppConstants.COMPRESSED_IDX_FOLDER,
               reduceItem.get(Mappers.IDX_NODE_NAME),
               AppConstants.SORTED_IDX_FILENAME);
           String path_for_save_freq_idx = Joiner.on(AppConstants.PATH_SPLITTER)
             .join(
               ImmutableProcessorTargets.getPathToIndex(),
               AppConstants.COMPRESSED_IDX_FOLDER,
               reduceItem.get(Mappers.IDX_NODE_NAME),
               AppConstants.FREQ_IDX_FILENAME);
           String path_for_save_rest_idx = Joiner.on(AppConstants.PATH_SPLITTER)
             .join(
               ImmutableProcessorTargets.getPathToIndex(),
               AppConstants.COMPRESSED_IDX_FOLDER,
               reduceItem.get(Mappers.IDX_NODE_NAME),
               AppConstants.FILENAME_REST_IDX);
           String path_for_save_sentences_idx = Joiner.on(AppConstants.PATH_SPLITTER)
             .join(
               ImmutableProcessorTargets.getPathToIndex(),
               AppConstants.COMPRESSED_IDX_FOLDER,
               reduceItem.get(Mappers.IDX_NODE_NAME),
               AppConstants.FILENAME_SENTENCES_IDX);
 
           // Сохраняем в JSON
           closer.register(new BufferedWriter(new FileWriter(path_for_save_sorted_idx)))
             .write(new Gson().toJson(sortedByFreqIdxForSave));
           closer.register(new BufferedWriter(new FileWriter(path_for_save_freq_idx)))
             .write(new Gson().toJson(idxForSave));
           closer.register(new BufferedWriter(new FileWriter(path_for_save_rest_idx)))
             .write(new Gson().toJson(restIdxForSave));
           closer.register(new BufferedWriter(new FileWriter(path_for_save_sentences_idx)))
             .write(new Gson().toJson(sentencesIdxForSave));
 
         } catch (CrosscuttingsException e) {
             Util.print(e.getMessage());
         } catch (Throwable e) {
           closer.rethrow(e);
         } finally {
           closer.close();
         }
       } catch (IOException e) {
         e.printStackTrace();
       }
     }  // for..
   }
 
   // Chain for BEC dictionary.
   public static void runBECChain() {
     // Как-то нужно правильно сопоставить слово и контент.
     try {
       ImmutableList<String> content =
           Util.file2list(
               Joiner.on(AppConstants.PATH_SPLITTER)
                   .join("raw-dicts", "vocabularity-folded.txt"));
       ImmutableBECParser cash = ImmutableBECParser.create(content);
 
       // Извлекаем данные и обрабатываем их
       List<String> dictWords = cash.getDict();
       Multimap<String, String> dictContent = cash.getContent();
       Multimap<String, String> dictTranslate = cash.getWordTranslates();

       Multimap<String, Integer> sentencesPtrs = HashMultimap.create();
       List<String> sentences = new ArrayList<String>();
 
       // Нуменация предложения с нуля!
       int idxSentence = 1;
       Set<String> keys = dictContent.keySet();
       for (final String key: keys) {
        // Значения не могут быть пустыми.
         Collection<String> value = dictContent.asMap().get(key);
         for (final String item: value) {
           sentences.add(item);
           sentencesPtrs.put(key, idxSentence);
           ++idxSentence;
         }
       }

      // Saver
      for (final String word: dictWords) {
        Util.print(sentencesPtrs.get(word));
      }
      //String pathToDefaultNode = Joiner.on()



     } catch (IOException e) {
       Util.print(e.getMessage());
     } catch (IllegalStateException e) {
       Util.print(e.getMessage());
     }
   }
 }
