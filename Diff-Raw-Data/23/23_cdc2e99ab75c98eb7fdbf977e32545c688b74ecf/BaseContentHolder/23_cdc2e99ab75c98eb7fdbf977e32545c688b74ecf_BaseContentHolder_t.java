 package idx_coursors;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class BaseContentHolder implements ContentHolder {
   public BaseContentHolder(
      ImmutableMap<String, List<Integer>> keys, ContentStorageAccessor accessor)
     {
    ACCESSOR = accessor;
     CASH_SENTENCES_KEYS_IDX = keys;
   }
   @Override
   public ImmutableList<String> getContentItem(String word) {
     // Т.к. ключит и предложения - это входные данные, то
     //   нужно
     // Порядок предложений важен
     List<String> sentences = new ArrayList<String>();
     if (CASH_SENTENCES_KEYS_IDX.containsKey(word)) {
       List<Integer> tmp = CASH_SENTENCES_KEYS_IDX.get(word);
       ImmutableList<Integer> pointers = ImmutableList.copyOf(tmp);
       for (final Integer ptr: pointers) {
        sentences.add(ACCESSOR.getSentence(ptr-1));
       }
     } else {
     }
     return ImmutableList.copyOf(sentences);
   }
 
  private final ContentStorageAccessor ACCESSOR;
   private final ImmutableMap<String, List<Integer>> CASH_SENTENCES_KEYS_IDX;
 }
