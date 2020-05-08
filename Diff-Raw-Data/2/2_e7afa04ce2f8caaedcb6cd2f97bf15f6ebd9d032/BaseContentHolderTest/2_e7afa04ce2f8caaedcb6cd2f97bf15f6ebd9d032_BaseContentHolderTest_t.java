 package idx_coursors;
 
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import org.junit.Test;
 
 import java.util.*;
 import java.util.concurrent.ExecutionException;
 
 class InMemorySentencesAccessor implements ContentStorageAccessor {
   private final ImmutableList<String> CONTENT;
   public InMemorySentencesAccessor(ImmutableList<String> content) {
     CONTENT = content;
   }
 
   @Override
   public String getSentence(Integer key) {
     return CONTENT.get(key);
   }
 }
 
 class ExtractSentenceException extends RuntimeException {
   public ExtractSentenceException(Throwable e) {super(e);}
 }
 
 class CashedContentAccessor implements ContentStorageAccessor {
   private String getSentenceFromFile(Integer key) {
     return "Sent";
   }
 
   public CashedContentAccessor () {
     GRAPHS = CacheBuilder.newBuilder()
       .maximumSize(1000)
       .build(
         new CacheLoader<Integer, String>() {
           @Override
          public String load(Integer key) /* Что-то нужно выкинуть */ {
             return getSentenceFromFile(key);
           }
         });
   }
   @Override
   public String getSentence(Integer key) {
     try {
       return GRAPHS.get(key);
     } catch (ExecutionException e) {
       throw new ExtractSentenceException(e.getCause());
     }
   }
 
   private final LoadingCache<Integer, String> GRAPHS;
 }
 
 public class BaseContentHolderTest {
   @Test
   public void testGetContentItem() throws Exception {
     ImmutableMap<String, List<Integer>> keys = ImmutableMap.of(
       "hello", Arrays.asList(1, 2), "hay", Arrays.asList(1));
 
     // Порядок предложений важен! Поэтому список с сохранением порядка
     // Коллекции Guava хранят порядок, но для сипска рекомендуюется использовать
     //   JDK версию данной коллекции.
     ImmutableList<String> sentences = ImmutableList.of("hello hay", "hay");
 
     // Плохо передавать предложения, лучше передать кэш. Предложений может быть много
     ContentStorageAccessor accessor = new InMemorySentencesAccessor(sentences);
     ContentHolder contentHolder = new BaseContentHolder(keys, accessor);
   }
 }
