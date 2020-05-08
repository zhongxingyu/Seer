 package ro.koch.resourcefacades.facades;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import ro.koch.resourcefacades.TitleAndSummary;
 
 public class TextTitleAndSummary implements TitleAndSummary {
     private final String text;
    private final static int TITLE_LENGTH = 90;
 
     public TextTitleAndSummary(String text) {
         this.text = checkNotNull(text);
     }
 
     @Override public String getTitle() {
        int newLinePos = text.indexOf("\n");
        String firstLine = newLinePos == -1 ? text : text.substring(0, newLinePos);

        return firstLine.length() <= TITLE_LENGTH ? firstLine
               : firstLine.substring(0, TITLE_LENGTH - 4) + " ...";
     }
 
     @Override public String getSummary() {
         return text;
     }
 }
