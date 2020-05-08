 package prj.httpparser.characterparse;
 
 import prj.httpparser.utils.EventSource;
 
 public class CharParser extends EventSource<CharListener>
 {
     boolean reset;
     int position;
     String current;
 
     public void parse(String input)
     {
         current = input;
         reset = false;
         for (int i = 0; i < input.length(); i++)
         {
             position = i;
             if (reset)
             {
                 break;
             }
             else
             {
                 char character = input.charAt(i);
                 CharType charType = getCharType(character);
                 fireCharacterFoundListener(charType, character, i);
             }
         }
     }
 
     public void reset()
     {
         current = null;
         reset = true;
     }
 
     public String remaining()
     {
        return current.substring(position + 1);
     }
 
     private CharType getCharType(char c)
     {
         switch (c)
         {
             case '\r' :
                 return CharType.CARRIAGE_RETURN;
             case ' ' :
                 return CharType.SPACE;
             case '\t' :
                 return CharType.HORIZONTAL_TAB;
             case '\n' :
                 return CharType.LINE_FEED;
             default :
                 return CharType.PRINTABLE;
         }
     }
 
     private void fireCharacterFoundListener(CharType type, char character, int position)
     {
         for (CharListener l : _listeners)
         {
             l.charFound(type, character, position);
         }
     }
 }
