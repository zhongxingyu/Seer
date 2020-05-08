 package parsers;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Splitter;
 import com.google.common.collect.*;
 import common.Util;
 import crosscuttings.AppConstants;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 
 // Наверное случайный выборщик будет оборачивать это класс, а не будет встроенным в него.
 // Если его использовать статическим, то его нельзя будет передать!
 //
 // Option - что-то вроде альтернативы обработки ошибок, но она не гибкая, не дает никакой информации.
 //   зато дает ответ - Да(результат)/Нет(ничего)
 //
 // Возможно на начальных стадиях финальными классы лучше не делать.
 //   http://www.ibm.com/developerworks/ru/library/j-jtp1029/
 //
 // Наверное лучше сделать интерфейс к этому тип. Ну нужно понять, что за API реально нужны.
 // Или сделать private classes и генерировать статическим методом. С одним интерфейсом.
 //   Может не суперудобно, но по крайней мере не нужно создавать кучу классов.
 //   Но в любом случае нужно понять что за API нужно. Еще возможно понадобится снимать immutable.
 //     чтобы можно было редактировать базу. Но как быть с скидвание кэша. Деструктров нет.
 //     Вообще почитать про кэши из Guava. А многопользовательность? Может отдельный класс сделать,
 //       а данные сливать при запросах. Т.е. исходные данные константны, а добавочные нет. В последствие
 //       возможно можно переводить из неконстантных в константные.
//
// Immutable не обязательно должен быть final, у него может быть закрыт конструктор.
//   Просто не всегда хорошо делать сам класс финальным.
 public final class ImmutableBECParser {
   // TODO(zaqwes) TOTH: Кажется синхронизация не нужна.
   public static ImmutableBECParser create(ImmutableList<String> fileContent) {
      return new ImmutableBECParser(fileContent);
   }
 
   // Данные для парсера передаем извне. Чтение внутри конструктора сомнительно очень.
   private ImmutableBECParser(ImmutableList<String> fileContent) {
 
     // Какие исключения могут генерировать списки, мапы,...
     CONTENT = ImmutableList.copyOf(fileContent);
 
     List<String> cashWords = new ArrayList<String>();
     Multimap<String, String> cashTranslate = HashMultimap.create();
     Multimap<String, String> cashContent = HashMultimap.create();
 
     for (final String record: CONTENT) {
       List<String> parsedLine = Lists.newArrayList(Splitter.on(SPLITTER).split(record));
       if (!parsedLine.isEmpty()) {
         String word = parsedLine.get(KEY_POS);
         cashWords.add(word);
         cashTranslate.put(word, FAKE_TRANSLATE);
         cashContent.putAll(word, parsedLine.subList(KEY_POS, parsedLine.size()));
       }
     }
 
     WORDS_TRANSLATES = ImmutableMultimap.copyOf(cashTranslate);
     SORTED_WORDS_ALPH = ImmutableList.copyOf(cashWords);
     WORDS_CONTENT = ImmutableMultimap.copyOf(cashContent);
     COUNT_WORDS = SORTED_WORDS_ALPH.size();
   }
 
   // Получить слово по индексу. Нужно для генератора случайных чисел.
   // 0 - CountWords
   //
   // Если использовать Optional, то нужно где-то все равно переходить к исключениям.
   public String getWordByIdx(Integer idx) throws VParserException {
     // Проверяем границы.
     // Вычитать 1 нужно, так как нумерация с нуля.
     if (!(idx < 0 || idx > COUNT_WORDS-1)) {
        return SORTED_WORDS_ALPH.get(idx);
     } else {
       throw new VParserException("Out of range.");
     }
   }
 
   private final Integer KEY_POS = 0;
   private final String SPLITTER = "@";
   private final String FAKE_TRANSLATE = "No";
 
   // Допустим испльзуем не исключение при чтении файла, возвращаем пустой список.
   // Если коллекция пустая, то возможно было пустой файл, а так сразу ясно, что что-то не так
   //   но появляние ошибки растянуто в времени.
   private final ImmutableList<String> CONTENT;
   private final Integer COUNT_WORDS;  // Нужно будет для генератора случайных числел
   //   но хранится будет здесь.
   // Индексты тоже сделать такими же
   private final ImmutableList<String> SORTED_WORDS_ALPH;
   private final ImmutableMultimap<String, String> WORDS_TRANSLATES;
   private final ImmutableMultimap<String, String> WORDS_CONTENT;
 
   public static void main(String[] args) {
     try {
       String fullFilename = Joiner.on(AppConstants.PATH_SPLITTER).join("statistic-data", "vocabularity-folded.txt");
       ImmutableList<String> content = Util.file2list(fullFilename);
       ImmutableBECParser cash = ImmutableBECParser.create(content);
 
     } catch (IOException e) {
       Util.print(e.getMessage());
     //} catch (VParserException e) {
     //  Utils.print(e.getMessage());
     } catch (IllegalStateException e) {
       Util.print(e.getMessage());
     }
   }
 }
