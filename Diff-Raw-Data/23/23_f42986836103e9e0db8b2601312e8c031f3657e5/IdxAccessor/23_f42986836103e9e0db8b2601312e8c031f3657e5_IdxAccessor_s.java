 package idx_coursors;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.io.Closer;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.reflect.TypeToken;
 import common.Util;
 import crosscuttings.AppConstants;
 import crosscuttings.CrosscuttingsException;
 import crosscuttings.jobs_processors.ProcessorTargets;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 // Вообще это геттер-сеттер к базе данных. Это синглетон.
 //
 // И он должен быть многопоточным. И еще желательно межпроцессозащищенным.
 //
 // @NoThreadSafe
 //   Доступ к файлу. Хотя возможно просто будет занят при открытии.
 public class IdxAccessor {
  /*
   // Получаем пересечение индексов
   static void get_confluence_idx() {
     Multiset<String> confluence_idx = HashMultiset.create();
     List<String> nodes = ImmutableBaseCoursor.getListNodes();
 
     Map<String, Integer> one_freq_idx =  get_freq_idx(nodes.get(0));  // можно любой
     for (Map.Entry<String, Integer> pair:
         one_freq_idx.entrySet()) {
       String word = pair.getKey();
       // Ищем слово в индексах
       Boolean occure = new Boolean(true);
       Integer summary_frequency = 0;
       for (String node: nodes) {
         // Учитываются все узлы, а первый только для получения списка ключей
         Map<String, Integer> tmp_freq_idx =  get_freq_idx(node);
         if (!tmp_freq_idx.containsKey(word)) {
           occure = false;
           break;
         }
         summary_frequency += pair.getValue();
       }
       // Если нашелся ключ
       if (occure) {
         utils.print(word+", "+summary_frequency);
         confluence_idx.add(word, summary_frequency);
       }
     }
   }
 
   // Получить индекс со словами оставшимися после сжатия
   static public HashMap<String, String> get_rest_idx(String node) {
     String sorted_freq_idx_json = utils.file2string(
       Joiner.on(AppConstants.PATH_SPLITTER)
         .join(
           ImmutableProcessorTargets.getPathToIndex(),
           AppConstants.COMPRESSED_IDX_FOLDER,
           node,
           AppConstants.FILENAME_REST_IDX));
     return (new Gson().fromJson(sorted_freq_idx_json,
       new TypeToken<HashMap<String, String>>() {}.getType()));
   }
 
  // Получить список единиц контанта узла
  static public List<String> get_list_sentences(String node) {
    return utils.file2list(Joiner.on(AppConstants.PATH_SPLITTER)
      .join(
        ImmutableProcessorTargets.getPathToIndex(),
        AppConstants.CONTENT_FOLDER,
        node,
        AppConstants.CONTENT_FILENAME
      ));
  }
   */
 
   /*
   // Получить список указателей на предложеия в которых встречалось слово.
   static public HashMap<String, List<Integer>> get_sentences_idx(String node) {
     String sorted_freq_idx_json = Util.fileToString(
       Joiner.on(AppConstants.PATH_SPLITTER)
         .join(
           ProcessorTargets.getPathToIndex(),
           AppConstants.COMPRESSED_IDX_FOLDER,
           node,
           AppConstants.FILENAME_SENTENCES_IDX));
     return (new Gson().fromJson(sorted_freq_idx_json,
       new TypeToken<HashMap<String, List<Integer>>>() {}.getType()));
   } */
 
 
 
 
   /*
   static public Optional<HashMap<String, Integer>> getFreqIdx(String node) {
     try {
       String sorted_freq_idx_json = Util.file2string(
         Joiner.on(AppConstants.PATH_SPLITTER)
           .join(
             ProcessorTargets.getPathToIndex(),
             AppConstants.COMPRESSED_IDX_FOLDER,
             node,
             AppConstants.FREQ_IDX_FILENAME));
       HashMap<String, Integer> freqIdx = (new Gson().fromJson(sorted_freq_idx_json,
           new TypeToken<HashMap<String, Integer>>() {}.getType()));
       return Optional.of(freqIdx);
     } catch (CrosscuttingsException e) {
       return Optional.absent();
     }
   }
 
   static public Optional<ImmutableList<String>> getSortedIdx(String node) {
     try {
       String sortedIdxJson = Util.file2string(
         Joiner.on(AppConstants.PATH_SPLITTER)
           .join(
             ProcessorTargets.getPathToIndex(),
             AppConstants.COMPRESSED_IDX_FOLDER,
             node,
             AppConstants.SORTED_IDX_FILENAME));
 
       List<String> sortedIdxCash = (new Gson().fromJson(sortedIdxJson,
         new TypeToken<ArrayList<String>>() {}.getType()));
       return Optional.of(ImmutableList.copyOf(sortedIdxCash));
     } catch (CrosscuttingsException e) {
       return Optional.absent();
     }
   }
     */
   static public synchronized Optional<ImmutableList<String>> getSortedIdx(String filename) {
     try {
 
       // !Критическая секция. Возможно, если кто-то другой его открыл, то он будет занят, но
       //   Возможно критическая секция упрощает доступ к файлу из разных потоков.
       String sortedIdxJson = Util.fileToString(filename).get();
       // !Критическая секция
 
       List<String> sortedIdxCash = (new Gson().fromJson(sortedIdxJson,
         new TypeToken<ArrayList<String>>() {}.getType()));
       return Optional.of(ImmutableList.copyOf(sortedIdxCash));
     } catch (IOException e) {
       return Optional.absent();
     }
   }
 
   static public synchronized Optional<ImmutableMap<String, List<Integer>>> getSentencesKeys(String filename) {
     try {
       // !Критическая секция. Возможно, если кто-то другой его открыл, то он будет занят, но
       //   Возможно критическая секция упрощает доступ к файлу из разных потоков.
       String sortedIdxJson = Util.fileToString(filename).get();
       // !Критическая секция
 
       Map<String, List<Integer>> sortedIdxCash = (new Gson().fromJson(sortedIdxJson,
         new TypeToken<HashMap<String, List<Integer>>>() {}.getType()));
       return Optional.of(ImmutableMap.copyOf(sortedIdxCash));
     } catch (IOException e) {
       return Optional.absent();
     }
   }
 
   /*
   static public HashMap<String, HashMap<String, String>>  get_static_notes() {
     String metadata_static_notes_json = utils.file2string(
       Joiner.on(AppConstants.PATH_SPLITTER)
         .join(
           ImmutableProcessorTargets.getPathToIndex(),
           AppConstants.STATIC_NOTES_FILENAME));
     return (new Gson().fromJson(metadata_static_notes_json,
       new TypeToken<HashMap<String, HashMap<String, String>>>() {}.getType()));
   }
 
   static List<String> get_base_filtered_sorted_idx(String node) {
     List<String> sorted_idx = get_sorted_idx(node);
 
     List<String> filtered = new ArrayList<String>();
     for (String stem : sorted_idx) {
       if (ImmutableBaseFilter.isContentStem(stem)) {
         filtered.add(stem);
       }
     }
     return filtered;
   }
 
   static public void main(String[] args) {
     List<String> nodes = ImmutableBaseCoursor.getListNodes();
     String node = nodes.get(0);  // пока один
 
     List<String> filtered_sorted_idx = get_base_filtered_sorted_idx(node);
     Map<String, Integer> freq_idx = get_freq_idx(node);
     for (String stem: filtered_sorted_idx) {
       utils.print(Joiner.on(",").join(stem, freq_idx.get(stem)));
     }
 
     utils.print(get_sorted_idx(node).size());
     utils.print(filtered_sorted_idx.size());
     //ImmutableIdxGetters.get_coupled_idx_for_node(node, nodes.subList(1, nodes.size()));
     //ImmutableIdxGetters.get_follow_data(node, nodes);//.subList(1, nodes.size()));
   } */
 
   // K - path to file
   // V - Объект годны для сереализации через Gson - базовая структура - Map, List.
   public static void saveListObjects(Map<String, Object> data) throws IOException {
     // Само сохранение. Вряд ли удасться выделить в метод. И свернуть в цикл.
     Closer closer = Closer.create();
     try {
       Gson gson = new GsonBuilder().setPrettyPrinting().create();
       for (final String key: data.keySet()) {
         closer.register(new BufferedWriter(new FileWriter(key))).write(gson.toJson(data.get(key)));
       }
     } catch (Throwable e) {
       closer.rethrow(e);
     } finally {
       closer.close();
     }
   }
 }
