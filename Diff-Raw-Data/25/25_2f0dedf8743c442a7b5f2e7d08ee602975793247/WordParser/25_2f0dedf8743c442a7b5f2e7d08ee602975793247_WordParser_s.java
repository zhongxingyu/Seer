 package prj.httpparser.wordparser;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import prj.httpparser.characterparse.CharListener;
 import prj.httpparser.characterparse.CharParser;
 import prj.httpparser.characterparse.CharType;
 import prj.httpparser.utils.EventSource;
 
 public class WordParser extends EventSource<WordListener> implements CharListener
 {
     private CharParser _charParser;
     private StringBuilder _stringBuilder;
     private Logger _logger;
 
     public WordParser(CharParser charParser)
     {
         _logger = LoggerFactory.getLogger(WordParser.class);
         _charParser = charParser;
         _charParser.addListener(this);
         _stringBuilder = new StringBuilder();
     }
 
     public void parse(String inputString)
     {
         _charParser.parse(inputString);
     }
 
     public void reset()
     {
         resetStringBuilder();
         _charParser.reset();
     }
 
     public String remaining()
     {
         return _charParser.remaining();
     }
 
     private void resetStringBuilder()
     {
         _stringBuilder = new StringBuilder();
     }
 
     @Override
     public void charFound(CharType type, char character, int position)
     {
 //        System.out.println(type + " " + character);
         switch (type)
         {
             case PRINTABLE:
                 _stringBuilder.append(character);
                 break;
             case CARRIAGE_RETURN:
                 if (_stringBuilder.length() > 0)
                 {
                     fireWordParsed(new Word(Word.WordType.WORD, _stringBuilder.toString()));
                     resetStringBuilder();
                 }
                 _stringBuilder.append(character);
                 break;
             case LINE_FEED:
                 if (_stringBuilder.length() == 1 && _stringBuilder.charAt(0) == '\r')
                 {
                     fireWordParsed(new Word(Word.WordType.CRLF, null));
                     resetStringBuilder();
                 }
                 else
                 {
                     _logger.warn("line feed only expected after a carriage return");
                     fireErrorEvent(_charParser.current());
                     resetStringBuilder();
                 }
                 break;
             case SPACE:
             case HORIZONTAL_TAB:
                 if (_stringBuilder.length() != 0)
                 {
                     fireWordParsed(new Word(Word.WordType.WORD, _stringBuilder.toString()));
                     resetStringBuilder();
                 }
                 break;
         }
     }
 
     private void fireWordParsed(Word word)
     {
         for (WordListener l : _listeners)
         {
             l.onWordArrived(word);
         }
     }
 
     private void fireErrorEvent(String requestString)
     {
         for (WordListener l : _listeners)
         {
             l.onParsingError(requestString);
         }
     }
 }
